package wei.yigulu.iec104.annotation;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import wei.yigulu.iec104.apdumodel.Apdu;
import wei.yigulu.iec104.asdudataframe.CustomElectroplateSummonType;
import wei.yigulu.iec104.asdudataframe.typemodel.container.Iec104Link;
import wei.yigulu.iec104.asdudataframe.typemodel.container.LinkContainer;
import wei.yigulu.iec104.util.SendDataFrameHelper;

import java.util.HashMap;
import java.util.Random;

/**
 * 自定义电镀召唤帧
 *
 * Project: protocol
 * Package: wei.yigulu.iec104.annotation
 * Version: 1.0
 * <p>
 * Created by WJX on 2022/5/16 15:58
 */
@Slf4j
@AsduType(typeId = 101)
public class CustomElectroplateSummon extends CustomElectroplateSummonType {

    private Random rand = new Random();

    @Override
    public byte[][] handleAndAnswer(Apdu apdu) throws Exception {

        Channel channel = apdu.getChannel();

        int commonAddress = apdu.getAsdu().getCommonAddress();

        try {
            // TODO 1、发送 电镀召唤确认
            //召唤确认(发送帧的镜像,除传送原因不同) ：68（启动符）0E（长度）10  00（发送序号）06  00（接收序号）65（类型标示）01（可变结构限定词）07  00（传输原因）01  00（公共地址）00 00 00（信息体地址）45（QCC）
            // 传送原因 64 07：总召激活确认
            // 64 03： 总召突发格式
            SendDataFrameHelper.sendElectroplateFrame(channel, commonAddress, 7, apdu.getLog());
        }catch (Exception e){
            log.error("响应电镀总召失败！");
        }


        // TODO 2、发送电镀数据
        //68（启动符）1A（长度）12  00（发送序号）06  00（接收序号）0F（类型标示）02（可变结构限定词,有两个电度量上送）05  00（传输原因）01  00（公共地址）01 0C 00（信息体地址，从0X0C01开始第0号电度）00 00 00 00（电度值）00（描述信息）02 0C 00（信息体地址，从0X0C01开始第1号电度）00 00 00 00（电度值）01（描述信息）
        try {
            HashMap<Integer, Number> ddDatas = new HashMap<>();
            for (int i = 0; i < 10; i++) {
                ddDatas.put(16785 + i, rand.nextFloat() + 10);
            }

            SendDataFrameHelper.sendDdDataFrame(channel, ddDatas, commonAddress, 7, apdu.getLog(), true);

        }catch (Exception e){
            log.error("响应总召，发送Dd数据失败！");
        }

        // TODO 3、结束总召唤帧
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
        // 68（启动符）0E（长度）14  00（发送序号）06  00（接收序号）65（类型标示）01（可变结构限定词）0A  00（传输原因）01  00（公共地址）00 00 00（信息体地址）45（QCC）
//        byte commonAddress1 = (byte) commonAddress;
//        byte commonAddress2 = (byte) (commonAddress >> 8);
        result[0] = new byte[]{0x68, 0x0e, sendSeqNum1, sendSeqNum2, receiveSeqNum1, receiveSeqNum2, 0x65, 0x01, 0x0a, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x45};

        return null;
    }
}
