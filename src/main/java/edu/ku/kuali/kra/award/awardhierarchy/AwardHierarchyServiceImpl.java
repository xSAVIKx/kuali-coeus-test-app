/*
 * Copyright 2005-2013 The Kuali Foundation
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
package edu.ku.kuali.kra.award.awardhierarchy;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kuali.coeus.common.framework.version.VersioningService;
import org.kuali.coeus.common.framework.version.history.VersionHistoryService;
import org.kuali.coeus.sys.framework.service.KcServiceLocator;
import org.kuali.kra.award.AwardAmountInfoService;
import org.kuali.kra.award.AwardNumberService;
import org.kuali.kra.award.awardhierarchy.AwardHierarchy;
import org.kuali.kra.award.document.AwardDocument;
import org.kuali.kra.award.home.Award;
import org.kuali.kra.award.home.AwardComment;
import org.kuali.kra.award.home.AwardService;
import org.kuali.kra.award.home.approvedsubawards.AwardApprovedSubaward;
import org.kuali.kra.award.notesandattachments.notes.AwardNotepad;
import org.kuali.kra.award.paymentreports.awardreports.AwardReportTerm;
import org.kuali.kra.award.paymentreports.closeout.AwardCloseout;
import org.kuali.kra.award.paymentreports.specialapproval.approvedequipment.AwardApprovedEquipment;
import org.kuali.kra.award.paymentreports.specialapproval.foreigntravel.AwardApprovedForeignTravel;
import org.kuali.kra.award.version.service.AwardVersionService;
import org.kuali.kra.infrastructure.Constants;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.springframework.transaction.annotation.Transactional;

import edu.ku.kuali.kra.award.home.AwardExtension;
import edu.ku.kuali.kra.bo.AwardTransmission;

@Transactional
public class AwardHierarchyServiceImpl extends org.kuali.kra.award.awardhierarchy.AwardHierarchyServiceImpl implements AwardHierarchyService {
    private static final Log LOG = LogFactory.getLog(org.kuali.kra.award.awardhierarchy.AwardHierarchyServiceImpl.class);

    private static final String DOCUMENT_DESCRIPTION_FIELD_NAME = "documentDescription";

    AwardNumberService awardNumberService;
    BusinessObjectService businessObjectService;
    DocumentService documentService;
    VersioningService versioningService;
    VersionHistoryService versionHistoryService;
    AwardAmountInfoService awardAmountInfoService;
    ParameterService parameterService;
    private AwardService awardService;
    AwardVersionService awardVersionService;

    /**
     * Clears all filtered attributes in the new award
     *
     * @param newAward
     *            new award
     */
    protected void clearFilteredAttributes(Award newAward) {
        // setting all financial information to null so copied award can spawn its own
        newAward.setAccountNumber(null);
        newAward.setFinancialAccountCreationDate(null);
        newAward.setFinancialAccountDocumentNumber(null);
        newAward.setFinancialChartOfAccountsCode(null);
        newAward.setNoticeDate(null);
        // simply clear the funding proposals since we haven't saved and they haven't been added to the associated proposal.
        newAward.getFundingProposals().clear();
        newAward.setAwardApprovedSubawards(new ArrayList<AwardApprovedSubaward>());
        newAward.setApprovedEquipmentItems(new ArrayList<AwardApprovedEquipment>());
        newAward.setApprovedForeignTravelTrips(new ArrayList<AwardApprovedForeignTravel>());
        newAward.setAwardNotepads(new ArrayList<AwardNotepad>());

        // BUKC-0014: KC/SAP Interface - Nullify Transmission Date and Transmission History
        ((AwardExtension) newAward.getExtension()).setLastTransmissionDate(null);
        ((AwardExtension) newAward.getExtension()).setAwardTransmissions(new ArrayList<AwardTransmission>());

        try {
            String defaultTxnTypeStr = parameterService.getParameterValueAsString(Constants.MODULE_NAMESPACE_AWARD,
                    ParameterConstants.DOCUMENT_COMPONENT, Constants.DEFAULT_TXN_TYPE_COPIED_AWARD);
            if (StringUtils.isNotEmpty(defaultTxnTypeStr)) {
                newAward.setAwardTransactionTypeCode(Integer.parseInt(defaultTxnTypeStr));
            }
        } catch (Exception e) {
            // do Nothing
        }
        newAward.setAwardCloseoutItems(new ArrayList<AwardCloseout>());

        for (AwardComment comment : newAward.getAwardComments()) {
            if (StringUtils.equals(Constants.CURRENT_ACTION_COMMENT_TYPE_CODE, comment.getCommentType().getCommentTypeCode())) {
                comment.setComments(Constants.DEF_CURRENT_ACTION_COMMENT_COPIED_AWARD);
            }
        }

        newAward.getAwardAttachments().clear();

        newAward.getSyncChanges().clear();
        newAward.getSyncStatuses().clear();
        newAward.getAwardBudgetLimits().clear();

        /**
         * per KRACOEUS-5448 portions of the payment and invoices sub panel items should not be copied.
         */
        List<AwardReportTerm> newTerms = new ArrayList<AwardReportTerm>();
        String paymentReportClassCode = getPaymentAndInvoicesReportClass().getReportClassCode();
        for (AwardReportTerm term : newAward.getAwardReportTermItems()) {
            if (!StringUtils.equals(paymentReportClassCode, term.getReportClassCode())) {
                newTerms.add(term);
            }
        }
        newAward.setAwardReportTermItems(newTerms);
        newAward.getPaymentScheduleItems().clear();
    }

    /**
     * Save Award PlaceHolder document with BU Extension
     */
    protected void savePlaceholderDocument(AwardDocument doc) {
        for (Award award : doc.getAwardList()) {
            if (award.getAwardId() != null && award.getExtension() != null && ((AwardExtension) award.getExtension()).getAwardId() == null) {
                ((AwardExtension) award.getExtension()).setAwardId(award.getAwardId());
            }
        }
        super.savePlaceholderDocument(doc);
        for (Award award : doc.getAwardList()) {
            // This is similar to edit create new version created extension fine. after save it looks
            // fine by debugging.
            // after redirect, only award persisted to table, but extension is not.
            // so, use this hook for now.
            if (GlobalVariables.getUserSession().retrieveObject("new-award:" + award.getAwardNumber()) != null) {
                KcServiceLocator.getService(BusinessObjectService.class).save(award);
                GlobalVariables.getUserSession().removeObject("new-award:" + award.getAwardNumber());
            }
        }

        LOG.info(doc.getAwardList().size());
    }

    /**
     * Add new award to Award PlaceHolder and add it to the session
     * 
     * @param doc
     * @param node
     */
    protected void addNewAwardToPlaceholderDocument(AwardDocument doc, AwardHierarchy node) {
        super.addNewAwardToPlaceholderDocument(doc, node);
        Award award = node.getAward();
        if (award.isNew()) {
            GlobalVariables.getUserSession().addObject("new-award:" + award.getAwardNumber(), (Object) award.getAwardNumber());
        }
    }

}
