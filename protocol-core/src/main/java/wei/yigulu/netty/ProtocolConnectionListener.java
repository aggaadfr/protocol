package wei.yigulu.netty;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.util.concurrent.ScheduledFuture;
import org.slf4j.Logger;
import wei.yigulu.utils.FutureListenerReconnectThreadPool;

/**
 * 框架只用的连接监听
 *
 * @author: xiuwei
 * @version:
 */
public abstract class ProtocolConnectionListener<T extends AbstractClientBuilder> implements ChannelFutureListener {

	protected ScheduledFuture<?> future;
	protected Logger log;
	protected T masterBuilder;
	protected boolean isStop = false;

	/**
	 * Only host connection listener
	 *
	 * @param masterBuilder master builder
	 */
	public ProtocolConnectionListener(T masterBuilder) {
		this.masterBuilder = masterBuilder;
		this.log = masterBuilder.getLog();
	}


	@Override
	public void operationComplete(ChannelFuture future) throws Exception {
		if (isStop) {
			log.info("通道已经停止，不再重连");
			return;
		} else {
			if (future == null || future.channel() == null || !future.channel().isActive()) {
				FutureListenerReconnectThreadPool.getInstance().submitReconnectJob(masterBuilder, () -> {
					reconnectFuture(future);
				});
			} else {
				log.warn("masterBuilder已经连接成功，不进行重连操作");
			}
		}
	}

	/**
	 * 连接任务
	 *
	 * @param channelFuture 频道任务
	 */
	protected abstract void reconnectFuture(ChannelFuture channelFuture);

	public void stop() {
		this.isStop = true;
	}
}
