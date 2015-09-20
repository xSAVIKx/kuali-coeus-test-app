package edu.ku.kuali.kra.subaward.bo;

import java.sql.Date;

import org.kuali.coeus.sys.framework.model.KcPersistableBusinessObjectBase;
import org.kuali.rice.krad.bo.PersistableBusinessObjectExtension;

public class SubAwardExtension extends KcPersistableBusinessObjectBase implements PersistableBusinessObjectExtension {

    private Long subAwardId;

    public Long getSubAwardId() {
        return subAwardId;
    }

    public void setSubAwardId(Long subAwardId) {
        this.subAwardId = subAwardId;
    }

    public Date getDateReceived() {
        return dateReceived;
    }

    public void setDateReceived(Date dateReceived) {
        this.dateReceived = dateReceived;
    }

    private Date dateReceived;

}
