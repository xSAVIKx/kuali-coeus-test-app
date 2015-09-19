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

import java.util.Date;

import org.kuali.coeus.sys.api.model.ScaleTwoDecimal;

public class BUAwardPrintAntAmt {
    private Date projectStart;
    private Date projectEnd;
    private ScaleTwoDecimal anticipatedChangeDirect;
    private ScaleTwoDecimal anticipatedChangeIndirect;
    private ScaleTwoDecimal anticipatedTotal;

    public Date getProjectStart() {
        return projectStart;
    }

    public void setProjectStart(Date projectStart) {
        this.projectStart = projectStart;
    }

    public Date getProjectEnd() {
        return projectEnd;
    }

    public void setProjectEnd(Date projectEnd) {
        this.projectEnd = projectEnd;
    }

    public ScaleTwoDecimal getAnticipatedChangeDirect() {
        return anticipatedChangeDirect;
    }

    public void setAnticipatedChangeDirect(ScaleTwoDecimal anticipatedChangeDirect) {
        this.anticipatedChangeDirect = anticipatedChangeDirect;
    }

    public ScaleTwoDecimal getAnticipatedChangeIndirect() {
        return anticipatedChangeIndirect;
    }

    public void setAnticipatedChangeIndirect(ScaleTwoDecimal anticipatedChangeIndirect) {
        this.anticipatedChangeIndirect = anticipatedChangeIndirect;
    }

    public ScaleTwoDecimal getAnticipatedTotal() {
        return anticipatedTotal;
    }

    public void setAnticipatedTotal(ScaleTwoDecimal anticipatedTotal) {
        this.anticipatedTotal = anticipatedTotal;
    }

}
