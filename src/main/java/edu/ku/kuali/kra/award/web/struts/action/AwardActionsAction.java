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

import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.coeus.common.framework.print.AttachmentDataSource;
import org.kuali.coeus.common.framework.version.history.VersionHistoryService;
import org.kuali.coeus.sys.framework.service.KcServiceLocator;
import org.kuali.coeus.sys.framework.validation.AuditHelper;
import org.kuali.kra.award.awardhierarchy.AwardHierarchy;
import org.kuali.kra.award.budget.AwardBudgetExt;
import org.kuali.kra.award.budget.AwardBudgetService;
import org.kuali.kra.award.budget.document.AwardBudgetDocument;
import org.kuali.kra.award.commitments.AwardCostShare;
import org.kuali.kra.award.document.AwardDocument;
import org.kuali.kra.award.home.Award;
import org.kuali.kra.award.printing.AwardPrintNotice;
import org.kuali.kra.award.printing.AwardPrintParameters;
import org.kuali.kra.award.printing.AwardPrintType;
import org.kuali.kra.award.printing.service.AwardPrintingService;
import org.kuali.kra.infrastructure.Constants;
import org.kuali.kra.infrastructure.KeyConstants;
import org.kuali.kra.subaward.bo.SubAward;
import org.kuali.kra.timeandmoney.document.TimeAndMoneyDocument;
import org.kuali.rice.kew.api.KewApiConstants;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.ku.kuali.kra.award.AwardDocumentRule;
import edu.ku.kuali.kra.award.AwardForm;
import edu.ku.kuali.kra.award.home.AwardExtension;
import edu.ku.kuali.kra.award.printing.service.BUAwardPrintingService;
import edu.ku.kuali.kra.award.sapintegration.SapIntegrationService;
import edu.ku.kuali.kra.award.sapintegration.SapTransmission;
import edu.ku.kuali.kra.award.sapintegration.SapTransmissionResponse;
import edu.ku.kuali.kra.award.sapintegration.ValidationError;
import edu.ku.kuali.kra.award.sapintegration.ValidationResults;
import edu.ku.kuali.kra.bo.AwardTransmission;
import edu.ku.kuali.kra.bo.AwardTransmissionChild;
import edu.ku.kuali.kra.infrastructure.BUConstants;
import edu.ku.kuali.kra.subaward.service.SubAwardService;

/**
 * This class represents the Struts Action for Award Actions page(AwardActions.jsp)
 */
public class AwardActionsAction extends org.kuali.kra.award.web.struts.action.AwardActionsAction {
    private static final String AWARD_COPY_CHILD_OF_OPTION = "d";
    private static final String NEW_CHILD_SELECTED_AWARD_OPTION = "c";
    private static final String AWARD_COPY_NEW_OPTION = "a";
    private static final String NEW_CHILD_NEW_OPTION = "a";
    private static final String NEW_CHILD_COPY_FROM_PARENT_OPTION = "b";

    private static final String AWARD_PREFIX = "award-";
    private static final String AWARD_UPDATE_PREFIX = "award-update-";
    private static final String AWARD_TRANSMISSION_PREFIX = "award-transmit-";
    private static final Log LOG = LogFactory.getLog(AwardActionsAction.class);

    private static final String ZERO = "0";

    private BUAwardPrintingService bUAwardPrintingService;

    // BUKC-0071: SAP Interface - Validate Prime Sponsor/Award Type only for federal accounts and if Sponsor/Prime Sponsor are not the same - replaces
    // code for issue (BUKC-0065)
    private static final Integer FEDERAL_CODE = 1;
    private static final Integer AWARD_TYPE_CODE_SUBGRANT = 6;
    private static final Integer AWARD_TYPE_CODE_SUBCONTRACT = 11;

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
     * Based on the same method in the foundation's AwardBudgetService method
     * to get the last budget version on the award
     *
     * @param award
     *            object to get the last version of budget associated with it
     * @return BudgetVersionOverview
     */
    protected AwardBudgetExt getLastBudgetVersion(AwardDocument award) {
        @SuppressWarnings("unchecked")
        List<AwardBudgetExt> awardBudgetDocumentVersions = award.getBudgetDocumentVersions();
        AwardBudgetExt budgetVersionOverview = null;
        int versionSize = awardBudgetDocumentVersions.size();
        if (versionSize > 0) {
            budgetVersionOverview = awardBudgetDocumentVersions.get(versionSize - 1);
        }
        return budgetVersionOverview;
    }

    /**
     *
     * This method corresponds to the Create New Child behavior on Award Hierarchy JQuery UI. It calls various helper methods based on the options
     * selected in the UI.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @Override
    public ActionForward create(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        AwardForm awardForm = (AwardForm) form;
        String awardNumber = getAwardNumber(request);
        // BUKC-0020: Fix a bug to allow creating awards after 10th child
        // int index = Integer.parseInt(StringUtils.split(awardNumber, "-")[1]);
        String i = awardNumber.replaceAll("\\d*\\-0*", "");
        int index = Integer.parseInt(i);
        ActionForward forward = null;

        if (awardForm.getAwardHierarchyTempObjects().get(index).getCreateNewChildRadio() != null) {
            AwardHierarchy targetNode = findTargetNode(request, awardForm);
            String radio = awardForm.getAwardHierarchyTempObjects().get(index).getCreateNewChildRadio();
            if (StringUtils.equalsIgnoreCase(radio, NEW_CHILD_NEW_OPTION)) {
                AwardHierarchy newChildNode = awardForm.getAwardHierarchyBean().createNewChildAward(targetNode.getAwardNumber());
                forward = prepareToForwardToNewChildAward(mapping, awardForm, targetNode, newChildNode);
            } else if (StringUtils.equalsIgnoreCase(radio, NEW_CHILD_COPY_FROM_PARENT_OPTION)) {
                AwardHierarchy newChildNode = awardForm.getAwardHierarchyBean().createNewAwardBasedOnParent(targetNode.getAwardNumber());
                forward = prepareToForwardToNewChildAward(mapping, awardForm, targetNode, newChildNode);
            } else if (StringUtils.equalsIgnoreCase(radio, NEW_CHILD_SELECTED_AWARD_OPTION)) {
                String awardNumberOfNodeToCopyFrom = awardForm.getAwardHierarchyTempObjects().get(index).getNewChildPanelTargetAward();
                if (StringUtils.isEmpty(awardNumberOfNodeToCopyFrom) || StringUtils.equalsIgnoreCase(awardNumberOfNodeToCopyFrom, ZERO)) {
                    GlobalVariables.getMessageMap().putError("awardHierarchyTempObject[" + index + "].newChildPanelTargetAward",
                            KeyConstants.ERROR_CREATE_NEW_CHILD_OTHER_AWARD_NOT_SELECTED, awardNumber);
                    forward = mapping.findForward(Constants.MAPPING_AWARD_BASIC);
                } else {
                    AwardHierarchy newChildNode = awardForm.getAwardHierarchyBean().createNewChildAwardBasedOnAnotherAwardInHierarchy(
                            awardNumberOfNodeToCopyFrom, targetNode.getAwardNumber());
                    forward = prepareToForwardToNewChildAward(mapping, awardForm, targetNode, newChildNode);
                }
            }
        } else {
            GlobalVariables.getMessageMap().putError("awardHierarchyTempObject[" + index + "].newChildPanelTargetAward",
                    KeyConstants.ERROR_CREATE_NEW_CHILD_NO_OPTION_SELECTED, awardNumber);
            forward = mapping.findForward(Constants.MAPPING_AWARD_BASIC);
        }
        return forward;

    }

    /**
     * This method corresponds copy award action on Award Hierarchy UI. Depending on various options selected appropriate helper methods get called.
     *
     * @see org.kuali.kra.award.web.struts.action.AwardActionsAction#copyAward(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward copyAward(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        AwardForm awardForm = (AwardForm) form;
        String awardNumber = getAwardNumber(request);

        // BUKC-0020: Fix a bug to allow creating awards after 10th child
        // int index = Integer.parseInt(StringUtils.split(awardNumber, "-")[1]);
        String i = awardNumber.replaceAll("\\d*\\-0*", "");
        int index = Integer.parseInt(i);

        ActionForward forward = null;
        AwardHierarchy newRootNode = null;
        if (!StringUtils.isEmpty(awardForm.getAwardHierarchyTempObjects().get(index).getCopyAwardRadio())) {
            String radio = awardForm.getAwardHierarchyTempObjects().get(index).getCopyAwardRadio();
            Boolean copyDescendants = awardForm.getAwardHierarchyTempObjects().get(index).getCopyDescendants();
            AwardHierarchy targetNode = findTargetNode(request, awardForm);
            if (StringUtils.equalsIgnoreCase(radio, AWARD_COPY_NEW_OPTION)) {
                if (copyDescendants != null && copyDescendants) {
                    newRootNode = awardForm.getAwardHierarchyBean().copyAwardAndAllDescendantsAsNewHierarchy(targetNode.getAwardNumber());
                    forward = prepareToForwardToNewFinalChildAward(mapping, awardForm, request, response, targetNode, newRootNode);

                } else {
                    newRootNode = awardForm.getAwardHierarchyBean().copyAwardAsNewHierarchy(targetNode.getAwardNumber());
                    forward = prepareToForwardToNewChildAward(mapping, awardForm, targetNode, newRootNode);
                }
            } else if (StringUtils.equalsIgnoreCase(radio, AWARD_COPY_CHILD_OF_OPTION)) {
                String awardNumberOfNodeToBeParent = awardForm.getAwardHierarchyTempObjects().get(index).getCopyAwardPanelTargetAward();
                if (!StringUtils.isEmpty(awardNumberOfNodeToBeParent) && !StringUtils.equalsIgnoreCase(awardNumberOfNodeToBeParent, ZERO)) {
                    if (copyDescendants != null && copyDescendants) {
                        if (!StringUtils.isEmpty(awardNumberOfNodeToBeParent)) {
                            newRootNode = awardForm.getAwardHierarchyBean().copyAwardAndDescendantsAsChildOfAnotherAward(targetNode.getAwardNumber(),
                                    awardNumberOfNodeToBeParent);
                            forward = prepareToForwardToNewFinalChildAward(mapping, awardForm, request, response, targetNode, newRootNode);
                        }
                    } else {
                        newRootNode = awardForm.getAwardHierarchyBean().copyAwardAsChildOfAnotherAward(targetNode.getAwardNumber(),
                                awardNumberOfNodeToBeParent);
                        forward = prepareToForwardToNewChildAward(mapping, awardForm, targetNode, newRootNode);
                    }
                } else {
                    GlobalVariables.getMessageMap().putError("awardHierarchyTempObject[" + index + "].copyAwardPanelTargetAward",
                            KeyConstants.ERROR_COPY_AWARD_CHILDOF_AWARD_NOT_SELECTED, awardNumber);
                    awardForm.getFundingProposalBean().setAllAwardsForAwardNumber(null);
                    forward = mapping.findForward(Constants.MAPPING_AWARD_BASIC);
                }
            }
            // BUKC-0014: KC/SAP Interface - Nullify Transmission Date and Transmission History when copying an award
            ((AwardExtension) newRootNode.getAward().getExtension()).setLastTransmissionDate(null);
            ((AwardExtension) newRootNode.getAward().getExtension()).setAwardTransmissions(new ArrayList<AwardTransmission>());
            // BUKC-0015: Nullify grant number when copy an award when copying an award
            ((AwardExtension) newRootNode.getAward().getExtension()).setGrantNumber(null);
            if (newRootNode.hasChildren() && (copyDescendants != null && copyDescendants)) {
                for (AwardHierarchy child : newRootNode.getChildren()) {
                    ((AwardExtension) child.getAward().getExtension()).setLastTransmissionDate(null);
                    ((AwardExtension) child.getAward().getExtension()).setAwardTransmissions(new ArrayList<AwardTransmission>());
                    ((AwardExtension) child.getAward().getExtension()).setGrantNumber(null);
                    ((AwardExtension) child.getAward().getExtension()).setWalkerSourceNumber(null);
                }
            }
        } else {
            GlobalVariables.getMessageMap().putError("awardHierarchyTempObject[" + index + "].copyAwardPanelTargetAward",
                    KeyConstants.ERROR_COPY_AWARD_NO_OPTION_SELECTED, awardNumber);
            forward = mapping.findForward(Constants.MAPPING_AWARD_BASIC);
        }
        return forward;
    }

    /**
     * @see org.kuali.kra.award.web.struts.action.AwardActionsAction#findTargetNode(javax.servlet.http.HttpServletRequest,
     *      org.kuali.kra.award.AwardForm)
     */
    private AwardHierarchy findTargetNode(HttpServletRequest request, AwardForm awardForm) {
        return awardForm.getAwardHierarchyBean().getRootNode().findNodeInHierarchy(getAwardNumber(request));
    }

    /*
     * Setup all needed when creating a new final child award.
     */
    private ActionForward prepareToForwardToNewFinalChildAward(ActionMapping mapping, AwardForm awardForm, HttpServletRequest request,
            HttpServletResponse response, AwardHierarchy targetNode, AwardHierarchy newNodeToView) throws Exception {
        ActionForward forward;
        if (newNodeToView != null) {
            awardForm.setCommand(KewApiConstants.INITIATE_COMMAND);
            createDocument(awardForm);
            Award newChildAward = newNodeToView.getAward();
            if (!newNodeToView.isRootNode()) {
                setMultipleNodeHierarchyOnAwardFormTrue(newChildAward);
            }
            awardForm.getAwardDocument().setAward(newChildAward);
            awardForm.getAwardDocument().getDocumentHeader().setDocumentDescription("Copied Hierarchy");

            awardForm.getAwardHierarchyBean().recordTargetNodeState(targetNode);
            awardForm.getFundingProposalBean().setAllAwardsForAwardNumber(null);

            // BUKC-0014: KC/SAP Interface - Nullify Transmission Date and Transmission History when copying copying a child award
            ((AwardExtension) newNodeToView.getAward().getExtension()).setLastTransmissionDate(null);
            ((AwardExtension) newNodeToView.getAward().getExtension()).setAwardTransmissions(new ArrayList<AwardTransmission>());
            // BUKC-0015: Nullify grant number when copy an award when copying an award
            ((AwardExtension) newNodeToView.getAward().getExtension()).setGrantNumber(null);
            if (newNodeToView.hasChildren()) {
                for (AwardHierarchy child : newNodeToView.getChildren()) {
                    ((AwardExtension) child.getAward().getExtension()).setLastTransmissionDate(null);
                    ((AwardExtension) child.getAward().getExtension()).setAwardTransmissions(new ArrayList<AwardTransmission>());
                    ((AwardExtension) child.getAward().getExtension()).setGrantNumber(null);
                    ((AwardExtension) child.getAward().getExtension()).setWalkerSourceNumber(null);
                }
            }

            super.save(mapping, awardForm, request, response);
            super.submitAward(mapping, awardForm, request, response);
            forward = mapping.findForward(Constants.MAPPING_AWARD_HOME_PAGE);
        } else {
            forward = mapping.findForward(Constants.MAPPING_AWARD_BASIC);
        }
        return forward;
    }

    private void createAwardTransmission(AwardExtension newExt, AwardExtension oldExt) {
        // also
        newExt.setAwardTransmissions(new ArrayList<AwardTransmission>());
        for (AwardTransmission transmission : oldExt.getAwardTransmissions()) {
            AwardTransmission copiedTransmission = (AwardTransmission) ObjectUtils.deepCopy(transmission);
            copiedTransmission.setAwardId(newExt.getAwardId());
            copiedTransmission.setTransmissionId(Long.parseLong(""));
            copiedTransmission.setTransmissionChildren(new ArrayList<AwardTransmissionChild>());
            for (AwardTransmissionChild tranChild : transmission.getTransmissionChildren()) {
                AwardTransmissionChild copiedTransmissionChild = (AwardTransmissionChild) ObjectUtils.deepCopy(tranChild);
                copiedTransmissionChild.setTransmissionId(Long.parseLong(""));
                copiedTransmissionChild.setTransmissionChildId(Long.parseLong(""));
                copiedTransmissionChild.setAwardId(newExt.getAwardId());
                copiedTransmission.getTransmissionChildren().add(copiedTransmissionChild);
            }
            newExt.getAwardTransmissions().add(copiedTransmission);
        }
    }

    /**
     * Setup all needed when creating a new child award.
     */
    private ActionForward prepareToForwardToNewChildAward(ActionMapping mapping, AwardForm awardForm, AwardHierarchy targetNode,
            AwardHierarchy newNodeToView) throws WorkflowException {
        ActionForward forward;
        if (newNodeToView != null) {
            awardForm.setCommand(KewApiConstants.INITIATE_COMMAND);
            createDocument(awardForm);
            Award newChildAward = newNodeToView.getAward();
            if (!newNodeToView.isRootNode()) {
                setMultipleNodeHierarchyOnAwardFormTrue(newChildAward);
            }
            awardForm.getAwardDocument().setAward(newChildAward);
            awardForm.getAwardHierarchyBean().recordTargetNodeState(targetNode);
            awardForm.getFundingProposalBean().setAllAwardsForAwardNumber(null);
            forward = mapping.findForward(Constants.MAPPING_AWARD_HOME_PAGE);
        } else {
            forward = mapping.findForward(Constants.MAPPING_AWARD_BASIC);
        }
        return forward;
    }

    /**
     * Since a child award will always be part of a multiple award hierarchy, we need to set the boolean to true so that the anticipated
     * and obligated totals on Details & Dates tab will be uneditable on initial creation. After the initial save of document
     * this is handled in the docHandler and home methods of AwardAction.
     *
     * @param award
     */
    private void setMultipleNodeHierarchyOnAwardFormTrue(Award award) {

        award.setAwardInMultipleNodeHierarchy(true);
    }

    /**
     * BU KC/SAP Integration: Update all account numbers retrieving from SAP
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
    public ActionForward updateAccountNumbers(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        BusinessObjectService boService = KcServiceLocator.getService(BusinessObjectService.class);
        // parse through the submitted awards, and set account number to parent
        // award account number
        AwardForm awardForm = (AwardForm) form;
        AwardHierarchy rootHierarchyNode = awardForm.getAwardHierarchyBean().getRootNode();

        for (AwardHierarchy awardHierarchy : rootHierarchyNode.getChildren()) {
            boolean wipeParentAccountNumber = false;
            for (AwardHierarchy awardHierarchy2 : awardHierarchy.getChildren()) {
                // check to see if the checkbox for this award is checked, if
                // so,
                if (request.getParameterMap().containsKey(AWARD_UPDATE_PREFIX + awardHierarchy2.getAwardNumber())) {
                    awardHierarchy2.getAward().setAccountNumber(awardHierarchy.getAward().getAccountNumber());
                    boService.save(awardHierarchy2.getAward());
                    wipeParentAccountNumber = true;
                }
            }
            if (wipeParentAccountNumber) {
                awardHierarchy.getAward().setAccountNumber(null);
                boService.save(awardHierarchy.getAward());
            }
        }

        return mapping.findForward(Constants.MAPPING_AWARD_ACTIONS_PAGE);
    }

    /**
     * BU KC/SAP Integration: Validate for SAP grant transmission
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
    public ActionForward validateForTransmission(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        KcServiceLocator.getService(AuditHelper.class).setAuditMode(mapping, (AwardForm) form, true);
        // call the validate() method of the IntegrationService
        SapIntegrationService integrationService = KcServiceLocator.getService(SapIntegrationService.class);
        AwardForm awardForm = (AwardForm) form;
        AwardHierarchy primaryAwardNode = awardForm.getAwardHierarchyBean().getRootNode();
        Award primaryAward = primaryAwardNode.getAward();
        ((AwardExtension) primaryAward.getExtension()).setProposedForTransmissionIndicator(Constants.TRUE_FLAG);
        ((AwardExtension) primaryAward.getExtension()).setValidatedForTransmission(false);

        // BUKC-0071: SAP Interface - Validate Prime Sponsor/Award Type only for federal accounts and if Sponsor/Prime Sponsor are not the same -
        // replaces code for issue (BUKC-0065)
        processPrimeSponsorAwardTypeSelectionBusinessRule(primaryAward.getAwardDocument());

        // cycle through the award list and only pass on those that have been
        // check as proposed for transmission
        ArrayList<Award> awardsList = new ArrayList<Award>();

        checkAwardChildren(primaryAwardNode, awardsList, request);

        SapTransmission transmission = new SapTransmission(primaryAward, awardsList);
        ValidationResults results = integrationService.validate(transmission);
        if (results != null) {
            if (results.getGlobalValidationErrors() != null) {
                for (ValidationError error : results.getGlobalValidationErrors()) {
                    String[] errorParams = error.getErrorParams().toArray(new String[error.getErrorParams().size()]);
                    GlobalVariables.getMessageMap().putError("awardTransmission", error.getErrorKey(), errorParams);
                }
            }
            if (results.getAwardsInError() != null) {
                for (Long awardId : results.getAwardsInError()) {
                    for (ValidationError error : results.getAwardValidationError(awardId)) {
                        if (error.getErrorKey() != null) {
                            String[] errorParams = error.getErrorParams().toArray(new String[error.getErrorParams().size()]);
                            GlobalVariables.getMessageMap().putError("awardTransmission", error.getErrorKey(), errorParams);
                        }
                    }
                }
            }
        }
        if (GlobalVariables.getMessageMap() != null && GlobalVariables.getMessageMap().hasNoErrors()) {
            ((AwardExtension) primaryAward.getExtension()).setValidatedForTransmission(true);

            for (Award award : awardsList) {
                AwardDocument tmpAwardDocument = award.getAwardDocument();
                AwardBudgetExt awardBudgetVersionOverviewExt = getLastBudgetVersion(tmpAwardDocument);
                if (!"Group".equalsIgnoreCase(((AwardExtension) award.getExtension()).getChildType()) && awardBudgetVersionOverviewExt != null
                        && !Constants.BUDGET_STATUS_CODE_TO_BE_POSTED.equals(awardBudgetVersionOverviewExt.getAwardBudgetStatusCode())) {
                    // add a warning message
                    GlobalVariables.getMessageMap().putWarning("awardTransmission", "warning.award.sapintegration.awardBudgetNotToBePosted",
                            award.getAwardNumber());
                }

                // BUKC-0071: SAP Interface - Validate Prime Sponsor/Award Type only for federal accounts and if Sponsor/Prime Sponsor are not the
                // same - replaces code for issue (BUKC-0065)
                processPrimeSponsorAwardTypeSelectionBusinessRule(tmpAwardDocument);

                ((AwardExtension) award.getExtension()).setValidatedForTransmission(true);
            }
        }
        return mapping.findForward(Constants.MAPPING_AWARD_ACTIONS_PAGE);
    }

    /**
     * Process business rule to show a warning when award type is either subgrant or subcontract and prime sposor is not entered
     * or if prime sponsor is present but the award type is not subgrant nor subgrant, this validation only applies on federal accounts.
     *
     * @param awardDocument
     */
    private void processPrimeSponsorAwardTypeSelectionBusinessRule(AwardDocument awardDocument) {
        Award award = awardDocument.getAward();

        // validate only if the account is federal
        if (isFederalAccount(award)) {
            if (StringUtils.isBlank(award.getPrimeSponsorCode()) && (isSubGrant(award) || isSubContract(award))) {
                GlobalVariables.getMessageMap().putWarning("awardTransmission",
                        "warning.award.sapintegration.primeSponsor.required.for.subcontract.subgrant", award.getAwardNumber());
            }

            if (!StringUtils.isBlank(award.getPrimeSponsorCode()) && !(award.getPrimeSponsorCode().equals(award.getSponsorCode()))) {
                if (!(isSubGrant(award) || isSubContract(award))) {
                    GlobalVariables.getMessageMap().putWarning("awardTransmission",
                            "warning.award.sapintegration.subcontract.subgrant.required.for.primeSponsor", award.getAwardNumber());
                }

            }
        }
    }

    /**
     * Determines if the given Award is a federal account. This is based on the
     * description of the AccountTypeCode on the Award.
     *
     * @param award
     *            the Award to check to determine if it is a federal account or not
     * @return true if the given award is a a federal account, false if not
     */
    private boolean isFederalAccount(Award award) {
        return award.getAccountTypeCode().equals(FEDERAL_CODE);
    }

    /**
     * Determines if the given Award is a Sub-grant. This is based on the
     * description of the AwardType on the Award.
     *
     * @param award
     *            the Award to check to determine if it is a sub-grant or not
     * @return true if the given award is a sub-grant, false if not
     */
    private boolean isSubGrant(Award award) {
        return AWARD_TYPE_CODE_SUBGRANT.equals(award.getAwardTypeCode());
    }

    /**
     * Determines if the given Award is a Sub-contract. This is based on the
     * description of the AwardType on the Award.
     *
     * @param award
     *            the Award to check to determine if it is a sub-contract or not
     * @return true if the given award is a sub-contract, false if not
     */
    private boolean isSubContract(Award award) {
        return AWARD_TYPE_CODE_SUBCONTRACT.equals(award.getAwardTypeCode());
    }

    /**
     * BU KC/SAP Integration: Validate user children selection and mark object proposed for transmission
     *
     * @param hierarchyNode
     *            - The AwardHierarchy instance
     * @param awardsList
     *            - AwardList
     * @param request
     *            - The HTTP request we are processing
     */
    private void checkAwardChildren(AwardHierarchy hierarchyNode, ArrayList<Award> awardsList, HttpServletRequest request) {
        for (AwardHierarchy awardHierarchy : hierarchyNode.getChildren()) {
            // check to see if the checkbox for this award is checked, if so,
            if (request.getParameterMap().containsKey(AWARD_PREFIX + awardHierarchy.getAwardNumber())) {
                // mark the object proposed for transmission and save it to the
                // DB
                ((AwardExtension) awardHierarchy.getAward().getExtension()).setProposedForTransmissionIndicator(Constants.TRUE_FLAG);
                awardsList.add(awardHierarchy.getAward());
            } else {
                ((AwardExtension) awardHierarchy.getAward().getExtension()).setProposedForTransmissionIndicator(Constants.FALSE_FLAG);
            }

            ((AwardExtension) awardHierarchy.getAward().getExtension()).setValidatedForTransmission(false);
            BusinessObjectService boService = KcServiceLocator.getService(BusinessObjectService.class);
            awardHierarchy.getAward().setAwardId(awardHierarchy.getAward().getAwardId());
            boService.save(awardHierarchy.getAward());
            if (awardHierarchy.getChildren() != null && awardHierarchy.getChildren().size() > 0) {
                checkAwardChildren(awardHierarchy, awardsList, request);
            }
        }
    }

    /**
     * BU KC/SAP Integration: Validate user children selection for transmission and mark object proposed for transmission
     */
    private void checkAwardChildrenForTransmission(AwardHierarchy hierarchyNode, ArrayList<Award> awardsList, HttpServletRequest request) {
        for (AwardHierarchy awardHierarchy : hierarchyNode.getChildren()) {
            // check to see if the checkbox for this award is checked, if so,
            if (request.getParameterMap().containsKey(AWARD_TRANSMISSION_PREFIX + awardHierarchy.getAwardNumber())) {
                // mark the object proposed for transmission and save it to the
                // DB
                ((AwardExtension) awardHierarchy.getAward().getExtension()).setValidatedForTransmission(true);
                awardsList.add(awardHierarchy.getAward());
            }

            // recursively check grandchildren
            if ("Group".equalsIgnoreCase(((AwardExtension) awardHierarchy.getAward().getExtension()).getChildType())
                    && awardHierarchy.getChildren() != null && awardHierarchy.getChildren().size() > 0) {
                checkAwardChildrenForTransmission(awardHierarchy, awardsList, request);
            }
        }
    }

    /**
     * BU KC/SAP Integration: RDFD print functionality.
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
     *             - if an exception occurs
     */
    public ActionForward printAward(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        SapIntegrationService integrationService = KcServiceLocator.getService(SapIntegrationService.class);

        // call the transmit() method on SAPIntegrationService
        AwardForm awardForm = (AwardForm) form;
        Award primaryAward = awardForm.getAwardHierarchyBean().getRootNode().getAward();
        // cycle through the award list and only pass on those that have been
        // marked as approved for transmission
        ArrayList<Award> awardsList = new ArrayList<Award>();

        checkAwardChildren(awardForm.getAwardHierarchyBean().getRootNode(), awardsList, request);

        SapTransmission transmission = new SapTransmission(primaryAward, awardsList);
        String transmissionXml = integrationService.getTransmitXml(transmission);
        transmissionXml = StringUtils.replace(transmissionXml, "ns2:", "");

        StringReader xmlReader = new StringReader(transmissionXml);
        StreamSource xmlSource = new StreamSource(xmlReader);

        InputStream xslStream = this.getClass().getResourceAsStream("/edu/bu/kuali/kra/printing/stylesheet/sapPrint.xslt");
        StreamSource xslSource = new StreamSource(xslStream);

        StringWriter resultWriter = new StringWriter();
        StreamResult result = new StreamResult(resultWriter);

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(xslSource);
        transformer.transform(xmlSource, result);

        PrintWriter out = response.getWriter();
        out.write(resultWriter.getBuffer().toString());

        return null;
    }

    /**
     * BU KC/SAP Integration: Transmission to SAP logic. Cycle through the award list and only pass on those that have been
     * marked as approved for transmission then create a record with sending data before calling the web service.
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
     *             - if an exception occurs
     */
    public ActionForward transmitAward(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        BusinessObjectService boService = KcServiceLocator.getService(BusinessObjectService.class);
        SapIntegrationService integrationService = KcServiceLocator.getService(SapIntegrationService.class);

        // call the transmit() method on SAPIntegrationService
        AwardForm awardForm = (AwardForm) form;
        Award primaryAward = awardForm.getAwardHierarchyBean().getRootNode().getAward();
        // cycle through the award list and only pass on those that have been
        // marked as approved for transmission
        ArrayList<Award> awardsList = new ArrayList<Award>();

        checkAwardChildrenForTransmission(awardForm.getAwardHierarchyBean().getRootNode(), awardsList, request);

        if (((AwardExtension) primaryAward.getExtension()).getLastTransmissionDate() == null && awardsList.isEmpty()) {
            GlobalVariables.getMessageMap().putErrorForSectionId("awardTransmission", "error.award.sapintegration.no.validated.child.awards");
            return mapping.findForward(Constants.MAPPING_AWARD_ACTIONS_PAGE);
        }

        SapTransmission transmission = new SapTransmission(primaryAward, awardsList);
        // create transmission record with sending data
        SapTransmissionResponse transmissionResponse = integrationService.transmit(transmission);
        // SapTransmissionResponse transmissionResponse= new SapTransmissionResponse(Status.SUCCESS, null, null, null, null, null, null);
        Map<Long, String> sponsoredProgramIds = transmissionResponse.getSponsoredProgramIds();

        // BU Customization ID: N/A 20130306 20140317 - Handle SAP Walker number
        Map<Long, String> walkerIds = transmissionResponse.getWalkerIds();

        // add to transmission record with response data and timestamp
        // mark all award as transmitted
        Calendar cl = Calendar.getInstance();
        Date transmissionDate = new Date(cl.getTime().getTime());

        if (transmissionResponse != null) {

            if (transmissionResponse.isSuccess()) {
                // set last transmission date on root award
                ((AwardExtension) primaryAward.getExtension()).setLastTransmissionDate(transmissionDate);
                if (sponsoredProgramIds.containsKey(primaryAward.getAwardId())) {
                    String sponsoredProgramId = sponsoredProgramIds.get(primaryAward.getAwardId());
                    LOG.info("Found a generated sponsored program id '" + sponsoredProgramId + "' from SAP service for parent award with id: "
                            + primaryAward.getAwardId() + ".  Updating award cost share source destination.");
                    for (AwardCostShare awardCostShare : primaryAward.getAwardCostShares()) {
                        awardCostShare.setDestination(sponsoredProgramId);
                    }
                }
                generateGrantNumber(primaryAward);
                boService.save(primaryAward);
                // set last transmission date on all awards
                for (Award award : awardsList) {
                    // Integer lastBudgetVersionNumber =
                    // getAwardBudgetService().getLastBudgetVersion(award.getAwardDocument()).getBudgetVersionNumber();

                    List<AwardBudgetExt> budgetDocumentVersions = getAwardBudgetService().getAllBudgetsForAward(award);
                    // iterate through versions and post the highest one that is
                    // "to be posted"
                    for (AwardBudgetExt budgetDocumentVersion : budgetDocumentVersions) {
                        AwardBudgetExt awardBudgetVersionOverviewExt = budgetDocumentVersion;
                        // lastBudgetVersionNumber ==
                        // budgetDocumentVersion.getBudgetVersionOverview().getBudgetVersionNumber()
                        // &&
                        if (awardBudgetVersionOverviewExt != null
                                && Constants.BUDGET_STATUS_CODE_TO_BE_POSTED.equals(awardBudgetVersionOverviewExt.getAwardBudgetStatusCode())) {
                            Document document = getDocumentService().getByDocumentHeaderId(budgetDocumentVersion.getDocumentNumber());
                            LOG.info("About to attempt to post an AwardBudgetDocument. Type: " + document.getDocumentNumber());
                            if (document instanceof AwardBudgetDocument) {
                                AwardBudgetDocument awardBudgetDocument = (AwardBudgetDocument) getDocumentService().getByDocumentHeaderId(
                                        budgetDocumentVersion.getDocumentNumber());
                                getAwardBudgetService().post(awardBudgetDocument);
                            }
                        }
                    }

                    // TODO - TEMP FIX, needs to be removed!!!
                    if (!"Group".equalsIgnoreCase(((AwardExtension) award.getExtension()).getChildType())) {
                        ((AwardExtension) award.getExtension()).setLastTransmissionDate(transmissionDate);
                    }
                    if (sponsoredProgramIds.containsKey(award.getAwardId())) {
                        String sponsoredProgramId = sponsoredProgramIds.get(award.getAwardId());
                        LOG.info("Found a generated sponsored program id '" + sponsoredProgramId + "' from SAP service for award with id: "
                                + award.getAwardId() + ".  Updating award account number.");
                        award.setAccountNumber(sponsoredProgramId);
                    }

                    // BU Customization ID: N/A 20130306 20140317 - Handle SAP Walker number
                    if (walkerIds.containsKey(award.getAwardId())) {
                        String walkerId = walkerIds.get(award.getAwardId());
                        LOG.info("Found a generated walker  id '" + walkerIds + "' from SAP service for award with id: " + award.getAwardId()
                                + ".  Updating award walker number.");
                        ((AwardExtension) award.getExtension()).setWalkerSourceNumber(walkerId);
                    }

                    generateGrantNumber(award);
                    boService.save(award);
                }
            } else {
                // display error
                GlobalVariables.getMessageMap().putErrorForSectionId("awardTransmission", Constants.GRANTS_GOV_GENERIC_ERROR_KEY,
                        transmissionResponse.getMessage());
            }

            // BU Customization ID: N/A 20130429 - Issue 55 - KC_SAP Interface to display warning
            List<String> warningMessages = transmissionResponse.getWarningMessages();
            if (warningMessages.size() > 0) {
                for (String warning : warningMessages) {
                    GlobalVariables.getMessageMap().putWarning("awardTransmission", "warning.award.sapintegration.warning.message", warning);
                    LOG.info("Found a warning message : " + warning);
                }
            }

            // add transmission record
            AwardTransmission newAwardTransmission = new AwardTransmission();
            Long awardId = awardForm.getAwardHierarchyBean().getRootNode().getAward().getAwardId();
            newAwardTransmission.setAwardId(awardId);
            newAwardTransmission.setInitiatorId(GlobalVariables.getUserSession().getPrincipalId());
            newAwardTransmission.setTransmitterId(GlobalVariables.getUserSession().getPrincipalId());
            newAwardTransmission.setSentData(transmissionResponse.getSentData());
            newAwardTransmission.setReturnedData(transmissionResponse.getReceivedData());
            newAwardTransmission.setTransmissionDate(transmissionDate);
            newAwardTransmission.setSuccessIndicator(transmissionResponse.isSuccess() ? "Y" : "N");
            newAwardTransmission.setBasisOfPaymentCode(primaryAward.getBasisOfPaymentCode());
            newAwardTransmission.setAccountTypeCode(primaryAward.getAccountTypeCode());
            newAwardTransmission.setSponsorCode(primaryAward.getSponsorCode());
            newAwardTransmission.setMethodOfPaymentCode(primaryAward.getMethodOfPaymentCode());
            newAwardTransmission.setDocumentNumber(primaryAward.getAwardDocument().getDocumentNumber());

            for (Award award : awardsList) {
                // add child award transmission for each
                AwardTransmissionChild transmissionChild = new AwardTransmissionChild();
                transmissionChild.setAwardId(award.getAwardId());
                transmissionChild.setParentDocumentNumber(primaryAward.getAwardDocument().getDocumentNumber());
                transmissionChild.setChildDocumentNumber(award.getAwardDocument().getDocumentNumber());
                transmissionChild.setLeadUnitNumber(award.getLeadUnitNumber());
                transmissionChild.setChildType(((AwardExtension) award.getExtension()).getChildType());
                transmissionChild.setAwardNumber(award.getAwardNumber());
                String overheadKey = (String) GlobalVariables.getUserSession().retrieveObject("overheadKey-".concat(award.getAwardNumber()));
                GlobalVariables.getUserSession().removeObject("overheadKey-".concat(award.getAwardNumber()));
                transmissionChild.setOverheadKey(overheadKey);
                String baseCode = (String) GlobalVariables.getUserSession().retrieveObject("baseCode-".concat(award.getAwardNumber()));
                GlobalVariables.getUserSession().removeObject("baseCode-".concat(award.getAwardNumber()));
                transmissionChild.setBaseCode(baseCode);
                String offCampus = (String) GlobalVariables.getUserSession().retrieveObject("offCampus-".concat(award.getAwardNumber()));
                GlobalVariables.getUserSession().removeObject("offCampus-".concat(award.getAwardNumber()));
                transmissionChild.setOffCampus(offCampus);
                newAwardTransmission.getTransmissionChildren().add(transmissionChild);
            }

            boService.save(newAwardTransmission);
            ((AwardExtension) awardForm.getAwardHierarchyBean().getRootNode().getAward().getExtension()).getAwardTransmissions().add(
                    newAwardTransmission);
        } else {
            GlobalVariables.getMessageMap().putErrorForSectionId("awardTransmission", "error.award.sapintegration.empty.transmission.response");
        }

        return mapping.findForward(Constants.MAPPING_AWARD_ACTIONS_PAGE);
    }

    /**
     * BU KC/SAP Integration: Generate BU Grant number after a successful transmission.
     *
     * @param award
     *            to generate grant number
     */
    private void generateGrantNumber(Award award) {
        // - This field should be 8 digits long
        // - The 1st 2 digits will be '50' if the Award.Account_Type = 1;
        // - The 1st 2 digits will be '55' if the Award.Account_Type = 2;
        // - The last 6 digits will be the first 6 digits of the
        // Award.Award_number;
        // - Ex. If Award_Number = 123456-00001 and Account_Type = 1; then
        // Grant_Number = 50123456
        String grantNumber;
        if (new Integer(1).compareTo(award.getAccountTypeCode()) == 0) {
            grantNumber = "50";
        } else if (new Integer(2).compareTo(award.getAccountTypeCode()) == 0) {
            grantNumber = "55";
        } else {
            // do nothing
            return;
        }
        grantNumber = grantNumber.concat(award.getAwardNumber().substring(0, 6));
        ((AwardExtension) award.getExtension()).setGrantNumber(grantNumber);
    }

    // BUKC-0022: Add BU's Award Notification
    /**
     * This method is overridden to accommodate changes for BU AwardPrintNotice Customization
     */
    @Override
    public ActionForward printNotice(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        AwardForm awardForm = (AwardForm) form;
        Map<String, Object> reportParameters = new HashMap<String, Object>();

        reportParameters.put(AwardPrintParameters.SIGNATURE_REQUIRED.getAwardPrintParameter(), awardForm.getAwardPrintNotice().getRequireSignature());
        AwardPrintingService awardPrintService = KcServiceLocator.getService(AwardPrintingService.class);

        // Added validation to make sure the user selects at least one document number from the list
        if (!hasDocumentSelectedForPrint(awardForm)) {
            GlobalVariables.getMessageMap().putError("datavalidation", BUConstants.ERROR_BUAWARDPRINT_CHECKBOX_SELECTION_REQUIRED, new String[] {});
            return mapping.findForward(Constants.MAPPING_AWARD_BASIC);
        }
        String rootAwardNumber = awardForm.getRootAwardNumber();
        Map<String, Object> fieldValues = new HashMap<String, Object>();
        Award rootAward = getAwardVersionService().getActiveAwardVersion(rootAwardNumber);
        fieldValues.put("awardNumber", rootAwardNumber);

        reportParameters.put("timeAndMoneyList",
                populateTimeAndMoneyInfoForPrintNoticeSelectedDocs(awardForm.getTimeAndMoneyInfoForPrintNotice(), awardForm));
        reportParameters.put("rootAward", rootAward);

        // Reset selected T&M docs
        AwardPrintNotice awardPrintNotice = awardForm.getAwardPrintNotice();
        ((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice).setTimeAndMoney1(false);
        ((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice).setTimeAndMoney2(false);
        ((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice).setTimeAndMoney3(false);
        ((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice).setTimeAndMoney4(false);
        ((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice).setTimeAndMoney5(false);

        AttachmentDataSource dataStream = awardPrintService.printAwardReport(awardForm.getAwardDocument().getAward(),
                AwardPrintType.AWARD_NOTICE_REPORT, reportParameters);
        streamToResponse(dataStream, response);

        return null;
    }

    /**
     * This methods validates user selection
     *
     * @param awardForm
     *            awardForm
     * @return whether the user selected
     */
    private boolean hasDocumentSelectedForPrint(AwardForm awardForm) {
        AwardPrintNotice awardPrintNotice = awardForm.getAwardPrintNotice();
        return (((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice).getTimeAndMoney1()
                || ((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice).getTimeAndMoney2()
                || ((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice).getTimeAndMoney3()
                || ((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice).getTimeAndMoney4() || ((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice)
                    .getTimeAndMoney5());
    }

    /**
     * This method populates T&M objects
     *
     * @param timeAndMoneyDocuments
     *            a list of T&M document
     * @param awardForm
     *            award form
     * @return user selection of the presented T&M document list
     * @throws WorkflowException
     */
    private List<TimeAndMoneyDocument> populateTimeAndMoneyInfoForPrintNoticeSelectedDocs(List<TimeAndMoneyDocument> timeAndMoneyDocuments,
            AwardForm awardForm) throws WorkflowException {
        AwardPrintNotice awardPrintNotice = awardForm.getAwardPrintNotice();

        int listSize;
        listSize = 5;
        if (timeAndMoneyDocuments.size() < 5) {
            listSize = timeAndMoneyDocuments.size();
        }

        List<TimeAndMoneyDocument> timeAndMoneyDocsForPrintNotice = new ArrayList<TimeAndMoneyDocument>();
        for (int i = 0; i < listSize; i++) {
            timeAndMoneyDocsForPrintNotice.add(timeAndMoneyDocuments.get(i));
        }
        // Filter list for selected T&M docs
        List<TimeAndMoneyDocument> returnList = new ArrayList<TimeAndMoneyDocument>();

        if (timeAndMoneyDocsForPrintNotice.size() > 0 && ((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice).getTimeAndMoney1()) {
            returnList.add(timeAndMoneyDocsForPrintNotice.get(0));
        }
        if (timeAndMoneyDocsForPrintNotice.size() > 1 && ((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice).getTimeAndMoney2()) {
            returnList.add(timeAndMoneyDocsForPrintNotice.get(1));
        }
        if (timeAndMoneyDocsForPrintNotice.size() > 2 && ((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice).getTimeAndMoney3()) {
            returnList.add(timeAndMoneyDocsForPrintNotice.get(2));
        }
        if (timeAndMoneyDocsForPrintNotice.size() > 3 && ((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice).getTimeAndMoney4()) {
            returnList.add(timeAndMoneyDocsForPrintNotice.get(3));
        }
        if (timeAndMoneyDocsForPrintNotice.size() > 4 && ((edu.ku.kuali.kra.award.printing.AwardPrintNotice) awardPrintNotice).getTimeAndMoney5()) {
            returnList.add(timeAndMoneyDocsForPrintNotice.get(4));
        }

        return returnList;
    }

    /**
     * This methods gets the BUAwardPrintingService attribute value.
     *
     * @returns BUAwardPrintingService service
     */
    public BUAwardPrintingService getBUAwardPrintingService() {
        return KcServiceLocator.getService(BUAwardPrintingService.class);
    }

    /**
     * This methods gets the AwardBudgetService attribute value.
     *
     * @returns AwardBudgetService service
     */
    @Override
    public AwardBudgetService getAwardBudgetService() {
        return KcServiceLocator.getService(AwardBudgetService.class);
    }

    /*
     * private AwardCostShare getMostRecentAwardCostShare(Award award) {
     * List<AwardCostShare> costShares = award.getAwardCostShares();
     * if (costShares.isEmpty()) {
     * return null;
     * }
     * return getMostRecentSequenceAssociate(costShares);
     * }
     * 
     * private <T extends SequenceAssociate<?>> T getMostRecentSequenceAssociate(List<T> sequenceAssociates) {
     * Collections.sort(sequenceAssociates, new Comparator<SequenceAssociate<?>>() {
     * public int compare(SequenceAssociate<?> sequence1, SequenceAssociate<?> sequence2) {
     * return sequence1.getSequenceNumber().compareTo(sequence2.getSequenceNumber());
     * }
     * });
     * return sequenceAssociates.get(sequenceAssociates.size() - 1);
     * }
     */

    @Override
    public ActionForward route(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        AwardForm awardForm = (AwardForm) form;
        AwardDocument awardDocument = awardForm.getAwardDocument();
        save(mapping, form, request, response);
        // BUKC-0090: Adding new custom data element needed in KC: FAIN (ENHC0012305)
        boolean valid = new AwardDocumentRule().processFainRequiredBusinessRule(awardDocument);

        /*
         * BUKC-0136-a: KC Subaward: (Revert to BUKC-0111) Funding Source Award data should be from the latest FINAL version (Jira KRAFDBCK-10754, BU
         * Subaward QA issue 5, DFCT0011410, and DFCT0011447)
         * Link the new version with the subawards from the previous active award version.
         */
        if (valid) {
            updateLinkedSubaward(awardDocument.getAward());
        }

        return super.route(mapping, form, request, response);
    }

    /**
     * Update the linked subaward's funding source to the new version of the award
     *
     * @param award
     * @throws WorkflowException
     */
    private void updateLinkedSubaward(Award award) {
        Set<Long> modifiedSubawards = new HashSet<Long>();

        List<SubAward> linkedSubawards = getSubAwardService().getLinkedSubAwards(award);
        for (SubAward subAward : award.getSubAwardList()) {
            modifiedSubawards.add(subAward.getSubAwardId());
        }

        if (linkedSubawards.size() > 0) {
            getSubAwardService().updateSubAwardFundingSource(modifiedSubawards, award);
        }
    }

    @Override
    protected VersionHistoryService getVersionHistoryService() {
        return KcServiceLocator.getService(VersionHistoryService.class);
    }

    @Override
    public SubAwardService getSubAwardService() {
        return KcServiceLocator.getService(SubAwardService.class);
    }
}
