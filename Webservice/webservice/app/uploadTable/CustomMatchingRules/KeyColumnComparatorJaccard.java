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
package uploadTable.CustomMatchingRules;


//import de.uni_mannheim.informatik.additionalWinterClasses.MatchableTableColumn;
//import de.uni_mannheim.informatik.additionalWinterClasses.MatchableTableRow;

import de.uni_mannheim.informatik.dws.winter.matching.rules.Comparator;
import de.uni_mannheim.informatik.dws.winter.model.Correspondence;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;
import de.uni_mannheim.informatik.dws.winter.similarity.string.TokenizingJaccardSimilarity;
import uploadTable.additionalWinterClasses.MatchableTableColumn;
import uploadTable.additionalWinterClasses.MatchableTableRow;

/**
 * {@link Comparator} for finding instance-matches between {@link Table}s 
 * It calculates a similarity score for two instances
 * 
 * The similarity score is the jaccardSimilarity between the two subjectColumn values of the two instances.
 * 
 * @author Benedikt Kleppmann (benedikt@dwslab.de)
 * 
 */


public class KeyColumnComparatorJaccard implements  Comparator<MatchableTableRow, MatchableTableColumn> {

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
//			System.out.println("KeyColumn," + record1.getSubjectColumnIndex() + "," +  record1.getRowNumber() + "," + subjectValue1 + "," + record2.getSubjectColumnIndex() + "," +  record2.getRowNumber() + "," + subjectValue2 + "," + String.format("%.4f", similarity) );

		}

		
		 

		return similarity;
	}

}

