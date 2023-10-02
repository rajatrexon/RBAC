package com.esq.rbac.service.contact.helpers;

import com.esq.rbac.service.auditlog.domain.AuditLog;
import com.esq.rbac.service.auditlog.service.AuditLogService;
import com.esq.rbac.service.contact.embedded.AuditLogJson;
import com.esq.rbac.service.util.AuditLogUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

@Slf4j
@Component
public class ContactUserRest {
    //private RestClient restClient;
    public static final String RESOURCE_PATH = "auditlog";
    public static final String CONTENT_TYPE = "Content-Type";
    private static final String CONF_DISPATCHER_APPLICATIONNAME = "dispatcher.applicationName";
    private static final String DEFAULT_DISPATCHER_APPLICATIONNAME = "Dispatcher";
    private Configuration configuration;
    private String applicationName;
    private AuditLogService auditLogDal;

	/*@Inject
	public void setRestClient(RestClient restClient2) {
		log.trace("setRestClient");
		this.restClient = restClient2;
	}*/

    @Autowired
    public void setAuditLogDal(AuditLogService auditLogDal) {
        this.auditLogDal = auditLogDal;
    }

    @Resource(name="propertyConfig")
    @Autowired
    public void setConfiguration(Configuration configuration) {
        log.trace("setConfiguration;");
        this.configuration = configuration;
        applicationName = this.configuration.getString(CONF_DISPATCHER_APPLICATIONNAME, DEFAULT_DISPATCHER_APPLICATIONNAME);
    }

    public void createAuditLog(String target, String operation, Map<String,String> properties, String userId){
        AuditLogJson auditLogJson = new AuditLogJson();
        auditLogJson.setTargetName(target);
        auditLogJson.setOperationName(operation);
        auditLogJson.setQueryField1(target+"."+operation);
        auditLogJson.setIsAlertable(false);
        auditLogJson.setUserId(Integer.parseInt(userId));
        postAuditLogData(auditLogJson, properties);
    }

    private AuditLogJson postAuditLogData(AuditLogJson auditLogJson, Map<String,String> properties) {
        log.info("postAuditLogData; logData={}", auditLogJson);
        auditLogJson.setApplicationName(applicationName);
        if(properties!=null){
            auditLogJson.setProperties(properties);
        }
		/*ClientResponse clientResponse = null;
		try {
			clientResponse = restClient
					.resource(RESOURCE_PATH)
					.entity(auditLogJson, MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)
					.post(ClientResponse.class);
			log.info("postAuditLogData; response={}", clientResponse);
			return clientResponse.getEntity(AuditLogJson.class);
		} catch (Exception e) {
			log.warn("postAuditLogData; Exception={} ", e);
		}
		finally{
            try{
             if(clientResponse!=null){
              clientResponse.close();
             }
            }
            catch(ClientHandlerException ce){
             log.debug("postAuditLogData; ClientHandlerException={}", ce);
            }
           }*/
        try {
            AuditLog aLog = auditLogDal.create(AuditLogUtil.convertToAuditLog(auditLogJson));
            return AuditLogUtil.convertToAuditLogJson(aLog);
        } catch (Exception e) {
            log.error("postAuditLogData; Exception={};", e);
        }
        return null;

    }
}

