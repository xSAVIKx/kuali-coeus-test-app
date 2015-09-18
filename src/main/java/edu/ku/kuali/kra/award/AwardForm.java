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
package edu.ku.kuali.kra.award;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.kuali.coeus.sys.framework.service.KcServiceLocator;
import org.kuali.kra.award.awardhierarchy.AwardHierarchy;
import org.kuali.kra.award.home.Award;
import org.kuali.kra.award.printing.AwardPrintNotice;
import org.kuali.kra.timeandmoney.document.TimeAndMoneyDocument;
import org.kuali.rice.kew.api.exception.WorkflowException;

import edu.ku.kuali.kra.award.home.AwardExtension;
import edu.ku.kuali.kra.timeandmoney.AwardVersionHistory;
import edu.ku.kuali.kra.timeandmoney.service.TimeAndMoneyHistoryService;

/**
 * This class represents the Award Form Struts class.
 */
public class AwardForm extends org.kuali.kra.award.AwardForm {

    // BUKC-0023: Add History tab
    private Map<Object, Object> timeAndMoneyHistory;
    private List<Integer> columnSpan;

    private boolean parentAward;

    List<TimeAndMoneyDocument> timeAndMoneyInfoForPrintNotice;

    private AwardPrintNotice awardPrintNotice;

    public AwardForm() {
        super.initialize();
        columnSpan = new ArrayList<Integer>();
        timeAndMoneyHistory = new LinkedHashMap<Object, Object>();
        awardPrintNotice = new AwardPrintNotice();
        timeAndMoneyInfoForPrintNotice = new ArrayList<TimeAndMoneyDocument>();
    }

    // BUKC-0023: Add History tab
    public List<AwardVersionHistory> getAwardVersionHistoryList() throws WorkflowException {
        return getAwardVersionHistoryList(this.getAwardHierarchyBean().getRootNode());
    }

    public Map<Object, Object> getTimeAndMoneyHistory() {
        return timeAndMoneyHistory;
    }

    public void setTimeAndMoneyHistory(Map<Object, Object> timeAndMoneyHistory) {
        this.timeAndMoneyHistory = timeAndMoneyHistory;
    }

    public List<Integer> getColumnSpan() {
        return columnSpan;
    }

    public void setColumnSpan(List<Integer> columnSpan) {
        this.columnSpan = columnSpan;
    }

    /**
     * Get all version history list recursively
     *
     * @param awardHierarchy
     *            object of the current award
     * @return a list of all versions
     * @throws WorkflowException
     */
    public List<AwardVersionHistory> getAwardVersionHistoryList(AwardHierarchy awardHierarchy) throws WorkflowException {
        List<AwardVersionHistory> awardVersionHistoryList = new ArrayList<AwardVersionHistory>();
        if (awardHierarchy != null) {
            TimeAndMoneyHistoryService tamhs = KcServiceLocator.getService(TimeAndMoneyHistoryService.class);
            // try {
            // tamhs.getTimeAndMoneyHistory(awardHierarchy.getAwardNumber(),
            // getTimeAndMoneyHistory(), getColumnSpan());
            // } catch (WorkflowException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }

            tamhs.buildTimeAndMoneyHistoryObjects(awardHierarchy.getAwardNumber(), awardVersionHistoryList, true);
            if (awardHierarchy.hasChildren()) {
                for (AwardHierarchy awardHierarchyChild : awardHierarchy.getChildren()) {
                    awardVersionHistoryList.addAll(getAwardVersionHistoryList(awardHierarchyChild));
                }
            }
        }
        return awardVersionHistoryList;
    }

    /**
     * gets BU Award Print Notice object
     * 
     * @return awardPrintNotice represents the object of awardPrintNotice
     */
    public AwardPrintNotice getAwardPrintNotice() {
        return awardPrintNotice;
    }

    /**
     * sets Award Print Notice object
     * 
     * @param awardPrintNotice
     */
    public void setAwardPrintNotice(AwardPrintNotice awardPrintNotice) {
        this.awardPrintNotice = awardPrintNotice;
    }

    public List<TimeAndMoneyDocument> getTimeAndMoneyInfoForPrintNotice() {
        return timeAndMoneyInfoForPrintNotice;
    }

    public void setTimeAndMoneyInfoForPrintNotice(List<TimeAndMoneyDocument> timeAndMoneyInfoForPrintNotice) {
        this.timeAndMoneyInfoForPrintNotice = timeAndMoneyInfoForPrintNotice;
    }

    // BUKC-0014: KC/SAP Interface - Get transmitted awards
    /**
     * Get Transmitted Awards. loop through awards. if it has children AND NONE
     * of the children have the same account number then add it to return list
     *
     * @return a list of transmitted awards
     */
    public List<AwardHierarchy> getTransmittedAwardsWithUnmatchedChildren() {
        List<AwardHierarchy> returnList = new ArrayList<AwardHierarchy>();

        if (((AwardExtension) this.getAwardHierarchyBean().getRootNode().getAward().getExtension()).getLastTransmissionDate() != null) {
            for (AwardHierarchy awardHierarchy : this.getAwardHierarchyBean().getRootNode().getChildren()) {
                if (awardHierarchy.hasChildren()) {
                    String currentAccountNumber = awardHierarchy.getAward().getAccountNumber();
                    if (currentAccountNumber != null || ((AwardExtension) awardHierarchy.getAward().getExtension()).getLastTransmissionDate() != null) {
                        for (AwardHierarchy awardHierarchy2 : awardHierarchy.getChildren()) {
                            Award childAward = awardHierarchy2.getAward();
                            if (!currentAccountNumber.equals(childAward.getAccountNumber())) {
                                returnList.add(awardHierarchy2);
                            }
                        }
                    }
                }
            }
        }

        return returnList;
    }

    /**
     * Determine if the current award is a parent award
     * 
     * @return true if the current award is a parent, false otherwise.
     */
    public boolean isParentAward() {
        return getAwardDocument().getAward().getAwardNumber().endsWith("-00001") || getAwardDocument().getAward().getAwardNumber().endsWith("-00000");
    }
}
