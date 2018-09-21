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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rapidminer.extension.json.Correspondence;
import com.rapidminer.extension.json.JSONTableResponse;
import com.rapidminer.extension.json.MetaDataTable;

import de.mannheim.uni.ds4dm.utils.ReadWriteGson;
import de.mannheim.uni.types.ColumnTypeGuesser.ColumnDataType;
import de.mannheim.uni.utils.TableColumnTypeGuesser;
import extendedSearch.DS4DMBasicMatcherX;
import extendedSearch.GetTableInformationX.DataSource;

public class MetaDataHandler {
	public static void saveTableDataForFetching(Map<String, Correspondence> instancesCorrespondences2QueryTable, DS4DMBasicMatcherX matcher, String[][] relation, String tableName, String[] columnHeaders, String[] keyColumn, Integer keyColumnIndex, double tableScore, DataSource dataSource, String repositoryName) throws IOException 
	{
		Date lastModified = new Date(System.currentTimeMillis());
		
		
		String textBeforeTable = "";
		String textAfterTable = "";
		
		String title = "";
		double coverage = (double) instancesCorrespondences2QueryTable.size()/ matcher.getSubjectsFromQueryTable().size();
		double ratio = (double) instancesCorrespondences2QueryTable.size() / keyColumn.length;
		double trust = 1; 
		double emptyValues = 0;
		
		String matchingType = "";
		if (dataSource==DataSource.DIREKTMATCH) matchingType = "direktMatch";
		else matchingType = "indirektMatch";
		
		MetaDataTable meta = new MetaDataTable(tableScore, 
											   lastModified.toString(),
											   matchingType, //table.getValue().getUrl()
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
		t_sab.setTableName(tableName);
		
		//t_sab.setKeyColumnIndex			
		String indexCol = Integer.toString(keyColumnIndex) + "_" + keyColumn[0];
		t_sab.setKeyColumnIndex(indexCol);
		
		
		//t_sab.setRelation			
		List<List<String>> new_relation = new ArrayList<List<String>>();

		for(int columnNumber =0; columnNumber < relation.length; columnNumber++){
			
			List<String> new_column = Arrays.asList(relation[columnNumber]);
			String indexedHeader = columnNumber + "_" + new_column.get(0);
			new_column.set(0, indexedHeader);
			
			new_relation.add(new_column);
		}
		t_sab.setRelation(new_relation);
		
		
		//t_sab.setDataTypes
		Map<String, String> dataTypes = guessTypes(new_relation);
		t_sab.setDataTypes(dataTypes);

		
		//t_sab.setMetaData
		meta.setURL(""); // table.getValue().getUrl()
		t_sab.setMetaData(meta);
		

//		-------------------------------------------------------------------------------------------------------------------
		ReadWriteGson<JSONTableResponse> resp = new ReadWriteGson<JSONTableResponse>(t_sab);
		
		File fetchedTablesFolder = new File("public/repositories/" + repositoryName + "/fetchedTables");
		if (!fetchedTablesFolder.exists())
			fetchedTablesFolder.mkdirs();
		File current_table = new File(fetchedTablesFolder.getAbsolutePath() + "/" + tableName);
		resp.writeJson(current_table);
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
