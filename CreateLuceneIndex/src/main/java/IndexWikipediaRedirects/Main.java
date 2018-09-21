package IndexWikipediaRedirects;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

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

public class Main {

	public static void main(String[] args){
		
		try {
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
	    	IndexWriterConfig config1 = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
			String indexPath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/Redirects & Surface forms/RedirectsIndex";
		    Directory directory = FSDirectory.open(new File(indexPath));
		
	    	IndexWriter IndexWriter = new IndexWriter(directory, config1);
			
			
			
		     CSVReader reader = new CSVReader(new FileReader("C:/Users/UNI-Mannheim/Documents/DS4DM_backend/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/redirects.tsv"), '\t');
		     String [] nextLine;
		     Integer rowNumber = 0;
		     while ((nextLine = reader.readNext()) != null) {
	
		    	 rowNumber ++;
		    	 System.out.println("rownumber: " + String.valueOf(rowNumber));
		    	 
			    // make a document
				Document doc = new Document();	
				doc.add(new StringField("redirectSource", nextLine[0], Field.Store.YES));
			    doc.add(new Field("redirectTarget", nextLine[1], TextField.TYPE_STORED));
				
			    // add the document to the index
			    try {
			    	IndexWriter.addDocument(doc);
				} catch (IOException e) {e.printStackTrace();}
		        
		     }
			
		     IndexWriter.close();
		     
		} catch (IOException e1) {e1.printStackTrace();}
	}
}
