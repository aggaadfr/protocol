package wei.yigulu.iec104.asdudataframe;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import wei.yigulu.iec104.apdumodel.Apdu;
import wei.yigulu.iec104.apdumodel.Asdu;
import wei.yigulu.iec104.apdumodel.Vsq;
import wei.yigulu.iec104.asdudataframe.AbstractDataFrameType;
import wei.yigulu.iec104.asdudataframe.typemodel.InformationBodyAddress;
import wei.yigulu.iec104.exception.Iec104Exception;
import wei.yigulu.iec104.nettyconfig.TechnicalTerm;
import wei.yigulu.iec104.util.SendDataFrameHelper;

import java.util.List;

/**
 * 电镀召唤帧 基类
 *
 * Project: protocol
 * Package: wei.yigulu.iec104.annotation
 * Version: 1.0
 * <p>
 * Created by WJX on 2022/5/16 15:45
 */
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CustomElectroplateSummonType extends AbstractDataFrameType {

    // 电镀 65
    public static final int TYPEID = TechnicalTerm.ELECTROPLATE_TYPE;
    private InformationBodyAddress address = new InformationBodyAddress(20);
    private int value;


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

    @Override
    public void encode(List<Byte> buffer) {
        address.encode(buffer);
        buffer.add((byte) value);
    }

    @Override
    public Asdu generateBack() {
        Asdu asdu = new Asdu();
        asdu.setTypeId(101);  // 电镀
        asdu.setDataFrame(this);
        asdu.getVsq().setNum(1);
        asdu.getVsq().setSq(0);
        asdu.getCot().setNot(7);
        asdu.setOriginatorAddress(0);
        asdu.setCommonAddress(1);
        return asdu;
    }

    @Override
    public byte[][] handleAndAnswer(Apdu apdu) throws Exception {
        return null;
    }

    @Override
    public String toString() {
        String s = address.toString();
        return "总召唤令；" + s + "\n召唤值：" + value + "\n";
    }
}
