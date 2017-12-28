package com.programming4phone.data.redis.demo.entity;

public class CustomerUsage {

	private String accountNumber;
	private Long currentUsage;
	private Long totalUsage;
	private String speed;
	
	public String getAccountNumber() {
		return accountNumber;
	}
	public CustomerUsage setAccountNumber(String accountNumber) {
		this.accountNumber = accountNumber;
		return this;
	}
	public Long getCurrentUsage() {
		return currentUsage;
	}
	public CustomerUsage setCurrentUsage(Long currentUsage) {
		this.currentUsage = currentUsage;
		return this;
	}
	public Long getTotalUsage() {
		return totalUsage;
	}
	public CustomerUsage setTotalUsage(Long totalUsage) {
		this.totalUsage = totalUsage;
		return this;
	}
	public String getSpeed() {
		return speed;
	}
	public CustomerUsage setSpeed(String speed) {
		this.speed = speed;
		return this;
	}
	
	@Override
	public String toString() {
		return "CustomerUsage [accountNumber=" + accountNumber + ", currentUsage=" + currentUsage + ", totalUsage="
				+ totalUsage + ", speed=" + speed + "]";
	}
	
}
