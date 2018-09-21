package de.mannheim.uni.ds4dm.extendedSearch;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonSyntaxException;
import com.rapidminer.extension.json.JSONRelatedTablesRequest;
import com.rapidminer.extension.json.JSONRelatedTablesResponse;
import de.mannheim.uni.ds4dm.extendedSearch.DS4DMBasicMatcherX;

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
