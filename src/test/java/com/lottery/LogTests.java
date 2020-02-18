package com.lottery;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTests {
	private static final Logger log = LoggerFactory.getLogger(LogTests.class);

	@Test
	public void logTest() {
		log.trace("test trace");
		log.debug("test debug");
		log.info("test info");
		log.warn("test warn");
		log.error("test error");
	}
}
