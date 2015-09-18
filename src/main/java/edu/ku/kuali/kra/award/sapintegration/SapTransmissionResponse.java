/*
 * Copyright (c) 2014. Boston University
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl1.php
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND either express or
 * implied.
 *
 * See the License for the specific language governing permissions and limitations under the License.
 */
package edu.ku.kuali.kra.award.sapintegration;

import java.util.*;

/**
 * BU KC/SAP Integration: Handle SAP transmission response
 */
public final class SapTransmissionResponse {

    public enum Status {SUCCESS, VALIDATION_FAILURE, TRANSMISSION_FAILURE}

    ;

    private final Status status;
    private final String message;
    private final String sentData;
    private final String receivedData;
    private final Map<Long, String> sponsoredProgramIds;
    private final Map<Long, String> walkerIds;

    // BU Customization ID: N/A mukadder 20130429 - ENHC0010154 - Issue 55 - KC_SAP Interface to display warning message

    private List<String> warningMessages;

    /**
     * Process KC/SAP webservice call response.
     *
     * @param status              of transmission
     * @param message             returned message from the WS call
     * @param warningMessages     if any
     * @param sponsoredProgramIds BU returned Sponsored Program IDs generated
     * @param walkerIds           generated Walker IDs
     * @param sentData            Transmission send TS
     * @param receivedData        Transmission recieved TS
     */
    public SapTransmissionResponse(Status status, String message, List<String> warningMessages, Map<Long, String> sponsoredProgramIds, Map<Long, String> walkerIds, String sentData, String receivedData) {
        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null.");
        }
        this.status = status;
        this.message = message;
        this.sentData = sentData;
        this.receivedData = receivedData;
        if (sponsoredProgramIds == null) {
            this.sponsoredProgramIds = new HashMap<Long, String>();
        } else {
            this.sponsoredProgramIds = sponsoredProgramIds;
        }
        if (walkerIds == null) {
            this.walkerIds = new HashMap<Long, String>();
        } else {
            this.walkerIds = walkerIds;
        }
        if (warningMessages == null) {
            this.warningMessages = new ArrayList<String>();
        } else {
            this.warningMessages = warningMessages;
        }

    }

    public static SapTransmissionResponse success(Map<Long, String> sponsoredProgramIds, Map<Long, String> walkerIds, List<String> warningMessages, String sentData, String receivedData) {
        return new SapTransmissionResponse(Status.SUCCESS, "Success", warningMessages, sponsoredProgramIds, walkerIds, sentData, receivedData);
    }

    public static SapTransmissionResponse validationFailure(String message) {
        return new SapTransmissionResponse(Status.VALIDATION_FAILURE, message, null, new HashMap<Long, String>(), new HashMap<Long, String>(), null, null);
    }

    public static SapTransmissionResponse transmissionFailure(String message, List<String> warningMessages, String sentData, String receivedData) {
        return new SapTransmissionResponse(Status.TRANSMISSION_FAILURE, message, warningMessages, new HashMap<Long, String>(), new HashMap<Long, String>(), sentData, receivedData);
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getSentData() {
        return sentData;
    }

    public String getReceivedData() {
        return receivedData;
    }

    public Map<Long, String> getSponsoredProgramIds() {
        return Collections.unmodifiableMap(sponsoredProgramIds);
    }

    public Map<Long, String> getWalkerIds() {
        return Collections.unmodifiableMap(walkerIds);
    }

    public List<String> getWarningMessages() {
        return Collections.unmodifiableList(warningMessages);
    }

}
