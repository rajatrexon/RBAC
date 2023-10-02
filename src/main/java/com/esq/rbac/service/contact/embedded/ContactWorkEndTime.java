package com.esq.rbac.service.contact.embedded;

import com.esq.rbac.service.contact.domain.Contact;

import java.util.Date;

public class ContactWorkEndTime {

    private final Date workEndTime;
    private final long relativeTime;
    private final Contact contact;

    public ContactWorkEndTime(Contact contact, Date workEndTime, long relativeTime) {
        this.contact = contact;
        this.workEndTime = workEndTime;
        this.relativeTime = relativeTime;
    }

    public Date getWorkEndTime() {
        return workEndTime;
    }

    public long getRelativeTime() {
        return relativeTime;
    }

    public Contact getContact() {
        return contact;
    }
}