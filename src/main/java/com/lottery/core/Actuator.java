package com.lottery.core;

import java.util.List;
import java.util.Queue;
import com.lottery.number.Number;
import java.util.concurrent.ConcurrentMap;

public interface Actuator<T> {

	default void init(List<T> resources) throws Exception {
	}

	void execute(ConcurrentMap<String, Object> context, T resource, Number number) throws Exception;

	void predicate(ConcurrentMap<String, Object> context, Number number, Queue<Exception> exceptions) throws Exception;

}
