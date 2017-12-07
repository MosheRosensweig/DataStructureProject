/**
 * Each BTreeMap has a key and a value
 * For this specific project (and most projects) My assumption is that
 * the key implements comparable.
 * 
 * The Key types I will need to support for this SQL project are:
 *  (1) String (2) int (3) double - all of which are comparable
 * @author mosherosensweig
 *
 */
public class BTreeMap<K extends Comparable, V> {
	
	private K key;
	private V value;
	
	public BTreeMap(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public void setValue(V value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return "\nBTreeMap key=" + key + ", value=" + value + "";
	}

	
}
