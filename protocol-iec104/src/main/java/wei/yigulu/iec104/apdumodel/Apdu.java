package wei.yigulu.iec104.apdumodel;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import wei.yigulu.iec104.container.Iec104Link;
import wei.yigulu.iec104.container.LinkContainer;
import wei.yigulu.iec104.exception.Iec104Exception;
import wei.yigulu.iec104.nettyconfig.TechnicalTerm;
import wei.yigulu.iec104.util.SendAndReceiveNumUtil;
import wei.yigulu.netty.BaseProtocolBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 104协议内核处理类
 * 104信息体模型类
 * 消息分为 S I U 格式帧
 * I格式帧具有ASDU消息体
 *
 * @author 修唯xiuwei
 * @version 3.0
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Apdu {


	private Logger log = LoggerFactory.getLogger(this.getClass());

	/**
	 * 本端的发送序号
	 */
	protected int sendSeqNum = 0;
	/**
	 * 本端的接收序号
	 */
	protected int receiveSeqNum = 0;
	/**
	 * 本帧的类型
	 */
	protected ApciType apciType = ApciType.I_FORMAT;

	/**
	 * Asdu
	 */
	protected Asdu asdu = null;

	/**
	 * Iec 104 builder
	 */
	protected BaseProtocolBuilder iec104Builder;

	/**
	 * 记录了该条APDU是在哪条通道里传输的
	 */
	protected Channel channel;

	/**
	 * 枚举，APCI类型，即I帧，S帧，U帧
	 */
	public enum ApciType {
		/**
		 * I帧
		 */
		I_FORMAT,
		/**
		 * S帧
		 */
		S_FORMAT,
		/**
		 * U帧，测试确认
		 */
		TESTFR_CON,
		/**
		 * U帧，测试命令
		 */
		TESTFR_ACT,
		/**
		 * U帧，停止确认
		 */
		STOPDT_CON,
		/**
		 * U帧，停止命令
		 */
		STOPDT_ACT,
		/**
		 * U帧，启动确认
		 */
		STARTDT_CON,
		/**
		 * U帧，启动命令
		 */
		STARTDT_ACT
	}


	/**
	 * 读取字节流 将数据帧转化为APDU
	 *
	 * @param dis dis
	 * @return apdu
	 * @throws Exception exception
	 */
	public Apdu loadByteBuf(ByteBuf dis) throws Exception {
		this.channel = channel;
		int start = dis.readByte() & 0xff;
		int len = dis.readByte() & 0xff;
		log.debug("APDU长度：" + len);
		byte[] controlFields = new byte[4];
		if (start != 0x68) {
			new Iec104Exception("起始字符错误" + start);
		} else if (len < 4 || len > 253) {
			new Iec104Exception("帧长度有误" + len);
		} else {
			//读4字节控制域
			dis.readBytes(controlFields);
			//第一比特=0  ===》I格式
			if ((controlFields[0] & 0x01) == 0) {
				//I帧
				this.apciType = ApciType.I_FORMAT;
				//发送序列号  先是最低有效位 ，接下来是最高有效位,最高有效位拿到后面，最低有效位后面补0，LSB 0;MSB
				sendSeqNum = ((controlFields[0] & 0xfe) >> 1) + ((controlFields[1] & 0xff) << 7);
				//接收序列号 原理同发送序列号
				receiveSeqNum = ((controlFields[2] & 0xfe) >> 1) + ((controlFields[3] & 0xff) << 7);
				log.debug("I帧，发送序列号：" + sendSeqNum + "，接收序列号：" + receiveSeqNum);
				if (this.channel != null) {
					SendAndReceiveNumUtil.receiveIFrame(this, this.channel.id());
				}
				//第一比特=1  第二比特=0 ===》S格式
			} else if ((controlFields[0] & 0x03) == 1) {
				//S帧
				this.apciType = ApciType.S_FORMAT;
				receiveSeqNum = ((controlFields[2] & 0xfe) >> 1) + ((controlFields[3] & 0xff) << 7);
				log.debug("S帧，接收序列号：" + receiveSeqNum);
				//第一比特=1  第二比特=1 ===》S格式
			} else if ((controlFields[0] & 0x03) == 3) {
				//U帧
				switch (controlFields[0]) {
					case 0x07:
						this.apciType = ApciType.STARTDT_ACT;
						log.debug("U帧，启动命令");
						break;
					case 0x0B:
						this.apciType = ApciType.STARTDT_CON;
						log.debug("U帧启动确认");
						break;
					case 0x13:
						this.apciType = ApciType.STOPDT_ACT;
						log.debug("U帧停止命令");
						break;
					case 0x23:
						this.apciType = ApciType.STOPDT_CON;
						log.debug("U帧停止确认");
						break;
					case 0x43:
						this.apciType = ApciType.TESTFR_ACT;
						log.debug("U帧测试命令");
						break;
					case (byte) 0x83:
						this.apciType = ApciType.TESTFR_CON;
						log.debug("U帧测试确认");
						break;
					default:
						log.debug("U帧类型异常");
						break;
				}
			}
			//构建数据单元
			if (len > 6) {
				this.asdu = new Asdu().setLog(log).loadByteBuf(dis);
			}
		}
		return this;
	}


	/**
	 * APDU编码由当前的apdu编码成数据帧
	 *
	 * @return byte [ ]
	 * @throws Exception exception
	 */
	public byte[] encode() throws Exception {
		List<Byte> buffer = new ArrayList<>();
		buffer.add((byte) 0x68);
		buffer.add((byte) 0x00);
		if (apciType == ApciType.I_FORMAT) {
			buffer.add((byte) (sendSeqNum << 1));
			buffer.add((byte) (sendSeqNum >> 7));
			buffer.add((byte) (receiveSeqNum << 1));
			buffer.add((byte) (receiveSeqNum >> 7));
			asdu.encode(buffer);
		} else if (apciType == ApciType.STARTDT_ACT) {
			buffer.add((byte) 0x07);
			buffer.add((byte) 0x00);
			buffer.add((byte) 0x00);
			buffer.add((byte) 0x00);
		} else if (apciType == ApciType.STARTDT_CON) {
			buffer.add((byte) 0x0b);
			buffer.add((byte) 0x00);
			buffer.add((byte) 0x00);
			buffer.add((byte) 0x00);
		} else if (apciType == ApciType.S_FORMAT) {
			buffer.add((byte) 0x01);
			buffer.add((byte) 0x00);
			buffer.add((byte) (receiveSeqNum << 1));
			buffer.add((byte) (receiveSeqNum >> 7));
		}
		buffer.set(1, (byte) (buffer.size() - 2));
		byte[] bs = new byte[buffer.size()];
		for (int i = 0; i < buffer.size(); i++) {
			bs[i] = buffer.get(i);
		}
		return bs;
	}


	/**
	 * Sets asdu *
	 *
	 * @param asdu asdu
	 * @return the asdu
	 */
	public Apdu setAsdu(Asdu asdu) {
		this.asdu = asdu;
		this.apciType = ApciType.I_FORMAT;
		return this;
	}


	/**
	 * 接收帧后的应答措施
	 *
	 * @throws Iec104Exception iec exception
	 */
	public void answer() throws Iec104Exception {
		byte[][] bb = new byte[0][];
		if (this.apciType == ApciType.I_FORMAT) {
			try {
				bb = this.asdu.getDataFrame().handleAndAnswer(this);
			} catch (Exception e) {
				if (e instanceof NullPointerException) {
					log.error("数据帧解析后的逻辑处理出现异常", e);
					//throw new Iec104Exception("该I帧无数据体");
				}
				log.error("数据帧解析后的逻辑处理出现异常", e);
				//throw new Iec104Exception("I帧响应帧编译出错");
			}
		} else if (this.apciType == ApciType.S_FORMAT) {
			bb = sHandleAndAnswer();
		} else {
			bb = uHandleAndAnswer();
		}
		ByteBuf buffer = Unpooled.compositeBuffer();
		if (bb != null) {
			for (byte[] b : bb) {
				buffer.writeBytes(b);
			}
		}
		this.channel.pipeline().writeAndFlush(buffer);
	}

	/**
	 * U帧的应答措施
	 *
	 * @return byte [ ] [ ]
	 * @throws Iec104Exception iec exception
	 */
	public byte[][] uHandleAndAnswer() throws Iec104Exception {
		byte[][] bb = null;
		if (this.apciType == ApciType.STARTDT_ACT) {
			bb = new byte[1][];
			bb[0] = TechnicalTerm.STARTBACK;
		} else if (this.apciType == ApciType.STARTDT_CON) {
			//bb = new byte[1][];
			//bb[0] = TechnicalTerm.GENERALINTERROGATION;
		} else if (this.apciType == ApciType.STOPDT_ACT) {
			bb = new byte[1][];
			bb[0] = TechnicalTerm.STOPBACK;
		} else if (this.apciType == ApciType.TESTFR_ACT) {
			bb = new byte[1][];
			bb[0] = TechnicalTerm.TESTBACK;
		}
		return bb;
	}


	/**
	 * 对方响应S帧的应答措施
	 *
	 * @return byte [ ] [ ]
	 * @throws Iec104Exception iec exception
	 */
	public byte[][] sHandleAndAnswer() throws Iec104Exception {
		Iec104Link link = LinkContainer.getInstance().getLink(channel.id());
		int send = this.receiveSeqNum;
		int send1=link.getISend();
		if(send1>send){
			loseSend();
			link.setISend(send);
		}
		if(send1<send){
			log.warn("我方或对方计数出错");
			link.setISend(send);
		}
		return null;
	}

	/**
	 * 我方丢失 通道对方放出的 i帧
	 */
	public void loseReceive() {
	}

	/**
	 * 通道对方丢失 我方发出的i帧
	 */
	public void loseSend() {
	}


}
