import wei.yigulu.iec104.nettyconfig.Iec104MasterBuilder;
import wei.yigulu.iec104.util.SendCommandHelper;

import java.util.Random;

/**
 * @author: xiuwei
 * @version:
 */
public class SendCommand {

	public static void main(String[] args) throws Exception {
		sendAp();
	}

	public static void sendAp() throws Exception {
		Random random = new Random();
		Iec104MasterBuilder builder = new Iec104MasterBuilder("127.0.0.1", 2404);
		builder.createByUnBlock();
		while (true) {
			Thread.sleep(3000L);
			try {
				SendCommandHelper.sendShortCommand(builder, 0, 0, 1, (float) (40 + 10 * random.nextDouble()));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


}
