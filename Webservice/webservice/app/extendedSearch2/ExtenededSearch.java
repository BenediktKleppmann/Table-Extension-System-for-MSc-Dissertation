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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.rapidminer.extension.json.Correspondence;
import com.rapidminer.extension.json.JSONRelatedTablesResponse;
import com.rapidminer.extension.json.JSONTableResponse;
import com.rapidminer.extension.json.MetaDataTable;
import com.rapidminer.extension.json.TableInformation;

import au.com.bytecode.opencsv.CSVWriter;
import de.mannheim.uni.ds4dm.utils.ReadWriteGson;
import de.mannheim.uni.types.ColumnTypeGuesser.ColumnDataType;
import de.mannheim.uni.utils.TableColumnTypeGuesser;
import extendedSearch.DS4DMBasicMatcherX;
import extendedSearch.GetCorrespondingTablesX;
import extendedSearch.GetTableInformationX;
import extendedSearch.MakeMatcherX;
import extendedSearch.SearchForTablesX;
import model.ExtendedTableInformation;
import model.QueryTable;
import extendedSearch.GetTableInformationX.DataSource;
import play.mvc.Controller;
import play.mvc.Result;

 	/**
 * The extendedSearch-class manages the most important functions of the {@link extendedSearch2.ExtenededSearch#extendedSearch(String)}-API functionality.
 * More detail on the individual functions is provided below.
 */ 

public class ExtenededSearch extends Controller {

	
	/**
	 * The extendedSearch provides the same functionality as {@link controllers.ExtendTable#search()}. It finds tables that can be used for extending the Query Table with an additional column. 
	 * It returns the names of matching tables, as well as the correspondences of the matching tables to the Query Table.yuv
	 * 
	 * Steps of execution:
	 * <ol>
	 * <li>load the request-body into a QueryTable-Object and set the globalVariables
	 * <li>search in the KeyColumnIndex for tables that have a similar key column as the QueryTable. This is done with {@link extendedSearch2.LuceneQueries#searchForTables_InLucene(GlobalVariables)}
	 * <li>determine the correspondences between the found table and the QueryTable with {@link extendedSearch2.LuceneQueries#addTheTableInformation_FromLucene(Map, GlobalVariables)}
	 * <li>if the number of found tables is less than the maximum number of allowed tables: find indirectly matching tables (i.e. tables that have key column correspondences to the found tables) with {@link extendedSearch2.CorrespondenceManager#findIndirectMatches(Map, GlobalVariables)}
	 * <li>for the indirectly matching tables: find the correspondences to the QueryTable with {@link extendedSearch2.LuceneQueries#addTheTableInformation_FromLucene(Map, GlobalVariables)}. For the schema-correspondences this could be direct or indirect correspondences. 
	 * <li>join the directlyFoundTables and indirectlyFoundTables to allFoundTables
	 * <li>for all found tables: find additional indirect instance-correspondences to the QueryTable with {@link extendedSearch2.CorrespondenceManager#GetCorrespondenceBasedInstanceMatches(Map, GlobalVariables)}. You have an indirect instance-correspondence for an entity, when this entity has a pre-calculated correspondence to the entity of another table, which has a direct correspondence to the QueryTable.
	 * <li>for all found tables: save the table data in json-files using {@link extendedSearch2.ExtenededSearch#saveTableDataForFetching(ExtendedTableInformation, GlobalVariables)}. The json-file for a table will be directly returned when the DS4DM Frontend calls {@link controllers.ExtendTable#fetchTable(String, String)}.
	 * <li>return the table names and correspondences for the found tables to the user in a resonseJsonString
	 * </ol>
	 * 
	 * 
	 * 
	 * @param repositoryName	the name of the repository used for the unconstrainedSearch
	 * @param request().body().asJson()	the Json in the body of the http-post-request is also used as parameter. There is more info on it <a href="web.informatik.uni-mannheim.de/ds4dm/API-definition.html">here</a>.	
	 * @return 					a Json String containing the extended
	 */
	
	public Result extendedSearch(String repositoryName) {
		try{
			PrintStream printStream = new PrintStream(new File("/home/bkleppma/Temporary_folders/2018-05-04/stdout.txt"));
			System.setOut(printStream);
		} catch(Exception e){}
		System.out.println("extendedSearch");
		
		GlobalVariables globalVariables = new GlobalVariables("public/globalVariables.conf");
		
		Gson gson = new Gson();	
		System.out.println(request().body().asJson().toString());
		model.QueryTable queryTable = gson.fromJson(request().body().asJson().toString(), model.QueryTable.class);
		globalVariables.setQueryTable(queryTable);
		globalVariables.setRepositoryName(repositoryName);
		
		
		
//		=========================================================================================
//		=========   ONLY VALID FOR WebWikiTables !!  ===========================================
//		=========   vvvvvvvvvvvvvvvvvvvvvvvvvvvvvv   ============================================
		globalVariables.setTablesFolderPath("/home/bkleppma/Temporary_folders/2018-03-30/tables/");
//		=========================================================================================
//		=========================================================================================
//		=========================================================================================
	
		
		
		Map<String, ExtendedTableInformation> directlyFoundTables = new HashMap<String, ExtendedTableInformation>();	
		Map<String, ExtendedTableInformation> indirectlyFoundTables = new HashMap<String, ExtendedTableInformation>();

		
		
		// GET THE DIRECT MATCHES  ----------------------------
		try {
			directlyFoundTables = LuceneQueries.searchForTables_InLucene(globalVariables);
		} catch (IOException e) { e.printStackTrace();}
		// Testing  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
		for (ExtendedTableInformation directlyFoundTable: directlyFoundTables.values()){System.out.println("directlyFound1: " + directlyFoundTable.getTableName());	}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		
		
		if (globalVariables.getLogFoundTables())  LogFoundTables(directlyFoundTables, "found.csv", globalVariables);
		
		System.out.println("Number of initally found tables:" + String.valueOf(directlyFoundTables.values().size()));
		
		try {	
			directlyFoundTables = LuceneQueries.addTheTableInformation_FromLucene(directlyFoundTables, globalVariables);
		} catch (IOException e) { e.printStackTrace();}
		// Testing  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
		for (ExtendedTableInformation directlyFoundTable: directlyFoundTables.values()){System.out.println("directlyFound2: " + directlyFoundTable.getTableName());	}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		
		
		if (globalVariables.getLogFoundTables())  LogFoundTables(directlyFoundTables, "found_with_instance_matches.csv", globalVariables);
		if (directlyFoundTables == null) System.out.println("directlyFoundTables == null");
		System.out.println("Number of initally found tables with instance matches:" + String.valueOf(directlyFoundTables.values().size()));
		
		
		
		Map<String, ExtendedTableInformation> allFoundTables = directlyFoundTables;
		
		if (directlyFoundTables.size() < globalVariables.getQueryTable().getMaximalNumberOfTables()){
			// GET THE INDIRECT MATCHES  ----------------------------
//			Pair<Map<String, ExtendedTableInformation>, Map<String, ExtendedTableInformation>> foundTables = null;
			Map.Entry<Map<String, ExtendedTableInformation>, Map<String, ExtendedTableInformation>> foundTables = null; 
			try {	
				foundTables =  CorrespondenceManager.findIndirectMatches(directlyFoundTables, globalVariables);
			} catch (IOException e) { e.printStackTrace();}
			directlyFoundTables = foundTables.getKey();
			indirectlyFoundTables = foundTables.getValue();
			// Testing  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
			for (ExtendedTableInformation indirectlyFoundTable: indirectlyFoundTables.values()){System.out.println("indirectlyFound1: " + indirectlyFoundTable.getTableName());	}
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			
			if (globalVariables.getLogFoundTables())  LogFoundTables(indirectlyFoundTables, "extensions.csv", globalVariables);		
	
			try {
				indirectlyFoundTables = LuceneQueries.addTheTableInformation_FromLucene(indirectlyFoundTables, globalVariables);
			} catch (IOException e) { e.printStackTrace();}
			// Testing  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
			for (ExtendedTableInformation indirectlyFoundTable: indirectlyFoundTables.values()){System.out.println("indirectlyFound2: " + indirectlyFoundTable.getTableName());	}
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
			
			if (globalVariables.getLogFoundTables())  LogFoundTables(indirectlyFoundTables, "extensions_with_instance_matches.csv", globalVariables);
			System.out.println("Number of indirectly found tables with instance matches:" + String.valueOf(indirectlyFoundTables.values().size()));
			
			
			// GET ADDITIONAL INSTANCES THROUGH INSTANCE CORRESPONDENCES  ---------------
			allFoundTables.putAll(indirectlyFoundTables);
			// Testing  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
			for (ExtendedTableInformation foundTable: allFoundTables.values()){System.out.println("allFoundTable1: " + foundTable.getTableName());	}
			// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^		
		}

		
		
		System.out.println("Number of tables in total:" + String.valueOf(allFoundTables.values().size()));
		try {
			allFoundTables = CorrespondenceManager.GetCorrespondenceBasedInstanceMatches(allFoundTables, globalVariables);
		} catch (Exception e) { e.printStackTrace();}
		// Testing  vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv
		for (ExtendedTableInformation foundTable: allFoundTables.values()){System.out.println("allFoundTable2: " + foundTable.getTableName());	}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
		
		// Save the Tabledata for the /FetchTable-method to return   ---------------
		System.out.println("saveTableDataForFetching...");
		for (ExtendedTableInformation table: directlyFoundTables.values()){	
			System.out.println("saving: " + table.getTableName());
			saveTableDataForFetching(table, globalVariables);
		}

			


		// build the ResponseObject and return it as JsonString  --------------------------
		System.out.println(" building the ResponseObject and return it as JsonString...");
		QueryTable the_queryTable = globalVariables.getQueryTable();
		JSONRelatedTablesResponse responseObject = the_queryTable.getResponseObject();
		
		ArrayList<TableInformation> relatedTables = new ArrayList<TableInformation>();
		for (ExtendedTableInformation table: directlyFoundTables.values()){
			System.out.println("transfering the tableInformation to relatedTables for: " + table.getTableName());
			relatedTables.add(table.getTableInformation());    // downsize to the smaller TableInformation-object
		}
		
		System.out.println("setting related Tables...");
		responseObject.setRelatedTables(relatedTables);
		System.out.println("converting response object to string...");
		String resonseJsonString = gson.toJson(responseObject);

		return ok(resonseJsonString);
	}
	
	
	

	
	public void LogFoundTables(Map<String, ExtendedTableInformation> tables, String logFilename, GlobalVariables globalVariables){
		if (tables != null){
			if (tables.values().size()>0){
				try{
					CSVWriter csvwriter = new CSVWriter(new FileWriter(globalVariables.getLogFolderPath() + "/" + logFilename), ',');
					ExtendedTableInformation[] tablesArray = tables.values().toArray(new ExtendedTableInformation[tables.values().size()]);
					
					for (int i = 0; i < tablesArray.length ; i++){
					     String[] entry = {tablesArray[i].getTableName()};
					     csvwriter.writeNext(entry);    
					}
					csvwriter.close();
				} catch (IOException e){e.printStackTrace();}		
			}
			else{
				try{
					FileUtils.writeStringToFile(new File(globalVariables.getLogFolderPath() + "/" + logFilename), "There are no tables at this step!");
				} catch (IOException e){e.printStackTrace();}
			}
		}	
	}
	
	
	
	public static void saveTableDataForFetching(ExtendedTableInformation table, GlobalVariables globalVariables)
	//(Map<String, Correspondence> instancesCorrespondences2QueryTable, DS4DMBasicMatcherX matcher, String[][] relation, String tableName, String[] columnHeaders, String[] keyColumn, Integer keyColumnIndex, double tableScore, DataSource dataSource, String repositoryName) throws IOException 
	{
		if (table.getRelation() != null){
			Date lastModified = new Date(System.currentTimeMillis());
			
			String textBeforeTable = "";
			String textAfterTable = "";
			
			String title = "";
			
			double coverage = (double) table.getInstancesCorrespondences2QueryTable().size()/ globalVariables.getQueryTable().getKeyColumn().length;
			double ratio = (double) table.getInstancesCorrespondences2QueryTable().size() / table.getKeyColumn().length;
			double trust = 1; 
			double emptyValues = 0;
			
	
	
			MetaDataTable meta = new MetaDataTable(table.getSchemaSimilarityScore(), 
												   lastModified.toString(),
												   table.getMatchingType(), //table.getValue().getUrl()
												   textBeforeTable, 
												   textAfterTable, 
												   title, 
												   coverage, 
												   ratio,
												   trust, 
												   emptyValues);
			
		
	//		-------------------------------------------------------------------------------------------------------------------
			
			JSONTableResponse t_sab = new JSONTableResponse();
			t_sab.setMetaData(meta);
			t_sab.setHasHeader(true);
			t_sab.setHasKeyColumn(true);
			t_sab.setHeaderRowIndex(Integer.toString(0));
			t_sab.setTableName(table.getTableName());
			
			//t_sab.setKeyColumnIndex			
			String indexCol = Integer.toString(table.getKeyColumnIndex()) + "_" + table.getKeyColumn()[0];
			t_sab.setKeyColumnIndex(indexCol);
			
			
			//t_sab.setRelation	
			List<List<String>> new_relation = new ArrayList<List<String>>();
	
			for(int columnNumber =0; columnNumber < table.getRelation().length; columnNumber++){
				
				List<String> new_column = Arrays.asList(table.getRelation()[columnNumber]);
				String indexedHeader = columnNumber + "_" + new_column.get(0);
				new_column.set(0, indexedHeader);
				
				new_relation.add(new_column);
			}
			t_sab.setRelation(new_relation);
			
			
			//t_sab.setDataTypes
			//RapidMiner Studio crashes if it receives a table that has null values in a "numeric" column. I don't know any solution for this.
			//Therefore w'll just set all datatypes to string:
			Map<String, String> dataTypes = new HashMap<String, String>();
			for (List<String> column: new_relation){
				dataTypes.put(column.get(0), "string");
				System.out.println("The Datatype of " + column.get(0) + " is: string");
			}
					
			
//			Map<String, String> dataTypes = guessTypes();
			
			t_sab.setDataTypes(dataTypes);
	
			
			//t_sab.setMetaData
			meta.setURL(""); // table.getValue().getUrl()
			t_sab.setMetaData(meta);
			
	
	//		-------------------------------------------------------------------------------------------------------------------
			ReadWriteGson<JSONTableResponse> resp = new ReadWriteGson<JSONTableResponse>(t_sab);
			
			File fetchedTablesFolder = new File("public/repositories/" + globalVariables.getRepositoryName() + "/fetchedTables");
			if (!fetchedTablesFolder.exists())
				fetchedTablesFolder.mkdirs();
			File current_table = new File(fetchedTablesFolder.getAbsolutePath() + "/" + table.getTableName());
			try{
				resp.writeJson(current_table);
			} catch(Exception e) {e.printStackTrace();}
		}
	}

	
	
	
	public static Map<String, String> guessTypes (List<List<String>> relation){
		Map<String, String> dataTypes = new HashMap<String, String>();
		TableColumnTypeGuesser tctg = new TableColumnTypeGuesser();
		for (List<String> e: relation){
			ColumnDataType type = ColumnDataType.string;;
			String columnName = e.get(0);
			List<String> columnValues = new ArrayList<String>();
			columnValues.addAll(e);
			columnValues.remove(0);
			type = tctg.guessTypeForColumn(columnValues, columnName, false, null);
			dataTypes.put(columnName, type.toString());
		}
		return dataTypes;
	}
	
	
}
