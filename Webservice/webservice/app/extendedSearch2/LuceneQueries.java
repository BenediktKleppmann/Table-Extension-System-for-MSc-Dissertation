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
 package extendedSearch2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.rapidminer.extension.json.Correspondence;

import au.com.bytecode.opencsv.CSVWriter;
import de.mannheim.uni.ds4dm.normalizer.StringNormalizer;
import de.uni_mannheim.informatik.dws.winter.similarity.string.GeneralisedStringJaccard;
import de.uni_mannheim.informatik.dws.winter.similarity.string.LevenshteinSimilarity;
import extendedSearch.StringNormalization;
import model.ExtendedTableInformation;

public class LuceneQueries {
	
	
	
	public static Map<String, ExtendedTableInformation> searchForTables_InLucene(GlobalVariables globalVariables) throws IOException {
		System.out.println("searchForTables_InLucene: getColumnNameIndexPath = " + globalVariables.getColumnNameIndexPath());
		//Get the queryParser ----------------------		
		String indexPath = globalVariables.getColumnNameIndexPath();
		Directory dir = FSDirectory.open(new File(indexPath));
		IndexReader indexReader = DirectoryReader.open(dir);
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		QueryParser queryParser = new QueryParser(Version.LUCENE_46, "value", new StandardAnalyzer(Version.LUCENE_46));


		//==============================    GET THE TABLENAMES	 ===========================================================	
		Map<String, ExtendedTableInformation> directlyFoundTables = new HashMap<String, ExtendedTableInformation>();
			
		String queryString = globalVariables.getQueryTable().getExtensionAttribute();
		
		
		if (queryString.equals("") || queryString.equalsIgnoreCase("null")){ 
			return directlyFoundTables;
		}
		else {
				
				//Escape the query string  ----------------------
				queryString = queryParser.escape(queryString);
				queryString = queryString.replace("_", " ");
				
				//Build the Query ----------------------
				System.out.println("querying for: " + queryString);
				BooleanQuery constrainedQuery = new BooleanQuery();
				Query q = null;
				try {
					q = queryParser.parse(queryString);
				} catch (ParseException e) { e.printStackTrace(); }
				constrainedQuery.add(q, BooleanClause.Occur.MUST);
	
				
				
				//Search for more and more results until... ---------------
				// one of the following:				
				// 1) no matches were found
				// 2) there were matches with different scores
				// 3) it looked for up to 100000 tables
				
				ScoreDoc[] hits = null;
				int numResults = 1000;
				if (globalVariables.getQueryTable().getMaximalNumberOfTables()>0)   numResults = globalVariables.getQueryTable().getMaximalNumberOfTables();
				

				hits = indexSearcher.search(constrainedQuery, numResults).scoreDocs;

				System.out.println("numResults = " + java.lang.Math.min(hits.length,numResults));
				System.out.println("numResults = " + numResults);
				
				//get the Table Information ----------------------
				for (int i = 0; i < java.lang.Math.min(hits.length,numResults); i++) {
					Document doc = indexSearcher.doc(hits[i].doc);
					
					System.out.println("found" + String.valueOf(i) + ": " + doc.getFields("tableHeader")[0].stringValue());
					ExtendedTableInformation foundTable = new ExtendedTableInformation();
					foundTable.setTableName(doc.getFields("tableHeader")[0].stringValue());
					foundTable.setSchemaSimilarityScore((double) hits[i].score);
					foundTable.setExtensionAttributePosition(String.valueOf(doc.getFields("columnindex")[0].stringValue()));
					foundTable.setMatchingType("directMatch");
					
					directlyFoundTables.put(foundTable.getTableName(), foundTable);
				}
		}

		return directlyFoundTables; 
	}
	
	
	
	
	
	
	public static Map<String, ExtendedTableInformation> addTheTableInformation_FromLucene(Map<String, ExtendedTableInformation> tables, GlobalVariables globalVariables) throws IOException{

	 	String indexPath = "public/repositories/" + globalVariables.getRepositoryName() + "/indexes/TableIndex";
	 	Directory directory = FSDirectory.open(new File(indexPath));
        DirectoryReader ireader = DirectoryReader.open(directory);
        IndexSearcher isearcher = new IndexSearcher(ireader);

        Map<String, ExtendedTableInformation> updatedTables = new HashMap<String, ExtendedTableInformation>();

        
		for (ExtendedTableInformation table: tables.values()){
			
	        //--- Run The Query -------------------
		    Query query = new WildcardQuery(new Term("tableHeader", table.getTableName())); 
		    ScoreDoc[] hits = null;
			try {
				hits = isearcher.search(query, null, 1000).scoreDocs;
			} catch (IOException e1) {}
		    
		    if (hits == null || hits.length == 0){
		    	System.out.println("No entries found in the TableIndex for the table " + table.getTableName());
		    } else {
		    
			    //--- Read Results From Query -------------------
				Map<String,Map<Integer,String>> entries = new HashMap<String,Map<Integer,String>>();
				int maxRow = 0;
			    	
				
		        // Iterate through the results:
				String keyColumnName = "";
				
		        for (int i = 0; i < hits.length; i++) {
		        	try {
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
					
		        	} catch (IOException e) {}
		        }
		        
		        //--- Deduce Some Further Values -------------------
		        String[][] relation = convertEntriesToRelation(entries, maxRow);
		        String[] columnHeaders = getColumnHeaders(relation);
		        Integer keyColumnIndex = getKeyColumnIndex(relation, keyColumnName);
		        String[] keyColumn = relation[keyColumnIndex];
		        System.out.println("getting tableinformation for " + table.getTableName() + ":   keyColumnName = " + keyColumn[0] + ", keyColumnIndex" + keyColumnIndex);

		        table.setRelation(relation);
		        table.setKeyColumnIndex(keyColumnIndex);
		        
		        
		        //get Schema Matches
				String[] normalizedTargetSchema = new String[globalVariables.getQueryTable().getNormalizedTargetSchema().size()];
				normalizedTargetSchema = globalVariables.getQueryTable().getNormalizedTargetSchema().toArray(normalizedTargetSchema);
		        System.out.println("Query-Table Schema = " + Arrays.toString(normalizedTargetSchema));
		        table.setTableSchema2TargetSchema(determineTableSchema2TargetSchema(table, globalVariables));
		        
		        //get Instance Matches
		        table.setInstancesCorrespondences2QueryTable(determineInstancesCorrespondences2QueryTable(table, globalVariables));
		        
		        //Filter: only keep those with instance matches
		        if (!table.getInstancesCorrespondences2QueryTable().isEmpty()){
		        	System.out.println("Table with Instance matches " + table.getTableName() );
		        	updatedTables.put(table.getTableName(), table);
		        } else {
		        	System.out.println("No Instance matches for " + table.getTableName() );
		        }
		        
		        
		    }
		    
		}
		return updatedTables;
	}
	
	
	public static Map<String, Correspondence> determineInstancesCorrespondences2QueryTable(ExtendedTableInformation table, GlobalVariables globalVariables) throws IOException {
		
		String[] foundKeyColumn  = table.getKeyColumn();
		Integer queryTableKeyColumnIndex  = Integer.valueOf(globalVariables.getQueryTable().getKeyColumnIndex());
		List<String> queryTableKeyColumn = Arrays.asList(globalVariables.getQueryTable().getNormalizedKeyColumn());
		StringNormalization stringNormalizer = new StringNormalization();
		GeneralisedStringJaccard sim = new GeneralisedStringJaccard(new LevenshteinSimilarity(), 0.8, 0.0);
		
		//vvvvvvvvvvvvvvvvvvvvvvvvvvvv  TESTING  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv	
//	     CSVWriter csvwriter = new CSVWriter(new FileWriter("C:/Users/UNI-Mannheim/Documents/Test Scripts/temporary files/2017-11-17/checking instance matches/" + table.getTableName()), ',');
//	     String[] header = {"normalizedQueryTableValue", "queryTableValue", "queryTableIndex","normalizedFoundTableValue", "foundTableValue", "foundTableIndex", "similarity_score"};
//	     csvwriter.writeNext(header);
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
		
        Map<String, Correspondence> instancesCorrespondences2QueryTable = new HashMap<String, Correspondence>();;
        
        System.out.println("Instance matching for "+ table.getTableName() + " , column: " + table.getColumnHeaders()[table.getKeyColumnIndex()] );
        System.out.println("Instance pairings = " + String.valueOf(foundKeyColumn.length) + " (from found table) x " + String.valueOf(queryTableKeyColumn.size()) + " (from query table)");
		for (int i = 1; i < foundKeyColumn.length; i++) {
			
			try {
				String foundTableValue = foundKeyColumn[i];
				String normalizedFoundTableValue = stringNormalizer.normalizeString(foundTableValue);
				

				for (String queryTableValue : queryTableKeyColumn){
					
					//vvvvvvvvvvvvvvvvvvvvvvvvvvvv  TESTING  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv	));
//					Integer queryTableIndex = queryTableKeyColumn.indexOf(queryTableValue);
//					String[] new_row = {queryTableValue, globalVariables.getQueryTable().getKeyColumn()[queryTableIndex], String.valueOf(queryTableIndex), normalizedFoundTableValue,  foundTableValue, String.valueOf(i), String.valueOf( sim.calculate(queryTableValue, normalizedFoundTableValue)) };	
//				    csvwriter.writeNext(new_row);
				    // ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
				    
					System.out.println("Instance-correspondence similarity " + table.getTableName() + ": " + foundTableValue + " [" + Integer.toString(i) + "]<-->" + queryTableValue + "  [" + Integer.toString(queryTableKeyColumn.indexOf(queryTableValue)) + "] (" + String.valueOf(sim.calculate(queryTableValue, normalizedFoundTableValue)) + ")" );
					
				     
					if ( sim.calculate(queryTableValue, normalizedFoundTableValue) >= 0.7) {
						String matched = Integer.toString(queryTableKeyColumn.indexOf(queryTableValue) );
						instancesCorrespondences2QueryTable.put(Integer.toString(i), new Correspondence(matched, 0.99));
						
					}
				}
			} catch (NullPointerException e) {}

		}
		
		//vvvvvvvvvvvvvvvvvvvvvvvvvvvv  TESTING  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv	
//	     csvwriter.close();
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^ 
	
		return instancesCorrespondences2QueryTable;
	}
	
	
	
	public static Map<String, Correspondence> determineTableSchema2TargetSchema(ExtendedTableInformation table, GlobalVariables globalVariables) {
		
		System.out.println("getting schema-matches for: " + table.getTableName() + " -  " + Arrays.toString(table.getColumnHeaders()));
		//PART1 - Determine the schema-matches through string equality
		StringNormalization stringNormalizer = new StringNormalization();
		GeneralisedStringJaccard sim = new GeneralisedStringJaccard(new LevenshteinSimilarity(), 0.8, 0.0);
		
		Map<String, Correspondence> tableSchema2TargetSchema = new HashMap<String, Correspondence>();
		String[] columnHeaders = table.getColumnHeaders();
		
		for (int i = 0; i < columnHeaders.length; i++) {
			String currentAttribute = stringNormalizer.normalizeString(columnHeaders[i].toLowerCase());
			
			for (int queryTableHeaderIndex = 0; queryTableHeaderIndex < globalVariables.getQueryTable().getNormalizedTargetSchema().size(); queryTableHeaderIndex++){
				String queryTableHeader = globalVariables.getQueryTable().getNormalizedTargetSchema().get(queryTableHeaderIndex);
				if (sim.calculate(currentAttribute, queryTableHeader) >= 0.7){
					double confidence = 1; 
					tableSchema2TargetSchema.put(Integer.toString(i) + "_" + columnHeaders[i], new Correspondence(globalVariables.getQueryTable().getTargetSchema().get(queryTableHeaderIndex), confidence));
					System.out.println("String-equality: " + table.getTableName() + " - FT:" + Integer.toString(i) + "_" + columnHeaders[i] + " <--> QT: " + queryTableHeaderIndex + "_" + globalVariables.getQueryTable().getTargetSchema().get(queryTableHeaderIndex));
				}
			}
			
		}
		
		
		//PART2 - If there isn't yet a schema-match for the extensionAttribute, use the previously found extensionAttributePosition
		//This priority is because the pre-calcualted correspondences aren't always all that brilliant, a direct string-match will usually be better
		String extensionAttributePosition = table.getExtensionAttributePosition();
		
		System.out.println("extensionAttributePosition1 " + table.getTableName() + " : " + extensionAttributePosition); 
		
		boolean extensionAttributeMatched = false;
		for (String key: tableSchema2TargetSchema.keySet()){
			if (tableSchema2TargetSchema.get(key).getMatching() == globalVariables.getQueryTable().getExtensionAttribute()) extensionAttributeMatched = true;
		}
		if (extensionAttributePosition != null && !extensionAttributePosition.isEmpty() && !extensionAttributeMatched){
			
			try{
				//Part2.1 - correct the extensionAttributePosition, according to the csv-file (in the index the columns got jumbled)
				System.out.println("extensionAttributePosition2 " + table.getTableName() );
				
				
				BufferedReader fileReader = new BufferedReader(new FileReader(globalVariables.getTablesFolderPath() + table.getTableName()));
				String filesHeadersString = fileReader.readLine();
				String[] filesHeaders = filesHeadersString.split(",");
				String foundExtensionColumnName =filesHeaders[Integer.valueOf(extensionAttributePosition)].toLowerCase().replace("\"", "").replace(".", "");
				extensionAttributePosition = String.valueOf(Arrays.asList(columnHeaders).indexOf(foundExtensionColumnName));
				fileReader.close();
				
				//Part2.2 - the acctural overwriting
				if (extensionAttributePosition != null && !extensionAttributePosition.isEmpty() && Integer.valueOf(extensionAttributePosition) > 0){
					double confidence = 1; 
					tableSchema2TargetSchema.put(extensionAttributePosition + "_" + columnHeaders[Integer.valueOf(extensionAttributePosition)],  new Correspondence(globalVariables.getQueryTable().getExtensionAttribute(), confidence));
					System.out.println("extensionAttributePosition3: " + table.getTableName() + " - FT:" + extensionAttributePosition + "_" + columnHeaders[Integer.valueOf(extensionAttributePosition)] + " <--> QT: " +  globalVariables.getQueryTable().getExtensionAttribute() );
				}
			} catch (IOException e){
				System.out.println("!!Table not found: " + globalVariables.getTablesFolderPath() + table.getTableName());
				e.printStackTrace();
				}
		}
		
        return tableSchema2TargetSchema;
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
	 
}
