package net.lzzy.practicesonline.network;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.PracticesActivity;
import net.lzzy.practicesonline.models.Practice;
import net.lzzy.practicesonline.models.PracticeFactory;
import net.lzzy.practicesonline.utils.AppUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.List;

/**
 * @author lzzy_gxy
 * @date 2019/4/28
 * Description:
 */
public class DetectWebService extends Service {
    public static final int NOTIFICATION_DETECT_ID=0;
    private int localcount;
    private NotificationManager manager;
    public static final String EXTRA_REFRESH = "extraRefresh";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        localcount = intent.getIntExtra(PracticesActivity.EXTRA_LOCAL_COUNT, 0);
        return new DetectWebBinder();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if (manager!=null){
            manager.cancel(NOTIFICATION_DETECT_ID);
        }
        return super.onUnbind(intent);

    }

    public class DetectWebBinder extends Binder {

        public static final int FLAG_SERVICE_EXCEPTION = 0;
        public static final int FLAG_DATA_CHANGED = 1;
        public static final int FLAG_DATE_SAME = 2;


        public void detect() {
            AppUtils.getExecutor().execute(() -> {
                int flag = compareData();
                if (flag == FLAG_SERVICE_EXCEPTION) {
                    notifyUser("服务器无法连接", android.R.drawable.ic_menu_compass, false);
                } else if (flag == FLAG_DATA_CHANGED) {
                    notifyUser("服务器有更新", android.R.drawable.ic_popup_sync, true);
                } else {
                    if (manager!=null){
                        manager.cancel(NOTIFICATION_DETECT_ID);
                    }
                }
            });
        }

        /**
         *
         * @param info
         * @param ic
         * @param refresh 区分从哪跳转过来
         */
        private void notifyUser(String info, int ic, boolean refresh) {
            Intent intent=new Intent(DetectWebService.this,PracticesActivity.class);

            intent.putExtra(EXTRA_REFRESH,refresh);
            PendingIntent pendingIntent=PendingIntent.getActivity(DetectWebService.this
            ,0,intent,PendingIntent.FLAG_ONE_SHOT);
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            Notification notification;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                notification = new Notification.Builder(DetectWebService.this,"0")
                        .setContentTitle("检测远程服务器")
                        .setContentText(info)
                        .setSmallIcon(ic)
                        .setContentIntent(pendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .build();
            }else {
                notification = new Notification.Builder(DetectWebService.this)
                        .setContentTitle("检测远程服务器")
                        .setContentText(info)
                        .setSmallIcon(ic)
                        .setContentIntent(pendingIntent)
                        .setWhen(System.currentTimeMillis())
                        .build();
            }
            if (manager!=null){
                manager.notify(NOTIFICATION_DETECT_ID,notification);
            }
        }


        private int compareData() {
            /**
             * 取远程数据跟本地数据对比 更新
             */
            try {
                List<Practice> remote = PracticeService
                        .getPractices(PracticeService.getPracticesFromServer());
                if (remote.size() != localcount) {
                    return FLAG_DATA_CHANGED;
                } else {
                    return FLAG_DATE_SAME;
                }
            } catch (IllegalAccessException | JSONException | InstantiationException | IOException e) {
                return FLAG_SERVICE_EXCEPTION;
            }
        }
    }

}
