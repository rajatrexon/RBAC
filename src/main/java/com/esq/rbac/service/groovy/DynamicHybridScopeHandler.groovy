package com.esq.rbac.service.groovy

import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo
import com.esq.rbac.service.group.domain.Group
import com.esq.rbac.service.group.service.GroupDalJpa
import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal
import com.esq.rbac.service.scope.builder.ScopeBuilder
import com.esq.rbac.service.scope.scopedefinition.domain.ScopeDefinition
import com.esq.rbac.service.scope.service.ScopeDalJpa
import com.esq.rbac.service.user.service.UserDalJpa
import com.esq.rbac.service.util.dal.OptionFilter
import com.esq.rbac.service.util.dal.Options
import com.esq.rbac.service.util.externaldatautil.HybridScopeHandler
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.json.JsonSlurper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service


@Service
public class DynamicHybridScopeHandler implements HybridScopeHandler {

    private static final Logger log = LoggerFactory.getLogger(DynamicHybridScopeHandler.class);

    OrganizationMaintenanceDal orgMaintenanceDal;
    def springContext
    ScopeBuilder scopeBuilder
    String reportPortalScopeKey = "RPDefineSharedReports";
    String RP_SHARED_REPORTS_KEY = "RPSharedReports";
    String RP_REPORTS_KEY = "RPReports";
    String posTaScopeKey = "DefinePOSStatsView";
    String POSTA_SHARED_VIEWS_KEY = "POSStatsView";
    String atmTaScopeKey = "DefineATMStatsView";
    String ATMTA_SHARED_VIEWS_KEY = "ATMStatsView";

    public void setApplicationContext(ApplicationContext ac)
            throws BeansException {
        springContext = ac;
    }

    public void setReportPortalScopeKey(String scopeKey)
    {
        reportPortalScopeKey = scopeKey;
    }

    public void setPosTaScopeKey(String scopeKey)
    {
        posTaScopeKey = scopeKey;
    }

    public void setAtmTaScopeKey(String scopeKey)
    {
        atmTaScopeKey = scopeKey;
    }

    public String getFilterKeyData(String sourcePath, String dataKey,
                                   String scopeKey, String userName, String additionalMap, String parentValue){
        if(sourcePath!=null && sourcePath.equalsIgnoreCase("rbacTenantGroups")){
            List<Map<String, Object>> resultList = new LinkedList<Map<String, Object>>();
            def userDal = springContext.getBean(UserDalJpa.class);
            def groupDal = springContext.getBean(GroupDalJpa.class);
            def scopeDal = springContext.getBean(ScopeDalJpa.class);
            Map<String, String> scopes = userDal.getUserScopes(userName, RBACUtil.RBAC_UAM_APPLICATION_NAME, true);
            String scope = RBACUtil.extractScopeForGroup(scopes, null, false);
            OptionFilter optionFilter = new OptionFilter();
            optionFilter.addFilter(RBACUtil.GROUP_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scope));
            def scopeObj = scopeDal.getByScopeKey(scopeKey);
            if(scopeObj!=null && scopeObj.applicationId!=null){
                optionFilter.addFilter("appRole", scopeObj.applicationId.toString());
            }
            Options options = new Options(optionFilter);
            List<Group> groups = groupDal.getList(options);
            if(groups!=null && !groups.isEmpty()){
                Map<String, Map<String, String>> tenantGroupMap = new LinkedHashMap<String, Map<String, String>>();
                for(Group g:groups){
                    if(!tenantGroupMap.containsKey(Lookup.getTenantNameById(g.getTenantId()))){
                        tenantGroupMap.put(Lookup.getTenantNameById(g.getTenantId()), new LinkedHashMap<String, String>());
                    }
                    tenantGroupMap.get(Lookup.getTenantNameById(g.getTenantId())).put(g.getGroupId().toString(), g.getName());
                }
                if(tenantGroupMap!=null && !tenantGroupMap.isEmpty()){
                    for(String tenantName: tenantGroupMap.keySet()){
                        Map<String, Object> resultSubMap = new LinkedHashMap<String, Object>();
                        resultSubMap.put("varOwner", tenantName);
                        resultSubMap.put("values", tenantGroupMap.get(tenantName));
                        resultList.add(resultSubMap);
                    }
                }
            }
            return new ObjectMapper().writeValueAsString(resultList);
        }
        else if(dataKey!=null && dataKey.equalsIgnoreCase("rbacTenantGroupsTA")){
            List<Map<String, Object>> resultList = new LinkedList<Map<String, Object>>();
            def userDal = springContext.getBean(UserDalJpa.class);
            def groupDal = springContext.getBean(GroupDalJpa.class);
            def scopeDal = springContext.getBean(ScopeDalJpa.class);
            Map<String, String> scopes = userDal.getUserScopes(userName, RBACUtil.RBAC_UAM_APPLICATION_NAME, true);
            String scope = RBACUtil.extractScopeForGroup(scopes, null, false);
            OptionFilter optionFilter = new OptionFilter();
            optionFilter.addFilter(RBACUtil.GROUP_SCOPE_QUERY, RBACUtil.encodeForScopeQuery(scope));
            def scopeObj = scopeDal.getByScopeKey(scopeKey);
            if(scopeObj!=null && scopeObj.applicationId!=null){
                optionFilter.addFilter("appRole", scopeObj.applicationId.toString());
            }
            Options options = new Options(optionFilter);
            List<Group> groups = groupDal.getList(options);
            if(groups!=null && !groups.isEmpty()){
                Map<String, Map<String, String>> tenantGroupMap = new LinkedHashMap<String, Map<String, String>>();
                for(Group g:groups){
                    if(!tenantGroupMap.containsKey(Lookup.getTenantNameById(g.getTenantId()))){
                        tenantGroupMap.put(Lookup.getTenantNameById(g.getTenantId()), new LinkedHashMap<String, String>());
                    }
                    tenantGroupMap.get(Lookup.getTenantNameById(g.getTenantId())).put(g.getGroupId().toString(), g.getName());
                }
                if(tenantGroupMap!=null && !tenantGroupMap.isEmpty()){
                    for(String tenantName: tenantGroupMap.keySet()){
                        Map<String, Object> resultSubMap = new LinkedHashMap<String, Object>();
                        resultSubMap.put("varOwner", tenantName);
                        resultSubMap.put("values", tenantGroupMap.get(tenantName));
                        resultList.add(resultSubMap);
                    }
                }
            }
            return new ObjectMapper().writeValueAsString(resultList);

        }
        return null;
    }

    private def getGroupMapFromJson(def object){
        def groupMap = [:];
        if(object!=null && object.rules!=null && object.rules.size() > 0){
            for(def rule: object.rules){
                if(rule.value!=null && rule.value.size() > 0){
                    for(def value: rule.value){
                        if(rule.subRules!=null && rule.subRules.size() > 0){
                            def groupIds = rule.subRules.value;
                            if(groupIds!=null && groupIds.size() > 0){
                                for(def groupIdArray: groupIds){
                                    if(groupIdArray!=null && groupIdArray.size() > 0){
                                        for(def groupId: groupIdArray) {
                                            if(!groupMap.containsKey(groupId)){
                                                groupMap.put(groupId, [:]);
                                            }
                                            if(!groupMap.get(groupId).containsKey(rule.id)){
                                                groupMap.get(groupId).put(rule.id, []);
                                            }
                                            groupMap.get(groupId).get(rule.id).add(''+value+'');
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return groupMap;
    }

    private def getDefinitionFromMap(def groupMap){
        if(groupMap!=null && !groupMap.isEmpty()){
            for(def groupIdValue: groupMap.keySet()){
                if(groupMap.get(groupIdValue)!=null && !groupMap.get(groupIdValue).isEmpty()){
                    ScopeDefinition newSd = new ScopeDefinition();
                    newSd.groupId = Integer.parseInt(groupIdValue);
                    newSd.scopeId = Lookup.getScopeIdByKey(RP_SHARED_REPORTS_KEY);
                    def tempScopeDef = new StringBuilder();
                    def tempScopeDefAddData = new StringBuilder('{"condition":"AND","rules":[');
                    int i = 0;
                    for(String filterId: groupMap.get(groupIdValue).keySet()){
                        def valueString = '';
                        log.info("getDefinitionFromMap; groupIdValue={}; reportIds={};", groupIdValue, StringUtils.join(groupMap.get(groupIdValue), ","));
                        if(i!=0){
                            tempScopeDef.append(' AND ');
                            tempScopeDefAddData.append(',');
                        }
                        tempScopeDef.append(filterId + ' IN (' + StringUtils.join(groupMap.get(groupIdValue).get(filterId), ", ") + ')');
                        tempScopeDefAddData.append('{"id":"'+ filterId+'","field":"'+filterId+'","type":"string","input":"select","operator":"in","value":[' + StringUtils.join(groupMap.get(groupIdValue).get(filterId), ", ") + '],"subRules":[]}');
                        i++;
                    }
                    tempScopeDefAddData = tempScopeDefAddData + ']}';
                    newSd.scopeDefinition = tempScopeDef.toString();
                    newSd.scopeAdditionalData = tempScopeDefAddData.toString();
                    log.info("getDefinitionFromMap; groupIdValue={}; newScopeDefinition={};", groupIdValue, newSd);
                }
            }
        }
        return null;
    }
    public void handleScope(Group group, AuditLogInfo auditLogInfo){
        List<Integer> groupIdListWithScope = [];
        List<Integer> groupIdListWithScopePosTA = [];
        List<Integer> groupIdListWithScopeAtmTA = [];
        boolean scopeFound = false;
        boolean scopeFoundPosTA = false;
        boolean scopeFoundAtmTA = false;
        def groupDal = springContext.getBean(GroupDalJpa.class);
        if(group!=null && group.getScopeDefinitions()!=null && !group.getScopeDefinitions().isEmpty()){
            for(ScopeDefinition sd:group.getScopeDefinitions()){
                String scopeKey = Lookup.getScopeKey(sd.scopeId);
                if(scopeKey.equals(reportPortalScopeKey)){
                    scopeFound = true;
                    def jsonSlurper = new JsonSlurper()
                    def object = jsonSlurper.parseText(sd.scopeAdditionalData);
                    def groupMap = getGroupMapFromJson(object);

                    log.debug("handleScope; group={}; groupMap={};", group, groupMap);
                    if(groupMap!=null && !groupMap.isEmpty()){
                        for(def groupIdValue: groupMap.keySet()){
                            groupIdListWithScope.add(Integer.parseInt(groupIdValue));
                            if(groupMap.get(groupIdValue)!=null && !groupMap.get(groupIdValue).isEmpty() /*&& Integer.parseInt(groupIdValue)!=group.groupId*/){
                                Integer newSdGroupId = Integer.parseInt(groupIdValue);
                                Integer newSdScopeId = Lookup.getScopeIdByKey(RP_SHARED_REPORTS_KEY);
                                if(newSdScopeId!=null){
                                    def tempScopeDefAddData = new StringBuilder('{"condition":"AND","rules":[');
                                    int i = 0;
                                    for(def filterId: groupMap.get(groupIdValue).keySet()){
                                        def valueString = '';
                                        log.debug("handleScope; groupIdValue={}; filterId={}; reportIds={};", groupIdValue, filterId, StringUtils.join(groupMap.get(groupIdValue), ","));
                                        if(i!=0){
                                            tempScopeDefAddData.append(',');
                                        }
                                        def tempArr = [];
                                        for(def tempString : groupMap.get(groupIdValue).get(filterId)){
                                            tempArr.add('"'+tempString+'"');
                                        }
                                        tempScopeDefAddData.append('{"id":"'+ filterId+'","field":"'+filterId+'","type":"string","input":"select","operator":"in","value":[' + StringUtils.join(tempArr, ", ") + '],"subRules":[]}');
                                        i++;
                                    }
                                    tempScopeDefAddData = tempScopeDefAddData + ']}';
                                    String newSdScopeDefinition = JsonOutput.toJson(groupMap.get(groupIdValue));
                                    String newSdScopeAdditionalData = tempScopeDefAddData.toString();

                                    Group grp = groupDal.getByName(Lookup.getGroupName(newSdGroupId));
                                    if(grp!=null){
                                        if(grp.getScopeDefinitions()==null){
                                            grp.setScopeDefinitions(new TreeSet<ScopeDefinition>());
                                        }
                                        ScopeDefinition newSd = new ScopeDefinition(newSdScopeDefinition, newSdScopeAdditionalData, newSdScopeId, newSdGroupId);
                                        newSd.setGroup(grp);
                                        grp.getScopeDefinitions().remove(newSd);
                                        grp.getScopeDefinitions().add(newSd);
                                        log.debug("handleScope; groupIdValue={}; grp={};", groupIdValue, grp);
                                        groupDal.updateScopeDefinition(grp, auditLogInfo.getLoggedInUserId());
                                    }
                                }
                            }
                        }
                        sd.scopeDefinition = JsonOutput.toJson(groupMap);
                    }
                    break;
                }
            }
            for(ScopeDefinition sd:group.getScopeDefinitions()){
                String scopeKey = Lookup.getScopeKey(sd.scopeId);
                if(scopeKey.equals(posTaScopeKey)){
                    scopeFoundPosTA = true;
                    def jsonSlurper = new JsonSlurper()
                    def object = jsonSlurper.parseText(sd.scopeAdditionalData);
                    def groupMap = getGroupMapFromJson(object);

                    log.debug("handleScope; group={}; groupMap={};", group, groupMap);
                    if(groupMap!=null && !groupMap.isEmpty()){
                        for(def groupIdValue: groupMap.keySet()){
                            groupIdListWithScopePosTA.add(Integer.parseInt(groupIdValue));
                            if(groupMap.get(groupIdValue)!=null && !groupMap.get(groupIdValue).isEmpty() /*&& Integer.parseInt(groupIdValue)!=group.groupId*/){
                                Integer newSdGroupId = Integer.parseInt(groupIdValue);
                                Integer newSdScopeId = Lookup.getScopeIdByKey(POSTA_SHARED_VIEWS_KEY);
                                if(newSdScopeId!=null){
                                    def tempScopeDefAddData = new StringBuilder('{"condition":"AND","rules":[');
                                    int i = 0;
                                    for(def filterId: groupMap.get(groupIdValue).keySet()){
                                        def valueString = '';
                                        log.debug("handleScope; groupIdValue={}; filterId={}; viewIds={};", groupIdValue, filterId, StringUtils.join(groupMap.get(groupIdValue), ","));
                                        if(i!=0){
                                            tempScopeDefAddData.append(',');
                                        }
                                        def tempArr = [];
                                        for(def tempString : groupMap.get(groupIdValue).get(filterId)){
                                            tempArr.add('"'+tempString+'"');
                                        }
                                        tempScopeDefAddData.append('{"id":"'+ filterId+'","field":"'+filterId+'","type":"string","input":"select","operator":"in","value":[' + StringUtils.join(tempArr, ", ") + '],"subRules":[]}');
                                        i++;
                                    }
                                    tempScopeDefAddData = tempScopeDefAddData + ']}';
                                    String newSdScopeDefinition = JsonOutput.toJson(groupMap.get(groupIdValue));
                                    String newSdScopeAdditionalData = tempScopeDefAddData.toString();

                                    Group grp = groupDal.getByName(Lookup.getGroupName(newSdGroupId));
                                    if(grp!=null){
                                        if(grp.getScopeDefinitions()==null){
                                            grp.setScopeDefinitions(new TreeSet<ScopeDefinition>());
                                        }
                                        ScopeDefinition newSd = new ScopeDefinition(newSdScopeDefinition, newSdScopeAdditionalData, newSdScopeId, newSdGroupId);
                                        newSd.setGroup(grp);
                                        grp.getScopeDefinitions().remove(newSd);
                                        grp.getScopeDefinitions().add(newSd);
                                        log.debug("handleScope; groupIdValue={}; grp={};", groupIdValue, grp);
                                        groupDal.updateScopeDefinition(grp, auditLogInfo.getLoggedInUserId());
                                    }
                                }
                            }
                        }
                        sd.scopeDefinition = JsonOutput.toJson(groupMap);
                    }
                    break;
                }
            }
            for(ScopeDefinition sd:group.getScopeDefinitions()){
                String scopeKey = Lookup.getScopeKey(sd.scopeId);
                if(scopeKey.equals(atmTaScopeKey)){
                    scopeFoundAtmTA = true;
                    def jsonSlurper = new JsonSlurper()
                    def object = jsonSlurper.parseText(sd.scopeAdditionalData);
                    def groupMap = getGroupMapFromJson(object);

                    log.debug("handleScope; group={}; groupMap={};", group, groupMap);
                    if(groupMap!=null && !groupMap.isEmpty()){
                        for(def groupIdValue: groupMap.keySet()){
                            groupIdListWithScopeAtmTA.add(Integer.parseInt(groupIdValue));
                            if(groupMap.get(groupIdValue)!=null && !groupMap.get(groupIdValue).isEmpty() /*&& Integer.parseInt(groupIdValue)!=group.groupId*/){
                                Integer newSdGroupId = Integer.parseInt(groupIdValue);
                                Integer newSdScopeId = Lookup.getScopeIdByKey(ATMTA_SHARED_VIEWS_KEY);
                                if(newSdScopeId!=null){
                                    def tempScopeDefAddData = new StringBuilder('{"condition":"AND","rules":[');
                                    int i = 0;
                                    for(def filterId: groupMap.get(groupIdValue).keySet()){
                                        def valueString = '';
                                        log.debug("handleScope; groupIdValue={}; filterId={}; viewIds={};", groupIdValue, filterId, StringUtils.join(groupMap.get(groupIdValue), ","));
                                        if(i!=0){
                                            tempScopeDefAddData.append(',');
                                        }
                                        def tempArr = [];
                                        for(def tempString : groupMap.get(groupIdValue).get(filterId)){
                                            tempArr.add('"'+tempString+'"');
                                        }
                                        tempScopeDefAddData.append('{"id":"'+ filterId+'","field":"'+filterId+'","type":"string","input":"select","operator":"in","value":[' + StringUtils.join(tempArr, ", ") + '],"subRules":[]}');
                                        i++;
                                    }
                                    tempScopeDefAddData = tempScopeDefAddData + ']}';
                                    String newSdScopeDefinition = JsonOutput.toJson(groupMap.get(groupIdValue));
                                    String newSdScopeAdditionalData = tempScopeDefAddData.toString();

                                    Group grp = groupDal.getByName(Lookup.getGroupName(newSdGroupId));
                                    if(grp!=null){
                                        if(grp.getScopeDefinitions()==null){
                                            grp.setScopeDefinitions(new TreeSet<ScopeDefinition>());
                                        }
                                        ScopeDefinition newSd = new ScopeDefinition(newSdScopeDefinition, newSdScopeAdditionalData, newSdScopeId, newSdGroupId);
                                        newSd.setGroup(grp);
                                        grp.getScopeDefinitions().remove(newSd);
                                        grp.getScopeDefinitions().add(newSd);
                                        log.debug("handleScope; groupIdValue={}; grp={};", groupIdValue, grp);
                                        groupDal.updateScopeDefinition(grp, auditLogInfo.getLoggedInUserId());
                                    }
                                }
                            }
                        }
                        sd.scopeDefinition = JsonOutput.toJson(groupMap);
                    }
                    break;
                }
            }
        }
        //check if there is no scope definition for reportPortalScopeKey(RPDefineSharedReports), then delete all RP_SHARED_REPORTS_KEY(RPSharedReports)
        def scopeDefCount = groupDal.getScopeDefCountByScopeKey(reportPortalScopeKey);
        log.debug("handleScope; reportPortalScopeKey={}; scopeDefCount={};", reportPortalScopeKey, scopeDefCount);
        //if scope is undefined or defined but no groups selected
        if(scopeDefCount==null || scopeDefCount==0 || (scopeFound==true && (groupIdListWithScope==null || groupIdListWithScope.isEmpty()))){
            def deletedScpCount = groupDal.removeScopeDefByScopeKey(RP_SHARED_REPORTS_KEY);
            log.debug("handleScope; reportPortalScopeKey={}; deleting scopes for all groups; deletedCount={};", RP_SHARED_REPORTS_KEY, deletedScpCount);
        }
        else if(scopeFound==true){
            log.debug("handleScope; reportPortalScopeKey={}; deleting scopes for all groups except; groupIdListWithScope={};", RP_SHARED_REPORTS_KEY, groupIdListWithScope);
            groupDal.removeScopeDefByScopeKeyExceptGroups(RP_SHARED_REPORTS_KEY, groupIdListWithScope);
        }

        //for posta same handling as JRP
        def scopeDefCountPosTA = groupDal.getScopeDefCountByScopeKey(posTaScopeKey);
        log.debug("handleScope; posTaScopeKey={}; scopeDefCountPosTA={};", posTaScopeKey, scopeDefCountPosTA);
        //if scope is undefined or defined but no groups selected
        if(scopeDefCountPosTA==null || scopeDefCountPosTA==0 || (scopeFoundPosTA==true && (groupIdListWithScopePosTA==null || groupIdListWithScopePosTA.isEmpty()))){
            def deletedScpCount = groupDal.removeScopeDefByScopeKey(POSTA_SHARED_VIEWS_KEY);
            log.debug("handleScope; posTaScopeKey={}; deleting scopes for all groups; deletedCount={};", POSTA_SHARED_VIEWS_KEY, deletedScpCount);
        }
        else if(scopeFoundPosTA==true){
            log.debug("handleScope; posTaScopeKey={}; deleting scopes for all groups except; groupIdListWithScopePosTA={};", POSTA_SHARED_VIEWS_KEY, groupIdListWithScopePosTA);
            groupDal.removeScopeDefByScopeKeyExceptGroups(POSTA_SHARED_VIEWS_KEY, groupIdListWithScopePosTA);
        }
        //for atmta same handling as JRP
        def scopeDefCountAtmTA = groupDal.getScopeDefCountByScopeKey(atmTaScopeKey);
        log.debug("handleScope; atmTaScopeKey={}; scopeDefCountAtmTA={};", atmTaScopeKey, scopeDefCountAtmTA);
        //if scope is undefined or defined but no groups selected
        if(scopeDefCountAtmTA==null || scopeDefCountAtmTA==0 || (scopeFoundAtmTA==true && (groupIdListWithScopeAtmTA==null || groupIdListWithScopeAtmTA.isEmpty()))){
            def deletedScpCount = groupDal.removeScopeDefByScopeKey(ATMTA_SHARED_VIEWS_KEY);
            log.debug("handleScope; atmTaScopeKey={}; deleting scopes for all groups; deletedCount={};", ATMTA_SHARED_VIEWS_KEY, deletedScpCount);
        }
        else if(scopeFoundAtmTA==true){
            log.debug("handleScope; atmTaScopeKey={}; deleting scopes for all groups except; groupIdListWithScopeAtmTA={};", ATMTA_SHARED_VIEWS_KEY, groupIdListWithScopeAtmTA);
            groupDal.removeScopeDefByScopeKeyExceptGroups(ATMTA_SHARED_VIEWS_KEY, groupIdListWithScopeAtmTA);
        }
    }

    public String validateAndBuildQuery(String scopeSql, String scopeJson, String scopeKey,
                                        String userName, String additionalMap) {
        if(RP_SHARED_REPORTS_KEY.equalsIgnoreCase(scopeKey) || RP_REPORTS_KEY.equalsIgnoreCase(scopeKey)){
            def jsonSlurper = new JsonSlurper()
            def object = jsonSlurper.parseText(scopeJson);
            log.debug("validateAndBuildQuery; scopeJsonObject={};", object);
            def reportCatMap = [:];
            if(object!=null && object.rules!=null && object.rules.size() > 0){
                for(def rule: object.rules){
                    if(rule.value!=null && rule.value.size() > 0){
                        for(def value: rule.value){
                            if(!reportCatMap.containsKey(rule.id)){
                                reportCatMap.put(rule.id, []);
                            }
                            reportCatMap.get(rule.id).add(value);
                        }
                    }
                }
            }
            log.debug("validateAndBuildQuery; scopeKey={}; scopeJson={}; reportCatMap={};", scopeKey, scopeJson, reportCatMap);
            if(reportCatMap!=null && !reportCatMap.isEmpty()){
                return JsonOutput.toJson(reportCatMap);
            }
            return scopeJson;
        }
        return null;
    }

}