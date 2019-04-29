package net.lzzy.practicesonline.models;

import net.lzzy.practicesonline.constants.DbConstents;
import net.lzzy.practicesonline.utils.AppUtils;
import net.lzzy.sqllib.DbPackager;
import net.lzzy.sqllib.SqlRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author lzzy_gxy
 * @date 2019/4/17
 * Description:
 */
public class PracticeFactory {
    private static final PracticeFactory OUT_INSTANCE = new PracticeFactory();
    private static SqlRepository<Practice> repository;

    private PracticeFactory() {
        repository = new SqlRepository<>(AppUtils.getContext(), Practice.class, DbConstents.packager);
    }

    public static PracticeFactory getInstance() {
        return OUT_INSTANCE;
    }

    public Practice getById(String id) {
        return repository.getById(id);
    }

    public List<Practice> get() {
        return repository.get();
    }

    public List<Practice> searchPractices(String shPractices) {
        try {
            return repository.getByKeyword(shPractices, new String[]{Practice.COL_NAME, Practice.COL_OUTLINES}, false);

        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

    }

    public boolean add(Practice practice) {
        if (isPracticeInDb(practice)) {
            return false;
        }
        repository.insert(practice);
        return true;
    }

    private boolean isPracticeInDb(Practice practice) {
        try {
            return repository.getByKeyword(String.valueOf(practice.getApiId()),
                    new String[]{Practice.COL_API_ID}, true).size() > 0;
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
            return true;
        }

    }

    private void setPracticeDown(String id) {
        Practice practice = getById(id);
        if (practice != null) {
            practice.setDownloaded(true);
            repository.update(practice);
        }
    }

    public void saveQuestions(List<Question> list, UUID id) {
        for (Question q : list) {
            QuestionFactory.getInstance().inset(q);
        }
        setPracticeDown(id.toString());
    }


    public UUID getPracticeId(int apiId) {
        try {
            List<Practice> practices = repository.getByKeyword(String.valueOf(apiId)
                    , new String[]{Practice.COL_API_ID}, true);
            if (practices.size() > 0) {
                return practices.get(0).getId();
            }
        } catch (IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean deletePracticeAndRelated(Practice practice) {
        try {
            List<String> sqlAction = new ArrayList<>();
            sqlAction.add(repository.getDeleteString(practice));
            QuestionFactory factory = QuestionFactory.getInstance();
            List<Question> questions = factory.getByPractice(practice.getId().toString());
            if (questions.size() > 0) {
                for (Question q : questions) {
                    sqlAction.addAll(factory.getDeleteString(q));
                }
            }
            repository.exeSqls(sqlAction);
            if (!isPracticeInDb(practice)){

            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
