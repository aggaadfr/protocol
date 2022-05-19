package wei.yigulu.iec104.util;


import io.netty.channel.Channel;
import org.slf4j.Logger;
import wei.yigulu.iec104.apdumodel.Apdu;
import wei.yigulu.iec104.apdumodel.Asdu;
import wei.yigulu.iec104.asdudataframe.BooleanType;
import wei.yigulu.iec104.asdudataframe.CustomElectroplateSummonType;
import wei.yigulu.iec104.asdudataframe.ShortFloatType;
import wei.yigulu.iec104.asdudataframe.TotalSummonType;
import wei.yigulu.iec104.asdudataframe.typemodel.InformationBodyAddress;
import wei.yigulu.iec104.bean.MutationArgs;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

/**
 * 发送数据帧的工具类
 *
 * @author: xiuwei
 * @version:
 */
public class SendDataFrameHelper {

    /**
     * TODO 单帧最长连续遥信个数
     */
    public static final int MAXCONTINUITYYXNUM = 127;

    /**
     * 单帧最长单点遥信个数
     */
    public static final int MAXDISCONTINUITYYXNUM = 49;

    /**
     * 单帧最长连续遥测个数
     */
    public static final int MAXCONTINUITYYCNUM = 45;

    /**
     * 单帧最长单点遥测个数
     */
    public static final int MAXDISCONTINUITYYCNUM = 25;


    /**
     * 发送遥信数据帧 数据连续发送
     *
     * @param channel 通达对象
     * @param dates   要发送的数据
     * @param address 公共地址位，子站端保持和主站端一致即可
     * @param cause   发送原因
     * @throws Exception 异常信息
     */
    public static void sendYxDataFrame(Channel channel, Map<Integer, Boolean> dates, Integer address, Integer cause, Logger log) throws Exception {
//		System.out.println("sendYxDataFrame========================");
//		System.out.println("sendYxDataFrame========================1：" + JSON.toJSONString(channel));
//		System.out.println("sendYxDataFrame========================2：" + JSON.toJSONString(dates));
//		address = 5;
//		System.out.println("sendYxDataFrame========================3：" + address);
//		System.out.println("sendYxDataFrame========================4：" + cause);
//		System.out.println("sendYxDataFrame========================5：" + JSON.toJSONString(log));

        BooleanType booleanType;
        Apdu apdu;
        Asdu asdu;
        Set<Integer> keys;
        Integer max;
        Integer min;
        if (dates.size() > 0) {
            keys = dates.keySet();
            max = Collections.max(keys);  //最带地址
            min = Collections.min(keys);  //最小地址
            if ((max - min) == (keys.size() - 1)) {
                // 如果数据大于了127，会把数据切分成多个list
                for (List<Integer> li : splitAndSort(keys, MAXCONTINUITYYXNUM)) {
                    booleanType = new BooleanType();
                    // 添加信息体地址
                    booleanType.addAddress(new InformationBodyAddress(li.get(0)));

                    // 添加信息数据
                    for (Integer i : li) {
                        booleanType.addData(dates.get(i));
                    }
                    apdu = new Apdu();
                    asdu = booleanType.generateBack();

                    asdu.setNot(cause);
                    asdu.setCommonAddress(address);

                    apdu.setAsdu(asdu);
                    SendAndReceiveNumUtil.sendIFrame(apdu, channel, log);
                    Thread.sleep(20);
                }
            } else {
                // 信息体不连续的处理方法
                for (Map<Integer, Boolean> m : split(dates, MAXDISCONTINUITYYXNUM)) {
                    booleanType = new BooleanType();
                    for (Map.Entry<Integer, Boolean> em : m.entrySet()) {
                        // 一个信息地址，一条数据
                        booleanType.addDataAndAdd(new InformationBodyAddress(em.getKey()), em.getValue());
                    }
                    apdu = new Apdu();
                    asdu = booleanType.generateBack();
                    asdu.setNot(cause);
                    asdu.setCommonAddress(address);
                    apdu.setAsdu(asdu);
                    SendAndReceiveNumUtil.sendIFrame(apdu, channel, log);
                    Thread.sleep(20);
                }
            }
        }
    }


    /**
     * 发送遥信数据帧 不连续
     *
     * @param channel 通达对象
     * @param dates   要发送的数据
     * @param address 公共地址位，子站端保持和主站端一致即可
     * @param cause   发送原因
     * @throws Exception 异常信息
     */
    public static void sendYxDataFrameDiscontinuity(Channel channel, Map<Integer, Boolean> dates, Integer address, Integer cause, Logger log) throws Exception {
        BooleanType booleanType;
        Apdu apdu;
        Asdu asdu;
        if (dates.size() > 0) {
            for (Map<Integer, Boolean> m : split(dates, MAXDISCONTINUITYYXNUM)) {
                booleanType = new BooleanType();
                for (Map.Entry<Integer, Boolean> em : m.entrySet()) {
                    booleanType.addDataAndAdd(new InformationBodyAddress(em.getKey()), em.getValue());
                }
                apdu = new Apdu();
                asdu = booleanType.generateBack();
                asdu.setNot(cause);
                asdu.setCommonAddress(address);
                apdu.setAsdu(asdu);
                SendAndReceiveNumUtil.sendIFrame(apdu, channel, log);
                Thread.sleep(20);
            }
        }
    }

    /**
     * 发送遥测 数据帧
     *
     * @param channel 通道对象
     * @param dates   需要发送的数据
     * @param address 公共地址位，子站端保持和主站端一致即可
     * @param cause   发送的原因
     * @throws Exception 异常
     */
    public static void sendYcDataFrame(Channel channel, Map<Integer, Number> dates, Integer address, Integer cause, Logger log) throws Exception {
        Apdu apdu;
        Asdu asdu;
        ShortFloatType shortFloatType;
        Set<Integer> keys;
        Integer max;
        Integer min;
        if (dates.size() > 0) {
            keys = dates.keySet();
            max = Collections.max(keys);
            min = Collections.min(keys);
            if ((max - min) == (keys.size() - 1)) {
                for (List<Integer> li : splitAndSort(keys, MAXCONTINUITYYCNUM)) {
                    shortFloatType = new ShortFloatType();
                    shortFloatType.addAddress(new InformationBodyAddress(li.get(0)));
                    for (Integer i : li) {
                        shortFloatType.addData(dates.get(i).floatValue());
                    }
                    apdu = new Apdu();
                    asdu = shortFloatType.generateBack();
                    asdu.setNot(cause);
                    asdu.setCommonAddress(address);
                    apdu.setAsdu(asdu);

                    SendAndReceiveNumUtil.sendIFrame(apdu, channel, log);
                    Thread.sleep(20);
                }
            } else {
                for (Map<Integer, Number> m : split(dates, MAXDISCONTINUITYYCNUM)) {
                    shortFloatType = new ShortFloatType();
                    for (Map.Entry<Integer, Number> em : m.entrySet()) {
                        shortFloatType.addDataAndAdd(new InformationBodyAddress(em.getKey()), em.getValue().floatValue());
                    }
                    apdu = new Apdu();
                    asdu = shortFloatType.generateBack();
                    asdu.setNot(cause);
                    asdu.setCommonAddress(address);
                    apdu.setAsdu(asdu);
                    SendAndReceiveNumUtil.sendIFrame(apdu, channel, log);
                    Thread.sleep(20);
                }
            }
        }
    }


    /**
     * 发送电镀信号
     *
     * @param channel       通道对象
     * @param dates         需要发送的数据
     * @param address       公共地址位，子站端保持和主站端一致即可
     * @param cause         发送的原因
     * @param log
     * @param discontinuity 是否连续
     */
    public static void sendDdDataFrame(Channel channel,
                                       Map<Integer, Number> dates,
                                       Integer address,
                                       Integer cause,
                                       Logger log,
                                       boolean discontinuity) throws Exception {

        Apdu apdu;
        Asdu asdu;
        ShortFloatType shortFloatType;
        Set<Integer> keys;
        Integer max;
        Integer min;
        if (discontinuity) {
            keys = dates.keySet();
            max = Collections.max(keys);
            min = Collections.min(keys);
            if ((max - min) == (keys.size() - 1)) {
                for (List<Integer> li : splitAndSort(keys, MAXCONTINUITYYCNUM)) {
                    shortFloatType = new ShortFloatType();
                    shortFloatType.addAddress(new InformationBodyAddress(li.get(0)));
                    for (Integer i : li) {
                        shortFloatType.addData(dates.get(i).floatValue());
                    }
                    apdu = new Apdu();
                    asdu = shortFloatType.generateBack();
                    asdu.setNot(cause);
                    asdu.setCommonAddress(address);
                    apdu.setAsdu(asdu);

                    SendAndReceiveNumUtil.sendIFrame(apdu, channel, log);
                    Thread.sleep(20);
                }
            } else {
                for (Map<Integer, Number> m : split(dates, MAXDISCONTINUITYYCNUM)) {
                    shortFloatType = new ShortFloatType();
                    for (Map.Entry<Integer, Number> em : m.entrySet()) {
                        shortFloatType.addDataAndAdd(new InformationBodyAddress(em.getKey()), em.getValue().floatValue());
                    }
                    apdu = new Apdu();
                    asdu = shortFloatType.generateBack();
                    asdu.setNot(cause);
                    asdu.setCommonAddress(address);
                    apdu.setAsdu(asdu);
                    SendAndReceiveNumUtil.sendIFrame(apdu, channel, log);
                    Thread.sleep(20);
                }
            }
        }


    }


    /**
     * 发送遥测 数据帧 不连续
     *
     * @param channel 通道对象
     * @param dates   需要发送的数据
     * @param address 公共地址位，子站端保持和主站端一致即可
     * @param cause   发送的原因
     * @throws Exception 异常
     */
    public static void sendYcDataFrameDiscontinuity(Channel channel, Map<Integer, Number> dates, Integer address, Integer cause, Logger log) throws Exception {
        Apdu apdu;
        Asdu asdu;
        ShortFloatType shortFloatType;
        if (dates.size() > 0) {
            for (Map<Integer, Number> m : split(dates, MAXDISCONTINUITYYCNUM)) {
                shortFloatType = new ShortFloatType();
                for (Map.Entry<Integer, Number> em : m.entrySet()) {
                    shortFloatType.addDataAndAdd(new InformationBodyAddress(em.getKey()), em.getValue().floatValue());
                }
                apdu = new Apdu();
                asdu = shortFloatType.generateBack();
                asdu.setNot(cause);
                asdu.setCommonAddress(address);
                apdu.setAsdu(asdu);
                SendAndReceiveNumUtil.sendIFrame(apdu, channel, log);
                Thread.sleep(20);
            }
        }
    }

    /**
     * 总召激活确认帧
     *
     * @param channel 通道对象
     * @param address 公共地址位，子站端保持和主站端一致即可
     * @param cause   发送的原因
     * @throws Exception 异常
     */
    public static void sendTotalSummonFrame(Channel channel, Integer address, Integer cause, Logger log) throws Exception {
        Apdu apdu = new Apdu();
        Asdu asdu;
        TotalSummonType dataFrameType = new TotalSummonType();
        dataFrameType.setAddress(new InformationBodyAddress(0));
        // 总召 I帧 的最后一位消息体   十六进制 14
        dataFrameType.setValue(20);
        asdu = dataFrameType.generateBack();
        asdu.setNot(cause);
        asdu.setCommonAddress(address);
        apdu.setAsdu(asdu);
        SendAndReceiveNumUtil.sendIFrame(apdu, channel, log);
    }


    /**
     * 发送电镀确认帧
     *
     * @param channel 通道对象
     * @param address 公共地址位，子站端保持和主站端一致即可
     * @param cause   发送的原因
     * @param log
     * @throws Exception
     */
    public static void sendElectroplateFrame(Channel channel, Integer address, Integer cause, Logger log) throws Exception {
        Apdu apdu = new Apdu();
        Asdu asdu;
        CustomElectroplateSummonType dataFrameType = new CustomElectroplateSummonType();
        dataFrameType.setAddress(new InformationBodyAddress(0));
        // // 总召 I帧 的最后一位消息体   十六进制 45
        dataFrameType.setValue(69);
        asdu = dataFrameType.generateBack();
        asdu.setNot(cause);
        asdu.setCommonAddress(address);
        apdu.setAsdu(asdu);
        SendAndReceiveNumUtil.sendIFrame(apdu, channel, log);

    }


    /**
     * 将发送的数据集合拆成n个长度合适的集合
     *
     * @param map    总集合
     * @param maxLen 设定最大的长度
     * @param <T>    类型T
     * @return 分集合
     */
    public static <T> List<HashMap<Integer, T>> split(Map<Integer, T> map, int maxLen) {
        List<HashMap<Integer, T>> list = new ArrayList<>();
        HashMap transfer = new HashMap(maxLen);
        int j = 0;
        for (Integer o : map.keySet()) {
            if (j < maxLen) {
                transfer.put(o, map.get(o));
                j++;
            } else {
                list.add(transfer);
                transfer = new HashMap(maxLen);
                transfer.put(o, map.get(o));
                j = 1;
            }
        }
        list.add(transfer);
        return list;
    }

    /**
     * 进行拆分并排序 主要是排序map 中的keyset 对keyset的int型进行排序
     * 如果一条数据大于了127，则会把数据进行切分成多个list
     *
     * @param set    要排序的set
     * @param maxLen 设定的最长长度
     * @return 拆分后的集合 的集合
     */
    public static List<List<Integer>> splitAndSort(Set<Integer> set, int maxLen) {
        List<List<Integer>> list = new ArrayList<>();
        List<Integer> ls = new ArrayList<>();
        List<Integer> transfer = new ArrayList<>(maxLen);
        for (Integer i : set) {
            ls.add(i);
        }
        Collections.sort(ls);
        int j = 0;
        for (Integer i : ls) {
            if (j < maxLen) {
                transfer.add(i);
                j++;
            } else {
                list.add(transfer);
                transfer = new ArrayList<>(maxLen);
                transfer.add(i);
                j = 1;
            }
        }
        list.add(transfer);
        return list;
    }

    /**
     * 发送遥信、遥测突变数据
     *
     * @param executorService
     * @param apdu
     * @param channel
     * @param commonAddress
     * @param rand
     */
    public static void sendYxYcMutationFrame(ScheduledExecutorService executorService,
                                             Apdu apdu,
                                             Channel channel,
                                             int commonAddress,
                                             Integer cause,
                                             Random rand,
                                             MutationArgs mutationArgs) {
        Logger log = apdu.getLog();

        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                int max = rand.nextInt(10);

                //do something
                try {
                    HashMap<Integer, Boolean> yxDatas = new HashMap<>();
                    for (int i = 0; i < max; i++) {
                        yxDatas.put(i + 102, rand.nextBoolean());
                    }
                    //发送遥信数据帧
                    SendDataFrameHelper.sendYxDataFrame(channel, yxDatas, commonAddress, cause, log);

                    Thread.sleep(mutationArgs.getSleepTime());
                } catch (Exception e) {
                    log.error("响应总召，发送YX数据失败！");
                }


                try {
                    HashMap<Integer, Number> ycDatas = new HashMap<>();
                    for (int i = 0; i < max; i++) {
                        ycDatas.put(16485 + i, rand.nextInt(100));
                    }
                    //发送遥测 数据帧
                    SendDataFrameHelper.sendYcDataFrame(channel, ycDatas, commonAddress, cause, log);
                } catch (Exception e) {
                    log.error("响应总召，发送YC数据失败！");
                }

            }
        }, mutationArgs.getInitialDelay(), mutationArgs.getPeriodv(), mutationArgs.getUnit());

    }

    /**
     * @param executorService
     * @param apdu
     * @param channel
     * @param commonAddress
     * @param cause
     * @param rand
     * @param mutationArgs
     */
    public static void sendDdMutationFrame(ScheduledExecutorService executorService,
                                           Apdu apdu,
                                           Channel channel,
                                           int commonAddress,
                                           Integer cause,
                                           Random rand,
                                           MutationArgs mutationArgs) {
        Logger log = apdu.getLog();

        executorService.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                int max = rand.nextInt(10);
                //do something
                try {
                    HashMap<Integer, Number> ddDatas = new HashMap<>();
                    for (int i = 0; i < max; i++) {
                        ddDatas.put(16785 + i, rand.nextFloat() + 10);
                    }
                    SendDataFrameHelper.sendDdDataFrame(channel, ddDatas, commonAddress, cause, log, true);
                } catch (Exception e) {
                    log.error("响应总召，发送Dd数据失败！");
                }
            }
        }, mutationArgs.getInitialDelay(), mutationArgs.getPeriodv(), mutationArgs.getUnit());

    }


}
