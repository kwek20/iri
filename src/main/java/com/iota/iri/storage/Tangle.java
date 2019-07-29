package com.iota.iri.storage;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.iota.iri.model.Hash;
import com.iota.iri.model.HashFactory;
import com.iota.iri.model.StateDiff;
import com.iota.iri.model.persistables.Address;
import com.iota.iri.model.persistables.Approvee;
import com.iota.iri.model.persistables.Bundle;
import com.iota.iri.model.persistables.Milestone;
import com.iota.iri.model.persistables.ObsoleteTag;
import com.iota.iri.model.persistables.Tag;
import com.iota.iri.model.persistables.Transaction;
import com.iota.iri.utils.Pair;
import com.iota.iri.zmq.MessageQueueProvider;

/**
 * Created by paul on 3/3/17 for iri.
 */
public class Tangle {
    private static final Logger log = LoggerFactory.getLogger(Tangle.class);

    public static final Map<String, Class<? extends Persistable>> COLUMN_FAMILIES = new LinkedHashMap<String, Class<? extends Persistable>>() {
        {
            put("transaction", Transaction.class);
            put("milestone", Milestone.class);
            put("stateDiff", StateDiff.class);
            put("address", Address.class);
            put("approvee", Approvee.class);
            put("bundle", Bundle.class);
            put("obsoleteTag", ObsoleteTag.class);
            put("tag", Tag.class);
        }
    };

    public static final Map.Entry<String, Class<? extends Persistable>> METADATA_COLUMN_FAMILY = new AbstractMap.SimpleImmutableEntry<>(
            "transaction-metadata", Transaction.class);

    private final List<PersistenceProvider> persistenceProviders = new ArrayList<>();
    private final List<MessageQueueProvider> messageQueueProviders = new ArrayList<>();

    private PersistenceCache cache;

    public void setCache(PersistenceCache cache) {
        this.cache = cache;
    }

    public void addPersistenceProvider(PersistenceProvider provider) {
        this.persistenceProviders.add(provider);
    }

    /**
     * Adds {@link com.iota.iri.storage.MessageQueueProvider} that should be
     * notified.
     * 
     * @param provider that should be notified.
     */
    public void addMessageQueueProvider(MessageQueueProvider provider) {
        this.messageQueueProviders.add(provider);
    }

    public void init() throws Exception {
        if (null != cache) {
            cache.init();
        }

        for (PersistenceProvider provider : this.persistenceProviders) {
            provider.init();
        }
    }

    public void shutdown() throws Exception {
        if (null != cache) {
            log.info("Shutting down Tangle data caching... ");
            this.cache.shutdown();
        }

        log.info("Shutting down Tangle Persistence Providers... ");
        this.persistenceProviders.forEach(PersistenceProvider::shutdown);
        this.persistenceProviders.clear();
        log.info("Shutting down Tangle MessageQueue Providers... ");
        this.messageQueueProviders.forEach(MessageQueueProvider::shutdown);
        this.messageQueueProviders.clear();
    }

    public Persistable load(Class<?> model, Indexable index) throws Exception {
        Persistable out = null;
        if (null != cache && null != (out = cache.get(model, index))) {
            return out;
        }

        for (PersistenceProvider provider : this.persistenceProviders) {
            if ((out = provider.get(model, index)) != null) {
                break;
            }
        }

        if (null != cache && null != out) {
            cache.add(out, index);
        }
        return out;
    }

    public Boolean saveBatch(List<Pair<Indexable, Persistable>> models) throws Exception {
        if (null != cache) {
            //return cache.saveBatch(models);
        }

        boolean exists = false;
        for (PersistenceProvider provider : persistenceProviders) {
            if (exists) {
                provider.saveBatch(models);
            } else {
                exists = provider.saveBatch(models);
            }
        }
        return exists;
    }

    public Boolean save(Persistable model, Indexable index) throws Exception {
        if (null != cache) {
            if (cache.save(model, index)) {
                return true;
            }
        }

        boolean exists = false;
        for (PersistenceProvider provider : persistenceProviders) {
            if (exists) {
                provider.save(model, index);
            } else {
                exists = provider.save(model, index);
            }
        }
        return exists;
    }

    public void deleteBatch(Collection<Pair<Indexable, ? extends Class<? extends Persistable>>> models)
            throws Exception {
        if (null != cache) {
            cache.deleteBatch(models);
        }

        for (PersistenceProvider provider : persistenceProviders) {
            provider.deleteBatch(models);
        }
    }

    public void delete(Class<?> model, Indexable index) throws Exception {
        if (null != cache) {
            cache.delete(model, index);
        }

        for (PersistenceProvider provider : persistenceProviders) {
            provider.delete(model, index);
        }
    }

    public Pair<Indexable, Persistable> getLatest(Class<?> model, Class<?> index) throws Exception {
        Pair<Indexable, Persistable> latest = null;
        if (null != cache && null != (latest = cache.latest(model, index))) {
            return latest;
        }

        for (PersistenceProvider provider : persistenceProviders) {
            if (latest == null) {
                latest = provider.latest(model, index);
            }
        }

        if (null != cache && null != latest && null != latest.hi) {
            cache.add(latest.hi, latest.low);
        }
        return latest;
    }

    /**
     * Updates all {@link PersistenceProvider} and publishes message to all
     * {@link com.iota.iri.storage.MessageQueueProvider}.
     *
     * @param model with transaction data
     * @param index {@link Hash} identifier of the {@link Transaction} set
     * @param item  identifying the purpose of the update
     * @throws Exception when updating the {@link PersistenceProvider} fails
     */
    public void update(Persistable model, Indexable index, String item) throws Exception {
        if (null != cache) {
            cache.update(model, index, item);
        }

        updatePersistenceProvider(model, index, item);
        updateMessageQueueProvider(model, index, item);
    }

    private void updatePersistenceProvider(Persistable model, Indexable index, String item) throws Exception {
        for (PersistenceProvider provider : this.persistenceProviders) {
            provider.update(model, index, item);
        }
    }

    private void updateMessageQueueProvider(Persistable model, Indexable index, String item) {
        for (MessageQueueProvider provider : this.messageQueueProviders) {
            provider.publishTransaction(model, index, item);
        }
    }

    /**
     * Notifies all registered {@link com.iota.iri.storage.MessageQueueProvider} and
     * publishes message to MessageQueue.
     *
     * @param message that can be formatted by
     *                {@link String#format(String, Object...)}
     * @param objects that should replace the placeholder in message.
     * @see com.iota.iri.zmq.ZmqMessageQueueProvider#publish(String, Object...)
     * @see String#format(String, Object...)
     */
    public void publish(String message, Object... objects) {
        for (MessageQueueProvider provider : this.messageQueueProviders) {
            provider.publish(message, objects);
        }
    }

    public Set<Indexable> keysWithMissingReferences(Class<?> modelClass, Class<?> referencedClass) throws Exception {
        // We dont store missing references in the cache

        Set<Indexable> output = null;
        for (PersistenceProvider provider : this.persistenceProviders) {
            output = provider.keysWithMissingReferences(modelClass, referencedClass);
            if (output != null && output.size() > 0) {
                break;
            }
        }
        return output;
    }

    public Set<Indexable> keysStartingWith(Class<?> modelClass, byte[] value) {
        Set<Indexable> output = new HashSet<Indexable>();
        if (null != cache) {
            output.addAll(cache.keysStartingWith(modelClass, value));
        }

        for (PersistenceProvider provider : this.persistenceProviders) {
            output.addAll(provider.keysStartingWith(modelClass, value));
        }

        return output.size() > 0 ? output : null;
    }

    public <T extends Indexable> List<T> loadAllKeysFromTable(Class<? extends Persistable> modelClass,
            Function<byte[], T> transformer) {
        List<byte[]> keys = new LinkedList<byte[]>();
        if (null != cache) {
            keys.addAll(cache.loadAllKeysFromTable(modelClass));
        }

        for (PersistenceProvider provider : this.persistenceProviders) {
            keys.addAll(provider.loadAllKeysFromTable(modelClass));
        }

        return keys.size() > 0 ? keys.stream().map(transformer).collect(Collectors.toList()) : null;
    }

    public Boolean exists(Class<?> modelClass, Indexable hash) throws Exception {
        if (null != cache && cache.exists(modelClass, hash)) {
            return true;
        }

        for (PersistenceProvider provider : this.persistenceProviders) {
            if (provider.exists(modelClass, hash)) {
                return true;
            }
        }
        return false;
    }

    public Boolean maybeHas(Class<?> model, Indexable index) throws Exception {
        if (null != cache && cache.mayExist(model, index)) {
            return true;
        }

        for (PersistenceProvider provider : this.persistenceProviders) {
            if (provider.mayExist(model, index)) {
                return true;
            }
        }
        return false;
    }

    public Long getCount(Class<?> modelClass) throws Exception {
        long value = null != cache ? cache.count(modelClass) : 0;

        for (PersistenceProvider provider : this.persistenceProviders) {
            value += provider.count(modelClass);
        }
        return value;
    }

    public Persistable find(Class<?> model, byte[] key) throws Exception {
        Persistable out = null;
        if (null != cache && null != (out = cache.seek(model, key))) {
            return out;
        }

        for (PersistenceProvider provider : this.persistenceProviders) {
            if ((out = provider.seek(model, key)) != null) {
                break;
            }
        }

        if (null != cache && null != out) {
            cache.add(out, HashFactory.GENERIC.create(model, key));
        }
        return out;
    }

    public Pair<Indexable, Persistable> next(Class<?> model, Indexable index) throws Exception {
        Pair<Indexable, Persistable> latest = null;
        if (null != cache && null != (latest = cache.next(model, index))) {
            return latest;
        }

        for (PersistenceProvider provider : persistenceProviders) {
            if (latest == null) {
                latest = provider.next(model, index);
            }
        }

        if (null != cache && null != latest && null != latest.hi) {
            cache.add(latest.hi, latest.low);
        }
        return latest;
    }

    public Pair<Indexable, Persistable> previous(Class<?> model, Indexable index) throws Exception {
        Pair<Indexable, Persistable> latest = null;
        if (null != cache && null != (latest = cache.previous(model, index))) {
            return latest;
        }

        for (PersistenceProvider provider : persistenceProviders) {
            if (latest == null) {
                latest = provider.previous(model, index);
            }
        }

        if (null != cache && null != latest && null != latest.hi) {
            cache.add(latest.hi, latest.low);
        }
        return latest;
    }

    public Pair<Indexable, Persistable> getFirst(Class<?> model, Class<?> indexModel) throws Exception {
        Pair<Indexable, Persistable> latest = null;
        if (null != cache && null != (latest = cache.first(model, indexModel))) {
            return latest;
        }


        for (PersistenceProvider provider : persistenceProviders) {
            if (latest == null) {
                latest = provider.first(model, indexModel);
            }
        }
        
        if (null != cache && null != latest && null != latest.hi) {
            cache.add(latest.hi, latest.low);
        }
        return latest;
    }

    public void clearColumn(Class<?> column) throws Exception {
        for (PersistenceProvider provider : persistenceProviders) {
            provider.clear(column);
        }
    }

    public void clearMetadata(Class<?> column) throws Exception {
        for (PersistenceProvider provider : persistenceProviders) {
            provider.clearMetadata(column);
        }
    }
}
