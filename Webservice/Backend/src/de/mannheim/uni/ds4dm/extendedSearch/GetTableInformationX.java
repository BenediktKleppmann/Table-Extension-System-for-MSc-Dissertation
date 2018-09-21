package de.mannheim.uni.ds4dm.extendedSearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.rapidminer.extension.json.Correspondence;
import com.rapidminer.extension.json.JSONTableResponse;
import com.rapidminer.extension.json.MetaDataTable;
import com.rapidminer.extension.json.TableInformation;

import de.mannheim.uni.ds4dm.extendedSearch.DS4DMBasicMatcherX;
import de.mannheim.uni.types.ColumnTypeGuesser.ColumnDataType;
import de.mannheim.uni.utils.TableColumnTypeGuesser;
import de.mannheim.uni.ds4dm.utils.ReadWriteGson;

public class GetTableInformationX {

	
	
	 public static TableInformation getTableInformation_FromLucene(String tableName, DS4DMBasicMatcherX matcher, String keyColumnName) throws IOException  
	    {
		 	
		 	//--- Open The Index -------------------		 
	        Directory directory = FSDirectory.open(new File("C:/Users/UNI-Mannheim/Documents/DS4DM_backend2/goldstandardTables/IndexOfValues"));
//	        Directory directory = FSDirectory.open(new File("/home/agentile/goldstandardTables/IndexOfValues"));
	        DirectoryReader ireader = DirectoryReader.open(directory);
	        IndexSearcher isearcher = new IndexSearcher(ireader);

	        
	        //--- Run The Query -------------------
	        tableName = tableName.replace(".json", "*");
		    Query query = new WildcardQuery(new Term("tableHeader", tableName)); 
		    ScoreDoc[] hits = isearcher.search(query, null, 1000).scoreDocs;
		    tableName = tableName.replace("*", ".json");
		    
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


	        //--- Save TableInformation -------------------
	        TableInformation tm_ds4dm = new TableInformation();
	        tm_ds4dm.setTableSchema2TargetSchema(getTableSchema2TargetSchema(columnHeaders, matcher));
	        tm_ds4dm.setTableName(tableName);
	        
	        
			// Get instancesCorrespondences2QueryTable ----------------
	        System.out.println("tableName: " + tableName + "------------------------------------------");
	        System.out.println("key column " + keyColumnIndex + "  " +  columnHeaders[keyColumnIndex]);
			Map<String, Correspondence> instancesCorrespondences2QueryTable = new HashMap<String, Correspondence>();
			for (int i = 1; i < keyColumn.length; i++) {
//				System.out.println(keyColumn[i]);
				if (matcher.getSubjectsFromQueryTable().contains(keyColumn[i])) {
					String matched = Integer.toString(matcher.getSubjectsFromQueryTable().indexOf(keyColumn[i]) + 1);       // here subjectsFromQueryTable have the first row removed (as it's the header) so need to +1 on the index
					instancesCorrespondences2QueryTable.put(Integer.toString(i), new Correspondence(matched, 0.99));
				}
			}

			
			// Save MetaDataTable in JSON-file  -----------------------------------------
			// and Add instancesCorrespondences2QueryTable to the TableInformation
	        if (!instancesCorrespondences2QueryTable.isEmpty()) {
				tm_ds4dm.setInstancesCorrespondences2QueryTable(instancesCorrespondences2QueryTable);
				saveMetaDataTable(instancesCorrespondences2QueryTable, matcher, relation, tableName, columnHeaders, keyColumn, keyColumnIndex);
				
	        }
	        else { tm_ds4dm =  null; } // if there weren't any corresponding instances between the query table and the candidateTable, then don't add the candidateTablet othe relevant_tables
	        
	        return tm_ds4dm;
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
	            if (relation[col][0].toLowerCase() == keyColumnName.toLowerCase()) {
	            	keyColumnIndex = col;
	            }
	        }
	        return keyColumnIndex;
	    }
		 
	        
	        
	 
		public static void saveMetaDataTable(Map<String, Correspondence> instancesCorrespondences2QueryTable, DS4DMBasicMatcherX matcher, String[][] relation, String tableName, String[] columnHeaders, String[] keyColumn, Integer keyColumnIndex) throws IOException 
		{
			double tableScore = 0;
			Date lastModified = new Date(System.currentTimeMillis());
			
			
			String textBeforeTable = "";
			String textAfterTable = "";
			
			String title = "";
			double coverage = (double) instancesCorrespondences2QueryTable.size()/ matcher.getSubjectsFromQueryTable().size();
			double ratio = (double) instancesCorrespondences2QueryTable.size() / keyColumn.length;
			double trust = 1; 
			double emptyValues = 0;
			
			
			MetaDataTable meta = new MetaDataTable(tableScore, 
												   lastModified.toString(),
												   "", //table.getValue().getUrl()
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
			ReadWriteGson<JSONTableResponse> resp = new ReadWriteGson<JSONTableResponse>(t_sab);
			
			File fetchedTablesFolder = new File("public/exampleData/tables");
			if (!fetchedTablesFolder.exists())
				fetchedTablesFolder.mkdirs();
			File current_table = new File(fetchedTablesFolder.getAbsolutePath() + "/" + tableName);
			resp.writeJson(current_table);
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
		
		
		

            
            
            

	 
	 public static Map<String, Correspondence> getTableSchema2TargetSchema(String[] columnHeaders, DS4DMBasicMatcherX matcher) {
	 
		 	Map<String, Correspondence> tableSchema2TargetSchema = new HashMap<String, Correspondence>();
		 

			
			for (int i = 0; i < columnHeaders.length; i++) {
				
				String currentAttribute = columnHeaders[i].toLowerCase();
				if (matcher.getNormalizedTargetSchema().contains(currentAttribute)) {
					double confidence = 1; 
					int matchedIndex = matcher.getNormalizedTargetSchema().indexOf(currentAttribute);
					tableSchema2TargetSchema.put(Integer.toString(i) + "_" + columnHeaders[i], new Correspondence(matcher.getTargetSchema().get(matchedIndex), confidence));
				}
			}

					
					
		 return tableSchema2TargetSchema;
	 }
	 
	 
}
