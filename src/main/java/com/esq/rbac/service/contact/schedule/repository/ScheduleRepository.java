package com.esq.rbac.service.contact.schedule.repository;

import com.esq.rbac.service.base.repository.Repository;
import com.esq.rbac.service.contact.schedule.embedded.ScheduleRule;
import com.esq.rbac.service.contact.schedule.queries.ScheduleQueries;
import com.esq.rbac.service.contact.schedule.domain.Schedule;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@Service("ScheduleRepository")
public class ScheduleRepository extends Repository<Schedule> {

    public ScheduleRepository() {
        super(Schedule.class);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public Schedule update(long id, Schedule schedule) {
        List<ScheduleRule> scheduleRuleList = schedule.getRules();

        // first update without schedule rules - all will be deleted
        schedule.setId(id);
        schedule.setRules(null);
        super.update(id, schedule);
        entityManager.flush();

        // then update with schedule rules - new list will be added
        schedule.setRules(scheduleRuleList);
        return super.update(id, schedule);
    }


    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int slaSearch(long q) throws Exception {
        log.debug("slaSearch; q={}", q);
        Query query = entityManager.createQuery(ScheduleQueries.SCHEDULE_SEARCH);
        query.setParameter(1, q);
        query.setParameter(2, q);
        query.setParameter(3, q);
        return (int)((Number) query.getSingleResult()).intValue();
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public int scheduleNameSearch(String name,long...tenantScope) throws Exception {
        log.debug("scheduleNameSearch; name={},tenantScope ={}", name,tenantScope);
        StringBuilder sb = new StringBuilder();
        if(tenantScope.length==0){
            sb.append(ScheduleQueries.SCHEDULE_NAME_SEARCH);
        }else if(tenantScope.length==1){
            sb.append(ScheduleQueries.SCHEDULE_NAME_SEARCH);
            sb.append(" and tenant_id = ").append(tenantScope[0]);
        }else{
            sb.append(ScheduleQueries.SCHEDULE_NAME_SEARCH);
            String tenantList= StringUtils.join(ArrayUtils.toObject(tenantScope), ",");
            sb.append(" and tenant_id in ( ").append(tenantList).append(")");
        }
        Query query = entityManager.createNativeQuery(sb.toString());
        query.setParameter(1, name);
        return (int)((Number) query.getSingleResult()).intValue();
    }
}

