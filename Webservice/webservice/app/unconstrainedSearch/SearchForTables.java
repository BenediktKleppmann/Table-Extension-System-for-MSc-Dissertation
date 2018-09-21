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
 package unconstrainedSearch;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import model.QueryTable;

public class SearchForTables {
	
	
	
	public static List<String> search(QueryTable queryTable, Integer keyColumnIndex, String repositoryName) {
		
		List<String> foundTablenames = new LinkedList<String>();
		System.out.println("Unconstrianed Search3");
		try{

			String indexPath = "public/repositories/" + repositoryName + "/indexes/KeyColumnIndex";
			Directory dir = FSDirectory.open(new File(indexPath));
			IndexReader indexReader = DirectoryReader.open(dir);
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			QueryParser queryParser = new QueryParser(Version.LUCENE_46, "keyColumnString", new StandardAnalyzer(Version.LUCENE_46));
			
			
			//get query string
			
			String[][] relation = queryTable.getQueryTable();
			String[] keyColumn = relation[keyColumnIndex];
			String originalKeyColumnString = StringUtils.join(keyColumn, " ");
			originalKeyColumnString = originalKeyColumnString.replaceAll("[^A-Za-z0-9 ]", "");
			String queryString  = uploadTable.FindCorrespondences.formatQueryString(originalKeyColumnString, 3);
			queryString = queryString.trim();

			
			Integer numResults = 1000;
			numResults = queryTable.getMaximalNumberOfTables();
			Query q = queryParser.parse(queryString);
			ScoreDoc[] hits = indexSearcher.search(q, numResults).scoreDocs;
			
			Double minimumKeyColumnSimilarity = queryTable.getMinimumKeyColumnSimilarity();
			if (minimumKeyColumnSimilarity == null) minimumKeyColumnSimilarity = 0.6;
			
			if(hits != null)
			{
				for (int i = 0; i < hits.length; i++) {
					if (hits[i].score >= minimumKeyColumnSimilarity){
						Document doc = indexSearcher.doc(hits[i].doc);	
						foundTablenames.add(doc.getField("tableHeader").stringValue());
						System.out.println("found table: " + doc.getField("tableHeader").stringValue() );
					}						
				}
	
			} else System.out.println("For this table no matches were found in the KeyColumnIndex");
			System.out.println("Unconstrianed Search6");
		} catch(Exception e) {
			e.printStackTrace();
			System.out.println("An error occurred while searching the KeyColumnIndex.");
		}
			
		return foundTablenames;
	}

	
	
	


}
