package net.lzzy.practicesonline.models.view;

import androidx.annotation.NonNull;

/**
 *
 * @author lzzy_gxy
 * @date 2019/5/8
 * Description:
 */
public enum WrongType {
    /**
     * 结果
     */
    RIGHT_OPTIONS("正确"),MISS_OPTIONS("少选"),EXTRA_OPTIONS("多选"),WRONG_OPTIONS("错选");
    private String name;

    WrongType(String name) {
        this.name=name;
    }
    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public static WrongType getInstance(int question) {
        for (WrongType type : WrongType.values()) {
            if (type.ordinal() == question) {
                return type;
            }
        }
        return null;
    }

}
