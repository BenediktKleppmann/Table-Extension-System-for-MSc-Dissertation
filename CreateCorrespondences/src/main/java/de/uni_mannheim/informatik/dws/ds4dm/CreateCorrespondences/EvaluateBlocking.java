package de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.opencsv.CSVWriter;

import de.uni_mannheim.informatik.additionalWinterClasses.MatchableTableColumn;
import de.uni_mannheim.informatik.additionalWinterClasses.MatchableTableRow;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.webtables.WebTablesStringNormalizer;

public class EvaluateBlocking {

	
	public static void main(String[] args) throws IOException, ParseException{
		
		
		//Parameters
		int numResults = 5;
		int maxEditDistance = 3;
		String keyColumnIndexFolder = "C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-07-04 test release/createLuceneIndexes/indexFolder/KeyColumnIndex/";
		
		// open index
		Directory directory = FSDirectory.open(new File(keyColumnIndexFolder));
        DirectoryReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
        System.out.println("Length = " + indexReader.maxDoc());
        
        //prepare the csv writer
	     CSVWriter writer = new CSVWriter(new FileWriter("C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-07-04 test release/evaluate blocking with KeyColumnIndex/input/blocked_tables.csv"));
	     String[] header = {"table1","table2", "similarityScore"};
	     writer.writeNext(header);

        
        
        
        for (int i=0; i<indexReader.maxDoc(); i++) {
		    Document doc = indexReader.document(i);
		    
		    // get the table-values
		    String originalTableHeader =doc.getFields("tableHeader")[0].stringValue();		
			String originalKeyColumnString =doc.getFields("keyColumnString")[0].stringValue();
			String queryString  = formatQueryString(originalKeyColumnString, maxEditDistance);

			
			if(queryString.trim().length()>0) {
			    QueryParser queryParser = new QueryParser(Version.LUCENE_46, "keyColumnString", new StandardAnalyzer(Version.LUCENE_46));
				Query q = queryParser.parse(queryString);
				ScoreDoc[] hits = indexSearcher.search(q, numResults).scoreDocs;
				
				for (ScoreDoc hit : hits){
					Document foundDoc = indexSearcher.doc(hit.doc);
					if (! foundDoc.getFields("tableHeader")[0].stringValue().equals(originalTableHeader)){
					     
				    	 String[] newRow = {originalTableHeader, foundDoc.getFields("tableHeader")[0].stringValue(), String.valueOf(hit.score)};
				    	 writer.writeNext(newRow);

					
					}
				}
			}
		}	
        writer.close();					
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
}
