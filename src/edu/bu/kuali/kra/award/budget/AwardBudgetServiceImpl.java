package edu.bu.kuali.kra.award.budget;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.kuali.coeus.common.budget.framework.calculator.BudgetCalculationService;
import org.kuali.coeus.common.budget.framework.core.Budget;
import org.kuali.coeus.common.budget.framework.core.BudgetParent;
import org.kuali.coeus.common.budget.framework.core.BudgetParentDocument;
import org.kuali.coeus.common.budget.framework.core.BudgetService;
import org.kuali.coeus.common.budget.framework.nonpersonnel.BudgetLineItem;
import org.kuali.coeus.common.budget.framework.nonpersonnel.BudgetLineItemBase;
import org.kuali.coeus.common.budget.framework.period.BudgetPeriod;
import org.kuali.coeus.common.budget.framework.personnel.BudgetPerson;
import org.kuali.coeus.common.budget.framework.personnel.BudgetPersonnelDetails;
import org.kuali.coeus.common.budget.framework.query.QueryList;
import org.kuali.coeus.common.budget.framework.query.operator.And;
import org.kuali.coeus.common.budget.framework.query.operator.Equals;
import org.kuali.coeus.common.budget.framework.rate.BudgetRate;
import org.kuali.coeus.common.budget.framework.rate.RateType;
import org.kuali.coeus.common.budget.framework.summary.BudgetSummaryService;
import org.kuali.coeus.common.budget.framework.version.AddBudgetVersionEvent;
import org.kuali.coeus.common.budget.framework.version.BudgetVersionOverview;
import org.kuali.coeus.common.framework.version.history.VersionHistory;
import org.kuali.coeus.common.framework.version.history.VersionHistoryService;
import org.kuali.coeus.sys.api.model.ScaleTwoDecimal;
import org.kuali.kra.award.budget.AwardBudgetExt;
import org.kuali.kra.award.budget.AwardBudgetPeriodExt;
import org.kuali.kra.award.budget.AwardBudgetPersonnelDetailsExt;
import org.kuali.kra.award.budget.AwardBudgetStatus;
import org.kuali.kra.award.budget.AwardBudgetType;
import org.kuali.kra.award.budget.AwardBudgetVersionOverviewExt;
import org.kuali.kra.award.budget.AwardBudgetVersionRule;
import org.kuali.kra.award.budget.BudgetLimitSummaryHelper;
import org.kuali.kra.award.budget.calculator.AwardBudgetCalculationService;
import org.kuali.kra.award.budget.document.AwardBudgetDocument;
import org.kuali.kra.award.commitments.AwardFandaRate;
import org.kuali.kra.award.document.AwardDocument;
import org.kuali.kra.award.home.Award;
import org.kuali.kra.award.home.AwardService;
import org.kuali.kra.award.home.fundingproposal.AwardFundingProposal;
import org.kuali.kra.budget.versions.AwardBudgetExt;
import org.kuali.kra.infrastructure.Constants;
import org.kuali.kra.infrastructure.KeyConstants;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentService;
import org.springframework.beans.BeanUtils;

public class AwardBudgetServiceImpl extends
		org.kuali.kra.award.budget.AwardBudgetServiceImpl implements
		AwardBudgetService {

	private final static String BUDGET_VERSION_ERROR_PREFIX = "document.parentDocument.AwardBudgetExt";
	private ParameterService parameterService;
	private BusinessObjectService businessObjectService;
	private DocumentService documentService;
	private BudgetService<Award> budgetService;
	private BudgetSummaryService budgetSummaryService;
	private BudgetCalculationService budgetCalculationService;
	private AwardBudgetCalculationService awardBudgetCalculationService;
	private VersionHistoryService versionHistoryService;
	private AwardService awardService;

	/**
	 * This method...
	 * 
	 * @param awardBudgetDocument
	 */
	protected void saveDocument(AwardBudgetDocument awardBudgetDocument) {
		try {
			getDocumentService().saveDocument(awardBudgetDocument);
		} catch (WorkflowException e) {
			e.printStackTrace();
		}
	}

	protected String getParameterValue(String awardBudgetParameter) {
		return getParameterService().getParameterValueAsString(
				AwardBudgetDocument.class, awardBudgetParameter);
	}

	protected String findStatusDescription(String statusCode) {
		AwardBudgetStatus budgetStatus = getBusinessObjectService()
				.findBySinglePrimaryKey(AwardBudgetStatus.class, statusCode);
		return budgetStatus.getDescription();
	}

	/**
	 * @see edu.bu.kuali.kra.award.budget.AwardBudgetService#rebudget(org.kuali.kra.award.document.AwardDocument,
	 *      String)
	 */
	public AwardBudgetDocument rebudget(AwardDocument awardDocument,
			String documentDescription) throws WorkflowException {
		AwardBudgetDocument rebudgetDocument = createNewBudgetDocument(
				documentDescription, awardDocument, true);
		return rebudgetDocument;
	}

	/**
	 * Gets the parameterService attribute.
	 * 
	 * @return Returns the parameterService.
	 */
	public ParameterService getParameterService() {
		return parameterService;
	}

	/**
	 * Sets the parameterService attribute value.
	 * 
	 * @param parameterService
	 *            The parameterService to set.
	 */
	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

	/**
	 * Gets the businessObjectService attribute.
	 * 
	 * @return Returns the businessObjectService.
	 */
	public BusinessObjectService getBusinessObjectService() {
		return businessObjectService;
	}

	/**
	 * Sets the businessObjectService attribute value.
	 * 
	 * @param businessObjectService
	 *            The businessObjectService to set.
	 */
	public void setBusinessObjectService(
			BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}

	/**
	 * Gets the documentService attribute.
	 * 
	 * @return Returns the documentService.
	 */
	public DocumentService getDocumentService() {
		return documentService;
	}

	/**
	 * Sets the documentService attribute value.
	 * 
	 * @param documentservice
	 *            The documentService to set.
	 */
	public void setDocumentService(DocumentService documentservice) {
		this.documentService = documentservice;
	}

	/**
	 *
	 * @see org.kuali.kra.budget.core.BudgetCommonService#getNewBudgetVersion(org.kuali.kra.budget.document.BudgetParentDocument,
	 *      java.lang.String)
	 */
	public AwardBudgetDocument getNewBudgetVersion(
			BudgetParentDocument<Award> parentBudgetDocument,
			String documentDescription) throws WorkflowException {

		if (checkForOutstandingBudgets(parentBudgetDocument)) {
			return null;
		}

		AwardDocument parentDocument = (AwardDocument) parentBudgetDocument;

		AwardBudgetDocument awardBudgetDocument = createNewBudgetDocument(
				documentDescription, parentDocument.getAward(), false);

		return awardBudgetDocument;
	}

	/**
	 * This method creates New Budget Document - BU Customization to set F&A
	 * Rate type from previous postet budget if exists
	 *
	 * @param documentDescription
	 *            new Budget Description
	 * @param parentDocument
	 *            Award Document
	 * @return new award budget document
	 * @throws WorkflowException
	 */
	protected AwardBudgetDocument createNewBudgetDocument(
			String documentDescription, AwardDocument parentDocument,
			boolean rebudget) throws WorkflowException {
		boolean success = new AwardBudgetVersionRule()
				.processAddBudgetVersion(new AddBudgetVersionEvent(
						BUDGET_VERSION_ERROR_PREFIX, parentDocument
								.getBudgetParent(), documentDescription));
		if (!success) {
			return null;
		}
		Integer budgetVersionNumber = parentDocument
				.getNextBudgetVersionNumber();
		AwardBudgetDocument awardBudgetDocument;
		// BUKC-0017: Default F&A Rate if there is previous posted budget
		AwardBudgetExt previousPostedBudget = new AwardBudgetExt();

		if (isPostedBudgetExist(parentDocument.getAward())) {
			ScaleTwoDecimal obligatedChangeAmount = getTotalCostLimit(parentDocument);
			previousPostedBudget = getLatestPostedBudget(parentDocument);
			AwardBudgetDocument postedBudgetDocument = previousPostedBudget
					.getBudgetDocument();
			awardBudgetDocument = copyBudgetVersion(postedBudgetDocument);
			copyObligatedAmountToLineItems(awardBudgetDocument,
					obligatedChangeAmount);
		} else {
			awardBudgetDocument = (AwardBudgetDocument) documentService
					.getNewDocument(AwardBudgetDocument.class);
		}
		awardBudgetDocument.setParentDocument(parentDocument);
		awardBudgetDocument.setParentDocumentKey(parentDocument
				.getDocumentNumber());
		awardBudgetDocument.setParentDocumentTypeCode(parentDocument
				.getDocumentTypeCode());
		awardBudgetDocument.getDocumentHeader().setDocumentDescription(
				documentDescription);

		AwardBudgetExt awardBudget = awardBudgetDocument.getAwardBudget();
		awardBudget.setBudgetVersionNumber(budgetVersionNumber);
		awardBudget.setBudgetDocument(awardBudgetDocument);
		org.kuali.kra.award.budget.AwardBudgetExt lastBudgetVersion = getLastBudgetVersion(parentDocument
				.getAward());
		awardBudget
				.setOnOffCampusFlag(lastBudgetVersion == null ? Constants.DEFALUT_CAMUS_FLAG
						: lastBudgetVersion.getOnOffCampusFlag());
		if (awardBudgetDocument.getDocumentHeader() != null
				&& awardBudgetDocument.getDocumentHeader()
						.hasWorkflowDocument()) {
			awardBudget.setBudgetInitiator(awardBudgetDocument
					.getDocumentHeader().getWorkflowDocument()
					.getInitiatorPrincipalId());
		}

		BudgetParent budgetParent = parentDocument.getBudgetParent();
		awardBudget.setStartDate(budgetParent.getRequestedStartDateInitial());
		awardBudget.setEndDate(budgetParent.getRequestedEndDateInitial());
		if (awardBudget.getOhRatesNonEditable()) {
			awardBudget
					.setOhRateClassCode(getAwardParameterValue(Constants.AWARD_BUDGET_DEFAULT_FNA_RATE_CLASS_CODE));
			awardBudget
					.setUrRateClassCode(getAwardParameterValue(Constants.AWARD_BUDGET_DEFAULT_UNDERRECOVERY_RATE_CLASS_CODE));
		} else {
			awardBudget
					.setOhRateClassCode(getBudgetParameterValue(Constants.BUDGET_DEFAULT_OVERHEAD_RATE_CODE));
			awardBudget
					.setUrRateClassCode(getBudgetParameterValue(Constants.BUDGET_DEFAULT_UNDERRECOVERY_RATE_CODE));
		}

		// BUKC-0017: Default F&A Rate if there is previous posted budget
		if (isPostedBudgetExist(parentDocument.getAward())) {
			awardBudget.setUrRateClassCode(previousPostedBudget
					.getUrRateClass().getRateClassTypeCode());
			awardBudget.setOhRateClassCode(previousPostedBudget.getRateClass()
					.getRateClassTypeCode());
			awardBudget.setRateClass(previousPostedBudget.getRateClass());
		}

		awardBudget
				.setOhRateTypeCode(getBudgetParameterValue(Constants.BUDGET_DEFAULT_OVERHEAD_RATE_TYPE_CODE));
		awardBudget.setModularBudgetFlag(parameterService
				.getParameterValueAsBoolean(AwardDocument.class,
						Constants.BUDGET_DEFAULT_MODULAR_FLAG));
		awardBudget
				.setAwardBudgetStatus(getAwardParameterValue(KeyConstants.AWARD_BUDGET_STATUS_IN_PROGRESS));
		// do not want the Budget adjustment doc number to be copied over to the
		// new budget.
		// this should be null so the budget can be posted again to the
		// financial system.
		awardBudget.setBudgetAdjustmentDocumentNumber("");
		awardBudget.setRateClassTypesReloaded(true);
		setBudgetLimits(awardBudgetDocument, parentDocument);
		if (isPostedBudgetExist(parentDocument)) {
			if (awardBudget.getTotalCostLimit().equals(ScaleTwoDecimal.ZERO)) {
				rebudget = true;
			}
			// else{
			// Budget budget = awardBudgetDocument.getBudget();
			// budget.getBudgetPeriods().clear();
			// }
		}
		recalculateBudget(awardBudgetDocument.getBudget());
		saveBudgetDocument(awardBudgetDocument, rebudget);
		awardBudgetDocument = (AwardBudgetDocument) documentService
				.getByDocumentHeaderId(awardBudgetDocument.getDocumentNumber());
		parentDocument.refreshAwardBudgetExts();

		return awardBudgetDocument;
	}

	/**
	 * This method...
	 * 
	 * @return
	 */
	private String getBudgetParameterValue(String parameter) {
		return parameterService.getParameterValueAsString(
				AwardBudgetDocument.class, parameter);
	}

	/**
	 * This method...
	 * 
	 * @return
	 */
	private String getAwardParameterValue(String parameter) {
		return parameterService.getParameterValueAsString(
				AwardBudgetDocument.class, parameter);
	}

	protected AwardBudgetExt getLatestPostedBudget(AwardDocument awardDocument) {
		List<AwardBudgetExt> documentVersions = awardDocument
				.getAwardBudgetExts();
		QueryList<AwardBudgetVersionOverviewExt> awardBudgetVersionOverviews = new QueryList<AwardBudgetVersionOverviewExt>();
		for (AwardBudgetExt AwardBudgetExt : documentVersions) {
			awardBudgetVersionOverviews
					.add((AwardBudgetVersionOverviewExt) AwardBudgetExt
							.getBudgetVersionOverview());
		}

		Equals eqPostedStatus = new Equals("awardBudgetStatusCode",
				getAwardPostedStatusCode());
		QueryList<AwardBudgetVersionOverviewExt> postedVersions = awardBudgetVersionOverviews
				.filter(eqPostedStatus);
		AwardBudgetExt postedBudget = null;
		if (!postedVersions.isEmpty()) {
			postedVersions.sort("budgetVersionNumber", false);
			AwardBudgetVersionOverviewExt postedVersion = postedVersions.get(0);
			try {
				AwardBudgetDocument awardBudgetDocument = (AwardBudgetDocument) documentService
						.getByDocumentHeaderId(postedVersion
								.getDocumentNumber());
				postedBudget = awardBudgetDocument.getAwardBudget();
			} catch (WorkflowException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return postedBudget;
	}

	/*
	 * A utility method to check whther a budget has been posted for this award,
	 * then it can be used as one of the condition to set rebudget type.
	 */
	protected boolean isPostedBudgetExist(Award parentDocument) {
		boolean exist = false;
		List<AwardBudgetExt> documentVersions = parentDocument.getBudgets();
		String postedStatusCode = getAwardPostedStatusCode();
		for (AwardBudgetExt AwardBudgetExt : documentVersions) {
			if (AwardBudgetExt.getAwardBudgetStatusCode().equals(
					postedStatusCode)) {
				exist = true;
				break;
			}
		}
		return exist;
	}

	protected String getAwardPostedStatusCode() {
		return this.parameterService.getParameterValueAsString(
				AwardBudgetDocument.class,
				KeyConstants.AWARD_BUDGET_STATUS_POSTED);
	}

	/**
	 * This method saves a budget document
	 *
	 * @param budgetDocument
	 *            budget to be saved
	 * @param rebudget
	 *            wheather this is a rebudget or not
	 * @throws WorkflowException
	 */
	protected void saveBudgetDocument1(AwardBudgetDocument budgetDocument,
			boolean rebudget) throws WorkflowException {
		AwardBudgetDocument awardBudgetDocument = budgetDocument;
		AwardBudgetExt budgetExt = awardBudgetDocument.getAwardBudget();
		// AwardBudgetExt budgetExt = (AwardBudgetExt) budget;

		String awardBudgetTypeID = getParameterValue(rebudget ? KeyConstants.AWARD_BUDGET_TYPE_REBUDGET
				: KeyConstants.AWARD_BUDGET_TYPE_NEW);
		AwardBudgetType awardBudgetType = getBusinessObjectService()
				.findBySinglePrimaryKey(AwardBudgetType.class,
						awardBudgetTypeID);
		budgetExt.setAwardBudgetTypeCode(awardBudgetType
				.getAwardBudgetTypeCode());
		budgetExt.setDescription(awardBudgetType.getDescription());
		budgetExt.setAwardBudgetType(awardBudgetType);

		processStatusChange(awardBudgetDocument,
				KeyConstants.AWARD_BUDGET_STATUS_IN_PROGRESS);
		saveDocument(awardBudgetDocument);
	}

	@SuppressWarnings("unchecked")
	protected void copyProposalBudgetLineItemsToAwardBudget(
			BudgetPeriod awardBudgetPeriod, BudgetPeriod proposalBudgetPeriod) {
		List awardBudgetLineItems = awardBudgetPeriod.getBudgetLineItems();
		List<BudgetLineItem> lineItems = proposalBudgetPeriod
				.getBudgetLineItems();
		for (BudgetLineItem budgetLineItem : lineItems) {
			String[] ignoreProperties = { "budgetId", "budgetLineItemId",
					"budgetPeriodId", "submitCostSharingFlag",
					"budgetLineItemCalculatedAmounts",
					"budgetPersonnelDetailsList", "budgetRateAndBaseList" };
			AwardBudgetLineItemExt awardBudgetLineItem = new AwardBudgetLineItemExt();
			BeanUtils.copyProperties(budgetLineItem, awardBudgetLineItem,
					ignoreProperties);
			awardBudgetLineItem.setLineItemNumber(awardBudgetPeriod.getBudget()
					.getHackedDocumentNextValue(
							Constants.BUDGET_LINEITEM_NUMBER));
			awardBudgetLineItem.setBudgetId(awardBudgetPeriod.getBudgetId());
			awardBudgetLineItem.setStartDate(awardBudgetPeriod.getStartDate());
			awardBudgetLineItem.setEndDate(awardBudgetPeriod.getEndDate());
			List<BudgetPersonnelDetails> awardBudgetPersonnelLineItems = awardBudgetLineItem
					.getBudgetPersonnelDetailsList();
			List<BudgetPersonnelDetails> budgetPersonnelLineItems = budgetLineItem
					.getBudgetPersonnelDetailsList();
			for (BudgetPersonnelDetails budgetPersonnelDetails : budgetPersonnelLineItems) {
				budgetPersonnelDetails.setBudgetLineItemId(budgetLineItem
						.getBudgetLineItemId());
				AwardBudgetPersonnelDetailsExt awardBudgetPerDetails = new AwardBudgetPersonnelDetailsExt();
				BeanUtils.copyProperties(budgetPersonnelDetails,
						awardBudgetPerDetails, new String[] {
								"budgetPersonnelLineItemId",
								"budgetLineItemId", "budgetId",
								"submitCostSharingFlag",
								"budgetPersonnelCalculatedAmounts",
								"budgetPersonnelRateAndBaseList",
								"validToApplyInRate" });
				awardBudgetPerDetails.setPersonNumber(awardBudgetPeriod
						.getBudget().getHackedDocumentNextValue(
								Constants.BUDGET_PERSON_LINE_NUMBER));
				BudgetPerson oldBudgetPerson = budgetPersonnelDetails
						.getBudgetPerson();
				BudgetPerson currentBudgetPerson = findMatchingPersonInBudget(
						awardBudgetPeriod.getBudget(), oldBudgetPerson,
						budgetPersonnelDetails.getJobCode());
				if (currentBudgetPerson == null) {
					currentBudgetPerson = new BudgetPerson();
					BeanUtils.copyProperties(oldBudgetPerson,
							currentBudgetPerson, new String[] { "budgetId",
									"personSequenceNumber" });
					currentBudgetPerson.setBudgetId(awardBudgetPeriod
							.getBudgetId());
					currentBudgetPerson
							.setPersonSequenceNumber(awardBudgetPeriod
									.getBudget()
									.getBudgetDocument()
									.getHackedDocumentNextValue(
											Constants.PERSON_SEQUENCE_NUMBER));
					awardBudgetPeriod.getBudget().getBudgetPersons()
							.add(currentBudgetPerson);
				}
				awardBudgetPerDetails.setBudgetPerson(currentBudgetPerson);
				awardBudgetPerDetails
						.setPersonSequenceNumber(currentBudgetPerson
								.getPersonSequenceNumber());
				awardBudgetPerDetails.setBudgetId(awardBudgetPeriod
						.getBudgetId());
				awardBudgetPerDetails.setCostElement(awardBudgetLineItem
						.getCostElement());
				awardBudgetPerDetails.setStartDate(awardBudgetLineItem
						.getStartDate());
				awardBudgetPerDetails.setEndDate(awardBudgetLineItem
						.getEndDate());
				awardBudgetPersonnelLineItems.add(awardBudgetPerDetails);
			}
			awardBudgetLineItems.add(awardBudgetLineItem);
			getAwardBudgetCalculationService().populateCalculatedAmount(
					awardBudgetPeriod.getBudget(), awardBudgetLineItem);
		}
	}

	public AwardBudgetDocument copyBudgetVersion(
			AwardBudgetDocument budgetDocument, boolean onlyOnePeriod)
			throws WorkflowException {
		// clear awardbudgetlimits before copy as they should always be copied
		// from
		// award document
		((AwardBudgetExt) budgetDocument.getBudget()).getAwardBudgetLimits()
				.clear();
		AwardBudgetDocument newBudgetDocument = getBudgetService()
				.copyBudgetVersion(budgetDocument, onlyOnePeriod);
		setBudgetLimits((AwardBudgetDocument) newBudgetDocument,
				(AwardDocument) newBudgetDocument.getParentDocument());
		return newBudgetDocument;
	}

	/**
	 * Sets the budgetService attribute value.
	 * 
	 * @param budgetService
	 *            The budgetService to set.
	 */
	public void setBudgetService(BudgetService<Award> budgetService) {
		this.budgetService = budgetService;
	}

	/**
	 * Gets the budgetService attribute.
	 * 
	 * @return Returns the budgetService.
	 */
	public BudgetService<Award> getBudgetService() {
		return budgetService;
	}

	/**
	 * Gets the budgetSummaryService attribute.
	 * 
	 * @return Returns the budgetSummaryService.
	 */
	public BudgetSummaryService getBudgetSummaryService() {
		return budgetSummaryService;
	}

	/**
	 * Sets the budgetSummaryService attribute value.
	 * 
	 * @param budgetSummaryService
	 *            The budgetSummaryService to set.
	 */
	public void setBudgetSummaryService(
			BudgetSummaryService budgetSummaryService) {
		this.budgetSummaryService = budgetSummaryService;
	}

	public void copyLineItemsFromProposalPeriods(Collection rawValues,
			BudgetPeriod awardBudgetPeriod) throws WorkflowException {
		awardBudgetPeriod.getBudgetLineItems().clear();
		Iterator iter = rawValues.iterator();
		while (iter.hasNext()) {
			BudgetPeriod proposalPeriod = (BudgetPeriod) iter.next();
			copyProposalBudgetLineItemsToAwardBudget(awardBudgetPeriod,
					proposalPeriod);
		}
		getDocumentService().saveDocument(
				awardBudgetPeriod.getBudget().getBudgetDocument());
		getBudgetSummaryService()
				.calculateBudget(awardBudgetPeriod.getBudget());
	}

	/**
	 * Use the business object service to match the criteria passed in
	 * 
	 * @param clazz
	 * @param key
	 * @param value
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected List findObjectsWithSingleKey(Class clazz, String key,
			Object value) {
		Map<String, Object> fieldValues = new HashMap<String, Object>();
		fieldValues.put(key, value);
		return (List) getBusinessObjectService().findMatching(clazz,
				fieldValues);
	}

	/**
	 * @see edu.bu.kuali.kra.award.budget.AwardBudgetService#findBudgetPeriodsFromLinkedProposal(java.lang.String)
	 */
	@SuppressWarnings("unchecked")
	public List<BudgetPeriod> findBudgetPeriodsFromLinkedProposal(
			String awardNumber) {
		BusinessObjectService businessObjectService = getBusinessObjectService();
		List<BudgetPeriod> budgetPeriods = new ArrayList<BudgetPeriod>();
		List<Award> awardVersions = findObjectsWithSingleKey(Award.class,
				"awardNumber", awardNumber);
		for (Award award : awardVersions) {
			List<AwardFundingProposal> fundingProposals = findObjectsWithSingleKey(
					AwardFundingProposal.class, "awardId", award.getAwardId());
			for (AwardFundingProposal fundingProposal : fundingProposals) {
				if (fundingProposal.isActive()) {
					List<InstitutionalProposal> instProposals = findObjectsWithSingleKey(
							InstitutionalProposal.class, "proposalNumber",
							fundingProposal.getProposal().getProposalNumber());
					for (InstitutionalProposal instProp : instProposals) {
						List<ProposalAdminDetails> proposalAdminDetails = findObjectsWithSingleKey(
								ProposalAdminDetails.class, "instProposalId",
								instProp.getProposalId());
						for (ProposalAdminDetails proposalAdminDetail : proposalAdminDetails) {
							String developmentProposalNumber = proposalAdminDetail
									.getDevProposalNumber();
							DevelopmentProposal proposalDevelopmentDocument = businessObjectService
									.findBySinglePrimaryKey(
											DevelopmentProposal.class,
											developmentProposalNumber);
							List<AwardBudgetExt> AwardBudgetExts = findObjectsWithSingleKey(
									AwardBudgetExt.class, "parentDocumentKey",
									proposalDevelopmentDocument
											.getProposalDocument()
											.getDocumentNumber());
							for (AwardBudgetExt AwardBudgetExt : AwardBudgetExts) {
								Budget budget = getBusinessObjectService()
										.findBySinglePrimaryKey(
												ProposalDevelopmentBudgetExt.class,
												AwardBudgetExt
														.getBudgetVersionOverview()
														.getBudgetId());
								if (budget.isFinalVersionFlag()) {
									// if this result set is being used by @see
									// org.kuali.kra.lookup.BudgetPeriodLookupableHelperServiceImpl
									// we need to populate these additional
									// fields so always populate them.
									for (BudgetPeriod budgetPeriod : budget
											.getBudgetPeriods()) {
										budgetPeriod
												.setInstitutionalProposalNumber(instProp
														.getProposalNumber());
										budgetPeriod
												.setInstitutionalProposalVersion(instProp
														.getSequenceNumber());
										budgetPeriods.add(budgetPeriod);
									}
								}
							}
						}
					}
				}
			}
		}
		return budgetPeriods;
	}

	public boolean checkForOutstandingBudgets(BudgetParentDocument parentDoc) {
		boolean result = false;

		for (AwardBudgetExt budgetVersion : parentDoc.getAwardBudgetExts()) {
			BudgetVersionOverview version = budgetVersion
					.getBudgetVersionOverview();
			AwardBudgetExt awardBudget = getBusinessObjectService()
					.findBySinglePrimaryKey(AwardBudgetExt.class,
							version.getBudgetId());
			if (!(StringUtils.equals(awardBudget.getAwardBudgetStatusCode(),
					getPostedBudgetStatus())
					|| StringUtils.equals(
							awardBudget.getAwardBudgetStatusCode(),
							getRejectedBudgetStatus()) || StringUtils.equals(
					awardBudget.getAwardBudgetStatusCode(),
					getCancelledBudgetStatus()))) {
				result = true;
				GlobalVariables.getMessageMap().putError(
						BUDGET_VERSION_ERROR_PREFIX,
						KeyConstants.ERROR_AWARD_UNFINALIZED_BUDGET_EXISTS,
						awardBudget.getDocumentDescription());
			}
		}

		return result;
	}

	/**
	 *
	 * @see edu.bu.kuali.kra.award.budget.AwardBudgetService#populateBudgetLimitSummary(org.kuali.kra.award.budget.BudgetLimitSummaryHelper,
	 *      org.kuali.kra.award.document.AwardDocument)
	 */
	public void populateBudgetLimitSummary(BudgetLimitSummaryHelper summary,
			AwardDocument awardDocument) {

		AwardBudgetExt currentBudget = getCurrentBudget(awardDocument
				.getAward());
		if (summary.getCurrentBudget() == null
				|| !ObjectUtils.equals(summary.getCurrentBudget(),
						currentBudget)) {
			getAwardBudgetCalculationService().calculateBudgetSummaryTotals(
					currentBudget, false);
			summary.setCurrentBudget(currentBudget);
		}
		AwardBudgetExt prevBudget = getPreviousBudget(awardDocument);
		if (summary.getPreviousBudget() == null
				|| !ObjectUtils.equals(summary.getPreviousBudget(), prevBudget)) {
			getAwardBudgetCalculationService().calculateBudgetSummaryTotals(
					prevBudget, true);
			summary.setPreviousBudget(prevBudget);
		}
	}

	protected AwardBudgetExt getNewestBudgetByStatus(
			AwardDocument awardDocument, List<String> statuses) {
		AwardBudgetVersionOverviewExt budgetVersion = null;
		List<AwardBudgetExt> awardBudgetDocuments = awardDocument
				.getAwardBudgetExts();
		for (AwardBudgetExt version : awardBudgetDocuments) {
			AwardBudgetVersionOverviewExt curVersion = (AwardBudgetVersionOverviewExt) version
					.getBudgetVersionOverview();
			if (statuses.contains(curVersion.getAwardBudgetStatusCode())) {
				if (budgetVersion == null
						|| curVersion.getBudgetVersionNumber() > budgetVersion
								.getBudgetVersionNumber()) {
					budgetVersion = curVersion;
				}
			}
		}
		AwardBudgetExt result = null;
		if (budgetVersion != null) {
			result = getBusinessObjectService().findBySinglePrimaryKey(
					AwardBudgetExt.class, budgetVersion.getBudgetId());
		}
		if (result == null) {
			result = new AwardBudgetExt();
		}
		return result;
	}

	public List<AwardBudgetExt> getAllBudgetsForAward(
			AwardDocument awardDocument) {
		HashSet<AwardBudgetExt> result = new HashSet<AwardBudgetExt>();
		List<VersionHistory> versions = getVersionHistoryService()
				.loadVersionHistory(Award.class,
						awardDocument.getAward().getAwardNumber());
		for (VersionHistory version : versions) {
			if (version.getSequenceOwnerSequenceNumber() <= awardDocument
					.getAward().getSequenceNumber()
					&& !(version.getSequenceOwner() == null)
					&& !(((Award) version.getSequenceOwner())
							.getAwardDocument() == null)) {
				result.addAll(((Award) version.getSequenceOwner())
						.getAwardDocument().getActualAwardBudgetExts());
			}
		}
		List<AwardBudgetExt> listResult = new ArrayList<AwardBudgetExt>(result);
		Collections.sort(listResult);
		return listResult;
	}

	public Award getActiveOrNewestAward(String awardNumber) {
		return awardService.getActiveOrNewestAward(awardNumber);
	}

	protected VersionHistoryService getVersionHistoryService() {
		return versionHistoryService;
	}

	public void setVersionHistoryService(
			VersionHistoryService versionHistoryService) {
		this.versionHistoryService = versionHistoryService;
	}

	protected AwardBudgetCalculationService getAwardBudgetCalculationService() {
		return awardBudgetCalculationService;
	}

	public void setAwardBudgetCalculationService(
			AwardBudgetCalculationService awardBudgetCalculationService) {
		this.awardBudgetCalculationService = awardBudgetCalculationService;
	}

	protected BudgetCalculationService getBudgetCalculationService() {
		return budgetCalculationService;
	}

	public void setBudgetCalculationService(
			BudgetCalculationService budgetCalculationService) {
		this.budgetCalculationService = budgetCalculationService;
	}

	/**
	 * This method...
	 * 
	 * @param budgetPeriod
	 * @param budget
	 * @return
	 */
	private ScaleTwoDecimal getPeriodFringeTotal(BudgetPeriod budgetPeriod,
			Budget budget) {
		if (budget.getBudgetSummaryTotals() == null
				|| budget.getBudgetSummaryTotals().get("personnelFringeTotals") == null)
			return ScaleTwoDecimal.ZERO;
		ScaleTwoDecimal periodFringeTotal = budget.getBudgetSummaryTotals()
				.get("personnelFringeTotals")
				.get(budgetPeriod.getBudgetPeriod() - 1);
		return periodFringeTotal;
	}

	public void recalculateBudget(Budget budget) {
		List<BudgetPeriod> awardBudgetPeriods = budget.getBudgetPeriods();
		for (BudgetPeriod budgetPeriod : awardBudgetPeriods) {
			removeBudgetSummaryPeriodCalcAmounts(budgetPeriod);
		}
		budgetCalculationService.calculateBudget(budget);
		budgetCalculationService.calculateBudgetSummaryTotals(budget);
	}

	public void recalculateBudgetPeriod(Budget budget, BudgetPeriod budgetPeriod) {
		removeBudgetSummaryPeriodCalcAmounts(budgetPeriod);
		budgetCalculationService.calculateBudgetPeriod(budget, budgetPeriod);
	}

	public void calculateBudgetOnSave(Budget budget) {
		budgetCalculationService.calculateBudget(budget);
		budgetCalculationService.calculateBudgetSummaryTotals(budget);
		List<BudgetPeriod> awardBudgetPeriods = budget.getBudgetPeriods();
		for (BudgetPeriod awardBudgetPeriod : awardBudgetPeriods) {
			AwardBudgetPeriodExt budgetPeriod = (AwardBudgetPeriodExt) awardBudgetPeriod;
			ScaleTwoDecimal periodFringeTotal = getPeriodFringeTotal(
					budgetPeriod, budget);
			if (!periodFringeTotal.equals(ScaleTwoDecimal.ZERO)
					|| !budgetPeriod.getTotalFringeAmount().equals(
							ScaleTwoDecimal.ZERO)) {
				budgetPeriod.setTotalDirectCost(budgetPeriod
						.getTotalDirectCost().subtract(periodFringeTotal)
						.add(budgetPeriod.getTotalFringeAmount()));
				budgetPeriod.setTotalCost(budgetPeriod.getTotalDirectCost()
						.add(budgetPeriod.getTotalIndirectCost()));
			}
		}
		setBudgetCostsFromPeriods(budget);
	}

	public boolean checkRateChange(Collection<BudgetRate> savedBudgetRates,
			Award award) {
		award.refreshReferenceObject("awardFandaRate");
		List<AwardFandaRate> awardFandaRates = award.getAwardFandaRate();
		boolean changeFlag = false;
		for (AwardFandaRate budgetFnARate : awardFandaRates) {
			RateType fnaRateType = budgetFnARate.getFandaRateType();
			Equals eqRateClasCode = new Equals("rateClassCode",
					fnaRateType.getRateClassCode());
			Equals eqRateTypeCode = new Equals("rateTypeCode",
					fnaRateType.getRateTypeCode());
			Equals eqCampusFlag = new Equals("onOffCampusFlag",
					budgetFnARate.getOnOffCampusFlag());
			And rateClassAndRateType = new And(eqRateClasCode, eqRateTypeCode);
			And rateClassAndRateTypeAndCampusFlag = new And(
					rateClassAndRateType, eqCampusFlag);
			QueryList<BudgetRate> matchAwardFnARate = new QueryList<BudgetRate>(
					savedBudgetRates).filter(rateClassAndRateTypeAndCampusFlag);
			if (matchAwardFnARate.isEmpty()
					|| matchAwardFnARate.size() > 1
					|| !matchAwardFnARate.get(0).getApplicableRate()
							.equals(budgetFnARate.getApplicableFandaRate())) {
				changeFlag = true;
			}
		}
		Equals eqRateClasCode = new Equals(
				"rateClassCode",
				getBudgetParameterValue(Constants.AWARD_BUDGET_EB_RATE_CLASS_CODE));
		Equals eqRateTypeCode = new Equals(
				"rateTypeCode",
				getBudgetParameterValue(Constants.AWARD_BUDGET_EB_RATE_TYPE_CODE));
		And rateClassAndRateType = new And(eqRateClasCode, eqRateTypeCode);
		QueryList<BudgetRate> matchAwardEBCampusRates = new QueryList<BudgetRate>(
				savedBudgetRates).filter(rateClassAndRateType);
		for (BudgetRate budgetEBRate : matchAwardEBCampusRates) {
			if (budgetEBRate.getOnOffCampusFlag()) {
				if (award.getSpecialEbRateOnCampus() != null
						&& !award.getSpecialEbRateOnCampus().equals(
								budgetEBRate.getApplicableRate())) {
					changeFlag = true;
				}
			} else {
				if (award.getSpecialEbRateOffCampus() != null
						&& !award.getSpecialEbRateOffCampus().equals(
								budgetEBRate.getApplicableRate())) {
					changeFlag = true;
				}
			}
		}
		if ((award.getSpecialEbRateOnCampus() != null || award
				.getSpecialEbRateOffCampus() != null)
				&& matchAwardEBCampusRates.isEmpty()) {
			changeFlag = true;
		}
		return changeFlag;
	}

	/**
	 * This method gets Award Service
	 * 
	 * @return awardService
	 */
	protected AwardService getAwardService() {
		return awardService;
	}

	public void setAwardService(AwardService awardService) {
		this.awardService = awardService;
	}

}
