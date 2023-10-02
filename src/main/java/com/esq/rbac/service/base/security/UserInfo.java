package com.esq.rbac.service.base.security;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

@XmlRootElement
public class UserInfo implements UserDetails {

    private String username;
    private String displayName;
    private String description;
    private Set<String> permissions = new HashSet<String>();
    private List<GrantedAuthority> authorities = null;
    private Map<String, String> scopeMap = new HashMap<String, String>();
    private Date lastSuccessfulLoginTime;

    @Override
    public String getUsername() {
        return username;
    }

    @XmlAttribute(name = "userName")
    @JsonProperty(value = "userName")
    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    @XmlElement(name = "permission")
    @JsonProperty(value = "permission")
    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }

    public void addPermissions(List<String> permissions) {
        this.permissions.addAll(permissions);
        this.authorities = null;
    }

    public void removePermissions(List<String> permissions) {
        this.permissions.removeAll(permissions);
        this.authorities = null;
    }

    public void clearPermissions() {
        this.permissions.clear();
        this.authorities = null;
    }

    public boolean hasPermission(String permission) {
        return permissions.contains(permission);
    }

    public void setScope(String permission, String scopeDef) {
        scopeMap.put(permission, scopeDef);
    }

    public String getScope(String permission) {
        return scopeMap.containsKey(permission) ? scopeMap.get(permission) : "";
    }

    public Date getLastSuccessfulLoginTime() {
        return lastSuccessfulLoginTime;
    }

    public void setLastSuccessfulLoginTime(Date lastSuccessfulLoginTime) {
        this.lastSuccessfulLoginTime = lastSuccessfulLoginTime;
    }

    public void claerScopes() {
        scopeMap.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("userName=").append(username);
        sb.append("; displayName=").append(displayName);
        sb.append("; description=").append(description);
        for (String text : permissions) {
            sb.append("; ").append(text);
        }
        return sb.toString();
    }

    @Override
    @XmlTransient
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            authorities = new ArrayList<GrantedAuthority>(permissions.size());
            for (String permission : permissions) {
                authorities.add(new SimpleGrantedAuthority(permission));
            }
        }
        return authorities;
    }

    @Override
    @XmlTransient
    @JsonIgnore
    public String getPassword() {
        return null;
    }

    @Override
    @XmlTransient
    @JsonIgnore
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    @XmlTransient
    @JsonIgnore
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    @XmlTransient
    @JsonIgnore
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    @XmlTransient
    @JsonIgnore
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final UserInfo other = (UserInfo) obj;
        return this.username.equals(other.username);
    }

    @Override
    public int hashCode() {
        return this.username.hashCode();
    }
}