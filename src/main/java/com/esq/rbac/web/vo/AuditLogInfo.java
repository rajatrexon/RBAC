/*
 * Copyright (c)2016 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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
package com.esq.rbac.web.vo;

public class AuditLogInfo {

	private Integer loggedInUserId;
	private String clientIp;
	private String target;
	private String operation;
	private String name;

	public Integer getLoggedInUserId() {
		return loggedInUserId;
	}

	public void setLoggedInUserId(Integer loggedInUserId) {
		this.loggedInUserId = loggedInUserId;
	}

	public String getClientIp() {
		return clientIp;
	}

	public void setClientIp(String clientIp) {
		this.clientIp = clientIp;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
	
	public void setTargetOperation(String target, String operation){
		this.target = target;
		this.operation = operation;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public AuditLogInfo() {

	}

	public AuditLogInfo(Integer loggedInUserId, String clientIp) {
		this.loggedInUserId = loggedInUserId;
		this.clientIp = clientIp;
	}

	public AuditLogInfo(Integer loggedInUserId, String clientIp, String name, String target, String operation) {
		this.loggedInUserId = loggedInUserId;
		this.clientIp = clientIp;
		this.name = name;
		this.target = target;
		this.operation = operation;
	}
}
