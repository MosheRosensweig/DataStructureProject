import java.util.ArrayList;

import com.sun.javafx.image.impl.ByteIndexed.Getter;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnValuePair;

/**
 * This is the table class. It consists of a list of rows (See Row class).
 * Additionally, it stores the rules of the row columns in columnDescriptions.
 * 
 * 1] Creating a new Row:
 * 
 * 		When a new Table is created, it generates a model row. Each column in the
 * 		row store their own rules.
 * 
 * 2] Adding a Row:
 * 
 * 		When a Row is added, the table duplicates it's modelRow and then edits it
 * 		with the new values. If any value does not fit the column, the row is not
 * 		saved and the method "addNewRow" returns null.
 * 
 * @author mosherosensweig
 * @version 5/8/17 3:54pm
 *
 */
public class Table {

	//this holds all the rows
	//Testing 
	private ArrayList<Row> table = new ArrayList<Row>();
	//this holds the description of the rows
	//TODO - consider removing this, and convert this into columnRowPairs - then copy and duplicate those
	private final ColumnDescription[] columnDescriptions;
	private final String tableName;
	private final ColumnDescription primaryKey;
	private final Row modelRow;
	private int tableSize = 0;
	
	public Table(String tableName, ColumnDescription[] columnDescriptions, ColumnDescription primaryKey)
	{
		this.tableName = tableName;
		this.columnDescriptions = columnDescriptions;
		this.primaryKey = primaryKey;
		this.modelRow = createModelRow(columnDescriptions);
	}
	
	private Row createModelRow(ColumnDescription[] columnDescriptions)
	{
		Row newRow = new Row(columnDescriptions, primaryKey);
		return newRow;
	}
	
	public Row getModelRow()
	{
		return modelRow;
	}
	
	public ArrayList<Row> getTable()
	{
		return table;
	}

	/**
	 * Create a new Row based on the model Row
	 * by duplicating and editing it
	 * @return
	 */
	public Row addNewRow(ColumnValuePair[] columnValuePairs)
	{
		//added later
		Row newRow = makeDuplicateModRow();
		
		/* This worked, but I moved it out - leaving it here just in case
		//make the basic row structure - duplicate the model row
		ArrayList<Column> cols = modelRow.getColumns();
		ArrayList<Column> temp = new ArrayList<Column>();
		for(Column col : cols){
			temp.add(new Column(col));
		}
		
		Row newRow = new Row(columnDescriptions, primaryKey, new ArrayList<Column>(temp), tableSize);
		*/
		//add the details 
		boolean rowWasMadeCorrectly = newRow.edit(columnValuePairs, table);
		if(rowWasMadeCorrectly){
			table.add(newRow);
			tableSize++;
		}
		return (rowWasMadeCorrectly) ? newRow : null;
	}
	
	public Row makeDuplicateModRow()
	{
		//make the basic row structure - duplicate the model row
		ArrayList<Column> cols = modelRow.getColumns();
		ArrayList<Column> temp = new ArrayList<Column>();
		for(Column col : cols){
			temp.add(new Column(col));
		}
		Row newRow = new Row(columnDescriptions, primaryKey, new ArrayList<Column>(temp), tableSize);
		return newRow;
	}
	
	public String getTableName()
	{
		return tableName;
	}
	
	public ColumnDescription[] getColumnDescriptions() {
		return columnDescriptions;
	}
	
	@Override
	public String toString() {
		String result = "---------------------------------------------------------------------------------------------------------------------------"
				+ "-------------------------------------------------"
				+ "\n"
				+ "Count] (RowNum)\t";
		int i = 0;
		for(Column clm : modelRow.getColumns()){
			String temp = clm.getColumnName();
			if(temp.length() < 10) temp = temp + "\t";
			result = result + "[" + i + "] " + temp + "\t| ";
			i++;
		}
		result = result +
				"\n---------------------------------------------------------------------------------------------------------------------------"
				+ "-------------------------------------------------"
				+ "\n\n";
		int j = 0;
		for(Row row : table){
			result = result + "Row " + j + "] (" + row.getRowNumber() + ")\t" + row.toString();
			j++;
		}
		return result;
	}
	
}
