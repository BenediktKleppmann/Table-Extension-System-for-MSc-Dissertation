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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;

import com.google.gson.Gson;
import com.rapidminer.extension.json.Correspondence;
import com.rapidminer.extension.json.TableInformation;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import extendedSearch.DS4DMBasicMatcherX;
import extendedSearch.GetTableInformationX;
import model.ExtendedTableInformation;

public class CorrespondenceManager {
	
	
	public static Map.Entry<Map<String, ExtendedTableInformation>, Map<String, ExtendedTableInformation>>  findIndirectMatches(Map<String, ExtendedTableInformation> directlyFoundTables, GlobalVariables globalVariables) throws IOException, ConcurrentModificationException {
		
		Map<String, ExtendedTableInformation> indirectlyFoundTables = new HashMap<String, ExtendedTableInformation>();
		Map<String, ExtendedTableInformation> directlyFoundTables_updated = new HashMap<String, ExtendedTableInformation>();
		
		for (ExtendedTableInformation directlyFoundTable: directlyFoundTables.values()){
	
			String tableName = directlyFoundTable.getTableName();
			System.out.println("getting extension tables for " + tableName + " ----------------");
			
	
			// open the correspondences-file	
			CSVReader reader = new CSVReader(new FileReader("public/repositories/" + globalVariables.getRepositoryName() + "/correspondences/schemaCorrespondences.csv"));
	    	
	    	String [] nextLine;
	    	while ((nextLine = reader.readNext()) != null) {
	    		//{"table1","header1", "identifier1", "columnindex1", "table2","header2", "identifier2", "columnindex2", "similarityScore"};
	    		// if there is an entry in the correspondences file for this table (where the first row equals the tablename)   
		        if (Objects.equals(nextLine[0], tableName) && Objects.equals(nextLine[3], directlyFoundTable.getExtensionAttributePosition()) && nextLine.length == 9){
		        	ExtendedTableInformation indirectlyFoundTable = new ExtendedTableInformation();
		        	
		        	indirectlyFoundTable.setTableName(nextLine[4]);
		        	indirectlyFoundTable.setExtensionAttributePosition(nextLine[7]);
		        	System.out.println("Setting ExtensionAttributePosition for indirect table  " + indirectlyFoundTable.getTableName() + " : " + nextLine[7]);
		        	double correspondenceScore = Double.valueOf(nextLine[8]);
		        	indirectlyFoundTable.setSchemaSimilarityScore(correspondenceScore * directlyFoundTable.getSchemaSimilarityScore());
		        	indirectlyFoundTable.setMatchingType("indirectMatch");
		        	
		        	//save the correspondences
		        	indirectlyFoundTable.addToCorrespondingTables(directlyFoundTable.getTableName());
		        	directlyFoundTable.addToCorrespondingTables(indirectlyFoundTable.getTableName());
		        	indirectlyFoundTables.put(indirectlyFoundTable.getTableName(), indirectlyFoundTable);
		        }       		 
	    	}
	    	directlyFoundTables_updated.put(directlyFoundTable.getTableName(), directlyFoundTable);
		}
		return new AbstractMap.SimpleEntry<Map<String, ExtendedTableInformation>, Map<String, ExtendedTableInformation>> (directlyFoundTables_updated, indirectlyFoundTables);
	}
	
	
	
	public static Map<String, ExtendedTableInformation> GetCorrespondenceBasedInstanceMatches(Map<String, ExtendedTableInformation> tables, GlobalVariables globalVariables) {

		Map<String, ExtendedTableInformation> updatedTables = new HashMap<String, ExtendedTableInformation>();
		
		// Find additional Instance matches for myTable		
		for (ExtendedTableInformation myTable:tables.values()){
			
			LinkedList<String> correspondingTableNames = myTable.getCorrespondingTables();
			for (String correspondingTableName: correspondingTableNames){
				try{

					CSVReader correspondenceReader = new CSVReader(new FileReader("public/repositories/" + globalVariables.getRepositoryName() + "/correspondences/instanceCorrespondences/" + correspondingTableName.replace(".csv","") + "__" + myTable.getTableName()));
					Map<String, Correspondence> instanceMatchesOfCorrespondingTable = tables.get(correspondingTableName).getInstancesCorrespondences2QueryTable();
					
					String [] nextCorrespondence;
		        	while ((nextCorrespondence = correspondenceReader.readNext()) != null) {
		        		if (instanceMatchesOfCorrespondingTable.keySet().contains(nextCorrespondence[0])){
		        			
		        			String queryTableIndex = instanceMatchesOfCorrespondingTable.get(nextCorrespondence[0]).getMatching();
		        			
		        			String myTableIndex = nextCorrespondence[1];
		        			double similarityScore = Double.valueOf(nextCorrespondence[2]);
		        			
		        			//If this instance match isn't there yet, then add it 
		        			if (!myTable.getInstancesCorrespondences2QueryTable().keySet().contains(myTableIndex)){
		        				myTable.putNewInstancesCorrespondences2QueryTable(myTableIndex, new Correspondence(queryTableIndex, similarityScore));
		        			}
	//	        			System.out.println("{'corresponding tableName':" + correspondingTableName + ", 'QT':" + queryTableIndex + " , 'FT':" + correpondingTableIndex  + " },");
	
		        			
		        		}
		        	}  
	        	correspondenceReader.close();	
				}catch(NumberFormatException| IOException e){}
	        	
			}
			updatedTables.put(myTable.getTableName(), myTable);
		}
		
		return updatedTables;
		
	
	}
}
