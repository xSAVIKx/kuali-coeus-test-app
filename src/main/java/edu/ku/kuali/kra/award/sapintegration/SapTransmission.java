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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.kuali.kra.award.home.Award;

/**
 * BU KC/SAP web service transmission process.
 *
 * @author Eric Westfall (ewestfal@gmail.com)
 */
public class SapTransmission {

    private Award award;
    private List<Award> childAwards;

    public SapTransmission(Award award, List<Award> childAwards) {
        if (award == null) {
            throw new IllegalArgumentException("Award must not be null.");
        }
        this.award = award;

        if (childAwards != null) {
            this.childAwards = new ArrayList<Award>(childAwards);
            Collections.copy(this.childAwards, childAwards);
        }
    }

    public Award getAward() {
        return this.award;
    }

    public List<Award> getChildAwards() {
        return Collections.unmodifiableList(childAwards);
    }

}
