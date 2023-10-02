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
package com.esq.rbac.service.password.passwordpolicy.service;

import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.password.paswordHistory.domain.PasswordHistory;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;




@Service
@Qualifier("passwordPolicyImpl")
public class PasswordPolicyImpl implements PasswordPolicy {

    private static final Logger log = LoggerFactory.getLogger(PasswordPolicyImpl.class);
    private static final String LOWER_CASE_ALPHA;
    private static final String UPPER_CASE_ALPHA;
    @SuppressWarnings("unused")
	private static final String ALPHA;
    @SuppressWarnings("unused")
	private static final String NUMERIC;
    private static final String SPECIAL = "!@#$%^&*()_+-=[]\\;',./{}|:\"<>?";
    public static final String CODE_PASSWORD_POLICY_VIOLATED = "passwordPolicyViolated";
    public static final String PARAM_VIOLATED_RULES = "violatedRules";
    public static final String POLICY_DATA = "policyData";



    private UserDal userDal;
    @Autowired
   public void setUserDal(UserDal userDal){
        log.debug("setUserDal; {}", userDal);
       this.userDal = userDal;
   }


    private int minLength = 6;
    private int numericRequired = 3;
    private int specialRequired = 3;
    private int alphaRequired = 3;
    private int lowerCaseRequired = 3;
    private int upperCaseRequired = 3;
    private boolean mixCaseRequired = true;
    private boolean noUserName = true;
    private int passwordHistory = 5;
    private int minValidation = 3;
  //RBAC-1917 start
	private int maxLength=20;
	//RBAC-1917 end
    static {
        StringBuilder sb = new StringBuilder();
        for (char c='a'; c<='z'; c++) {
            sb.append(c);
        }
        LOWER_CASE_ALPHA = sb.toString();
        
        sb = new StringBuilder();
        for (char c='A'; c<='Z'; c++) {
            sb.append(c);
        }
        UPPER_CASE_ALPHA = sb.toString();
        ALPHA = LOWER_CASE_ALPHA + UPPER_CASE_ALPHA;

        sb = new StringBuilder();
        for (char c='0'; c<='9'; c++) {
            sb.append(c);
        }
        NUMERIC = sb.toString();
    }

    private Configuration configuration;



    @Autowired
    public void setConfiguration(@Qualifier("DatabaseConfigurationWithCache")Configuration configuration) {
        log.debug("setConfiguration; {}", configuration);
        this.configuration = configuration;
    }

    public void setMinLength(int minLength) {
        this.minLength = minLength;
    }

    public void setAlphaRequired(int alphaRequired) {
        this.alphaRequired = alphaRequired;
    }

    public void setNumericRequired(int numericRequired) {
        this.numericRequired = numericRequired;
    }

    public void setSpecialRequired(int specialRequired) {
        this.specialRequired = specialRequired;
    }

    public void setLowerCaseRequired(int lowerCaseRequired) {
        this.lowerCaseRequired = lowerCaseRequired;
    }

    public void setUpperCaseRequired(int upperCaseRequired) {
        this.upperCaseRequired = upperCaseRequired;
    }

    public void setMixCaseRequired(boolean mixCaseRequired) {
        this.mixCaseRequired = mixCaseRequired;
    }

    public void setNoUserName(boolean noUserName) {
        this.noUserName = noUserName;
    }

    public void setPasswordHistory(int passwordHistory) {
        this.passwordHistory = passwordHistory;
    }
    
    public void setMinValidation(int minValidation){
    	this.minValidation = minValidation;
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
         Set<String> optionalViolatedRules = new HashSet<String>();
         int validationPassed = 0;
         do {
             if (newPassword == null) {
             	requiredViolatedRules.add("nullPassword");
                 break;
             }
             if (minLength > 0 && newPassword.length() < minLength) {
             	requiredViolatedRules.add("minLength=" + minLength);
             }
           //RBAC-1917 start
             if (maxLength > 0 && newPassword.length() > maxLength) {
              	requiredViolatedRules.add("maxLength=" + maxLength);
              }
           //RBAC-1917 end
             String userName = user.getUserName();
             if (noUserName && userName != null && newPassword.toLowerCase().contains(userName.toLowerCase())) {
             	requiredViolatedRules.add("noUserName");
             }
             if (alphaRequired > 0 && countAlphabets(newPassword) <  alphaRequired) {
             	optionalViolatedRules.add("alphaRequired");
             }else if(alphaRequired > 0){
             	validationPassed ++;
             }
             if (lowerCaseRequired > 0 && countLowerCaseCharacter(newPassword) < lowerCaseRequired) {
             	optionalViolatedRules.add("lowerCaseRequired");
             }else if(lowerCaseRequired > 0){
             	validationPassed ++;
             }
             if (upperCaseRequired > 0 && countUpperCaseCharacter(newPassword) < upperCaseRequired) {
             	optionalViolatedRules.add("upperCaseRequired");
             }else if(upperCaseRequired > 0){
             	validationPassed ++;
             }
            /* if (mixCaseRequired
                     && (!contains(newPassword, LOWER_CASE_ALPHA) || !contains(newPassword, UPPER_CASE_ALPHA))) {
                 violatedRules.add("mixCaseRequired");
             }*/
             if (numericRequired > 0 && countNumericCharacter(newPassword) < numericRequired) {
             	optionalViolatedRules.add("numericRequired");
             }else if(numericRequired > 0){
             	validationPassed ++;
             }
             if (specialRequired > 0 && countSpecialCharacter(newPassword) < specialRequired) {
             	optionalViolatedRules.add("specialRequired");
             }else if(specialRequired > 0){
             	validationPassed ++;
             }
             if (passwordHistory > 0 && userDal != null) {
                 List<PasswordHistory> history = userDal.getPasswordHistory(user.getUserId(), passwordHistory);
                 for (PasswordHistory entry : history) {
                     String hash = User.hashPassword(entry.getPasswordSalt(), newPassword);
                     if (hash.equals(entry.getPasswordHash())) {
                     	requiredViolatedRules.add("passwordHistory");
                         break;
                     }
                 }
             }
         } while (false);
         
         if(validationPassed < minValidation && !optionalViolatedRules.isEmpty()){
     		requiredViolatedRules.addAll(optionalViolatedRules);
     	 }
         return requiredViolatedRules; 
    }

    @SuppressWarnings("unused")
	private boolean contains(String input, String charSet) {
        for (char c : input.toCharArray()) {
            if (charSet.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }

    private void readConfiguration() {
        if (configuration == null) {
            return;
        }

        minLength = configuration.getInt("rbac.passwordPolicy.minLength", 0);
      //RBAC-1917 start
        maxLength = configuration.getInt("rbac.passwordPolicy.maxLength", 0);
      //RBAC-1917 end
        numericRequired = configuration.getInteger("rbac.passwordPolicy.numeric", 3);
        specialRequired = configuration.getInteger("rbac.passwordPolicy.special", 3);
        alphaRequired = configuration.getInteger("rbac.passwordPolicy.alpha", 3);
        lowerCaseRequired = configuration.getInteger("rbac.passwordPolicy.lowerCase", 3);
        upperCaseRequired = configuration.getInteger("rbac.passwordPolicy.upperCase", 3);
        mixCaseRequired = configuration.getBoolean("rbac.passwordPolicy.mixedCase", false);
        noUserName = configuration.getBoolean("rbac.passwordPolicy.noUserName", false);
        passwordHistory = configuration.getInt("rbac.passwordPolicy.passwordHistory", 0);
        minValidation = configuration.getInt("rbac.passwordPolicy.conditionToFollow", 3);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("PasswordPolicyImpl{minLength=").append(minLength);
      //RBAC-1917 start
        sb.append("; maxLength=").append(maxLength);
      //RBAC-1917 end
        if (alphaRequired > 0) {
            sb.append("; alphaRequired");
        }
        if (numericRequired > 0) {
            sb.append("; numericRequired");
        }
        if (specialRequired > 0) {
            sb.append("; specialRequired");
        }
        if (lowerCaseRequired > 0) {
            sb.append("; lowerCaseRequired");
        }
        if (upperCaseRequired > 0) {
            sb.append("; upperCaseRequired");
        }
        if (mixCaseRequired) {
            sb.append("; mixCaseRequired");
        }
        if (noUserName) {
            sb.append("; noUserName");
        }
        if (passwordHistory > 0) {
            sb.append("; passwordHistory=").append(passwordHistory);
        }
        sb.append("}");
        return sb.toString();
    }
    
    public Map<String,Integer> getPolicyMap(){
        readConfiguration();
    	Map<String, Integer> policyMap = new HashMap<String, Integer>();
    	
    	policyMap.put("minLength", minLength);
    	//RBAC-1917 start
    	policyMap.put("maxLength", maxLength);
    	//RBAC-1917 end
        if (alphaRequired > 0) {
        	policyMap.put("alphaRequired", alphaRequired);
        }
        if (numericRequired > 0) {
        	policyMap.put("numericRequired",numericRequired);
        }
        if (specialRequired > 0) {
        	policyMap.put("specialRequired",specialRequired);
        }
        if (lowerCaseRequired > 0) {
        	policyMap.put("lowerCaseRequired",lowerCaseRequired);
        }
        if (upperCaseRequired > 0) {
        	policyMap.put("upperCaseRequired",upperCaseRequired);
        }
        if (mixCaseRequired) {
        	policyMap.put("mixCaseRequired",null);
        }
        if (noUserName) {
        	policyMap.put("noUserName",null);
        }
        if (passwordHistory > 0) {
        	policyMap.put("passwordHistory",passwordHistory);
        }
        if(minValidation > 0) {
        	policyMap.put("minValidation",minValidation);
        }
        
        return policyMap;
    }

    private int countNumericCharacter(String str) {
	
    	int charCount = 0;
    	char temp;
    	for( int i = 0; i < str.length( ); i++ )
    	{
    	    temp = str.charAt( i );
    	    if( Character.isDigit(temp) )
    	        charCount++;
    	}
    	return charCount;
    }
    private int countUpperCaseCharacter(String str) {
    	
    	int charCount = 0;
    	char temp;
    	for( int i = 0; i < str.length( ); i++ )
    	{
    	    temp = str.charAt( i );
    	    if( Character.isUpperCase(temp))
    	        charCount++;
    	}
    	return charCount;
    }
    private  int countLowerCaseCharacter(String str) {
    	
    	int charCount = 0;
    	char temp;
    	for( int i = 0; i < str.length( ); i++ )
    	{
    	    temp = str.charAt( i );
    	    if( Character.isLowerCase(temp))
    	        charCount++;
    	}
    	return charCount;
    }
    private  int countSpecialCharacter(String str) {
    	
    	int charCount = 0;
    	char temp;
    	for( int i = 0; i < str.length( ); i++ )
    	{
    	    temp = str.charAt( i );
    	    if(!Character.isLetter(temp) && !Character.isDigit(temp))
    	        charCount++;
    	}
    	return charCount;
    }
    private int countAlphabets(String str) {
    	
    	int charCount = 0;
    	char temp;
    	for( int i = 0; i < str.length( ); i++ )
    	{
    	    temp = str.charAt( i );
    	    if(Character.isLetter(temp) && !Character.isDigit(temp))
    	        charCount++;
    	}
    	return charCount;
    }

	@Override
	public String generateRandomPassword(User user, int attemptCount) {
        readConfiguration();
		StringBuffer password = new StringBuffer("");
		if (numericRequired > 0) {
			password.append(RandomStringUtils.random(numericRequired, false,
					true));
		}
		if (alphaRequired > 0) {
			password.append(RandomStringUtils
					.random(alphaRequired, true, false));
		}
		if (specialRequired > 0) {
			password.append(RandomStringUtils.random(specialRequired, 0,
					specialRequired + 1, false, false, SPECIAL.toCharArray()));
		}
		if (lowerCaseRequired > 0) {
			password.append(RandomStringUtils.random(lowerCaseRequired, true,
					false).toLowerCase());
		}
		if (upperCaseRequired > 0) {
			password.append(RandomStringUtils.random(upperCaseRequired, true,
					false).toUpperCase());
		}

		if (minLength > 0) {
			if (password.length() < (minLength + (minLength / 2))) {
				password.append(RandomStringUtils.random(
						(minLength + (minLength / 2)) - password.length(),
						true, true));
			}
		} else {
			password.append(RandomStringUtils.random(8, true, true));
		}
		List<Character> characters = new ArrayList<Character>();
		for (char c : password.toString().toCharArray()) {
			characters.add(c);
		}
		StringBuilder output = new StringBuilder(password.toString().length());
		while (characters.size() != 0) {
			int randPicker = (int) (Math.random() * characters.size());
			output.append(characters.remove(randPicker));
		}
		
		Set<String> requiredViolatedRules = getViolatedRules(user, output.toString());
        if (!requiredViolatedRules.isEmpty()) {
        	if(requiredViolatedRules.contains("noUserName") || requiredViolatedRules.contains("passwordHistory")){
        		attemptCount++;
        		//to avoid stack overflow
        		if(attemptCount<5){
        			generateRandomPassword(user, attemptCount);
        		}
        	}
        }
		return output.toString();
	}

}
