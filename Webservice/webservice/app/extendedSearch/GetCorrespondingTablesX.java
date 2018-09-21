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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Scanner;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;

import com.google.gson.Gson;
import com.rapidminer.extension.json.Correspondence;
import com.rapidminer.extension.json.TableInformation;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class GetCorrespondingTablesX {


	public static Map<String,TableInformation>  extendRelevantTablesWithCorrespondences_new(Map<String,TableInformation> relevantTables, String tableName, DS4DMBasicMatcherX matcher, String repositoryName,  String extensionAttribute) throws IOException, ConcurrentModificationException {
		System.out.println("getting extension tables for " + tableName + " ----------------");
		
//		String correspondenceFolderPath = "C:/Users/UNI-Mannheim/Documents/DS4DM_backend/ds4dm_webservice/DS4DM/DS4DM_webservice/public/repositories/" + repositoryName;
		String correspondenceFolderPath = "public/repositories/" + repositoryName + "/correspondences";
		
		// open the extensionAttributePositions - file
		try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step2.1");} catch (IOException e){}
		Gson gson = new Gson();	
		String extensionAttributePositions_string = new Scanner(new File("public/repositories/" + repositoryName + "/extensionAttributePositions/" + extensionAttribute +".json")).useDelimiter("\\Z").next();
		HashMap<String, String> extensionAttributePositions = gson.fromJson(extensionAttributePositions_string, HashMap.class);
	
		
		// open the correspondences-file	
		try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step2.2");} catch (IOException e){}
		CSVReader reader = new CSVReader(new FileReader(correspondenceFolderPath + "/schemaCorrespondences.csv"));
    	
    	String [] nextLine;
    	while ((nextLine = reader.readNext()) != null) {
    		try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step2.3");} catch (IOException e){}
    		//{"table1","header1", "identifier1", "columnindex1", "table2","header2", "identifier2", "columnindex2", "similarityScore"};
    		// if there is an entry in the correspondences file for this table (where the first row equals the tablename)    	
	        if (Objects.equals(nextLine[0], tableName) && nextLine.length == 9){
	        	
	        	
	        	// if there is a schema match for the extension attribute, save it in extensionAttributePositions
	        	if (extensionAttributePositions.get(tableName) == nextLine[3]){
	        		extensionAttributePositions.put(nextLine[4], nextLine[7]);
	        	}
	        	
	        	String correspondingTableName = nextLine[4];
	        	System.out.println("correspondingTableName: " + correspondingTableName);
	        	
			    
	        	
	        	double tableScore = Double.valueOf(nextLine[8]);

	        	
	        	
	        	//create Instance Correspondences-------------------------------------------------------------------------------------------------
	        	try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step2.4");} catch (IOException e){}
	        	Map<String, Correspondence> new_instancesCorrespondences2QueryTable = new HashMap<String, Correspondence>();
	        			
	        	TableInformation originalTable = relevantTables.get(tableName);
	        	Map<String, Correspondence> originalInstancesCorrespondences =  originalTable.getInstancesCorrespondences2QueryTable();
	        	
	    		CSVReader instanceMatchReader = new CSVReader(new FileReader(correspondenceFolderPath + "/instanceCorrespondences/" + tableName.replace(".csv","") + "__" + correspondingTableName));
	    		//{"valueIndex1", "valueIndex2","similarityScore"};
	        	String [] nextInstanceMatch;
	        	while ((nextInstanceMatch = instanceMatchReader.readNext()) != null) {
	        		if (originalInstancesCorrespondences.keySet().contains(nextInstanceMatch[0])){
	        			
	        			String queryTableIndex = originalInstancesCorrespondences.get(nextInstanceMatch[0]).getMatching();
	        			String correpondingTableIndex = nextInstanceMatch[1];
	        			double similarityScore = Double.valueOf(nextInstanceMatch[2]);
	        			new_instancesCorrespondences2QueryTable.put(correpondingTableIndex, new Correspondence(queryTableIndex, similarityScore));
    			
//	        			System.out.println("{'corresponding tableName':" + correspondingTableName + ", 'QT':" + queryTableIndex + " , 'FT':" + correpondingTableIndex  + " },");

	        			
	        		}
	        	}
	        	
	        	//get the remaining TableInformation from Lucene 
	        	try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step2.5");} catch (IOException e){}
	        	TableInformation corresponding_tm = null;
	        	corresponding_tm =  GetTableInformationX.getTableInformation_FromLucene(correspondingTableName, matcher, tableScore, GetTableInformationX.DataSource.INDIREKTMATCH, new_instancesCorrespondences2QueryTable, repositoryName, extensionAttribute);
//	        	System.out.println("corresponding table: " + correspondingTableName + ";   corresponding_table_has_tableinformation = " + String.valueOf(corresponding_tm!= null) + ";  this_table_was_already_found " + String.valueOf(relevantTables.containsKey(correspondingTableName)) );
	        	if(corresponding_tm!= null && !relevantTables.containsKey(correspondingTableName)) {
	        		relevantTables.put(correspondingTableName, corresponding_tm );	
		        	System.out.println("corresponding table: " + correspondingTableName);
		        	try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step2.6");} catch (IOException e){}
		        	
	        	} 
	        	
	        	instanceMatchReader.close();
	        }       		 
    	}
    	
    	try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step2.7");} catch (IOException e){}
		extensionAttributePositions_string = gson.toJson(extensionAttributePositions, HashMap.class);
		try(  PrintWriter out = new PrintWriter("public/repositories/" + repositoryName + "/extensionAttributePositions/" + extensionAttribute +".json" )  ){
		    out.println( extensionAttributePositions_string );
		}

    	reader.close();
		return relevantTables;
	}
	
	
	
}
