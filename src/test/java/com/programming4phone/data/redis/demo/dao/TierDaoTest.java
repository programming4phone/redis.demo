package com.programming4phone.data.redis.demo.dao;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.programming4phone.data.redis.demo.entity.Tier;
import com.programming4phone.data.redis.demo.entity.UsageTierEnum;
import com.programming4phone.data.redis.demo.error.InvalidTierSpeedException;
import com.programming4phone.data.redis.demo.error.NoTiersFoundException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TierDaoTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private TierDao tierDao;
	
	@Test
	public void testCreateTiers() {
		List<Tier> tiers;
		
		createTestTiers();
		tiers = tierDao.getAllTiers();
		assertNotNull(tiers);
		assertThat(tiers.size(), equalTo(3));
		tiers.stream().forEach(t->logger.info("TIER: " + t.getSpeed() + " THRESHHOLD: " + Double.toString(t.getThreshhold())));
		
		deleteTestTiers();
		
		try {
			tiers = tierDao.getAllTiers();
			fail("Expected an NoTiersFoundException to be thrown");
		}
		catch(NoTiersFoundException ntfe) {
			assertNotNull(ntfe);
		}
	}
	
	@Test
	public void testTiers() {
		createTestTiers();
		Tier usageTier = tierDao.getCurrentUsageTier(10L);
		assertNotNull(usageTier);
		assertThat(usageTier.getSpeed(),equalTo(UsageTierEnum.FAST.name()));
		deleteTestTiers();
	}
	
	private void createTestTiers() {
		tierDao.addTier(new Tier().setSpeed(UsageTierEnum.FAST.name()).setThreshhold(-1L));
		tierDao.addTier(new Tier().setSpeed(UsageTierEnum.MEDIUM.name()).setThreshhold(3221225472L));
		tierDao.addTier(new Tier().setSpeed(UsageTierEnum.SLOW.name()).setThreshhold(5368709120L));
	}
	
	private void deleteTestTiers() {
		tierDao.deleteTier(new Tier().setSpeed(UsageTierEnum.SLOW.name()));
		tierDao.deleteTier(new Tier().setSpeed(UsageTierEnum.MEDIUM.name()));
		tierDao.deleteTier(new Tier().setSpeed(UsageTierEnum.FAST.name()));
	}
	
	@Test
	public void testCreateTierValidation() {
		try {
			tierDao.addTier(new Tier().setSpeed("BLAZING").setThreshhold(25L));
			fail("Expected an InvalidTierSpeedException to be thrown");
		}
		catch(InvalidTierSpeedException itse) {
			 assertNotNull(itse);
		}
	}
	
	@Test
	public void testDeleteTierValidation() {
		try {
			tierDao.deleteTier(new Tier().setSpeed("SNAILS_PACE").setThreshhold(3L));
			fail("Expected an InvalidTierSpeedException to be thrown");
		}
		catch(InvalidTierSpeedException itse) {
			 assertNotNull(itse);
		}
	}

}
