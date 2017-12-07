/**
 * This class is effectively a wrapper on the BTree class
 * This holds the 
 * 		- Btree "index" for a certain column
 * 		- the name of this index
 * 		- the name of the column
 * 		- the number position of the column within a row
 * 
 * @author mosherosensweig
 * @version 5/8/17
 *
 */
public class Index {
	private BTree3 btree;
	private final String columnName;
	private final String indexName;
	private final int positionNumberInRow;
	
	public Index(BTree3 btree, String columnName, int positionNumberInRow, String indexName)
	{
		this.btree = btree;
		this.columnName = columnName;
		this.positionNumberInRow = positionNumberInRow;
		this.indexName = indexName;
	}

	public BTree3 getBtree() {
		return btree;
	}

	public String getColumnName() {
		return columnName;
	}

	public int getPositionNumberInRow() {
		return positionNumberInRow;
	}
	
	public String getIndexName()
	{
		return indexName;
	}
}
