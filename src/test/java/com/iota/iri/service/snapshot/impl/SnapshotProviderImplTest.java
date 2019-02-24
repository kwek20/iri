package com.iota.iri.service.snapshot.impl;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.iota.iri.conf.ConfigFactory;
import com.iota.iri.conf.IotaConfig;
import com.iota.iri.model.Hash;
import com.iota.iri.service.snapshot.SnapshotException;
import com.iota.iri.service.spentaddresses.SpentAddressesException;

public class SnapshotProviderImplTest {

    SnapshotProviderImpl provider;
    
    @Before
    public void setUp() throws Exception {
        provider = new SnapshotProviderImpl();
    }

    @After
    public void tearDown() throws Exception {
        provider.shutdown();
    }
    
    @Test
    public void testGetLatestSnapshot() throws SnapshotException, SpentAddressesException {
        IotaConfig iotaConfig = ConfigFactory.createIotaConfig(true);
        provider.init(iotaConfig);

        assertEquals(iotaConfig.getMilestoneStartIndex(), provider.getInitialSnapshot().getIndex());
        assertEquals(iotaConfig.getSnapshotTime(), provider.getInitialSnapshot().getInitialTimestamp());
        assertEquals(Hash.NULL_HASH, provider.getInitialSnapshot().getHash());
        assertTrue(provider.getInitialSnapshot().getBalances().size() > 0);
        assertTrue(provider.getInitialSnapshot().hasCorrectSupply());
        
        assertEquals(provider.getInitialSnapshot(), provider.getLatestSnapshot());
    }
}
