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
package edu.ku.kuali.kra.institutionalproposal.proposallog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kra.institutionalproposal.proposallog.ProposalLog;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.service.DocumentService;

/**
 * Lookupable helper service used for proposal log lookup
 */
public class ProposalLogLookupableHelperServiceImpl extends org.kuali.kra.institutionalproposal.proposallog.ProposalLogLookupableHelperServiceImpl {

    private DocumentService documentService;

    // BUKC-0059: Cleanup Overlay class to fix an issue caused by BUKC-0039 that displays "print" twice on the PL lookup screen
    // by removing all the methods that are not not used by the overridden method in the overlay project.

    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

    /*
     * Overriding this to only return the currently Active version of a proposal
     */
    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {

        checkIsLookupForProposalCreation();
        // List<ProposalLog> results = (List<ProposalLog>)super.getSearchResults(fieldValues);
        List<? extends BusinessObject> results = getSearchResultsHelper(
                org.kuali.rice.krad.lookup.LookupUtils.forceUppercase(getBusinessObjectClass(), fieldValues), false);
        String returnLocation = fieldValues.get("backLocation");
        // BUKC-0039: Disable Proposal Log filtering due to an issue with obtaining proposal logs documents from MaintenanceDocumentBase
        // List<ProposalLog> searchList = filterForPermissions(results);
        if (StringUtils.containsIgnoreCase(returnLocation, "negotiationNegotiation")) {
            return cleanSearchResultsForNegotiationLookup(results); // searchResult
        }
        return results; // searchResult
    }

    private List<ProposalLog> cleanSearchResultsForNegotiationLookup(Collection<? extends BusinessObject> searchResults) {
        List<ProposalLog> newResults = new ArrayList<ProposalLog>();
        for (BusinessObject pl : searchResults) {
            ProposalLog proposalLog = (ProposalLog) pl;
            if (StringUtils.isBlank(proposalLog.getInstProposalNumber())) {
                newResults.add(proposalLog);
            }
        }
        return newResults;
    }

}
