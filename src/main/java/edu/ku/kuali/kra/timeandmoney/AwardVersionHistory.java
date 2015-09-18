/*
 * Copyright 2005-2013 The Kuali Foundation
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
package edu.ku.kuali.kra.timeandmoney;

import org.kuali.kra.award.home.Award;

/**
 * A collection of this class is maintained on Time And Money Document. There
 * will be one entry for each version of the current Award on Time And Money
 * Document.
 */
public class AwardVersionHistory extends
		org.kuali.kra.timeandmoney.AwardVersionHistory {

	// BUKC-0023: Add History tab - add Award Number to display on the History
	// tab
	private String awardNumber;

	public AwardVersionHistory(Award parent) {
		super(parent);
	}

	/**
	 * Gets the awardNumber attribute.
	 * 
	 * @return the awardNumber
	 */
	public String getAwardNumber() {
		return awardNumber;
	}

	/**
	 * Sets the awardNumber attribute value.
	 * 
	 * @param awardNumber
	 *            the awardNumber to set
	 */
	public void setAwardNumber(String awardNumber) {
		this.awardNumber = awardNumber;
	}
}
