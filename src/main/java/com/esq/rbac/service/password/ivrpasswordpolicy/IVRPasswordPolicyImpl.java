/*
 * Copyright (c)2014 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
 *
 * Permission to use, copy, modify, and distribute this software requires
 * a signed licensing agreement.
 *
 * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
 * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
 * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE.
 */
package com.esq.rbac.service.password.ivrpasswordpolicy;

import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.ivrpasswordhistory.domain.IVRPasswordHistory;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import org.apache.commons.configuration.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.inject.Inject;
import java.util.*;


@Service
public class IVRPasswordPolicyImpl implements IVRPasswordPolicy {

    private static final Logger log = LoggerFactory.getLogger(IVRPasswordPolicyImpl.class);
    @SuppressWarnings("unused")
	private static final String NUMERIC;
    public static final String CODE_PASSWORD_POLICY_VIOLATED = "ivrPasswordPolicyViolated";
    public static final String PARAM_VIOLATED_RULES = "violatedRules";
    public static final String POLICY_DATA = "policyData";
    private UserDal userDal;
    private Configuration configuration;
    private int pinMinLength = 6;
    private int pinMaxLength = 6;
    private int ivrPasswordHistory = 5;
    private int userIdMaxLength = 8;
    private int userIdMinLength = 5;

    static {
        StringBuilder sb = new StringBuilder();
        sb = new StringBuilder();
        for (char c='0'; c<='9'; c++) {
            sb.append(c);
        }
        NUMERIC = sb.toString();
    }
    
    @Autowired
    public void setUserDal(UserDal userDal) {
        log.debug("setUserDal; {}", userDal);
        this.userDal = userDal;
    }

    @Autowired
    public void setConfiguration(@Qualifier("DatabaseConfigurationWithCache") Configuration configuration) {
        log.debug("setConfiguration; {}", configuration);
        this.configuration = configuration;
    }

    @Override
    public void checkNewPassword(User user, String newPassword) {
        readConfiguration();
        log.trace("checkNewPassword; policy={}", this);
        Set<String> requiredViolatedRules = getViolatedRules(user, newPassword);
        if (!requiredViolatedRules.isEmpty()) {
         	
            StringBuilder sb = new StringBuilder();
            sb.append(CODE_PASSWORD_POLICY_VIOLATED).append("; ");
            sb.append(PARAM_VIOLATED_RULES).append("=").append(requiredViolatedRules.toString());
            log.info("checkNewPassword; {}", sb.toString());
            ErrorInfoException e = new ErrorInfoException(CODE_PASSWORD_POLICY_VIOLATED, sb.toString());
            e.getParameters().put(PARAM_VIOLATED_RULES, requiredViolatedRules.toString());
            e.getParameters().put(POLICY_DATA, getPolicyMap().toString());
            throw e;
        }
       
    }
    
    private Set<String> getViolatedRules(User user, String newPassword){
    	 Set<String> requiredViolatedRules = new HashSet<String>();
         if (newPassword == null) {
         	requiredViolatedRules.add("nullPassword");
         }
         if (pinMinLength > 0 && newPassword.length() < pinMinLength) {
         	requiredViolatedRules.add("minLength=" + pinMinLength);
         }
         if (pinMaxLength > 0 && newPassword.length() > pinMaxLength) {
          	requiredViolatedRules.add("maxLength=" + pinMaxLength);
          }
         
         if (ivrPasswordHistory > 0 && user != null && userDal != null && user.getUserId() != null) {
             List<IVRPasswordHistory> history = userDal.getIVRPasswordHistory(user.getUserId(), ivrPasswordHistory);
             for (IVRPasswordHistory entry : history) {
                 String hash = User.hashPassword(entry.getIvrPasswordSalt(), newPassword);
                 if (hash.equals(entry.getIvrPasswordHash())) {
                 	requiredViolatedRules.add("IVRPasswordHistory");
                     break;
                 }
             }
         }
         
         return requiredViolatedRules; 
    }

    private void readConfiguration() {
        if (configuration == null) {
            return;
        }
        userIdMaxLength = configuration.getInt("rbac.ivrPasswordPolicy.userIdMaxLength", 6);
        userIdMinLength = configuration.getInt("rbac.ivrPasswordPolicy.userIdMinLength", 6);
        ivrPasswordHistory = configuration.getInt("rbac.ivrPasswordPolicy.pinHistory", 5);
        pinMaxLength = configuration.getInt("rbac.ivrPasswordPolicy.pinMaxLength", 6);
        pinMinLength = configuration.getInt("rbac.ivrPasswordPolicy.pinMinLength", 6);
        
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PasswordPolicyImpl{minLength=").append(pinMinLength);
        sb.append("; PasswordPolicyImpl{maxLength=").append(pinMaxLength);
        if (ivrPasswordHistory > 0) {
            sb.append("; passwordHistory=").append(ivrPasswordHistory);
        }
        sb.append("}");
        return sb.toString();
    }
    
    public Map<String,Integer> getPolicyMap(){
        readConfiguration();
    	Map<String, Integer> policyMap = new HashMap<String, Integer>();
    	
    	policyMap.put("minLength", pinMinLength);
    	policyMap.put("maxLength", pinMaxLength);
    	policyMap.put("userIdMaxLength", userIdMaxLength);
    	policyMap.put("userIdMinLength", userIdMinLength);
    	if (ivrPasswordHistory > 0) {
        	policyMap.put("passwordHistory",ivrPasswordHistory);
        }
        
        return policyMap;
    }
   
	@Override
	public String generateRandomPassword(User user, int attemptCount) {
        readConfiguration();
        
		char[] chars = "0123456789".toCharArray();
		StringBuilder password = new StringBuilder();
		Random random = new Random();
		for (int i = 0; i <= this.pinMaxLength; i++) {
		    char c = chars[random.nextInt(chars.length)];
		    password.append(c);
		}
		
		return password.toString();
	}
}
