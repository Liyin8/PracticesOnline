package net.lzzy.practicesonline.activities;

import android.app.AlertDialog;
import android.content.Intent;

import androidx.fragment.app.Fragment;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.fragments.ChartFragment;
import net.lzzy.practicesonline.fragments.GridFragment;
import net.lzzy.practicesonline.models.Favorite;
import net.lzzy.practicesonline.models.FavoriteFactory;
import net.lzzy.practicesonline.models.Question;
import net.lzzy.practicesonline.models.view.QuestionResult;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzzy_gxy
 * @date 2019/5/13
 * Description:
 */
public class ResultActivity extends BaseActivity implements ChartFragment.OnResultSwitchListener, GridFragment.OnResultListener {
    public static final String RESULT_POSITION = "resultPosition";
    public static final String QUESTION_C = "question_c";
    List<QuestionResult> results;


    @Override
    protected int getLayoutRes() {
        return R.layout.activity_result;
    }

    @Override
    protected int getContainerId() {
        return R.id.activity_result_container;
    }

    @Override
    protected Fragment createFragment() {
        results = getIntent().getParcelableArrayListExtra(QuestionActivity.EXTRA_RESULT);
        return GridFragment.newInstance(results);
    }


    @Override
    public void onResultTopic(int pos) {
        Intent intent = new Intent();
        intent.putExtra(RESULT_POSITION, pos);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClickImgButton() {
        getManager().beginTransaction().replace(getContainerId()
                , ChartFragment.newInstance(results)).commit();
    }

    @Override
    public void gotoGrid() {
        getManager().beginTransaction().replace(getContainerId()
                , GridFragment.newInstance(results)).commit();
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setMessage("返回")
                .setPositiveButton("查看收藏", (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.putExtra(QUESTION_C, true);
                    setResult(RESULT_OK, intent);
                    finish();
                })
                .setNegativeButton("章节列表", (dialog, which) ->
                        startActivity(new Intent(this, PracticesActivity.class))
                )
                .setNeutralButton("返回题目", (dialog, which) ->
                        finish()
                )
                .show();
    }
}
