
package cn.telltim.guardian.app.init;

import com.telltim.startup.AppBootUpTask;

import java.util.Collections;
import java.util.List;
public class AppInitTask extends AppBootUpTask {

    @Override
    public void run() {

    }

    /**
     * 返回值为空,表示不依赖其他启动任务
     * @return List<Class<? extends AppBootUpTask>>
     */
    @Override
    protected List<Class<? extends AppBootUpTask>> dependencies() {
        return Collections.emptyList();
    }

    @Override
    public String getTaskName() {
        return "AppInitTask";
    }
}
