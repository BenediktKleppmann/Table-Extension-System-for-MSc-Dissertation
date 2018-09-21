package de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.commons.io.FilenameUtils;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import de.uni_mannheim.informatik.additionalWinterClasses.MatchableTableColumn;
import de.uni_mannheim.informatik.additionalWinterClasses.MatchableTableRow;
import de.uni_mannheim.informatik.additionalWinterClasses.WebTableDataSetLoader;
import de.uni_mannheim.informatik.dws.winter.matching.MatchingEvaluator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.MatchingGoldStandard;
import de.uni_mannheim.informatik.dws.winter.model.Performance;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.CsvTableParser;

public class CalculatePrecisionAndRecall {


	
	
	
	
	
	
	
	public static void main(String[] args) throws Exception{
		System.out.println("running CalculatePrecisionAndRecall...");
		//GLOBAL VARIABLES ==========================================================================
		String correspondenceFolderPath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-07-04 test release/calculate precision and recall for the CreateCorrespondences/results/found correspondences";
	    WebTableDataSetLoader loader = new WebTableDataSetLoader();
	    CsvTableParser csvParser = new CsvTableParser();
	    
	    
		// csv writer for the Instance Correspondence Precision and Recall
	     CSVWriter instanceWriter = new CSVWriter(new FileWriter("C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-07-04 test release/calculate precision and recall for the CreateCorrespondences/results/precision and recall - java/instance_match_results.csv"));
	     String[] header = {"table1","table2", "precision","recall","f1"};
	     instanceWriter.writeNext(header);
	     
	    // csv writer for the Schema Correspondence Precision and Recall
	     CSVWriter schemaWriter = new CSVWriter(new FileWriter("C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-07-04 test release/calculate precision and recall for the CreateCorrespondences/results/precision and recall - java/schema_match_results.csv"));
	     String[] header2 = {"table1","table2", "precision","recall","f1"};
	     schemaWriter.writeNext(header2);


	     
	     
	     
		
		// THE PROGRAM ==============================================================================
		//get tablenames in folder
		String datafilePath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/tables - csv/";
		File dir = new File(datafilePath);
		File[] datafiles = dir.listFiles();
    
	    
	    //loop through the files
    	for (File datafile1: datafiles){
    		
    		if (FilenameUtils.getExtension(datafile1.getName()).equals("csv")){
				Table table1 = csvParser.parseTable(datafile1);
				DataSet<MatchableTableRow, MatchableTableColumn> data1 = loader.createTableDataSet(table1);
				
	        	for (File datafile2: datafiles){
	        		if (FilenameUtils.getExtension(datafile2.getName()).equals("csv") && ! datafile1.getName().equals(datafile2.getName()))
	        		{
						Table table2 = csvParser.parseTable(datafile2);
						DataSet<MatchableTableRow, MatchableTableColumn> data2 = loader.createTableDataSet(table2);
						
						Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> instanceCorrespondences = getInstancePrecisionAndRecall(data1, data2, datafile1, datafile2, correspondenceFolderPath, instanceWriter);
//						String[] newRow = getInstancePrecisionAndRecall2(data1, data2, datafile1, datafile2, correspondenceFolderPath, writer);
						
						if (instanceCorrespondences.get().size()>0){
							getSchemaPrecisionAndRecall(data1, data2, datafile1, datafile2, instanceCorrespondences, correspondenceFolderPath, schemaWriter);	
						}
	        		}
	        	}
    		}	
    	}
    	instanceWriter.close();
    	schemaWriter.close();
	}
	
	
	public static Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> getInstancePrecisionAndRecall(DataSet<MatchableTableRow, MatchableTableColumn> data1,  DataSet<MatchableTableRow, MatchableTableColumn> data2, File file1, File file2, String correspondenceFolderPath, CSVWriter writer) throws Exception{
		System.out.println("=========  " + file1.getName().replace(".csv","") + "__" + file2.getName() + "  =========");
		    
		Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> instanceCorrespondences = Main.getInstanceMatches(data1, data2, file1, file2, correspondenceFolderPath);
		

		String goldstandardFilepath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/Correspondences/temp/" + file1.getName().replace(".csv","") + "__" + file2.getName();
		File goldstandardFile = new File("C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/Correspondences/temp/" + file1.getName().replace(".csv","") + "__" + file2.getName());
		if(!goldstandardFile.exists() || goldstandardFile.isDirectory()) { 
			goldstandardFilepath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/Correspondences/2 derived correpondences/instance correspondences/empty_correspondences_file.csv";
		}
		// load the gold standard (test set)
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(new File(goldstandardFilepath));

		// evaluate your result
		MatchingEvaluator<MatchableTableRow, MatchableTableColumn> evaluator = new MatchingEvaluator<MatchableTableRow, MatchableTableColumn>(true);
		Performance perfTest = evaluator.evaluateMatching(instanceCorrespondences.get(), gsTest);

		// print the evaluation result
		
		
		
		// save the precision and recall to a csv-file
	     String[] newRow = {file1.getName() , file2.getName() , String.valueOf(perfTest.getPrecision()), String.valueOf(perfTest.getRecall()), String.valueOf(perfTest.getF1())};
	     writer.writeNext(newRow);
	     
	     System.out.println(String.format("Precision: %.4f;  Recall: %.4f; F1:  %.4f", perfTest.getPrecision(), perfTest.getRecall(), perfTest.getF1()));
	     
	     
		return instanceCorrespondences;
	}
	
	
	

	
	public static void getSchemaPrecisionAndRecall(DataSet<MatchableTableRow, MatchableTableColumn> data1, DataSet<MatchableTableRow, MatchableTableColumn> data2, File file1, File file2, Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> instanceCorrespondences, String correspondenceFolderPath, CSVWriter writer) throws IOException{

		Processable<Correspondence<MatchableTableColumn, MatchableTableRow>> schemaCorrespondences = Main.getDuplicateBasedSchemaMatches(data1, data2, file1, file2, instanceCorrespondences, correspondenceFolderPath);	
		
		// save the schema correspondences to file
//        CSVWriter csvwriter = new CSVWriter(new FileWriter("C:/Users/UNI-Mannheim/Documents/Test Scripts/temporary files/2017-07-20/found_schema_matches.csv", true), ',');
//    	String[] header = {"Identifier1", "Identifier2", "SimilarityScore"};
//    	csvwriter.writeNext(header);
//        for (Correspondence<MatchableTableColumn, MatchableTableRow> schemaCorrespondence: schemaCorrespondences.get()){
//        	String[] newRow = {schemaCorrespondence.getFirstRecord().getIdentifier(), schemaCorrespondence.getSecondRecord().getIdentifier(), String.valueOf(schemaCorrespondence.getSimilarityScore())};
//        	csvwriter.writeNext(newRow);
//        }
//        csvwriter.close();
		
		
		//load goldstandard file
		String gsFilePath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/Correspondences/schemaCorrespondences_v2/" + file1.getName().replace(".csv", "") + "__" + file2.getName();
		File goldstandardFile = new File(gsFilePath);
		if(!goldstandardFile.exists() || goldstandardFile.isDirectory()) { 
			gsFilePath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/Correspondences/2 derived correpondences/instance correspondences/empty_correspondences_file.csv";
		}
		File gsFile = new File(gsFilePath);
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(gsFile);
		gsTest.setComplete(true);
		
		MatchingEvaluator<MatchableTableColumn, MatchableTableRow> evaluator = new MatchingEvaluator<MatchableTableColumn, MatchableTableRow>(true);
		
		Performance perfTest = evaluator.evaluateMatching(schemaCorrespondences.get(),gsTest);
		
		System.out.println(String.format("Precision: %.4f\nRecall: %.4f\nF1: %.4f",	perfTest.getPrecision(), perfTest.getRecall(), perfTest.getF1()));
		
		// save the schema-matching results to a csv-file		
	     String[] newRow = {file1.getName() , file2.getName() , String.valueOf(perfTest.getPrecision()), String.valueOf(perfTest.getRecall()), String.valueOf(perfTest.getF1())};
	     writer.writeNext(newRow);

}
	
	


	//========================================================================================================	
	// ALTERNATE MAIN FUNCTION: promising_files_for_evaluation
	//========================================================================================================
	public static void main__promisingFilesForEvaluation(String[] args) throws Exception{
		System.out.println("running CalculatePrecisionAndRecall...");
		//GLOBAL VARIABLES ==========================================================================
		String correspondenceFolderPath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-07-04 test release/calculate precision and recall for the CreateCorrespondences/results/found correspondences";
	    WebTableDataSetLoader loader = new WebTableDataSetLoader();
	    CsvTableParser csvParser = new CsvTableParser();
	    
	    
		// csv writer for the Instance Correspondence Precision and Recall
	     CSVWriter instanceWriter = new CSVWriter(new FileWriter("C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-07-04 test release/calculate precision and recall for the CreateCorrespondences/results/precision and recall - java/instance_match_results.csv"));
	     String[] header = {"table1","table2", "precision","recall","f1"};
	     instanceWriter.writeNext(header);
	     
	    // csv writer for the Schema Correspondence Precision and Recall
	     CSVWriter schemaWriter = new CSVWriter(new FileWriter("C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-07-04 test release/calculate precision and recall for the CreateCorrespondences/results/precision and recall - java/schema_match_results.csv"));
	     String[] header2 = {"table1","table2", "precision","recall","f1"};
	     schemaWriter.writeNext(header2);


		
	     CSVReader reader = new CSVReader(new FileReader("C:/Users/UNI-Mannheim/Documents/Test Scripts/temporary files/2017-07-20/promising_files_for_evaluation.csv"));
	     String [] nextLine;
	     while ((nextLine = reader.readNext()) != null) {

	        String filename1 = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/tables - csv/" + nextLine[0];
	        String filename2 = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/tables - csv/" + nextLine[1];
	        
	        File datafile1 = new File(filename1);
	        Table table1 = csvParser.parseTable(datafile1);
			DataSet<MatchableTableRow, MatchableTableColumn> data1 = loader.createTableDataSet(table1);
			
			File datafile2 = new File(filename2);
			Table table2 = csvParser.parseTable(datafile2);
			DataSet<MatchableTableRow, MatchableTableColumn> data2 = loader.createTableDataSet(table2);
			
			
			Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> instanceCorrespondences = getInstancePrecisionAndRecall(data1, data2, datafile1, datafile2, correspondenceFolderPath, instanceWriter);
//			String[] newRow = getInstancePrecisionAndRecall2(data1, data2, datafile1, datafile2, correspondenceFolderPath, writer);
			
			if (instanceCorrespondences.get().size()>0){
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("!!!!!  SCHEMA MACHTCHES  !!!!!");
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				getSchemaPrecisionAndRecall(data1, data2, datafile1, datafile2, instanceCorrespondences, correspondenceFolderPath, schemaWriter);	
			}
	     }
	     
	    
	   
    	instanceWriter.close();
    	schemaWriter.close();
    	reader.close();
	}
	
	
	
}
