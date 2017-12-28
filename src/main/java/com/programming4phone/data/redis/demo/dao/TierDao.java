package com.programming4phone.data.redis.demo.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

import com.programming4phone.data.redis.demo.entity.Tier;
import com.programming4phone.data.redis.demo.entity.UsageTierEnum;
import com.programming4phone.data.redis.demo.error.InvalidTierSpeedException;
import com.programming4phone.data.redis.demo.error.NoTiersFoundException;

@Component
public class TierDao {
	
	private static final String TIERS = "TIERS";
	
	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	
	/**
	 * Add a usage tier. Each tier contains a data usage threshold and a bandwidth speed.
	 * Tiers are stored in Redis within a Sorted Set. The data usage threshold is represented
	 * by the score for each element within the set.
	 * @param tier Tier
	 */
	public void addTier(Tier tier) {
		validateTierSpeed(tier.getSpeed());
		TypedTuple<String> tierTuple = new DefaultTypedTuple<String>(tier.getSpeed(), tier.getThreshhold().doubleValue());
		Set<TypedTuple<String>> tiers = new HashSet<TypedTuple<String>>();
		tiers.add(tierTuple);
		stringRedisTemplate.boundZSetOps(TIERS).add(tiers);
	}
	
	/**
	 * Remove a tier. The tier is deleted from the Redis Sorted Set and will no longer be used
	 * to determine bandwidth speed.
	 * @param tier Tier
	 */
	public void deleteTier(Tier tier) {
		validateTierSpeed(tier.getSpeed());
		stringRedisTemplate.boundZSetOps(TIERS).remove(tier.getSpeed());
	}
	
	/**
	 * All tiers stored in Redis are returned, ordered by data usage threshold (which is the score for each
	 * element with the Sorted Set).
	 * @return <b>List of Tier objects</b>
	 */
	public List<Tier> getAllTiers() {
		// return all tiers from the Redis Sorted Set
		Set<TypedTuple<String>> tiers = 
				stringRedisTemplate
					.boundZSetOps(TIERS)
					.rangeWithScores(0, -1);
		// convert internal Redis representation to Tier objects
		List<Tier> allTiers = 
				tiers.stream()
					.map(t -> new Tier().setThreshhold(t.getScore().longValue()).setSpeed(t.getValue()))
					.collect(Collectors.toList());
		if(allTiers.isEmpty()) throw new NoTiersFoundException();
		return allTiers;
	}
	
	/**
	 * Return the appropriate tier based on current usage. 
	 * As data usage increases for a account holder, bandwidth speed is throttled downwards as the usage amount crosses each threshold.
	 * @param currentUsage Long
	 * @return Tier
	 */
	public Tier getCurrentUsageTier(Long currentUsage) {
		List<Tier> allTiers = getAllTiers();
		Optional<Tier> currentUsageTier = 
				allTiers.stream()
					.filter(t->currentUsage.compareTo(t.getThreshhold())>0)
					.max((t1,t2)->t1.getThreshhold().compareTo(t2.getThreshhold()));
		return currentUsageTier.orElse(new Tier().setThreshhold(0L).setSpeed("UNKNOWN"));
	}
	
	/**
	 * Validates the tier speed. If an invalid speed is provided an InvalidTierSpeedException is
	 * thrown, ultimately resulting in an HTTP status code 400 (BAD_REQUEST).
	 * @param speed String
	 * @throws com.programming4phone.data.redis.demo.error.InvalidTierSpeedException
	 * @see com.programming4phone.data.redis.demo.entity.UsageTierEnum
	 */
	private void validateTierSpeed(String speed) {
		try {
			UsageTierEnum.valueOf(speed);
		}
		catch(IllegalArgumentException iae) {
			throw new InvalidTierSpeedException();
		}
		
	}

}
