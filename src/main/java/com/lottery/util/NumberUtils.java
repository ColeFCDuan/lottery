package com.lottery.util;

import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ThreadLocalRandom;

public class NumberUtils {

	private NumberUtils() {
	}

	private static Random RANDOM = new Random();
	private static int RED_NUM = 33;
	private static int BLUE_NUM = 16;
	private static int NEED_RED_NUM = 6;

	private static String getNumber0(Random random) {
		Set<Integer> records = new TreeSet<>();
		int num = NEED_RED_NUM;
		while (records.size() != num) {
			records.add(random.nextInt(RED_NUM) + 1);
		}
		Iterator<Integer> iterator = records.iterator();
		StringBuilder sb = new StringBuilder();
		int tmp = 0;
		for (; iterator.hasNext(); num--) {
			tmp = iterator.next();
			sb.append(tmp < 10 ? "0" : "").append(tmp).append(num == 1 ? "\\" : ",");
		}
		int blue = random.nextInt(BLUE_NUM) + 1;
		sb.append(blue < 10 ? "0" : "").append(blue);
		return sb.toString();
	}

	public static String getNumber() {
		return getNumber0(RANDOM);
	}

	public static String getNumber(Random random) {
		if (Objects.isNull(random))
			random = RANDOM;
		return getNumber0(random);
	}

	public static String getSafeNumber() {
		ThreadLocalRandom random = ThreadLocalRandom.current();
		return getNumber0(random);
	}

}
