package net.lzzy.practicesonline.activities;

import androidx.fragment.app.Fragment;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.fragments.PracticesFragment;

/**
 *
 * @author lzzy_gxy
 * @date 2019/4/16
 * Description:
 */
public class PracticesActivity extends BaseActivity {

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_practies;
    }

    @Override
    protected void populate() {

    }

    @Override
    protected int getContainerId() {
        return R.id.activity_practices_container;
    }

    @Override
    protected Fragment createFragment() {
        return new PracticesFragment();
    }


}
