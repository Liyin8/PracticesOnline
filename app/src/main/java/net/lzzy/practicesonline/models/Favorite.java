package net.lzzy.practicesonline.models;

import net.lzzy.sqllib.Sqlitable;

import java.util.UUID;

/**
 *
 * @author lzzy_gxy
 * @date 2019/4/16
 * Description:
 */
public class Favorite extends BaseEntity implements Sqlitable {
    public static final String COL_QUESTION_ID="questionId";
    private UUID questionId;

    public UUID getQuestionId() {
        return questionId;
    }

    public void setQuestionId(UUID questionId) {
        this.questionId = questionId;
    }

    @Override
    public boolean needUpdate() {
        return false;
    }

}
