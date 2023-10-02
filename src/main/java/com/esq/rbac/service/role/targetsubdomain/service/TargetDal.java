package com.esq.rbac.service.role.targetsubdomain.service;

import com.esq.rbac.service.role.targetsubdomain.domain.Target;
import com.esq.rbac.service.util.dal.Options;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface TargetDal {
    Target create(Target target);

    Target update(Target target);

    Target getById(int targetId);

    void deleteById(int targetId);

    List<Target> getList(Options options);

    int getCount(Options options);
}
