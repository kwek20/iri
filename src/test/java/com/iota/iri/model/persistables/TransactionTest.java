package com.iota.iri.model.persistables;

import static org.junit.Assert.*;

import org.junit.Test;

import com.iota.iri.TransactionTestUtils;
import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.model.Hash;
import com.iota.iri.utils.Converter;

public class TransactionTest {

    @Test
    public void testBytes() {
        Transaction t = TransactionTestUtils.getRandomTransaction();
        
        Transaction newtx = new Transaction();
        newtx.read(t.bytes());
        newtx.readMetadata(t.metadata());
        
        assertArrayEquals(t.metadata(), newtx.metadata());
    }
    
    @Test
    public void fromTrits() {
        byte[] trits = TransactionTestUtils.getRandomTransactionTrits();
        byte[] bytes = Converter.allocateBytesForTrits(trits.length);
        Converter.bytes(trits, bytes);
        
        TransactionViewModel TVM = new TransactionViewModel(trits, Hash.NULL_HASH);
        TVM.getAddressHash();
        TVM.getTrunkTransactionHash();
        TVM.getBranchTransactionHash();
        TVM.getBundleHash();
        TVM.getTagValue();
        TVM.getObsoleteTagValue();
        TVM.setAttachmentData();
        TVM.setMetadata();
        
        assertArrayEquals(TVM.getBytes(), bytes);
        
        Transaction tvmTransaction = TVM.getTransaction();
        
        assertEquals(tvmTransaction.branch, TVM.getTransaction().branch);
    }
}
