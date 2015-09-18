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
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.coeus.sys.framework.service.KcServiceLocator;
import org.kuali.kra.award.document.AwardDocument;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.krad.uif.control.UifKeyValuesFinderBase;

import edu.ku.kuali.kra.infrastructure.BUConstants;

/**
 * BUKC-0002: Federal Rate Agreement Dated field on Award Module under Payment,
 * Reports & Terms Tab on Payments & Invoices under sub panel Additional
 * Financial Information.
 */
public class FederalRateAgreementDateValuesFinder extends UifKeyValuesFinderBase {

    /**
     * @see org.kuali.rice.krad.keyvalues.KeyValuesBase#getKeyValues()
     */
    public List<KeyValue> getKeyValues() {
        ParameterService parameterService = (ParameterService) KcServiceLocator.getService("parameterService");
        Collection<String> paramValues = parameterService.getParameterValuesAsString(AwardDocument.class,
                BUConstants.FEDERAL_RATE_DATE_OVERHEAD_KEY_FIELD_MAPPINGS);

        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        keyValues.add(0, new ConcreteKeyValue("NA", "N/A"));
        for (String paramValue : paramValues) {
            String[] keyValueString = StringUtils.split(paramValue, "=");
            keyValues.add(new ConcreteKeyValue(keyValueString[0], keyValueString[0]));
        }
        return keyValues;
    }
}
