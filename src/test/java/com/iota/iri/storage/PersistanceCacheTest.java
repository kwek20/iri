package com.iota.iri.storage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.iota.iri.TransactionTestUtils;
import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.model.Hash;
import com.iota.iri.model.persistables.Transaction;

public class PersistanceCacheTest {

    private static final int CACHE_SIZE = 10;

    private static final Hash A = TransactionTestUtils.getTransactionHash();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private PersistenceProvider persistence;

    private PersistenceCache cache;

    @Before
    public void setUp() {
        // Cache holds 10
        int amount = (int) (TransactionViewModel.SIZE * 3 * Math.log(3) / Math.log(2) / 8) * CACHE_SIZE;
        cache = new PersistenceCache(persistence, amount);
    }

    @After
    public void tearDown() {
        cache.shutdown();
    }

    @Test
    public void sizeTest() {
        assertEquals("Cache size should have been calculated based on TX size", CACHE_SIZE, cache.getMaxSize());
    }

    @Test
    public void saveWhenFullTest() throws Exception {
        fillCacheWithGarbage();

        Transaction tx = TransactionTestUtils.getTransaction();
        Mockito.when(persistence.get(Transaction.class, A)).thenReturn(tx);

        // This makes the cache full, so it cleans a percentage, we dont check the
        // amount cleaned
        cache.get(Transaction.class, A);

        verify(persistence, atLeast(1)).saveBatch(Mockito.any());
    }

    private void fillCacheWithGarbage() throws Exception {
        for (int i = 0; i < CACHE_SIZE; i++) {
            Transaction tx = TransactionTestUtils.getTransaction();
            Hash A = TransactionTestUtils.getTransactionHash();
            Mockito.when(persistence.get(Transaction.class, A)).thenReturn(tx);

            cache.add(tx, A);
        }
    }
}
