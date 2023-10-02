///*
// * Copyright (c)2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
// *
// * Permission to use, copy, modify, and distribute this software requires
// * a signed licensing agreement.
// *
// * IN NO EVENT SHALL ESQ BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT, SPECIAL,
// * INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS, ARISING OUT OF
// * THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF ESQ HAS BEEN ADVISED
// * OF THE POSSIBILITY OF SUCH DAMAGE. ESQ SPECIFICALLY DISCLAIMS ANY WARRANTIES,
// * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
// * FITNESS FOR A PARTICULAR PURPOSE.
// */
//package com.esq.rbac.service.scope.scopedefinition.domain;
//
//import com.esq.rbac.service.group.domain.Group;
//import com.fasterxml.jackson.annotation.JsonBackReference;
//import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
//import com.fasterxml.jackson.databind.deser.std.StringDeserializer;
//import jakarta.persistence.Entity;
//import jakarta.persistence.Table;
//import lombok.Data;
//
////This class uses a special de-serializer for some fields (RBAC-1187)
//@Entity
//@Table(schema = "rbac",name = "scopeDefinition")
//@Data
//public class ScopeDefinitionRj implements Comparable<ScopeDefinitionRj>{
//
//	@JsonDeserialize(using = StringDeserializer.class)
//	private String scopeDefinition;
//	@JsonDeserialize(using = StringDeserializer.class)
//    private String scopeAdditionalData;
//    private Integer scopeId;
//    private Integer groupId;
//
//    @JsonBackReference("scopeDefinitions")
//    private Group group;
//
//
//
//	public Group getGroup() {
//		return group;
//	}
//
//	public void setGroup(Group group) {
//		this.group = group;
//	}
//
//	public ScopeDefinitionRj(String scopeDefinition, String scopeAdditionalData, Integer scopeId, Integer groupId){
//    	this.scopeAdditionalData = scopeAdditionalData;
//    	this.scopeDefinition = scopeDefinition;
//    	this.scopeId = scopeId;
//    	this.groupId = groupId;
//	}
//
//    public ScopeDefinitionRj() {
//	}
//
//	public String getScopeDefinition() {
//		return scopeDefinition;
//	}
//	public void setScopeDefinition(String scopeDefinition) {
//		this.scopeDefinition = scopeDefinition;
//	}
//	public String getScopeAdditionalData() {
//		return scopeAdditionalData;
//	}
//	public void setScopeAdditionalData(String scopeAdditionalData) {
//		this.scopeAdditionalData = scopeAdditionalData;
//	}
//
//	@Override
//	public String toString() {
//		StringBuilder sb = new StringBuilder();
//		sb.append("ScopeDefinition{scopeDefinition=").append(scopeDefinition);
//		sb.append("; scopeAdditionalData=").append(scopeAdditionalData);
//		sb.append("; scopeId=").append(scopeId);
//		sb.append("; groupId=").append(groupId);
//		return sb.toString();
//
//	}
//
//	public Integer getScopeId() {
//		return scopeId;
//	}
//
//	public void setScopeId(Integer scopeId) {
//		this.scopeId = scopeId;
//	}
//
//	public Integer getGroupId() {
//		return groupId;
//	}
//
//	public void setGroupId(Integer groupId) {
//		this.groupId = groupId;
//	}
//
//	@Override
//	public int compareTo(ScopeDefinitionRj sd) {
//		if(groupId == null || scopeId == null)
//			return 0;
//		int res = this.groupId.compareTo(sd.groupId);
//		if (res != 0) {
//		    return res;
//		}
//		res = this.scopeId.compareTo(sd.scopeId);
//	    if (res != 0) {
//	        return res;
//	    }
//		return 0;
//	}
//}
//
//
