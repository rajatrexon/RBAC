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
package com.esq.rbac.service.application.childapplication.appurldata;

import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.util.DeploymentUtil;
import com.esq.rbac.service.util.SpecialCharValidator;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.util.HashSet;


@Entity
@Table(name = "appUrlData",schema = "rbac")
@Data
public class AppUrlData implements Comparable<AppUrlData>  {


	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "appUrlId")
	private Integer appUrlId;
	@Pattern(regexp= DeploymentUtil.URL_PATTERN,message="Invalid Logout URL")
	@Column(name = "logoutServiceUrl")
	private String logoutServiceUrl;
	@Pattern(regexp=DeploymentUtil.URL_PATTERN,message="Invalid Home URL")
	@Column(name = "homeUrl")
	private String homeUrl;
	@Pattern(regexp=DeploymentUtil.URL_PATTERN,message="Invalid Service URL")
	@Column(name = "serviceUrl", nullable = false)
	private String serviceUrl;


	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "childApplicationId")
	@JsonBackReference("ApplicationUrls")
	private ChildApplication childApplication;

	@SpecialCharValidator
	@Column(name = "tag")
	private String tag;

	public String getLogoutServiceUrl() {
		return logoutServiceUrl;
	}

	public void setLogoutServiceUrl(String logoutServiceUrl) {
		this.logoutServiceUrl = logoutServiceUrl;
	}

	public String getHomeUrl() {
		return homeUrl;
	}

	public void setHomeUrl(String homeUrl) {
		this.homeUrl = homeUrl;
	}

	public String getServiceUrl() {
		return serviceUrl;
	}

	public void setServiceUrl(String serviceUrl) {
		this.serviceUrl = serviceUrl;
	}

	public Integer getAppUrlId() {
		return appUrlId;
	}

	public void setAppUrlId(Integer appUrlId) {
		this.appUrlId = appUrlId;
	}

	public ChildApplication getChildApplication() {
		return childApplication;
	}

//	public void setChildApplication(ChildApplication childApplication) {
//		// set new application
//				this.childApplication = childApplication;
//
//				// add this ChildApplication to newly set application
//				if (this.childApplication != null) {
//					if (this.childApplication.getAppUrlDataSet() == null) {
//						this.childApplication
//								.setAppUrlDataSet(new HashSet<AppUrlData>());
//					}
//					if (!this.childApplication.getAppUrlDataSet().contains(this)) {
//						this.childApplication.getAppUrlDataSet().add(this);
//					}
//				}
//	}

	public AppUrlData() {
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@Override
	public int compareTo(AppUrlData o) {
		if (this.serviceUrl != null) {
			return this.serviceUrl.compareTo(o.serviceUrl);
		}
		return 0;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("DeploymentUrl(tag=").append(tag==null?"":tag);
		sb.append("; serviceUrl=").append(serviceUrl==null?"":serviceUrl);
		sb.append("; homeUrl=").append(homeUrl==null?"":homeUrl);
		sb.append("; logoutServiceUrl=").append(logoutServiceUrl==null?"":logoutServiceUrl);
		sb.append(")");
		return sb.toString();
	}

}
