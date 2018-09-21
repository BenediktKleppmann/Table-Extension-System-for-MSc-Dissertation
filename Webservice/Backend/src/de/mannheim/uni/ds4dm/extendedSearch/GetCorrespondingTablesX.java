package de.mannheim.uni.ds4dm.extendedSearch;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import com.rapidminer.extension.json.TableInformation;

import au.com.bytecode.opencsv.CSVReader;

public class GetCorrespondingTablesX {

	public static Map<String,TableInformation>  extendRelevantTablesWithCorrespondences(Map<String,TableInformation> relevantTables, String tableName, DS4DMBasicMatcherX matcher) throws IOException{
				
		
		// open the correspondences-file		
		CSVReader reader = new CSVReader(new FileReader("C:/Users/UNI-Mannheim/Documents/Test Scripts/10-04-2017 correspondences from goldstandard/property_correspondences.csv"));
    	
    	String [] nextLine;
    	while ((nextLine = reader.readNext()) != null) {
    		
    		// if there is an entry in the correspondences file for this table (where the first row equals the tablename)    	
    		// && nextLine.length == 8
	        if (Objects.equals(nextLine[0], tableName) ){
	        	String correspondingTableName = nextLine[4];
	        	String correspondingKeyColumnName = nextLine[5];
	        	TableInformation corresponding_tm = null;
	        	corresponding_tm =  GetTableInformationX.getTableInformation_FromLucene(correspondingTableName, matcher, correspondingKeyColumnName);
	        	if(corresponding_tm!= null && !relevantTables.containsKey(tableName)) {
	        		relevantTables.put(tableName, corresponding_tm );	
		        	System.out.println("corresponding table: " + correspondingTableName);
	        	}
	        }
		        		 
    	}
    		

    	reader.close();
		return relevantTables;
		
		
		
	}
}
