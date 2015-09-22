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
package edu.ku.kuali.kra.timeandmoney;

import java.util.List;

import org.kuali.kra.award.timeandmoney.AwardDirectFandADistribution;
import org.kuali.kra.award.timeandmoney.AwardDirectFandADistributionRuleEvent;
import org.kuali.kra.infrastructure.Constants;
import org.kuali.kra.timeandmoney.document.TimeAndMoneyDocument;
import org.kuali.kra.timeandmoney.transactions.AddTransactionRuleEvent;
import org.kuali.kra.timeandmoney.transactions.TransactionRuleEvent;
import org.kuali.kra.timeandmoney.transactions.TransactionRuleImpl;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.MessageMap;

import edu.ku.kuali.kra.award.timeandmoney.AwardDirectFandADistributionRuleImpl;

/**
 * Main Business Rule class for <code>{@link org.kuali.kra.timeandmoney.document.TimeAndMoneyDocument}</code>.
 * Responsible for delegating rules to independent rule classes.
 *
 */
public class TimeAndMoneyDocumentRule extends org.kuali.kra.timeandmoney.TimeAndMoneyDocumentRule {

    // BUKC-0023: Direct/F&A Funds Distribution panel validation
    /**
     * This method validates Award Direct/F&A Distribution business validation
     *
     * @param document
     *            T&M Document
     * @return true if there is no errors
     */
    public boolean processAwardDirectFandADistributionBusinessRules(Document document) {
        boolean valid = true;
        MessageMap errorMap = GlobalVariables.getMessageMap();
        TimeAndMoneyDocument timeAndMoneyDocument = (TimeAndMoneyDocument) document;
        int i = 0;
        List<AwardDirectFandADistribution> awardDirectFandADistributions = timeAndMoneyDocument.getAward().getAwardDirectFandADistributions();
        errorMap.addToErrorPath(DOCUMENT_ERROR_PATH);
        errorMap.addToErrorPath(AWARD_ERROR_PATH);
        String errorPath = "awardDirectFandADistribution[" + i + Constants.RIGHT_SQUARE_BRACKET;
        errorMap.addToErrorPath(errorPath);

        AwardDirectFandADistributionRuleEvent event = new AwardDirectFandADistributionRuleEvent(errorPath, timeAndMoneyDocument,
                awardDirectFandADistributions);

        valid &= new AwardDirectFandADistributionRuleImpl().processAwardDirectFandADistributionBusinessRules(event);
        errorMap.removeFromErrorPath(errorPath);
        errorMap.removeFromErrorPath(AWARD_ERROR_PATH);
        errorMap.removeFromErrorPath(DOCUMENT_ERROR_PATH);
        return valid;
    }

    /**
     * This method validates business rules when adding budget periods data
     * on Award Direct/F&A Distribution panel
     *
     * @param event
     *            AwardDirectFandADistributionRuleEvent
     * @return true if there is no errors
     */
    @Override
    public boolean processAddAwardDirectFandADistributionBusinessRules(AwardDirectFandADistributionRuleEvent event) {
        return new AwardDirectFandADistributionRuleImpl().processAddAwardDirectFandADistributionBusinessRules(event);
    }

    // BUKC-0020: Adding transactions is not allowed when transaction type is No Cost Extension or Administrative Changes
    // Use edu.bu.kuali.kra.timeandmoney.transactions.TransactionRuleImpl to invoke the rule for this validation
    /**
     * This method process adding PT business rules
     *
     * @param event
     *            AddTransactionRuleEvent
     * @return true if there is no errors
     */
    @Override
    public boolean processAddPendingTransactionBusinessRules(AddTransactionRuleEvent event) {
        return new TransactionRuleImpl().processAddPendingTransactionBusinessRules(event);
    }

    /**
     * This method process PT business rules
     *
     * @param event
     *            TransactionRuleEvent
     * @return true if there is no errors
     */
    @Override
    public boolean processPendingTransactionBusinessRules(TransactionRuleEvent event) {
        return new TransactionRuleImpl().processPendingTransactionBusinessRules(event);
    }

}
