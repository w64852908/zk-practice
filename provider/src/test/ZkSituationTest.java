import java.util.concurrent.TimeUnit;

import com.lanxiang.zk.practice.service.annotation.ZkConfig;
import com.lanxiang.zk.practice.service.core.zkcc.ZkConfigClient;
import org.apache.curator.framework.CuratorFramework;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.lanxiang.zk.practice.service.common.ZkConnection;
import com.lanxiang.zk.practice.service.executetest.zkconfig.LanxiangConfig;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Resource;

/**
 * Created by lanxiang on 2018/5/27.
 */
public class ZkSituationTest extends AbstractTest implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    private CuratorFramework curator = new ZkConnection("127.0.0.1:2181", "com.lanxiang.zk.practice").connect();

    @Before
    public void init() throws Exception {
        curator.start();

        curator.setData().forPath("/lanxiangZkPractice/lanxiangZkConfig/lanxiang.name", "lanxiang".getBytes());
        curator.setData().forPath("/lanxiangZkPractice/lanxiangZkConfig/lanxiang.age", "25".getBytes());
    }

    @After
    public void destroy() throws Exception {
        TimeUnit.SECONDS.sleep(15);
        ZkUtilTest zkUtilTest = new ZkUtilTest();
        zkUtilTest.init();
        zkUtilTest.showAllNodes();
    }

    /**
     * 测试zk配置中心逻辑
     */
    @Test
    public void testZkcc() throws Exception {
        //主线程sleep，等待NodeCacheListener和IConfigChangeListener更新
        Thread.sleep(1000L);
        System.out.println(LanxiangConfig.getName());
        System.out.println(LanxiangConfig.getAge());
        updateNameAndAge("liju", "24");
        Thread.sleep(1000L);
        System.out.println(LanxiangConfig.getName());
        System.out.println(LanxiangConfig.getAge());
        updateNameAndAge("bamei", "25");
        Thread.sleep(1000L);
        System.out.println(LanxiangConfig.getName());
        System.out.println(LanxiangConfig.getAge());

        ZkConfigClient zkConfigClient = applicationContext.getBean("zkConfigClient", ZkConfigClient.class);

        zkConfigClient.setValue(LanxiangConfig.class.getDeclaredField("name").getAnnotation(ZkConfig.class).key(), "兰翔啦啦啦");
        Thread.sleep(1000L);
        System.out.println(LanxiangConfig.getName());

    }

    private void updateNameAndAge(String name, String age) throws Exception {
        curator.setData().forPath("/lanxiangZkPractice/lanxiangZkConfig/lanxiang.name", name.getBytes());
        curator.setData().forPath("/lanxiangZkPractice/lanxiangZkConfig/lanxiang.age", age.getBytes());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
