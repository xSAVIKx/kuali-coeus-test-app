package edu.ku.kuali.kra.subaward.service;

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

import java.util.List;
import java.util.Set;

import org.kuali.kra.award.home.Award;
import org.kuali.kra.subaward.bo.SubAward;
import org.kuali.rice.kew.api.exception.WorkflowException;

/**
 * This class represents SubAwardService...
 */
public interface SubAwardService extends org.kuali.kra.subaward.service.SubAwardService {

    /**
     * .
     * Update the subaward to use the new FundingSource.
     * If the award is versioned use the new award to link awards and subawards.
     *
     * @param subAwardNumbers
     * @param award
     */

    public List<SubAward> updateSubAwardFundingSource(Set<Long> subAwardNumbers, Award award);

    public void finalizeTheExistingVersion(SubAward subAward, Award award) throws WorkflowException;

}
