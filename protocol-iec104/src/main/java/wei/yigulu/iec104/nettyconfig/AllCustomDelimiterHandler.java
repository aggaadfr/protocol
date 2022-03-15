package wei.yigulu.iec104.nettyconfig;


import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import wei.yigulu.netty.AbstractDelimiterHandler;
import wei.yigulu.utils.DataConvertor;


/**
 * 未继承netty的
 * 数据帧处理拆包类
 *
 * @author 修唯xiuwei
 * @version 3.0
 */
//@Slf4j
public class AllCustomDelimiterHandler extends AbstractDelimiterHandler {


	private static final byte[] HEAD = new byte[]{0x68};


	public AllCustomDelimiterHandler() {
		//判断是否是断包的最大时间间隔
		super.maxTimeSpace = 100;
		//接收的最长的报文长度
		super.maxLength = 10240;
	}

	/**
	 * 收到数据后调用--拆包和封包
	 *
	 * @param ctx
	 * @param msg 字节缓冲区
	 * @throws Exception
	 */
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		System.out.println("channelRead111---拆包or封包:" + JSON.toJSONString(msg));
		//判断写入报文是否超过最大长度，并把数据写入缓存内容
		if (isOverMaxLength((ByteBuf) msg)) {
			return;
		}
		int len;
		//查看第一个 HEAD 的头位置
		int headIndex = getHeadIndex(0, cumulation.writerIndex(), cumulation, HEAD);
		//当数据帧里存在头字节 且长度大于3时进入循环
		while (cumulation.readableBytes() >= 6 && headIndex != -1) {
			//如果头字节不在第一个字节 那么读取标志向后推到头字节位置
			if (headIndex > cumulation.readerIndex()) {
				log.warn("舍弃了一无用段报文:" + DataConvertor.ByteBuf2StringAndRelease(cumulation.readBytes(headIndex - cumulation.readerIndex())));
			}
			//标记读取位置
			cumulation.markReaderIndex();
			//向后读取一位 即0x68的占位
			cumulation.readBytes(1).release();
			//获取到该帧的长度 帧内标定的长度
			len = cumulation.readUnsignedByte();
			//如果帧的真实长度少于 帧内标定长度则代表数据帧不完整，退出循环等待下一数据帧进入进行粘帧
			if (cumulation.readableBytes() < len) {
				cumulation.resetReaderIndex();
				//数据帧长度不足 记录时间
				timeMark = DateTime.now();
				return;
			} else {
				cumulation.resetReaderIndex();
				//TODO 输出数据
				//如果数据帧长度足够 将规定长度的直接加入out 队列
				ctx.fireChannelRead(cumulation.readBytes(len + 2));
				//查看后续的字节里面头字节的位置
				headIndex = getHeadIndex(cumulation.readerIndex(), cumulation.writerIndex(), cumulation, HEAD);
			}
		}
		if (cumulation.readableBytes() != 0 && headIndex >= cumulation.readerIndex()) {
			//buffer中还有数据 而且其中有数据头
			timeMark = DateTime.now();
			return;
		} else {
			//buffer没有数据 或剩余这段字节中没有数据头
			if (cumulation.readableBytes() != 0) {
				log.warn("这段字节中没有数据头,舍弃:" + DataConvertor.ByteBuf2String(cumulation.readBytes(cumulation.readableBytes())));
			}
			//清除寄存ByteBuf的指向和内容
			clearCumulation();
		}
	}

}
