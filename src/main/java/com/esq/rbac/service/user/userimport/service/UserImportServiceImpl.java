package com.esq.rbac.service.user.userimport.service;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import com.esq.rbac.service.exception.ErrorInfoException;
import com.esq.rbac.service.lookup.Lookup;
import com.esq.rbac.service.makerchecker.service.MakerCheckerDalJpa;
import com.esq.rbac.service.user.vo.UserImportDecorator;
import com.esq.rbac.service.util.DeploymentUtil;
import org.dozer.DozerBeanMapper;
import org.supercsv.io.dozer.CsvDozerBeanReader;
import org.supercsv.io.dozer.CsvDozerBeanWriter;
import org.supercsv.io.dozer.ICsvDozerBeanReader;
import org.supercsv.io.dozer.ICsvDozerBeanWriter;
import org.supercsv.prefs.CsvPreference;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import com.esq.rbac.service.user.domain.User;
import com.esq.rbac.service.user.service.UserDal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserImportServiceImpl implements UserImportService {

    private DozerBeanMapper configuredBeanMapper;

    private UserDal userDal;

    private List<String> responseHeaderList;

    Pattern p = Pattern.compile("[^A-Za-z0-9@._\\- ]");

    //RBAC-1892 Start
    private DozerBeanMapper dozerBeanMapperExport;
    private  List<String> exportHeaderList;

    private DeploymentUtil deploymentUtil;

    @Autowired
    public void setDeploymentUtil(DeploymentUtil deploymentUtil) {
        this.deploymentUtil = deploymentUtil;
    }


    public void setDozerBeanMapperExport(DozerBeanMapper dozerBeanMapper) {
        log.info("setDozerBeanMapperExport; dozerBeanMapper={};", dozerBeanMapper);
        this.dozerBeanMapperExport = dozerBeanMapper;
    }

    public void setExportHeaderList(List<String> exportHeaderList) {
        this.exportHeaderList = exportHeaderList;
    }
    //RBAC-1892 End

    @Autowired
    public void setUserDal(UserDal userDal) {
        this.userDal = userDal;
    }

    public void setResponseHeaderList(List<String> responseHeaderList) {
        this.responseHeaderList = responseHeaderList;
    }

    public void setDozerBeanMapper(DozerBeanMapper dozerBeanMapper) {
        log.info("setDozerBeanMapper; dozerBeanMapper={};", dozerBeanMapper);
        this.configuredBeanMapper = dozerBeanMapper;
    }

    @Override
    public void importFromCSV(Reader reader, Writer writer,
                              Integer loggedInUserId, Long currentTenantId) throws Exception {
        ICsvDozerBeanReader beanReader = null;
        ICsvDozerBeanWriter beanWriter = null;
        try {
            beanReader = new CsvDozerBeanReader(reader,
                    CsvPreference.STANDARD_PREFERENCE, configuredBeanMapper);
            beanWriter = new CsvDozerBeanWriter(writer,
                    CsvPreference.STANDARD_PREFERENCE, configuredBeanMapper);
            beanReader.getHeader(true);

            String[] outpuHeaders = new String[responseHeaderList.size()];
            responseHeaderList.toArray(outpuHeaders);
            beanWriter.writeHeader(outpuHeaders);

            UserImportDecorator userForImportTemp;
            Map<String, UserImportDecorator> usersList = new LinkedHashMap<String, UserImportDecorator>();
            try {
                while ((userForImportTemp = beanReader
                        .read(UserImportDecorator.class)) != null) {
                    usersList.put(
                            beanReader.getLineNumber() + ":"
                                    + beanReader.getRowNumber(),
                            userForImportTemp);
                    userForImportTemp = null;
                }
            } catch (Exception e) {
                log.error(
                        "importFromCSV; lineNo:rowNo={}; Exception={};",
                        beanReader.getLineNumber() + ":"
                                + beanReader.getRowNumber(), e);
                if(e instanceof ErrorInfoException){
                    throw e;
                }
                throw new ErrorInfoException("csvImportInvalidFile");
            }
            if(log.isDebugEnabled()){
                log.debug("importFromCSV; size={}; usersList={}; ", usersList.size(), usersList);
            }
            for (String lineRowNum : usersList.keySet()) {
                UserImportDecorator userForImport = usersList.get(lineRowNum);
                if (userForImport == null || userForImport.getUser() == null) {
                    log.info("importFromCSV; lineNo:rowNo={}; is null",
                            lineRowNum);
                    continue;
                }

                if (userForImport.getUser().getUserName() == null
                        || userForImport.getUser().getUserName().trim()
                        .length() == 0) {
                    logErrorMessage(userForImport, beanWriter, lineRowNum, "Username not set");
                    continue;
                }

                Matcher userNameSpecialChar = p.matcher(userForImport.getUser().getUserName());
                Matcher firstNameSpecialChar = p.matcher(
                        userForImport.getUser().getFirstName() != null ? userForImport.getUser().getFirstName() : "");
                Matcher lastNameSpecialChar = p.matcher(
                        userForImport.getUser().getLastName() != null ? userForImport.getUser().getLastName() : "");
                boolean emailAddressSpecialChar = Pattern.matches(DeploymentUtil.EMAIL_PATTERN,
                        userForImport.getUser().getEmailAddress() != null ? userForImport.getUser().getEmailAddress()
                                : "");

                if (userNameSpecialChar.find() || firstNameSpecialChar.find() || lastNameSpecialChar.find()
                        || !emailAddressSpecialChar) {
                    logErrorMessage(userForImport, beanWriter, lineRowNum,
                            "Given sheet data contains special character in either User Name, First Name,Last Name or Email Address");
                    continue;
                }
                if(deploymentUtil.isAssertPasswords()){
                    if (userForImport.getUser().getChangePassword()==null){
                        throw new ErrorInfoException("mustRequirePassword");
                    }
                }

                User existingUser = userDal.getByUserName(userForImport.getUser().getUserName());

                if (userForImport.getUser().getIsEnabled() == null) {
                    userForImport.getUser().setIsEnabled(true);
                }
                if (userForImport.getUser().getIsLocked() == null) {
                    userForImport.getUser().setIsLocked(false);
                }
                Integer groupId = null;
                if (userForImport.getGroupName() != null) {
                    groupId = Lookup.getGroupId(userForImport
                            .getGroupName());
                    if (groupId == -1) {
                        logErrorMessage(userForImport, beanWriter, lineRowNum, "Group not found");
                        continue;
                    } else {
                        userForImport.getUser().setGroupId(groupId);
                    }
                }
                if (userForImport.getOrganizationName() != null
                        && !StringUtils.isEmpty(userForImport.getOrganizationName())) {
                    Long organizationId = Lookup
                            .getOrganizationIdByNameWithTenantId(userForImport.getOrganizationName(), currentTenantId);
                    Long orgTenantId = Lookup.getTenantIdByOrganizationId(organizationId);
                    if (organizationId == null) {
                        logErrorMessage(userForImport, beanWriter, lineRowNum, "Organization not found");
                        continue;
                    } else if (groupId != null && orgTenantId != null && (!currentTenantId.equals(orgTenantId)
                            || !userDal.checkTenantIdInOrgAndGroup(organizationId, groupId))) {
                        logErrorMessage(userForImport, beanWriter, lineRowNum,
                                "Organization Not valid for Current Tenant");
                        continue;
                    } else {
                        userForImport.getUser().setOrganizationId(organizationId);
                    }
                } else { // Organization Name is required for new as well as existing users
                    logErrorMessage(userForImport, beanWriter, lineRowNum, "Organization name is required");
                    continue;
                }

                try {
                    User persistedUser = null;
                    if(existingUser != null) {
                        userForImport.getUser().setUserId(existingUser.getUserId());
                        persistedUser = userDal.update(userForImport.getUser(), loggedInUserId, null).getUser();
                    }else {
                        persistedUser = userDal.create(
                                userForImport.getUser(), loggedInUserId, "User", "Import");
                    }
                    if (persistedUser.getIdentities() != null
                            && !persistedUser.getIdentities().isEmpty()) {
                        userDal.evictSecondLevelCacheById(persistedUser
                                .getUserId());
                    }
                    Lookup.updateUserLookupTable(persistedUser);
                    log.info(
                            "importFromCSV; lineNo:rowNo={}; persistedUser={};",
                            lineRowNum, persistedUser);
                    UserImportDecorator userImportDecorator = new UserImportDecorator(
                            persistedUser);
                    userImportDecorator.setIsSuccess(true);
                    if (userForImport.getUser().getChangePassword() != null) {
                        userImportDecorator.setPassword("*****");
                    }
                    userImportDecorator.setGroupName(userForImport
                            .getGroupName());
                    userImportDecorator.setOrganizationName(userForImport
                            .getOrganizationName());

                    // MakerChecker
                    if (persistedUser.getMakerCheckerId() != null && persistedUser.getIsStatus() == 0
                            && persistedUser.getUserName().contains(MakerCheckerDalJpa.MKR_SEPARATOR)) {
                        String username = MakerCheckerDalJpa.extractUserNameFromMKR(persistedUser.getUserName());
                        userImportDecorator.getUser().setUserName(username);
                        userImportDecorator
                                .setImportMessage(username + " has been added temporarily. Pending Approval.");
                    }
                    beanWriter.write(userImportDecorator);
                } catch (Exception e) {
                    log.error(
                            "importFromCSV; lineNo:rowNo={}; failedUser={}; Exception={};",
                            lineRowNum, userForImport.getUser(), e);
                    userForImport.setIsSuccess(false);
                    if (userForImport.getUser().getChangePassword() != null) {
                        userForImport.setPassword("*****");
                    }
                    userForImport.setImportMessage(e.getMessage());
                    beanWriter.write(userForImport);
                }

            }

        } finally {
            if (beanReader != null) {
                beanReader.close();
            }
            if (beanWriter != null) {
                beanWriter.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void logErrorMessage(UserImportDecorator userForImport, ICsvDozerBeanWriter beanWriter,String lineRowNum, String errorMessage) throws IOException {
        userForImport.setIsSuccess(false);
        if (userForImport.getUser().getChangePassword() != null) {
            userForImport.setPassword("*****");
        }
        userForImport.setImportMessage(errorMessage);
        beanWriter.write(userForImport);
        log.error(
                "importFromCSV; lineNo:rowNo={}; failedUser={}; Exception={};",
                lineRowNum, userForImport.getUser(),
                errorMessage);
    }



    //RBAC-1892 Start
    @Override
    public void exportToCSV(List<User> userList,Map<Integer, Set<String>> getRoleListByGroup, Writer writer)
            throws Exception {
        ICsvDozerBeanWriter beanWriter = null;
        try {
            beanWriter = new CsvDozerBeanWriter(writer, CsvPreference.STANDARD_PREFERENCE, dozerBeanMapperExport);

            String[] outpuHeaders = new String[exportHeaderList.size()];
            exportHeaderList.toArray(outpuHeaders);
            beanWriter.writeHeader(outpuHeaders);

            for (User user : userList) {
                try {
                    UserImportDecorator userImportDecorator = new UserImportDecorator(user);
                    userImportDecorator.setOrganizationName(Lookup.getOrganizationNameById(user.getOrganizationId()));
                    if (user.getGroupId() != null && user.getGroupId() > 0) {
                        if (getRoleListByGroup != null && getRoleListByGroup.containsKey(user.getGroupId()))
                            userImportDecorator
                                    .setRoles(getRoleListByGroup.get(user.getGroupId()).stream().collect(Collectors.joining(",")));
                        else
                            userImportDecorator.setRoles(null);

                        userImportDecorator.setGroupName(Lookup.getGroupName(user.getGroupId()));
                    } else {
                        userImportDecorator.setGroupName(null);
                        userImportDecorator.setRoles(null);
                    }
                    beanWriter.write(userImportDecorator);
                } catch (Exception e) {
                    log.error("exportToCSV; lineNo: failedUser={}; Exception={};", user, e);
                }

            }

        } finally {
            if (beanWriter != null) {
                beanWriter.close();
            }
        }
    }
    //RBAC-1892 End
}
