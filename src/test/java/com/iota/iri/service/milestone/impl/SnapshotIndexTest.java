package com.iota.iri.service.milestone.impl;

import com.iota.iri.TangleMockUtils;
import com.iota.iri.TransactionTestUtils;
import com.iota.iri.cache.Cache;
import com.iota.iri.cache.impl.CacheConfigurationImpl;
import com.iota.iri.cache.impl.CacheImpl;
import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.model.Hash;
import com.iota.iri.model.HashFactory;
import com.iota.iri.model.persistables.Transaction;
import com.iota.iri.service.snapshot.Snapshot;
import com.iota.iri.service.snapshot.SnapshotProvider;
import com.iota.iri.service.snapshot.impl.SnapshotMockUtils;
import com.iota.iri.storage.Indexable;
import com.iota.iri.storage.Tangle;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static com.iota.iri.TransactionTestUtils.getTransactionHash;
import static com.iota.iri.TransactionTestUtils.getTransactionTritsWithTrunkAndBranch;
import static org.junit.Assert.assertEquals;

public class SnapshotIndexTest {
    @Rule 
    public MockitoRule mockitoRule = MockitoJUnit.rule();
    
    private enum MockedMilestone {
        X("999999999999999999999999999999999999999999999999999999999999999999999999999999999", 1),
        A("ARWY9LWHXEWNL9DTN9IGMIMIVSBQUIEIDSFRYTCSXQARRTVEUFSBWFZRQOJUQNAGQLWHTFNVECELCOFYB", 2),
        B("BRWY9LWHXEWNL9DTN9IGMIMIVSBQUIEIDSFRYTCSXQARRTVEUFSBWFZRQOJUQNAGQLWHTFNVECELCOFYB", 3),
        C("CRWY9LWHXEWNL9DTN9IGMIMIVSBQUIEIDSFRYTCSXQARRTVEUFSBWFZRQOJUQNAGQLWHTFNVECELCOFYB", 4);

        private final Hash transactionHash;

        private final int milestoneIndex;

        MockedMilestone(String transactionHash, int milestoneIndex) {
            this.transactionHash = HashFactory.TRANSACTION.create(transactionHash);
            this.milestoneIndex = milestoneIndex;
        }

        public void mock(Tangle tangle, Hash hash, Snapshot snapshot) throws Exception {
            TangleMockUtils.mockMilestone(tangle, transactionHash, milestoneIndex);
            
            TransactionViewModel ms = new TransactionViewModel(getTransactionTritsWithTrunkAndBranch(
                    hash, hash), transactionHash);
            ms.isMilestone(tangle, snapshot, true);
            
            Transaction mockedTransaction = TangleMockUtils.mockTransaction(tangle, ms);
            mockedTransaction.snapshot = milestoneIndex;
        }
    }
    
    @Mock
    private Tangle tangle;

    @Mock
    private SnapshotProvider snapshotProvider;

    @InjectMocks
    private MilestoneServiceImpl milestoneService;

    private Snapshot snapshot;
    
    private Cache<Indexable, TransactionViewModel> tvmCache;

    @Before
    public void setUp() throws Exception {
        SnapshotMockUtils.mockSnapshotProvider(snapshotProvider);
        snapshot = SnapshotMockUtils.createSnapshot();
        
        CacheConfigurationImpl config = new CacheConfigurationImpl(100, 1);
        tvmCache = new CacheImpl<Indexable, TransactionViewModel>(config);
        
        Mockito.when(tangle.getCache(TransactionViewModel.class)).thenReturn(tvmCache);
    }
    
    @Test
    public void test() throws Exception {
        //11 tx
        TransactionViewModel transaction, transaction1, transaction2, transaction3, transaction4, 
                             transaction5, transaction6, transaction7, transaction8, transaction9, transaction10;

        //----------- X
        
        transaction = new TransactionViewModel(getTransactionTritsWithTrunkAndBranch(
                MockedMilestone.X.transactionHash, MockedMilestone.X.transactionHash), getTransactionHash());

        //----------- A
        
        transaction1 = new TransactionViewModel(TransactionTestUtils.get9Transaction(), getTransactionHash());
        
        transaction2 = new TransactionViewModel(getTransactionTritsWithTrunkAndBranch(
                transaction1.getHash(), MockedMilestone.A.transactionHash), getTransactionHash());
        

        transaction3 = new TransactionViewModel(TransactionTestUtils.get9Transaction(), getTransactionHash());

        transaction4 = new TransactionViewModel(getTransactionTritsWithTrunkAndBranch(
                transaction3.getHash(), MockedMilestone.A.transactionHash), getTransactionHash());

        //----------- B
        
        transaction5 = new TransactionViewModel(getTransactionTritsWithTrunkAndBranch(
                transaction1.getHash(), MockedMilestone.B.transactionHash), getTransactionHash());

        transaction6 = new TransactionViewModel(getTransactionTritsWithTrunkAndBranch(
                transaction4.getHash(), MockedMilestone.B.transactionHash), getTransactionHash());
        
        //----------- C
        
        transaction7 = new TransactionViewModel(getTransactionTritsWithTrunkAndBranch(
                MockedMilestone.C.transactionHash, MockedMilestone.C.transactionHash), getTransactionHash());
                
        transaction8 = new TransactionViewModel(getTransactionTritsWithTrunkAndBranch(
                MockedMilestone.B.transactionHash, MockedMilestone.C.transactionHash), getTransactionHash());
                
        transaction9 = new TransactionViewModel(getTransactionTritsWithTrunkAndBranch(
                transaction8.getHash(), transaction6.getHash()), getTransactionHash());
        
        transaction10 = new TransactionViewModel(getTransactionTritsWithTrunkAndBranch(
                transaction8.getHash(), transaction9.getHash()), getTransactionHash());
        
        
        MockedMilestone.X.mock(tangle, Hash.NULL_HASH, snapshot); //start
        TangleMockUtils.mockTransaction(tangle, transaction);
        MockedMilestone.A.mock(tangle, transaction.getHash(), snapshot); // 1

        TangleMockUtils.mockTransaction(tangle, transaction1);
        TangleMockUtils.mockTransaction(tangle, transaction2); // To 1 and above
        
        TangleMockUtils.mockTransaction(tangle, transaction3);
        TangleMockUtils.mockTransaction(tangle, transaction4); // To 1 and above
        
        MockedMilestone.B.mock(tangle, transaction2.getHash(), snapshot); // 2
        
        TangleMockUtils.mockTransaction(tangle, transaction5); // to 2 and transaction1
        TangleMockUtils.mockTransaction(tangle, transaction6); // to 2 and transaction4
        
        MockedMilestone.C.mock(tangle, transaction5.getHash(), snapshot); // 3

        TangleMockUtils.mockTransaction(tangle, transaction7); // to 3
        TangleMockUtils.mockTransaction(tangle, transaction8); // to 2 and 3
        TangleMockUtils.mockTransaction(tangle, transaction9); // to transaction8 and transaction6
        TangleMockUtils.mockTransaction(tangle, transaction10); // to transaction8 and transaction9
        
        transaction10.calculateTransactionRoots(tangle);
        
        assertEquals(4, TransactionViewModel.fromHash(tangle, transaction7.getHash()).getOldestTxRootSnapshot());
        assertEquals(4, TransactionViewModel.fromHash(tangle, transaction7.getHash()).getYoungestTxRootSnapshot());
        System.out.println("TEST");
    }
}


















