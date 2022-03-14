package wei.yigulu.iec104.nettyconfig;


import io.netty.channel.socket.SocketChannel;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import wei.yigulu.netty.AbstractTcpSlaverBuilder;
import wei.yigulu.netty.ProtocolChannelInitializer;


/**
 * 104的子站  是向主站提供数据的 主站发送总召唤 子站响应主站的召唤
 * 向主站上送数据
 *
 * @author 修唯xiuwei
 * @version 3.0
 */
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class Iec104SlaverBuilder extends AbstractTcpSlaverBuilder {


	public Iec104SlaverBuilder(int port) {
		super(port);
	}

	/**
	 * 初始化通道
	 * @return
	 */
	@Override
	protected ProtocolChannelInitializer getOrCreateChannelInitializer() {
		return new ProtocolChannelInitializer<SocketChannel>(this) {
			//初始化多个Channel上的事件处理
			@Override
			protected void initChannel(SocketChannel ch) throws Exception {
				//添加是有顺序的
				//添加组件 数据帧处理拆包类  一个线程执行
				ch.pipeline().addLast(new AllCustomDelimiterHandler());
				//添加组件 消息处理类  一个线程执行
				ch.pipeline().addLast(new Slave104Handle((Iec104SlaverBuilder) builder));
			}

		};
	}

}
