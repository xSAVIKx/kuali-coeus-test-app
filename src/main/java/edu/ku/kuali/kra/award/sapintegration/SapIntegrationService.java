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

import org.kuali.kra.award.home.Award;

/**
 * <p>
 * A service which provides integration with Boston University's SAP implementation. Provides two primary operations, one for performing validation
 * against incoming award hierarchy data, and other for transmitting award hierarchy data to SAP.
 * <p/>
 * <p>
 * It is intended that invocation of the {@link #transmit(SapTransmission)} method, will invoke validation prior to performing submission. The
 * additional {@link #validate(SapTransmission)} method is provided to allow for identifying and reporting on possible validation issues prior to
 * submission of the SAP transmission.
 *
 * @author Eric Westfall (ewestfal@gmail.com)
 */

public interface SapIntegrationService {

    public ValidationResults validate(SapTransmission transmission);

    public String getTransmitXml(SapTransmission transmission);

    public SapTransmissionResponse transmit(SapTransmission transmission);

    public String determineCostShareMemoMatch(Award award);
}
