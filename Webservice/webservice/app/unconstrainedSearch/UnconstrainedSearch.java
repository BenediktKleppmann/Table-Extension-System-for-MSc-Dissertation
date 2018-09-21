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
 package unconstrainedSearch;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVWriter;

import de.uni_mannheim.informatik.dws.winter.matching.aggregators.TopKCorrespondencesAggregator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.AggregateBySecondRecordRule;
import de.uni_mannheim.informatik.dws.winter.matching.rules.FlattenAggregatedCorrespondencesRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.CsvTableParser;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.TableFactory;
import tests.CsvTableParser_keepCase;
import tests.SaveTableToCsv;
import uploadTable.additionalWinterClasses.MatchableTableColumn;
import uploadTable.additionalWinterClasses.MatchableTableRow;
import uploadTable.additionalWinterClasses.WebTableDataSetLoader;

public class UnconstrainedSearch {

	
	public static Table getFusedTable(model.QueryTable queryTableObject, String repositoryName) throws IOException{
		
		queryTableObject.saveToFile("public/exampleData/temp_query_table.csv");
		CsvTableParser csvtableparser = new CsvTableParser();
		Table queryTable = csvtableparser.parseTable(new File("public/exampleData/temp_query_table.csv"));
		
		// TESTING  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//		SaveTableToCsv.save(queryTable, "queryTable");
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		
		Table fused = null;
		
		// get keyColumnIndex
		String keyColumnIndexStr = queryTableObject.getKeyColumnIndex();
		
		
		Integer keyColumnIndex = null;
		if (keyColumnIndexStr == null || keyColumnIndexStr.equals("")){
			keyColumnIndex = queryTable.getSubjectColumnIndex();
		} else{
			keyColumnIndex = Integer.parseInt(keyColumnIndexStr);
			if (keyColumnIndex<0) keyColumnIndex = queryTable.getSubjectColumnIndex();
		}
		
		/*******************************************************
		 * SEARCH
		 *******************************************************/
		System.out.println("Unconstrianed Search2");
		
		TableFactory fac = new TableFactory();
		
		// load the query table
		Map<Integer, Table> tablesById = new HashMap<>();
		tablesById.put(queryTable.getTableId(), queryTable);
		
		// search 
		List<String> foundTablenames = SearchForTables.search(queryTableObject, keyColumnIndex, repositoryName);
		System.out.println("Unconstrianed Search7.1");


		
		if(foundTablenames!=null && foundTablenames.size()>0) {
			System.out.println("Unconstrianed Search7.2");
			
//			TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv

			CSVWriter csvwriter = new CSVWriter(new FileWriter("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/testTable.csv"), ';');
//			^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			
			// load the tables from the search result
			Collection<Table> tables = new LinkedList<>();
			int foundTableId = 1;
			for(String foundTablename : foundTablenames) {
				Table foundTable = fac.createTableFromFile(new File("public/repositories/" + repositoryName + "/tables/" + foundTablename));
				if(foundTable!=null && !foundTable.getPath().equals(queryTable.getPath())) {
					foundTable.setTableId(foundTableId++);
					tablesById.put(foundTable.getTableId(), foundTable);
					tables.add(foundTable);
					//	TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
					String[] new_row = {foundTablename};
					csvwriter.writeNext(new_row);
					//	^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^	
				}
			}

//			TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
			csvwriter.close();
//			^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			
			
			// add the query table to the search results
			// it will match the query table perfectly and make sure that all records are kept in the final result
			// even if no other table contained a certain record
			queryTable.setPath("query");
			queryTable.setTableId(foundTableId);
			tablesById.put(queryTable.getTableId(), queryTable);
			tables.add(queryTable);
			
			

			
			/*******************************************************
			 * SCHEMA MATCHING
			 *******************************************************/
			System.out.println("Unconstrianed Search8");
			
			// load the tables into datasets
			WebTableDataSetLoader loader = new WebTableDataSetLoader();
			FusibleDataSet<MatchableTableRow, MatchableTableColumn> queryDS = loader.createQueryDataSet(queryTable);
			FusibleDataSet<MatchableTableRow, MatchableTableColumn> tablesDS = loader.createTablesDataSet(tables);
			
			// TESTING  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//			SaveTableToCsv.save(queryDS, "queryDS");
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			
			// run schema matching
			WebTableMatcher matcher = new WebTableMatcher();
			Processable<Correspondence<MatchableTableColumn, Matchable>> schemaCorrespondences = null;
			try {
				schemaCorrespondences = matcher.matchSchemas(queryDS, tablesDS);
			} catch (Exception e) {e.printStackTrace();}
			// get the ids of all tables in the search result that could be matched
			Set<Integer> matchedTables = new HashSet<>(schemaCorrespondences.map((Correspondence<MatchableTableColumn, Matchable> cor, DataIterator<Integer> c)-> c.next(new Integer(cor.getSecondRecord().getTableId()))).distinct().get());
			
			// remove unmatched tables
			Processable<MatchableTableColumn> attributes = tablesDS.getSchema().where(((c)->matchedTables.contains(c.getTableId())));

			/*******************************************************
			 * SCHEMA CONSOLIDATION
			 *******************************************************/
			System.out.println("Unconstrianed Search9");
			
			// transform query table and result tables into the consolidated schema
			attributes = queryDS.getSchema().append(attributes);
			SearchJoinSchemaConsolidator consolidator = new SearchJoinSchemaConsolidator(tablesById);
			Pair<Table, Table> consolidated = consolidator.consolidate(queryDS, tablesDS, attributes, schemaCorrespondences, queryTable);

			if(consolidated!=null) {
			
				Table queryConsolidated = consolidated.getFirst();
				Table tablesConsolidated = consolidated.getSecond();
				
				
				// set the subject column to the column that was the subject column in the query table
				for(TableColumn c: queryConsolidated.getSchema().getRecords()) {
					if(c.getProvenance().contains(queryTable.getSubjectColumn().getIdentifier())) {
						queryConsolidated.setSubjectColumnIndex(c.getColumnIndex());
						break;
					}
				}
				for(TableColumn c: tablesConsolidated.getSchema().getRecords()) {
					if(c.getProvenance().contains(queryTable.getSubjectColumn().getIdentifier())) {
						tablesConsolidated.setSubjectColumnIndex(c.getColumnIndex());
						break;
					}
				}

				/*******************************************************
				 * IDENTITY RESOLUTION
				 *******************************************************/
				System.out.println("Unconstrianed Search10");
				
				// create datasets from the consolidated tables
				queryDS = loader.createQueryDataSet(queryConsolidated);
				tablesDS = loader.createQueryDataSet(tablesConsolidated);
				
				
				// TESTING  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//				SaveTableToCsv.save(queryDS, "queryDS2");
				// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
				
				// run identity resolution
				Double minimumInstanceSimilarity = queryTableObject.getMinimumInstanceSimilarity();
				if (minimumInstanceSimilarity == null) minimumInstanceSimilarity = 0.9;
				Processable<Correspondence<MatchableTableRow, Matchable>> recordCorrespondences = matcher.matchRecords(queryDS, tablesDS, minimumInstanceSimilarity);

				// make sure that no two records from the query table are mapped to the same record in a result table
				// the result would be that these records are merged in the final result
				recordCorrespondences = recordCorrespondences
						.aggregate(
								new AggregateBySecondRecordRule<MatchableTableRow, Matchable>(0.0), 
								new TopKCorrespondencesAggregator<>(1))
						.map(new FlattenAggregatedCorrespondencesRule<>());
				
				/*******************************************************
				 * DATA FUSION
				 *******************************************************/
				System.out.println("Unconstrianed Search11");
				
				// fuse the records into a final table
				WebTableFuser fuser = new WebTableFuser();
				
				try {
								
					fused = fuser.fuseTables(queryConsolidated, queryDS, tablesDS, recordCorrespondences);	
						
					// TESTING  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//					SaveTableToCsv.save(fused, "fused");
					// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					
					Double minimumDensity = queryTableObject.getMinimumDensity();
					if (minimumDensity==null){minimumDensity= 0.6;}
					fused = consolidator.removeSparseColumns(fused, minimumDensity);  // remove columns that are mostly NULL
					
					// TESTING  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
//					SaveTableToCsv.save(fused, "fused2");
					// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
					
				} catch (Exception e) {e.printStackTrace();}
				
				
				/*******************************************************
				 *  COPY THE COLUMNS FROM THE TABLE
				 *******************************************************/
				
				// Schema matching
				Map<String, Integer> qtColumnIndexes = new HashMap<String, Integer>();
				CsvTableParser_keepCase csvtableparser_keepCase = new CsvTableParser_keepCase();
				Table queryTable_keepCase = csvtableparser_keepCase.parseTable(new File("public/exampleData/temp_query_table.csv"));
				for (TableColumn column: queryTable_keepCase.getColumns()){
					qtColumnIndexes.put(column.getHeader(), column.getColumnIndex());
				}
				
				
				// Instance Matching
				Map<Integer, Integer> instanceMatches = new HashMap<Integer, Integer>();
				for (TableRow fusedRow: fused.getRows()){
					for (TableRow queryRow: queryTable_keepCase.getRows()){
						if (fusedRow.get(fused.getSubjectColumnIndex()).toString().equalsIgnoreCase(queryRow.get(queryTable_keepCase.getSubjectColumnIndex()).toString())){
							System.out.println("Matching rows " + String.valueOf(fusedRow.getRowNumber()) + " " + String.valueOf(queryRow.getRowNumber()) + ": " + fusedRow.get(fused.getSubjectColumnIndex()) + " <--> " + queryRow.get(queryTable_keepCase.getSubjectColumnIndex()));
							instanceMatches.put(fusedRow.getRowNumber(), queryRow.getRowNumber());
						}
					}
				}
				
							
				for (TableColumn fusedColumn: fused.getColumns()){
					
					Integer qtColumnIndex = null;
					for (String qtheader :qtColumnIndexes.keySet()){
						if (qtheader.equalsIgnoreCase(fusedColumn.getHeader())) qtColumnIndex = qtColumnIndexes.get(qtheader);
					}
					
					if (qtColumnIndex!= null){
						// Insert the column values from the query table
						for (TableRow fusedRow: fused.getRows()){
							int queryTableRowNumber = instanceMatches.get(fusedRow.getRowNumber());
							Object new_value = queryTable_keepCase.get(queryTableRowNumber).get(qtColumnIndex);
							fusedRow.set(fusedColumn.getColumnIndex(), new_value);
						}
						
						// Insert the original header from the query table
						fusedColumn.setHeader(queryTable_keepCase.getSchema().get(qtColumnIndex).getHeader());
						
					}
				}
				
					
				
			}
			
		} else {
			System.out.println("For this table no matches were found in the KeyColumnIndex");
			
		}
		
		return fused;
		
	}
	
	
	
	
	
	public static Table removeSparselyPopulatedColumns(Table fused, Double minimumDensity){
		// initialize numberOfNullsDict  
		Map<Integer, Pair<Integer, Integer>> numberOfNullsDict = new HashMap<Integer, Pair<Integer, Integer>>();
		for (int columnIndex = 0; columnIndex< fused.getSchema().getSize(); columnIndex ++){
			numberOfNullsDict.put(columnIndex, new Pair(0,0));
		}
		
		// loop through values and increment numberOfValues- and numberOfNulls- counters
		for (TableRow row :fused.getRows()){
			for (int columnIndex = 0; columnIndex< fused.getSchema().getSize(); columnIndex ++){
				Integer numberOfValues = numberOfNullsDict.get(columnIndex).getFirst() +1;
				Integer numberOfNulls = numberOfNullsDict.get(columnIndex).getSecond();

				if (row.get(columnIndex)==null) numberOfNulls ++;
				numberOfNullsDict.put(columnIndex, new Pair(numberOfValues, numberOfNulls)); 
			}
		}
		
		//filter out the columns with too low density
		for (int columnIndex = 0; columnIndex< fused.getSchema().getSize(); columnIndex ++){
			Double density = ((double) numberOfNullsDict.get(columnIndex).getFirst())/ ((double) numberOfNullsDict.get(columnIndex).getSecond());
			if (density < minimumDensity) fused.removeColumn(fused.getSchema().get(columnIndex));
		}
		
		return fused;
	}
	
	
	
	
//	public static Table removeUncorrelatedColumns(Table fused, String correlationAttribute, LinkedList headers){
//		
//		Integer subjectColumnIndex = fused.getSubjectColumnIndex()
//		Integer correlationAttributeIndex = headers.indexOf(correlationAttribute);
//		
//		// extract the column information from the fused-Table
//		HashMap<Integer, Column> columns = new HashMap<Integer, Column>();
//		for (int columnIndex = 0; columnIndex< fused.getSchema().getSize(); columnIndex ++){
//			Column column= new Column();
//			column.setHeader(fused.getSchema().get(columnIndex).getHeader());
//			column.setDataType(fused.getSchema().get(columnIndex).getDataType());
//			columns.put(columnIndex, column);
//		}
//		
//		//add the values to the column information
//		for (TableRow row :fused.getRows()){
//			for (int columnIndex = 0; columnIndex< fused.getSchema().getSize(); columnIndex ++){
//				columns.get(columnIndex).addValue(row.get(columnIndex)); 
//			}
//		}
//		
//
//		
//		DataType correlationAttributeDataType = columns.get(correlationAttributeIndex).getDataType();
//		for (int evaluatedColumnIndex = 0; evaluatedColumnIndex< fused.getSchema().getSize(); evaluatedColumnIndex ++){
//			if ((evaluatedColumnIndex != subjectColumnIndex) && (evaluatedColumnIndex != correlationAttributeIndex)){
//				
//				CorrelationCalculator CorrelationCalculator = new CorrelationCalculator(columns.get(correlationAttributeIndex), columns.get(evaluatedColumnIndex));
//				
//				Double correlationCoefficient = CorrelationCalculator.calculateCorrelation();
//				
//				
//				
//				
//			}
//		}
//		
//		return fused;
//	}
//	
//	
//	
//	
//	public static Double calcualtePearsonsCorrelationCoefficient(Column column1, Column column2){
//		Double correlationCoefficient = 0.0;
//		return correlationCoefficient;
//	}
	
	
	
}
