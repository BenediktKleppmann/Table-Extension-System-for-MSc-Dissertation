package de.mannheim.uni.ds4dm.refactoredSearch;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

public class SearchForTables {
	
	
	
	
	
	public static Set<String> searchForTables_InLucene(String keyColumnName) throws IOException {
		

		//Get the queryParser ----------------------		
		IndexReader indexReader = null;
		IndexSearcher indexSearcher = null;
		QueryParser queryParser = null;
		
		if (indexSearcher == null) {
//			Directory dir = FSDirectory.open(new File("/home/agentile/wiki_500kTables/web_wiki_btc_attributes"));
			Directory dir = FSDirectory.open(new File("C:/Users/UNI-Mannheim/Documents/DS4DM_backend2/wiki_500kTables/web_wiki_btc_attributes"));
			indexReader = DirectoryReader.open(dir);
			indexSearcher = new IndexSearcher(indexReader);
			queryParser = new QueryParser(Version.LUCENE_46, "value", new StandardAnalyzer(Version.LUCENE_46));
		}

		
		//==============================    GET THE TABLENAMES	 ===========================================================	
		Set<String> candidateTableNames = new HashSet<String>();
			
		if (keyColumnName.equals("") || keyColumnName.equalsIgnoreCase("null")){ 
			return candidateTableNames;
		}
		else {
				
				//Escape the query string  ----------------------
				keyColumnName = queryParser.escape(keyColumnName);
				
				//Build the Query ----------------------
				BooleanQuery constrainedQuery = new BooleanQuery();
				Query q = null;
				try {
					q = queryParser.parse(keyColumnName);
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
					candidateTableNames.add(doc.getFields("tableHeader")[0].stringValue());
				}
		}
			
		return candidateTableNames; 
	}
	
}
