package de.uni_mannheim.informatik.dws.ds4dm.CreateLuceneIndex;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.apache.commons.io.FilenameUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TableHeaderDetectorContentBased;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TableKeyIdentification;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.CsvTableParser;


/**
 * This is the Main Class of the DS4DM Preprocessing Component called "CreateLuceneIndex".
 * 
 * About DS4DM:
 * DS4DM stands for 'Data Search for Data Mining'. It is a extension to the RapidMiner software.
 * This extension allows users to extend a data table with additional attributes(=columns) of their choice.
 * E.g. if you have a table with company information uploaded in RapidMiner, you can use the DS4DM-RapidMiner-operators for getting the additional column 'Headquarter location' - ready populated with the correct values.
 * 
 * The DS4DM Webservice:
 * The table extension operator of the DS4DM-RapiMiner extension uses a custom-made webservice. 
 * This webservice hosts a large corpus of datatables. Whenever a table needs to be extended, the webservice looks for the right tables (in our example: tables with company headquarter information) 
 * and returns these tables to the RapidMiner operator.
 * 
 * Reason for the Indexes:
 * The DS4DM Webservice has to be fast at identifying the correct tables to return to the DS4DM RapidMiner operator.
 * To achieve this speed despite the large corpus of data tables it has to search through, Indexes are needed.
 * In total 3 lucene indexes are used; they contain information about the data tables in the webservice's corpus.
 * 
 * 
 *  ***********************************************************************************************************
 *  *                      This Software component creates these 3 lucene indexes                             *
 *  *  You run it before you start the webservice, or whenever you would like to use a new corpus of tables.  *
 *  *                      This is why it is called a 'Preprocessing Component'.                              *
 *  ***********************************************************************************************************
 *
 *
 * The 3 Indexes are: 
 * 
 *   * ColumnNameIndex
 *     This index has an entry for each column of each table (in the corpus)
 *     In every entry following information is saved: tableHeader (=table name), value (=column name), columnDataType, tableCardinality, columnDistinctValues, columnindex, columnOriginalHeader, fullTablePath (=folder in which the original table is located)
 *     
 *   * TableIndex
 *     This index has an entry for each distinct value in each column of each table
 *     In every entry following information is saved: id (=the distinct-value-index for each column), tableHeader, columnHeader, columnDataType, tableCardinality, columnDistinctValues, valueMultiplicity (=how often the distinct value appears in this column), value (=the distinct value), fullTablePath, isPrimaryKey (=true if the column is the PK), originalValue
 *      
 *   * KeyColumnIndex
 *     This index has an entry for each table
 *     In every entry following information is saved: tableHeader, columnHeader, keyColumnString (= a list of all the values in the table's key column concatenated into one long string), keyColumnIndex 
 *     Unlike the other two, this index is not used by the DS4DM webservice. It is used by another Preprocessing component: CreateCorrespondenceFiles
 *     
 *     
 *  Operation:
 *  This java program can be executed by...
 *    * calling its .jar-file via the commandline/terminal.  
 *      In this case the command should look like this:  java -jar CreateLuceneIndex-0.0.1-SNAPSHOT-jar-with-dependencies.jar "path/to/the/csvTables/you'd/like/to/have/indexed"  "path/to/where/the/indexes/will/be/saved"
 *      
 *    * running the main-Method of the Main-Class in an java environment with the same two parameters
 * 
 * 
 * @author Benedikt Kleppmann
 *
 */
public class Main {
	
	
	
	
	
	
	/**
	 * The main Method
	 * 
	 * This is where the program starts.
	 * When calling it two parameters must be passed to it:  datafilePath (=path/to/the/folder/where/the/to-be-indexed/csv-files/are) and indexFolderPath (=path/to/the/folder/where/the/indexes/should/be/saved)
	 * If these parameters aren't passed. The program ends with an error message.
	 * 
	 * the steps in this function are:
	 * 1. open the indexes
	 * 2. loop through the csv-files and call the indexing-method
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException{
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println("Starting CreateLuceneIndex");
		System.out.println(dateFormat.format(new Date())); //2016/11/16 12:08:43
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		
		if (args.length!=3){
			System.out.println("====================================================================================================");
			System.out.println("|  Error: the wrong number of Parameters have been passed to this function.                        |");
			System.out.println("|  2 or 3 Parameters have to be passfed:                                                           |");
			System.out.println("|     1. datafilePath  (=path/to/the/folder/where/the/to-be-indexed/csv-files/are)                 |");
			System.out.println("|     2. indexFolderPath   (=path/to/the/folder/where/the/indexes/should/be/saved)                 |");
			System.out.println("|     optional: 3. keyColumnsCSVPath   (=path/to/the/file/with/keycolumnindexes/for/every/table)   |");
			System.out.println("====================================================================================================");
		} else {
			
			String datafilePath = args[0];
			String indexFolderPath = args[1];
			String keyColumnsCSVPath = null;
			if (args.length ==3) {
				if (args[2].length() > 0) keyColumnsCSVPath = args[2];
			}
	
			
	
			// OPEN INDEXES ============================================
			//if the last character of the indexFolderPath is '/', remove it
			System.out.println("CreateLuceneIndex 1");
			if (indexFolderPath.substring(indexFolderPath.length() - 1)=="/") {
				indexFolderPath = indexFolderPath.substring(0, indexFolderPath.length() - 1);
			}
			
			//index config
	    	Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
	    	TableIndexer tableIndexer = new TableIndexer();
	
			// get the keyColumnIndexWriter and save it in the TableIndexer
	    	System.out.println("CreateLuceneIndex 2");
	    	IndexWriterConfig config1 = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
			String indexPath1 = indexFolderPath + "/KeyColumnIndex/";
		    Directory directory1 = FSDirectory.open(new File(indexPath1));
	    	IndexWriter keyColumnIndexWriter = new IndexWriter(directory1, config1);
	    	tableIndexer.setKeyColumnIndexWriter(keyColumnIndexWriter);
	    	
			// get the ColumnNameIndex and save it in the TableIndexer
	    	System.out.println("CreateLuceneIndex 3");
	    	IndexWriterConfig config2 = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
			String indexPath2 = indexFolderPath + "/ColumnNameIndex/";
		    Directory directory2 = FSDirectory.open(new File(indexPath2));
	    	IndexWriter columnNameIndexWriter = new IndexWriter(directory2, config2);
	    	tableIndexer.setColumnNameIndexWriter(columnNameIndexWriter);
	    	
			// get the TableIndex and save it in the TableIndexer
	    	System.out.println("CreateLuceneIndex 4");
	    	IndexWriterConfig config3 = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
			String indexPath3 = indexFolderPath + "/TableIndex/";
		    Directory directory3 = FSDirectory.open(new File(indexPath3));
	    	IndexWriter tableIndexWriter = new IndexWriter(directory3, config3);
	    	tableIndexer.setTableIndexWriter(tableIndexWriter);
	    	
	    	
	    	

	    	// LOOP THROUGH FILES IN datafilePath =======================================================================
	    	System.out.println("CreateLuceneIndex 5");
			File dir = new File(datafilePath);
			File[] dataFiles = dir.listFiles();
		
	    	for (File dataFile: dataFiles){
	    		System.out.println("Indexing table " + dataFile.getName());
	    		
	    		if (FilenameUtils.getExtension(dataFile.getName()).equals("csv"))
	    		{
	    			System.out.println(dataFile.getName() + "===============================================================================");
	    			try{
	    				tableIndexer.writeTableToIndexes(dataFile, keyColumnsCSVPath);
	    			} catch (Exception e){System.out.println("An Exception occured in tableIndexer.writeTableToIndexes("+ dataFile + "," + keyColumnsCSVPath + ")");}
	
	    		}
	    	}
	    	
	    	// CLOSE INDEXES ============================================
	    	keyColumnIndexWriter.close();
	    	columnNameIndexWriter.close();
	    	tableIndexWriter.close();
		}
		
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println("End-of CreateLuceneIndex");
		System.out.println(dateFormat.format(new Date())); //2016/11/16 12:08:43
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	}
	
	


	
}

