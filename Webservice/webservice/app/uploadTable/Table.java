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
 package uploadTable;

import java.io.FileWriter;
import java.io.IOException;
import java.util.UUID;

import au.com.bytecode.opencsv.CSVWriter;

public class Table {
	private String[][] relation;
	private Integer keyColumnIndex;
	private String tablename;
	
	
	public void setRelation(String[][] relation){
		this.relation = relation;
	}
	public String[][] getRelation(){
		return this.relation;
	}
	
	
	public void setKeyColumnIndex(int keyColumnIndex){
		this.keyColumnIndex = keyColumnIndex;
	}
	
	public Integer getKeyColumnIndex(){
		return this.keyColumnIndex;
	}
	
	
	public void setTablename(String tablename){
		this.tablename = tablename;
	}
	public String getTablename(){
		if (this.tablename==null){
			String newTableName = UUID.randomUUID().toString();
			this.tablename = newTableName + ".csv";
		}
		return this.tablename;
	}
	
	
	public void saveToFile(String filepath) throws IOException{
		
	    CSVWriter csvwriter = new CSVWriter(new FileWriter(filepath), ',');
	    
	    String[][] transposedRelation = transposeRelation(relation);
	    
	    for (String[] row:  transposedRelation){
	    	csvwriter.writeNext(row);
	    	
	    }
	    csvwriter.close();
		}
	
	
	public String[][] transposeRelation(String[][] relation){
	    int m = relation.length;
	    int n = relation[0].length;

	    String[][] trasposedRelation = new String[n][m];

	    for(int x = 0; x < n; x++)
	    {
	        for(int y = 0; y < m; y++)
	        {
	        	trasposedRelation[x][y] = relation[y][x];
	        }
	    }
	    return trasposedRelation;
	}
	
}
