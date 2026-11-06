package com.oshanker.examples;

import java.util.Set;

public interface Cache<K, V> {

	Set<K> keySet();

	V put(K key, V value);

	V get(K key);

	boolean containsKey(K key);

}
