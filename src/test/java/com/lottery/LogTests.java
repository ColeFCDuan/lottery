package com.lottery;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogTests {
	private static final Logger log = LoggerFactory.getLogger(LogTests.class);

	@Test
	public void logTest() {
		double a = Double.NEGATIVE_INFINITY;
		if (a + 1 == a) {
		    System.out.println("ok");
		}
	}
}
