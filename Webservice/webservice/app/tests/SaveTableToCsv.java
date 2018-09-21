package tests;

import java.io.FileWriter;
import java.io.IOException;

import com.opencsv.CSVWriter;

import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;
import uploadTable.additionalWinterClasses.MatchableTableColumn;
import uploadTable.additionalWinterClasses.MatchableTableRow;

public class SaveTableToCsv {
	
	public static Integer NumberOfTablesAlreadySaved= 0;
	
	public static void save(FusibleDataSet<MatchableTableRow, MatchableTableColumn> table, String tablename) {
		try{
		     CSVWriter csvwriter5 = new CSVWriter(new FileWriter("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/testTable" + String.valueOf(NumberOfTablesAlreadySaved) + "_" + tablename + ".csv"), ';');
	
		     //output the header
		     DataSet<MatchableTableColumn,MatchableTableColumn> schema = table.getSchema();
		     if (schema!=null){
		    	 String[] header5 = new String[table.getSchema().get().size()];
		   	     Integer col_number5 = 0;
		   	     for (MatchableTableColumn col :table.getSchema().get()){
		   	    	 header5[col_number5] = col.getHeader();
		   	    	 col_number5++;
		   	     }  
		   	     csvwriter5.writeNext(header5);
		     }
	
		     // output the rows
		     for (MatchableTableRow row: table.get()){
		    	 String[] row_values = new String[table.get().size()];
		    	 for (int columnNumber = 0; columnNumber<table.get().size(); columnNumber++){
		    		 row_values[columnNumber] = String.valueOf(row.get(columnNumber));
		    	 }
		    	 csvwriter5.writeNext(row_values);
		     }
		     csvwriter5.close();
		     NumberOfTablesAlreadySaved++;
		} catch (IOException e) {e.printStackTrace();}
	}
	
	
	public static void save(Table table, String tablename) {
		try{
		     CSVWriter csvwriter6 = new CSVWriter(new FileWriter("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/testTable" + String.valueOf(NumberOfTablesAlreadySaved) + "_" + tablename + ".csv"), ';');
		     
		     //output the header
		     String[] header6 = new String[table.getColumns().size()];
		     Integer col_number6 = 0;
		     for (de.uni_mannheim.informatik.dws.winter.webtables.TableColumn col :table.getColumns()){
		    	 header6[col_number6] = col.getHeader();
		    	 col_number6++;
		     }  
		     csvwriter6.writeNext(header6);
	
		     // output the rows
		     for (TableRow row: table.getRows()){
		    	 String[] row_values = new String[table.getColumns().size()];
		    	 for (int columnNumber = 0; columnNumber<table.getColumns().size(); columnNumber++){
		    		 row_values[columnNumber] = String.valueOf(row.get(columnNumber));
		    	 }
		    	 csvwriter6.writeNext(row_values);
		     }
		     csvwriter6.close();
		     NumberOfTablesAlreadySaved++;
		} catch (IOException e) {e.printStackTrace();}
	}

}
