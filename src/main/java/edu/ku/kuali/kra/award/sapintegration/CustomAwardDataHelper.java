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

import java.sql.Date;

import edu.ku.kuali.kra.award.home.AwardExtension;

import org.kuali.kra.award.home.Award;

/**
 * A helper class which wraps an Award and provides access to custom data
 * defined by the BU implementation of the custom award data.
 *
 * @author Eric Westfall (ewestfal@gmail.com)
 */
public final class CustomAwardDataHelper {

    private final Award award;

    /**
     * Constructor
     *
     * @param award
     *            been worked on
     */
    CustomAwardDataHelper(Award award) {
        if (award == null) {
            throw new IllegalArgumentException("Award cannot be null.");
        }
        this.award = award;
    }

    /**
     * returns BU's Interest Earned
     *
     * @return String
     */
    String getInterestEarned() {
        return ((AwardExtension) award.getExtension()).getInterestEarned();
    }

    /**
     * returns BU field Major Project
     *
     * @return String represents Major Project value
     */
    String getMajorProject() {
        return ((AwardExtension) award.getExtension()).getMajorProject();
    }

    /**
     * BU field - A133 Cluster
     *
     * @return String represents A133 Cluster value
     */
    String getA133Cluster() {
        return ((AwardExtension) award.getExtension()).getA133Cluster();
    }

    /**
     * BU field - ARRA
     *
     * @return boolean represents weather is ARRA or not
     */
    boolean isArra() {
        return "Yes".equals(((AwardExtension) award.getExtension()).getArraCode());
    }

    /**
     * BU field - AVC Indicator
     *
     * @return String represent AVC indicator
     */
    String getAvcIndicator() {
        return ((AwardExtension) award.getExtension()).getAvcIndicator();
    }

    /**
     * BU's field - Child type
     *
     * @return String represents child type field
     */
    String getChildType() {
        return ((AwardExtension) award.getExtension()).getChildType();
    }

    /**
     * BU field - Child description
     *
     * @return String represent child description
     */
    String getChildDescription() {
        return ((AwardExtension) award.getExtension()).getChildDescription();
    }

    /**
     * BU field - Frings not allowed indicator
     *
     * @return boolean represents if fringe not allowed on award
     */
    boolean isFringeNotAllowed() {
        return ((AwardExtension) award.getExtension()).getFringeNotAllowedIndicator();
    }

    /**
     * BU field- Last transmission date
     *
     * @return Date represent last transmission date
     */
    Date getLastTransmissionDate() {
        Date date = ((AwardExtension) award.getExtension()).getLastTransmissionDate();
        if (date != null) {
            return new Date(date.getTime());
        }
        return null;
    }

}
