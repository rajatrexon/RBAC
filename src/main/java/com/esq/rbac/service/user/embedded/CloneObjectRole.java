package com.esq.rbac.service.user.embedded;

import com.esq.rbac.service.contact.embedded.BaseEntity;
import com.esq.rbac.service.contact.objectrole.domain.ObjectRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.OneToMany;

import java.util.List;

public class CloneObjectRole extends BaseEntity {

    @OneToMany(mappedBy = "contact", cascade = CascadeType.PERSIST)
    private List<ObjectRole> to;
    private ObjectRole from;

    public List<ObjectRole> getTo() {
        return to;
    }

    public void setTo(List<ObjectRole> to) {
        this.to = to;
    }

    public ObjectRole getFrom() {
        return from;
    }

    public void setFrom(ObjectRole from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "CloneObjectRole [to=" + to + ", from=" + from + "]";
    }


}
