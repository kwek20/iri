
var System = java.lang.System;

var FileWriter = java.io.FileWriter;

var Files = java.nio.file.Files;
var Paths = java.nio.file.Paths;

var tracker 			= IOTA.latestMilestoneTracker

var snapshotProvider 	= IOTA.snapshotProvider;
var snapshotService 	= IOTA.snapshotService;

var iri = com.iota.iri;
var Callable = iri.service.CallableRequest;
var Response = iri.service.dto.IXIResponse;
var ErrorResponse = iri.service.dto.ErrorResponse;

var STATE_FILE_NAME = "ledgerState";

/**
 * Writes current ledger state to file.
 * This file is identical to a global snapshot taken at this ledger state
 *
 * @param ledgerState state object that shall be written
 */
function writeLedgerState(ledgerState, location) {
    try {
		var path = Paths.get(location, STATE_FILE_NAME + "-" + ledgerState.getIndex());
        var fw;
        try {
        	fw = new FileWriter(path.toString());
	    	for each (var balanceEntry in ledgerState.getBalances().entrySet()){
	    		if (balanceEntry.getValue() != 0){
	    			var line = balanceEntry.getKey().toString() + ";" + balanceEntry.getValue();
	    			fw.write(line + "\n");   	
	    		}
	        }
	        fw.close();
		} catch(error) {
			fw.close();
			throw error;
		}

		return path;
        /*
		// Apparently this is not a charsequence iterator
        return Files.write(
            Paths.get(location, STATE_FILE_NAME + "-" + ledgerState.getIndex()),
            //balances
            ledgerState.getBalances().entrySet()
		        .stream()
		        .filter(function(entry){
		        	return entry.getValue() != 0 && ++i < 50;
		        })
		        .map(function(entry){ //<CharSequence>
		        	return entry.getKey() + ";" + entry.getValue()
		    	})
		        .sorted()
		        .iterator()
        );*/
    } catch (exception) {
        throw exception;
    }
}

/**
 * Gets a copy of the ledger state from the IRI instance
 */
function getLedgerState(){
	return snapshotProvider.getLatestSnapshot().clone();
}

/**
 * Updates the ledger to the supplied index 
 *
 * @param ledgerState The current state of the ledger
 * @param milestoneIndex The index we want to roll back/forward ti
 * @param epochTime the time of this milestone index in milliseconds
 */
function updateLedgerState(ledgerState, milestoneIndex, epochTime){
	if (ledgerState.getIndex() > milestoneIndex){
		snapshotService.rollBackMilestones(ledgerState, milestoneIndex+1);
	} else if (ledgerState.getIndex() < milestoneIndex){
		snapshotService.replayMilestones(ledgerState, milestoneIndex-1);
	}
}


/*
curl http://localhost:14265 -X POST -H 'X-IOTA-API-Version: 1.4.1' -H 'Content-Type: application/json' -d '{"command": "LedgerState.getState", "milestoneEpoch": "0", "milestoneIndex": ""}'
*/
function getSnapshot(request) {
	var milestoneIndex = parseInt(request['milestoneIndex']);
	var epochTime = request['milestoneEpoch'];

	var SnapshotImpl = Java.type("com.iota.iri.service.snapshot.impl.SnapshotImpl");
	var stateField = SnapshotImpl.class.getDeclaredField("state");
	stateField.setAccessible(true);

	if (!milestoneIndex || !epochTime){
		return ErrorResponse.create("We need both 'milestoneIndex' and 'milestoneEpoch' in order to work.");
	} else if (milestoneIndex < snapshotProvider.getInitialSnapshot().getIndex()){
		return ErrorResponse.create("Milestone index is too old. (min: " + snapshotProvider.getInitialSnapshot().getIndex() + ")");
	} else if (milestoneIndex > tracker.getLatestMilestoneIndex()){
		return ErrorResponse.create("We dont have this milestone yet. (max: " + tracker.getLatestMilestoneIndex() + ")");
	}

	var location = request['url'];
	if (!location){
		location = "";
	}

	try {
		var ledgerState = getLedgerState();
		updateLedgerState(ledgerState, milestoneIndex, epochTime);

		System.out.println(snapshotProvider.getInitialSnapshot());
		System.out.println(snapshotProvider.getInitialSnapshot().state);

		System.out.println(ledgerState.state);
		System.out.println(snapshotProvider.getLatestSnapshot());
		System.out.println(snapshotProvider.getLatestSnapshot().state);
		System.out.println(ledgerState.equals(snapshotProvider.getLatestSnapshot()));
		System.out.println(snapshotProvider.getLatestSnapshot().getIndex());
		System.out.println(milestoneIndex);

		if (ledgerState.getIndex() !== milestoneIndex){
			return ErrorResponse.create("Failed to change to milestone");
		} else if (snapshotProvider.getLatestSnapshot().getIndex() != milestoneIndex && ledgerState.equals(snapshotProvider.getLatestSnapshot())){
			return ErrorResponse.create("Nothing changed during updating...");
		}

		var path = writeLedgerState(ledgerState, location);

		stateField.setAccessible(false);
	    return Response.create({
	        ledgerStatePath: path.toAbsolutePath().toString()
	    });
	} catch (exception) {
		stateField.setAccessible(false);
        return ErrorResponse.create(exception);
    }
}

API.put("getState", new Callable({ call: getSnapshot }))

