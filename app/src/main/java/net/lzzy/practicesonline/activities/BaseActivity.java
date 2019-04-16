package net.lzzy.practicesonline.activities;

import android.os.Bundle;
import android.view.Window;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.fragments.SplashFragment;
import net.lzzy.practicesonline.utils.AppUtils;

/**
 * @author lzzy_gxy
 * @date 2019/4/11
 * Description:
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(getLayoutRes());
        populate();
        splashFragment();

    }

    /**
     * 托管fragment代码
     */
    public void splashFragment() {
        AppUtils.addActivity(this);
        FragmentManager manager = getSupportFragmentManager();
        Fragment fragment = manager.findFragmentById(getContainerId());
        if (fragment == null) {
            fragment = createFragment();
            manager.beginTransaction().add(getContainerId(), fragment).commit();
        }
    }

    /**
     * 传视图组件
     *
     * @return
     */
    protected abstract int getLayoutRes();

    /**
     * 绑定视图
     */
    protected abstract void populate();

    /**
     * 托管fragment 视图
     * @return
     */
    protected abstract int getContainerId();

    /**
     * 生成托管fragment 对象
     * @return
     */
    protected abstract Fragment createFragment();

    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppUtils.removeActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.setRunningActivity(getLocalClassName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppUtils.setStopped(getLocalClassName());
    }

}
