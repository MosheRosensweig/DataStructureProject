import java.util.ArrayList;

import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;

/**
 * The B-Tree class
 * @author MosheRosensweig
 *Last update: 4/27/17 12:28 am
 */
public class BTree3<K extends Comparable, V> {
	
	//use this for deleting
	private boolean deleting = false;
	
	private BTreeNode<K, V> rootNode;
	
	//Implemented based on Judah's in class explanation - I'm not sure I need them
	//they potentially are useful to enter the tree from the highest or lowest value
	private BTreeNode<K, V> originalRoot;
	private BTreeNode<K, V> highestNode;
		//use this for the index method
	private boolean usingAsIndexConditionally = false;
	
	public BTree3(K key, V value)
	{
		rootNode = new BTreeNode<K, V>(key, value, true);
		originalRoot = rootNode;
	}
	
	public void put(K key, V value)
	{
		BTreeMap<K, V> map = new BTreeMap<K, V>(key, value);
		put(rootNode, map); 
	}
	
	private BTreeNode<K, V> put(BTreeNode<K, V> root, BTreeMap<K, V> map)
	{
		BTreeNode<K, V> result = null;
		boolean didTheInternalRootNodeSplit = false;
		boolean isLeaf = !root.get(1).getValue().getClass().getName().contains("BTreeNode");
		/*Handle leaf nodes*/
		if(isLeaf){
			root.addToNode(map);
			BTreeNode<K, V> splitResult = null;
			if(root.isFull()) splitResult = split(root);
			result = splitResult;
		}
		
		/*Handle internal nodes*/
		else{
			BTreeMap correctMap = null;
			int rootSize = root.size();
			//find the right spot to put it
			for(int i = 1; i < rootSize; i++){
				//if this entry's key is bigger than the map's key
				if(root.get(i).getKey().compareTo(map.getKey()) > 0){
					//put it by the i-1 entry
					correctMap = root.get(i-1);
					break;
				}
				//if map got to the last element in the node and map's key > lastE's key
				if(i == rootSize-1) correctMap = root.get(i);
			}
			result = put( (BTreeNode) correctMap.getValue(), map);
		}
		
		//if there was a split
		if(result != null){
			if(!isLeaf){
				root.addToNode(result.get(0).getKey(), (V) result); 
				if(root.isFull()){
					result = split(root);
					if(root.isRoot()) didTheInternalRootNodeSplit = true;
				}
				else result = null;
			}
			//it's the rootNode
			if(root.isRoot() && (isLeaf || didTheInternalRootNodeSplit)){
				BTreeNode<K, V> tempRootNode = new BTreeNode<K, V>( (K)"*", (V) rootNode);
				tempRootNode.addToNode(result.get(0).getKey(), (V) result);
				rootNode.setIsRoot(false); //say that the old root is no longer the root
				tempRootNode.setIsRoot(true);
				rootNode = tempRootNode;   //make the new root  
			}
		}
		return result;
	}
	
	/**
	 * If the nodeToSplit is full, then the split method is called.
	 * This method creates a new node, shifts the upper m/2 maps into
	 * the lower m/2 spots in the new node and shifts the size of the
	 * original node to m/2
	 * @param nodeToSplit
	 * @return the new node - the one with the old node's upper m/2 elements
	 */
	private BTreeNode split(BTreeNode nodeToSplit)
	{
		int M = BTreeNode.getM();
		int mDivTwo = (M/2);
		BTreeNode result = new BTreeNode(nodeToSplit.get(mDivTwo));
		for(int i = mDivTwo + 1; i < M; i++){
			result.addToNode(nodeToSplit.get(i));
		}
		nodeToSplit.setSize((short) mDivTwo);
		
		//update pointers
		/*
		 * Check their pointers 
		 */
		BTreeNode tempOriginalRight;
		if(nodeToSplit.getRightNode() !=  null){
			tempOriginalRight = nodeToSplit.getRightNode();
			nodeToSplit.setRightNode(result);
			result.setLeftNode(nodeToSplit);
			result.setRightNode(tempOriginalRight);
			tempOriginalRight.setLeftNode(result);
		}
		else{
			nodeToSplit.setRightNode(result);
			result.setLeftNode(nodeToSplit);
		}
		//class pointer - I don't think I need this
		this.highestNode = result;
		
		//result.setSize((short) mDivTwo);
		return result;
	}
	
	/**
	 * Get the desired BTreeMap 
	 * @param key
	 * @return the Value of that key, if the key exists. Otherwise return null.
	 */
	public V get(K key)
	{
		return this.get(rootNode, key);
	}
 
	private V get(BTreeNode root, K key)
	{
		V result = null;
		boolean isLeaf = !root.get(1).getValue().getClass().getName().contains("BTreeNode");
		int rootSize = root.size();
		
		/*Handle leaf nodes*/
		//if it's a leaf node
		if(isLeaf){
			for(int i = 0; i < rootSize; i++){
				BTreeMap thisMap = root.get(i);
				if(!thisMap.getKey().equals("*") && thisMap.getKey().compareTo(key) == 0){
					result = (V) thisMap.getValue();
					//for deleting
					if(deleting){
						thisMap.setValue(null);
					}
					break;
				}
				else result = null;
			}
			/* Added this for Judah's conditional implementation - see method findAt()
			 * Basically, when using the btree - if I want to find all the values of (x >= n)
			 * I can just call the findAt(int) method to find the node where "x" is.
			 * Then I can check if this node has values I need, and using it's pointers right 
			 * and left, I can get all the values.
			 */
			if(usingAsIndexConditionally){
				return (V) root;
			}
		}
		
		/*Handle internal nodes*/
		else{//it's an internal node
			for(int i = 1; i < rootSize; i++){
				//I'm putting a cast (see below) - assuming I'm dealing with an internal node. 
				//This is ok because leaf nodes are handled beforehand
				
				BTreeMap thisMap = root.get(i);
				
				//if thisMap's key > searchKey
				if(thisMap.getKey().compareTo(key) > 0){ 
					result = this.get( (BTreeNode) root.get(i-1).getValue(), key);
					break;
				}
				//if it reaches the last element in the node and the key is still bigger than the other keys
				if(i == (root.size() - 1) && thisMap.getKey().compareTo(key) <= 0) 
					result = this.get((BTreeNode) thisMap.getValue(), key);
				}
			}
		return result;
		}

	public V delete(K key)
	{
		deleting = true;
		V result = get(key);
		deleting = false;
		return result;
	}
	
	/**
	 * This method allows me to find the node where "key" should be.
	 * This is useful for conditional stuff.
	 * if I'm looking for all values x >= n, I do findAt(x) which will
	 * return a BTreeNode. Then (since I'm looking for greater than) I 
	 * 1] Check all the values in the node to the right of "x"
	 * 2] do BTreeNode.getRightNode - and repeat until BTreeNode.getRightNode == null.
	 * The opposite would work for less than
	 * 
	 * @param key - the key you want to 
	 * @return
	 */
	public BTreeNode findAt(K key)
	{
		this.usingAsIndexConditionally = true;
		BTreeNode tempBTN = null;	
		tempBTN = (BTreeNode) get(key);
		this.usingAsIndexConditionally = false;
		return tempBTN;
	}
	
	public BTreeNode<K, V> getOriginalRoot() {
		return originalRoot;
	}

	public BTreeNode<K, V> getHighestNode() {
		return highestNode;
	}
	
	//------------------------------//
	//		  Testing Code			//
	//------------------------------//
	
	public void printNodesKeys()
	{
		printNodesKeys(rootNode);
	}
	private void printNodesKeys(BTreeNode<K, V> root)
	{
		if(root != null){
			//ArrayList<BTreeNode> fifo = new ArrayList<BTreeNode>();
			for(int i = 0; i < root.size(); i++){
				System.out.print(root.get(i).getKey() + ", ");
			}
			System.out.println("\n\non to the next node \n");
			for(int i = 0; i < root.size(); i++){
				if(root.get(i).getValue() != null && root.get(i).getValue().getClass().getName().equals("BTreeNode")){
					System.out.println("Now going to " + root.get(i).getKey() + "'s value");
					printNodesKeys((BTreeNode) root.get(i).getValue());
				}
			}
		}
		
	}
	
	private void printNodesKeys2(BTreeNode<K, V> root)
	{
		if(root != null){
			ArrayList<BTreeMap> fifo = new ArrayList<BTreeMap>();
			for(int i = 0; i < root.size(); i++){
				fifo.add(root.get(i));
				System.out.print(root.get(i).getKey() + ", ");
			}
			System.out.println("\n\non to the next node \n");
			for(BTreeMap temp : fifo){
				if(temp.getValue().getClass().getName().equals("BTreeNode")){
					System.out.println("Now going to " + temp.getKey() + "'s value");
					printNodesKeys2((BTreeNode) temp.getValue());
				}
				else System.out.println(temp.getValue());
			}
		}
		
	}
	
	
	public void printNodesKeysAndValues()
	{
		printNodesKeysAndValues(rootNode);
	}
	private void printNodesKeysAndValues(BTreeNode<K, V> root)
	{
		if(root != null){
			//ArrayList<BTreeNode> fifo = new ArrayList<BTreeNode>();
			for(int i = 0; i < root.size(); i++){
				System.out.print(root.get(i).getKey() + ", ");
			}
			System.out.print("\t\t Values = ");
			for(int i = 0; i < root.size(); i++){
				System.out.print("Key: " + root.get(i).getKey() + " ->" + root.get(i).getValue() + ", ");
			}
			System.out.println("\n\non to the next node \n");
			for(int i = 0; i < root.size(); i++){
				if(root.get(i).getValue() != null && root.get(i).getValue().getClass().getName().equals("BTreeNode")){
					System.out.println("Now going to " + root.get(i).getKey() + "'s value");
					printNodesKeys((BTreeNode) root.get(i).getValue());
				}
			}
		}
		
	}
}
