package net.lzzy.practicesonline.constants;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.utils.AppUtils;
import net.lzzy.sqllib.DbPackager;

/**
 * Created by lzzy_gxy on 2019/4/17.
 * Description:
 */
public class DbConstents {
    private static final String DB_NAME="practice.db";
    private static final int DB_VERSION=1;
    public static DbPackager packager
            =DbPackager.getInstance(
                    AppUtils.getContext(),DB_NAME,DB_VERSION, R.raw.models);
}
