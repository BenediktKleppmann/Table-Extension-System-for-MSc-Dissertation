//package unconstrainedSearch;
//
//import java.util.Arrays;
//import java.util.LinkedList;
//import java.util.List;
//
//import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
//
//public class CorrelationCalculator {
//
//	private Column column1;
//	private Column column2;
//	private static List<DataType> orderedDataTypes =  Arrays.asList(DataType.numeric, DataType.date, DataType.coordinate);
//	private static List<DataType> categoricalDataTypes =  Arrays.asList(DataType.string, DataType.link, DataType.unknown, DataType.unit, DataType.list);
//	
//	
//	public CorrelationCalculator(Column column1, Column column2){
//		this.column1 = column1;
//		this.column2 = column2;
//	}
//	
//	public Double calculateCorrelation(){
//		
//
//		DataType dataType1 = column1.getDataType();
//		DataType dataType2 = column2.getDataType();
//		Double correlationCoefficient = 0;
//		
//		switch (dataType1){
//			case numeric: case date: case coordinate: 
//				switch (dataType2){
//					case numeric: case date: case coordinate: 
//						correlationCoefficient = pearsonsCorrelationCoefficient(column1, column2);
//						break;
//					case string: case link: case unknown: case unit: case list: case bool: 
//						correlationCoefficient = anova(column2, column1);
//						break;
//				}
//				break;
//			case string: case link: case unknown: case unit: case list: case bool: 
//				switch (dataType2){
//					case numeric: case date: case coordinate: 
//						correlationCoefficient = anova(column1, column2);
//						break;
//					case string: case link: case unknown: case unit: case list:  case bool:
//						correlationCoefficient = mutualInformationBasedCorrelation(column1, column2);
//						break;
//				}
//				break;
//		}
//		
//	
//		return correlationCoefficient;
//	}
//	
//	
//	
//	
//	public Double pearsonsCorrelationCoefficient(Column column1, Column column2){
//		
//		Column newColumn1 = assureColumnIsNumeric(column1);
//		Column newColumn2 = assureColumnIsNumeric(column2);
//		Double correlationCoefficient = 0.0;
//		return correlationCoefficient;
//	}
//	
//	public Double anova(Column column1, Column column2){
//		Double correlationCoefficient = 0.0;
//		return correlationCoefficient;
//	}
//	
//	public Double mutualInformationBasedCorrelation(Column column1, Column column2){	
//		Double correlationCoefficient = 0.0;
//		return correlationCoefficient;
//	}
//	
//	
//	public Column assureColumnIsNumeric(Column myColumn){
//		
//		if (myColumn.getDataType() == DataType.date){
//			LinkedList<Integer> newValues = new LinkedList<Integer>();
//			for (Object value :myColumn.getValues()){
//				String.valueOf(value);
//			}
//		}
//		if (myColumn.getDataType() == DataType.coordinate){
//			
//		}
//			
//		return myColumn;
//	}
//}
