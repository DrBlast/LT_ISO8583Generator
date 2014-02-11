package ru.kvaga.amphora.svfp.iso8583.lt.loadgenerator.util;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: u_m0llb
 * Date: 13.12.13
 * Time: 16:45
 */
public class CollectionsPlus<K, V> {


	/**
	 * Sort map by value
	 *
	 * @param map
	 * @return
	 */
	public static <K, V extends Comparable<V>> Map<K, V> sortMapByValue(Map<K, V> map) {
		List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
			public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
				return (o2.getValue().compareTo(o1.getValue()));
			}
		});

		Map<K, V> result = new LinkedHashMap<K, V>();
		for (Iterator<Map.Entry<K, V>> it = list.iterator(); it.hasNext(); ) {
			Map.Entry<K, V> entry = it.next();
			result.put(entry.getKey(), entry.getValue());
		}
		return result;
	}
}