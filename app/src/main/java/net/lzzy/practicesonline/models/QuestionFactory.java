package net.lzzy.practicesonline.models;

import android.text.TextUtils;

import net.lzzy.practicesonline.constants.DbConstents;
import net.lzzy.practicesonline.utils.AppUtils;
import net.lzzy.sqllib.SqlRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzzy_gxy
 * @date 2019/4/17
 * Description:
 */
public class QuestionFactory {
    private static final QuestionFactory OUT_INSTANCE = new QuestionFactory();
    private SqlRepository<Question> repository;
    private SqlRepository<Option> optionSqlRepository;

    private QuestionFactory() {
        repository = new SqlRepository<>(AppUtils.getContext(), Question.class, DbConstents.packager);
        optionSqlRepository = new SqlRepository<>(AppUtils.getContext(), Option.class, DbConstents.packager);
    }

    public static QuestionFactory getInstance() {
        return OUT_INSTANCE;
    }

    private void completeQuestion(Question question) {
        List<Option> options = null;
        try {
            options = optionSqlRepository.getByKeyword(
                    question.getId().toString(),new String[]{Option.COL_QUESTION_ID},true
            );
        } catch (IllegalAccessException |InstantiationException e) {
            e.printStackTrace();
        }
        question.setOptions(options);
        question.setDbType(question.getDbType());
    }

    public List<Question> getByPractice(String practice) {
        try {
            List<Question> questions = repository.getByKeyword(practice
                    , new String[]{Question.COL_PRACTICE_ID}, true);
            for (Question question:questions){
                completeQuestion(question);
            }
            return questions;
        } catch (IllegalAccessException |InstantiationException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    public Question getById(String questionId) {
        try {
            Question question = repository.getById(questionId);
            completeQuestion(question);
            return question;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public void inset(Question question) {
        List<Option> options = question.getOptions();
        List<String> sqlActions = new ArrayList<>();
        for (Option option : options) {
            sqlActions.add(optionSqlRepository.getInsertString(option));
        }
        sqlActions.add(repository.getInsertString(question));
        repository.exeSqls(sqlActions);
    }

    protected List<String> getDeleteString(Question question) {
        List<String> sqlAction = new ArrayList<>();
        sqlAction.add(repository.getDeleteString(question));
        for (Option option : question.getOptions()) {
            sqlAction.add(optionSqlRepository.getDeleteString(option));
        }
        String f = FavoriteFactory.getInstance().getDeleteString(question.getId().toString());
        if (!TextUtils.isEmpty(f)) {
            sqlAction.add(f);
        }

        return sqlAction;

    }

}
