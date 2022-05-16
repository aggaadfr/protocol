package wei.yigulu.iec104.asdudataframe;

import io.netty.buffer.ByteBuf;
import lombok.Data;
import lombok.NoArgsConstructor;
import wei.yigulu.iec104.annotation.AsduType;
import wei.yigulu.iec104.apdumodel.Apdu;
import wei.yigulu.iec104.apdumodel.Asdu;
import wei.yigulu.iec104.apdumodel.Vsq;
import wei.yigulu.iec104.asdudataframe.qualitydescription.IeMeasuredQuality;
import wei.yigulu.iec104.asdudataframe.typemodel.IeShortFloat;
import wei.yigulu.iec104.asdudataframe.typemodel.InformationBodyAddress;
import wei.yigulu.iec104.exception.Iec104Exception;
import wei.yigulu.iec104.nettyconfig.TechnicalTerm;
import wei.yigulu.iec104.util.CommandWaiter;
import wei.yigulu.iec104.util.SendAndReceiveNumUtil;
import wei.yigulu.iec104.util.SendCommandHelper;
import wei.yigulu.iec104.util.SendDataFrameHelper;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 自定义发送模拟数据
 */
//@AsduType(typeId = 100)
public class CustomDeviceCommand extends TotalSummonType {


	/**
	 *
	 * @param apdu
	 * @return
	 * @throws Exception
	 */
	@Override
	public byte[][] handleAndAnswer(Apdu apdu) throws Exception {
//		Map<Integer, Boolean> booleans = new HashMap<>();
//		for (int i = 0; i < 100; i++) {
//			booleans.put(i, false);
//		}
//		SendDataFrameHelper.sendYxDataFrame(apdu.getChannel(), booleans, 1, 20, null);
		final Map<Integer, Number> booleans = new HashMap<>();



		///////////////////////////定时任务////////////////////////////////////
		// 创建任务队列---
		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10); // 10 为线程数量
		// 执行任务
		scheduledExecutorService.scheduleAtFixedRate(() -> {

			for (int i = 0; i < 120; i++) {
				booleans.put(i, new Number() {
					@Override
					public int intValue() {
						return 0;
					}

					@Override
					public long longValue() {
						return 0;
					}

					@Override
					public float floatValue() {
						return makeRandom(100,1,1).floatValue();
					}

					@Override
					public double doubleValue() {
						return 0;
					}
				});
			}
			try {
				//发送遥测 数据帧
				SendDataFrameHelper.sendYcDataFrame(apdu.getChannel(), booleans, 1, 20, null);
			}catch (Exception e){
//				e.printStackTrace();
			}


		}, 1, 5, TimeUnit.SECONDS); // 1s 后开始执行，每 3s 执行一次


		return null;
	}

	/**
	 * 生成指定范围，指定小数位数的随机数
	 * @param max 最大值
	 * @param min 最小值
	 * @param scale 小数位数
	 * @return
	 */
	private static BigDecimal makeRandom(float max, float min, int scale){
		BigDecimal cha = new BigDecimal(Math.random() * (max-min) + min);
		return cha.setScale(scale,BigDecimal.ROUND_HALF_UP);//保留 scale 位小数，并四舍五入
	}


}
