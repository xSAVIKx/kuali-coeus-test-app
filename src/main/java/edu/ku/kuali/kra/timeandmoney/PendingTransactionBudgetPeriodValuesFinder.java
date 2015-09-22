package edu.ku.kuali.kra.timeandmoney;

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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kra.award.timeandmoney.AwardDirectFandADistribution;
import org.kuali.kra.timeandmoney.TimeAndMoneyForm;
import org.kuali.kra.timeandmoney.document.TimeAndMoneyDocument;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.kns.util.KNSGlobalVariables;
import org.kuali.rice.krad.uif.control.UifKeyValuesFinderBase;

// BUKC-0025: Add Budget Period field to T&M
/**
 * This class builds the dropdown list for the budget periods
 *
 */
public class PendingTransactionBudgetPeriodValuesFinder extends UifKeyValuesFinderBase {

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        keyValues.add(new ConcreteKeyValue("", "select:"));
        TimeAndMoneyForm timeAndMoneyForm = (TimeAndMoneyForm) KNSGlobalVariables.getKualiForm();
        TimeAndMoneyDocument document = timeAndMoneyForm.getTimeAndMoneyDocument();

        if (document.getAward().getAwardDirectFandADistributions() != null) {
            SimpleDateFormat outputDf = new SimpleDateFormat("MM/dd/yyyy");
            for (AwardDirectFandADistribution awardDirectFandADistribution : document.getAward().getAwardDirectFandADistributions()) {
                String startDate = outputDf.format(awardDirectFandADistribution.getStartDate());
                String endDate = outputDf.format(awardDirectFandADistribution.getEndDate());
                String period = startDate + " - " + endDate;

                keyValues.add(new ConcreteKeyValue(period, period));
            }
        }

        return keyValues;
    }

    public List<KeyValue> getKeyValues(TimeAndMoneyForm timeAndMoneyForm) {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        keyValues.add(new ConcreteKeyValue("", "select:"));
        TimeAndMoneyDocument document = timeAndMoneyForm.getTimeAndMoneyDocument();

        if (document.getAward().getAwardDirectFandADistributions() != null) {
            SimpleDateFormat outputDf = new SimpleDateFormat("MM/dd/yyyy");
            for (AwardDirectFandADistribution awardDirectFandADistribution : document.getAward().getAwardDirectFandADistributions()) {
                String startDate = outputDf.format(awardDirectFandADistribution.getStartDate());
                String endDate = outputDf.format(awardDirectFandADistribution.getEndDate());
                String period = startDate + " - " + endDate;

                keyValues.add(new ConcreteKeyValue(period, period));
            }
        }

        return keyValues;
    }

}
