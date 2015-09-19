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

import java.util.List;

import org.kuali.coeus.sys.api.model.ScaleTwoDecimal;

public class BUAwardPrintAmountInfo implements Comparable<BUAwardPrintAmountInfo> {
    private String internalOrderNbr;
    private String description;
    private String legacySource;
    private List<String> piFullNames;
    private ScaleTwoDecimal fAndARate;
    private ScaleTwoDecimal obligatedChangeDirect;
    private ScaleTwoDecimal obligatedChangeIndirect;
    private ScaleTwoDecimal totalChange;

    public String getInternalOrderNbr() {
        return internalOrderNbr;
    }

    public void setInternalOrderNbr(String internalOrderNbr) {
        this.internalOrderNbr = internalOrderNbr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLegacySource() {
        return legacySource;
    }

    public void setLegacySource(String legacySource) {
        this.legacySource = legacySource;
    }

    public List<String> getPiFullNames() {
        return piFullNames;
    }

    public void setPiFullNames(List<String> piFullNames) {
        this.piFullNames = piFullNames;
    }

    public ScaleTwoDecimal getfAndARate() {
        return fAndARate;
    }

    public void setfAndARate(ScaleTwoDecimal fAndARate) {
        this.fAndARate = fAndARate;
    }

    public ScaleTwoDecimal getObligatedChangeDirect() {
        return obligatedChangeDirect;
    }

    public void setObligatedChangeDirect(ScaleTwoDecimal obligatedChangeDirect) {
        this.obligatedChangeDirect = obligatedChangeDirect;
    }

    public ScaleTwoDecimal getObligatedChangeIndirect() {
        return obligatedChangeIndirect;
    }

    public void setObligatedChangeIndirect(ScaleTwoDecimal obligatedChangeIndirect) {
        this.obligatedChangeIndirect = obligatedChangeIndirect;
    }

    public ScaleTwoDecimal getTotalChange() {
        return totalChange;
    }

    public void setTotalChange(ScaleTwoDecimal totalChange) {
        this.totalChange = totalChange;
    }

    // Implement natural sort by internal order.
    public int compareTo(BUAwardPrintAmountInfo info) {
        return this.internalOrderNbr.compareTo(info.internalOrderNbr);
    }

}
