/*
 * Copyright 2005-2010 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.ku.kuali.kra.award.timeandmoney;

import java.util.List;

import org.kuali.coeus.sys.api.model.ScaleTwoDecimal;
import org.kuali.kra.award.timeandmoney.AwardDirectFandADistribution;
import org.kuali.kra.award.timeandmoney.AwardDirectFandADistributionRuleEvent;

import edu.ku.kuali.kra.infrastructure.BUConstants;

public class AwardDirectFandADistributionRuleImpl extends org.kuali.kra.award.timeandmoney.AwardDirectFandADistributionRuleImpl {

    private static final String NEW_AWARD_DIRECT_FNA_DISTRIBUTION = "newAwardDirectFandADistribution";

    // BUKC-0024: Direct/F&A Funds Distribution panel validation
    private static final String DISTRIBUTIONS_REQUIRED = ".distributionsRequired";
    private static final String DISTRIBUTIONS_NOT_ALLOWED = ".distributionsNotAllowed";

    AwardDirectFandADistribution awardDirectFandADistribution;

    // BUKC-0024: Direct/F&A Funds Distribution panel validation
    /**
     * @see org.kuali.kra.award.timeandmoney.AwardDirectFandADistributionRuleImpl#processAwardDirectFandADistributionBusinessRules(org.kuali.kra.award.timeandmoney.AwardDirectFandADistributionRuleEvent)
     */
    @Override
    public boolean processAwardDirectFandADistributionBusinessRules(AwardDirectFandADistributionRuleEvent awardDirectFandADistributionRuleEvent) {
        // BUKC-0052: Fix and issue with duplicates warning messages caused by
        // calling validation twice (from BUKC-0024)
        boolean valid = true;
        // boolean valid =
        // super.processAwardDirectFandADistributionBusinessRules(awardDirectFandADistributionRuleEvent);

        valid &= validateAwardDirectFandADistribution(awardDirectFandADistributionRuleEvent);

        return valid;
    }

    /**
     * @see org.kuali.kra.award.timeandmoney.AwardDirectFandADistributionRule#processAddAwardDirectFandADistributionRuleBusinessRules
     *      (org.kuali.kra.award.timeandmoney.AwardDirectFandADistributionRuleEvent)
     */
    @Override
    public boolean processAddAwardDirectFandADistributionBusinessRules(AwardDirectFandADistributionRuleEvent awardDirectFandADistributionRuleEvent) {
        boolean valid = super.processAddAwardDirectFandADistributionBusinessRules(awardDirectFandADistributionRuleEvent);
        valid &= validateAwardDirectFandADistribution(awardDirectFandADistributionRuleEvent);

        return valid;
    }

    /**
     * This is a helper method validates at least one direct distribution is
     * entered on the parent and no distributions are allowed on child award
     *
     * @param awardDirectFandADistributions
     * @param awardNumber
     * @return
     */
    private boolean validateAwardDirectFandADistribution(AwardDirectFandADistributionRuleEvent awardDirectFandADistributionRuleEvent) {
        boolean valid = true;
        this.awardDirectFandADistribution = awardDirectFandADistributionRuleEvent.getAwardDirectFandADistributionForValidation();
        List<AwardDirectFandADistribution> awardDirectFandADistributions = awardDirectFandADistributionRuleEvent.getTimeAndMoneyDocument().getAward()
                .getAwardDirectFandADistributions();

        String awardNumber = awardDirectFandADistributionRuleEvent.getTimeAndMoneyDocument().getAwardNumber();

        boolean distributionsPopulated = false;

        for (AwardDirectFandADistribution awardDirectFandADistribution : awardDirectFandADistributions) {
            if (awardDirectFandADistribution.getDirectCost().isGreaterThan(ScaleTwoDecimal.ZERO)
                    || awardDirectFandADistribution.getIndirectCost().isGreaterThan(ScaleTwoDecimal.ZERO)) {
                distributionsPopulated = true;
                break;
            }
        }

        // At least one distribution is required on parent award
        if (awardNumber.endsWith("-00001") && !distributionsPopulated) {
            valid = false;
            reportError(NEW_AWARD_DIRECT_FNA_DISTRIBUTION + DISTRIBUTIONS_REQUIRED, BUConstants.ERROR_AWARD_FANDA_DISTRIB_REQUIRED_PARENT_AWARD);
        }

        // No distributions are allowed on child award
        if (!awardNumber.endsWith("-00001") && distributionsPopulated) {
            valid = false;
            reportError(NEW_AWARD_DIRECT_FNA_DISTRIBUTION + DISTRIBUTIONS_NOT_ALLOWED, BUConstants.ERROR_AWARD_FANDA_DISTRIB_NOT_ALLOWED_CHILD_AWARD);
        }

        return valid;
    }

}
