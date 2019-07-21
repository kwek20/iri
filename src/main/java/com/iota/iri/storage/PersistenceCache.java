package com.iota.iri.storage;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
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
public class PersistenceCache implements PersistenceProvider, DataCache<Indexable, Persistable> {

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
    private ListOrderedMap<Indexable, Persistable> cache;

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

        cache = new ListOrderedMap<Indexable, Persistable>();

        // CacheSize divided by trytes to bytes conversion of size per transaction
        calculatedMaxSize = (int) Math
                .ceil(cacheSizeInBytes / (TransactionViewModel.SIZE * 3 * Math.log(3) / Math.log(2) / 8));
    }

    public void add(Indexable key, Persistable value) throws CacheException {
        synchronized (lock) {
            if (isFullAfterAdd()) {
                cleanUp();
            }

            cache.put(key, value);
        }
    }

    private void cleanUp() throws CacheException {
        log.debug("Cleaning cache...");
        try {
            List<Pair<Indexable, Persistable>> listBatch = null;
            try {
                listBatch = cache.entrySet().stream().limit(getNumEvictions()).map(entry -> {
                    return new Pair<Indexable, Persistable>(entry.getKey(), entry.getValue());
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
                cache.remove(pair.low, pair.hi);
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
            persistance.saveBatch(cache.entrySet().stream().map(entry -> {
                System.out.println(entry);
                return new Pair<Indexable, Persistable>(entry.getKey(), entry.getValue());
            }).collect(Collectors.toList()));
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
        if (!cache.containsKey(index)) {
            add(index, model);
        }
        return true;
    }

    @Override
    public boolean saveBatch(List<Pair<Indexable, Persistable>> models) throws Exception {
        synchronized (lock) {
            int test = 375273523;
            cache.putAll(models.stream().collect(Collectors.toMap(pair -> pair.low, pair -> pair.hi)));

            if (cache.size() >= calculatedMaxSize) {
                cleanUp();
            }
        }
        return true;
    }

    @Override
    public boolean update(Persistable model, Indexable index, String item) throws Exception {
        synchronized (lock) {
            if (cache.containsKey(index)) {
                cache.replace(index, model);
            } else {
                add(index, model);
            }
        }
        return true;
    }

    @Override
    public boolean exists(Class<?> model, Indexable key) throws Exception {
        return cache.containsKey(key) && cache.get(key).getClass().equals(model);
    }

    @Override
    public Pair<Indexable, Persistable> latest(Class<?> model, Class<?> indexModel) throws Exception {
        synchronized (lock) {
            for (Entry<Indexable, Persistable> entries : cache.entrySet()) {
                if (entries.getKey().getClass().equals(indexModel) && entries.getValue().getClass().equals(model)) {
                    return new Pair<Indexable, Persistable>(entries.getKey(), entries.getValue());
                }
            }
        }

        return null;
    }

    @Override
    public Persistable seek(Class<?> model, byte[] key) throws Exception {
        synchronized (lock) {
            for (Entry<Indexable, Persistable> entries : cache.entrySet()) {
                if (entries.getKey().bytes().equals(key) && entries.getValue().getClass().equals(model)) {
                    return entries.getValue();
                }
            }
        }

        return null;
    }

    @Override
    public Persistable get(Class<?> model, Indexable index) throws Exception {
        synchronized (lock) {
            for (Entry<Indexable, Persistable> entries : cache.entrySet()) {
                if (entries.getKey().equals(index) && entries.getValue().getClass().equals(model)) {
                    return entries.getValue();
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
        return cache.entrySet().parallelStream().filter(e -> e.getValue().getClass().equals(modelClass))
                .filter(e -> keyStartsWithValue(value, e.getKey().bytes())).map(e -> e.getKey())
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
        cache.entrySet().removeIf(entry -> entry.getKey().equals(index) && entry.getValue().getClass().equals(model));
    }

    @Override
    public void deleteBatch(Collection<Pair<Indexable, ? extends Class<? extends Persistable>>> models)
            throws Exception {
        OrderedMapIterator<Indexable, Persistable> it = cache.mapIterator();

        while (it.hasNext() && !models.isEmpty()) {
            Indexable next = it.next();
            Pair<Indexable, Class<? extends Persistable>> pair = new Pair<Indexable, Class<? extends Persistable>>(next,
                    it.getValue().getClass());
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
        for (Entry<Indexable, Persistable> entries : cache.entrySet()) {
            if (entries.getKey().getClass().equals(indexModel) && entries.getValue().getClass().equals(model)) {
                return new Pair<Indexable, Persistable>(entries.getKey(), entries.getValue());
            }
        }

        return null;
    }

    @Override
    public void clear(Class<?> column) throws Exception {
        cache.values().removeIf(value -> value.getClass().equals(column));
    }

    @Override
    public void clearMetadata(Class<?> column) throws Exception {

    }

    @Override
    public List<byte[]> loadAllKeysFromTable(Class<? extends Persistable> model) {
        return cache.entrySet().stream().filter(entry -> entry.getValue().getClass().equals(model))
                .map(entry -> entry.getKey().bytes()).collect(Collectors.toList());
    }

    /**
     * Not implemented as it is unused, returns <code>null</code>
     * 
     * {@inheritDoc}
     * 
     */
    @Override
    public Pair<Indexable, Persistable> next(Class<?> model, Indexable index) throws Exception {
        return null;
    }

    /**
     * Not implemented as it is unused, returns <code>null</code>
     * 
     * {@inheritDoc}
     * 
     */
    @Override
    public Pair<Indexable, Persistable> previous(Class<?> model, Indexable index) throws Exception {
        return null;
    }
}
