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
package edu.ku.kuali.kra.subaward.bo;

public class SubAwardFundingSource extends org.kuali.kra.subaward.bo.SubAwardFundingSource {

    private String sponsorAwardNumber;
    private String principalInvestigatorName;

    // BUKC-0110: Pre-populate Subaward Title field from Funding Source (Enhancement # 11)
    private String title;

    /**
     * .
     * This is the Getter Method for sponsorAwardNumber
     *
     * @return Returns the sponsorAwardNumber.
     */
    public String getSponsorAwardNumber() {
        return sponsorAwardNumber;
    }

    /**
     * .
     * This is the Setter Method for sponsorAwardNumber
     *
     * @param sponsorAwardNumber
     *            The sponsorAwardNumber to set.
     */
    public void setSponsorAwardNumber(String sponsorAwardNumber) {
        this.sponsorAwardNumber = sponsorAwardNumber;
    }

    /**
     * .
     * This is the Getter Method for principalInvestigatorName
     *
     * @return Returns the principalInvestigatorName.
     */
    public String getPrincipalInvestigatorName() {
        return principalInvestigatorName;
    }

    /**
     * .
     * This is the Getter Method for principalInvestigatorName
     *
     * @return Returns the principalInvestigatorName.
     */
    public void setPrincipalInvestigatorName(String principalInvestigatorName) {
        this.principalInvestigatorName = principalInvestigatorName;
    }

    /**
     * .
     * This is the Getter Method for title
     *
     * @return Returns the title.
     */
    public String getTitle() {
        return title;
    }

    /**
     * .
     * This is the Getter Method for title
     *
     * @return Returns the title.
     */
    public void setTitle(String title) {
        this.title = title;
    }

}
