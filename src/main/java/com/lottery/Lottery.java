package com.lottery;

import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
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

	private ExecutorService executorService = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
			Runtime.getRuntime().availableProcessors(), 0, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(1 << 16),
			(r, executor) -> {
				try {
					if (!executor.isShutdown()) {
						executor.getQueue().put(r);
					}
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					throw new RejectedExecutionException(
							"Executor was interrupted while the task was waiting to put on work queue", e);
				}
			});

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

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
		if (Objects.isNull(resource) || Objects.isNull(actuator) || Objects.isNull(number) || Objects.isNull(path)) {
			throw new IllegalArgumentException("resource or actuator or number or path is empty");
		}
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
