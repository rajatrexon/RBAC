package com.esq.rbac.service.loginservice.embedded;

import com.esq.rbac.service.user.vo.SSOLogoutData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutResponse {

    private String resultCode;
    private Boolean isSuccess;
    private String resultMessage;
    private Map<String, List<String>> sessionHashChildApplicationNames = new LinkedHashMap<String, List<String>>();
    private List<SSOLogoutData> ssoLogoutDataList;

    public LogoutResponse(String sessionHash, String resultCode,  Boolean isSuccess, String... childApplicationNames){
        this.resultCode = resultCode;
        this.isSuccess = isSuccess;
        if(childApplicationNames!=null && childApplicationNames.length!=0){
            this.sessionHashChildApplicationNames.put(sessionHash, Arrays.asList(childApplicationNames));
        }
    }

    public LogoutResponse(Map<String, List<String>> sessionHashChildApplicationNames){
        this.sessionHashChildApplicationNames = sessionHashChildApplicationNames;
    }

    public static final String INVALID_APP_KEY = "invalidAppKey";
    public static final String INVALID_SERVICE_TICKET = "invalidServiceTicket";
    public static final String LOGOUT_SUCCESSFULL = "logoutSuccessful";

    public Map<String, List<String>> getSessionHashChildApplicationNames() {
        return sessionHashChildApplicationNames;
    }
}
