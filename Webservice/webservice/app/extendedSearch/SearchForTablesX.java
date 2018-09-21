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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.google.gson.Gson;

import au.com.bytecode.opencsv.CSVWriter;

public class SearchForTablesX {
	
	
	
	
	
	public static Map<String, Double> searchForTables_InLucene(String keyColumnName, String repositoryName) throws IOException {
		

		
		
		//Get the queryParser ----------------------		
		IndexReader indexReader = null;
		IndexSearcher indexSearcher = null;
		QueryParser queryParser = null;
		
		if (indexSearcher == null) {                  

//			String indexPath = "C:/Users/UNI-Mannheim/Documents/DS4DM_backend/ds4dm_webservice/DS4DM/DS4DM_webservice/public/repositories/" + repositoryName + "/indexes/ColumnNameIndex";
			String indexPath = "public/repositories/" + repositoryName + "/indexes/ColumnNameIndex";
			Directory dir = FSDirectory.open(new File(indexPath));
			indexReader = DirectoryReader.open(dir);
			indexSearcher = new IndexSearcher(indexReader);
			queryParser = new QueryParser(Version.LUCENE_46, "value", new StandardAnalyzer(Version.LUCENE_46));
		}
		
		
		// ExtensionAttributePositions to be saved in json-file to avoid the error-prone schema-matching
		HashMap<String, String> extensionAttributePositions = new HashMap<String, String>();
		



		
		//==============================    GET THE TABLENAMES	 ===========================================================	
		Map<String, Double> candidateTableNames = new LinkedHashMap<String, Double>();
			
		if (keyColumnName.equals("") || keyColumnName.equalsIgnoreCase("null")){ 
			return candidateTableNames;
		}
		else {
				
				//Escape the query string  ----------------------
				keyColumnName = queryParser.escape(keyColumnName);
				
				//Build the Query ----------------------
				System.out.println("querying for: " + keyColumnName);
				BooleanQuery constrainedQuery = new BooleanQuery();
				Query q = null;
				try {
					q = queryParser.parse(keyColumnName);
					System.out.println("QueryString = " + keyColumnName);
				} catch (ParseException e) { e.printStackTrace(); }
				constrainedQuery.add(q, BooleanClause.Occur.MUST);
	
				//Search for more and more results until... ---------------
				// one of the following:				
				// 1) no matches were found
				// 2) there were matches with different scores
				// 3) it looked for up to 100000 tables
				
				ScoreDoc[] hits = null;
				int numResults = 1000;
				
				do {
					hits = indexSearcher.search(constrainedQuery, numResults).scoreDocs;
					numResults *= 10;		
				} while (hits.length > 0 && (hits[0].score == hits[hits.length - 1].score) && numResults < 1000000 && numResults > 0);  // check if all the scores are the same; if yes, retrieve more documents
				
				
				//get the table names ----------------------
				for (int i = 0; i < hits.length; i++) {
					Document doc = indexSearcher.doc(hits[i].doc);
					double schemaSimilarityScore = hits[i].score;
					candidateTableNames.put(doc.getFields("tableHeader")[0].stringValue(), schemaSimilarityScore);
					System.out.println("found for ("+ keyColumnName +"): " + doc.getFields("tableHeader")[0].stringValue());
					
					String tablename = doc.getFields("tableHeader")[0].stringValue();
					String extensionAtttributeColumnIndex = doc.getFields("columnindex")[0].stringValue();
					extensionAttributePositions.put(tablename, extensionAtttributeColumnIndex);
				    
				}
				
				System.out.println("numberOfHits = " + String.valueOf(hits.length));
		}
		
		System.out.println("numberOfFoundTables = " + String.valueOf(candidateTableNames.size()));
		
		
		// save the extensionAttributePositions to a json-file to avoid the error-prone schema-matching later
		Gson gson = new Gson();
		String extensionAttributePositions_string = gson.toJson(extensionAttributePositions, HashMap.class);
		
		String extensionAttributePositionsFilePath = "public/repositories/" + repositoryName + "/extensionAttributePositions/" + keyColumnName +".json";
		File extensionAttributePositionsFile = new File(extensionAttributePositionsFilePath);
        try {
    		extensionAttributePositionsFile.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            PrintWriter pw = new PrintWriter(extensionAttributePositionsFile);
            pw.println(extensionAttributePositions_string);
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
		


		
		return candidateTableNames; 
	}
	
	
	
	
	
//	public static Map<String, Double> searchForTables_InLucene_byKeyColumn(String keyColumnName, String repositoryName) throws IOException {
//		
//
//		//Get the queryParser ----------------------		
//		IndexReader indexReader = null;
//		IndexSearcher indexSearcher = null;
//		QueryParser queryParser = null;
//		
//		if (indexSearcher == null) {                  
//
////			String indexPath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/Web Wiki Tables/indexes/ColumnNameIndex"
////			String indexPath = "public/repositories/" + repositoryName + "/indexes/KeyColumnIndex";
//			String indexPath = "C:/Users/UNI-Mannheim/Documents/DS4DM_backend/ds4dm_webservice/DS4DM/DS4DM_webservice/public/repositories/" + repositoryName + "/indexes/KeyColumnIndex";
//			Directory dir = FSDirectory.open(new File(indexPath));
//			indexReader = DirectoryReader.open(dir);
//			indexSearcher = new IndexSearcher(indexReader);
//			queryParser = new QueryParser(Version.LUCENE_46, "value", new StandardAnalyzer(Version.LUCENE_46));
//		}
//
//		
//		//==============================    GET THE TABLENAMES	 ===========================================================	
//		Map<String, Double> candidateTableNames = new LinkedHashMap<String, Double>();
//			
//		if (keyColumnName.equals("") || keyColumnName.equalsIgnoreCase("null")){ 
//			return candidateTableNames;
//		}
//		else {
//				
//				//Escape the query string  ----------------------
//				keyColumnName = queryParser.escape(keyColumnName);
//				
//				//Build the Query ----------------------
//				System.out.println("querying for: " + keyColumnName);
//				BooleanQuery constrainedQuery = new BooleanQuery();
//				Query q = null;
//				try {
//					q = queryParser.parse(keyColumnName);
//					System.out.println("QueryString = " + keyColumnName);
//				} catch (ParseException e) { e.printStackTrace(); }
//				constrainedQuery.add(q, BooleanClause.Occur.MUST);
//	
//				
//				//Search for more and more results until... ---------------
//				// one of the following:				
//				// 1) no matches were found
//				// 2) there were matches with different scores
//				// 3) it looked for up to 100000 tables
//				
//				ScoreDoc[] hits = null;
//				int numResults = 1000;
//				
//				do {
//					hits = indexSearcher.search(constrainedQuery, numResults).scoreDocs;
//					numResults *= 10;		
//				} while (hits.length > 0 && (hits[0].score == hits[hits.length - 1].score) && numResults < 1000000 && numResults > 0);  // check if all the scores are the same; if yes, retrieve more documents
//				
//				
//				//get the table names ----------------------
//				for (int i = 0; i < hits.length; i++) {
//					Document doc = indexSearcher.doc(hits[i].doc);
//					double schemaSimilarityScore = hits[i].score;
//					candidateTableNames.put(doc.getFields("tableHeader")[0].stringValue(), schemaSimilarityScore);
//					System.out.println("found: " + doc.getFields("tableHeader")[0].stringValue());
//				}
//		}
//			
//		return candidateTableNames; 
//	}
	
	
	
	
}
