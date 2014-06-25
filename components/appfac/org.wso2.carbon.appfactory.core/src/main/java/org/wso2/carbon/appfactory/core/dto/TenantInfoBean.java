/**
 * 
 */
package org.wso2.carbon.appfactory.core.dto;
import java.util.Calendar;

public class TenantInfoBean {
	
	   private String admin; //admin's user name
	   private String firstname;
	   private String lastname;
	   private String adminPassword;
	   private String tenantDomain;
	   private int tenantId;
	   private String email;
	   private boolean active;
	   private String successKey;
	   Calendar createdDate;
	   private String originatedService;
	   private String usagePlan;
	
	   public String getFirstname() {
	       return firstname;
	   }
	
	   public void setFirstname(String firstname) {
	       this.firstname = firstname;
	   }
	
	   public String getLastname() {
	       return lastname;
	   }
	
	   public void setLastname(String lastname) {
	       this.lastname = lastname;
	   }
	
	   public String getEmail() {
	
	       return email;
	   }
	
	   public void setEmail(String email) {
	       this.email = email;
	   }
	
	   public int getTenantId() {
	       return tenantId;
	   }
	
	   public void setTenantId(int tenantId) {
	       this.tenantId = tenantId;
	   }
	
	   public String getTenantDomain() {
	       return tenantDomain;
	   }
	
	   public void setTenantDomain(String tenantDomain) {
	       this.tenantDomain = tenantDomain;
	   }
	
	   public String getAdmin() {
	       return admin;
	   }
	
	   public void setAdmin(String admin) {
	       this.admin = admin;
	   }
	
	   public String getAdminPassword() {
	       return adminPassword;
	   }
	
	   public void setAdminPassword(String adminPassword) {
	       this.adminPassword = adminPassword;
	   }
	
	   public boolean isActive() {
	       return active;
	   }
	
	   public void setActive(boolean active) {
	       this.active = active;
	   }
	
	   public String getSuccessKey() {
	       return successKey;
	   }
	
	   public void setSuccessKey(String successKey) {
	       this.successKey = successKey;
	   }
	
	   public Calendar getCreatedDate() {
	       return createdDate;
	   }
	
	   public void setCreatedDate(Calendar createdDate) {
	       this.createdDate = createdDate;
	   }
	
	   public String getOriginatedService() {
	       return originatedService;
	   }
	
	   public void setOriginatedService(String originatedService) {
	       this.originatedService = originatedService;
	   }
	
	   public String getUsagePlan() {
	       return usagePlan;
	   }
	
	   public void setUsagePlan(String usagePlan) {
	       this.usagePlan = usagePlan;
	   }

}
