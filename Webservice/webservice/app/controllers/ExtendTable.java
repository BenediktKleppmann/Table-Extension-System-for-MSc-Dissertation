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
 package controllers;

 
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.opencsv.CSVIterator;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.rapidminer.extension.json.JSONRelatedTablesRequest;
import com.rapidminer.extension.json.JSONRelatedTablesResponse;
import com.rapidminer.extension.json.TableInformation;


import de.mannheim.uni.ds4dm.demo1.exploreData.GenerateMatchingExample_withKeywords;
import de.mannheim.uni.ds4dm.searcher.CandidateBuilder_fromLuceneIndex;
import de.mannheim.uni.ds4dm.utils.ReadWriteGson;
import de.mannheim.uni.ds4dm.refactoredSearch.DS4DMBasicMatcher;
import de.mannheim.uni.ds4dm.refactoredSearch.GetTableInformation;
import de.mannheim.uni.ds4dm.refactoredSearch.MakeMatcher;
//import unconstrainedSearch.SearchForTables;
//import unconstrainedSearch.SearchJoinSchemaConsolidator;
import unconstrainedSearch.UnconstrainedSearch;
//import unconstrainedSearch.WebTableFuser;
//import de.uni_mannheim.informatik.additionalWinterClasses.MatchableTableColumn;
//import de.uni_mannheim.informatik.additionalWinterClasses.MatchableTableRow;
//import de.uni_mannheim.informatik.additionalWinterClasses.WebTableDataSetLoader;
import de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences.Main;
import unconstrainedSearch.WebTableMatcher;
import de.uni_mannheim.informatik.dws.winter.matching.aggregators.TopKCorrespondencesAggregator;
import de.uni_mannheim.informatik.dws.winter.matching.rules.AggregateBySecondRecordRule;
import de.uni_mannheim.informatik.dws.winter.matching.rules.FlattenAggregatedCorrespondencesRule;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.DataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.model.Pair;
import de.uni_mannheim.informatik.dws.winter.processing.Processable;
import de.uni_mannheim.informatik.dws.winter.webtables.Table;
import de.uni_mannheim.informatik.dws.winter.webtables.TableColumn;
import de.uni_mannheim.informatik.dws.winter.webtables.TableRow;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TableHeaderDetectorContentBased;
import de.uni_mannheim.informatik.dws.winter.webtables.detectors.TableKeyIdentification;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.CsvTableParser;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableParser;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.JsonTableSchema;
import de.uni_mannheim.informatik.dws.winter.webtables.parsers.TableFactory;
import de.uni_mannheim.informatik.dws.winter.webtables.writers.CSVTableWriter;
import edu.stanford.nlp.io.EncodingPrintWriter.out;
import de.uni_mannheim.informatik.dws.winter.processing.DataIterator;

//import de.mannheim.uni.ds4dm.extendedSearch.DS4DMBasicMatcherX;
//import de.mannheim.uni.ds4dm.extendedSearch.GetCorresondingTablesX;
//import de.mannheim.uni.ds4dm.extendedSearch.GetTableInformationX;
//import de.mannheim.uni.ds4dm.extendedSearch.MakeMatcherX;
//import de.mannheim.uni.ds4dm.extendedSearch.SearchForTablesX;

import extendedSearch.DS4DMBasicMatcherX;
import extendedSearch.GetCorrespondingTablesX;
import extendedSearch.GetTableInformationX;
import extendedSearch.MakeMatcherX;
import extendedSearch.SearchForTablesX;
//import model.QueryTable;
import play.mvc.Controller;
import tests.CsvTableParser_keepCase;
import tests.SaveTableToCsv;
import uploadTable.FindCorrespondences;
import uploadTable.TableIndexer;
import uploadTable.additionalWinterClasses.MatchableTableColumn;
import uploadTable.additionalWinterClasses.MatchableTableRow;
import uploadTable.additionalWinterClasses.WebTableDataSetLoader;
import play.data.DynamicForm;
import play.data.Form;
import play.*;
import play.mvc.*;
import play.libs.Json;

import views.html.*;




/**
* The ExtendTable-class is the main controller of the DS4DM Backend.
* All the API functions that are specified in ds4dm_webservice/conf/routes directly lead to the execution of one of the functions here.
* Below the functions are described in more detail, but for a high-level overview...
* 
* Data Search Functions:
* <ul>
* <li>search()  -  The old keyword-based search this doesn't return as good results as the extendedSearch, has however a faster execution time
* <li>extendedSearch(String repositoryName)  -  Also called 'correspondence-based Search'. Just like 'search()' it returnes the tablenames and correspondences necessary for extending a table with one additional column
* <li>extendedSearch_T2DGoldstandard()  -  For the case that no repositoryName is specified for the extendedSearch, one of these three functions can be called instead. They use a pre-defined repository.
* <li>extendedSearch_Produktdata()
* <li>extendedSearch_WebWikiTables()
* <li>unconstrainedSearch(String repositoryName)   -  instead of providing the data necessary for extending a table with exactly one additional column, as is done by search() and extendedSearch(), the unconstrainedSearch extends the queryTable with as many columns as possible.
* <li>correlationBasedSearch(String repositoryName)  -  The correlationBasedSearch works the same way as the unconstrainedSearch, just that it only extends the QueryTable with columns that correlate with the 'correlationAttribute'
* </ul>
* <p>
* Fetch Table Functions:  -  the functions search() and extendedSearch() don't return new data, only the correspondences necessary for fusing the new data. In order to get the new data, the fetchTable-function has to be called.
* <ul>
* <li>fetchTable(String name, String repositoryName)  - returns the data and metadata of the specified table
* <li>fetchTable_T2DGoldstandard(String name)  -  in the case that no repository name is specified, the following three functions could be used. They work with pre-defined repositories.
* <li>fetchTable_Produktdata(String name) 
* <li>fetchTable_WebWikiTables(String name)
* </ul>
* <p>
* Repository Maintenance Functions:
* <ul>
* <li>createRepository(String repositoryName)  -  create a new empty repository with the specified name
* <li>getRepositoryNames()  -  returns the names of all existing repositories
* <li>getRepositoryStatistics(String repositoryName)  -  returns information about the specified repository, such as numberOfTablesInRepository, created_timestamp and creator_ip
* <li>deleteRepository(String repositoryName)
* <li>uploadTable(String repositoryName)  -  this uploads a single Table to a repository
* </ul>
* <p>
* Bulk upload functions:  -  for uploading many tables to a repository, the bulk upload has more than an order of magnitude better performance (see <a href="http://web.informatik.uni-mannheim.de/ds4dm/#evaluation">DS4DM Backend website</a>)
* <ul>
* <li>moderateBulkUploadTables(final String repositoryName)  -  this functions makes sure that the bulkUpload is done in parallel and returns a status {"status": "ACCEPTED"} message
* <li>bulkUploadTables(String repositoryName, String uploadId, long startTime)   -  the actual bulkUpload
* <li>getUploadStatus(String repositoryName, String uploadID)   -  this returns "PROCESSING", "UPLOAD SUCCESSFUL", or "UPLOAD UNSUCCESSFUL" depending on the current status of the specified BulkUpload
* </ul>
* 
* @author Benedikt Kleppmann (benedikt@informatik.uni-mannheim.de)
* 
*/


public class ExtendTable extends Controller {

	
	File tebleReositoryFolder = new File("public/exampleData/mappings");
	String conf = "testConf.conf";
	

	/**
	 * ------------------------------------------------------------------------
	 * search()
	 * ------------------------------------------------------------------------
	 * 
	 * The matching is performed by the SearJoin Service, via a POST request to
	 * http://ds4dm.informatik.uni-mannheim.de/search where the input is the
	 * table specified above.
	 * 
	 * The SearchJoin service returns a composite response, containing: A mapping
	 * object, which specifies: the "targetSchema", constructed using the header
	 * of the query input table plus the extension attribute(s) the "dataTypes"
	 * of the target schema the mapping between the initial query table schema
	 * and the target schema ("queryTable2targetSchema") the mapping between the
	 * extension attributes in the query table and the target schema
	 * (“extensionAttributes2targetSchema") and an array of "relatedTables".
	 * Each related table has: a name ("tableName") which is a unique identifier
	 * for the table within the SearchJoin Index the correspondence between
	 * instances in the table and those from the query table
	 * ("instancesCorrespondences2queryTable"); these are given as the <row
	 * index from current table>-<row index in query table> the correspondence
	 * between the schema of the current table and the target schema
	 * ("tableSchema2TargetSchema"); these are given as the <column index from
	 * current table>-<column index in target schema>
	 * 
	 * @return
	 */

	public Result search() {

		JsonNode json = request().body().asJson();

		if (json == null) {
			return badRequest("Expecting Json data");
		} else {

			File response = new File("public/exampleData/response.json");

			ObjectMapper mapper = new ObjectMapper();
			String json_str = "";
			try {
				json_str = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(json);
			} catch (JsonProcessingException e2) {
				e2.printStackTrace();
			}

			File request = new File("public/exampleData/request_temp.json");

			try (FileOutputStream fop = new FileOutputStream(request)) {

				// if file doesn't exists, then create it
				if (!request.exists()) { request.createNewFile();}

				// get the content in bytes
				byte[] contentInBytes = json_str.getBytes();

				fop.write(contentInBytes);
				fop.flush();
				fop.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			JSONRelatedTablesRequest qts = new JSONRelatedTablesRequest();
			try {

				ReadWriteGson<JSONRelatedTablesRequest> rwj = new ReadWriteGson<JSONRelatedTablesRequest>(qts);
				qts = rwj.fromJson(request);

				System.out.println(qts.toString());

			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

			CandidateBuilder_fromLuceneIndex candBuilder = new CandidateBuilder_fromLuceneIndex(conf);

			File fetchedTablesFolder = new File("public/exampleData/tables");
			if (!fetchedTablesFolder.exists())
				fetchedTablesFolder.mkdirs();
			GenerateMatchingExample_withKeywords.serchTables_fromLucene(request, fetchedTablesFolder, response,
					candBuilder);

			System.out.println(response);
			return ok(response);
		}
	}


	
	public Result PreCalculatedSearch(){
		String result = "{\"targetSchema\":[\"game\",\"genre\"],\"dataTypes\":{\"game\":\"string\",\"genre\":\"string\"},\"queryTable2TargetSchema\":{\"game\":\"game\"},\"extensionAttributes2TargetSchema\":{\"genre\":\"genre\"},\"relatedTables\":[{\"instancesCorrespondences2QueryTable\":{\"19\":{\"matching\":\"11\",\"confidence\":0.99},\"54\":{\"matching\":\"18\",\"confidence\":0.99},\"175\":{\"matching\":\"3\",\"confidence\":0.99}},\"tableSchema2TargetSchema\":{\"0_game\":{\"matching\":\"game\",\"confidence\":1.0},\"2_genre\":{\"matching\":\"genre\",\"confidence\":1.0}},\"tableName\":\"46671561_0_6122315295162029872.csv\"},{\"instancesCorrespondences2QueryTable\":{\"11\":{\"matching\":\"1\",\"confidence\":0.99},\"4\":{\"matching\":\"3\",\"confidence\":0.99},\"15\":{\"matching\":\"19\",\"confidence\":0.99},\"5\":{\"matching\":\"18\",\"confidence\":0.99},\"16\":{\"matching\":\"12\",\"confidence\":0.99},\"6\":{\"matching\":\"9\",\"confidence\":0.99},\"17\":{\"matching\":\"2\",\"confidence\":0.99},\"7\":{\"matching\":\"20\",\"confidence\":0.99},\"8\":{\"matching\":\"4\",\"confidence\":0.99}},\"tableSchema2TargetSchema\":{\"0_game\":{\"matching\":\"game\",\"confidence\":1.0},\"3_genre\":{\"matching\":\"genre\",\"confidence\":1.0}},\"tableName\":\"29414811_12_251152470253168163.csv\"},{\"instancesCorrespondences2QueryTable\":{\"79\":{\"matching\":\"14\",\"confidence\":0.99},\"125\":{\"matching\":\"12\",\"confidence\":0.99}},\"tableSchema2TargetSchema\":{},\"tableName\":\"27466715_0_3913547177671701530.csv\"},{\"instancesCorrespondences2QueryTable\":{\"33\":{\"matching\":\"21\",\"confidence\":0.99},\"34\":{\"matching\":\"2\",\"confidence\":0.99},\"35\":{\"matching\":\"17\",\"confidence\":0.99},\"14\":{\"matching\":\"5\",\"confidence\":0.99},\"47\":{\"matching\":\"9\",\"confidence\":0.99},\"26\":{\"matching\":\"10\",\"confidence\":0.99},\"48\":{\"matching\":\"6\",\"confidence\":0.99},\"49\":{\"matching\":\"21\",\"confidence\":0.99},\"31\":{\"matching\":\"16\",\"confidence\":0.99}},\"tableSchema2TargetSchema\":{\"0_game\":{\"matching\":\"game\",\"confidence\":1.0},\"5_genre\":{\"matching\":\"genre\",\"confidence\":1.0}},\"tableName\":\"57938705_0_8737506792349461963.csv\"},{\"instancesCorrespondences2QueryTable\":{\"11\":{\"matching\":\"1\",\"confidence\":0.99},\"12\":{\"matching\":\"16\",\"confidence\":0.99},\"3\":{\"matching\":\"8\",\"confidence\":0.99},\"4\":{\"matching\":\"21\",\"confidence\":0.99},\"6\":{\"matching\":\"11\",\"confidence\":0.99},\"7\":{\"matching\":\"20\",\"confidence\":0.99},\"9\":{\"matching\":\"13\",\"confidence\":0.99}},\"tableSchema2TargetSchema\":{\"0_game\":{\"matching\":\"game\",\"confidence\":1.0},\"3_genre\":{\"matching\":\"genre\",\"confidence\":1.0}},\"tableName\":\"29414811_6_8221428333921653560.csv\"},{\"instancesCorrespondences2QueryTable\":{\"2\":{\"matching\":\"8\",\"confidence\":0.99},\"3\":{\"matching\":\"3\",\"confidence\":0.99},\"14\":{\"matching\":\"19\",\"confidence\":0.99},\"4\":{\"matching\":\"18\",\"confidence\":0.99},\"15\":{\"matching\":\"12\",\"confidence\":0.99},\"16\":{\"matching\":\"2\",\"confidence\":0.99},\"6\":{\"matching\":\"20\",\"confidence\":0.99},\"7\":{\"matching\":\"4\",\"confidence\":0.99},\"8\":{\"matching\":\"13\",\"confidence\":0.99},\"10\":{\"matching\":\"1\",\"confidence\":0.99}},\"tableSchema2TargetSchema\":{\"0_game\":{\"matching\":\"game\",\"confidence\":1.0},\"3_genre\":{\"matching\":\"genre\",\"confidence\":1.0}},\"tableName\":\"29414811_2_4773219892816395776.csv\"},{\"instancesCorrespondences2QueryTable\":{\"11\":{\"matching\":\"1\",\"confidence\":0.99},\"12\":{\"matching\":\"16\",\"confidence\":0.99},\"13\":{\"matching\":\"15\",\"confidence\":0.99},\"15\":{\"matching\":\"19\",\"confidence\":0.99},\"5\":{\"matching\":\"18\",\"confidence\":0.99},\"16\":{\"matching\":\"12\",\"confidence\":0.99},\"6\":{\"matching\":\"9\",\"confidence\":0.99},\"17\":{\"matching\":\"2\",\"confidence\":0.99},\"7\":{\"matching\":\"20\",\"confidence\":0.99},\"8\":{\"matching\":\"4\",\"confidence\":0.99}},\"tableSchema2TargetSchema\":{\"0_game\":{\"matching\":\"game\",\"confidence\":1.0},\"3_genre\":{\"matching\":\"genre\",\"confidence\":1.0}},\"tableName\":\"29414811_13_8724394428539174350.csv\"}]}";
		return ok(result);
	}
	
	
	

	/**
	 * This is an old version of the extendedSearch.<br>
	 * In practice the refactored version is used: {@link extendedSearch2.ExtenededSearch#extendedSearch(String repositoryName)}
	 */
	
	public Result extendedSearch(String repositoryName) {
		
		try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step2");} catch (IOException e){}
		System.out.println("Extended Search with " + repositoryName);
		if (repositoryName == null || repositoryName=="") repositoryName = "DefaultRepository";
		
		JsonNode requestData = request().body().asJson();
		try {
			FileWriter fileWriter = new FileWriter(new File("public/exampleData/request_temp.json"));
			fileWriter.write(requestData.toString());
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		File request = new File("public/exampleData/request_temp.json");
		File response = new File("public/exampleData/response.json");

		DS4DMBasicMatcherX matcher = MakeMatcherX.makeMatcherX(request);

		// Get keyColumnName From The Matcher ----------------------
		List<String> attributes = matcher.getExtAtt();
		String[] extensionHeaders = attributes.toArray(new String[attributes.size()]);
		
		String extensionAttribute = "";
		if (extensionHeaders.length > 0)
			extensionAttribute = extensionHeaders[0];
		

		// Search for tables that have the this keyColumn as one of their
		// columns
		try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step3");} catch (IOException e){}
		Map<String, Double> candidateTableNames = new HashMap<String, Double>();
		try {
			System.out.println("searching for tables with column: " + extensionAttribute);
			candidateTableNames = SearchForTablesX.searchForTables_InLucene(extensionAttribute, repositoryName);
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		
		
		// For each of these Tables get the TableInformation
		try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step4");} catch (IOException e){}
		Map<String, TableInformation> relevantTables = new HashMap<String, TableInformation>();
		int numberOfFoundTables = 0;
		int maxTables = matcher.getMaximalNumberOfTables();
		Iterator<Entry<String, Double>> candidateTablesIterator = candidateTableNames.entrySet().iterator();
		System.out.println("numberOfFoundTables: " + String.valueOf(candidateTableNames.size()) + "; maxTables" + String.valueOf(maxTables));
		System.out.println(candidateTableNames.keySet());
		
		try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step5");} catch (IOException e){}
		while (candidateTablesIterator.hasNext() && numberOfFoundTables < maxTables) {
			Map.Entry<String, Double> pair = (Map.Entry<String, Double>) candidateTablesIterator.next();
			String tableName = pair.getKey();
			try {
				TableInformation tm_ds4dm = null;
				System.out.println("getting table information for:" + tableName);
				tm_ds4dm = GetTableInformationX.getTableInformation_FromLucene(tableName, matcher, pair.getValue(), GetTableInformationX.DataSource.DIREKTMATCH, null, repositoryName, extensionAttribute);
				if (tm_ds4dm != null) System.out.println("3originally found table: " + tableName);
				
				if (tm_ds4dm != null && !relevantTables.containsKey(tableName)) {
					System.out.println("4originally found table: " + tableName);
					numberOfFoundTables++;
					
					// INSERTING THE DIRECT MATCHES  ----------------------------
					relevantTables.put(tableName, tm_ds4dm);
					// --------------------------------------------------------	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step6");} catch (IOException e){}
//		candidateTablesIterator.remove();
		try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step7");} catch (IOException e){}
		

		
		
			
		    
			// GET THE INDIRECT MATCHES  ----------------------------
			Set<String> relevantTableNames =  new HashSet<String>(relevantTables.keySet());
			try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step8");} catch (IOException e){}
			for (String relevantTableName: relevantTableNames){
				try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step9");} catch (IOException e){}
				try {
					relevantTables = GetCorrespondingTablesX.extendRelevantTablesWithCorrespondences_new(relevantTables, relevantTableName, matcher, repositoryName, extensionAttribute);
					try{ FileUtils.writeStringToFile(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test.txt"), "extendedSearch_T2DGoldstandard step7");} catch (IOException e){}
				} catch (Exception e) {e.printStackTrace();}	
			}
			// --------------------------------------------------------	
	

		
		// Write the Tables into JSON-response file
		if (matcher != null) {
			JSONRelatedTablesResponse responseMapping = MakeMatcherX.constructResponseObject(matcher);
			List<TableInformation> relevantTablesList = new ArrayList<TableInformation>(relevantTables.values());
			responseMapping.setRelatedTables(relevantTablesList);
			ReadWriteGson<JSONRelatedTablesResponse> resp = new ReadWriteGson<JSONRelatedTablesResponse>(
					responseMapping);
			try {
				resp.writeJson(response);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println(response);
		return ok(response);
	}

	
	/**
	 * If the DS4DM Frontend does not specify the repositoryName, then this function can be used instead.
	 * It calls {@link extendedSearch2.ExtenededSearch#extendedSearch(String repositoryName)} with the repositoryName set to "T2D_Goldstandard".
	 */	
	public Result extendedSearch_T2DGoldstandard() {
		System.out.println("extendedSearch_T2DGoldstandard");
		String repositoryName = "T2D_Goldstandard";
		extendedSearch2.ExtenededSearch ExtenededSearchObject = new extendedSearch2.ExtenededSearch();
		Result result = ExtenededSearchObject.extendedSearch(repositoryName);
		return result;
	}
	
	/**
	 * If the DS4DM Frontend does not specify the repositoryName, then this function can be used instead.
	 * It calls {@link extendedSearch2.ExtenededSearch#extendedSearch(String repositoryName)} with the repositoryName set to "ProductDataRepository_withSubjectcolumns".
	 */	
	public Result extendedSearch_Produktdata() {
		System.out.println("extendedSearch_Produktdata");
		String repositoryName = "ProductDataRepository_withSubjectcolumns";
		extendedSearch2.ExtenededSearch ExtenededSearchObject = new extendedSearch2.ExtenededSearch();
		Result result = ExtenededSearchObject.extendedSearch(repositoryName);
		return result;
	}
	
	/**
	 * If the DS4DM Frontend does not specify the repositoryName, then this function can be used instead.
	 * It calls {@link extendedSearch2.ExtenededSearch#extendedSearch(String repositoryName)} with the repositoryName set to "WebWikiTables".
	 */	
	public Result extendedSearch_WebWikiTables() {
		System.out.println("extendedSearch_WebWikiTables");
		String repositoryName = "WebWikiTables";
		extendedSearch2.ExtenededSearch ExtenededSearchObject = new extendedSearch2.ExtenededSearch();
		Result result = ExtenededSearchObject.extendedSearch(repositoryName);
		return result;
	}
	
	
	
	/**
	 * {@link controllers.ExtendTable#search()} and {@link extendedSearch2.ExtenededSearch#extendedSearch(String repositoryName)} provide the information necessary for extending a table with <b>one additional column</b>.<br>
	 * The unconstrainedSearch on the other hand has the following two differences:
	 * <ul>
	 * <li>it returns the a fully fused table (instead of providing the data for the fusion to happen in the frontend)
	 * <li>it extends the query table with <b>many additional columns</b> columns. Specifically, all columns that it manages to populate to a certain threshold density (specified by the parameter "minimumDensity" in the http-post-body)
	 * <ul>
	 * <br>

	 * Steps of execution:
	 * <ol>
	 * <li>extract the query table from the http-request-body
	 * <li>extend the query table with {@link unconstrainedSearch.UnconstrainedSearch#getFusedTable(model.QueryTable queryTableObject, String repositoryName)}
	 * <li>save the returned data as a relation (=list of columns)
	 * <li>save the relation table in a Table-Object, convert this to a json string and return it
	 * </ol>
	 * 
	 * 
	 * @param repositoryName	the name of the repository used for the unconstrainedSearch
	 * @param request().body().asJson()	the Json in the body of the http-post-request is also used as parameter. There is more info on it <a href="web.informatik.uni-mannheim.de/ds4dm/API-definition.html">here</a>.	
	 * @return 					a Json String containing the extended table
	 */
	
	public Result unconstrainedSearch(String repositoryName) {
		
		try{
			FileOutputStream fileOutputStream = new FileOutputStream(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test3.txt"));
			System.setOut(new PrintStream(fileOutputStream));
		}catch (FileNotFoundException e){e.printStackTrace();}
		
		System.out.println("Unconstrianed Search....");
		if (repositoryName == null || repositoryName=="") repositoryName = "DefaultRepository";
		
	
		Gson gson = new Gson();	
		model.QueryTable queryTableObject = gson.fromJson(request().body().asJson().toString(), model.QueryTable.class);
		// TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv		
		System.out.println("minimumDensity:" + String.valueOf(queryTableObject.getMinimumDensity()));
		System.out.println("KeyColumnIndex:" + String.valueOf(queryTableObject.getKeyColumnIndex()));
		for (String[] row : queryTableObject.getQueryTable()){
			System.out.println("QueryTable row :" + String.join(",", row));
		}
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^				
		
		System.out.println("Unconstrianed Search1");
		Table fused = UnconstrainedSearch.getFusedTable(queryTableObject, repositoryName);
		
	
		/*******************************************************
		 * RETURN FUSED TABLE
		 *******************************************************/
		System.out.println("Unconstrianed Search12");
		//make relation--------------------------------------
		
		String[][] relation = queryTableObject.getQueryTable();
		if (fused != null){
			relation = new String[fused.getSchema().getSize()][fused.getRows().size() + 1];
			
			//copy the headers
			for (int columnIndex= 0; columnIndex< fused.getSchema().getSize(); columnIndex++){
				relation[columnIndex][0] = fused.getSchema().get(columnIndex).getHeader().toString();
			}
			
			//copy the values
			for (TableRow row: fused.getRows()){
				System.out.println("row " + String.valueOf(row.getRowNumber()));
				for (int columnIndex= 0; columnIndex < fused.getSchema().getSize(); columnIndex++){
					// System.out.println(String.valueOf(row.getRowNumber()) + " - " + String.valueOf(columnIndex));
					if (row.get(columnIndex)!=null){
						relation[columnIndex][row.getRowNumber()+1] = row.get(columnIndex).toString();
						if (row.get(columnIndex).toString().equals("")) relation[columnIndex][row.getRowNumber()+1] = null;
					} 
				}
			}
		}

		System.out.println("Unconstrianed Search13");
		uploadTable.Table fusedTable = new uploadTable.Table();
		fusedTable.setRelation(relation);
		fusedTable.setTablename("fused table");
		
		
		String fusedTableJson = gson.toJson(fusedTable, uploadTable.Table.class);
//		System.out.println("the fused table is: " + fusedTableJson);

		return ok(fusedTableJson);
	}

	
	
	
	
	public Result test(String repository, String index) throws IOException, org.apache.lucene.queryparser.classic.ParseException{
		
		String indexPath = "/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/repositories/" + repository + "/indexes/" + index;
		Directory dir = FSDirectory.open(new File(indexPath));
		IndexReader indexReader = DirectoryReader.open(dir);
		
		// Query		
//		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
//		QueryParser queryParser = new QueryParser(Version.LUCENE_46, "keyColumnString", new StandardAnalyzer(Version.LUCENE_46));
//		String queryString = "normalizedproductname~3 vizio~3 d43c1~3 43uf6400~3 55eg9600~3 65eg9600~3 gpx~3 tde1384b~3 samsung~3 un40ju6400f~3 sony~3 xbr43x830c~3 samsung~3 un60js7000f~3 samsung~3 un50hu6900f~3";
//		Query q = queryParser.parse(queryString);
//		ScoreDoc[] hits = indexSearcher.search(q, 1000).scoreDocs;
		
		// Output everything	
		String returnString = "";
		for (int i=0; i<indexReader.maxDoc(); i++) {
	        Document doc = indexReader.document(i);
	        System.out.println(String.valueOf(i) + "   " + doc.getField("tableHeader").stringValue());
	        returnString = returnString + String.valueOf(i) + "   " + doc.getField("tableHeader").stringValue() + "$";
		}
		
		return ok(returnString);
	}
	
	
	public void test2(String repository) throws IOException, org.apache.lucene.queryparser.classic.ParseException{
		
		String indexPath = "/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/repositories/" + repository + "/indexes/KeyColumnIndex";
		Directory dir = FSDirectory.open(new File(indexPath));
		IndexReader indexReader = DirectoryReader.open(dir);
		
		String returnString = "";
		// Query		
		IndexSearcher indexSearcher = new IndexSearcher(indexReader);
		QueryParser queryParser = new QueryParser(Version.LUCENE_46, "keyColumnString", new StandardAnalyzer(Version.LUCENE_46));
		String queryString = "normalizedproductname~3 vizio~3 d43c1~3 43uf6400~3 55eg9600~3 65eg9600~3 gpx~3 tde1384b~3 samsung~3 un40ju6400f~3 sony~3 xbr43x830c~3 samsung~3 un60js7000f~3 samsung~3 un50hu6900f~3";
		Query q = queryParser.parse(queryString);
		ScoreDoc[] hits = indexSearcher.search(q, 1000).scoreDocs;
		for (ScoreDoc hit: hits){
			Integer docID = hit.doc;
			Document doc = indexSearcher.doc(docID);
			System.out.println("(" + String.valueOf(hit.score) + ") Found table " + doc.getField("tableHeader").stringValue() + "; key_column_index = '" + doc.getField("keyColumnString").stringValue() + "'");
			returnString += "(" + String.valueOf(hit.score) + ") Found table " + doc.getField("tableHeader").stringValue() + "; key_column_index = '" + doc.getField("keyColumnString").stringValue() + "'";	
		}
	}
	
	
	
	public Result test3(){
		try{
//			Process process = Runtime.getRuntime().exec("python public/exampleData/temp_tables/correlation_based_filtering.py  0.1 \"Museum\" \"Visitors 2010\"");
			
//			ProcessBuilder builder = new ProcessBuilder("python", "public/exampleData/temp_tables/correlation_based_filtering.py", "0.1", "Museum", "Visitors 2010"); 
//			Process process = builder.start();
			
			Process process = Runtime.getRuntime().exec(new String[] { "bash", "-c", "python public/exampleData/temp_tables/correlation_based_filtering.py  0.1 \"Museum\" \"Visitors 2010\"" });
		    Scanner scanner = new Scanner(process.getInputStream());
		    
		    while (scanner.hasNext()) {
		    	System.out.println(scanner.nextLine());
		    }
			process.waitFor();
			System.out.println("line3");
			System.out.println(String.valueOf(process.exitValue()));
	
			int len;
			if ((len = process.getErrorStream().available()) > 0) {
			  byte[] buf = new byte[len];
			  process.getErrorStream().read(buf);
			  out.println("Command error:\t\""+new String(buf)+"\"");
			}
		
			

			
			
			return ok("done");
		} catch (Exception e){e.printStackTrace();return ok("exception");}
	}
	
	
	/**
	 * The {@link controllers.ExtendTable#unconstrainedSearch(String repositoryName)} extends the query table with as many columns as possible. When this is done with a big repository, this can lead to significantly more than 100 columns being added to the query table.
	 * This can be overwhelming for the user. This is why the correlationBasedSearch is useful, it extends the query table only with columns that correlate with the "correlation attribute" (specified by the parameter "correlationAttribute" in the http-request-body).<br> 
	 * 
	 * Steps of execution:
	 * <ol>
	 * <li>extract the query table from the http-request-body
	 * <li>pivot the query table - to be a list of rows instead of a list of columns. Save it in temp_table1.csv
	 * <li>extend the query table with as many columns as possible with {@link unconstrainedSearch.UnconstrainedSearch#getFusedTable(model.QueryTable, String)}
	 * <li>save the extended table to temp_table2.csv
	 * <li>execute correlation_based_filtering in a new process
	 * <li>read the result (temp_table3.csv), pivot it to be a list of columns and return it as json string
	 * </ol>
	 * 
	 * @param repositoryName	the name of the repository used for the unconstrainedSearch
	 * @param request().body().asJson()	the Json in the body of the http-post-request is also used as parameter. There is more info on it <a href="web.informatik.uni-mannheim.de/ds4dm/API-definition.html">here</a>.	
	 * @return 					a Json String containing the extended table
	 */

	public Result correlationBasedSearch(String repositoryName) throws IOException, InterruptedException{
		
		try{
			FileOutputStream fileOutputStream = new FileOutputStream(new File("public/exampleData/test3.txt"));
			System.setOut(new PrintStream(fileOutputStream));
		}catch (FileNotFoundException e){e.printStackTrace();}
		System.out.println("correlationBasedSearch....");
		
		if (repositoryName == null || repositoryName=="") repositoryName = "DefaultRepository";
		

		Gson gson = new Gson();	
		model.QueryTable queryTableObject = gson.fromJson(request().body().asJson().toString(), model.QueryTable.class);
		
		/*******************************************************
		 * save the original table to csv
		 *******************************************************/
		String[][] transposedRelation = queryTableObject.getQueryTable();
		String[][] relationValues = new String[transposedRelation[0].length][transposedRelation.length];
		
		for (int columnnumber=0; columnnumber<transposedRelation.length;  columnnumber++){
			for( int rownumber=0; rownumber<transposedRelation[columnnumber].length;  rownumber++){
				 String value = transposedRelation[columnnumber][rownumber];
				 relationValues[rownumber][columnnumber] =value;
			}
		}
		
		CSVWriter writer1 = new CSVWriter(new FileWriter("public/exampleData/temp_tables/temp_table1.csv"), '\t');
		for (String[] row: relationValues){
			 writer1.writeNext(row);
		}
		writer1.close();
		
		/*******************************************************
		 * Unconstrianed Search
		 *******************************************************/
		System.out.println("Unconstrianed Search...");
		Table fused = UnconstrainedSearch.getFusedTable(queryTableObject, repositoryName);
		
		// TESTING vvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvvv		
//		SaveTableToCsv.save(fused, "fused");
		// ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^		
	
		/*******************************************************
		 * save the fused-table to csv
		 *******************************************************/
		System.out.println("save the fused-table to csv");
		
	    CSVWriter writer = new CSVWriter(new FileWriter("public/exampleData/temp_tables/temp_table2.csv"), '\t');

	    
	    System.out.println("= 1 =================================");
		if (fused != null){
			//save the headers  -------------------------------------------------------
			String[] header = new String[fused.getSchema().getSize()];
			for (int columnIndex= 0; columnIndex< fused.getSchema().getSize(); columnIndex++){
				header[columnIndex] = fused.getSchema().get(columnIndex).getHeader().toString();
			}
		    writer.writeNext(header);
		    System.out.println(Arrays.toString(header));
			//save the values  -------------------------------------------------------
			for (TableRow row: fused.getRows()){
				String[] row_array = new String[fused.getSchema().getSize()];
				for (int columnIndex= 0; columnIndex < fused.getSchema().getSize(); columnIndex++){
					if (row.get(columnIndex)!=null){
						row_array[columnIndex]= row.get(columnIndex).toString();
					} else {
						row_array[columnIndex]= "";
					}
				}
			writer.writeNext(row_array);	
			System.out.println(Arrays.toString(row_array));
			}
		}
		writer.close();
		System.out.println("==================================");
		
		
		/*******************************************************
		 * correlation based filtering
		 *******************************************************/
		
		System.out.println("correlation based filtering");
		Double minimumCorrelation = queryTableObject.getMinimumCorrelation();
		if (minimumCorrelation == null){minimumCorrelation = 0.4;}
		

//		Process process = Runtime.getRuntime().exec("python public/exampleData/temp_tables/correlation_based_filtering.py  " + String.valueOf(minimumCorrelation) + " \"" + fused.getSubjectColumn().getHeader()  + "\" \"" + queryTableObject.getCorrelationAttribute() + "\"");
//		Process process = new ProcessBuilder("python", "public/exampleData/temp_tables/correlation_based_filtering.py", String.valueOf(minimumCorrelation), fused.getSubjectColumn().getHeader(), queryTableObject.getCorrelationAttribute()).start();
		
		Process process = Runtime.getRuntime().exec(new String[] { "bash", "-c", "python public/exampleData/temp_tables/correlation_based_filtering.py " + String.valueOf(minimumCorrelation) + " '" + fused.getSubjectColumn().getHeader()  + "' '" + queryTableObject.getCorrelationAttribute() + "'" });
		
	    Scanner scanner = new Scanner(process.getInputStream());
	    
	    PrintWriter out = new PrintWriter("public/exampleData/temp_tables/test.txt");
		System.out.println("python public/exampleData/temp_tables/correlation_based_filtering.py  " + String.valueOf(minimumCorrelation) + " '" + fused.getSubjectColumn().getHeader()  + "' '" + queryTableObject.getCorrelationAttribute() + "'" );
		System.out.println("line2");
	    while (scanner.hasNext()) {
	    	System.out.println(scanner.nextLine());
	    }
		process.waitFor();
		System.out.println("line3");
		System.out.println(String.valueOf(process.exitValue()));

		int len;
		if ((len = process.getErrorStream().available()) > 0) {
		  byte[] buf = new byte[len];
		  process.getErrorStream().read(buf);
		  out.println("Command error:\t\""+new String(buf)+"\"");
		}
		
		out.close();


		/*******************************************************
		 * get the result from csv-file and return it
		 *******************************************************/
		System.out.println("= 2 =================================");
	    CSVReader reader = new CSVReader(new FileReader("public/exampleData/temp_tables/temp_table3.csv"), '\t');
	    List<List<String>> temporary_table_values = new LinkedList<List<String>>();
	    String [] nextLine;
	    while ((nextLine = reader.readNext()) != null) {
	   	 	List<String> new_row = Arrays.asList(nextLine);
	    	temporary_table_values.add(new_row);
	    	System.out.println(Arrays.toString(nextLine));
	    }
	    System.out.println("==================================");
	     
	    String[][] relation = new String[temporary_table_values.get(0).size()][temporary_table_values.size()];
	    for (int rowIndex = 0; rowIndex < temporary_table_values.size(); rowIndex++){
		     for (int colIndex = 0; colIndex < temporary_table_values.get(rowIndex).size(); colIndex++){
		    	 relation[colIndex][rowIndex] = temporary_table_values.get(rowIndex).get(colIndex); 
		    	 if (temporary_table_values.get(rowIndex).get(colIndex).equals("")) relation[colIndex][rowIndex] = null;
		     }
	    }
		

		System.out.println("prepare the return-object");
		uploadTable.Table fusedTable = new uploadTable.Table();
		fusedTable.setRelation(relation);
		fusedTable.setTablename("fused table");
		
		String fusedTableJson = gson.toJson(fusedTable, uploadTable.Table.class);
//		System.out.println("the fused table is: " + fusedTableJson);

		return ok(fusedTableJson);
	}
	
	
	
	

	/**
	 * Returns the names of all existing repositories.<br>
	 * 
	 * Steps of execution:
	 * <ol>
	 * <li>get list of sub-folders in the folder "public/repositories/"
	 * <li>concatenate the names of the sub-folders to a list
	 * <li>return the list as json-string
	 * <ol>
	 * 
	 * @return 					a Json String the list of repository names
	 */

	
	public Result getRepositoryNames() {

		System.out.println("getRepositoryNames");
		String directoryPath = "public/repositories/";

		
		File[] repositoryFolders = (new File(directoryPath)).listFiles();
		if(repositoryFolders==null){System.out.println("isnull");}
		String repositoryNamesList = "{\"repositoryNames\": [";  
		for (int index=0; index<repositoryFolders.length; index++){
			if (repositoryFolders[index].isDirectory()){
				repositoryNamesList += "\"" + repositoryFolders[index].getName() + "\",";
			}	
		}
		if (repositoryNamesList.substring(repositoryNamesList.length() -1).equals(",")){
			repositoryNamesList = repositoryNamesList.substring(0, repositoryNamesList.length() -1) + "]}";
		}
	    
		return ok(repositoryNamesList);
	}
	
	
	/**
	 * Returns information about the specified repository, such as numberOfTablesInRepository, created_timestamp and creator_ip.<br>
	 * 
	 * Steps of execution:
	 * <ol>
	 * <li>see how many tables are in the folder "public/repositories/"+ repositoryName + "/tables/". This is the numberOfTablesInRepository.
	 * <li>read repositoryStatistics.json as JSONObject (this contains created_timestamp and creator_ip) and add the numberOfTablesInRepository.
	 * <li>return the JSONObject as json-string
	 * <ol>
	 * 
	 * @param repositoryName	the name of the repository for which the information should be returned
	 * @return 					a Json String with the numberOfTablesInRepository, created_timestamp and creator_ip for the specified repository.
	 */
	public Result getRepositoryStatistics(String repositoryName) {
		
		try{
			System.out.println("getRepositoryStatistics");
			String tablesFolderPath = "public/repositories/" + repositoryName + "/tables/";
			
			File[] tablesFolder = (new File(tablesFolderPath)).listFiles();
			Integer numberOfTablesInRepository = 0;
			if(tablesFolder!=null){
				numberOfTablesInRepository = tablesFolder.length;
			}
	
			JSONParser jsonParser = new JSONParser();
			JSONObject jsonObject = (JSONObject) jsonParser.parse(new FileReader("public/repositories/" + repositoryName + "/repositoryStatistics.json"));
			jsonObject.remove("creator_ip");
			jsonObject.put("number_of_tables_in_repository", numberOfTablesInRepository);
			
			try (FileWriter file = new FileWriter("public/exampleData/temp_repository_statistics.json")) {
				file.write(jsonObject.toJSONString());
			}
	     
		} catch(Exception e){
			e.printStackTrace();
			return internalServerError("An internal error occured. Probably the Repository doesn't exist.");	
		}
	     return ok(new File("public/exampleData/temp_repository_statistics.json"));
	}
	
	
	
	/**
	 * Deletes the specified repository.<br>
	 * 
	 * Steps of execution:
	 * <ol>
	 * <li>retrieve the ip of the repository creator from repositoryStatistics.json
	 * <li>check if the ip of the computer that sent the http-request matches that of the repository creator
	 * <li><b>IF</b> the ips match, or the correct password was submitted in the http-request-body, <b>THEN</b> delete the folder of the specified repository (with its subfolders).
	 * <ol>
	 * 
	 * @param repositoryName	the name of the repository being deleted
	 * @param request().body().asJson()	(optional) admin-password in the body of the http-post-request. 
	 * @return 					the message: "The repository " + repositoryName + " was deleted."
	 */
	public Result deleteRepository(String repositoryName) {
		System.out.println("deleteRepository");
		
		try {	
			String correctPassword = "emergencyAdminPassword";
			System.out.println(request().body().asText());
			
	        InputStream in = new FileInputStream(new File("public/repositories/AdminPassword.txt"));
	        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
	        correctPassword =reader.readLine();
			in.close();
			reader.close();

	        JSONParser jsonParser = new JSONParser();
//	        FileReader fileReader = new FileReader("C:/Users/UNI-Mannheim/Documents/DS4DM_backend/ds4dm_webservice/DS4DM/DS4DM_webservice/public/repositories/" + repositoryName + "/repositoryStatistics.json");
	        FileReader fileReader = new FileReader("public/repositories/" + repositoryName + "/repositoryStatistics.json");
			JSONObject jsonObject = (JSONObject) jsonParser.parse(fileReader);
			String correctIP = (String) jsonObject.get("creator_ip");
			fileReader.close();


			String submittedPassword = request().body().asText();
			if (submittedPassword == null) submittedPassword= "";
			
			if (submittedPassword.equals(correctPassword) || request().remoteAddress().equals(correctIP)){
				System.out.println("The access rights are sufficient - proceeding with the deleting of " + repositoryName);
//				FileUtils.deleteDirectory(new File("C:/Users/UNI-Mannheim/Documents/DS4DM_backend/ds4dm_webservice/DS4DM/DS4DM_webservice/public/repositories/" + repositoryName));
				FileUtils.deleteDirectory(new File("public/repositories/" + repositoryName));
			} else {
				return internalServerError("The access rights aren't sufficient to proceed with the deletion. You either need to have the admin password in the request-body, or your IP address must match that of the repository creator.");
			}	
		} catch (Exception e)  {
			e.printStackTrace();
			return internalServerError("An internal error occured. Probably the Repository doesn't exist.");
		}
	    return ok("The repository " + repositoryName + " was deleted.");
	}
	
	
	/**
	 * Create a new empty repository with the specified name.<br>
	 * 
	 * Steps of execution:
	 * <ol>
	 * <li>if no repositoryName was chosen, then "DefaultRepository" is chosen as name.
	 * <li>if there is no repository-folder yet with the specified repositoryName, then create the repository-folder along with all its subfolders.
	 * <li>create the repositoryStatistics.json-file. This made from a JSONObject containing the repository name, the current timestamp, and the ip-adress from the request-sender.
	 * <li> return "Successfully created repository " + repositoryName  
	 * <ol>
	 * 
	 * @param repositoryName	the name of the repository being created
	 * @return 					the message: "Successfully created repository " + repositoryName
	 */
	
public Result createRepository(String repositoryName) {
		
		System.out.println("Creating Repository ---------------------------");
		if (repositoryName == null || repositoryName=="") repositoryName = "DefaultRepository";

		
		File repository = new File("public/repositories/" + repositoryName);
		if (!repository.exists()) {
			System.out.println("This repository doesn't exist -> creating new one");
			(new File("public/repositories/" + repositoryName)).mkdirs();
			(new File("public/repositories/" + repositoryName + "/indexes")).mkdirs();
			(new File("public/repositories/" + repositoryName + "/indexes/ColumnNameIndex")).mkdirs();
			(new File("public/repositories/" + repositoryName + "/indexes/KeyColumnIndex")).mkdirs();
			(new File("public/repositories/" + repositoryName + "/indexes/TableIndex")).mkdirs();
			(new File("public/repositories/" + repositoryName + "/correspondences")).mkdirs();
			(new File("public/repositories/" + repositoryName + "/correspondences/instanceCorrespondences")).mkdirs();
			(new File("public/repositories/" + repositoryName + "/tables")).mkdirs();
			(new File("public/repositories/" + repositoryName + "/extensionAttributePositions")).mkdirs();
			(new File("public/repositories/" + repositoryName + "/bulkUploadLogs")).mkdirs();	
			
			
			// create repositoryStatistics-file
			JSONObject repositoryStatistics = new JSONObject();
			repositoryStatistics.put("repository_name", repositoryName);
			repositoryStatistics.put("created_timestamp", (new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")).format(new Date()));
			repositoryStatistics.put("creator_ip", request().remoteAddress());
			if (repositoryStatistics==null){System.out.println("repositoryStatistics==null");}
			System.out.println("Trying to save repositoryStatistics: " + repositoryStatistics.toJSONString());
			try{
//				String filepath = "C:/Users/UNI-Mannheim/Documents/DS4DM_backend/ds4dm_webservice/DS4DM/DS4DM_webservice/public/repositories/" + repositoryName + "/repositoryStatistics.json";
				String filepath = "public/repositories/" + repositoryName + "/repositoryStatistics.json";
				FileUtils.writeStringToFile(new File(filepath), repositoryStatistics.toJSONString());
			} catch (Exception e){return internalServerError("The file repositoryStatistics.json couldn't be created.");}

			
		} else {
			return internalServerError("No repository created, because this repository already exists.");
		}	

		return ok("Successfully created repository " + repositoryName);

	}
	

	/**
	 * This uploads a single Table to a specified repository.
	 * The table-data is passed as json in the body of the post-request. More information on the format of the json is <a href="web.informatik.uni-mannheim.de/ds4dm/API-definition.html">here</a>.<br>
	 * 
	 * Steps of execution:
	 * <ol>
	 * <li>check if the specified repository exists. If not: return "The repository " + repositoryName + " doesn't exist"
	 * <li>convert the table in body of the post-request to a Table-Object. Save this table (as csv-file) in the  "public/repositories/" + repositoryName + "/tables/" folder.
	 * <li>open/create the three indexes: KeyColumnIndex, ColumnNameIndex and TableIndex.
	 * <li>save the table in the indexes by executing {@link uploadTable.TableIndexer#writeTableToIndexes(File)} 
	 * <li>retrieve the keyColumn of the submitted table from the KeyColumnIndex 
	 * <li>get Table-object for the submitted table by reading it from where we saved it (as csv-file) in step 2. 
	 * <li>format the keyColumn (from step 5) and use it to search in the KeyColumnIndex for tables with similar key columns.
	 * <li>for each table found this way:
	 * <ol>
	 * <ul>
	 * <li>   get the instance-correspondences to the submitted table and save them to in the correspondence-files by executing {@link uploadTable.FindCorrespondences#getInstanceMatches(DataSet, DataSet, File, File, String)}
	 * <li>   get the schema-correspondences to the submitted table and save them to in the correspondence-files by executing {@link uploadTable.FindCorrespondences#getDuplicateBasedSchemaMatches(DataSet, DataSet, File, File, Processable, String)}
	 * </ul>
	 * 
	 * @param repositoryName	the name of the repository to which the table should be uploaded to
	 * @param request().body().asJson()	The request body should contain the table data of the table that is to be uploaded.  
	 * @return 					the message: "Table " + tableName + " successfully uploaded to " + repositoryName
	 */	

	public Result uploadTable(String repositoryName) {
		System.out.println("Uploading Table ---------------------------");
		
		// check if repositoryName was given and wether this repository exists
		if (repositoryName == null || repositoryName=="") {
			return internalServerError("No repository name given. Please pass a repository name with the url-paramenter 'repository'." );
		}
		File repository = new File("public/repositories/" + repositoryName);
		if (!repository.exists()) {
			return internalServerError("The repository " + repositoryName + " doesn't exist" );
		}
		
		
//		String indexFolderPath = "C:/Users/UNI-Mannheim/Documents/DS4DM_backend/ds4dm_webservice/DS4DM/DS4DM_webservice/public/repositories/" + repositoryName + "/indexes";
		String indexFolderPath = "public/repositories/" + repositoryName + "/indexes";
//		String correspondenceFolderPath = "C:/Users/UNI-Mannheim/Documents/DS4DM_backend/ds4dm_webservice/DS4DM/DS4DM_webservice/public/repositories/" + repositoryName + "/correspondences";
		String correspondenceFolderPath = "public/repositories/" + repositoryName + "/correspondences";
		
		//  =========================  PART1: INDEXING ==================================== 
		System.out.println("Creating Indexes");
		Gson gson = new Gson();
		
		JsonNode newTableJson = request().body().asJson();
		uploadTable.Table newTableObject = gson.fromJson(newTableJson.toString(), uploadTable.Table.class);
		String tableName = newTableObject.getTablename();
		Integer keyColumnIndex = newTableObject.getKeyColumnIndex();
		
		
		String newFilePath = "public/repositories/" + repositoryName + "/tables/" + tableName;
		try { 
			newTableObject.saveToFile(newFilePath);
		} catch (IOException e) {
			e.printStackTrace();
			return internalServerError("Error, the file " + tableName + " couldn't be saved as csv.");	
		}
		System.out.println("----------------  " + tableName + "  --------------------");
		
		
		
		//--------------------------------------------------------
		//open the indexes
		//--------------------------------------------------------
    	Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_CURRENT);
    	TableIndexer tableIndexer = new TableIndexer();
    	tableIndexer.setKeyColumnIndex(keyColumnIndex);
    	

    	
    	try {
			// get the keyColumnIndexWriter and save it in the TableIndexer
	    	IndexWriterConfig config1 = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
			String indexPath1 = indexFolderPath + "/KeyColumnIndex/";
		    Directory directory1 = FSDirectory.open(new File(indexPath1));
		    IndexWriter keyColumnIndexWriter = new IndexWriter(directory1, config1);
	    	tableIndexer.setKeyColumnIndexWriter(keyColumnIndexWriter);
	    	
			// get the ColumnNameIndex and save it in the TableIndexer
	    	IndexWriterConfig config2 = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
			String indexPath2 = indexFolderPath + "/ColumnNameIndex/";
		    Directory directory2 = FSDirectory.open(new File(indexPath2));
		    IndexWriter columnNameIndexWriter = new IndexWriter(directory2, config2);
	    	tableIndexer.setColumnNameIndexWriter(columnNameIndexWriter);
	    	
			// get the TableIndex and save it in the TableIndexer
	    	IndexWriterConfig config3 = new IndexWriterConfig(Version.LUCENE_CURRENT, analyzer);
			String indexPath3 = indexFolderPath + "/TableIndex/";
		    Directory directory3 = FSDirectory.open(new File(indexPath3));
		    IndexWriter tableIndexWriter = new IndexWriter(directory3, config3);
			tableIndexer.setTableIndexWriter(tableIndexWriter);
		    
	    	boolean successfulIndexing = tableIndexer.writeTableToIndexes(new File(newFilePath));
			if (successfulIndexing != true){
				return internalServerError("For this table no KeyColumn can be automatically detected, please specify one.");
			}
				
	    	keyColumnIndexWriter.close();
	    	columnNameIndexWriter.close();
	    	tableIndexWriter.close();
			
			
		} catch (IOException e) {
			e.printStackTrace();
			return internalServerError("Error, the indexWriter for one of the 3 indexes couldn't be opened.");	
		} finally{
			tableIndexer.closeIndexes();
		}



		
    	
    	//  =========================  PART2: CORRESPONDENCE FINDING ==================================== 
    	System.out.println("Finding correspondences");
    	try {
	    	
	    	// open index
	    	Directory directory = FSDirectory.open(new File(indexFolderPath + "/KeyColumnIndex/"));    	
	
			
	        DirectoryReader indexReader = DirectoryReader.open(directory);
	        IndexSearcher indexSearcher = new IndexSearcher(indexReader);
	        
	        Query query = new WildcardQuery(new Term("tableHeader", tableName)); 
		    ScoreDoc[] indexEntries = indexSearcher.search(query, null, 1000).scoreDocs;
		    
		    if (indexEntries.length == 0){
		    	System.out.println("No entries found in the KeyColumnIndex for the table " + tableName);
		    	
		    } else {
		    	ScoreDoc indexEntry = indexEntries[0];
				Document doc = indexSearcher.doc(indexEntry.doc);
				
				//prepare the data from the uploaded table (the "new-" table)
				File newCSVFile = new File(newFilePath);
				
				CsvTableParser csvParser = new CsvTableParser();
				Table newTable = csvParser.parseTable(newCSVFile);
				
				WebTableDataSetLoader loader = new WebTableDataSetLoader();
				DataSet<MatchableTableRow, MatchableTableColumn> newDataset = loader.createTableDataSet(newTable);
				
			    // get the table-values
			    String originalTableHeader = doc.getFields("tableHeader")[0].stringValue();		
				String originalKeyColumnString = doc.getFields("keyColumnString")[0].stringValue();
				FindCorrespondences matchingFunctions = new FindCorrespondences();
				String queryString  = matchingFunctions.formatQueryString(originalKeyColumnString, 3);
				System.out.println("query string ===============================================================");
				System.out.println(queryString);
				
				if(queryString.trim().length()>0) {
					QueryParser queryParser = new QueryParser(Version.LUCENE_46, "keyColumnString", new StandardAnalyzer(Version.LUCENE_46));
					Query q = queryParser.parse(queryString);
					ScoreDoc[] hits = indexSearcher.search(q, 5).scoreDocs;
					
					for (ScoreDoc hit : hits){
						Document foundDoc = indexSearcher.doc(hit.doc);
						System.out.println("keyColumnIndexMatch  " + foundDoc.getFields("tableHeader")[0].stringValue() + " ---------------------------------");
						System.out.println(foundDoc.getFields("keyColumnString")[0].stringValue());
						if (! foundDoc.getFields("tableHeader")[0].stringValue().equals(originalTableHeader)){
							String foundTableName =  foundDoc.getFields("tableHeader")[0].stringValue();
							
							//prepare the data from the found table
							File foundCSVFile = new File("public/repositories/" + repositoryName + "/tables/" + foundTableName );
							Table foundTable = csvParser.parseTable(foundCSVFile);
							DataSet<MatchableTableRow, MatchableTableColumn> foundDataset = loader.createTableDataSet(foundTable);

							System.out.println("========================  " + originalTableHeader + " vs. " + foundTableName + "  ===================================");
							Processable<Correspondence<MatchableTableRow, MatchableTableColumn>> instanceCorrespondences = matchingFunctions.getInstanceMatches(newDataset, foundDataset, newCSVFile, foundCSVFile, correspondenceFolderPath);
							if (instanceCorrespondences.get().size()>0){
								matchingFunctions.getDuplicateBasedSchemaMatches(newDataset, foundDataset, newCSVFile, foundCSVFile, instanceCorrespondences, correspondenceFolderPath);	
							}
							
						}
				}
	
				}
		    }
				
		
		} catch (Exception e) {
			e.printStackTrace();
			return internalServerError("Something went wrong with the correspondence finding");
		}
	

    	String return_message = "Table " + tableName + " successfully uploaded to " + repositoryName + ".";
    	if (keyColumnIndex == null || keyColumnIndex < 0){
    		return_message += " As no keyColumnIndex was submitted, column " + String.valueOf(tableIndexer.getKeyColumnIndex()) + " was identified as keyColumn.";
    	} else{
    		return_message += " The submitted keycolumn " + String.valueOf(keyColumnIndex) + " was used.";
    	}
		return ok(return_message);

		
		
		
	}
	
	

	
	
	public Result suggestAttributes() {
		System.out.println("suggestAttributes----------");
		return ok(new File("public/exampleData/response.json"));
	}

	/**
	 * to retrieve the full content of each of the related table, it is possible
	 * to use the service http://ds4dm.informatik.uni-mannheim.de/fetchTable
	 * with a GET request, passing the tableName parameter. This is to avoid
	 * returning potentially big results in a single request, as we expect the
	 * number and size of tables to grow in future iterations.
	 * 
	 * @return
	 */
	// curl --header "Content-type: application/json" --request POST --data
	// '{"name": "11688006_0_8123036130090004213.json"}'
	// http://ds4dm.informatik.uni-mannheim.de/fetchTable

	// "dataTypes": {... },
	// "hasHeader": true,
	// "hasKeyColumn": true,
	// "headerRowIndex": 0,
	// "keyColumnIndex": 1,
	// "url": "http://www.worldatlas.com/aatlas/populations/ctypopls.htm",
	// "relation": [ ...]

	
	
	/**
	 * This method manages the {@link controllers.ExtendTable#bulkUploadTables(String, String, long)} on an high level.
	 * Amongst others, it ensures that the bulkUpload is executed as a separate process that doesn't block the webservice.<br>
	 * 
	 * Steps of execution:
	 * <ol>
	 * <li>check if the specified repository exists. If not: return "The repository " + repositoryName + " doesn't exist".
	 * <li>Create an uploadId by hashing the json in the body of the post-request 
	 * <li>run {@link controllers.ExtendTable#bulkUploadTables(String, String, long)} in a separate process
	 * <li>return the json {"status": "ACCEPTED", "message": <uploadId>}
	 * <ol>
	 * 
	 * @param repositoryName	the name of the repository to which the tables should be uploaded to
	 * @param request().body().asJson()	The request body should contain a list of tables to be uploaded. The format of this json-string is specified <a href="web.informatik.uni-mannheim.de/ds4dm/API-definition.html">here</a>
	 * @return 					the json: {"status": "ACCEPTED", "message": <uploadId>}
	 */	
	
	public Result moderateBulkUploadTables(final String repositoryName) throws IOException {
		
		long startTime = System.currentTimeMillis();
		
		System.out.println("moderateBulkUploadTables--------------------------------");
		
		
		// check if repositoryName was given and wether this repository exists
		if (repositoryName == null || repositoryName=="") {
			return internalServerError("No repository name given. Please pass a repository name with the url-paramenter 'repository'." );
		}
		
		
		File repository = new File("public/repositories/" + repositoryName);
		if (!repository.exists()) {
			return internalServerError("The repository " + repositoryName + " doesn't exist" );
		}
	

		final JsonNode newTableListJson = request().body().asJson();
		String requestBody = newTableListJson.toString();
		
		// hash the requestBody-string
	    String uploadIdentificationHash = null;
	    
	    try {   
		    byte[] requestBody_bytes = requestBody.getBytes("UTF-8");
		    uploadIdentificationHash = DigestUtils.sha1Hex(requestBody_bytes);
	    } catch (Exception e) { e.printStackTrace();}
	    final String uploadId = uploadIdentificationHash;
	     
	   
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("timestep0: " + String.valueOf(System.currentTimeMillis() -  startTime));
				try{ 
					// The main line !!!!
					 
					bulkUploadTables(repositoryName, uploadId, startTime, requestBody);
				} catch (Exception e) {e.printStackTrace();}
				System.out.println("timestep6: " + String.valueOf(System.currentTimeMillis() -  startTime));
			}
		});
		thread.start();
		System.out.println("timestep7: " + String.valueOf(System.currentTimeMillis() -  startTime));
		
		
		ObjectNode result = Json.newObject();
		result.put("status", "ACCEPTED");
		result.put("message",  uploadId);
		System.out.println("timestep8: " + String.valueOf(System.currentTimeMillis() -  startTime));
	    return ok(result);

	}
	


	
	/**
	 * bulkUploadTables<br>
	 * For uploading many tables to a repository, the bulk upload has more than an order of magnitude better performance - see <a href="http://web.informatik.uni-mannheim.de/ds4dm/#evaluation">evaluation</a>.
	 * This method is not directly called by the Webservice API, it gets called by {@link controllers.ExtendTable#moderateBulkUploadTables(String)}.
	 * <br>
	 * Steps of execution:
	 * <ol>
	 * <li>convert request body to ListOfTables-object
	 * <li>save the tables in ListOfTables as csv files
	 * <li>save the tables in the indexes by running {@link de.uni_mannheim.informatik.dws.ds4dm.CreateLuceneIndex.Main#main(String[])}
	 * <li>find the correspondences between the tables and the already indexed tables by running {@link de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences.Main#main(String[])}
	 * <li>return message: "UPLOAD SUCCESSFUL\n"
	 * <ol>
	 * 
	 * @param repositoryName	the name of the repository to which the tables are being uploaded
	 * @param uploadId	The uploadId is a code that can be used by {@link controllers.ExtendTable#getUploadStatus(String, String)} to check the status of this bulkUpload while (and after) it's being executed.
	 * @param startTime	the time when the execution of {@link controllers.ExtendTable#moderateBulkUploadTables(String)} began. This is only for evaluation purposes.
	 * @param requestBody	The requestBody should contain a list of tables to be uploaded. The format of this json-string is specified <a href="web.informatik.uni-mannheim.de/ds4dm/API-definition.html">here</a>
	 * @return 					"UPLOAD SUCCESSFUL\n"
	 */	
	
	
	public void bulkUploadTables(String repositoryName, String uploadId, long startTime, String requestBody) throws IOException {
		
		try{
			FileOutputStream fileOutputStream = new FileOutputStream(new File("/home/bkleppma/ds4dm_webservice/DS4DM/DS4DM_webservice/public/exampleData/test2.txt"));
			System.setOut(new PrintStream(fileOutputStream));
		}catch (FileNotFoundException e){e.printStackTrace();}
		
		System.out.println("Bulk Upload of Tables ---------------------------");
		
		
		String indexFolderPath = "public/repositories/" + repositoryName + "/indexes";
		String correspondenceFolderPath = "public/repositories/" + repositoryName + "/correspondences";
		String datafilePath = "public/repositories/" + repositoryName + "/tables";
//		String datafilePath = "/home/bkleppma/Temporary_folders/2018-03-30/tables";
		String logfilePath = "public/repositories/" + repositoryName + "/bulkUploadLogs";
		String blockSize = "5";
		String return_message = "";
		System.out.println("timestep1: " + String.valueOf(System.currentTimeMillis() -  startTime));

		
		//  =========================  SAVING TO LOG FILE  ==================================== 
		FileUtils.writeStringToFile(new File(logfilePath + "/" + uploadId + ".log"), "PROCESSING");
		System.out.println("timestep2: " + String.valueOf(System.currentTimeMillis() -  startTime));
	    
		
		//  =========================  PART0: SAVING TABLES AS CSV FILES ==================================== 
		Gson gson = new Gson();
		
		
		uploadTable.ListOfTables listOfTables = gson.fromJson(requestBody, uploadTable.ListOfTables.class);
		System.out.println(String.valueOf(listOfTables.getListOfTables().size()) + " table to upload.");
		for (uploadTable.Table table: listOfTables.getListOfTables()){
			table.saveToFile("public/repositories/" + repositoryName + "/tables/" + table.getTablename());
		}
		
		//  =========================  PART1: INDEXING ==================================== 
	     
	     String keyColumnsCSVPath = "public/repositories/" + repositoryName + "/keyColumnsCSV.csv";
	     
  
	     
		de.uni_mannheim.informatik.dws.ds4dm.CreateLuceneIndex.Main luceneIndexCreator = new de.uni_mannheim.informatik.dws.ds4dm.CreateLuceneIndex.Main();
		String[] indexingArguments = {datafilePath, indexFolderPath, keyColumnsCSVPath};
		
		System.out.println("luceneIndexCreator.main(" + String.join(",", indexingArguments) + ")");
		try{
			luceneIndexCreator.main(indexingArguments);
		} catch (IOException e){
			e.printStackTrace();
			return_message += "An IOException occured during the Index Creation.\n";
		}
		

	     
    	//  =========================  PART2: CORRESPONDENCE FINDING ==================================== 
	     
		de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences.Main correspondenceFileCreator = new de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences.Main();
    	String[] correspondenceArguments = {indexFolderPath, datafilePath, correspondenceFolderPath, blockSize};
	     
		try{         
	    	correspondenceFileCreator.main(correspondenceArguments);    	
		} catch (Exception e){
			e.printStackTrace();
			return_message += "An Exception occured during the CorrespondenceFile Creation.\n";
		}
    	

		
		if (return_message.length() > 10000){
			FileUtils.writeStringToFile(new File(logfilePath + "/" + uploadId + ".log"), "UPLOAD UNSUCCESSFUL\n");
		}
		else{
			FileUtils.writeStringToFile(new File(logfilePath + "/" + uploadId + ".log"), "UPLOAD SUCCESSFUL\n");
		}

	
		System.out.println("timestep5: " + String.valueOf(System.currentTimeMillis() -  startTime));
	}
	

	
	
	/**
	 * This function returns the status of the {@link controllers.ExtendTable#moderateBulkUploadTables(String)}-job with the specified uploadID. When a {@link controllers.ExtendTable#moderateBulkUploadTables(String)}-job is started, it returns its uploadID in the return-message.
	 * During the acctual bulkupload (which is running in a seperate process), the status of the bulkUpload is written to a file. The status may be "PROCESSING", "UPLOAD SUCCESSFUL\n" or "UPLOAD UNSUCCESSFUL\n".
	 * The getUploadStatus reads the status from the file and returns it to the user
	 * <br>
	 * 
	 * @param repositoryName	the name of the repository to which the tables are being uploaded
	 * @param uploadId	The uploadId is the unique identifier of a uploadProcess. It is returned by {@link controllers.ExtendTable#moderateBulkUploadTables(String)}
	 * @return 					"UPLOAD SUCCESSFUL\n"
	 */	
		
	
	public Result getUploadStatus(String repositoryName, String uploadID) {
		String logContent = "";
		
		try { 
			String logfilePath = "public/repositories/" + repositoryName + "/bulkUploadLogs/" + uploadID + ".log";
			logContent = new Scanner(new File(logfilePath)).useDelimiter("\\Z").next();
		} catch (Exception e) {e.printStackTrace();}
		
		return ok(logContent);
	}
	
	
	
	
	
	
	
	/**
	 * This function is not part of the standard Backend API.  
	 * It is only used on the rare occasion, that for a bulk-upload the Index-creation has already been done, but no correspondences have been created yet.
	 */	
		
	
	public Result generateCorrespondences(final String repositoryName) throws IOException {
		final JsonNode newTableListJson = request().body().asJson();
		String requestBody = newTableListJson.toString();
		
		String indexFolderPath = "public/repositories/" + repositoryName + "/indexes";
		String correspondenceFolderPath = "public/repositories/" + repositoryName + "/correspondences";
		String datafilePath = "/home/bkleppma/Temporary_folders/2018-03-30/tables";
		String logfilePath = "public/repositories/" + repositoryName + "/bulkUploadLogs";
		String blockSize = "5";
		String return_message = "";
		
		de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences.Main correspondenceFileCreator = new de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences.Main();
		String[] correspondenceArguments = {indexFolderPath, datafilePath, correspondenceFolderPath, blockSize};
	     
		try{         
	    	correspondenceFileCreator.main(correspondenceArguments);    	
		} catch (Exception e){
			e.printStackTrace();
			return_message += "An Exception occured during the CorrespondenceFile Creation.\n";
		}
		return ok(return_message);	
	}
	
	
	/**
	 * This function is not part of the standard Backend API.  
	 * It is only used on the rare occasion, that for a bulk-upload the Index-creation and the blocking has already been done, but no correspondences have been created yet.
	 */	
	
	public Result generateCorrespondences_withKnownBlocking(final String repositoryName, final String blockingsFileName) throws IOException {
		
		System.out.println("generateCorrespondences_withKnownBlocking?repositoryName=" +repositoryName+ "&blockingsFileName="+ blockingsFileName);
		final JsonNode newTableListJson = request().body().asJson();
		String requestBody = newTableListJson.toString();
		
		String indexFolderPath = "public/repositories/" + repositoryName + "/indexes";
		String datafilePath = "/home/bkleppma/Temporary_folders/2018-03-30/tables";
		String correspondenceFolderPath = "public/repositories/" + repositoryName + "/correspondences";
		String logfilePath = "public/repositories/" + repositoryName + "/bulkUploadLogs";
		String blockSize = "5";
		
		String return_message = "";
		
		de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences.Main_withKnownBlocking correspondenceFileCreator = new de.uni_mannheim.informatik.dws.ds4dm.CreateCorrespondences.Main_withKnownBlocking();
		String[] correspondenceArguments = {indexFolderPath, datafilePath, correspondenceFolderPath, blockSize, blockingsFileName};
	     
		try{   
			System.out.println("creating correspondences...");
	    	correspondenceFileCreator.main(correspondenceArguments);
	    	System.out.println("finished creating correspondences.");
		} catch (Exception e){
			e.printStackTrace();
			return_message += "An Exception occured during the CorrespondenceFile Creation.\n";
		}
		
		System.out.println("Returning message: " + return_message);
		return ok(return_message);	
	}
	
	
	
	
	/**
	 * The fetchTablePOST function is rarely used in practice. 
	 * The DS4DM-Frontend uses the {@link controllers.ExtendTable#fetchTable(String, String)}-GET-method instead.
	 * The GET- and the POST- method work in exactly the same way. More info here:  {@link controllers.ExtendTable#fetchTable(String, String)}
	 */	
	
	public Result fetchTablePOST(String repositoryName) {

		File tableIndex = new File("public/repositories/" + repositoryName + "/fetchedTables");
		File[] t = tableIndex.listFiles();
		List<String> tables = new ArrayList<String>();
		for (File f : t)
			tables.add(f.getName());

		JsonNode json = request().body().asJson();
		if (json == null) {
			return badRequest("Expecting Json data");
		} else {
			String name = json.findPath("name").textValue();

			if (name != null) {
				if (!name.endsWith(".json"))
					name = name + ".json";

				if (tables.contains(name)) {
					return ok(new File("public/repositories/" + repositoryName + "/fetchedTables/" + name));

				} else {
					// return ok(new File("public/repositories/" + repositoryName + "/fetchedTables/"+name));

					return notFound("Table " + name + " not found");
				}

			} else {

				return badRequest("Expecting json request, with table id specified by the attribute name");
			}
		}
	}


	
	
	/**
	 * If the DS4DM Frontend does not specify the repositoryName, then this function can be used instead of {@link controllers.ExtendTable#fetchTable(String, String)}
	 * It calls {@link controllers.ExtendTable#fetchTable(String, String)} with the repositoryName set to "T2D_Goldstandard".
	 */	
	public Result fetchTable_T2DGoldstandard(String name) {
		String repositoryName = "T2D_Goldstandard";
		Result result = fetchTable(name, repositoryName);
		return result;
	}
	
	
	/**
	 * If the DS4DM Frontend does not specify the repositoryName, then this function can be used instead of {@link controllers.ExtendTable#fetchTable(String, String)}
	 * It calls {@link controllers.ExtendTable#fetchTable(String, String)} with the repositoryName set to "Produktdata".
	 */	
	public Result fetchTable_Produktdata(String name) {
		String repositoryName = "Produktdata";
		Result result = fetchTable(name, repositoryName);
		return result;
	}
	
	
	/**
	 * If the DS4DM Frontend does not specify the repositoryName, then this function can be used instead of {@link controllers.ExtendTable#fetchTable(String, String)}
	 * It calls {@link controllers.ExtendTable#fetchTable(String, String)} with the repositoryName set to "WebWikiTables".
	 */	
	public Result fetchTable_WebWikiTables(String name) {
		String repositoryName = "WebWikiTables";
		Result result = fetchTable(name, repositoryName);
		return result;
	}
	
	
	
	/**
	 * 
	 * 
	 * The {@link extendedSearch2.ExtenededSearch#extendedSearch(String)}-methods searches for tables in the repository that contain useful data for extending a table with an additional column. 
	 * It returns the name of the found tables as well as it's correspondences to the query table (which are necessary for constructing the additional column). It however does not return the actual data from the found tables, as the http-response-messages would be too big.<br>
	 * The actual data from the found tables have to be requested seperately  - with the <b>fetchTable-method</b>. 
	 * 
	 * This is done as follows:
	 * at the end of the {@link extendedSearch2.ExtenededSearch#extendedSearch(String)}-method, the found tables are saved to json-files (using {@link extendedSearch2.ExtenededSearch#saveTableDataForFetching(model.ExtendedTableInformation, extendedSearch2.GlobalVariables)}).
	 * The fetchTable-method just opens the requested json-file and returns its content.
	 */	
	
	public Result fetchTable(String name, String repositoryName) {

		File tableIndex = new File("public/repositories/" + repositoryName + "/fetchedTables");
		File[] t = tableIndex.listFiles();
		List<String> tables = new ArrayList<String>();
		for (File f : t)
			tables.add(f.getName());

		if (name != null) {
			// if (!name.endsWith(".json"))
			// name = name+".json";

			if (tables.contains(name)) {
				return ok(new File("public/repositories/" + repositoryName + "/fetchedTables/" + name));

			} else {
				// return ok(new File("public/repositories/" + repositoryName + "/fetchedTables/"+name));

				return notFound("Table " + name + " not found");
			}

		} else {

			return badRequest("Expecting json request, with table id specified by the attribute name");
		}

	}

	public Result ind() {
		return ok(index.render("Your new application is ready."));
	}

	
	
	
	
	
	
	

	
	
	
}



