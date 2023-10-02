package com.esq.rbac.service.role.operationsubdomain.service;

import com.esq.rbac.service.role.operationsubdomain.domain.Operation;
import com.esq.rbac.service.util.dal.Options;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface OperationDal {

    Operation create(Operation operation);

    Operation update(Operation operation);

    Operation getById(int operationId);

    void deleteById(int operationId);

    List<Operation> getList(Options options);

    int getCount(Options options);
}
