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
package com.esq.rbac.service.scope.service;


import com.esq.rbac.service.basedal.BaseDal;
import com.esq.rbac.service.scope.domain.Scope;
import com.esq.rbac.service.util.dal.Options;

import java.util.List;

public interface ScopeDal extends BaseDal {

    Scope create(Scope scope, int userId);

    Scope update(Scope scope, int userId);

    Scope getById(int scopeId);
    
    Scope getByScopeKey(String scopeKey);

    void deleteById(int scopeId, Boolean force);

    List<Scope> getList(Options options);
    
    List<Scope> getListGlobal();

    Integer getCount(Options options);

    int isScopeNameDuplicate(Integer applicationId, String name, Integer scopeId);

    int isScopeInOperationScope(Integer scopeId);

    int isScopeInScopeDefinition(Integer scopeId);
    
    List<Scope> searchList(Options options);
	
	Integer getSearchCount(Options options);
}
