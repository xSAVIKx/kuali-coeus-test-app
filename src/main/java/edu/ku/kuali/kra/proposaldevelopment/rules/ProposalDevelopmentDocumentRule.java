/*
 * Copyright 2005-2010 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.ku.kuali.kra.proposaldevelopment.rules;

import org.apache.commons.lang3.StringUtils;
import org.kuali.coeus.common.api.sponsor.SponsorService;
import org.kuali.coeus.propdev.impl.core.DevelopmentProposal;
import org.kuali.coeus.propdev.impl.core.ProposalDevelopmentDocument;
import org.kuali.coeus.sys.framework.util.DateUtils;
import org.kuali.kra.infrastructure.Constants;
import org.kuali.kra.infrastructure.KeyConstants;
import org.kuali.rice.core.api.util.RiceKeyConstants;
import org.kuali.rice.kns.service.DataDictionaryService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;

/**
 * Main Business Rule class for <code>{@link org.kuali.kra.proposaldevelopment.document.ProposalDevelopmentDocument}</code>. Responsible for
 * delegating rules to independent rule classes.
 *
 * @see org.kuali.kra.proposaldevelopment.rules.ProposalDevelopmentDocumentRule
 */
public class ProposalDevelopmentDocumentRule extends org.kuali.coeus.propdev.impl.core.ProposalDevelopmentDocumentRule {

    /**
     * Validate Sponsor/program Information rule. Regex validation for CFDA number(7 digits with a period in the 3rd character and an optional alpha
     * character in the 7th field).
     *
     * @see org.kuali.kra.proposaldevelopment.rules.ProposalDevelopmentDocumentRule#processSponsorProgramBusinessRule(org.kuali.kra.proposaldevelopment.document.ProposalDevelopmentDocument)
     */
    private boolean processSponsorProgramBusinessRule(ProposalDevelopmentDocument proposalDevelopmentDocument) {

        boolean valid = true;
        // BUKC-0008: Allow 2.2, 2.3, and 2.3a-zA-Z format for CFDA number
        // String regExpr = "(\\d{2})(\\.)(\\d{3})[a-zA-z]?";
        String regExpr = "(\\d{2})(\\.)(\\d{2})|(\\d{2})(\\.)(\\d{3})[a-zA-z]?";
        MessageMap errorMap = GlobalVariables.getMessageMap();
        DataDictionaryService dataDictionaryService = getDataDictionaryService();
        if (StringUtils.isNotBlank(proposalDevelopmentDocument.getDevelopmentProposal().getCfdaNumber())
                && !(proposalDevelopmentDocument.getDevelopmentProposal().getCfdaNumber().matches(regExpr))
                && GlobalVariables.getMessageMap().getMessages("document.developmentProposalList[0].cfdaNumber") == null) {
            errorMap.putError("developmentProposalList[0].cfdaNumber", RiceKeyConstants.ERROR_INVALID_FORMAT, new String[] {
                    dataDictionaryService.getAttributeErrorLabel(DevelopmentProposal.class, "cfdaNumber"),
                    proposalDevelopmentDocument.getDevelopmentProposal().getCfdaNumber()
            });
            valid = false;
        }

        SponsorService sponsorService = getSponsorService();
        String sponsorCode = proposalDevelopmentDocument.getDevelopmentProposal().getPrimeSponsorCode();

        if (sponsorCode != null) {
            String sponsorName = sponsorService.getSponsorName(sponsorCode);
            if (sponsorName == null) {
                errorMap.putError("developmentProposalList[0].primeSponsorCode", RiceKeyConstants.ERROR_EXISTENCE, new String[] {
                        dataDictionaryService.getAttributeLabel(DevelopmentProposal.class, "primeSponsorCode")
                });
                valid = false;
            }
        }
        if (proposalDevelopmentDocument.getDevelopmentProposal().getDeadlineTime() != null) {

            String deadLineTime = DateUtils.formatFrom12Or24Str(proposalDevelopmentDocument.getDevelopmentProposal().getDeadlineTime());
            if (!deadLineTime.equalsIgnoreCase(Constants.INVALID_TIME)) {
                proposalDevelopmentDocument.getDevelopmentProposal().setDeadlineTime(deadLineTime);
            } else {
                errorMap.putError("deadlineTime", KeyConstants.INVALID_DEADLINE_TIME,
                        dataDictionaryService.getAttributeErrorLabel(DevelopmentProposal.class, "deadlineTime"));
                valid = false;
            }
        }
        return valid;
    }
}
