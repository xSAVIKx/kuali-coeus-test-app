package edu.ku.kuali.kra.subaward.service

import org.codehaus.groovy.transform.tailrec.VariableReplacedListener.*
import org.kuali.coeus.common.framework.version.VersioningService
import org.kuali.coeus.common.framework.version.history.VersionHistory
import org.kuali.coeus.common.framework.version.history.VersionHistoryService
import org.kuali.kra.award.home.Award
import org.kuali.kra.award.version.service.AwardVersionService
import org.kuali.kra.subaward.bo.SubAward
import org.kuali.kra.subaward.document.SubAwardDocument
import org.kuali.rice.coreservice.framework.parameter.ParameterService
import org.kuali.rice.krad.bo.AdHocRouteRecipient
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

    //    Map<String, Object> values = new HashMap<String, Object>();
    //    values.put("subAwardId", subAwardId);
    //    List<SubAward> subAwards = (List<SubAward>) getBusinessObjectService().findMatching(SubAward.class, values);
    //    SubAward subAward = subAwards.get(0);
    //
    //    SubAward activeSubAward = (SubAward) getVersionHistoryService().findActiveVersion(SubAward.class, subAward.getSubAwardCode())
    //            .getSequenceOwner();
    //
    //    getAmountInfo(activeSubAward);
    //    return activeSubAward;

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
