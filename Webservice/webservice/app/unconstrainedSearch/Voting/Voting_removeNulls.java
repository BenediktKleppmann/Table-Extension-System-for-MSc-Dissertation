package unconstrainedSearch.Voting;


import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.ConflictResolutionFunction;
import de.uni_mannheim.informatik.dws.winter.model.FusedValue;
import de.uni_mannheim.informatik.dws.winter.model.Fusible;
import de.uni_mannheim.informatik.dws.winter.model.FusibleDataSet;
import de.uni_mannheim.informatik.dws.winter.model.FusibleValue;
//import de.uni_mannheim.informatik.dws.winter.datafusion.conflictresolution.ConflictResolutionFunction;
import de.uni_mannheim.informatik.dws.winter.model.Matchable;

/**
 * Vote {@link ConflictResolutionFunction}: returns the most frequent value, in
 * case of two or more similar frequent values the first one in the list of
 * {@link FusableValue}s is returned.
 * 
 * @author Oliver Lehmberg (oli@dwslab.de)
 * 
 * @param <ValueType>
 * @param <RecordType>
 */
public class Voting_removeNulls<ValueType, RecordType extends Matchable & Fusible<SchemaElementType>, SchemaElementType extends Matchable> extends
		ConflictResolutionFunction<ValueType, RecordType, SchemaElementType> {


	@Override
	public FusedValue<ValueType, RecordType, SchemaElementType> resolveConflict(
			Collection<FusibleValue<ValueType, RecordType, SchemaElementType>> values) {

//		removeNulls
		for (FusibleValue<ValueType, RecordType, SchemaElementType> value : values){
			if (value.getValue().toString().equals("null") || value.getValue().toString().equals("nan")) {
				RecordType record= value.getRecord();
				FusibleDataSet<RecordType, SchemaElementType> dataset = value.getDataset();
				ValueType val = null;
				FusibleValue<ValueType, RecordType, SchemaElementType> new_value = new FusibleValue<ValueType, RecordType, SchemaElementType>(val, record, dataset);
				values.remove(value);
				values.add(new_value);
			}	
		}

		
		// determine the frequencies of all values
		Map<ValueType, Integer> frequencies = new HashMap<>();

		for (FusibleValue<ValueType, RecordType, SchemaElementType> value : values) {
			Integer freq = frequencies.get(value.getValue());
			if (freq == null) {
				freq = 0;
			}
			frequencies.put(value.getValue(), freq + 1);
		}

		// find the most frequent value
		ValueType mostFrequent = null;

		for (ValueType value : frequencies.keySet()) {
			if (mostFrequent == null
					|| frequencies.get(value) > frequencies.get(mostFrequent)) {
				mostFrequent = value;
			}
		}

		FusedValue<ValueType, RecordType, SchemaElementType> result = new FusedValue<>(
				mostFrequent);

		// collect all original records with the most frequent value
		for (FusibleValue<ValueType, RecordType, SchemaElementType> value : values) {
			if (value.getValue().equals(mostFrequent)) {
				result.addOriginalRecord(value);
			}
		}

		
		
		return result;
	}



}
