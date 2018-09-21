package de.uni_mannheim.informatik.dws.ds4dm.customMatchingRules;


import de.uni_mannheim.informatik.additionalWinterClasses.MatchableTableColumn;
import de.uni_mannheim.informatik.additionalWinterClasses.MatchableTableRow;
import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.similarity.string.TokenizingJaccardSimilarity;

/**
 * {@link Comparator} for finding instance-matches between {@link Table}s 
 * It calculates a similarity score for two instances
 * 
 * The similarity score is the jaccardSimilarity between the two subjectColumn values of the two instances.
 * 
 * @author Benedikt Kleppmann (benedikt@dwslab.de)
 * 
 */


public class KeyColumnComparatorJaccard implements Comparator<MatchableTableRow, MatchableTableColumn> {

	private static final long serialVersionUID = 1L;
	private TokenizingJaccardSimilarity sim = new TokenizingJaccardSimilarity();

	@Override
	public double compare(
			MatchableTableRow record1,
			MatchableTableRow record2,
			Correspondence<MatchableTableColumn, Matchable> schemaCorrespondences) {
		
		double similarity = 0;
		
		if (record1.get(record1.getSubjectColumnIndex())!=null && record2.get(record2.getSubjectColumnIndex())!=null){
			String subjectValue1 = record1.get(record1.getSubjectColumnIndex()).toString();
			String subjectValue2 = record2.get(record2.getSubjectColumnIndex()).toString();
			
			similarity = sim.calculate(subjectValue1,subjectValue2);
			similarity = Math.pow(similarity,0.1);
//			if (similarity>0.5){
//				System.out.println("Key Column: " + subjectValue1 + " <--> " + subjectValue2 + " (" + String.format("%.4f", similarity) + ")");
//			}
		}

		
		 

		return similarity;
	}

}

