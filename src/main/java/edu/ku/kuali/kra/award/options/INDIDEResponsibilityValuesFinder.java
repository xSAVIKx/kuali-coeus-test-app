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

package edu.ku.kuali.kra.award.options;

import java.util.ArrayList;
import java.util.List;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.uif.control.UifKeyValuesFinderBase;

/**
 * BUKC-0002: IND/IDE Responsibility field on Award Module under Custom Data Tab
 * on Clinical Trial Information panel under sub panel Clinical Trial
 * Information
 */
public class INDIDEResponsibilityValuesFinder extends UifKeyValuesFinderBase {

    /**
     * @see org.kuali.rice.krad.keyvalues.KeyValuesBase#getKeyValues()
     */
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        keyValues.add(0, new ConcreteKeyValue("", "Select"));
        keyValues.add(1, new ConcreteKeyValue("PI", "Principal Investigator"));
        keyValues.add(2, new ConcreteKeyValue("Sponsor", "Sponsor"));
        return keyValues;
    }
}
