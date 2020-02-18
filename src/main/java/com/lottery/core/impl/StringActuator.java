package com.lottery.core.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lottery.core.Actuator;
import com.lottery.number.LotteryNum;
import com.lottery.util.StringSimilarUtils;

public class StringActuator implements Actuator<String> {

	private static final Logger log = LoggerFactory.getLogger(StringActuator.class);
	private static PriorityBlockingQueue<Result> results;
	private static PriorityBlockingQueue<Result> records;
	private static int baseSize = 10;
	private static volatile boolean needRemove = false;
	private static final int BASE_LEN = 14;
	private static volatile int times = -1;
	private static volatile boolean calComplete = false;
	private static List<Result> lists;
	private static Object lock = new Object();

	@Override
	public void init(List<String> resources) throws Exception {
		results = new PriorityBlockingQueue<>(baseSize * 2,
				(t0, t1) -> t0.score == t1.score ? 0 : t0.score > t1.score ? -1 : 1);
		records = new PriorityBlockingQueue<>(baseSize * 2,
				(t0, t1) -> t0.score == t1.score ? 0 : t0.score > t1.score ? 1 : -1);
		lists = new ArrayList<>(baseSize * 2);
	}

	@Override
	public void execute(ConcurrentMap<String, Object> context, String resource, LotteryNum number) throws Exception {
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
		double socre = StringSimilarUtils.impossibleEnd(resource, dist, false);
		synchronized (lock) {
			Result t = new Result(socre, tmp);
			results.add(t);
			records.add(t);
			if (needRemove) {
				results.remove(records.poll());
			} else if (results.size() >= baseSize) {
				needRemove = true;
			}
		}
//		synchronized (lock) {
//			lists.add(new Result(socre, tmp));
//			if (needRemove) {
//				lists.sort((t0, t1) -> t0.score == t1.score ? 0 : t0.score > t1.score ? -1 : 1);
//				lists.remove(lists.size() - 1);
//			} else if (lists.size() >= baseSize) {
//				needRemove = true;
//			}
//		}
	}

	@Override
	public void predicate(ConcurrentMap<String, Object> context, LotteryNum number, Queue<Exception> exceptions)
			throws Exception {
		if (!exceptions.isEmpty()) {
			log.error("can't predicate result because of exists exception");
		}
		for (long i = 0; i < baseSize; i++) {
			log.info("result: [{}]", results.poll());
		}
//		synchronized (lock) {
//			log.info("result: [{}]", lists);
//		}
	}

	static class Result {
		double score;
		String todayBuy;

		public double getScore() {
			return score;
		}

		public void setScore(double score) {
			this.score = score;
		}

		public String getTodayBuy() {
			return todayBuy;
		}

		public void setTodayBuy(String todayBuy) {
			this.todayBuy = todayBuy;
		}

		public Result(double score, String todayBuy) {
			this.score = score;
			this.todayBuy = todayBuy;
		}

		@Override
		public String toString() {
			return "Result [score=" + score + ", todayBuy=" + todayBuy + "]";
		}
	}

}
