package com.programming4phone.data.redis.demo.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.programming4phone.data.redis.demo.dao.UsageDao;
import com.programming4phone.data.redis.demo.entity.CustomerUsage;
import com.programming4phone.data.redis.demo.error.InvalidUsageAmountException;

@CrossOrigin
@RestController
@RequestMapping("/throttle/usage")
public class ThrottleUsageController {
	
	@Autowired
	private UsageDao usageDao;
	
	/**
	 * Increase the data usage amount for an account.
	 * @param customerUsage CustomerUsage containing the amount to increase by
	 * @return <b>CustomerUsage</b> containing the updated total data usage
	 */
	@RequestMapping(value="/increase",method=RequestMethod.POST, consumes="application/json", produces="application/json")
	public CustomerUsage increaseUsageAmount(@RequestBody CustomerUsage customerUsage) {
		Long totalAmountUsed = usageDao.increaseAmountUsed(customerUsage.getAccountNumber(), customerUsage.getCurrentUsage());
		customerUsage.setTotalUsage(totalAmountUsed);
		return customerUsage;
	}
	
	/**
	 * Decrease the data usage amount for an account. If the total data usage becomes 
	 * negative (less than zero), the amount is reset to zero.
	 * @param customerUsage CustomerUsage containing the amount to decrease by
	 * @return <b>CustomerUsage</b> containing the updated total data usage
	 */
	@RequestMapping(value="/decrease",method=RequestMethod.POST, consumes="application/json", produces="application/json")
	public CustomerUsage decreaseUsageAmount(@RequestBody CustomerUsage customerUsage) {
		Long totalAmountUsed = usageDao.decreaseAmountUsed(customerUsage.getAccountNumber(), customerUsage.getCurrentUsage());
		customerUsage.setTotalUsage(totalAmountUsed);
		return customerUsage;
	}

	/**
	 * Removes the account from the Redis database.
	 * @param accountNumber String
	 */
	@RequestMapping(value="/remove/{accountNumber}",method=RequestMethod.DELETE)
	public void removeAccount(@PathVariable String accountNumber) {
		usageDao.removeAccount(accountNumber);
	}
	
	/**
	 * Reset the data usage count for an account to zero.
	 * @param accountNumber String
	 */
	@RequestMapping(value="/reset/{accountNumber}",method=RequestMethod.DELETE)
	public void resetAccount(@PathVariable String accountNumber) {
		usageDao.resetAccount(accountNumber);
	}

	/**
	 * Get the total amount of data used for a specific account.
	 * @param accountNumber String
	 * @return <b>CustomerUsage</b> containing the total data usage
	 */
	@RequestMapping(value="/{accountNumber}", method=RequestMethod.GET, produces="application/json")
	public CustomerUsage getCurrentAmountUsed(@PathVariable String accountNumber) {
		Long totalAmountUsed = usageDao.getCurrentAmountUsed(accountNumber);
		return new CustomerUsage().setAccountNumber(accountNumber).setTotalUsage(totalAmountUsed);
	}
	
	@ExceptionHandler(InvalidUsageAmountException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public void invalidUsageAmount() {
	}
}
