package com.iota.iri.service.milestone.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.common.base.Strings;
import com.iota.iri.TangleMockUtils;
import com.iota.iri.TransactionTestUtils;
import com.iota.iri.conf.IotaConfig;
import com.iota.iri.controllers.TagViewModel;
import com.iota.iri.model.Hash;
import com.iota.iri.model.HashFactory;
import com.iota.iri.model.persistables.ObsoleteTag;
import com.iota.iri.model.persistables.Tag;
import com.iota.iri.model.persistables.Transaction;
import com.iota.iri.service.milestone.LatestMilestoneTracker;
import com.iota.iri.service.milestone.MilestoneException;
import com.iota.iri.service.milestone.MilestoneService;
import com.iota.iri.service.milestone.MilestoneSolidifier;
import com.iota.iri.service.milestone.MilestoneValidity;
import com.iota.iri.service.snapshot.SnapshotProvider;
import com.iota.iri.storage.Tangle;
import com.iota.iri.utils.Converter;

public class LatestMilestoneTrackerImplTest {


    private Hash coordinator = TransactionTestUtils.getTransactionHash();
    
    private Tangle tangle;

    private LatestMilestoneTrackerImpl tracker;
    
    @Before
    public void setUp() throws MilestoneException {
        this.tangle = Mockito.mock(Tangle.class);
        
        IotaConfig config = Mockito.mock(IotaConfig.class);
        Mockito.when(config.getCoordinator()).thenReturn(coordinator);

        MilestoneService milestoneService = Mockito.mock(MilestoneServiceImpl.class);
        Mockito.when(milestoneService.getMilestoneIndex(Mockito.any()))
            .thenReturn(1, 2);
        
        Mockito.when(milestoneService.validateMilestone(Mockito.any(), Mockito.anyInt()))
            .thenReturn(MilestoneValidity.VALID);
        
        this.tracker = new LatestMilestoneTrackerImpl();
        tracker.init(tangle, 
                Mockito.mock(SnapshotProvider.class, Mockito.RETURNS_DEEP_STUBS), 
                milestoneService, 
                Mockito.mock(MilestoneSolidifier.class), 
                config);
    }
    
    @Test
    public void test() throws Exception {
        int milestone = 1; 
        String tag = Strings.padEnd(Converter.getTrytesForIndex(milestone), 81, '9');
        byte[] trits = Converter.allocatingTritsFromTrytes(tag);
        
        Transaction t = new Transaction();
        t.obsoleteTag = HashFactory.OBSOLETETAG.create(trits);
        t.address = coordinator;
        t.milestone = true;
        
        Hash a = TransactionTestUtils.getTransactionHash();
        TangleMockUtils.mockTransaction(tangle, a, t);
        
        Mockito.when(tangle.load(ObsoleteTag.class, t.obsoleteTag))
            .thenReturn(new Tag(a));
        
        tracker.start();

        // Wait until different thread did its job
        Thread.currentThread().sleep(100);
        assertEquals("Latest milestone should be " + milestone, milestone, tracker.getLatestMilestoneIndex());
    }
}
