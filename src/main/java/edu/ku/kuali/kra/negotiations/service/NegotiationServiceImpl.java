/*
 * Copyright 2005-2014 The Kuali Foundation
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
package edu.ku.kuali.kra.negotiations.service;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kra.negotiations.bo.NegotiationActivity;
import org.kuali.kra.negotiations.bo.NegotiationActivityHistoryLineBean;

/**
 * Service impl for NegotiationService.
 */
public class NegotiationServiceImpl extends org.kuali.kra.negotiations.service.NegotiationServiceImpl {
    private static final String DATE_FORMAT_MM_DD_YYYY = "MM/dd/yyyy";

    @Override
    public List<NegotiationActivityHistoryLineBean> getNegotiationActivityHistoryLineBeans(List<NegotiationActivity> activities) {
        List<NegotiationActivityHistoryLineBean> beans = new ArrayList<NegotiationActivityHistoryLineBean>();
        for (NegotiationActivity activity : activities) {
            if (activity.getLocation() != null && activity.getActivityType() != null) {
                NegotiationActivityHistoryLineBean bean = new NegotiationActivityHistoryLineBean(activity);
                beans.add(bean);
            }
        }
        Collections.sort(beans);

        // now set the effective dates and calculate the location days.
        Date previousStartDate = null;
        Date previousEndDate = null;
        String previousLocation = "";
        int counter = 1;
        List<NegotiationActivityHistoryLineBean> beansToReturn = new ArrayList<NegotiationActivityHistoryLineBean>();
        for (NegotiationActivityHistoryLineBean bean : beans) {
            if (StringUtils.equals(previousLocation, bean.getLocation())) {
                if (isDateBetween(bean.getStartDate(), previousStartDate, previousEndDate)
                        && isDateBetween(bean.getEndDate(), previousStartDate, previousEndDate)) {
                    // current date range lies within the previous date range
                    setBeanStuff(bean, null, null, "0 Days");
                    // leave previous alone
                } else if (isDateBetween(bean.getStartDate(), previousStartDate, previousEndDate) && (bean.getEndDate() != null)
                        && bean.getEndDate().after(previousEndDate)) {
                    // current date range starts within the previous range, but
                    // finishes past it.
                    Date previousEndDatePlusOneDay = new Date(previousEndDate.getTime() + NegotiationActivity.MILLISECS_PER_DAY);
                    previousEndDate = bean.getEndDate();
                    setBeanStuff(bean, previousEndDatePlusOneDay, bean.getEndDate(),
                            NegotiationActivity.getNumberOfDays(previousEndDatePlusOneDay, bean.getEndDate()));
                } else {
                    // completely separate range.
                    previousStartDate = bean.getStartDate();
                    previousEndDate = bean.getEndDate();
                    setBeanStuff(bean, bean.getStartDate(), bean.getEndDate(),
                            NegotiationActivity.getNumberOfDays(bean.getStartDate(), bean.getEndDate()));
                }
            } else {
                // new location so set the effective date
                previousStartDate = bean.getStartDate();
                previousEndDate = bean.getEndDate();
                previousLocation = bean.getLocation();
                setBeanStuff(bean, bean.getStartDate(), bean.getEndDate(),
                        NegotiationActivity.getNumberOfDays(bean.getStartDate(), bean.getEndDate()));

                // BUKC-0150: Negotiation - Line skip on Activity/Location History page (Neg. QA Testing Issue 13)
                /*
                 * if (!beansToReturn.isEmpty()) {
                 * beansToReturn.add(new NegotiationActivityHistoryLineBean());
                 * }
                 */
            }
            bean.setLineNumber(String.valueOf(counter));
            beansToReturn.add(bean);
            counter++;
        }
        return beansToReturn;
    }

    private boolean isDateBetween(Date checkDate, Date rangeStart, Date rangeEnd) {
        if (rangeStart == null) {
            return false;
        }
        if (checkDate == null) {
            checkDate = new Date(Calendar.getInstance().getTimeInMillis());
        }
        if (rangeEnd == null) {
            rangeEnd = new Date(Calendar.getInstance().getTimeInMillis());
        }
        boolean startOk = rangeStart.equals(checkDate) || rangeStart.before(checkDate);
        boolean endOk = rangeEnd.equals(checkDate) || rangeEnd.after(checkDate);
        return startOk && endOk;
    }

    private void setBeanStuff(NegotiationActivityHistoryLineBean bean, Date efectiveLocationStartDate, Date efectiveLocationEndDate,
            String locationDays) {
        bean.setEfectiveLocationEndDate(efectiveLocationEndDate);
        bean.setEfectiveLocationStartDate(efectiveLocationStartDate);
        bean.setLocationDays(locationDays);
    }

}
