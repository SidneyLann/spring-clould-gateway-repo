package com.blockchain.gateway.common;

import java.security.Principal;
import java.util.Date;
import java.util.Map;
import java.util.Set;

public class SecurityUser implements Principal {
  private String id;
  private String loginName;
  private String nickName;
  private String avatar;
  private String wxNickName;
  private String wxAvatar;
  private String password;
  private String orgId;
  private String orgName;
  private String orgType;
  private String receiptOrgId;
  private String receiptMemId;
  private String token;
  private Date issuedAt;
  private Date expiresAt;
  private Set<String> authorities;
  private Map<String,String> storeNames;
  private boolean enabled = true;

  @Override
  public String getName() {
    return loginName;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getLoginName() {
    return loginName;
  }

  public void setLoginName(String loginName) {
    this.loginName = loginName;
  }

  public String getNickName() {
    return nickName;
  }

  public void setNickName(String nickName) {
    this.nickName = nickName;
  }

  public String getAvatar() {
    return avatar;
  }

  public void setAvatar(String avatar) {
    this.avatar = avatar;
  }

  public String getWxNickName() {
    return wxNickName;
  }

  public void setWxNickName(String wxNickName) {
    this.wxNickName = wxNickName;
  }

  public String getWxAvatar() {
    return wxAvatar;
  }

  public void setWxAvatar(String wxAvatar) {
    this.wxAvatar = wxAvatar;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getOrgId() {
    return orgId;
  }

  public void setOrgId(String orgId) {
    this.orgId = orgId;
  }

  public String getOrgName() {
    return orgName;
  }

  public void setOrgName(String orgName) {
    this.orgName = orgName;
  }

  public String getOrgType() {
    return orgType;
  }

  public void setOrgType(String orgType) {
    this.orgType = orgType;
  }

  public String getReceiptOrgId() {
    return receiptOrgId;
  }

  public void setReceiptOrgId(String receiptOrgId) {
    this.receiptOrgId = receiptOrgId;
  }

  public String getReceiptMemId() {
    return receiptMemId;
  }

  public void setReceiptMemId(String receiptMemId) {
    this.receiptMemId = receiptMemId;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Date getIssuedAt() {
    return issuedAt;
  }

  public void setIssuedAt(Date issuedAt) {
    this.issuedAt = issuedAt;
  }

  public Date getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(Date expiresAt) {
    this.expiresAt = expiresAt;
  }

  public Set<String> getAuthorities() {
    return authorities;
  }

  public void setAuthorities(Set<String> authorities) {
    this.authorities = authorities;
  }

  public Map<String, String> getStoreNames() {
    return storeNames;
  }

  public void setStoreNames(Map<String, String> storeNames) {
    this.storeNames = storeNames;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }
  
  
}
