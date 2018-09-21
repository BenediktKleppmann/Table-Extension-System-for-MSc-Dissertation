=======================================================
Evaluation of the Correspondence Discovery
=======================================================

When tables are uploaded to a repository, instance- and schema- correspondences between the new tables and all other tables in the repository are automatically generated.
These correspondences between the tables in the repository can then be used by the Correspondence-based search. 
The Correspondence generation/finding is done by the CorrespondenceCreator (more info here: http://web.informatik.uni-mannheim.de/ds4dm/#components_PreProcCorrespondenceCreation).
For evaluating the quality of the found/generated correspondences, the T2D Goldstandard was used (more info here: http://web.informatik.uni-mannheim.de/ds4dm/#evaluation). 
This Goldstandard is a collection of tables for which the correspondences have been manually determined. The evaluation is done by comparing the correspondences found by the CorrespondenceCreator-Preprocessing-Program, with the true correspondences (i.e. the manually determined ones).

The sub-folder "result" contains the correspondences found by the CorrespondenceCreator-Preprocessing-Program.
The sub-folder "solution" contains the true correspondences (i.e. the manually determined ones).

Each of these folders has a file for the instance matches and a file for the schema matches.

