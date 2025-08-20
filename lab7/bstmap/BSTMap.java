package bstmap;

import java.util.Iterator;
import java.util.Set;
import java.util.HashSet;

public class BSTMap <K extends Comparable<K>, V> implements Map61B<K, V> {
    private BSTNode root;
    private int size;

    @Override
    /*no need*/
    public Iterator<K> iterator() {
        return null;
    }

    private class BSTNode {
        K key;
        V value;
        BSTNode left, right;

        BSTNode(K k, V v) {
            key = k;
            value = v;
        }
    }

    @Override
    /* Removes all of the mappings from this map. */
    public void clear() {
        root = null;
        size = 0;
    }

    @Override
    /* Returns true if this map contains a mapping for the specified key. */
    public boolean containsKey(K key) {
        return containsKey(root, key);
    }

    private boolean containsKey(BSTNode node, K key) {
        if (node == null) return false;
        int cmp = key.compareTo(node.key);
        if (cmp < 0) return containsKey(node.left, key);
        else if (cmp > 0) return containsKey(node.right, key);
        else return true;  // 只要键存在即返回true，无论值是否为null
    }

    @Override
    /* Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    public V get(K key) {
        return get(root, key);
    }

    private V get(BSTNode node, K key) {
        if (node == null) return null;
        int cmp = key.compareTo(node.key);
        if (cmp < 0) return get(node.left, key);
        else if (cmp > 0) return get(node.right, key);
        else return node.value;
    }

    @Override
    /* Returns the number of key-value mappings in this map. */
    public int size() {
        return size;
    }

    @Override
    /* Associates the specified value with the specified key in this map. */
    public void put(K key, V value) {
        root = put(root, key, value);
        size++;
    }

    private BSTNode put(BSTNode node, K key, V value) {
        if (node == null) return new BSTNode(key, value);
        int cmp = key.compareTo(node.key);
        if (cmp < 0) node.left = put(node.left, key, value);
        else if (cmp > 0) node.right = put(node.right, key, value);
        else node.value = value; // 键已存在则更新值
        return node;
    }

    @Override
    /*no need*/
    /* Returns a Set view of the keys contained in this map. Not required for Lab 7.
     * If you don't implement this, throw an UnsupportedOperationException. */
    public Set<K> keySet() {
        return Set.of();
    }

    /*no need*/
    @Override
    /* Removes the mapping for the specified key from this map if present.
     * Not required for Lab 7. If you don't implement this, throw an
     * UnsupportedOperationException. */
    public V remove(K key) {
        return null;
    }

    @Override
    /* Removes the entry for the specified key only if it is currently mapped to
     * the specified value. Not required for Lab 7. If you don't implement this,
     * throw an UnsupportedOperationException.*/
    public V remove(K key, V value) {
        return null;
    }
}
