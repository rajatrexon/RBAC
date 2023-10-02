package com.esq.rbac.service.contact.schedule.queries;

public interface ScheduleQueries {

    String SCHEDULE_SEARCH =
            " SELECT count(csh.id)"
                    + " FROM contact.schedule  csh"
                    + " LEFT OUTER JOIN contact.sla csl  ON (csl.schedule_id=csh.id)"
                    + " LEFT OUTER JOIN contact.contact cc ON (cc.schedule_id=csh.id)"
                    + " LEFT OUTER JOIN contact.objectrole cor ON (cor.schedule_id=csh.id)"
                    + " WHERE (csl.schedule_id=?"
                    + " OR cc.schedule_id=?"
                    + " OR cor.schedule_id=? ) ";

    String SCHEDULE_NAME_SEARCH ="select count(1) from contact.schedule where name= ?";
}
