package com.lottery.core.impl;

import java.util.Date;
import java.util.List;
import java.util.Objects;
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
	private static final int BASE_LEN = 14;
	private PriorityBlockingQueue<Result> results;
	private PriorityBlockingQueue<Result> records;
	private int baseSize = 10;
	private volatile boolean needRemove = false;
	private int repeatTimes = -1;
	private Object lock = new Object();
	private long times;
	private Date endDate;
	private double score = 0;
	private volatile boolean scoreOk = false;
	private List<String> resources;

	public StringActuator(Date endDate) {
		super();
		this.endDate = endDate;
	}

	public StringActuator(double score) {
		super();
		this.score = score;
	}

	public StringActuator(long times) {
		super();
		this.times = times;
	}

	@Override
	public void init(List<String> resources) throws Exception {
		if (Objects.nonNull(resources) && !resources.isEmpty()) {
			repeatTimes = resources.get(0).length() / BASE_LEN;
			this.resources = resources;
		} else {
			throw new IllegalArgumentException("resources is empty");
		}
		if (times == 0 && Objects.isNull(endDate) && score == 0) {
			times = 5000;
		}
		results = new PriorityBlockingQueue<>(baseSize * 2,
				(t0, t1) -> t0.score == t1.score ? 0 : t0.score > t1.score ? -1 : 1);
		records = new PriorityBlockingQueue<>(baseSize * 2,
				(t0, t1) -> t0.score == t1.score ? 0 : t0.score > t1.score ? 1 : -1);
	}

	@Override
	public void execute(ConcurrentMap<String, Object> context, String resource, LotteryNum number) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int tmp = repeatTimes; tmp > 0; tmp--) {
			sb.append(number.getNumber().toString());
		}
		String tmp = number.getNumber();
		String dist = sb.toString();
		double score = StringSimilarUtils.impossibleEnd(resource, dist, false);
		synchronized (lock) {
			Result t = new Result(score, tmp);
			results.add(t);
			records.add(t);
			if (needRemove) {
				results.remove(records.poll());
			} else if (results.size() >= baseSize) {
				needRemove = true;
			}
		}
		if (this.score != 0 && this.score >= score) {
			scoreOk = true;
		}
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

	@Override
	public String judge() throws Exception {
		if (scoreOk) {
			return null;
		}
		if (score != 0 || Objects.nonNull(endDate) && endDate.after(new Date()) || times-- > 0) {
			return resources.get(0);
		}
		return null;
	}

}
