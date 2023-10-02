package com.esq.rbac.service.groovy

import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal
import com.esq.rbac.service.scope.builder.ScopeBuilder
import com.esq.rbac.service.scope.builder.util.ScopeGenerator
import com.esq.rbac.service.util.dal.OptionFilter
import com.esq.rbac.service.util.dal.Options
import com.google.gson.Gson
import jakarta.ws.rs.core.MultivaluedMap
import lombok.extern.slf4j.Slf4j
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap
import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service

@Slf4j
@Service
public class DynamicScopeGenerator implements ScopeGenerator {

OrganizationMaintenanceDal orgMaintenanceDal;
def springContext
	ScopeBuilder scopeBuilder

	public String getFilterKeyData(String sourcePath, String dataKey, String scopeKey,
			String userName, String additionalMap,
			Map<String, String> scopeMap, String parentValue){
			orgMaintenanceDal = springContext.getBean(OrganizationMaintenanceDal.class);
			scopeBuilder = springContext.getBean(ScopeBuilder.class);
				Map<String, Object> filterMap = new HashMap<String, Object>();
				if("codes".equalsIgnoreCase(sourcePath)){
//					MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
					MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
					String scopeQuery = null;
					if (scopeMap != null && !scopeMap.isEmpty()) {
						scopeQuery = RBACUtil.encodeForScopeQuery(scopeMap
								.get(RBACUtil.ORGANIZATION_SCOPE_QUERY));
					}
					queryParams.add(RBACUtil.ORGANIZATION_SCOPE_QUERY, scopeQuery);
					queryParams.add("organizationSubTypeCode", dataKey);
					Options options = new Options(new OptionFilter(queryParams));
					List<Map<String,Object>> resultList= orgMaintenanceDal.getOrganizationIdNames(options);
					if(resultList!=null && !resultList.isEmpty()){
						for (Map<String, Object> tempMap : resultList) {
							filterMap.put(tempMap.get("organizationId").toString(),
									tempMap.get("organizationName"));
						}
					}
					String templabel = scopeBuilder.getCurrentLabel(scopeKey,
									dataKey, additionalMap);
							if (templabel != null && ScopeBuilder.getRbacType(additionalMap).equalsIgnoreCase("ORGANIZATION_SUBTYPE")) {
								filterMap.put("\$\$currentuser.organization.organizationName\$\$",
												templabel);
							}

				}
				new Gson().toJson(filterMap)
			}

	@Override
	void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		springContext = applicationContext;
	}
}