package de.uni_mannheim.informatik.dws.ds4dm.CreateLuceneIndex;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

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

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TableHeaderDetectorContentBased;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TableKeyIdentification;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.CsvTableParser;

public class TableIndexer {

	private IndexWriter keyColumnIndexWriter;
	private IndexWriter columnNameIndexWriter;
	private IndexWriter tableIndexWriter;
	
	
	public void setKeyColumnIndexWriter(IndexWriter keyColumnIndexWriter){
		this.keyColumnIndexWriter = keyColumnIndexWriter;
	}
	
	public void setColumnNameIndexWriter(IndexWriter columnNameIndexWriter){
		this.columnNameIndexWriter = columnNameIndexWriter;
	}
	
	public void setTableIndexWriter(IndexWriter tableIndexWriter){
		this.tableIndexWriter = tableIndexWriter;
	}
	
	
	
	
	
	/**
	 * writeTableToIndexes
	 * 
	 * This method saves information about the csv-dataFile to the following Indexes: KeyColumnIndex, ColumnNameIndex, TableIndex
	 * This method does following steps:
	 * 1. read the csv-table into a table-object
	 * 2. determine the key-column of the table
	 * 3. if a key column was detected: 
	 * 		3.1. create a hashmap with the distinct values of every column (this will be needed for the TableIndex and the KeyColumnIndex)
	 * 		3.2. call the indexing-methods for the individual Indexes
	 * 
	 * @param dataFile
	 * @param keyColumnIndexWriter
	 * @param columnNameIndexWriter
	 * @param tableIndexWriter
	 * @throws IOException
	 */
	public void writeTableToIndexes(File dataFile, String keyColumnsCSVPath) throws Exception {
		
		// get Table-object for tableBeingIndexed
		CsvTableParser csvtableparser = new CsvTableParser();
		Table table = csvtableparser.parseTable(dataFile);

		// TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//		if (dataFile.getName().equals("phones_www.newegg.com.csv")){
//			for (int columnNumber = 0; columnNumber < table.getSchema().getSize(); columnNumber++){
//				System.out.println("phones_www.newegg.com.csv    -    " + table.getSchema().get(columnNumber).getHeader());
//			}
//		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

		// if keycolumnIndexes were provided: get them from the keyColumnsCSV-file and save them in the table
		try{
			if (keyColumnsCSVPath != null){
			     CSVReader reader = new CSVReader(new FileReader(keyColumnsCSVPath), '\t');
			     String [] nextLine;
			     while ((nextLine = reader.readNext()) != null) {
			    	 if (nextLine[0].equals(dataFile.getName())){
			    		try {
			    			String givenKeyColumnName = nextLine[1];
			    			Integer givenKeyColumnIndex = null;
			    			for (int columnIndex = 0; columnIndex< table.getSchema().getSize(); columnIndex++){
			    				String columnName = table.getSchema().get(columnIndex).getHeader();
			    				if (nextLine[1].equalsIgnoreCase(columnName)){
			    					givenKeyColumnIndex = columnIndex;
			    				}
			    			}
			    			
			    			System.out.println(String.valueOf(givenKeyColumnIndex));
			    			if (givenKeyColumnIndex >= 0) table.setSubjectColumnIndex(givenKeyColumnIndex);	   
			    			
			    		} catch (NumberFormatException e) {}	 
			    	 }
			     }
			     reader.close();
			}
		} catch (IOException e) {System.out.println("Error: " + keyColumnsCSVPath + " not found");}
		
		Integer keyColumnIndex = table.getSubjectColumnIndex();
		System.out.println("keyColumnIndex = " + String.valueOf(keyColumnIndex)); 
		if (keyColumnIndex==-1) {
			System.out.println("error: no key column was detected for the table " + dataFile.getName());
		} else {
			
			HashMap<String, HashMap<String, Integer>> distinctTableValues = makeDistinctValuesMap(table);	
			System.out.println("writing " + dataFile.getName() + " to ColumnNameIndex...");
			boolean columnNameIndexSuccess = writeTableToColumnNameIndex(table, dataFile, distinctTableValues);
			System.out.println("writing " + dataFile.getName() + " to TableIndex...");
//			try{
			boolean tableIndexIndexSuccess = writeTableToTableIndex(table, dataFile, distinctTableValues);
//			} catch (IOException e) {System.out.println("IOError while executing writeTableToTableIndex.");}
			System.out.println("writing " + dataFile.getName() + " to KeyColumnIndex...");
			boolean keyColumnIndexSuccess = writeTableToKeyColumnIndex(table, dataFile.getName(), keyColumnIndex);
			
		}

	}
	
	/**
	 * makeDistinctValuesMap
	 * 
	 * This method creates a hashmap containing the distinct values of the table's columns
	 * the input is a Table, the output is a hashmap with the following structure:
	 * { 
	 *   column1: {first_value_of_column1: occurence_count, second_value_of_column1: occurence_count,..},
	 *   column2: {first_value_of_column2: occurence_count, second_value_of_column2: occurence_count,..},
	 *   :
	 * }
	 * 
	 * @param table
	 * @return distinctValues-hashmap
	 */
	public static HashMap<String, HashMap<String, Integer>> makeDistinctValuesMap(Table table){
		HashMap<String, HashMap<String, Integer>> distinctValues = new HashMap<String, HashMap<String, Integer>>();
		
		for (TableRow row: table.getRows()){
			for (int columnIndex = 0; columnIndex< table.getSchema().getSize(); columnIndex++){
				
				String columnname = table.getSchema().get(columnIndex).getHeader();
				
				if (row.get(columnIndex) != null){
					String value = row.get(columnIndex).toString();
					
					if (distinctValues.keySet().contains(columnname)){
						if (distinctValues.get(columnname).keySet().contains(value)){
							HashMap<String, Integer> thisColumnsValueCount = distinctValues.get(columnname);
							thisColumnsValueCount.put(value, thisColumnsValueCount.get(value) + 1);
							distinctValues.put(columnname,thisColumnsValueCount);
						} else {
							HashMap<String, Integer> thisColumnsValueCount = distinctValues.get(columnname);
							thisColumnsValueCount.put(value, 1);
							distinctValues.put(columnname,thisColumnsValueCount);
						}
			
						
					} else {
						HashMap<String, Integer> newValueCountMap  = new HashMap<String, Integer>();
						newValueCountMap.put("columnIndex", columnIndex);
						newValueCountMap.put(value, 1);
						distinctValues.put(columnname,newValueCountMap);
					}
					
					
					
					
				}
			}
		}
		
		return distinctValues;
	}
	
	
	/**
	 * writeTableToKeyColumnIndex
	 * 
	 * This method saves the key column of the table to the KeyColumnIndex
	 * It does the following steps:
	 * 1. make the keyColumnString (=the concatenated values of the table's keyColumn (seperated by " "))
	 * 2. save a document with the following values to the keyColumnIndex: tableHeader, columnHeader, keyColumnString, keyColumnIndex
	 * 
	 * @param table
	 * @param tablename
	 * @param keyColumnIndexWriter
	 * @param keyColumnIndex
	 * @return boolean: 'success' 
	 */
	public boolean writeTableToKeyColumnIndex(Table table, String tablename, int keyColumnIndex){
		boolean success = true;
		
		// read in the key column values and append them to keyColumnString
		String keyColumnString = "";

		for (TableRow row : table.getRows()){
			keyColumnString += row.get(keyColumnIndex) + " ";
		}


	    // make a document
		Document doc = new Document();	
		doc.add(new StringField("tableHeader", tablename, Field.Store.YES));
	    doc.add(new Field("columnHeader", table.getSchema().get(keyColumnIndex).getHeader(), TextField.TYPE_STORED));
	    doc.add(new Field("keyColumnString", keyColumnString, TextField.TYPE_STORED));
	    doc.add(new Field("keyColumnIndex", String.valueOf(keyColumnIndex), TextField.TYPE_STORED));
	
	    System.out.println("XX doc.add(new StringField('tableHeader', '" + tablename + "', Field.Store.YES));");
	    System.out.println("XX doc.add(new Field('columnHeader', '" + table.getSchema().get(keyColumnIndex).getHeader() + "', Field.Store.YES));");
	    System.out.println("XX doc.add(new Field('keyColumnString', '" + keyColumnString + "', Field.Store.YES));");
	    System.out.println("XX doc.add(new Field('keyColumnIndex', '" + String.valueOf(keyColumnIndex) + "', Field.Store.YES));");
	    
	    
	    // add the document to the index
	    try {
	    	this.keyColumnIndexWriter.addDocument(doc);
	    	this.keyColumnIndexWriter.commit();
		} catch (IOException e) {
			System.out.println("IOException while adding document for " + tablename + ", col=" + table.getSchema().get(keyColumnIndex).getHeader() + " ...");
			e.printStackTrace();
			success = false;
		}
	    

	    
		return success;
	}
	
	/**
	 * writeTableToColumnNameIndex
	 * 
	 * This method saves the names of the table's columns to the ColumnNameIndex
	 * It does the following steps:
	 * 1. loop through every column in the table:
	 * 		1.1. 
	 * 
	 * @param table
	 * @param dataFile
	 * @param distinctTableValues
	 * @param columnNameIndexWriter
	 * @return
	 * @throws IOException 
	 */
	public boolean writeTableToColumnNameIndex(Table table, File dataFile, HashMap<String, HashMap<String, Integer>> distinctTableValues) {
		boolean success = true;
    
		
		for (int columnIndex = 0 ; columnIndex< table.getSchema().getSize(); columnIndex++){	
			String columnname = table.getSchema().get(columnIndex).getHeader();
			
			if (distinctTableValues.get(columnname) != null){
				
				Integer columnDistinctValues = distinctTableValues.get(columnname).keySet().size() - 1;
					
			    // make a document
				Document doc = new Document();	
				doc.add(new StringField("tableHeader", dataFile.getName(), Field.Store.YES));
			    doc.add(new Field("value", columnname, TextField.TYPE_STORED));
			    doc.add(new Field("columnDataType", table.getSchema().get(columnIndex).getDataType().toString(), TextField.TYPE_STORED));
			    doc.add(new Field("tableCardinality", String.valueOf(table.getSchema().getSize()), TextField.TYPE_STORED));
			    doc.add(new Field("columnDistinctValues", String.valueOf(columnDistinctValues), TextField.TYPE_STORED));
			    doc.add(new Field("columnindex", String.valueOf(columnIndex), TextField.TYPE_STORED));
			    doc.add(new Field("columnOriginalHeader", columnname, TextField.TYPE_STORED));
			    doc.add(new Field("fullTablePath", dataFile.getAbsolutePath(), TextField.TYPE_STORED));
				

			    // add the document to the index
			    try {

			    	this.columnNameIndexWriter.addDocument(doc);
			    	this.columnNameIndexWriter.commit();
				} catch (IOException e) {
					e.printStackTrace();
					success = false;
				}
			    
			}
		}
		

		return success;
	}
	
	
	/**
	 * writeTableToTableIndex
	 * 
	 * This method saves the distinct values of every of the table's columns to the TableIndex
	 * It does the following steps:
	 * 1. loop through all the columns in the table
	 * 		1.1. loop through all the distinct values in this column
	 * 			1.1.1. save a document with following values to the TableIndex: id, tableHeader, columnHeader, columnDataType, tableCardinality, columnDistinctValues, valueMultiplicity, value, fullTablePath, isPrimaryKey, originalValue
	 * 
	 * @param table
	 * @param dataFile
	 * @param distinctTableValues
	 * @param tableIndexWriter
	 * @return boolean: 'success'
	 * @throws IOException 
	 */
	public boolean writeTableToTableIndex(Table table, File dataFile, HashMap<String, HashMap<String, Integer>> distinctTableValues){
		boolean success = true;
		
		int rownumber = 1;
		
		
		for (TableRow row: table.getRows()){

			for (int columnIndex = 0; columnIndex< table.getSchema().getSize(); columnIndex++){
				

				
				String columnname = table.getSchema().get(columnIndex).getHeader();
				String dataType =  table.getSchema().get(columnIndex).getDataType().toString();	
			     
				
				if (distinctTableValues.keySet().contains(columnname)){
					
					String columnvalue = "null";
					if (row.get(columnIndex) != null) columnvalue = row.get(columnIndex).toString();

					
					Integer columnDistinctValues = distinctTableValues.get(columnname).keySet().size() - 1;
					Integer valueMultiplicity = distinctTableValues.get(columnname).get(columnvalue);
					Boolean isPrimaryKey = (table.getSubjectColumnIndex()==columnIndex ? true : false);
					
					
					// make a document
					Document doc = new Document();
				    doc.add(new Field("id", String.valueOf(rownumber), TextField.TYPE_STORED));
				    doc.add(new StringField("tableHeader", dataFile.getName(), Field.Store.YES));
				    doc.add(new Field("columnHeader", columnname, TextField.TYPE_STORED));
				    doc.add(new Field("columnDataType", dataType, TextField.TYPE_STORED));
				    doc.add(new Field("tableCardinality", String.valueOf(distinctTableValues.keySet().size()), TextField.TYPE_STORED));
				    doc.add(new Field("columnDistinctValues", String.valueOf(columnDistinctValues), TextField.TYPE_STORED));
				    doc.add(new Field("valueMultiplicity", String.valueOf(valueMultiplicity), TextField.TYPE_STORED));
				    doc.add(new Field("value", columnvalue, TextField.TYPE_STORED));
				    doc.add(new Field("fullTablePath", dataFile.getAbsolutePath(), TextField.TYPE_STORED));
				    doc.add(new Field("isPrimaryKey", String.valueOf(isPrimaryKey), TextField.TYPE_STORED));
				    doc.add(new Field("originalValue", columnvalue, TextField.TYPE_STORED));
				    	
					
				    // add the document to the index
				    try {
				    	this.tableIndexWriter.addDocument(doc);
				    	this.tableIndexWriter.commit();
					} catch (IOException e) {
						System.out.println(e.getStackTrace().toString());
						
						success = false;
					}
			    
				}
			}
			rownumber++;
		}
		
		
		return success;
	}

	
	public static boolean writeTableToTableIndex_old(Table table, File dataFile, HashMap<String, HashMap<String, Integer>> distinctTableValues, IndexWriter tableIndexWriter){
		boolean success = true;
					
		for (String columnname: distinctTableValues.keySet()){
			
			Integer columnIndex = distinctTableValues.get(columnname).get("columnIndex");
			String dataType =  table.getSchema().get(columnIndex).getDataType().toString();	
			Integer columnDistinctValues = distinctTableValues.get(columnname).keySet().size() - 1;
			Integer valueCounter = 0;
			boolean isPrimaryKey = (table.getSubjectColumnIndex()==columnIndex ? true : false);
			
			
			for (String columnvalue: distinctTableValues.get(columnname).keySet()){
				valueCounter++;
				Integer valueMultiplicity = distinctTableValues.get(columnname).get(columnvalue);
				
				
				
			    // make a document
				Document doc = new Document();	
			    doc.add(new Field("id", String.valueOf(valueCounter), TextField.TYPE_STORED));
			    doc.add(new StringField("tableHeader", dataFile.getName(), Field.Store.YES));
			    doc.add(new Field("columnHeader", columnname, TextField.TYPE_STORED));
			    doc.add(new Field("columnDataType", dataType, TextField.TYPE_STORED));
			    doc.add(new Field("tableCardinality", String.valueOf(distinctTableValues.keySet().size()), TextField.TYPE_STORED));
			    doc.add(new Field("columnDistinctValues", String.valueOf(columnDistinctValues), TextField.TYPE_STORED));
			    doc.add(new Field("valueMultiplicity", String.valueOf(valueMultiplicity), TextField.TYPE_STORED));
			    doc.add(new Field("value", columnvalue, TextField.TYPE_STORED));
			    doc.add(new Field("fullTablePath", dataFile.getAbsolutePath(), TextField.TYPE_STORED));
			    doc.add(new Field("isPrimaryKey", String.valueOf(isPrimaryKey), TextField.TYPE_STORED));
			    doc.add(new Field("originalValue", columnvalue, TextField.TYPE_STORED));

				
			    // add the document to the index
			    try {
			    	tableIndexWriter.addDocument(doc);
				} catch (IOException e) {
					e.printStackTrace();
					success = false;
				}
			    
			    
			}
		}
		return success;
	}




public static void test2()throws IOException {
		
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
    	
    	IndexWriterConfig config1 = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
		String indexPath1 = "public/repositories/ProductDataRepository/indexes/KeyColumnIndex/";
	    Directory directory1;
	    IndexWriter keyColumnIndexWriter;

		directory1 = FSDirectory.open(new File(indexPath1));
		keyColumnIndexWriter = new IndexWriter(directory1, config1);
		
		
		try {Document doc = new Document();
		doc.add(new StringField("tableHeader", "phones_www.bestbuy.com.csv", Field.Store.YES));
		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
		doc.add(new Field("keyColumnString", "accessory_nexus 6p samsung galaxy s6 edge 32gb samsung galaxy s6 edge 32gb accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_iphone 4 nexus 6p 128gb accessory_lg g4 samsung galaxy s6 edge 32gb samsung galaxy s6 edge 32gb ", TextField.TYPE_STORED));
		doc.add(new Field("keyColumnIndex", "18", TextField.TYPE_STORED));
		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}

//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_intl.target.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "samsung galaxy s6 edge 32gb samsung galaxy s6 edge 32gb ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "8", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "headphones_www.aliexpress.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "shure se 215 shure se 215 shure se2 15 ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "4", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_www.flipkart.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "iphone 6 plus 16gb accessory_iphone accessory_htc iphone 6s plus 128gb accessory_nokia iphone 6s 64gb accessory_nokia accessory_iphone accessory_htc accessory_htc iphone 6 16gb accessory_iphone accessory_iphone iphone 6s 64gb accessory_htc iphone 6s 16gb accessory_nokia accessory_nokia ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "47", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "headphones_www.ebay.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "accesory_grado shure se425 accessory_sennheiser accessory_shure accessory_shure akg k712 pro accessory_shure accessory_shure accessory_shure accessory_shure sennheiser hd 650 akg k712 pro accessory_shure audio-technica ath-m50x akg k240 accessory_sennheiser shure srh 440 accessory_shure accessory_shure shure srh 440 shure srh 440 accessory_shure accessory_sennheiser shure srh440 akg k702 accessory_sennheiser accessory_akg sennheiser hd 429 shure se 215 accessory_shure accessory_shure akg k99 sennheiser hd 650 shure se 215 accessory_sennheiser sennheiser momentum 2.0 akg k712 pro accessory_shure sennheiser hd 650 accessory_shure accessory_sennheiser accessory_shure akg k712 pro accessory_sennheiser audio-technica ath-m50x akg k612 pro shure se 425 akg k7shure srh 440 sennheiser momentum 2.0 shure se 425 akg k712 pro akg k712 pro accessory_sennheiser akg k712 pro shure se425 accessory_akg shure se 215 accessory_akg akg k712 pro sennheiser hd 650 accessory_shure sennheiser momentum 2.0 accessory_akg sennheiser cx 2.00 shure srh 440 shure se 425 sennheiser hd 650 accessory_shure sennheiser hd 558 accessory_akg akg k712 pro shure se 215 accessory_sennheiser akg k712 pro accessory_sennheiser accessory_sennheiser audio-technica ath-m50x accessory_sennheiser accessory_sennheiser akg k712 pro accessory_shure accessory_sennheiser accessory_shure accessory_sennheiser accessory_akg sennheiser hd 650 accessory_shure akg k712 pro sennheiser momentum 2.0 accessory_sennheiser oppo pm-3 shure se 215 accessory_shure sennheiser cx 3.00 akg k712 pro accessory_sennheiser shure se 425 accessory_shure akg k240 accessory_akg accessory_shure grado sr80e akg k712 pro sennheiser hd 650 sennheiser hd 650 akg k812 accessory_shure sennheiser momentum 2.0 audio-technica ath-m50x audio-technica ath-m50x accessory_akg accessory_sennheiser accessory_shure accessory_shure accessory_shure accessory_sennheiser sennheiser momentum 2.0 sennheiser hd 650 audio-technica ath-m50x accessory_shure sennheiser hd 650 accessory_shure accessory_shure accessory_shure akg k712 pro accessory_shure accessory_shure accessory_shure akg k712 pro akg k712 pro shure se 425 accessory_akg shure se 215 accessory_shure shure se 425 accessory_sennheiser accessory_akg sennheiser momentum 2.0 sennheiser hd 650 accessory_shure accessory_akg accessory_akg akg k712 pro accessory_sennheiser accessory_shure sennheiser hd 650 accessory_shure accessory_shure shure se 425 accessory_akg shure se 425 shure se 215 accessory_sennheiser audio-technica ath-m50x shure srh 440 shure se215 accessory_shure accessory_akg akg k712 pro akg k309 accessory_shure sennheiser hd 650 sennheiser momentum 2.0 accessory_shure accessory_sennheiser accessory_shure accessory_sennheiser shure se 215 sennheiser hd 650 accessory_shure shure se 425 accessory_sennheiser accessory_shure sennheiser hd 650 accessory_sennheiser shure se 215 akg k171 pro accessory_shure accessory_shure sennheiser momentum 2.0 accessory_sennheiser sennheiser hd 650 accessory_grado accessory_grado accessory_grado shure se 315 akg k712 pro akg k712 pro shure srh440 shure se 425 akg k712 pro accessory_oppo grado sr325e shure se 425 shure se 425 sennheiser cx 400 sennheiser cx 685 sennheiser cx 685 audio-technica ath-m50x accessory_sennheiser accessory_grado accessory_grado akg k712 pro shure se 425 sennheiser cx 685 accessory_grado akg k712 pro akg q701 sennheiser cx 686 audio-technica ath-m50x sennheiser hd 650 sennheiser hd 600 grado sr80e sennheiser cx 685 accessory_grado akg k712 pro accessory_shure accessory_sennheiser audio-technica ath-m50x sennheiser hd 650 sennheiser hd 650 accessory_shure shure se 215 akg k612 pro shure srh 440 accessory_sennheiser hd 650 akg k712 pro accessory_akg sennheiser hd 650 akg k612 pro accessory_sennheiser accessory_shure accessory_shure akg k712 pro shure se215 sennheiser cx 1.00 shure se425 accessory_sennheiser shure se215 sennheiser momentum 2.0 sennheiser hd 429 accessory_akg accessory_sennheiser sennheiser hd 650 akg k309 accessory_sennheiser shure se 215 oppo pm-3 sennheiser cx 3.00 shure srh440 accessory_shure accessory_sennheiser accessory_akg audio-technica ath-m50x accessory_shure sennheiser cx 3.00 akg k712 pro shure srh 440 shure srh 440 oppo pm-3 sennheiser hd 650 akg k712 pro accessory_sennheiser sennheiser hd 650 sennheiser momentum 2.0 audio-technica ath-m50x akg k712 pro audio-technica ath-m50x grado sr80e shure se 215 shure se 215 grado sr 125e accessory_sennheiser grado sr 60e sennheiser hd 650 accessory_sennheiser accessory_shure accessory_shure accessory_akg accessory_shure accessory_akg accessory_shure accessory_sennheiser accessory_akg akg k712 pro akg k712 pro accessory_shure shure srh 440 accessory_shure accessory_sennheiser accessory_shure accessory_akg accessory_sennheiser ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "2", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_www.walmart.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "lg g4 samsung galaxy s4 16gb accessory_iphone accessory_iphone accessory_iphone accessory_iphone nokia lumia 635 nokia lumia 520 nokia lumia 520 nokia lumia 520 accessory_htc lg g4 accessory_iphone nokia lumia 635 nokia lumia 520 nokia lumia 920 accessory_htc nokia lumia 520 accessory_htc lg g4 lg g4 iphone 5s 16gb iphone 5c 16gb samsung galaxy s4 16gb samsung galaxy s4 16gb samsung galaxy s4 16gb ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "49", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_www.aliexpress.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p iphone 5c 16gb;iphone 5c 32gb accessory_iphone 4 accessory_iphone 6 accessory_iphone 6 accessory_iphone 6 accessory_iphone 6 iphone 4s 16gb;iphone 4s 32gb;iphone 4s 64gb nokia lumia 720 accessory_nexus 6p htc one m9 htc one m9 htc one m9 htc one m9 iphone 5c 32gb iphone 4s 16gb htc one m9+ htc one m9 accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p accessory_nexus 6p ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "64", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "headphones_www.flipkart.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "shure se 425 shure se 425 akg k712 pro akg k712 pro shure srh 550 shure se 425 ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "9", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_www.bjs.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "accessory_iphone accessory_iphone ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "9", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_www.samsclub.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "lg g4 samsung galaxy s6 edge 32gb;samsung galaxy s6 edge 64gb;samsung galaxy s6 edge 128gb samsung galaxy s6 edge+ ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "8", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_www.macmall.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "accessory_iphone accessory_iphone sony xperia z5 accessory_samsung sony xperia z5 ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "37", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_www.microcenter.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "accessory_iphone samsung galaxy s6 32gb accessory_samsung accessory_iphone accessory_samsung accessory_samsung accessory_samsung accessory_iphone accessory_iphone accessory_iphone iphone 6s plus 64gb ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "40", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "tvs_www.microcenter.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "55.0 55.0 65.0 65.0 ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "23", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_www.tesco.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "iphone 6s plus 128gb iphone 6 16gb iphone 6 16gb samsung galaxy s5 16gb accessory_iphone accessory_iphone accessory_iphone accessory_iphone accessory_iphone accessory_iphone accessory_iphone iphone 6s 128gb accessory_iphone iphone 6s plus 128gb nokia lumia 635 samsung galaxy s6 edge+ iphone 6s 16gb iphone 6s 16gb iphone 6s 128gb iphone 6 plus 16gb accessory_iphone accessory_iphone iphone 5s 32gb iphone 6 128gb nokia lumia 635 iphone 6s plus 64gb iphone 6s plus 64gb iphone 6s plus 64gb iphone 6s 16gb accessory_iphone iphone 6 128gb iphone 6s 16gb accessory_iphone iphone 6s plus 16gb iphone 6s plus 16gb iphone 6 plus 16gb iphone 5s 16gb iphone 6s 64gb iphone 6s 64gb ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "50", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_www.alibaba.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "accessory_iphone 4 accessory_iphone accessory_iphone accessory_iphone ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "18", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_www.searsoutlet.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "motorola moto g motorola moto g ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "15", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "tvs_www.sony.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "sony xbr55x850d sony xbr55x850d;sony xbr65x850d;sony xbr75x850d;sony xbr85x850d sony xbr65x850d;sony xbr75x850d;sony xbr85x850d ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "2", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "tvs_www.bestbuy.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "436400.0 65.0 65.0 65.0 65.0 43830.0 43830.0 43830.0 436400.0 436400.0 607000.0 607000.0 607000.0 55.0 55.0 55.0 ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "32", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_www.ebay.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "samsung galaxy s6 edge 128gb iphone 6 plus 64gb htc one m9 htc one m9 htc one m9 htc one m9 htc one m9 htc one m9 htc one m9 htc one m9 htc one m9 htc one m9 lg g4 htc one m9 htc one m9 htc one m9 htc one m9 htc one m9 htc one m9 htc one m9 htc one m9 htc one m9 htc one m9 lg g4 htc one m9 htc one m9 htc one m9 iphone 5s 16gb;iphone 5s 32gb;iphone 5s 64gb iphone 6 64gb iphone 6 64gb iphone 5c 8gb iphone 6 plus 16gb;iphone 6 plus 64gb;iphone 6 plus 128gb nokia lumia 635 nexus 6p 64gb lg g4 nexus 6p 64gb nexus 6p 64gb nexus 6p 64gb nexus 6p 64gb nexus 6p 64gb nexus 6p 64gb nexus 6p 64gb nexus 6p 64gb nexus 6p 64gb nexus 6p 64gb lg g4 nexus 6p 64gb nexus 6p 64gb nexus 6p 64gb nexus 6p 64gb nexus 6p 64gb nexus 6p 64gb nexus 6p 64gb iphone 5 32gb iphone 4 16gb iphone 4 16gb lg g4 iphone 4 16gb iphone 4 16gb htc one max nexus 6p 128gb samsung galaxy s6 edge+ samsung galaxy s6 edge+ samsung galaxy s6 edge+ samsung galaxy s6 edge+ sony xperia z5 nokia lumia 920 lg g4 nokia lumia 920 nokia lumia 920 iphone 5 32gb iphone 5 32gb samsung galaxy s6 edge 32gb samsung galaxy s6 edge 32gb samsung galaxy s6 edge 32gb iphone 4s 32gb iphone 4s 32gb iphone 4s 32gb lg g4 htc one m7 one m7 htc one m7 htc one m7 accessory_htc accessory_htc htc one m9 htc one m9 htc one m8 htc one m8 lg g4 htc one m8 htc one m8 htc one m8 htc one m8 htc one m8 iphone 6 plus 128gb htc one m9+ nokia lumia 1520 htc one m8 htc one m8 lg g4 htc one m9 nexus 6p 32gb sony xperia z5 iphone 4s 8gb nokia lumia 530 nexus 6p 32gb htc one max nokia lumia 929 htc one m7 htc one m7 samsung galaxy s6 edge 128gb lg g4 htc one m7 htc one m7 accessory_htc htc one m9 htc one m9 htc one m9 htc one m8 htc one m8 htc one m8 htc one m8 lg g4 htc one m8 htc one m9+ htc one m7 samsung galaxy s4 mini 16gb sony xperia z5 samsung galaxy s6 edge 32gb nexus 6p 64gb htc one m7 htc one m7 htc one m7 lg g4 htc one a9 htc one max htc one x htc one m7 htc one m7 htc one m9 nokia lumia 1520 htc one m9+ nexus 6p 64gb samsung galaxy s6 edge 32gb lg g4 iphone 5c 16gb htc one m7 nexus 6p 64gb lg g4 lg g4 lg g4 lg g4 lg g4 lg g4 htc one m9 lg g4 lg g4 lg g4 lg g4 lg g4 iphone 5 16gb htc one m8 htc one m8 htc one m8 iphone 6 128gb htc one m9+ iphone 6 128gb iphone 4s 16gb iphone 4s 16gb iphone 4s 16gb iphone 4s 16gb iphone 4s 16gb iphone 4s 16gb iphone 4s 16gb iphone 4s 16gb iphone 4s 16gb samsung galaxy s6 edge 64gb iphone 4s 16gb iphone 4s 16gb iphone 4s 16gb iphone 4s 16gb iphone 4s 16gb nexus 6p 32gb iphone 4s 16gb samsung galaxy s6 edge 32gb sony xperia z5 samsung galaxy s6 32gb samsung galaxy s6 edge 64gb samsung galaxy s6 32gb samsung galaxy s6 32gb iphone 6 plus 16gb iphone 6 plus 16gb iphone 4s 8gb iphone 4s 8gb iphone 5s 16gb iphone 5s 16gb iphone 5s 16gb iphone 5s 16gb iphone 5s 32gb iphone 5s 16gb samsung galaxy s6 edge 128gb iphone 6 16gb iphone 4 8gb iphone 4 8gb iphone 6 16gb iphone 6 16gb iphone 6 16gb iphone 6 16gb iphone 6 16gb iphone 5s 32gb iphone 6 16gb iphone 6 16gb iphone 6 16gb iphone 6 16gb iphone 6 16gb samsung galaxy s5 16gb sony xperia z5 sony xperia z5 premium sony xperia z5 premium sony xperia z5 premium iphone 5s 32gb sony xperia z5 premium sony xperia z5 premium sony xperia z5 premium sony xperia z5 premium sony xperia z5 premium iphone 5c 16gb htc one m9 htc one m9 htc one m9 htc one m9 ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "96", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_de.dhgate.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "accessory_htc ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "9", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "tvs_www.ebay.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "gpx tde1982b vizio d43-c1 gpx te1384b sony xbr55hx950 accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio vizio d43-c1 accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio vizio d43-c1 accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio vizio d43-c1 vizio d43-c1 sony xbr65x930c vizio d43-c1 sony xbr65x930c accessory_gpx accessory_gpx accessory_gpx accessory_gpx accessory_gpx accessory_gpx accessory_gpx accessory_gpx accessory_gpx vizio d43-c1 accessory_gpx accessory_gpx accessory_gpx accessory_gpx accessory_gpx accessory_gpx accessory_gpx accessory_gpx accessory_gpx accessory_gpx vizio d43-c1 samsung un50eh5300 samsung un50eh5300 lg 55eg9600 lg 55eg9600 lg 55eg9600 lg 55eg9600 lg 55eg9600 lg 55eg9600 lg 55eg9600 lg 65eg9600 vizio d43-c1 lg 65eg9600 lg 65eg9600 lg 65eg9600 lg 65eg9600 accessory_samsung samsung un40ju6500 samsung un48ju6400 samsung un40ju6700 sony xbr65x900a sony xbr55x850c vizio d43-c1 sony xbr55x850c sony xbr55x850c samsung un50js7000 samsung un50js7000 sony xbr43x830c sony xbr43x830c sony xbr43x830c samsung un40ju6400 samsung un40ju6400 samsung un40ju6400 vizio d43-c1 samsung un40ju6400 samsung un40ju6400 samsung un40ju6400 samsung un40ju6400 samsung un40ju6400 samsung un50hu8550 accessory_lg accessory_lg accessory_lg accessory_lg vizio d43-c1 accessory_lg accessory_lg accessory_lg accessory_lg accessory_lg accessory_lg accessory_lg accessory_lg accessory_lg accessory_lg gpx tde1982b vizio d43-c1 accessory_lg accessory_lg accessory_lg accessory_lg accessory_lg accessory_lg accessory_samsung accessory_samsung accessory_samsung accessory_samsung vizio d43-c1 accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung vizio d43-c1 accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung sony xbr55x900c vizio d43-c1 accessory_55eg9600 sony xbr75x940c lg 55eg9600 gpx tde1982b sony xbr65x850c sony xbr65x850c sony xbr65x850a sony xbr65x810c lg 43uf6430 gpx te1384b vizio d43-c1 sony xbr55x800b sony xbr55x800b sony xbr55x800b sony xbr75x910c sony xbr49x830c sony xbr49x850b sony xbr49x850b samsung un40ju640da samsung un40ju640da samsung un40ju640da vizio d43-c1 accessory_sony accessory_sony accessory_sony accessory_sony accessory_sony accessory_sony accessory_sony accessory_sony accessory_sony accessory_sony vizio d43-c1 lg 49uf6400 sony xbr55x900c sony xbr55x900c sony xbr55x850c accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio accessory_vizio sony xbr65x900c accessory_vizio accessory_vizio accessory_vizio accessory_vizio sony xbr65x930c accessory_gpx accessory_gpx accessory_gpx accessory_gpx accessory_gpx sony xbr65x950b accessory_gpx accessory_gpx samsung un50js7000 accessory_lg accessory_lg accessory_lg accessory_lg accessory_lg accessory_lg accessory_lg sony xbr55x800b sony xbr65x850c sony xbr55x850a accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung lg 55eg9100 lg 43uf6430 accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung lg 55eg9100 accessory_sony sony xbr65x850c lg 43uf6400 samsung un55ju6500 sony xbr65x900c samsung un55ju6500 sony xbr43x830c lg 55eg9100 accessory_sony accessory_sony accessory_sony accessory_sony samsung un48ju6400, 4k uhd ju6400 series smart tv - 48 class (47.6 diag.) lg 43uf6400 lg 49uf6400 sony xbr55x900c accessory_vizio accessory_vizio accessory_vizio accessory_vizio vizio d43-c1 accessory_gpx accessory_gpx accessory_gpx gpx tde1384b accessory_samsung sony xbr55x850c lg 49uf6400 accessory_lg accessory_lg accessory_lg accessory_lg accessory_lg accessory_lg accessory_samsung gpx tde1384b accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung accessory_samsung sony xbr65x850c sony xbr55x800b samsung un40ju640da accessory_sony gpx tde1384b accessory_vizio accessory_vizio samsung un48ju6400 sony xbr55x850c accessory_lg accessory_lg accessory_samsung accessory_samsung gpx te1384b accessory_vizio gpx tde1384b accessory_vizio accessory_gpx vizio d43-c1 accessory_samsung sony xbr75x850c accessory_sony sony xbr55x850c accessory_gpx accessory_lg accessory_samsung gpx tde1384b gpx tde1384b gpx tde1384b lg 55eg9100 samsung un50eh5300 samsung un50eh5300 gpx te1384b sony xbr55x850c sony xbr55x800b sony xbr55x800b sony xbr55x800b sony xbr43x830c sony xbr43x830c sony xbr43x830c sony xbr65x850c sony xbr43x830c accessory_65eg9600 accessory_65eg9600 accessory_65eg9600 accessory_65eg9600 lg 55eg9100 lg 43uf6430 samsung un40ju640da accessory_sony accessory_sony sony xbr65x850c accessory_sony accessory_sony accessory_sony accessory_sony accessory_sony accessory_sony accessory_sony accessory_sony accessory_sony accessory_sony samsung un40ju7500 accessory_sony accessory_sony accessory_sony samsung un48ju6400 sony xbr55x900c accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 sony xbr55hx950 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 sony xbr65x950b accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 accessory_d43-c1 ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "322", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "tvs_www.conns.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "55.0 55.0 55.0 55.0 55.0 55.0 55.0 55.0 55.0 ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "9", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "tvs_www.flipkart.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "lg 32lf550a lg 32lf550a ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "28", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_www.newegg.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "htc one m9+ htc one m7 accessory_htc accessory_htc accessory_htc accessory_htc accessory_htc htc one m7 htc one m7 accessory_htc samsung galaxy s6 edge 64gb samsung galaxy s6 edge 64gb samsung galaxy s6 edge 64gb samsung galaxy s6 32gb samsung galaxy s6 32gb htc one x samsung galaxy s6 edge+ samsung galaxy s6 edge+ ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "64", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_www.overstock.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "lg g4 lg g4 iphone 6 64gb ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "13", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "headphones_www.walmart.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "sennheiser momentum 2.0 shure srh 440 sennheiser momentum 2.0 sennheiser momentum 2.0 sennheiser momentum 2.0 akg k712 pro sennheiser momentum 2.0 shure se 425 shure se 215 sennheiser momentum 2.0 sennheiser hd 650 shure se 215 sennheiser momentum 2.0 sennheiser momentum 2.0 sennheiser momentum 2.0 shure srh 440 sennheiser momentum 2.0 sennheiser momentum 2.0 sennheiser momentum 2.0 sennheiser momentum 2.0 sennheiser momentum 2.0 ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "7", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "headphones_www.newegg.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "accessory_shure accessory_shure sennheiser momentum 2.0 sennheiser hd 598 sennheiser hd 280 sennheiser hd 280 akg k272 accessory_audio_technica accessory_audio_technica audio-technica ath-m50x sennheiser momentum 2.0 akg k712 pro sennheiser momentum m2 accessory_audio_technica accessory_audio_technica sennheiser ocx 686 akg k830 sennheiser ocx 686 akg q460 sennheiser hd 600 audio-technica ath-ad700x audio-technica ath-ad700x accessory_shure sennheiser pc 363d sennheiser momentum 2.0 shure srh 440 sennheiser momentum 2.0 akg k77 shure se 215 sennheiser hd 219s sennheiser momentum 2.0 accessory_shure accessory_akg accessory_shure accessory_sennheiser akg k272 sennheiser momentum 2.0 sennheiser momentum 2.0 akg av100 accessory_shure sennheiser hd 558 accessory_shure sennheiser momentum 2.0 sennheiser mm 550-x sennheiser momentum 2.0 shure se 215 accessory_shure accessory_akg accessory_sennheiser akg k44 accessory_akg shure se 215 sennheiser momentum 2.0 sennheiser hd 598 sennheiser hd 219s sennheiser momentum 2.0 sennheiser momentum 2.0 shure se 215 audio-technica ath-ad700x sennheiser momentum 2.0 accessory_sennheiser shure srh 440 sennheiser pc 363d sennheiser rs 175 audio-technica ath-avc400 audio-technica ath-m50x sennheiser momentum 2.0 sennheiser momentum 2.0 sennheiser momentum 2.0 sennheiser momentum 2.0 sennheiser momentum 2.0 iei shure srh 1840 sennheiser mm 550-x accessory_sennheiser sennheiser momentum 2.0 sennheiser momentum 2.0 sennheiser mm 550-x shure se 215 sennheiser hd 598 shure se2 15 audio-technica ath-avc400 accessory_shure sennheiser momentum 2.00 accessory_shure akg y 23u accessory_shure sennheiser momentum 2.0 accessory_shure audio-technica ath-m50x sennheiser hd 600 accessory_sennheiser sennheiser momentum 2.0 sennheiser momentum 2.0 sennheiser momentum 2.0 sennheiser momentum 2.0 audio-technica ath-m50x shure srh 440 sennheiser momentum 2.0 accessory_shure accessory_shure shure se 215 shure se 215 shure srh 440 shure se 425 sennheiser momentum 2.0 akg k340 audio-technica ath-m50x akg k612 pro sennheiser hd 380 pro akg k712 pro shure srh 440 sennheiser pc 363d sennheiser momentum 2.0 akg y55 sennheiser momentum 2.0 accessory_shure shure se 215 ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "2", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "tvs_www.macmall.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "gpx tde1384b samsung un60js7000 ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "5", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "headphones_www.bestbuy.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "sennheiser cx 3.00 sennheiser cx 3.00 sennheiser cx 3.00 ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "16", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "phones_www.shop.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "samsung galaxy s6 edge 64gb samsung galaxy s6 edge 64gb iphone 6s plus 64gb iphone 6s plus 64gb iphone 6 plus 16gb accessory_sony accessory_sony accessory_sony accessory_sony accessory_sony iphone 6 16gb iphone 6 plus 64gb accessory_lg accessory_lg accessory_lg accessory_lg samsung galaxy s6 32gb samsung galaxy s6 32gb iphone 6 plus 64gb iphone 6 64gb accessory_iphone accessory_iphone iphone 6 plus 64gb iphone 6 64gb iphone 6 64gb iphone 6 plus 64gb iphone 6s 16gb lg g4 samsung galaxy s6 edge+ samsung galaxy s6 edge+ samsung galaxy s6 edge 32gb lg g4 samsung galaxy s6 edge 64gb ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "51", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
//
//		try {Document doc = new Document();
//		doc.add(new StringField("tableHeader", "tvs_www.walmart.com.csv", Field.Store.YES));
//		doc.add(new Field("columnHeader", "product_name", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnString", "43.0 55.0 55.0 55.0 55.0 55.0 55.0 55.0 55.0 55.0 55.0 43.0 55.0 65.0 65.0 406400.0 43.0 43.0 43.0 43.0 43.0 null 55.0 ", TextField.TYPE_STORED));
//		doc.add(new Field("keyColumnIndex", "41", TextField.TYPE_STORED));
//		keyColumnIndexWriter.addDocument(doc);} catch (IOException e) {}
		
		keyColumnIndexWriter.commit();
}


}
