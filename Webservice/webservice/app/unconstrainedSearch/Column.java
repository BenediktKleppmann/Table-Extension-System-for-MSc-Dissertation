//package unconstrainedSearch;
//
//import java.util.LinkedList;
//
//import de.uni_mannheim.informatik.dws.winter.preprocessing.datatypes.DataType;
//
//public class Column {
//	
//	private LinkedList<Object> values;
//	private String header;
//	private DataType dataType;
//	
//	//===================================================================
//	// Methods
//	//===================================================================
//	
//	// values --------------
//	public void setValues(LinkedList<Object> values){
//		this.values = values;
//	}
//	
//	public void addValue(Object value){
//		this.values.add(value);
//	}
//	
//	public LinkedList<Object> getValues(){
//		return this.values;
//	}
//	
//	// header --------------
//	public void setHeader(String header){
//		this.header = header;
//	}
//	
//	public String getHeader(){
//		return this.header;
//	}
//	
//	// dataType --------------
//	public void setDataType(DataType dataType){
//		this.dataType = dataType;
//	}
//	
//	public DataType getDataType(){
//		return this.dataType;
//	}
//	
//	
//	public void simplifyTheColumnsDataTypes(){
//		
//		 
//		if (this.dataType == DataType.numeric){
//			
//			LinkedList<Integer> newValues = new LinkedList<Integer>();
//			for (Object value: this.values){
//				
//				Integer newValue = null;
//				newValue = (Integer) value;
//				newValues.add(newValue);
//			}
//			this.values = newValues;
//		}
//		
//		
//		
//	}
//}
