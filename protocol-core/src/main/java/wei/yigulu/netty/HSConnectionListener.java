package wei.yigulu.netty;


import io.netty.channel.ChannelFuture;
import io.netty.util.internal.StringUtil;

/**
 * 负责监听启动时连接失败，重新连接功能
 * 带有主备切换功能的 连接监听
 *
 * @author 修唯xiuwei
 * @version 3.0
 */
public class HSConnectionListener extends ProtocolConnectionListener<AbstractHSTcpMasterBuilder> {


	private int retryTimes;

	/**
	 * Only host connection listener
	 *
	 * @param masterBuilder master builder
	 */
	public HSConnectionListener(AbstractHSTcpMasterBuilder masterBuilder) {
		super(masterBuilder);
	}


	@Override
	protected void reconnectFuture(ChannelFuture channelFuture) {
		try {
			if (masterBuilder.future == null || !masterBuilder.future.channel().isActive()) {
				log.warn("开始执行{}重连任务", masterBuilder.builderId);
				if (this.retryTimes < 10) {
					log.error("服务端{}:{}链接不上，开始重连操作,第{}次尝试", this.masterBuilder.getIp(), this.masterBuilder.getPort(), retryTimes);
					masterBuilder.createByUnBlock();
					log.warn("重试连接失败");
					this.retryTimes++;
				} else {
					if (!StringUtil.isNullOrEmpty(this.masterBuilder.getSpareIp()) || (this.masterBuilder.getSparePort() != null && this.masterBuilder.getSparePort() != 0)) {
						log.info("服务端{}:{}链接不上，切换主备机{}:{}", this.masterBuilder.getIp(), this.masterBuilder.getPort(), this.masterBuilder.getSpareIp(), this.masterBuilder.getSparePort());
						this.masterBuilder.switchover();
					}
					this.masterBuilder.refreshLoopGroup();
					this.retryTimes = 0;
					masterBuilder.createByUnBlock();
					log.info("重置重试次数=0");
				}
			} else {
				log.warn("masterBuilder在延迟过程中已由其他线程连接成功，此处略过重连");
			}
		} catch (Exception e) {
			log.error("ModbusMaster重试连接时发生异常", e);
			this.masterBuilder.refreshLoopGroup();
			this.retryTimes = 0;
			try {
				operationComplete(channelFuture);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}

