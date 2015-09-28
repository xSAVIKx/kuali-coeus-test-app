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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import edu.ku.kuali.kra.infrastructure.BUConstants;

/**
 * This class represents the Struts Action for Notes & Attachments
 * page(AwardNotesAndAttachments.jsp)
 */
public class AwardNotesAndAttachmentsAction extends org.kuali.kra.award.web.struts.action.AwardNotesAndAttachmentsAction {

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
}
