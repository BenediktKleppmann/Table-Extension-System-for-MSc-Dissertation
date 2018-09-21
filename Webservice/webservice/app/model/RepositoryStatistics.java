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

public class RepositoryStatistics {
	
	private String created_timestamp;
	private String creator_ip;
	
	public void setCreatedTimestamp(String created_timestamp){
		this.created_timestamp = created_timestamp;
	}
	
	public String getCreatedTimestamp(){
		return this.created_timestamp;
	}
	
	public void setCreatorIP(String creator_ip){
		this.creator_ip = creator_ip;
	}
	
	public String getCreatorIP(){
		return this.creator_ip;
	}

}