package com.esq.rbac.service.loginlog.service;

import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.loginlog.domain.LoginLog;
import com.esq.rbac.service.loginlog.repository.LoginLogRepository;
import com.esq.rbac.service.lookup.Lookup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class LoginLogServiceImpl implements LoginLogService{


    private LoginLogRepository loginLogRepository;

    @Autowired
    public void setLoginLogRepository(LoginLogRepository loginLogRepository) {
        this.loginLogRepository = loginLogRepository;
    }



    private int THREAD_SIZE = 1;
    ExecutorService loginLogExecutor = Executors.newFixedThreadPool(THREAD_SIZE);


    private LoginLogDalProcessor loginLogDalProcessor;

    @Autowired
    public void setLoginLogDalProcessor(LoginLogDalProcessor loginLogDalProcessor) {
        this.loginLogDalProcessor = loginLogDalProcessor;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void create(LoginLog loginLog) {
        loginLogExecutor.execute(getLoginLogRunnable(loginLog));
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void createLoginLogUseFromLoginLogProcessor(LoginLog loginLog) {
        if(loginLog.getUserId()==null){
            int userId= Lookup.getUserId(loginLog.getUserName());
            loginLog.setUserId(userId!=-1?userId:null);
        }
        com.esq.rbac.service.application.childapplication.domain.ChildApplication childApp = Lookup.getChildApplicationByServiceUrlNew(loginLog.getServiceUrl());
        Integer childApplicationId = childApp!=null?childApp.getChildApplicationId():null;
        ChildApplication childApplication = null;
        if(loginLog.getAppKey()!=null && !loginLog.getAppKey().isEmpty() && !loginLog.getAppKey().equals("")){
            childApplication = Lookup.getChildApplicationByAppKeyNew(loginLog.getAppKey());
            if(childApplication!=null){
                childApplicationId =  childApplication.getChildApplicationId();
            }
        }
        loginLog.setChildApplicationId(childApplicationId);
        if(loginLog.getChildApplicationId()!=null && loginLog.getChildApplicationId()!=-1){
            if(childApplication!=null){
                loginLog.setAppType(childApplication.getAppType());
            }
            else{
                ChildApplication childAppForType = Lookup.getChildApplicationByNameNew(Lookup.getChildApplicationName(loginLog.getChildApplicationId()));
                loginLog.setAppType(childAppForType!=null?childAppForType.getAppType():null);
            }
        }
        loginLog.setSessionHash(loginLog.getSessionHash());
        loginLogRepository.save(loginLog);
    }

    public Runnable getLoginLogRunnable(final LoginLog loginLog){
        return new Runnable(){
            @Override
            public void run() {
                log.debug("run;");
                loginLogDalProcessor.createLoginLog(loginLog);
            }
        };

    }
}
