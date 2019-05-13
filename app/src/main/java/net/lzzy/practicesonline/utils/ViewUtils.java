package net.lzzy.practicesonline.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.SplashActivity;

/**
 * @author lzzy_gxy
 * @date 2019/4/15
 * Description:
 */
public class ViewUtils {

    private static AlertDialog dialog;

    public static void showProgress(Context context,String message){
        if (dialog==null){
            View view=LayoutInflater.from(context).inflate(R.layout.dialog_progress,null);
            TextView tv=view.findViewById(R.id.dialog_progress_tv);
            tv.setText(message);
            dialog=new AlertDialog.Builder(context).create();
            dialog.setView(view);
        }
        dialog.show();
    }

    public static void disminssProgress(){
        if (dialog!=null&&dialog.isShowing()){
            dialog.dismiss();
        }
    }
    //region px dp 转换方法

    public static int px2dp(int pxValue, Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static int dp2px(int dpValue, Context context) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
    //endregion

    public static void gotoSetting(Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_setting, null);
        Pair<String, String> url = AppUtils.loadServerSetting(context);
        EditText edIp = view.findViewById(R.id.dialog_setting_edt_ip);
        edIp.setText(url.first);
        EditText edPort = view.findViewById(R.id.dialog_setting_edt_port);
        edPort.setText(url.second);
        new AlertDialog.Builder(context)
                .setMessage("设置网络ip")
                .setView(view)
                .setNeutralButton("取消", (dialog, which) -> gotoMain(context))
                .setPositiveButton("确定", (dialog, which) -> {
                    String ip = edIp.getText().toString();
                    String port = edPort.getText().toString();
                    if (TextUtils.isEmpty(ip) || TextUtils.isEmpty(port)) {
                        Toast.makeText(context, "地址为空", Toast.LENGTH_LONG).show();
                        return;
                    }
                    AppUtils.saveServerSetting(ip, port, context);
                    gotoMain(context);
                })
                .show();
    }

    private static void gotoMain(Context context) {
        if (context instanceof SplashActivity) {
            ((SplashActivity) context).gotoMain();
        }
    }

    public abstract static class AbstractQueryListener implements
            SearchView.OnQueryTextListener{
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            handleQuery(newText);
            return true;
        }

        /**
         * 处理搜索逻辑
         * @param kw 搜索关键词
         */
        public abstract void handleQuery(String kw);
    }

}
