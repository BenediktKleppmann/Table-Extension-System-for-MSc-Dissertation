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
 package extendedSearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.google.gson.Gson;
import com.rapidminer.extension.json.Correspondence;
import com.rapidminer.extension.json.JSONTableResponse;
import com.rapidminer.extension.json.MetaDataTable;
import com.rapidminer.extension.json.TableInformation;

import extendedSearch.DS4DMBasicMatcherX;
import de.mannheim.uni.types.ColumnTypeGuesser.ColumnDataType;
import de.mannheim.uni.utils.TableColumnTypeGuesser;
import de.mannheim.uni.ds4dm.utils.ReadWriteGson;

public class GetTableInformationX {

	public enum DataSource {DIREKTMATCH, INDIREKTMATCH}
	
	 public static TableInformation getTableInformation_FromLucene(String tableName, DS4DMBasicMatcherX matcher, double tableScore, DataSource dataSource, Map<String, Correspondence> new_instancesCorrespondences2QueryTable, String repositoryName, String extensionAttribute) throws IOException  
	    {
		 	String keyColumnName = "";
		 	
		 	//--- Open The Index -------------------	
//		 	String indexPath = "C:/Users/UNI-Mannheim/Documents/DS4DM_backend/ds4dm_webservice/DS4DM/DS4DM_webservice/public/repositories/" + repositoryName + "/indexes/TableIndex";
		 	String indexPath = "public/repositories/" + repositoryName + "/indexes/TableIndex";
		 	Directory directory = FSDirectory.open(new File(indexPath));
	        DirectoryReader ireader = DirectoryReader.open(directory);
	        IndexSearcher isearcher = new IndexSearcher(ireader);

	        
	        //--- Run The Query -------------------
		    Query query = new WildcardQuery(new Term("tableHeader", tableName)); 
		    ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
		    
		    if (hits.length == 0){
		    	System.out.println("No entries found in the TableIndex for the table " + tableName);
		    	return null;
		    } else {
		    
			    //--- Read Results From Query -------------------
				Map<String,Map<Integer,String>> entries = new HashMap<String,Map<Integer,String>>();
				int maxRow = 0;
			    	
				
		        // Iterate through the results:
		        for (int i = 0; i < hits.length; i++) {
		        	Document doc = isearcher.doc(hits[i].doc);
		        	
		        	//get values	
		        	String columnHeader = doc.getFields("columnHeader")[0].stringValue();
		        	String  id_string = doc.getFields("id")[0].stringValue();
		        	Integer id = Math.round(Float.parseFloat(id_string));
		        	String originalValue = doc.getFields("originalValue")[0].stringValue();
		        	
		        	if (doc.getFields("isPrimaryKey")[0].stringValue().toLowerCase().equals("true")){
		        		keyColumnName = doc.getFields("columnHeader")[0].stringValue();
		        	}
	
		        	// write values into entries-hashMap
					if (!entries.containsKey(columnHeader))
						entries.put(columnHeader, new HashMap<Integer,String>() );
					entries.get(columnHeader).put(id, originalValue);
					
					if (id>maxRow) maxRow = id;				
		        }
		        
		       
		        ireader.close();
		        directory.close();
	
		        //--- Deduce Some Further Values -------------------
		        String[][] relation = convertEntriesToRelation(entries, maxRow);
	
		        String[] columnHeaders = getColumnHeaders(relation);
		        Integer keyColumnIndex = getKeyColumnIndex(relation, keyColumnName);
		        String[] keyColumn = relation[keyColumnIndex];
	
		        //getTableSchema2TargetSchema
				Gson gson = new Gson();	
				String extensionAttributePositions_string = new Scanner(new File("public/repositories/" + repositoryName + "/extensionAttributePositions/" + extensionAttribute +".json")).useDelimiter("\\Z").next();
				HashMap<String, String> extensionAttributePositions = gson.fromJson(extensionAttributePositions_string, HashMap.class);
				String extensionAttributePosition = extensionAttributePositions.get(tableName);
		        
		    	
		        //--- Save TableInformation -------------------

				BufferedReader fileReader = new BufferedReader(new FileReader("public/repositories/" + repositoryName + "/tables/" + tableName));
				String filesHeadersString = fileReader.readLine();
				String[] filesHeaders = filesHeadersString.split(",");
				String foundExtensionColumnName =filesHeaders[Integer.valueOf(extensionAttributePosition)].toLowerCase().replace("\"", "").replace(".", "");
				extensionAttributePosition = String.valueOf(Arrays.asList(columnHeaders).indexOf(foundExtensionColumnName));

			
//				System.out.println("^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v");
//				System.out.println("^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v");
//				System.out.println("tableName:  " + tableName);
//				System.out.println("columnHeaders:  " + Arrays.toString(columnHeaders));
//				System.out.println("extensionAttributePosition: " + extensionAttributePosition);
//				System.out.println("foundExtensionColumnName: " + foundExtensionColumnName);
//				System.out.println("^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v");
//				System.out.println("^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v^v");

		        TableInformation tm_ds4dm = new TableInformation();
		        tm_ds4dm.setTableSchema2TargetSchema(getTableSchema2TargetSchema(columnHeaders, matcher, extensionAttributePosition, foundExtensionColumnName,  extensionAttribute));
		        tm_ds4dm.setTableName(tableName);
		        
		        
				// Get instancesCorrespondences2QueryTable ----------------  
		        Map<String, Correspondence> instancesCorrespondences2QueryTable = new HashMap<String, Correspondence>();
		        if (new_instancesCorrespondences2QueryTable==null){
		        	
					for (int i = 1; i < keyColumn.length; i++) {
						
						
						try {
							String foundTableValue = keyColumn[i];
							foundTableValue = foundTableValue.toLowerCase();

							System.out.println("---Subject Column Values -------------------------------------");
							for(String subjectColumn_value :matcher.getSubjectsFromQueryTable()){System.out.print(subjectColumn_value + ", ");}
							System.out.println("---found key Column Values -------------------------------------");
							for(String foundKeyColumn_value :keyColumn){System.out.print(foundKeyColumn_value + ", ");}
							System.out.println("-------------------------- -------------------------------------");
							
							if (matcher.getSubjectsFromQueryTable().contains(foundTableValue)) {
								String matched = Integer.toString(matcher.getSubjectsFromQueryTable().indexOf(foundTableValue) + 1);       // here subjectsFromQueryTable have the first row removed (as it's the header) so need to +1 on the index
								instancesCorrespondences2QueryTable.put(Integer.toString(i), new Correspondence(matched, 0.99));
								
								//vvvvvvvvvvvvvvvvvvvvvvvvvvvv  TESTING  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv						
//								System.out.println("{'tableName':" + tableName + ", 'Value':" + keyColumn[i].toLowerCase() + " , 'QT':" + matched + " , 'FT':" + Integer.toString(i)  + " },");
//								System.out.println(Arrays.toString(keyColumn));
								// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
							}
						} catch (NullPointerException e) {}

					}
					
					System.out.println("new instances Correspondences " + String.valueOf(instancesCorrespondences2QueryTable.size()));
		        } else {
		        	instancesCorrespondences2QueryTable = new_instancesCorrespondences2QueryTable;
//		        	System.out.println("corresponding table has instance-matches =  " +  String.valueOf(!instancesCorrespondences2QueryTable.isEmpty()) + "");
		        }
		        
				// Save MetaDataTable in JSON-file  -----------------------------------------
				// and Add instancesCorrespondences2QueryTable to the TableInformation
		        System.out.println("saving Meta Data Table1");
		        if (!instancesCorrespondences2QueryTable.isEmpty()) {
		        	System.out.println("saving Meta Data Table2");
					tm_ds4dm.setInstancesCorrespondences2QueryTable(instancesCorrespondences2QueryTable);
					System.out.println("saving Meta Data Table3");
					saveMetaDataTable(instancesCorrespondences2QueryTable, matcher, relation, tableName, columnHeaders, keyColumn, keyColumnIndex, tableScore, dataSource, repositoryName);
					
		        }
		        else { return null; } // if there weren't any corresponding instances between the query table and the candidateTable, then don't add the candidateTablet othe relevant_tables
		        System.out.println("getting table info7");
		        System.out.println("1originally found table: " + tableName);
		        if (tm_ds4dm != null) System.out.println("2originally found table: " + tableName);
		        
		        return tm_ds4dm;
		    }
	    }
	 
	 
	 
	 
		 public static String[][] convertEntriesToRelation(Map<String,Map<Integer,String>> entries, int maxRow){
			String [][] relation = new String [entries.size()][maxRow+2];
	
			int c =0;
			for (Entry<String, Map<Integer, String>> e : entries.entrySet()){
				relation [c][0]= e.getKey();
				for (Entry<Integer, String> val : e.getValue().entrySet()){
					int rowindex = val.getKey();
					rowindex = rowindex+1;
					//TODO check
					try {
						relation [c][rowindex]= val.getValue();
					} catch (Exception e1) {
						System.err.println("error at column = "+c+" row = "+rowindex);
						System.err.println("size "+relation.length+" row = "+relation[c].length);
						e1.printStackTrace();
					}
				}
				c++;
			}
			return relation;
		 }
		 
		 
		 
	    public static String[] getColumnHeaders(String[][] relation) {
	        String[] headers = null;
	        headers = new String[relation.length];
	    
	        for (int col = 0; col < relation.length; col++) {
	            headers[col] = relation[col][0];
	        }
	        return headers;
	    }
	        
	    
	    public static Integer getKeyColumnIndex(String[][] relation, String keyColumnName) {
	    	Integer keyColumnIndex = 0;
	    
	        for (int col = 0; col < relation.length; col++) {
	        	
	            if (relation[col][0].toLowerCase().equals(keyColumnName.toLowerCase())) {
	            	keyColumnIndex = col;
	            }
	        }
	        return keyColumnIndex;
	    }
		 
	        
	        
	 
		public static void saveMetaDataTable(Map<String, Correspondence> instancesCorrespondences2QueryTable, DS4DMBasicMatcherX matcher, String[][] relation, String tableName, String[] columnHeaders, String[] keyColumn, Integer keyColumnIndex, double tableScore, DataSource dataSource, String repositoryName) throws IOException 
		{
			System.out.println("saving Meta Data Table4");
			Date lastModified = new Date(System.currentTimeMillis());
			
			
			String textBeforeTable = "";
			String textAfterTable = "";
			
			String title = "";
			double coverage = (double) instancesCorrespondences2QueryTable.size()/ matcher.getSubjectsFromQueryTable().size();
			double ratio = (double) instancesCorrespondences2QueryTable.size() / keyColumn.length;
			double trust = 1; 
			double emptyValues = 0;
			
			String matchingType = "";
			if (dataSource==DataSource.DIREKTMATCH) matchingType = "direktMatch";
			else matchingType = "indirektMatch";
			
			MetaDataTable meta = new MetaDataTable(tableScore, 
												   lastModified.toString(),
												   matchingType, //table.getValue().getUrl()
												   textBeforeTable, 
												   textAfterTable, 
												   title, 
												   coverage, 
												   ratio,
												   trust, 
												   emptyValues);
			
		
//			-------------------------------------------------------------------------------------------------------------------
			
			
			JSONTableResponse t_sab = new JSONTableResponse();
			t_sab.setMetaData(meta);
			t_sab.setHasHeader(true);
			t_sab.setHasKeyColumn(true);
			t_sab.setHeaderRowIndex(Integer.toString(0));
			t_sab.setTableName(tableName);
			
			//t_sab.setKeyColumnIndex			
			String indexCol = Integer.toString(keyColumnIndex) + "_" + keyColumn[0];
			t_sab.setKeyColumnIndex(indexCol);
			
			
			//t_sab.setRelation		
			System.out.println("saving Meta Data Table5");
			List<List<String>> new_relation = new ArrayList<List<String>>();

			for(int columnNumber =0; columnNumber < relation.length; columnNumber++){
				
				List<String> new_column = Arrays.asList(relation[columnNumber]);
				String indexedHeader = columnNumber + "_" + new_column.get(0);
				new_column.set(0, indexedHeader);
				
				new_relation.add(new_column);
			}
			t_sab.setRelation(new_relation);
			
			
			//t_sab.setDataTypes
			Map<String, String> dataTypes = guessTypes(new_relation);
			t_sab.setDataTypes(dataTypes);

			
			//t_sab.setMetaData
			meta.setURL(""); // table.getValue().getUrl()
			t_sab.setMetaData(meta);
			

//			-------------------------------------------------------------------------------------------------------------------
			System.out.println("saving Meta Data Table6");
			ReadWriteGson<JSONTableResponse> resp = new ReadWriteGson<JSONTableResponse>(t_sab);
			
			File fetchedTablesFolder = new File("public/repositories/" + repositoryName + "/fetchedTables");
			if (!fetchedTablesFolder.exists())
				fetchedTablesFolder.mkdirs();
			File current_table = new File(fetchedTablesFolder.getAbsolutePath() + "/" + tableName);
			System.out.println("saving Meta Data Table7");
			resp.writeJson(current_table);
			System.out.println("saving Meta Data Table8");
		}

		
		
		
		public static Map<String, String> guessTypes (List<List<String>> relation){
			Map<String, String> dataTypes = new HashMap<String, String>();
			TableColumnTypeGuesser tctg = new TableColumnTypeGuesser();
			for (List<String> e: relation){
				ColumnDataType type = ColumnDataType.string;;
				String columnName = e.get(0);
				List<String> columnValues = new ArrayList<String>();
				columnValues.addAll(e);
				columnValues.remove(0);
				type = tctg.guessTypeForColumn(columnValues, columnName, false, null);
				dataTypes.put(columnName, type.toString());
			}
			return dataTypes;
		}
		
		
		

            
            
            

	 
	 public static Map<String, Correspondence> getTableSchema2TargetSchema(String[] columnHeaders, DS4DMBasicMatcherX matcher, String extensionAttributePosition, String foundExtensionColumnName, String extensionAttribute) {
	 
		 	Map<String, Correspondence> tableSchema2TargetSchema = new HashMap<String, Correspondence>();
		 

			
			for (int i = 0; i < columnHeaders.length; i++) {
				
				String currentAttribute = columnHeaders[i].toLowerCase();
				if (matcher.getNormalizedTargetSchema().contains(currentAttribute)) {
					double confidence = 1; 
					int matchedIndex = matcher.getNormalizedTargetSchema().indexOf(currentAttribute);
					tableSchema2TargetSchema.put(Integer.toString(i) + "_" + columnHeaders[i], new Correspondence(matcher.getTargetSchema().get(matchedIndex), confidence));
				}
			}
			
			
			if (extensionAttributePosition != null && !extensionAttributePosition.isEmpty()){
				double confidence = 1; 
				tableSchema2TargetSchema.put(extensionAttributePosition + "_" + foundExtensionColumnName,  new Correspondence(extensionAttribute, confidence));
			}

					
					
		 return tableSchema2TargetSchema;
	 }
	 
	 
}
