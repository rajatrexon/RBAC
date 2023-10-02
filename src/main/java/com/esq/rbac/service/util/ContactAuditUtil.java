package com.esq.rbac.service.util;

import com.esq.rbac.service.schedulerule.domain.ScheduleRule;
import com.esq.rbac.service.targetoperations.TargetOperations;
import com.esq.rbac.service.contact.contactrole.domain.ContactRole;
import com.esq.rbac.service.contact.domain.Contact;
import com.esq.rbac.service.contact.location.domain.Location;
import com.esq.rbac.service.contact.mappingtype.domain.MappingType;
import com.esq.rbac.service.contact.party.domain.Party;
import com.esq.rbac.service.contact.party.partytype.domain.PartyType;
import com.esq.rbac.service.contact.schedule.domain.Schedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ContactAuditUtil {

    public static Map<String, String> compareObject(Object oldObject, Object newObject) {
        if (oldObject == null && newObject == null) {
            return null;
        }
        Map<String, String> comparisionString = new LinkedHashMap<String, String>();

        try{
            // if new object is null and old object is not null
            if (oldObject != null && newObject == null) {
                Field oldField[] = oldObject.getClass().getDeclaredFields();
                for (int i = 0; i < oldField.length; i++) {
                    oldField[i].setAccessible(true);
                    try {
                        Object oldValue = oldField[i].get(oldObject);
                        if ((oldField[i].getType()== List.class)) {
                            if(oldField[i].getName().equals("contacts") || oldField[i].getName().equals("rules"))
                            {
                                comparisionString.put(oldField[i].getName()+":old", Arrays.toString(((List) oldValue).toArray()));
                            }
                            else{
                                List oldChildField = null;
                                oldChildField = (List) oldField[i].get(oldObject);
                                for (int j = 0; j < oldChildField.size(); j++) {
                                    if(oldChildField!=null){
                                        Map<String, String> childValue = compareObject(
                                                oldChildField.get(j), null,
                                                oldField[i].getName());
                                        comparisionString.putAll(childValue);}
                                }
                            }
                        }
                        else
                        {
                            if(oldField[i].get(oldObject) != null
                                    && !oldField[i].getName().equals("id")){
                                comparisionString.put(oldField[i].getName()+":old", oldValue.toString());
                            }

                        }
                    }
                    catch (IllegalArgumentException e) {
                        log.warn("compareObject;Exception={}", e);
                    } catch (IllegalAccessException e) {
                        log.warn("compareObject;Exception={}", e);
                    }
                }
                log.debug("compareObject; comparisionString={}", comparisionString);
                return comparisionString;
            }
            // if new object is not null and old object is  null
            if(oldObject == null && newObject != null)
            {

                Field newField[] = newObject.getClass().getDeclaredFields();
                for (int i = 0; i < newField.length; i++) {
                    newField[i].setAccessible(true);
                    try {
                        Object newValue = newField[i].get(newObject);
                        if ((newField[i].getType()==List.class)) {
                            if(newField[i].getName().equals("contacts") || newField[i].getName().equals("rules"))
                            {
                                comparisionString.put(newField[i].getName()+":new", Arrays.toString(((List) newValue).toArray()));
                            }
                            else{
                                List newChildField = null;
                                newChildField = (List) newField[i].get(newObject);
                                for (int j = 0; j < newChildField.size(); j++) {
                                    if(!newChildField.isEmpty()){
                                        Map<String, String> childValue = compareObject(
                                                null,newChildField.get(j),
                                                newField[i].getName());
                                        comparisionString.putAll(childValue);
                                    }
                                }
                            }
                        }
                        else if((newField[i].getType()== Party.class)
                                || (newField[i].getType()== Contact.class)
                                || (newField[i].getType()== Location.class)
                                || (newField[i].getType()== PartyType.class)
                                || (newField[i].getType()== MappingType.class)
                                || (newField[i].getType()== ContactRole.class)
                                || (newField[i].getType()== Schedule.class)
                                || (newField[i].getType()== ScheduleRule.class))
                        {

                            Map<String, String> childValue = compareObject(
                                    null,newValue,
                                    newField[i].getName());
                            comparisionString.putAll(childValue);
                        }
                        else if(newField[i].get(newObject) != null
                                && !newField[i].getName().equals("id")){
                            comparisionString.put(newField[i].getName()+":new", newValue.toString());
                        }
                    }
                    catch (IllegalArgumentException e) {
                        log.warn("compareObject;Exception={}", e);
                    } catch (IllegalAccessException e) {
                        log.warn("compareObject;Exception={}", e);
                    }
                }
                log.debug("compareObject; comparisionString={}", comparisionString);

                return comparisionString;
            }

            //for both objects are not null
            Field oldField[] = oldObject.getClass().getDeclaredFields();
            Field newField[] = newObject.getClass().getDeclaredFields();

            if (oldObject.getClass().isInstance(oldObject)
                    && oldField.length == newField.length) {

                for (int i = 0; i < oldField.length; i++) {

                    oldField[i].setAccessible(true);
                    newField[i].setAccessible(true);
                    try {
                        Object oldValue = oldField[i].get(oldObject);
                        Object newValue = newField[i].get(newObject);

                        if (oldValue!=null && newValue!=null && newField[i].getType()==List.class && oldField[i].getType()==List.class) {
                            if (newField[i].getName().equals("contacts")
                                    || oldField[i].getName().equals("contacts") || newField[i].getName().equals("rules")
                                    || oldField[i].getName().equals("rules")) {
                                comparisionString.put(newField[i].getName()+":new",Arrays.toString(((List) newValue).toArray()));
                                comparisionString.put(oldField[i].getName()+":old", Arrays.toString(((List) oldValue).toArray()));
                            }
                            else{
                                List oldChildField = null;
                                List newChildField = null;
                                oldChildField = (List) oldField[i].get(oldObject);
                                newChildField = (List) newField[i].get(newObject);

                                if (oldChildField.size() == 0 && !newChildField.isEmpty() ) {
                                    for (int j = 0; j < newChildField.size(); j++) {
                                        Map<String, String> childValue = compareObject(null,
                                                newChildField.get(j),
                                                oldField[i].getName());
                                        comparisionString.putAll(childValue);
                                    }
                                }
                                else{
                                    for (int j = 0; j < oldChildField.size(); j++) {
                                        if (newChildField.size() != 0) {
                                            Map<String,String> childValue = compareObject(
                                                    oldChildField.get(j),
                                                    newChildField.get(j),
                                                    oldField[i].getName());
                                            comparisionString.putAll(childValue);
                                        } else {
                                            Map<String,String> childValue = compareObject(
                                                    oldChildField.get(j),
                                                    null,
                                                    oldField[i].getName());
                                            comparisionString.putAll(childValue);
                                        }
                                    }
                                }
                            }
                        } else {
                            if((oldField[i].getType()==ContactRole.class)
                                    || (oldField[i].getType()==Schedule.class)
                                    || (oldField[i].getType()==MappingType.class)
                                    || (oldField[i].getType()==PartyType.class)
                                    || (oldField[i].getType()==Party.class)
                                    || (oldField[i].getType()==Location.class)
                                    || (oldField[i].getType()==Contact.class)
                                    || (newField[i].getType()==Contact.class)
                                    || (newField[i].getType()==Location.class)
                                    || (newField[i].getType()==Party.class)
                                    || (newField[i].getType()==PartyType.class)
                                    || (newField[i].getType()==MappingType.class)
                                    || (newField[i].getType()==ContactRole.class)
                                    || (newField[i].getType()==Schedule.class)
                                    || (newField[i].getType()==ScheduleRule.class))
                            {

                                Map<String,String> childValue = compareObject(
                                        oldValue,
                                        newValue,
                                        newField[i].getName());
                                if(childValue!=null && !childValue.isEmpty()){
                                    comparisionString.putAll(childValue);
                                }
                            }
                            else if (oldField[i].get(oldObject) != null
                                    && newField[i].get(newObject) != null
                                    && !newField[i].getName().equals("id")
                                    && !oldValue.toString().equals(
                                    newValue.toString())) {
                                comparisionString.put(newField[i].getName()+":new", newValue.toString());
                                comparisionString.put(oldField[i].getName()+":old", oldValue.toString());
                            } else if (oldValue == null && newValue != null && !(newField[i].getName().equals("id"))) {
                                comparisionString.put(newField[i].getName()+":new", newValue.toString());
                                comparisionString.put(oldField[i].getName()+":old", "");
                            } else if (oldValue != null && newValue == null && !(oldField[i].getName().equals("id"))) {
                                comparisionString.put(newField[i].getName()+":new", "");
                                comparisionString.put(oldField[i].getName()+":old", oldValue.toString());
                            }

                        }
                    } catch (IllegalArgumentException e) {
                        log.warn("compareObject;Exception={}", e);
                    } catch (IllegalAccessException e) {
                        log.warn("compareObject;Exception={}", e);
                    }
                }
            }

            log.debug("compareObject; comparisionString={}", comparisionString);
        }catch (Exception e) {
            log.warn("compareObject;Exception={}", e);
        }
        return comparisionString;

    }

    public static Map<String,String> compareObject(Object oldObject, Object newObject,
                                                   String parent) {
        if (oldObject == null && newObject == null) {
            return null;
        }
        Map<String, String> comparisionString = new LinkedHashMap<String, String>();
        try{
            if (newObject == null && oldObject != null) {
                Field oldField[] = oldObject.getClass().getDeclaredFields();
                for (int i = 0; i < oldField.length; i++) {
                    oldField[i].setAccessible(true);
                    Object oldValue = null;
                    try {
                        oldValue = oldField[i].get(oldObject);
                    } catch (IllegalArgumentException e) {
                        log.warn("compareObject;Exception={}", e);
                    } catch (IllegalAccessException e) {
                        log.warn("compareObject;Exception={}", e);
                    }
                    if(oldValue!=null){
                        comparisionString.put(parent+"."+oldField[i].getName()+":new", "");
                        comparisionString.put(parent+"."+oldField[i].getName()+":old", oldValue.toString());
                    }
                }
                return comparisionString;

            }

            if (newObject != null && oldObject == null) {
                Field newField[] = newObject.getClass().getDeclaredFields();
                for (int i = 0; i < newField.length; i++) {
                    newField[i].setAccessible(true);

                    Object newValue = null;
                    try {
                        newValue = newField[i].get(newObject);
                        if (newField[i].getType()==List.class ) {
                            List newChildField = null;
                            newChildField = (List) newField[i].get(newObject);
                            if(!newChildField.isEmpty()){
                                for (int j = 0; j < newChildField.size(); j++) {
                                    Map<String,String> childValue = compareObject(null,
                                            newChildField.get(j), parent + "."
                                                    + newField[i].getName());
                                    comparisionString.putAll(childValue);
                                }
                            }
                        }
                        else if((newField[i].getType()==Party.class)
                                || (newField[i].getType()==Contact.class)
                                || (newField[i].getType()==Location.class)
                                || (newField[i].getType()==PartyType.class)
                                || (newField[i].getType()==MappingType.class)
                                || (newField[i].getType()==ContactRole.class)
                                || (newField[i].getType()==Schedule.class)
                                || (newField[i].getType()==ScheduleRule.class))
                        {

                            Map<String,String> childValue = compareObject(null,
                                    newValue, parent + "."
                                            + newField[i].getName());
                            if(childValue!=null && !childValue.isEmpty()){
                                comparisionString.putAll(childValue);
                            }
                        }
                        else if(newValue!=null && !newField[i].getName().equals("id")){
                            comparisionString.put(parent+"."+newField[i].getName()+":new", newValue.toString());
                            comparisionString.put(parent+"."+newField[i].getName()+":old","");
                        }
                    }
                    catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }

                }
                return comparisionString;
            }

            Field oldField[] = oldObject.getClass().getDeclaredFields();
            Field newField[] = newObject.getClass().getDeclaredFields();

            if (oldObject.getClass().isInstance(oldObject)
                    && oldField.length == newField.length) {

                for (int i = 0; i < oldField.length; i++) {

                    oldField[i].setAccessible(true);
                    newField[i].setAccessible(true);
                    try {
                        Object oldValue = oldField[i].get(oldObject);
                        Object newValue = newField[i].get(newObject);

                        if (newField[i].getType()==List.class && oldField[i].getType()==List.class) {
                            List oldChildField = null;
                            List newChildField = null;
                            oldChildField = (List) oldField[i].get(oldObject);
                            newChildField = (List) newField[i].get(newObject);
                            if (oldChildField.size() == 0 && !newChildField.isEmpty()) {
                                for (int j = 0; j < newChildField.size(); j++) {
                                    Map<String, String> childValue = compareObject(
                                            oldChildField.get(j), newChildField.get(j),
                                            parent + "." + oldField[i].getName());
                                    comparisionString.putAll(childValue);

                                }
                            }
                        }
                        else if((oldField[i].getType()==ContactRole.class)
                                || (oldField[i].getType()==Schedule.class)
                                || (oldField[i].getType()==MappingType.class)
                                || (oldField[i].getType()==PartyType.class)
                                || (oldField[i].getType()==Party.class)
                                || (oldField[i].getType()==Location.class)
                                || (oldField[i].getType()==Contact.class)
                                || (newField[i].getType()==Contact.class)
                                || (newField[i].getType()==Location.class)
                                || (newField[i].getType()==Party.class)
                                || (newField[i].getType()==PartyType.class)
                                || (newField[i].getType()==MappingType.class)
                                || (newField[i].getType()==ContactRole.class)
                                || (newField[i].getType()==Schedule.class)
                                || (newField[i].getType()==ScheduleRule.class))
                        {

                            Map<String, String> childValue = compareObject(
                                    oldValue, newValue,
                                    parent + "." + newField[i].getName());
                            if(childValue!=null && !childValue.isEmpty()){
                                comparisionString.putAll(childValue);
                            }
                        }else	if (oldField[i].get(oldObject) != null
                                && newField[i].get(newObject) != null
                                && !newField[i].getName().equals("id")
                                && !oldValue.toString().equals(
                                newValue.toString())) {
                            comparisionString.put(parent+"."+newField[i].getName()+":new", newValue.toString());
                            comparisionString.put(parent+"."+oldField[i].getName()+":old", oldValue.toString());
                        } else if (oldValue == null
                                && !newField[i].getName().equals("id") && newValue!=null) {
                            comparisionString.put(parent+"."+newField[i].getName()+":new", newValue.toString());
                            comparisionString.put(parent+"."+oldField[i].getName()+":old", "");
                        } else if (oldValue != null && newValue == null) {
                            comparisionString.put(parent+"."+newField[i].getName()+":new", "");
                            comparisionString.put(parent+"."+oldField[i].getName()+":old", oldValue.toString());
                        }

                    } catch (IllegalArgumentException e) {
                        log.warn("compareObject;Exception={}", e);
                    } catch (IllegalAccessException e) {
                        log.warn("compareObject;Exception={}", e);
                    }
                }
            }
        }catch(Exception e)
        {
            log.warn("compareObject;Exception={}", e);
        }
        return comparisionString;

    }

    public static Map<String, String> convertToJSON(Object object, String targetOperation) {

        if (object == null) {
            return null;
        }
        Map<String, String> createString = new LinkedHashMap<String, String>();
        try{
            for (Field field : object.getClass().getDeclaredFields()) {
                field.setAccessible(true);
                Object value = null;
                try {
                    value = field.get(object);
                    if (field.getType()==List.class) {
                        List newChildField = null;
                        newChildField = (List) field.get(object);
                        if(!newChildField.isEmpty()){
                            for (Object newChildFieldVal : newChildField) {
                                Map<String, String> childValue = convertToJSON(newChildFieldVal,
                                        targetOperation, field.getName());
                                createString.putAll(childValue);
                            }
                        }
                    } else {
                        if(value!=null && (field.getType()==ContactRole.class ||
                                (field.getType()==PartyType.class)||(field.getType()==ScheduleRule.class)|| field.getType()==Schedule.class  || field.getType()==MappingType.class))
                        {
                            Map<String, String> childValue = convertToJSON(value,
                                    targetOperation, field.getName());
                            createString.putAll(childValue);
                        }
                        else if (value != null && !value.toString().contains("[]")
                                && !field.getName().equals("id")
                                && !value.toString().equals("")) {
                            if (targetOperation.equals(TargetOperations.CREATE_OPERATION)) {
                                createString.put(field.getName()+":new", value.toString());
                            } else if (targetOperation.equals(TargetOperations.DELETE_OPERATION)
                                    || targetOperation.equals(TargetOperations.DEACTIVATE_OPERATION)) {
                                createString.put(field.getName()+":old", value.toString());
                            }

                        }

                    }
                } catch (IllegalArgumentException e) {
                    log.warn("convertToJSON;Exception={}", e);
                } catch (IllegalAccessException e) {
                    log.warn("convertToJSON;Exception={}", e);
                }

            }
            log.debug("convertToJSON; createString={}", createString);
        }catch(Exception e)
        {
            log.warn("convertToJSON;Exception={}", e);
        }
        return createString;
    }

    public static Map<String, String> convertToJSON(Object object, String targetOperation,
                                                    String parent) {

        if (object == null) {
            return null;
        }
        Map<String,String> createString = new LinkedHashMap<String, String>();
        try{
            Field objectField[] = object.getClass().getDeclaredFields();
            for (int i = 0; i < objectField.length; i++) {
                objectField[i].setAccessible(true);

                Object objectVal = objectField[i].get(object);

                if ((objectField[i].getType()==List.class)) {

                    List newChildField = null;
                    newChildField = (List) objectField[i].get(object);
                    if(!newChildField.isEmpty()){
                        for (Object newChildFieldVal : newChildField) {
                            Map<String, String> childValue = convertToJSON(newChildFieldVal,
                                    targetOperation, objectField[i].getName());
                            createString.putAll(childValue);
                        }
                    }

                }
                else{

                    if (objectVal != null && !objectVal.toString().contains("[]")
                            && !objectField[i].getName().equals("id")
                            && !objectVal.toString().equals("")) {
                        if (targetOperation.equals(TargetOperations.CREATE_OPERATION)) {
                            createString.put(parent + "."+objectField[i].getName().toString()+":new", objectVal.toString());
                        } else if (targetOperation.equals(TargetOperations.DELETE_OPERATION)
                                || targetOperation.equals(TargetOperations.DEACTIVATE_OPERATION)) {
                            createString.put(parent + "."+objectField[i].getName().toString()+":old", objectVal.toString());

                        }
                    }
                }

            }
        } catch (Exception e) {
            log.warn("convertToJSON;Exception={}", e);
        }
        return createString;
    }
}
