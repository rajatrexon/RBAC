package com.esq.rbac.service.groovy

import com.esq.rbac.service.auditloginfo.domain.AuditLogInfo
import com.esq.rbac.service.codes.domain.Code
import com.esq.rbac.service.codes.service.CodeDal
import com.esq.rbac.service.group.domain.Group
import com.esq.rbac.service.group.service.GroupDal
import com.esq.rbac.service.organization.domain.Organization
import com.esq.rbac.service.organization.organizationmaintenance.service.OrganizationMaintenanceDal
import com.esq.rbac.service.scope.builder.ScopeBuilder
import com.esq.rbac.service.scope.scopedefinition.domain.ScopeDefinition
import com.esq.rbac.service.tenant.domain.Tenant
import com.esq.rbac.service.user.domain.User
import com.esq.rbac.service.util.EnvironmentUtil
import com.esq.rbac.service.util.TenantStructureGenerator
import com.esq.rbac.service.util.dal.OptionFilter
import com.esq.rbac.service.util.dal.Options
import jakarta.ws.rs.core.MultivaluedMap
import lombok.extern.slf4j.Slf4j
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap
import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional


@Slf4j
@Service
public class GroovyTenantStructureGenerator implements TenantStructureGenerator{


	private static final TENANT_ORG_TYPE = "Tenant";
	private static final TENANT_ORG_SUBTYPE = "Bank";


	def springContext

	@Autowired
	public void setApplicationContext(ApplicationContext ac)
			throws BeansException {
		springContext = ac;
	}

	List<Organization> createOrganizationForTenants(Tenant tenant) {
		def result=[];
		def organization = new Organization ( [remarks:'Auto Generated' ] );
		organization.organizationType = new Code();
		//check static Lookup object
		organization.organizationType.codeValue = Lookup.getCodeValueById(tenant.tenantType.codeId);
		organization.organizationName = tenant.tenantName + '-' +organization.organizationType.codeValue
		organization.tenantId = tenant.tenantId
		log.info("codeValue = {};", organization.organizationType.codeValue);
		if(tenant.tenantType?.codeValue=='Host'){
			return null;
		}
		OrganizationMaintenanceDal organizationMaintenanceDal = springContext.getBean(OrganizationMaintenanceDal.class);
		if(organizationMaintenanceDal!=null){
			log.info('organizationMaintenanceDal is not null');
		}
		result[result.size()] = organization
		result
	}

	List<Group> createGroupsForOrganization(Organization organization) {
		def result=[];
		def group = new Group ();
		group.name = organization.organizationName + '-Group'
		group.scopeDefinitions = [];
		group.tenantId = organization.tenantId
		def tenantScopeDefinition = new ScopeDefinition();
		tenantScopeDefinition.scopeDefinition = "t.tenantId  IN('" + organization.tenantId + "')"
		tenantScopeDefinition.scopeAdditionalData = '{"condition":"AND","rules":[{"id":"t.tenantId ","field":"t.tenantId ","type":"string","input":"select","operator":"in","value":[' + organization.tenantId + '],"subRules":[]}]}'
		//tenantScopeDefinition.scopeId =
		group.scopeDefinitions.add(tenantScopeDefinition);
		result[result.size()] = group
		result

	}

	List<User> createUsersForOrganizationAndGroup(Organization organization, Group group) {
		def result=[];
		def user = new User ();
		user.userName = organization.organizationName + '01'
		user.organizationId = organization.organizationId;
		user.groupId = group.groupId
		result[result.size()] = user
		result
	}

	ScopeDefinition createDefaultScopeForTenant(Long tenantId){
		def tenantScopeDefinition = new ScopeDefinition();
		String tenantNameForScope = Lookup.getTenantNameById(tenantId);
		tenantScopeDefinition.scopeDefinition = "t.tenantId  IN('" + tenantId + "')"
		tenantScopeDefinition.scopeAdditionalData = '{"condition":"AND","rules":[{"id":"t.tenantId ","field":"t.tenantId ","type":"string","input":"select","operator":"in","value":[' + tenantId + '],"subRules":[], "pluginValue":[{"text":"'+ tenantNameForScope +'","id":"'+ tenantId +'"}]}]}'
		tenantScopeDefinition
	}

	ScopeDefinition createDefaultScopeForCode(String codeName){
		def tenantScopeDefinition = new ScopeDefinition();
		tenantScopeDefinition.scopeDefinition = "ATM."+codeName+" = '\$currentuser.organization.organizationName\$'"
		tenantScopeDefinition.scopeAdditionalData = '{"condition":"AND","rules":[{"id":"ATM.'+codeName+'","field":"ATM.'+codeName+'","type":"string","input":"select","operator":"equal","value":["$$currentuser.organization.organizationName$$"],"subRules":[]}]}'
		tenantScopeDefinition
	}

	Group handleGroupCreation(Group group){
		if(group.getTenantId()!=null && springContext.getBean(EnvironmentUtil.class).isMultiTenantEnvironment()){
			ScopeDefinition sd = createDefaultScopeForTenant(group.getTenantId());
			sd.setGroup(group);
			sd.setGroupId(group.getGroupId());
			sd.setScopeId(Lookup.getScopeIdByKey(RBACUtil.SCOPE_KEY_TENANT));
			log.trace("handleGroupCreation; scopeKey={}; definition={};", RBACUtil.SCOPE_KEY_TENANT, sd);
			//remove previous of same groupId and scopeId
			group.getScopeDefinitions().remove(sd);
			group.getScopeDefinitions().add(sd);
		}
		group
	}

	Group handleGroupUpdation(Group existingGroup, Group group){
		existingGroup
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	Tenant handleTenantCreation(Tenant tenant, AuditLogInfo auditLogInfo){
		Organization org = new Organization();
		org.setOrganizationName(tenant.getTenantName());
		org.setTenantId(tenant.getTenantId());
		CodeDal codeDal = springContext.getBean(CodeDal.class);
		OrganizationMaintenanceDal organizationMaintenanceDal = springContext.getBean(OrganizationMaintenanceDal.class);
//		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
		queryParams.add("codeType", "ORGANIZATION_TYPE");
		queryParams.add("codeValue", TENANT_ORG_TYPE);
		Options options = new Options(new OptionFilter(queryParams));
		org.setOrganizationType(codeDal.list(options).get(0));
		queryParams = new MultivaluedStringMap();
		queryParams.add("codeType", "ORGANIZATION_SUBTYPE");
		queryParams.add("codeValue", TENANT_ORG_SUBTYPE);
		options = new Options(new OptionFilter(queryParams));
		org.setOrganizationSubType(codeDal.list(options).get(0));
		Organization retOrg = organizationMaintenanceDal.create(org, auditLogInfo.getLoggedInUserId(), "Organization", "Create");
		Lookup.updateOrganizationLookupTable(retOrg);
		GroupDal groupDal = springContext.getBean(GroupDal.class);
		ScopeBuilder scopeBuilder = springContext.getBean(ScopeBuilder.class);
		List<Code> codesList = scopeBuilder.getScopedCodes();
		if(codesList!=null && !codesList.isEmpty()){
			for(Code code: codesList){
				if((code.parentCodeValue).equalsIgnoreCase("Department")){
					Group group1 = new Group();
					group1.setName(tenant.getTenantName() + " " +code.getName() + " Group");
					group1.setTenantId(tenant.getTenantId());
					groupDal.create(group1, auditLogInfo.getLoggedInUserId());
					Group dbGroup = groupDal.getById(group1.getGroupId());
					Lookup.updateGroupLookupTable(dbGroup);
					ScopeDefinition sd = createDefaultScopeForCode(code.getName().toUpperCase());
					sd.setGroup(dbGroup);
					sd.setGroupId(dbGroup.getGroupId());
					sd.setScopeId(Lookup.getScopeIdByKey('atmScope'));
					dbGroup.getScopeDefinitions().add(sd);
					groupDal.updateRoles(dbGroup, true, auditLogInfo.getLoggedInUserId());
				}
			}
		}
		List<Group> groupsList = groupDal.getTemplateGroups();
		if(groupsList!=null && !groupsList.isEmpty()){
			for(Group group: groupsList){
				Group group1 = new Group();
				group1.setName(tenant.getTenantName() + " " + group.getName());
				group1.setTenantId(tenant.getTenantId());
				groupDal.create(group1, auditLogInfo.getLoggedInUserId());
				Group dbGroup = groupDal.getById(group1.getGroupId());
				Lookup.updateGroupLookupTable(dbGroup);

				Set<Integer> roleIds = group.getRolesIds();
				if(roleIds!=null && !roleIds.isEmpty()){
					Set<Integer> newRoleIds = new HashSet<Integer>();
					newRoleIds.addAll(roleIds);
					dbGroup.setRolesIds(newRoleIds);
				}
				Set<ScopeDefinition> scopeDefs = group.getScopeDefinitions();
				if(scopeDefs!=null && !scopeDefs.isEmpty()){
					Set<ScopeDefinition> newScopeDefs = new HashSet<ScopeDefinition>();
					for(ScopeDefinition sd: scopeDefs){
						ScopeDefinition sdNew = new ScopeDefinition(sd.getScopeDefinition(), sd.getScopeAdditionalData(), sd.getScopeId(), dbGroup.getGroupId());
						newScopeDefs.add(sdNew);
					}
					dbGroup.getScopeDefinitions().addAll(newScopeDefs);
				}
				groupDal.updateRoles(dbGroup, true, auditLogInfo.getLoggedInUserId());
			}
		}
		//Lookup.fillGroupLookupTable(groupDal.getGroupIdNames(null));
		tenant
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	Tenant handleTenantUpdation(Tenant newTenant, Tenant tenant, AuditLogInfo auditLogInfo) {
		OrganizationMaintenanceDal organizationMaintenanceDal = springContext.getBean(OrganizationMaintenanceDal.class);
		CodeDal codeDal = springContext.getBean(CodeDal.class);
//		MultivaluedMap<String, String> codeTypeQueryParams = new MultivaluedMapImpl();
		MultivaluedMap<String, String> codeTypeQueryParams = new MultivaluedStringMap();
		codeTypeQueryParams.add("codeType", "ORGANIZATION_TYPE");
		codeTypeQueryParams.add("codeValue",TENANT_ORG_TYPE);
		Options orgTypeCodeOptions = new Options(new OptionFilter(codeTypeQueryParams));
		Integer orgType = codeDal.list(orgTypeCodeOptions).get(0).getCodeId();
//		MultivaluedMap<String, String> codeSubTypeQueryParams = new MultivaluedMapImpl();
		MultivaluedMap<String, String> codeSubTypeQueryParams = new MultivaluedStringMap();
		codeSubTypeQueryParams.add("codeType", "ORGANIZATION_SUBTYPE");
		codeSubTypeQueryParams.add("codeValue",TENANT_ORG_SUBTYPE);
		Options orgSubTypeCodeOptions = new Options(new OptionFilter(codeSubTypeQueryParams));
		Integer orgSubType = codeDal.list(orgSubTypeCodeOptions).get(0).getCodeId();

//		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl();
		MultivaluedMap<String, String> queryParams = new MultivaluedStringMap();
		queryParams.add("tenantId", tenant.getTenantId());
		queryParams.add("organizationName", tenant.getTenantName());
		queryParams.add("organizationSubTypeCode", orgSubType);
		queryParams.add("organizationTypeCode", orgType);
		Options options = new Options(new OptionFilter(queryParams));
		List<Organization> tenantOrganization = organizationMaintenanceDal.getList(options);
		for(Organization organization: tenantOrganization){
			organization.setOrganizationName(newTenant.getTenantName());
			Organization retOrg = organizationMaintenanceDal.update(organization, auditLogInfo.getLoggedInUserId(), "Organization", "Update");
			Lookup.updateOrganizationLookupTable(retOrg);

		}

		//handle group name updation for codes groups
		GroupDal groupDal = springContext.getBean(GroupDal.class);
		ScopeBuilder scopeBuilder = springContext.getBean(ScopeBuilder.class);
		List<Code> codesList = scopeBuilder.getScopedCodes();
		if(codesList!=null && !codesList.isEmpty()){
			for(Code code: codesList){
				if((code.parentCodeValue).equalsIgnoreCase("Department")){
					Integer groupId = Lookup.getGroupId(tenant.getTenantName() + " " +code.getName() + " Group");
					if(groupId!=null && groupId!=-1){
						Group existgGroup = groupDal.getById(groupId);
						if(existgGroup!=null){
							existgGroup.setName(newTenant.getTenantName() + " " +code.getName() + " Group");
							Group retGroup = groupDal.update(existgGroup, auditLogInfo.getLoggedInUserId());
							Lookup.updateGroupLookupTable(retGroup);
						}
					}

				}
			}
		}
		//handle group name updation for template groups
		List<Group> groupsList = groupDal.getTemplateGroups();
		if(groupsList!=null && !groupsList.isEmpty()){
			for(Group group: groupsList){
				Integer groupId = Lookup.getGroupId(tenant.getTenantName() + " " + group.getName());
				log.info("groupName={}; groupId={};",tenant.getTenantName() + " " + group.getName(), groupId);
				if(groupId!=null && groupId!=-1){
					Group existgGroup = groupDal.getById(groupId);
					if(existgGroup!=null){
						existgGroup.setName(newTenant.getTenantName() + " " + group.getName());
						Group retGroup = groupDal.update(existgGroup, auditLogInfo.getLoggedInUserId());
						Lookup.updateGroupLookupTable(retGroup);
					}
				}
			}
		}
		//Lookup.fillGroupLookupTable(groupDal.getGroupIdNames(null));
		newTenant;
	}

}