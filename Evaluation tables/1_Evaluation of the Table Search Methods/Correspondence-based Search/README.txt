=======================================================
Evaluation of the Correspondence-based Search
=======================================================

Each sub-folder in this folder corresponds to a different table extension that was performed using the Correspondence-based Search.
E.g. The sub-folder "City-Mayor" corresponds to the task of extending a table with a column of City names by the additional column "mayor" (the names of the mayors of the given cities).

Each of these sub-folders contains two files:

* 	query_table.csv
A tab-seperated datafile containing the correct solution for the table extension task. This correct solution was generated from the data in the T2D Goldstandard (more info here: http://web.informatik.uni-mannheim.de/ds4dm/#evaluation).
For the "City-Mayor" sub-folder this corresponds to a table with the two columns "city" and "mayor". 
For the evaluation the table was extended (using the Correspondence-based Search) with the an additional mayor column (Note that the table extension does not use the original mayor-column in any way). Then the newly generated mayor-column was compared with the original mayor-column and precision- and recall- scores were calculated.
	
* 	fused_table.csv
This semicolon-seperated data file contains the result of the table extension. 
Unlike the table extension results obtained by the DataSearch-Operator from the DS4DM-RapidMiner-Extension, the un-fused results columns are kept in the table (this allows for a more exact analysis of the table extension). 
In concrete this means: the left two columns "city" and "mayor" are the two columns from the original table (query_table.csv). 
The columns "newCol0", "newCol1", "newCol2", etc. are the mayor-columns from the different tables found by the Correspondence-based Search. 
The column "newCol" is the result obtained by fusing the columns "newCol0", "newCol1", etc. - this is the actual extension column. In the DataSearch-Operator this column would not be called "newCol", but "mayor".
As mentioned above, the the precision- and recall- scores were calculated by comparing the extension column "newCol" with the correct reference-column "mayor".

