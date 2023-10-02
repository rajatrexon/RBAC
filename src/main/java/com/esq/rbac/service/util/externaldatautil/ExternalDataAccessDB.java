package com.esq.rbac.service.util.externaldatautil;

import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.scope.scopeconstraint.domain.ScopeConstraint;
import com.esq.rbac.service.util.dal.OptionFilter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import jakarta.activation.DataSource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.util.*;
@Slf4j
public class ExternalDataAccessDB implements ExternalDataAccess{

    @Autowired
    private DataSource dataSource;
    @Autowired
    NamedParameterJdbcTemplate jdbcTemplate;

    private static final String SQL = "SQL";
    public void setDataSource(DataSource dataSource) {
        log.info("setDataSource; dataSource={}", dataSource);
        this.dataSource = dataSource;
    }

    @Override
    public String list(ScopeConstraint scopeConstraint, HttpServletRequest servletRequest, Integer userId) {
        String appName = scopeConstraint.getApplicationName();
        Map<String, String[]> parameterMap = servletRequest.getParameterMap();
        log.trace("list; requestUri={}", servletRequest.getRequestURI());
        MultivaluedMap<String, String> uriInfo = new MultivaluedHashMap<>();
        parameterMap.forEach((key, values) -> uriInfo.addAll(key, Arrays.asList(values)));
        Map<String, String> filters = new LinkedHashMap<String, String>();
        if(servletRequest!=null){
            OptionFilter optionFilter = new OptionFilter(
                    uriInfo);
            filters = optionFilter.getFilters();
        }
        if(scopeConstraint.getSourceType().equals(SQL)){
            if(dataSource==null){
                log.error("list; noDataSourceFound; appName={}",appName);
                throw new ErrorInfoException("noDataSourceFound");
            }
//            NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate((javax.sql.DataSource) dataSource);
            List<Map<String, Object>> customDataList = null;
            customDataList = jdbcTemplate.queryForList(scopeConstraint.getSqlQuery(), filters);
            String response = new Gson().toJson(customDataList).toString();
            return response;
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String update(ScopeConstraint scopeConstraint, String data, String contentType, Integer userId) {
        JsonNode jsonObj;
        try {
            jsonObj = new ObjectMapper().readTree(data);
            Map<String, Object> dataMap = new HashMap<String, Object>();
            Iterator dataKeys = jsonObj.iterator();
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            while(dataKeys.hasNext()){
                String type = (String)dataKeys.next();
                parameters.addValue(type, jsonObj.get(type));
            }
            parameters.addValue("userId", userId);
            KeyHolder keyHolder = new GeneratedKeyHolder();
//            NamedParameterJdbcTemplate jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
            jdbcTemplate.update(scopeConstraint.getUpdateSqlQuery(), parameters, keyHolder);
            String id = keyHolder.getKey().toString();
            dataMap.put("id", id);
            String response = new Gson().toJson(dataMap).toString();
            return response;
        } catch (Exception e) {
            log.error("update; JSONException={}", e);
        }
        return null;

    }
}
