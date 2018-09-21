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
 package model;


import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import com.rapidminer.extension.json.Correspondence;
import com.rapidminer.extension.json.TableInformation;


public class ExtendedTableInformation {
	
	private String tableName;
	private Map<String,Correspondence> tableSchema2TargetSchema;
	private Map<String,Correspondence> instancesCorrespondences2QueryTable;
	private Double schemaSimilarityScore;
	private String extensionAttributePosition;
	
	
	private String[][] relation;
	private Integer keyColumnIndex;
	private String[] columnHeaders;
	
	private LinkedList<String> correspondingTables;
	private String matchingType;
	
	//=========================================================================
	// get and set methods
	//=========================================================================
	
	// Constructor ===========================================
	public ExtendedTableInformation(){
		// some initialisation...
		this.tableName = new String();
		this.tableSchema2TargetSchema = new HashMap<String,Correspondence>();
		this.instancesCorrespondences2QueryTable = new HashMap<String,Correspondence>();
		this.schemaSimilarityScore = 0.0;
		this.extensionAttributePosition = new String();
		this.keyColumnIndex = 0;
		this.correspondingTables = new LinkedList<String>();
		this.matchingType = new String();

		
	}

	
	
	
	// Setters and Getters ===========================================
	
	
	//tableName --------------------------------------------
	public String getTableName(){
		return this.tableName;
	}
	
	public void setTableName(String tableName){
		this.tableName = tableName;
	}
	
	
	//tableSchema2TargetSchema --------------------------------------------
	public Map<String,Correspondence> getTableSchema2TargetSchema(){
		return this.tableSchema2TargetSchema;
	}
	
	public void setTableSchema2TargetSchema(Map<String,Correspondence> tableSchema2TargetSchema){
		this.tableSchema2TargetSchema = tableSchema2TargetSchema;
	}
	
	
	//instancesCorrespondences2QueryTable --------------------------------------------
	public Map<String,Correspondence> getInstancesCorrespondences2QueryTable(){
		return this.instancesCorrespondences2QueryTable;
	}
	
	public void putNewInstancesCorrespondences2QueryTable(String foundTableIndex ,Correspondence correspondence){
		this.instancesCorrespondences2QueryTable.put(foundTableIndex, correspondence);
	}
	
	public void setInstancesCorrespondences2QueryTable(Map<String,Correspondence> instancesCorrespondences2QueryTable){
		this.instancesCorrespondences2QueryTable = instancesCorrespondences2QueryTable;
	}
	
	//schemaSimilarityScore --------------------------------------------
	public Double getSchemaSimilarityScore(){
		return this.schemaSimilarityScore;
	}
	
	public void setSchemaSimilarityScore(Double schemaSimilarityScore){
		this.schemaSimilarityScore = schemaSimilarityScore;
	}
	
	//extensionAttributePosition --------------------------------------------
	public String getExtensionAttributePosition(){
		return this.extensionAttributePosition;
	}
	
	public void setExtensionAttributePosition(String extensionAttributePosition){
		if (Integer.valueOf(extensionAttributePosition)>=0)
			this.extensionAttributePosition = extensionAttributePosition;
	}
	
	//relation --------------------------------------------
	public String[][] getRelation(){
		return this.relation;
	}
	
	public void setRelation(String[][] relation){
		this.relation = relation;
	}	
	
	
	//keyColumnIndex --------------------------------------------
	public Integer getKeyColumnIndex(){
		return this.keyColumnIndex;
	}
	
	public void setKeyColumnIndex(Integer keyColumnIndex){
		this.keyColumnIndex = keyColumnIndex;
	}
	
	

	
	//correspondingTables --------------------------------------------
	public LinkedList<String> getCorrespondingTables(){
		return this.correspondingTables;
	}
	
	public void addToCorrespondingTables(String correspondingTable){
		if (this.correspondingTables==null) this.correspondingTables = new LinkedList<String>();
		this.correspondingTables.add(correspondingTable);
	}
	
	public void setCorrespondingTables(LinkedList<String> correspondingTables){
		this.correspondingTables = correspondingTables;
	}
	
	

	//matchingType --------------------------------------------
	public String getMatchingType(){
		return this.matchingType;
	}
	
	public void setMatchingType(String matchingType){
		this.matchingType = matchingType;
	}
	
	
	
	
	// Additional Methods===========================================
	
	public String[] getColumnHeaders(){
       String[] headers = null;
       headers = new String[relation.length];
   
       for (int col = 0; col < relation.length; col++) {
           headers[col] = relation[col][0];
       }
       return headers;
	}
	
	//----------------------------------------------------------
	public String[] getKeyColumn(){
		return this.relation[keyColumnIndex];
	}
	
	//----------------------------------------------------------
	// downsize to the smaller TableInformation-object
	public TableInformation getTableInformation(){
		TableInformation myTable = new TableInformation();
		myTable.setTableName(this.getTableName());
		myTable.setTableSchema2TargetSchema(this.getTableSchema2TargetSchema());
		myTable.setInstancesCorrespondences2QueryTable(this.getInstancesCorrespondences2QueryTable());		
		return myTable;
	}
	
}
