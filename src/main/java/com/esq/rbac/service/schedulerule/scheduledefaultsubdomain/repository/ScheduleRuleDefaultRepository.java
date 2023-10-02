package com.esq.rbac.service.schedulerule.scheduledefaultsubdomain.repository;

import com.esq.rbac.service.schedulerule.scheduledefaultsubdomain.domain.ScheduleRuleDefault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ScheduleRuleDefaultRepository extends JpaRepository<ScheduleRuleDefault,Long> {

}
