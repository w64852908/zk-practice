import org.junit.Test;

import com.lanxiang.zk.practice.service.warmup.ConnnectionWatcher;

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
}