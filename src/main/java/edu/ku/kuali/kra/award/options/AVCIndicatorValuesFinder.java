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
 * BUKC-0002: AVC Indicator field on Award Module under Payment, Reports & Terms
 * Tab on Panel Payments & Invoices under sub panel Additional Financial
 * Information.
 */
public class AVCIndicatorValuesFinder extends UifKeyValuesFinderBase {

    /**
     * @see org.kuali.rice.krad.uif.control.UifKeyValuesFinderBase#getKeyValues()
     */
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        keyValues.add(0, new ConcreteKeyValue("125%", "125%"));
        keyValues.add(1, new ConcreteKeyValue("100%", "100%"));
        keyValues.add(1, new ConcreteKeyValue("0", "0"));
        return keyValues;
    }
}
