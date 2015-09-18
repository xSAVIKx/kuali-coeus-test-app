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
package edu.ku.kuali.kra.award;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kra.award.document.AwardDocument;
import org.kuali.kra.award.home.Award;
import org.kuali.kra.infrastructure.Constants;
import org.kuali.rice.kns.util.KNSGlobalVariables;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.AuditCluster;
import org.kuali.rice.krad.util.AuditError;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;

import edu.ku.kuali.kra.award.home.AwardExtension;

/**
 * Extend main Business Rule class for <code>{@link AwardDocument}</code>.
 * Responsible for delegating rules to independent rule classes.
 *
 * @author dhaywood
 */
public class AwardDocumentRule extends org.kuali.kra.award.AwardDocumentRule {

    // BUKC-0012: Default BU's award extension fields values
    public static final String AVC_INDICATOR = "125%";
    public static final String A133_CLUSTER = "M";
    public static final boolean FRINGE_NOT_ALLOWED = false;
    public static final String PROGRAM_INCOME = "No";
    public static final String STOCK_AWARD = "No";
    public static final String INTEREST_EARNED = "Return to sponsor";
    public static final String FEDERAL_RATE_AGREEMENT_DATE = "NA";
    public static final String BU_BMC_FA_SPLIT = "No";
    public static final String FOREIGN_CURRENCY_AWARD = "No";

    // BUKC-0011: Remove Sponsor Contact Required Validation
    private static final String CONTACTS_AUDIT_ERRORS = "contactsAuditErrors";
    private static final String AWARD_SPONSOR_CONTACT_LIST_ERROR_KEY = "document.awardList[0].sponsorContact.auditErrors";
    private static final String ERROR_AWARD_NO_SPONSOR_CONTACTS = "error.awardSponsorContact.none";

    private static final Integer AWARD_TYPE_CODE_SUBGRANT = 6;
    private static final Integer AWARD_TYPE_CODE_SUBCONTRACT = 11;

    // BUKC-0070: Award - Validate Prime Sponsor and Award Type selection
    private static final String AWARD_ERROR_PATH_PREFIX = "document.awardList[0].";
    private static final Integer FEDERAL_CODE = 1;
    private static final String AWARD_HOME_AUDIT_WARNINGS = "homePageAuditWarnings";
    private static final String PRIME_SPONSOR_PRESENT_WITH_INVALID_AWARD_TYPE_WARNING = "warning.award.subcontract.subgrant.required.for.primeSponsor";
    private static final String PRIME_SPONSOR_REQUIRED_FOR_SELECTED_AWARD_TYPE_WARNING = "warning.award.primeSponsor.required.for.subcontract.subgrant";
    private static final String PRIME_SPONSOR_CODE = "primeSponsorCode";
    private static final String AWARD_TYPE_CODE = "awardTypeCode";

    // BUKC-0090: Adding new custom data element needed in KC: FAIN
    // (ENHC0012305)
    private static final Integer AWARD_TYPE_CODE_GRANT = 5;
    private static final Integer AWARD_TYPE_CODE_COOPERATIVEAGREEMENT = 1;
    private static final String AWARD_HOME_AUDIT_ERRORS = "homePageAuditErrors";
    private static final String FAIN = "extension.fain";
    private static final String FAIN_REQUIRED_FOR_AWARD = "error.award.fain.required";

    /**
     * @see org.kuali.rice.krad.rules.DocumentRuleBase#processCustomSaveDocumentBusinessRules(org.kuali.rice.krad.document.Document)
     */
    @Override
    protected boolean processCustomSaveDocumentBusinessRules(Document document) {
        Award award = ((AwardDocument) document).getAward();
        if (award.getExtension() != null && ((AwardExtension) award.getExtension()).getAwardId() == null && award.getAwardId() != null) {
            ((AwardExtension) award.getExtension()).setAwardId(award.getAwardId());
        }

        if (StringUtils.isEmpty(((AwardExtension) award.getExtension()).getAvcIndicator())) {
            ((AwardExtension) award.getExtension()).setAvcIndicator(AVC_INDICATOR);
        }
        if (StringUtils.isEmpty(((AwardExtension) award.getExtension()).getA133Cluster())) {
            ((AwardExtension) award.getExtension()).setA133Cluster(A133_CLUSTER);
        }
        if (((AwardExtension) award.getExtension()).getFringeNotAllowedIndicator() == null) {
            ((AwardExtension) award.getExtension()).setFringeNotAllowedIndicator(FRINGE_NOT_ALLOWED);
        }
        if (StringUtils.isEmpty(((AwardExtension) award.getExtension()).getProgramIncome())) {
            ((AwardExtension) award.getExtension()).setProgramIncome(PROGRAM_INCOME);
        }
        if (StringUtils.isEmpty(((AwardExtension) award.getExtension()).getStockAward())) {
            ((AwardExtension) award.getExtension()).setStockAward(STOCK_AWARD);
        }
        if (StringUtils.isEmpty(((AwardExtension) award.getExtension()).getInterestEarned())) {
            ((AwardExtension) award.getExtension()).setInterestEarned(INTEREST_EARNED);
        }
        if (StringUtils.isEmpty(((AwardExtension) award.getExtension()).getFederalRateDate())) {
            ((AwardExtension) award.getExtension()).setFederalRateDate(FEDERAL_RATE_AGREEMENT_DATE);
        }
        if (StringUtils.isEmpty(((AwardExtension) award.getExtension()).getBuBmcFaSplit())) {
            ((AwardExtension) award.getExtension()).setBuBmcFaSplit(BU_BMC_FA_SPLIT);
        }
        if (StringUtils.isEmpty(((AwardExtension) award.getExtension()).getForeignCurrencyAward())) {
            ((AwardExtension) award.getExtension()).setForeignCurrencyAward(FOREIGN_CURRENCY_AWARD);
        }

        // BUKC-0013: Child Type validation
        // If rule processing is enabled, apply superclass method for foundation
        // edits, then apply new edit for Child Type field.

        if (skipRuleProcessing(document)) {
            return true;
        }
        boolean retval = true;
        AwardDocument awardDocument = (AwardDocument) document;
        MessageMap errorMap = GlobalVariables.getMessageMap();
        retval &= super.processCustomSaveDocumentBusinessRules(document);
        retval &= processChildTypeBusinessRule(errorMap, awardDocument);

        // BUKC-0070: Award - Validate Prime Sponsor and Award Type selection
        processPrimeSponsorAndAwardTypeSelectionBusinessRule(awardDocument);

        // BUKC-0090: Adding new custom data element needed in KC: FAIN
        // (ENHC0012305)
        processFainBusinessRule(awardDocument);

        return retval;
    }

    private boolean skipRuleProcessing(Document document) {
        return AwardDocument.PLACEHOLDER_DOC_DESCRIPTION.equals(document.getDocumentHeader().getDocumentDescription());
    }

    // BUKC-0011: Remove Sponsor Contact Required Validation
    // BUKC-0030: Revise Contact validation to retain PI edit

    /**
     * @see org.kuali.kra.award.AwardDocumentRule#processRunAuditBusinessRules(org.kuali.rice.krad.document.Document)
     *      <p/>
     *      Run method in super class, then look for Sponsor Contact error and remove it from AuditErrorMap
     */
    @Override
    @Deprecated
    public boolean processRunAuditBusinessRules(Document document) {

        // Apply Foundation business rules from parent class
        super.processRunAuditBusinessRules(document);

        // BUKC-0070: Award - Validate Prime Sponsor and Award Type selection
        AwardDocument awardDocument = (AwardDocument) document;
        processPrimeSponsorAndAwardTypeSelectionBusinessRule(awardDocument);

        // Remove *all* contact audit error group from error map, remove Sponsor
        // Contact edit error from group, put back remainder.
        AuditCluster contactAuditErrors = getAuditErrorMap().get(CONTACTS_AUDIT_ERRORS);

        if (contactAuditErrors != null) {
            Iterator<AuditError> iterator = contactAuditErrors.getAuditErrorList().iterator();
            while (iterator.hasNext()) {
                AuditError ver = iterator.next();
                String errorKey = ver.getErrorKey();
                if (StringUtils.equals(AWARD_SPONSOR_CONTACT_LIST_ERROR_KEY, errorKey)) {
                    iterator.remove();
                    break; // break after removing sponsor contact error
                }
            }
            getAuditErrorMap().remove(CONTACTS_AUDIT_ERRORS);
            // BUKC-0034: Revise Contact validation to correct problem with the
            // validation code
            if (!contactAuditErrors.getAuditErrorList().isEmpty()) {
                getAuditErrorMap().put(CONTACTS_AUDIT_ERRORS, contactAuditErrors);
            }
        }

        // Return 'true' if there are no errors.
        return getAuditErrorMap().isEmpty();

    }

    // BUKC-0091: Improve award's child type validation and hide child type and
    // desc on parent awards
    private boolean processChildTypeBusinessRule(MessageMap errorMap, AwardDocument awardDocument) {

        Award award = awardDocument.getAward();
        errorMap.addToErrorPath(DOCUMENT_ERROR_PATH);
        errorMap.addToErrorPath(AWARD_ERROR_PATH);

        boolean success = true;
        boolean isParentAward = award.getAwardNumber().endsWith("-00001");

        if (!isParentAward && StringUtils.isBlank(((AwardExtension) award.getExtension()).getChildType())) {
            errorMap.putError("extension.childType", "error.award.childType.required", award.getAwardNumber());
            success = false;
        }

        errorMap.removeFromErrorPath(AWARD_ERROR_PATH);
        errorMap.removeFromErrorPath(DOCUMENT_ERROR_PATH);
        return success;
    }

    /**
     * this method is called only when the user tries to submit the award.
     * 
     * @param awardDocument
     * @return
     */
    public boolean processFainRequiredBusinessRule(AwardDocument awardDocument) {
        MessageMap errorMap = GlobalVariables.getMessageMap();

        Award award = awardDocument.getAward();
        errorMap.addToErrorPath(DOCUMENT_ERROR_PATH);
        errorMap.addToErrorPath(AWARD_ERROR_PATH);

        boolean success = true;

        boolean isParentAward = award.getAwardNumber().endsWith("-00001") || award.getAwardNumber().endsWith("-00000");
        if (isParentAward) {
            if (isFederalSponsor(award)) {
                if (isGrant(award) || isCooperativeAgreement(award) || isSubGrant(award)) {
                    if (StringUtils.isBlank(((AwardExtension) award.getExtension()).getFain())) {
                        errorMap.putError(FAIN, FAIN_REQUIRED_FOR_AWARD, award.getAwardNumber());
                        success = false;
                    }
                }
            }
        }

        errorMap.removeFromErrorPath(AWARD_ERROR_PATH);
        errorMap.removeFromErrorPath(DOCUMENT_ERROR_PATH);
        return success;
    }

    /**
     * Process business rule to show an error when Fain is null and sponsor type
     * OR prime sponsor type = Federal AND award type = grant (5), cooperative
     * agreement (1) OR subgrant (6) - This method will allow the award top be
     * saved but it'll show an error on the form
     *
     * @param awardDocument
     */
    private boolean processFainBusinessRule(AwardDocument awardDocument) {
        Award award = awardDocument.getAward();
        boolean retVal = true;
        String link = Constants.MAPPING_AWARD_HOME_PAGE + "." + Constants.MAPPING_AWARD_HOME_DETAILS_AND_DATES_PAGE_ANCHOR;
        AuditCluster awardHomeAuditErrorsClustor = getAuditErrorMap().get(AWARD_HOME_AUDIT_ERRORS);
        List<AuditError> auditErrors = new ArrayList<AuditError>();
        if (!(awardHomeAuditErrorsClustor == null)) {
            auditErrors = awardHomeAuditErrorsClustor.getAuditErrorList();
        }

        boolean isParentAward = award.getAwardNumber().endsWith("-00001") || award.getAwardNumber().endsWith("-00000");
        if (isParentAward) {
            if (isFederalSponsor(award)) {
                if (isGrant(award) || isCooperativeAgreement(award) || isSubGrant(award)) {
                    if (StringUtils.isBlank(((AwardExtension) award.getExtension()).getFain())) {
                        String errorKey = AWARD_ERROR_PATH_PREFIX + FAIN;
                        auditErrors.add(new AuditError(errorKey, FAIN_REQUIRED_FOR_AWARD, link));
                        retVal = false;
                    }
                }
            }
        }

        if (auditErrors.size() > 0) {
            getAuditErrorMap().put(AWARD_HOME_AUDIT_ERRORS,
                    new AuditCluster(Constants.MAPPING_AWARD_HOME_DETAILS_AND_DATES_PAGE_NAME, auditErrors, Constants.AUDIT_ERRORS));
        }
        return retVal;
    }

    /**
     * Process business rule to show a warning when award type is either
     * subgrant or subcontract and prime sposor is not entered or if prime
     * sponsor is present but the award type is not subgrant nor subgrant, this
     * validation only applies on federal accounts.
     *
     * @param awardDocument
     */
    private void processPrimeSponsorAndAwardTypeSelectionBusinessRule(AwardDocument awardDocument) {
        Award award = awardDocument.getAward();
        String link = Constants.MAPPING_AWARD_HOME_PAGE + "." + Constants.MAPPING_AWARD_HOME_DETAILS_AND_DATES_PAGE_ANCHOR;
        AuditCluster awardHomeAuditWarningsClustor = getAuditErrorMap().get(AWARD_HOME_AUDIT_WARNINGS);
        List<AuditError> auditWarnings = new ArrayList<AuditError>();
        if (!(awardHomeAuditWarningsClustor == null)) {
            auditWarnings = awardHomeAuditWarningsClustor.getAuditErrorList();
        }

        // validate only if the account is federal
        if (isFederalAccount(award)) {
            if (StringUtils.isBlank(award.getPrimeSponsorCode()) && (isSubGrant(award) || isSubContract(award))) {
                String errorKey = AWARD_ERROR_PATH_PREFIX + PRIME_SPONSOR_CODE;
                auditWarnings.add(new AuditError(errorKey, PRIME_SPONSOR_REQUIRED_FOR_SELECTED_AWARD_TYPE_WARNING, link));
            }

            if (!StringUtils.isBlank(award.getPrimeSponsorCode()) && !(award.getPrimeSponsorCode().equals(award.getSponsorCode()))) {
                if (!(isSubGrant(award) || isSubContract(award))) {
                    String errorKey = AWARD_ERROR_PATH_PREFIX + AWARD_TYPE_CODE;
                    auditWarnings.add(new AuditError(errorKey, PRIME_SPONSOR_PRESENT_WITH_INVALID_AWARD_TYPE_WARNING, link));
                }

            }
        }
        if (auditWarnings.size() > 0) {
            getAuditErrorMap().put(AWARD_HOME_AUDIT_WARNINGS,
                    new AuditCluster(Constants.MAPPING_AWARD_HOME_DETAILS_AND_DATES_PAGE_NAME, auditWarnings, Constants.AUDIT_WARNINGS));
        }
    }

    /**
     * Determines if the given Award sponsor or prime sponsor is a federal
     * sponsor. .
     * 
     * @param award
     *            the Award to check to determine if it is Sponsor or Prime
     *            Sponsor type is federal or not
     * @return true if the given award is a a federal sponsor is on the account,
     *         false if not
     */
    protected boolean isFederalSponsor(Award award) {
        if (!(award.getPrimeSponsor() == null)) {
            return Integer.parseInt(award.getPrimeSponsor().getSponsorTypeCode()) == FEDERAL_CODE;
        } else {
            return Integer.parseInt(award.getSponsor().getSponsorTypeCode()) == FEDERAL_CODE;
        }
    }

    /**
     * Determines if the given Award is a federal account. This is based on the
     * description of the AccountTypeCode on the Award.
     *
     * @param award
     *            the Award to check to determine if it is a federal account or
     *            not
     * @return true if the given award is a a federal account, false if not
     */
    protected boolean isFederalAccount(Award award) {
        if (award.getAccountTypeCode() != null) {
            return award.getAccountTypeCode().equals(FEDERAL_CODE);
        }
        return true;
    }

    /**
     * Determines if the given Award is a Sub-grant. This is based on the
     * description of the AwardType on the Award.
     *
     * @param award
     *            the Award to check to determine if it is a sub-grant or not
     * @return true if the given award is a sub-grant, false if not
     */
    protected boolean isSubGrant(Award award) {
        if (award.getAwardTypeCode() != null) {
            return AWARD_TYPE_CODE_SUBGRANT.equals(award.getAwardTypeCode());
        }
        return true;
    }

    /**
     * Determines if the given Award is a Sub-contract. This is based on the
     * description of the AwardType on the Award.
     *
     * @param award
     *            the Award to check to determine if it is a sub-contract or not
     * @return true if the given award is a sub-contract, false if not
     */
    protected boolean isSubContract(Award award) {
        if (award.getAwardTypeCode() != null) {
            return AWARD_TYPE_CODE_SUBCONTRACT.equals(award.getAwardTypeCode());
        }
        return true;
    }

    /**
     * Determines if the given Award is a grant. This is based on the
     * description of the AwardType on the Award.
     *
     * @param award
     *            the Award to check to determine if it is a sub-grant or not
     * @return true if the given award is a grant, false if not
     */
    protected boolean isGrant(Award award) {
        if (award.getAwardTypeCode() != null) {
            return AWARD_TYPE_CODE_GRANT.equals(award.getAwardTypeCode());
        }
        return true;
    }

    /**
     * Determines if the given Award is a Cooperative Agreement. This is based
     * on the description of the AwardType on the Award.
     *
     * @param award
     *            the Award to check to determine if it is a Cooperative
     *            Agreement or not
     * @return true if the given award is a grant, false if not
     */
    protected boolean isCooperativeAgreement(Award award) {
        if (award.getAwardTypeCode() != null) {
            return AWARD_TYPE_CODE_COOPERATIVEAGREEMENT.equals(award.getAwardTypeCode());
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
