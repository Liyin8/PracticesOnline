package net.lzzy.practicesonline.constants;

import net.lzzy.practicesonline.utils.AppUtils;

import java.net.URL;

/**
 *
 * @author lzzy_gxy
 * @date 2019/4/15
 * Description:
 */
public class ApiConstants {
    private static final String IP= AppUtils.loadServerSetting(AppUtils.getContext()).first;
    private static final String PORT=AppUtils.loadServerSetting(AppUtils.getContext()).second;
    private static final String PROTOCOL="http://";

    /**
     * 动态 API 地址
     */
    public static final String URL_API=PROTOCOL.concat(IP).concat(":").concat(PORT);
    /**
     * practices地址
     */
    public static final String ACTION_PRACTICES="/api/practices";
    public static final String URL_PRACTICES= URL_API.concat(ACTION_PRACTICES);
    /**
     * questions地址
     */
    public static final String ACTION_QUESTION="/api/pquestions?practiceid=";
    public static final String URL_QUESTION= URL_API.concat(ACTION_QUESTION);


    /**
     * practice的json标签
     */
    public static final String JSON_PRACTICES_API_ID="Id";
    public static final String JSON_PRACTICES_NAME="Name";
    public static final String JSON_PRACTICES_QUESTION_COUNT="QuestionCount";
    public static final String JSON_PRACTICES_OUTLINES="OutLines";

    /**
     * questions的json标签
     */
    public static final String JSON_QUESTION_ANALYSIS="Analysis";
    public static final String JSON_QUESTION_CONTENT="Content";
    public static final String JSON_QUESTION_TYPE="QuestionType";
    public static final String JSON_QUESTION_OPTIONS="Options";
    public static final String JSON_QUESTION_ANSWER="Answers";

    /**
     *option 的json标签
     */
    public static final String JSON_OPTION_CONTENT="Content";
    public static final String JSON_OPTION_LABEL="Label";
    public static final String JSON_OPTION_API_ID="Id";
    public static final String JSON_ANSWER_OPTION_ID="OptionId";

    /**
     * post方法的json标签
     */
    public static final String JSON_RESULT_API_ID="PracticeID";
    public static final String JSON_RESULT_SCORE_RATIO="ScroreRatio";
    public static final String JSON_RESULT_WRONG_IDS="WrongQuestionIds";
    public static final String JSON_RESULT_PERSON_INFO="PhoneNo";

    /**
     *提交地址
     */
    public static final String ACTION_RESULT="/api/result/practiceResult";
    public static final String URL_RESULT= URL_API.concat(ACTION_RESULT);
}
