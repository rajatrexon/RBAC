/*
 * Copyright (c)2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.esq.rbac.service.util;

import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.restriction.domain.Restriction;
import com.esq.rbac.service.role.operationsubdomain.operationlookup.OperationLookup;
import com.esq.rbac.service.scope.scopedefinition.domain.ScopeDefinition;
import java.util.*;


public class AuditLogHelperUtil {
   
    protected String fromHour, toHour;
    private Map<String, String> objectChanges = Collections.synchronizedMap(new TreeMap<String, String>());
   
    protected static final String OBJECTCHANGES_OLD_TEXT = ":old";
    protected static final String OBJECTCHANGES_NEW_TEXT = ":new";
    protected static final String OBJECTNAME = "name";
    protected static final String OBJECTCHANGES_GROUPNAME = "groupName";
    protected static final String OBJECTCHANGES_GROUP_ATTRIBUTES = "groupAttributes";
    protected static final String OBJECTCHANGES_USERGROUPASSIGN = "userGroupAssignment";
    protected static final String OBJECTCHANGES_ROLEIDS = "roleIds";
    protected static final String OBJECTCHANGES_OPERATIONIDS = "operationIds";
    protected static final String OBJECTCHANGES_TARGETS = "targets";
    protected static final String OBJECTCHANGES_TARGETS_NAME = "targets.name";
    protected static final String OBJECTCHANGES_TARGETS_KEY = "targets.targetKey";
    protected static final String OBJECTCHANGES_TARGETS_DESCRIPTION = "targets.description";
    protected static final String OBJECTCHANGES_TARGETS_LABEL = "targets.labels";
    protected static final String OBJECTCHANGES_OPERATIONS= "operations";
    protected static final String OBJECTCHANGES_OPERATIONS_NAME = "targets.operations.name";
    protected static final String OBJECTCHANGES_OPERATIONS_KEY = "targets.operations.operationKey";
    protected static final String OBJECTCHANGES_OPERATIONS_DESCRIPTION = "targets.operations.description";
    protected static final String OBJECTCHANGES_OPERATIONS_LABEL = "targets.operations.labels";
    protected static final String OBJECTCHANGES_OPERATIONS_SCOPENAMES = "targets.operations.scopeName";
    protected static final String OBJECTCHANGES_ISENABLED = "isEnabled";
    protected static final String OBJECTCHANGES_FIRSTNAME = "firstName";
    protected static final String OBJECTCHANGES_LASTNAME = "lastName";
    protected static final String OBJECTCHANGES_USERNAME = "userName";
    protected static final String OBJECTCHANGES_PASSWORD = "password";
    protected static final String OBJECTCHANGES_CHANGE_PASSWORD_LOGON = "changePasswordOnLoggon";
    protected static final String OBJECTCHANGES_EMAIL = "eMail";
    protected static final String OBJECTCHANGES_NOTES = "notes";
    protected static final String OBJECTCHANGES_ISLOCKED = "isLocked";
    protected static final String OBJECTCHANGES_LABELS = "labels";
    protected static final String OBJECTCHANGES_VARIABLE = "variable.";
    protected static final String OBJECTCHANGES_IDENTITY = "identity.";
    protected static final String OBJECTCHANGES_RESTRICTIONS = "restrictions.";
    protected static final String OBJECTCHANGES_RESTRICTIONS_TIMEZONE = "timeZone";
    protected static final String OBJECTCHANGES_RESTRICTIONS_FROMDATE = "allowFrom";
    protected static final String OBJECTCHANGES_RESTRICTIONS_TODATE = "allowUntil";
    protected static final String OBJECTCHANGES_RESTRICTIONS_HOURS = "hours";
    protected static final String OBJECTCHANGES_RESTRICTIONS_DAYOFWEEK = "dayOfWeek";
    protected static final String OBJECTCHANGES_RESTRICTIONS_ALLOWEDIPS = "allowedIPs";
    protected static final String OBJECTCHANGES_RESTRICTIONS_DISALLOWEDIPS = "dissalowedIps";
    protected static final String OBJECTCHANGES_APPNAME = "applicationName";
    protected static final String OBJECTCHANGES_ROLENAME = "roleName";
    protected static final String OBJECTCHANGES_SCOPENAME = "scopeName";
    protected static final String OBJECTCHANGES_SCOPEKEY = "scopeKey";
    protected static final String OBJECTCHANGES_DESCRIPTION = "description";
    protected static final String OBJECTCHANGES_HOMEURL = "homeUrl";
    protected static final String OBJECTCHANGES_SERVICEURL = "serviceUrl";
    protected static final String OBJECTCHANGES_OPERATIONNAMES = "Operation Names";
    protected static final String OBJECTCHANGES_ISMANDATORY = "isMandatory";
    protected static final String OBJECTCHANGES_SSOALLOWED = "ssoAllowed";
    protected static final String OBJECTCHANGES_APPID = "applicationId";
    protected static final String OBJECTCHANGES_USERID = "userId";
    protected static final String OBJECTCHANGES_GROUPID = "groupId";
    protected static final String OBJECTCHANGES_SCOPEID = "scopeId";
    protected static final String OBJECTCHANGES_ROLEID = "roleId";
    protected static final String OBJECTCHANGES_USER_IMAGE= "userImage";
    protected static final String OBJECTCHANGES_SCOPEDEFINE= "scopeDefinitions";
    protected static final String OBJECTCHANGES_MAINTENANCEID="maintenanceId";
    protected static final String OBJECTCHANGES_FROMDATE="fromDate";
    protected static final String OBJECTCHANGES_TODATE="toDate";
    protected static final String OBJECTCHANGES_MESSAGE="message";
    protected static final String OBJECTCHANGES_MAINTENANCEISENABLED="maintenanceIsEnabled";
    
   
    public  Map<String, String> getObjectChangeSet() {
        return objectChanges;
    }

    protected  void clearObjectChangeSet() {
        objectChanges.clear();
    }

    public void putToObjectChangeSetIgnoreNulls(String objChangeStr, String objChangeParam, Object oldVal, String oldValOut, Object newVal, String newValOut) {
        if(oldVal!=null || oldValOut!=null){
    		objectChanges.put(objChangeStr + (objChangeParam != null ? objChangeParam : "") + OBJECTCHANGES_OLD_TEXT, oldValOut != null ? oldValOut : (oldVal != null ? oldVal.toString() : ""));
    	}
    	if(newVal!=null || newValOut!=null){
    		objectChanges.put(objChangeStr + (objChangeParam != null ? objChangeParam : "") + OBJECTCHANGES_NEW_TEXT, newValOut != null ? newValOut : (newVal != null ? newVal.toString() : ""));
    	}
    }
    
    public  void putToObjectChangeSet(String key, String value) {
        objectChanges.put((key != null) ? key : "", (value != null) ? value : "");
    }

    public void putToObjectChangeSet(String objChangeStr, String objChangeParam, Object oldVal, String oldValOut, Object newVal, String newValOut) {
        objectChanges.put(objChangeStr + (objChangeParam != null ? objChangeParam : "") + OBJECTCHANGES_OLD_TEXT, oldValOut != null ? oldValOut : (oldVal != null ? oldVal.toString() : ""));
        objectChanges.put(objChangeStr + (objChangeParam != null ? objChangeParam : "") + OBJECTCHANGES_NEW_TEXT, newValOut != null ? newValOut : (newVal != null ? newVal.toString() : ""));
    }

    @SuppressWarnings({ "rawtypes" })
    public void checkPutToObjectChangeSet(String objChangeStr, Object newVal, Object oldVal, String newValOut, String oldValOut) {
        if (newVal != null && oldVal != null) {
            if (newVal instanceof Integer && oldVal instanceof Integer) {
                if (!newVal.equals(oldVal)) {
                    putToObjectChangeSet(objChangeStr, null, oldVal, oldValOut, newVal, newValOut);
                }
            } else {
                if (!newVal.equals(oldVal) && !(newVal instanceof Collection) && !(oldVal instanceof Collection)) {
                    putToObjectChangeSet(objChangeStr, null, oldVal, oldValOut, newVal, newValOut);
                }  
                if (newVal instanceof Collection && oldVal instanceof Collection) {
                    newVal = (((Collection) newVal).size() > 0) ? newVal : "";
                    oldVal = (((Collection) oldVal).size() > 0) ? oldVal : "";
                    putToObjectChangeSet(objChangeStr, null, oldVal, oldValOut, newVal, newValOut);
                }

            }
        } else if (newVal != null && oldVal == null) {
            if (newVal instanceof String && newVal.toString().isEmpty()) {
                return;
            }
            putToObjectChangeSet(objChangeStr, null, "", null, newVal, newValOut);
        } else if (newVal == null && oldVal != null) {
            putToObjectChangeSet(objChangeStr, null, oldVal, oldValOut, "", null);
        }
    }
    
    @SuppressWarnings({ "rawtypes" })
    public void checkPutToObjectChangeSetIgnoreNulls(String objChangeStr, Object newVal, Object oldVal, String newValOut, String oldValOut) {
        if (newVal != null && oldVal != null) {
            if (newVal instanceof Integer && oldVal instanceof Integer) {
                if (!newVal.equals(oldVal)) {
                    putToObjectChangeSetIgnoreNulls(objChangeStr, null, oldVal, oldValOut, newVal, newValOut);
                }
            } else {
                if (!newVal.equals(oldVal) && !(newVal instanceof Collection) && !(oldVal instanceof Collection)) {
                	putToObjectChangeSetIgnoreNulls(objChangeStr, null, oldVal, oldValOut, newVal, newValOut);
                }  
                if (newVal instanceof Collection && oldVal instanceof Collection) {
                    newVal = (((Collection) newVal).size() > 0) ? newVal : null;
                    oldVal = (((Collection) oldVal).size() > 0) ? oldVal : null;
                    putToObjectChangeSetIgnoreNulls(objChangeStr, null, oldVal, oldValOut, newVal, newValOut);
                }

            }
        } else if (newVal != null && oldVal == null) {
            if (newVal instanceof String && newVal.toString().isEmpty()) {
                return;
            }
            putToObjectChangeSetIgnoreNulls(objChangeStr, null, null, null, newVal, newValOut);
        } else if (newVal == null && oldVal != null) {
        	putToObjectChangeSetIgnoreNulls(objChangeStr, null, oldVal, oldValOut, null, null);
        }
    }

    protected String getFullDays(String days) {
        String[] arrayDays = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        String daysOfWeek = "";
        for (int i = 0; i < days.length(); i++) {
            if (days.charAt(i) != '-') {
                if (daysOfWeek != "") {
                    daysOfWeek += ",";
                }
                daysOfWeek += arrayDays[i];
            }
        }
        if (daysOfWeek.length() > 0) {
            return daysOfWeek;
        } else {
            return "";
        }
    }

    protected void setHour(int count, String minute) {
        if (this.fromHour == "") {
             this.fromHour = "" + count + "." + minute;
        } else {
             this.toHour = "" + count + "." + minute;
        }
    }

    protected String getHours(String hours) {

        this.fromHour = ""; this.toHour = "";
        String hour = "";
        for (int i = 0; i < hours.length(); i++) {
            if (hours.charAt(i) == '1') {
                this.setHour(i,"00");
            }else if (hours.charAt(i) == '2') {
                this.setHour((i-1),"30");
            }else if (hours.charAt(i) == '3') {
                this.setHour(i,"30");
            }
            if (hours.charAt(i) == '0' || i == 23) {
                if (this.fromHour != "" && this.toHour != "") {
                    if (hour != "") {
                        hour += ", ";
                    }
                    hour += "" + this.fromHour + "-" + this.toHour;

                } else if (this.fromHour != "" && this.toHour == "") {
                    if (hour != "") {
                        hour += ", ";
                    }
                    hour += "" + this.fromHour;
                }
                this.fromHour = "";
                this.toHour = "";
            }
        }
        return hour;
    }

    public void checkMapPutToObjectChangeSet(Map<String, String> newMap, Map<String, String> oldMap) {
        for (Map.Entry<String, String> newEntry : newMap.entrySet()) {
            if (!oldMap.containsKey(newEntry.getKey())) {
                putToObjectChangeSet(OBJECTCHANGES_VARIABLE, newEntry.getKey(), null, null, newEntry.getValue(), null);
            } else if (oldMap.containsKey(newEntry.getKey())) {
                if (!newMap.get(newEntry.getKey()).equals(oldMap.get(newEntry.getKey()))) {
                    putToObjectChangeSet(OBJECTCHANGES_VARIABLE, newEntry.getKey(), oldMap.get(newEntry.getKey()), null, newEntry.getValue(), null);
                }
            }
        }
        for (Map.Entry<String, String> oldEntry : oldMap.entrySet()) {
            if (!newMap.containsKey(oldEntry.getKey())) {
                putToObjectChangeSet(OBJECTCHANGES_VARIABLE, oldEntry.getKey(), oldEntry.getValue(), null, null, null);
            }
        }
    }

    public void checkRestrictionPutToObjectChangeSet(Restriction newRestriction, Restriction oldRestriction) {
        checkPutToObjectChangeSet(OBJECTCHANGES_RESTRICTIONS_TIMEZONE,
                (newRestriction != null && newRestriction.getTimeZone() != null ? newRestriction.getTimeZone() : ""),
                (oldRestriction != null && oldRestriction.getTimeZone() != null ? oldRestriction.getTimeZone() : ""),
                null,
                null);
        checkPutToObjectChangeSet(OBJECTCHANGES_RESTRICTIONS_FROMDATE,
                (newRestriction != null && newRestriction.getFromDate() != null ? newRestriction.getFromDate() : ""),
                (oldRestriction != null && oldRestriction.getFromDate() != null ? oldRestriction.getFromDate() : ""),
                null,
                null);
        checkPutToObjectChangeSet(OBJECTCHANGES_RESTRICTIONS_TODATE,
                (newRestriction != null && newRestriction.getToDate() != null ? newRestriction.getToDate() : ""),
                (oldRestriction != null && oldRestriction.getToDate() != null ? oldRestriction.getToDate() : ""),
                null,
                null);
        checkPutToObjectChangeSet(OBJECTCHANGES_RESTRICTIONS_HOURS,
                (newRestriction != null && newRestriction.getHours() != null ? getHours(newRestriction.getHours()) : ""),
                (oldRestriction != null && oldRestriction.getHours() != null ? getHours(oldRestriction.getHours()) : ""),
                null,
                null);
        checkPutToObjectChangeSet(OBJECTCHANGES_RESTRICTIONS_DAYOFWEEK,
                (newRestriction != null && newRestriction.getDayOfWeek() != null ? getFullDays(newRestriction.getDayOfWeek()) : ""),
                (oldRestriction != null && oldRestriction.getDayOfWeek() != null ? getFullDays(oldRestriction.getDayOfWeek()) : ""),
                null,
                null);
        checkPutToObjectChangeSet(OBJECTCHANGES_RESTRICTIONS_ALLOWEDIPS,
                (newRestriction != null && newRestriction.getAllowedIPs() != null && newRestriction.getAllowedIPs().size() > 0 ? newRestriction.getAllowedIPs() : ""),
                (oldRestriction != null && oldRestriction.getAllowedIPs() != null && oldRestriction.getAllowedIPs().size() > 0 ? oldRestriction.getAllowedIPs() : ""),
                null,
                null);
        checkPutToObjectChangeSet(OBJECTCHANGES_RESTRICTIONS_DISALLOWEDIPS,
                (newRestriction != null && newRestriction.getDisallowedIPs() != null && newRestriction.getDisallowedIPs().size() > 0 ? newRestriction.getDisallowedIPs() : ""),
                (oldRestriction != null && oldRestriction.getDisallowedIPs() != null && oldRestriction.getDisallowedIPs().size() > 0 ? oldRestriction.getDisallowedIPs() : ""),
                null,
                null);
    }
    
    protected String getRoleNames(Set<Integer> roleIds) {
        String roleNames = "";
        for(Integer i : roleIds)
        {
           //Role tmp = em.find(Role.class, i.intValue());
        	String roleName= Lookup.getRoleName(i.intValue());
           roleNames+= roleName + "," ;
        }
        return roleNames.equals("") ? "" : roleNames.substring(0,roleNames.length() - 1);
    }
    
    public void checkRoleIdsPutToObjectChangeSet(Set<Integer> newRoleIds, Set<Integer> oldRoleIds) {
    	 newRoleIds = new TreeSet<Integer>(newRoleIds);
    	 oldRoleIds = new TreeSet<Integer>(oldRoleIds);
         checkPutToObjectChangeSet(OBJECTCHANGES_ROLEIDS,
                (newRoleIds != null ? getRoleNames(newRoleIds) : ""),
                (oldRoleIds != null ? getRoleNames(oldRoleIds) : ""),
                null,
                null);
        
    }
	
	 protected String getTargetOperationName(Integer operationId) {
		 return OperationLookup.getTargetOperationName(operationId);
	    }
	    
		protected  void checkOperationIdsPutToObjectChangeSet(Set<Integer> newOperationIds, Set<Integer> oldOperationIds) {
			if (oldOperationIds==null || oldOperationIds.isEmpty()) {
				if(newOperationIds!=null && !newOperationIds.isEmpty()){
					for (Integer i : newOperationIds) {
						objectChanges.put((getTargetOperationName(i) != null) ? getTargetOperationName(i) + ":new" : "", "Allow");
					}
				}
			} else {
				if(newOperationIds!=null && !newOperationIds.isEmpty()){
					for (Integer i : newOperationIds) {
						if (!oldOperationIds.contains(i)) {
							objectChanges.put((getTargetOperationName(i) != null) ? getTargetOperationName(i) + ":new" : "", "Allow");
							objectChanges .put((getTargetOperationName(i) != null) ? getTargetOperationName(i) + ":old" : "", "Disallow");
						}
					}
				}
				for (Integer j : oldOperationIds) {
					if (newOperationIds!=null && !newOperationIds.contains(j)) {
						objectChanges.put((getTargetOperationName(j) != null) ? getTargetOperationName(j) + ":new" : "", "Disallow");
						objectChanges.put((getTargetOperationName(j) != null) ? getTargetOperationName(j) + ":old" : "", "Allow");
					}
				}
			}
		}
    
		  public void checkScopeNamePutToObjectChangeSet(Set<ScopeDefinition> newScopeName, Set<ScopeDefinition> oldScopeName)
		    {
				Set<String> keys = new HashSet<String>();
				Map<String, ScopeDefinition> newScopeNameMap = new HashMap<String, ScopeDefinition>();
				if (newScopeName != null && !newScopeName.isEmpty()) {
					for (ScopeDefinition sd : newScopeName) {
						keys.add(sd.getGroupId() + "_" + sd.getScopeId());
						newScopeNameMap.put(sd.getGroupId() + "_" + sd.getScopeId(), sd);
					}
				}

				Map<String, ScopeDefinition> oldScopeNameMap = new HashMap<String, ScopeDefinition>();
				if (oldScopeName != null && !oldScopeName.isEmpty()) {
					for (ScopeDefinition sd : oldScopeName) {
						keys.add(sd.getGroupId() + "_" + sd.getScopeId());
						oldScopeNameMap.put(sd.getGroupId() + "_" + sd.getScopeId(), sd);
					}
				}

				Map<String, String> changedScopMap = new HashMap<String, String>();
				Map<String, String> originalScopeMap = new HashMap<String, String>();
				if (keys != null && !keys.isEmpty()) {
					for (String key : keys) {

						if (oldScopeNameMap.containsKey(key) && newScopeNameMap.containsKey(key)) {
							// exists in old and new definition
							// check for scope definition equivalence
                            ScopeDefinition sdOld = oldScopeNameMap.get(key);
                            ScopeDefinition sdNew = newScopeNameMap.get(key);
							if (!sdOld.getScopeDefinition().equalsIgnoreCase(sdNew.getScopeDefinition())) {
								// Definition are not equal, add to auditlog change object
								changedScopMap.put(Lookup.getScopeName(sdNew.getScopeId()), sdNew.getScopeDefinition());
								originalScopeMap.put(Lookup.getScopeName(sdOld.getScopeId()), sdOld.getScopeDefinition());
							}
						}

						if (oldScopeNameMap.containsKey(key) && !newScopeNameMap.containsKey(key)) {
							// exists in old and but not in new
                            ScopeDefinition sdOld = oldScopeNameMap.get(key);
							originalScopeMap.put(Lookup.getScopeName(sdOld.getScopeId()), sdOld.getScopeDefinition());
						}

						if (!oldScopeNameMap.containsKey(key) && newScopeNameMap.containsKey(key)) {
							// does not exists in old and but exists in new
                            ScopeDefinition sdNew = newScopeNameMap.get(key);
							changedScopMap.put(Lookup.getScopeName(sdNew.getScopeId()), sdNew.getScopeDefinition());
						}

					}

				}

				checkPutToObjectChangeSet(OBJECTCHANGES_SCOPEDEFINE,
						(changedScopMap != null && !changedScopMap.isEmpty() ? changedScopMap : ""),
						(originalScopeMap != null && !originalScopeMap.isEmpty() ? originalScopeMap : ""), null, null);
			}
		  
	/*
	 * private Map<String, String> getScopeNameDefinitions(Set<ScopeDefinition>
	 * newScopeName){ Map<String, String> newScopeNameMap = null;
	 * if(newScopeName!=null && !newScopeName.isEmpty()){ newScopeNameMap = new
	 * HashMap<String, String>(); for(ScopeDefinition sd:newScopeName){
	 * newScopeNameMap.put(Lookup.getScopeName(sd.getScopeId()),
	 * sd.getScopeDefinition()); } } return newScopeNameMap; }
	 */
		
	protected void checkObjectPutToObjectChangeSet(String text) {
		checkPutToObjectChangeSet(text, " ", "", null, null);
	}


}