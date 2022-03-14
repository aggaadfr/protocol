package wei.yigulu.netty;


import io.netty.channel.ChannelFuture;
import lombok.extern.slf4j.Slf4j;

/**
 * 负责监听启动时连接失败，重新连接功能
 *
 * @author 修唯xiuwei
 * @create 2019-03-13 14:15
 * @Email 524710549@qq.com
 **/

@Slf4j
public class RtuModeConnectionListener extends ProtocolConnectionListener<AbstractRtuModeBuilder> {


	/**
	 * Only host connection listener
	 *
	 * @param masterBuilder master builder
	 */
	public RtuModeConnectionListener(AbstractRtuModeBuilder masterBuilder) {
		super(masterBuilder);
	}

	@Override
	protected void reconnectFuture(ChannelFuture channelFuture) {
		log.error("RTU:{}端链接不上，开始重连操作...", masterBuilder.getCommPortId());
		channelFuture.channel().closeFuture();
		masterBuilder.create();
	}
}

