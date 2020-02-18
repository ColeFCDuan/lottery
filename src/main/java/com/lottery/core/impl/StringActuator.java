package com.lottery.core.impl;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lottery.core.Actuator;
import com.lottery.number.Number;
import com.lottery.util.StringSimilarUtils;

public class StringActuator implements Actuator<String> {

	private static final Logger log = LoggerFactory.getLogger(StringActuator.class);
	private static PriorityQueue<Result> results = new PriorityQueue<>(
			(t0, t1) -> t0.score == t1.score ? 0 : t0.score > t1.score ? -1 : 1);
	private static int baseSize = 3;
	private static final int BASE_LEN = 14;
	private static Object lock = new Object();
	private static volatile int times = -1;
	private static volatile boolean calComplete = false;

	@Override
	public void execute(ConcurrentMap<String, Object> context, String resource, Number number) throws Exception {
		if (!calComplete) {
			times = resource.length() / BASE_LEN;
			calComplete = true;
		}
		StringBuilder sb = new StringBuilder();
		for (int tmp = times; tmp > 0; tmp--) {
			sb.append(number.getNumber().toString());
		}
		String tmp = number.getNumber();
		String dist = sb.toString();
		float socre = StringSimilarUtils.sorensenDice(resource, dist);
		synchronized (lock) {
			results.add(new Result(socre, tmp));
		}
	}

	@Override
	public void predicate(ConcurrentMap<String, Object> context, Number number, Queue<Exception> exceptions)
			throws Exception {
		if (!exceptions.isEmpty()) {
			log.error("can't predicate result because of exists exception");
		}
		for (long i = 0; i < baseSize; i++) {
			log.info("result: [{}]", results.poll());
		}
	}

	static class Result {
		float score;
		String todayBuy;

		public float getScore() {
			return score;
		}

		public void setScore(float score) {
			this.score = score;
		}

		public String getTodayBuy() {
			return todayBuy;
		}

		public void setTodayBuy(String todayBuy) {
			this.todayBuy = todayBuy;
		}

		public Result(float score, String todayBuy) {
			this.score = score;
			this.todayBuy = todayBuy;
		}

		@Override
		public String toString() {
			return "Result [score=" + score + ", todayBuy=" + todayBuy + "]";
		}
	}

}
