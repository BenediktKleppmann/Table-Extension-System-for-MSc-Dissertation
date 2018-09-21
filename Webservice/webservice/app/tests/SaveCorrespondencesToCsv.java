package tests;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.opencsv.CSVWriter;

import de.uni_mannheim.informatik.dws.winter.datafusion.CorrespondenceSet;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.model.RecordGroup;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import uploadTable.additionalWinterClasses.MatchableTableColumn;
import uploadTable.additionalWinterClasses.MatchableTableRow;

public class SaveCorrespondencesToCsv {
	
	
	public static Integer NumberOfCorrespondencesAlreadySaved= 0;
	
	
	
	public static void save(Pair<Table, Map<MatchableTableColumn, TableColumn>> reconstruction, String correspondencesName) {
		
		try {
			CSVWriter csvwriter5 = new CSVWriter(new FileWriter("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/testCorrespondence" + String.valueOf(NumberOfCorrespondencesAlreadySaved) + "_" + correspondencesName + ".csv"), ';');

			String[] title1 = {"table:"};
			csvwriter5.writeNext(title1);
			
			List<String> table_columns = new LinkedList<String>();
			for (TableColumn column: reconstruction.getFirst().getColumns()){
				table_columns.add(column.getHeader());
			}
			
			String[] table_columns_array = new String[table_columns.size()];
			table_columns_array = table_columns.toArray(table_columns_array);
			csvwriter5.writeNext(table_columns_array);
			
			String[] title2 = {"mapping:"};
			csvwriter5.writeNext(title2);
			

			for (Map.Entry<MatchableTableColumn, TableColumn> entry: reconstruction.getSecond().entrySet()){
				String[] mapEntry = new String[3];
				mapEntry[0] = entry.getKey().getHeader();
				mapEntry[1] = " <--> ";
				mapEntry[2] = entry.getValue().getHeader();
				csvwriter5.writeNext(mapEntry);
			}
		    
		    csvwriter5.close();
		    NumberOfCorrespondencesAlreadySaved++;
	    
		} catch (IOException e) {e.printStackTrace();}
	}
	
	
	
	
	public static void save(Map<Collection<MatchableTableColumn>, MatchableTableColumn> attributeClusters, String correspondencesName) {
		
		try {
			CSVWriter csvwriter5 = new CSVWriter(new FileWriter("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/testCorrespondence" + String.valueOf(NumberOfCorrespondencesAlreadySaved) + "_" + correspondencesName + ".csv"), ';');


		    for (Map.Entry<Collection<MatchableTableColumn>, MatchableTableColumn> attributeCluster : attributeClusters.entrySet()){
		    	
		    	List<String> unfused_columns = new LinkedList<String>();
		    	if(attributeCluster.getValue() != null) unfused_columns.add(attributeCluster.getValue().getHeader());
		    	unfused_columns.add(" <--> ");
		    	
		    	
		    	for (MatchableTableColumn unfused_column: attributeCluster.getKey()){
		    		unfused_columns.add(unfused_column.getHeader());
		    	}
	
		    	String[] unfused_columns_array = new String[unfused_columns.size()];
		    	unfused_columns_array = unfused_columns.toArray(unfused_columns_array);
		    	
		    	csvwriter5.writeNext(unfused_columns_array);   
		    }
		    
		    csvwriter5.close();
		    NumberOfCorrespondencesAlreadySaved++;
	    
		} catch (IOException e) {e.printStackTrace();}
	}

	
	
	
	
	public static void save(Processable<Correspondence<MatchableTableRow, Matchable>> correspondences, String correspondencesName) throws IOException{

	     CSVWriter csvwriter5 = new CSVWriter(new FileWriter("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/testCorrespondence" + String.valueOf(NumberOfCorrespondencesAlreadySaved) + "_" + correspondencesName + ".csv"), ';');
	     
	     //output the header
	    String[] header5 = {"Identifiers", "SimilarityScore"};
	    csvwriter5.writeNext(header5);

	     
	  // output the rows
		for (Correspondence<MatchableTableRow, Matchable> correspondence: correspondences.get()){
			String[] new_row = {correspondence.getIdentifiers(), String.valueOf(correspondence.getSimilarityScore()), String.valueOf(correspondence.getSecondRecord().getRowNumber()), String.valueOf(correspondence.getSecondRecord().getTableId())};
			csvwriter5.writeNext(new_row);
		} 

	     csvwriter5.close();
	     NumberOfCorrespondencesAlreadySaved++;
	}
	
	
	
	public static void save(CorrespondenceSet<MatchableTableRow, MatchableTableColumn> correspondences, String correspondencesName) throws IOException{

		CSVWriter csvwriter5 = new CSVWriter(new FileWriter("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/testCorrespondence" + String.valueOf(NumberOfCorrespondencesAlreadySaved) + "_" + correspondencesName + ".csv"), ';');
	       
	  // output the rows
		Collection<RecordGroup<MatchableTableRow, MatchableTableColumn>> correspondenceCollection = correspondences.getRecordGroups();
		for (RecordGroup<MatchableTableRow, MatchableTableColumn> correspondence : correspondenceCollection){
			List<String> new_row = new ArrayList<String>();
			for (MatchableTableRow row: correspondence.getRecords()){
				new_row.add(row.getIdentifier());
				new_row.add(String.valueOf(row.getRowNumber()));
				new_row.add(String.valueOf(row.getTableId()));

			}
			String [] array_row = new String [new_row.size()];
			array_row = new_row.toArray(array_row);
			csvwriter5.writeNext(array_row);
		}

	     csvwriter5.close();
	     NumberOfCorrespondencesAlreadySaved++;
	}
	
	
	
	
	
}
