import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.lanxiang.zk.practice.AppBootStrap;


/**
 * Created by lanxiang on 2017/12/21.
 */

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {AppBootStrap.class})
@WebAppConfiguration
public abstract class AbstractTest {

}
