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
import java.io.FileNotFoundException;
import  java.util.Scanner;

import com.google.gson.Gson;

import model.QueryTable;

public class GlobalVariables {
	
	//========  PARAMETERS  =====================================================
	private String repositoryName;
	
	private String columnNameIndexPath;
	
	private model.QueryTable queryTable;
	
	private Boolean logFoundTables;
	
	private String logFolderPath;
	
	private String tablesFolderPath;
	
	

	
	
	//========  METHODS  ========================================================

	// constructor ---------------------------------
	public GlobalVariables(String globalVariablesFilePath) {
		
		String globalVariablesString = "{}";
		try {
			globalVariablesString = new Scanner(new File(globalVariablesFilePath)).useDelimiter("\\Z").next();
		} catch (FileNotFoundException e) {e.printStackTrace();}
		
		
		Gson gson = new Gson();	
		GlobalVariables temporaryObject = gson.fromJson(globalVariablesString, GlobalVariables.class);
		
		

		this.queryTable = temporaryObject.getQueryTable();
		this.logFoundTables = temporaryObject.getLogFoundTables();
		this.logFolderPath = temporaryObject.getLogFolderPath();
		
		
		final String theRepositoryName =  temporaryObject.getRepositoryName();
		
		if (temporaryObject.getRepositoryName()!= null) {
			this.repositoryName = temporaryObject.getRepositoryName();
			this.tablesFolderPath = "public/repositories/" + temporaryObject.getRepositoryName() + "/tables/";
		}
		else  this.repositoryName = "DefaultRepository";
		
		if (temporaryObject.getColumnNameIndexPath() != null) this.columnNameIndexPath = temporaryObject.getColumnNameIndexPath();
		else this.columnNameIndexPath = "public/repositories/" + temporaryObject.getRepositoryName() + "/indexes/ColumnNameIndex";

	}



	
	
	// repositoryName -------------------------------
	public String getRepositoryName(){
		return this.repositoryName;
	}
	
	public void setRepositoryName(String repositoryName){
		this.repositoryName = repositoryName;
		this.tablesFolderPath = "public/repositories/" + repositoryName + "/tables/";
		
		if (this.columnNameIndexPath.equals("public/repositories/DefaultRepository/indexes/ColumnNameIndex"))
			this.columnNameIndexPath = "public/repositories/" + repositoryName + "/indexes/ColumnNameIndex";
	}
	
	

	// columnNameIndexPath -------------------------------
	public String getColumnNameIndexPath(){
		return this.columnNameIndexPath;
	}
	
	public void setColumnNameIndexPath(String columnNameIndexPath){
		this.columnNameIndexPath = columnNameIndexPath;
	}

	
	// queryTable -------------------------------
	public QueryTable getQueryTable(){
		return this.queryTable;
	}
	
	public void setQueryTable(QueryTable queryTable) {
		this.queryTable = queryTable;
	}
	
	
	// logFoundTables -------------------------------
	public Boolean getLogFoundTables(){
		return this.logFoundTables;
	}
	
	public void setLogFoundTables(Boolean logFoundTables) {
		this.logFoundTables = logFoundTables;
	}

	// logFolderPath -------------------------------
	public String getLogFolderPath(){
		if (this.logFolderPath == null) this.logFoundTables = false;
		return this.logFolderPath;
	}
	
	public void setLogFolderPath(String logFolderPath){
		this.logFolderPath = logFolderPath;
	}
	
	// tablesFolderPath -------------------------------
	public String getTablesFolderPath(){
		return this.tablesFolderPath;
	}
	
	public void setTablesFolderPath(String tablesFolderPath){
		this.tablesFolderPath = tablesFolderPath;
	}
	
	

}
