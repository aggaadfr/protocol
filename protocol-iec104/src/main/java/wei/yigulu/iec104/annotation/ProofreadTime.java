package wei.yigulu.iec104.annotation;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import wei.yigulu.iec104.apdumodel.Apdu;
import wei.yigulu.iec104.apdumodel.Vsq;
import wei.yigulu.iec104.asdudataframe.ProofreadTimeType;
import wei.yigulu.iec104.asdudataframe.typemodel.IeProofreadTime;
import wei.yigulu.iec104.asdudataframe.typemodel.InformationBodyAddress;
import wei.yigulu.iec104.asdudataframe.typemodel.container.Iec104Link;
import wei.yigulu.iec104.asdudataframe.typemodel.container.LinkContainer;
import wei.yigulu.iec104.exception.Iec104Exception;
import wei.yigulu.iec104.util.SendAndReceiveNumUtil;

import java.util.List;

/**
 * 自定义对时 报文 请求
 *
 * Project: protocol
 * Package: wei.yigulu.iec104.annotation
 * Version: 1.0
 * <p>
 * Created by WJX on 2022/5/17 15:32
 */
@AsduType(typeId = 67)
@Slf4j
public class ProofreadTime extends ProofreadTimeType {


    private InformationBodyAddress address;
    private IeProofreadTime ieProofreadTime;


    @Override
    public void encode(List<Byte> buffer) {
        address.encode(buffer);
        ieProofreadTime.encode(buffer);
    }

    @Override
    public void loadByteBuf(ByteBuf is, Vsq vsq) {
        try {
            address = new InformationBodyAddress(is);
            ieProofreadTime = new IeProofreadTime(is);
        } catch (Iec104Exception e) {
            if (e.getCode() == 3301) {
                return;
            }
        }
    }

    @Override
    public byte[][] handleAndAnswer(Apdu apdu) {

        Channel channel = apdu.getChannel();
        int commonAddress = apdu.getAsdu().getCommonAddress();

        byte commonAddress1 = (byte) commonAddress;
        byte commonAddress2 = (byte) (commonAddress >> 8);


        //返回主站的对时报文即可，
        // 68（启动符）14（长度）0C  00（发送序号）02  00（接收序号）67（类型标示）01（可变结构限定词）07  00（传输原因）01  00（公共地址）00 00 00（信息体地址）**（毫秒低位）**（毫秒高位）**（分钟）04（时）81（日与星期）09（月）05（年）
        byte[][] result = new byte[1][];


        // 更新发送序号
        SendAndReceiveNumUtil.setSendAndReceiveNum(apdu, channel.id());

        // 拿到更新序号并转成byte
        Iec104Link link = LinkContainer.getInstance().getLink(channel.id());
        int send = link.getISend();
        int receive = link.getIReceive();

        byte sendSeqNum1 = (byte) (send << 1);
        byte sendSeqNum2 = (byte) (send >> 7);
        byte receiveSeqNum1 = (byte) (receive << 1);
        byte receiveSeqNum2 = (byte) (receive >> 7);



        //获得master那边解析后的时间
        byte[] btime = ieProofreadTime.getTimesBy16();

        result[0] = new byte[]{0x68, 0x14, sendSeqNum1, sendSeqNum2, receiveSeqNum1, receiveSeqNum2, 0x67, 0x01, 0x07, 0x00, commonAddress1, commonAddress2, 0x00, 0x00, 0x00, btime[0], btime[1], btime[2], btime[3], btime[4], btime[5], btime[6]};


        return result;
    }


    @Override
    public String toString() {
        String s = address.toString() + "\n";
        s += "时间校对帧,时间为：" + ieProofreadTime.toString();
        return s;
    }
}
