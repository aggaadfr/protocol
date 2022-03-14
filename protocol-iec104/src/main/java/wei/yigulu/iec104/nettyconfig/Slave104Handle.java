package wei.yigulu.iec104.nettyconfig;


import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import wei.yigulu.iec104.apdumodel.Apdu;
import wei.yigulu.iec104.container.Iec104Link;
import wei.yigulu.iec104.container.LinkContainer;
import wei.yigulu.iec104.util.PropertiesReader;
import wei.yigulu.utils.DataConvertor;

import java.net.InetSocketAddress;

/**
 * 消息处理类 继承netty SimpleChannelInboundHandler自动释放数据
 *
 * @author 修唯xiuwei
 * @version 3.0
 */
@NoArgsConstructor
public class Slave104Handle extends SimpleChannelInboundHandler<ByteBuf> {

	private Logger log;

	private static final String STARTASKPROPNAME = "haveStartAsk";

	private static final boolean STARTASKDEFVAL = false;

	private static final boolean STARTASK = PropertiesReader.getInstance().getBooleanProp(STARTASKPROPNAME, STARTASKDEFVAL);

	/**
	 * Slave 104 handle
	 *
	 * @param slaverBuilder slaver builder
	 */
	public Slave104Handle(Iec104SlaverBuilder slaverBuilder) {
		this.slaverBuilder = slaverBuilder;
		this.log = slaverBuilder.getLog();
	}

	public Slave104Handle(Iec104SlaverBuilder slaverBuilder, Class<? extends Apdu> apduClass) {
		this(slaverBuilder);
		this.apduClass = apduClass;
	}

	private Iec104SlaverBuilder slaverBuilder;


	private Class<? extends Apdu> apduClass = Apdu.class;

	/**
	 * 拆包后的数据会来到这个
	 *
	 * @param ctx
	 * @param msg 字节缓冲区
	 * @throws Exception
	 */
	@Override
	public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		System.out.println("channelRead222: 拆包后的数据进入消息处理类" + JSON.toJSONString(msg));
		//收数据
		log.debug("----------------------------------------------------------------------------------");
		log.debug("re <= " + DataConvertor.ByteBuf2String(msg));
		//设置通道传输，并将字节流数据帧转化为相应的APDU指令
		Apdu apdu = apduClass.newInstance().setChannel(ctx.channel()).setIec104Builder(slaverBuilder).setLog(slaverBuilder.getLog()).loadByteBuf(msg);
		System.out.println("channelRead0-apdu(S I U 格式帧):" + JSON.toJSONString(apdu));
		//接收帧后的应答措施
		apdu.answer();
	}


	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("104Slave交互时发生异常", cause);
	}


	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		log.debug("发出U帧，启动命令");
		InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientIp = ipSocket.getAddress().getHostAddress();
		Integer clientPort = ipSocket.getPort();
		if (!this.slaverBuilder.getConnectFilterManager().verdict(ctx.channel())) {
			ctx.channel().close();
			log.info(clientIp + ":" + clientPort + "客户端被过滤链拦截，已关闭通道");
			return;
		}
		log.info(clientIp + ":" + clientPort + "客户端连接");
		LinkContainer.getInstance().getLinks().put(ctx.channel().id(), new Iec104Link(ctx.channel(), clientIp, clientPort, Iec104Link.Role.MASTER, slaverBuilder.getLog()));
		this.slaverBuilder.connected(ipSocket);
		this.slaverBuilder.getChannels().add(ctx.channel());
		if (STARTASK) {
			ctx.writeAndFlush(Unpooled.copiedBuffer(TechnicalTerm.START));
		}


	}


	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientIp = ipSocket.getAddress().getHostAddress();
		Integer clientPort = ipSocket.getPort();
		log.info(clientIp + ":" + clientPort + "客户端断开连接");
		this.slaverBuilder.getChannels().remove(ctx.channel());
		LinkContainer.getInstance().getLinks().remove(ctx.channel().id());
		this.slaverBuilder.disconnected(ipSocket);
	}

}

