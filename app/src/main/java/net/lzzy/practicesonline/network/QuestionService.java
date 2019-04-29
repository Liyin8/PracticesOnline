package net.lzzy.practicesonline.network;

import net.lzzy.practicesonline.constants.ApiConstants;
import net.lzzy.practicesonline.models.Option;
import net.lzzy.practicesonline.models.Question;
import net.lzzy.sqllib.JsonConverter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 *
 * @author lzzy_gxy
 * @date 2019/4/22
 * Description:
 */
public class QuestionService {
    public static String getQuestionsOfPracticeFromServer(int apiId) throws IOException {
        String address = ApiConstants.URL_QUESTION + apiId;
        return ApiService.okGet(address);
    }

    public static List<Question> getQuestions(String json, UUID practiceId) throws IllegalAccessException, JSONException, InstantiationException {
        JsonConverter<Question> converter=new JsonConverter<>(Question.class);
        List<Question> questions=converter.getArray(json);
        for (Question question:questions){
            question.setPracticeId(practiceId);
        }
        return questions;
    }

    public static List<Option> getOptionFromJson(String json,String jsonAnswers)throws IllegalAccessException, JSONException, InstantiationException{
        JsonConverter<Option> converter=new JsonConverter<>(Option.class);
        List<Option> options=converter.getArray(json);
        List<Integer> answerIds=new ArrayList<>();
        JSONArray array = (JSONArray)(new JSONTokener(jsonAnswers)).nextValue();
        for (int i=0;i<array.length();i++){
            JSONObject obj=array.getJSONObject(i);
            answerIds.add(obj.getInt(ApiConstants.JSON_ANSWER_OPTION_ID));
        }
        for (Option o:options){
            if (answerIds.contains(o.getApiId())){
                o.setAnswer(true);
            }else {
                o.setAnswer(false);
            }
        }
        return options;
    }

}
