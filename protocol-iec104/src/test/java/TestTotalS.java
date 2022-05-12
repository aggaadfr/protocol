/**
 * @author: xiuwei
 * @version:
 */

import wei.yigulu.iec104.annotation.AsduType;
import wei.yigulu.iec104.apdumodel.Apdu;
import wei.yigulu.iec104.asdudataframe.TotalSummonType;
import wei.yigulu.iec104.util.SendDataFrameHelper;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * @AsduType：会自动扫描该注释的所有类，并加载
 */

@AsduType
public class TestTotalS extends TotalSummonType {

	@Override
	public byte[][] handleAndAnswer(Apdu apdu) throws Exception {
//		Map<Integer, Boolean> booleans = new HashMap<>();
//		for (int i = 0; i < 100; i++) {
//			booleans.put(i, false);
//		}
//		SendDataFrameHelper.sendYxDataFrame(apdu.getChannel(), booleans, 1, 20, null);
//		final Map<Integer, Number> booleans = new HashMap<>();
//
//
//
//		///////////////////////////定时任务////////////////////////////////////
//		// 创建任务队列---
//		ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(10); // 10 为线程数量
//		// 执行任务
//		scheduledExecutorService.scheduleAtFixedRate(() -> {
//
//			for (int i = 0; i < 3; i++) {
//				booleans.put(i, new Number() {
//					@Override
//					public int intValue() {
//						return 0;
//					}
//
//					@Override
//					public long longValue() {
//						return 0;
//					}
//
//					@Override
//					public float floatValue() {
//						return makeRandom(100,1,1).floatValue();
//					}
//
//					@Override
//					public double doubleValue() {
//						return 0;
//					}
//				});
//			}
//			try {
				// 多线程发送遥测数据
//				SendDataFrameHelper.sendYcDataFrame(apdu.getChannel(), booleans, 1, 20, null);
//			}catch (Exception e){
//				System.out.println("发送消息错误：" + e.getMessage());
//			}
//
//
//		}, 1, 3, TimeUnit.SECONDS); // 1s 后开始执行，每 3s 执行一次


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
