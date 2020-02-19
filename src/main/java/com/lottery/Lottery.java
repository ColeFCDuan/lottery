package com.lottery;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lottery.core.Actuator;
import com.lottery.number.LotteryNum;
import com.lottery.resource.Resource;

@SuppressWarnings("rawtypes")
public class Lottery {

	private static final Logger log = LoggerFactory.getLogger(Lottery.class);

	private String path;

	private Resource resource;

	private LotteryNum number;

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

	public LotteryNum getNumber() {
		return number;
	}

	public Lottery setNumber(LotteryNum number) {
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
		List<?> resources = null;
		try {
			resources = resource.load(path);
		} catch (Exception e) {
			log.error("resource load err", e);
			return;
		}
		try {
			actuator.init(resources);
		} catch (Exception e) {
			log.error("actuator init err", e);
			return;
		}
		ConcurrentMap<String, Object> context = new ConcurrentHashMap<>();
		ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		Queue<Exception> exceptions = new ConcurrentLinkedQueue<>();
		try {
			Object obj = null;
			while (Objects.nonNull(obj) && !exceptions.isEmpty() || Objects.nonNull(obj = actuator.judge())) {
				Object tmp = obj;
				executorService.execute(() -> {
					try {
						actuator.execute(context, tmp, number);
					} catch (Exception e) {
						executorService.shutdownNow();
						exceptions.add(e);
						log.error("actuator execute err", e);
					}
				});
			}
			executorService.shutdown();
		} catch (Exception e) {
			log.info("actuator judge err", e);
		}
		while (!executorService.isTerminated()) {
			try {
				TimeUnit.SECONDS.sleep(1);
			} catch (Exception e) {
				// swallow it
			}
		}
		try {
			actuator.predicate(context, number, exceptions);
		} catch (Exception e) {
			log.error("actuator predicate err", e);
		}
	}
}
