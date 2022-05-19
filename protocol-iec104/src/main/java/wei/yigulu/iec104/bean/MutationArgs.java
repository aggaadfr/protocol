package wei.yigulu.iec104.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.TimeUnit;

/**
 * 突变参数设置工具类
 * <p>
 * Project: protocol
 * Package: wei.yigulu.iec104.bean
 * Version: 1.0
 * <p>
 * Created by WJX on 2022/5/18 15:46
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MutationArgs {
    private Long initialDelay = 10000L;
    private Long periodv = 30000L;
    private TimeUnit unit = TimeUnit.MILLISECONDS;
    private Long sleepTime = 10000L;
}
