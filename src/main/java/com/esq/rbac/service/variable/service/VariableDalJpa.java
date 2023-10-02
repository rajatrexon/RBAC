package com.esq.rbac.service.variable.service;

import java.util.*;
import com.esq.rbac.service.application.childapplication.domain.ChildApplication;
import com.esq.rbac.service.basedal.BaseDalJpa;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.filters.domain.Filters;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.esq.rbac.service.util.dal.OptionPage;
import com.esq.rbac.service.util.dal.Options;
import com.esq.rbac.service.variable.domain.Variable;
import com.esq.rbac.service.variable.repository.VariableRepository;
import com.esq.rbac.service.variable.variableinfo.domain.VariableInfo;
import com.esq.rbac.service.variable.variableinfov2.domain.VariableInfoV2;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class VariableDalJpa extends BaseDalJpa implements VariableDal {

	@Autowired
	private EntityManager em;


	private VariableRepository variableRepository;

	@Autowired
	public void setVariableRepository(VariableRepository variableRepository) {
		this.variableRepository = variableRepository;
	}

	//kept native to support < 0 userId, groupId for OB/IMS
	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<VariableInfo> getList(Options options) {
		CustomFilters filters = prepareFilters(options, false);
		return getList(em,
				"select v.variableName, v.variableValue, u.userName, g.name, a.name from rbac.variable v "
						+ "left join rbac.application a on (a.applicationId=v.applicationId) "
						+ "left join rbac.groupTable g on (g.groupId=v.groupId) "
						+ "left join rbac.userTable u on (u.userId=v.userId) ",
				options, filters);
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public List<VariableInfoV2> getListV2(Options options) {
		CustomFilters filters = prepareFilters(options, true);
		return getListV2(em,
				"select v.variableName, v.variableValue, u.userName, g.name, a.name, c.appKey from rbac.variable v "
						+ "left join rbac.application a on (a.applicationId=v.applicationId) "
						+ "left join rbac.groupTable g on (g.groupId=v.groupId) "
						+ "left join rbac.userTable u on (u.userId=v.userId) "
						+ "left join rbac.childApplication c on (c.childApplicationId = v.childApplicationId) ",
				options, filters);
	}

	/*
	 * To clear the child app variables when application is created or updated.
	 */
	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void cleanVariablesForApplicationChanges() {
		variableRepository.deleteVariablesNotInChildApplications();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void deleteForCascade(Integer userId, Integer groupId, Integer applicationId) {
		StringBuilder queryText = new StringBuilder();
		queryText.append("delete from Variable v where ");
		Map<String, Object> paramList= new HashMap<String, Object>();
		if (userId!= null) {
			queryText.append(" v.userId = :userId ");
			paramList.put("userId", userId);
		}
		else if (groupId!= null) {
			queryText.append(" v.groupId = :groupId ");
			paramList.put("groupId", groupId);
		}
		else if (applicationId!= null) {
			queryText.append(" v.applicationId = :applicationId ");
			paramList.put("applicationId", applicationId);
		}
		log.trace("deleteForCascade; queryText={}", queryText.toString());
		Query query = em.createQuery(queryText.toString());
		if(!paramList.isEmpty()){
			for(String paramKey:paramList.keySet()){
				query.setParameter(paramKey, paramList.get(paramKey));
			}
		}
		query.executeUpdate();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void delete(Variable variable) {
		if (!isVariableExists(variable)) {
			ErrorInfoException errorInfo = new ErrorInfoException(
					"variableDoesntExists", "Variable Doesn't Exist");
			throw errorInfo;
		}
		StringBuilder queryText = new StringBuilder();
		queryText.append("delete from Variable v where ");
		Map<String, Object> paramList= new HashMap<String, Object>();
		if (variable.getGroupId() != null) {
			queryText.append(" v.groupId = :groupId "
					+ " and ");
			paramList.put("groupId", variable.getGroupId());
		} else {
			queryText.append(" v.groupId IS NULL " + " and ");
		}
		if (variable.getUserId() != null) {
			queryText.append(" v.userId = :userId "
					+ " and ");
			paramList.put("userId", variable.getUserId());
		} else {
			queryText.append(" v.userId IS NULL " + " and ");
		}
		if (variable.getApplicationId() != null) {
			queryText.append(" v.applicationId = :applicationId "
					+   " and ");
			paramList.put("applicationId", variable.getApplicationId());
		} else {
			queryText.append(" v.applicationId IS NULL " + " and ");
		}
		if (variable.getChildApplicationId() != null) {
			queryText.append(" v.childApplicationId = :childApplicationId "
					+   " and ");
			paramList.put("childApplicationId", variable.getChildApplicationId());
		} else {
			queryText.append(" v.childApplicationId IS NULL " + " and ");
		}
		queryText.append(" v.variableName = :variableName ");
		paramList.put("variableName", variable.getVariableName());
		log.trace("delete; queryText={}", queryText.toString());
		Query query = em.createQuery(queryText.toString());
		if(!paramList.isEmpty()){
			for(String paramKey:paramList.keySet()){
				query.setParameter(paramKey, paramList.get(paramKey));
			}
		}
		query.executeUpdate();
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Variable create(Variable variable) {
		if (!isVariableValid(variable)) {
			ErrorInfoException errorInfo = new ErrorInfoException("rbacIdsInvalid", "RBAC Id's are invalid");
			throw errorInfo;
		}
		if (isVariableExists(variable)) {
			//changed to allow hd ob to update variable in case it already exists
			/*ErrorInfoException errorInfo = new ErrorInfoException(
					"variableAlreadyExists", "Variable Already Exists");
			throw errorInfo;*/
			return variableRepository.save(variable);
		}
		else{
			variableRepository.save(variable);
		}
		return variable;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Variable update(Variable variable) {
		Integer existingVariableId = getExistingVariableId(variable);
		if (existingVariableId == null || existingVariableId < 1) {
			ErrorInfoException errorInfo = new ErrorInfoException("variableDoesntExists", "Variable Doesn't Exist");
			throw errorInfo;
		}

		if (!isVariableValid(variable)) {
			ErrorInfoException errorInfo = new ErrorInfoException("rbacIdsInvalid", "RBAC Id's are invalid");
			throw errorInfo;
		}
		Variable existingVariable = em.find(Variable.class, existingVariableId);
		if(existingVariable==null){
			ErrorInfoException errorInfo = new ErrorInfoException("updateVariableFailure", "Updation of Variable Failed");
			throw errorInfo;
		}
		existingVariable.setVariableValue(variable.getVariableValue());

		return variableRepository.save(existingVariable);
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public boolean isVariableExists(Variable variable) {

		Integer existingId = getExistingVariableId(variable);
		if (existingId != null) {
			log.debug("isVariableExists; existingId={}"+existingId);
			return existingId > 0 ? true : false;
		}
		return false;
	}

	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	private Integer getExistingVariableId(Variable variable){
		StringBuilder queryText = new StringBuilder();
		Map<String, Object> paramList= new HashMap<String, Object>();
		queryText.append("select v.variableId from Variable v where ");
		if (variable.getGroupId() != null) {
			queryText.append(" v.groupId = :groupId "
					+ " and ");
			paramList.put("groupId", variable.getGroupId());
		} else {
			queryText.append(" v.groupId IS NULL " + " and ");
		}
		if (variable.getUserId() != null) {
			queryText.append(" v.userId = :userId "
					+ " and ");
			paramList.put("userId", variable.getUserId());
		} else {
			queryText.append(" v.userId IS NULL " + " and ");
		}
		if (variable.getApplicationId() != null) {
			queryText.append(" v.applicationId = :applicationId "
					+ " and ");
			paramList.put("applicationId", variable.getApplicationId());
		} else {
			queryText.append(" v.applicationId IS NULL " + " and ");
		}
		if (variable.getChildApplicationId() != null) {
			queryText.append(" v.childApplicationId = :childApplicationId "
					+ " and ");
			paramList.put("childApplicationId", variable.getChildApplicationId());
		} else {
			queryText.append(" v.childApplicationId IS NULL " + " and ");
		}
		queryText.append(" v.variableName= :variableName ");
		paramList.put("variableName", variable.getVariableName());
		//queryText.append(" ) then 1 else 0 end");

		log.trace("isVariableExists; queryText={}", queryText.toString());

		TypedQuery<Integer> query = em.createQuery(queryText.toString(), Integer.class);
		if(!paramList.isEmpty()){
			for(String paramKey:paramList.keySet()){
				query.setParameter(paramKey, paramList.get(paramKey));
			}
		}
		Integer queryResult = null;
		try{
			queryResult = query.getSingleResult();
		}
		catch(NoResultException e){
			return null;
		}
		return queryResult;
	}

	//kept native to support < 0 userId, groupId for OB/IMS
	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public boolean isVariableValid(Variable variable) {
		StringBuilder queryText = new StringBuilder();
		StringBuilder whereClause = new StringBuilder();
		List<Object> paramList= new LinkedList<Object>();
		queryText.append(" select case when COUNT(1) >= 1 then 1 else 0 end from ");
		whereClause.append(" where ");
		if (variable.getApplicationId() != null) {
			queryText.append(" rbac.application a ");
			whereClause.append(" a.applicationId = ? ");
			paramList.add(variable.getApplicationId());
		}
		if (variable.getGroupId() != null) {
			if (variable.getApplicationId() != null) {
				queryText.append(" ,");
				whereClause.append(" and ");
			}
			queryText.append(" rbac.groupTable g ");
			whereClause.append(" g.groupId = ? ");
			paramList.add(variable.getGroupId());
		}
		if (variable.getUserId() != null) {
			if ((variable.getGroupId() != null)
					|| (variable.getApplicationId() != null)) {
				queryText.append(" ,");
				whereClause.append(" and ");
			}
			queryText.append(" rbac.userTable u ");
			whereClause.append(" u.userId = ? ");
			paramList.add(variable.getUserId());
		}
		if (variable.getChildApplicationId() != null) {
			if ((variable.getGroupId() != null)
					|| (variable.getApplicationId() != null)
					|| (variable.getUserId() != null)) {
				queryText.append(" ,");
				whereClause.append(" and ");
			}
			queryText.append(" rbac.childApplication c ");
			whereClause.append(" c.childApplicationId = ? ");
			paramList.add(variable.getChildApplicationId());
		}
		//whereClause.append(") then 1 else 0 end");
		queryText.append(whereClause);
		log.trace("isVariableValid; queryText={}", queryText.toString());

		Query query = em.createNativeQuery(queryText.toString());
		if(!paramList.isEmpty()){
			for(int i=1; i<=paramList.size();i++ ){
				query.setParameter(i, paramList.get(i-1));
			}
		}
		if (query != null && query.getSingleResult() != null) {
			log.debug("isVariableValid; queryResult={}"
					+ query.getSingleResult());
			return (Integer) query.getSingleResult()==1?true:false;
		}
		return false;
	}

	private CustomFilters prepareFilters(Options options, boolean isV2) {

		CustomFilters result = new CustomFilters();
		OptionFilter optionFilter = options == null ? null : options
				.getOption(OptionFilter.class);
		Map<String, String> filters = optionFilter == null ? null
				: optionFilter.getFilters();
		if (filters != null) {

			String variableName = filters.get("variableName");
			if (variableName != null && variableName.length() > 0) {
				result.addCondition("v.variableName = ? ");
				result.paramList.add(variableName);
			}

			String applicationName = filters.get("applicationName");
			if (applicationName != null && applicationName.length() > 0) {
				result.addCondition("a.name = ? ");
				result.paramList.add(applicationName);
			}

			String groupName = filters.get("groupName");
			if (groupName != null && groupName.length() > 0) {
				result.addCondition("g.name= ? ");
				result.paramList.add(groupName);
			}

			String userName = filters.get("userName");
			if (userName != null && userName.length() > 0) {
				result.addCondition("u.userName = ? ");
				result.paramList.add(userName);
			}

			if(isV2){
				String appKey = filters.get("appKey");
				if (appKey != null && appKey.length() > 0) {
					result.addCondition(" c.appKey = ? ");
					result.paramList.add(appKey);
				}
			}
		}
		return result;
	}

	//kept native to support < 0 userId, groupId for OB/IMS
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	private List<VariableInfo> getList(EntityManager em, String queryText,
									   Options options, CustomFilters filters) {

		StringBuilder sb = new StringBuilder();
		sb.append(queryText);

		boolean isFirst = true;
		//added to avoid including V2 variables in response.
		filters.addCondition(" v.childApplicationId is NULL ");
		for (String condition : filters.getConditions()) {
			if (isFirst) {
				isFirst = false;
				sb.append(" where ");
			} else {
				sb.append(" and ");
			}
			sb.append(condition);
		}
		log.trace("getList; stringQuery={}", sb.toString());
		Query query = em.createNativeQuery(sb.toString());
		if(!filters.paramList.isEmpty()){
			for(int i=1; i<=filters.paramList.size();i++ ){
				query.setParameter(i, filters.paramList.get(i-1));
			}
		}
		OptionPage optionPage = options != null ? options
				.getOption(OptionPage.class) : null;
		if (optionPage != null) {
			query.setFirstResult(optionPage.getFirstResult());
			query.setMaxResults(optionPage.getMaxResults());
		}
		log.trace("getList; query={}", query);

		@SuppressWarnings("unchecked")
		List<Object[]> list = query.getResultList();
		List<VariableInfo> resultList = new ArrayList<>();
		if(list!=null && !list.isEmpty()){
			for (Object[] pair : list) {
				VariableInfo temp = new VariableInfo();
				temp.setVariableName(pair[0].toString());
				temp.setVariableValue((pair[1] != null ? pair[1]
						.toString() : null));
				temp.setUserName((pair[2] != null ? pair[2]
						.toString() : null));
				temp.setGroupName((pair[3] != null ? pair[3]
						.toString() : null));
				temp.setApplicationName((pair[4] != null ? pair[4]
						.toString() : null));
				resultList.add(temp);
			}
		}
		return resultList;
	}

	//kept native to support < 0 userId, groupId for OB/IMS
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	private List<VariableInfoV2> getListV2(EntityManager em, String queryText,
										   Options options, CustomFilters filters) {

		StringBuilder sb = new StringBuilder();
		sb.append(queryText);

		boolean isFirst = true;
		for (String condition : filters.getConditions()) {
			if (isFirst) {
				isFirst = false;
				sb.append(" where ");
			} else {
				sb.append(" and ");
			}
			sb.append(condition);
		}
		log.trace("getListV2; stringQuery={}", sb.toString());
		Query query = em.createNativeQuery(sb.toString());
		if(!filters.paramList.isEmpty()){
			for(int i=1; i<=filters.paramList.size();i++ ){
				query.setParameter(i, filters.paramList.get(i-1));
			}
		}
		OptionPage optionPage = options != null ? options
				.getOption(OptionPage.class) : null;
		if (optionPage != null) {
			query.setFirstResult(optionPage.getFirstResult());
			query.setMaxResults(optionPage.getMaxResults());
		}
		log.trace("getListV2; query={}", query);

		@SuppressWarnings("unchecked")
		List<Object[]> list = query.getResultList();
		List<VariableInfoV2> resultList = new ArrayList<VariableInfoV2>();
		if(list!=null && !list.isEmpty()){
			for (Object[] pair : list) {
				VariableInfoV2 temp = new VariableInfoV2();
				temp.setVariableName(pair[0].toString());
				temp.setVariableValue((pair[1] != null ? pair[1]
						.toString() : null));
				temp.setUserName((pair[2] != null ? pair[2]
						.toString() : null));
				temp.setGroupName((pair[3] != null ? pair[3]
						.toString() : null));
				temp.setApplicationName((pair[4] != null ? pair[4]
						.toString() : null));
				temp.setAppKey((pair[5] != null ? pair[5]
						.toString() : null));
				resultList.add(temp);
			}
		}
		return resultList;
	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Variable toVariable(VariableInfo variableInfo) {
		if(variableInfo.getVariableName()==null ||
				(variableInfo.getApplicationName()==null && variableInfo.getGroupName()==null && variableInfo.getUserName()==null)){
			ErrorInfoException errorInfo = new ErrorInfoException(
					"missingParams", "One or more of required params are missing");
			throw errorInfo;
		}
		Variable variable = new Variable();
		variable.setVariableName(variableInfo.getVariableName());
		variable.setVariableValue(variableInfo.getVariableValue());
		try{
			if(variableInfo.getApplicationName()!=null && !variableInfo.getApplicationName().isEmpty()){
				variable.setApplicationId(Lookup.getApplicationId(variableInfo.getApplicationName()));
			}
			if(variableInfo.getGroupName()!=null && !variableInfo.getGroupName().isEmpty()){
				variable.setGroupId(Lookup.getGroupId(variableInfo.getGroupName()));
			}
			if(variableInfo.getUserName()!=null && !variableInfo.getUserName().isEmpty()){
				variable.setUserId(Lookup.getUserId(variableInfo.getUserName()));
			}
		}
		catch(Exception e){
			log.error("toVariable Exception={};",e);
		}
		finally{
			if((variableInfo.getApplicationName()!=null && !variableInfo.getApplicationName().isEmpty() && variable.getApplicationId()==null)
					|| (variableInfo.getGroupName()!=null && !variableInfo.getGroupName().isEmpty() && variable.getGroupId() == null)
					|| (variableInfo.getUserName()!=null && !variableInfo.getUserName().isEmpty() && variable.getUserId() == null)){
				ErrorInfoException errorInfo = new ErrorInfoException(
						"invalidParams", "applicationName/groupName/userName are invalid");
				throw errorInfo;
			}
		}
		return variable;

	}

	@Override
	@Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
	public Variable toVariableV2(VariableInfoV2 variableInfo) {
		if(variableInfo.getVariableName()==null ||
				(variableInfo.getApplicationName()==null && variableInfo.getGroupName()==null && variableInfo.getUserName()==null
						&& variableInfo.getAppKey()==null)){
			ErrorInfoException errorInfo = new ErrorInfoException("missingParams", "One or more of required params are missing");
			throw errorInfo;
		}
		Variable variable = new Variable();
		variable.setVariableName(variableInfo.getVariableName());
		variable.setVariableValue(variableInfo.getVariableValue());
		try{
			if(variableInfo.getApplicationName()!=null && !variableInfo.getApplicationName().isEmpty()){
				variable.setApplicationId(Lookup.getApplicationId(variableInfo.getApplicationName()));
			}
			if(variableInfo.getGroupName()!=null && !variableInfo.getGroupName().isEmpty()){
				variable.setGroupId(Lookup.getGroupId(variableInfo.getGroupName()));
			}
			if(variableInfo.getUserName()!=null && !variableInfo.getUserName().isEmpty()){
				variable.setUserId(Lookup.getUserId(variableInfo.getUserName()));
			}
			if(variableInfo.getAppKey()!=null && !variableInfo.getAppKey().isEmpty()){
				ChildApplication childApp = Lookup.getChildApplicationByAppKeyNew(variableInfo.getAppKey());
				if(childApp!=null){
					variable.setChildApplicationId(childApp.getChildApplicationId());
				}
			}
		}
		catch(Exception e){
			log.error("toVariable Exception={};",e);
		}
		finally{
			if((variableInfo.getApplicationName()!=null && !variableInfo.getApplicationName().isEmpty() && variable.getApplicationId()==null)
					|| (variableInfo.getGroupName()!=null && !variableInfo.getGroupName().isEmpty() && variable.getGroupId() == null)
					|| (variableInfo.getUserName()!=null && !variableInfo.getUserName().isEmpty() && variable.getUserId() == null)
					|| (variableInfo.getAppKey()!=null && !variableInfo.getAppKey().isEmpty() && variable.getChildApplicationId() == null)){
				ErrorInfoException errorInfo = new ErrorInfoException(
						"invalidParams", "applicationName/groupName/userName/appKey are invalid");
				throw errorInfo;
			}
		}
		return variable;

	}

	private static class CustomFilters extends Filters {
		List<Object> paramList= new LinkedList<Object>();

	}

}

