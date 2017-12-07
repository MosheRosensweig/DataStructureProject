import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnDescription;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnID;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.ColumnValuePair;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.Condition;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.CreateIndexQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.CreateTableQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.DeleteQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.InsertQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SQLQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SelectQuery;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SelectQuery.FunctionInstance;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.SelectQuery.OrderBy;
import edu.yu.cs.dataStructures.fall2016.SimpleSQLParser.UpdateQuery;

/**
 * This class does most of the dirty work. 
 * It takes the query and determines which type of query it is
 * and then handles the query
 * 
 * @author mosherosensweig
 * @version 5/8/17 3:51pm
 *
 */
public class SQLParserControl {
	
	private ArrayList<TablePackage> tablePackages = new ArrayList<TablePackage>();
	//for testing
	public TablePackage mostRecentTP;
	
	public SQLParserControl(ArrayList<TablePackage> tablePackages)
	{
		this.tablePackages = tablePackages;
	}
	
	/**
	 * Figure out which type of query we got
	 * 
	 * Exception Handling follows the following principle:
	 * 		[1] If this is an exception I throw intentionally, I want to rethrow it
	 * 			after all I already put the information I want into it
	 * 		[2] If it's a standard error, I want to wrap it and resend it
	 * @return the resulting table
	 */
	public ResultSet typeCheck(SQLQuery query) throws Exception
	{
		ResultSet result = null;

		if(query instanceof CreateTableQuery){
			try {
				result = createTable((CreateTableQuery) query);
			} 
			catch (ProjectException f) {
				throw f;
			}
			catch (Exception e) {
				throw new Exception("CreateTable Error: " + e);
				
			}
		}
		else if(query instanceof InsertQuery){
			try{
				result = createInsertQuery((InsertQuery) query);
			}
			catch(ProjectException f){
				throw f;
			}
			catch (Exception e) {
				e.printStackTrace();
				throw new Exception("CreateTable Error: " + e);
			}
		}
		else if(query instanceof CreateIndexQuery){
			try{
				result = createIndex((CreateIndexQuery) query);
			}
			catch (ProjectException f){
				throw f;
			}
			catch (Exception e){
				e.printStackTrace();
				throw new Exception("Create Index Error: " + e);
			}
		}
		else if(query instanceof DeleteQuery){
			try{
				result = delete((DeleteQuery) query);
			}
			catch (ProjectException f){
				throw f;
			}
			catch (Exception e){
				e.printStackTrace();
				throw new Exception("Deletion Error: " + e);
			}
		}
		else if(query instanceof SelectQuery){
			try{	
				result = select((SelectQuery) query);
			}
			catch (ProjectException f){
				throw f;
			}
			catch (Exception e){
				e.printStackTrace();
				throw new Exception("Select Error: " + e);
			}
		}
		else if(query instanceof UpdateQuery){
			try{
				result = update((UpdateQuery) query);
			}
			catch (ProjectException f){
				throw f;
			}
			catch (Exception e){
				e.printStackTrace();
				throw new Exception("Update Error: " + e);
			}
		}
		return result;
	}
	
	/**
	 * Create a new table. 
	 * @param ctq - the create table query object
	 * @return - the table created
	 * @throws Exception [1] you tried to create a table with a name that's already being used
	 */
	private ResultSet createTable(CreateTableQuery ctq) throws Exception
	{
		ResultSet result = null;
		Table table = new Table(ctq.getTableName(), ctq.getColumnDescriptions(), ctq.getPrimaryKeyColumn());
		Row worked = table.getModelRow();
		if(worked != null){
			for(TablePackage tempTP : tablePackages){ 
				if(tempTP.getTable().getTableName().toLowerCase().equals(ctq.getTableName().toLowerCase())) throw new ProjectException("Table" + ctq.getTableName() + "Already exists");
			}
			TablePackage tabPack = new TablePackage(table);
			tablePackages.add(tabPack);
			// old way - TODO - remove
			//result = new ResultSet(table);
			result = new ResultSet(tabPack);
		}
		else{
			throw new ProjectException("Table could not be made");
		}
		return result;
	}
	
	/**
	 * Insert a Row into the table. If the Row was created 
	 * @param insrtQ - the Insert Row  query object
	 * @return
	 * @throws Exception 
	 */
	private ResultSet createInsertQuery(InsertQuery insrtQ) throws Exception
	{
		ResultSet result = null;
		String insertTableName = insrtQ.getTableName();
		Row worked = null;
		TablePackage tempTP = null;
		//-------------------------------------------------//
		// Find the proper tablePackage to add this row to //
		//-------------------------------------------------//
		for(TablePackage tp : tablePackages){
			if(tp.getTable().getTableName().toLowerCase().equals(insertTableName.toLowerCase())){
				tempTP = tp;
				worked = tp.getTable().addNewRow(insrtQ.getColumnValuePairs());
			}
		}
		/* Index Checking */
		if(worked != null){ //if the row was successfully made
			//check if any of the columns have an index - if they do, update the index
			for(Column clm : worked.getColumns()){
				int clmI = clm.getI();
				//if it has an index
				if(tempTP.getBtrees()[clmI] != null){
					BTree3 tempTree = tempTP.getBtrees()[clmI].getBtree();
					ArrayList<Integer> value = (ArrayList<Integer>) tempTree.get((Comparable) clm.getValue());
					//add the position of this row in the table
					//old way 
					//Integer newInt = new Integer(tempTP.getTable().getTable().size() - 1);
					Integer newInt = new Integer(worked.getRowNumber());
					if(value == null){
						ArrayList<Integer> rowsThatContainThisValue = new ArrayList<Integer>();
						rowsThatContainThisValue.add(newInt);
						tempTree.put((Comparable) clm.getValue(), rowsThatContainThisValue);
					}
					else value.add(newInt);
				}
				else if(clm.isPrimaryKey()){
					//the value for it's btree is it's row number. Since this is the latest row, it's row number is size - 1
					//old way
					//Integer rowNum = new Integer(tempTP.getTable().getTable().size() - 1);
					
					//new way - each row stores it's own unique "row" number
					Integer rowNum = new Integer(worked.getRowNumber());
					ArrayList<Integer> rowsThatContainThisValue = new ArrayList<Integer>();
					rowsThatContainThisValue.add(rowNum);
					BTree3 tempBT = new BTree3((Comparable) clm.getValue(), rowsThatContainThisValue);
					tempTP.getBtrees()[clmI] = new Index(tempBT, clm.getColumnName(), clmI, "primaryKey");
				}
			}
		}
		else{
			throw new ProjectException("Row was unable to be added");
		}
		//testing purposes
			mostRecentTP = tempTP;
		result = new ResultSet(tempTP, true);
		return result;
	}
	
	/**
	 * Make an index
	 * 1] Find the correct TablePackage and extract it's table
	 * 2] Find I (the position of the column in each row) (see column)
	 * 3] Get the value from column I and add it to the Index (btree)
	 * 
	 * Index Design:
	 * 		Each BTree key's value stores an ArrayList<Integer>. Each integer
	 * 		is the value of a row that contains this key. To delete a row,
	 * 		just do "al.remove(row#)" it will remove the row number, not the
	 * 		nth item in the array.
	 * 
	 * 
	 * @param query
	 * @return
	 */
	private ResultSet createIndex(CreateIndexQuery query) throws Exception
	{
		ResultSet result = null;
		String insertTableName = query.getTableName().toLowerCase();
		String columnName = query.getColumnName();
		TablePackage tempTP = null;
		
		//------------------------//
		// Find the Correct Table //
		//------------------------//
		for(TablePackage tp : tablePackages){
			if(tp.getTable().getTableName().toLowerCase().equals(insertTableName)) tempTP = tp;
			break;
		}
		//make sure it got a table
		if(tempTP == null) throw new ProjectException("Cound't find a table with that name");
		
		//-------------------------//
		// Unwrap the TablePackage //
		//-------------------------//
		Table tempTable = tempTP.getTable();
		Index[] tempIndexs = tempTP.getBtrees();
		int I = -1; 
		//find "I"
		for(Column clm : tempTable.getModelRow().getColumns()){
			if(clm.getColumnName().toLowerCase().equals(columnName.toLowerCase())){
				I = clm.getI();
				break;
			}
		}
		
		//make sure it found "I"
		if(I < 0) throw new Exception("Couldn't find \"I\"");	
		
		//old way
		//int rowCounter = 0;
		
		for(Row row : tempTable.getTable()){
			//get column "I" from the row
			Column clm = row.getColumns().get(I);
			
			/* If no index exists yet for this column */
			if(tempIndexs[I] == null){
				//old way
				//Integer rowNum = new Integer(rowCounter);
				Integer rowNum = new Integer(row.getRowNumber());
				ArrayList<Integer> rowsThatContainThisValue = new ArrayList<Integer>();
				rowsThatContainThisValue.add(rowNum);
				BTree3 tempBT = new BTree3((Comparable) clm.getValue(), rowsThatContainThisValue);
				tempIndexs[I] = new Index(tempBT, clm.getColumnName(), I, query.getIndexName());
			}
			
			/* If there is already an index for this column */
			else{
				BTree3 tempTree = tempIndexs[I].getBtree();
				ArrayList<Integer> value = (ArrayList<Integer>) tempTree.get((Comparable) clm.getValue());
				//add the position of this row in the table
				//old way
				//Integer rowNum = new Integer(rowCounter);
				Integer rowNum = new Integer(row.getRowNumber());
				//if there is no key for this value
				if(value == null){
					ArrayList<Integer> rowsThatContainThisValue = new ArrayList<Integer>();
					rowsThatContainThisValue.add(rowNum);
					tempTree.put((Comparable) clm.getValue(), rowsThatContainThisValue);
				}
				else value.add(rowNum);
			}
			//old way
			//rowCounter++;
		}
		mostRecentTP = tempTP;
		result = new ResultSet(tempTP, "Index");
		return result;
	}
	
	private ResultSet delete(DeleteQuery query) throws Exception
	{
		ResultSet result = null;
		
		//-----------------------//
		// Get the tablePackage  //
		//-----------------------//
		String tableNameToDelete = query.getTableName();
		TablePackage tempTP = null;
		for(TablePackage tp : tablePackages){
			if(tp.getTable().getTableName().toLowerCase().equals(tableNameToDelete.toLowerCase())) tempTP = tp;
		}
		if(tempTP == null) throw new ProjectException("Couldn't find a table by the name: " + tableNameToDelete); 
		ArrayList<Integer> rowsNumsToDelete;
		int endComparissonTable = tempTP.getTable().getTable().size();//use this to see if the table changed at all since it started
		//---------------//
		// Do the delete //
		//---------------//
		/* No table conditions means delete the whole table*/
		if(query.getWhereCondition() == null){
		//option1 - delete the whole table
			//tablePackages.remove(tempTP);
		//option2 - keep the table but erase all the rows
			tempTP.getTable().getTable().clear();
			tempTP.resetBtrees();
		}
		/* 
		 * If there are conditions 
		 * 1] Get a List of all the rows#'s to delete 
		 * 2] Go through the table - for each row delete it 
		 * 3] Traverse the leaves of the btree like a list and delete any reference to those rows
		 */
		else{
			Condition ctn = query.getWhereCondition();
			rowsNumsToDelete = sortConditons(ctn.getLeftOperand(), ctn.getOperator(), ctn.getRightOperand(), tempTP);
			ArrayList<Row> rows = tempTP.getTable().getTable();
			Iterator<Row> rowsItr = rows.iterator();//use iterator to safely remove values
			
			//for(Row row : rows){
			while(rowsItr.hasNext()){
				Row row = (Row) rowsItr.next();
				Integer rowNumber = new Integer(row.getRowNumber());
				if(rowsNumsToDelete.contains(rowNumber)){
					/* Delete from table */
					//rows.remove(row);
					rowsItr.remove();
					/* Delete from index */
					Index[] indexes = tempTP.getBtrees();
					for(Index ind : indexes){
						if(ind != null){
							//start with the bottomLeft most node
							BTreeNode tempNode = ind.getBtree().getOriginalRoot();
							do{
								for(int i = 0; i < tempNode.size(); i++){
									if(!tempNode.get(i).getKey().equals("*")){
										ArrayList<Integer> value = (ArrayList<Integer>) tempNode.get(i).getValue();
										if(value.contains(rowNumber)) value.remove(rowNumber);
									}
								}
								tempNode = tempNode.getRightNode();
							}while(tempNode != null);
						}
					}
				}
			}
		}
		boolean tableChanged = endComparissonTable == tempTP.getTable().getTable().size();
		if(tableChanged) result = new ResultSet(tempTP, true, tableChanged);
		else result = new ResultSet(tempTP, true);
		return result;
	}
	
	private ResultSet select(SelectQuery query) throws Exception
	{
		ResultSet result = null;
		SelectTable selTab;
		//-----------------------//
		// Get the tablePackage  //
		//-----------------------//
		/* Judah wrote that we only have to select from one table to I can assume all cvp's have the same columnName*/
		//String tableNameToSelect = query.getSelectedColumnNames()[0].getTableName().toLowerCase();
		String tableNameToSelect = query.getFromTableNames()[0].toLowerCase();
		TablePackage tempTP = null;
		for(TablePackage tp : tablePackages){
			if(tp.getTable().getTableName().toLowerCase().equals(tableNameToSelect)) tempTP = tp;
		}
		if(tempTP == null) throw new ProjectException("Couldn't find a table by the name: " + tableNameToSelect); 
		Table table = tempTP.getTable();
		ArrayList<Integer> rowNumsToUpdate = new ArrayList<Integer>();
		//----------------------------//
		// Get the list of RowNumbers //
		//----------------------------//
		/* No table conditions means update the whole table*/
		if(query.getWhereCondition() == null){
			for(Row row : table.getTable()){
				rowNumsToUpdate.add(new Integer(row.getRowNumber()));
			}
		}
		/* 
		 * If there are conditions 
		 * 1] Get a List of all the rows#'s to update 
		 */
		else{
			Condition ctn = query.getWhereCondition();
			rowNumsToUpdate = sortConditons(ctn.getLeftOperand(), ctn.getOperator(), ctn.getRightOperand(), tempTP);
			ArrayList<Row> rows = tempTP.getTable().getTable();
		}
		//---------------//
		// Sort the rows //
		//---------------//
		/* So far I have a list of rowNumbers
		 * If there are functions - then at most one row will be returned
		 * If there are no functions, then multiple rows could be returned
		 * 
		 */
		ArrayList<Row> finalRowList = new ArrayList<Row>();
		//Assuming no functions apply
		if(query.getFunctionMap().size() < 1){
			//------------------------//
			// [1] Check distinctness //
			//------------------------//
			ArrayList<Row> distinctResult = checkDistinct(query, rowNumsToUpdate, tempTP, false, null);
			//-------------------------//
			// [2] organize by orderBy //
			//-------------------------//
			if(query.getOrderBys().length >= 1) finalRowList = sortOrderBY(distinctResult, query.getOrderBys(), 0);
			else  finalRowList = distinctResult;
			//-------------------------//
			// [3] Get desired columns //
			//-------------------------//
			Row modRow = table.getModelRow();
			///* Get the I's /
			ArrayList<Integer> clmIs = new ArrayList<Integer>();
			ColumnID[] clmIDs = query.getSelectedColumnNames();
			//if it's supposed to get all rows
			if(clmIDs[0].getColumnName().equals("*")){
				//for(Column)
			}
			for(Column clm : modRow.getColumns()){
				for(ColumnID clmName : query.getSelectedColumnNames()){
					String colIDColName = clmName.getColumnName().toLowerCase();
					String colName = clm.getColumnName().toLowerCase();
					//if the columnNames match or the columnName list is "*" which means all
					if(colIDColName.equals(colName) || colIDColName.equals("*")) clmIs.add(clm.getI());
				}
			}
			//--------------------//
			// Prepare for result //
			//--------------------//
			ArrayList<SelectTable.OrderByRow> orderBYRows = new ArrayList<SelectTable.OrderByRow>();
			for(Row row : finalRowList){
				ArrayList<Column> columnsToAdd = new ArrayList<>();
				ArrayList<Column> rowsClms = row.getColumns();
				for(int Ival : clmIs){
					columnsToAdd.add(rowsClms.get(Ival));
				}
				orderBYRows.add(new SelectTable.OrderByRow(columnsToAdd));
			}
			SelectTable st = new SelectTable(orderBYRows);
			selTab = st;
			//System.out.println(st);
		}
		/* There are functions */
		else{
			ArrayList<SelectTable.FunctionColumn> functionColumns = new ArrayList<>();
			HashMap<ColumnID,FunctionInstance> functions = (HashMap<ColumnID, FunctionInstance>) query.getFunctionMap();
			for(Map.Entry<ColumnID, FunctionInstance> entry : functions.entrySet()){
				//--------//
				// Unwrap //
				//--------//
				String functionName = entry.getValue().function.toString();
				String columnName = entry.getKey().getColumnName();
				//----------------//
				// Check Distinct //
				//----------------//
				ColumnID[] clmID = {entry.getKey()};
				ArrayList<Row> distinctResult;
				if(entry.getValue().isDistinct){
					distinctResult = checkDistinct(query, rowNumsToUpdate, tempTP, true, clmID);
				}
				else distinctResult = checkDistinct(query, rowNumsToUpdate, tempTP, false, clmID);
				//---------------------//
				// Apply the function! //
				//---------------------//
				Double columnValue = doFunction(columnName, functionName, distinctResult);
				SelectTable.FunctionColumn tempFunCol = new SelectTable.FunctionColumn(columnValue, functionName, columnName);
				functionColumns.add(tempFunCol);
			}
			SelectTable st = new SelectTable(functionColumns, true);
			selTab = st;
			//System.out.println(st);
		}
		result = new ResultSet(tempTP, selTab);
		return result;
	}
	
	/**
	 * 	1. Find table
	 *	2. Get rows based on conditions
	 *	3. For each row, find column I1 and I2
	 *	4. Change the value to the new set values 
     *		1.  there should be no mistakes here, except if the value violates the 
     *     		1. Unique 
     *    		2. Not Null rules
	 * @param query
	 * @return
	 * @throws Exception 
	 */
	private ResultSet update(UpdateQuery query) throws Exception
	{
		ResultSet result = null;
		//-----------------------//
		// Get the tablePackage  //
		//-----------------------//
		String tableNameToUpdate = query.getTableName();
		TablePackage tempTP = null;
		for(TablePackage tp : tablePackages){
			if(tp.getTable().getTableName().toLowerCase().equals(tableNameToUpdate.toLowerCase())) tempTP = tp;
		}
		if(tempTP == null) throw new ProjectException("Couldn't find a table by the name: " + tableNameToUpdate); 
		Table table = tempTP.getTable();
		ArrayList<Integer> rowsNumsToUpdate = new ArrayList<Integer>();
		boolean tableWasChanged = false;
		//----------------------------//
		// Get the list of RowNumbers //
		//----------------------------//
		/* No table conditions means update the whole table*/
		if(query.getWhereCondition() == null){
			for(Row row : table.getTable()){
				rowsNumsToUpdate.add(new Integer(row.getRowNumber()));
			}
			tableWasChanged = true;
		}
		/* 
		 * If there are conditions 
		 * 1] Get a List of all the rows#'s to update 
		 */
		else{
			Condition ctn = query.getWhereCondition();
			rowsNumsToUpdate = sortConditons(ctn.getLeftOperand(), ctn.getOperator(), ctn.getRightOperand(), tempTP);
			if(!rowsNumsToUpdate.isEmpty()) tableWasChanged = true;
			ArrayList<Row> rows = tempTP.getTable().getTable();
		}
		//---------------------------------------//
		// Make sure Unique will not be violated //
		//---------------------------------------//
		/* We need to do this check separately from the one below, because the one below won't account for following rows */
		ColumnValuePair[] cvps = query.getColumnValuePairs();
		ArrayList<Column> modRowClms = table.getModelRow().getColumns();
		/* Get the I's of the columns to update - useful later */
		ArrayList<Integer> positionInRows = new ArrayList<Integer>();
		for(Column clm : modRowClms){
			for(ColumnValuePair cvp : cvps){
				//if this clm is going to be updated
				if(clm.getColumnName().toLowerCase().equals(cvp.getColumnID().getColumnName().toLowerCase())){
					if((clm.getCd().isUnique() || clm.isPrimaryKey()) && (rowsNumsToUpdate.size() > 1)) 
						throw new ProjectException("Update Error: Unique violation (In a Unique column (" + clm.getColumnName()
								+ "), you tried to set 2 different rows to the same value)");
					/* If the Unique isn't a problem - add this value's I number*/
					positionInRows.add(new Integer(clm.getI()));
				}
			}
		}
		//--------------------------------------------//
		// Make sure all the other rules are followed //
		//--------------------------------------------//
		Row tempRow = table.makeDuplicateModRow();
		//editing a row automatically checks that all it's rules should be followed
		boolean rowWasMadeCorrectly = tempRow.updateEdit(cvps, table.getTable());
		if(!rowWasMadeCorrectly) throw new ProjectException("Update Error: new values did not fit column rules (ex: string was too long...)");
		//------------------//
		// Update the table //
		//------------------//
		/* Remember we have a list of column I's from above - positionInRows
		 * (the order of clms should be the same as the order of the cvp's - but I won't rely on that)
		 * At this point - we can assume all the rules have been followed
		 */
		for(Row row : table.getTable()){
			ArrayList<Column> thisRow = row.getColumns();
			//make sure this row is a row to update
			if(rowsNumsToUpdate.contains(new Integer(row.getRowNumber()))){
				for(int pos : positionInRows){
					Column clm = thisRow.get(pos);
					String clmName = clm.getColumnName().toLowerCase();
					for(ColumnValuePair cvp : cvps){
						if(clmName.equals(cvp.getColumnID().getColumnName().toLowerCase())){
							boolean worked = clm.updateValue(cvp.getValue());
							if(!worked) throw new ProjectException("Update Error: Unexpected Update error. "
									+ "\nThe table might be faulty - i.e. part of it may have been update incorrectly before this Exception was thrown");
						}
					}
				}
			}
		}
		//------------------//
		// Update the Index //
		//------------------//
		/*
		 * I could have updated the Indices row by row above, but I thought it would be more efficient to do one run through
		 * the leaves of the table at the end instead or recursing the all the trees every time.
		 * 	1] Make a list of columnValuPair column names
		 *	2] for(Index ind : TablePackage.getBtrees()) if it's name is one on the above list
		 *	3] Use same method as delete did for getting rid of all references to this
		 *	4]  do a put on the btree, using (columnValuePai.getValue(), listOfRowsToUpdate)
		 */
		ArrayList<String> cvpClmNames = new ArrayList<String>();
		for(ColumnValuePair cvp : cvps) cvpClmNames.add(cvp.getColumnID().getColumnName().toLowerCase());
		Index[] tableBtrees = tempTP.getBtrees();
		for(Index tempInd : tableBtrees){
			//if this Index indexes one of the columns that was updated
			if(!(tempInd == null) && cvpClmNames.contains(tempInd.getColumnName().toLowerCase())){
				/* Step [1] Traverse the leaves and get rid of any references to rowsNumsToUpdate */
				//start with the bottomLeft most node
				BTree3 thisBtree = tempInd.getBtree();
				BTreeNode tempNode = thisBtree.getOriginalRoot();
				do{
					for(int i = 0; i < tempNode.size(); i++){
						if(!tempNode.get(i).getKey().equals("*")){
							ArrayList<Integer> value = (ArrayList<Integer>) tempNode.get(i).getValue();
							//remove stuff "safely"
							Iterator valItr = value.iterator();
							while(valItr.hasNext()){
								Integer tempInt = (Integer) valItr.next();
								if(rowsNumsToUpdate.contains(tempInt)) valItr.remove();;
							}
						}
					}
					tempNode = tempNode.getRightNode();
				}while(tempNode != null);
				/* After removing all references to rowsNumsToUpdate, add rowsNumsToUpdate to the index */
				//find the cvp key
				ColumnValuePair tempCvp = null;
				for(ColumnValuePair cvvp : cvps){
					if(cvvp.getColumnID().getColumnName().toLowerCase().equals(tempInd.getColumnName().toLowerCase())){
						tempCvp = cvvp;
					}
				}
				if(tempCvp == null) throw new ProjectException("Update Error: unable to locate columnValuePair for Index");
				//find out what type of value it should be
				ColumnDescription[] cds = table.getColumnDescriptions();
				String dataType = null;
				for(ColumnDescription cd : cds){
					if(cd.getColumnName().toLowerCase().equals(tempCvp.getColumnID().getColumnName().toLowerCase())){
						dataType = cd.getColumnType().toString();
					}
				}
				if(dataType == null) throw new ProjectException("Update Error: unable to determine update's Value type");
				String tempCVPVal = tempCvp.getValue();
				ArrayList<Integer> value = new ArrayList<Integer>();
				boolean finishedUpdate = false;
				switch(dataType){
				case "VARCHAR":
					value = (ArrayList<Integer>) thisBtree.get(tempCVPVal);
					if(value == null){
						thisBtree.put(tempCVPVal, rowsNumsToUpdate);
						finishedUpdate = true;
					}
					break;
				case "INT" : 
					value = (ArrayList<Integer>) thisBtree.get(new Integer(tempCVPVal));
					if(value == null){
						thisBtree.put(new Integer(tempCVPVal), rowsNumsToUpdate);
						finishedUpdate = true;
					}
					break;
				case "DECIMAL" : 
					value = (ArrayList<Integer>) thisBtree.get(new Double(tempCVPVal));
					if(value == null){
						thisBtree.put(new Double(tempCVPVal), rowsNumsToUpdate);
						finishedUpdate = true;
					}
					break;
				case "BOOLEAN" :
					value = (ArrayList<Integer>) thisBtree.get(new Boolean(tempCVPVal));
					if(value == null){
						thisBtree.put(new Boolean(tempCVPVal), rowsNumsToUpdate);
						finishedUpdate = true;
					}
					break;
				}
				if(!finishedUpdate){//Value was not == to null
					for(Integer integ : rowsNumsToUpdate){
						if(!value.contains(integ)) value.add(integ);
					}
				}
				
			}
		}
		if(!tableWasChanged) result = new ResultSet(tableWasChanged, tempTP);
		else result = new ResultSet(tempTP, true);
		return result;
	}
	
	//------------------//
	// Conditional code //
	//------------------//
	
	/** [Exceptions] If there is a mismatch when trying to check get values from a Btree or a table, it will throw an error
	  * which will be caught by the caller and rethrown indicating it was a deletion error
	  * 
	  * [Caller] The deletion caller will call this to get an ArrayList<Integer> of row#'s to delete.
	  * 
	  * There are two types of cases to deal with [1] Base cases [2] Higher cases:
	  * [1] Base Cases:
	  * 	In a base case, the following is true:
	  * 		- leftOp   = columnName;
	  * 		- rightOp  = comparisonValue;
	  * 		- operator = "=" "!=" ">" ">=" "<" "<=" //these will be stored as words (enums) ex: EQUALS
	  * 	Base cases process information in the following steps:
	  * 
	  * 	[a] Use a switch case to determine the operator. Each case will have the following structure (EQUALS as example):
	  * 		- case "EQUALS" : List<Integer> = EQUALS(left, right); break;
	  * 	[b] In the Operator method determine the method of lookup.
	  * 		[i] This column is indexed - Use the method to jump to the leaves (the specific implementation of this
	  * 			will depend on the operator - for example, equals will just start at the left, while x < 5 will start
	  * 			as close to 5 as possible and move left, while x > 5 will start as close to 5 as possible and move right).
	  * 			It will get add the list of rows for each key that matches the condition, to local temporary list.
	  * 		[ii] This column is not index - it will get the table's rows and check each row at position[I] for a correct
	  * 			 value. Then it will add that row to the temporary list.
	  * 	[c] Then return the temporary list of rows
	  * 
	  * [2] Higher Cases:
	  * 	In a Higher case, the following is true:
	  * 		- leftOp   = condition;
	  * 		- rightOp  = condition;
	  * 		- operator = "AND" or "OR"
	  * 	Higher cases process information in the following steps:
	  * 	
	  * 	[a] Check that both (leftOp.getClass().name() == Condition) && (rightOp.getClass().name() == Condition)
	  * 	[b] Recurse! 
	  * 		- ArrayList<Integer> leftList = sortConditons(leftOp.getLeft(), LeftOp.getOp(), LeftOp.getRigh());
	  * 		Do the same for the right
	  * 	[c] Using an if/else do the AND(left, right) or OR(left, right) methods.
	  * 		[i] AND - Find the larger size of the two lists. Then for(int i = 0; i < largerSize; i++)...
	  * 			check each value in the left list to see if it's in the right list. If not, remove it fromt he left 
	  * 			list. Ditto for the right list.
	  * 		[ii] OR -  Merge the two lists together
	  * 	[d] Return the resulting temporary List to the caller
	  * @throws Exception 
	  * 
	  * 
	  */
	 private <V extends Comparable> ArrayList<Integer> sortConditons(Object leftOp, Object operator, Object rightOp, TablePackage tabPa) throws Exception
	 {
		Table table = tabPa.getTable(); // This is the table object, it contains a table object which is a list of rows
		boolean isBaseCase = !(leftOp instanceof Condition); 
		ArrayList<Integer> resultList = new ArrayList<Integer>();
		
		//--------------//
		// Higher Cases //
		//--------------//
		if(!isBaseCase){
			Condition left = null;
			Condition right = null;
			if(leftOp instanceof Condition) left = (Condition) leftOp;
			if(rightOp instanceof Condition) right = (Condition) rightOp;
			if(left == null || right == null) throw new ProjectException("Condition error: left or right wasnt a conditions");
			ArrayList<Integer> leftList  = sortConditons(left.getLeftOperand(),left.getOperator(), left.getRightOperand(), tabPa);
			ArrayList<Integer> rightList = sortConditons(right.getLeftOperand(),right.getOperator(), right.getRightOperand(), tabPa);
			
			HashSet<Integer> tempHashInt = new HashSet<Integer>();
			/* Combine with an AND */
			if(operator.toString().equals("AND")){
				int bigger = Math.max(leftList.size(), rightList.size());
				for(int i = 0; i < bigger; i++){
					if((leftList.size() -1) >= i){
						Integer temp = leftList.get(i);
						if(rightList.contains(temp)) tempHashInt.add(temp);
					}
					if((rightList.size() -1) >= i){
						Integer temp = rightList.get(i);
						if(leftList.contains(temp)) tempHashInt.add(temp);
					}
				}
			}
			/* Combine with an OR */
			else{//It's an "OR"
				tempHashInt.addAll(leftList);
				tempHashInt.addAll(rightList);
			}
			resultList.addAll(tempHashInt);
		}
		//--------------//
		//  Base  Case  //
		//--------------//
		else if(isBaseCase){
			/* Unwrap the operands */
			ColumnID columID = (ColumnID) leftOp;
			String columnName = columID.getColumnName();//the left value
			String comparisonValueString = (String) rightOp.toString();
			V comparisonValue = null;//The right value
			/* Turn the right operand into it's value */
			//1] Get I and the cvt
			Row modRow = table.getModelRow();
			int I = -1;
			String columnValueType = null;
			for(Column clm : modRow.getColumns()){
				if(clm.getColumnName().toLowerCase().equals(columnName.toLowerCase())){
					I = clm.getI();
					columnValueType = "" + clm.getCd().getColumnType();
					break;
				}
			}
			if(I == -1) throw new ProjectException("Condition Error: Couldn't find I in the base case");
			if(columnValueType == null) throw new ProjectException("Condition Error: Couldn't find the column value type");

			//2] translate the value
			switch(columnValueType){
			case "VARCHAR": comparisonValue = (V) comparisonValueString;
			break;
			case "DECIMAL": comparisonValue = (V) new Double(comparisonValueString);
			break;
			case "INT": comparisonValue = (V) new Integer(comparisonValueString);
			break;	 
			case "BOOLEAN": comparisonValue = (V) new Boolean(comparisonValueString);
			break;	 
			}
			if(comparisonValue == null) throw new ProjectException("Condition Error: trouble converting right into the correct data type");
			/* Now We have both the left and right value */
			String opAsString = operator.toString();
			//set up the result
			resultList = determineBasedOnOp(tabPa, comparisonValue, I, opAsString);
		}
		
		return resultList;
	 }
	 
	 /**
	  * This method determines what type of operation should be used for checking
	  * 
	  * 
	  * @param tabPa - the tablePackage 
	  * @param columnName - 
	  * @param comparisonValue
	  * @param I - position in the row
	  * @param opAsString
	  * @return - an arrayList of row#'s - 
	  */
	 private <V extends Comparable> ArrayList<Integer> determineBasedOnOp(TablePackage tabPa, V comparisonValue, int I, String opAsString)
	 {
		 ArrayList<Integer> resultList = new ArrayList<Integer>();
		 Index btree = tabPa.getBtrees()[I];
		 boolean hasIndex = !(btree == null);		 
		 boolean equalsOrNotEquals = false;
		 
		 switch(opAsString){
		 case "=": resultList = equalsCompare(hasIndex, btree, tabPa, comparisonValue, true, I);
			 break;
		 case "<>": resultList = equalsCompare(hasIndex, btree, tabPa, comparisonValue, false, I);
			 break;	  
		 case ">": 
			 resultList = greaterThan(hasIndex, btree, tabPa, comparisonValue, false, I); 
			 break;	 
		 case ">=": resultList = greaterThan(hasIndex, btree, tabPa, comparisonValue, true, I); 
			 break;	
		 case "<": resultList = lessThan(hasIndex, btree, tabPa, comparisonValue, false, I); 
			 break;	 
		 case "<=": resultList = lessThan(hasIndex, btree, tabPa, comparisonValue, true, I); 
			 break;	  
		 } 
		 
		 return resultList;
	 }

	 /**
	  * If there is a btree, go to the bottom left node and go all the way
	  * to the right most node, all the while making comparisons 
	  * 
	  * If there is no btree, get the table and traverse each row. 
	  * 
	  * @param hasIndex
	  * @param btree
	  * @param tabPa
	  * @param comparisonValue
	  * @param checkForEquals - this determines if we're checking for equivalence or not
	  * 		True means  - check for equals
	  * 		False means - check for inequivalence
	  * @return
	  */
	 private <V extends Comparable> ArrayList<Integer> equalsCompare(boolean hasIndex, Index btree, TablePackage tabPa, V comparisonValue, boolean checkForEquals, int I)
	 {
		 boolean checkForUnequals = !checkForEquals;
		 ArrayList<Integer> resultList = new ArrayList<Integer>();
		 HashSet<Integer> tempSet = new HashSet<Integer>();
		 String compValStr = comparisonValue.toString().toLowerCase();
		 if(hasIndex){
			 BTree3 btree3 = btree.getBtree();
			 //start at the bottom left of the tree
			 BTreeNode tempNode = btree3.getOriginalRoot();
			 do{
				 for(int i = 0; i < tempNode.size(); i++){
					 BTreeMap tempMap = tempNode.get(i);
					 String tempMVal = tempMap.getKey().toString().toLowerCase();
					 /* if this map's key == the comparison value - add it's rows to the list */
					 if(tempMap.getKey().equals("*"));//do nothing - i.e. ignore the sentinel 
					 //refactor this to use compValStr
					 //String comparingStr = tempMap.getKey().toString().toLowerCase();
					 else if(((tempMVal.compareTo(comparisonValue.toString().toLowerCase()) == 0) ||
							 compareForNull(tempMVal, compValStr)) && checkForEquals){
						 tempSet.addAll( (ArrayList<Integer>) tempMap.getValue());
					 }
					 else if(!(tempMap.getKey().compareTo(comparisonValue) == 0 || compareForNull(tempMVal, compValStr)) && checkForUnequals){
						 tempSet.addAll( (ArrayList<Integer>) tempMap.getValue());
					 }
				 }
				 tempNode = tempNode.getRightNode();//prevent infinite looping
			 } while(tempNode != null);//as long as there is another node to the right
		 }
		 else{
			 /* Traverse the table */
			 ArrayList<Row> tempTable = tabPa.getTable().getTable();
			 for(int i = 0; i< tempTable.size(); i++){
				 Row row = tempTable.get(i);
				 Column clm = row.getColumns().get(I);
				 V clmVal = (V) clm.getValue();
				 if(clmVal != null){
					 /*
					  * I had to change from the commented out version to accommodate lower case string mismatching
					  */
					 //if((clmVal.compareTo(comparisonValue) == 0 || compareForNull(clmVal.toString().toLowerCase(), compValStr)) && checkForEquals) tempSet.add(new Integer(row.getRowNumber()));
					 if((clmVal.toString().toLowerCase().equals(compValStr) || compareForNull(clmVal.toString().toLowerCase(), compValStr)) && checkForEquals) tempSet.add(new Integer(row.getRowNumber()));
					 else if((clmVal.compareTo(comparisonValue) != 0  && !compareForNull(clmVal.toString().toLowerCase(), compValStr)) && checkForUnequals) tempSet.add(new Integer(row.getRowNumber()));
				 }
			 }
		 }
		 //convert the hashset into an ArrayList
		 resultList.addAll(tempSet);
		 return resultList;
	 }
	 
	 /*
	  * Compare for null values
	  */
	 private boolean compareForNull(String tempMVal, String compValStr)
	 {
		 boolean result = false;
		 if(compValStr.equals("null") || compValStr.equals("'null'")){
			 if(tempMVal.equals("'null'") || tempMVal.equals("null")) result = true;
		 }
		 return result;
	 }
	 
	 /**
	  * If it has an index - check using the index and it's findAt method.
	  * 1] 	Find the node to start from. Check that entire node for any values 
	  * 	such that: value > comapriosnValue.
	  * 2] 	Then add all the keys' values from all the right nodes (which by the 
	  * 	nature of a btree are greater than the comparison value)
	  * 
	  * @param hasIndex
	  * @param btree
	  * @param tabPa
	  * @param comparisonValue
	  * @param orEqualsTo - this indicates if this is doing a > or a >= check. 
	  * 		False means: >
	  * 		True  means: >=
	  * @param I
	  * @return
	  */
	 private <V extends Comparable> ArrayList<Integer> greaterThan(boolean hasIndex, Index btree, TablePackage tabPa, V comparisonValue, boolean orEqualsTo, int I)
	 {
		 ArrayList<Integer> resultList = new ArrayList<Integer>();
		 HashSet<Integer> tempSet = new HashSet<Integer>();
		 if(hasIndex){
			 BTree3 btree3 = btree.getBtree();
			 //start at the node that should contain the comparison value
			 BTreeNode tempNode = btree3.findAt((Comparable) comparisonValue);
			 /*
			  * I only need to do this once, after that, all the nodes to the right of 
			  * this node should be added by definition of the btree
			  */
			 for(int i = 0; i < tempNode.size(); i++){
				 BTreeMap tempMap = tempNode.get(i);
				 /* if this map's key > the comparison value - add it's rows to the list */
				 if((tempMap.getKey().compareTo(comparisonValue) > 0)){
					 tempSet.addAll( (ArrayList<Integer>) tempMap.getValue());
				 }
				 else if((tempMap.getKey().compareTo(comparisonValue) == 0) && orEqualsTo){
					 tempSet.addAll( (ArrayList<Integer>) tempMap.getValue());
				 }
			 }
			 tempNode = tempNode.getRightNode();//prevent infinite looping
			 while(tempNode != null){//as long as there is another node to the right
				 for(int i = 0; i < tempNode.size(); i++){
					 BTreeMap tempMap = tempNode.get(i);
					 ArrayList<Integer> tempArLi = (ArrayList<Integer>) tempMap.getValue();
					 tempSet.addAll(tempArLi);
				 }
				 tempNode = tempNode.getRightNode();//prevent infinite looping
			 }
		 }
		 else{
			 /* Traverse the table */
			 ArrayList<Row> tempTable = tabPa.getTable().getTable();
			 for(int i = 0; i< tempTable.size(); i++){
				 Row row = tempTable.get(i);
				 Column clm = row.getColumns().get(I);
				 V clmVal = (V) clm.getValue();
				 if(clmVal != null){
					 if(clmVal.compareTo(comparisonValue) > 0) tempSet.add(new Integer(row.getRowNumber()));
					 else if(clmVal.compareTo(comparisonValue) == 0 && orEqualsTo) tempSet.add(new Integer(row.getRowNumber()));
				 }
			 }
		 }
		 //convert the hashset into an ArrayList
		 resultList.addAll(tempSet);
		 return resultList;
	 }
	 
	 /**
	  * See greater than - this is the same basic idea, just the reverse
	  * @param hasIndex
	  * @param btree
	  * @param tabPa
	  * @param comparisonValue
	  * @param orEqualsTo
	  * @param I
	  * @return
	  */
	 private <V extends Comparable> ArrayList<Integer> lessThan(boolean hasIndex, Index btree, TablePackage tabPa, V comparisonValue, boolean orEqualsTo, int I)
	 {
		 ArrayList<Integer> resultList = new ArrayList<Integer>();
		 HashSet<Integer> tempSet = new HashSet<Integer>();
		 if(hasIndex){
			 BTree3 btree3 = btree.getBtree();
			 //start at the node that should contain the comparison value
			 BTreeNode tempNode = btree3.findAt((Comparable) comparisonValue);
			 /*
			  * I only need to do this once, after that, all the nodes to the right of 
			  * this node should be added by definition of the btree
			  */
			 for(int i = 0; i < tempNode.size(); i++){
				 BTreeMap tempMap = tempNode.get(i);
				 /* if this map's key > the comparison value - add it's rows to the list */
				 if((tempMap.getKey().compareTo(comparisonValue) < 0)){
					 tempSet.addAll( (ArrayList<Integer>) tempMap.getValue());
				 }
				 else if((tempMap.getKey().compareTo(comparisonValue) == 0) && orEqualsTo){
					 tempSet.addAll( (ArrayList<Integer>) tempMap.getValue());
				 }
			 }
			 tempNode = tempNode.getLeftNode();//prevent infinite looping
			 while(tempNode != null){//as long as there is another node to the right
				 for(int i = 0; i < tempNode.size(); i++){
					 BTreeMap tempMap = tempNode.get(i);
					 //avoid the sentinel 
					 if(!tempMap.getKey().equals("*")) tempSet.addAll( (ArrayList<Integer>) tempMap.getValue());
				 }
				 tempNode = tempNode.getLeftNode();//prevent infinite looping
			 }
		 }
		 else{
			 /* Traverse the table */
			 ArrayList<Row> tempTable = tabPa.getTable().getTable();
			 for(int i = 0; i< tempTable.size(); i++){
				 Row row = tempTable.get(i);
				 Column clm = row.getColumns().get(I);
				 V clmVal = (V) clm.getValue();
				 if(clmVal != null){
					 if(clmVal.compareTo(comparisonValue) < 0) tempSet.add(new Integer(row.getRowNumber()));
					 else if(clmVal.compareTo(comparisonValue) == 0 && orEqualsTo) tempSet.add(new Integer(row.getRowNumber()));
				 }
			 }
		 }
		 //convert the hashset into an ArrayList
		 resultList.addAll(tempSet);
		 return resultList;
	 }

	 //-------------//
	 // Select code //
	 //-------------//
	 
	 /**
	  * This method returns an ArrayList<Row> from the table. If it has to be Distinct it will be. 
	  * If not, it just gets all the relevant rows.
	  * 
	  * The parameters are that way to accommodate two different ways of entry
	  * @param query
	  * @param rowNumsToUpdate
	  * @param tempTP
	  * @return
	  * @throws ProjectException
	  */
	 private ArrayList<Row> checkDistinct(SelectQuery query, ArrayList<Integer> rowNumsToUpdate,
			 TablePackage tempTP, boolean distinctFromFunctions, ColumnID[] functionColumnID) throws ProjectException
	 {
		 Table table = tempTP.getTable();
		 ArrayList<Row> result = new ArrayList<Row>();
		 //------------------//
		 // get the I values //
		 //------------------//
		 ColumnID[] clmIds = (functionColumnID == null) ? query.getSelectedColumnNames() : functionColumnID;
		 Row modRow = table.getModelRow();
		 if(clmIds[0].getColumnName().equals("*")){//all rows are selected
			 ColumnID[] tempClmIDList = new ColumnID[modRow.getColumns().size()];
			 for(int i = 0 ; i < modRow.getColumns().size(); i++){
				 Column forNow = modRow.getColumns().get(i);
				 tempClmIDList[i] = new ColumnID(forNow.getColumnName(), null);
			 }
			 clmIds = tempClmIDList;
		 }
		 int[] Ivalue = new int[clmIds.length];
		 Arrays.fill(Ivalue, -1);//setup for error checking
		 int j = 0;
		 for(ColumnID clmID : clmIds){
			 for(Column clm : modRow.getColumns()){
				 if(clmID.getColumnName().toLowerCase().equals(clm.getColumnName().toLowerCase())){
					 Ivalue[j] = clm.getI();
					 j++;//only increment if I add a value
					 break;
				 }
			 } 
		 }
		 for(int i : Ivalue) if(i == -1) throw new ProjectException("Select Errror: Couldn't get I's properly");
		 /* Now We have the column I's */
		 if(query.isDistinct() || distinctFromFunctions){
			  for(Row row : table.getTable()){
				 //check if it's a selected row
				 if(rowNumsToUpdate.contains(new Integer(row.getRowNumber()))){
					 //--------------------------------------------------//
					 // check the list of rows added, for any duplicates //
					 //--------------------------------------------------//
					 /*
					  * For each row in the result list, check all relevant columns against the new row
					  * If any column fails - break out of the check and go onto the next row
					  */
					 boolean aColumnFailed = false;
					 for(Row alreadyAddedRow : result){
						 for(int i : Ivalue){
							 if(alreadyAddedRow.getColumns().get(i).getValue().toString().
									 equals(row.getColumns().get(i).getValue().toString())){
								 aColumnFailed = true;
								 break;//it's not distinct -> don't add the row
							 }
							 if(aColumnFailed) break;
						 }
						 if(aColumnFailed) break;
					 }
					 if(!aColumnFailed) result.add(row);
				 }
			 }
		 }
		 else{//it's not distinct
			 for(Row row : table.getTable()){
				 if(rowNumsToUpdate.contains(new Integer(row.getRowNumber()))) result.add(row);
			 }
		 }
		 return result;
	 }
	 
	 /**
	  * Here's the plan:
	  * 1] Unwrap information
	  * 2] put all the rows into a btree based on the select column's key - with a value of a list of rows that have that key
	  * 3] traverse the leaves of the tree 
	  * 	[a] if there is another level of depth call a recursive version of this method
	  * 	[b] if not, add these rows to the resultList - this will maintain the order
	  * 4] return resultList
	  * 
	  * Remember, NULL values come first
	  * @param distinctResult
	  * @return
	 * @throws Exception 
	  */
	 private <V> ArrayList<Row> sortOrderBY(ArrayList<Row> distinctResult, OrderBy[] orderBys, int timesDone) throws Exception
	 {
		 ArrayList<Row> resultList = new ArrayList<Row>();
		 //--------------------//
		 // Unwrap the orderBy //
		 //--------------------//
		 OrderBy thisLevelsOrderBy = orderBys[timesDone];
		 String orderByColumnName = thisLevelsOrderBy.getColumnID().getColumnName();
		 boolean isAscending = thisLevelsOrderBy.isAscending();
		 //----------------------------------//
		 // Determine the dataType and get I //
		 //----------------------------------//
		 int thisLevelColumnI = -1;
		 String thisLevelColumnDataType = null;
		 for(Column	clm : distinctResult.get(0).getColumns()){
			 if(clm.getColumnName().toLowerCase().equals(orderByColumnName.toLowerCase())){
				 thisLevelColumnI = clm.getI();
				 thisLevelColumnDataType = clm.getCd().getColumnType().toString();
				 break;
			 }
		 }
		 if(thisLevelColumnDataType.equals(null) || thisLevelColumnI == -1) throw new ProjectException("Select Error: OrderBy Error: couldn't find the column's (I || datatype)");
		 //----------------//
		 //	Set up to sort //
		 //----------------//
		 /*
		  * The way I sort, is I make a temporary btree, and traverse the leaves as though they were a sorted list
		  */
		 Row firstRow = distinctResult.get(0);
		 Column clmI = firstRow.getColumns().get(thisLevelColumnI);
		 ArrayList<Row> tempRowList = new ArrayList<Row>();
		 tempRowList.add(firstRow);
		 //------------------//
		 // Create the Btree //
		 //------------------//
		 BTree3 tempBtree = new BTree3((Comparable) clmI.getValue(), tempRowList);
		 //--------------------------------------------------//
		 // Add to the btree - i.e sort and group the values //
		 //--------------------------------------------------//
		 /* Logic borrowed from index above */
		 for(int i = 1; i < distinctResult.size(); i++){
			 Row tempRow = distinctResult.get(i);
			 Column tempClm = tempRow.getColumns().get(thisLevelColumnI);
			 ArrayList<Row> tempListRow = (ArrayList<Row>) tempBtree.get((Comparable) tempClm.getValue());
			 if(tempListRow == null){
				 ArrayList<Row> rowsToAdd = new ArrayList<Row>();
				 rowsToAdd.add(tempRow);
				 tempBtree.put((Comparable) tempClm.getValue(), rowsToAdd);
			 }
			 else{
				 tempListRow.add(tempRow);
			 }
		 }
		 //--------------------------------//
		 // Use the Btree as a sorted list //
		 //--------------------------------//
		 /*
		  * The Strategy:
		  * 1] Make a list of lists
		  * 2] Sort the original row list using the btree
		  * 3] At each leaf, get a list of the rows that that key contains. If that row needs more ordering, it will
		  * 4] The leaf result is stored in the list of lists
		  * 5] If the order is desc, the list of lists is reversed
		  * 6] The list of lists adds all of it's contents to the resultList
		  */
		 BTreeNode tempNode = tempBtree.getOriginalRoot();
		 ArrayList<ArrayList<Row>> rowListHolder = new ArrayList<ArrayList<Row>>();
		// boolean thereIsANull = false;
		 do{
			 for(int i = 0; i < tempNode.size(); i++){
				 BTreeMap btm = tempNode.get(i);
				 if(!btm.getKey().equals("*")){//ignore the sentinel 
					 //if(btm.getKey().toString().toLowerCase().equals("null") || btm.getKey().toString().toLowerCase().equals("'null'")) thereIsANull = true;
					 ArrayList<Row> sortedRow = (ArrayList<Row>) btm.getValue();
					 if((orderBys.length > (timesDone + 1))) sortedRow = sortOrderBY(sortedRow, orderBys, (timesDone+1));
					 rowListHolder.add(sortedRow);
					 //resultList.addAll(sortedRow);
				 }
			 }
			 tempNode = tempNode.getRightNode();
		 }while(tempNode != null);
		 //-------------------//
		 // Checking for Null //
		 //-------------------//
		 /* I haven't thoroughly tested this - but for now it works*/
		 if(timesDone == 0){
			 ArrayList<ArrayList<Row>> nullListOfListOfRows = new ArrayList<>(); 
			 ArrayList<ArrayList<Row>> notNullListOfListOfRows = new ArrayList<>();
			 for(ArrayList<Row> temporaryRowList : rowListHolder){
				 Column clm = temporaryRowList.get(0).getColumns().get(thisLevelColumnI);
				 String colKeyStr = clm.getValue().toString().toLowerCase();
				 if(colKeyStr.equals("null") || colKeyStr.equals("'null'")){
					 nullListOfListOfRows.add(temporaryRowList);
				 }
				 else notNullListOfListOfRows.add(temporaryRowList);
			 }
			 if(!(nullListOfListOfRows.size() < 1)){
				 rowListHolder = new ArrayList<>();
				 rowListHolder.addAll(nullListOfListOfRows);
				 rowListHolder.addAll(notNullListOfListOfRows);
			 }
		 }
		 if(!isAscending){
			 ArrayList<ArrayList<Row>> reverseRow = new ArrayList<ArrayList<Row>>();
			 for(int ii = (rowListHolder.size() - 1); ii >= 0; ii--) reverseRow.add(rowListHolder.get(ii));
			 rowListHolder = reverseRow;
		 }
		 for(ArrayList<Row> holder : rowListHolder) resultList.addAll(holder);
		 return resultList;
	 }

	 /**
	  * I chose the return type to be a double - for the chance that we're dealing with double's.
	  * If we're not - it doesn't hurt to convert ints to doubles.
	  * 
	  * Note: this might be off because I'm not sure I'm properly checking for null in checkDistinct
	  * 
	  * @param columnName
	  * @param functionName
	  * @param distinctResult
	  * @return
	  * @throws Exception
	  */
	 private Double doFunction(String columnName, String functionName, ArrayList<Row> distinctResult) throws Exception
	 {
		 Double result = new Double(-1);
		 int I = -1;
		 for(Column clm : distinctResult.get(0).getColumns()) 
			 if(clm.getColumnName().toLowerCase().equals(columnName.toLowerCase())) I = clm.getI();
		 if(I == - 1) throw new ProjectException("Select Error: Function Error: Couldn't get I");
		 
		 switch(functionName.toLowerCase()){
		 case "count":
			 Double count = new Double(0);
			 for(Row row : distinctResult){
				 Column clm = row.getColumns().get(I);
				 if(!(clm.getValue().toString().toLowerCase().equals("null") || clm.getValue().toString().toLowerCase().equals("'null'"))){
					 count++;
				 }
			 }
			 result = count;
			 break;
		 case "sum":
			 Double sum = new Double(0);
			 for(Row row : distinctResult){
				 Column clm = row.getColumns().get(I);
				 if(!(clm.getValue().toString().toLowerCase().equals("null"))){
					 Double newVal = new Double(clm.getValue().toString());
					 sum = sum + newVal;
				 }
			 }
			 result = sum;
			 break;
		 case "min":
			 Double min = new Double(0);
			 boolean initialized = false;
			 for(Row row : distinctResult){
				 Column clm = row.getColumns().get(I);
				 if(!(clm.getValue().toString().toLowerCase().equals("null"))){
					 Double newVal = new Double(clm.getValue().toString());
					 if(!initialized){
						 min = newVal;
						 initialized = true;
					 }
					 min = Math.min(min, newVal);
				 }
			 }
			 result = min;
			 break;
		 case "max":
			 Double max = new Double(0);
			 for(Row row : distinctResult){
				 Column clm = row.getColumns().get(I);
				 if(!(clm.getValue().toString().toLowerCase().equals("null"))){
					 Double newVal = new Double(clm.getValue().toString());
					 max = Math.max(max, newVal);
				 }
			 }
			 result = max;
			 break;
		 case "avg":
			 Double avg = new Double(0);
			 int counter = 0;
			 for(Row row : distinctResult){
				 Column clm = row.getColumns().get(I);
				 if(!(clm.getValue().toString().toLowerCase().equals("null"))){
					 Double newVal = new Double(clm.getValue().toString());
					 avg = avg + newVal;
					 counter++;
				 }
			 }
			 
			 result = avg/counter;
			 break;
		 }
		 if(result == -1) throw new ProjectException("Select Error: Function Error: Unecpected Error");
		 return result;
	 }
}
