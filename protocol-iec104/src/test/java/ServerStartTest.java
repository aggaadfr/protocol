import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

/**
 * 测试如何启动一个slave客户端，关键是：自定义  消息处理类
 *
 * @author 修唯xiuwei
 * @create 2018-02-05 15:56
 * @Email 524710549@qq.com
 **/

public class ServerStartTest {
	public static void main(String[] args) {
		//创建2个netty线程组
		// 一个Netty服务端启动时，通常会有两个NioEventLoopGroup：一个是监听线程组，主要是监听客户端请求，另一个是工作线程组，主要是处理与客户端的数据通讯。
		EventLoopGroup boss = new NioEventLoopGroup();
		EventLoopGroup worker = new NioEventLoopGroup();

		try {
			//辅助启动类
			ServerBootstrap bootstrap = new ServerBootstrap();
			//设置线程池   有无先后顺序
			bootstrap.group(boss, worker);

			//设置socket工厂  用于服务端非阻塞地接收TCP连接
			bootstrap.channel(NioServerSocketChannel.class);

			//设置管道工厂
			bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel socketChannel) throws Exception {
					//获取管道
					ChannelPipeline pipeline = socketChannel.pipeline();
					//字符串解码器
					pipeline.addLast(new StringDecoder());
					//字符串编码器
					pipeline.addLast(new StringEncoder());
					//TODO 自定义  消息处理类  只是处理string消息并打印，没有做复杂的逻辑处理
					pipeline.addLast(new ServerHandler4());
				}
			});

			//设置TCP参数
			//1.链接缓冲池的大小（ServerSocketChannel的设置）
			bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
			//维持链接的活跃，清除死链接(SocketChannel的设置)
			bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
			//关闭延迟发送
			bootstrap.childOption(ChannelOption.TCP_NODELAY, true);

			//绑定端口
			ChannelFuture future = bootstrap.bind(9001).sync();
			System.out.println("server start ...... ");

			//优雅关闭 等待服务端监听端口关闭
			future.channel().closeFuture().sync();

		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			//优雅退出，释放线程池资源
			boss.shutdownGracefully();
			worker.shutdownGracefully();
		}
	}

}

/**
 * 自定义处理类
 * 显式地只处理特定类型的消息<String>
 */
class ServerHandler4 extends SimpleChannelInboundHandler<String> {

	//读取客户端发送过来的数据
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		System.out.println("client response :" + msg);
	}

	//新客户端接入后调用
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channelActive");
	}

	//客户端断开后调用
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		System.out.println("channelInactive");
	}

	//异常后调用
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		//关闭通道
		ctx.channel().close();
		//打印异常
		cause.printStackTrace();
	}
}
