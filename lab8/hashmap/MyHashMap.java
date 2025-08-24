package hashmap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.HashSet;
import java.util.Set;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {

    private Collection<Node>[] buckets;
    private Set<K> keySet;        // 必须是 Set<K> 类型
    private int size;
    private double loadFactor;

    private int hash(K key) {
        return (key.hashCode() & 0x7fffffff) % buckets.length;
    }

    @Override
    public void clear() {
        buckets = createTable(16);  // 重置为默认大小
        size = 0;
        keySet.clear();
    }

    @Override
    public boolean containsKey(K key) {
        int index = hash(key);
        Collection<Node> bucket = buckets[index];

        for (Node node : bucket) {
            if (node.key.equals(key)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public V get(K key) {
        int index = hash(key);
        Collection<Node> bucket = buckets[index];

        for (Node node : bucket) {
            if (node.key.equals(key)) {
                return node.value;
            }
        }
        return null;
    }

    @Override
    public int size() {
        return size;
    }

    private void resize(int newSize) {
        Collection<Node>[] newBuckets = createTable(newSize);

        for (Collection<Node> bucket : buckets) {
            for (Node node : bucket) {
                int newIndex = (node.key.hashCode() & 0x7fffffff) % newSize;
                newBuckets[newIndex].add(node);
            }
        }

        buckets = newBuckets;
    }

    @Override
    public void put(K key, V value) {
        if ((double) size / buckets.length > loadFactor) {
            resize(buckets.length * 2);
        }

        int index = hash(key);
        Collection<Node> bucket = buckets[index]; // 此时bucket可能为null

        // +++ 新增的检查逻辑 +++
        if (bucket == null) {
            bucket = new ArrayList<>(); // 或 LinkedList<>(), 根据你的需求定
            buckets[index] = bucket; // 将新初始化的桶放回数组
        }
        // +++ 检查逻辑结束 +++

        // 移除有问题的第83行的 add
        // bucket.add(new Node(key, value)); // <-- 注释掉或删除这一行

        for (Node node : bucket) {
            if (node.key.equals(key)) {
                node.value = value;
                return;
            }
        }
        // 现在这个 add 是安全的，因为bucket肯定不是null
        bucket.add(new Node(key, value));
        keySet.add(key);
        size++;
    }

    @Override
    public Set<K> keySet() {
        return (Set<K>) keySet;
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        return null;
    }

    @Override
    public Iterator<K> iterator() {
        return (Iterator<K>) keySet.iterator();
    }

    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    // You should probably define some more!

    /** Constructors */
    public MyHashMap() {
        this(16, 0.75);
    }

    public MyHashMap(int initialSize) {
        this(initialSize, 0.75);
    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param loadFactor maximum load factor
     */
    //public MyHashMap(int initialSize, double maxLoad) { }
    @SuppressWarnings("unchecked")
    public MyHashMap(int initialSize, double loadFactor) {
        buckets = (Collection<Node>[]) new Collection[initialSize];
        keySet = new HashSet<>();
        size = 0;
        this.loadFactor = loadFactor;
    }
    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return null;
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() {
        return null;
    }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table = new Collection[tableSize];
        for (int i = 0; i < tableSize; i++) {
            table[i] = createBucket();
        }
        return table;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

}
