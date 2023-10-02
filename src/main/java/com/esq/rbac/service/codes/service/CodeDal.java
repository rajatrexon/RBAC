package com.esq.rbac.service.codes.service;

import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo;
import com.esq.rbac.service.codes.domain.Code;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import org.springframework.stereotype.Service;

import java.util.List;

public interface CodeDal {
    Code create(Code code);
    List<Code> list(Options options);
    List<Code> getCodesByApplication(OptionFilter optionFilter);


}
