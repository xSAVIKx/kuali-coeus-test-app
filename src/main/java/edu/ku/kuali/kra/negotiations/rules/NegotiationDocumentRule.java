package edu.ku.kuali.kra.negotiations.rules;

import org.apache.commons.lang.StringUtils;
import org.kuali.kra.negotiations.bo.Negotiation;
import org.kuali.kra.negotiations.bo.NegotiationActivity;
import org.kuali.kra.negotiations.bo.NegotiationAssociationType;
import org.kuali.kra.negotiations.document.NegotiationDocument;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.util.GlobalVariables;

import edu.ku.kuali.kra.infrastructure.BUConstants;

/**
 * @author mukadder
 */
public class NegotiationDocumentRule extends org.kuali.kra.negotiations.rules.NegotiationDocumentRule {

    private static final String NEGOTIATION_ERROR_PATH = "document.negotiationList[0]";
    private static final String PI_EMPLOYEE = "unAssociatedDetail.piEmployeeUserName";
    private static final String ACTIVITIES_PREFIX = "activities[";

    @Override
    protected boolean processCustomSaveDocumentBusinessRules(Document document) {
        NegotiationDocument negotiationDocument = (NegotiationDocument) document;
        Negotiation negotiation = negotiationDocument.getNegotiation();

        boolean result = true;

        result &= super.processCustomSaveDocumentBusinessRules(document);

        GlobalVariables.getMessageMap().addToErrorPath(NEGOTIATION_ERROR_PATH);
        result &= validateNegotiationAttributesWhenNoneSelected(negotiation);
        GlobalVariables.getMessageMap().removeFromErrorPath(NEGOTIATION_ERROR_PATH);

        return result;
    }

    // BUKC-0156: Negotiation - sufficient message when Activity Start/End Date are outside the bounds of the Negotiation Start/End Date (Neg. QA
    // Issue 19)
    @Override
    public boolean validateNegotiationActivities(Negotiation negotiation) {
        boolean result = true;
        int index = 0;
        NegotiationActivityRuleImpl rule = new NegotiationActivityRuleImpl();
        for (NegotiationActivity activity : negotiation.getActivities()) {
            GlobalVariables.getMessageMap().addToErrorPath(ACTIVITIES_PREFIX + index + "]");
            result &= rule.validateNegotiationActivity(activity, negotiation);
            result &= validateActivityAttachments(negotiation, activity);
            GlobalVariables.getMessageMap().removeFromErrorPath(ACTIVITIES_PREFIX + index + "]");
            index++;
        }
        return result;
    }

    // BUKC-0154: Negotiation - Required fields for Association = None (Neg. Enhancements 4)
    public boolean validateNegotiationAttributesWhenNoneSelected(Negotiation negotiation) {
        boolean valid = true;
        if (negotiation.getNegotiationAssociationType() != null
                && StringUtils.equals(negotiation.getNegotiationAssociationType().getCode(), NegotiationAssociationType.NONE_ASSOCIATION)) {
            if (StringUtils.isBlank(negotiation.getUnAssociatedDetail().getPiEmployeeUserName())
                    && StringUtils.isBlank(negotiation.getUnAssociatedDetail().getPiRolodexId())) {
                valid = false;
                getErrorReporter().reportError(PI_EMPLOYEE, BUConstants.NEGOTIATION_ERROR_PI_REQUIRED);
            }
        }

        return valid;
    }

}
