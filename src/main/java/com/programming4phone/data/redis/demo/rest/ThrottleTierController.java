package com.programming4phone.data.redis.demo.rest;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.programming4phone.data.redis.demo.dao.TierDao;
import com.programming4phone.data.redis.demo.dao.UsageDao;
import com.programming4phone.data.redis.demo.entity.CustomerUsage;
import com.programming4phone.data.redis.demo.entity.Tier;
import com.programming4phone.data.redis.demo.error.InvalidTierSpeedException;
import com.programming4phone.data.redis.demo.error.NoTiersFoundException;

@RestController
@RequestMapping("/throttle/tier")
public class ThrottleTierController {
	
	@Autowired
	private TierDao tierDao;
	
	@Autowired
	private UsageDao usageDao;
	
	/**
	 * Retrieve the total data usage amount for a specific account including the tier usage speed.
	 * If the account does not exist in the Redis database, the total usage amount retruned will
	 * be zero.
	 * @param accountNumber String
	 * @return <b>CustomerUsage</b>
	 */
	@RequestMapping(value="/{accountNumber}", method=RequestMethod.GET, produces="application/json")
	public CustomerUsage getUsageAmount(@PathVariable String accountNumber) {
		Long totalAmountUsed = usageDao.getCurrentAmountUsed(accountNumber);
		Tier usageTier = tierDao.getCurrentUsageTier(totalAmountUsed);
		return new CustomerUsage().setAccountNumber(accountNumber).setTotalUsage(totalAmountUsed).setSpeed(usageTier.getSpeed());
	}
	
	/**
	 * Retrieve all Tier objects currently stored in the Redis database. If none exist, then the
	 * data layer will throw a NoTiersFoundException which will ultimately result in an HTTP status 404
	 * (NOT_FOUND) being by the web service.
	 * @return <b>List</b> of Tier objects
	 */
	@RequestMapping(method=RequestMethod.GET, produces="application/json")
	public List<Tier> getAllTiers() {
		List<Tier> usageTiers = tierDao.getAllTiers();
		return usageTiers;
	}
	
	/**
	 * Add a Tier object to the Redis database. If the Tier object contains an invalid speed
	 * (i.e. one not contained in UsageTierEnum) the data layer will throw an InvalidTierSpeedException 
	 * which will ultimately result in an HTTP status 400 (BAD_REQUEST) being by the web service.
	 * @param tier Tier
	 * @see com.programming4phone.data.redis.demo.entity.UsageTierEnum
	 */
	@RequestMapping(method=RequestMethod.PUT, consumes="application/json")
	@ResponseStatus(HttpStatus.CREATED)
	public void addTier(@RequestBody Tier tier) {
		tierDao.addTier(tier);
	}
	
	/**
	 * Remove a Tier object from the Redis database. If the provided speed is invalid
	 * (i.e. one not contained in UsageTierEnum) the data layer will throw an InvalidTierSpeedException 
	 * which will ultimately result in an HTTP status 400 (BAD_REQUEST) being by the web service.
	 * @param speed String
	 * @see com.programming4phone.data.redis.demo.entity.UsageTierEnum
	 */
	@RequestMapping(value="/delete/{speed}", method=RequestMethod.DELETE)
	public void deleteTier(@PathVariable String speed) {
		tierDao.deleteTier(new Tier().setSpeed(speed));
	}
	
	@ExceptionHandler(InvalidTierSpeedException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public void invalidTierSpeed() {
	}
	
	@ExceptionHandler(NoTiersFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public void noTiersFound() {
	}
}
