package create_indexes_and_correspondences;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;

import au.com.bytecode.opencsv.CSVWriter;

public class Test2_main {

	
	public static void main(String[] args) throws IOException {
		
		System.out.println("Start -----------------------");
		
		String workingDirectory = "C:/Users/UNI-Mannheim/workspace/test2";

		String blockSize = "5";
		String return_message = "";

		
		//  =========================  PART0: opening file ==================================== 
		System.out.println("opening file");

		
        InputStream in = new FileInputStream(new File("C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/Web Wiki Tables/tables.json/split/0.json"));
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String newTableListJson = reader.readLine();
		in.close();
		reader.close();
		
//		Gson gson = new Gson();
//		ListOfTables listOfTables = gson.fromJson(newTableListJson, ListOfTables.class);
		
		
		
		
		//  =========================  PART0: SAVING TABLES AS CSV FILES ==================================== 
		Gson gson = new Gson();
		
		ListOfTables listOfTables = gson.fromJson(newTableListJson, ListOfTables.class);

	     
		for (Table newTableObject: listOfTables.getListOfTables()){
			
			String tableName = newTableObject.getTablename();
			Integer keyColumnIndex = newTableObject.getKeyColumnIndex();
			
			String newFilePath = "C:/Users/UNI-Mannheim/workspace/test2/tables/" + tableName;
			try { 
				newTableObject.saveToFile(newFilePath);
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("Error, the file " + tableName + " couldn't be saved as csv.\n");	
			}
		}
		
		
		
		
		//  =========================  PART1: INDEXING ==================================== 
		System.out.println("INDEXING -----------------------");
	    String keyColumnsCSVPath = workingDirectory + "/indexes 2018-03-30/keyColumnsCSV.csv";;
	     
	     CSVWriter keyColumnsCsvwriter = new CSVWriter(new FileWriter(keyColumnsCSVPath), '\t');

	         
	     for (Table newTableObject: listOfTables.getListOfTables()){
	    	 if (newTableObject.getKeyColumnIndex()!= null){
	    		 
	    		 String keyColumnName =  newTableObject.getRelation()[newTableObject.getKeyColumnIndex()][0];
			     String[] newRow = {newTableObject.getTablename(), keyColumnName};
			     keyColumnsCsvwriter.writeNext(newRow);
			     System.out.println("KeyColumnIndex of " + newTableObject.getTablename() + " = " + keyColumnName);
	    	 }else{
	    		 System.out.println("KeyColumnIndex of " + newTableObject.getTablename() + " is null ");
	    	 }
	     }
	     keyColumnsCsvwriter.close();
	     
	     System.out.println("keyColumnsCSV created");
	     
	     String datafilePath = workingDirectory + "/tables";
	     String indexFolderPath = workingDirectory + "/indexes 2018-03-30";


	     
		 de.uni_mannheim.informatik.dws.ds4dm.CreateLuceneIndex.Main luceneIndexCreator = new de.uni_mannheim.informatik.dws.ds4dm.CreateLuceneIndex.Main();
		 String[] indexingArguments = {datafilePath, indexFolderPath, keyColumnsCSVPath};
		 System.out.println(Arrays.toString(indexingArguments));
		 try{
		 	luceneIndexCreator.main(indexingArguments);
		 } catch (IOException e){
		 	e.printStackTrace();
		 	System.out.println("ERROR: while indexing");
		 }
		

		 System.out.println("INDEXING COMPLETE -----------------------");
	     
    	//  =========================  PART2: CORRESPONDENCE FINDING ==================================== 
	     
		String correspondenceFolderPath =  indexFolderPath;
		 
		de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences.Main correspondenceFileCreator = new de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences.Main();
    	String[] correspondenceArguments = {indexFolderPath, datafilePath, correspondenceFolderPath, blockSize};
	     
    	System.out.println("CREATE CORRESPONDENCES -----------------------");
		try{         
	    	correspondenceFileCreator.main(correspondenceArguments);    	
		} catch (Exception e){
			e.printStackTrace();
			System.out.println("ERROR: An Exception occured during the CorrespondenceFile Creation.\n");
		}
    	
		System.out.println("CORRESPONDENCES CREATED -----------------------");

	}
	
}
