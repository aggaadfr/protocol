package wei.yigulu.iec104.asdudataframe;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.NoArgsConstructor;
import wei.yigulu.iec104.apdumodel.Apdu;
import wei.yigulu.iec104.apdumodel.Asdu;
import wei.yigulu.iec104.apdumodel.Vsq;
import wei.yigulu.iec104.asdudataframe.qualitydescription.IeMeasuredQuality;
import wei.yigulu.iec104.asdudataframe.typemodel.IeShortInteger;
import wei.yigulu.iec104.asdudataframe.typemodel.InformationBodyAddress;
import wei.yigulu.iec104.exception.Iec104Exception;
import wei.yigulu.iec104.nettyconfig.TechnicalTerm;
import wei.yigulu.iec104.util.SendAndReceiveNumUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 遥调，设点命令，标度化值数据类型（将数据设置成）
 * <p>
 * Project: protocol
 * Package: wei.yigulu.iec104.asdudataframe.typemodel
 * Version: 1.0
 * <p>
 * Created by WJX on 2022/5/19 11:29
 */
@NoArgsConstructor
@Data
public class RemoteRegulationCommandType extends AbstractDataFrameType {

    /**
     * TYPEID
     */
    public static final int TYPEID = TechnicalTerm.REMOTE_REGULATION_COMMAND_TYPE;

    private InformationBodyAddress addresses = new InformationBodyAddress();

    //短整型
    private List<IeShortInteger> val = new ArrayList<>();

    private IeMeasuredQuality quality = new IeMeasuredQuality();

    @Override
    public void loadByteBuf(ByteBuf is, Vsq vsq) {
        try {
            this.addresses = new InformationBodyAddress(is);
            this.val.add(new IeShortInteger(is));
            this.val.add(new IeShortInteger(is));
            // 品质描述解析
//            this.quality = new IeMeasuredQuality(is);
        } catch (Iec104Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void encode(List<Byte> buffer) {
        this.addresses.encode(buffer);

        for (IeShortInteger ieShortInteger : val) {
            ieShortInteger.encode(buffer);
        }
//        buffer.add((byte) this.quality.encode());
    }

    @Override
    public Asdu generateBack() {
        Asdu asdu = new Asdu();
        asdu.setTypeId(TYPEID);
        asdu.setDataFrame(this);
        asdu.getVsq().setSq(0);
        asdu.getVsq().setNum(1);
        asdu.setOriginatorAddress(0);
        asdu.setCommonAddress(1);
        return asdu;
    }

    @Override
    public byte[][] handleAndAnswer(Apdu apdu) throws Exception {
        // 请求或被请求
        byte[][] bs = new byte[1][];
        apdu.getAsdu().getCot().setNot(5);
        SendAndReceiveNumUtil.setSendAndReceiveNum(apdu, apdu.getChannel().id());
        bs[0] = apdu.encode();
        return bs;
    }

    @Override
    public String toString() {
        String s = "标度化值控制命令——";
        s += "地址：" + this.addresses.toString();
        s += "设定值：";
        for (IeShortInteger ieShortInteger : val) {
            s += ieShortInteger + ",";
        }
//        s += "设定值：" + this.val + ";";
//        s += this.quality.toString();
        return s;
    }
}
