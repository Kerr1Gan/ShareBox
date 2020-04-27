package com.sendby.entity;

import com.google.gson.annotations.SerializedName;

public class LoginEntity {


    /**
     * email : supernini1919id@gmail.com
     * name : Ni Ni
     * picture : https://lh4.googleusercontent.com/-QFIS9k2ZWG4/AAAAAAAAAAI/AAAAAAAAAAA/AAKWJJOOJhjKA8I03UY7mvgEsqqO0E0rjA/s96-c/photo.jpg
     * locale : zh-CN
     * familyName : Ni
     * givenName : Ni
     * emailVerified : true
     * androidId : 171afc03864409bb
     * appPackageName : com.projectwinter.sendby
     * apkVersion : 1.1.21
     * createTime : null
     * vipExpirationTime : null
     */
    @SerializedName("email")
    private String email;
    @SerializedName("name")
    private String name;
    @SerializedName("picture")
    private String picture;
    @SerializedName("locale")
    private String locale;
    @SerializedName("familyName")
    private String familyName;
    @SerializedName("givenName")
    private String givenName;
    @SerializedName("emailVerified")
    private boolean emailVerified;
    @SerializedName("androidId")
    private String androidId;
    @SerializedName("appPackageName")
    private String appPackageName;
    @SerializedName("apkVersion")
    private String apkVersion;
    @SerializedName("createTime")
    private Long createTime;
    @SerializedName("vipExpirationTime")
    private Long vipExpirationTime;

    private static final String STUB = new LoginEntity().getClass().getSimpleName();

    public LoginEntity() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public String getApkVersion() {
        return apkVersion;
    }

    public void setApkVersion(String apkVersion) {
        this.apkVersion = apkVersion;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getVipExpirationTime() {
        return vipExpirationTime;
    }

    public void setVipExpirationTime(Long vipExpirationTime) {
        this.vipExpirationTime = vipExpirationTime;
    }
}
