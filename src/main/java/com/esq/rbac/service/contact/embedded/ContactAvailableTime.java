package com.esq.rbac.service.contact.embedded;

import com.esq.rbac.service.contact.domain.Contact;

import java.util.Date;

public class ContactAvailableTime {

    private final Date availableTime;
    private final Contact contact;
    private final boolean available;


    public ContactAvailableTime(Date availableTime, Contact contact) {
        this.availableTime = availableTime;
        this.contact = contact;
        this.available=false;
    }

    public ContactAvailableTime(Date availableTime, Contact contact,boolean available) {
        this.availableTime = availableTime;
        this.contact = contact;
        this.available=available;
    }

    public Date getAvailableTime() {
        return availableTime;
    }

    public Contact getContact() {
        return contact;
    }

    public boolean isAvailable() {
        return available;
    }
}

