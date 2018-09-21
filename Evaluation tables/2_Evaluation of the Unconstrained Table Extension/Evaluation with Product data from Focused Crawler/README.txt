====================================================================================================================
Evaluation of the Unconstrained Table Extension with the Product data, which was scraped by the Focussed Crawler
====================================================================================================================

The tab "1 - Unconstrained Search" of the spreadsheet Evaluation.xlsx shows the calculation of the evaluation scores.
You will also find the scores and a high-level description of the evaluation on http://web.informatik.uni-mannheim.de/ds4dm/#evaluation.


The results of the evaluation can be both found in the subfolders ("headphones", "phones" and "tvs") is the actual data which was used for the evaluation.
Each of these folders contains the following files:
*  query_table.csv
A csv-file containing one column: the normalized_product_name of the product in question (= either the names of headphones, or of phones, or of tvs).
This file was then extended by the Unconstrained Search. The result of this extension is...

*  result.csv
A comma-seperated csv-file. With the extended query table - it contains the original column normalized_product_name, plus additional ones that were added by the Unconstrained Search.

*  solution.csv
A comma-seperated csv-file. The column normalized_product_name contains the same product names as query_table.csv and result.csv. The other columns contain lists of values. 
The values in each list are the values from the source-tables that have this specific entity+attribute. You will notice that some lists are empty; in this case there was no entry in any of the source tables for this entry+attribute.
For the evaluation, a value in result.csv is counted as correct if it corresponds to any one of the values in the corresponding list in solution.csv.
