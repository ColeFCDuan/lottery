package com.lottery.core.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lottery.core.Actuator;
import com.lottery.number.LotteryNum;

public class NormalActuator implements Actuator<Long> {

	private static final Logger log = LoggerFactory.getLogger(NormalActuator.class);
	private static Queue<Long> results;

	@Override
	public void init(List<Long> resources) throws Exception {
		results = new ConcurrentLinkedQueue<>();
	}

	@Override
	public void execute(ConcurrentMap<String, Object> context, Long resource, LotteryNum number) throws Exception {
		for (long count = 0;; count++) {
			// 生成一组随机的数据
			Long dist = number.getNumber();
			if (dist.equals(resource)) {
				results.offer(count);
				break;
			}
		}
	}

	@Override
	public void predicate(ConcurrentMap<String, Object> context, LotteryNum number, Queue<Exception> exceptions)
			throws Exception {
		if (!exceptions.isEmpty()) {
			log.error("can't predicate result because of exists exception");
		}
		long totalNum = 0;
		for (long per : results) {
			totalNum += per;
		}
		// 取所有次数的平均数
		long averageNum = totalNum / results.size();
		// 取出平均数加前后三次的数据，作为今日数据
		List<Long> todayBuyList = new ArrayList<>();
		for (long generatePer = 0; generatePer < averageNum; generatePer++) {
			if (averageNum - generatePer == 1) {
				// 生成今日数据
				Long todayBuy = number.getNumber();
				todayBuyList.add(todayBuy);
			}

		}
		log.info("result: [{}]", todayBuyList);
	}

}
