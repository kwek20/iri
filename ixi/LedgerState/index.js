
var iri = com.iota.iri;
var tracker = IOTA.milestoneTracker
var snapshot = tracker.latestSnapshot;

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
    	for each (var balanceEntry in ledgerState.getBalances().entrySet()){
    		if (entry.getValue() != 0){
    			balances.push(entry.getKey() + ";" + entry.getValue())
    		}
        }

        Files.write(
                Paths.get(STATE_FILE_NAME),
                balances
        );
    } catch (exception) {
        System.out.println("error");
    }
}

/**
 * Gets the ledger state from the IRI instance
 */
function getLedgerState(){

}

/**
 * Updates the ledger to the supplied index 
 *
 * @param ledgerState The current state of the ledger
 * @param milestoneIndex The index we want to roll back/forward ti
 * @param epochTime the time of this milestone index in milliseconds
 */
function updateLedgerState(ledgerState, milestoneIndex, epochTime){

}

function getSnapshot(request) {
	var milestoneIndex = request['milestoneIndex'];
	var epochTime = request['milestoneEpoch'];

	var ledgerState = this.getLedgerState();
	this.updateLedgerState(ledgerState, milestoneIndex, epochTime);
	this.writeLedgerState(ledgerState);

    return Response.create({
        epochTime: epochTime,
        milestoneIndex: milestoneIndex,
        snapshotIndex: snapshot.index(),
        latestSolidIndex: tracker.latestSolidSubtangleMilestoneIndex
    });
}

API.put("getState", new Callable({ call: getSnapshot }))
