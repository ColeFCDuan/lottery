package com.lottery.number.impl;

import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

import com.lottery.number.LotteryNum;

public class NormalNumber implements LotteryNum {

	private static Random RANDOM = new Random();
	private static int RED_NUM = 33;
	private static int BLUE_NUM = 16;
	private static int NEED_RED_NUM = 6;

	private static Long getNumber0(Random random) {
		long result = 0;
		Set<Integer> records = new TreeSet<>();
		int num = NEED_RED_NUM;
		while (records.size() != num) {
			records.add(random.nextInt(RED_NUM) + 1);
		}
		Iterator<Integer> iterator = records.iterator();
		for (long base = 1000_000_000_000L; iterator.hasNext(); base /= 100) {
			result += (iterator.next().longValue() * base);
		}
		result += random.nextInt(BLUE_NUM) + 1;
		return result;
	}

	@SuppressWarnings("unchecked")
	public Long getNumber(Random random) {
		if (Objects.isNull(random))
			random = RANDOM;
		return getNumber0(random);
	}

	@SuppressWarnings("unchecked")
	public Long getNumber() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		return getNumber0(random);
	}

}
