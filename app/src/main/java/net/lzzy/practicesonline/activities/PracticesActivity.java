package net.lzzy.practicesonline.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Window;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.fragments.PracticesFragment;
import net.lzzy.practicesonline.models.PracticeFactory;
import net.lzzy.practicesonline.network.DetectWebService;
import net.lzzy.practicesonline.utils.AppUtils;
import net.lzzy.practicesonline.utils.ViewUtils;

/**
 * @author lzzy_gxy
 * @date 2019/4/16
 * Description:
 */
public class PracticesActivity extends BaseActivity
        implements PracticesFragment.OnQuestionSelectedListener {

    public static final String EXTRA_LOCAL_COUNT = "extraLocalCount";
    private ServiceConnection connection;
    public static final String EXTRA_PRACTICE_ID = "practices_id";
    public static final String EXTRA_API_ID = "api_id";
    private boolean refresh;

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_practies;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        iniViews();
        if (getIntent() != null) {
            refresh = getIntent().getBooleanExtra(DetectWebService.EXTRA_REFRESH, false);
        }

        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                DetectWebService.DetectWebBinder binder = (DetectWebService.DetectWebBinder) service;
                binder.detect();
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        int localCount = PracticeFactory.getInstance().get().size();
        Intent intent = new Intent(this, DetectWebService.class);
        intent.putExtra(EXTRA_LOCAL_COUNT, localCount);
        bindService(intent, connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (refresh) {
            ((PracticesFragment)getFragment()).startRefresh();
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("确定退出应用？")
                .setPositiveButton("确认",(dialog, which) -> AppUtils.exit())
                .setNegativeButton("取消",null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
    }

    private void iniViews() {
        SearchView search = findViewById(R.id.bar_title_search);
        search.setQueryHint("输入关键词搜索");
        SearchView.SearchAutoComplete auto = search.findViewById(R.id.search_src_text);

        search.setOnQueryTextListener(new ViewUtils.AbstractQueryListener() {
            @Override
            public void handleQuery(String kw) {
                ((PracticesFragment) getFragment()).search(kw);
            }
        });

        auto.setHintTextColor(Color.WHITE);
        auto.setTextColor(Color.WHITE);
        ImageView icon = search.findViewById(R.id.search_button);
        ImageView icx = search.findViewById(R.id.search_close_btn);
        ImageView icg = search.findViewById(R.id.search_go_btn);
        icon.setColorFilter(Color.WHITE);
        icx.setColorFilter(Color.WHITE);
        icg.setColorFilter(Color.WHITE);
    }


    @Override
    protected int getContainerId() {
        return R.id.activity_practices_container;
    }

    @Override
    protected Fragment createFragment() {
        return new PracticesFragment();
    }


    @Override
    public void onQuestionSelected(String practiceId, int apiId) {
        Intent intent = new Intent(this, QuestionActivity.class);
        intent.putExtra(PracticesActivity.EXTRA_PRACTICE_ID, practiceId);
        intent.putExtra(PracticesActivity.EXTRA_API_ID, apiId);
        startActivity(intent);
    }
}
