package wei.yigulu.netty;


import io.netty.channel.ChannelFuture;

/**
 * 负责监听启动时连接失败，重新连接功能
 * 相对与主备模式  该连接监听仅支持单主机模式
 *
 * @author 修唯xiuwei
 * @version 3.0
 */

public class SimpleTcpConnectionListener extends ProtocolConnectionListener<AbstractTcpMasterBuilder> {

	/**
	 * Only host connection listener
	 *
	 * @param masterBuilder master builder
	 */
	public SimpleTcpConnectionListener(AbstractTcpMasterBuilder masterBuilder) {
		super(masterBuilder);
	}


	@Override
	protected void reconnectFuture(ChannelFuture channelFuture) {
		try {
			if (masterBuilder.future == null || !masterBuilder.future.channel().isActive()) {
				log.error("服务端{}:{}链接不上，开始重连操作", this.masterBuilder.getIp(), this.masterBuilder.getPort());
				masterBuilder.createByUnBlock();
			} else {
				log.warn("masterBuilder在延迟过程中已由其他线程连接成功，此处略过重连");
			}
		} catch (Exception e) {
			log.error("TcpMaster重试连接时发生异常", e);
			try {
				operationComplete(channelFuture);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}

