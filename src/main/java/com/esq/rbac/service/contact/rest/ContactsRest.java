package com.esq.rbac.service.contact.rest;

import com.esq.rbac.service.base.error.RestErrorMessages;
import com.esq.rbac.service.base.exception.RestException;
import com.esq.rbac.service.base.rest.BaseRest;
import com.esq.rbac.service.contact.domain.Contact;
import com.esq.rbac.service.contact.party.repository.PartyRepository;
import com.esq.rbac.service.contact.repository.ContactRepository;
import com.esq.rbac.service.targetoperations.TargetOperations;
import com.esq.rbac.service.contact.helpers.ContactUserRest;
import com.esq.rbac.service.util.ContactAuditUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.lang.reflect.Array;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/contacts")
// Todo @ManagedResource(objectName = "com.esq.rbac.service.user.contact:type=REST,name=Contacts")
public class ContactsRest extends BaseRest<Contact> {

    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String ADDRESS = "address";
    private static final String UPDATED_TIME = "updatedTime";
    private static final Set<String> FILTER_COLUMNS;
    private static final Set<String> ORDER_COLUMNS;
    private static final Set<String> SEARCH_COLUMNS;
    private ContactUserRest userRest;
    private PartyRepository partyRepository;
    private ContactRepository contactRepository;
    static {
        FILTER_COLUMNS = new HashSet<>(Arrays.asList(
                NAME, ADDRESS));

        ORDER_COLUMNS = new HashSet<>(Arrays.asList(
                ID, NAME, ADDRESS,
                UPDATED_TIME));

        SEARCH_COLUMNS = new HashSet<>(Arrays.asList(
                NAME, ADDRESS));
    }

    @Autowired
    public ContactsRest(ContactRepository contactRepository) {
        super(Contact.class, contactRepository);
        this.contactRepository=contactRepository;
    }

    @Autowired
    public void setUserRest(ContactUserRest userRest) {
        log.trace("setUserRest;");
        this.userRest = userRest;
    }

    @Autowired
    public void setPartyRepository(PartyRepository partyRepository) {
        this.partyRepository = partyRepository;
    }

    @Autowired
    public void setContactRepository(ContactRepository contactRepository) {
        this.contactRepository = contactRepository;
    }

    @PostMapping(consumes ={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Contact> create(@RequestBody Contact contact, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        ResponseEntity<Contact> response= super.create(contact);
        Contact createdContact=(Contact) super.readById(contact.getId()).getBody();
        try {
            userRest.createAuditLog(TargetOperations.CONTACT_TARGET_NAME,TargetOperations.CREATE_OPERATION, ContactAuditUtil.convertToJSON(createdContact, TargetOperations.CREATE_OPERATION), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("create;exception={}",e);
        }
        return response;
    }

    @PutMapping(value = "/{id}",consumes ={MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML},
            produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Contact> update(@PathVariable("id") long id, @RequestBody Contact contact, @RequestHeader org.springframework.http.HttpHeaders headers) throws Exception {
        ResponseEntity<Contact> response = null;
        Contact savedContact=(Contact) super.readById(id).getBody();
        response=super.update(id, contact);
        Contact newContact=(Contact)  super.readById(id).getBody();
        try {
            userRest.createAuditLog(TargetOperations.CONTACT_TARGET_NAME,TargetOperations.UPDATE_OPERATION, ContactAuditUtil.compareObject(savedContact, newContact), headers.get("userId").get(0));
        }
        catch (Exception e) {
            log.warn("update;exception={}",e);
        }
        return response;
    }

    @GetMapping(produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    @Override
    public ResponseEntity<Object[]> list(HttpServletRequest request) {
        return super.list(request);
    }

    @SuppressWarnings("unchecked")
    @GetMapping(value = "/party/{id}", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Object[]> byParty(@PathVariable("id") long partyId) {
        List<Contact> list = repository.getQuery().filter("partyId", partyId).list();
        Contact[] c = (Contact[]) Array.newInstance(Contact.class, list.size());
        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache()).body(list.toArray(c));
    }


    @GetMapping(value = "/partyId/{id}", produces = {MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public ResponseEntity<Integer> isPartyIdAssociated(@PathVariable("id") long partyId) {
        int count = 0;
        try {
            count = partyRepository.partyContactSearch(partyId);
        } catch (Exception e) {
            log.warn("isPartyIdAssociated;exception={}", e);
        }
        return ResponseEntity.ok().cacheControl(org.springframework.http.CacheControl.noCache()).body(count);
    }


    @DeleteMapping(value = "/dispatchMappingContact/{id}")
    public void deleteDispatchMappingContactId(@PathVariable("id") long id, @RequestHeader org.springframework.http.HttpHeaders headers) {
        ResponseEntity<Contact> res=super.readById(id);
        Contact objectContact=(Contact) res.getBody();
        try {
            contactRepository.deleteContactById(id);
            userRest.createAuditLog(TargetOperations.CONTACT_TARGET_NAME,TargetOperations.DELETE_OPERATION, ContactAuditUtil.convertToJSON(objectContact, TargetOperations.DELETE_OPERATION), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("deleteDispatchMappingContactId;exception={}",e);
        }
    }

    @DeleteMapping(value = "/{id}")
    public void deleteById(@PathVariable("id") long id, @RequestHeader org.springframework.http.HttpHeaders headers) {
        ResponseEntity<Contact> res=super.readById(id);
        int result = 0;
        Contact objectContact=(Contact) res.getBody();
        long partyId=objectContact.getPartyId();
        try {
            result = partyRepository.partyContactSearch(partyId);
        } catch (Exception e1) {
            log.warn("deleteById;exception={}", e1);
        }
        if (result != 0) {
            logException("deleteById;exception={}", new RestException(RestErrorMessages.DELETE_NOT_ALLOWED_CONTACT,"Failed to delete resource"));
            throw new RestException(RestErrorMessages.DELETE_NOT_ALLOWED_CONTACT,"Failed to delete resource");
        }
        try {
            super.deleteById(id);
            userRest.createAuditLog(TargetOperations.CONTACT_TARGET_NAME,TargetOperations.DELETE_OPERATION, ContactAuditUtil.convertToJSON(objectContact, TargetOperations.DELETE_OPERATION), headers.get("userId").get(0));
        } catch (Exception e) {
            log.warn("deleteById;exception={}",e);
        }
    }

    @Override
    protected Set<String> getFilterColumns() {
        return FILTER_COLUMNS;
    }

    @Override
    protected Set<String> getSearchColumns() {
        return SEARCH_COLUMNS;
    }

    @Override
    protected Set<String> getOrderColumns() {
        return ORDER_COLUMNS;
    }
}
