package wei.yigulu.iec104.nettyconfig;


import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.NoArgsConstructor;
import org.slf4j.Logger;
import wei.yigulu.iec104.apdumodel.Apdu;
import wei.yigulu.iec104.asdudataframe.typemodel.container.Iec104Link;
import wei.yigulu.iec104.asdudataframe.typemodel.container.LinkContainer;
import wei.yigulu.iec104.util.PropertiesReader;
import wei.yigulu.utils.DataConvertor;

import java.net.InetSocketAddress;

/**
 * 消息处理类 继承netty SimpleChannelInboundHandler自动释放数据
 *
 * 自定义处理类
 * 显式地只处理特定类型的消息<ByteBuf>
 *
 *
 * @author 修唯xiuwei
 * @version 3.0
 */
@NoArgsConstructor
public class Slave104Handle extends SimpleChannelInboundHandler<ByteBuf> {

	private Logger log;

	//读取配置文件
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
	 * 收到消息后将调用
	 *
	 * @param ctx
	 * @param msg 字节缓冲区  经过AllCustomDelimiterHandler过滤后的数据
	 * @throws Exception
	 */
	@Override
	public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
		System.out.println("channelRead222: slave消息处理" + JSON.toJSONString(msg));
		//收数据
		log.debug("----------------------------------------------------------------------------------");
		log.debug("re <= " + DataConvertor.ByteBuf2String(msg));
		//设置通道传输，并将字节流数据帧转化为相应的APDU指令，将msg封装进asdu实体类，并标识
		Apdu apdu = apduClass.newInstance()
				.setChannel(ctx.channel())    //记录了该条APDU是在哪条通道里传输的
				.setIec104Builder(slaverBuilder)
				.setLog(slaverBuilder.getLog())
				.loadByteBuf(msg);
//		System.out.println("channelRead0-apdu(S I U 格式帧):" + JSON.toJSONString(apdu));
		//接收帧后的应答措施
		apdu.answer();
	}


	/**
	 * netty异常后调用
	 * @param ctx
	 * @param cause
	 * @throws Exception
	 */
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		log.error("104Slave交互时发生异常", cause);
	}


	/**
	 * fireChannelActive(活跃)
	 * 在channelRead0之前调用
	 * TODO 通道激活后将调用  转发到ChannelPipeline中的下一个
	 *
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
//		log.debug("通道激活，发出U帧，启动命令");
		//返回连接此通道的远端地址   通过 channel 拿到远程 client ip地址
		InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientIp = ipSocket.getAddress().getHostAddress();
		//远端端口
		Integer clientPort = ipSocket.getPort();
		if (!this.slaverBuilder.getConnectFilterManager().verdict(ctx.channel())) {
			ctx.channel().close();
			log.info(clientIp + ":" + clientPort + "客户端被过滤链拦截，已关闭通道");
			return;
		}
		log.info("slaver连接： " + clientIp + ":" + clientPort + " 客户端被连接");
		//slave储存104连接
		LinkContainer.getInstance().getLinks().put(ctx.channel().id(), new Iec104Link(ctx.channel(), clientIp, clientPort, Iec104Link.Role.MASTER, slaverBuilder.getLog()));
//		this.slaverBuilder.connected(ipSocket);
		//添加 channel子通道
		this.slaverBuilder.getChannels().add(ctx.channel());

		//是否需要启动开始时的应答，slave端不需要发送起始帧
/*		if (STARTASK) {
			log.info("从机建立连接，发送起始帧！！！！");
			//发送建立连接时发送的起始帧
			ctx.writeAndFlush(Unpooled.copiedBuffer(TechnicalTerm.START));
		}*/
	}

	/**
	 * fireChannelInactive(不活跃)
	 *
	 *
	 * @param ctx
	 * @throws Exception
	 */
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		InetSocketAddress ipSocket = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientIp = ipSocket.getAddress().getHostAddress();
		Integer clientPort = ipSocket.getPort();
		log.info(clientIp + ":" + clientPort + "客户端断开连接");
		//移除不活跃的channel
		this.slaverBuilder.getChannels().remove(ctx.channel());
		LinkContainer.getInstance().getLinks().remove(ctx.channel().id());
		this.slaverBuilder.disconnected(ipSocket);
	}

}

