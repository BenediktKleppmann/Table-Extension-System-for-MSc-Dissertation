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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapidminer.extension.json.JSONRelatedTablesRequest;

import de.mannheim.uni.ds4dm.demo1.exploreData.GenerateMatchingExample_withKeywords;
import de.mannheim.uni.ds4dm.searcher.CandidateBuilder_fromLuceneIndex;
import de.mannheim.uni.ds4dm.utils.ReadWriteGson;
import play.mvc.Controller;
import play.*;
import play.mvc.*;

import views.html.*;

public class Config {
	

	static String conf = "testConf.conf";


	
		
}
