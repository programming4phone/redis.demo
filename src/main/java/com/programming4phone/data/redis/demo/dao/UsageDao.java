package com.programming4phone.data.redis.demo.dao;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.programming4phone.data.redis.demo.error.InvalidUsageAmountException;


@Component
public class UsageDao {

	private static final String ACCOUNT_PREFIX = "ACCOUNT:";
	private static final Long LONG_ZERO = Long.valueOf(0);
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	@Value("${tier.expiry.seconds}")
	private String tierExpirySeconds;
	
	/**
	 * Removes the account key from the Redis database.
	 * @param accountNumber String
	 */
	public void removeAccount(String accountNumber) {
		String accountKey = ACCOUNT_PREFIX+accountNumber;
		stringRedisTemplate.delete(accountKey);
	}
	
	/**
	 * Reset the account. This action resets the data usage count to zero and
	 * resets the Redis account key expiration. Keys expire based on 
	 * the <i>tier.expiry.seconds</i> property set in the application.properties file.
	 * @param accountNumber String
	 */
	public void resetAccount(String accountNumber) {
		String accountKey = ACCOUNT_PREFIX+accountNumber;
		stringRedisTemplate.opsForValue().set(accountKey, "0");
		stringRedisTemplate.expire(accountKey, Long.parseLong(tierExpirySeconds), TimeUnit.SECONDS);
	}
	
	/**
	 * Increase the total amount of data usage for an account. If the account does not exist in the 
	 * Redis database then it is created and the account key expiration is set. Keys expire based on 
	 * the <i>tier.expiry.seconds</i> property set in the application.properties file.
	 * @param accountNumber String
	 * @param usageAmount Long containing amount to increase by
	 * @return <b>Long</b> containing new total amount of usage
	 */
	public Long increaseAmountUsed(String accountNumber, Long usageAmount) {
		String accountKey = ACCOUNT_PREFIX+accountNumber;
		Long newUsageAmount = stringRedisTemplate
								.opsForValue()
								.increment(accountKey, validateUsageAmount(usageAmount));
		/*
		 * If new usage amount EQ the amount to increase then it must be a new account
		 * so reset the key expiration accordingly.
		 */
		if(newUsageAmount.compareTo(usageAmount)==0) {
			stringRedisTemplate
				.expire(accountKey, Long.parseLong(tierExpirySeconds), TimeUnit.SECONDS);
		}
		return newUsageAmount;
	}
	
	/**
	 * Decrease the total amount of data usage for an account. If the total data usage becomes 
	 * negative (less than zero), the amount is reset to zero and the account key expiration is reset. 
	 * Keys expire based on the <i>tier.expiry.seconds</i> property set in the application.properties file.
	 * @param accountNumber
	 * @param usageAmount
	 * @return <b>Long</b> containing new total amount of usage
	 */
	public Long decreaseAmountUsed(String accountNumber, Long usageAmount) {
		String accountKey = ACCOUNT_PREFIX+accountNumber;
		Long newUsageAmount = 	stringRedisTemplate
								.opsForValue()
								.increment(accountKey, validateUsageAmount(usageAmount)*-1L);
		if(newUsageAmount.compareTo(LONG_ZERO)<=0) {
			stringRedisTemplate
				.opsForValue()
				.set(accountKey, "0");
			stringRedisTemplate
				.expire(accountKey, Long.parseLong(tierExpirySeconds), TimeUnit.SECONDS);
			newUsageAmount = LONG_ZERO;
		}
		return newUsageAmount;
	}
	
	/**
	 * Get the total amount of data used for a specific account.
	 * @param accountNumber
	 * @return <b>Long</b> containing total amount of usage 
	 */
	public Long getCurrentAmountUsed(String accountNumber) {
		return Long.valueOf(
					Optional.ofNullable(
						stringRedisTemplate
						.opsForValue()
						.get(ACCOUNT_PREFIX+accountNumber)).orElse("0"));
	}
	
	/**
	 * Validates the usage. If a null is provided then the usage amount defaults to zero.
	 * If the usage amount is negative (less than zero) an InvalidUsageAmountException is
	 * thrown, ultimately resulting in an HTTP status code 400 (BAD_REQUEST).
	 * @param iUsageAmount Long
	 * @return <b>Long</b> containing a valid usage amount
	 * @throws com.programming4phone.data.redis.demo.error.InvalidUsageAmountException
	 */
	private Long validateUsageAmount(Long iUsageAmount) {
		Long usageAmount = Optional.ofNullable(iUsageAmount).orElse(LONG_ZERO);
		if(usageAmount.compareTo(LONG_ZERO)<0) throw new InvalidUsageAmountException();
		return usageAmount;
	}
}
