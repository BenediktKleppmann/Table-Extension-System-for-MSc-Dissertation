package de.uni_mannheim.informatik.dws.ds4dm.customMatchingRules;

import java.util.LinkedList;
import java.util.List;

import org.joda.time.DateTime;

import de.uni_mannheim.informatik.additionalWinterClasses.MatchableTableColumn;
import de.uni_mannheim.informatik.additionalWinterClasses.MatchableTableRow;
import de.uni_mannheim.informatik.dws.ds4dm.customSimilarity.InverseExponentialDateSimilarity;
import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
import de.uni_mannheim.informatik.dws.winter.matrices.SimilarityMatrix;
import de.uni_mannheim.informatik.dws.winter.matrices.SparseSimilarityMatrixFactory;
import de.uni_mannheim.informatik.dws.winter.matrices.matcher.BestChoiceMatching;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
import de.uni_mannheim.informatik.dws.winter.similarity.date.NormalisedDateSimilarity;
import de.uni_mannheim.informatik.dws.winter.similarity.date.WeightedDateSimilarity;
import de.uni_mannheim.informatik.dws.winter.similarity.numeric.DeviationSimilarity;
import de.uni_mannheim.informatik.dws.winter.similarity.string.TokenizingJaccardSimilarity;



/**
 * {@link Comparator} for finding instance-matches between {@link Table}s 
 * It calculates a similarity score for two instances
 * 
 * For calculating the similarity score, only the non-key-column values of the instances are considered.
 * For each of these non-key-column values of the left instance, the non-key-column value on the right side with the highest similarity is identified as corresponding value.
 * The overall similarity score is the average of the scores of corresponding values.
 * 
 *  When calculating the similarity between a pair of values, the datatype of these values is considered:
 *    * If they are both numeric, then we use DeviationSimilarity.
 *    * If they are both dates, then we use NormalisedDateSimilarity.
 *    * If they are both boolean, then we check for equality 
 *    * Otherwise, we use TokenizingJaccardSimilarity
 *    
 * 
 * @author Benedikt Kleppmann (benedikt@dwslab.de)
 * 
 */


public class NonKeyColumnComparator_withDType implements Comparator<MatchableTableRow, MatchableTableColumn> {

	private static final long serialVersionUID = 1L;
	
	private DeviationSimilarity deviationSim = new DeviationSimilarity();
	private InverseExponentialDateSimilarity dateSim = new InverseExponentialDateSimilarity();
	private TokenizingJaccardSimilarity jaccardSim = new TokenizingJaccardSimilarity();
	
	@Override
	public double compare(
			MatchableTableRow record1,
			MatchableTableRow record2,
			Correspondence<MatchableTableColumn, Matchable> schemaCorrespondences) {
		
		SimilarityMatrix<MatchableTableColumn> sim = new SparseSimilarityMatrixFactory().createSimilarityMatrix(0, 0);
		
		List<Double> listOfSimilarityScores = new LinkedList<Double>();
		
		for (int columnIndex1 = 0; columnIndex1< record1.getSchema().length; columnIndex1++){
			for (int columnIndex2 = 0; columnIndex2< record2.getSchema().length; columnIndex2++){
				if(columnIndex1!=record1.getSubjectColumnIndex() && columnIndex2!=record2.getSubjectColumnIndex()){
					
					MatchableTableColumn c1 = record1.getSchema()[columnIndex1];
					MatchableTableColumn c2 = record2.getSchema()[columnIndex2];
					
					DataType datatype1 = c1.getType();
					DataType datatype2 = c2.getType();
					
					double similarity = 0;
					
					String typeOfComparison = "";
					if(record1.get(columnIndex1)!=null && record2.get(columnIndex2)!=null){
						
						if (datatype1 == DataType.numeric && datatype2 == DataType.numeric)	{
							similarity = deviationSim.calculate(Double.valueOf(record1.get(columnIndex1).toString()), Double.valueOf(record2.get(columnIndex2).toString()));
							typeOfComparison = "numbers";
						} else if (datatype1 == DataType.date && datatype2 == DataType.date)	{
							dateSim.setHalvingTime(730.0);
							similarity = dateSim.calculate( (DateTime) record1.get(columnIndex1), (DateTime) record2.get(columnIndex2));
							typeOfComparison = "dates";
						} else if (datatype1 == DataType.bool && datatype2 == DataType.bool)	{
							similarity = (record1.get(columnIndex1)==record2.get(columnIndex2) ? 1 : 0);
							typeOfComparison = "booleans";
						} else{
							similarity = jaccardSim.calculate(record1.get(columnIndex1).toString(),record2.get(columnIndex2).toString());
							typeOfComparison = "strings";
						}
					}
					
//					if (similarity !=0.0){
//						System.out.println(String.format("comparing %s: '%s' <--> '%s' (%.4f)", typeOfComparison, String.valueOf(record1.get(columnIndex1)), String.valueOf(record2.get(columnIndex2)), similarity));
//					}
					
					
					if (!Double.isNaN(similarity)) {
						
						sim.add(c1, c2, similarity);
						listOfSimilarityScores.add(similarity);
					}
				}	
			}
		}
		
		BestChoiceMatching bcm = new BestChoiceMatching();
		
		sim = bcm.match(sim);
		
		double NumberOfNonZeroElements = (double)sim.getNumberOfNonZeroElements();
		double finalSim = 0.0;
		if (NumberOfNonZeroElements != 0.0){
			finalSim = sim.getSum() / NumberOfNonZeroElements;
		}
		
		
		finalSim = Math.pow(finalSim,0.1);
		

//		for (double similarity: listOfSimilarityScores){
//			if (similarity != 0.0 && finalSim==0.0){
//				System.out.println("Herrecy!! the bloody BestChoiceMatching is fucking it up!!");
//				System.out.println("Non-key Column: " + record1.getIdentifier() + " <--> " + record2.getIdentifier() + String.format(" (%.4f, %.4f)",  finalSim, similarity));
//			}
//		}
		
		
		return finalSim;
	}

}

