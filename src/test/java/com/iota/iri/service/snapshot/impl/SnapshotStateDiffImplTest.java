package com.iota.iri.service.snapshot.impl;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.iota.iri.TransactionTestUtils;
import com.iota.iri.model.Hash;

public class SnapshotStateDiffImplTest {
    
    private static Hash A = TransactionTestUtils.getRandomTransactionHash();
    private static Hash B = TransactionTestUtils.getRandomTransactionHash();
    private static Hash C = TransactionTestUtils.getRandomTransactionHash();
    
    @Test
    public void getBalanceChanges() {
        SnapshotStateDiffImpl stateDiff = new SnapshotStateDiffImpl(Collections.EMPTY_MAP);
        Map<Hash, Long> change = stateDiff.getBalanceChanges();
        change.put(A, 1l);
        assertNotEquals(stateDiff.getBalanceChanges().size(), change.size());
    }

    @Test
    public void isConsistent() {
        SnapshotStateDiffImpl stateDiff = new SnapshotStateDiffImpl(new HashMap<Hash, Long>(){{
            put(A, 1l);
            put(B, 5l);
            put(C, -6l);
        }});
        assertTrue(stateDiff.isConsistent());
        
        stateDiff = new SnapshotStateDiffImpl(Collections.EMPTY_MAP);
        assertTrue(stateDiff.isConsistent());
        
        stateDiff = new SnapshotStateDiffImpl(new HashMap<Hash, Long>(){{
            put(A, 1l);
            put(B, 5l);
        }});
        
        assertFalse(stateDiff.isConsistent());
    }
}
