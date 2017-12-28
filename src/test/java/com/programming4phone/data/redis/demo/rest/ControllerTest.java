package com.programming4phone.data.redis.demo.rest;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.programming4phone.data.redis.demo.entity.CustomerUsage;
import com.programming4phone.data.redis.demo.entity.Tier;

import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class ControllerTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private static final String TIER_BASE_URL = "/throttle/tier";
	private static final String TIER_DELETE_URL = TIER_BASE_URL + "/delete/{speed}";
	private static final String TIER_USAGE_URL = TIER_BASE_URL + "/{accountNumber}";
	
	private static final String USAGE_BASE_URL = "/throttle/usage";
	private static final String USAGE_INCREASE_URL = USAGE_BASE_URL + "/increase";
	private static final String USAGE_DECREASE_URL = USAGE_BASE_URL + "/decrease";
	private static final String USAGE_REMOVE_URL = USAGE_BASE_URL + "/remove/{accountNumber}";
	private static final String USAGE_RESET_URL = USAGE_BASE_URL + "/reset/{accountNumber}";
	private static final String USAGE_GET_URL = USAGE_BASE_URL + "/{accountNumber}";
	
	private static final String FAST_SPEED = "FAST";
	private static final String MEDIUM_SPEED = "MEDIUM";
	private static final String SLOW_SPEED = "SLOW";
	
	private static final String TEST_ACCOUNT_NUMBER = "123456";
	
	@Autowired
	private TestRestTemplate restTemplate;
	
	@Test
	public void testUsage() {
		
		CustomerUsage requestCustomerUsage;
		CustomerUsage responseCustomerUsage;
		HttpEntity<CustomerUsage> httpEntity;
		ResponseEntity<CustomerUsage> responseEntity;
		ResponseEntity<Void> voidResponseEntity;
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		Map<String, String> params = new HashMap<String, String>();
	    params.put("accountNumber", TEST_ACCOUNT_NUMBER);
		
		createTiers();

		/*
		 * Get usage for non-existent Account
		 */
		responseEntity = restTemplate.getForEntity(USAGE_GET_URL, CustomerUsage.class, TEST_ACCOUNT_NUMBER);
		assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
		responseCustomerUsage = responseEntity.getBody();
		assertThat(responseCustomerUsage.getTotalUsage(),equalTo(0L));
		assertThat(responseCustomerUsage.getAccountNumber(),equalTo(TEST_ACCOUNT_NUMBER));
		
		/*
		 * Increase usage for non-existent account. New account is created.
		 */
		requestCustomerUsage = new CustomerUsage().setAccountNumber(TEST_ACCOUNT_NUMBER).setCurrentUsage(5L);
		httpEntity = new HttpEntity<>(requestCustomerUsage, headers);
		responseEntity = restTemplate.exchange(USAGE_INCREASE_URL, HttpMethod.POST, httpEntity, CustomerUsage.class);
		assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
		responseCustomerUsage = responseEntity.getBody();
		assertThat(responseCustomerUsage.getTotalUsage(),equalTo(5L));
		
		/*
		 * Get tier usage amount for new account, should use FAST usage tier speed.
		 */
		responseEntity = restTemplate.getForEntity(TIER_USAGE_URL, CustomerUsage.class, TEST_ACCOUNT_NUMBER);
		assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
		responseCustomerUsage = responseEntity.getBody();
		assertThat(responseCustomerUsage.getTotalUsage(),equalTo(5L));
		assertThat(responseCustomerUsage.getAccountNumber(),equalTo(TEST_ACCOUNT_NUMBER));
		assertThat(responseCustomerUsage.getSpeed(),equalTo(FAST_SPEED));

		/*
		 * Increase usage for existing account. 
		 */
		requestCustomerUsage = new CustomerUsage().setAccountNumber(TEST_ACCOUNT_NUMBER).setCurrentUsage(150L);
		httpEntity = new HttpEntity<>(requestCustomerUsage, headers);
		responseEntity = restTemplate.exchange(USAGE_INCREASE_URL, HttpMethod.POST, httpEntity, CustomerUsage.class);
		assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
		responseCustomerUsage = responseEntity.getBody();
		assertThat(responseCustomerUsage.getTotalUsage(),equalTo(155L));
		
		/*
		 * Get tier usage amount for existing account, should use MEDIUM usage tier speed.
		 */
		responseEntity = restTemplate.getForEntity(TIER_USAGE_URL, CustomerUsage.class, TEST_ACCOUNT_NUMBER);
		assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
		responseCustomerUsage = responseEntity.getBody();
		assertThat(responseCustomerUsage.getTotalUsage(),equalTo(155L));
		assertThat(responseCustomerUsage.getAccountNumber(),equalTo(TEST_ACCOUNT_NUMBER));
		assertThat(responseCustomerUsage.getSpeed(),equalTo(MEDIUM_SPEED));
		
		
		/*
		 * Increase usage even more for existing account. 
		 */
		requestCustomerUsage = new CustomerUsage().setAccountNumber(TEST_ACCOUNT_NUMBER).setCurrentUsage(100L);
		httpEntity = new HttpEntity<>(requestCustomerUsage, headers);
		responseEntity = restTemplate.exchange(USAGE_INCREASE_URL, HttpMethod.POST, httpEntity, CustomerUsage.class);
		assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
		responseCustomerUsage = responseEntity.getBody();
		assertThat(responseCustomerUsage.getTotalUsage(),equalTo(255L));
		
		/*
		 * Get tier usage amount for existing account, should use SLOW usage tier speed.
		 */
		responseEntity = restTemplate.getForEntity(TIER_USAGE_URL, CustomerUsage.class, TEST_ACCOUNT_NUMBER);
		assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
		responseCustomerUsage = responseEntity.getBody();
		assertThat(responseCustomerUsage.getTotalUsage(),equalTo(255L));
		assertThat(responseCustomerUsage.getAccountNumber(),equalTo(TEST_ACCOUNT_NUMBER));
		assertThat(responseCustomerUsage.getSpeed(),equalTo(SLOW_SPEED));
		
		/*
		 * Decrease usage for existing account. 
		 */
		requestCustomerUsage = new CustomerUsage().setAccountNumber(TEST_ACCOUNT_NUMBER).setCurrentUsage(200L);
		httpEntity = new HttpEntity<>(requestCustomerUsage, headers);
		responseEntity = restTemplate.exchange(USAGE_DECREASE_URL, HttpMethod.POST, httpEntity, CustomerUsage.class);
		assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
		responseCustomerUsage = responseEntity.getBody();
		assertThat(responseCustomerUsage.getTotalUsage(),equalTo(55L));
		
		/*
		 * Get tier usage amount for existing account, should use FAST usage tier speed.
		 */
		responseEntity = restTemplate.getForEntity(TIER_USAGE_URL, CustomerUsage.class, TEST_ACCOUNT_NUMBER);
		assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
		responseCustomerUsage = responseEntity.getBody();
		assertThat(responseCustomerUsage.getTotalUsage(),equalTo(55L));
		assertThat(responseCustomerUsage.getAccountNumber(),equalTo(TEST_ACCOUNT_NUMBER));
		assertThat(responseCustomerUsage.getSpeed(),equalTo(FAST_SPEED));
		
		/*
		 * Reset account
		 */

		voidResponseEntity = restTemplate.exchange(USAGE_RESET_URL, HttpMethod.DELETE, null, Void.class,params);
		assertThat(voidResponseEntity.getStatusCode(), equalTo(HttpStatus.OK));
		
		/*
		 * Get tier usage amount for reset account, should use FAST usage tier speed.
		 */
		responseEntity = restTemplate.getForEntity(TIER_USAGE_URL, CustomerUsage.class, TEST_ACCOUNT_NUMBER);
		assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
		responseCustomerUsage = responseEntity.getBody();
		assertThat(responseCustomerUsage.getTotalUsage(),equalTo(0L));
		assertThat(responseCustomerUsage.getAccountNumber(),equalTo(TEST_ACCOUNT_NUMBER));
		assertThat(responseCustomerUsage.getSpeed(),equalTo(FAST_SPEED));
		
		/*
		 * Remove account
		 */
		params.put("accountNumber", TEST_ACCOUNT_NUMBER);
		voidResponseEntity = restTemplate.exchange(USAGE_REMOVE_URL, HttpMethod.DELETE, null, Void.class,params);
		assertThat(voidResponseEntity.getStatusCode(), equalTo(HttpStatus.OK));
		
		/*
		 * Get usage for non-existent Account
		 */
		responseEntity = restTemplate.getForEntity(USAGE_GET_URL, CustomerUsage.class, TEST_ACCOUNT_NUMBER);
		assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
		responseCustomerUsage = responseEntity.getBody();
		assertThat(responseCustomerUsage.getTotalUsage(),equalTo(0L));
		assertThat(responseCustomerUsage.getAccountNumber(),equalTo(TEST_ACCOUNT_NUMBER));
		
		deleteTiers();
	}

	@Test
	public void testTiersFound() {
		
		ResponseEntity<Tier[]> response;
		
		createTiers();
		
		response = restTemplate.getForEntity(TIER_BASE_URL, Tier[].class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.OK));
		
		List<Tier> usageTiers = Arrays.asList(response.getBody());
		assertThat(usageTiers.size(), equalTo(3));
		usageTiers.forEach(t->logger.info(t.toString()));
		
		deleteTiers();
		
		response = restTemplate.getForEntity(TIER_BASE_URL, Tier[].class);
		assertThat(response.getStatusCode(), equalTo(HttpStatus.NOT_FOUND));
	}
	
	private void createTiers() {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		
		HttpEntity<Tier> requestUpdate;
		
		Tier tier1 = new Tier().setSpeed(FAST_SPEED).setThreshhold(-1L);
		requestUpdate = new HttpEntity<>(tier1, headers);
		restTemplate.exchange(TIER_BASE_URL, HttpMethod.PUT, requestUpdate, Void.class);
		
		Tier tier2 = new Tier().setSpeed(MEDIUM_SPEED).setThreshhold(100L);
		requestUpdate = new HttpEntity<>(tier2, headers);
		restTemplate.exchange(TIER_BASE_URL, HttpMethod.PUT, requestUpdate, Void.class);
		
		Tier tier3 = new Tier().setSpeed("SLOW").setThreshhold(200L);
		requestUpdate = new HttpEntity<>(tier3, headers);
		restTemplate.exchange(TIER_BASE_URL, HttpMethod.PUT, requestUpdate, Void.class);		
	}
	
	private void deleteTiers() {
		Map<String, String> params = new HashMap<String, String>();
	    params.put("speed", FAST_SPEED);
		restTemplate.exchange(TIER_DELETE_URL, HttpMethod.DELETE, null, Void.class,params);
		params.put("speed", MEDIUM_SPEED);
		restTemplate.exchange(TIER_DELETE_URL, HttpMethod.DELETE, null, Void.class,params);
		params.put("speed", SLOW_SPEED);
		restTemplate.exchange(TIER_DELETE_URL, HttpMethod.DELETE, null, Void.class,params);
	}

}
