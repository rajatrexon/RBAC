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
package com.esq.rbac.web.security.util;

import com.esq.rbac.web.vo.VariableInfo;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

public class VariableInfoDeserializer extends StdDeserializer<VariableInfo> {

	public VariableInfoDeserializer() {
		this(null);
	}

	public VariableInfoDeserializer(Class<?> vc) {
		super(vc);
	}

	@Override
	public VariableInfo deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		JsonNode node = jp.getCodec().readTree(jp);
		VariableInfo variableInfo = new VariableInfo(
				node.get("variableName") != null ? XSSValidatorUtil.handleForXSS(node.get("variableName").asText()) : null,
				node.get("variableValue") != null ? node.get("variableValue").asText() : null,
				node.get("applicationName") != null ? XSSValidatorUtil.handleForXSS(node.get("applicationName").asText()) : null,
				node.get("userName") != null ? XSSValidatorUtil.handleForXSS(node.get("userName").asText()) : null,
				node.get("groupName") != null ? XSSValidatorUtil.handleForXSS(node.get("groupName").asText()) : null);
		return variableInfo;
	}

}
