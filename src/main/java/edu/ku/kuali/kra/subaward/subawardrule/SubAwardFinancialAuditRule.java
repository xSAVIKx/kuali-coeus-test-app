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
package edu.ku.kuali.kra.subaward.subawardrule;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.kra.infrastructure.Constants;
import org.kuali.kra.subaward.bo.SubAward;
import org.kuali.kra.subaward.bo.SubAwardAmountInfo;
import org.kuali.kra.subaward.document.SubAwardDocument;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.AuditCluster;
import org.kuali.rice.krad.util.AuditError;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.ku.kuali.kra.infrastructure.BUConstants;
import edu.ku.kuali.kra.subaward.bo.SubAwardAmountInfoExtension;

/**
 * This class processes audit rules (warnings)
 * for the Report Information related
 * data of the SubAwardDocument.
 */
public class SubAwardFinancialAuditRule extends org.kuali.kra.subaward.subawardrule.SubAwardFinancialAuditRule {

    private static final String SUBAWARD_FINANCIAL_AUDIT_ERRORS = "subawardFinancialdAuditErrors";
    private List<AuditError> auditErrors;

    /**
     * @see org.kuali.kra.subaward.subawardrule.SubAwardFinancialAuditRule #processRunAuditBusinessRules(org.kuali.core.document.Document)
     * @return boolean
     */
    @Override
    public boolean processRunAuditBusinessRules(Document document) {
        boolean valid = true;
        auditErrors = new ArrayList<AuditError>();

        valid &= super.processRunAuditBusinessRules(document);
        valid &= checkSubAwardAmountInfos(document);

        reportAndCreateFinancialAuditCluster();

        return valid;

    }

    /**
     * This method creates and adds the
     * AuditCluster to the Global AuditErrorMap.
     */
    @Override
    @SuppressWarnings("unchecked")
    protected void reportAndCreateFinancialAuditCluster() {
        if (auditErrors.size() > 0) {
            AuditCluster existingErrors = getAuditErrorMap().get(SUBAWARD_FINANCIAL_AUDIT_ERRORS);
            if (existingErrors == null) {
                getAuditErrorMap().put(SUBAWARD_FINANCIAL_AUDIT_ERRORS,
                        new AuditCluster(Constants.SUBAWARD_FINANCIAL_PANEL_NAME, auditErrors, Constants.AUDIT_ERRORS));
            } else {
                existingErrors.getAuditErrorList().addAll(auditErrors);
            }
        }
    }

    /**
     * .
     * This method is for checking whether obligated amount is zero
     *
     * @param document
     * @return boolean
     */
    protected boolean checkSubAwardAmountInfos(Document document) {
        SubAwardDocument subAwardDocument = (SubAwardDocument) document;
        SubAward subAward = subAwardDocument.getSubAward();

        boolean rulePassed = true;

        int index = 0;
        // BUKC-0143: Validation for - Subaward Financial - Amendment No. field (ENHC0013244)
        // Validate BU's specific fields format
        for (SubAwardAmountInfo amountInfo : subAward.getSubAwardAmountInfoList()) {
            if (!(StringUtils.isBlank(amountInfo.getModificationID()))) {
                String regExp = "^[0-9]{3}$";
                Pattern pattern = Pattern.compile(regExp);
                Matcher matcher = pattern.matcher(amountInfo.getModificationID());
                if (!(matcher.matches())) {
                    auditErrors.add(new AuditError("document.subAwardList[0].subAwardAmountInfoList[" + index + "].modificationID",
                            BUConstants.ERROR_SUBAWARD_MODIFICATION_ID_INVALID, Constants.MAPPING_FINANCIAL_PAGE + "."
                                    + Constants.SUBAWARD_FINANCIAL_PANEL));
                }
            }

            // Validate BU's specific requirement, the ModificationId is required only when Mod Type isn't either New or Converted Records
            if (StringUtils.isNotBlank(((SubAwardAmountInfoExtension) amountInfo.getExtension()).getModificationType())) {
                if (!(((SubAwardAmountInfoExtension) amountInfo.getExtension()).getModificationType().equals("New") || ((SubAwardAmountInfoExtension) amountInfo
                        .getExtension()).getModificationType().equals("Converted Record"))) {
                    if (StringUtils.isBlank(amountInfo.getModificationID())) {

                        auditErrors.add(new AuditError("document.subAwardList[0].subAwardAmountInfoList[" + index + "].modificationID",
                                BUConstants.ERROR_SUBAWARD_MODIFICATION_ID_REQUIRED, Constants.MAPPING_FINANCIAL_PAGE + "."
                                        + Constants.SUBAWARD_FINANCIAL_PANEL));
                    }
                }

                // validate the uniqueness of the values if it is present
                if (StringUtils.isNotBlank(amountInfo.getModificationID())) {
                    for (SubAwardAmountInfo subAwardAmountInfo : subAward.getSubAwardAmountInfoList()) {
                        if (subAwardAmountInfo.getSubAwardAmountInfoId() != amountInfo.getSubAwardAmountInfoId()) {
                            if (ObjectUtils.equals(subAwardAmountInfo.getModificationID(), amountInfo.getModificationID())) {
                                auditErrors.add(new AuditError("document.subAwardList[0].subAwardAmountInfoList[" + index + "].modificationID",
                                        BUConstants.ERROR_SUBAWARD_MODIFICATION_ID_DUPLICATE, Constants.MAPPING_FINANCIAL_PAGE + "."
                                                + Constants.SUBAWARD_FINANCIAL_PANEL));
                            }
                        }
                    }
                }
            }
            index++;
        }
        if (auditErrors.size() > 0) {
            // GlobalVariables.getMessageMap().addToErrorPath("newSubAwardAmountInfo");
            // for (AuditError error:auditErrors){
            // GlobalVariables.getMessageMap().putError("newSubAwardAmountInfo",
            // error.getMessageKey());
            // }
            // GlobalVariables.getMessageMap().removeFromErrorPath("newSubAwardAmountInfo");
            return false;
        }
        return true;
    }

    /**
     * Retrieve GlobalVariables.AuditErrorMap
     *
     * @return auditErrorMap
     */
    protected Map<String, AuditCluster> getAuditErrorMap() {
        return GlobalVariables.getAuditErrorMap();
    }
}
