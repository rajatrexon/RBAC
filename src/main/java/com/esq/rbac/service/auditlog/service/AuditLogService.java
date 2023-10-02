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
package com.esq.rbac.service.auditlog.service;
import com.esq.rbac.service.auditlog.domain.AuditLog;
import com.esq.rbac.service.util.dal.Options;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

public interface AuditLogService {

	AuditLog create(AuditLog auditLog);
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	List<AuditLog> getList(Options options);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	List<AuditLog> getAuditLogByUserId(int userId, Options options);

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	List<Object[]> getAuditLogHistoryFeedByUserId(int userId, Options options);

	void deleteById(int auditLogId);

	int getCount(Options options);

	public void createAsyncLog(Integer userId, String name, String target, String operation, Map<String, String> objectChanges);

	void createAuditLogUseFromAuditLogProcessor(AuditLog auditLog);

	void createSyncLog(Integer userId, String name, String target,
					   String operation, Map<String, String> objectChanges);
}
