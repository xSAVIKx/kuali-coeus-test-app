package edu.ku.kuali.kra.subaward.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.uif.control.UifKeyValuesFinderBase;

public class SubawardModificationTypeTypeValuesFinder extends UifKeyValuesFinderBase {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        keyValues.add(0, new ConcreteKeyValue("", "select"));
        keyValues.add(1, new ConcreteKeyValue("New", "New"));
        keyValues.add(2, new ConcreteKeyValue("Continuation", "Continuation"));
        keyValues.add(3, new ConcreteKeyValue("Increment", "Increment"));
        keyValues.add(4, new ConcreteKeyValue("No Cost Extension", "No Cost Extension"));
        keyValues.add(5, new ConcreteKeyValue("Converted Record", "Converted Record"));
        keyValues.add(6, new ConcreteKeyValue("Other", "Other"));

        return keyValues;
    }
}
