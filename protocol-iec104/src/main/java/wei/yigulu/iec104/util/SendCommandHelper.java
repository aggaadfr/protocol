package wei.yigulu.iec104.util;

import lombok.extern.slf4j.Slf4j;
import wei.yigulu.iec104.apdumodel.Apdu;
import wei.yigulu.iec104.apdumodel.Asdu;
import wei.yigulu.iec104.asdudataframe.ShortFloatCommand;
import wei.yigulu.iec104.asdudataframe.typemodel.IecDataInterface;
import wei.yigulu.netty.AbstractMasterBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 发送控制命令的工具类
 *
 * @author: xiuwei
 * @version:
 */
@Slf4j
public class SendCommandHelper {

	//在 List的操作外包加了一层synchronize同步控制
	private static List<CommandWaiter> commandWaiters = Collections.synchronizedList(new ArrayList());

	/**
	 * 发送一条遥测控制命令
	 *
	 * @param masterBuilder 104主站
	 * @param sourceAddress 源地址 0
	 * @param commonAddress 公共地址 1
	 * @param dataAddress   信息体地址 7
	 * @param value			数据内容
	 * @return
	 * @throws Exception
	 */
	public static boolean sendShortCommand(AbstractMasterBuilder masterBuilder, Integer sourceAddress, Integer commonAddress, Integer dataAddress, Float value) throws Exception {
		//创建一个帧
		ShortFloatCommand command = new ShortFloatCommand(dataAddress, value);
		System.out.println(command.toString());
		Apdu apdu = new Apdu();
		//把ShortFloatCommand转成Asdu
		Asdu asdu = command.generateBack();
		asdu.setCommonAddress(commonAddress);
		asdu.setOriginatorAddress(sourceAddress);
		asdu.getCot().setNot(6);

		apdu.setAsdu(asdu);
		//发送I帧
		SendAndReceiveNumUtil.sendIFrame(apdu, masterBuilder.getFuture().channel(), masterBuilder.getLog());
		//命令等待
		CommandWaiter commandWaiter = new CommandWaiter(masterBuilder.getFuture().channel().id(), apdu, dataAddress);
		commandWaiters.add(commandWaiter);
		IecDataInterface data;
		try {
			//等待5秒并返回数据
			data = commandWaiter.get();
		}catch (Exception e){
			throw e;
		}finally {
			commandWaiters.remove(commandWaiter);
		}

		if (data!=null && value.equals(data.getIecValue())) {
			return true;
		} else {
			return false;
		}
	}


	public static void setIecValue(CommandWaiter commandWaiter) {
		int i = commandWaiters.indexOf(commandWaiter);
		if (i != -1) {
			commandWaiters.get(i).set(commandWaiter.getData());
		} else {

		}
	}
}

