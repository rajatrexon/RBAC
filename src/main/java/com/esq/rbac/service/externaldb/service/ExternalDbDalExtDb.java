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

package com.esq.rbac.service.externaldb.service;


import com.esq.rbac.service.application.domain.Application;
import com.esq.rbac.service.application.service.ApplicationDal;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.session.constraintdata.domain.ConstraintData;
import jakarta.ws.rs.core.MultivaluedMap;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class ExternalDbDalExtDb implements ExternalDbDal {


	private static final Map<String, String> dateFormatMap = new HashMap<String, String>();

	static {
		dateFormatMap.put("mm/dd/yyyy", "101");
		dateFormatMap.put("yy.mm.dd", "102");
		dateFormatMap.put("dd/mm/yy", "3");
		dateFormatMap.put("dd.mm.yy", "4");
		dateFormatMap.put("dd-mm-yy", "105");
		dateFormatMap.put("yyyy/mm/dd", "111");
		System.out.println("Got it");
	}
	private static final Logger log = LoggerFactory
			.getLogger(ExternalDbDalExtDb.class);
	
	private Map<String, DataSource> dataSourceMap;
	
	public void setDataSourceMap(Map<String, DataSource> dataSourceMap) {
		dataSourceMap.put("url", dataSourceMap.get("url"));
		log.info("setDataSourceMap; dataSourceMap={}", dataSourceMap);
		this.dataSourceMap = dataSourceMap;
	}
	private ApplicationDal applicationDal;
	
	@Autowired
	public void setApplicationDal(ApplicationDal applicationDal) {
		this.applicationDal = applicationDal;
	}
	// RBAC-853
	public void validateDate(String dateString, String format) {
		if (dateString == null) {
			return;
		}
		try {
			if (format == null) {
				// used joda datetime, look at ExternalDbDalExtDbIT
				new DateTime(dateString);
			} else {
				new SimpleDateFormat(format).parse(dateString);
			}
		} catch (Exception e) {
			log.error("validateDate; dateString={}; format={}; e={};",
					dateString, format, e);
			throw new ErrorInfoException("invalidDate");
		}
	}

	@Override
	public ConstraintData[] getData(String appName, String query, MultivaluedMap<String, String> parameters) {
		log.debug("getData; appName={}; query={}", appName, query);
		DataSource dataSource = dataSourceMap.get(appName);
		if(dataSource==null){
			log.error("getData; noDataSourceFound appName={}",appName);
			throw new ErrorInfoException("noDataSourceFound");
		}
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);

		List<ConstraintData> constraintDataList = jdbcTemplate.query(query,new MapSqlParameterSource(parameters),
				new RowMapper<ConstraintData>() {
					@Override
					public ConstraintData mapRow(ResultSet rs, int rowNum)
							throws SQLException {
						ConstraintData constraintData = new ConstraintData();
						constraintData.setId(rs.getObject(1).toString());
						constraintData.setValue(rs.getString(2));
						return constraintData;
					}
				});
		log.debug("getData; constraintDataList={}",constraintDataList);
		return constraintDataList.toArray(new ConstraintData[constraintDataList
				.size()]);
	}
	
	@Override
	public List<Map<String, Object>> getRowSetData(String appName, String query, Map<String, String> filters) {
		log.trace("getRowSetData; appName={}; query={}", appName, query);
		DataSource dataSource = dataSourceMap.get(appName);
		if(dataSource==null){
			log.error("getRowSetData; noDataSourceFound appName={}",appName);
			throw new ErrorInfoException("noDataSourceFound");
		}
		MapSqlParameterSource parameterSource = new MapSqlParameterSource(filters);
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> customDataList = null;
		customDataList = jdbcTemplate.queryForList(query, parameterSource);
		log.trace("getRowSetData; constraintDataList={}",customDataList);
		return customDataList;
	}
	
	@Override
	public List<Map<String, Object>> getCustomData(String appName, String sql, Map<String, String> filters, String constraintName, List<?> restrictionIds ) {
		log.trace("getCustomData; appName={};", appName);
		
        MapSqlParameterSource parameterSource = new MapSqlParameterSource(filters);
        String loggedinUserName = filters.get("currentLoggedInUser");
        //validate date fields
        validateDate(filters.get("toDate"), null);
        validateDate(filters.get("fromDate"), null);
		if (constraintName.equals("auditLog")
				|| constraintName.equals("auditLogCount")) {
			if (filters.get("timeOffset") != null
					&& filters.get("localeDateFormat") != null && !sql.contains("@fromDerbyDate")) {
				String offSet = filters.get("timeOffset");
				String localeDateFormat = dateFormatMap.get(filters
						.get("localeDateFormat")) == null ? "101"
						: dateFormatMap
								.get(filters.get("localeDateFormat"));
				try {
					int timeOffset = Integer.parseInt(offSet);
					sql = sql
							.replace(
									",createdTime as",
									", convert(varchar,DATEADD(MI, -("
											+ timeOffset
											+ "), createdTime), "
											+ localeDateFormat
											+ ") + ' ' + convert(varchar,DATEADD(MI, -("
											+ timeOffset
											+ "), createdTime), 108) as ");
				} catch (Exception e) {
				}
			} else if (filters.get("timeOffset") != null && filters.get("localeDateFormat") != null
					&& sql.contains("@fromDerbyDate")) {
				try {
					sql = sql.replace(",createdTime as", ",createdTime || ' UTC' as");
				} catch (Exception exception) {
				}
			}
			if (filters.get("toDate") != null) {
				sql = sql.replace("@toDate",
						" and a.createdTime <= CONVERT(datetime,'"
								+ filters.get("toDate") + "', 127)");
			} else {
				sql = sql.replace("@toDate", "");
			}
			if (filters.get("fromDate") != null) {
				sql = sql.replace("@fromDate",
						" and a.createdTime >= CONVERT(datetime,'"
								+ filters.get("fromDate") + "', 127)");
			} else {
				sql = sql.replace("@fromDate", "");
			}
			
			//Changes for DerbyDate
			if (filters.get("toDate") != null) {
		        sql = sql.replace("@toDerbyDate", " and a.createdTime <= TIMESTAMP('" + ((String)filters
		              .get("toDate")).replace("T", " ").replace("Z", "") + "')");
		      } else {
		        sql = sql.replace("@toDerbyDate", "");
		      } 
		      if (filters.get("fromDate") != null) {
		        sql = sql.replace("@fromDerbyDate", " and a.createdTime >= TIMESTAMP('" + ((String)filters
		            .get("fromDate")).replace("T", " ").replace("Z", "") + "')");
		      } else {
		        sql = sql.replace("@fromDerbyDate", "");
		      } 
		      
			
			if (filters.get("userId") != null
					&& filters.get("userId").equals("All")) {
				if(restrictionIds==null){
					sql = sql.replace("and userId IN (:userId)", " and userId > 0 ");
				}
				else if(restrictionIds.isEmpty()){
					sql = sql.replace(":userId", "''");
				}
				else{
					sql = sql.replace(":userId", StringUtils.join(restrictionIds, ','));
				}
				//parameterSource.addValue("userId", restrictionIds);
			}else if (filters.get("userId") != null
					&& !filters.get("userId").equals("All")) {
				sql = sql.replace("UserName as \"User Name\",", " ");
			}
			
		} 
		
		else if (constraintName.equals("auditLogAppTargetOperation") || constraintName.equals("auditLogAppTargetOperationCount")) {
			if (filters.get("timeOffset") != null && filters.get("localeDateFormat") != null && !sql.contains("@fromDerbyDate")){
				String offSet = filters.get("timeOffset");
				String localeDateFormat = dateFormatMap.get(filters.get("localeDateFormat")) == null ? "101" : dateFormatMap.get(filters.get("localeDateFormat"));
				try {
					int timeOffset = Integer.parseInt(offSet);
					sql = sql
							.replace(
									",createdTime as",
									", convert(varchar,DATEADD(MI, -("
											+ timeOffset
											+ "), createdTime), "
											+ localeDateFormat
											+ ") + ' ' + convert(varchar,DATEADD(MI, -("
											+ timeOffset
											+ "), createdTime), 108) as ");
				} catch (Exception e) {
				}
			} else if (filters.get("timeOffset") != null && filters.get("localeDateFormat") != null
					&& sql.contains("@fromDerbyDate")) {
				try {
					sql = sql.replace(",createdTime as", ",createdTime || ' UTC' as");
				} catch (Exception exception) {
				}
			}
			if (filters.get("toDate") != null) {
				sql = sql.replace("@toDate",
						" and a.createdTime <= CONVERT(datetime,'"
								+ filters.get("toDate") + "', 127)");
			} else {
				sql = sql.replace("@toDate", "");
			}
			if (filters.get("fromDate") != null) {
				sql = sql.replace("@fromDate",
						" and a.createdTime >= CONVERT(datetime,'"
								+ filters.get("fromDate") + "', 127)");
			} else {
				sql = sql.replace("@fromDate", "");
			}
			//Changes for DerbyDate
			if (filters.get("toDate") != null) {
		        sql = sql.replace("@toDerbyDate", " and a.createdTime <= TIMESTAMP('" + ((String)filters
		              .get("toDate")).replace("T", " ").replace("Z", "") + "')");
		      } else {
		        sql = sql.replace("@toDerbyDate", "");
		      } 
		      if (filters.get("fromDate") != null) {
		        sql = sql.replace("@fromDerbyDate", " and a.createdTime >= TIMESTAMP('" + ((String)filters
		            .get("fromDate")).replace("T", " ").replace("Z", "") + "')");
		      } else {
		        sql = sql.replace("@fromDerbyDate", "");
		      } 
			
			 if (filters.get("operationId") != null && !filters.get("operationId").isEmpty()) {
					
					String[] opertionIdArr=filters.get("operationId").split(",");
					
					sql = sql.replace(":operationId", StringUtils.join(opertionIdArr, ','));
		       }
			
			if (filters.get("userId") != null
					&& filters.get("userId").equals("All")) {
				if(restrictionIds==null){
					sql = sql.replace("and userId IN (:userId)", " and userId > 0 ");
				}
				else if(restrictionIds.isEmpty()){
					sql = sql.replace(":userId", "''");
				}
				else{
					sql = sql.replace(":userId", StringUtils.join(restrictionIds, ','));
				}
				//parameterSource.addValue("userId", restrictionIds);
			}else if (filters.get("userId") != null
					&& !filters.get("userId").equals("All")) {
				sql = sql.replace("UserName as \"User Name\",", " ");
			}
			
		}
		
		else if (constraintName.equals("auditLogAppTarget") || constraintName.equals("auditLogAppTargetCount")) {
			if (filters.get("timeOffset") != null && filters.get("localeDateFormat") != null && !sql.contains("@fromDerbyDate")){
				String offSet = filters.get("timeOffset");
				String localeDateFormat = dateFormatMap.get(filters.get("localeDateFormat")) == null ? "101" : dateFormatMap.get(filters.get("localeDateFormat"));
				try {
					int timeOffset = Integer.parseInt(offSet);
					sql = sql
							.replace(
									",createdTime as",
									", convert(varchar,DATEADD(MI, -("
											+ timeOffset
											+ "), createdTime), "
											+ localeDateFormat
											+ ") + ' ' + convert(varchar,DATEADD(MI, -("
											+ timeOffset
											+ "), createdTime), 108) as ");
				} catch (Exception e) {
				}
			} else if (filters.get("timeOffset") != null && filters.get("localeDateFormat") != null
					&& sql.contains("@fromDerbyDate")) {
				try {
					sql = sql.replace(",createdTime as", ",createdTime || ' UTC' as");
				} catch (Exception exception) {
				}
			}
			if (filters.get("toDate") != null) {
				sql = sql.replace("@toDate",
						" and a.createdTime <= CONVERT(datetime,'"
								+ filters.get("toDate") + "', 127)");
			} else {
				sql = sql.replace("@toDate", "");
			}
			if (filters.get("fromDate") != null) {
				sql = sql.replace("@fromDate",
						" and a.createdTime >= CONVERT(datetime,'"
								+ filters.get("fromDate") + "', 127)");
			} else {
				sql = sql.replace("@fromDate", "");
			}
			
			//Changes for DerbyDate
			if (filters.get("toDate") != null) {
				sql = sql.replace("@toDerbyDate", " and a.createdTime <= TIMESTAMP('"
						+ ((String) filters.get("toDate")).replace("T", " ").replace("Z", "") + "')");
			} else {
				sql = sql.replace("@toDerbyDate", "");
			}
			if (filters.get("fromDate") != null) {
				sql = sql.replace("@fromDerbyDate", " and a.createdTime >= TIMESTAMP('"
						+ ((String) filters.get("fromDate")).replace("T", " ").replace("Z", "") + "')");
			} else {
				sql = sql.replace("@fromDerbyDate", "");
			}
			if (filters.get("userId") != null
					&& filters.get("userId").equals("All")) {
				if(restrictionIds==null){
					sql = sql.replace("and userId IN (:userId)", " and userId > 0 ");
				}
				else if(restrictionIds.isEmpty()){
					sql = sql.replace(":userId", "''");
				}
				else{
					sql = sql.replace(":userId", StringUtils.join(restrictionIds, ','));
				}
				//parameterSource.addValue("userId", restrictionIds);
			}else if (filters.get("userId") != null
					&& !filters.get("userId").equals("All")) {
				sql = sql.replace("UserName as \"User Name\",", " ");
			}
			
		}
		
		else if (constraintName.equals("auditLogApp") || constraintName.equals("auditLogAppCount")) {
			if (filters.get("timeOffset") != null && filters.get("localeDateFormat") != null && !sql.contains("@fromDerbyDate")){
				String offSet = filters.get("timeOffset");
				String localeDateFormat = dateFormatMap.get(filters.get("localeDateFormat")) == null ? "101" : dateFormatMap.get(filters.get("localeDateFormat"));
				try {
					int timeOffset = Integer.parseInt(offSet);
					sql = sql
							.replace(
									",createdTime as",
									", convert(varchar,DATEADD(MI, -("
											+ timeOffset
											+ "), createdTime), "
											+ localeDateFormat
											+ ") + ' ' + convert(varchar,DATEADD(MI, -("
											+ timeOffset
											+ "), createdTime), 108) as ");
				} catch (Exception e) {
				}
			} else if (filters.get("timeOffset") != null && filters.get("localeDateFormat") != null
					&& sql.contains("@fromDerbyDate")) {
				try {
					sql = sql.replace(",createdTime as", ",createdTime || ' UTC' as");
				} catch (Exception exception) {
				}
			}
			if (filters.get("toDate") != null) {
				sql = sql.replace("@toDate",
						" and a.createdTime <= CONVERT(datetime,'"
								+ filters.get("toDate") + "', 127)");
			} else {
				sql = sql.replace("@toDate", "");
			}
			if (filters.get("fromDate") != null) {
				sql = sql.replace("@fromDate",
						" and a.createdTime >= CONVERT(datetime,'"
								+ filters.get("fromDate") + "', 127)");
			} else {
				sql = sql.replace("@fromDate", "");
			}	
			
			//Changes for DerbyDate
			if (filters.get("toDate") != null) {
				sql = sql.replace("@toDerbyDate", " and a.createdTime <= TIMESTAMP('"
						+ ((String) filters.get("toDate")).replace("T", " ").replace("Z", "") + "')");
			} else {
				sql = sql.replace("@toDerbyDate", "");
			}
			if (filters.get("fromDate") != null) {
				sql = sql.replace("@fromDerbyDate", " and a.createdTime >= TIMESTAMP('"
						+ ((String) filters.get("fromDate")).replace("T", " ").replace("Z", "") + "')");
			} else {
				sql = sql.replace("@fromDerbyDate", "");
			}
			if (filters.get("applicationId") != null && !filters.get("applicationId").isEmpty()) {
				List<Integer> applicationIds = new ArrayList<Integer>();

				if (loggedinUserName != null) {
					List<Application> tempApplicationList = applicationDal
							.getUserAuthorizedApps(loggedinUserName.toString());
					for (Application application : tempApplicationList) {
						if (!applicationIds.contains(application.getApplicationId())) {
							applicationIds.add(application.getApplicationId());
						}
					}
				} else {
					applicationIds.add(-1);
				}
				if (filters.get("applicationId").equals("All")) {
					if (applicationIds != null && !applicationIds.isEmpty()) {
						sql = sql.replace(":applicationId", StringUtils.join(applicationIds, ","));
					}
				} else if (isParsableFromStringToInteger(filters.get("applicationId"))
						&& applicationIds.contains(Integer.parseInt(filters.get("applicationId")))) {
					// check filters.get("applicationId") is instance of
					sql = sql.replace(":applicationId", filters.get("applicationId"));
				} else {
					sql = sql.replace(":applicationId", "-1");
				}

			}else {
	    	   sql = sql.replace(":applicationId", "-1");
	       }
			
			if (filters.get("userId") != null
					&& filters.get("userId").equals("All")) {
				if(restrictionIds==null){
					sql = sql.replace("and userId IN (:userId)", " and userId > 0 ");
				}
				else if(restrictionIds.isEmpty()){
					sql = sql.replace(":userId", "''");
				}
				else{
					sql = sql.replace(":userId", StringUtils.join(restrictionIds, ','));
				}
				//parameterSource.addValue("userId", restrictionIds);
			}else if (filters.get("userId") != null
					&& !filters.get("userId").equals("All")) {
				sql = sql.replace("UserName as \"User Name\",", " ");
			}
			
		}
		
		else if (constraintName.equals("accessMatrix")
				|| constraintName.equals("accessMatrixCount")) {
			if (filters.get("userId") != null) {
					if(isParsableFromStringToInteger(filters.get("userId"))) {
				sql = sql.replace("@userNameField,", "");
				
			} else {

				sql = sql.replace(":userId", filters.get("userId"));
				sql = sql.replace("@userNameField",
						"username + ' [Group Name: ' + groupName  + ']' as \"User Name\"");
			}
			}
		} else if (constraintName.equals("accessMatrixGroup")
				|| constraintName.equals("accessMatrixGroupCount")) {
			if (filters.get("groupId") != null
					&& !filters.get("groupId").equals("All")) {
				sql = sql.replace("@groupNameField,", "");
			} else if (filters.get("groupId") != null
					&& filters.get("groupId").equals("All")) {
				if(restrictionIds==null){
					sql = sql.replace("where  gr.groupId IN (:groupId)", " where gr.groupId > 0 ");
				}
				else if(restrictionIds.isEmpty()){
					sql = sql.replace(":groupId", "''");
				}
				else{
					sql = sql.replace(":groupId", StringUtils.join(restrictionIds, ','));
				}
				//parameterSource.addValue("groupId", restrictionIds);
				sql = sql.replace("@groupNameField",
						"groupName as \"Group Name\"");

			}
		} else if (constraintName.equals("groupScopeDescription")
				|| constraintName.equals("groupScopeDescriptionCount")) {
			if (filters.get("groupId") != null
					&& !filters.get("groupId").equals("All")) {
				sql = sql.replace("@groupNameField,", "");
			} else if (filters.get("groupId") != null
					&& filters.get("groupId").equals("All")) {
				if(restrictionIds==null){
					sql = sql.replace("and gr.groupId IN (:groupId)", "  and gr.groupId > 0  ");
				}
				else if(restrictionIds.isEmpty()){
					sql = sql.replace(":groupId", "''");
				}
				else{
					sql = sql.replace(":groupId", StringUtils.join(restrictionIds, ','));
				}
				//parameterSource.addValue("groupId", restrictionIds);
			}
		} else if (constraintName.equals("groupDetails")) {
			if (filters.get("groupId") != null
					&& filters.get("groupId").equals("All")) {
				sql = sql.replace("where groupId = :groupId", "  ");
			}

		} else if (constraintName.equals("userDetails")) {
			sql = sql.replace("where userId = :userId","where userId IN ("+filters.get("userId")+")");
			
		} else if (constraintName.equals("userActivity") || constraintName.equals("userActivityCount")){
			if(filters.get("groupId") != null && filters.get("groupId").equals("All")){
				if(restrictionIds==null){
					sql = sql.replace("where u.groupId IN (:groupId)", " where u.groupId > 0 ");
				}
				else if(restrictionIds.isEmpty()){
					sql = sql.replace(":groupId", "''");
				}
				else{
					sql = sql.replace(":groupId", StringUtils.join(restrictionIds, ','));
				}
				//parameterSource.addValue("groupId", restrictionIds);
				/*if(filters.get("fileName") != null){
					sql = sql.replace("u.createdOn as", " convert(varchar,u.createdOn, 101) + ' ' + convert(varchar,u.createdOn, 108) as " );
					sql = sql.replace("loginTime as", " convert(varchar,loginTime, 101) + ' ' + convert(varchar,loginTime, 108) as " );
				}*/
													
			}
			if (filters.get("timeOffset") != null
					&& filters.get("localeDateFormat") != null) {
				String offSet = filters.get("timeOffset");
				String localeDateFormat = dateFormatMap.get(filters
						.get("localeDateFormat")) == null ? "101"
						: dateFormatMap
								.get(filters.get("localeDateFormat"));
				try {
					int timeOffset = Integer.parseInt(offSet);
					sql = sql
							.replace(
									", u.createdOn as",
									", convert(varchar,DATEADD(MI, -("
											+ timeOffset
											+ "), u.createdOn), "
											+ localeDateFormat
											+ ") + ' ' + convert(varchar,DATEADD(MI, -("
											+ timeOffset
											+ "), u.createdOn), 108) as ");
					sql = sql
							.replace(
									", loginTime as",
									", convert(varchar,DATEADD(MI, -("
											+ timeOffset
											+ "), loginTime), "
											+ localeDateFormat
											+ ") + ' ' + convert(varchar,DATEADD(MI, -("
											+ timeOffset
											+ "), loginTime), 108) as ");
				} catch (Exception e) {
				}
			}
			
		}else if (constraintName.equals("loginLog")
				|| constraintName.equals("loginLogCount")) {
			if (filters.get("timeOffset") != null && filters
			        .get("localeDateFormat") != null && !sql.contains("@fromDerbyDate")) {
			String offSet = filters.get("timeOffset");
			String localeDateFormat = dateFormatMap.get(filters
					.get("localeDateFormat")) == null ? "101"
					: dateFormatMap
							.get(filters.get("localeDateFormat"));
			try {
				int timeOffset = Integer.parseInt(offSet);
				sql = sql
						.replace(
								",l.createdTime",
								", convert(varchar,DATEADD(MI, -("
										+ timeOffset
										+ "), l.createdTime), "
										+ localeDateFormat
										+ ") + ' ' + convert(varchar,DATEADD(MI, -("
										+ timeOffset
										+ "), l.createdTime), 108) as createdTime");
			} catch (Exception e) {
			}
			} else if (filters.get("timeOffset") != null && filters.get("localeDateFormat") != null
					&& sql.contains("@fromDerbyDate")) {
				try {
					sql = sql.replace(",l.createdTime", ",l.createdTime || ' UTC'");
				} catch (Exception exception) {
				}
			}
			
			if (filters.get("toDate") != null) {
				sql = sql.replace("@toDate",
						" and l.createdTime <= CONVERT(datetime,'"
								+ filters.get("toDate") + "', 127)");
			} else {
				sql = sql.replace("@toDate", "");
			}
			if (filters.get("fromDate") != null) {
				sql = sql.replace("@fromDate",
						" and l.createdTime >= CONVERT(datetime,'"
								+ filters.get("fromDate") + "', 127)");
			} else {
				sql = sql.replace("@fromDate", "");
			}
			//Changes for DerbyDate
			if (filters.get("toDate") != null) {
				sql = sql.replace("@toDerbyDate", " and l.createdTime <= TIMESTAMP('"
						+ ((String) filters.get("toDate")).replace("T", " ").replace("Z", "") + "')");
			} else {
				sql = sql.replace("@toDerbyDate", "");
			}
			if (filters.get("fromDate") != null) {
				sql = sql.replace("@fromDerbyDate", " and l.createdTime >= TIMESTAMP('"
						+ ((String) filters.get("fromDate")).replace("T", " ").replace("Z", "") + "')");
			} else {
				sql = sql.replace("@fromDerbyDate", "");
			}
			if (filters.get("userName") != null
					&& filters.get("userName").equals("All")) {
				//parameterSource.addValue("userIds", restrictionIds);
				if(restrictionIds==null){
					sql = sql.replace("u.userId IN (:userIds)"," u.userId > 0 ");
				}
				else if(restrictionIds.isEmpty()){
					sql = sql.replace(":userIds", "''");
				}
				else{
					sql = sql.replace(":userIds", StringUtils.join(restrictionIds, ','));
				}
				sql = sql.replace("and l.userId = :userId", "");
			}else if (filters.get("userName") != null
					&& !filters.get("userName").equals("All")) {
				//parameterSource.addValue("userIds", restrictionIds);
				if(restrictionIds==null){
					sql = sql.replace("u.userId IN (:userIds)"," u.userId > 0 ");
				}
				else if(restrictionIds.isEmpty()){
					sql = sql.replace(":userIds", "''");
				}
				else{
					sql = sql.replace(":userIds", StringUtils.join(restrictionIds, ','));
				}
				//userid is passed in username loginlog.js
				parameterSource.addValue("userId",parameterSource.getValues().get("userName"));
				sql = sql.replace("UserName as \"User Name\",", " ");
			}
		}

		DataSource dataSource = dataSourceMap.get(appName);
		if(dataSource==null){
			log.error("getCustomData; noDataSourceFound appName={}",appName);
			throw new ErrorInfoException("noDataSourceFound");
		}
		NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		List<Map<String, Object>> customDataList = null;
		customDataList = jdbcTemplate.queryForList(sql, parameterSource);
		if(log.isTraceEnabled()){
			log.trace("getCustomData; constraintDataList={}",customDataList);
		}
		
		List<Map<String, Object>> fitleredList = new ArrayList<Map<String,Object>>();
		String csvInjectionRegex ="^[+=@-].*";
		if(customDataList != null) {
			for(Map<String, Object> entry :customDataList){
				Map<String, Object> subMap = new HashMap<String, Object>();
				entry.entrySet().forEach(vl ->{
					Object value = vl.getValue();
					if(Pattern.matches(csvInjectionRegex,value+"")) {
						value = "'"+value;
					}
					subMap.put(vl.getKey(), value);
				});
				fitleredList.add(subMap);
			}
		}
		return fitleredList;
	}

	
	private boolean isParsableFromStringToInteger(String input) {
		try {
			Integer.parseInt(input);
			return true;
		}catch(NumberFormatException e) {
			return false;
		}catch(Exception ex) {
			return false;
		}
	}
	
	
	@Override
	public boolean executeProc(String appName, String procStatement,
			Object... arguments) {
		log.trace("executeProc; appName={}; procStatement={}", appName,
				procStatement);
		DataSource dataSource = dataSourceMap.get(appName);
		if(dataSource==null){
			log.error("executeProc; noDataSourceFound appName={}",appName);
			throw new ErrorInfoException("noDataSourceFound");
		}
		JdbcTemplate jdbcTemplate = new JdbcTemplate();
		jdbcTemplate.setDataSource(dataSource);
		jdbcTemplate.update(procStatement, arguments);
		return true;
	}

}
