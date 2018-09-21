=======================================================
Evaluation of the Blocking Step
=======================================================

The CorrespondenceCreator employs a blocking step to reduce the number of table comparisons that are needed for identifying correspondences. 
For each table we need to calculate the correspondences to all other tables. Instead of calculating these correspondences, the blocking technique uses a tf-idf-like score to calculate the similarities between the table and all other tables in the repository. 
Correspondences are then only calculated for tables the five most similar tables (if there are not enough tables are above a certain similarity threshold, it might be less than five).

In the comma-seperated data file "blockedTableCombinations.csv", you find the 431 blockings that were found for the T2D Goldstandard (more info here: http://web.informatik.uni-mannheim.de/ds4dm/#evaluation).
=> for the T2D Goldstandard, the CorrespondenceCreator will only have to check 431 table-combinations for correspondences, instead of 53 824 (a reduction ratio of 0.992).

The comma-seperated data file "subject_column_group_table_names.csv" contains groups of tables in the T2D Goldstandard that truly correspond to each other (these true correspondences had been manually determined).
By comparing the these two files, we find that ca. 70% of true correspondences were contained in the list of blockings. This corresponds to a pair completeness of 0.701.
More information on this can be found here: http://web.informatik.uni-mannheim.de/ds4dm/#evaluation.
