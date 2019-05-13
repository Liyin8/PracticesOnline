package net.lzzy.practicesonline.models.view;



import net.lzzy.practicesonline.constants.ApiConstants;
import net.lzzy.practicesonline.models.UserCookies;

import org.json.JSONException;
import org.json.JSONObject;


import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by lzzy_gxy on 2019/5/8.
 * Description:
 */
public class PracticeResult {

    public static final String STRING = ",";
    private List<QuestionResult> results;
    private int id;
    private String info;


    public PracticeResult(List<QuestionResult> results, int id, String info) {
        this.results = results;
        this.id = id;
        this.info = info;
    }


    public List<QuestionResult> getResults() {
        return results;
    }

    public int getId() {
        return id;
    }

    public String getInfo() {
        return info;
    }

    /**
     * 算分数
     *
     * @return
     */
    private double getRatio() {
        double score = 0;
        for (int i = 0; i < results.size(); i++) {
            if (results.get(i).isRight()) {
                score = score + (1.00 / results.size());
            }
        }
        return score * 1.0;

    }

    /**
     * 错误题的序号
     */
    private String getWrongOrders() {
        int i = 0;
        String ids = "";
        for (QuestionResult result : results) {
            i++;
            if (!result.isRight()) {
                ids = ids.concat(i + STRING);
            }
        }
        if (ids.endsWith(STRING)) {
            ids = ids.substring(0, ids.length() - 1);
        }
        return ids;
    }

    /**
     * 转换字符串
     */
    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put(ApiConstants.JSON_RESULT_API_ID, id);
        json.put(ApiConstants.JSON_RESULT_SCORE_RATIO, new DecimalFormat("#.00").format(getRatio()));
        json.put(ApiConstants.JSON_RESULT_WRONG_IDS, getWrongOrders());
        json.put(ApiConstants.JSON_RESULT_PERSON_INFO, info);
        return json;
    }

}
