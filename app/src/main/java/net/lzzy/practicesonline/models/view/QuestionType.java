package net.lzzy.practicesonline.models.view;

import androidx.annotation.NonNull;

/**
 * @author lzzy_gxy
 * @date 2019/4/16
 * Description:
 */
public enum QuestionType {
    /**
     * 题目类型
     */
    SINGLE_CHOICE("单项选择"), MULTI_CHOICE("多项选择"), JUDGE("判断");
    private String name;

    QuestionType(String name) {
        this.name = name;
    }


    @NonNull
    @Override
    public String toString() {
        return name;
    }

    public static QuestionType getInstance(int question) {
        for (QuestionType type : QuestionType.values()) {
            if (type.ordinal() == question) {
                return type;
            }
        }
        return null;
    }
}
