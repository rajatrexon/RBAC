package com.esq.rbac.service.configuration.service;

import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.configuration.domain.Configuration;
import com.esq.rbac.service.configuration.enc.EsqSymetricCipher;
import com.esq.rbac.service.configuration.repository.ConfigurationRepository;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.util.DatabaseConfigurationWithCache;
import com.esq.rbac.service.util.DeploymentUtil;
import com.esq.rbac.service.util.EncryptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@Service
@Slf4j
public class ConfigrationDalJpa extends BaseDalJpa implements ConfigurationDal{


    private ConfigurationRepository configurationRepository;

    @Autowired
    public void setConfigurationRepository(ConfigurationRepository configurationRepository) {
        log.trace("setConfigurationRepository; {};", configurationRepository);
        this.configurationRepository =configurationRepository;

    }

    private static final Integer PASSWORD_MINIMUM_LENGTH=7;


    private DeploymentUtil deploymentUtil;

    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
        log.trace("setDeploymentUtil; {};", deploymentUtil);
        this.deploymentUtil =deploymentUtil;

    }



    DatabaseConfigurationWithCache configuration;

    @Autowired
    public void setDeploymentUtil(DatabaseConfigurationWithCache configuration) {
        log.trace("setDatabaseConfigurationWithCache; {};", configuration);
        this.configuration =configuration;

    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public boolean updateConfiguration(List<Configuration> configurationList) {
        try{
            clearObjectChangeSet();
            int updateCount = 0;
            for (Configuration config : configurationList) {

                if(config.getConfKey() != null && config.getConfKey().equals("rbac.passwordPolicy.minLength")) {
                    Integer pwdLength=Integer.parseInt(config.getConfValue());
                    if(pwdLength<PASSWORD_MINIMUM_LENGTH) {
                        config.setConfValue(PASSWORD_MINIMUM_LENGTH.toString());
                    }
                }


                if(config.getConfKey() != null && configuration.containsKey(config.getConfKey())){
                    if(config.getConfType()!=null && config.getConfType().equals("PASSWORD") ){
                        if(config.getConfValue()!=null && !config.getConfValue().isEmpty() && !config.getConfValue().startsWith("{") && !config.getConfValue().endsWith("}")){
                            if(deploymentUtil.isEnableStandardPasswordHashing()){
                                config.setConfValue("{"+ EncryptionUtils.encryptPassword(config.getConfValue())+"}");
                            }else {
                                config.setConfValue("{"+ EsqSymetricCipher.encryptPassword(config.getConfValue())+"}");
                            }
                        }
                        if(config.getConfValue()!=null && !config.getConfValue().equals(configuration.getString(config.getConfKey()))){
                            if(setObjectChangeSet(config.getConfKey(), "***", "*****"))
                                updateCount++;
                        }
                    }
                    else{
                        if(setObjectChangeSet(config.getConfKey(), configuration.getString(config.getConfKey()), config.getConfValue()))
                            updateCount++;
                    }
                    if(config.getConfType()!=null && config.getConfType().equals("STRING")){
                        if(config.getConfValue()!=null && config.getConfValue().contains(",")){
                            config.setConfValue(config.getConfValue().replaceAll(",", Matcher.quoteReplacement("\\,")));
                        }
                    }
                    Integer result=configurationRepository.updateConfValueByConfKey(config.getConfValue(),config.getConfKey());
                    if (result != null) {
                        log.debug("updateConfiguration; queryResult={}", result);
                        if (result.intValue() == 0) {
                            return false;
                        }
                    }
                }

            }
            if(updateCount > 0) {
                putToObjectChangeSet(OBJECTNAME, "Configuration");
                configuration.clearAll();
                return true;
            }else {
                configuration.clearAll();
                return false;
            }
        } catch (Exception e) {
            log.error("updateConfiguration; exception={}", e);
            ErrorInfoException errorInfo = new ErrorInfoException("updateConfigFailed");
            throw errorInfo;
        }
    }

    private boolean setObjectChangeSet(String ConfigKey, String oldValue, String newValue) {
        if((oldValue!=null && !oldValue.equals(newValue)) || (newValue!=null && !newValue.equals(oldValue))){
            checkPutToObjectChangeSet(ConfigKey, newValue, oldValue, null, null);
            return true;
        }
        return false;
    }


    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Configuration> getConfiguration() {
       // TypedQuery<Object[]> getConfigurationQuery = em.createNamedQuery(GET_CONFIGURATION, Object[].class);
        List<Object[]> result =  configurationRepository.findVisibleConfigurations();
        //configurationRepository.findVisibleConfigurations();
        List<Configuration> configurationList = new ArrayList<Configuration>();
        Configuration config = null;
        for (Object[] pair : result) {
            if(pair[2]!=null && pair[2].toString().equalsIgnoreCase("STRING") && pair[1]!=null && pair[1].toString().contains("\\,")){
                pair[1]=pair[1].toString().replaceAll(Matcher.quoteReplacement("\\,"),",");
            }
            config = new Configuration(pair[0].toString(), pair[1]!=null?pair[1].toString():null, pair[2].toString(), pair[3].toString(), pair[4].toString(), pair[5].toString());
            configurationList.add(config);
        }
        return configurationList;
    }
}
