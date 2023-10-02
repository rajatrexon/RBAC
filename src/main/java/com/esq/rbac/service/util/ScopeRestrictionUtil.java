package com.esq.rbac.service.util;

import com.esq.rbac.service.group.service.GroupDal;
import com.esq.rbac.service.user.service.UserDal;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class ScopeRestrictionUtil {
    private static final Logger log = LoggerFactory.getLogger(ScopeRestrictionUtil.class);
    private UserDal userDal;
    private GroupDal groupDal;

    @Autowired
    public void setUserDalJpa(UserDal userDal) {
        this.userDal = userDal;
    }
    @Autowired
    public void setGroupDalJpa(GroupDal groupDal) {
        this.groupDal = groupDal;
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<String> handleForUserRestriction(String scopeQuery) {
        // handle for "All" scenarios when scopes are applied
        List<String> idList = new ArrayList<String>();
        Options options =  null;
        if (scopeQuery != null && !scopeQuery.isEmpty()) {

            OptionFilter optionFilter = new OptionFilter();
            optionFilter.addFilter(RBACUtil.USER_SCOPE_QUERY, scopeQuery);
            options=new Options(optionFilter);
            List<Map<String,Object>> response = userDal.getUserIdNames(options);
            for (Map<String, Object> map : response) {
                idList.add(map.get("userId").toString());
            }
        }
        else{
            //return null to avoid sending all the id's
            return null;
        }
        if(log.isTraceEnabled()){
            log.trace("handleForUserRestriction; idList={}", idList);
        }
        return idList;
    }

    public List<String> handleForGroupRestriction(String scopeQuery) {
        List<String> idList = new ArrayList<String>();
        Options options =  null;
        if (scopeQuery != null && !scopeQuery.isEmpty()) {

            OptionFilter optionFilter = new OptionFilter();
            optionFilter.addFilter(RBACUtil.GROUP_SCOPE_QUERY, scopeQuery);
            options=new Options(optionFilter);
            List<Map<String,Object>> response = groupDal.getGroupIdNames(options);
            for (Map<String, Object> map : response) {
                idList.add(map.get("groupId").toString());
            }
        }
        else{
            //return null to avoid sending all the id's
            return null;
        }
        if(log.isTraceEnabled()){
            log.trace("handleForGroupRestriction; idList={}", idList);
        }
        return idList;
    }


}
