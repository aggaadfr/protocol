package wei.yigulu.iec104;

import wei.yigulu.iec104.nettyconfig.Iec104SlaverBuilder;

import java.util.Random;

public class Main {

    public static void main(String[] args)throws Exception {
        testServer();
    }

    private static void testServer()throws Exception{
        Iec104SlaverBuilder slaverBuilder = new Iec104SlaverBuilder(2404);
		/*slaverBuilder.getConnectFilterManager().appendFilter((c) -> {
			if (slaverBuilder.getChannels().size() >= 1) {
				return -1;
			}
			InetSocketAddress ipSocket = (InetSocketAddress) c.remoteAddress();
			String clientIp = ipSocket.getAddress().getHostAddress();
			Integer clientPort = ipSocket.getPort();
			if (clientPort > 30000) {
				return -1;
			} else {
				return 1;
			}
		});*/
        slaverBuilder.create();
    }

}
