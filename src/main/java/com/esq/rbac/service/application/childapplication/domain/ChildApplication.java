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
package com.esq.rbac.service.application.childapplication.domain;

import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.application.childapplication.appurldata.AppUrlData;
import com.esq.rbac.service.application.childapplication.childapplicationlicense.ChildApplicationLicense;
import com.esq.rbac.service.util.SpecialCharValidator;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


@Entity
@Builder
@AllArgsConstructor
@Table(name = "childApplication",schema = "rbac")
public class ChildApplication implements Comparable<ChildApplication> {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "childMaintenanceIdGenerator")
	@TableGenerator(name = "childMaintenanceIdGenerator", schema = "rbac", table = "idSequence",pkColumnName = "idName",
			valueColumnName = "idValue",	pkColumnValue = "childApplicationId",initialValue = 1,allocationSize = 10)

	@Column(name = "childApplicationId")
	private Integer childApplicationId;

	@SpecialCharValidator
	@Column(name = "childApplicationName", nullable = false,length=50)
	private String childApplicationName;

	@JsonBackReference("ApplicationChild")
	@ManyToOne
	@JoinColumn(name = "applicationId")
	private Application application;

	@Column(name = "isDefault")
	private Boolean isDefault;

	@Column(name = "appType")
	private Integer appType;

	@Column(name = "allowMultipleLogins")
	private Boolean allowMultipleLogins;

	@SpecialCharValidator
	@Column(name = "appKey",length=100)
	private String appKey;

	@Valid
	@JsonManagedReference("ApplicationUrls")
//	@OneToMany(mappedBy = "childApplication", orphanRemoval = true, cascade = CascadeType.ALL)
	@OneToMany(mappedBy = "childApplication",fetch = FetchType.EAGER, cascade = CascadeType.ALL)
	private Set<AppUrlData> appUrlDataSet;

	@Column(name = "permissionValidator")
	private String permissionValidator;

	@Column(name = "permissionValidatorData")
	private String permissionValidatorData;

	@Column(name = "description")
	private String description;

	//	@OneToOne(mappedBy = "childApplication", orphanRemoval = true, cascade = CascadeType.ALL)
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "childAppLicenseId")
	private ChildApplicationLicense childApplicationLicense;

	@Transient
	private ChildApplicationLicense changeLicense;



	public Set<AppUrlData> getAppUrlDataSet() {
		if (appUrlDataSet != null && !(appUrlDataSet instanceof TreeSet)) {
			appUrlDataSet = new TreeSet<AppUrlData>(appUrlDataSet);
		}
		return appUrlDataSet;
	}

	public void setAppUrlDataSet(Set<AppUrlData> appUrlDataSet) {
		this.appUrlDataSet = appUrlDataSet;
	}

	public static enum APP_TYPE {SSO(0), NON_SSO(1), NATIVE(2);
		private Integer code;
		APP_TYPE(Integer code){
			this.code = code;
		}
		public Integer getCode(){
			return code;
		}

		public static APP_TYPE getByCode(Integer code){
			switch(code) {
				case 0: return SSO;
				case 1: return NON_SSO;
				case 2: return NATIVE;
				default : return SSO;
			}
		}

		public boolean equals(Integer code){
			if(this.code.equals(code)){
				return true;
			}
			return false;
		}
	};


	public ChildApplication() {

	}

	public Integer getChildApplicationId() {
		return childApplicationId;
	}

	public void setChildApplicationId(Integer childApplicationId) {
		this.childApplicationId = childApplicationId;
	}

	public String getChildApplicationName() {
		return childApplicationName;
	}

	public void setChildApplicationName(String childApplicationName) {
		this.childApplicationName = childApplicationName;
	}

	public Application getApplication() {
		return application;
	}

//	public void setApplication(Application application) {
//		// set new application
//		this.application = application;
//
//		// add this ChildApplication to newly set application
//		if (this.application != null) {
//			if (this.application.getChildApplications() == null) {
//				this.application
//						.setChildApplications(new HashSet<ChildApplication>());
//			}
//			if (!this.application.getChildApplications().contains(this)) {
//				this.application.getChildApplications().add(this);
//			}
//		}
//	}

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}

	public Integer getAppType() {
		return appType;
	}

	public void setAppType(Integer appType) {
		this.appType = appType;
	}

	public Boolean isAllowMultipleLogins() {
		return allowMultipleLogins;
	}

	public void setAllowMultipleLogins(
			Boolean allowMultipleLogins) {
		this.allowMultipleLogins = allowMultipleLogins;
	}

	public String getAppKey() {
		return appKey;
	}

	public void setAppKey(String appKey) {
		this.appKey = appKey;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@JsonIgnore
	public Boolean isNonSSOAllowMultipleLogins(){
		if(appType!=null && appType.equals(APP_TYPE.NON_SSO.getCode())){
			if(allowMultipleLogins!=null && allowMultipleLogins.equals(Boolean.TRUE)){
				return true;
			}
		}
		return false;
	}

	@Override
	public int compareTo(ChildApplication o) {
		if (this.childApplicationName != null) {
			return this.childApplicationName.compareTo(o.childApplicationName);
		}
		return 0;
	}

	@JsonIgnore
	public static boolean isSSO(Integer code){
		if(APP_TYPE.SSO.getCode().equals(code)){
			return true;
		}
		return false;
	}

	@JsonIgnore
	public static boolean isNonSSO(Integer code){
		if(APP_TYPE.NON_SSO.getCode().equals(code)){
			return true;
		}
		return false;
	}

	@JsonIgnore
	public static boolean isNative(Integer code){
		if(APP_TYPE.NATIVE.getCode().equals(code)){
			return true;
		}
		return false;
	}

	@JsonIgnore
	public String getFirstServiceUrl(){
		if(appUrlDataSet!=null && !appUrlDataSet.isEmpty()){
			for(AppUrlData urlData: appUrlDataSet){
				return urlData.getServiceUrl();
			}
		}
		return null;
	}

	@JsonIgnore
	public String getFirstHomeUrl(){
		if(appUrlDataSet!=null && !appUrlDataSet.isEmpty()){
			for(AppUrlData urlData: appUrlDataSet){
				return urlData.getHomeUrl();
			}
		}
		return null;
	}

	@JsonIgnore
	public String getHomeUrlByTag(String tag){
		if(appUrlDataSet!=null && !appUrlDataSet.isEmpty() && tag!=null && !tag.isEmpty()){
			for(AppUrlData urlData: appUrlDataSet){
				//RBAC-774, handling for case when tag is missed/null and only one url is present. If more than one url is present,
				//there is no way to determine which one to return.
				if(urlData.getTag()==null && appUrlDataSet.size() == 1){
					return urlData.getHomeUrl();
				}
				if(tag.equalsIgnoreCase(urlData.getTag())){
					return urlData.getHomeUrl();
				}
			}
		}
		return null;
	}

	@JsonIgnore
	public String getPermissionValidator() {
		return permissionValidator;
	}

	public void setPermissionValidator(
			String permissionValidator) {
		this.permissionValidator = permissionValidator;
	}

	@JsonIgnore
	public String getPermissionValidatorData() {
		return permissionValidatorData;
	}

	public void setPermissionValidatorData(String permissionValidatorData) {
		this.permissionValidatorData = permissionValidatorData;
	}

	@JsonIgnore
	//need to hide this from everywhere except license API
	public ChildApplicationLicense getChildApplicationLicense() {
		return childApplicationLicense;
	}

	@JsonIgnore
	public void setChildApplicationLicense(ChildApplicationLicense childApplicationLicense) {
		this.childApplicationLicense = childApplicationLicense;
	}

	public ChildApplicationLicense getChangeLicense() {
		return changeLicense;
	}

	public void setChangeLicense(ChildApplicationLicense changeLicense) {
		this.changeLicense = changeLicense;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Context(name=").append(childApplicationName == null ? "":childApplicationName);
		sb.append("; appType=").append(appType == null ? "": APP_TYPE.getByCode(appType));
		sb.append("; appKey=").append(appKey == null ? "":appKey);
		sb.append("; allowMultipleLogins=").append(allowMultipleLogins == null ? "":allowMultipleLogins);
		sb.append("; description=").append(description == null ? "":description);
		if(appUrlDataSet != null && appUrlDataSet.size() > 0){
			sb.append("; deploymentUrls=(").append(new TreeSet<AppUrlData>(appUrlDataSet)+")");
		}
		if(changeLicense!=null &&
				changeLicense.getLicense()!=null && !changeLicense.getLicense().isEmpty()
		){
			sb.append("; license=").append("************");
		}
		if(changeLicense!=null &&
				changeLicense.getAdditionalData()!=null && !changeLicense.getAdditionalData().isEmpty()){
			sb.append("; licenseAdditonalData=").append(changeLicense.getAdditionalData());
		}
		sb.append(")");
		return sb.toString();
	}
}
