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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.kuali.coeus.common.budget.framework.nonpersonnel.BudgetRateAndBase;
import org.kuali.coeus.sys.api.model.ScaleTwoDecimal;
import org.kuali.kra.award.budget.AwardBudgetExt;
import org.kuali.kra.award.home.Award;
import org.kuali.rice.krad.service.BusinessObjectService;

/**
 * This service implementation helps with calculations related to budget rate
 * and base. Kuali Coeus does not provide a service that provides this kind of
 * information, so one has been implemented as part of the SAP integration work
 * in order to derive this kind of information.
 *
 * @author Eric Westfall (ewestfal@gmail.com)
 */
public class BudgetRateAndBaseServiceImpl implements BudgetRateAndBaseService {

	private BusinessObjectService businessObjectService;

	/**
	 * Uses the budget start date as the effective date
	 *
	 * @see edu.ku.kuali.kra.award.sapintegration.BudgetRateAndBaseService#calculateApplicableFandARate(org.kuali.kra.award.home.Award)
	 */

	public ScaleTwoDecimal calculateApplicableFandARate(Award award) {
		if (award == null) {
			throw new IllegalArgumentException("Award was null");
		}
		AwardBudgetExt budget = award.getAwardDocument()
				.getBudgetVersionOverview();
		if (budget == null) {
			throw new IllegalArgumentException(
					"Cannot locate budget for the given award with number: "
							+ award.getAwardNumber());
		}
		Date effectiveDate = budget.getStartDate();
		if (budget.getStartDate() == null) {
			throw new IllegalStateException("Budget " + budget.getBudgetId()
					+ " does not have a start date.");
		}
		Long budgetId = budget.getBudgetId();
		Map<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put("budgetId", budgetId);
		Collection<BudgetRateAndBase> budgetRates = businessObjectService
				.findMatching(BudgetRateAndBase.class, fieldValues);
		if (budgetRates != null) {

			// BU Customization ID: N/A mkousheh 20120208 - Get the maximum
			// applied rate rather than effective date calc
			ScaleTwoDecimal maxAppliedRate = ScaleTwoDecimal.ZERO;
			for (BudgetRateAndBase budgetRate : budgetRates) {
				if (budgetRate.getAppliedRate().isGreaterThan(maxAppliedRate)) {
					maxAppliedRate = budgetRate.getAppliedRate();
				}
			}
			return maxAppliedRate;
			/*
			 * if ((effectiveDate.after(budgetRate.getStartDate()) &&
			 * effectiveDate.before(budgetRate.getEndDate())) ||
			 * effectiveDate.equals(budgetRate.getStartDate()) ||
			 * effectiveDate.equals(budgetRate.getEndDate())) { return
			 * budgetRate.getAppliedRate(); } }
			 */
		}
		return null;
	}

	/**
	 * @param businessObjectService
	 */
	public void setBusinessObjectService(
			BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}
}
