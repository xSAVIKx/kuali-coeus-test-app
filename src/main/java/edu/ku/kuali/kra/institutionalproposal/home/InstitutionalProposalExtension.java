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

package edu.ku.kuali.kra.institutionalproposal.home;

import org.kuali.coeus.sys.api.model.ScaleTwoDecimal;
import org.kuali.coeus.sys.framework.model.KcPersistableBusinessObjectBase;
import org.kuali.rice.krad.bo.PersistableBusinessObjectExtension;

/**
 * This class represent BU additional fields (BUKC-0003) for Institutional Proposal module.
 *
 * @author mkousheh
 */
public class InstitutionalProposalExtension extends KcPersistableBusinessObjectBase implements PersistableBusinessObjectExtension {

    private static final long serialVersionUID = 3138263301644052314L;
    private String conferenceGrant;
    private String individualFellowship;
    private String approvedFAWaiverIndicator;
    private ScaleTwoDecimal initialPeriodFARate1;
    private ScaleTwoDecimal initialPeriodFARate2;
    private String majorProject;
    private Long proposalId;

    public InstitutionalProposalExtension() {
        super();

    }

    public Long getProposalId() {
        return proposalId;
    }

    public void setProposalId(Long proposalId) {
        this.proposalId = proposalId;
    }

    public String getMajorProject() {
        return majorProject;
    }

    public void setMajorProject(String majorProject) {
        this.majorProject = majorProject;
    }

    public String getConferenceGrant() {
        return conferenceGrant;
    }

    public void setConferenceGrant(String conferenceGrant) {
        this.conferenceGrant = conferenceGrant;
    }

    public String getIndividualFellowship() {
        return individualFellowship;
    }

    public void setIndividualFellowship(String individualFellowship) {
        this.individualFellowship = individualFellowship;
    }

    public String getApprovedFAWaiverIndicator() {
        return approvedFAWaiverIndicator;
    }

    public void setApprovedFAWaiverIndicator(String approvedFAWaiverIndicator) {
        this.approvedFAWaiverIndicator = approvedFAWaiverIndicator;
    }

    public ScaleTwoDecimal getInitialPeriodFARate1() {
        return initialPeriodFARate1;
    }

    public void setInitialPeriodFARate1(ScaleTwoDecimal initialPeriodFARate1) {
        this.initialPeriodFARate1 = initialPeriodFARate1;
    }

    public ScaleTwoDecimal getInitialPeriodFARate2() {
        return initialPeriodFARate2;
    }

    public void setInitialPeriodFARate2(ScaleTwoDecimal initialPeriodFARate2) {
        this.initialPeriodFARate2 = initialPeriodFARate2;
    }

}
