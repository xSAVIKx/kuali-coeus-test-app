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
package edu.ku.kuali.kra.institutionalproposal.web.struts.action;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.kuali.coeus.common.framework.version.VersionException;
import org.kuali.coeus.sys.framework.service.KcServiceLocator;
import org.kuali.kra.infrastructure.Constants;
import org.kuali.kra.institutionalproposal.document.InstitutionalProposalDocument;
import org.kuali.kra.institutionalproposal.home.InstitutionalProposal;
import org.kuali.kra.institutionalproposal.proposallog.ProposalLog;
import org.kuali.kra.institutionalproposal.proposallog.ProposalLogUtils;
import org.kuali.kra.institutionalproposal.web.struts.form.InstitutionalProposalForm;
import org.kuali.kra.negotiations.service.NegotiationService;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kns.question.ConfirmationQuestion;
import org.kuali.rice.krad.util.KRADConstants;

/**
 * This class...
 */
public class InstitutionalProposalHomeAction extends org.kuali.kra.institutionalproposal.web.struts.action.InstitutionalProposalHomeAction {

    private static final String VERSION_EDITPENDING_PROMPT_KEY = "message.award.version.editpending.prompt";

    /**
     * Constructs a InstitutionalProposalHomeAction.java.
     */
    public InstitutionalProposalHomeAction() {
        super();
    }

    /**
     * This method is used to handle the edit button action on an ACTIVE Institutional Proposal. If no Pending version exists for the same
     * proposalNumber, a new IP version is created. If a Pending version exists, the user is prompted as to whether she would
     * like to edit the Pending version. Answering Yes results in that Pending version InstitutionalProposalDocument to be opened. Answering No
     * simply returns the user to the ACTIVE document screen
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    public ActionForward editOrVersion(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        InstitutionalProposalForm institutionalProposalForm = (InstitutionalProposalForm) form;
        InstitutionalProposalDocument institutionalProposalDocument = institutionalProposalForm.getInstitutionalProposalDocument();

        InstitutionalProposal institutionalProposal = institutionalProposalDocument.getInstitutionalProposal();
        InstitutionalProposal pendingProposal = findPendingVersion(institutionalProposal.getProposalNumber());

        ActionForward forward;
        if (pendingProposal != null) {
            Object question = request.getParameter(KRADConstants.QUESTION_CLICKED_BUTTON);
            forward = question == null ? showPromptForEditingPendingVersion(mapping, institutionalProposalForm, request, response)
                    : processPromptForEditingPendingVersionResponse(mapping, request, response, institutionalProposalForm, pendingProposal);
        } else {
            forward = createAndSaveNewVersion(response, institutionalProposalForm, institutionalProposalDocument, institutionalProposal);
        }
        return forward;
    }

    private InstitutionalProposal findPendingVersion(String proposalNumber) {
        return getInstitutionalProposalVersioningService().getPendingInstitutionalProposalVersion(proposalNumber);
    }

    private ActionForward createAndSaveNewVersion(HttpServletResponse response, InstitutionalProposalForm institutionalProposalForm,
            InstitutionalProposalDocument institutionalProposalDocument, InstitutionalProposal institutionalProposal) throws VersionException,
            WorkflowException, IOException {

        InstitutionalProposalDocument newInstitutionalProposalDocument = getInstitutionalProposalService().createAndSaveNewVersion(
                institutionalProposal, institutionalProposalDocument);
        // BUKC-116- Fix an issue with IP to save BU's IP_EXT record when saving new and versioning a PL.
        getDocumentService().saveDocument(newInstitutionalProposalDocument);
        reinitializeForm(institutionalProposalForm, newInstitutionalProposalDocument);

        return new ActionRedirect(makeDocumentOpenUrl(newInstitutionalProposalDocument));
    }

    private ActionForward showPromptForEditingPendingVersion(ActionMapping mapping, InstitutionalProposalForm form, HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return this.performQuestionWithoutInput(mapping, form, request, response, "EDIT_OR_VERSION_QUESTION_ID",
                getResources(request).getMessage(VERSION_EDITPENDING_PROMPT_KEY), KRADConstants.CONFIRMATION_QUESTION, KRADConstants.MAPPING_CANCEL,
                "");
    }

    private ActionForward processPromptForEditingPendingVersionResponse(ActionMapping mapping, HttpServletRequest request,
            HttpServletResponse response, InstitutionalProposalForm institutionalProposalForm, InstitutionalProposal institutionalProposal)
            throws WorkflowException, IOException {
        ActionForward forward;
        Object buttonClicked = request.getParameter(KRADConstants.QUESTION_CLICKED_BUTTON);
        if (ConfirmationQuestion.NO.equals(buttonClicked)) {
            forward = mapping.findForward(Constants.MAPPING_BASIC);
        } else {
            initializeFormWithInstutitionalProposal(institutionalProposalForm, institutionalProposal);
            response.sendRedirect(makeDocumentOpenUrl(institutionalProposalForm.getInstitutionalProposalDocument()));
            forward = null;
        }
        return forward;
    }

    /**
     * This method prepares the AwardForm with the document found via the Award lookup
     * Because the helper beans may have preserved a different AwardForm, we need to reset these too
     *
     * @param institutionalProposalForm
     * @param document
     */
    private void reinitializeForm(InstitutionalProposalForm institutionalProposalForm, InstitutionalProposalDocument document)
            throws WorkflowException {
        institutionalProposalForm.populateHeaderFields(document.getDocumentHeader().getWorkflowDocument());
        institutionalProposalForm.setDocument(document);
        // document.setDocumentSaveAfterAwardLookupEditOrVersion(true);
        institutionalProposalForm.initialize();
    }

    private String makeDocumentOpenUrl(InstitutionalProposalDocument newInstitutionalProposalDocument) {
        String workflowUrl = getKualiConfigurationService().getPropertyValueAsString(KRADConstants.WORKFLOW_URL_KEY);
        String url = String.format("%s/DocHandler.do?command=displayDocSearchView&docId=%s", workflowUrl,
                newInstitutionalProposalDocument.getDocumentNumber());
        return url;
    }

    private void initializeFormWithInstutitionalProposal(InstitutionalProposalForm institutionalProposalForm,
            InstitutionalProposal institutionalProposal) throws WorkflowException {
        reinitializeForm(institutionalProposalForm, findDocumentForInstitutionalProposal(institutionalProposal));
    }

    private InstitutionalProposalDocument findDocumentForInstitutionalProposal(InstitutionalProposal institutionalProposal) throws WorkflowException {
        InstitutionalProposalDocument document = (InstitutionalProposalDocument) getDocumentService().getByDocumentHeaderId(
                institutionalProposal.getInstitutionalProposalDocument().getDocumentNumber());
        document.setInstitutionalProposal(institutionalProposal);
        return document;
    }

    @Override
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        InstitutionalProposalForm ipForm = (InstitutionalProposalForm) form;
        InstitutionalProposal ip = ipForm.getInstitutionalProposalDocument().getInstitutionalProposal();
        if (!ip.getInstitutionalProposalScienceKeywords().isEmpty()) {
            ip.setScienceCodeIndicator("1");
        } else {
            ip.setScienceCodeIndicator("0");
        }
        ActionForward forward = super.save(mapping, form, request, response);
        ProposalLog proposalLog = retrieveProposalLog(ipForm.getProposalNumber());
        if (proposalLog != null && !proposalLog.getLogStatus().equals(ProposalLogUtils.getProposalLogSubmittedStatusCode())) {
            // ipForm.getInstitutionalProposalDocument().getInstitutionalProposal().doProposalLogDataFeed(proposalLog);
            getProposalLogService().promoteProposalLog(proposalLog.getProposalNumber());
            this.getNegotationService().promoteProposalLogNegotiation(proposalLog.getProposalNumber(), ip.getProposalNumber());
        }
        ip.setSponsorNihMultiplePi(getSponsorHierarchyService().isSponsorNihMultiplePi(ip.getSponsorCode()));

        // BUKC-0116 - Fix an issue with IP to save BU's IP_EXT record when saving new and versioning a PL.

        getBusinessObjectService().save(ip.getExtension());

        return forward;
    }

    @Override
    public NegotiationService getNegotationService() {
        return KcServiceLocator.getService(NegotiationService.class);
    }

    private ProposalLog retrieveProposalLog(String proposalNumber) {
        Map<String, String> criteria = new HashMap<String, String>();
        criteria.put("proposalNumber", proposalNumber);
        return getBusinessObjectService().findByPrimaryKey(ProposalLog.class, criteria);
    }
}
