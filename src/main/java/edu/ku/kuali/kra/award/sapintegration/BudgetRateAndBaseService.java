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

import org.kuali.coeus.sys.api.model.ScaleTwoDecimal;
import org.kuali.kra.award.home.Award;

/**
 * This service helps with calculations related to budget rate and base. Kuali
 * Coeus does not provide a service that provides this kind of information, so
 * one has been implemented as part of the SAP integration work in order to
 * derive this kind of information.
 *
 * @author Eric Westfall (ewestfal@gmail.com)
 */
public interface BudgetRateAndBaseService {

    /**
     * Calculates the applicable F & A rate for the given award. This should be
     * the F&A rate based on the start date of the award's budget.
     *
     * @param award
     *            the award to calculate the applicable rate for
     * @return a BudgetDecimal representing the rate, or null or no rate for the
     *         given affective date could be found
     * @throws IllegalArgumentException
     *             if the given award is null
     * @throws IllegalArgumentException
     *             if the given award does not have a budget
     * @throws IllegalStateException
     *             if the located budget does not have a start date
     */
    ScaleTwoDecimal calculateApplicableFandARate(Award award);

}
