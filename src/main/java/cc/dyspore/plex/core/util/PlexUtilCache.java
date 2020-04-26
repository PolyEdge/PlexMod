package cc.dyspore.plex.core.util;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PlexUtilCache<K, V> {
    private static final int DEFAULT_MAX = 25;

    private List<Map.Entry<K, V>> cache = new ArrayList<>();
    private int max = DEFAULT_MAX;
    private boolean referenceComparison = false;

    public PlexUtilCache(int max, boolean referenceComparison) {
        this.setMax(max);
        this.setReferenceComparison(referenceComparison);
    }

    public PlexUtilCache(int max) {
        this(max, false);
    }

    public PlexUtilCache(boolean referenceComparison) {
        this(DEFAULT_MAX, referenceComparison);
    }

    public PlexUtilCache() {
        this(DEFAULT_MAX, false);
    }


    public int getMax() {
        return this.max;
    }

    public boolean getReferenceComparison() {
        return this.referenceComparison;
    }

    public void setMax(int max) {
        this.max = max;
    }

    public void setReferenceComparison(boolean referenceComparison) {
        this.referenceComparison = referenceComparison;
    }

    private boolean compare(K key1, K key2) {
        return this.referenceComparison ? key1 == key2 : key1 != null && key1.equals(key2);
    }

    public void clean() {
        while (this.cache.size() > this.max) {
            this.cache.remove(0);
        }
    }

    public void clear() {
        this.cache.clear();
    }

    public V get(K key) {
        for (Map.Entry<K, V> entry : this.cache) {
            if (this.compare(key, entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public V put(K key, V value) {
        for (int i = 0; i < this.cache.size(); i++) {
            Map.Entry<K, V> entry = this.cache.get(i);
            if (this.compare(key, entry.getKey())) {
                this.cache.remove(entry);
                i--;
            }
        }
        this.cache.add(new AbstractMap.SimpleEntry<>(key, value));
        this.clean();
        return value;
    }
}
