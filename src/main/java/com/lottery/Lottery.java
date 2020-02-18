package com.lottery;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.lottery.number.Number;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lottery.core.Actuator;
import com.lottery.resource.Resource;

@SuppressWarnings("rawtypes")
public class Lottery {

	private static final Logger log = LoggerFactory.getLogger(Lottery.class);

	private String path;

	private Resource resource;

	private Number number;

	private Actuator actuator;

	public String getPath() {
		return path;
	}

	public Lottery setPath(String path) {
		this.path = path;
		return this;
	}

	public Resource getResource() {
		return resource;
	}

	public Lottery setResource(Resource resource) {
		this.resource = resource;
		return this;
	}

	public Number getNumber() {
		return number;
	}

	public Lottery setNumber(Number number) {
		this.number = number;
		return this;
	}

	public Actuator getActuator() {
		return actuator;
	}

	public Lottery setActuator(Actuator actuator) {
		this.actuator = actuator;
		return this;
	}

	@SuppressWarnings("unchecked")
	void start() {
		try {
			actuator.init();
		} catch (Exception e) {
			log.error("actuator init err", e);
			return;
		}
		List<?> resources = null;
		try {
			resources = resource.load(path);
		} catch (Exception e) {
			log.error("resource load err", e);
			return;
		}
		ConcurrentMap<String, Object> context = new ConcurrentHashMap<>();
		int size = resources.size();
		CountDownLatch countDownLatch = new CountDownLatch(size);
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Queue<Exception> exceptions = new ConcurrentLinkedQueue<>();
		try {
			for (Object obj : resources) {
				executorService.execute(() -> {
					try {
						actuator.execute(context, obj, number);
						countDownLatch.countDown();
					} catch (Exception e) {
						executorService.shutdownNow();
						exceptions.add(e);
						log.error("actuator execute err", e);
						long tmp = countDownLatch.getCount();
						while (tmp-- > 0) {
							countDownLatch.countDown();
						}
					}
				});
			}

		} catch (Exception e) {
			log.info("swallow it");
		}
		try {
			countDownLatch.await();
			actuator.predicate(context, number, exceptions);
		} catch (Exception e) {
			log.error("actuator predicate err", e);
		}
	}
}
