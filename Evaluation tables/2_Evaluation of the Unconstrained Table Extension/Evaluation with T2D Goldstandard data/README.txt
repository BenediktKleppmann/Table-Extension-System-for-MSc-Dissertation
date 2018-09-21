==================================================================================
Evaluation of the Unconstrained Table Extension with T2D Goldstandard data
==================================================================================

Each sub-folder in this folder corresponds to a different table extension-query that was performed using the Unconstrained Table Extension.

E.g. The sub-folder "10_mountains" corresponds to the task of extending a table that has one column with the names of mountains with as many as possible other columns (containing attributes of the given mountains).

Each of these sub-folders contains two files:

* 	result_table.csv
This tab-separated data file is the result of the Unconstrained Table Extension. 
One of the columns (in our example: "gipfel") is the original column that was fed into the Unconstrained Table Extension. 
The other columns ("asia", "durchschnitt", "entfernung", etc.) are the columns that were added by the Unconstrained Table Extension. 
	
* 	solution_table.csv
This tab-separated data file contains the ideal solution for the Unconstrained Table Extension (the result that would be obtained if the Unconstrained Table Extension would work perfectly). It was created from the data in the T2D Goldstandard (more info here: http://web.informatik.uni-mannheim.de/ds4dm/#evaluation).
The left-most column is the original column that was fed into the Unconstrained Table Extension (in our example: names of mountains). The other columns contain attributes of these mountains.
The column names have no significance, they are a relict of the algorithm used for generating the attribute columns.
You will notice, that many fields contain a list of values (e.g. "[2901, 2900]" ) instead of a single value. 
For the left-most column this is of no importance, but for the other columns (the attribute-columns), this is important, because the values in the list are the correct values from different tables (for this field). 
If the Unconstrained Table Extension finds any one of these values (except for the null-values), then the result is correct.
	