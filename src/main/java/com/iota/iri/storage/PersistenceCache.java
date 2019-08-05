package com.iota.iri.storage;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.model.StateDiff;
import com.iota.iri.model.persistables.Approvee;
import com.iota.iri.utils.Pair;
import com.iota.iri.utils.datastructure.CuckooFilter;
import com.iota.iri.utils.datastructure.impl.CuckooFilterImpl;

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

    /**
     * The persistence we use to save values once they are evicted.
     */
    private PersistenceProvider persistance;

    /**
     * ListOrderedMap cache, chosen because it has no extras, doesn't modify
     * position on read and on double-add.
     */
    private Map<byte[], Pair<Indexable, Persistable>> cache;

    /**
     * Maximum size of the cache, based on the amount of transaction data we can fit
     * in the bytes passed in the constructor.
     */
    private final int calculatedMaxSize;
    
    private CuckooFilter filter;

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

        // CacheSize divided by trytes to bytes conversion of size per transaction
        calculatedMaxSize = (int) Math
                .ceil(cacheSizeInBytes / (TransactionViewModel.SIZE * 3 * Math.log(3) / Math.log(2) / 8));
        
        cache = new ConcurrentHashMap<>(calculatedMaxSize);
        filter = new CuckooFilterImpl(calculatedMaxSize);
    }
    
    private boolean shouldAdd(Persistable value, Indexable key) {
        if (value.merge() || !value.exists()) {
            // These are recalculated every time!
            return false;
        }

        return true;
    }
    
    private boolean possiblyContain(Class<?> model) {
        return !model.equals(Approvee.class) && !model.equals(StateDiff.class);
    }

    @Override
    public boolean add(Persistable value, Indexable key) throws CacheException {
        if (!shouldAdd(value, key) || !possiblyContain(value.getClass())) {
            return false;
        } 
        
        synchronized (this) {
            byte[] filterBytes = filterBytes(key, value.getClass());
            cache.put(filterBytes, new Pair<>(key, value));
            filter.add(filterBytes);
        }
        
        if (isFull()) {
            cleanUp();
        }
        
        return true;
    }
    
    private byte[] filterBytes(Indexable key, Class<?> model) {
        return filterBytes(key.bytes(), model);
    }
    
    private byte[] filterBytes(byte[] bytes, Class<?> model) {
        byte[] hash = ByteBuffer.allocate(4).putInt(model.hashCode()).array();
        byte[] filterBytes = new byte[bytes.length + 4];

        System.arraycopy(hash, 0, filterBytes, 0, 4);
        System.arraycopy(bytes, 0, filterBytes, 4, bytes.length);
        return bytes;
    }

    private void cleanUp() throws CacheException {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                synchronized (PersistenceCache.this) {
                    if (!isFull()) {
                        return ;
                    }
                    
                    try {
                        Map<byte[], Pair<Indexable, Persistable>> listBatch = null;
                        try {
                            listBatch = cache.entrySet().stream().limit(getNumEvictions()).collect(Collectors.toMap(
                                    entry -> entry.getKey(), 
                                    entry -> entry.getValue()));
                            
                        } catch (Exception e) {
                            log.debug(e.getMessage());
                            return;
                        }
                
                        persistance.saveBatch(listBatch.values());
    
                        // Then remove one by one
                        for (Iterator<byte[]> iterator = listBatch.keySet().iterator(); iterator.hasNext();) {
                            byte[] pair = iterator.next();
                            filter.delete(pair);
                            cache.remove(pair);    
                        }
                        
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        });
    }
    
    private boolean isFull() {
        return cache.size() >= calculatedMaxSize;
    }

    private int getNumEvictions() {
        return (int) Math.ceil(calculatedMaxSize / 100.0 * PERCENT_CLEAN) + (cache.size() - calculatedMaxSize); // Clean up 5%;
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
            persistance.saveBatch(cache.values());
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

    /**
     * As there is no init, we are always available
     * 
      * {@inheritDoc}
     */
    @Override
    public boolean isAvailable() {
        return true;
    }

    /**
     * We don't save because saving is always a result from getting, which doesn't mean its useful in caching
      * {@inheritDoc}
     */
    @Override
    public boolean save(Persistable model, Indexable index) throws Exception {
        return true;
    }
    
    /**
     * We don't save because saving is always a result from getting, which doesn't mean its useful in caching
      * {@inheritDoc}
     */
    @Override
    public boolean saveBatch(Collection<Pair<Indexable, Persistable>> models) throws Exception {
        return true;
    }

    /**
     * Updates the model, or adds it if we didn't have it yet.
     * 
      * {@inheritDoc}
     */
    @Override
    public boolean update(Persistable model, Indexable index, String item) throws Exception {
        if (!exists(model.getClass(), index)) {
            add(model, index);
        } else {
            cache.replace(filterBytes(index, model.getClass()), new Pair<>(index, model));
        }
        
        return true;
    }

    /**
     * Check if the cache contains this pair. Has a 3% false positive rate.
      * {@inheritDoc}
     */
    @Override
    public boolean exists(Class<?> model, Indexable key) throws Exception {
        return cache.containsKey(filterBytes(key, model));
    }

    /**
     * Expensive lookup over each cache entry
     * 
      * {@inheritDoc}
     */
    @Override
    public Pair<Indexable, Persistable> latest(Class<?> model, Class<?> indexModel) throws Exception {
        for (Pair<Indexable, Persistable> entry : cache.values()) {
            if (entry.low.getClass().equals(indexModel) && entry.hi.getClass().equals(model)) {
                return entry;
            }
        }
        
        return null;
    }

    @Override
    public Persistable seek(Class<?> model, byte[] key) throws Exception {
        if (!cache.containsKey(filterBytes(key, model))){
            return null;
        }
        
        for (Pair<Indexable, Persistable> entry : cache.values()) {
            if (Arrays.equals(entry.low.bytes(), key) && entry.hi.getClass().equals(model)) {
                return entry.hi;
            }
        }

        return null;
    }

    @Override
    public Persistable get(Class<?> model, Indexable index) throws Exception {
        byte[] bytes = filterBytes(index, model);
        Pair<Indexable, Persistable> p;
        return !possiblyContain(model) ? null : (p = cache.get(bytes)) != null ? p.hi : null;
    }

    /**
     * 
     * {@inheritDoc}
     * 
     */
    @Override
    public Set<Indexable> keysStartingWith(Class<?> modelClass, byte[] value) {
        return cache.entrySet().parallelStream().filter(e -> e.getValue().hi.getClass().equals(modelClass))
                .filter(e -> keyStartsWithValue(value, e.getValue().low.bytes())).map(e -> e.getValue().low)
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
     * <a href="https://thispointer.com/java-how-to-get-keys-by-a-value-in-hashmap-search-by-value-in-map/">source</a>
     * 
     * @param <K>
     * @param <V>
     * @param map
     * @param value
     * @return
     */
    private static <K, V> List<K> getAllKeysForValue(Map<K, V> map, V value) {
        List<K> listOfKeys = null;

        // Check if Map contains the given value
        if (map.containsValue(value)) {
            // Create an Empty List
            listOfKeys = new ArrayList<>();

            // Iterate over each entry of map using entrySet
            for (Map.Entry<K, V> entry : map.entrySet()) {
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

    @Override
    public void delete(Class<?> model, Indexable index) throws Exception {
        byte[] bytes = filterBytes(index, model);
        cache.remove(bytes);
        //filter.delete(bytes);
    }

    @Override
    public void deleteBatch(Collection<Pair<Indexable, ? extends Class<? extends Persistable>>> models)
            throws Exception {
        Iterator<Entry<byte[], Pair<Indexable, Persistable>>> it = cache.entrySet().iterator();

        while (it.hasNext() && !models.isEmpty()) {
            Entry<byte[], Pair<Indexable, Persistable>> next = it.next();
            Pair<Indexable, Class<? extends Persistable>> pair = new Pair<Indexable, Class<? extends Persistable>>(
                    next.getValue().low, next.getValue().hi.getClass());
            if (models.contains(pair)) {
                cache.remove(next.getKey());
                filter.delete(next.getKey());
                models.remove(pair);
            }
        }
    }

    /**
     * 
     * {@inheritDoc}
     * 
     */
    @Override
    public boolean mayExist(Class<?> model, Indexable index) throws Exception {
        return exists(model, index);
    }

    @Override
    public long count(Class<?> model) throws Exception {
        return cache.values().stream().filter(p -> p.hi.getClass().equals(model)).count();
    }

    @Override
    public Pair<Indexable, Persistable> first(Class<?> model, Class<?> indexModel) throws Exception {
        for (Pair<Indexable, Persistable> entry : cache.values()) {
            if (entry.low.getClass().equals(indexModel) && entry.hi.getClass().equals(model)) {
                return new Pair<Indexable, Persistable>(entry.low, entry.hi);
            }
        }
        
        return null;
    }

    @Override
    public void clear(Class<?> column) throws Exception {
        cache.values().removeIf(value -> value.low.getClass().equals(column));
    }

    @Override
    public List<byte[]> loadAllKeysFromTable(Class<? extends Persistable> model) {
        return cache.values().stream().filter(entry -> entry.hi.getClass().equals(model))
            .map(entry -> entry.hi.bytes()).collect(Collectors.toList());
        
    }

    @Override
    public Pair<Indexable, Persistable> next(Class<?> model, Indexable index) throws Exception {
        for (Pair<Indexable, Persistable> entry : cache.values()) {
            if (entry.hi.getClass().equals(model) && entry.low.compareTo(index) == 1) {
                return new Pair<Indexable, Persistable>(entry.low, entry.hi);
            }
        }
        
        return null;
    }

    @Override
    public Pair<Indexable, Persistable> previous(Class<?> model, Indexable index) throws Exception {
        for (Pair<Indexable, Persistable> entry : cache.values()) {
            if (entry.hi.getClass().equals(model) && entry.low.compareTo(index) == -1) {
                return new Pair<Indexable, Persistable>(entry.low, entry.hi);
            }
        }
        return null;
    }

    @Override
    public void clearMetadata(Class<?> column) throws Exception {
        // TODO
    }

    /**
     * We do not store keys without
     * 
     * {@inheritDoc}
     */
    @Override
    public Set<Indexable> keysWithMissingReferences(Class<?> modelClass, Class<?> otherClass) throws Exception {
        // Should we?
        return Collections.emptySet();
    }
}
