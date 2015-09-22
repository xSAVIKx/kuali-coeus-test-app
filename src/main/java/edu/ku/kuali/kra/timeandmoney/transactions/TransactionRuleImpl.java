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
package edu.ku.kuali.kra.timeandmoney.transactions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.coeus.sys.framework.service.KcServiceLocator;
import org.kuali.kra.award.home.Award;
import org.kuali.kra.award.home.AwardAmountInfo;
import org.kuali.kra.infrastructure.Constants;
import org.kuali.kra.infrastructure.KeyConstants;
import org.kuali.kra.timeandmoney.document.TimeAndMoneyDocument;
import org.kuali.kra.timeandmoney.history.TransactionDetail;
import org.kuali.kra.timeandmoney.service.ActivePendingTransactionsService;
import org.kuali.kra.timeandmoney.transactions.AddTransactionRuleEvent;
import org.kuali.kra.timeandmoney.transactions.AwardAmountTransaction;
import org.kuali.kra.timeandmoney.transactions.PendingTransaction;
import org.kuali.rice.coreservice.framework.parameter.ParameterConstants;

import edu.ku.kuali.kra.infrastructure.BUConstants;

/**
 * The AwardPaymentScheduleRuleImpl class process business rules when working on with transactions on T&M
 */
public class TransactionRuleImpl extends org.kuali.kra.timeandmoney.transactions.TransactionRuleImpl {

    private static final String NEW_AWARD_AMOUNT_TRANSACTION = "newAwardAmountTransaction";
    private static final String TRANSACTION_TYPE_CODE = ".transactionTypeCode";

    // BUKC-0025: Add Budget Period field to T&M
    private static final String BUDGET_PERIOD_PROPERTY = "budgetPeriod";
    private static final String BUDGET_PERIOD_PROPERTY_PARM = "Period (Budget Period)";

    // BUKC-0053: Enforce validation on direct and F&A costs when broken out (QA issue 42 and JIRA KRAFDBCK-9911)
    private static final String OBLIGATED_AMOUNT_PROPERTY = "obligatedAmount";
    private static final String ANTICIPATED_AMOUNT_PROPERTY = "anticipatedAmount";

    /**
     * This method processes new Pending Transaction rules
     *
     * @param addTransactionRuleEvent
     *            addTransactionRuleEvent
     * @return boolean
     */
    @Override
    public boolean processAddPendingTransactionBusinessRules(AddTransactionRuleEvent addTransactionRuleEvent) {
        boolean valid = super.processAddPendingTransactionBusinessRules(addTransactionRuleEvent);

        // BUKC-0025: Add Budget Period field to T&M
        valid &= isBudgetPeriodFieldComplete(addTransactionRuleEvent.getPendingTransactionItemForValidation());

        // BUKC-0021: Adding transactions is not allowed when transaction type is No Cost Extension or Administrative Changes
        valid &= validateAwardTransactionType(addTransactionRuleEvent);

        // BUKC-0053: Enforce validation on direct and F&A costs when broken out (QA issue 42 and JIRA KRAFDBCK-9911)
        if (valid) {
            addTransactionRuleEvent.getTimeAndMoneyDocument().add(addTransactionRuleEvent.getPendingTransactionItemForValidation());
            List<Award> awards = processTransactions(addTransactionRuleEvent.getTimeAndMoneyDocument());
            addTransactionRuleEvent.getTimeAndMoneyDocument().getPendingTransactions()
            .remove(addTransactionRuleEvent.getPendingTransactionItemForValidation());
            Award award = getLastSourceAwardReferenceInAwards(awards, addTransactionRuleEvent.getPendingTransactionItemForValidation()
                    .getSourceAwardNumber());
            // if source award is External, then check values against target award.
            if (award == null) {
                award = getLastTargetAwardReferenceInAwards(awards, addTransactionRuleEvent.getPendingTransactionItemForValidation()
                        .getDestinationAwardNumber());
            }

            if (award != null) {
                valid &= validateSourceObligatedFundsBrokenOut(addTransactionRuleEvent.getPendingTransactionItemForValidation(), award);
                valid &= validateSourceAnticipatedFundsBrokenOut(addTransactionRuleEvent.getPendingTransactionItemForValidation(), award);
                // need to remove the award amount info created from this process transactions call so there won't be a double entry in collection.
                for (Award curAward : awards) {
                    curAward.refreshReferenceObject("awardAmountInfos");
                }
            }
        }
        return valid;
    }

    /**
     * validate transaction type
     *
     * @param event
     * @return
     */
    private boolean validateAwardTransactionType(AddTransactionRuleEvent event) {
        boolean valid = true;
        // add the transaction to the document so we can simulate processing the transaction.
        event.getTimeAndMoneyDocument().add(event.getPendingTransactionItemForValidation());
        if (!(event.getTimeAndMoneyDocument().getAwardAmountTransactions().get(0).getTransactionTypeCode() == null)) {
            int transactionTypeCode = event.getTimeAndMoneyDocument().getAwardAmountTransactions().get(0).getTransactionTypeCode();

            if (transactionTypeCode == BUConstants.AWARD_TRANSACTION_TYPE_ADMINISTRATION_CHANGE
                    || transactionTypeCode == BUConstants.AWARD_TRANSACTION_TYPE_NO_COST_EXTENSION) {
                reportError(NEW_AWARD_AMOUNT_TRANSACTION + TRANSACTION_TYPE_CODE, BUConstants.PARENT_TRANSACTION_TYPE_NOCOST_EXT_OR_ADMIN_CHANGE);
                valid = false;
            }
        }
        event.getTimeAndMoneyDocument().getPendingTransactions().remove(event.getTimeAndMoneyDocument().getPendingTransactions().size() - 1);
        return valid;
    }

    // BUKC-0025: Add Budget Period field to T&M

    /**
     * This method validates budget period is present
     *
     * @param pendingTransactionItem
     *            current transaction to be added
     * @return boolean
     */
    protected boolean isBudgetPeriodFieldComplete(PendingTransaction pendingTransactionItem) {
        boolean itemValid = ((PendingTransactionExtension) pendingTransactionItem.getExtension()).getBudgetPeriod() != null;
        if (!itemValid) {
            reportError(BUDGET_PERIOD_PROPERTY, KeyConstants.ERROR_REQUIRED, BUDGET_PERIOD_PROPERTY_PARM);
        }

        return itemValid;
    }

    private boolean validateSourceObligatedFundsBrokenOut(PendingTransaction pendingTransaction, Award award) {
        AwardAmountInfo awardAmountInfo = award.getLastAwardAmountInfo();
        boolean valid = true;

        String directIndirectEnabledValue = getParameterService().getParameterValueAsString(Constants.PARAMETER_MODULE_AWARD,
                ParameterConstants.DOCUMENT_COMPONENT, "ENABLE_AWD_ANT_OBL_DIRECT_INDIRECT_COST");
        if (directIndirectEnabledValue.equals("1")) {
            if (awardAmountInfo.getObligatedTotalDirect().isNegative()) {
                reportError(OBLIGATED_AMOUNT_PROPERTY, BUConstants.ERROR_OBLIGATED_DIRECT_AMOUNT_INVALID);
                valid = false;
            }

            if (awardAmountInfo.getObligatedTotalIndirect().isNegative()) {
                reportError(OBLIGATED_AMOUNT_PROPERTY, BUConstants.ERROR_OBLIGATED_INDIRECT_AMOUNT_INVALID);
                valid = false;
            }
        }
        return valid;
    }

    private boolean validateSourceAnticipatedFundsBrokenOut(PendingTransaction pendingTransaction, Award award) {
        AwardAmountInfo awardAmountInfo = award.getLastAwardAmountInfo();
        boolean valid = true;

        String directIndirectEnabledValue = getParameterService().getParameterValueAsString(Constants.PARAMETER_MODULE_AWARD,
                ParameterConstants.DOCUMENT_COMPONENT, "ENABLE_AWD_ANT_OBL_DIRECT_INDIRECT_COST");
        if (directIndirectEnabledValue.equals("1")) {
            if (awardAmountInfo.getAnticipatedTotalDirect().isNegative()) {
                reportError(ANTICIPATED_AMOUNT_PROPERTY, BUConstants.ERROR_ANTICIPATED_DIRECT_AMOUNT_INVALID);
                valid = false;
            }
            if (awardAmountInfo.getAnticipatedTotalIndirect().isNegative()) {
                reportError(ANTICIPATED_AMOUNT_PROPERTY, BUConstants.ERROR_ANTICIPATED_INDIRECT_AMOUNT_INVALID);
                valid = false;
            }
        }
        return valid;

    }

    private List<Award> processTransactions(TimeAndMoneyDocument timeAndMoneyDocument) {
        Map<String, AwardAmountTransaction> awardAmountTransactionItems = new HashMap<String, AwardAmountTransaction>();
        List<Award> awardItems = new ArrayList<Award>();
        List<TransactionDetail> transactionDetailItems = new ArrayList<TransactionDetail>();
        ActivePendingTransactionsService service = KcServiceLocator.getService(ActivePendingTransactionsService.class);
        service.processTransactionsForAddRuleProcessing(timeAndMoneyDocument, timeAndMoneyDocument.getAwardAmountTransactions().get(0),
                awardAmountTransactionItems, awardItems, transactionDetailItems);

        return awardItems;
    }

    private Award getLastSourceAwardReferenceInAwards(List<Award> awards, String sourceAwardNumber) {
        Award returnAward = null;
        for (Award award : awards) {
            if (award.getAwardNumber() == sourceAwardNumber) {
                returnAward = award;
            }
        }
        if (returnAward == null) {
            returnAward = getAwardVersionService().getWorkingAwardVersion(sourceAwardNumber);
            // if(returnAward == null){
            // returnAward = getActiveAwardVersion(sourceAwardNumber);
            // }
        }
        return returnAward;
    }

    private Award getLastTargetAwardReferenceInAwards(List<Award> awards, String targetAwardNumber) {
        Award returnAward = null;
        for (Award award : awards) {
            if (award.getAwardNumber() == targetAwardNumber) {
                returnAward = award;
            }
        }
        if (returnAward == null) {
            returnAward = getAwardVersionService().getWorkingAwardVersion(targetAwardNumber);
        }
        return returnAward;
    }

}
