package com.programming4phone.data.redis.demo.dao;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class UsageDaoTest {

	@Autowired
	private UsageDao usageDao;
	
	@Test
	public void testExpire() throws InterruptedException {
		String TEST_ACCOUNT_NUMBER = "123456";
		
		usageDao.removeAccount(TEST_ACCOUNT_NUMBER);
		
		Long usageAmount;
		usageAmount = usageDao.getCurrentAmountUsed(TEST_ACCOUNT_NUMBER); // does not yet exist in Redis
		assertThat(usageAmount,equalTo(Long.valueOf(0)));
		
		usageAmount = usageDao.increaseAmountUsed(TEST_ACCOUNT_NUMBER, Long.valueOf(50));
		assertThat(usageAmount,equalTo(Long.valueOf(50)));
		
		Thread.sleep(11000);
		usageAmount = usageDao.getCurrentAmountUsed(TEST_ACCOUNT_NUMBER); // should have expired
		assertThat(usageAmount,equalTo(Long.valueOf(0)));
	}

	@Test
	public void testIncreaseDescrease()  {
		String TEST_ACCOUNT_NUMBER = "654321";
		
		usageDao.removeAccount(TEST_ACCOUNT_NUMBER);
		
		Long usageAmount;
		usageAmount = usageDao.getCurrentAmountUsed(TEST_ACCOUNT_NUMBER); // does not yet exist in Redis
		assertThat(usageAmount,equalTo(Long.valueOf(0)));
		
		usageAmount = usageDao.increaseAmountUsed(TEST_ACCOUNT_NUMBER, Long.valueOf(50));
		assertThat(usageAmount,equalTo(Long.valueOf(50)));
		
		usageAmount = usageDao.getCurrentAmountUsed(TEST_ACCOUNT_NUMBER);
		assertThat(usageAmount,equalTo(Long.valueOf(50)));
		
		usageAmount = usageDao.increaseAmountUsed(TEST_ACCOUNT_NUMBER, Long.valueOf(50));
		assertThat(usageAmount,equalTo(Long.valueOf(100)));
		
		usageAmount = usageDao.getCurrentAmountUsed(TEST_ACCOUNT_NUMBER);
		assertThat(usageAmount,equalTo(Long.valueOf(100)));
		
		usageAmount = usageDao.increaseAmountUsed(TEST_ACCOUNT_NUMBER, Long.valueOf(50));
		assertThat(usageAmount,equalTo(Long.valueOf(150)));
		
		usageAmount = usageDao.getCurrentAmountUsed(TEST_ACCOUNT_NUMBER);
		assertThat(usageAmount,equalTo(Long.valueOf(150)));
		
		usageAmount = usageDao.decreaseAmountUsed(TEST_ACCOUNT_NUMBER, Long.valueOf(100));
		assertThat(usageAmount,equalTo(Long.valueOf(50)));
		
		usageAmount = usageDao.getCurrentAmountUsed(TEST_ACCOUNT_NUMBER);
		assertThat(usageAmount,equalTo(Long.valueOf(50)));

		usageAmount = usageDao.decreaseAmountUsed(TEST_ACCOUNT_NUMBER, Long.valueOf(100));
		assertThat(usageAmount,equalTo(Long.valueOf(0)));
		
		usageAmount = usageDao.getCurrentAmountUsed(TEST_ACCOUNT_NUMBER);
		assertThat(usageAmount,equalTo(Long.valueOf(0)));
		
		usageAmount = usageDao.increaseAmountUsed(TEST_ACCOUNT_NUMBER, Long.valueOf(50));
		assertThat(usageAmount,equalTo(Long.valueOf(50)));
		
		usageAmount = usageDao.getCurrentAmountUsed(TEST_ACCOUNT_NUMBER);
		assertThat(usageAmount,equalTo(Long.valueOf(50)));
		
		usageDao.resetAccount(TEST_ACCOUNT_NUMBER);
		
		usageAmount = usageDao.getCurrentAmountUsed(TEST_ACCOUNT_NUMBER);
		assertThat(usageAmount,equalTo(Long.valueOf(0)));
	}
}
