package com.esq.rbac.service.group.json;

import java.util.Set;
import java.util.TreeSet;

public class UsersInGroupJson implements Comparable<UsersInGroupJson>{
    private Integer groupId;
    private String groupName;
    private Set<UserJson> users = new TreeSet<UserJson>();

    public UsersInGroupJson() {

    }

    public UsersInGroupJson(Integer groupId, String groupName) {
        this.groupId = groupId;
        this.groupName = groupName;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public Set<UserJson> getUsers() {
        return users;
    }

    public void addUser(Integer userId, String userName) {
        UserJson userJson = new UserJson();
        userJson.setUserId(userId);
        userJson.setUserName(userName);
        users.add(userJson);
    }

    @Override
    public int compareTo(UsersInGroupJson o) {
        if (o instanceof UsersInGroupJson) {
            return o.groupName.compareToIgnoreCase(groupName);
        }
        return 0;
    }

    @Override
    public int hashCode() {
        return groupId.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof UsersInGroupJson) {
            return this.groupId.equals(((UsersInGroupJson) o).getGroupId());
        }
        return false;
    }

    public static class UserJson implements Comparable<UserJson> {
        private Integer userId;
        private String userName;

        public Integer getUserId() {
            return userId;
        }

        public void setUserId(Integer userId) {
            this.userId = userId;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public UserJson() {
            super();
        }

        public UserJson(Integer userId, String userName) {
            super();
            this.userId = userId;
            this.userName = userName;
        }

        @Override
        public int compareTo(UserJson o) {
            if (o instanceof UserJson) {
                return o.userName.compareToIgnoreCase(userName);
            }
            return 0;
        }

        @Override
        public int hashCode() {
            return userId.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof UserJson) {
                return this.userId.equals(((UserJson) o).getUserId());
            }
            return false;
        }
    }
}
