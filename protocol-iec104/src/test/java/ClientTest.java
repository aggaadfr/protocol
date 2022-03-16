import wei.yigulu.iec104.nettyconfig.Iec104HSMasterBuilder;


/**
 * 客户端和slave建立连接测试
 *
 * @author 修唯xiuwei
 * @create 2019-01-22 16:05
 * @Email 524710549@qq.com
 **/
public class ClientTest {

	public static void main(String[] args) {
		//TODO Master主入口 create  阻塞线程
		new Iec104HSMasterBuilder("127.0.0.1", 2404).create();
		System.out.println(123);
	}

}
