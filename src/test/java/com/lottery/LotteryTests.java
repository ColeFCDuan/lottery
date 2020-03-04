package com.lottery;

import org.junit.Test;

import com.lottery.core.Actuator;
import com.lottery.core.impl.NormalActuator;
import com.lottery.core.impl.StringActuator;
import com.lottery.number.LotteryNum;
import com.lottery.number.impl.NormalNumber;
import com.lottery.number.impl.StringNumber;
import com.lottery.resource.Resource;
import com.lottery.resource.impl.NormalResource;
import com.lottery.resource.impl.StringResource;

public class LotteryTests {

	@Test
	public void testStart() {
		Lottery lottery = new Lottery();
		
        Resource resource = new StringResource();
        Actuator actuator = new StringActuator();
        LotteryNum number = new StringNumber();

//		Resource resource = new NormalResource();
//		Actuator actuator = new NormalActuator();
//		LotteryNum number = new NormalNumber();

		lottery.setPath("/ssq_result_test.txt");
		lottery.setResource(resource);
		lottery.setActuator(actuator);
		lottery.setNumber(number);
		long s = System.currentTimeMillis();
		lottery.start();
		System.out.println(System.currentTimeMillis() - s);
	}
}
