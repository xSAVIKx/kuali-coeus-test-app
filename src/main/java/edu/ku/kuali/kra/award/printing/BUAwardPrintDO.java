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

import java.sql.Date;
import java.util.List;

import org.kuali.kra.award.contacts.AwardPerson;

public class BUAwardPrintDO {
    private String transactionType;
    private String grantNumber;
    private String fundsCenter;
    private List<AwardPerson> investigators;
    private String title;
    private String sponsor;
    private String primeSponsor;
    private String sponsorAwardId;
    private List<BUAwardPrintAdmin> deptAdmin;
    private List<BUAwardPrintAdmin> ospAdmin;
    private List<BUAwardPrintAdmin> pafoAdmin;
    private List<BUAwardPrintAmountInfo> amountInfo;
    private BUAwardPrintCumObAmt cumObAmt;
    private BUAwardPrintAntAmt antAmt;
    private List<String> comments;
    private List<BUAwardPrintReportReq> reportReqs;
    private List<BUAwardPrintTerm> terms;
    private String accountNumber;
    private String awardNumber;
    private Date transactionDate;
    private List<String> timeAndMoneyDocumentNumbers;

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getGrantNumber() {
        return grantNumber;
    }

    public void setGrantNumber(String grantNumber) {
        this.grantNumber = grantNumber;
    }

    public String getFundsCenter() {
        return fundsCenter;
    }

    public void setFundsCenter(String fundsCenter) {
        this.fundsCenter = fundsCenter;
    }

    public List<AwardPerson> getInvestigators() {
        return investigators;
    }

    public void setInvestigators(List<AwardPerson> investigators) {
        this.investigators = investigators;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSponsor() {
        return sponsor;
    }

    public void setSponsor(String sponsor) {
        this.sponsor = sponsor;
    }

    public String getPrimeSponsor() {
        return primeSponsor;
    }

    public void setPrimeSponsor(String primeSponsor) {
        this.primeSponsor = primeSponsor;
    }

    public String getSponsorAwardId() {
        return sponsorAwardId;
    }

    public void setSponsorAwardId(String sponsorAwardId) {
        this.sponsorAwardId = sponsorAwardId;
    }

    public List<BUAwardPrintAdmin> getDeptAdmin() {
        return deptAdmin;
    }

    public void setDeptAdmin(List<BUAwardPrintAdmin> deptAdmin) {
        this.deptAdmin = deptAdmin;
    }

    public List<BUAwardPrintAdmin> getOspAdmin() {
        return ospAdmin;
    }

    public void setOspAdmin(List<BUAwardPrintAdmin> ospAdmin) {
        this.ospAdmin = ospAdmin;
    }

    public List<BUAwardPrintAdmin> getPafoAdmin() {
        return pafoAdmin;
    }

    public void setPafoAdmin(List<BUAwardPrintAdmin> pafoAdmin) {
        this.pafoAdmin = pafoAdmin;
    }

    public List<BUAwardPrintAmountInfo> getAmountInfo() {
        return amountInfo;
    }

    public void setAmountInfo(List<BUAwardPrintAmountInfo> amountInfo) {
        this.amountInfo = amountInfo;
    }

    public BUAwardPrintCumObAmt getCumObAmt() {
        return cumObAmt;
    }

    public void setCumObAmt(BUAwardPrintCumObAmt cumObAmt) {
        this.cumObAmt = cumObAmt;
    }

    public BUAwardPrintAntAmt getAntAmt() {
        return antAmt;
    }

    public void setAntAmt(BUAwardPrintAntAmt antAmt) {
        this.antAmt = antAmt;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public List<BUAwardPrintReportReq> getReportReqs() {
        return reportReqs;
    }

    public void setReportReqs(List<BUAwardPrintReportReq> reportReqs) {
        this.reportReqs = reportReqs;
    }

    public List<BUAwardPrintTerm> getTerms() {
        return terms;
    }

    public void setTerms(List<BUAwardPrintTerm> terms) {
        this.terms = terms;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAwardNumber() {
        return awardNumber;
    }

    public void setAwardNumber(String awardNumber) {
        this.awardNumber = awardNumber;
    }

    public Date getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(Date transactionDate) {
        this.transactionDate = transactionDate;
    }

    public List<String> getTimeAndMoneyDocumentNumbers() {
        return timeAndMoneyDocumentNumbers;
    }

    public void setTimeAndMoneyDocumentNumbers(List<String> timeAndMoneyDocumentNumbers) {
        this.timeAndMoneyDocumentNumbers = timeAndMoneyDocumentNumbers;
    }

}
