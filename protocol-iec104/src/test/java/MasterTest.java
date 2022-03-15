import io.netty.util.ResourceLeakDetector;
import wei.yigulu.iec104.nettyconfig.Iec104HSMasterBuilder;
import wei.yigulu.iec104.util.SendCommandHelper;

/**
 * dad
 *
 * @author 修唯xiuwei
 * @create 2019-03-14 16:46
 * @Email 524710549@qq.com
 **/
public class MasterTest {

	public static void main(String[] args) throws Exception {
		//设置资源泄漏检测级别
		ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
		Iec104HSMasterBuilder masterBuilder = new Iec104HSMasterBuilder("127.0.0.1", 2404);
		//不阻塞线程
		masterBuilder.createByUnBlock();
		Thread.sleep(3000L);
//		SendCommandHelper.sendShortCommand(masterBuilder, 0, 1, 16385, 0.452f);
		SendCommandHelper.sendShortCommand(masterBuilder, 0, 1, 7, 0.452f);

	/*	//创建总召唤类型I帧
		TotalSummonType totalSummonType = new TotalSummonType();
		//反向生成asdu
		Asdu asdu = totalSummonType.generateBack();
		//配置总召唤发送原因
		asdu.setNot(6);
		//配置公共地址位
		asdu.setCommonAddress(1);
		Apdu apdu = new Apdu().setAsdu(asdu);
		masterBuilder.sendFrameToOpposite(apdu.encode());*/

	}
}
