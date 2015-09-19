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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.kuali.coeus.common.framework.print.AbstractPrint;
import org.kuali.coeus.common.framework.print.util.PrintingUtils;

/**
 * This class returns the stylesheet stream for BUAwardNotice
 */
public class BUAwardNoticePrint extends AbstractPrint {
    private static final String BU_XSL_AWARD_NOTICE = "BUAwardNotice.xslt";
    private static String BU_XSL_CONTEXT_DIR = "/edu/bu/kuali/kra/printing/stylesheet/";

    public List<Source> getXSLTemplates() {
        Source src = new StreamSource(new PrintingUtils().getClass().getResourceAsStream(BU_XSL_CONTEXT_DIR + "/" + BU_XSL_AWARD_NOTICE));

        try {
            int size = ((StreamSource) src).getInputStream().available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ;

        ArrayList<Source> sourceList = new ArrayList<Source>();
        sourceList.add(src);
        return sourceList;

    }

}
