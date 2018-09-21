package de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.joda.time.DateTime;

import com.opencsv.CSVWriter;

import de.uni_mannheim.informatik.additionalWinterClasses.MatchableTableColumn;
import de.uni_mannheim.informatik.additionalWinterClasses.MatchableTableRow;
import de.uni_mannheim.informatik.additionalWinterClasses.WebTableDataSetLoader;
import de.uni_mannheim.informatik.dws.ds4dm.customMatchingRules.KeyColumnComparatorJaccard;
import de.uni_mannheim.informatik.dws.ds4dm.customMatchingRules.NonKeyColumnComparator_withDType;
import de.uni_mannheim.informatik.dws.ds4dm.customSimilarity.InverseExponentialDateSimilarity;
import de.uni_mannheim.informatik.dws.ds4dm.customSimilarity.NumericDeviationSimilarity;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEngine;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.matching.aggregators.VotingAggregator;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.NoSchemaBlocker;
import de.uni_mannheim.informatik.dws.winter.matching.rules.LinearCombinationMatchingRule;
import de.uni_mannheim.informatik.dws.winter.matching.rules.VotingMatchingRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.similarity.date.NormalisedDateSimilarity;
import de.uni_mannheim.informatik.dws.winter.similarity.numeric.DeviationSimilarity;
import de.uni_mannheim.informatik.dws.winter.similarity.string.TokenizingJaccardSimilarity;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.WebTablesStringNormalizer;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.CsvTableParser;
import de.uni_mannheim.informatik.dws.winter.matching.blockers.NoBlocker;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;

/**
 * 
 *  ****************************************************************************************************************************
 *  *            This is the Main Class of the DS4DM Preprocessing Component called "CreateCorrespondenceFiles".               *
 *  *  This Software component finds the schema- and instance- correspondences between the tables in the webservice's corpus   *
 *  ****************************************************************************************************************************
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
 * The Search function:
 * The webservice offers two table-search functions: search and extendedSearch.
 * The search function will only find and return tables that have a column with a name similar to the name of the desired extension attribute.
 * In our example: only tables will be found that have a columnname similar to 'Headquarter location'. There might however be tables where this column exists, but has a quite different name e.g 'HQ city'. 
 * In the standard search function these tables would not be found.
 *                 
 * The ExtendedSearch function               
 * Therefore the webservice also offers the extendedSearch.
 * Here, there is a first step in which tables are searched for in the same way as in the standard search function - using the similarity of columnnames.
 * There is however a second step in which the list of found tables is extended with additional tables that have the correct column, the column however has an unpractical name.
 * This second step (the extension-step) uses pre-calculated correspondences between the tables in the webservice's table corpus.
 * 
 * Schema-Correspondences
 * A schema-correspondence marks two columns from two different tables as containing the same attribute e.g. column1 from table1 and column4 from table3 both contain company sizes.
 * The pre-calculated schema-correspondences between tables in the corpus, allow us - knowing the 'Headquarter location'-columns found in the standard search - to find further, less obvious 'Headquarter location'-columns in other tables.
 * These additional tables, which we found via the schema-correspondences are then added to our list of found tables.
 * 
 *   
 *  *************************************************************************************************************************************
 *  *      This Software component finds the schema- and instance- correspondences between the tables in the webservice's corpus        *
 *  *             You run it before you start the webservice, or whenever you would like to use a new corpus of tables.                 *
 *  *                                      This is why it is called a 'Preprocessing Component'.                                        *
 *  *************************************************************************************************************************************
 * 
 * 
 * Instance-Correspondences
 * We mentioned, that instance correspondences are also created by this software component.
 * An instance-correspondence marks two rows from two different tables as referring to the same real-world-object e.g. row2 from table1 and row11 from table3 both contain information about the company Tesla.
 * The webservice also needs these instance-correspondences to help add the 'Headquarter location'-information to the right company.
 *  
 *     
 *  Operation:
 *  This java program can be executed by...
 *    * calling its .jar-file via the commandline/terminal.  
 *      In this case the command should look like this:  java -jar CreateCorrespondenceFiles-0.0.1-SNAPSHOT-jar-with-dependencies.jar "path/to/where/the/indexes/are/saved" "path/to/the/csvTables/constituting/the/webservice's/corpus" "path/to/where/the/correspondences/will/be/saved" "Integer blockSize. 5 is a good default value"
 *      
 *    * running the main-Method of the Main-Class in an java environment with the same four parameters
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
	 * When calling it four parameters must be passed to it:  indexFolderPath (=path/to/where/the/indexes/are/saved), datafilePath (=path/to/the/csvTables/constituting/the/webservice's/corpus), correspondenceFolderPath (=path/to/where/the/correspondences/will/be/saved), blockSize ("5" is a good value to choose for this integer)
	 * If these parameters aren't passed. The program ends with an error message.
	 * 
	 * the steps in this function are:
	 * 1. open the KeyColumnIndex
	 * 2. create the hashmap 'blockedTableCombinations'
	 *    Looking for correspondences between each of the NxN table-combinations is too computationally expensive. Therefore we first create a list of likely corresponding table pairs - the blockedTableCombinations.
	 *    2.1. the KeyColumnIndex contains the key column of each table in the webservice's corpus. In turn we retrieve each of these key columns
	 *    		2.1.1. for we use the column values from retrieved key column to search in the index for 5 the tables with the most similar column values - the blocking partners for the originally retrieved key column.
	 *    		2.1.2. we save the so found table-pairs in the blockedTableCombinations-hashmap in such a way that each table-pair appears only once.
	 * 3. loop through the table pairs saved in the blockedTableCombinations-hashmap
	 * 	  3.1. for each table pair: find the instance matches
	 *    3.2. pass the instance matches on to the duplicateBasedSchemaMatching-method, to also find the schema-matches
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{

		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println("Starting Create Correspondence Files");
		System.out.println(dateFormat.format(new Date())); //2016/11/16 12:08:43
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		
		
		if (args.length!=4){
			System.out.println("=======================================================================================");
			System.out.println("|  Error: the wrong number of Parameters have been passed to this function.           |");
			System.out.println("|  4 Parameters have to be passed:                                                    |");
			System.out.println("|     1. indexFolderPath  (=path/to/where/the/indexes/are/saved)                      |");
			System.out.println("|     2. datafilePath  (=path/to/the/csvTables/constituting/the/webservice's/corpus)  |");
			System.out.println("|     3. correspondenceFolderPath  (=path/to/where/the/correspondences/will/be/saved) |");
			System.out.println("|     4. blockSize   (\"5\" is a good value to choose for this integer)               |");
			System.out.println("=======================================================================================");
		} else {
			
			
			// PARAMETERS   ===========================================================================================
			// Command-line Parameters
			String indexFolderPath = args[0];
			String datafilePath = args[1];
			String correspondenceFolderPath = args[2];
			String blockSize = args[3];
			Integer numResults = Integer.valueOf(blockSize);
			
//			String indexFolderPath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-08-04 evaluate indexing of wikitables/indexFolder" ;
//			String datafilePath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/temporary files/2017-08-04/tables" ;
//			String correspondenceFolderPath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-08-04 evaluate indexing of wikitables/correspondenceFiles";
//			Integer numResults = 5;
			
			
			
			
			// more Parameters
			int maxEditDistance = 3;
			

			
			

			
			
			
			// CODE         ========================================================================
			
			// open index
			Directory directory = FSDirectory.open(new File(indexFolderPath + "/KeyColumnIndex/"));
	        DirectoryReader indexReader = DirectoryReader.open(directory);
	        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
	        System.out.println("Length = " + String.valueOf(indexReader.maxDoc()));
	        
	      
		     
		    // Make HashMap of blocked Tables ===================================================== 
	        // loop through all tables in the index, to create blockedTableCombinations
	        System.out.println("determining blocked tables... [" + dateFormat.format(new Date()) + "]");
	        HashMap<String,HashSet<String>> blockedTableCombinations = new HashMap<String,HashSet<String>>();
	        
	        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//	        CSVWriter writer = new CSVWriter(new FileWriter("/home/bkleppma/ds4dm_webservice/DS4DM_experimental/DS4DM/DS4DM_webservice/public/exampleData/yourfile2.csv"), '\t');
//	        String[] entries = { "test1: " + String.valueOf(indexReader.maxDoc())};
//	        writer.writeNext(entries);
	        CSVWriter writer = new CSVWriter(new FileWriter("/home/bkleppma/create correspondences with known blocking/blockings.csv"), '\t');
	        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
      
		    for (int i=0; i<indexReader.maxDoc(); i++) {
			    Document doc = indexReader.document(i);
		        
			    System.out.println("finding the blocked partner tables for Table in lucene document " + String.valueOf(i));
			    // get the table-values
			    String originalTableHeader =doc.getFields("tableHeader")[0].stringValue();		
				String originalKeyColumnString =doc.getFields("keyColumnString")[0].stringValue();
				String queryString  = formatQueryString(originalKeyColumnString, maxEditDistance);
 
		        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//		        String[] entries2 = {"test2: " + String.valueOf(i) + " " +  originalTableHeader};
//		        writer.writeNext(entries2);
		        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				
				
				if(queryString.trim().length()>0) {
				    QueryParser queryParser = new QueryParser(Version.LUCENE_46, "keyColumnString", new StandardAnalyzer(Version.LUCENE_46));
					Query q = queryParser.parse(queryString);
					ScoreDoc[] hits = indexSearcher.search(q, numResults).scoreDocs;
					
			        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//			        String[] entries3 = {"test3: " + String.valueOf(hits.length)};
//			        writer.writeNext(entries3);
			        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					
					
					for (ScoreDoc hit : hits){
						Document foundDoc = indexSearcher.doc(hit.doc);
						
						
				        String candidateTableHeader =  foundDoc.getFields("tableHeader")[0].stringValue();
				        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//				        String[] entries4 = {"test4: " + candidateTableHeader};
//				        writer.writeNext(entries4);
				        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				        
				        
						if (! candidateTableHeader.equals(originalTableHeader)){
							
			
					        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//					        String[] entries5 = {"test5"};
//					        writer.writeNext(entries5);
					        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
							
							// sort the tables  (table1 < table2)
							String table1 = null;
							String table2 = null;
							if(originalTableHeader.compareTo(candidateTableHeader)<0){
								table1 = originalTableHeader;
								table2 = candidateTableHeader;
							} else if(originalTableHeader.compareTo(candidateTableHeader)>0){
								table1 = candidateTableHeader;
								table2 = originalTableHeader;
							}
							
					        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//					        String[] entries6 = {"test6"};
//					        writer.writeNext(entries6);
					        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					        
							// append to HashMap
							HashSet<String> correspondingTables = blockedTableCombinations.get(table1);
							if (correspondingTables==null){
								correspondingTables = new HashSet<String>();
								correspondingTables.add(table2);	
							}else{
								correspondingTables.add(table2);
							}	
							blockedTableCombinations.put(table1,correspondingTables);
							
					        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
							
							List<String> newRowList = new ArrayList<String>(correspondingTables);
							newRowList.add(0, table1);
							
							String[] newRowArray = new String[newRowList.size()];
							newRowArray = newRowList.toArray(newRowArray);
							
					        writer.writeNext(newRowArray);
					        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
						}
				        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//				        String[] entries8 = {"test8"};
//				        writer.writeNext(entries8);
				        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					}
			        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//			        String[] entries9 = {"test9"};
//			        writer.writeNext(entries9);
			        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				}   
		    }	

				
		    
	        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//	        String[] entries10 = {"test10"};
//	        writer.writeNext(entries10);
	        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		    
		    // Loop through tables in HashMap and find Correspondences between them=====================================================
		    System.out.println("finding correspondences between the blocked tables... [" + dateFormat.format(new Date()) + "]");
		    WebTableDataSetLoader loader = new WebTableDataSetLoader();
		    CsvTableParser csvParser = new CsvTableParser();
	        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//	        String[] entries11 = {"test11: " + String.valueOf(blockedTableCombinations.keySet().size())};
//	        writer.writeNext(entries11);
	        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
	        
		    System.out.println(blockedTableCombinations);
		    	
		    for (String tableName1 : blockedTableCombinations.keySet()) {
		    	
		        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//		        String[] entries115 = {"test11.5: "+ tableName1};
//		        writer.writeNext(entries115);
		        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		    	File file1 = new File(datafilePath + "/" + tableName1);
				Table table1 = csvParser.parseTable(file1);
				DataSet<MatchableTableRow, MatchableTableColumn> data1 = loader.createTableDataSet(table1);
				
				
		    	HashSet<String> correspondingTables = blockedTableCombinations.get(tableName1);
		        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//		        String[] entries12 = {"test12: "+ String.valueOf(correspondingTables.size())};
//		        writer.writeNext(entries12);
		        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		    	for (String tableName2 : correspondingTables) {
		    	    
			        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//			        String[] entries13 = {"test13: " + tableName2};
//			        writer.writeNext(entries13);
			        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			    	File file2 = new File(datafilePath + "/" + tableName2);
					Table table2 = csvParser.parseTable(file2);
					DataSet<MatchableTableRow, MatchableTableColumn> data2 = loader.createTableDataSet(table2);
		    		
					Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> instanceCorrespondences = getInstanceMatches(data1, data2, file1, file2, correspondenceFolderPath);
			        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//			        String[] entries14 = {"test14: " + String.valueOf(instanceCorrespondences.get().size())};
//			        writer.writeNext(entries14);
			        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					
					if (instanceCorrespondences.get().size()>0){
						
						Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> instanceCorrespondences_short = new ProcessableCollection<Correspondence<MatchableTableRow, MatchableTableColumn>>();
				        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//				        String[] entries15 = {"test15"};
//				        writer.writeNext(entries15);
				        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
						if (instanceCorrespondences.get().size()>300){
					        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//					        String[] entries16 = {"test16"};
//					        writer.writeNext(entries16);
					        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
							Integer counter =  0;
							for (Correspondence<MatchableTableRow, MatchableTableColumn> instanceCorrespondence: instanceCorrespondences.get()){
								counter ++;
								instanceCorrespondences_short.add(instanceCorrespondence);
								if (counter >300) break;
							}
						} else {
					        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//					        String[] entries18 = {"test18"};
//					        writer.writeNext(entries18);
					        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
							instanceCorrespondences_short = instanceCorrespondences;
						}
				        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//				        String[] entries19 = {"test19: " + String.valueOf(instanceCorrespondences_short.get().size())};
//				        writer.writeNext(entries19);
				        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
						System.out.println(String.valueOf(instanceCorrespondences_short.get().size()));
						getDuplicateBasedSchemaMatches(data1, data2, file1, file2, instanceCorrespondences_short, correspondenceFolderPath);	
				        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//				        String[] entries20 = {"test20"};
//				        writer.writeNext(entries20);
				        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					}
			        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//			        String[] entries21 = {"test21"};
//			        writer.writeNext(entries21);
			        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		    	}
		        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//		        String[] entries22 = {"test22"};
//		        writer.writeNext(entries22);
		        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		    }
	        // TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//	        String[] entries23 = {"test23"};
//	        writer.writeNext(entries23);
	        writer.close();
	        // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		
		
	System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	System.out.println("End-of Create Correspondence Files");
	System.out.println(dateFormat.format(new Date())); //2016/11/16 12:08:43
	System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
	}
}	

	
	
	
	
	
	
	
	

	/**
	 * getInstanceMatches
	 * 
	 * This method finds the instance-matches between a pair of tables and returns these instance matches to a csv-file
	 * 
	 * For finding instance matches, all NxM row combinations between the two tables are considered.
	 * Row combinations with a similarity score bigger than 0.7 are considered instance matches.
	 * The similarity score for a row combination = 0.5 * KeyColumnSimilarity + 0.5 * NonKeyColumnSimilarity
	 * To find out how the KeyColumnSimilarity and the NonKeyColumnSimilarity are calculated, please look at the classes KeyColumnComparatorJaccard.java and NonKeyColumnComparator_withDType.java respectively. 
	 * 
	 * If the parameter deternimePrecisionAndRecall is set to true (by default it is false), then following steps are done additionally: 
	 * 1. read a goldstandard file from memory. This goldstandard file should contain a list of the correct instance correspondences - usually these will have been manually determined by a person.
	 * 2. compare the found instance correspondences with the correct instance correspondences from the goldstandard file and calculate the Precision-, Recall- and F1- scores.
	 * 3. save these scores to a csv-file
	 * 
	 * @param data1
	 * @param data2
	 * @param file1
	 * @param file2
	 * @param correspondenceFolderPath
	 * @param deternimePrecisionAndRecall
	 * @param fileLocationsForEvaluation
	 * @return
	 * @throws Exception
	 */
	public static Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> getInstanceMatches(DataSet<MatchableTableRow, MatchableTableColumn> data1,  DataSet<MatchableTableRow, MatchableTableColumn> data2, File file1, File file2, String correspondenceFolderPath) throws Exception {
		

		
		// create a matching rule
		LinearCombinationMatchingRule<MatchableTableRow, MatchableTableColumn> matchingRule = new LinearCombinationMatchingRule<>(0.5);
		
		// add comparators
		matchingRule.addComparator(new KeyColumnComparatorJaccard(), 0.5);
		matchingRule.addComparator(new NonKeyColumnComparator_withDType(), 0.5);
		matchingRule.setFinalThreshold(0.1);

		
		// create a blocker (blocking strategy)
		NoBlocker<MatchableTableRow, MatchableTableColumn> blocker = new NoBlocker<MatchableTableRow, MatchableTableColumn>();

		// Initialize Matching Engine
		MatchingEngine<MatchableTableRow, MatchableTableColumn> engine = new MatchingEngine<>();

		// Execute the matching
		Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> correspondences = engine.runIdentityResolution(
				data1, data2, null, matchingRule,
				blocker);

		
		
		// write the correspondences to the output file
		try {
			System.out.println("===============Save " + correspondences.get().size() + " instance correspondences to file for " + file1.getName() + "  " + file2.getName() + "   ========================");
			String instanceCorrespondencesFilename = correspondenceFolderPath + "/instanceCorrespondences/" + file1.getName().replaceAll(".csv", "") + "__" + file2.getName();
	        CSVWriter csvwriter = new CSVWriter(new FileWriter(instanceCorrespondencesFilename), ',');
	        
			for (Correspondence<MatchableTableRow, MatchableTableColumn> correspondence : correspondences.get()){
	//			System.out.println(correspondence.getFirstRecord().getRowNumber() + " <--> " + correspondence.getSecondRecord().getRowNumber() + "  (" + String.format("%.4f", correspondence.getSimilarityScore()) + ")" );
		        String[] newRow = {String.valueOf(correspondence.getFirstRecord().getRowNumber()),String.valueOf(correspondence.getSecondRecord().getRowNumber()),String.format("%.4f", correspondence.getSimilarityScore())};
		        csvwriter.writeNext(newRow);
			}
			csvwriter.close();
		} catch (Exception e) {e.printStackTrace();	} 


		

		
		return correspondences;
	}
	

	
	
	
	
	
	
	
	/**
	 * getDuplicateBasedSchemaMatches
	 * 
	 * This method uses the previously found instance-matches between two tables to identify the schema-matches between these tables.
	 * It then saves the found schema-matches to a csv-file.
	 * 
	 * The schema-match finding works as follows:
	 * In each table, we only consider the instances that have an instance match to the other table.
	 * For each column combination (column1 - column2) between the two tables, we compare the values the left instances have for the column1 with the values the corresponding right instances have for the column2.
	 * If the average similarity between these instance values is bigger than 0.7, we consider this column-combination to be a schema-correspondence. 
	 * (The exact way that two instance values are compared is data type specific. It is specified in the nested method 'compare'.)
	 * 
	 * Once the schema-matches have been found, they are saved to a csv-file.
	 * 
	 * 
	 * @param data1
	 * @param data2
	 * @param file1
	 * @param file2
	 * @param instanceCorrespondences
	 * @param correspondenceFolderPath
	 * @return
	 * @throws IOException
	 */
	public static Processable<Correspondence<MatchableTableColumn, MatchableTableRow>> getDuplicateBasedSchemaMatches(DataSet<MatchableTableRow, MatchableTableColumn> data1, DataSet<MatchableTableRow, MatchableTableColumn> data2, File file1, File file2, Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> instanceCorrespondences, String correspondenceFolderPath) throws IOException{

			
			// define the schema matching rule
			VotingMatchingRule<MatchableTableColumn, MatchableTableRow> rule = new VotingMatchingRule<MatchableTableColumn, MatchableTableRow>(0.5) 
					{

						private static final long serialVersionUID = 1L;
						private NumericDeviationSimilarity deviationSim = new NumericDeviationSimilarity();
//						private DeviationSimilarity deviationSim = new DeviationSimilarity();
						private InverseExponentialDateSimilarity dateSim = new InverseExponentialDateSimilarity();
//						private NormalisedDateSimilarity dateSim = new NormalisedDateSimilarity();
						private TokenizingJaccardSimilarity jaccardSim = new TokenizingJaccardSimilarity();

						@Override
						public double compare(MatchableTableColumn a1, MatchableTableColumn a2,
								Correspondence<MatchableTableRow, Matchable> c) {
							
							double similarity = 0;
						
							if (c.getFirstRecord().get(a1.getColumnIndex())!=null && c.getSecondRecord().get(a2.getColumnIndex())!=null){
								
								
								DataType datatype1 = a1.getType();
								DataType datatype2 = a2.getType();

								if (datatype1 == DataType.numeric && datatype2 == DataType.numeric)	{
									similarity = 0.05* deviationSim.calculate(Double.valueOf(c.getFirstRecord().get(a1.getColumnIndex()).toString()), Double.valueOf(c.getSecondRecord().get(a2.getColumnIndex()).toString()));
								} else if (datatype1 == DataType.date && datatype2 == DataType.date)	{
									dateSim.setHalvingTime(730.0);
									similarity = dateSim.calculate( (DateTime) c.getFirstRecord().get(a1.getColumnIndex()), (DateTime) c.getSecondRecord().get(a2.getColumnIndex()));
								} else if (datatype1 == DataType.bool && datatype2 == DataType.bool)	{
									similarity = (c.getFirstRecord().get(a1.getColumnIndex())==c.getSecondRecord().get(a2.getColumnIndex()) ? 1 : 0);
								} else{
									similarity = jaccardSim.calculate(c.getFirstRecord().get(a1.getColumnIndex()).toString(), c.getSecondRecord().get(a2.getColumnIndex()).toString());
								}	
								
								//System.out.println(a1.getHeader() + "<>" + a2.getHeader() + "||" + c.getFirstRecord().get(a1.getColumnIndex()).toString() + " <--> " + c.getSecondRecord().get(a2.getColumnIndex()).toString() + " (" + String.format("%.4f", similarity) + ")");
							}
							similarity = Math.sqrt(similarity);
							return similarity;
						}
					};
			
					
			// Initialize Matching Engine
			MatchingEngine<MatchableTableRow, MatchableTableColumn> engine = new MatchingEngine<>();
			// Execute the matching
			Processable<Correspondence<MatchableTableColumn, MatchableTableRow>> correspondences = engine.runDuplicateBasedSchemaMatching(data1.getSchema(), data2.getSchema(), instanceCorrespondences, rule, null, new VotingAggregator<>(true, 0.05), new NoSchemaBlocker<>());
			
			// print results
			for(Correspondence<MatchableTableColumn, MatchableTableRow> cor : correspondences.get()) {
				System.out.println(String.format("'%s' <-> '%s' (%.4f)", cor.getFirstRecord().getIdentifier(), cor.getSecondRecord().getIdentifier(), cor.getSimilarityScore()));
			}
			
			
			// save schema correspondences to csv
			System.out.println("===============Save " + correspondences.get().size() + " schema correspondences to file for " + file1.getName() + "  " + file2.getName() + "   ========================");
			CSVWriter csvWriter = new CSVWriter(new FileWriter(correspondenceFolderPath + "/schemaCorrespondences.csv", true));
			for(Correspondence<MatchableTableColumn, MatchableTableRow> cor : correspondences.get()) {
//				String[] schemaCorrespondenceRow = {cor.getFirstRecord().getIdentifier(), cor.getSecondRecord().getIdentifier(), String.valueOf(cor.getSimilarityScore())};
				String[] schemaCorrespondenceRow = {file1.getName(), cor.getFirstRecord().getHeader(), cor.getFirstRecord().getIdentifier(), String.valueOf(cor.getFirstRecord().getColumnIndex()), file2.getName(), cor.getSecondRecord().getHeader(), cor.getSecondRecord().getIdentifier(), String.valueOf(cor.getSecondRecord().getColumnIndex()), String.valueOf(cor.getSimilarityScore())};
				csvWriter.writeNext(schemaCorrespondenceRow);
			}
			csvWriter.close();	
			
		    
	
			return correspondences;
	}
	
	
	
	
	
	
	
	/**
	 * formatQueryString
	 * 
	 * this small method takes the keyColumnString from one table and prepares it so that it can be used for searching tables with similar key columns.
	 * 
	 * The steps are:
	 * 1. remove the commas from the keyColumnString
	 * 2. split the keyColumnString into a list of words
	 * 3. limit the list of words to a length of 1000
	 * 4. add '~maxEditDistance' to the end of every word. The maxEditDistance specifies the maximum levenstein distance, that a word may have to be counted as match 
	 *    (by default: maxEditDistance=3)
	 * 
	 * 
	 * @param keyColumnString
	 * @param maxEditDistance
	 * @return
	 */
	public static String formatQueryString(String keyColumnString, int maxEditDistance) {
		keyColumnString = keyColumnString.replace(',', ' ');
	    List<String> keyColumnString_tokens = WebTablesStringNormalizer.tokenise(keyColumnString, true);
	    StringBuilder stringBuilder = new StringBuilder();
	    
	    if (keyColumnString_tokens.size()>1000) keyColumnString_tokens = keyColumnString_tokens.subList(0, 1000);


	    for(String token : keyColumnString_tokens) {
	    	stringBuilder.append(token);
	        
	        if(maxEditDistance>0) {
	        	stringBuilder.append("~");
	        	stringBuilder.append(maxEditDistance);
	        }
	        stringBuilder.append(" ");
	    }
	    
	    keyColumnString = stringBuilder.toString();
	    return keyColumnString;
	}

	
	
	
	
	
	
	
	
	
	
	
//	public static void test() throws Exception{
//	
//	//  GLOBAL VARIABLES   ========================================================================
//	HashMap<String, String> filePaths = new HashMap<String, String>();
//	filePaths.put("instanceCorrespondenceFilePath", "C:/Users/UNI-Mannheim/T2Kv2/Backend_Correspondence_generator/resources/generatedMatches/instanceMatches.csv");
//	filePaths.put("schemaCorrespondenceFilePath", "C:/Users/UNI-Mannheim/T2Kv2/Backend_Correspondence_generator/resources/generatedMatches/schemaMatches.csv");
//	boolean printOuput = false;
//	
//
//	//  CODE   ========================================================================
//	// load data
//    WebTableDataSetLoader loader = new WebTableDataSetLoader();
//    CsvTableParser csvParser = new CsvTableParser();
//    
//	File file1 = new File("C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-05-05 Winte.r/goldstandard data/tables - csv/10630177_0_4831842476649004753.csv");
//	Table table1 = csvParser.parseTable(file1);
//	DataSet<MatchableTableRow, MatchableTableColumn> data1 = loader.createTableDataSet(table1);
//	
//	File file2 = new File("C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-05-05 Winte.r/goldstandard data/tables - csv/54773498_0_4341462266113591159.csv");
//	Table table2 = csvParser.parseTable(file2);
//	DataSet<MatchableTableRow, MatchableTableColumn> data2 = loader.createTableDataSet(table2);
//	
//	Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> instanceCorrespondences = getInstanceMatches(data1, data2, filePaths, printOuput);
//	if (instanceCorrespondences.get().size()>0){
//		getDuplicateBasedSchemaMatches(data1, data2, instanceCorrespondences, filePaths, printOuput);	
//	}
//	
//}

	
}
