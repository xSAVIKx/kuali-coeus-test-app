package edu.ku.kuali.kra.subaward.options

import org.kuali.rice.core.api.util.ConcreteKeyValue
import org.kuali.rice.core.api.util.KeyValue

import spock.lang.Specification

class SubawardModificationTypeTypeValuesFinderSpockTest extends Specification {
    SubawardModificationTypeTypeValuesFinder finder

    def "setup"(){
        finder = new SubawardModificationTypeTypeValuesFinder()
    }

    def "test getKeyValues"(){
        given:
        List<KeyValue> expected = new ArrayList<KeyValue>()
        expected.add(0, new ConcreteKeyValue("", "select"))
        expected.add(1, new ConcreteKeyValue("New", "New"))
        expected.add(2, new ConcreteKeyValue("Continuation", "Continuation"))
        expected.add(3, new ConcreteKeyValue("Increment", "Increment"))
        expected.add(4, new ConcreteKeyValue("No Cost Extension", "No Cost Extension"))
        expected.add(5, new ConcreteKeyValue("Converted Record", "Converted Record"))
        expected.add(6, new ConcreteKeyValue("Other", "Other"))
        when:
        def result = finder.getKeyValues()
        then:
        result != null
        result.size() == expected.size()
        result == expected
    }
}
