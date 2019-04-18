package net.lzzy.practicesonline.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.constants.ApiConstants;
import net.lzzy.practicesonline.fragments.SplashFragment;
import net.lzzy.practicesonline.utils.AbstractStaticHandler;
import net.lzzy.practicesonline.utils.AppUtils;
import net.lzzy.practicesonline.utils.ViewUtils;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Administrator
 */
public class SplashActivity extends BaseActivity
        implements SplashFragment.OnSplashFinishedListener {
    public static final int WHAT_COUNTING = 0;
    public static final int WHAT_EXCEPTION = 1;
    public static final int WHAT_COUNT_DOWN = 2;
    public static final int WHAT_SERVER_OFF = 3;
    private int seconds = 20;
    private SplashHandler handler = new SplashHandler(this);
    private TextView tvDate;
    private boolean isServeron=true;

    private static class SplashHandler extends AbstractStaticHandler<SplashActivity> {

        protected SplashHandler(SplashActivity context) {
            super(context);
        }

        @Override
        public void handleMessage(Message msg, SplashActivity splashActivity) {
            switch (msg.what) {
                case WHAT_COUNTING:
                    String display = msg.obj + "秒";
                    splashActivity.tvDate.setText(display);
                    break;
                case WHAT_COUNT_DOWN:
                    if (splashActivity.isServeron){
                        splashActivity.gotoMain();
                    }
                    break;

                //无网络对话框
                case WHAT_EXCEPTION:
                    new AlertDialog.Builder(splashActivity)
                            .setMessage(msg.obj.toString())
                            .setNegativeButton("退出", (dialog, which) -> AppUtils.exit())
                            .setPositiveButton("继续", (dialog, which) -> splashActivity.gotoMain())
                            .show();
                    break;
                    //服务器未响应对话框
                case WHAT_SERVER_OFF:
                    Activity context = AppUtils.getRuningActivity();
                    new AlertDialog.Builder(context)
                            .setMessage("服务器未响应")
                            .setPositiveButton("确定", (dialog, which) -> {
                                if (context instanceof SplashActivity) {
                                    ((SplashActivity) context).gotoMain();
                                }
                            })
                            .setNegativeButton("退出", (dialog, which) -> AppUtils.exit())
                            .setNeutralButton("设置网络", (dialog, which) -> ViewUtils.gotoSetting(context))
                            .show();
                default:
                    break;
            }
        }
    }
//region 检测网络

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvDate = findViewById(R.id.activity_splash_tv_count_down);
        tvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                seconds=0;
            }
        });
        if (!AppUtils.isNetworkAvailable()) {
            new AlertDialog.Builder(this)
                    .setMessage("当前无网络")
                    .setNegativeButton("退出", (dialog, which) -> {
                        AppUtils.exit();
                    })
                    .setPositiveButton("继续", (dialog, which) -> {
                        gotoMain();
                    })
                    .show();
        } else {
            ThreadPoolExecutor executor = AppUtils.getExecutor();
            executor.execute(this::countDown);
            executor.execute(this::detectServerStatus);
        }

    }
//endregion

    private void countDown() {
        while (seconds >= 0) {
            handler.sendMessage(handler.obtainMessage(WHAT_COUNTING, seconds));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                handler.sendMessage(handler.obtainMessage(WHAT_EXCEPTION, e.getMessage()));
            }
            seconds--;
        }
        handler.sendEmptyMessage(WHAT_COUNT_DOWN);
    }
    //检测服务器

    private void detectServerStatus() {
        try {
            AppUtils.tryConnectService(ApiConstants.URL_API);
        } catch (IOException e) {
            isServeron=false;
            handler.sendMessage(handler.obtainMessage(WHAT_SERVER_OFF, e.getMessage()));
        }
    }


    @Override
    protected void populate() {

    }

    @Override
    protected int getContainerId() {
        return R.id.fragment_splash_container;
    }

    @Override
    protected Fragment createFragment() {
        return new SplashFragment();
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.activity_splash;
    }


    @Override
    public void cancelCount() {

    }
    // region 退出时提问

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("确定退出吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", (dialog, which) -> AppUtils.exit())
                .show();
    }
//endregion


    public void gotoMain() {
        startActivity(new Intent(this,PracticesActivity.class));
        finish();
    }


}