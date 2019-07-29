package com.iota.iri.storage;

/**
 * 
 * A data cache wraps around a storage object. It will keep information in its
 * memory for a while, preventing duplicate read/writes.
 *
 */
public interface DataCache {

    /**
     * Stops the cache, which causes a call to {@link #writeAll()} and a clean
     * afterwards.
     */
    void shutdown();

    /**
     * Writes all data stored in the cache to the {@link PersistenceProvider}
     * 
     * @throws CacheException If writing goes wrong
     */
    void writeAll() throws CacheException;

    /**
     * Checks the cache for this key. If the cache does not have this key, we try to
     * get it from our {@link PersistenceProvider}. Afterwards this will be added
     * using {@link #add(Indexable, Object)}.
     * 
     * @param <T>
     * 
     * @param key The key we are looking for
     * @return The value we cached
     * @throws CacheException If we couldn't find the value related to the key
     */
    // <T> T get(Class<T> model, Indexable index) throws Exception;

    /**
     * Checks if the cache contains this key. Does not check in the underlying
     * persistence.
     * 
     * @param key The key to check for
     * @return <code>true</code> if it is cached, otherwise <code>false</code>
     */
    // boolean contains(K key);
    
    /**
     * Add the key/value pair to the cache. If the cache is full after this add, the
     * cache will be cleaned before adding.
     * 
     * @param key   The key we are adding
     * @param value The value we add, related to the key
     * @return <code>true</code> if it was added to the cache
     * @throws CacheException If writing goes wrong
     */
    boolean add(Persistable key, Indexable value) throws CacheException;

    /**
     * Gets the maximum size of the cache.
     * 
     * @return The maximum size of the cache
     */
    int getMaxSize();
}
