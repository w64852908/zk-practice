import java.lang.reflect.Field;

import org.junit.Test;

import com.lanxiang.zk.practice.service.executetest.zkconfig.LanxiangConfig;

/**
 * Created by lanxiang on 2018/5/27.
 */
public class NormalTest {

    @Test
    public void test1() throws Exception {
        LanxiangConfig config = new LanxiangConfig();

        User user = new User();
        user.setAge(10);
        user.setName("lanxiang");

        Field[] fields = user.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
//            System.out.println(field.get(user));
        }
    }


}

class User {

    private String name;

    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
