package edu.ku.kuali.kra.subawardReporting.printing.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.kuali.coeus.common.framework.print.PrintingService;
import org.kuali.kra.subaward.bo.SubAwardForms;
import org.kuali.kra.subaward.bo.SubAwardPrintAgreement;
import org.kuali.kra.subaward.reporting.printing.print.SubAwardSF294Print;
import org.kuali.kra.subaward.reporting.printing.print.SubAwardSF295Print;
import org.kuali.kra.subawardReporting.printing.print.SubAwardFDPAgreement;
import org.kuali.kra.subawardReporting.printing.print.SubAwardFDPModification;
import org.kuali.rice.krad.service.BusinessObjectService;

/**
 * Created by mkousheh on 10/20/14.
 */
public class SubAwardPrintingServiceImpl extends org.kuali.kra.subaward.reporting.printing.service.impl.SubAwardPrintingServiceImpl {

    private static final String SUB_AWARD_FDP_TEMPLATE = "fdpAgreement";
    private static final String SUB_AWARD_FDP_MODIFICATION = "fdpModification";

    private SubAwardSF294Print subAwardSF294Print;
    private SubAwardSF295Print subAwardSF295Print;
    private PrintingService printingService;
    private SubAwardFDPModification subAwardFDPModification;
    private SubAwardFDPAgreement subAwardFDPAgreement;
    private BusinessObjectService businessObjectService;

    /**
     * This method gets the form template from the given sponsor form table
     *
     *
     * @param sponsorFormTemplateLists
     *            -
     *            list of sponsor form template list
     * @return list of sponsor form template
     */
    @Override
    public List<SubAwardForms> getSponsorFormTemplates(SubAwardPrintAgreement subAwardPrint, List<SubAwardForms> subAwardFormList) {
        List<SubAwardForms> printFormTemplates = new ArrayList<SubAwardForms>();
        if (subAwardPrint.getFdpType().equals(SUB_AWARD_FDP_TEMPLATE)) {
            printFormTemplates.add(getBusinessObjectService().findBySinglePrimaryKey(SubAwardForms.class, "FDP Template"));
        } else if (subAwardPrint.getFdpType().equals(SUB_AWARD_FDP_MODIFICATION)) {
            printFormTemplates.add(getBusinessObjectService().findBySinglePrimaryKey(SubAwardForms.class, "FDP Modification"));
        }
        if (subAwardPrint.getAttachment3A()) {
            printFormTemplates.add(getBusinessObjectService().findBySinglePrimaryKey(SubAwardForms.class, "FDP_ATT_3A"));
        }
        if (subAwardPrint.getAttachment3B()) {
            printFormTemplates.add(getBusinessObjectService().findBySinglePrimaryKey(SubAwardForms.class, "FDP_ATT_3B"));
        }
        if (subAwardPrint.getAttachment3BPage2()) {
            printFormTemplates.add(getBusinessObjectService().findBySinglePrimaryKey(SubAwardForms.class, "FDP_ATT_3B_2"));
        }

        // BUKC-0097: Remove Attachment 4 selection from Subaward Actions - Print - FDP Attachments (Subawards Enhancement 50)
        // if(subAwardPrint.getAttachment4()){
        // printFormTemplates.add(getBusinessObjectService().findBySinglePrimaryKey(SubAwardForms.class, "FDP_ATT_4"));
        // }

        for (SubAwardForms subAwardFormValues : subAwardFormList) {
            if (subAwardFormValues.getSelectToPrint()) {
                String description = subAwardFormValues.getDescription();
                String[] token = description.split("\\s");
                printFormTemplates.add(getBusinessObjectService().findBySinglePrimaryKey(SubAwardForms.class, "FDP_" + token[0]));
            }
        }

        resetsubAwardFormList(subAwardFormList);
        return printFormTemplates;
    }

}
