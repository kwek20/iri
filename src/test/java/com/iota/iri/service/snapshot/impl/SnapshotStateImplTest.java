package com.iota.iri.service.snapshot.impl;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.iota.iri.TransactionTestUtils;
import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.model.Hash;
import com.iota.iri.service.snapshot.SnapshotException;
import com.iota.iri.service.snapshot.SnapshotState;
import com.iota.iri.service.snapshot.SnapshotStateDiff;

public class SnapshotStateImplTest {
    
    private static final Hash A = TransactionTestUtils.getRandomTransactionHash();
    
    private static Map<Hash, Long> map = new HashMap<Hash, Long>(){{
        put(Hash.NULL_HASH, TransactionViewModel.SUPPLY - 10);
        put(A, 10l);
    }};
    
    private static Map<Hash, Long> inconsistentMap = new HashMap<Hash, Long>(){{
        put(Hash.NULL_HASH, 5l);
        put(A, -10l);
    }};

    private SnapshotStateImpl state;
    private SnapshotStateImpl balanceState;

    @Before
    public void setUp() throws Exception {
        state = new SnapshotStateImpl(new HashMap<>());
        balanceState = new SnapshotStateImpl(map);
    }

    @Test
    public void testGetBalance() {
        assertNull(balanceState.getBalance(null));
        
        long balance = balanceState.getBalance(Hash.NULL_HASH);
        assertEquals(TransactionViewModel.SUPPLY - 10l, balance);
        
        balance = balanceState.getBalance(A);
        assertEquals(10l, balance);
    }

    @Test
    public void testGetBalances() {
        assertEquals(state.getBalances(), new HashMap<>());
        assertEquals(balanceState.getBalances(), map);
    }

    @Test
    public void testIsConsistent() {
        assertTrue(state.isConsistent());
        assertTrue(balanceState.isConsistent());
        
        SnapshotStateImpl inconsistentState = new SnapshotStateImpl(inconsistentMap);
        assertFalse(inconsistentState.isConsistent());
    }

    @Test
    public void testHasCorrectSupply() {
        assertFalse(state.hasCorrectSupply());
        assertTrue(balanceState.hasCorrectSupply());
        
        SnapshotStateImpl inconsistentState = new SnapshotStateImpl(inconsistentMap);
        assertFalse(inconsistentState.hasCorrectSupply());
    }

    @Test
    public void testUpdate() {
        assertNotEquals(state, balanceState);
        state.update(balanceState);
        assertEquals(state, balanceState);
    }

    @Test
    public void testApplyStateDiff() throws SnapshotException {
        Map<Hash, Long> map = new HashMap<>();
        map.put(Hash.NULL_HASH, 5l);
        map.put(A, -5l);
        
        SnapshotStateDiff diff = new SnapshotStateDiffImpl(map);
        state.applyStateDiff(diff);
        
        long balance = state.getBalance(Hash.NULL_HASH);
        assertEquals(5l, balance);
        
        balance = state.getBalance(A);
        assertEquals(-5l, balance);
    }
    
    @Test(expected = SnapshotException.class)
    public void testApplyStateDiffThrowsException() throws SnapshotException {
        SnapshotStateDiff diff = new SnapshotStateDiffImpl(inconsistentMap);
        state.applyStateDiff(diff);
    }

    @Test
    public void testPatchedState() {
        SnapshotStateDiff diff = new SnapshotStateDiffImpl(map);
        SnapshotState patchedState = state.patchedState(diff);
        
        assertEquals(patchedState, balanceState);
        
        Map<Hash, Long> map = new HashMap<>();
        map.put(Hash.NULL_HASH, 5l);
        map.put(A, -5l);
        
        diff = new SnapshotStateDiffImpl(map);
        patchedState = balanceState.patchedState(diff);
        
        long balance = patchedState.getBalance(Hash.NULL_HASH);
        assertEquals(TransactionViewModel.SUPPLY - 5l, balance);
        
        balance = patchedState.getBalance(A);
        assertEquals(5, balance);
    }
}
