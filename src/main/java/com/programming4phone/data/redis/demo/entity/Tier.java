package com.programming4phone.data.redis.demo.entity;

public class Tier {

	private String speed;
	private Long threshhold;
	
	public String getSpeed() {
		return speed;
	}
	public Tier setSpeed(String speed) {
		this.speed = speed;
		return this;
	}
	public Long getThreshhold() {
		return threshhold;
	}
	public Tier setThreshhold(Long threshhold) {
		this.threshhold = threshhold;
		return this;
	}
	
	@Override
	public String toString() {
		return "Tier [speed=" + speed + ", threshhold=" + threshhold + "]";
	}
	
}
