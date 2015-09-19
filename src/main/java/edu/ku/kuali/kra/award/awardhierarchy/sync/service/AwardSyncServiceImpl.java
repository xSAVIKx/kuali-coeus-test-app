/*
 * Copyright 2005-2014 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.ku.kuali.kra.award.awardhierarchy.sync.service;

// BUKC-0051: Prevent validation error on Award Hierarchy sync
// Reference BU version of Award Document rules to skip validation of sponsor contact
import edu.ku.kuali.kra.award.AwardDocumentRule;

import org.apache.commons.lang.StringUtils;
import org.kuali.kra.award.awardhierarchy.sync.AwardSyncLog;
import org.kuali.kra.award.awardhierarchy.sync.AwardSyncStatus;
import org.kuali.kra.award.awardhierarchy.sync.service.AwardSyncUtilityService;
import org.kuali.kra.award.document.AwardDocument;

/**
 * Award Hierarchy Sync Service Implementation.
 */
public class AwardSyncServiceImpl extends org.kuali.kra.award.awardhierarchy.sync.service.AwardSyncServiceImpl {

    private AwardSyncUtilityService awardSyncUtilityService;

    /**
     * Run the {@link AwardDocumentRule#processSaveDocument} and {@link AwardDocumentRule#processRunAuditBusinessRules} against award. Add all
     * messages generated from running the rules to logList as {@link AwardSyncLog}.
     * Return false if any of the error keys generated by the rules are not in the ignoredMessageKeys list.
     * 
     * @param award
     * @param logs
     * @return
     */
    @Override
    @SuppressWarnings("deprecation")
    protected boolean validateModifiedAward(AwardDocument award, AwardSyncStatus awardStatus) {
        AwardDocumentRule rule = new AwardDocumentRule();
        boolean result = true;
        if (!rule.processSaveDocument(award)) {
            getAwardSyncUtilityService().getLogsFromSaveErrors(awardStatus);
        }
        if (!rule.processRunAuditBusinessRules(award)) {
            getAwardSyncUtilityService().getLogsFromAuditErrors(awardStatus);
        }
        String[] ignoredErrors = IGNORED_MESSAGE_KEYS.split(",");
        for (AwardSyncLog log : awardStatus.getValidationLogs()) {
            if (!StringUtils.startsWithAny(log.getMessageKey(), ignoredErrors) && !log.isSuccess()) {
                result = false;
            } else {
                log.setSuccess(true);
            }
        }
        return result;
    }

    @Override
    protected AwardSyncUtilityService getAwardSyncUtilityService() {
        return awardSyncUtilityService;
    }

}