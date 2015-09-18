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
package edu.ku.kuali.kra.award.sapintegration;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.ws.BindingProvider;

import org.apache.commons.lang.StringUtils;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.transport.Conduit;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.apache.log4j.Logger;
import org.kuali.coeus.common.api.rolodex.RolodexContract;
import org.kuali.coeus.common.api.rolodex.RolodexService;
import org.kuali.coeus.common.budget.framework.version.BudgetVersionOverview;
import org.kuali.coeus.common.framework.org.Organization;
import org.kuali.coeus.common.framework.rolodex.Rolodex;
import org.kuali.coeus.common.framework.sponsor.Sponsor;
import org.kuali.coeus.common.framework.unit.Unit;
import org.kuali.coeus.common.framework.unit.UnitService;
import org.kuali.coeus.common.framework.unit.admin.UnitAdministrator;
import org.kuali.coeus.common.framework.version.history.VersionHistory;
import org.kuali.coeus.common.framework.version.history.VersionHistoryService;
import org.kuali.coeus.common.framework.version.sequence.associate.SequenceAssociate;
import org.kuali.coeus.sys.api.model.ScaleTwoDecimal;
import org.kuali.coeus.sys.framework.service.KcServiceLocator;
import org.kuali.coeus.sys.framework.util.DateUtils;
import org.kuali.coeus.sys.framework.validation.ErrorReporter;
import org.kuali.kra.award.AwardAmountInfoService;
import org.kuali.kra.award.awardhierarchy.AwardHierarchy;
import org.kuali.kra.award.awardhierarchy.AwardHierarchyService;
import org.kuali.kra.award.budget.AwardBudgetExt;
import org.kuali.kra.award.budget.AwardBudgetService;
import org.kuali.kra.award.commitments.AwardCostShare;
import org.kuali.kra.award.contacts.AwardPerson;
import org.kuali.kra.award.contacts.AwardPersonUnit;
import org.kuali.kra.award.contacts.AwardPersonUnitCreditSplit;
import org.kuali.kra.award.contacts.AwardSponsorContact;
import org.kuali.kra.award.contacts.AwardUnitContact;
import org.kuali.kra.award.document.AwardDocument;
import org.kuali.kra.award.home.Award;
import org.kuali.kra.award.home.AwardAmountInfo;
import org.kuali.kra.award.home.AwardSponsorTerm;
import org.kuali.kra.award.home.ValuableItem;
import org.kuali.kra.award.home.approvedsubawards.AwardApprovedSubaward;
import org.kuali.kra.award.paymentreports.awardreports.AwardReportTerm;
import org.kuali.kra.award.paymentreports.paymentschedule.AwardPaymentSchedule;
import org.kuali.kra.award.service.impl.AwardHierarchyUIServiceImpl;
import org.kuali.kra.infrastructure.Constants;
import org.kuali.kra.timeandmoney.AwardHierarchyNode;
import org.kuali.kra.timeandmoney.document.TimeAndMoneyDocument;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.springframework.beans.factory.InitializingBean;

import com.sap.document.sap.rfc.functions.BAPIRET2;
import com.sap.document.sap.rfc.functions.GMSPPROGRAMFMBT;
import com.sap.document.sap.rfc.functions.GMSPPROGRAMFMBTTT;
import com.sap.document.sap.rfc.functions.ObjectFactory;
import com.sap.document.sap.rfc.functions.ZBAPI0035HEADER;
import com.sap.document.sap.rfc.functions.ZBAPI0035HEADERADD;
import com.sap.document.sap.rfc.functions.ZBAPI0035RESPONSIBLE;
import com.sap.document.sap.rfc.functions.ZBAPI0035RESPONSIBLET;
import com.sap.document.sap.rfc.functions.ZBAPI0035SPONSOREDOBJECTS;
import com.sap.document.sap.rfc.functions.ZBAPI0035SPONSOREDOBJECTST;
import com.sap.document.sap.rfc.functions.ZFIGMSPRESPONSIBLETABKCRM;
import com.sap.document.sap.rfc.functions.ZFIGRANTDATA;
import com.sap.document.sap.rfc.functions.ZFIKCRMSPXWALK;
import com.sap.document.sap.rfc.functions.ZGMBILLINGPLANSTRUCTURE;
import com.sap.document.sap.rfc.functions.ZGMFACREDIT;
import com.sap.document.sap.rfc.functions.ZGMFACREDITT;
import com.sap.document.sap.rfc.functions.ZGMGRANTSTRUCTURE;
import com.sap.document.sap.rfc.functions.ZGMKCRMINTERFACE;
import com.sap.document.sap.rfc.functions.ZGMKCRMINTERFACE.BILLINGPLAN;
import com.sap.document.sap.rfc.functions.ZGMKCRMINTERFACE.SPONSOR;
import com.sap.document.sap.rfc.functions.ZGMKCRMINTERFACE.SPONSOREDPROGRAMS;
import com.sap.document.sap.rfc.functions.ZGMKCRMINTERFACE.SPONSOREDPROGRAMSGRP;
import com.sap.document.sap.rfc.functions.ZGMKCRMINTERFACEResponse;
import com.sap.document.sap.rfc.functions.ZGMKCRMINTERFACEResponse.GRANTMESSAGES;
import com.sap.document.sap.rfc.functions.ZGMKCRMINTERFACEResponse.RETURN;
import com.sap.document.sap.rfc.functions.ZGMKCRMINTERFACEResponse.SPONSOREDPROGRAMSMESSAGES;
import com.sap.document.sap.rfc.functions.ZGMKCRMINTERFACEResponse.SPONSORMESSAGES;
import com.sap.document.sap.rfc.functions.ZGMKCRMINTERFACEResponse.SPXWALKT;
import com.sap.document.sap.rfc.functions.ZGMSPONSORSTRUCTURE;
import com.sap.document.sap.rfc.functions.ZGMSPPROGRAMGRPSTRUCTURE;
import com.sap.document.sap.rfc.functions.ZGMSPPROGRAMSTRUCTURE;
import com.sap.document.sap.rfc.functions.ZGMSPRESPONSIBLEKCRM;
import com.sap.document.sap.rfc.functions.ZGRANTMESSAGES;
import com.sap.document.sap.rfc.functions.ZSPONSORMESSAGES;
import com.sap.document.sap.rfc.functions.ZSPPROGRAMMESSAGES;

import edu.bu.sap.kcrm.SIKCRMPROCESSOUTBOUND;
import edu.bu.sap.kcrm.SIKCRMPROCESSOUTBOUNDService;
import edu.ku.kuali.kra.award.home.AwardExtension;
import edu.ku.kuali.kra.bo.AwardTransmission;
import edu.ku.kuali.kra.bo.AwardTransmissionChild;
import edu.ku.kuali.kra.infrastructure.BUConstants;

/**
 * <p>
 * A service implementation which provides integration with Boston University's
 * SAP implementation. Provides two primary operations, one for performing
 * validation against incoming award hierarchy data, and other for transmitting
 * award hierarchy data to SAP.
 * <p/>
 * <p>
 * It is intended that invocation of the {@link #transmit(SapTransmission)}
 * method, will invoke validation prior to performing submission. The additional
 * {@link #validate(SapTransmission)} method is provided to allow for
 * identifying and reporting on possible validation issues prior to submission
 * of the SAP transmission.
 *
 * @author Eric Westfall (ewestfal@gmail.com)
 */

public class SapIntegrationServiceImpl implements SapIntegrationService,
		InitializingBean {
	private Logger LOG = Logger.getLogger(SapIntegrationServiceImpl.class);

	private static final String SAP_SERVICE_WSDL_URL_PARAM = "sapService.wsdl.url";
	private static final String SAP_SERVICE_URL_PARAM = "sapService.url";
	private static final String SAP_SERVICE_USERNAME_PARAM = "sapService.username";
	private static final String SAP_SERVICE_PASSWORD_PARAM = "sapService.password";
	private static final String SAP_SERVICE_CONNECTION_TIMEOUT_PARAM = "sapService.connectionTimeout";
	private static final String SAP_SERVICE_RECEIVE_TIMEOUT_PARAM = "sapService.receiveTimeout";

	private static final Integer FEDERAL_CODE = 1;
	private static final Integer NON_FEDERAL_CODE = 2;

	private static final Integer AWARD_TYPE_CODE_SUBAWARD = 6;

	private static final String MANUAL_BASIS_OF_PAYMENT = "2";
	private static final String RRB_BASIS_OF_PAYMENT = "1";
	private static final String MILESTONE_BASIS_OF_PAYMENT = "4";

	private static final String DHHS_LOC_MATHOD_OF_PAYMENT = "6";

	private static final String INTERFACE_NEW = "N";
	private static final String INTERFACE_UPDATE = "U";

	private static final String ARRA_FUNDING_INFORMATION = "ARRA";

	private static final String PAYMENT_INVOICES_REPORT_CLASS_CODE = "6";
	private static final String DEFAULT_SPONSOR_TYPE_FOR_BILLING_PARTNER = "13";

	private static final String DATE_FORMAT_PATTERN = "yyyy-MM-dd";

	private static final String ERROR_MESSAGE_TYPE = "E";
	private static final String WARNING_MESSAGE_TYPE = "W";

	// BU Constants
	public static final int AWARD_TRANSACTION_TYPE_NO_COST_EXTENSION = 6;
	public static final int AWARD_TRANSACTION_TYPE_ADMINISTRATION_CHANGE = 10;
	private AwardAmountInfoService awardAmountInfoService;
	private UnitService unitService;
	private AwardHierarchyService awardHierarchyService;
	private BusinessObjectService businessObjectService;
	private BudgetRateAndBaseService budgetRateAndBaseService;
	private RolodexService rolodexService;
	private DocumentService documentService;
	private ParameterService parameterService;
	private ObjectFactory objectFactory;
	private AwardBudgetService AwardBudgetService;

	private ErrorReporter errorReporter;

	public SapIntegrationServiceImpl() {
		this.objectFactory = new ObjectFactory();
	}

	public void setParameterService(ParameterService parameterService) {
		this.parameterService = parameterService;
	}

	protected ParameterService getParameterService() {
		return parameterService;
	}

	public void afterPropertiesSet() throws Exception {
		if (awardAmountInfoService == null) {
			throw new IllegalStateException(
					"The awardAmountInfoService was not injected.");
		}
		if (unitService == null) {
			throw new IllegalStateException("The unitService was not injected.");
		}
		if (awardHierarchyService == null) {
			throw new IllegalStateException(
					"The awardHierarchyService was not injected.");
		}
		if (businessObjectService == null) {
			throw new IllegalStateException(
					"The businessObjectService was not injected.");
		}
		if (budgetRateAndBaseService == null) {
			throw new IllegalStateException(
					"The budgetRateAndBaseService was not injected.");
		}
		if (rolodexService == null) {
			throw new IllegalStateException(
					"The rolodexService was not injected.");
		}

	}

	public String getTransmitXml(SapTransmission transmission) {
		ZGMKCRMINTERFACE sapInterface = constructSapInterface(transmission,
				new ArrayList<Long>());
		StringWriter stringWriter = new StringWriter();
		try {
			JAXBContext context = JAXBContext
					.newInstance(ZGMKCRMINTERFACE.class);
			Marshaller marshaller = context.createMarshaller();

			marshaller.marshal(sapInterface, stringWriter);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
		// LOG.info(stringWriter.toString()); //TODO mkousheh delete once in
		// production
		return stringWriter.toString();
	}

	public SapTransmissionResponse transmit(SapTransmission transmission) {
		ValidationResults results = validate(transmission);
		if (!results.calculateSuccess()) {
			return SapTransmissionResponse
					.validationFailure("Failed to validate transmission successfully.");
		}

		return executeSapService(transmission);
	}

	protected ZGMKCRMINTERFACE constructSapInterface(
			SapTransmission transmission,
			List<Long> interfacedSponsoredProgramIds) {
		ZGMGRANTSTRUCTURE grant = constructGrant(transmission.getAward());

		ZBAPI0035SPONSOREDOBJECTST sponsoredObjects = constructSponsoredObjects(transmission);
		grant.setSPONSOREDOBJECTS(sponsoredObjects);

		SPONSOR sponsors = objectFactory.createZGMKCRMINTERFACESPONSOR();
		BILLINGPLAN billingPlans = objectFactory
				.createZGMKCRMINTERFACEBILLINGPLAN();

		ZGMSPONSORSTRUCTURE sponsor = constructSponsor(transmission.getAward());
		sponsors.getItem().add(sponsor);

		// if a billing plan exists on the parent award, send it as a sponsor
		AwardSponsorContact billingPartner = determineBillingPartner(transmission
				.getAward());
		if (billingPartner != null) {
			ZGMSPONSORSTRUCTURE billingPartnerSponsor = constructSponsorFromBillingPartner(
					transmission.getAward(), billingPartner);
			sponsors.getItem().add(billingPartnerSponsor);
		}

		ZGMBILLINGPLANSTRUCTURE billingPlan = constructBillingPlan(transmission
				.getAward());
		if (billingPlan != null) {
			billingPlans.getItem().add(billingPlan);
		}

		SPONSOREDPROGRAMS sponsoredPrograms = objectFactory
				.createZGMKCRMINTERFACESPONSOREDPROGRAMS();
		for (Award childAward : transmission.getChildAwards()) {
			if (determineCostShareMemoMatch(childAward) == null) {
				if (!"group".equalsIgnoreCase(((AwardExtension) childAward
						.getExtension()).getChildType())) {
					ZGMSPPROGRAMSTRUCTURE sponsoredProgram = constructSponsoredProgram(
							childAward, false);
					sponsoredPrograms.getItem().add(sponsoredProgram);
					interfacedSponsoredProgramIds.add(childAward.getAwardId());
				}

				ZGMSPONSORSTRUCTURE childAwardSponsor = constructSponsor(childAward);
				sponsors.getItem().add(childAwardSponsor);
				if (childAward.getPrimeSponsor() != null) {
					ZGMSPONSORSTRUCTURE childAwardPrimeSponsor = constructSponsor(
							childAward, childAward.getPrimeSponsor());
					sponsors.getItem().add(childAwardPrimeSponsor);
				}

				for (AwardApprovedSubaward awardApprovedSubaward : childAward
						.getAwardApprovedSubawards()) {
					ZGMSPONSORSTRUCTURE subAwardOrgSponsor = constructSponsorFromOrganization(
							childAward, awardApprovedSubaward.getOrganization());
					sponsors.getItem().add(subAwardOrgSponsor);
				}

				ZGMBILLINGPLANSTRUCTURE childBillingPlan = constructBillingPlan(childAward);
				if (childBillingPlan != null) {
					billingPlans.getItem().add(childBillingPlan);
				}
			}
		}

		ZGMSPPROGRAMSTRUCTURE costSharingSponsoredProgram = processCostSharing(transmission
				.getAward());
		if (costSharingSponsoredProgram != null) {
			sponsoredPrograms.getItem().add(costSharingSponsoredProgram);
			interfacedSponsoredProgramIds.add(transmission.getAward()
					.getAwardId());
		}

		SPONSOREDPROGRAMSGRP sponsoredProgramsGrp = constructSponsoredProgramGroups(transmission
				.getAward());

		ZGMKCRMINTERFACE kcrmInterface = objectFactory.createZGMKCRMINTERFACE();
		kcrmInterface.setBILLINGPLAN(billingPlans);
		kcrmInterface.setSPONSOR(sponsors);
		kcrmInterface.setGRANT(grant);
		kcrmInterface.setSPONSOREDPROGRAMS(sponsoredPrograms);
		kcrmInterface.setSPONSOREDPROGRAMSGRP(sponsoredProgramsGrp);

		return kcrmInterface;
	}

	protected SapTransmissionResponse executeSapService(
			SapTransmission transmission) {

		List<Long> interfacedSponsoredProgramIds = new ArrayList<Long>();
		ZGMKCRMINTERFACE kcrmInterface = constructSapInterface(transmission,
				interfacedSponsoredProgramIds);

		// execute the service

		StringWriter sendWriter = new StringWriter();
		StringWriter receiveWriter = new StringWriter();
		SIKCRMPROCESSOUTBOUND sapService = newWebServicePort(new PrintWriter(
				sendWriter), new PrintWriter(receiveWriter));

		LOG.info("Outbound Message: " + getTransmitXml(transmission)); // TODO
																		// mkousheh
																		// delete
																		// once
																		// in
																		// production

		ZGMKCRMINTERFACEResponse response = null;
		try {
			response = sapService.siKCRMPROCESSOUTBOUND(kcrmInterface);
		} catch (Fault fault) {
			Throwable nestedCause = fault.getCause();
			if (nestedCause instanceof SocketTimeoutException) {
				LOG.error(
						"A SocketTimeoutException was thrown from service invocation.",
						fault);
				return SapTransmissionResponse.transmissionFailure(
						nestedCause.getMessage(), null, sendWriter.toString(),
						receiveWriter.toString());
			}

			// BU Customization ID: N/A mkousheh N/A - N/A
			// errorReporter.reportError("A SocketTimeoutException was thrown from service invocation.",
			// DATE_FORMAT_PATTERN);
			throw fault;
		}

		// BU Customization ID: N/A mukadder 20130429 - ENHC0010154 - Issue 55 -
		// KC_SAP Interface to display warning message
		List<String> warningMessages = processWarningMessages(response);

		String failureMessage = processResponseMessages(response);
		if (failureMessage != null) {
			return SapTransmissionResponse.transmissionFailure(failureMessage,
					warningMessages, sendWriter.toString(),
					receiveWriter.toString());
		}

		Map<Long, String> sponsoredProgramIds = null;

		SPONSOREDPROGRAMSMESSAGES sponsoredProgramsMessages = response
				.getSPONSOREDPROGRAMSMESSAGES();
		try {
			sponsoredProgramIds = extractSponsoredProgramIds(
					interfacedSponsoredProgramIds, transmission,
					sponsoredProgramsMessages);
		} catch (IllegalStateException e) {
			throw new IllegalStateException(e.getMessage() + "/n/n"
					+ receiveWriter.toString());
		}

		// BU Customization ID: N/A mukadder 20130306 - Handle SAP Walker number
		Map<Long, String> walkerIds = null;
		SPXWALKT walkerMessages = response.getSPXWALKT();
		try {
			walkerIds = extractWalkerIds(interfacedSponsoredProgramIds,
					transmission, walkerMessages);
		} catch (IllegalStateException e) {
			throw new IllegalStateException(e.getMessage() + "/n/n"
					+ receiveWriter.toString());
		}

		// BU Customization ID: N/A mukadder 20130429 - ENHC0010154 - Issue 55 -
		// KC_SAP Interface to display warning message
		return SapTransmissionResponse.success(sponsoredProgramIds, walkerIds,
				warningMessages, sendWriter.toString(),
				receiveWriter.toString());
	}

	/**
	 * Extract Walker numbers from the return message
	 */
	private Map<Long, String> extractWalkerIds(
			List<Long> interfacedSponsoredProgramIds,
			SapTransmission transmission, SPXWALKT walkerMessages) {
		Map<Long, String> walkerIds = new HashMap<Long, String>();
		if (walkerMessages != null && walkerMessages.getItem() != null) {
			if (walkerMessages.getItem().size() != interfacedSponsoredProgramIds
					.size()) {
				throw new IllegalStateException(
						"The number of sponsored program messages returned should be equal to the number of sponsored programs interfaced.  "
								+ "Instead "
								+ interfacedSponsoredProgramIds.size()
								+ " sponsored programs were interfaced and "
								+ walkerMessages.getItem().size()
								+ " sponsored program messages were returned.");
			}
			for (int index = 0; index < walkerMessages.getItem().size(); index++) {
				ZFIKCRMSPXWALK walkerMessage = walkerMessages.getItem().get(
						index);
				Long awardId = interfacedSponsoredProgramIds.get(index);
				walkerIds.put(awardId, walkerMessage.getFNDUNTDPTPRESUF());
			}
		}
		return walkerIds;
	}

	/**
	 * Constructs a new web service "port" and returns it.
	 */
	protected SIKCRMPROCESSOUTBOUND newWebServicePort(PrintWriter sendWriter,
			PrintWriter receiveWriter) {
		String sapWebServiceUrlValue = ConfigContext.getCurrentContextConfig()
				.getProperty(SAP_SERVICE_WSDL_URL_PARAM);
		if (StringUtils.isBlank(sapWebServiceUrlValue)) {
			throw new IllegalStateException(
					"The "
							+ SAP_SERVICE_WSDL_URL_PARAM
							+ " configuration parameter needs to be set in order to invoke the service.");
		}
		try {
			URL sapWebServiceUrl = new URL(sapWebServiceUrlValue);
			// TODO could probably cache the created ZFIGMKCRMINTERFACE_Service
			SIKCRMPROCESSOUTBOUND sapService = new SIKCRMPROCESSOUTBOUNDService(
					sapWebServiceUrl).getSIKCRMPROCESSOUTBOUNDPort();
			configureServiceEndpoint(sapService, sendWriter, receiveWriter);
			return sapService;
		} catch (MalformedURLException e) {
			throw new RuntimeException(
					"Failed to initialize SAP web service url because it was invalid, value was: "
							+ sapWebServiceUrlValue, e);
		}
	}

	/**
	 * Configures the service endpoint by configuring basic http authentication
	 * on the service as well as setting a custom value for the service endpoint
	 * if one has been configured.
	 */
	protected void configureServiceEndpoint(
			SIKCRMPROCESSOUTBOUND serviceEndpoint, PrintWriter sendWriter,
			PrintWriter receiveWriter) {

		Client client = ClientProxy.getClient(serviceEndpoint);
		client.getOutInterceptors().add(new LoggingOutInterceptor(sendWriter));
		client.getInInterceptors().add(new LoggingInInterceptor(receiveWriter));

		String connectionTimeout = ConfigContext.getCurrentContextConfig()
				.getProperty(SAP_SERVICE_CONNECTION_TIMEOUT_PARAM);
		String receiveTimeout = ConfigContext.getCurrentContextConfig()
				.getProperty(SAP_SERVICE_RECEIVE_TIMEOUT_PARAM);
		if (!StringUtils.isBlank(connectionTimeout)
				|| !StringUtils.isBlank(receiveTimeout)) {
			Conduit conduit = client.getConduit();
			if (conduit instanceof HTTPConduit) {
				HTTPConduit httpConduit = (HTTPConduit) conduit;
				HTTPClientPolicy httpClientPolicy = new HTTPClientPolicy();
				if (!StringUtils.isBlank(connectionTimeout)) {
					httpClientPolicy.setConnectionTimeout(Long
							.parseLong(connectionTimeout));
				}
				if (!StringUtils.isBlank(receiveTimeout)) {
					httpClientPolicy.setReceiveTimeout(Long
							.parseLong(receiveTimeout));
				}
				httpConduit.setClient(httpClientPolicy);
			}
		}

		if (!(serviceEndpoint instanceof BindingProvider)) {
			throw new IllegalArgumentException(
					"The given service endpoint should be an instance of BindingProvider but was not.");
		}
		BindingProvider provider = (BindingProvider) serviceEndpoint;
		Map<String, Object> requestContext = provider.getRequestContext();

		String username = ConfigContext.getCurrentContextConfig().getProperty(
				SAP_SERVICE_USERNAME_PARAM);
		String password = ConfigContext.getCurrentContextConfig().getProperty(
				SAP_SERVICE_PASSWORD_PARAM);

		if (StringUtils.isBlank(username)) {
			throw new IllegalStateException(
					"No username was configured for the SAP service, please ensure that the following configuration parameter is set: "
							+ SAP_SERVICE_USERNAME_PARAM);
		}
		if (StringUtils.isBlank(password)) {
			throw new IllegalStateException(
					"No passwrod was configured for the SAP service, please ensure that the following configuration parameter is set: "
							+ SAP_SERVICE_PASSWORD_PARAM);
		}

		requestContext.put(BindingProvider.USERNAME_PROPERTY, username);
		requestContext.put(BindingProvider.PASSWORD_PROPERTY, password);

		// next check if a custom endpoint url has been configured
		String endpointUrl = ConfigContext.getCurrentContextConfig()
				.getProperty(SAP_SERVICE_URL_PARAM);
		if (!StringUtils.isBlank(endpointUrl)) {
			requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
					endpointUrl);
		}

	}

	/**
	 * In the case of warning, returns a non-null string that contains the
	 * warning message. BU Customization ID: N/A mukadder 20130429 - ENHC0010154
	 * - Issue 55 - KC_SAP Interface to display warning message
	 */
	private List<String> processWarningMessages(
			ZGMKCRMINTERFACEResponse response) {
		List<String> warningMessages = new ArrayList<String>();
		if (response != null) {
			RETURN returnMessages = response.getRETURN();
			if (returnMessages != null & returnMessages.getItem() != null) {
				for (BAPIRET2 returnMessage : returnMessages.getItem()) {
					if (WARNING_MESSAGE_TYPE.equals(returnMessage.getTYPE())) {
						warningMessages.add(returnMessage.getMESSAGE());
					}
				}
			}
		}
		return warningMessages;
	}

	/**
	 * In the case of failure, returns a non-null string that contains the
	 * failure message.
	 */
	private String processResponseMessages(ZGMKCRMINTERFACEResponse response) {

		boolean isSuccess = true;
		boolean hasSuccessMessage = false;
		StringBuilder successMessage = new StringBuilder();
		StringBuilder failureMessage = new StringBuilder();

		successMessage.append("SAP integration success messages:\n");
		failureMessage.append("SAP integration failure messages:\n");

		if (response != null) {

			GRANTMESSAGES grantMessages = response.getGRANTMESSAGES();
			SPONSORMESSAGES sponsorMessages = response.getSPONSORMESSAGES();
			SPONSOREDPROGRAMSMESSAGES sponsoredProgramsMessages = response
					.getSPONSOREDPROGRAMSMESSAGES();

			if (grantMessages != null && grantMessages.getItem() != null) {
				for (ZGRANTMESSAGES grantMessage : grantMessages.getItem()) {
					if (ERROR_MESSAGE_TYPE.equals(grantMessage.getTYPE())) {
						isSuccess = false;
						failureMessage.append("    [Grant Failure Message: "
								+ grantMessage.getGRANTNBR() + "] - "
								+ grantMessage.getMESSAGE() + "\n");
					} else {
						hasSuccessMessage = true;
						successMessage.append("    [Grant Success Message: "
								+ grantMessage.getGRANTNBR() + "] - "
								+ grantMessage.getMESSAGE() + "\n");
					}
				}
			}

			if (sponsorMessages != null && sponsorMessages.getItem() != null) {
				for (ZSPONSORMESSAGES sponsorMessage : sponsorMessages
						.getItem()) {
					if (ERROR_MESSAGE_TYPE.equals(sponsorMessage.getTYPE())) {
						isSuccess = false;
						failureMessage.append("    [Sponsor Failure Message: "
								+ sponsorMessage.getSPONSOR() + "] - "
								+ sponsorMessage.getMESSAGE() + "\n");
					} else {
						hasSuccessMessage = true;
						successMessage.append("    [Sponsor Success Message: "
								+ sponsorMessage.getSPONSOR() + "] - "
								+ sponsorMessage.getMESSAGE() + "\n");
					}
				}
			}

			if (sponsoredProgramsMessages != null
					&& sponsoredProgramsMessages.getItem() != null) {
				for (ZSPPROGRAMMESSAGES sponsoredProgramsMessage : sponsoredProgramsMessages
						.getItem()) {
					if (ERROR_MESSAGE_TYPE.equals(sponsoredProgramsMessage
							.getTYPE())) {
						isSuccess = false;
						failureMessage
								.append("    [Sponsored Program Failure Message: "
										+ sponsoredProgramsMessage
												.getSPONSOREDPROG()
										+ "] - "
										+ sponsoredProgramsMessage.getMESSAGE()
										+ "\n");
					} else {
						hasSuccessMessage = true;
						successMessage
								.append("    [Sponsored Program Success Message: "
										+ sponsoredProgramsMessage
												.getSPONSOREDPROG()
										+ "] - "
										+ sponsoredProgramsMessage.getMESSAGE()
										+ "\n");
					}
				}
			}

		}

		if (hasSuccessMessage) {
			LOG.info(successMessage.toString());
		}
		if (!isSuccess) {
			LOG.info(failureMessage.toString());
			return failureMessage.toString();
		}
		return null;
	}

	/**
	 * Implements 1.7.1 and 1.7.2 of the functional specification.
	 * <p/>
	 * This data maps 1:1 from the Parent Award that is being interfaced.
	 */
	protected ZGMGRANTSTRUCTURE constructGrant(Award award) {

		// Specification Section 1.7.1

		ZGMGRANTSTRUCTURE grant = objectFactory.createZGMGRANTSTRUCTURE();

		CustomAwardDataHelper helper = new CustomAwardDataHelper(award);

		if (helper.getLastTransmissionDate() == null) {
			grant.setGRANTUPDATE(INTERFACE_NEW);
		} else {
			grant.setGRANTUPDATE(INTERFACE_UPDATE);
		}

		ZBAPI0035HEADER grantHeader = objectFactory.createZBAPI0035HEADER();
		ZBAPI0035HEADERADD grantHeaderAdd = objectFactory
				.createZBAPI0035HEADERADD();
		ZFIGRANTDATA grantData = objectFactory.createZFIGRANTDATA();

		grant.setHEADER(grantHeader);
		grant.setHEADERADD(grantHeaderAdd);
		grant.setEXTENSIONIN(grantData);

		// Position 1
		grantHeader.setGRANTNBR(deriveGrantNumber(award.getAwardNumber()));

		// BU Customization ID: N/A mkousheh 20110620 - Send Parent Transaction
		// Type
		grantHeader.setPARENTTRANSACTIONTYPE(award.getAwardTransactionType()
				.getDescription());

		// Position 2
		grantHeader.setGRANTTYPE(convertAccountTypeToGrantType(
				award.getAccountTypeCode(), award));

		// Position 4
		grantHeader.setSPONSOR(award.getSponsorCode());

		// Position 5 - pad with zeros to 10 chars
		grantHeaderAdd.setAUTHGROUP(StringUtils.rightPad(
				award.getLeadUnitNumber(), 10, "0"));

		// Position 6
		grantHeaderAdd.setAWARDTYPE(convertAwardTypeCodeToAwardType(
				award.getAwardTypeCode(), award));

		AwardAmountInfo awardAmountInfo = getAwardAmountInfoService()
				.fetchLastAwardAmountInfoForAwardVersionAndFinalizedTandMDocumentNumber(
						award);

		// Position 8
		grantHeaderAdd.setGRANTTOTAL(awardAmountInfo.getAmountObligatedToDate()
				.bigDecimalValue());

		// Position 9
		grantHeader
				.setVALIDFROM(dateToString(DateUtils
						.convertToSqlDate(awardAmountInfo
								.getCurrentFundEffectiveDate())));

		// BU Customization ID: N/A mkousheh 20110620 - Set Valid From Budget to
		// 3 months if null
		if (awardAmountInfo.getCurrentFundEffectiveDate() != null) {
			java.util.Date tmpDate = org.apache.commons.lang3.time.DateUtils
					.addMonths(awardAmountInfo.getCurrentFundEffectiveDate(),
							-3);
			grantHeader.setVALIDFROMBUDGET(dateToString(DateUtils
					.convertToSqlDate(tmpDate)));
		}

		// Position 10
		grantHeader
				.setVALIDTO(dateToString(DateUtils
						.convertToSqlDate(awardAmountInfo
								.getObligationExpirationDate())));
		// BU Customization ID: N/A mkousheh 20110620 - Set Valid To Budget to 1
		// year if null
		if (awardAmountInfo.getObligationExpirationDate() != null) {
			java.util.Date tmpDate2 = org.apache.commons.lang3.time.DateUtils
					.addYears(awardAmountInfo.getObligationExpirationDate(), 1);
			grantHeader.setVALIDTOBUDGET(dateToString(DateUtils
					.convertToSqlDate(tmpDate2)));
		}

		// Position 11
		grantHeader.setEXTREFERENCE(award.getSponsorAwardNumber());
		grantHeader.setINTREFERENCE(award.getAwardNumber());

		// Position 12
		grantHeaderAdd.setCFDANBR(award.getCfdaNumber());

		// Position 13
		if (award.getBasisOfPaymentCode() == null) {
			logAwardInfo(award,
					"awardBasisOfPayment is null, not sending billingRule on grant");
		} else {
			grantHeaderAdd.setBILLINGRULE(convertBasisOfPaymentToBillingRule(
					award.getBasisOfPaymentCode(), award));
		}

		// Position 14
		grantHeaderAdd
				.setLETTEROFCREDIT(convertMethodOfPaymentToLetterOfCredit(
						award.getMethodOfPaymentCode(), award));

		// BUKC-0124: Send new custom field FAIN to SAP via interface
		// (ENHC0012816)
		grantHeaderAdd.setFUNDINGORIGIN(((AwardExtension) award.getExtension())
				.getFain());

		// Position 16 and 17
		for (AwardReportTerm awardReportTermItem : award
				.getAwardReportTermItems()) {
			// skip Report Codes 27 and 24
			if (!awardReportTermItem.getReportCode().equals("27")
					&& !awardReportTermItem.getReportCode().equals("24")) {
				if (PAYMENT_INVOICES_REPORT_CLASS_CODE
						.equals(awardReportTermItem.getReportClassCode())) {
					grantData
							.setZZINVOICEFREQ(convertFrequencyCodeToInvoiceFrequency(awardReportTermItem
									.getFrequencyCode()));
					grantData
							.setZZINVOICEFORM(convertReportCodeToInvoiceForm(awardReportTermItem
									.getReportCode()));
					break;
				}
			}
		}

		// Position 18
		String advancePayment = determineAdvancePayment(award);
		if (advancePayment != null) {
			grantData.setZZADVPYMNTIND(advancePayment);
		}

		// Position 19
		grantData.setZZFUNDCENTER(StringUtils.rightPad(
				award.getLeadUnitNumber(), 10, "0"));

		// Position 20
		grantData.setZZAWARDTITLE(award.getTitle());

		// Position 22
		grantData.setZZINTEARNED(helper.getInterestEarned());

		// Position 23
		if (helper.isArra()) {
			grantData.setZZLDCODE(ARRA_FUNDING_INFORMATION);
		}

		// Position 24
		grantData
				.setZZMJRPRJCT("yes".equalsIgnoreCase(helper.getMajorProject()) ? "x"
						: "");

		// grantData.setZZINVOICEFREQ(""); // NOT ON AWARD
		// grantData.setZZINVOICEFORM(""); // NOT ON AWARD

		// Position 25
		String propertyOwnerTitle = determinePropertyOwnerTitle(award);
		if (propertyOwnerTitle != null) {
			grantData.setZZPRPRTYOWNR(propertyOwnerTitle);
		}

		// Position 26
		String costShareMemoMatch = determineCostShareMemoMatch(award);
		if (costShareMemoMatch != null) {
			grantData.setZZCOSTSHARE(costShareMemoMatch);
		}

		// Position 27
		grantData.setZZAVCTOLERANCE(convertAvcIndicatorToAvcTolerance(helper
				.getAvcIndicator()));

		// Position 28
		grantData.setZZNSFCTGRY(award.getNsfCode());

		// Position 29
		grantData.setZZA133CLSTR(helper.getA133Cluster());

		// Position 31
		// BUKC-0088: Transmitting Project Start Date as AwardEffectiveDate and
		// not BeginDate (fields switched after 3.0.1 release) - (DFCT0011210)
		grantData.setZZPROJBEGDA(dateToString(award.getAwardEffectiveDate()));
		// grantData.setZZPROJBEGDA(dateToString(award.getBeginDate()));

		// Position 32
		grantData.setZZPROJENDDA(dateToString(awardAmountInfo
				.getFinalExpirationDate()));

		grantData.setZZAWARDNO(award.getAwardNumber());

		// Position 33
		grantData.setZZSPONSOR(award.getPrimeSponsorCode());

		// Position 34
		grantHeader.setUSERSTATUS(convertStatusCodeToResponsibility(
				award.getStatusCode(), award));

		// Position 35
		grantData.setZZINTEARNED(convertInterestEarnedCode(helper
				.getInterestEarned()));

		// Position 36
		if (getMostRecentAwardReportTerm(award) != null
				&& getMostRecentAwardReportTerm(award)
						.getAwardReportTermRecipients().size() > 0) {
			grantData.setZZBILLPARTNER(getMostRecentAwardReportTerm(award)
					.getAwardReportTermRecipients().get(0).getRolodexId()
					.toString());
		}

		// Specification Section 1.7.2
		ZGMFACREDITT faCredit = objectFactory.createZGMFACREDITT();
		grant.setFACREDIT(faCredit);
		List<ZGMFACREDIT> faCredits = faCredit.getItem();
		for (AwardPerson awardPerson : award.getProjectPersons()) {
			for (AwardPersonUnit awardPersonUnit : awardPerson.getUnits()) {
				AwardPersonUnitCreditSplit creditSplit = getMostRecentCreditSplit(awardPersonUnit);
				ZGMFACREDIT credit = objectFactory.createZGMFACREDIT();
				credit.setGRANTNBR(award.getAwardNumber());
				credit.setDEPT(StringUtils.rightPad(
						awardPersonUnit.getUnitNumber(), 10, "0"));
				if (creditSplit != null) {
					credit.setPERCENTAGE(creditSplit.getCredit()
							.bigDecimalValue());
				}
				faCredits.add(credit);
			}
		}

		// Specification Section 1.7.3

		ZBAPI0035RESPONSIBLET grantPersons = objectFactory
				.createZBAPI0035RESPONSIBLET();
		grant.setRESPONSIBILITY(grantPersons);
		for (AwardPerson awardPerson : award.getProjectPersons()) {
			ZBAPI0035RESPONSIBLE grantPerson = objectFactory
					.createZBAPI0035RESPONSIBLE();
			grantPersons.getItem().add(grantPerson);
			grantPerson.setUSERID(awardPerson.getPersonId());
			grantPerson.setUSERNAME(awardPerson.getFullName());
			// BUKC-0062: SAP Interface - Handle multiple PIs for NIH awards
			// when transmit to SAP
			if (awardPerson.isMultiplePi()) {
				grantPerson.setRESPONSIBILITY("PI");
			} else {
				grantPerson
						.setRESPONSIBILITY(convertProjectRoleToResponsibility(
								awardPerson.getContactRoleCode(), award));

			}
		}

		for (AwardUnitContact awardContact : award.getAwardUnitContacts()) {
			if ("2".equals(awardContact.getUnitAdministratorTypeCode())) {
				ZBAPI0035RESPONSIBLE grantPerson = objectFactory
						.createZBAPI0035RESPONSIBLE();
				grantPersons.getItem().add(grantPerson);
				grantPerson.setUSERID(awardContact.getPersonId());
				grantPerson.setUSERNAME(awardContact.getFullName());
				grantPerson.setRESPONSIBILITY("DA");
			}
			if ("6".equals(awardContact.getUnitAdministratorTypeCode())) {
				ZBAPI0035RESPONSIBLE grantPerson = objectFactory
						.createZBAPI0035RESPONSIBLE();
				grantPersons.getItem().add(grantPerson);
				grantPerson.setUSERID(awardContact.getPersonId());
				grantPerson.setUSERNAME(awardContact.getPerson().getFullName());
				grantPerson.setRESPONSIBILITY("OAV");
			}

			// BUKC-0140: Add new Admin Type (Clinical Trial Admin)
			if ("8".equals(awardContact.getUnitAdministratorTypeCode())) {
				ZBAPI0035RESPONSIBLE grantPerson = objectFactory
						.createZBAPI0035RESPONSIBLE();
				grantPersons.getItem().add(grantPerson);
				grantPerson.setUSERID(awardContact.getPersonId());
				grantPerson.setUSERNAME(awardContact.getPerson().getFullName());
				grantPerson.setRESPONSIBILITY("CTAD");
			}

		}

		// BU Customization ID: N/A mkousheh 20110706 - Get OSP and PAFO Admin
		// from Unit Admin per lead unit
		// (above code is not accurate since Data are not stored on award-person
		for (UnitAdministrator unitAdministrator : award.getLeadUnit()
				.getUnitAdministrators()) {
			if ("3".equals(unitAdministrator.getUnitAdministratorTypeCode())) {
				ZBAPI0035RESPONSIBLE grantPerson = objectFactory
						.createZBAPI0035RESPONSIBLE();
				grantPersons.getItem().add(grantPerson);
				grantPerson.setUSERID(unitAdministrator.getPersonId());
				grantPerson.setUSERNAME(unitAdministrator.getPerson()
						.getFullName());
				grantPerson.setRESPONSIBILITY("OSP");
			}
			if ("4".equals(unitAdministrator.getUnitAdministratorTypeCode())) {
				ZBAPI0035RESPONSIBLE grantPerson = objectFactory
						.createZBAPI0035RESPONSIBLE();
				grantPersons.getItem().add(grantPerson);
				grantPerson.setUSERID(unitAdministrator.getPersonId());
				grantPerson.setUSERNAME(unitAdministrator.getPerson()
						.getFullName());
				grantPerson.setRESPONSIBILITY("PAFO");
			}

		}

		return grant;
	}

	/**
	 * To derive grant number from award number, we take the first 6 digits.
	 * <p/>
	 * For example, if award number is 123456-00001 then grant number will be
	 * 123456.
	 *
	 * @param awardNumber
	 *            the award number
	 * @return the derived grant number
	 * @throws IllegalArgumentException
	 *             if the award number is not in a valid format
	 */
	private String deriveGrantNumber(String awardNumber) {
		if (StringUtils.isBlank(awardNumber) || awardNumber.length() < 6) {
			throw new IllegalArgumentException(
					"Grant number could not be derived from the given award number, it was either blank or less than 6 digits.  Award number was: "
							+ awardNumber);
		}
		return awardNumber.substring(0, 6);
	}

	/**
	 * Converts the given frequency code from KC to an SAP invoice frequency
	 * according to the following mapping:
	 * <p/>
	 * When Frequency_Code = 1, Invoice_Frequency = "M� When Frequency_Code = 2,
	 * Invoice_Frequency = "Q" All other values for Frequency_Code,
	 * Invoice_Frequency = "O"
	 *
	 * @param frequencyCode
	 *            the frequencyCode to convert
	 * @return the resulting invoice frequency
	 * @throws IllegalArgumentException
	 *             if the given frequencyCode is blank or null
	 */
	private String convertFrequencyCodeToInvoiceFrequency(String frequencyCode) {
		if (StringUtils.isBlank(frequencyCode)) {
			throw new IllegalArgumentException(
					"The given frequencyCode was null or blank, could not convert to an invoice frequency.");
		}
		if ("1".equals(frequencyCode)) {
			return "M";
		} else if ("2".equals(frequencyCode)) {
			return "Q";
		} else {
			return "O";
		}
	}

	/**
	 * Converts the given report code from KC to an SAP invoice form according
	 * to the following mapping:
	 * <p/>
	 * When Report_Code = 1, Invoice_Form = "ZFI_GM_SF425" When Report_Code = 3,
	 * Invoice_Form = "ZFI_GM_SF270" When Report_Code = 25, Invoice_Form =
	 * "ZFI_GM_SF1034-1035" When Report_Code = 26, Invoice_Form =
	 * "ZFI_GM_BU-Standard" When Report_Code = 28, Invoice_Form = "Manual"
	 */
	private String convertReportCodeToInvoiceForm(String reportCode) {
		String invoiceForm = getParameterService()
				.getSubParameterValueAsString(AwardDocument.class,
						BUConstants.REPORT_CODE_TO_INVOICE_FORM_MAPPING,
						reportCode);
		if (StringUtils.isEmpty(invoiceForm)) {
			throw new IllegalArgumentException(
					"Could not resolve an invoice form from the given report code, value was: "
							+ reportCode);
		}
		return invoiceForm;
	}

	/**
	 * Determines the Advance_Payment value using the following algorithm:
	 * <p/>
	 * When a row exists on AWARD_REPORT_TERMS where
	 * AWARD_REPORT_TERMS.REPORT_CLASS_CODE = 8 and
	 * AWARD_REPORT_TERMS.REPORT_CODE = 27 � Send an "X" value for
	 * Advance_Payment. Otherwise don"t send a value.
	 *
	 * @param award
	 *            the award to determine the advance payment for
	 * @return the value of the advance payment ("X") or null if no advance
	 *         payment data should be sent
	 */
	private String determineAdvancePayment(Award award) {
		List<AwardReportTerm> awardReportTerms = award
				.getAwardReportTermItems();
		for (AwardReportTerm awardReportTerm : awardReportTerms) {
			if (PAYMENT_INVOICES_REPORT_CLASS_CODE.equals(awardReportTerm
					.getReportClassCode())
					&& "27".equals(awardReportTerm.getReportCode())) {
				return "X";
			}
		}
		return null;
	}

	/**
	 * Determines the Property_Owner_Title for the given Award using the
	 * following mapping:
	 * <p/>
	 * If an Award has AWARD_SPONSOR_TERM_ID = 420; Property_Owner_Title = "BU"
	 * If an Award has AWARD_SPONSOR_TERM_ID = 421; Property_Owner_Title = "FD"
	 * If an Award has AWARD_SPONSOR_TERM_ID = 422; Property_Owner_Title = "OI"
	 * Otherwise, don"t send anything
	 *
	 * @param award
	 *            the award to determine the property owner title for
	 * @return the property owner title or null if one cannot be determined
	 */
	private String determinePropertyOwnerTitle(Award award) {
		AwardSponsorTerm sponsorTerm = getMostRecentAwardSponsorTerm(award);
		if (sponsorTerm != null) {
			if (new Long(420).equals(sponsorTerm.getSponsorTermId())) {
				return "BU";
			} else if (new Long(421).equals(sponsorTerm.getSponsorTermId())) {
				return "FD";
			} else if (new Long(422).equals(sponsorTerm.getSponsorTermId())) {
				return "OI";
			}
		}
		return null;
	}

	/**
	 * Determines the Cost_Share_Memo_Match value using the following algorithm:
	 * <p/>
	 * If an Award has a row on the AWARD_COST_SHARE where Cost_Share_Type_Code
	 * = 3, then Cost_Share_Memo_Match = "X". Otherwise don't send a value.
	 *
	 * @param award
	 *            the award to determine the cost share memo match for
	 * @return the value of the cost share memo match ("X") or null if no cost
	 *         share memo match data should be sent
	 */
	public String determineCostShareMemoMatch(Award award) {
		List<AwardCostShare> awardCostShares = award.getAwardCostShares();
		for (AwardCostShare awardCostShare : awardCostShares) {
			if (new Integer(3).equals(awardCostShare.getCostShareTypeCode())) {
				return "X";
			}
		}
		return null;
	}

	/**
	 * Takes the given Award Type and converts it to an Award Type tolerance.
	 *
	 * @param awardTypeCode
	 *            the awardTypeCode to convert
	 * @return the avc tolerance value for the given indicator
	 */
	private String convertAwardTypeCodeToAwardType(int awardTypeCode,
			Award award) {
		String returnValue = getParameterService()
				.getSubParameterValueAsString(AwardDocument.class,
						BUConstants.AWARD_TYPE_MAPPING,
						Integer.toString(awardTypeCode));
		if (StringUtils.isEmpty(returnValue)) {
			throw new IllegalArgumentException(
					"Failed to convert award type, given award type value was not understood: "
							+ awardTypeCode + " awardNumber was: "
							+ award.getAwardNumber() + "\n" + award.toString());
		}
		return returnValue;
	}

	/**
	 * Takes the given AVC indicator and converts it to an AVC tolerance.
	 *
	 * @param avcIndicator
	 *            the avcIndicator to convert
	 * @return the avc tolerance value for the given indicator
	 */
	private String convertAvcIndicatorToAvcTolerance(String avcIndicator) {
		if (StringUtils.isBlank(avcIndicator)) {
			throw new IllegalArgumentException(
					"The avc indicator was blank or null.");
		}
		if ("100%".equals(avcIndicator)) {
			return "Z001";
		} else if ("125%".equals(avcIndicator)) {
			return "Z002";
		} else if ("0".equals(avcIndicator)) {
			return "Z999";
		}
		throw new IllegalArgumentException(
				"Could not resolve avc tolerance from the given avc indicator, value was: "
						+ avcIndicator);
	}

	/**
	 * Takes the given Interest Earned Code and converts it to an Interest
	 * Earned Code tolerance.
	 *
	 * @param interestEarnedCode
	 * @return the interest earned code tolerance value for the given code
	 */
	private String convertInterestEarnedCode(String interestEarnedCode) {
		if ("Not Applicable".equals(interestEarnedCode)) {
			return "N";
		} else if ("Return to sponsor".equals(interestEarnedCode)) {
			return "S";
		} else if ("Return to award".equals(interestEarnedCode)) {
			return "A";
		}
		throw new IllegalArgumentException(
				"Could not convert the given interest earned code, value was: "
						+ interestEarnedCode);
	}

	/**
	 * Determines the Billing Partner ID value if found, otherwise use the
	 * Rolodex ID
	 *
	 * @param award
	 *            the award to determine the billing partner match for
	 * @return the value of tje billing partner id
	 */
	private Integer determineBillingPartnerId(Award award) {
		AwardSponsorContact billingPartner = determineBillingPartner(award);
		if (billingPartner != null) {
			return billingPartner.getRolodexId();
		}
		return null;
	}

	/**
	 * @param award
	 *            award the award to determine the billing partner match for
	 * @return an object of the billing contact if found
	 */
	private AwardSponsorContact determineBillingPartner(Award award) {
		for (AwardSponsorContact contact : award.getSponsorContacts()) {
			if ("1".equals(contact.getContactRoleCode())) {
				return contact;
			}
		}
		return null;
	}

	/**
	 * Implements section 1.7.7 of the functional specification to create
	 * sponsored term data. It does this for child awards as well as their
	 * parent.
	 * <p/>
	 * <p>
	 * Note that if an award has multiple sponsored terms, they all should be
	 * sent (not only the most recent).
	 */
	protected ZBAPI0035SPONSOREDOBJECTST constructSponsoredObjects(
			SapTransmission transmission) {
		ZBAPI0035SPONSOREDOBJECTST sapSponsoredObjects = objectFactory
				.createZBAPI0035SPONSOREDOBJECTST();
		Set<SponsoredObject> parentSponsoredObjects = constructSponsoredObjects(
				null, transmission.getAward());
		if (parentSponsoredObjects != null
				&& isCostSharing(transmission.getAward())) {
			loadSapSponsoredObjects(sapSponsoredObjects, parentSponsoredObjects);
		}
		for (Award award : transmission.getChildAwards()) {
			if (!"group".equalsIgnoreCase(((AwardExtension) award
					.getExtension()).getChildType())) {
				Set<SponsoredObject> sponsoredObjects = constructSponsoredObjects(
						parentSponsoredObjects, award);
				loadSapSponsoredObjects(sapSponsoredObjects, sponsoredObjects);
			}
		}
		return sapSponsoredObjects;
	}

	protected Set<SponsoredObject> constructSponsoredObjects(
			Set<SponsoredObject> parentSponsoredObjects, Award award) {
		Set<SponsoredObject> sponsoredObjects = new HashSet<SponsoredObject>();
		List<AwardSponsorTerm> awardSponsorTerms = award.getAwardSponsorTerms();
		String sponsoredProgram = null;
		if (StringUtils.isEmpty(award.getAccountNumber())) {
			sponsoredProgram = award.getAwardNumber();
		} else {
			sponsoredProgram = award.getAccountNumber();
		}
		if (awardSponsorTerms != null) {
			for (AwardSponsorTerm awardSponsorTerm : awardSponsorTerms) {
				String sponsoredClass = convertSponsorTermToSponsorClass(
						awardSponsorTerm.getSponsorTermId(), award);
				if (!StringUtils.isBlank(sponsoredProgram)
						&& !StringUtils.isBlank(sponsoredClass)) {
					sponsoredObjects.add(new SponsoredObject(sponsoredProgram,
							sponsoredClass));
				}
			}
		}
		// copy in the entries from the parent, using the child sponsored
		// program
		if (parentSponsoredObjects != null) {
			for (SponsoredObject parentSponsoredObject : parentSponsoredObjects) {
				sponsoredObjects.add(new SponsoredObject(sponsoredProgram,
						parentSponsoredObject.sponsoredClass));
			}
		}
		return sponsoredObjects;
	}

	protected void loadSapSponsoredObjects(
			ZBAPI0035SPONSOREDOBJECTST sapSponsoredObjects,
			Set<SponsoredObject> sponsoredObjects) {
		for (SponsoredObject sponsoredObject : sponsoredObjects) {
			ZBAPI0035SPONSOREDOBJECTS sapSponsoredObject = objectFactory
					.createZBAPI0035SPONSOREDOBJECTS();
			sapSponsoredObject
					.setSPONSOREDPROG(sponsoredObject.sponsoredProgram);
			sapSponsoredObject
					.setSPONSOREDCLASS(sponsoredObject.sponsoredClass);
			sapSponsoredObjects.getItem().add(sapSponsoredObject);
		}
	}

	/**
	 * This class is just a wrapper around sponsored program and sponsored
	 * class. Implements hashcode and equals method which allows us to use it
	 * inside a Set and prevent duplicate entries from happenign.
	 */
	private static class SponsoredObject {
		private final String sponsoredProgram;
		private final String sponsoredClass;

		SponsoredObject(String sponsoredProgram, String sponsoredClass) {
			if (StringUtils.isBlank(sponsoredProgram)
					|| StringUtils.isBlank(sponsoredClass)) {
				throw new IllegalArgumentException(
						"both sponsored program and sponsored class must be non-null!");
			}
			this.sponsoredProgram = sponsoredProgram;
			this.sponsoredClass = sponsoredClass;
		}

		@Override
		public boolean equals(Object object) {
			if (object instanceof SponsoredObject) {
				SponsoredObject sponsoredObject = (SponsoredObject) object;
				return sponsoredObject.sponsoredProgram
						.equals(sponsoredProgram)
						&& sponsoredObject.sponsoredClass
								.equals(sponsoredClass);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return sponsoredProgram.hashCode();
		}

	}

	/**
	 * Implements section 1.7.4, 1.7.5, and 1.7.6 of the functional
	 * specification.
	 *
	 * @param award
	 *            the award for which to generate a sponsored program
	 * @param costSharing
	 *            indicates if this is a cost sharing program or not, if true
	 *            then this method produces a sponsored program structure which
	 *            compiles with specification section 1.7.4, if false then it
	 *            will generate one which complies with specification section
	 *            1.7.5
	 * @param parentAward
	 *            true if the given award is a parent award, false otherwise
	 * @return
	 */
	protected ZGMSPPROGRAMSTRUCTURE constructSponsoredProgram(Award award,
			boolean costSharing) {

		CustomAwardDataHelper helper = new CustomAwardDataHelper(award);
		AwardBudgetExt abvoe = getLastBudgetVersion(award.getAwardDocument());
		boolean awardBudgetVersionToBePosted = false;
		if (abvoe != null) {
			String budgetStatus = abvoe.getAwardBudgetStatusCode();
			awardBudgetVersionToBePosted = Constants.BUDGET_STATUS_CODE_TO_BE_POSTED
					.equalsIgnoreCase(budgetStatus);
		}

		ZGMSPPROGRAMSTRUCTURE sponsoredProgramStructure = objectFactory
				.createZGMSPPROGRAMSTRUCTURE();

		if (helper.getLastTransmissionDate() == null) {
			sponsoredProgramStructure.setSPPROGRAMUPDATE(INTERFACE_NEW);
		} else {
			sponsoredProgramStructure.setSPPROGRAMUPDATE(INTERFACE_UPDATE);
		}

		sponsoredProgramStructure.setSPPROGRAMNUMBER(award.getAccountNumber());

		// Handle data which is shared between 1.7.4 and 1.7.5

		// Position 1
		if (StringUtils.isEmpty(award.getAccountNumber())) {
			sponsoredProgramStructure.setSPONSOREDPROG(award.getAwardNumber());
		} else {
			sponsoredProgramStructure
					.setSPONSOREDPROG(award.getAccountNumber());
		}

		AwardAmountInfo awardAmountInfo = getAwardAmountInfoService()
				.fetchLastAwardAmountInfoForAwardVersionAndFinalizedTandMDocumentNumber(
						award);

		// Position 3
		sponsoredProgramStructure.setZZVALIDFROM(dateToString(awardAmountInfo
				.getCurrentFundEffectiveDate()));

		// Position 4
		sponsoredProgramStructure.setZZVALIDTO(dateToString(awardAmountInfo
				.getObligationExpirationDate()));

		// Position 6
		sponsoredProgramStructure
				.setBUSINESSAREA(convertLeadUnitToBusinessArea(
						award.getLeadUnitNumber(), award));

		// Position 7
		GMSPPROGRAMFMBTTT programFmbtTt = objectFactory
				.createGMSPPROGRAMFMBTTT();
		GMSPPROGRAMFMBT programFmbt = objectFactory.createGMSPPROGRAMFMBT();
		programFmbtTt.getItem().add(programFmbt);
		sponsoredProgramStructure.setTGMSPPROGRAMFMBT(programFmbtTt);

		programFmbt.setSPONSOREDPROG(award.getAccountNumber());
		programFmbt.setFUNCTIONALAREA(convertActivityTypeToFunctionalArea(
				award.getActivityTypeCode(), award));

		// Position 8
		// If this is a cost sharing sponsored program then it is sourced from
		// the parent award, but they don't have
		// budgets so the F and A rate would not be able to be calculated.
		// also, parent awards cant have budgets, so check to see if their are
		// children as well
		AwardHierarchy hierarchy = getAwardHierarchyService()
				.loadAwardHierarchy(award.getAwardNumber());
		String overheadKey = "";
		if (!awardBudgetVersionToBePosted) {

			// BU Customization ID: N/A mkousheh 20120504 - Get the latest child
			// that matches that was successful transmitted
			AwardTransmissionChild lastTransmittedChild = getLatestChildAwardTransmitted(
					hierarchy.getRoot().getAward(), award.getAwardNumber(),
					true);
			if (lastTransmittedChild != null) {
				overheadKey = lastTransmittedChild.getOverheadKey();
			}

			// BU Customization ID: N/A mkousheh 20120504 - Commented out the
			// following code and replaced with the above -
			// BUG (it gets latest transmitted then matches the child, sometimes
			// no matching child in the last transmission
			// AwardTransmission lastTransmission =
			// getLatestAwardTransmission(hierarchy.getRoot().getAward(), true);
			// if (lastTransmission != null) {
			// AwardTransmissionChild lastChildTransmission =
			// findMatchingTransmissionChild(lastTransmission.getTransmissionChildren(),
			// award.getAwardNumber());
			// if (lastChildTransmission != null) {
			// overheadKey = lastChildTransmission.getOverheadKey();
			// }
			// }

			// BU Customization ID: N/A mkousheh N/A - Switching this to use
			// value saved on last transmission child -
			// BigDecimal fAndARate = calculateApplicableFandARate(award);
			// if(fAndARate != null) {
			// sponsoredProgramStructure.setOVERHEADKEY(StringUtils.leftPad(fAndARate.toPlainString(),
			// 6, "0"));
			// }
		} else if (!costSharing && !hierarchy.hasChildren()
				&& awardBudgetVersionToBePosted) {
			BigDecimal fAndARate = calculateApplicableFandARate(award);
			if (fAndARate != null
					&& (((AwardExtension) award.getExtension())
							.getFederalRateDate() == null || "NA"
							.equalsIgnoreCase(((AwardExtension) award
									.getExtension()).getFederalRateDate()))) {
				overheadKey = StringUtils.leftPad(fAndARate.toPlainString(), 6,
						"0");
			} else {
				// get the system parameter for these values and lookup the
				// mapping value based on the date
				overheadKey = getParameterService()
						.getSubParameterValueAsString(
								AwardDocument.class,
								BUConstants.FEDERAL_RATE_DATE_OVERHEAD_KEY_FIELD_MAPPINGS,
								((AwardExtension) award.getExtension())
										.getFederalRateDate());
				if (fAndARate == null || StringUtils.isBlank(overheadKey)) {
					overheadKey = ("000.00");
				}
			}
		}
		sponsoredProgramStructure.setOVERHEADKEY(overheadKey);
		GlobalVariables.getUserSession().addObject(
				"overheadKey-".concat(award.getAwardNumber()),
				(Object) overheadKey);
		LOG.info("Writing overheadKey to session: " + overheadKey);

		// Position 9
		programFmbt.setFUNDSCENTER(StringUtils.rightPad(
				award.getLeadUnitNumber(), 10, "0"));

		// Position 11
		sponsoredProgramStructure.setZZREFERENCE(award.getAwardNumber());

		// Position 12
		sponsoredProgramStructure.setZZAWARDNO(award.getSponsorAwardNumber());

		// Position 13
		sponsoredProgramStructure.setZZDOCNO(award.getDocumentFundingId());

		AwardBudgetExt budget = null;
		if (!costSharing) {
			budget = award.getAwardDocument().getBudgetVersionOverview();
			if (budget == null) {
				throw new IllegalArgumentException("Award with number "
						+ award.getAwardNumber() + " does not have a budget.");
			}
		}

		// BU Customization ID: N/A mkousheh 20110706 - In case of CostSharing
		// check if updated
		boolean costSharesHaveChanged = false;
		if (costSharing) {
			AwardCostShare tmpAwardCostShare = getMostRecentAwardCostShare(award);
			if (tmpAwardCostShare != null
					&& ((AwardExtension) award.getExtension())
							.getLastTransmissionDate() != null
					&& tmpAwardCostShare.getUpdateTimestamp() != null
					&& ((AwardExtension) award.getExtension())
							.getLastTransmissionDate().before(
									tmpAwardCostShare.getUpdateTimestamp())) {
				costSharesHaveChanged = true;
			}
		}

		// if (!costSharesHaveChanged) {
		// Position 17
		if (awardBudgetVersionToBePosted && budget != null
				&& budget.getTotalDirectCost() != null) {
			sponsoredProgramStructure.setBUDGETTDC(budget.getTotalDirectCost()
					.bigDecimalValue());
		} else {
			sponsoredProgramStructure.setBUDGETTDC(ScaleTwoDecimal.ZERO
					.bigDecimalValue());
		}

		// Position 18
		if (awardBudgetVersionToBePosted && budget != null
				&& budget.getTotalIndirectCost() != null) {
			sponsoredProgramStructure.setBUDGETFA(budget.getTotalIndirectCost()
					.bigDecimalValue());
		} else {
			sponsoredProgramStructure.setBUDGETFA(ScaleTwoDecimal.ZERO
					.bigDecimalValue());
		}
		// }

		// BU Customization ID: N/A mkousheh 20110712 - If Parent Award
		// Transaction is NoCostExtention or Administrative Changes Set Budget
		// to zero
		if (hierarchy.getRoot().getAward().getAwardTransactionTypeCode() != null) {
			if (hierarchy.getRoot().getAward().getAwardTransactionTypeCode()
					.equals(10)
					|| hierarchy.getRoot().getAward()
							.getAwardTransactionTypeCode().equals(6)) {
				sponsoredProgramStructure.setBUDGETTDC(ScaleTwoDecimal.ZERO
						.bigDecimalValue());
				sponsoredProgramStructure.setBUDGETFA(ScaleTwoDecimal.ZERO
						.bigDecimalValue());
			}
		}

		// Position 19
		boolean fringeNotAllowed = helper.isFringeNotAllowed();
		sponsoredProgramStructure.setFRINGECODE(FringeCodeMapping
				.mapToSapFringeCode(fringeNotAllowed,
						award.getAccountTypeCode()));

		/**
		 * Data which is only part of 1.7.4
		 */
		if (!costSharing && !hierarchy.hasChildren()) {

			// Position 10
			sponsoredProgramStructure.setZZORDCAT(convertChildType(
					helper.getChildType(), award));

			// Position 14
			String offCampus = null;
			if (!awardBudgetVersionToBePosted) {

				// BU Customization ID: N/A mkousheh 20120504 - Get the latest
				// child that matches that was successful transmitted
				// AwardTransmission lastTransmission =
				// getLatestAwardTransmission(hierarchy.getRoot().getAward(),
				// true);
				// if (lastTransmission != null) {
				// AwardTransmissionChild lastChildTransmission =
				// findMatchingTransmissionChild(lastTransmission.getTransmissionChildren(),
				// award.getAwardNumber());
				// if (lastChildTransmission != null) {
				// offCampus = lastChildTransmission.getOffCampus();
				// }
				// }
				AwardTransmissionChild lastTransmittedChild = getLatestChildAwardTransmitted(
						hierarchy.getRoot().getAward(), award.getAwardNumber(),
						true);
				if (lastTransmittedChild != null) {
					offCampus = lastTransmittedChild.getOffCampus();
				}

			} else if (budget != null) {
				offCampus = budget.getOnOffCampusFlag();
			}
			sponsoredProgramStructure.setZZOFFCAMPUS(offCampus);
			GlobalVariables.getUserSession().addObject(
					"offCampus-".concat(award.getAwardNumber()),
					(Object) offCampus);
			LOG.info("Writing offCampus to session: " + offCampus);

			// Position 16
			String baseCode = null;
			if (!awardBudgetVersionToBePosted) {

				// BU Customization ID: N/A mkousheh 20120504 - Get the latest
				// child that matches that was successful transmitted
				// AwardTransmission lastTransmission =
				// getLatestAwardTransmission(hierarchy.getRoot().getAward(),
				// true);
				// if (lastTransmission != null) {
				// AwardTransmissionChild lastChildTransmission =
				// findMatchingTransmissionChild(lastTransmission.getTransmissionChildren(),
				// award.getAwardNumber());
				// if (lastChildTransmission != null) {
				// baseCode = lastChildTransmission.getBaseCode();
				// }
				// }

				AwardTransmissionChild lastTransmittedChild = getLatestChildAwardTransmitted(
						hierarchy.getRoot().getAward(), award.getAwardNumber(),
						true);
				if (lastTransmittedChild != null) {
					baseCode = lastTransmittedChild.getBaseCode();
				}

			} else if (budget != null) { // AAP removed per whit - &&
											// awardBudgetVersionToBePosted
				baseCode = budget.getOhRateClassCode();
			}
			sponsoredProgramStructure.setBASECODE(baseCode);
			GlobalVariables.getUserSession().addObject(
					"baseCode-".concat(award.getAwardNumber()),
					(Object) baseCode);
			LOG.info("Writing baseCode to session: " + baseCode);

			// Position 20
			sponsoredProgramStructure.setDESCRIPTION(helper
					.getChildDescription());

			// Position 21
			// BU Customization ID: N/A mkousheh 20110816 - SUBRecipient no
			// longer needed
			// ZFIGMSUBRECIPIENTTAB awardApprovedSubawards =
			// objectFactory.createZFIGMSUBRECIPIENTTAB();
			// sponsoredProgramStructure.setTSUBRECIPIENT(awardApprovedSubawards);
			// List<AwardApprovedSubaward> subawards =
			// award.getAwardApprovedSubawards();
			// for (AwardApprovedSubaward subaward : subawards) {
			// GMSUBRECIPIENT awardApprovedSubaward =
			// objectFactory.createGMSUBRECIPIENT();
			// awardApprovedSubawards.getItem().add(awardApprovedSubaward);
			// awardApprovedSubaward.setSUBRECIPIENT(subaward.getOrganizationId());
			// awardApprovedSubaward.setSUBNAME(subaward.getOrganizationName());
			// awardApprovedSubaward.setSUBAMOUNT(subaward.getAmount().bigDecimalValue());
			// }

			// AwardApprovedSubaward awardApprovedSubaward =
			// getMostRecentAwardApprovedSubaward(award);
			// if (awardApprovedSubaward != null) {
			// ZGMSUBRECIPIENT subRecipent = new ZGMSUBRECIPIENT();
			// subRecipent.setSUBRECIPIENT(awardApprovedSubaward.getOrganizationId());
			// subRecipent.setSUBNAME(awardApprovedSubaward.getOrganizationName());
			// subRecipent.setSUBAMOUNT(awardApprovedSubaward.getAmount().bigDecimalValue());
			// sponsoredProgramStructure.setZZSUBRECIPIENT(subRecipent);
			// sponsoredProgramStructure.setZZSUBRECIPIENT(awardApprovedSubaward.getOrganizationId());
			// }

			// Position 22
			sponsoredProgramStructure.setKCRMSTATUS(StringUtils.leftPad(award
					.getStatusCode().toString(), 2, "0"));

			// Position 23
			sponsoredProgramStructure.setSPONSOR(award.getSponsorCode());

			// Position 24
			sponsoredProgramStructure.setPRIMESPONSOR(award
					.getPrimeSponsorCode());

			/**
			 * Specification section 1.7.6
			 */

			ZFIGMSPRESPONSIBLETABKCRM sponsoredProgramPersons = objectFactory
					.createZFIGMSPRESPONSIBLETABKCRM();
			sponsoredProgramStructure.setTRESPONSIBLE(sponsoredProgramPersons);
			List<AwardPerson> persons = award.getProjectPersons();
			for (AwardPerson person : persons) {
				ZGMSPRESPONSIBLEKCRM sponsoredProgramPerson = objectFactory
						.createZGMSPRESPONSIBLEKCRM();
				sponsoredProgramPersons.getItem().add(sponsoredProgramPerson);
				sponsoredProgramPerson.setSPONSOREDPROG(award
						.getAccountNumber());
				sponsoredProgramPerson.setOBJECTID(person.getPersonId());
				sponsoredProgramPerson.setOBJECTNAME(person.getFullName());

				// BUKC-0062: SAP Interface - Handle multiple PIs for NIH awards
				// when transmit to SAP
				if (person.isMultiplePi()) {
					sponsoredProgramPerson.setRESPONSIBILITY("PI");
				} else {
					sponsoredProgramPerson
							.setRESPONSIBILITY(convertProjectRoleToResponsibility(
									person.getContactRoleCode(), award));

				}
			}

			// BU Customization ID: N/A mkousheh 20110828 - Add DAs to
			// SponsoredPrograms
			for (AwardUnitContact awardContact : award.getAwardUnitContacts()) {
				if ("2".equals(awardContact.getUnitAdministratorTypeCode())) {
					ZGMSPRESPONSIBLEKCRM sponsoredProgramPerson = objectFactory
							.createZGMSPRESPONSIBLEKCRM();
					sponsoredProgramPersons.getItem().add(
							sponsoredProgramPerson);
					sponsoredProgramPerson.setOBJECTID(awardContact
							.getPersonId());
					sponsoredProgramPerson.setOBJECTNAME(awardContact
							.getFullName());
					sponsoredProgramPerson.setRESPONSIBILITY("DA");
				}
				if ("6".equals(awardContact.getUnitAdministratorTypeCode())) {
					ZGMSPRESPONSIBLEKCRM sponsoredProgramPerson = objectFactory
							.createZGMSPRESPONSIBLEKCRM();
					sponsoredProgramPersons.getItem().add(
							sponsoredProgramPerson);
					sponsoredProgramPerson.setOBJECTID(awardContact
							.getPersonId());
					sponsoredProgramPerson.setOBJECTNAME(awardContact
							.getPerson().getFullName());
					sponsoredProgramPerson.setRESPONSIBILITY("OAV");
				}

				// BUKC-0140: Add new Admin Type (Clinical Trial Admin)
				if ("8".equals(awardContact.getUnitAdministratorTypeCode())) {
					ZGMSPRESPONSIBLEKCRM sponsoredProgramPerson = objectFactory
							.createZGMSPRESPONSIBLEKCRM();
					sponsoredProgramPersons.getItem().add(
							sponsoredProgramPerson);
					sponsoredProgramPerson.setOBJECTID(awardContact
							.getPersonId());
					sponsoredProgramPerson.setOBJECTNAME(awardContact
							.getPerson().getFullName());
					sponsoredProgramPerson.setRESPONSIBILITY("CTAD");
				}
			}
		}

		/**
		 * Data which is only part of 1.7.5
		 */
		ScaleTwoDecimal costSharCommitmentMinusMemoMatch = getTotalCostShareCommitmentAmountMinusMemoMatch(award);

		if (costSharing && costSharCommitmentMinusMemoMatch != null
				&& costSharCommitmentMinusMemoMatch.isPositive()) {

			// Position 2
			// Sponsored_Program_type - according to Whit - this does not need
			// to be sent

			// Position 10
			sponsoredProgramStructure.setZZORDCAT("CS1");

			// Position 14
			sponsoredProgramStructure.setZZOFFCAMPUS("Y");

			// Position 21
			sponsoredProgramStructure.setKCRMSTATUS(award.getStatusCode()
					.toString());

			sponsoredProgramStructure
					.setBUDGETTDC(costSharCommitmentMinusMemoMatch
							.bigDecimalValue()); // getTotalDirectCost()

			// BU Customization ID: N/A mkousheh 20120731 - Remove the work
			// around as SAP team fixed the issue on their side
			/*
			 * // Temporary fix for Cost Sharing. When we send an update for a
			 * Cost Sharing Sponsored Program the Budget_TDC and Budget_FA
			 * should default to 0.00 AwardTransmission lastTransmission =
			 * getLatestAwardTransmission(hierarchy.getRoot().getAward(), true);
			 * if (lastTransmission != null) { AwardTransmissionChild
			 * lastChildTransmission =
			 * findMatchingTransmissionChild(lastTransmission
			 * .getTransmissionChildren(), award.getAwardNumber()); if
			 * (isCostSharing(award) && helper.getLastTransmissionDate() !=
			 * null) { sponsoredProgramStructure.setBUDGETTDC(KualiDecimal.ZERO.
			 * bigDecimalValue());
			 * sponsoredProgramStructure.setBUDGETFA(KualiDecimal
			 * .ZERO.bigDecimalValue());
			 * 
			 * } } // end temporary fix
			 */

			// BU Customization ID: N/A mkousheh 20120808 - Set 000.00 for
			// overhead rate when cost sharing
			sponsoredProgramStructure.setOVERHEADKEY("000.00");

			AwardCostShare awardCostShare = getMostRecentAwardCostShare(award);
			if (awardCostShare != null
					&& awardCostShare.getDestination() != null) {
				sponsoredProgramStructure.setSPONSOREDPROG(awardCostShare
						.getDestination());
			} else {
				sponsoredProgramStructure.setSPONSOREDPROG(award
						.getAwardNumber());
			}

		}

		return sponsoredProgramStructure;
	}

	private ScaleTwoDecimal getTotalCostShareCommitmentAmountMinusMemoMatch(
			Award award) {
		List<AwardCostShare> awardCostSharesMinusMemoMatch = new ArrayList<AwardCostShare>();

		for (AwardCostShare awardCostShare : award.getAwardCostShares()) {
			if (!new Integer(3).equals(awardCostShare.getCostShareTypeCode())) {
				awardCostSharesMinusMemoMatch.add(awardCostShare);
			}
		}

		return getTotalAmount(awardCostSharesMinusMemoMatch);
	}

	ScaleTwoDecimal getTotalAmount(List<? extends ValuableItem> valuableItems) {
		ScaleTwoDecimal returnVal = new ScaleTwoDecimal(0.00);
		for (ValuableItem item : valuableItems) {
			ScaleTwoDecimal amount = item.getAmount() != null ? item
					.getAmount() : new ScaleTwoDecimal(0.00);
			returnVal = returnVal.add(amount);
		}
		return returnVal;
	}

	/**
	 * Calculates the applicable F and A rate for the given Award using the
	 * current system time as the effective date.
	 *
	 * @param award
	 *            the award for whic to calculate the rate
	 * @return the value of the rate as a BigDecimal
	 * @throws IllegalStateException
	 *             if the rate could not be found for the current date/time
	 */
	private BigDecimal calculateApplicableFandARate(Award award) {
		ScaleTwoDecimal fAndARate = getBudgetRateAndBaseService()
				.calculateApplicableFandARate(award);
		if (fAndARate == null) {
			return null;
		}
		return fAndARate.bigDecimalValue();
	}

	/**
	 * Converts the given child type to a format that SAP can understand
	 * according to the following mapping:
	 * <p/>
	 * If "Standard�; Child_Type = ST1 If "Fabrication", Child_Type = P01 If
	 * "Participant Support Costs", Child_Type = PS1 If "Subaward", Child_Type =
	 * SA1
	 *
	 * @param childType
	 *            the child type to convert
	 * @return the converted child type value
	 */
	private String convertChildType(String childType, Award award) {
		String returnValue = getParameterService()
				.getSubParameterValueAsString(AwardDocument.class,
						BUConstants.CHILD_TYPE_MAPPING, childType);
		if (StringUtils.isEmpty(returnValue)) {
			throw new IllegalArgumentException(
					"Failed to convert child type, given child type value was not understood: "
							+ childType + " awardNumber was: "
							+ award.getAwardNumber() + "\n" + award.toString());
		}
		return returnValue;
	}

	/**
	 * Implements section 1.7.5 of the specification.
	 * <p/>
	 * A sponsored program for cost sharing will only get created if the award
	 * which is passed in supports cost sharing.
	 *
	 * @return the sponsored program information for the cost sharing award, or
	 *         null if the award does not support cost sharing
	 */
	protected ZGMSPPROGRAMSTRUCTURE processCostSharing(Award award) {
		ScaleTwoDecimal costSharCommitmentMinusMemoMatch = getTotalCostShareCommitmentAmountMinusMemoMatch(award);
		if (isCostSharing(award)
				&& costSharCommitmentMinusMemoMatch.isPositive()) {
			return constructSponsoredProgram(award, true);
		}
		return null;
	}

	/**
	 * Cost Sharing Exists when data for an Award exists on Award_Cost_Share and
	 * the Commitment Amount > 0
	 */
	protected boolean isCostSharing(Award award) {
		AwardCostShare awardCostShare = getMostRecentAwardCostShare(award);
		if (awardCostShare != null) {
			if (awardCostShare.getCommitmentAmount() != null
					&& awardCostShare.getCommitmentAmount().isPositive()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Implements section 1.7.9 of functional specification which relates to
	 * Billing Plans.
	 */
	protected ZGMBILLINGPLANSTRUCTURE constructBillingPlan(Award award) {

		AwardPaymentSchedule schedule = getMostRecentAwardPaymentSchedule(award);

		if (schedule == null) {
			return null;
		}

		ZGMBILLINGPLANSTRUCTURE billingPlan = objectFactory
				.createZGMBILLINGPLANSTRUCTURE();
		billingPlan.setBILLINGDATE(dateToString(schedule.getDueDate()));
		billingPlan.setBILLINGVALUE(schedule.getAmount().bigDecimalValue());

		return billingPlan;
	}

	/**
	 * Implements section 1.8 of functional spec related to Sponsor data
	 * mapping.
	 */
	protected ZGMSPONSORSTRUCTURE constructSponsor(Award award) {

		Sponsor sponsor = award.getSponsor();
		return constructSponsor(award, sponsor);
	}

	/**
	 * Implements section 1.8 of functional spec related to Sponsor data
	 * mapping.
	 */
	protected ZGMSPONSORSTRUCTURE constructSponsor(Award award, Sponsor sponsor) {

		Rolodex rolodex = sponsor.getRolodex();

		ZGMSPONSORSTRUCTURE sponsorStructure = objectFactory
				.createZGMSPONSORSTRUCTURE();

		// Position 1
		sponsorStructure.setSPONSOR(sponsor.getSponsorCode());

		// Position 2
		sponsorStructure.setSPONSORTYPE(convertSponsorCodeToSponsorType(
				sponsor.getSponsorTypeCode(), award));

		// Position 3
		sponsorStructure.setSPONSORNAME(sponsor.getSponsorName());

		// Position 4
		sponsorStructure.setCONTACTPERSON(rolodex.getFirstName() + " "
				+ rolodex.getLastName());

		// Position 5
		sponsorStructure.setADDRESSLINE1(rolodex.getAddressLine1());

		// Position 6
		sponsorStructure.setADDRESSLINE2(rolodex.getAddressLine2());

		// Position 7
		sponsorStructure.setCITY(rolodex.getCity());

		// Position 8
		sponsorStructure.setSTATE(rolodex.getState());

		// Position 9
		sponsorStructure.setPOSTCODE1(rolodex.getPostalCode());

		// Position 10
		sponsorStructure.setCOUNTRY(rolodex.getCountryCode());

		// Position 11
		sponsorStructure.setTELNUMBER(rolodex.getPhoneNumber());

		// Position 12
		sponsorStructure.setFAXNUMBER(rolodex.getFaxNumber());

		// Position 13
		sponsorStructure.setFUND(sponsor.getDodacNumber());

		// BU Customization ID: N/A mkousheh 20110712 - Special case where a
		// Prime Sponsor present then use
		// DODAC from Prime Sponsor
		if (award.getPrimeSponsor() != null) {
			sponsorStructure.setFUND(award.getPrimeSponsor().getDodacNumber());
		}

		return sponsorStructure;
	}

	/**
	 * Construct a sponsor structure for an Organization from a SubAward
	 */
	protected ZGMSPONSORSTRUCTURE constructSponsorFromOrganization(Award award,
			Organization organization) {

		Sponsor sponsor = businessObjectService.findBySinglePrimaryKey(
				Sponsor.class, organization.getOrganizationId());
		if (sponsor == null) {
			throw new IllegalStateException(
					"Failed to locate a Sponsor record for the given organization with id: "
							+ organization.getOrganizationId());
		}

		Rolodex rolodex = organization.getRolodex();

		ZGMSPONSORSTRUCTURE sponsorStructure = objectFactory
				.createZGMSPONSORSTRUCTURE();

		// Position 1
		sponsorStructure.setSPONSOR(organization.getOrganizationId());

		// Position 2

		sponsorStructure.setSPONSORTYPE(convertSponsorCodeToSponsorType(
				sponsor.getSponsorTypeCode(), award));

		// Position 3
		sponsorStructure.setSPONSORNAME(organization.getOrganizationName());

		// Position 4
		sponsorStructure.setCONTACTPERSON(rolodex.getFirstName() + " "
				+ rolodex.getLastName());

		// Position 5
		sponsorStructure.setADDRESSLINE1(rolodex.getAddressLine1());

		// Position 6
		sponsorStructure.setADDRESSLINE2(rolodex.getAddressLine2());

		// Position 7
		sponsorStructure.setCITY(rolodex.getCity());

		// Position 8
		sponsorStructure.setSTATE(rolodex.getState());

		// Position 9
		sponsorStructure.setPOSTCODE1(rolodex.getPostalCode());

		// Position 10
		sponsorStructure.setCOUNTRY(rolodex.getCountryCode());

		// Position 11
		sponsorStructure.setTELNUMBER(rolodex.getPhoneNumber());

		// Position 12
		sponsorStructure.setFAXNUMBER(rolodex.getFaxNumber());

		// Position 13
		sponsorStructure.setFUND(organization.getDodacNumber());

		return sponsorStructure;
	}

	/**
	 * Construct a sponsor structure from an AwardSponsorContact representing
	 * the billing partner
	 */
	protected ZGMSPONSORSTRUCTURE constructSponsorFromBillingPartner(
			Award award, AwardSponsorContact billingPartner) {

		RolodexContract rolodex = billingPartner.getRolodex();

		if (rolodex == null) {
			if (billingPartner.getRolodexId() != null) {
				rolodex = getRolodexService().getRolodex(
						billingPartner.getRolodexId());
			}
			if (rolodex == null) {
				throw new IllegalStateException(
						"Billing partner does not have a rolodex entry.  Award contact id of billing partner is: "
								+ billingPartner.getAwardContactId()
								+ ", rolodex id is: "
								+ billingPartner.getRolodexId());
			}
		}

		ZGMSPONSORSTRUCTURE sponsorStructure = objectFactory
				.createZGMSPONSORSTRUCTURE();

		// Position 1
		sponsorStructure.setSPONSOR(rolodex.getRolodexId().toString());

		// Position 2
		sponsorStructure
				.setSPONSORTYPE(DEFAULT_SPONSOR_TYPE_FOR_BILLING_PARTNER);

		// Position 3
		sponsorStructure.setSPONSORNAME(billingPartner
				.getContactOrganizationName());

		// Position 4
		sponsorStructure.setCONTACTPERSON(rolodex.getFirstName() + " "
				+ rolodex.getLastName());

		// Position 5
		sponsorStructure.setADDRESSLINE1(rolodex.getAddressLine1());

		// Position 6
		sponsorStructure.setADDRESSLINE2(rolodex.getAddressLine2());

		// Position 7
		sponsorStructure.setCITY(rolodex.getCity());

		// Position 8
		sponsorStructure.setSTATE(rolodex.getState());

		// Position 9
		sponsorStructure.setPOSTCODE1(rolodex.getPostalCode());

		// Position 10
		sponsorStructure.setCOUNTRY(rolodex.getCountryCode());

		// Position 11
		sponsorStructure.setTELNUMBER(rolodex.getPhoneNumber());

		// Position 12
		sponsorStructure.setFAXNUMBER(rolodex.getFaxNumber());

		// Position 13
		// TODO should a fund be passed?
		// sponsorStructure.setFund(?);

		return sponsorStructure;
	}

	private <T extends SequenceAssociate<?>> T getMostRecentSequenceAssociate(
			List<T> sequenceAssociates) {
		Collections.sort(sequenceAssociates,
				new Comparator<SequenceAssociate<?>>() {
					public int compare(SequenceAssociate<?> sequence1,
							SequenceAssociate<?> sequence2) {
						return sequence1.getSequenceNumber().compareTo(
								sequence2.getSequenceNumber());
					}
				});
		return sequenceAssociates.get(sequenceAssociates.size() - 1);
	}

	private AwardApprovedSubaward getMostRecentAwardApprovedSubaward(Award award) {
		List<AwardApprovedSubaward> awardApprovedSubawards = award
				.getAwardApprovedSubawards();
		if (awardApprovedSubawards == null || awardApprovedSubawards.isEmpty()) {
			return null;
		}
		return getMostRecentSequenceAssociate(awardApprovedSubawards);
	}

	private AwardPersonUnitCreditSplit getMostRecentCreditSplit(
			AwardPersonUnit awardPersonUnit) {
		List<AwardPersonUnitCreditSplit> creditSplits = awardPersonUnit
				.getCreditSplits();
		if (creditSplits.isEmpty()) {
			// credit split panel not being used any more
			// throw new
			// IllegalStateException("Person unit has no credit splits.");
			return null;
		}
		return getMostRecentSequenceAssociate(creditSplits);
	}

	private AwardReportTerm getMostRecentAwardReportTerm(Award award) {
		List<AwardReportTerm> reportTerms = award.getAwardReportTermItems();
		if (reportTerms.isEmpty()) {
			throw new IllegalStateException("Award has no report terms.");
		}
		return getMostRecentSequenceAssociate(reportTerms);
	}

	private AwardPaymentSchedule getMostRecentAwardPaymentSchedule(Award award) {
		List<AwardPaymentSchedule> schedules = award.getPaymentScheduleItems();
		if (schedules == null || schedules.isEmpty()) {
			return null;
		}
		return getMostRecentSequenceAssociate(schedules);
	}

	private AwardCostShare getMostRecentAwardCostShare(Award award) {
		List<AwardCostShare> costShares = award.getAwardCostShares();
		if (costShares.isEmpty()) {
			return null;
		}
		return getMostRecentSequenceAssociate(costShares);
	}

	private AwardSponsorTerm getMostRecentAwardSponsorTerm(Award award) {
		List<AwardSponsorTerm> awardSponsorTerms = award.getAwardSponsorTerms();
		if (awardSponsorTerms.isEmpty()) {
			return null;
		}
		return getMostRecentSequenceAssociate(awardSponsorTerms);
	}

	/**
	 * Implement section 1.7.8 regarding sending of the sponsored program group
	 */
	private SPONSOREDPROGRAMSGRP constructSponsoredProgramGroups(Award award) {

		SPONSOREDPROGRAMSGRP programGroups = objectFactory
				.createZGMKCRMINTERFACESPONSOREDPROGRAMSGRP();
		AwardHierarchy hierarchy = getAwardHierarchyService()
				.loadAwardHierarchy(award.getAwardNumber());
		for (AwardHierarchy childHierarchy : hierarchy.getChildren()) {
			if ("Group"
					.equalsIgnoreCase(((AwardExtension) award.getExtension())
							.getChildType())) {
				// ZGMSPPROGRAMGRPSTRUCTURE programGroup =
				// objectFactory.createZGMSPPROGRAMGRPSTRUCTURE();
				// programGroup.setPARENTAWARD(childHierarchy.getParentAwardNumber());
				// programGroup.setCHILDAWARD(childHierarchy.getAwardNumber());
				// programGroup.setTITLE(childHierarchy.getAward().getTitle());
				// programGroup.setSPONSOREDPROG(childHierarchy.getAward().getAccountNumber());
				// programGroups.getItem().add(programGroup);
				generateProgramGroupChildren(programGroups, childHierarchy);
			}
		}

		return programGroups;
	}

	private void generateProgramGroupChildren(
			SPONSOREDPROGRAMSGRP programGroups, AwardHierarchy awardHierarchy) {
		for (AwardHierarchy childHierarchy : awardHierarchy.getChildren()) {
			ZGMSPPROGRAMGRPSTRUCTURE childProgramGroup = objectFactory
					.createZGMSPPROGRAMGRPSTRUCTURE();
			childProgramGroup.setPARENTAWARD(childHierarchy
					.getParentAwardNumber());
			childProgramGroup.setCHILDAWARD(childHierarchy.getAwardNumber());
			childProgramGroup.setTITLE(childHierarchy.getAward().getTitle());
			childProgramGroup.setSPONSOREDPROG(childHierarchy.getAward()
					.getAccountNumber());
			programGroups.getItem().add(childProgramGroup);
			if ("Group".equalsIgnoreCase(((AwardExtension) (childHierarchy
					.getAward().getExtension())).getChildType())
					&& childHierarchy.hasChildren()) {
				generateProgramGroupChildren(programGroups, childHierarchy);
			}
		}
	}

	/**
	 * Extracts sponsored program ids from the given sponsored programs message
	 * responses.
	 * <p/>
	 * This allows us to implement section 1.6 of the functional specification
	 * and deal with the cases where an award is being assigned a new sponsored
	 * program id from SAP.
	 * <p/>
	 * TODO for now assume one-to-one mapping between sponsored program message
	 * and passed in sponsored programs?
	 *
	 * @param transmission
	 *            the original award transmission data
	 * @param sponsoredProgramsMessages
	 *            the sponsored programs message responses from the SAP service
	 * @return a populated Map containing the award id as the key and it's
	 *         corresponding sponsored program id as the value
	 */
	private Map<Long, String> extractSponsoredProgramIds(
			List<Long> interfacedSponsoredProgramIds,
			SapTransmission transmission,
			SPONSOREDPROGRAMSMESSAGES sponsoredProgramsMessages) {
		Map<Long, String> sponsoredProgramIds = new HashMap<Long, String>();
		if (sponsoredProgramsMessages != null
				&& sponsoredProgramsMessages.getItem() != null) {
			if (sponsoredProgramsMessages.getItem().size() != interfacedSponsoredProgramIds
					.size()) {
				throw new IllegalStateException(
						"The number of sponsored program messages returned should be equal to the number of sponsored programs interfaced.  "
								+ "Instead "
								+ interfacedSponsoredProgramIds.size()
								+ " sponsored programs were interfaced and "
								+ sponsoredProgramsMessages.getItem().size()
								+ " sponsored program messages were returned.");
			}
			for (int index = 0; index < sponsoredProgramsMessages.getItem()
					.size(); index++) {
				ZSPPROGRAMMESSAGES sponsoredProgramMessage = sponsoredProgramsMessages
						.getItem().get(index);
				Long awardId = interfacedSponsoredProgramIds.get(index);
				sponsoredProgramIds.put(awardId,
						sponsoredProgramMessage.getSPONSOREDPROG());
			}
		}
		return sponsoredProgramIds;
	}

	/**
	 * Run set of BU custom validation before transmitting award to SAP
	 *
	 * @param transmission
	 *            in progress
	 * @return Validation results object
	 */
	public ValidationResults validate(SapTransmission transmission) {
		ValidationResults validationResults = new ValidationResults();

		// transmission should have both a parent award and at least one child
		if (transmission.getAward() == null) {
			ErrorMessageKeys.NO_PARENT_AWARD.populate(validationResults);
		} else {
			checkPrimaryAwardForChanges(transmission.getAward(),
					validationResults);
			for (Award childAward : transmission.getChildAwards()) {
				checkChildAwardForChanges(transmission.getAward(), childAward,
						validationResults);
			}

			CustomAwardDataHelper helper = new CustomAwardDataHelper(
					transmission.getAward());
			if (helper.getLastTransmissionDate() == null) {
				if (transmission.getChildAwards() == null
						|| transmission.getChildAwards().isEmpty()) {
					ErrorMessageKeys.NO_CHILDREN_AWARD
							.populate(validationResults);
				}
			}

			// BU Customization ID: N/A mkousheh 20120713 - Add validation to
			// prevent transmission if Parent transaction
			// type is "No Cost Extension" or "Administrative Change" and the
			// selected child's budget status is "to be posted"
			if (transmission.getAward().getAwardTransactionTypeCode()
					.equals(AWARD_TRANSACTION_TYPE_NO_COST_EXTENSION)
					|| transmission
							.getAward()
							.getAwardTransactionTypeCode()
							.equals(AWARD_TRANSACTION_TYPE_ADMINISTRATION_CHANGE)) {
				for (Award childAward : transmission.getChildAwards()) {

					AwardBudgetExt abvoe = getLastBudgetVersion(childAward
							.getAwardDocument());
					boolean awardBudgetVersionToBePosted = false;
					if (abvoe != null) {
						String budgetStatus = abvoe.getAwardBudgetStatusCode();
						awardBudgetVersionToBePosted = Constants.BUDGET_STATUS_CODE_TO_BE_POSTED
								.equalsIgnoreCase(budgetStatus);
					}

					if (awardBudgetVersionToBePosted) {
						ErrorMessageKeys.PARENT_TRANSACTION_TYPE_NOCOST_EXT_OR_ADMIN_CHANGE
								.populate(transmission.getAward().getAwardId(),
										validationResults, transmission
												.getAward()
												.getAwardTransactionType()
												.getDescription(),
										childAward.getAwardNumber(),
										transmission.getAward()
												.getSequenceNumber().toString());
						break;
					}
				}
			}

			if (transmission.getAward().getAccountTypeCode() == 0) {
				ErrorMessageKeys.NO_PARENT_ACCOUNT_TYPE
						.populate(validationResults);
			}
			if (transmission.getAward().getAwardAmountInfos().size() > 0) {
				String timeAndMoneyDocumentNumber = transmission.getAward()
						.getLastAwardAmountInfo()
						.getTimeAndMoneyDocumentNumber();

				try {
					if (StringUtils.isNotBlank(timeAndMoneyDocumentNumber)) {
						TimeAndMoneyDocument timeAndMoneyDocument = (TimeAndMoneyDocument) getDocumentService()
								.getByDocumentHeaderId(
										timeAndMoneyDocumentNumber);
						if (timeAndMoneyDocument != null
								&& timeAndMoneyDocument.getDocumentHeader()
										.hasWorkflowDocument()
								&& !timeAndMoneyDocument.getDocumentHeader()
										.getWorkflowDocument().isFinal()) {
							ErrorMessageKeys.TIME_MONEY_NOT_FINAL
									.populate(validationResults);
						}
					}
				} catch (WorkflowException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		// only proceed with full validation if no validation errors were raised
		// in previous step
		if (validationResults.isEmpty()) {
			validateAward(validationResults, transmission.getAward(), true);
			for (Award childAward : transmission.getChildAwards()) {
				validateAward(validationResults, childAward, false);
			}
		}

		return validationResults;
	}

	/**
	 * Find the previous transmission data
	 *
	 * @param rootAward
	 *            - parent award object
	 * @param getLastSuccesful
	 *            retuns whether there was a successful transmission or not
	 * @return last transmission object
	 */
	private AwardTransmission getLatestAwardTransmission(Award rootAward,
			boolean getLastSuccesful) {
		AwardTransmission latestTransmission = null;

		if (getLastSuccesful) {
			for (AwardTransmission awardTransmission : ((AwardExtension) rootAward
					.getExtension()).getAwardTransmissions()) {
				if (awardTransmission.isSuccess()) {
					latestTransmission = awardTransmission;
				}
			}
		} else {
			if (((AwardExtension) rootAward.getExtension())
					.getAwardTransmissions() != null
					&& ((AwardExtension) rootAward.getExtension())
							.getAwardTransmissions().size() > 0)
				latestTransmission = ((AwardExtension) rootAward.getExtension())
						.getAwardTransmissions().get(
								((AwardExtension) rootAward.getExtension())
										.getAwardTransmissions().size() - 1);
		}

		return latestTransmission;
	}

	// BU Customization ID: N/A mkousheh 20120505 - Return the latest award
	// child that was successfully transmitted
	private AwardTransmissionChild getLatestChildAwardTransmitted(
			Award rootAward, String awardNumber, boolean getLastSuccesful) {
		AwardTransmission latestTransmissionWithMatchChild = null;

		List<AwardTransmission> awardTransmissionReversed = ((AwardExtension) rootAward
				.getExtension()).getAwardTransmissions();

		// reverse award transmission list
		Collections.sort(awardTransmissionReversed,
				new Comparator<AwardTransmission>() {
					public int compare(AwardTransmission awardTransmission1,
							AwardTransmission awardTransmission2) {
						return ((Long) awardTransmission1.getTransmissionId())
								.compareTo((Long) awardTransmission2
										.getTransmissionId());
					}
				});
		Collections.reverse(awardTransmissionReversed);

		if (getLastSuccesful) {
			for (AwardTransmission awardTransmission : (List<AwardTransmission>) awardTransmissionReversed) {
				// for (AwardTransmission awardTransmission :
				// rootAward.getAwardTransmissions()) {

				if (awardTransmission.isSuccess()) {
					for (AwardTransmissionChild awardTransmissionChild : awardTransmission
							.getTransmissionChildren()) {
						if (awardNumber.equals(awardTransmissionChild
								.getAwardNumber())) {
							return awardTransmissionChild;
						}
					}
				}
			}
		} else {
			if (((AwardExtension) rootAward.getExtension())
					.getAwardTransmissions() != null
					&& ((AwardExtension) rootAward.getExtension())
							.getAwardTransmissions().size() > 0) {
				// latestTransmission =
				// rootAward.getAwardTransmissions().get(rootAward.getAwardTransmissions().size()
				// - 1);
				for (AwardTransmissionChild awardTransmissionChild : ((AwardExtension) rootAward
						.getExtension())
						.getAwardTransmissions()
						.get(((AwardExtension) rootAward.getExtension())
								.getAwardTransmissions().size() - 1)
						.getTransmissionChildren()) {
					if (awardNumber.equals(awardTransmissionChild
							.getAwardNumber())) {
						return awardTransmissionChild;
					}
				}
			}
		}
		return null;
	}

	private void checkPrimaryAwardForChanges(Award award,
			ValidationResults validationResults) {
		AwardTransmission lastTransmission = getLatestAwardTransmission(award,
				true);

		if (award != null && lastTransmission != null
				&& lastTransmission.isSuccess()) {
			if (award.getAccountTypeCode() != null
					&& lastTransmission.getAccountTypeCode() != null
					&& award.getAccountTypeCode().compareTo(
							lastTransmission.getAccountTypeCode()) != 0) {
				ErrorMessageKeys.PARENT_ACCOUNT_TYPE_CANNOT_CHANGE.populate(
						award.getAwardId(), validationResults, award
								.getAwardNumber(), lastTransmission
								.getAccountTypeCode().toString(), award
								.getAccountTypeCode().toString());
			}

			// BU Customization ID: N/A mkousheh 20121130 - Issue 82 only
			// prevent changing Payment
			// Basis Cost reimbursement (Resource Related Billing) (1) to
			// Milestone (4) or vice-versa
			if ((RRB_BASIS_OF_PAYMENT.equals(award.getBasisOfPaymentCode()) && MILESTONE_BASIS_OF_PAYMENT
					.equals(lastTransmission.getBasisOfPaymentCode()))
					|| (MILESTONE_BASIS_OF_PAYMENT.equals(award
							.getBasisOfPaymentCode()) && RRB_BASIS_OF_PAYMENT
							.equals(lastTransmission.getBasisOfPaymentCode()))) {
				ErrorMessageKeys.PARENT_PAYMENT_BASIS_CANNOT_CHANGE.populate(
						award.getAwardId(), validationResults,
						award.getAwardNumber(),
						lastTransmission.getBasisOfPaymentCode(),
						award.getBasisOfPaymentCode());
			}

			/*
			 * if (!MANUAL_BASIS_OF_PAYMENT.equalsIgnoreCase(lastTransmission.
			 * getBasisOfPaymentCode()) &&
			 * !StringUtils.equals(award.getBasisOfPaymentCode(),
			 * lastTransmission.getBasisOfPaymentCode())) {
			 * ErrorMessageKeys.PARENT_PAYMENT_BASIS_CANNOT_CHANGE
			 * .populate(award.getAwardId(), validationResults,
			 * award.getAwardNumber(), lastTransmission.getBasisOfPaymentCode(),
			 * award.getBasisOfPaymentCode()); }
			 */

			if (DHHS_LOC_MATHOD_OF_PAYMENT.equalsIgnoreCase(lastTransmission
					.getMethodOfPaymentCode())
					&& !StringUtils.equals(award.getMethodOfPaymentCode(),
							lastTransmission.getMethodOfPaymentCode())) {
				ErrorMessageKeys.PARENT_PAYMENT_METHOD_CANNOT_CHANGE.populate(
						award.getAwardId(), validationResults,
						award.getAwardNumber(),
						lastTransmission.getMethodOfPaymentCode(),
						award.getMethodOfPaymentCode());
			}
			if (!StringUtils.equals(award.getSponsorCode(),
					lastTransmission.getSponsorCode())) {
				ErrorMessageKeys.PARENT_SPONSOR_CODE_CANNOT_CHANGE.populate(
						award.getAwardId(), validationResults,
						award.getAwardNumber(),
						lastTransmission.getSponsorCode(),
						award.getSponsorCode());
			}
		}
	}

	private AwardTransmissionChild findMatchingTransmissionChild(
			List<AwardTransmissionChild> transmissionChildren,
			String awardNumber) {
		for (AwardTransmissionChild awardTransmissionChild : transmissionChildren) {
			if (awardNumber.equals(awardTransmissionChild.getAwardNumber())) {
				return awardTransmissionChild;
			}
		}

		return null;
	}

	private void checkChildAwardForChanges(Award primaryAward, Award award,
			ValidationResults validationResults) {
		// BU Customization ID: N/A mkousheh 20120504 - Return the latest award
		// child that was successfully transmitted
		AwardTransmissionChild lastTransmittedChild = getLatestChildAwardTransmitted(
				primaryAward, award.getAwardNumber(), true);
		if (award != null && lastTransmittedChild != null) {
			if (!"group".equalsIgnoreCase(((AwardExtension) award
					.getExtension()).getChildType())
					&& !StringUtils.equals(((AwardExtension) award
							.getExtension()).getChildType(),
							lastTransmittedChild.getChildType())) {
				ErrorMessageKeys.CHILD_CHILD_TYPE_CANNOT_CHANGE.populate(
						award.getAwardId(), validationResults,
						award.getAwardNumber(),
						lastTransmittedChild.getChildType(),
						((AwardExtension) award.getExtension()).getChildType());
			}
			if (!StringUtils.equals(award.getLeadUnitNumber(),
					lastTransmittedChild.getLeadUnitNumber())) {
				ErrorMessageKeys.CHILD_LEAD_UNIT_CANNOT_CHANGE.populate(
						award.getAwardId(), validationResults,
						award.getAwardNumber(),
						lastTransmittedChild.getLeadUnitNumber(),
						award.getLeadUnitNumber());
			}
		}
	}

	private void validateAward(ValidationResults results, Award award,
			boolean parentAward) {
		// BU Customization ID: N/A mkousheh N/A - RSN:8311755 (remove
		// validation)
		// BUKC-0028: Enable validation to identify child has no budget
		validateChildHasBudget(results, award, parentAward);

		validateAwardIsFinal(results, award);
		validateAwardDFAFSNumber(results, award);
		validateChildAwardType(results, award, parentAward);
		validateAccountType(results, award);
		validateDollarAmmount(results, award);

		// BU Customization ID: N/A mkousheh N/A - RSN:8311755
		// validateLeadUnit(results, award);

		validateProjectDates(results, award);

		// BUKC-0127: Remove validation for cost-share on child award
		// (ENHC0012984)
		// validateCostSharingOnChildAward(results, award, parentAward);

		validateGroupAwardHasChildren(results, award);

		// BU Customization ID: N/A mkousheh N/A - RSN:8311717
		// validateFederalAward(results, award);

		validateNIHAward(results, award);
		validateDollarObligation(results, award);
		// validatePrimeSponsorOnSubAward(results, award);
	}

	private void validateAwardIsFinal(ValidationResults results, Award award) {
		AwardDocument awardDocument;
		VersionHistoryService vhs = (VersionHistoryService) KcServiceLocator
				.getService(VersionHistoryService.class);
		try {
			VersionHistory versionHistory = vhs.findPendingVersion(Award.class,
					award.getAwardNumber(),
					new Integer(award.getSequenceNumber() + 1).toString());
			if (versionHistory != null) {
				Award activeAward = (Award) versionHistory.getSequenceOwner();
				if (activeAward != null) {
					awardDocument = (AwardDocument) getDocumentService()
							.getByDocumentHeaderId(
									activeAward.getAwardDocument()
											.getDocumentNumber());
					if (awardDocument.getDocumentHeader().hasWorkflowDocument()) {
						if (!awardDocument.getDocumentHeader()
								.getWorkflowDocument().isFinal()) {
							ErrorMessageKeys.AWARD_NOT_FINAL.populate(
									award.getAwardId(), results,
									activeAward.getAwardNumber());
						}
					} else {
						ErrorMessageKeys.AWARD_DOCUMENT_NULL.populate(
								award.getAwardId(), results,
								activeAward.getAwardNumber());
					}
				} else {
					// show error that there is no active award?
					ErrorMessageKeys.AWARD_NOT_FINAL.populate(
							award.getAwardId(), results,
							activeAward.getAwardNumber());
				}
			} else if (award.getAwardDocument().getDocumentHeader()
					.hasWorkflowDocument()
					&& !award.getAwardDocument().getDocumentHeader()
							.getWorkflowDocument().isFinal()) {
				ErrorMessageKeys.AWARD_NOT_FINAL.populate(award.getAwardId(),
						results, award.getAwardNumber());
			}
		} catch (WorkflowException e) {
			e.printStackTrace();
		}
	}

	private void validateAwardDFAFSNumber(ValidationResults results, Award award) {
		/*
		 * Integer methodOfPaymentCode = 0; try { methodOfPaymentCode =
		 * Integer.parseInt(award.getMethodOfPaymentCode()); } finally { // do
		 * nothing }
		 */
		String dfafsNumber = award.getDocumentFundingId();

		// BUKC-0041: Parameterize LOC (Line of Credit) method of payment to
		// accommodate the new LOC codes added (Issue 62)
		if (isLocMethodOfPayment(award.getMethodOfPaymentCode())
				&& StringUtils.isBlank(dfafsNumber)) {
			// if (methodOfPaymentCode.compareTo(27) <= 0 &&
			// StringUtils.isBlank(dfafsNumber)) {
			ErrorMessageKeys.AWARD_DOCUMENT_NO_DOCUMENT_NUMBER.populate(
					award.getAwardId(), results, award.getAwardNumber());
		}
	}

	private boolean isLocMethodOfPayment(String methodOfPaymentCode) {
		String locMethodOfPaymentList = parameterService
				.getParameterValueAsString(AwardDocument.class,
						BUConstants.BU_LOC_METHOD_OF_PAYMENT_CD);
		String[] termIds = locMethodOfPaymentList.split(",");

		return Arrays.asList(termIds).contains(methodOfPaymentCode);
	}

	/**
	 * Child awards must have a budget.
	 */
	private void validateChildHasBudget(ValidationResults results, Award award,
			boolean parentAward) {
		if (!parentAward) {
			AwardBudgetExt budget = award.getAwardDocument()
					.getBudgetVersionOverview();
			if (budget.getBudgetId() == null) {
				ErrorMessageKeys.NO_BUDGET_ON_CHILD_AWARD.populate(
						award.getAwardId(), results, award.getAwardNumber());
			}
		}
	}

	private void validateChildAwardType(ValidationResults results, Award award,
			boolean isParentAward) {
		if (!isParentAward
				&& StringUtils.isBlank(((AwardExtension) award.getExtension())
						.getChildType())) {
			ErrorMessageKeys.NO_CHILD_TYPE_ON_CHILD_AWARD.populate(
					award.getAwardId(), results, award.getAwardNumber());
		}
	}

	/**
	 * Validates the following:
	 * <p/>
	 * <ol>
	 * <li>Account Type is NULL; Account Type should always be populated</li>
	 * <li>Account Type is Federal, and a non-Federal Sponsor is attached;
	 * Federal Account Types should only have Federal Sponsors.</li>
	 * <li>Account Type is Non-Federal, and a Federal Sponsor is attached;
	 * Federal Account Types should only have Federal Sponsors.</li>
	 * </ol>
	 */
	private void validateAccountType(ValidationResults results, Award award) {
		if (award.getAccountTypeCode() == null) {
			ErrorMessageKeys.ACCOUNT_TYPE_NULL.populate(award.getAwardId(),
					results, award.getAwardNumber());
		} else {
			if (isFederalAccount(award) && isNonFederalSponsor(award)) {
				ErrorMessageKeys.FEDERAL_ACCOUNT_NON_FEDERAL_SPONSOR.populate(
						award.getAwardId(), results, award.getAwardNumber());
			} else if (isNonFederalAccount(award) && isFederalSponsor(award)) {
				ErrorMessageKeys.NON_FEDERAL_ACCOUNT_FEDERAL_SPONSOR.populate(
						award.getAwardId(), results, award.getAwardNumber());
			}
		}
	}

	private boolean isFederalAccount(Award award) {
		return award.getAccountTypeCode().equals(FEDERAL_CODE);
	}

	// BUKC-0035: Fix an issue with the code to compare the same data type for
	// Federal/NonFederal issue
	private boolean isFederalSponsor(Award award) {
		// BUKC-0064: If Prime Sponsor present, validate against it rather that
		// the Sponsor
		if (!(award.getPrimeSponsor() == null)) {
			return Integer.parseInt(award.getPrimeSponsor()
					.getSponsorTypeCode()) == FEDERAL_CODE;
		} else {
			return Integer.parseInt(award.getSponsor().getSponsorTypeCode()) == FEDERAL_CODE;
		}
	}

	private boolean isNonFederalAccount(Award award) {
		return award.getAccountTypeCode().equals(NON_FEDERAL_CODE);
	}

	private boolean isNonFederalSponsor(Award award) {
		// BUKC-0029: Fix an issue with Acount Type validation
		// (Federal/Non-Federal validation)
		// return true if Sposor is (non) federal - all should be consider non
		// federal if type <> 1
		// return
		// award.getSponsor().getSponsorTypeCode().equals(NON_FEDERAL_CODE);
		return (!isFederalSponsor(award));
	}

	/**
	 * Validates if Dollar Amounts Appear on an Award Record that has Children;
	 * Dollar amounts can only appear on child awards (that have no children of
	 * their own).
	 */
	private void validateDollarAmmount(ValidationResults results, Award award) {
		AwardHierarchy awardHierarchy = award.getAwardHierarchyService()
				.loadAwardHierarchy(award.getAwardNumber());
		if (awardHierarchy == null) {
			ErrorMessageKeys.AWARD_HIERARCHY_NULL.populate(award.getAwardId(),
					results, award.getAwardNumber());

		}

		// BU Customization ID: N/A mkousheh N/A - Based on Eddie's request
		/*
		 * } else if (awardHierarchy.hasChildren() &&
		 * award.calculateObligatedDistributedAmountTotal
		 * ().isGreaterThan(KualiDecimal.ZERO)) {
		 * ErrorMessageKeys.AWARD_DOLLAR_AMOUNT_ON_CHILD
		 * .populate(award.getAwardId(), results, award.getAwardNumber()); }
		 */
	}

	/**
	 * Determines if the Lead Unit on the Award is invalid; The Lead Unit can
	 * only be populated with a Child Node in the Unit Hierarchy.
	 */
	private void validateLeadUnit(ValidationResults results, Award award) {
		List<Unit> subUnits = unitService
				.getSubUnits(award.getLeadUnitNumber());
		if (subUnits != null && !subUnits.isEmpty()) {
			ErrorMessageKeys.INVALID_LEAD_UNIT.populate(award.getAwardId(),
					results, award.getAwardNumber());
		}
	}

	/**
	 * Validates the following:
	 * <p/>
	 * <ol>
	 * <li>Sub-Award does not have a Prime Sponsor Listed; When the Award Type
	 * is listed as a Sub-award it needs to have a Prime Sponsor attached</li>
	 * - *
	 * <li>Multiple Subawards are recorded on a Single Award; Only one subaward
	 * should be listed on an award.</li>
	 * <li>Subawards are recorded on a Parent Award; Subawards can only be
	 * listed on child awards.</li>
	 * </ol>
	 */
	private void validateSubAwards(ValidationResults results, Award award,
			boolean parentAward) {

		if (parentAward && isSubAward(award)
				&& StringUtils.isBlank(award.getPrimeSponsorCode())) {
			ErrorMessageKeys.SUB_AWARD_NO_PRIME_SPONSOR.populate(
					award.getAwardId(), results, award.getAwardNumber());
		}

		// BU Customization ID: N/A mkousheh N/A - Per RSN:8311717
		// List<AwardApprovedSubaward> subAwards =
		// award.getAwardApprovedSubawards();
		// if (subAwards != null && subAwards.size() > 1) {
		// ErrorMessageKeys.MULTIPLE_SUBAWARDS.populate(award.getAwardId(),
		// results, award.getAwardNumber());
		// }
		// if (parentAward && subAwards != null && !subAwards.isEmpty()) {
		// ErrorMessageKeys.SUBAWARD_ON_PARENT.populate(award.getAwardId(),
		// results, award.getAwardNumber());
		// }

	}

	/**
	 * Determines if the given Award is a Sub-award. This is based on the
	 * description of the AwardType on the Award.
	 *
	 * @param award
	 *            the Award to check to determine if it is a sub-award or not
	 * @return true if the given award is a sub-award, false if not
	 */
	private boolean isSubAward(Award award) {
		return AWARD_TYPE_CODE_SUBAWARD.equals(award.getAwardTypeCode());
	}

	/**
	 * Validates the following:
	 * <p/>
	 * <ol>
	 * <li>Obligation End Date is greater than Project End Date; Obligation End
	 * Date should always be equal to or less than the Project End Date.</li>
	 * </ol>
	 */
	private void validateProjectDates(ValidationResults results, Award award) {

		AwardHierarchyNode awardHierarchyNode = getAwardHierarchyNode(award);

		java.util.Date projectStartDate = zeroOutTime(award
				.getAwardEffectiveDate());
		java.util.Date projectEndDate = zeroOutTime(award.getProjectEndDate());
		java.util.Date obligationStartDate = zeroOutTime(awardHierarchyNode
				.getCurrentFundEffectiveDate());
		java.util.Date obligationEndDate = zeroOutTime(award
				.getObligationExpirationDate());

		if (projectStartDate == null) {
			ErrorMessageKeys.PROJECT_START_DATE_NULL.populate(
					award.getAwardId(), results, award.getAwardNumber());
		}

		if (obligationStartDate == null) {
			ErrorMessageKeys.OBLIGATION_START_DATE_NULL.populate(
					award.getAwardId(), results, award.getAwardNumber());
		}

		if (projectEndDate == null) {
			ErrorMessageKeys.PROJECT_START_END_NULL.populate(
					award.getAwardId(), results, award.getAwardNumber());
		}

		if (obligationEndDate == null) {
			ErrorMessageKeys.OBLIGATION_END_DATE_NULL.populate(
					award.getAwardId(), results, award.getAwardNumber());
		}

	}

	/**
	 * Performs the (somewhat convoluted) logic to extract an AwardHierarchyNode
	 * from an Award. This code was largely reverse engineered by looking at
	 * {@link AwardHierarchyUIServiceImpl}.
	 *
	 * @param award
	 *            the award to extract the AwardHierarchyNode from
	 * @return the award hierarchy node for the given award
	 */
	private AwardHierarchyNode getAwardHierarchyNode(Award award) {
		Map<String, AwardHierarchyNode> awardHierarchyNodes = new HashMap<String, AwardHierarchyNode>();
		Map<String, AwardHierarchy> awardHierarchyItems = getAwardHierarchyService()
				.getAwardHierarchy(award.getAwardNumber(),
						new ArrayList<String>());
		getAwardHierarchyService().populateAwardHierarchyNodes(
				awardHierarchyItems, awardHierarchyNodes,
				award.getAwardNumber(), award.getSequenceNumber().toString());
		return awardHierarchyNodes.get(award.getAwardNumber());
	}

	/**
	 * Takes the given Date and returns a copy of the Date which has all of the
	 * time portions of the date (hours, minutes, seconds, etc.) set to zero.
	 *
	 * @param givenDate
	 *            the date to zero out
	 * @return a copy of the given date with the temporal components zeroed out
	 */
	private java.util.Date zeroOutTime(java.util.Date givenDate) {
		if (givenDate != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(givenDate);
			calendar.set(Calendar.HOUR_OF_DAY, 0);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 0);
			calendar.set(Calendar.MILLISECOND, 0);
			return calendar.getTime();
		} else {
			return null;
		}
	}

	/**
	 * Validates if Cost Sharing Appears on a Child Award; Cost Sharing can only
	 * be represented at the Parent..
	 *
	 * @param results
	 * @param award
	 */
	private void validateCostSharingOnChildAward(ValidationResults results,
			Award award, boolean parentAward) {
		if (!parentAward && isCostSharing(award)) {
			ErrorMessageKeys.COST_SHARING_ON_CHILD_AWARD.populate(
					award.getAwardId(), results, award.getAwardNumber());
		}
	}

	private void validateGroupAwardHasChildren(ValidationResults results,
			Award award) {
		AwardHierarchy hierarchy = getAwardHierarchyService()
				.loadAwardHierarchy(award.getAwardNumber());

		if ("Group".equalsIgnoreCase(((AwardExtension) award.getExtension())
				.getChildType()) && !hierarchy.hasChildren()) {
			ErrorMessageKeys.GROUP_AWARD_HAS_NO_CHILDREN.populate(
					award.getAwardId(), results, award.getAwardNumber());
		}
	}

	/**
	 * Validates if Federal Award does not have a CFDA Number; When the Account
	 * Type is Federal the CFDA Number is required.
	 *
	 * @param results
	 * @param award
	 */
	private void validateFederalAward(ValidationResults results, Award award) {
		if (isFederalAccount(award)
				&& StringUtils.isBlank(award.getCfdaNumber())) {
			ErrorMessageKeys.CFDA_NUMBER_REQUIRED_FEDERAL_ACCOUNT.populate(
					award.getAwardId(), results, award.getAwardNumber());
		}
	}

	/**
	 * Validates if NIH Award does not have a Document Number; When an NIH
	 * sponsor is listed the Document Number should not be Null.
	 *
	 * @param results
	 * @param award
	 */
	private void validateNIHAward(ValidationResults results, Award award) {

		boolean isNih = award.getSponsorName().startsWith("HHS/NIH");
		if (isNih && StringUtils.isBlank(award.getDocumentFundingId())) {
			ErrorMessageKeys.NIH_AWARD_NO_DOCUMENT_NUMBER.populate(
					award.getAwardId(), results, award.getAwardNumber());
		}

	}

	/**
	 * This is a validation that was originally spec'd. This Validation should
	 * trigger if Award.Award_Type = 6 and Award.Prime_Sponsor_Code is NULL
	 *
	 * @param results
	 * @param award
	 */
	private void validatePrimeSponsorOnSubAward(ValidationResults results,
			Award award) {
		if (AWARD_TYPE_CODE_SUBAWARD == award.getAwardTypeCode()
				&& StringUtils.isBlank(award.getPrimeSponsorCode())) {
			ErrorMessageKeys.SUB_AWARD_WITH_NO_PRIME_SPONSOR.populate(
					award.getAwardId(), results, award.getAwardNumber());
		}
	}

	/**
	 * This is a validation that was originally spec'd. This validation should
	 * trigger if the parent award has an amount > 0 the
	 * Award_Amount_info.Obli_Distributable_amount
	 *
	 * @param results
	 * @param award
	 */
	private void validateDollarObligation(ValidationResults results, Award award) {
		AwardAmountInfo awardAmountInfo = getAwardAmountInfoService()
				.fetchLastAwardAmountInfoForAwardVersionAndFinalizedTandMDocumentNumber(
						award);
		AwardHierarchy hierarchy = getAwardHierarchyService()
				.loadAwardHierarchy(award.getAwardNumber());
		if (hierarchy.hasChildren()
				&& awardAmountInfo.getObliDistributableAmount().isPositive()) {
			// ErrorMessageKeys.DOLLARS_OBLIGATED_ON_AWARD_WITH_NO_CHILDREN.populate(award.getAwardId(),
			// results, award.getAwardNumber());
			LOG.error("Transmision Error: DOLLARS_OBLIGATED_ON_AWARD_WITH_NO_CHILDREN");
		}
	}

	/**
	 * An enumeration which defines all of the error messages that can be
	 * produced by validation of an SAP award transmission.
	 */
	private static enum ErrorMessageKeys {

		AWARD_DOCUMENT_NULL("error.award.sapintegration.awardDocumentNull"), AWARD_NOT_FINAL(
				"error.award.sapintegration.awardMustBeFinal"), TIME_MONEY_NOT_FINAL(
				"error.award.sapintegration.timeAndMoneyDocumentMustBeFinal"), NO_PARENT_ACCOUNT_TYPE(
				"error.award.sapintegration.noParentAccountType"), NO_PARENT_AWARD(
				"error.award.sapintegration.noParent"), NO_CHILDREN_AWARD(
				"error.award.sapintegration.noChildren"), NO_CHILD_TYPE_ON_CHILD_AWARD(
				"error.award.sapintegration.noChildType"), NO_BUDGET_ON_CHILD_AWARD(
				"error.award.sapintegration.noBudgetOnChild"), ACCOUNT_TYPE_NULL(
				"error.award.sapintegration.nullAccountType"), AWARD_HIERARCHY_NULL(
				"error.award.sapintegration.nullHierarchy"), AWARD_DOLLAR_AMOUNT_ON_CHILD(
				"error.award.sapintegration.dollarAmountOnChild"), FEDERAL_ACCOUNT_NON_FEDERAL_SPONSOR(
				"error.award.sapintegration.federalAccount.nonFederalSponsor"), NON_FEDERAL_ACCOUNT_FEDERAL_SPONSOR(
				"error.award.sapintegration.nonFederalAccount.federalSponsor"), MULTIPLE_SUBAWARDS(
				"error.award.sapintegration.subaward.multiple"), SUBAWARD_ON_PARENT(
				"error.award.sapintegration.subawardOnParent"), SUB_AWARD_NO_PRIME_SPONSOR(
				"error.award.sapintegration.subaward.noPrimeSponsor"), INVALID_LEAD_UNIT(
				"error.award.sapintegration.invalid.leadUnit"), PROJECT_START_DATE_NULL(
				"error.award.sapintegration.projectStartDate.required"), PROJECT_START_END_NULL(
				"error.award.sapintegration.projectEndDate.required"), OBLIGATION_START_DATE_NULL(
				"error.award.sapintegration.obligationStartDate.required"), OBLIGATION_END_DATE_NULL(
				"error.award.sapintegration.obligationEndDate.required"), OBLIGATION_END_DATE_AFTER_PROJECT_END_DATE(
				"error.award.sapintegration.obligationEndDate.after.projectEndDate"), COST_SHARING_ON_CHILD_AWARD(
				"error.award.sapintegration.costSharing.onChildAward"), CFDA_NUMBER_REQUIRED_FEDERAL_ACCOUNT(
				"error.award.sapintegration.cfdaNumber.requiredOn.federalAccount"), NIH_AWARD_NO_DOCUMENT_NUMBER(
				"error.award.sapintegration.nihAward.noDocumentNumber"), DOLLARS_OBLIGATED_ON_AWARD_WITH_NO_CHILDREN(
				"error.award.sapintegration.dollarsObligatedOnAwardWithChildren"), SUB_AWARD_WITH_NO_PRIME_SPONSOR(
				"error.award.sapintegration.subAwardWithNoPrimeSponsor"), PARENT_ACCOUNT_TYPE_CANNOT_CHANGE(
				"error.award.sapintegration.parentAward.accountTypeChanged"), PARENT_SPONSOR_CODE_CANNOT_CHANGE(
				"error.award.sapintegration.parentAward.sponsorCodeChanged"), PARENT_PAYMENT_METHOD_CANNOT_CHANGE(
				"error.award.sapintegration.parentAward.paymentMethodChanged"), PARENT_PAYMENT_BASIS_CANNOT_CHANGE(
				"error.award.sapintegration.parentAward.paymentBasisChanged"), CHILD_CHILD_TYPE_CANNOT_CHANGE(
				"error.award.sapintegration.childAward.childTypeChanged"), CHILD_LEAD_UNIT_CANNOT_CHANGE(
				"error.award.sapintegration.childAward.leadUnitChanged"), GROUP_AWARD_HAS_NO_CHILDREN(
				"error.award.sapintegration.groupAward.hasNoChildren"), AWARD_DOCUMENT_NO_DOCUMENT_NUMBER(
				"error.award.sapintegration.awardDocument.noDocumentNumber"), PARENT_TRANSACTION_TYPE_NOCOST_EXT_OR_ADMIN_CHANGE(
				"error.award.sapintegration.parentAward.transactionType.noCostExt_Or_administrativeChange");

		private String errorMessageKey;

		private ErrorMessageKeys(String errorMessageKey) {
			this.errorMessageKey = errorMessageKey;
		}

		// private void populate(Long awardId, ValidationResults
		// validationResults, String parentAwardTransactionType, String
		// childAwardNumber) {
		// ValidationError validationError = new
		// ValidationError(errorMessageKey,
		// Arrays.asList(parentAwardTransactionType, childAwardNumber));
		// validationResults.addAwardValidationError(awardId, validationError);
		// }

		// private void populate(Long awardId, ValidationResults
		// validationResults, String awardNumber, String oldValue, String
		// newValue) {
		// ValidationError validationError = new
		// ValidationError(errorMessageKey, Arrays.asList(awardNumber, oldValue,
		// newValue));
		// validationResults.addAwardValidationError(awardId, validationError);
		// }

		private void populate(Long awardId,
				ValidationResults validationResults, String param1,
				String param2, String param3) {
			ValidationError validationError = new ValidationError(
					errorMessageKey, Arrays.asList(param1, param2, param3));
			validationResults.addAwardValidationError(awardId, validationError);
		}

		private void populate(Long awardId,
				ValidationResults validationResults, String awardNumber) {
			ValidationError validationError = new ValidationError(
					errorMessageKey, new ArrayList<String>(
							Arrays.asList(awardNumber)));
			validationResults.addAwardValidationError(awardId, validationError);
		}

		private void populate(ValidationResults validationResults) {
			ValidationError validationError = new ValidationError(
					errorMessageKey, new ArrayList<String>());
			validationResults.addGlobalValidationError(validationError);
		}

	}

	private String dateToString(Date date) {
		if (date == null) {
			return null;
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT_PATTERN);
		return dateFormat.format(date);
	}

	/**
	 * Maps the given KC sponsor's account type code to SAP grant type code as
	 * per "Mapping Table M1" in the specification.
	 */
	private String convertAccountTypeToGrantType(int accountType, Award award) {
		String returnValue = getParameterService()
				.getSubParameterValueAsString(AwardDocument.class,
						BUConstants.ACCOUNT_TYPE_MAPPING,
						Integer.toString(accountType));
		if (StringUtils.isEmpty(returnValue)) {
			throw new IllegalArgumentException(
					"Failed to convert account type, given account type value was not understood: "
							+ accountType + " awardNumber was: "
							+ award.getAwardNumber() + "\n" + award.toString());
		}
		return returnValue;
	}

	/**
	 * Defines the mapping from KC account types to SAP fringe codes.
	 */
	private static enum FringeCodeMapping {
		FEDERAL("FD"), NON_FEDERAL("NF"), FRINGE_NOT_ALLOWED("NA");

		private final String code;

		private FringeCodeMapping(String code) {
			this.code = code;
		}

		private static String mapToSapFringeCode(boolean isFringeNotAllowed,
				Integer accountTypeCode) {
			if (isFringeNotAllowed) {
				return FRINGE_NOT_ALLOWED.code;
			} else if (FEDERAL_CODE.equals(accountTypeCode)) {
				return FEDERAL.code;
			} else if (NON_FEDERAL_CODE.equals(accountTypeCode)) {
				return NON_FEDERAL.code;
			}
			return null;
		}
	}

	/**
	 * Defines the mapping from KC activity types to SAP functional areas as per
	 * "Mapping Table M7" in the specification.
	 */
	private String convertActivityTypeToFunctionalArea(String activityType,
			Award award) {
		String returnValue = getParameterService()
				.getSubParameterValueAsString(AwardDocument.class,
						BUConstants.ACTIVITY_TYPE_MAPPING, activityType);
		if (StringUtils.isEmpty(returnValue)) {
			throw new IllegalArgumentException(
					"Failed to convert activity type, given activity type value was not understood: "
							+ activityType
							+ " awardNumber was: "
							+ award.getAwardNumber() + "\n" + award.toString());
		}
		return returnValue;
	}

	/**
	 * Defines the mapping from KC Basis of Payment code to SAP Billing Rule as
	 * per "Mapping Table M3" in the specification.
	 */
	private String convertBasisOfPaymentToBillingRule(
			String basisOfPaymentCode, Award award) {
		String returnValue = getParameterService()
				.getSubParameterValueAsString(AwardDocument.class,
						BUConstants.BASIS_OF_PAYMENT_MAPPING,
						basisOfPaymentCode);
		if (StringUtils.isEmpty(returnValue)) {
			throw new IllegalArgumentException(
					"Failed to convert basis of payment code, given basis of payment value was not understood: "
							+ basisOfPaymentCode
							+ " awardNumber was: "
							+ award.getAwardNumber() + "\n" + award.toString());
		}
		return returnValue;
	}

	/**
	 * Defines the mapping from KC Method of Payment code to SAP Letter of
	 * Credit as per "Mapping Table M4" in the specification.
	 */
	private String convertMethodOfPaymentToLetterOfCredit(
			String methodOfPaymentCode, Award award) {
		String returnValue = getParameterService()
				.getSubParameterValueAsString(AwardDocument.class,
						BUConstants.METHOD_OF_PAYMENT_MAPPING,
						methodOfPaymentCode);
		if (StringUtils.isBlank(returnValue)) {
			returnValue = null;
		}
		return returnValue;
	}

	/**
	 * Defines the mapping from KC project role to SAP responsibility as per
	 * "Mapping Table M6" in the specification.
	 */
	private String convertProjectRoleToResponsibility(String projectRole,
			Award award) {
		String returnValue = getParameterService()
				.getSubParameterValueAsString(AwardDocument.class,
						BUConstants.PROJECT_ROLE_MAPPING, projectRole);
		if (StringUtils.isEmpty(returnValue)) {
			throw new IllegalArgumentException(
					"Failed to convert project role, given project role value was not understood: "
							+ projectRole + " awardNumber was: "
							+ award.getAwardNumber() + "\n" + award.toString());
		}
		return returnValue;
	}

	/**
	 * Defines the mapping from KC status code to SAP status code as per
	 * "Mapping Table M5" in the specification.
	 */
	private String convertStatusCodeToResponsibility(int statusCode, Award award) {
		String returnValue = getParameterService()
				.getSubParameterValueAsString(AwardDocument.class,
						BUConstants.STATUS_CODE_MAPPING,
						Integer.toString(statusCode));
		if (StringUtils.isEmpty(returnValue)) {
			throw new IllegalArgumentException(
					"Failed to convert status code, given status code value was not understood: "
							+ statusCode + " awardNumber was: "
							+ award.getAwardNumber() + "\n" + award.toString());
		}
		return returnValue;
	}

	/**
	 * Defines the mapping from KC sponsor type codes to SAP sponsor type codes.
	 */
	private String convertSponsorCodeToSponsorType(String sponsorCode,
			Award award) {
		String returnValue = getParameterService()
				.getSubParameterValueAsString(AwardDocument.class,
						BUConstants.SPONSOR_CODE_MAPPING, sponsorCode);
		if (StringUtils.isEmpty(returnValue)) {
			throw new IllegalArgumentException(
					"Failed to convert sponsor code, given sponsor code value was not understood: "
							+ sponsorCode + " awardNumber was: "
							+ award.getAwardNumber() + "\n" + award.toString());
		}
		return returnValue;
	}

	/**
	 * Defines the mapping from Sponsor Terms in KCRM to Sponsor Class in SAP as
	 * per functional specification mapping table M8.
	 */
	private String convertSponsorTermToSponsorClass(long sponsorTerm,
			Award award) {
		String returnValue = getParameterService()
				.getSubParameterValueAsString(AwardDocument.class,
						BUConstants.SPONSOR_TERM_MAPPING,
						Long.toString(sponsorTerm));
		if (StringUtils.isEmpty(returnValue)) {
			LOG.info("[awardId: "
					+ award.getAwardId()
					+ ", awardNumber: "
					+ award.getAwardNumber()
					+ "] - "
					+ "Failed to convert sponsor term, given sponsor term value was not understood: "
					+ sponsorTerm);
		}
		return returnValue;
	}

	private String convertLeadUnitToBusinessArea(String leadUnitNumber,
			Award award) {
		String firstDigit = StringUtils.left(leadUnitNumber, 1);
		String returnValue = getParameterService()
				.getSubParameterValueAsString(AwardDocument.class,
						BUConstants.LEAD_UNIT_MAPPING, firstDigit);
		if (StringUtils.isEmpty(returnValue)) {
			throw new IllegalArgumentException(
					"Failed to convert lead unit, given lead unit value was not understood: "
							+ leadUnitNumber + " awardNumber was: "
							+ award.getAwardNumber() + "\n" + award.toString());
		}
		return returnValue;
	}

	private void logAwardInfo(Award award, String message) {
		if (LOG.isInfoEnabled()) {
			LOG.info("[awardId: " + award.getAwardId() + ", awardNumber: "
					+ award.getAwardNumber() + "] - " + message);
		}
	}

	public void setAwardAmountInfoService(
			AwardAmountInfoService awardAmountInfoService) {
		this.awardAmountInfoService = awardAmountInfoService;
	}

	public void setUnitService(UnitService unitService) {
		this.unitService = unitService;
	}

	public void setAwardHierarchyService(
			AwardHierarchyService awardHierarchyService) {
		this.awardHierarchyService = awardHierarchyService;
	}

	public void setBusinessObjectService(
			BusinessObjectService businessObjectService) {
		this.businessObjectService = businessObjectService;
	}

	public void setBudgetRateAndBaseService(
			BudgetRateAndBaseService budgetRateAndBaseService) {
		this.budgetRateAndBaseService = budgetRateAndBaseService;
	}

	public void setRolodexService(RolodexService rolodexService) {
		this.rolodexService = rolodexService;
	}

	protected AwardAmountInfoService getAwardAmountInfoService() {
		return awardAmountInfoService;
	}

	protected UnitService getUnitService() {
		return unitService;
	}

	protected AwardHierarchyService getAwardHierarchyService() {
		return awardHierarchyService;
	}

	protected BusinessObjectService getBusinessObjectService() {
		return businessObjectService;
	}

	protected BudgetRateAndBaseService getBudgetRateAndBaseService() {
		return budgetRateAndBaseService;
	}

	protected RolodexService getRolodexService() {
		return rolodexService;
	}

	public DocumentService getDocumentService() {
		return documentService;
	}

	public void setDocumentService(DocumentService documentService) {
		this.documentService = documentService;
	}

	protected AwardBudgetService getAwardBudgetService() {
		return KcServiceLocator.getService(AwardBudgetService.class);
	}

	/**
	 * Based on the same method in the foundation's AwardBudgetService method to
	 * get the last budget version on the award
	 *
	 * @param award
	 *            on progress to be transmitted
	 * @return last budget version on award
	 */
	protected AwardBudgetExt getLastBudgetVersion(AwardDocument award) {
		List<AwardBudgetExt> awardBudgetDocumentVersions = award
				.getBudgetDocumentVersions();
		AwardBudgetExt budgetVersionOverview = null;
		int versionSize = awardBudgetDocumentVersions.size();
		if (versionSize > 0) {
			budgetVersionOverview = awardBudgetDocumentVersions
					.get(versionSize - 1);
		}
		return budgetVersionOverview;
	}
}

class AwardTransmissionComparator implements Comparator<AwardTransmission>,
		Serializable {

	private static final long serialVersionUID = 8230902362851330642L;

	public int compare(AwardTransmission awardTransmission1,
			AwardTransmission awardTransmission2) {
		return ((Long) awardTransmission1.getTransmissionId())
				.compareTo((Long) awardTransmission2.getTransmissionId());
	}
}
