import java.util.ArrayList;

import javax.print.DocFlavor.READER;

/**
 * Each resultSet contains the information requested in the query -> It contains
 * a list of rows -> It contains a list of name.dataTypes
 * 
 * Note: It is NOT a "join"
 * 
 * Returns: 
 * 		-> Create Table -> return an empty table with the columns that were just created 
 * 		-> Create Index -> returns a 1x1 boolean table indicating success. True means the query didn't refer to non-existent data
 * 		-> Update -> returns a 1x1 boolean table indicating success. True means the query didn't refer to non-existent data
 * 		-> Insert -> returns a 1x1 boolean table indicating success. True means the query didn't refer to non-existent data
 * 		-> Delete -> returns a 1x1 boolean table indicating success. True means the query didn't refer to non-existent data
 * 		-> Select -> a single table of the values that matched the query
 * 
 * Note: Update to the required results from piazza:
 * 		insert, update, and delete
 * 		aren't getting any data from the DB, they are just changing the DB, and
 * 		therefore you need some way to indicate to the caller that the query ran
 * 		successfully, thus returning a one cell table with "true". when it comes to
 *		bad queries, you can either return a two cell table - one with "false" and
 *		one with the details of the error or exception - or you can simply throw an
 *		exception - either is acceptable, but be sure to include in a comment which
 * 		did and why
 * 
 * 		I thought it was easier to throw exceptions where problems occurred and writing why it happened
 * 		- thereby giving the user as much information about the problem as possible.
 * 		Being that I set my ResultSet in the SQLParserControl class, and throw errors from that class,
 * 		It would be difficult to account for the exceptions.
 * 
 * Note: If Delete/Update doesn't change anything, it doesn't throw an error, it just says the table worked
 * 
 * @author mosherosensweig
 *
 */
public class ResultSet {

	private TablePackage tablePackage;
	//boolean indicator if it's a create table query
	private boolean createTableQ = false;
	//this is the boolean for update, delete, and insert
	private boolean tableUpdate = false;
	private boolean deleteChangedSomething = false;
	private boolean updateChangedSomething = false;
	/* this is the boolean to indicate if it was a index query - I have this 
	 * Separate, so that it can naturally act differently (like printouts) 
	 */
	private boolean indexQuery = false;
	//boolean indicator if it's a selectTable query
	private boolean selectTableBool = false;
	private SelectTable selectTable;
	//the verbose flag - used for rpinting
	private boolean verbose = false;

	// ----------------------//
	// for testing purposes //
	// ----------------------//
	private Table table;

	public ResultSet(Table table) {
		this.table = table;
	}

	public Table getTable() {
		return table;
	}
	//-----------------------------//
	// end of for testing purposes //
	//-----------------------------//

	/**				 **
	 * ************* **
	 * Constructors  **
	 * ************* **
	 				 **/
	
	// Constructor for createTableQuery
	public ResultSet(TablePackage tablePackage) {
		this.tablePackage = tablePackage;
		this.createTableQ = true;
	}

	/**
	 * Constructor for Insert, Delete, and Update
	 * @param tablePackage - the updated version of the table and indices
	 * @param hey - indicating if it worked. Being that exceptions are thrown
	 * 			if it doesn't work, this should always be true.
	 */
	public ResultSet(TablePackage tablePackage, boolean updated)
	{
		this.tablePackage = tablePackage;
		this.tableUpdate = true;
	}
	
	public ResultSet(TablePackage tablePackage, String index)
	{
		this.tablePackage = tablePackage;
		this.indexQuery = true;
	}
	
	/**
	 * SelectQuery Constructor
	 * @param tablePackage
	 * @param selectTable
	 */
	public ResultSet(TablePackage tablePackage, SelectTable selectTable)
	{
		this.tablePackage = tablePackage;
		this.selectTable = selectTable;
		this.selectTableBool = true;
	}

	
	/*
	 * Constructor for a delete that effected nothing
	 */
	public ResultSet(TablePackage tablePackage, boolean updated, boolean delete)
	{
		this.tablePackage = tablePackage;
		this.tableUpdate = true;
		this.deleteChangedSomething = delete;
	}
	
	/*
	 * Constructor for An update that effected nothing
	 */
	public ResultSet(boolean updateStatus, TablePackage tablePackage)
	{
		this.tablePackage = tablePackage;
		this.updateChangedSomething = !updateStatus;
	}
	
	//---------//
	// Getters //
	//---------//
	
	public TablePackage getTablePackage() {
		return tablePackage;
	}

	public SelectTable getSelectTable() {
		return selectTable;
	}
	public boolean didDeleteChangeSomething()
	{
		return deleteChangedSomething;
	}
	public boolean isCreateTableQ() {
		return createTableQ;
	}

	public boolean isTableUpdate() {
		return tableUpdate;
	}

	public boolean isIndexQuery() {
		return indexQuery;
	}

	public boolean isSelectTableBool() {
		return selectTableBool;
	}

	
	public boolean isVerbose() {
		return verbose;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}


	@Override
	public String toString() {
		 String result = "";
		 if(verbose) result = toStringVerbose();
		 else result = toStringNonVerbose();
		 return result;
	}

	public String toStringVerbose() 
	{
		
		String result = "";
		if(createTableQ) result = tablePackage.getTable().toString();
		else if(deleteChangedSomething) 
			result = "\n##################################\n# Nothing Deleted, old table is: #\n##################################\n" + tablePackage.getTable().toString();
		else if(updateChangedSomething) 
			result = "\n##################################\n# Nothing Updated, old table is: #\n##################################\n" + tablePackage.getTable().toString();
		else if(tableUpdate && ! deleteChangedSomething && !updateChangedSomething) 
			result = "\n##########################\n# Updated, new table is: #\n##########################\n" + tablePackage.getTable().toString();
		else if(selectTableBool) result = "\n###################\n# Your Selection: #\n###################\n" + selectTable.toString();
		else if(indexQuery){
			System.out.println("\t----------------------------------------------------------------------------------------------------\n"
					+ "\t\tThe non-verbose printout:\n"
					+ "\t\t(This 1st prints out the Root node, then it recursively prints out the left most node's\n"
					+ "\t\t child.)\n"
					+ "\t----------------------------------------------------------------------------------------------------"
					+ "\n\n");
			Index[] indicies = tablePackage.getBtrees();
			for(int i = 0; i < indicies.length; i++){
				if(indicies[i] != null){
					BTree3 btree = indicies[i].getBtree();
					btree.printNodesKeys();
				}
			}
			System.out.println("\n\n"
					+ "\t----------------------------------------------------------------------------------------------------"
					+ "\n\t\tThe verbose print-out (each key has it's list of rowNumbers indicated (See README):\n"
					+ "\t----------------------------------------------------------------------------------------------------"
					+ "\n\n\n");
			for(int i = 0; i < indicies.length; i++){
				if(indicies[i] != null){
					BTree3 btree = indicies[i].getBtree();
					btree.printNodesKeysAndValues();
				}
			}
		}
		
		return result;
	}

	public String toStringNonVerbose() {
		String result = "";
		if(createTableQ) result = tablePackage.getTable().toString();
		else if(deleteChangedSomething) 
			result = "\n###################\n# Nothing Deleted #\n###################\n";
		else if(updateChangedSomething) 
			result = "\n###################\n# Nothing Updated #\n###################\n";
		else if(tableUpdate && ! deleteChangedSomething && !updateChangedSomething) 
			result = "\n###########\n# Updated #\n###########\n";
		else if(selectTableBool) result = "\n###################\n# Your Selection: #\n###################\n" + selectTable.toString();
		else if(indexQuery){
			result = "\n#######################\n# Index Created #\n#######################\n";
		}
		
		return result;
	}

	
}
