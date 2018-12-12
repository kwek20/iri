
var System = java.lang.System;
var SnapshotServiceImpl = com.iota.iri.service.snapshot.impl.SnapshotServiceImpl;

var iri = com.iota.iri;
var tracker = IOTA.milestoneTracker
var snapshot = tracker.latestSnapshot;

var snapshotProvider = IOTA.snapshotProvider;
var snapshotService = new SnapshotServiceImpl();

var Files = java.nio.file.Files;
var Paths = java.nio.file.Paths;

var Callable = iri.service.CallableRequest;
var Response = iri.service.dto.IXIResponse;

var STATE_FILE_NAME = "ledgerState";

/**
 * Writes current ledger state to file.
 * This file is identical to a global snapshot taken at this ledger state
 *
 * @param ledgerState state object that shall be written
 */
function writeLedgerState(ledgerState) {
    try {
    	var balances = [];

    	System.out.println(ledgerState.getBalances().size())
    	for each (var balanceEntry in ledgerState.getBalances().entrySet()){
    		if (balanceEntry.getValue() != 0){
    			balances.push(balanceEntry.getKey() + ";" + balanceEntry.getValue())
    		}
        }

        Files.write(
            Paths.get(STATE_FILE_NAME + "-" + ledgerState.getIndex()),
            balances
            /*ledgerState.getBalances().entrySet()
		        .stream()
		        .filter(function(entry){
		        	return entry.getValue() != 0
		        })
		        .map(function(entry){ //<CharSequence>
		        	return entry.getKey() + ";" + entry.getValue()
		    	})
		        .sorted()
		        .iterator()*/
        );
    } catch (exception) {
        System.out.println("error: " + exception);
    }
}

/**
 * Gets the ledger state from the IRI instance
 */
function getLedgerState(){
	return snapshotProvider.getLatestSnapshot();
}

/**
 * Updates the ledger to the supplied index 
 *
 * @param ledgerState The current state of the ledger
 * @param milestoneIndex The index we want to roll back/forward ti
 * @param epochTime the time of this milestone index in milliseconds
 */
function updateLedgerState(ledgerState, milestoneIndex, epochTime){
	if (tracker.latestSolidSubtangleMilestoneIndex > milestoneIndex){
		snapshotService.rollBackMilestones(IOTA.tangle, ledgerState, milestoneIndex);
	} else {
		snapshotService.replayMilestones(IOTA.tangle, ledgerState, milestoneIndex);
	}
}

function getSnapshot(request) {
	var milestoneIndex = request['milestoneIndex'];
	var epochTime = request['milestoneEpoch'];

	var ledgerState = getLedgerState();
	updateLedgerState(ledgerState, milestoneIndex, epochTime);
	writeLedgerState(ledgerState);

    return Response.create({
        epochTime: epochTime,
        milestoneIndex: milestoneIndex,
        snapshotIndex: snapshot.index(),
        latestSolidIndex: tracker.latestSolidSubtangleMilestoneIndex
    });
}

API.put("getState", new Callable({ call: getSnapshot }))
