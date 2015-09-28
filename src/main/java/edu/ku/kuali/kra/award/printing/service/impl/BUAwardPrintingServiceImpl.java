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

package edu.ku.kuali.kra.award.printing.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.kuali.coeus.common.budget.framework.nonpersonnel.BudgetRateAndBase;
import org.kuali.coeus.common.framework.unit.admin.UnitAdministrator;
import org.kuali.coeus.common.framework.version.VersionStatus;
import org.kuali.coeus.common.framework.version.history.VersionHistory;
import org.kuali.coeus.common.framework.version.sequence.owner.SequenceOwner;
import org.kuali.coeus.sys.api.model.ScaleTwoDecimal;
import org.kuali.coeus.sys.framework.service.KcServiceLocator;
import org.kuali.kra.award.AwardAmountInfoService;
import org.kuali.kra.award.awardhierarchy.AwardHierarchy;
import org.kuali.kra.award.awardhierarchy.AwardHierarchyService;
import org.kuali.kra.award.budget.AwardBudgetExt;
import org.kuali.kra.award.contacts.AwardPerson;
import org.kuali.kra.award.contacts.AwardUnitContact;
import org.kuali.kra.award.document.AwardDocument;
import org.kuali.kra.award.home.Award;
import org.kuali.kra.award.home.AwardAmountInfo;
import org.kuali.kra.award.home.AwardComment;
import org.kuali.kra.award.home.AwardSponsorTerm;
import org.kuali.kra.award.paymentreports.awardreports.AwardReportTerm;
import org.kuali.kra.award.timeandmoney.AwardDirectFandADistribution;
import org.kuali.kra.timeandmoney.document.TimeAndMoneyDocument;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.service.BusinessObjectService;

import edu.ku.kuali.kra.award.home.AwardExtension;
import edu.ku.kuali.kra.award.printing.BUAwardPrintAdmin;
import edu.ku.kuali.kra.award.printing.BUAwardPrintAmountInfo;
import edu.ku.kuali.kra.award.printing.BUAwardPrintAntAmt;
import edu.ku.kuali.kra.award.printing.BUAwardPrintCumObAmt;
import edu.ku.kuali.kra.award.printing.BUAwardPrintDO;
import edu.ku.kuali.kra.award.printing.BUAwardPrintReportReq;
import edu.ku.kuali.kra.award.printing.BUAwardPrintTerm;
import edu.ku.kuali.kra.award.printing.service.BUAwardPrintingService;
import edu.ku.kuali.kra.infrastructure.BUConstants;

/**
 * This class represents BU's version of award notification (BUKC-0022)
 */
public class BUAwardPrintingServiceImpl implements BUAwardPrintingService {

    public static final String SEQUENCE_OWNER_CLASS_NAME_FIELD = "sequenceOwnerClassName";
    public static final String SEQUENCE_OWNER_REFERENCE_VERSION_NAME = "sequenceOwnerVersionNameValue";

    // Only show DAs in DA section - exclude any flag not equal to 'U:Unit' such as 'C:Central'
    private static final String UNIT_ADMIN_GROUP_FLAG = "U";

    // Only show general comments on the form
    private static final String GENERAL_COMMENTS = "2";

    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;
    private AwardAmountInfoService awardAmountInfoService;
    private AwardHierarchyService awardHierarchyService;

    /**
     * This method gets BU's specific Award Print DO (BUKC-0021)
     *
     * @param reportParameters
     *            objects
     * @return awardPrintDO
     */
    @Override
    public BUAwardPrintDO getAwardPrintDO(Map<String, Object> reportParameters) {
        Award rootAward = (Award) reportParameters.get("rootAward");
        List<TimeAndMoneyDocument> timeAndMoneyList = new ArrayList<TimeAndMoneyDocument>();
        timeAndMoneyList = (List<TimeAndMoneyDocument>) reportParameters.get("timeAndMoneyList");
        BUAwardPrintDO awardPrintDO = new BUAwardPrintDO();
        awardPrintDO = this.populateBUAwardPrintDO(rootAward, timeAndMoneyList);

        return awardPrintDO;
    }

    /**
     * This method builds award print DO objects
     *
     * @param award
     *            in progress
     * @param timeAndMoneyList
     *            selected T&M list
     * @return awardPrintDO
     */
    private BUAwardPrintDO populateBUAwardPrintDO(Award award, List<TimeAndMoneyDocument> timeAndMoneyList) {
        BUAwardPrintDO awardPrintDO = new BUAwardPrintDO();
        List<BUAwardPrintAmountInfo> amountInfoList = populateBUAwardPrintAmountInfo(award, timeAndMoneyList);
        awardPrintDO.setAmountInfo(amountInfoList);
        BUAwardPrintAntAmt antAmt = populateBUAwardPrintAntAmt(award);
        awardPrintDO.setAntAmt(antAmt);
        awardPrintDO.setComments(populateBUAwardPrintComments(award));
        awardPrintDO.setTimeAndMoneyDocumentNumbers(populateTimeAndMoneyDocumentNumbers(timeAndMoneyList));
        BUAwardPrintCumObAmt cumObAmt = populateBUAwardPrintCumObAmt(award);
        awardPrintDO.setCumObAmt(cumObAmt);
        List<BUAwardPrintAdmin> deptAdmins = populateBUAwardPrintDeptAdmin(award);
        awardPrintDO.setDeptAdmin(deptAdmins);
        List<BUAwardPrintAdmin> ospAdmins = populateBUAwardPrintOSPAdmin(award);
        awardPrintDO.setOspAdmin(ospAdmins);
        List<BUAwardPrintAdmin> pafoAdmins = populateBUAwardPrintPafoAdmin(award);
        awardPrintDO.setPafoAdmin(pafoAdmins);
        awardPrintDO.setFundsCenter(award.getLeadUnitNumber() + "-" + award.getLeadUnit().getUnitName());
        //awardPrintDO.setGrantNumber(((AwardExtension) award.getExtension()).getGrantNumber());
        awardPrintDO.setInvestigators(award.getInvestigators());
        awardPrintDO.setPrimeSponsor(award.getPrimeSponsor() != null ? award.getPrimeSponsor().getSponsorName() : null);
        awardPrintDO.setSponsor(award.getSponsorName());
        awardPrintDO.setSponsorAwardId(award.getSponsorAwardNumber());
        awardPrintDO.setTitle(award.getTitle());
        awardPrintDO.setAccountNumber(award.getAccountNumber());
        awardPrintDO.setAwardNumber(award.getAwardNumber());
        awardPrintDO.setTransactionDate(award.getAwardEffectiveDate());
        awardPrintDO.setTransactionType(award.getAwardTransactionType().getDescription());
        List<BUAwardPrintReportReq> reportReqs = populateAwardPrintReportReq(award);
        awardPrintDO.setReportReqs(reportReqs);
        awardPrintDO.setSponsorAwardId(award.getSponsorAwardNumber());
        //List<BUAwardPrintTerm> terms = populateAwardPrintTerms(award);
        //awardPrintDO.setTerms(terms);

        return awardPrintDO;
    }

    /**
     * This method populates terms section on the notification. It excludes terms that
     * are listed in the system parameter. These terms are generics and should be suppressed.
     * The code builds three arrays from the data retrieved from the system parameters, then
     * matches each array element bu index to the current award term Id, Term Cd, and Term Type Cd.
     * If matched, then don't add it to the term list.
     *
     * @param award
     *            to generate the notification for
     * @return award terms to print on the notification
     */
    private List<BUAwardPrintTerm> populateAwardPrintTerms(Award award) {
        ParameterService parameterService = getParameterService();

        List<BUAwardPrintTerm> terms = new ArrayList<BUAwardPrintTerm>();
        List<AwardSponsorTerm> sponsorTerms = award.getAwardSponsorTerms();

        String suppressedTermIds = parameterService.getParameterValueAsString(AwardDocument.class, BUConstants.AWARD_NOTIFICATION_SUPRESS_TERM_ID);
        String suppressedTermCds = parameterService.getParameterValueAsString(AwardDocument.class, BUConstants.AWARD_NOTIFICATION_SUPRESS_TERM_CD);
        String suppressedTermTypeCds = parameterService.getParameterValueAsString(AwardDocument.class,
                BUConstants.AWARD_NOTIFICATION_SUPRESS_TERM_TYPE_CD);

        Map<Long, String[]> suppressed = buildSuppressed(suppressedTermIds, suppressedTermCds, suppressedTermTypeCds);

        for (AwardSponsorTerm sponsorTerm : sponsorTerms) {
            BUAwardPrintTerm term = new BUAwardPrintTerm();

            if (!isSuppressed(suppressed, sponsorTerm.getSponsorTermId(), sponsorTerm.getSponsorTermCode(), sponsorTerm.getSponsorTermTypeCode())) {
                term.setTermDesc(sponsorTerm.getDescription());
                term.setTermType(sponsorTerm.getSponsorTerm().getSponsorTermType().getDescription());
            }

            terms.add(term);
        }

        return terms;
    }

    /**
     * This method builds the arrays for TermId, TermCd, TermTypeCd from the system parameter service. The data stored as a string with
     * comma delimiter
     *
     * @param suppressedTermIds
     *            Terms IDs as a string with comma delimiter
     * @param suppressedTermCodes
     *            Term Codes as a string with comma delimiter
     * @param suppressedTermTypeCodes
     *            Term Type Codes as a string with comma delimiter
     * @return map of the combinations to be suppressed
     */
    private Map<Long, String[]> buildSuppressed(String suppressedTermIds, String suppressedTermCodes, String suppressedTermTypeCodes) {
        String[] termIds = suppressedTermIds.split(",");
        String[] termCodes = suppressedTermCodes.split(",");
        String[] termTypeCodes = suppressedTermTypeCodes.split(",");
        Map<Long, String[]> suppressed = new HashMap<Long, String[]>();
        for (int i = 0; i < termIds.length; i++) {
            suppressed.put(Long.parseLong(termIds[i]), new String[] {
                    termCodes[i], termTypeCodes[i]
            });
        }

        return suppressed;
    }

    /**
     * Checks if the current combos should be suppressed
     *
     * @param suppressed
     *            list
     * @param termId
     *            current term Id
     * @param termCd
     *            current term code
     * @param termTypeCd
     *            current term type code
     * @return boolean value if the current combo should dbe suppressed or not
     */
    private boolean isSuppressed(Map<Long, String[]> suppressed, Long termId, String termCd, String termTypeCd) {
        boolean suppress = false;
        if (suppressed.containsKey(termId)) {
            String[] termCodeTermTypeCombo = suppressed.get(termId);
            if (termCodeTermTypeCombo[0].equals(termCd) && termCodeTermTypeCombo[1].equals(termTypeCd)) {
                suppress = true;
            }
        }

        return suppress;
    }

    /**
     * This method populates Report Class section on the notification.
     *
     * @param award
     *            to generate the notification for
     * @return report requirements to print on the notification
     */
    private List<BUAwardPrintReportReq> populateAwardPrintReportReq(Award award) {
        List<BUAwardPrintReportReq> reportReqs = new ArrayList<BUAwardPrintReportReq>();
        List<AwardReportTerm> awardReportTerms = award.getAwardReportTermItems();

        for (AwardReportTerm awardReportTerm : awardReportTerms) {
            BUAwardPrintReportReq reportReq = new BUAwardPrintReportReq();
            reportReq.setReportClass(awardReportTerm.getReportClass().getDescription());
            reportReq.setFrequency(awardReportTerm.getFrequency().getDescription());
            reportReq.setReportType(awardReportTerm.getReport().getDescription());
            reportReqs.add(reportReq);
        }

        return reportReqs;
    }

    /**
     * This method populates Comment section on the notification. Only show the award general comments
     *
     * @param award
     *            to generate the notification for
     * @return comments object
     */
    private List<String> populateBUAwardPrintComments(Award award) {
        List<String> comments = new ArrayList<String>();

        List<AwardComment> awardComments = award.getAwardComments();

        for (AwardComment awardComment : awardComments) {
            if (awardComment.getCommentTypeCode().equals(GENERAL_COMMENTS)) {
                comments.add(awardComment.getComments());
            }
        }

        return comments;
    }

    /**
     * This method returns a list of T&M document number to display on the form
     *
     * @param timeAndMoneyList
     *            the selected T&M list
     * @return a list of selected T&M documents to then format it as a string to add it to the notification for reference
     */
    private List<String> populateTimeAndMoneyDocumentNumbers(List<TimeAndMoneyDocument> timeAndMoneyList) {
        List<String> timeAndMoneyDocumentNumbers = new ArrayList<String>();
        for (TimeAndMoneyDocument timeAndMoney : timeAndMoneyList) {
            timeAndMoneyDocumentNumbers.add(timeAndMoney.getDocumentNumber());
        }

        return timeAndMoneyDocumentNumbers;
    }

    /**
     * This method populates OSP administrators section on the notification.
     *
     * @param award
     *            to generate the notification for
     * @return OSP administrators to print on the notification
     */
    @SuppressWarnings("unchecked")
    private List<BUAwardPrintAdmin> populateBUAwardPrintOSPAdmin(Award award) {
        List<BUAwardPrintAdmin> ospAdmins = new ArrayList<BUAwardPrintAdmin>();
        List<UnitAdministrator> unitAdmins = new ArrayList<UnitAdministrator>();
        Map<String, Object> fieldValues = new HashMap<String, Object>();
        fieldValues.put("unitNumber", award.getLeadUnitNumber());
        List<UnitAdministrator> unitAdministrators = (List<UnitAdministrator>) getBusinessObjectService().findMatching(UnitAdministrator.class,
                fieldValues);
        for (UnitAdministrator unitAdministrator : unitAdministrators) {
            if (unitAdministrator.getUnitAdministratorTypeCode().equals(AdminStatus.OSP.getStatusCode())) {
                unitAdmins.add(unitAdministrator);
            }
        }
        for (UnitAdministrator unitAdministrator : unitAdmins) {
            BUAwardPrintAdmin printAdmin = new BUAwardPrintAdmin();
            printAdmin.setName(unitAdministrator.getPerson().getFullName());
            printAdmin.setEmail(unitAdministrator.getPerson().getEmailAddress());
            printAdmin.setPhone(unitAdministrator.getPerson().getPhoneNumber());
            ospAdmins.add(printAdmin);
        }
        for (BUAwardPrintAdmin buAwardPrintAdmin : getListOfPafoOrOspAdminsOnUnitContact(award, AdminStatus.OSP)) {
            if (!ospAdmins.contains(buAwardPrintAdmin)) {
                ospAdmins.add(buAwardPrintAdmin);
            }
        }
        return ospAdmins;
    }

    /**
     * This method populates PAFO administrators section on the notification.
     *
     * @param award
     *            to generate the notification for
     * @return PAFO administrators to print on the notification
     */
    @SuppressWarnings("unchecked")
    private List<BUAwardPrintAdmin> populateBUAwardPrintPafoAdmin(Award award) {
        List<BUAwardPrintAdmin> pafoAdmins = new ArrayList<BUAwardPrintAdmin>();
        List<UnitAdministrator> unitPafoAdmins = new ArrayList<UnitAdministrator>();
        Map<String, Object> fieldValues = new HashMap<String, Object>();
        fieldValues.put("unitNumber", award.getLeadUnitNumber());
        List<UnitAdministrator> unitAdministrators = (List<UnitAdministrator>) getBusinessObjectService().findMatching(UnitAdministrator.class,
                fieldValues);
        for (UnitAdministrator unitAdministrator : unitAdministrators) {
            if (unitAdministrator.getUnitAdministratorTypeCode().equals(AdminStatus.PAFO.getStatusCode())) {
                unitPafoAdmins.add(unitAdministrator);
            }
        }
        for (UnitAdministrator unitAdministrator : unitPafoAdmins) {
            BUAwardPrintAdmin printAdmin = new BUAwardPrintAdmin();
            printAdmin.setName(unitAdministrator.getPerson().getFullName());
            printAdmin.setEmail(unitAdministrator.getPerson().getEmailAddress());
            printAdmin.setPhone(unitAdministrator.getPerson().getPhoneNumber());
            pafoAdmins.add(printAdmin);
        }

        for (BUAwardPrintAdmin buAwardPrintAdmin : getListOfPafoOrOspAdminsOnUnitContact(award, AdminStatus.PAFO)) {
            if (!pafoAdmins.contains(buAwardPrintAdmin)) {
                pafoAdmins.add(buAwardPrintAdmin);
            }
        }

        // BUKC-0140: Add new Admin Type (Clinical Trial Admin)
        for (BUAwardPrintAdmin buAwardPrintAdmin : getListOfPafoOrOspAdminsOnUnitContact(award, AdminStatus.CTAD)) {
            if (!pafoAdmins.contains(buAwardPrintAdmin)) {
                pafoAdmins.add(buAwardPrintAdmin);
            }
        }

        return pafoAdmins;
    }

    /**
     * This method is fetching in th list of OSP OR PAFO admins wh are in contact table based on parameter passed of type AdminStatus type
     * This method...
     *
     * @param award
     * @param type
     * @return
     */
    private List<BUAwardPrintAdmin> getListOfPafoOrOspAdminsOnUnitContact(Award award, AdminStatus type) {
        List<BUAwardPrintAdmin> deptAdmins = new ArrayList<BUAwardPrintAdmin>();

        List<AwardUnitContact> unitContacts = new ArrayList<AwardUnitContact>();
        Map<String, Object> fieldValues = new HashMap<String, Object>();
        fieldValues.put("awardNumber", award.getAwardNumber());
        // BUKC-0055: Award Notification: Get OSP/PAFO Admins data on the last award version if added to Unit Contact tab
        fieldValues.put("sequenceNumber", award.getSequenceNumber());

        List<AwardUnitContact> matchingContacts = (List<AwardUnitContact>) getBusinessObjectService().findMatching(AwardUnitContact.class,
                fieldValues);
        for (AwardUnitContact contact : matchingContacts) {
            if (contact.getUnitAdministratorTypeCode().equals(type.statusCode)) {
                unitContacts.add(contact);
            }
        }

        for (AwardUnitContact unitContact : unitContacts) {
            BUAwardPrintAdmin deptAdmin = new BUAwardPrintAdmin();
            deptAdmin.setEmail(unitContact.getEmailAddress());
            deptAdmin.setName(unitContact.getFullName());
            deptAdmin.setPhone(unitContact.getPhoneNumber());

            deptAdmins.add(deptAdmin);
        }

        return deptAdmins;
    }

    /**
     * This method populates department administrators section on the notification.
     *
     * @param award
     *            to generate the notification for
     * @return department administrators to print on the notification
     */
    private List<BUAwardPrintAdmin> populateBUAwardPrintDeptAdmin(Award award) {
        List<BUAwardPrintAdmin> deptAdmins = new ArrayList<BUAwardPrintAdmin>();

        Award activeAward = getActiveAwardVersion(award.getAwardNumber());

        List<AwardUnitContact> unitContacts = activeAward.getAwardUnitContacts();

        for (AwardUnitContact unitContact : unitContacts) {
            if (unitContact.getUnitAdministratorType().getDefaultGroupFlag().equals(UNIT_ADMIN_GROUP_FLAG)) {
                // BUKC-0140: Add new Admin Type (Clinical Trial Admin)
                if (!(unitContact.getUnitAdministratorTypeCode().equals(AdminStatus.CTAD.getStatusCode()))) {
                    BUAwardPrintAdmin deptAdmin = new BUAwardPrintAdmin();
                    deptAdmin.setEmail(unitContact.getEmailAddress());
                    deptAdmin.setName(unitContact.getFullName());
                    deptAdmin.setPhone(unitContact.getPhoneNumber());

                    deptAdmins.add(deptAdmin);
                }
            }
        }

        return deptAdmins;
    }

    /**
     * This method calculates anticipated amount section on the notification.
     *
     * @param award
     *            to generate the notification for
     * @return anticipated amount to print on the notification
     */
    @SuppressWarnings("unchecked")
    private BUAwardPrintAntAmt populateBUAwardPrintAntAmt(Award award) {
        AwardAmountInfoService awardAmountInfoService = getAwardAmountInfoService();
        BusinessObjectService businessObjectService = getBusinessObjectService();
        BUAwardPrintAntAmt antAmt = new BUAwardPrintAntAmt();
        AwardAmountInfo newestAAI = awardAmountInfoService.fetchLastAwardAmountInfoForAwardVersionAndFinalizedTandMDocumentNumber(award);
        antAmt.setProjectStart(award.getAwardEffectiveDate());
        antAmt.setProjectEnd(newestAAI.getFinalExpirationDate());
        Map<String, Object> fieldValues = new HashMap<String, Object>();

        fieldValues.put("awardId", award.getAwardId());
        List<AwardDirectFandADistribution> fnaDistributionList = (List<AwardDirectFandADistribution>) businessObjectService.findMatching(
                AwardDirectFandADistribution.class, fieldValues);
        ScaleTwoDecimal antDirectAmt = new ScaleTwoDecimal(0);
        ScaleTwoDecimal antIndirectAmt = new ScaleTwoDecimal(0);
        ScaleTwoDecimal antTotal = new ScaleTwoDecimal(0);
        for (AwardDirectFandADistribution awardDirectFandADistribution : fnaDistributionList) {
            antDirectAmt = antDirectAmt.add(new ScaleTwoDecimal(awardDirectFandADistribution.getDirectCost().doubleValue()));
            antIndirectAmt = antIndirectAmt.add(new ScaleTwoDecimal(awardDirectFandADistribution.getIndirectCost().doubleValue()));
        }
        antTotal = antTotal.add(antIndirectAmt);
        antTotal = antTotal.add(antDirectAmt);

        antAmt.setAnticipatedChangeDirect(new ScaleTwoDecimal(antDirectAmt.doubleValue()));
        antAmt.setAnticipatedChangeIndirect(new ScaleTwoDecimal(antIndirectAmt.doubleValue()));
        antAmt.setAnticipatedTotal(new ScaleTwoDecimal(antTotal.doubleValue()));

        return antAmt;
    }

    /**
     * This method calculates cumulative obligated amount section on the notification.
     *
     * @param award
     *            to generate the notification for
     * @return cumulative obligated amount to print on the notification
     */
    private BUAwardPrintCumObAmt populateBUAwardPrintCumObAmt(Award award) {
        AwardAmountInfoService awardAmountInfoService = getAwardAmountInfoService();
        BUAwardPrintCumObAmt cumObAmt = new BUAwardPrintCumObAmt();
        AwardAmountInfo newestAAI = awardAmountInfoService.fetchLastAwardAmountInfoForAwardVersionAndFinalizedTandMDocumentNumber(award);
        cumObAmt.setObligationStart(newestAAI.getCurrentFundEffectiveDate());
        cumObAmt.setObligationEnd(newestAAI.getObligationExpirationDate());
        cumObAmt.setObligatedChangeDirect(new ScaleTwoDecimal(newestAAI.getObligatedTotalDirect().doubleValue()));
        cumObAmt.setObligatedChangeIndirect(new ScaleTwoDecimal(newestAAI.getObligatedTotalIndirect().doubleValue()));
        cumObAmt.setObligatedTotal(new ScaleTwoDecimal(newestAAI.getAmountObligatedToDate().doubleValue()));

        return cumObAmt;
    }

    /**
     * This method populate award amount info section on the notification.
     *
     * @param award
     *            to generate the notification for
     * @return award amount info to print on the notification
     */
    @SuppressWarnings("unchecked")
    private List<BUAwardPrintAmountInfo> populateBUAwardPrintAmountInfo(Award award, List<TimeAndMoneyDocument> timeAndMoneyList) {
        BusinessObjectService businessObjectService = getBusinessObjectService();
        List<BUAwardPrintAmountInfo> amountInfoList = new ArrayList<BUAwardPrintAmountInfo>();
        // Sorting map by award number using TreeMap
        Map<String, BUAwardPrintAmountInfo> printObjects = new TreeMap<String, BUAwardPrintAmountInfo>();
        if (timeAndMoneyList != null) {
            for (TimeAndMoneyDocument timeAndMoneyDocument : timeAndMoneyList) {
                Map<String, Object> fieldValues = new HashMap<String, Object>();
                fieldValues.put("timeAndMoneyDocumentNumber", timeAndMoneyDocument.getDocumentNumber());
                List<AwardAmountInfo> awardAmountInfoList = (List<AwardAmountInfo>) businessObjectService.findMatching(AwardAmountInfo.class,
                        fieldValues);

                // remove duplicate AAI from previous versions of awards.
                awardAmountInfoList = trimAwardAmountInfoForActiveAwardVersion(awardAmountInfoList);
                for (AwardAmountInfo aai : awardAmountInfoList) {
                    if (printObjects.containsKey(aai.getAwardNumber())) {
                        updatePrintObjects(aai, printObjects);
                    } else {
                        addNewPrintObject(aai, printObjects);
                    }
                }
            }
        }
        amountInfoList.addAll(printObjects.values());
        // mukadder comparator to sort internal orders in the order
        /*
         * Collections.sort(amountInfoList, new Comparator<BUAwardPrintAmountInfo>() {
         * public int compare(BUAwardPrintAmountInfo o1, BUAwardPrintAmountInfo o2) {
         * 
         * NumberFormat format = NumberFormat.getIntegerInstance();
         * int value1 = 0;
         * try {
         * if(o1.getInternalOrderNbr()!=null)
         * value1 = format.parse(o1.getInternalOrderNbr()).intValue();
         * }
         * catch (ParseException e) {
         * // TODO Auto-generated catch block
         * e.printStackTrace();
         * }
         * int value2 = 0;
         * try {
         * if(o2.getInternalOrderNbr()!=null)
         * value2 = format.parse(o2.getInternalOrderNbr()).intValue();
         * }
         * catch (ParseException e) {
         * // TODO Auto-generated catch block
         * e.printStackTrace();
         * }
         * 
         * return value1 - value2; // time2 - time1 to sort descending...
         * };
         * });
         */

        return amountInfoList;
    }

    /**
     * This method removes AwardAmountInfo BO's that are from Archived or Pending Award versions.
     *
     * @param awardAmountInfoList
     *            aai list
     */
    private List<AwardAmountInfo> trimAwardAmountInfoForActiveAwardVersion(List<AwardAmountInfo> awardAmountInfoList) {
        List<AwardAmountInfo> trimmedList = new ArrayList<AwardAmountInfo>();
        for (AwardAmountInfo awardAmountInfo : awardAmountInfoList) {
            AwardHierarchy hierarchy = getAwardHierarchyService().loadAwardHierarchyBranch(awardAmountInfo.getAwardNumber());
            // BUKC-0068: Print Notification - disable looking for last sequence in AAI
            // int seqNum = getActiveAwardVersion(awardAmountInfo.getAwardNumber()).getSequenceNumber();
            // if (awardAmountInfo.getSequenceNumber() == seqNum && (!awardAmountInfo.getAwardNumber().endsWith("-00001"))) {

            // BUKC-0085: Ignore totals on any node with childern (group nodes) - per Defect No DFCT0011155
            // if(!awardAmountInfo.getAwardNumber().endsWith("-00001"))
            if (!hierarchy.hasChildren()) {
                trimmedList.add(awardAmountInfo);
            }
        }

        return trimmedList;
    }

    /**
     * This method updates print objects
     *
     * @param aai
     *            aai object
     * @param printObjects
     *            print objects
     */
    private void updatePrintObjects(AwardAmountInfo aai, Map<String, BUAwardPrintAmountInfo> printObjects) {
        BUAwardPrintAmountInfo updateObject = printObjects.get(aai.getAwardNumber());
        // mukadder setting totalChange to 0 values to avoid npe
        if (updateObject.getTotalChange() == null) {
            updateObject.setTotalChange(new ScaleTwoDecimal(0.00));
        }
        if (aai.getObligatedChange() == null) {
            aai.setObligatedChange(new ScaleTwoDecimal(0.00));
        }
        updateObject.setTotalChange(updateObject.getTotalChange().add(aai.getObligatedChange()));
        updateObject.setObligatedChangeDirect(updateObject.getObligatedChangeDirect().add(aai.getObligatedChangeDirect()));
        updateObject.setObligatedChangeIndirect(updateObject.getObligatedChangeIndirect().add(aai.getObligatedChangeIndirect()));
    }

    /**
     * This method adds print object for the aai section
     *
     * @param aai
     *            aai object
     * @param printObjects
     *            print objects
     */
    private void addNewPrintObject(AwardAmountInfo aai, Map<String, BUAwardPrintAmountInfo> printObjects) {
        BUAwardPrintAmountInfo newObject = new BUAwardPrintAmountInfo();

        // BUKC-0073: AN - Get print object from the active award for "This Action" section - due to AAI data model changes
        Award activeAward = getActiveAwardVersion(aai.getAwardNumber());

        if (aai.getObligatedChange() == null) {
            aai.setObligatedChange(new ScaleTwoDecimal(0.00));
        }
        newObject.setTotalChange(aai.getObligatedChange());

        newObject.setObligatedChangeDirect(aai.getObligatedChangeDirect());
        newObject.setObligatedChangeIndirect(aai.getObligatedChangeIndirect());

        newObject.setPiFullNames(getActiveAwardPiNames(activeAward.getAwardNumber()));
        newObject.setfAndARate(getMaxFAndARate(activeAward));

        newObject.setInternalOrderNbr(activeAward.getAccountNumber());
        //newObject.setDescription(((AwardExtension) activeAward.getExtension()).getChildDescription());
        //newObject.setLegacySource(((AwardExtension) activeAward.getExtension()).getWalkerSourceNumber());

        printObjects.put(aai.getAwardNumber(), newObject);

    }

    /**
     * This method gets the maximum of F&A rate
     *
     * @param award
     *            award
     * @return the maximum of the F&A rate for the award
     */
    @SuppressWarnings("unchecked")
    private ScaleTwoDecimal getMaxFAndARate(Award award) {
        BusinessObjectService businessObjectService = getBusinessObjectService();
        AwardBudgetExt latestBudgetVersionOverview = getLastBudgetVersion(award.getAwardDocument());
        ScaleTwoDecimal returnMaxVal = new ScaleTwoDecimal(0);
        if (!(latestBudgetVersionOverview == null)) {
            Long budgetId = latestBudgetVersionOverview.getBudgetId();
            Map<String, Object> fieldValues = new HashMap<String, Object>();
            fieldValues.put("budgetId", budgetId);
            List<BudgetRateAndBase> budgetRateAndBaseList = (List<BudgetRateAndBase>) businessObjectService.findMatching(BudgetRateAndBase.class,
                    fieldValues);
            for (BudgetRateAndBase budgetRateAndBase : budgetRateAndBaseList) {
                if (budgetRateAndBase.getAppliedRate().isGreaterThan(returnMaxVal)) {
                    returnMaxVal = budgetRateAndBase.getAppliedRate();
                }
            }
        }

        return returnMaxVal;
    }

    /**
     * This method gets the active award's PI names
     *
     * @param awardNumber
     *            award number
     * @return list of PI names
     */
    private List<String> getActiveAwardPiNames(String awardNumber) {
        List<String> piNames = new ArrayList<String>();
        Award activeAward = getActiveAwardVersion(awardNumber);
        List<AwardPerson> awardPersons = activeAward.getProjectPersons();
        for (AwardPerson awardPerson : awardPersons) {
            if (awardPerson.getContactRoleCode().equals("PI")) {
                piNames.add(awardPerson.getFullName());
            }
        }

        return piNames;
    }

    /**
     * This method gets the award's active version
     *
     * @param awardNumber
     *            award number
     * @return the active award
     */
    @SuppressWarnings("unchecked")
    public Award getActiveAwardVersion(String awardNumber) {
        BusinessObjectService businessObjectService = getBusinessObjectService();
        List<VersionHistory> versions = findVersionHistory(Award.class, awardNumber);
        VersionHistory result = getActiveVersionHistory(versions);
        Map<String, Object> fieldValues = new HashMap<String, Object>();
        fieldValues.put("awardNumber", result.getSequenceOwnerVersionNameValue());
        fieldValues.put("sequenceNumber", result.getSequenceOwnerSequenceNumber());
        List<Award> awardList = (List<Award>) businessObjectService.findMatching(Award.class, fieldValues);
        return awardList.get(0);
    }

    /**
     * This method gets the active version of a list of version history
     *
     * @param list
     *            of version history
     * @return the active version history
     */
    private VersionHistory getActiveVersionHistory(List<VersionHistory> list) {
        VersionHistory returnVal = null;
        for (VersionHistory vh : list) {
            if (vh.getStatus().equals(VersionStatus.ACTIVE)) {
                returnVal = vh;
            }
        }

        return returnVal;
    }

    /**
     * This method gets award's version history
     *
     * @param klass
     * @param versionName
     * @return
     */
    public List<VersionHistory> findVersionHistory(Class<? extends SequenceOwner> klass, String versionName) {
        BusinessObjectService businessObjectService = getBusinessObjectService();
        Map<String, Object> fieldValues = new HashMap<String, Object>();
        fieldValues.put(SEQUENCE_OWNER_CLASS_NAME_FIELD, klass.getName());
        fieldValues.put(SEQUENCE_OWNER_REFERENCE_VERSION_NAME, versionName);

        return new ArrayList<VersionHistory>(businessObjectService.findMatching(VersionHistory.class, fieldValues));
    }

    /**
     * This method gets the last budget version on the award
     *
     * @param award
     * @return the last budget version
     */
    protected AwardBudgetExt getLastBudgetVersion(AwardDocument awardDocument) {
        List<AwardBudgetExt> awardBudgetDocumentVersions = awardDocument.getAward().getBudgets();
        AwardBudgetExt latestBudget = null;
        int versionSize = awardBudgetDocumentVersions.size();
        if (versionSize > 0) {
            latestBudget = awardBudgetDocumentVersions.get(versionSize - 1);
        }

        return latestBudget;
    }

    /**
     * Gets the parameterService attribute.
     *
     * @return Returns the parameterService.
     */
    protected ParameterService getParameterService() {
        return this.parameterService;
    }

    /**
     * Sets the businessObjectService attribute value.
     *
     * @param parameterService
     *            The businessObjectService to set.
     */
    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    /**
     * Gets the awardAmountInfoService attribute.
     *
     * @return Returns the awardAmountInfoService.
     */
    protected AwardAmountInfoService getAwardAmountInfoService() {
        return this.awardAmountInfoService;
    }

    /**
     * Sets the businessObjectService attribute value.
     *
     * @param awardAmountInfoService
     *            The businessObjectService to set.
     */
    public void setAwardAmountInfoService(AwardAmountInfoService awardAmountInfoService) {
        this.awardAmountInfoService = awardAmountInfoService;
    }

    /**
     * Gets the businessObjectService attribute.
     *
     * @return Returns the businessObjectService.
     */
    protected BusinessObjectService getBusinessObjectService() {
        return this.businessObjectService;
    }

    public AwardHierarchyService getAwardHierarchyService() {
        if (awardHierarchyService == null) {
            awardHierarchyService = KcServiceLocator.getService(AwardHierarchyService.class);
        }
        return awardHierarchyService;
    }

    /**
     * Sets the businessObjectService attribute value.
     *
     * @param businessObjectService
     *            The businessObjectService to set.
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * This Enumerated types for PAfo and OSp type codes ...
     */
    private static enum AdminStatus {
        OSP(BUConstants.OSP_ADMINISTRATOR_TYPE_CODE), PAFO(BUConstants.PAFO_ADMINISTRATOR_TYPE_CODE), CTAD(BUConstants.CTAD_ADMINISTRATOR_TYPE_CODE);
        private String statusCode;

        private AdminStatus(String code) {
            statusCode = code;
        }

        private String getStatusCode() {
            return statusCode;
        }

    }
}
