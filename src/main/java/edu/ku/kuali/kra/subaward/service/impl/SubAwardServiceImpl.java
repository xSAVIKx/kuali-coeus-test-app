package edu.ku.kuali.kra.subaward.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;
import org.kuali.coeus.common.framework.version.VersionException;
import org.kuali.coeus.common.framework.version.VersioningService;
import org.kuali.coeus.common.framework.version.history.VersionHistory;
import org.kuali.coeus.common.framework.version.history.VersionHistoryService;
import org.kuali.coeus.sys.api.model.ScaleTwoDecimal;
import org.kuali.kra.award.home.Award;
import org.kuali.kra.award.version.service.AwardVersionService;
import org.kuali.kra.subaward.bo.SubAward;
import org.kuali.kra.subaward.bo.SubAwardAmountInfo;
import org.kuali.kra.subaward.bo.SubAwardAmountReleased;
import org.kuali.kra.subaward.bo.SubAwardFundingSource;
import org.kuali.kra.subaward.document.SubAwardDocument;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.document.DocumentStatus;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.bo.AdHocRouteRecipient;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.SequenceAccessorService;

/**
 * This class is service impl for subAward...
 */
public class SubAwardServiceImpl extends org.kuali.kra.subaward.service.impl.SubAwardServiceImpl implements
        edu.ku.kuali.kra.subaward.service.SubAwardService {

    private BusinessObjectService businessObjectService;
    private VersioningService versioningService;
    private VersionHistoryService versionHistoryService;
    private DocumentService documentService;
    private SequenceAccessorService sequenceAccessorService;
    private ParameterService parameterService;
    private AwardVersionService awardVersionService;

    /**
     * .
     * This method is using for getAmountInfo
     *
     * @param subAward
     * @return subAward
     */
    @Override
    public SubAward getAmountInfo(SubAward subAward) {

        List<SubAwardAmountInfo> subAwardAmountInfoList = subAward.getSubAwardAmountInfoList();

        List<SubAwardAmountReleased> subAwardAmountReleasedList = subAward.getSubAwardAmountReleasedList();
        ScaleTwoDecimal totalObligatedAmount = new ScaleTwoDecimal(0.00);
        ScaleTwoDecimal totalAnticipatedAmount = new ScaleTwoDecimal(0.00);
        ScaleTwoDecimal totalAmountReleased = new ScaleTwoDecimal(0.00);
        if (subAwardAmountInfoList != null && subAwardAmountInfoList.size() > 0) {
            for (SubAwardAmountInfo subAwardAmountInfo : subAwardAmountInfoList) {
                if (subAwardAmountInfo.getObligatedChange() != null) {
                    subAward.setTotalObligatedAmount(totalObligatedAmount.add(subAwardAmountInfo.getObligatedChange()));
                    totalObligatedAmount = subAward.getTotalObligatedAmount();
                }
                if (subAwardAmountInfo.getAnticipatedChange() != null) {
                    subAward.setTotalAnticipatedAmount(totalAnticipatedAmount.add(subAwardAmountInfo.getAnticipatedChange()));
                    totalAnticipatedAmount = subAward.getTotalAnticipatedAmount();
                }
                if (subAwardAmountInfo.getModificationEffectiveDate() != null) {
                    subAward.setModificationEffectiveDate(subAwardAmountInfo.getModificationEffectiveDate());
                }
                if (subAwardAmountInfo.getModificationID() != null) {
                    subAward.setModificationId(subAwardAmountInfo.getModificationID());
                }
                if (subAwardAmountInfo.getPeriodofPerformanceStartDate() != null) {
                    subAward.setPerformanceStartDate(subAwardAmountInfo.getPeriodofPerformanceStartDate());
                }
                if (subAwardAmountInfo.getPeriodofPerformanceEndDate() != null) {
                    subAward.setPerformanceEnddate(subAwardAmountInfo.getPeriodofPerformanceEndDate());
                }
            }
            for (SubAwardAmountReleased subAwardAmountReleased : subAwardAmountReleasedList) {

                if (subAwardAmountReleased.getAmountReleased() != null
                        && !(StringUtils.equals(subAwardAmountReleased.getInvoiceStatus(), DocumentStatus.DISAPPROVED.getCode())
                                || StringUtils.equals(subAwardAmountReleased.getInvoiceStatus(), DocumentStatus.CANCELED.getCode()) || StringUtils
                                    .equals(subAwardAmountReleased.getInvoiceStatus(), DocumentStatus.RECALLED.getCode()))) {
                    subAward.setTotalAmountReleased(totalAmountReleased.add(subAwardAmountReleased.getAmountReleased()));
                    totalAmountReleased = subAward.getTotalAmountReleased();
                }
            }
            SubAwardAmountInfo amountInfo = subAward.getSubAwardAmountInfoList().get(subAward.getSubAwardAmountInfoList().size() - 1);
            amountInfo.setAnticipatedAmount(totalAnticipatedAmount);
            amountInfo.setObligatedAmount(totalObligatedAmount);
        }
        subAward.setTotalObligatedAmount(totalObligatedAmount);
        subAward.setTotalAnticipatedAmount(totalAnticipatedAmount);
        subAward.setTotalAmountReleased(totalAmountReleased);
        subAward.setTotalAvailableAmount(totalObligatedAmount.subtract(totalAmountReleased));

        // BUKC-0145: Transactions on Financial Tab - Order switching (DFCT0011505) - fixes issue with BUKC-0105
        List<SubAwardAmountInfo> sortedlist = subAward.getSubAwardAmountInfoList();
        if (!sortedlist.isEmpty() && !(sortedlist.get(sortedlist.size() - 1).getSubAwardAmountInfoId() == null)) {
            Collections.sort(sortedlist, new Comparator<SubAwardAmountInfo>() {
                @Override
                public int compare(SubAwardAmountInfo o1, SubAwardAmountInfo o2) {
                    return o1.getSubAwardAmountInfoId() - o2.getSubAwardAmountInfoId();
                }
            });
        }
        subAward.setSubAwardAmountInfoList(sortedlist);

        return subAward;
    }

    @Override
    public SubAward getActiveSubAward(Long subAwardId) {

        Map<String, Object> values = new HashMap<String, Object>();
        values.put("subAwardId", subAwardId);
        List<SubAward> subAwards = (List<SubAward>) getBusinessObjectService().findMatching(SubAward.class, values);
        SubAward subAward = subAwards.get(0);

        SubAward activeSubAward = (SubAward) getVersionHistoryService().findActiveVersion(SubAward.class, subAward.getSubAwardCode())
                .getSequenceOwner();

        getAmountInfo(activeSubAward);
        return activeSubAward;
    }

    @Override
    public List<SubAward> getLinkedSubAwards(Award award) {
        Map<String, Object> values = new HashMap<String, Object>();

        // BUKC-0136-a: KC Subaward: (Revert to BUKC-0111) Funding Source Award data should be from the latest FINAL version (Jira KRAFDBCK-10754, BU
        // Subaward QA issue 5, DFCT0011410, and DFCT0011447)
        // Get the current (finalized) award
        Award currentAward = getAwardVersionService().getActiveAwardVersion(award.getAwardNumber());
        List<SubAward> subAwards = new ArrayList<SubAward>();

        if (currentAward != null) {
            values.put("awardId", currentAward.getAwardId());

            Collection<SubAwardFundingSource> subAwardFundingSources = getBusinessObjectService().findMatching(SubAwardFundingSource.class, values);
            Set<String> subAwardSet = new TreeSet<String>();

            for (SubAwardFundingSource subAwardFundingSource : subAwardFundingSources) {
                // mk: check only the active subaward id against what the search by award_id returns
                if (subAwardFundingSource.getSubAward().getSubAwardId()
                        .equals(getActiveSubAward(subAwardFundingSource.getSubAward().getSubAwardId()).getSubAwardId())) {
                    subAwardSet.add(subAwardFundingSource.getSubAward().getSubAwardCode());
                }
            }
            for (String subAwardCode : subAwardSet) {
                VersionHistory activeVersion = getVersionHistoryService().findActiveVersion(SubAward.class, subAwardCode);
                if (activeVersion == null) {
                    VersionHistory pendingVersion = getVersionHistoryService().findPendingVersion(SubAward.class, subAwardCode);
                    if (pendingVersion != null) {
                        // BUKC-0136-a: KC Subaward: (Revert to BUKC-0111) Funding Source Award data should be from the latest FINAL version (Jira
                        // KRAFDBCK-10754, BU Subaward QA issue 5, DFCT0011410, and DFCT0011447)
                        // Get Subaward with the Amount Info data
                        subAwards.add(getAmountInfo((SubAward) pendingVersion.getSequenceOwner()));
                    }
                } else {
                    // Get Subaward with the Amount Info data
                    subAwards.add(getAmountInfo((SubAward) activeVersion.getSequenceOwner()));
                }
            }
        }

        return subAwards;
    }

    // BUKC-0136-a: KC Subaward: (Revert to BUKC-0111) Funding Source Award data should be from the latest FINAL version (Jira KRAFDBCK-10754, BU
    // Subaward QA issue 5, DFCT0011410, and DFCT0011447)
    /**
     * Update Subaward Funding Source to link the subaward with the new finalized award by
     * create a new subaward version and link them.
     *
     * @param subAwardIds
     * @param award
     * @return
     */
    @Override
    public List<SubAward> updateSubAwardFundingSource(Set<Long> subAwardIds, Award award) {
        List<SubAward> updatedSubawards = new ArrayList<SubAward>();

        try {
            for (Long subAwardId : subAwardIds) {
                SubAward activeVersion = getActiveSubAward(subAwardId);
                SubAward pendingVersion = null;
                if (getVersionHistoryService().findPendingVersion(SubAward.class, activeVersion.getSubAwardCode()) != null) {
                    pendingVersion = (SubAward) getVersionHistoryService().findPendingVersion(SubAward.class, activeVersion.getSubAwardCode())
                            .getSequenceOwner();
                }
                if (pendingVersion != null) {
                    finalizeTheExistingVersion(pendingVersion, award);
                    updatedSubawards.add(pendingVersion);
                } else {
                    if (activeVersion != null) {
                        SubAward newVersion = getVersioningService().createNewVersion(activeVersion);

                        for (SubAwardFundingSource subAwardFundingSource : newVersion.getSubAwardFundingSourceList()) {
                            if (subAwardFundingSource.getAward().getAwardNumber().equals(award.getAwardNumber())) {
                                subAwardFundingSource.setAwardId(award.getAwardId());
                                newVersion.setSubAwardFundingSource(subAwardFundingSource);
                                // newVersion.setVersionNumber(activeVersion.getVersionNumber()+1);

                                SubAwardDocument subAwardDocument = (SubAwardDocument) documentService.getNewDocument(SubAwardDocument.class);
                                subAwardDocument.getDocumentHeader().setDocumentDescription(
                                        activeVersion.getSubAwardDocument().getDocumentHeader().getDocumentDescription());

                                subAwardDocument.setSubAward(newVersion);

                                newVersion.setSubAwardDocument(subAwardDocument);

                                documentService.routeDocument(subAwardDocument,
                                        "Update Subaward Funding Source to link the Subaward with the newly created version of the Award",
                                        new ArrayList<AdHocRouteRecipient>());

                                updatedSubawards.add(newVersion);
                            }
                        }
                    }
                }
            }
            return updatedSubawards;

        } catch (WorkflowException we) {
            we.printStackTrace();
        } catch (VersionException ve) {
            ve.printStackTrace();
        }

        return updatedSubawards;
    }

    // BUKC-0166: Subaward - Finalize subawards if in save status (ENHC0013648)
    @Override
    public void finalizeTheExistingVersion(SubAward pendingVersion, Award award) throws WorkflowException {

        for (SubAwardFundingSource subAwardFundingSource : pendingVersion.getSubAwardFundingSourceList()) {
            if (subAwardFundingSource.getAward().getAwardNumber().equals(award.getAwardNumber())) {
                subAwardFundingSource.setAwardId(award.getAwardId());
                pendingVersion.setSubAwardFundingSource(subAwardFundingSource);
                // newVersion.setVersionNumber(activeVersion.getVersionNumber()+1);
                SubAwardDocument subAwardDocument = (SubAwardDocument) documentService.getByDocumentHeaderId(pendingVersion.getSubAwardDocument()
                        .getDocumentNumber());
                subAwardDocument.setSubAward(pendingVersion);
                pendingVersion.setSubAwardDocument(subAwardDocument);
                try {
                    documentService.routeDocument(subAwardDocument,
                            "Update Subaward Funding Source to link the Subaward with the newly created version of the Award",
                            new ArrayList<AdHocRouteRecipient>());
                } catch (WorkflowException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * .
     * This method is for getAwardVersionService
     *
     * @return awardVersionService...
     *
     */
    protected AwardVersionService getAwardVersionService() {
        return awardVersionService;
    }

    /**
     * This method is for setting awardVersionService...
     *
     * @param awardVersionService
     *            the awardVersionService
     */
    public void setAwardVersionService(AwardVersionService awardVersionService) {
        this.awardVersionService = awardVersionService;
    }

    /**
     * .
     * This method is for getBusinessObjectService
     *
     * @return awardVersionService...
     *
     */

    @Override
    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    /**
     * This method is for setting businessObjectService...
     *
     * @param businessObjectService
     *            the businessObjectService
     */
    @Override
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * .
     * This method is for getVersioningService
     *
     * @return versioningService...
     *
     */
    @Override
    protected VersioningService getVersioningService() {
        return versioningService;
    }

    /**
     * This method is for setting versioningService...
     *
     * @param versioningService
     *            the versioningService
     */
    @Override
    public void setVersioningService(VersioningService versioningService) {
        this.versioningService = versioningService;
    }

    /**
     * .
     * This is the Getter Method for documentService
     *
     * @return Returns the documentService.
     */
    @Override
    public DocumentService getDocumentService() {
        return documentService;
    }

    /**
     * .
     * This is the Setter Method for documentService
     *
     * @param documentService
     *            The documentService to set.
     */
    @Override
    public void setDocumentService(DocumentService documentService) {
        this.documentService = documentService;
    }

}
