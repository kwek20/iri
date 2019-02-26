package com.iota.iri.service.snapshot.impl;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import com.iota.iri.conf.SnapshotConfig;
import com.iota.iri.service.milestone.LatestMilestoneTracker;
import com.iota.iri.service.snapshot.SnapshotException;
import com.iota.iri.service.snapshot.SnapshotProvider;
import com.iota.iri.service.snapshot.SnapshotService;
import com.iota.iri.service.transactionpruning.TransactionPruner;
import com.iota.iri.utils.thread.ThreadUtils;

public class LocalSnapshotManagerImplTest {
    
    private static final int DELAY_SYNC = 5;
    private static final int DELAY_UNSYNC = 1;
    private static final int SNAPSHOT_DEPTH = 5;

    @Rule 
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Mock
    private static SnapshotConfig config;
    
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    SnapshotProvider snapshotProvider;
    
    @Mock
    SnapshotService snapshotService;
    
    @Mock
    TransactionPruner transactionPruner;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    LatestMilestoneTracker milestoneTracker;
    
    private LocalSnapshotManagerImpl lsManager;
    
    @BeforeClass
    public static void loadConfig() {
        
    }

    @Before
    public void setUp() throws Exception {
        this.lsManager = new LocalSnapshotManagerImpl();
        
        lsManager.init(snapshotProvider, snapshotService, transactionPruner, config);
        when(snapshotProvider.getLatestSnapshot().getIndex()).thenReturn(-5, -1, 10, 998, 999, 1999, 2000);
        
        when(config.getLocalSnapshotsIntervalSynced()).thenReturn(DELAY_SYNC);
        when(config.getLocalSnapshotsIntervalUnsynced()).thenReturn(DELAY_UNSYNC);
        when(config.getLocalSnapshotsDepth()).thenReturn(SNAPSHOT_DEPTH);
    }

    @After
    public void tearDown() throws Exception {
        
    }
    
    @Test
    // We use an external variable, multi threading might break it
    public synchronized void takeLocalSnapshot() throws SnapshotException {
        // Always return true
        when(milestoneTracker.isInitialScanComplete()).thenReturn(true);
        
        // When we call it, we are in sync
        when(milestoneTracker.getLatestMilestoneIndex()).thenReturn(-5);
        
        // We are more then the depth ahead
        when(snapshotProvider.getLatestSnapshot().getIndex()).thenReturn(100);
        when(snapshotProvider.getInitialSnapshot().getIndex()).thenReturn(100 - SNAPSHOT_DEPTH - DELAY_SYNC - 1);
        
        // Run in separate thread to allow is to time-out
        Thread t = new Thread(new Runnable() {
            
            @Override
            public void run() {
                lsManager.monitorThread(milestoneTracker);
                
            }
        });

        t.start();
        // We should finish directly, margin for slower computers
        ThreadUtils.sleep(100);
        
        // Cancel the thread
        t.interrupt();
        
        // Verify we took a snapshot
        verify(snapshotService, times(1)).takeLocalSnapshot(Mockito.any(), Mockito.any());
    }

    @Test
    public void isInSyncTestScanIncomplete() {
        when(milestoneTracker.isInitialScanComplete()).thenReturn(false);
        assertFalse(lsManager.isInSync(milestoneTracker));
    }
    
    @Test
    public void isInSyncTestScanComplete() {
        // Always return true
        when(milestoneTracker.isInitialScanComplete()).thenReturn(true);
        when(milestoneTracker.getLatestMilestoneIndex()).thenReturn(-1, 5, 10, 998 + DELAY_SYNC - 1, 2000);
        
        // snapshotProvider & milestoneTracker
        // -5 & -1 -> not in sync
        assertFalse(lsManager.isInSync(milestoneTracker));
        
        // -1 and 5 -> not in sync
        assertFalse(lsManager.isInSync(milestoneTracker));
        
        // 10 and 10 -> in sync
        assertTrue(lsManager.isInSync(milestoneTracker));
        
        // 998 and 1002 -> in sync since sync gap = 5
        assertTrue(lsManager.isInSync(milestoneTracker));
        
        // 999 and 2000 -> out of sync again, bigger gap than 5
        assertFalse(lsManager.isInSync(milestoneTracker));
        
        // 1999 and 2000 -> out of sync still
        assertFalse(lsManager.isInSync(milestoneTracker));
        
        // 2000 and 2000 -> in sync again
        assertTrue(lsManager.isInSync(milestoneTracker));
    }
    
    @Test
    public void getDelayTest() {
        assertEquals(DELAY_UNSYNC, lsManager.getSnapshotInterval(false));
        assertEquals(DELAY_SYNC, lsManager.getSnapshotInterval(true));
    }
}
