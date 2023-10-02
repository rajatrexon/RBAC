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
package com.esq.rbac.service.attributes.service;


import com.esq.rbac.service.attributes.domain.AttributesData;
import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.util.dal.Options;

import java.util.List;
import java.util.Map;

public interface AttributesDal extends BaseDal {

    AttributesData create(AttributesData application);

    AttributesData updateAtributeName(String attributeDataId, String attributeDataValue);
    
    List<Map<String, Object>> getByAttributeId(Integer attributeId, Options options);
    
    Integer getAttributeId(Integer scopeConstraintId);
	
    Object getAttributeAssociation(String valueReferenceId);

}
