import java.util.ArrayList;
import java.util.Arrays;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnValuePair;

/**
 * Each Row has a list of columns.
 * The Table class creates a default Row and then copies it and modifies it every time it makes a new row
 * 
 * @author mosherosensweig
 * @version 5/1/17 3:53pm
 *
 */
public class Row {

	private ColumnDescription[] columnDescs;
	private ArrayList<Column> columns = new ArrayList<Column>();
	private final ColumnDescription primaryKey;
	private final int rowNumber;//The position of the row in the table
	
	/**
	 * The constructor for the model row
	 * @param columnDescriptions
	 * @param primaryKey
	 */
	public Row(ColumnDescription[] columnDescriptions, ColumnDescription primaryKey)
	{
		this.columnDescs = columnDescriptions;
		this.primaryKey = primaryKey;
		buildRow(columnDescriptions);
		this.rowNumber = -1;
	}
	
	/**
	 * Constructor of new rows
	 * @param columnDescriptions
	 * @param primaryKey
	 * @param columns
	 */
	public Row(ColumnDescription[] columnDescriptions, ColumnDescription primaryKey, ArrayList<Column> columns, int rowNumber)
	{
		this.columnDescs = columnDescriptions;
		this.primaryKey = primaryKey;
		this.columns = columns;
		this.rowNumber = rowNumber;
	}
	
	private void buildRow(ColumnDescription[] columnDescriptions)
	{
		int i = 0;
		for(ColumnDescription cd : columnDescriptions){
			columns.add(new Column(cd.getColumnName(), cd, primaryKey.getColumnName(), i));
			i++;
		}
	}
	
	public boolean edit(ColumnValuePair[] columnValuePairs, ArrayList<Row> table)
	{
		//flag if it works
		boolean result = false;
		//I convert this into a list so that I can remove items as they are used
		ArrayList<ColumnValuePair> cvps = new ArrayList<ColumnValuePair>(Arrays.asList(columnValuePairs));
		
		for(Column clm : columns)
		{
			String columnName = clm.getColumnName();
			boolean hadAValue = false;
			//for(ColumnValuePair cvp : cvps){
			for(int i = 0; i < cvps.size(); i++){
				//if this is the right info to put in column clm
				if(columnName.toUpperCase().equals(cvps.get(i).getColumnID().getColumnName().toUpperCase())){
					//pass in the new value, all the rows, and the position of this column in each row
					result = clm.setValue(cvps.get(i).getValue(), table);
					if(result == false) return result;
					cvps.remove(i); //so future iterations dont waste time.
					hadAValue = true;
					break;
				}
			}
			//if there is no value for this column, give it a null value
			if(!hadAValue){
				result = clm.setValue(null, table);
				if(result == false) return result;
			}
			
		}
		return result;
	}
	
	/**
	 * This is used uniquely for update (although if I have time, I will come back and refactor regular edit using this)
	 * This is the almost the same as the regular edit - the difference being - in this method, if there is no
	 * value, it ignores that column
	 * 
	 * @param columnValuePairs
	 * @param table
	 * @return
	 */
	public boolean updateEdit(ColumnValuePair[] columnValuePairs, ArrayList<Row> table)
	{
		//flag if it works
		boolean result = false;
		//I convert this into a list so that I can remove items as they are used
		ArrayList<ColumnValuePair> cvps = new ArrayList<ColumnValuePair>(Arrays.asList(columnValuePairs));
		
		for(Column clm : columns)
		{
			String columnName = clm.getColumnName();
			boolean hadAValue = false;
			//for(ColumnValuePair cvp : cvps){
			for(int i = 0; i < cvps.size(); i++){
				//if this is the right info to put in column clm
				if(columnName.toUpperCase().equals(cvps.get(i).getColumnID().getColumnName().toUpperCase())){
					//pass in the new value, all the rows, and the position of this column in each row
					result = clm.setValue(cvps.get(i).getValue(), table);
					if(result == false) return result;
					cvps.remove(i); //so future iterations dont waste time.
					hadAValue = true;
					break;
				}
			}
		}
		return result;
	}

	//----------------------//
	//	Getters and Setters //
	//----------------------//
	
	public ColumnDescription[] getColumnDescs() {
		return columnDescs;
	}

	public ArrayList<Column> getColumns() {
		return columns;
	}

	public ColumnDescription getPrimaryKey() {
		return primaryKey;
	}
	
	public int getRowNumber() {
		return rowNumber;
	}

	@Override
	public String toString() {
		String result = "";
		int i = 0;
		for(Column clm : columns){
			String temp = clm.toString();
			if(temp.length() < 10) temp = temp + "\t";
			result = result + "[" + i + "] " + temp + "\t| ";
			i++;
		}
		result = result + "\n";
		return result; 
	}
		
	
	
}
