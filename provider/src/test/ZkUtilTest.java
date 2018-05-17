import org.apache.zookeeper.ZooKeeper;
import org.junit.Before;
import org.junit.Test;

import com.lanxiang.zk.practice.service.utils.ZkUtilCopy;
import com.lanxiang.zk.practice.service.warmup.ConnnectionWatcher;

/**
 * Created by lanxiang on 2018/5/17.
 */
public class ZkUtilTest {

    private static final String LOCAL_HOST = "127.0.0.1:2181";

    private ZooKeeper zk;

    @Before
    public void init() throws Exception {
        zk = new ZooKeeper(LOCAL_HOST, 5000, new ConnnectionWatcher());
    }

    @Test
    public void showAllNodes() throws Exception {
        System.out.println(ZkUtilCopy.listSubTreeBFS(zk, "/"));
    }

    @Test
    public void deleteAllNodes() throws Exception {
        ZkUtilCopy.deleteRecursive(zk, "/home");
    }
}
