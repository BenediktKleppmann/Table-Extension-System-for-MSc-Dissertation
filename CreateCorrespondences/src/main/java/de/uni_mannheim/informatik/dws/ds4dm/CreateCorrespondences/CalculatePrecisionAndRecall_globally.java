package de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences;



import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
import de.uni_mannheim.informatik.dws.winter.processing.ProcessableCollection;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.CsvTableParser;




public class CalculatePrecisionAndRecall_globally {


	public static void main_allFiles(String[] args) throws Exception{
		System.out.println("running CalculatePrecisionAndRecall...");
		//GLOBAL VARIABLES ==========================================================================
		String correspondenceFolderPath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-07-04 test release/calculate precision and recall for the CreateCorrespondences/results/found correspondences";
	    WebTableDataSetLoader loader = new WebTableDataSetLoader();
	    CsvTableParser csvParser = new CsvTableParser();
	     
		
		// THE PROGRAM ==============================================================================
		//get tablenames in folder
		String datafilePath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/tables - csv/";
		File dir = new File(datafilePath);
		File[] datafiles = dir.listFiles();
    
	    
		Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> allInstanceCorrespondences = new ProcessableCollection<Correspondence<MatchableTableRow, MatchableTableColumn>>();
		Processable<Correspondence<MatchableTableColumn, MatchableTableRow>> allSchemaCorrespondences = new ProcessableCollection<Correspondence<MatchableTableColumn, MatchableTableRow>>();
		
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
						
						// GET INSTANCE CORRESPONDENCES
						// and individually add the instance correspondences to allInstanceCorrespondences
						Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> instanceCorrespondences = Main.getInstanceMatches(data1, data2, datafile1, datafile2, correspondenceFolderPath);
						for (Correspondence<MatchableTableRow, MatchableTableColumn> instanceCorrespondence: instanceCorrespondences.get()){
							allInstanceCorrespondences.add(instanceCorrespondence);
						}
						
						if (instanceCorrespondences.get().size()>0){
							// GET SCHEMA CORRESPONDENCES
							// and individually add the instance correspondences to allInstanceCorrespondences
							Processable<Correspondence<MatchableTableColumn, MatchableTableRow>> schemaCorrespondences = Main.getDuplicateBasedSchemaMatches(data1, data2, datafile1, datafile2, instanceCorrespondences, correspondenceFolderPath);
							for (Correspondence<MatchableTableColumn, MatchableTableRow> schemaCorrespondence: schemaCorrespondences.get()){
								allSchemaCorrespondences.add(schemaCorrespondence);
							}
						}
	        		}
	        	}
    		}	
    	}
    	
    	
    	// FOR EVALUATION PUROPSES: SAVE ALL INSTANCE CORRESPONDENCES TO A FILE
		// csv writer for the Instance Correspondence Precision and Recall
	     CSVWriter instanceWriter = new CSVWriter(new FileWriter("C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-07-04 test release/calculate precision and recall for the CreateCorrespondences/results/precision and recall - java/instance_match_results.csv"));
	     String[] header = {"Identifier1","Identifier2", "similarityScore"};
	     instanceWriter.writeNext(header);
	     for  (Correspondence<MatchableTableRow, MatchableTableColumn> instanceCorrespondence: allInstanceCorrespondences.get()){
	    	 String[] newRow = {instanceCorrespondence.getFirstRecord().getIdentifier(), instanceCorrespondence.getSecondRecord().getIdentifier(), String.valueOf(instanceCorrespondence.getSimilarityScore())};
	    	 instanceWriter.writeNext(newRow);
	     }
	     instanceWriter.close();
	     
	     
	    // FOR EVALUATION PUROPSES: SAVE ALL SCHEMA CORRESPONDENCES TO A FILE
	    // csv writer for the Schema Correspondence Precision and Recall
	     CSVWriter schemaWriter = new CSVWriter(new FileWriter("C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-07-04 test release/calculate precision and recall for the CreateCorrespondences/results/precision and recall - java/schema_match_results.csv"));
	     String[] header2 = {"table1","table2", "precision","recall","f1"};
	     schemaWriter.writeNext(header2);
	     for (Correspondence<MatchableTableColumn, MatchableTableRow> schemaCorrespondence: allSchemaCorrespondences.get()){
	    	 String[] newRow = {schemaCorrespondence.getFirstRecord().getIdentifier(), schemaCorrespondence.getSecondRecord().getIdentifier(), String.valueOf(schemaCorrespondence.getSimilarityScore())};
	    	 schemaWriter.writeNext(newRow);
	     }
	     schemaWriter.close();

	     
	     
	     
    	
	    //====================================================================================
	    //                                      EVALUATION  
		//====================================================================================
	     
    	// EVALUATE THE INSTANCE CORRESPONDENCES

		// load the gold standard 
		MatchingGoldStandard gsInstance = new MatchingGoldStandard();
		gsInstance.loadFromCSVFile(new File("C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/Correspondences/allInstanceCorrespondences.csv"));

		// evaluate your result
		MatchingEvaluator<MatchableTableRow, MatchableTableColumn> evaluatorInstance = new MatchingEvaluator<MatchableTableRow, MatchableTableColumn>(true);
		Performance perfInstance = evaluatorInstance.evaluateMatching(allInstanceCorrespondences.get(), gsInstance);

		// print the evaluation result
		System.out.println("===========================================================================================");
		System.out.println("=======================   Instance Correspondence Evaluation   ============================");
		System.out.println("===========================================================================================");
		System.out.println(String.format("Precision: %.4f;  Recall: %.4f; F1:  %.4f", perfInstance.getPrecision(), perfInstance.getRecall(), perfInstance.getF1()));
		System.out.println("===========================================================================================");
		System.out.println("===========================================================================================");
		System.out.println("===========================================================================================");
		

	     
	     
    	// EVALUATE THE SCHEMA CORRESPONDENCES

		File gsFile = new File("C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/Correspondences/allSchemaCorrespondences.csv");
		MatchingGoldStandard gsSchema = new MatchingGoldStandard();
		gsSchema.loadFromCSVFile(gsFile);
		gsSchema.setComplete(true);
		
		MatchingEvaluator<MatchableTableColumn, MatchableTableRow> schemaEvaluator = new MatchingEvaluator<MatchableTableColumn, MatchableTableRow>(true);
		
		Performance perfSchema = schemaEvaluator.evaluateMatching(allSchemaCorrespondences.get(),gsSchema);
		
		
		System.out.println("===========================================================================================");
		System.out.println("=======================   Instance Correspondence Evaluation   ============================");
		System.out.println("===========================================================================================");
		System.out.println(String.format("Precision: %.4f\nRecall: %.4f\nF1: %.4f",	perfSchema.getPrecision(), perfSchema.getRecall(), perfSchema.getF1()));
		System.out.println("===========================================================================================");
		System.out.println("===========================================================================================");
		System.out.println("===========================================================================================");
		
		
		
    	
	}
	
	
	
	
	//========================================================================================================	
	// ALTERNATE MAIN FUNCTION: random_subset_of_tables
	//========================================================================================================
//	__randomSubsetOfTables
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception{
		System.out.println("running CalculatePrecisionAndRecall...");
		//GLOBAL VARIABLES ==========================================================================
		String correspondenceFolderPath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-07-04 test release/calculate precision and recall for the CreateCorrespondences/results/found correspondences";
	    WebTableDataSetLoader loader = new WebTableDataSetLoader();
	    CsvTableParser csvParser = new CsvTableParser();
	     
		
		// THE PROGRAM ==============================================================================
	    
		Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> allInstanceCorrespondences = new ProcessableCollection<Correspondence<MatchableTableRow, MatchableTableColumn>>();
		Processable<Correspondence<MatchableTableColumn, MatchableTableRow>> allSchemaCorrespondences = new ProcessableCollection<Correspondence<MatchableTableColumn, MatchableTableRow>>();
		
		//loop through the files marked in the csv-file
	    CSVReader reader = new CSVReader(new FileReader("C:/Users/UNI-Mannheim/Documents/Test Scripts/temporary files/2017-07-21/random_subset_of_tables.csv"));
	    //C:/Users/UNI-Mannheim/Documents/Test Scripts/temporary files/2017-07-20/promising_files_for_evaluation.csv
	    String [] nextLine;
	    Integer counter = 0;
	    while ((nextLine = reader.readNext()) != null) {
	    	
	    	System.out.println("=========================================================");
	    	System.out.println("=========================================================");
	    	System.out.println("========================   " + counter + "   ===========================");
	    	System.out.println("=========================================================");
	    	System.out.println("=========================================================");
	    	counter ++;
	    	
	        String filename1 = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/tables - csv/" + nextLine[0];
	        String filename2 = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/tables - csv/" + nextLine[1];
	        
	        File datafile1 = new File(filename1);
	        Table table1 = csvParser.parseTable(datafile1);
			DataSet<MatchableTableRow, MatchableTableColumn> data1 = loader.createTableDataSet(table1);
			
			File datafile2 = new File(filename2);
			Table table2 = csvParser.parseTable(datafile2);
			DataSet<MatchableTableRow, MatchableTableColumn> data2 = loader.createTableDataSet(table2);
			
			// GET INSTANCE CORRESPONDENCES
			// and individually add the instance correspondences to allInstanceCorrespondences
			Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> instanceCorrespondences = Main.getInstanceMatches(data1, data2, datafile1, datafile2, correspondenceFolderPath);
			for (Correspondence<MatchableTableRow, MatchableTableColumn> instanceCorrespondence: instanceCorrespondences.get()){
				allInstanceCorrespondences.add(instanceCorrespondence);
			}
			
			if (instanceCorrespondences.get().size()>0){
				// GET SCHEMA CORRESPONDENCES
				// and individually add the instance correspondences to allInstanceCorrespondences
				Processable<Correspondence<MatchableTableColumn, MatchableTableRow>> schemaCorrespondences = Main.getDuplicateBasedSchemaMatches(data1, data2, datafile1, datafile2, instanceCorrespondences, correspondenceFolderPath);
				for (Correspondence<MatchableTableColumn, MatchableTableRow> schemaCorrespondence: schemaCorrespondences.get()){
					allSchemaCorrespondences.add(schemaCorrespondence);
				}
			}
	    }
		
	
    	// FOR EVALUATION PUROPSES: SAVE ALL INSTANCE CORRESPONDENCES TO A FILE
		// csv writer for the Instance Correspondence Precision and Recall
	     CSVWriter instanceWriter = new CSVWriter(new FileWriter("C:/Users/UNI-Mannheim/Documents/Test Scripts/temporary files/2017-07-21/instance_match_results.csv"));
	     String[] header = {"Identifier1","Identifier2", "similarityScore"};
	     instanceWriter.writeNext(header);
	     for  (Correspondence<MatchableTableRow, MatchableTableColumn> instanceCorrespondence: allInstanceCorrespondences.get()){
	    	 String[] newRow = {instanceCorrespondence.getFirstRecord().getIdentifier(), instanceCorrespondence.getSecondRecord().getIdentifier(), String.valueOf(instanceCorrespondence.getSimilarityScore())};
	    	 instanceWriter.writeNext(newRow);
	     }
	     instanceWriter.close();
	     
	     
	    // FOR EVALUATION PUROPSES: SAVE ALL SCHEMA CORRESPONDENCES TO A FILE
	    // csv writer for the Schema Correspondence Precision and Recall
	     CSVWriter schemaWriter = new CSVWriter(new FileWriter("C:/Users/UNI-Mannheim/Documents/Test Scripts/temporary files/2017-07-21/schema_match_results.csv"));
	     String[] header2 = {"table1","table2", "precision","recall","f1"};
	     schemaWriter.writeNext(header2);
	     for (Correspondence<MatchableTableColumn, MatchableTableRow> schemaCorrespondence: allSchemaCorrespondences.get()){
	    	 String[] newRow = {schemaCorrespondence.getFirstRecord().getIdentifier(), schemaCorrespondence.getSecondRecord().getIdentifier(), String.valueOf(schemaCorrespondence.getSimilarityScore())};
	    	 schemaWriter.writeNext(newRow);
	     }
	     schemaWriter.close();

	     
	     
	     
    	
	    //====================================================================================
	    //                                      EVALUATION  
		//====================================================================================
	     
    	// EVALUATE THE INSTANCE CORRESPONDENCES

		// load the gold standard 
		MatchingGoldStandard gsInstance = new MatchingGoldStandard();
		gsInstance.loadFromCSVFile(new File("C:/Users/UNI-Mannheim/Documents/Test Scripts/temporary files/2017-07-21/instance_match_goldstandard.csv"));
		//C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/Correspondences/allInstanceCorrespondences.csv

		// evaluate your result
		MatchingEvaluator<MatchableTableRow, MatchableTableColumn> evaluatorInstance = new MatchingEvaluator<MatchableTableRow, MatchableTableColumn>(true);
		Performance perfInstance = evaluatorInstance.evaluateMatching(allInstanceCorrespondences.get(), gsInstance);
	     
	     
    	// EVALUATE THE SCHEMA CORRESPONDENCES

		File gsFile = new File("C:/Users/UNI-Mannheim/Documents/Test Scripts/temporary files/2017-07-21/schema_match_goldstandard.csv");
		//C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/Correspondences/allSchemaCorrespondences.csv
		MatchingGoldStandard gsSchema = new MatchingGoldStandard();
		gsSchema.loadFromCSVFile(gsFile);
		gsSchema.setComplete(true);
		
		MatchingEvaluator<MatchableTableColumn, MatchableTableRow> schemaEvaluator = new MatchingEvaluator<MatchableTableColumn, MatchableTableRow>(true);
		
		Performance perfSchema = schemaEvaluator.evaluateMatching(allSchemaCorrespondences.get(),gsSchema);
		
		
		
		// print the evaluation results
		System.out.println();
		System.out.println("=======================   Instance Correspondence Evaluation   ============================");
		System.out.println(String.format("Precision: %.4f\nRecall: %.4f\nF1:  %.4f", perfInstance.getPrecision(), perfInstance.getRecall(), perfInstance.getF1()));
		System.out.println();
		System.out.println("=======================   Schema Correspondence Evaluation   ============================");
		System.out.println(String.format("Precision: %.4f\nRecall: %.4f\nF1: %.4f",	perfSchema.getPrecision(), perfSchema.getRecall(), perfSchema.getF1()));


		
		
		
    	
	}

	

	
	
}
