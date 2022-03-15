package wei.yigulu.netty;


import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import wei.yigulu.threadpool.LocalThreadPool;
import wei.yigulu.utils.FutureListenerReconnectThreadPool;


/**
 * 网络传输层的client
 *
 * @author 修唯xiuwei
 * @version 3.0
 */

@Accessors(chain = true)
public abstract class AbstractClientBuilder extends BaseProtocolBuilder {


	/**
	 * Work group
	 */
	protected EventLoopGroup workGroup = null;
	/**
	 * Bootstrap
	 */
	protected Bootstrap bootstrap = null;

	/**
	 * Future  通道  异步
	 */
	@Getter
	@Setter
	protected ChannelFuture future;
	/**
	 * Connection listener
	 */
	protected ProtocolConnectionListener connectionListener = null;


	protected ProtocolChannelInitializer channelInitializer = null;


	public boolean isConnected(){
		return this.future!=null && this.future.channel().isActive();
	}

	public void stop() {
		log.info("关闭通道{}", this.builderId);
		if (this.future != null) {
			this.future.removeListener(getOrCreateConnectionListener());
			this.future.addListener(ChannelFutureListener.CLOSE);
		}
		getOrCreateConnectionListener().stop();
		this.connectionListener = null;
		if (this.workGroup != null) {
			this.workGroup.shutdownGracefully();
		}
		FutureListenerReconnectThreadPool.getInstance().remove(this);
		this.bootstrap = null;
		this.workGroup = null;
	}


	/**
	 * 创建Master 连接
	 */
	public abstract void create();

	/**
	 * Create by un block
	 */
	public void createByUnBlock() {
		LocalThreadPool.getInstance().getLocalPool().execute(this::create);
	}


	/**
	 * null则创建，有则获取获取EventLoopGroup 用与bootstrap的绑定
	 *
	 * @return or create work group
	 */
	public abstract EventLoopGroup getOrCreateWorkGroup();


	/**
	 * null则创建，有则获取获取bootstrap
	 *
	 * @return or create bootstrap
	 */
	public abstract Bootstrap getOrCreateBootstrap();


	/**
	 * null则创建，有则获取获取ConnectionListener
	 *
	 * @return or create connection listener
	 */
	public abstract ProtocolConnectionListener getOrCreateConnectionListener();

	/**
	 * null则创建，有则获取获取ChannelInitializer
	 *
	 * @return or create ChannelInitializer
	 */
	protected abstract ProtocolChannelInitializer getOrCreateChannelInitializer();

	/**
	 * 通道连接成功
	 */
	public void connected() {

	}

	/**
	 * 通道断开连接
	 */
	public void disconnected() {

	}


}
