//as of 5/4/17 7:29pm
import java.util.ArrayList;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription;

/**
 * Each column contains stores the data of the table.
 * Each column stores it's description (cd) so that it knows it's own rules
 * There are two column constructors:
 * 	[1] The 1st is a standard constructor to make model rows. 
 *  [2] The 2nd take a column as it's parameter. (I did this instead of implementing 
 *  	clonable - credit goes to stackExchange for the idea)
 *  When a regular row is created, it uses the setValue method to set up it's new 
 *  value correctly.
 *  
 *  Each column stores it's "i" value - the index of where it is stored in the row
 *  (I chose "i" because of my standard "(int i = 0; i < etc.." )
 * 
 * @author mosherosensweig
 * @version 5/2/17 7:36pm
 *
 * @param <V>
 */
public class Column <V> {
	
	private final String columnName;
	private final ColumnDescription cd;
	private V value;
	private boolean isPrimaryKey;
	private int i;
	//TODO - remove this
	//private String valueType;
	
	/**
	 * The constructor for the modelRow
	 * @param columnName
	 * @param cD
	 * @param primaryKeyName
	 * @param i - the position within the arrayList that it's in
	 */
	public Column(String columnName, ColumnDescription cD, String primaryKeyName, int i)
	{
		this.columnName = columnName;
		this.cd = cD;
		this.value = createValue(cD);
		this.isPrimaryKey = columnName.equals(primaryKeyName);
		this.i = i;
	}
	
	/**
	 * The constructor for a cloned column
	 * @param col
	 */
	public Column(Column col)
	{
		this.columnName = new String(col.getColumnName());
		this.cd = col.getCd();
		V tempVal = (V) col.getValue();
		if(tempVal != null){
			String tempValName = tempVal.getClass().getName();
			if(tempValName.equals("Boolean"))		   this.value = (V) new Boolean((Boolean)tempVal);
				else if(tempValName.equals("Integer")) this.value = (V) new Integer((Integer)tempVal); 
				else if(tempValName.equals("String"))  this.value = (V) new String((String)tempVal);
				else if(tempValName.equals("Double"))  this.value = (V) new Double((Double)tempVal);
		}
		this.isPrimaryKey = new Boolean(col.isPrimaryKey());
		this.i = new Integer(col.getI());
	}
	
	/**
	 * Determine what type of value the type is and return it's default or null if doesn't have a default
	 * @param cD - this column's column description
	 * @return - the default value
	 */
	private V createValue(ColumnDescription cD)
	{
		V result = null;
		
		String dataType = "" + cD.getColumnType();
		
		//It's a string
		if(dataType.equals("VARCHAR")){
			String str = (!cD.getHasDefault()) ? null : (cD.getDefaultValue());
			result = (V) str; 
		}
		//It's a Double
		else if(dataType.equals("DECIMAL")){
			Double doubl = (!cD.getHasDefault()) ? null : new Double(cD.getDefaultValue());
			result = (V) doubl; 
		}
		else if(dataType.equals("INT")){
		//It's an Integer
			Integer intege = (!cD.getHasDefault()) ? null : new Integer(cD.getDefaultValue());
			result = (V) intege;
		}
		//It's Boolean
		else{
			Boolean boolea = (!cD.getHasDefault()) ? null : new Boolean(cD.getDefaultValue());
			result = (V) boolea;
		}

		return result;
	}

	public String getColumnName() {
		return columnName;
	}

	public V getValue() {
		return value;
	}

	/**
	 * 
	 * @param value - the new value for this
	 * @param table - the list of all rows - used for checking uniqueness
	 * @param i 	- the position of this column with each row
	 * @return - if the column value fit this column's rules - return true; otherwise return false
	 */
	public boolean setValue(V value2, ArrayList<Row> table) {
		boolean worked = false;
		//if there is a value
		if(value2 != null){ //old way - keeping just in case
			//it needs to be unique
			worked = checkUnique(value2, table);
			if(!worked) return false;
			worked = checkRules(value2);
			return worked;
		}
		//no value was specified, use the default value
		else{
			if(this.isPrimaryKey) return false;
			if(cd.isNotNull())	  return false; //the value is not allowed to be null
			//this.value = (V) cd.getDefaultValue();
			this.value = createValue(cd);
			worked = checkUnique(value, table);
			if(!worked) return false;
			return true;
		}
	}
	
	/**
	 * When updating a value - we've already checked for uniquess. 
	 * Now we just need to make sure the value fits
	 * @param value2
	 * @return
	 */
	public boolean updateValue(V value2) {
			boolean worked = false;
			worked = checkRules(value2);
			return worked;
	}
	
	private boolean checkRules(V value2)
	{
		boolean worked = false;
		String valToStr = value2.toString();
		boolean setToNull = valToStr.toLowerCase().equals("null");
		if(setToNull){
			if(cd.isNotNull()) return false; //the value is not allowed to be null
			else{
				value = null;
				return true;
			}
		}
		boolean isString = valToStr.startsWith("'"); // || valToStr.startsWith("\"");
		boolean isNumber = Character.isDigit(valToStr.charAt(0));
		boolean isBool = (valToStr.toLowerCase().equals("false") || valToStr.toLowerCase().equals("true"));
		String columnType = cd.getColumnType().toString();
		if(columnType.equals("VARCHAR") && isString){
			boolean ok = checkVarChar((String) value2); 
			worked = (!ok) ? false : true;
		}
		else if(columnType.equals("DECIMAL") && isNumber){
			Double value3 = new Double((String) value2);
			boolean ok = checkDecimal(value3);
			worked = (!ok) ? false : true;
		}
		else if(columnType.equals("INT") && isNumber){
			Integer value3 = new Integer((String) value2);
			value = (V) value3;
			worked = true;
		}
		else if(columnType.equals("BOOLEAN") && isBool){
			Boolean value3 = new Boolean((String) value2);
			value = (V) value3;
			worked = true;
		}
		return worked;
	}
	private boolean checkUnique(V value2, ArrayList<Row> table)
	{
		if(this.isPrimaryKey || cd.isUnique()){
			for(Row row : table){
				//if any other column has the same value
				V rowssValue = (V) row.getColumns().get(i).getValue();
				String rowsValuesName = (rowssValue == null) ? "null" : rowssValue.toString();
				String thissValessName = (value2 == null) ? "null" : value2.toString();
				if(rowsValuesName.equals(thissValessName)) return false;
			}
		}
		return true;
	}
	
	//assuming the protocol is not to truncate
	private boolean checkVarChar(String value2)
	{
		 int maxLength = cd.getVarCharLength();
		 if(maxLength >= value2.length()) value = (V) value2;
		 else return false;
		 return true; 
	}
	
	private boolean checkDecimal(Double value2)
	{
		int fracLength  = cd.getFractionLength();//1 means -10 < x <10; 2 means -100 < x < 100
		int wholeLength = cd.getWholeNumberLength();//1 means .0 < x .9; 2 means .01 < x < .99
		String sV2 = "" + value2;
		//for "1.2" = 1; for "10.2" = 2; Therefore, indexOfDot represents the number of whole digits
		int indexOfDot = sV2.indexOf(".");
		int fromDotToEnd = sV2.substring(indexOfDot+1).length();
		if(wholeLength >= indexOfDot && fracLength >= fromDotToEnd) value = (V) value2;
		else return false;
		return true;
	}

	public ColumnDescription getCd() {
		return cd;
	}

	public boolean isPrimaryKey() {
		return isPrimaryKey;
	}

	public int getI() {
		return i;
	}

	@Override
	public String toString() {
		return this.value + " ";
	}
	
}
