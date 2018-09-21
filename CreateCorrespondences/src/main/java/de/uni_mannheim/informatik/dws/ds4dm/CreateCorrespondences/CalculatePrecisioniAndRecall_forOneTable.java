package de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences;



import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

public class CalculatePrecisioniAndRecall_forOneTable {

	
	public static void main(String[] args) throws Exception{
		System.out.println("running CalculatePrecisionAndRecall...");
		//GLOBAL VARIABLES ==========================================================================
		String correspondenceFolderPath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-07-04 test release/calculate precision and recall for the CreateCorrespondences/results/found correspondences";
	    WebTableDataSetLoader loader = new WebTableDataSetLoader();
	    CsvTableParser csvParser = new CsvTableParser();
	    

		
		String datafilename1 = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/tables - csv/1146722_1_7558140036342906956.csv";
		String datafilename2 = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/tables - csv/88353875_0_4876234304797064286.csv";
	    
		File datafile1 = new File(datafilename1);
		File datafile2 = new File(datafilename2);
				
		Table table1 = csvParser.parseTable(datafile1);
		DataSet<MatchableTableRow, MatchableTableColumn> data1 = loader.createTableDataSet(table1);
		
		Table table2 = csvParser.parseTable(datafile2);
		DataSet<MatchableTableRow, MatchableTableColumn> data2 = loader.createTableDataSet(table2);
		
		
		// INSTANCE CORRESPONDENCES
		Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> instanceCorrespondences = getInstancePrecisionAndRecall(data1, data2, datafile1, datafile2, correspondenceFolderPath);
		
	    // SCHEMA CORRESPONDENCES
		if (instanceCorrespondences.get().size()>0){
			getSchemaPrecisionAndRecall(data1, data2, datafile1, datafile2, instanceCorrespondences, correspondenceFolderPath);
			
		}
	    
	}
	
	
	

	
	public static Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> getInstancePrecisionAndRecall(DataSet<MatchableTableRow, MatchableTableColumn> data1,  DataSet<MatchableTableRow, MatchableTableColumn> data2, File file1, File file2, String correspondenceFolderPath) throws Exception{
		System.out.println("=========  " + file1.getName().replace(".csv","") + "__" + file2.getName() + "  =========");
		    
		Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> instanceCorrespondences = Main.getInstanceMatches(data1, data2, file1, file2, correspondenceFolderPath);
		
		String goldstandardFilepath ="C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/Correspondences/temp/1146722_1_7558140036342906956__88353875_0_4876234304797064286.csv";
		
		
		// load the gold standard (test set)
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(new File(goldstandardFilepath));

		// evaluate your result
		MatchingEvaluator<MatchableTableRow, MatchableTableColumn> evaluator = new MatchingEvaluator<MatchableTableRow, MatchableTableColumn>(true);
		Performance perfTest = evaluator.evaluateMatching(instanceCorrespondences.get(), gsTest);

		// print the evaluation result
  
	     System.out.println(String.format("Precision: %.4f;  Recall: %.4f; F1:  %.4f", perfTest.getPrecision(), perfTest.getRecall(), perfTest.getF1()));
	     
	     
		return instanceCorrespondences;
	}
	
	
	
	
	
	public static void getSchemaPrecisionAndRecall(DataSet<MatchableTableRow, MatchableTableColumn> data1, DataSet<MatchableTableRow, MatchableTableColumn> data2, File file1, File file2, Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> instanceCorrespondences, String correspondenceFolderPath) throws IOException{

		Processable<Correspondence<MatchableTableColumn, MatchableTableRow>> schemaCorrespondences = Main.getDuplicateBasedSchemaMatches(data1, data2, file1, file2, instanceCorrespondences, correspondenceFolderPath);	
		
		// save the schema correspondences to file
        CSVWriter csvwriter = new CSVWriter(new FileWriter("C:/Users/UNI-Mannheim/Documents/Test Scripts/temporary files/2017-07-20/found_schema_matches.csv", true), ',');
    	String[] header = {"Identifier1", "Identifier2", "SimilarityScore"};
    	csvwriter.writeNext(header);
        for (Correspondence<MatchableTableColumn, MatchableTableRow> schemaCorrespondence: schemaCorrespondences.get()){
        	String[] newRow = {schemaCorrespondence.getFirstRecord().getIdentifier(), schemaCorrespondence.getSecondRecord().getIdentifier(), String.valueOf(schemaCorrespondence.getSimilarityScore())};
        	csvwriter.writeNext(newRow);
        }
        csvwriter.close();
		
		
		//load goldstandard file
        String gsFilePath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/useful stuff/Data/T2D Goldstandard/Correspondences/schemaCorrespondences_v2/" + file1.getName().replace(".csv", "") + "__" + file2.getName();
//		String gsFilePath = "C:/Users/UNI-Mannheim/Documents/Test Scripts/2017-05-05 Winte.r/calculate precision and recall with olis framework/gs_for_tables2 -  extended/" + file1.getName().replace(".csv", "") + "__" + file2.getName();
		File gsFile = new File(gsFilePath);
		MatchingGoldStandard gsTest = new MatchingGoldStandard();
		gsTest.loadFromCSVFile(gsFile);
		gsTest.setComplete(true);
		
		MatchingEvaluator<MatchableTableColumn, MatchableTableRow> evaluator = new MatchingEvaluator<MatchableTableColumn, MatchableTableRow>(true);
		
		Performance perfTest = evaluator.evaluateMatching(schemaCorrespondences.get(),gsTest);
		
		System.out.println(String.format("Precision: %.4f\nRecall: %.4f\nF1: %.4f",	perfTest.getPrecision(), perfTest.getRecall(), perfTest.getF1()));
		
	    

}

	
}	
	
	