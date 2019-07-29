package com.iota.iri.service.tipselection.impl;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.iota.iri.controllers.ApproveeViewModel;
import com.iota.iri.controllers.TransactionViewModel;
import com.iota.iri.model.Hash;
import com.iota.iri.model.HashId;
import com.iota.iri.model.HashPrefix;
import com.iota.iri.service.snapshot.SnapshotProvider;
import com.iota.iri.service.tipselection.RatingCalculator;
import com.iota.iri.storage.Tangle;
import com.iota.iri.utils.collections.impl.TransformingMap;
import com.iota.iri.utils.collections.interfaces.UnIterableMap;

/**
 * Calculates the weight recursively/on the fly
 * Used to create a weighted random walks.
 *
 * @see <a href="cumulative.md">https://github.com/alongalky/iota-docs/blob/master/cumulative.md</a>
 */
public class RecursiveWeightCalculator implements RatingCalculator {

    private final Tangle tangle;
    private final SnapshotProvider snapshotProvider;

    /**
     * Constructor for Recursive Weight Calculator
     * @param tangle Tangle object which acts as a database interface
     * @param snapshotProvider accesses ledger's snapshots
     */
    public RecursiveWeightCalculator(Tangle tangle, SnapshotProvider snapshotProvider) {
        this.tangle = tangle;
        this.snapshotProvider = snapshotProvider;
    }

    @Override
    public UnIterableMap<HashId, Integer> calculate(Hash entryPoint) throws Exception {
        UnIterableMap<HashId, Integer> hashWeightMap = calculateRatingDfs(entryPoint);
        
        return hashWeightMap;
    }
    
    private UnIterableMap<HashId, Integer> calculateRatingDfs(Hash entryPoint) throws Exception {
        TransactionViewModel tvm = TransactionViewModel.fromHash(tangle, entryPoint);
        int depth = tvm.snapshotIndex() > 0 
                ? snapshotProvider.getLatestSnapshot().getIndex() - tvm.snapshotIndex() + 1 
                : 1;

        // Estimated capacity per depth, assumes 5 minute gap in between milestones, at 3tps
        UnIterableMap<HashId, Integer> hashWeight = createTxHashToCumulativeWeightMap( 5 * 60 * 3 * depth);

        Map<Hash, Set<Hash>> txToDirectApprovers = new HashMap<>();

        Deque<Hash> stack = new ArrayDeque<>();
        stack.push(entryPoint);

        while (!stack.isEmpty()) {
            Hash txHash = stack.peekLast();

            Set<Hash> approvers = getTxDirectApproversHashes(txHash, txToDirectApprovers);
            if (null != approvers && (approvers.size() == 0 || hasAll(hashWeight, approvers, stack))) {
                approvers.add(txHash);
                hashWeight.put(txHash, getRating(approvers, txToDirectApprovers));
                stack.removeLast();
            } else {
                stack.addAll(approvers);
            }
        }

        return hashWeight;
    }

    /**
     * Gets the rating of a set, calculated by checking its approvers
     * 
     * @param startingSet
     * @param txToDirectApproversCache 
     * @return
     * @throws Exception
     */
    private int getRating(Set<Hash> startingSet, Map<Hash, Set<Hash>> txToDirectApproversCache) throws Exception {
        Deque<Hash> stack = new ArrayDeque<>(startingSet);
        while (stack.isEmpty()) {
            Set<Hash> approvers = getTxDirectApproversHashes(stack.pollLast(), txToDirectApproversCache);
            for (Hash hash : approvers) {
                if (startingSet.add(hash)) {
                    stack.add(hash);
                }
            }
        }

        return startingSet.size();
    }

    /**
     * 
     * @param source
     * @param requester
     * @param stack
     * @return
     */
    private boolean hasAll(UnIterableMap<HashId, Integer> source, Set<Hash> requester, Deque<Hash> stack) {
        for (Hash h : requester) {
            if (!source.containsKey(h) && !stack.contains(h)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Finds the approvers of a transaction, and adds it to the txToDirectApprovers map if they weren't there yet.
     * 
     * @param txHash The tx we find the approvers of
     * @param txToDirectApprovers The map we look in, and add to
     * @param fallback The map we check in before going in the database, can be <code>null</code>
     * @return A set with the direct approvers of the given hash
     * @throws Exception
     */
    private Set<Hash> getTxDirectApproversHashes(Hash txHash, Map<Hash, Set<Hash>> txToDirectApprovers)
            throws Exception {
        
        Set<Hash> txApprovers = txToDirectApprovers.get(txHash);
        if (txApprovers == null) {
            ApproveeViewModel approvers = ApproveeViewModel.load(tangle, txHash);
            Collection<Hash> appHashes;
            if (approvers == null || approvers.getHashes() == null) {
                appHashes = Collections.emptySet();
            } else {
                appHashes = approvers.getHashes();
            }
            
            txApprovers = new HashSet<>(appHashes.size());
            for (Hash appHash : appHashes) {
                // if not genesis (the tx that confirms itself)
                if (!snapshotProvider.getInitialSnapshot().hasSolidEntryPoint(appHash)) {
                    txApprovers.add(appHash);
                }
            }
            txToDirectApprovers.put(txHash, txApprovers);
        }
        
        return new HashSet<Hash>(txApprovers);
    }
    
    private static UnIterableMap<HashId, Integer> createTxHashToCumulativeWeightMap(int size) {
        return new TransformingMap<>(size, HashPrefix::createPrefix, null);
    }
}
