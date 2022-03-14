package wei.yigulu.netty;

import io.netty.buffer.ByteBuf;

/**
 * 协议层的master
 *
 * @author: xiuwei
 * @version:
 */
public interface MasterInterface {
	/**
	 * 向对端 发送数据帧
	 *
	 * @param bytes
	 */
	void sendFrameToOpposite(byte[] bytes);

	/**
	 * 向对端 发送数据帧
	 *
	 * @param byteBuf
	 */
	void sendFrameToOpposite(ByteBuf byteBuf);

}
