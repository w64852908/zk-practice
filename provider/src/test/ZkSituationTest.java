import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.junit.Before;
import org.junit.Test;

import com.lanxiang.zk.practice.service.common.ZkConnection;
import com.lanxiang.zk.practice.service.executetest.zkconfig.LanxiangConfig;

/**
 * Created by lanxiang on 2018/5/27.
 */
public class ZkSituationTest extends AbstractTest {

    @Before
    public void init() throws Exception {
        CuratorFramework curator = new ZkConnection("127.0.0.1:2181", "com.lanxiang.zk.practice").connect();
        curator.start();
        curator.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath("/lanxiangZkPractice/lanxiangZkConfig/lanxiang.name");
        curator.create().creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath("/lanxiangZkPractice/lanxiangZkConfig/lanxiang.age");
        curator.setData().forPath("/lanxiangZkPractice/lanxiangZkConfig/lanxiang.name", "lanxiang".getBytes());
        curator.setData().forPath("/lanxiangZkPractice/lanxiangZkConfig/lanxiang.age", "25".getBytes());
    }

    @Test
    public void testZkcc() {
        System.out.println(LanxiangConfig.getName());
        System.out.println(LanxiangConfig.getAge());
    }

}
