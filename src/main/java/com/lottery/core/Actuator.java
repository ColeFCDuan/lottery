package com.lottery.core;

import java.util.List;
import java.util.Queue;
import com.lottery.number.LotteryNum;
import java.util.concurrent.ConcurrentMap;

public interface Actuator<T> {

	default void init(List<T> resources) throws Exception {
	}

	void execute(ConcurrentMap<String, Object> context, T resource, LotteryNum number) throws Exception;

	void predicate(ConcurrentMap<String, Object> context, LotteryNum number, Queue<Exception> exceptions) throws Exception;

}
