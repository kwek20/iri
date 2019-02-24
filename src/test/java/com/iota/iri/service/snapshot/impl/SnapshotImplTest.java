package com.iota.iri.service.snapshot.impl;

import static org.junit.Assert.*;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.iota.iri.TransactionTestUtils;
import com.iota.iri.model.Hash;
import com.iota.iri.service.snapshot.Snapshot;
import com.iota.iri.service.snapshot.SnapshotMetaData;
import com.iota.iri.service.snapshot.SnapshotState;

public class SnapshotImplTest {
    
    private static SnapshotState state;
    
    private static SnapshotMetaData metaData;

    @Before
    public void setUp() throws Exception {
        state = new SnapshotStateImpl(new HashMap<>());
        metaData = new SnapshotMetaDataImpl(Hash.NULL_HASH, 1, 1l, new HashMap<>(), new HashMap<>());
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void skippedMilestoneTest() {
        Snapshot snapshot = new SnapshotImpl(state, metaData);
        assertTrue(snapshot.addSkippedMilestone(1));
        
        assertFalse(snapshot.addSkippedMilestone(1));
        assertTrue(snapshot.removeSkippedMilestone(1));
        assertFalse(snapshot.removeSkippedMilestone(1));
    }
    
    @Test
    public void updateTest() {
        Snapshot snapshot = new SnapshotImpl(state, metaData);
        snapshot.setIndex(0);
        snapshot.setHash(Hash.NULL_HASH);
        snapshot.setInitialTimestamp(1l);
        
        Snapshot newSnapshot = snapshot.clone();
        newSnapshot.setIndex(1);
        snapshot.setHash(TransactionTestUtils.getRandomTransactionHash());
        snapshot.setInitialTimestamp(5l);
        
        assertFalse(snapshot.equals(newSnapshot));
        snapshot.update(newSnapshot);
        assertTrue(snapshot.equals(newSnapshot));
    }
    
    @Test
    public void cloneTest() {
        Snapshot oldSnapshot = new SnapshotImpl(state, metaData);
        Snapshot newSnapshot = oldSnapshot.clone();
        
        // Equals on original is not true due to mockito
        assertTrue(oldSnapshot.equals(newSnapshot));
        
        oldSnapshot.addSkippedMilestone(1);
        
        // Clone shouldnt have the skipped milestone
        assertFalse(newSnapshot.removeSkippedMilestone(1));
        assertFalse(oldSnapshot.equals(newSnapshot));
    }
}
