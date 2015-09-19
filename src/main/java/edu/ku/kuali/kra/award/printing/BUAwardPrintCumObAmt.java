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

public class BUAwardPrintCumObAmt {
    private Date obligationStart;
    private Date obligationEnd;
    private ScaleTwoDecimal obligatedChangeDirect;
    private ScaleTwoDecimal obligatedChangeIndirect;
    private ScaleTwoDecimal obligatedTotal;

    public Date getObligationStart() {
        return obligationStart;
    }

    public void setObligationStart(Date obligationStart) {
        this.obligationStart = obligationStart;
    }

    public Date getObligationEnd() {
        return obligationEnd;
    }

    public void setObligationEnd(Date obligationEnd) {
        this.obligationEnd = obligationEnd;
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

    public ScaleTwoDecimal getObligatedTotal() {
        return obligatedTotal;
    }

    public void setObligatedTotal(ScaleTwoDecimal obligatedTotal) {
        this.obligatedTotal = obligatedTotal;
    }

}
