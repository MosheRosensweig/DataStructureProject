import java.util.ArrayList;

import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnID;

/**
 * This is the table representation for select Queries
 * I made the regular Table structure to specific to contain these values.
 * These behave similarly enough to a table
 * 
 * The printout should be the same style as the table
 * @author mosherosensweig
 *
 */
public class SelectTable {

	private ArrayList<OrderByRow> table;
	private ArrayList<FunctionColumn> functionColumns;

	public SelectTable(ArrayList<OrderByRow> table)
	{
		this.table = table;
	}

	public SelectTable(ArrayList<FunctionColumn> functionColumns, boolean unnecisary)
	{
		this.functionColumns = functionColumns;
	}

	/**
	 * Basically the same logic from the Table classes
	 */
	@Override
	public String toString() {
		int resultLen = 0;
		String result = "";
		//-----------------//
		// Make the header //
		//-----------------//
		result = "---------------------------------------------------------------------------------------------------------------------------"
				+ "-------------------------------------------------"
				+ "\n\t";
		resultLen = result.length();
		int i = 0;
		if(this.table != null){
			if(this.table.size() > 0){
			//for(int i = 0; i < table.size(); i++){
				for(Column clm : table.get(0).getColumns()){
					String temp = clm.getColumnName();
					if(temp.length() < 10) temp = temp + "\t";
					result = result + "[" + i + "] " + temp + "\t| ";
					i++;
				}
			}
		}
		else{
			for(FunctionColumn fc : functionColumns){
				String temp = fc.getColumnName();
				if(temp.length() < 10) temp = temp + "\t";
				result = result + "[" + i + "] " + temp + "\t| ";
				i++;
			}
			result = result + "\n";
			result = result + "Function: ";
			for(FunctionColumn fc : functionColumns){
				String temp = fc.getFunctionName();
				if(temp.length() < 10) temp = temp + "\t";
				result = result + "   " + temp + "\t| ";
			}
		}
		if(result.length() == resultLen) result = result + "No Results";
		result = result +
				"\n---------------------------------------------------------------------------------------------------------------------------"
				+ "-------------------------------------------------"
				+ "\n\n";
		int j = 0;
		if(table != null){
			for(OrderByRow row : table){
				result = result + "Row " + j + "]" + "\t" + row.toString();
				j++;
			}
		}
		else{
			result = result + "Row " + 0 + "]\t";
			for(FunctionColumn fc : functionColumns){
				String temp = fc.getValue().toString();
				if(temp.length() < 10) temp = temp + "\t";
				result = result + "[" + j + "]    " + temp + "\t| "; 
				j++;
			}
		}
		return result;
	}

	public ArrayList<OrderByRow> getTable() {
		return table;
	}

	public ArrayList<FunctionColumn> getFunctionColumns() {
		return functionColumns;
	}

	//----------------//
	// Static Classes //
	//----------------//

	public static class OrderByRow {
		private ArrayList<Column> row;

		public OrderByRow(ArrayList<Column> row)
		{
			this.row = row;
		}

		public ArrayList<Column> getColumns()
		{
			return row;
		}

		@Override
		public String toString() {
			String result = "";
			int i = 0;
			for(Column clm : row){
				String temp = clm.toString();
				if(temp.length() < 10) temp = temp + "\t";
				result = result + "[" + i + "] " + temp + "\t| ";
				i++;
			}
			result = result + "\n";
			return result;
		}


	}

	public static class FunctionColumn {
		private Double value;
		private String functionName;
		private String columnName;

		public FunctionColumn(Double value, String functionName, String columnName) {
			this.value = value;
			this.functionName = functionName;
			this.columnName = columnName;
		}

		public Double getValue() {
			return value;
		}

		public String getFunctionName() {
			return functionName;
		}

		public String getColumnName() {
			return columnName;
		}

		@Override
		public String toString() {
			return " ";
		}


	}
}
