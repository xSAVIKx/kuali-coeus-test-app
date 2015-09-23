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
package edu.ku.kuali.kra.service.impl;

import java.util.ArrayList;

import org.kuali.coeus.common.framework.version.VersionException;
import org.kuali.coeus.common.framework.version.sequence.owner.SequenceOwner;
import org.kuali.kra.award.home.Award;
import org.kuali.kra.institutionalproposal.home.InstitutionalProposal;
import org.kuali.rice.krad.util.ObjectUtils;

import edu.ku.kuali.kra.award.home.AwardExtension;
import edu.ku.kuali.kra.bo.AwardTransmission;
import edu.ku.kuali.kra.bo.AwardTransmissionChild;
import edu.ku.kuali.kra.institutionalproposal.home.InstitutionalProposalExtension;
import edu.ku.kuali.kra.service.VersioningService;

/**
 * This service implements generic versioning.
 */
public class VersioningServiceImpl extends org.kuali.coeus.common.impl.version.VersioningServiceImpl implements VersioningService {

    // BUKC-0014: KC/SAP Interface - Retain award transmission history when version an award
    /**
     * {@inheritDoc}
     */
    @Override
    public <T extends SequenceOwner<?>> T createNewVersion(T oldVersion) throws VersionException {
        T newVersion = super.createNewVersion(oldVersion);
        if (oldVersion instanceof Award) {
            createAwardExt((Award) newVersion, (Award) oldVersion);
        } else if (oldVersion instanceof InstitutionalProposal) {
            createIpExt((InstitutionalProposal) newVersion, (InstitutionalProposal) oldVersion);
        }

        return newVersion;
    }

    /**
     * Creates BU's Award Extension fields
     *
     * @param newVersion
     *            new version of the award
     * @param oldVersion
     *            old version of the award
     */
    private void createAwardExt(Award newVersion, Award oldVersion) {
        // also
        if (oldVersion.getExtension() != null) {
            AwardExtension newExt = (AwardExtension) ObjectUtils.deepCopy(oldVersion.getExtension());
            newExt.setAwardId(null);
            newVersion.setExtension(newExt);
            newExt.setAwardTransmissions(new ArrayList<AwardTransmission>());
            for (AwardTransmission transmission : ((AwardExtension) oldVersion.getExtension()).getAwardTransmissions()) {
                AwardTransmission copiedTransmission = (AwardTransmission) ObjectUtils.deepCopy(transmission);
                copiedTransmission.setAwardId(Long.parseLong(""));
                copiedTransmission.setTransmissionId(Long.parseLong(""));
                copiedTransmission.setTransmissionChildren(new ArrayList<AwardTransmissionChild>());
                for (AwardTransmissionChild tranChild : transmission.getTransmissionChildren()) {
                    AwardTransmissionChild copiedTransmissionChild = (AwardTransmissionChild) ObjectUtils.deepCopy(tranChild);
                    copiedTransmissionChild.setTransmissionId(Long.parseLong(""));
                    copiedTransmissionChild.setTransmissionChildId(Long.parseLong(""));
                    copiedTransmission.getTransmissionChildren().add(copiedTransmissionChild);
                }
                newExt.getAwardTransmissions().add(copiedTransmission);
            }
        }
    }

    /**
     * Creates BU's Institutional Proposal Extension fields
     *
     * @param newVersion
     *            new version of the Institutional Proposal
     * @param oldVersion
     *            old version of the Institutional Proposal
     */
    private void createIpExt(InstitutionalProposal newVersion, InstitutionalProposal oldVersion) {
        // also
        if (oldVersion.getExtension() != null) {
            InstitutionalProposalExtension newExt = (InstitutionalProposalExtension) ObjectUtils.deepCopy(oldVersion.getExtension());
            newExt.setProposalId(null);
            newVersion.setExtension(newExt);
        }
    }

}
