package com.iota.iri.service.dto;

import java.util.List;

/**
 * This class represents the core API request 'getTips'.
 * Returns the list of tips.
 **/
public class GetTipsResponse extends AbstractResponse {
	
	private String [] hashes;

	public static AbstractResponse create(List<String> elements) {
		GetTipsResponse res = new GetTipsResponse();
		res.hashes = elements.toArray(new String[] {});
		return res;
	}
	
    /**
     * The list of current tips
     *
     * @return The hashes.
     */
	public String[] getHashes() {
		return hashes;
	}

}
