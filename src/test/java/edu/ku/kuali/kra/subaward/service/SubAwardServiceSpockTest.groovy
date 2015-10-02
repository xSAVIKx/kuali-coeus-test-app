package edu.ku.kuali.kra.subaward.service

import org.codehaus.groovy.transform.tailrec.VariableReplacedListener.*
import org.kuali.coeus.common.framework.version.VersioningService
import org.kuali.coeus.common.framework.version.history.VersionHistory
import org.kuali.coeus.common.framework.version.history.VersionHistoryService
import org.kuali.coeus.sys.api.model.ScaleTwoDecimal
import org.kuali.kra.award.home.Award
import org.kuali.kra.award.version.service.AwardVersionService
import org.kuali.kra.subaward.bo.SubAward
import org.kuali.kra.subaward.bo.SubAwardAmountInfo
import org.kuali.kra.subaward.bo.SubAwardAmountReleased
import org.kuali.kra.subaward.document.SubAwardDocument
import org.kuali.rice.coreservice.framework.parameter.ParameterService
import org.kuali.rice.krad.bo.AdHocRouteRecipient
import org.kuali.rice.krad.bo.DocumentHeader
import org.kuali.rice.krad.service.BusinessObjectService
import org.kuali.rice.krad.service.DocumentService
import org.kuali.rice.krad.service.SequenceAccessorService

import spock.lang.Specification
import edu.ku.kuali.kra.subaward.bo.SubAwardFundingSource
import edu.ku.kuali.kra.subaward.service.impl.SubAwardServiceImpl

class SubAwardServiceSpockTest extends Specification {
    SubAwardService service

    private BusinessObjectService businessObjectService = Mock()
    private VersioningService versioningService = Mock()
    private VersionHistoryService versionHistoryService = Mock()
    private DocumentService documentService = Mock()
    private SequenceAccessorService sequenceAccessorService = Mock()
    private ParameterService parameterService = Mock()
    private AwardVersionService awardVersionService = Mock()

    def "setup"(){
        service = new SubAwardServiceImpl(awardVersionService:awardVersionService,parameterService:parameterService,sequenceAccessorService:sequenceAccessorService,businessObjectService:businessObjectService, versioningService:versioningService,versionHistoryService:versionHistoryService,documentService:documentService)
    }
    def "test getAmountInfo"(){
        given:
        ScaleTwoDecimal expectedTotalObligatedAmount = new ScaleTwoDecimal(1.00)
        ScaleTwoDecimal expectedTotalAnticipatedAmount = new ScaleTwoDecimal(1.00)
        ScaleTwoDecimal expectedTotalAmountReleased = new ScaleTwoDecimal(0.00)
        ScaleTwoDecimal expectedAvailableAmount = new ScaleTwoDecimal(1.00)

        ScaleTwoDecimal obligatedChange = new ScaleTwoDecimal(1.00)
        ScaleTwoDecimal anticipatedChange = new ScaleTwoDecimal(1.00)
        java.sql.Date modificationEffectiveDate = new java.sql.Date(0)
        java.sql.Date periodofPerformanceStartDate = new java.sql.Date(0)
        java.sql.Date periodofPerformanceEndDate = new java.sql.Date(0)
        def modificationID = "modificationID"
        def subAwardAmountInfoId = 1
        SubAwardAmountInfo subAwardAmountInfo = new SubAwardAmountInfo(subAwardAmountInfoId:subAwardAmountInfoId,modificationID:modificationID,periodofPerformanceEndDate:periodofPerformanceEndDate,periodofPerformanceStartDate:periodofPerformanceStartDate,obligatedChange:obligatedChange,anticipatedChange:anticipatedChange,modificationEffectiveDate:modificationEffectiveDate)

        ScaleTwoDecimal amountReleased = new ScaleTwoDecimal(0.00)
        SubAwardAmountReleased subAwardAmountReleased = new SubAwardAmountReleased(amountReleased:amountReleased)

        SubAward subAward = Spy()
        subAward.getSubAwardAmountInfoList() >> [subAwardAmountInfo]
        subAward.getSubAwardAmountReleasedList() >> [subAwardAmountReleased]

        when:
        def result = service.getAmountInfo(subAward)

        then:
        result!=null
        result.totalObligatedAmount == expectedTotalObligatedAmount
        result.totalAnticipatedAmount == expectedTotalAnticipatedAmount
        result.totalAmountReleased == expectedTotalAmountReleased
        result.totalAvailableAmount == expectedAvailableAmount
        result.modificationEffectiveDate == modificationEffectiveDate
        result.modificationId == modificationID
        result.performanceStartDate == periodofPerformanceStartDate
        result.performanceEnddate == periodofPerformanceEndDate
    }

    def "test getAmountInfo, when released list is empty"(){
        given:
        ScaleTwoDecimal expectedTotalObligatedAmount = new ScaleTwoDecimal(1.00)
        ScaleTwoDecimal expectedTotalAnticipatedAmount = new ScaleTwoDecimal(1.00)
        ScaleTwoDecimal expectedTotalAmountReleased = new ScaleTwoDecimal(0.00)
        ScaleTwoDecimal expectedAvailableAmount = new ScaleTwoDecimal(1.00)

        ScaleTwoDecimal obligatedChange = new ScaleTwoDecimal(1.00)
        ScaleTwoDecimal anticipatedChange = new ScaleTwoDecimal(1.00)
        java.sql.Date modificationEffectiveDate = new java.sql.Date(0)
        java.sql.Date periodofPerformanceStartDate = new java.sql.Date(0)
        java.sql.Date periodofPerformanceEndDate = new java.sql.Date(0)
        def modificationID = "modificationID"
        def subAwardAmountInfoId = 1
        SubAwardAmountInfo subAwardAmountInfo = new SubAwardAmountInfo(subAwardAmountInfoId:subAwardAmountInfoId,modificationID:modificationID,periodofPerformanceEndDate:periodofPerformanceEndDate,periodofPerformanceStartDate:periodofPerformanceStartDate,obligatedChange:obligatedChange,anticipatedChange:anticipatedChange,modificationEffectiveDate:modificationEffectiveDate)


        SubAward subAward = Spy()
        subAward.getSubAwardAmountInfoList() >> [subAwardAmountInfo]
        subAward.getSubAwardAmountReleasedList() >> []

        when:
        def result = service.getAmountInfo(subAward)

        then:
        result!=null
        result.totalObligatedAmount == expectedTotalObligatedAmount
        result.totalAnticipatedAmount == expectedTotalAnticipatedAmount
        result.totalAmountReleased == expectedTotalAmountReleased
        result.totalAvailableAmount == expectedAvailableAmount
        result.modificationEffectiveDate == modificationEffectiveDate
        result.modificationId == modificationID
        result.performanceStartDate == periodofPerformanceStartDate
        result.performanceEnddate == periodofPerformanceEndDate
    }

    def "test getAmountInfo, when released list is empty and no info in SubAwardAmountInfo presented"(){
        given:
        SubAward subAward = Spy()
        SubAwardAmountInfo subAwardAmountInfo = new SubAwardAmountInfo()
        ScaleTwoDecimal expectedTotalObligatedAmount = new ScaleTwoDecimal(0.00)
        ScaleTwoDecimal expectedTotalAnticipatedAmount = new ScaleTwoDecimal(0.00)
        ScaleTwoDecimal expectedTotalAmountReleased = new ScaleTwoDecimal(0.00)
        ScaleTwoDecimal expectedAvailableAmount = new ScaleTwoDecimal(0.00)
        subAward.getSubAwardAmountInfoList() >> [subAwardAmountInfo]
        subAward.getSubAwardAmountReleasedList() >> []
        when:
        def result = service.getAmountInfo(subAward)
        then:
        result!=null
        result.totalObligatedAmount == expectedTotalObligatedAmount
        result.totalAnticipatedAmount == expectedTotalAnticipatedAmount
        result.totalAmountReleased == expectedTotalAmountReleased
        result.totalAvailableAmount == expectedAvailableAmount
    }

    def "test getAmountInfo, when info list and released list are empty"(){
        given:
        SubAward subAward = Spy()
        ScaleTwoDecimal expectedTotalObligatedAmount = new ScaleTwoDecimal(0.00)
        ScaleTwoDecimal expectedTotalAnticipatedAmount = new ScaleTwoDecimal(0.00)
        ScaleTwoDecimal expectedTotalAmountReleased = new ScaleTwoDecimal(0.00)
        ScaleTwoDecimal expectedAvailableAmount = new ScaleTwoDecimal(0.00)
        subAward.getSubAwardAmountInfoList() >> []
        subAward.getSubAwardAmountReleasedList() >> []
        when:
        def result = service.getAmountInfo(subAward)
        then:
        result!=null
        result.totalObligatedAmount == expectedTotalObligatedAmount
        result.totalAnticipatedAmount == expectedTotalAnticipatedAmount
        result.totalAmountReleased == expectedTotalAmountReleased
        result.totalAvailableAmount == expectedAvailableAmount
    }

    def "test getLinkedSubAwards without active and penging versions"(){
        given:
        def awardNumber = "awardNumber"
        def subAwardId = 1L
        def subAwardCode = "subAwardCode"
        def awardId = 666L
        SubAward sourceSubAward = new SubAward(subAwardId:subAwardId,subAwardCode:subAwardCode)
        SubAward subAward = new SubAward(subAwardId:subAwardId,subAwardCode : subAwardCode)
        SubAwardFundingSource source = new SubAwardFundingSource(subAward:sourceSubAward)
        Award currentAward = new Award(awardId:awardId,awardNumber:awardNumber)
        Award award = new Award(awardNumber:awardNumber)
        SubAward activeSubAward = Mock()
        activeSubAward.getSubAwardAmountInfoList() >> []
        activeSubAward.getSubAwardId() >> subAwardId
        VersionHistory versionHistory = Mock()
        when:
        def result = service.getLinkedSubAwards(award)
        then:
        1 * awardVersionService.getActiveAwardVersion(awardNumber) >> currentAward
        1 * businessObjectService.findMatching(_, ["awardId":awardId]) >> [source]
        1 * businessObjectService.findMatching(_, ["subAwardId":subAwardId]) >> [subAward]
        2 * versionHistoryService.findActiveVersion(SubAward.class, subAwardCode) >> versionHistory >> null
        1 * versionHistoryService.findPendingVersion(SubAward.class, subAwardCode) >> null
        1 * versionHistory.getSequenceOwner() >> activeSubAward
        result!=null
        result.size() == 0
    }

    def "test getLinkedSubAwards with PengingAction"(){
        given:
        def awardNumber = "awardNumber"
        def subAwardId = 1L
        def subAwardCode = "subAwardCode"
        def awardId = 666L
        SubAward sourceSubAward = new SubAward(subAwardId:subAwardId,subAwardCode:subAwardCode)
        SubAward subAward = new SubAward(subAwardId:subAwardId,subAwardCode : subAwardCode)
        SubAwardFundingSource source = new SubAwardFundingSource(subAward:sourceSubAward)
        Award currentAward = new Award(awardId:awardId,awardNumber:awardNumber)
        Award award = new Award(awardNumber:awardNumber)
        SubAward activeSubAward = Mock()
        activeSubAward.getSubAwardAmountInfoList() >> []
        activeSubAward.getSubAwardId() >> subAwardId
        VersionHistory versionHistory = Mock()
        when:
        def result = service.getLinkedSubAwards(award)
        then:
        1 * awardVersionService.getActiveAwardVersion(awardNumber) >> currentAward
        1 * businessObjectService.findMatching(_, ["awardId":awardId]) >> [source]
        1 * businessObjectService.findMatching(_, ["subAwardId":subAwardId]) >> [subAward]
        2 * versionHistoryService.findActiveVersion(SubAward.class, subAwardCode) >> versionHistory >> null
        1 * versionHistoryService.findPendingVersion(SubAward.class, subAwardCode) >> versionHistory
        2 * versionHistory.getSequenceOwner() >> activeSubAward
        result!=null
        result.size() == 1
        result.get(0) == activeSubAward
    }
    def "test getLinkedSubAwards with activeVersion"(){
        given:
        def awardNumber = "awardNumber"
        def subAwardId = 1L
        def subAwardCode = "subAwardCode"
        def awardId = 666L
        SubAward sourceSubAward = new SubAward(subAwardId:subAwardId,subAwardCode:subAwardCode)
        SubAward subAward = new SubAward(subAwardId:subAwardId,subAwardCode : subAwardCode)
        SubAwardFundingSource source = new SubAwardFundingSource(subAward:sourceSubAward)
        Award currentAward = new Award(awardId:awardId,awardNumber:awardNumber)
        Award award = new Award(awardNumber:awardNumber)
        SubAward activeSubAward = Mock()
        activeSubAward.getSubAwardAmountInfoList() >> []
        activeSubAward.getSubAwardId() >> subAwardId
        VersionHistory versionHistory = Mock()
        when:
        def result = service.getLinkedSubAwards(award)
        then:
        1 * awardVersionService.getActiveAwardVersion(awardNumber) >> currentAward
        1 * businessObjectService.findMatching(_, ["awardId":awardId]) >> [source]
        1 * businessObjectService.findMatching(_, ["subAwardId":subAwardId]) >> [subAward]
        2 * versionHistoryService.findActiveVersion(SubAward.class, subAwardCode) >> versionHistory
        2 * versionHistory.getSequenceOwner() >> activeSubAward
        result!=null
        result.size() == 1
        result.get(0) == activeSubAward
    }
    def "test getLinkedSubAwards with no active and pending version history found"(){
        given:
        def awardNumber = "awardNumber"
        def subAwardId = 1L
        def subAwardCode = "subAwardCode"
        def awardId = 666L
        SubAward sourceSubAward = new SubAward(subAwardId:subAwardId,subAwardCode:subAwardCode)
        SubAward subAward = new SubAward(subAwardCode : subAwardCode)
        SubAwardFundingSource source = new SubAwardFundingSource(subAward:sourceSubAward)
        Award currentAward = new Award(awardId:awardId,awardNumber:awardNumber)
        Award award = new Award(awardNumber:awardNumber)
        SubAward activeSubAward = Mock()
        activeSubAward.getSubAwardAmountInfoList() >> []
        VersionHistory versionHistory = Mock()
        when:
        def result = service.getLinkedSubAwards(award)
        then:
        1 * awardVersionService.getActiveAwardVersion(awardNumber) >> currentAward
        1 * businessObjectService.findMatching(_, ["awardId":awardId]) >> [source]
        1 * businessObjectService.findMatching(SubAward.class, _) >> [subAward]
        1 * versionHistoryService.findActiveVersion(SubAward.class, subAwardCode) >> versionHistory
        1 * versionHistory.getSequenceOwner() >> activeSubAward
        result!=null
        result.size() == 0
    }

    //    public List<SubAward> getLinkedSubAwards(Award award) {
    //        Map<String, Object> values = new HashMap<String, Object>();
    //
    //        // BUKC-0136-a: KC Subaward: (Revert to BUKC-0111) Funding Source Award data should be from the latest FINAL version (Jira KRAFDBCK-10754, BU
    //        // Subaward QA issue 5, DFCT0011410, and DFCT0011447)
    //        // Get the current (finalized) award
    //        Award currentAward = getAwardVersionService().getActiveAwardVersion(award.getAwardNumber());
    //        List<SubAward> subAwards = new ArrayList<SubAward>();
    //
    //        if (currentAward != null) {
    //            values.put("awardId", currentAward.getAwardId());
    //
    //            Collection<SubAwardFundingSource> subAwardFundingSources = getBusinessObjectService().findMatching(SubAwardFundingSource.class, values);
    //            Set<String> subAwardSet = new TreeSet<String>();
    //
    //            for (SubAwardFundingSource subAwardFundingSource : subAwardFundingSources) {
    //                // mk: check only the active subaward id against what the search by award_id returns
    //                if (subAwardFundingSource.getSubAward().getSubAwardId()
    //                        .equals(getActiveSubAward(subAwardFundingSource.getSubAward().getSubAwardId()).getSubAwardId())) {
    //                    subAwardSet.add(subAwardFundingSource.getSubAward().getSubAwardCode());
    //                }
    //            }
    //            for (String subAwardCode : subAwardSet) {
    //                VersionHistory activeVersion = getVersionHistoryService().findActiveVersion(SubAward.class, subAwardCode);
    //                if (activeVersion == null) {
    //                    VersionHistory pendingVersion = getVersionHistoryService().findPendingVersion(SubAward.class, subAwardCode);
    //                    if (pendingVersion != null) {
    //                        // BUKC-0136-a: KC Subaward: (Revert to BUKC-0111) Funding Source Award data should be from the latest FINAL version (Jira
    //                        // KRAFDBCK-10754, BU Subaward QA issue 5, DFCT0011410, and DFCT0011447)
    //                        // Get Subaward with the Amount Info data
    //                        subAwards.add(getAmountInfo((SubAward) pendingVersion.getSequenceOwner()));
    //                    }
    //                } else {
    //                    // Get Subaward with the Amount Info data
    //                    subAwards.add(getAmountInfo((SubAward) activeVersion.getSequenceOwner()));
    //                }
    //            }
    //        }
    //
    //        return subAwards;
    //    }


    def "test getLinkedSubAwards returns empty list"(){
        given:
        def awardNumber = "awardNumber"
        Award award = new Award(awardNumber:awardNumber)
        when:
        def result = service.getLinkedSubAwards(award)
        then:
        result==[]
    }


    def "test getActiveSubAward"(){
        given:
        def subAwardCode = "code"
        Long subAwardId = 1L
        SubAward subAward = new SubAward(subAwardCode : subAwardCode)
        SubAward activeSubAward = Mock()
        activeSubAward.getSubAwardAmountInfoList() >> []
        VersionHistory versionHistory = Mock()
        when:
        def result = service.getActiveSubAward(subAwardId)
        then:
        1 * businessObjectService.findMatching(SubAward.class, _) >> [subAward]
        1 * versionHistoryService.findActiveVersion(SubAward.class, subAwardCode) >> versionHistory
        1 * versionHistory.getSequenceOwner() >> activeSubAward
        result !=null
    }

    def "test updateSubAwardFundingSource"(){
        given:
        def subAwardId = 1L
        Set<Long> subAwardIds = new HashSet<Long>()
        subAwardIds.add(subAwardId)
        def awardNumber = "awardNumber"
        def awardId = 666L
        Award award = new Award(awardNumber:awardNumber,awardId:awardId)
        def subAwardCode = "subAwardCode"
        SubAward subAward = new SubAward(subAwardCode:subAwardCode)
        SubAward activeVersion = Mock()
        activeVersion.getSubAwardAmountInfoList() >> []
        VersionHistory versionHistory = Mock()
        Award sourceAward = new Award(awardNumber:awardNumber)
        SubAwardFundingSource subAwardFundingSource = new SubAwardFundingSource(award:sourceAward)
        SubAward newVersion = new SubAward(subAwardFundingSourceList:[subAwardFundingSource])
        SubAwardDocument subAwardDocument = Mock()
        DocumentHeader documentHeader = Mock()
        subAwardDocument.getDocumentHeader() >> documentHeader
        def documentDescription = "documentDescription"
        documentHeader.getDocumentDescription() >> documentDescription
        activeVersion.getSubAwardDocument() >> subAwardDocument
        when:
        def result = service.updateSubAwardFundingSource(subAwardIds, award)
        then:
        1 * businessObjectService.findMatching(SubAward.class, _) >> [subAward]
        1 * versionHistoryService.findActiveVersion(SubAward.class, subAwardCode) >> versionHistory
        1 * versioningService.createNewVersion(activeVersion) >> newVersion
        1 * versionHistory.getSequenceOwner() >> activeVersion
        subAwardFundingSource.awardId == awardId
        newVersion.subAwardFundingSource == subAwardFundingSource
        1 * documentService.getNewDocument(_) >> subAwardDocument
        1 * documentService.routeDocument(subAwardDocument,
                "Update Subaward Funding Source to link the Subaward with the newly created version of the Award",
                new ArrayList<AdHocRouteRecipient>())
        result != null
        result.size() == 1
        result.get(0) == newVersion
    }
    def "test updateSubAwardFundingSource when newVersion doesn't have subAwardFundingSourceList"(){
        given:
        def subAwardId = 1L
        Set<Long> subAwardIds = new HashSet<Long>()
        subAwardIds.add(subAwardId)
        Award award = new Award()
        def subAwardCode = "subAwardCode"
        SubAward subAward = new SubAward(subAwardCode:subAwardCode)
        SubAward activeSubAward = Mock()
        activeSubAward.getSubAwardAmountInfoList() >> []
        VersionHistory versionHistory = Mock()
        SubAward newVersion = new SubAward(subAwardFundingSourceList:[])

        when:
        def result = service.updateSubAwardFundingSource(subAwardIds, award)
        then:
        1 * businessObjectService.findMatching(SubAward.class, _) >> [subAward]
        1 * versionHistoryService.findActiveVersion(SubAward.class, subAwardCode) >> versionHistory
        1 * versioningService.createNewVersion(activeSubAward) >> newVersion
        1 * versionHistory.getSequenceOwner() >> activeSubAward
        result == []
    }
    def "test updateSubAwardFundingSource with empty subAwardIds set"(){
        given:
        Set<Long> subAwardIds = new HashSet<Long>()
        Award award = new Award()
        when:
        def result = service.updateSubAwardFundingSource(subAwardIds, award)
        then:
        result == []
    }

    def "test finalizeTheExistingVersion"(){
        given:
        String awardNumber = "n1"
        String documentNumber = "documentNumber"
        SubAwardDocument subAwardDocument = new SubAwardDocument(documentNumber: documentNumber)
        Long awardId = 1L
        Award award = new Award(awardNumber:awardNumber, awardId:awardId)
        Award sourceAward = new Award(awardNumber:awardNumber)
        SubAwardDocument sourceSubAwardDocument = new SubAwardDocument(documentNumber:documentNumber)
        SubAwardFundingSource source = new SubAwardFundingSource(award:sourceAward)
        SubAward pendingVersion = Spy()
        pendingVersion.subAwardFundingSourceList = [source]
        pendingVersion.subAwardDocument = sourceSubAwardDocument

        when:
        service.finalizeTheExistingVersion(pendingVersion, award)

        then:

        pendingVersion.refreshReferenceObject("subAwardDocument") >> {}
        1 * documentService.getByDocumentHeaderId(documentNumber) >> subAwardDocument
        source.getAwardId() == awardId
        pendingVersion.getSubAwardFundingSource() == source
        subAwardDocument.getSubAward() == pendingVersion
        pendingVersion.getSubAwardDocument() == subAwardDocument
        1 * documentService.routeDocument(subAwardDocument,
                "Update Subaward Funding Source to link the Subaward with the newly created version of the Award",
                new ArrayList<AdHocRouteRecipient>())
    }

    def "test finalizeTheExistingVersion awardNumbers are not equals"(){
        given:
        SubAward pendingVersion = Mock()
        SubAwardFundingSource source = Mock()
        String awardNumber = "n1"
        Award award = new Award(awardNumber:awardNumber)
        when:
        service.finalizeTheExistingVersion(pendingVersion, award)
        then:
        1 * pendingVersion.getSubAwardFundingSourceList() >> [source]
        1 * source.getAward() >> new Award()
    }
    def "test finalizeTheExistingVersion has empty subAwardFundingSourceList"(){
        given:
        SubAward pendingVersion = Mock()
        Award award = Mock()
        when:
        service.finalizeTheExistingVersion(pendingVersion, award)
        then:
        1 * pendingVersion.getSubAwardFundingSourceList() >> []
    }
}
