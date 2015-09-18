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

package edu.ku.kuali.kra.award.printing;

import org.kuali.kra.timeandmoney.document.TimeAndMoneyDocument;

import java.util.List;

public class AwardPrintNotice extends
		org.kuali.kra.award.printing.AwardPrintNotice {

	private static final long serialVersionUID = 1L;
	private Boolean terms;
	private Boolean specialReview;
	private Boolean equipment;
	private Boolean foreignTravel;
	private Boolean subAward;
	private Boolean costShare;
	private Boolean faRates;
	private Boolean benefitsRates;
	private Boolean flowThru;
	private Boolean comments;
	private Boolean fundingSummary;
	private Boolean hierarchy;
	private Boolean technicalReports;
	private Boolean reports;
	private Boolean payment;
	private Boolean closeout;
	private Boolean sponsorContacts;
	private Boolean otherData;
	private Boolean keywords;
	private Boolean requireSignature;

	/*
	 * BUKC-0022: Add BU's Award Notification
	 */
	private Boolean timeAndMoney1;
	private Boolean timeAndMoney2;
	private Boolean timeAndMoney3;
	private Boolean timeAndMoney4;
	private Boolean timeAndMoney5;
	private String timeAndMoneyDocNumber1;
	private String timeAndMoneyDocNumber2;
	private String timeAndMoneyDocNumber3;
	private String timeAndMoneyDocNumber4;
	private String timeAndMoneyDocNumber5;
	private List<TimeAndMoneyDocument> timeAndMoneyDocs;

	public AwardPrintNotice() {
		setDefaults();
	}

	public void refresh() {
		// do nothing
	}

	public void setDefaults() {
		terms = true;
		specialReview = true;
		equipment = true;
		foreignTravel = true;
		subAward = true;
		costShare = true;
		faRates = true;
		benefitsRates = true;
		flowThru = true;
		comments = true;
		technicalReports = true;
		reports = true;
		payment = true;
		closeout = true;
		sponsorContacts = true;
		otherData = true;
		keywords = true;
		fundingSummary = false;
		hierarchy = false;
		requireSignature = false;

		// BUKC-0022: Add BU's Award Notification
		timeAndMoney1 = false;
		timeAndMoney2 = false;
		timeAndMoney3 = false;
		timeAndMoney4 = false;
		timeAndMoney5 = false;
	}

	/**
	 * Selects all items except requireSignature
	 */
	public void selectAllItems() {
		setAllItems(true);
	}

	/**
	 * Deselects all items except requireSignature
	 */
	public void deselectAllItems() {
		setAllItems(false);
	}

	/**
	 * Sets all items, except requireSignature for the select all/none button
	 */
	private void setAllItems(Boolean value) {
		terms = true;
		specialReview = true;
		equipment = true;
		foreignTravel = true;
		subAward = true;
		costShare = true;
		faRates = true;
		benefitsRates = true;
		flowThru = true;
		comments = true;
		fundingSummary = true;
		hierarchy = true;
		technicalReports = true;
		reports = true;
		payment = true;
		closeout = true;
		sponsorContacts = true;
		otherData = true;
		keywords = true;

		/* BUKC-0022: Add BU's Award Notification */
		timeAndMoney1 = true;
		timeAndMoney2 = true;
		timeAndMoney3 = true;
		timeAndMoney4 = true;
		timeAndMoney5 = true;
	}

	public Boolean getCloseout() {
		return closeout;
	}

	public void setCloseout(Boolean closeout) {
		this.closeout = closeout;
	}

	public Boolean getCostShare() {
		return costShare;
	}

	public void setCostShare(Boolean costShare) {
		this.costShare = costShare;
	}

	public Boolean getEquipment() {
		return equipment;
	}

	public void setEquipment(Boolean equipment) {
		this.equipment = equipment;
	}

	public Boolean getFlowThru() {
		return flowThru;
	}

	public void setFlowThru(Boolean flowThru) {
		this.flowThru = flowThru;
	}

	public Boolean getForeignTravel() {
		return foreignTravel;
	}

	public void setForeignTravel(Boolean foreignTravel) {
		this.foreignTravel = foreignTravel;
	}

	public Boolean getFundingSummary() {
		return fundingSummary;
	}

	public void setFundingSummary(Boolean fundingSummary) {
		this.fundingSummary = fundingSummary;
	}

	public Boolean getHierarchy() {
		return hierarchy;
	}

	public void setHierarchy(Boolean hierarchy) {
		this.hierarchy = hierarchy;
	}

	public Boolean getKeywords() {
		return keywords;
	}

	public void setKeywords(Boolean keywords) {
		this.keywords = keywords;
	}

	public Boolean getOtherData() {
		return otherData;
	}

	public void setOtherData(Boolean otherData) {
		this.otherData = otherData;
	}

	public Boolean getPayment() {
		return payment;
	}

	public void setPayment(Boolean payment) {
		this.payment = payment;
	}

	public Boolean getSpecialReview() {
		return specialReview;
	}

	public void setSpecialReview(Boolean specialReview) {
		this.specialReview = specialReview;
	}

	public Boolean getSubAward() {
		return subAward;
	}

	public void setSubAward(Boolean subAward) {
		this.subAward = subAward;
	}

	public Boolean getTerms() {
		return terms;
	}

	public void setTerms(Boolean terms) {
		this.terms = terms;
	}

	public Boolean getRequireSignature() {
		return requireSignature;
	}

	public void setRequireSignature(Boolean requireSignature) {
		this.requireSignature = requireSignature;
	}

	public Boolean getFaRates() {
		return faRates;
	}

	public void setFaRates(Boolean faRates) {
		this.faRates = faRates;
	}

	public Boolean getBenefitsRates() {
		return benefitsRates;
	}

	public void setBenefitsRates(Boolean benefitsRates) {
		this.benefitsRates = benefitsRates;
	}

	public Boolean getTechnicalReports() {
		return technicalReports;
	}

	public void setTechnicalReports(Boolean technicalReports) {
		this.technicalReports = technicalReports;
	}

	public Boolean getReports() {
		return reports;
	}

	public void setReports(Boolean reports) {
		this.reports = reports;
	}

	public Boolean getSponsorContacts() {
		return sponsorContacts;
	}

	public void setSponsorContacts(Boolean sponsorContacts) {
		this.sponsorContacts = sponsorContacts;
	}

	public Boolean getComments() {
		return comments;
	}

	public void setComments(Boolean comments) {
		this.comments = comments;
	}

	public Boolean getTimeAndMoney1() {
		return timeAndMoney1;
	}

	public void setTimeAndMoney1(Boolean timeAndMoney1) {
		this.timeAndMoney1 = timeAndMoney1;
	}

	public Boolean getTimeAndMoney2() {
		return timeAndMoney2;
	}

	public void setTimeAndMoney2(Boolean timeAndMoney2) {
		this.timeAndMoney2 = timeAndMoney2;
	}

	public Boolean getTimeAndMoney3() {
		return timeAndMoney3;
	}

	public void setTimeAndMoney3(Boolean timeAndMoney3) {
		this.timeAndMoney3 = timeAndMoney3;
	}

	public Boolean getTimeAndMoney4() {
		return timeAndMoney4;
	}

	public void setTimeAndMoney4(Boolean timeAndMoney4) {
		this.timeAndMoney4 = timeAndMoney4;
	}

	public Boolean getTimeAndMoney5() {
		return timeAndMoney5;
	}

	public void setTimeAndMoney5(Boolean timeAndMoney5) {
		this.timeAndMoney5 = timeAndMoney5;
	}

	public String getTimeAndMoneyDocNumber1() {
		return timeAndMoneyDocNumber1;
	}

	public void setTimeAndMoneyDocNumber1(String timeAndMoneyDocNumber1) {
		this.timeAndMoneyDocNumber1 = timeAndMoneyDocNumber1;
	}

	public String getTimeAndMoneyDocNumber2() {
		return timeAndMoneyDocNumber2;
	}

	public void setTimeAndMoneyDocNumber2(String timeAndMoneyDocNumber2) {
		this.timeAndMoneyDocNumber2 = timeAndMoneyDocNumber2;
	}

	public String getTimeAndMoneyDocNumber3() {
		return timeAndMoneyDocNumber3;
	}

	public void setTimeAndMoneyDocNumber3(String timeAndMoneyDocNumber3) {
		this.timeAndMoneyDocNumber3 = timeAndMoneyDocNumber3;
	}

	public String getTimeAndMoneyDocNumber4() {
		return timeAndMoneyDocNumber4;
	}

	public void setTimeAndMoneyDocNumber4(String timeAndMoneyDocNumber4) {
		this.timeAndMoneyDocNumber4 = timeAndMoneyDocNumber4;
	}

	public String getTimeAndMoneyDocNumber5() {
		return timeAndMoneyDocNumber5;
	}

	public void setTimeAndMoneyDocNumber5(String timeAndMoneyDocNumber5) {
		this.timeAndMoneyDocNumber5 = timeAndMoneyDocNumber5;
	}

	public List<TimeAndMoneyDocument> getTimeAndMoneyDocs() {
		return timeAndMoneyDocs;
	}

	public void setTimeAndMoneyDocs(List<TimeAndMoneyDocument> timeAndMoneyDocs) {
		this.timeAndMoneyDocs = timeAndMoneyDocs;
	}
}
