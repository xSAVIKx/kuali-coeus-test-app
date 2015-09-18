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
package edu.ku.kuali.kra.award.sapintegration;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * BU SAP Integration: Validation errors are associated with the id of the award which triggered the error.
 * Each award undergoing validation could potentially produce zero or more errors.  Global
 * errors are supported through the use of a method for adding global validation errors
 * to the results.
 *
 * @author Eric Westfall (ewestfal@gmail.com)
 */
public class ValidationError {

    private String errorKey;
    private List<String> errorParams;

    public ValidationError(String errorKey, List<String> errorParams) {
        if (StringUtils.isBlank(errorKey)) {
            throw new IllegalArgumentException("The error key must be non-null.");
        }
        this.errorKey = errorKey;
        this.errorParams = new ArrayList<String>(errorParams);
        if (errorParams != null) {
            Collections.copy(this.errorParams, errorParams);
        }
    }

    public String getErrorKey() {
        return errorKey;
    }

    public List<String> getErrorParams() {
        return Collections.unmodifiableList(errorParams);
    }

}
