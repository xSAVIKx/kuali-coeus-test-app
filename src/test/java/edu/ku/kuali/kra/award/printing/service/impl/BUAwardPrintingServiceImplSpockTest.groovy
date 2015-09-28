package edu.ku.kuali.kra.award.printing.service.impl

import org.kuali.coeus.common.framework.version.history.VersionHistory
import org.kuali.kra.award.AwardAmountInfoService
import org.kuali.kra.award.awardhierarchy.AwardHierarchyService
import org.kuali.kra.award.budget.AwardBudgetExt
import org.kuali.kra.award.document.AwardDocument
import org.kuali.kra.award.home.Award
import org.kuali.rice.coreservice.framework.parameter.ParameterService
import org.kuali.rice.krad.service.BusinessObjectService

import spock.lang.Specification
import spock.lang.Unroll

class BUAwardPrintingServiceImplSpockTest extends Specification {
    BUAwardPrintingServiceImpl service

    private BusinessObjectService businessObjectService = Mock()
    private ParameterService parameterService = Mock()
    private AwardAmountInfoService awardAmountInfoService = Mock()
    private AwardHierarchyService awardHierarchyService = Mock()

    def "setup"(){
        service = new BUAwardPrintingServiceImpl(businessObjectService:businessObjectService, parameterService:parameterService,awardAmountInfoService:awardAmountInfoService,awardHierarchyService:awardHierarchyService)
    }

    def "test findVersionHistory"(){
        given:
        def versionName = "versionName"
        def versionHistoryArray = []
        when:
        def result = service.findVersionHistory(Object.class, versionName)
        then:
        1 * businessObjectService.findMatching(VersionHistory.class, _ as Map<String,Object>) >> versionHistoryArray
        result == versionHistoryArray
    }

    def "test getLastBudgetVersion throws NPE cause awardDocument.getAward.getBudgets is null"(){
        given:
        AwardDocument awardDocument = Mock()
        Award award = Mock()
        awardDocument.getAward() >> award
        when:
        service.getLastBudgetVersion(awardDocument)
        then:
        thrown(NullPointerException.class)
    }
    def "test getLastBudgetVersion throws NPE cause awardDocument.getAward is null"(){
        given:
        AwardDocument awardDocument = Mock()
        when:
        service.getLastBudgetVersion(awardDocument)
        then:
        thrown(NullPointerException.class)
    }
    def "test getLastBudgetVersion throws NPE cause awardDocument is null"(){
        when:
        service.getLastBudgetVersion(null)
        then:
        thrown(NullPointerException.class)
    }

    def "test getLastBudgetVersion return null"(){
        given:
        AwardDocument awardDocument = Mock()
        Award award = Mock()
        AwardBudgetExt awardBudgetExt = Mock()
        def awardBudgetDocumentVersions = []
        when:
        def result = service.getLastBudgetVersion(awardDocument)
        then:
        1 * award.getBudgets() >> awardBudgetDocumentVersions
        1 * awardDocument.getAward() >> award
        result == null
    }
    @Unroll
    def "test getLastBudgetVersion with #amount elements"(){
        given:
        AwardDocument awardDocument = Mock()
        Award award = Mock()
        AwardBudgetExt awardBudgetExt = Mock()
        def awardBudgetDocumentVersions = createAwardBudgetExtList(amount)
        when:
        def result = service.getLastBudgetVersion(awardDocument)
        then:
        1 * award.getBudgets() >> awardBudgetDocumentVersions
        1 * awardDocument.getAward() >> award
        result == awardBudgetDocumentVersions.get(amount-1)
        where:
        amount << [1, 2, 3]
    }

    def createAwardBudgetExtList(amount){
        def res = []
        for(int i = 0 ; i < amount;i++){
            res.add(createAwardBudgetExt(i))
        }
        return res
    }

    def createAwardBudgetExt(awardId){
        def awd = new AwardBudgetExt()
        awd.setAwardId(awardId)
        return awd
    }
}
