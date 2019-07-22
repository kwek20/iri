package com.iota.iri.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections4.OrderedMapIterator;
import org.apache.commons.collections4.map.ListOrderedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.utils.Pair;

/**
 * <p>
 * Persistence cache is a caching layer that goes over a
 * {@link PersistenceProvider}. When we request a value from the cache, we check
 * if it is already cached, and if it isn't, we ask the
 * {@link PersistenceProvider}.
 * </p>
 * <p>
 * When a value gets requested, which is already added, this value will not move
 * its position back in the front of the cache. Once the cache gets filled until
 * {@link #getMaxSize()}, we clean the oldest 5%.
 * </p>
 *
 * @param <T>
 */
public class PersistenceCache implements PersistenceProvider, DataCache {

    private static final Logger log = LoggerFactory.getLogger(PersistenceCache.class);

    /**
     * The percentage of the cache we clear when we are full. Must be at least 1%.
     */
    private static final int PERCENT_CLEAN = 5;

    private Object lock = new Object();

    /**
     * The persistence we use to save values once they are evicted.
     */
    private PersistenceProvider persistance;

    /**
     * ListOrdered cache, chosen because it has no extras, doesn't modify position
     * on read and on double-add.
     */
    private ListOrderedMap<Persistable, Indexable> cache;

    /**
     * Maximum size of the cache, based on the amount of transaction data we can fit
     * in the bytes passed in the constructor.
     */
    private final int calculatedMaxSize;

    /**
     * Creates a new instance of the cache.
     * 
     * @param persistance      The persistence we use to request values which are
     *                         not yet cached.
     * @param cacheSizeInBytes The size of the cache we want to maintain in memory,
     *                         in bytes.
     * @param persistableModel The model this cache persists.
     */
    public PersistenceCache(PersistenceProvider persistance, int cacheSizeInBytes) {

        this.persistance = persistance;

        cache = new ListOrderedMap<>();

        // CacheSize divided by trytes to bytes conversion of size per transaction
        calculatedMaxSize = (int) Math
                .ceil(cacheSizeInBytes / (TransactionViewModel.SIZE * 3 * Math.log(3) / Math.log(2) / 8));
    }

    @Override
    public void add(Persistable value, Indexable key) throws CacheException {
        synchronized (lock) {
            if (isFullAfterAdd()) {
                cleanUp();
            }

            cache.put(value, key);
        }
    }

    private void cleanUp() throws CacheException {
        log.debug("Cleaning cache...");
        try {
            List<Pair<Indexable, Persistable>> listBatch = null;
            try {
                listBatch = cache.entrySet().stream().limit(getNumEvictions()).map(entry -> {
                    return new Pair<Indexable, Persistable>(entry.getValue(), entry.getKey());
                }).collect(Collectors.toList());
            } catch (Exception e) {
                log.debug(e.getMessage());
                return;
            }

            // Write in batch to the database
            persistance.saveBatch(listBatch);

            // Then remove one by one
            for (Iterator<Pair<Indexable, Persistable>> iterator = listBatch.iterator(); iterator.hasNext();) {
                Pair<Indexable, Persistable> pair = iterator.next();
                cache.remove(pair.hi, pair.low);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isFullAfterAdd() {
        return cache.size() + 1 >= calculatedMaxSize;
    }

    private int getNumEvictions() {
        return (int) Math.ceil(calculatedMaxSize / 100.0 * PERCENT_CLEAN); // Clean up 5%
    }

    public int getMaxSize() {
        return calculatedMaxSize;
    }

    @Override
    public void init() throws Exception {
        // Nothing to do here
    }

    @Override
    public void writeAll() throws CacheException {
        try {
            List<Pair<Indexable, Persistable>> list = cache.entrySet().stream().map(entry -> {
                return new Pair<Indexable, Persistable>(entry.getValue(), entry.getKey());
            }).collect(Collectors.toList());

            persistance.saveBatch(list);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void shutdown() {
        try {
            writeAll();
        } catch (CacheException e) {
            e.printStackTrace();
        }
        cache.clear();
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public boolean save(Persistable model, Indexable index) throws Exception {
        if (!cache.containsKey(model)) {
            add(model, index);
        }
        return true;
    }

    @Override
    public boolean saveBatch(List<Pair<Indexable, Persistable>> models) throws Exception {
        synchronized (lock) {
            cache.putAll(models.stream().collect(Collectors.toMap(pair -> pair.hi, pair -> pair.low)));

            if (cache.size() >= calculatedMaxSize) {
                cleanUp();
            }
        }
        return true;
    }

    @Override
    public boolean update(Persistable model, Indexable index, String item) throws Exception {
        synchronized (lock) {
            if (cache.containsKey(model)) {
                cache.replace(model, index);
            } else {
                add(model, index);
            }
        }
        return true;
    }

    @Override
    public boolean exists(Class<?> model, Indexable key) throws Exception {
        if (!cache.containsValue(key)) {
            return false;
        }

        List<Persistable> keys = getAllKeysForValue(cache, key);
        for (Persistable persistable : keys) {
            if (persistable.getClass().equals(model)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Pair<Indexable, Persistable> latest(Class<?> model, Class<?> indexModel) throws Exception {
        synchronized (lock) {
            for (Entry<Persistable, Indexable> entry : cache.entrySet()) {
                if (entry.getValue().getClass().equals(indexModel) && entry.getKey().getClass().equals(model)) {
                    return new Pair<Indexable, Persistable>(entry.getValue(), entry.getKey());
                }
            }
        }

        return null;
    }

    @Override
    public Persistable seek(Class<?> model, byte[] key) throws Exception {
        synchronized (lock) {
            for (Entry<Persistable, Indexable> entries : cache.entrySet()) {
                if (Arrays.equals(entries.getValue().bytes(), key) && entries.getKey().getClass().equals(model)) {
                    return entries.getKey();
                }
            }
        }

        return null;
    }

    @Override
    public Persistable get(Class<?> model, Indexable index) throws Exception {
        synchronized (lock) {
            for (Entry<Persistable, Indexable> entries : cache.entrySet()) {
                if (entries.getValue().equals(index) && entries.getKey().getClass().equals(model)) {
                    return entries.getKey();
                }
            }
        }

        return null;
    }

    /**
     * 
     * {@inheritDoc}
     * 
     */
    @Override
    public Set<Indexable> keysStartingWith(Class<?> modelClass, byte[] value) {
        return cache.entrySet().parallelStream().filter(e -> e.getKey().getClass().equals(modelClass))
                .filter(e -> keyStartsWithValue(value, e.getValue().bytes())).map(e -> e.getValue())
                .collect(Collectors.toSet());
    }

    /**
     * @param value What we are looking for.
     * @param key   The bytes we are searching in.
     * @return true If the {@code key} starts with the {@code value}.
     */
    private static boolean keyStartsWithValue(byte[] value, byte[] key) {
        if (key == null || key.length < value.length) {
            return false;
        }
        for (int n = 0; n < value.length; n++) {
            if (value[n] != key[n]) {
                return false;
            }
        }
        return true;
    }

    /**
     * https://thispointer.com/java-how-to-get-keys-by-a-value-in-hashmap-search-by-value-in-map/
     * 
     * @param <K>
     * @param <V>
     * @param mapOfWords
     * @param value
     * @return
     */
    private static <K, V> List<K> getAllKeysForValue(Map<K, V> mapOfWords, V value) {
        List<K> listOfKeys = null;

        // Check if Map contains the given value
        if (mapOfWords.containsValue(value)) {
            // Create an Empty List
            listOfKeys = new ArrayList<>();

            // Iterate over each entry of map using entrySet
            for (Map.Entry<K, V> entry : mapOfWords.entrySet()) {
                // Check if value matches with given value
                if (entry.getValue().equals(value)) {
                    // Store the key from entry to the list
                    listOfKeys.add(entry.getKey());
                }
            }
        }
        // Return the list of keys whose value matches with given value.
        return listOfKeys;
    }

    /**
     * We do not store keys without
     * 
     * {@inheritDoc}
     */
    @Override
    public Set<Indexable> keysWithMissingReferences(Class<?> modelClass, Class<?> otherClass) throws Exception {

        return Collections.emptySet();
    }

    @Override
    public void delete(Class<?> model, Indexable index) throws Exception {
        cache.entrySet().removeIf(entry -> entry.getValue().equals(index) && entry.getKey().getClass().equals(model));
    }

    @Override
    public void deleteBatch(Collection<Pair<Indexable, ? extends Class<? extends Persistable>>> models)
            throws Exception {
        OrderedMapIterator<Persistable, Indexable> it = cache.mapIterator();

        while (it.hasNext() && !models.isEmpty()) {
            Persistable next = it.next();
            Pair<Indexable, Class<? extends Persistable>> pair = new Pair<Indexable, Class<? extends Persistable>>(
                    it.getValue(), next.getClass());
            if (models.contains(pair)) {
                it.remove();
                models.remove(pair);
            }
        }
    }

    /**
     * 
     * {@inheritDoc}
     * 
     * Will make a call to {@link #exists(Class, Indexable)}
     */
    @Override
    public boolean mayExist(Class<?> model, Indexable index) throws Exception {
        return exists(model, index);
    }

    @Override
    public long count(Class<?> model) throws Exception {
        return cache.valueList().stream().filter(p -> p.getClass().equals(model)).count();
    }

    @Override
    public Pair<Indexable, Persistable> first(Class<?> model, Class<?> indexModel) throws Exception {
        for (Entry<Persistable, Indexable> entry : cache.entrySet()) {
            if (entry.getValue().getClass().equals(indexModel) && entry.getKey().getClass().equals(model)) {
                return new Pair<Indexable, Persistable>(entry.getValue(), entry.getKey());
            }
        }

        return null;
    }

    @Override
    public void clear(Class<?> column) throws Exception {
        cache.keySet().removeIf(value -> value.getClass().equals(column));
    }

    @Override
    public void clearMetadata(Class<?> column) throws Exception {

    }

    @Override
    public List<byte[]> loadAllKeysFromTable(Class<? extends Persistable> model) {
        return cache.entrySet().stream().filter(entry -> entry.getKey().getClass().equals(model))
                .map(entry -> entry.getValue().bytes()).collect(Collectors.toList());
    }

    @Override
    public Pair<Indexable, Persistable> next(Class<?> model, Indexable index) throws Exception {
        /*
         * boolean found = false; for (Entry<Persistable, Indexable> entry :
         * cache.entrySet()) { if (entry.getValue().equals(index) &&
         * entry.getKey().getClass().equals(model)) { found = true; } else if (found &&
         * entry.getKey().getClass().equals(model)) { return new Pair<Indexable,
         * Persistable>(entry.getValue(), entry.getKey()); } }
         */

        for (Entry<Persistable, Indexable> entry : cache.entrySet()) {
            if (entry.getKey().getClass().equals(model) && entry.getValue().compareTo(index) == 1) {
                return new Pair<Indexable, Persistable>(entry.getValue(), entry.getKey());
            }
        }

        return null;
    }

    @Override
    public Pair<Indexable, Persistable> previous(Class<?> model, Indexable index) throws Exception {
        /*
         * boolean flip = false;
         * 
         * OrderedMapIterator<Persistable, Indexable> it = cache.mapIterator(); while
         * (flip ? it.hasPrevious() : it.hasNext()) { Persistable entry = flip ?
         * it.previous() : it.next(); if (it.getValue().equals(index) &&
         * entry.getClass().equals(model)) { flip = true; } else if (flip &&
         * entry.getClass().equals(model)) { return new Pair<Indexable,
         * Persistable>(it.getValue(), entry); } }
         */
        for (Entry<Persistable, Indexable> entry : cache.entrySet()) {
            if (entry.getKey().getClass().equals(model) && entry.getValue().compareTo(index) == -1) {
                return new Pair<Indexable, Persistable>(entry.getValue(), entry.getKey());
            }
        }

        return null;
    }
}
