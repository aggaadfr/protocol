package wei.yigulu.iec104.annotation;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.extern.log4j.Log4j;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import wei.yigulu.iec104.apdumodel.Apdu;
import wei.yigulu.iec104.apdumodel.Vsq;
import wei.yigulu.iec104.asdudataframe.TotalSummonType;
import wei.yigulu.iec104.asdudataframe.typemodel.InformationBodyAddress;
import wei.yigulu.iec104.asdudataframe.typemodel.container.Iec104Link;
import wei.yigulu.iec104.asdudataframe.typemodel.container.LinkContainer;
import wei.yigulu.iec104.exception.Iec104Exception;
import wei.yigulu.iec104.util.SendDataFrameHelper;
import wei.yigulu.utils.DataConvertor;

import java.util.HashMap;
import java.util.Random;

/**
 * 自定义总召处理发送数据
 *
 * Project: protocol
 * Package: wei.yigulu.iec104.annotation
 * Version: 1.0
 * <p>
 * Created by WJX on 2022/5/13 10:48
 */


@AsduType(typeId = 100)
@Slf4j
public class CustomTotalSummon extends TotalSummonType {

    private Random rand = new Random();
    private InformationBodyAddress address = new InformationBodyAddress(20);
    private int value;

    /**
     * 读取总召数据，并打印信息
     * @param is  msg
     * @param vsq 可变结构限定词
     */
    @Override
    public void loadByteBuf(ByteBuf is, Vsq vsq) {
        try {
            this.address = new InformationBodyAddress(is);
            this.value = (is.readByte() & 0xff);
        } catch (Iec104Exception e) {
            if (e.getCode() == 3301) {
                return;
            }
        }
    }

    /**
     * 响应总召，并发送数据
     * @param apdu apdu
     * @return
     * @throws Exception
     */
    @Override
    public byte[][] handleAndAnswer(Apdu apdu) throws Exception {


        Channel channel = apdu.getChannel();
        int commonAddress = apdu.getAsdu().getCommonAddress();

        try {
            // TODO 1、发送 总召唤确认
            //68（启动符）0E（长度） 00  00（发送序号，2个字节）  02 00（接收序号，2个字节）  64 （类型标识） 01（可变结构限定词）  07  00（传送原因，2个字节）  01  00（公共地址，即RTU站址，2个字节）00  00 00（信息体地址，3个字节）  14 （QOI）
            // 传送原因 64 07：总召激活确认
            // 64 03： 总召突发格式
            SendDataFrameHelper.sendTotalSummonFrame(channel, commonAddress, 7, apdu.getLog());
        }catch (Exception e){
            log.error("响应总召失败！");
        }


        // TODO 2、发送YX帧，直到遥信信号全部发送完毕，（一条报文，一次发送最大127，会自动拆分报文的长度，分批发送报文）
        try {
            HashMap<Integer, Boolean> yxDatas = new HashMap<>();
            for (int i = 0; i < 10; i++) {
                yxDatas.put(i + 102, rand.nextBoolean());
            }
            //发送遥信数据帧
            SendDataFrameHelper.sendYxDataFrame(channel, yxDatas, commonAddress, 20, apdu.getLog());
        }catch (Exception e){
            log.error("响应总召，发送YX数据失败！");
        }



        // TODO 3、发送YC帧，知道遥信信号全部发送完毕
        try {
            HashMap<Integer, Number> ycDatas = new HashMap<>();
            for (int i = 0; i < 10; i++) {
                ycDatas.put(16485 + i, rand.nextInt(100));
            }
            //发送遥测 数据帧
            SendDataFrameHelper.sendYcDataFrame(channel, ycDatas, commonAddress, 20, apdu.getLog());
        }catch (Exception e){
            log.error("响应总召，发送YC数据失败！");
        }

        // TODO 5、结束总召唤帧
        // 更新发送序号
        Iec104Link link = LinkContainer.getInstance().getLink(channel.id());
        int send = link.getISend();
        int receive = link.getIReceive();
        // 发送次数+1
        apdu.setSendSeqNum(send++);
        // 接收次数不变
        apdu.setReceiveSeqNum(receive);
        // 重新设置通道的发送 id
        link.setISend(send);
        // 重新放回通道
        LinkContainer.getInstance().getLinks().put(channel.id(), link);


        byte sendSeqNum1 = (byte) (send << 1);
        byte sendSeqNum2 = (byte) (send >> 7);
        byte receiveSeqNum1 = (byte) (receive << 1);
        byte receiveSeqNum2 = (byte) (receive >> 7);

        byte[][] result = new byte[1][];
        // 68 （启动符） 0E（长度）08  00 （发送序号，2个字节）02  00（接收序号，2个字节）  64  （类型标识）01 （可变结构限定词）0A  00 （传送原因，2个字节） 01  00 （公共地址，即RTU站址，2个字节） 00  00  00（信息体地址，3个字节）  14（QOI）
//        byte commonAddress1 = (byte) commonAddress;
//        byte commonAddress2 = (byte) (commonAddress >> 8);
        result[0] = new byte[]{0x68, 0x0e, sendSeqNum1, sendSeqNum2, receiveSeqNum1, receiveSeqNum2, 0x64, 0x01, 0x0a, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x14};

        return result;
    }



    @Override
    public String toString() {
        String s = address.toString();
        return "总召唤令；" + s + "\n召唤值：" + value + "\n";
    }

}
