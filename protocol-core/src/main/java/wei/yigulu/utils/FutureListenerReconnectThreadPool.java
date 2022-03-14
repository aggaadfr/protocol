package wei.yigulu.utils;

import wei.yigulu.netty.BaseProtocolBuilder;

import java.util.Map;
import java.util.concurrent.*;

/**
 * Netty future的侦听器连接线程池（定时任务池）
 *
 * @author xiuwei
 * @date 2021/11/22
 */
public class FutureListenerReconnectThreadPool {

	ScheduledExecutorService pool = Executors.newScheduledThreadPool(10);
	private Map<BaseProtocolBuilder, ScheduledFuture> scheduledFutureMap = new ConcurrentHashMap<>();

	private FutureListenerReconnectThreadPool() {
	}

	public static final FutureListenerReconnectThreadPool getInstance() {
		return LazyHolder.INSTANCE;
	}

	public ScheduledFuture submitReconnectJob(BaseProtocolBuilder protocolBuilder, Runnable command) {
		return submitReconnectJob(protocolBuilder, command, 30);
	}

	public ScheduledFuture submitReconnectJob(BaseProtocolBuilder protocolBuilder, Runnable command, int delaySecond) {
		synchronized (protocolBuilder) {
            protocolBuilder.getLog().info("{},添加延时重连任务", protocolBuilder.getBuilderId());
			if (this.scheduledFutureMap.containsKey(protocolBuilder)) {
				ScheduledFuture f = this.scheduledFutureMap.get(protocolBuilder);
				//线程池内有客户端对应的定时任务线程
				if (!f.isDone() ) {
					//如果之前提交的定时任务未执行完毕
                    protocolBuilder.getLog().info("重连任务中已经包含未执行的重连任务", protocolBuilder.getBuilderId());
					f.cancel(true);
				}
			}
			this.scheduledFutureMap.put(protocolBuilder, pool.schedule(command, delaySecond, TimeUnit.SECONDS));
		}
		return this.scheduledFutureMap.get(protocolBuilder);
	}

	public void remove(BaseProtocolBuilder protocolBuilder) {
		if (this.scheduledFutureMap.containsKey(protocolBuilder)) {
			if (!this.scheduledFutureMap.get(protocolBuilder).isDone()) {
				this.scheduledFutureMap.get(protocolBuilder).cancel(true);
			}
			this.scheduledFutureMap.remove(protocolBuilder);
		}
	}

	private static class LazyHolder {
		private static final FutureListenerReconnectThreadPool INSTANCE = new FutureListenerReconnectThreadPool();
	}
}