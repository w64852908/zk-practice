import org.joda.time.DateTime;
import org.junit.Test;

import com.lanxiang.zk.practice.service.warmup.ConnnectionWatcher;
import com.lanxiang.zk.practice.service.warmup.CuratorConnection;

/**
 * Created by lanxiang on 2018/5/16.
 */
public class LocalTest {

    private static final String LOCAL_HOST = "127.0.0.1:2181";

    @Test
    public void testConnectZk() throws Exception {
        ConnnectionWatcher watcher = new ConnnectionWatcher();
        watcher.connection(LOCAL_HOST);
        Thread.sleep(3000);
        watcher.close();
    }

    @Test
    public void testConnectZkByCurator() throws Exception {
        CuratorConnection curator = new CuratorConnection();
        curator.connection(LOCAL_HOST);
        curator.createNode();
    }

    @Test
    public void test1() {
        int start = 20180320;
        int end = 20181220;
        int[] arr = new int[20181220 - 20180320];
        for (int i = start; i < end; i++) {
            arr[i - start] = i;
        }

        String format = "yyyyMMdd";
        DateTime today = new DateTime();

        while (true) {
            int index = Integer.valueOf(today.toString(format)) - start;
            System.out.println(arr[index]);
            today = today.plusDays(1);
        }
    }
}