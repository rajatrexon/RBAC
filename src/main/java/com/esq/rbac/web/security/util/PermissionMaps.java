package com.esq.rbac.web.security.util;

import java.util.*;

public class PermissionMaps {

    public static Map<String, List<String>> getPermissionsMaps(String permissionKey){
        String[] getTargetName = permissionKey.split("\\.");
        System.out.println("--------------------------"+getTargetName[0]);
        Map<String, List<String>> permissionsMap = new HashMap<>();
        List<String> permissions = new ArrayList<>();
        if(getTargetName[0].equalsIgnoreCase("Configuration")){
            permissions.add("Update");
            permissions.add("View");
        }else if(getTargetName[0].equalsIgnoreCase("Group")){
            permissions.add("Create");
            permissions.add("Delete");
            permissions.add("MarkIsTemplate");
            permissions.add("MenuView");
            permissions.add("SelfUpdate");
            permissions.add("Update");
            permissions.add("View");
            permissions.add("SelfUpdate");
        }else if(getTargetName[0].equalsIgnoreCase("User")){
            permissions.add("AssignGroup");
            permissions.add("Create");
            permissions.add("CreatePassword");
            permissions.add("Delete");
            permissions.add("Export");
            permissions.add("Import");
            permissions.add("MenuView");
            permissions.add("ProfileManagement");
            permissions.add("View");
            permissions.add("Restrictions");
            permissions.add("SelfUpdate");
            permissions.add("StatusManagement");
            permissions.add("Update");
            permissions.add("Variables");
    }else if(getTargetName[0].equalsIgnoreCase("Role")) {
            permissions.add("Create");
            permissions.add("Delete");
            permissions.add("Update");
            permissions.add("View");
        }
        permissionsMap.put(getTargetName[0], permissions);
        return permissionsMap;
    }

    public static Set<String> getListOfPermissions() {
        Set<String> userPermissions = new TreeSet<String>();

        // Add each permission to the TreeSet
        userPermissions.add("Application.Create");
        userPermissions.add("Application.Delete");
        userPermissions.add("Application.ManageLicense");
        userPermissions.add("Application.MenuView");
        userPermissions.add("Application.Update");
        userPermissions.add("Application.View");
        userPermissions.add("Calendar.Create");
        userPermissions.add("Calendar.Delete");
        userPermissions.add("Calendar.Update");
        userPermissions.add("Calendar.View");
        userPermissions.add("Configuration.Update");
        userPermissions.add("Configuration.View");
        userPermissions.add("DistributionGroup.Create");
        userPermissions.add("DistributionGroup.Delete");
        userPermissions.add("DistributionGroup.MenuView");
        userPermissions.add("DistributionGroup.Update");
        userPermissions.add("DistributionGroup.UserMap");
        userPermissions.add("DistributionGroup.View");
        userPermissions.add("Group.Allow.Undefined.Scopes");
        userPermissions.add("Group.Attributes.Create");
        userPermissions.add("Group.Attributes.Update");
        userPermissions.add("Group.Create");
        userPermissions.add("Group.Delete");
        userPermissions.add("Group.MarkIsTemplate");
        userPermissions.add("Group.MenuView");
        userPermissions.add("Group.SelfUpdate");
        userPermissions.add("Group.Update");
        userPermissions.add("Group.View");
        userPermissions.add("Organization.Attributes.Create");
        userPermissions.add("Organization.Attributes.Delete");
        userPermissions.add("Organization.Attributes.Update");
        userPermissions.add("Organization.Attributes.View");
        userPermissions.add("Organization.Create");
        userPermissions.add("Organization.Delete");
        userPermissions.add("Organization.MenuView");
        userPermissions.add("Organization.Update");
        userPermissions.add("Organization.View");
        userPermissions.add("Report.Access.Matrix.Group-wise");
        userPermissions.add("Report.Access.Matrix.User-wise");
        userPermissions.add("Report.Audit.Log");
        userPermissions.add("Report.Global.User.Search");
        userPermissions.add("Report.LoginLog");
        userPermissions.add("Report.Scope.Details");
        userPermissions.add("Report.User.Activity");
        userPermissions.add("Role.Create");
        userPermissions.add("Role.Delete");
        userPermissions.add("Role.Update");
        userPermissions.add("Role.View");
        userPermissions.add("ScheduleMaintenance.Create");
        userPermissions.add("ScheduleMaintenance.Delete");
        userPermissions.add("ScheduleMaintenance.Update");
        userPermissions.add("ScheduleMaintenance.View");
        userPermissions.add("Scope.Create");
        userPermissions.add("Scope.Delete");
        userPermissions.add("Scope.Update");
        userPermissions.add("Scope.View");
        userPermissions.add("Tenant.Create");
        userPermissions.add("Tenant.Delete");
        userPermissions.add("Tenant.MenuView");
        userPermissions.add("Tenant.Update");
        userPermissions.add("Tenant.View");
        userPermissions.add("User.Approve");
        userPermissions.add("User.AssignGroup");
        userPermissions.add("User.Attributes.Create");
        userPermissions.add("User.Attributes.Update");
        userPermissions.add("User.Create");
        userPermissions.add("User.CreatePassword");
        userPermissions.add("User.GeneratePassword");
        userPermissions.add("User.Delete");
        userPermissions.add("User.Export");
        userPermissions.add("User.Import");
        userPermissions.add("User.MenuView");
        userPermissions.add("User.ProfileManagement");
        userPermissions.add("User.Restrictions");
        userPermissions.add("User.SelfUpdate");
        userPermissions.add("User.StatusManagement");
        userPermissions.add("User.Update");
        userPermissions.add("User.Variables");
        userPermissions.add("User.View");
        userPermissions.add("UserSession.Terminate");
        userPermissions.add("UserSession.View");
        userPermissions.add("UserSync.Create");
        userPermissions.add("UserSync.Delete");
        userPermissions.add("UserSync.View");
        return userPermissions;
    }
}
