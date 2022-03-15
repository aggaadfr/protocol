package wei.yigulu.iec104.nettyconfig;


import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import wei.yigulu.iec104.util.PropertiesReader;
import wei.yigulu.netty.AbstractTcpMasterBuilder;
import wei.yigulu.netty.ProtocolChannelInitializer;

import java.util.concurrent.TimeUnit;

/**
 * 104的主站  向子站发送总召唤 获取子站的数据
 * <p>
 * 简单的主站  相对于主备机主站    仅有主机 不支持切换
 *
 * @author 修唯xiuwei
 * @version 3.0
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class Iec104MasterBuilder extends AbstractTcpMasterBuilder {

	private static final String HEARTBEATPROPNAME = "heartBeatIntervalTime";
	private static final int HEARTBEATDEFVAL = 30;
	private static final int HEARTBEAT = PropertiesReader.getInstance().getIntProp(HEARTBEATPROPNAME, HEARTBEATDEFVAL);


	/**
	 * Simple master builder
	 *
	 * @param ip   ip
	 * @param port port
	 */
	public Iec104MasterBuilder(String ip, Integer port) {
		super(ip, port);
	}

	/**
	 * 通道初始化
	 *
	 * @return
	 */
	@Override
	protected ProtocolChannelInitializer getOrCreateChannelInitializer() {
		return getDefaultChannelInitializer(this);
	}

	/**
	 * 获取或创建通道初始化
	 *
	 * @param masterBuilder
	 * @return
	 */
	public static ProtocolChannelInitializer<SocketChannel> getDefaultChannelInitializer(AbstractTcpMasterBuilder masterBuilder) {
		return new ProtocolChannelInitializer<SocketChannel>(masterBuilder) {
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				//数据帧处理拆包类
				AllCustomDelimiterHandler handler = new AllCustomDelimiterHandler();
				//日志打印由BaseProtocolBuilder打印
				handler.setLog(masterBuilder.getLog());
				//添加  数据帧处理拆包类
				ch.pipeline().addLast(handler);
				//添加  心跳机制
				ch.pipeline().addLast(new IdleStateHandler(HEARTBEAT, 0, 0, TimeUnit.SECONDS));
				//添加  master消息处理类
				ch.pipeline().addLast(new Master104Handle((AbstractTcpMasterBuilder) builder));
			}
		};
	}
}
