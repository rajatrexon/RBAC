package com.esq.rbac.service.configuration.service;

import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.configuration.domain.Configuration;
import java.util.List;

public interface ConfigurationDal extends BaseDal {

    boolean updateConfiguration(List<Configuration> configurationList);

    List<Configuration> getConfiguration();
}
