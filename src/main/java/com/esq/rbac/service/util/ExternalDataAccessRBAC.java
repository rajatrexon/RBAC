/*

 * Copyright (c)2013 ESQ Management Solutions Pvt Ltd. All Rights Reserved.
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

package com.esq.rbac.service.util;

import com.esq.rbac.service.attributes.domain.AttributesData;
import com.esq.rbac.service.attributes.service.AttributesDal;
import com.esq.rbac.service.scope.scopeconstraint.domain.ScopeConstraint;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.Options;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ExternalDataAccessRBAC {

	private static final Logger log = LoggerFactory
			.getLogger(ExternalDataAccessRBAC.class);
	
	private AttributesDal attributesDal;
	
	@Autowired
	public void setAttributesDal(AttributesDal attributesDal) {
		log.info("setAttributesDal; attributesDal={}", attributesDal);
		this.attributesDal = attributesDal;
	}
	
	public Integer getAttributeId(Integer constraintId) {
		return attributesDal.getAttributeId(constraintId);
	}
	
		
	
	public String list(ScopeConstraint scopeConstraint, HttpServletRequest servletRequest, Integer userId, Options options) {
		log.trace("list; scopeConstraint={}", scopeConstraint);

		Integer attributeId = getAttributeId(scopeConstraint.getConstraintId());
			try{
		return new Gson().toJson(getAttributeDataById(attributeId, options, userId)).toString();
			} catch (Exception e) {
			log.error("list;  Failed to fetch data   e = {}", e.getMessage());
			return Response.Status.INTERNAL_SERVER_ERROR.toString();
		}
	}
	
	public List<Map<String, Object>> getAttributeDataById(Integer attributeId, Options options, Integer userId){
			List<Map<String, Object>> customDataList = null;
        	OptionFilter optionFilter = options == null ? null : options.getOption(OptionFilter.class);
        	if(optionFilter!=null){
	        	customDataList = attributesDal.getByAttributeId(attributeId, options);
        	}
    		return customDataList;
	}
	
	public String update(ScopeConstraint scopeConstraint, String data, String contentType, Integer userId) {
		log.trace("update; scopeConstraint={}", scopeConstraint);
		JsonNode jsonObj;
		try {
			jsonObj = new ObjectMapper().readTree(data);
			String operation = "";
			operation = jsonObj.path("OPERATION").asText();
			Integer attributeId = getAttributeId(scopeConstraint.getConstraintId());

			if(operation.equals("CREATE")){
				AttributesData attributeData = new AttributesData();
				attributeData.setAttributeId(attributeId);
				attributeData.setAttributeDataValue(jsonObj.path("data").asText());
				attributeData = attributesDal.create(attributeData);
				return "{\"id\": "+ attributeData.getAttributeDataId() +"}";
			}else if(operation.equals("UPDATE")){
				String  attrName = jsonObj.path("ATTRIBUTE_NAME").asText();
				String  attributeDataValue = jsonObj.path(attrName  + "Name").asText();
				String  attributeDataId = jsonObj.path(attrName  + "Id").asText();
				AttributesData attributeData = attributesDal.updateAtributeName(attributeDataId, attributeDataValue);
				return "{\"name\":\"" + attributeDataValue + "\", \"id\": "+ attributeData.getAttributeDataId() +"}";
			}
		} 
		catch (Exception e) {
			log.error("update;  Failed to update data   e = {}", e.getMessage());
			return Response.Status.INTERNAL_SERVER_ERROR.toString();
		}
		
		return null;
	}
	
	
	public String getAttributeAssociation(String valueReferenceId, Integer userId) {
		log.trace("getAttributeAssociation; attributeId={}", valueReferenceId);
		Object associatedGroupCount = null;
		try{
			associatedGroupCount = attributesDal.getAttributeAssociation(valueReferenceId);
			String response = new Gson().toJson(associatedGroupCount).toString();
			return response;
		} catch (Exception e) {
			log.error("getAttributeAssociation;  Failed to fetch data   e = {}", e.getMessage());
			return Response.Status.INTERNAL_SERVER_ERROR.toString();
		}
	}
}
