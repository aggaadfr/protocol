import lombok.extern.slf4j.Slf4j;
import wei.yigulu.modbus.domain.datatype.RegisterValue;
import wei.yigulu.modbus.domain.datatype.numeric.P_AB;
import wei.yigulu.modbus.domain.synchronouswaitingroom.TcpSynchronousWaitingRoom;
import wei.yigulu.modbus.exceptiom.ModbusException;
import wei.yigulu.modbus.netty.ModbusTcpMasterBuilder;
import wei.yigulu.modbus.utils.ModbusCommandDataUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author: xiuwei
 * @version:
 */
@Slf4j
public class TestTcpCommandMaster {
	public static void main(String[] args) throws InterruptedException, ModbusException {
		ModbusTcpMasterBuilder master = new ModbusTcpMasterBuilder("127.0.0.1", 5002);
		master.createByUnBlock();
		TcpSynchronousWaitingRoom.waitTime = 5000L;
		Thread.sleep(5000L);
		Random random = new Random();
		BigDecimal val;
		BigDecimal val1;
		for (; ; ) {
			val = BigDecimal.valueOf(random.nextInt(100));
			System.out.println("数据个数：" + val);
			List<RegisterValue> list = new ArrayList<>();
			for (int i = 0; i <= val.intValue(); i++) {
				val1 = BigDecimal.valueOf(random.nextInt(11));
				System.out.println("数据值：" + val1);
				list.add(new P_AB().setValue(val1));
			}
			System.out.println(ModbusCommandDataUtils.commandRegister(master, 1, 0, list));
			Thread.sleep(60000L);
		}
	}


}
