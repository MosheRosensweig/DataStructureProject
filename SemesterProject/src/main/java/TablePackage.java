import java.util.ArrayList;

/**
 * This class wraps a table and it's indices together
 * Each Index contains: (1) The BTree
 * 					    (2) The name of the column that the BTree is made of
 * 					    (3) The position of that column in each row 
 * 					    (4) The name of the index
 * The "btrees" field contains an array the size of the number of columns. 
 * Each position in the array has a btree that corresponds with the column
 * in that position in the row.
 * Ex: If the row was "FirstName, LastName" -> then the position of FirstName (also known as "I") 
 * is "0". So btrees[0] holds the index for "FirstName".
 * 
 * @author mosherosensweig
 *
 */
public class TablePackage {
	private Table table;
	private String tableName;
	//private ArrayList<Index> btrees;
	//make an array the size of the number of columns
	private Index[] btrees;
	
	/**
	 * 
	 * @param table
	 */
	public TablePackage(Table table)
	{
		this.table = table;
		this.tableName = table.getTableName();
		btrees = new Index[table.getModelRow().getColumns().size()];
	}

	public Table getTable() {
		return table;
	}
	
	public Index[] getBtrees() {
		return btrees;
	}
	
	public void resetBtrees(){
		int len = btrees.length;
		btrees = new Index[len];
		/* Didnt work
		for(Index ind : btrees){
			ind. = null;
		}
		*/
	}
}
