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
 package extendedSearch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonSyntaxException;
import com.rapidminer.extension.json.JSONRelatedTablesRequest;
import com.rapidminer.extension.json.JSONRelatedTablesResponse;
import extendedSearch.DS4DMBasicMatcherX;

import de.mannheim.uni.ds4dm.utils.ReadWriteGson;

public class MakeMatcherX {
	
	public static DS4DMBasicMatcherX makeMatcherX(File request){
		
			DS4DMBasicMatcherX matcher = null;
			
			try {
					JSONRelatedTablesRequest qts = new JSONRelatedTablesRequest();
					ReadWriteGson<JSONRelatedTablesRequest> rwg = new ReadWriteGson<JSONRelatedTablesRequest>(qts);
					qts = rwg.fromJson(request);
					matcher = new DS4DMBasicMatcherX(qts);
				} catch (JsonSyntaxException | IOException e) {
					e.printStackTrace();
				}

			
			return matcher;
			
	}
	
	public static JSONRelatedTablesResponse constructResponseObject(DS4DMBasicMatcherX matcher) {
		// construct response object
		// ***********************
		JSONRelatedTablesResponse responseMappimg = new JSONRelatedTablesResponse();
		responseMappimg.setTargetSchema(matcher.getTargetSchema());
		responseMappimg.setExtensionAttributes2TargetSchema(matcher.getE2t_str());
		responseMappimg.setQueryTable2TargetSchema(matcher.getQ2t_str());
		Map<String, String> dataTypes = new HashMap<String, String>();
		for (int i = 0; i < matcher.getTargetSchema().size(); i++) {
			dataTypes.put(matcher.getTargetSchema().get(i), matcher.getTargetSchemaDataTypes().get(i).toString());
		}
		responseMappimg.setDataTypes(dataTypes);
		return responseMappimg;
	}



}
