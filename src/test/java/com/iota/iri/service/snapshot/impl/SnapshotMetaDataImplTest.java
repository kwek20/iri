package com.iota.iri.service.snapshot.impl;

import static org.junit.Assert.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.iota.iri.TransactionTestUtils;
import com.iota.iri.conf.BaseIotaConfig;
import com.iota.iri.model.Hash;
import com.iota.iri.service.snapshot.SnapshotMetaData;

public class SnapshotMetaDataImplTest {

    private static Hash A = TransactionTestUtils.getRandomTransactionHash();
    private static Hash B = TransactionTestUtils.getRandomTransactionHash();
    private static Hash C = TransactionTestUtils.getRandomTransactionHash();
    
    private static Map<Hash, Integer> solidEntryPoints = new HashMap<Hash, Integer>(){{
        put(A, 1);
        put(B, 2);
        put(C, -1);
    }};
    
    private static Map<Hash, Integer> seenMilestones = new HashMap<Hash, Integer>(){{
        put(A, 10);
        put(B, 11);
        put(C, 12);
    }};
    
    private SnapshotMetaDataImpl meta;

    @Before
    public void setUp() {
        meta = new SnapshotMetaDataImpl(A, 
                BaseIotaConfig.Defaults.MILESTONE_START_INDEX, 
                BaseIotaConfig.Defaults.GLOBAL_SNAPSHOT_TIME, 
                solidEntryPoints, 
                seenMilestones);
    }
    
    @Test
    public void initialIndexTest(){
        assertEquals(meta.getInitialIndex(), BaseIotaConfig.Defaults.MILESTONE_START_INDEX);
        assertEquals(meta.getIndex(), BaseIotaConfig.Defaults.MILESTONE_START_INDEX);
        
        meta.setIndex(BaseIotaConfig.Defaults.MILESTONE_START_INDEX + 1);
        assertNotEquals(meta.getInitialIndex(), meta.getIndex());
    }
    
    @Test
    public void initialTimestampTest(){
        assertEquals(meta.getInitialTimestamp(), BaseIotaConfig.Defaults.GLOBAL_SNAPSHOT_TIME);
        assertEquals(meta.getTimestamp(), BaseIotaConfig.Defaults.GLOBAL_SNAPSHOT_TIME);
        
        meta.setTimestamp(BaseIotaConfig.Defaults.GLOBAL_SNAPSHOT_TIME + 1);
        assertNotEquals(meta.getInitialTimestamp(), meta.getTimestamp());
    }
    
    @Test
    public void hashTest(){
        assertEquals(meta.getInitialHash(), A);
        
        assertEquals(meta.getHash(), A);
        
        meta.setHash(B);
        assertNotEquals(meta.getInitialHash(), meta.getHash());
    }
    
    @Test
    public void solidEntryPointsTest(){
        assertTrue(meta.hasSolidEntryPoint(A));
        assertTrue(meta.hasSolidEntryPoint(B));
        assertTrue(meta.hasSolidEntryPoint(C));
        
        assertEquals(1, meta.getSolidEntryPointIndex(A));
        assertEquals(2, meta.getSolidEntryPointIndex(B));
        
        // Test -1 to ensure, if we ever enforce this positive, something could break 
        assertEquals(-1, meta.getSolidEntryPointIndex(C));
        
        assertEquals(meta.getSolidEntryPoints().size(), solidEntryPoints.size());
        assertEquals(meta.getSolidEntryPoints(), new HashMap<>(solidEntryPoints));
        
        meta.setSolidEntryPoints(seenMilestones);
        assertEquals(10, meta.getSolidEntryPointIndex(A));
        assertEquals(11, meta.getSolidEntryPointIndex(B));
        assertEquals(12, meta.getSolidEntryPointIndex(C));
    }
    
    @Test
    public void seenMilestonesTest(){
        assertEquals(meta.getSeenMilestones().size(), seenMilestones.size());
        assertEquals(meta.getSeenMilestones(), new HashMap<>(seenMilestones));
    }
    
    @Test
    public void createTest(){
        SnapshotMetaData newMetaData = new SnapshotMetaDataImpl(meta);
        assertEquals(newMetaData, meta);
        
        newMetaData = new SnapshotMetaDataImpl(Hash.NULL_HASH, 0, 0l, Collections.EMPTY_MAP, Collections.EMPTY_MAP);
        newMetaData.update(meta);
        assertEquals(newMetaData, meta);
    }
    
}
