package net.lzzy.practicesonline.models;

import android.content.Context;
import android.content.SharedPreferences;

import net.lzzy.practicesonline.utils.AppUtils;
import net.lzzy.practicesonline.utils.DateTimeUtils;

import java.util.Date;

/**
 *
 * @author lzzy_gxy
 * @date 2019/4/17
 * Description:
 */
public class UserCookies {
    public static final String KEY_TIME = "keyTime";
    /**
     * 记录时间
     */
    private SharedPreferences spTime;

    private static final UserCookies INSTANCE=new UserCookies();
    private UserCookies(){
        spTime= AppUtils.getContext().getSharedPreferences("refresh_time", Context.MODE_PRIVATE);

    }

    public static UserCookies getInstants(){
        return INSTANCE;
    }

    public void updateLastRefreshTime(){
        String time= DateTimeUtils.DATE_TIME_FORMAT.format(new Date());
        spTime.edit().putString(KEY_TIME,time).apply();
    }

    public String getLastRefreshTime(){
     return spTime.getString(KEY_TIME,"");
    }

}
