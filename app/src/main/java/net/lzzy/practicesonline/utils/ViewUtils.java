package net.lzzy.practicesonline.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.SplashActivity;

/**
 *
 * @author lzzy_gxy
 * @date 2019/4/15
 * Description:
 */
public class ViewUtils {
    public static void gotoSetting(Context context){
        View view= LayoutInflater.from(context).inflate(R.layout.dialog_setting,null);
        Pair<String,String> url=AppUtils.loadServerSetting(context);
        EditText edIp=view.findViewById(R.id.dialog_setting_edt_ip);
        edIp.setText(url.first);
        EditText edPort=view.findViewById(R.id.dialog_setting_edt_port);
        edPort.setText(url.second);
        new AlertDialog.Builder(context)
                .setMessage("设置网络ip")
                .setView(view)
                .setNeutralButton("取消",(dialog, which) ->gotoMain(context))
                .setPositiveButton("确定",(dialog, which) ->{
                    String ip=edIp.getText().toString();
                    String port=edPort.getText().toString();
                    if (TextUtils.isEmpty(ip)||TextUtils.isEmpty(port)){
                        Toast.makeText(context,"地址为空",Toast.LENGTH_LONG).show();
                        return;
                    }
                    AppUtils.saveServerSetting(ip,port,context);
                    gotoMain(context);
                } )
                .show();
    }

    private static void gotoMain(Context context) {
        if (context instanceof SplashActivity){
            ((SplashActivity)context).gotoMain();
        }
    }
}
