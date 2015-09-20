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
package edu.ku.kuali.kra.subaward.web.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kra.infrastructure.Constants;
import org.kuali.kra.subaward.SubAwardForm;
import org.kuali.kra.subaward.bo.SubAward;
import org.kuali.kra.subaward.document.SubAwardDocument;
import org.kuali.kra.subaward.subawardrule.SubAwardDocumentRule;

public class SubAwardFinancialAction extends org.kuali.kra.subaward.web.struts.action.SubAwardFinancialAction {

    /**
     * @see org.kuali.kra.web.struts.action.KraTransactionalDocumentActionBase #save(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */

    @Override
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        SubAwardForm subAwardForm = (SubAwardForm) form;
        SubAwardDocument subAwardDocument = subAwardForm.getSubAwardDocument();// .getSubAward();
        SubAward subAward = subAwardForm.getSubAwardDocument().getSubAward();// .getSubAward();
        // BUKC-0143: Validation for - Subaward Financial - Amendment No. field (ENHC0013244)
        if (new SubAwardDocumentRule().processAddSubAwardAmountInfoBusinessRules(subAward.getSubAwardAmountInfo(), subAward)) {

            // if (new SubAwardDocumentRule().processRunAuditBusinessRules(subAwardDocument)) {
            ActionForward forward = super.save(mapping, form, request, response);
            return forward;
        } else {
            return mapping.findForward(Constants.MAPPING_BASIC);
        }
    }
}
