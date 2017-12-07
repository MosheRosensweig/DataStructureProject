import java.util.ArrayList;
import java.util.Arrays;

/**
 * Each node has "m" BTreeMaps
 * @author mosherosensweig
 *Last update: 4/27/17 12:28 am
 */
public class BTreeNode<K extends Comparable, V> {
	private static final short M = 6;
	private BTreeMap<K, V>[] node;
	private short size; 
	//This also tells how many elements are in the node. Ex: when it points to 0, there are 0 elements in the node
	private boolean isRoot = false;
	//Implemented based on Judah's in class explanation
	private BTreeNode leftNode;
	private BTreeNode rightNode;
	
	
	//--------------//
	// Constructors //
	//--------------//
	
	/**
	 * Regular node constructor
	 * @param key
	 * @param value
	 */
	public BTreeNode(K key, V value)
	{
		node = new BTreeMap[M];
		size = 0;
		addToNode(key, value);
	}
	
	public BTreeNode(BTreeMap nodeToAdd)
	{
		node = new BTreeMap[M];
		size = 0;
		addToNode(nodeToAdd);
	}
	
	/**
	 * Root node constructor
	 * @param key
	 * @param value
	 * @param isRoot - if it's true, make this a root node
	 */
	public BTreeNode(K key, V value, boolean isRoot2)
	{
		node = new BTreeMap[M];
		size = 0;
		if(isRoot2){
			node[size] = (BTreeMap<K, V>) new BTreeMap<String, String>("*", "");
			size++;
			this.isRoot = true;
		}
		addToNode(key, value);
	}
	
	//-------------//
	//   Methods   //
	//-------------//
	
	//TODO remove this method - well 1st check if it's used
	/**
	 * This should be called by the BTree class when it (1) modifies an internal node 
	 * or (2) when it modifies a leaf node
	 * @param generic - the key
	 * @param generic - the value
	 */
	public void addToNode(K key, V value)
	{
		BTreeMap<K, V> newMap = new BTreeMap<K, V>(key, value);
		addToNode2(newMap);
	}
	
	public void addToNode(BTreeMap newMap)
	{
		addToNode2(newMap);
	}
	
	private void addToNode2(BTreeMap newMap)
	{
		if(size == 0) node[size] = newMap;
		else{
			for(int i = 0; i < size; i++){
				//if newMap's key is smaller than the current map
				if(!this.get(i).getKey().equals("*") && this.get(i).getKey().compareTo(newMap.getKey()) > 0){
					for(int j = size-1; j >= i; j--){
						node[j+1] = node[j];
					}
					node[i] = newMap;
					break;
				}
				
				if(!this.get(i).getKey().equals("*") && this.get(i).getKey().compareTo(newMap.getKey()) == 0){
					node[i].setValue((V) newMap.getValue());
					size--; // countering the automatic count below
					break;
				}
				//if it got to the last element and didn't break, the new key must be larger than the current key
				if(i == size-1) node[size] = newMap;
			}
		}
		size++;
	}
	
	/**
	 * Check to see if the node is full
	 * @return if the node is full
	 * The node is full (for now) if all (M-1) spaces are used up
	 */
	public boolean isFull()
	{
		boolean result = false;
		if(this.node[M-1] != null && size == M) result = true;
		return result;
	}
	
	/**
	 * Return the map at the position given. 
	 * @param num - position within the node of the desired map to be returned
	 * @return - if the number is valid, return the node. If the number is too big or tooo small
	 *  return null.
	 */
	public BTreeMap<K, V> get(int num)
	{
		if((num > size) || (num < 0)) return null;
		return node[num];
	}
	
	/**
	 * Put the map at the given index
	 * @param num - the index to put it at
	 * @param newMap - the map to add
	 * @return the BTreeMap being evicted
	 * 
	 * (Use this method when splitting a node)
	 */
	private BTreeMap<K, V> put(int num, BTreeMap<K, V> newMap)
	{
		BTreeMap<K, V> result = node[num];
		node[num] = newMap;
		return result;
	}
	
	/**
	 * Get the number of elements in the node
	 * @return the number of elements in the node
	 */
	public int size()
	{
		return size;
	}
	
	/**
	 * Use this method when splitting a node
	 * @param newSize
	 */
	public void setSize(short newSize)
	{
		size = newSize;
	}
	
	public static short getM()
	{
		return M;
	}
	
	public boolean isRoot()
	{
		return isRoot;
	}
	
	public void setIsRoot(boolean value)
	{
		isRoot = value;
	}

	public BTreeNode getLeftNode() {
		return leftNode;
	}

	public BTreeNode getRightNode() {
		return rightNode;
	}

	public void setLeftNode(BTreeNode leftNode) {
		this.leftNode = leftNode;
	}

	public void setRightNode(BTreeNode rightNode) {
		this.rightNode = rightNode;
	}

	@Override
	public String toString() {
		String hu = "";
		for(int i = 0; i < size; i++){
			hu = hu + node[i].toString();
		}
		return "BTreeNode:\n" + hu + "\n";
	}
	
	
}
