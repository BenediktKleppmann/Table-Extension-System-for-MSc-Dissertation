/*
 * Copyright (c) 2018 Data and Web Science Group, University of Mannheim, Germany (http://dws.informatik.uni-mannheim.de/)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
 package uploadTable;


import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TableHeaderDetectorContentBased;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TableKeyIdentification;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.CsvTableParser;

public class TableIndexer {

	private IndexWriter keyColumnIndexWriter;
	private IndexWriter columnNameIndexWriter;
	private IndexWriter tableIndexWriter;
	private Integer keyColumnIndex;
	
	
	public void setKeyColumnIndexWriter(IndexWriter keyColumnIndexWriter){
		this.keyColumnIndexWriter = keyColumnIndexWriter;
	}
	
	public void setColumnNameIndexWriter(IndexWriter columnNameIndexWriter){
		this.columnNameIndexWriter = columnNameIndexWriter;
	}
	
	public void setTableIndexWriter(IndexWriter tableIndexWriter){
		this.tableIndexWriter = tableIndexWriter;
	}
	
	public void setKeyColumnIndex(Integer keyColumnIndex){
		this.keyColumnIndex = keyColumnIndex;
	}
	
	public Integer getKeyColumnIndex(){
		return this.keyColumnIndex;
	}
	
	public void closeIndexes() {
		try {
			this.keyColumnIndexWriter.close();
			this.columnNameIndexWriter.close();
			this.tableIndexWriter.close();
		} catch (IOException e) {e.printStackTrace();}
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
	public boolean writeTableToIndexes(File dataFile) throws IOException{
		boolean success = true;
		
		CsvTableParser csvtableparser = new CsvTableParser();
		Table table = csvtableparser.parseTable(dataFile);
		
		
		if (this.keyColumnIndex == null || this.keyColumnIndex < 0){
			this.keyColumnIndex = table.getSubjectColumnIndex();
		}
		
		
		
		if (keyColumnIndex == null || keyColumnIndex < 0) {
			System.out.println("error: no key column was detected for the table " + dataFile.getName());
			success = false;
		} else {
			
			HashMap<String, HashMap<String, Integer>> distinctTableValues = makeDistinctValuesMap(table);
			
			boolean columnNameIndexSuccess = writeTableToColumnNameIndex(table, dataFile, distinctTableValues);
			boolean tableIndexIndexSuccess = writeTableToTableIndex(table, dataFile, distinctTableValues);
			boolean keyColumnIndexSuccess = writeTableToKeyColumnIndex(table, dataFile.getName(), keyColumnIndex);
			
		}
		
		return success;

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
				
				String value;
				if (row.get(columnIndex) == null){
					value = "null";
				} else {
					value = row.get(columnIndex).toString();
				}	
			

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
	
	    // add the document to the index
	    try {
			this.keyColumnIndexWriter.addDocument(doc);
			this.keyColumnIndexWriter.commit();
		} catch (IOException e) {
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
	 */
	public boolean writeTableToColumnNameIndex(Table table, File dataFile, HashMap<String, HashMap<String, Integer>> distinctTableValues){
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
	 */
	public boolean writeTableToTableIndex(Table table, File dataFile, HashMap<String, HashMap<String, Integer>> distinctTableValues){
		boolean success = true;
		
		int rownumber = 1;
		
		for (TableRow row: table.getRows()){
			for (int columnIndex = 0; columnIndex< table.getSchema().getSize(); columnIndex++){
				
				String columnvalue = "null";
				if (row.get(columnIndex) != null) columnvalue = row.get(columnIndex).toString();
				
				String columnname = table.getSchema().get(columnIndex).getHeader();
				String dataType =  table.getSchema().get(columnIndex).getDataType().toString();	
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
					e.printStackTrace();
					success = false;
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
}
