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
package edu.ku.kuali.kra.award.web.struts.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.coeus.sys.framework.service.KcServiceLocator;
import org.kuali.kra.award.document.AwardDocument;
import org.kuali.kra.award.home.Award;
import org.kuali.kra.timeandmoney.document.TimeAndMoneyDocument;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.KRADConstants;

import edu.ku.kuali.kra.award.AwardForm;
import edu.ku.kuali.kra.infrastructure.BUConstants;

/**
 * This class extends Kuali foundation class that represents the Struts Action for Award page(AwardHome.jsp)
 */
public class AwardHomeAction extends org.kuali.kra.award.web.struts.action.AwardHomeAction {

    // BUKC-0023: Add History tab
    /**
     * This method gets called upon navigation to History tab.
     *
     * @param mapping
     *            - The ActionMapping used to select this instance
     * @param form
     *            - The optional ActionForm bean for this request (if any)
     * @param request
     *            - The HTTP request we are processing
     * @param response
     *            - The HTTP response we are creating
     * @return an ActionForward instance describing where and how control should be forwarded, or null if the response has already been completed.
     */
    public ActionForward history(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return mapping.findForward(BUConstants.MAPPING_AWARD_HISTORY_PAGE);
    }

    /**
     * This method...
     *
     * @param mapping
     *            - The ActionMapping used to select this instance
     * @param form
     *            - The optional ActionForm bean for this request (if any)
     * @param request
     *            - The HTTP request we are processing
     * @param response
     *            - The HTTP response we are creating
     * @return an ActionForward instance describing where and how control should be forwarded, or null if the response has already been completed.
     * @throws Exception
     *             if thrown
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionForward actionForward = super.execute(mapping, form, request, response);
        AwardForm awardForm = (AwardForm) form;
        // BUKC-0022: Add BU's Award Notification
        populateTimeAndMoneyInfoForPrintNotice(awardForm);
        String commandParam = request.getParameter(KRADConstants.PARAMETER_COMMAND);
        if (StringUtils.isNotBlank(commandParam) && commandParam.equals("initiate")
                && StringUtils.isNotBlank(request.getParameter(AWARD_ID_PARAMETER_NAME))) {
            Award award = findSelectedAward(request.getParameter(AWARD_ID_PARAMETER_NAME));
            initializeFormWithAward(awardForm, award);
        }
        if (StringUtils.isNotBlank(commandParam) && "redirectAwardHistoryFullViewForPopup".equals(commandParam)) {
            String awardDocumentNumber = request.getParameter("awardDocumentNumber");
            String awardNumber = request.getParameter("awardNumber");
            actionForward = redirectAwardHistoryFullViewForPopup(mapping, form, request, response, awardDocumentNumber, awardNumber);
        }

        return actionForward;
    }

    /**
     * This METHODS populates Time and Money documents for BU Customization for Award Print Notice
     *
     * @param awardForm
     *            Award Form
     * @throws WorkflowException
     */
    private void populateTimeAndMoneyInfoForPrintNotice(AwardForm awardForm) throws WorkflowException {
        Map<String, Object> fieldValues = new HashMap<String, Object>();
        // populate the rootAwardNumber so T&M docs will display on initial visit to Award Actions page.
        if (awardForm.getRootAwardNumber() == null) {
            populateAwardHierarchy(awardForm);
        }
        fieldValues.put("rootAwardNumber", awardForm.getRootAwardNumber());
        BusinessObjectService businessObjectService = KcServiceLocator.getService(BusinessObjectService.class);

        // businessObjectService now Fetches Records based upon document number and also reverses the fetched resultSet
        List<TimeAndMoneyDocument> timeAndMoneyDocuments = (List<TimeAndMoneyDocument>) businessObjectService.findMatchingOrderBy(
                TimeAndMoneyDocument.class, fieldValues, "documentNumber", false);

        // We should exclude the canceled documents from the list
        List<TimeAndMoneyDocument> removedCancelledTimeAndMoneyDocumentList = new LinkedList<TimeAndMoneyDocument>();

        for (TimeAndMoneyDocument timeAndMoneyDocument : timeAndMoneyDocuments) {
            TimeAndMoneyDocument document = (TimeAndMoneyDocument) getDocumentService().getByDocumentHeaderId(
                    timeAndMoneyDocument.getDocumentNumber());
            if (document.getDocumentRouteStatus()) {
                removedCancelledTimeAndMoneyDocumentList.add(timeAndMoneyDocument);
            }
        }
        int listSize;
        listSize = 5;
        if (removedCancelledTimeAndMoneyDocumentList.size() < 5) {
            listSize = removedCancelledTimeAndMoneyDocumentList.size();
        }

        List<TimeAndMoneyDocument> timeAndMoneyDocsForPrintNotice = new ArrayList<TimeAndMoneyDocument>();

        // Flip the sequence of time and money documents to resolve the most recent order
        for (int i = 0; i < listSize; i++) {
            timeAndMoneyDocsForPrintNotice.add(removedCancelledTimeAndMoneyDocumentList.get(i));
        }

        // Apply the final sort by timestamp here using comparator
        Collections.sort(timeAndMoneyDocsForPrintNotice, new Comparator<TimeAndMoneyDocument>() {
            @Override
            public int compare(TimeAndMoneyDocument timeAndMoneyDoc1, TimeAndMoneyDocument timeAndMoneyDoc2) {

                return timeAndMoneyDoc1.getUpdateTimestamp().compareTo(timeAndMoneyDoc2.getUpdateTimestamp()); // time2 - time1 to
                // sort descending...
            };
        });
        awardForm.setTimeAndMoneyInfoForPrintNotice(timeAndMoneyDocsForPrintNotice);
    }

    private ActionForward redirectAwardHistoryFullViewForPopup(ActionMapping mapping, ActionForm form, HttpServletRequest request,
            HttpServletResponse response, String awardDocumentNumber, String awardNumber) throws Exception {
        // super.populateAwardHierarchy(form);
        AwardForm awardForm = (AwardForm) form;
        response.sendRedirect("awardHistory.do?methodToCall=openWindow&awardDocumentNumber=" + awardDocumentNumber + "&awardNumber=" + awardNumber
                + "&docTypeName=" + awardForm.getDocTypeName());

        return null;
    }

    private void initializeFormWithAward(AwardForm awardForm, Award award) throws WorkflowException {
        reinitializeAwardForm(awardForm, findDocumentForAward(award));
    }

    private void reinitializeAwardForm(AwardForm awardForm, AwardDocument document) throws WorkflowException {
        awardForm.populateHeaderFields(document.getDocumentHeader().getWorkflowDocument());
        awardForm.setDocument(document);
        document.setDocumentSaveAfterAwardLookupEditOrVersion(true);
        awardForm.initialize();
    }

    private AwardDocument findDocumentForAward(Award award) throws WorkflowException {
        AwardDocument document = (AwardDocument) getDocumentService().getByDocumentHeaderId(award.getAwardDocument().getDocumentNumber());
        document.setAward(award);
        return document;
    }

}
