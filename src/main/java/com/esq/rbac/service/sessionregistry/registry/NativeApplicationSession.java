package com.esq.rbac.service.sessionregistry.registry;

import com.esq.rbac.service.util.RBACUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NativeApplicationSession {

    private String ticket;
    private String serviceUrl;
    private String appKey;
    private Map<String, String> additionalInfo;
    private String sessionHash;
    private String childApplicationName;

    public NativeApplicationSession(String ticket, String apppKey, String childApplicationName) {
        this.ticket = ticket;
        this.appKey = apppKey;
        this.sessionHash = RBACUtil.hashString(ticket+apppKey);
        this.childApplicationName = childApplicationName;
    }

    public static String generateSessionHash(String ticket, String appKey){
        return RBACUtil.hashString(ticket+appKey);
    }
}
