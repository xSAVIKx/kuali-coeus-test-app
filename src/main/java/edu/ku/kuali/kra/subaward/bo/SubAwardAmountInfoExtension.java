package edu.ku.kuali.kra.subaward.bo;

import org.kuali.coeus.sys.framework.model.KcPersistableBusinessObjectBase;
import org.kuali.rice.krad.bo.PersistableBusinessObjectExtension;

public class SubAwardAmountInfoExtension extends KcPersistableBusinessObjectBase implements PersistableBusinessObjectExtension {
    private String modificationType;

    public String getModificationType() {
        return modificationType;
    }

    public void setModificationType(String modificationType) {
        this.modificationType = modificationType;
    }

    public Integer getSubAwardAmountInfoId() {
        return subAwardAmountInfoId;
    }

    public void setSubAwardAmountInfoId(Integer subAwardAmountInfoId) {
        this.subAwardAmountInfoId = subAwardAmountInfoId;
    }

    private Integer subAwardAmountInfoId;

}
