package net.lzzy.practicesonline.activities;


import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.fragments.PracticesFragment;
import net.lzzy.practicesonline.fragments.QuestionFragment;
import net.lzzy.practicesonline.models.FavoriteFactory;
import net.lzzy.practicesonline.models.Question;
import net.lzzy.practicesonline.models.QuestionFactory;
import net.lzzy.practicesonline.models.UserCookies;
import net.lzzy.practicesonline.models.view.PracticeResult;
import net.lzzy.practicesonline.models.view.QuestionResult;
import net.lzzy.practicesonline.network.PracticeService;
import net.lzzy.practicesonline.utils.AbstractStaticHandler;
import net.lzzy.practicesonline.utils.AppUtils;
import net.lzzy.practicesonline.utils.ViewUtils;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Administrator
 */
public class QuestionActivity extends AppCompatActivity {
    public static final int SUCCEED_CODES_MIX = 200;
    public static final int SUCCEED_CODES_MAX = 220;
    public static final int FAILED = 1;
    public static final int REQUEST_CODE_RESULT = 0;
    public static final String EXTRA_PRACTICE_ID = "extraPracticeId";
    public static final String EXTRA_RESULT = "extraResult";

    private String practiceId;
    private int apiId;
    private List<Question> questions;
    private boolean isCommitted = false;
    private TextView tvView;
    private TextView tvCommit;
    private ViewPager pager;
    private TextView tvHint;
    private FragmentStatePagerAdapter adapter;
    private int pos;
    private View[] dots;
    private ResultHandler handler = new ResultHandler(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_question);
        AppUtils.addActivity(this);
        retrieveData();
        initViews();
        initDots();
        setListeners();
        pos = UserCookies.getInstants().getCurrentQuestion(practiceId);
        pager.setCurrentItem(pos);
        refreshDots(pos);
        UserCookies.getInstants().updateReadCount(questions.get(pos).getId().toString());
    }

    //左右滑动

    private void setListeners() {
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                //滚动触动方法
            }

            @Override
            public void onPageSelected(int position) {
                //切换页的时候使用方法
                refreshDots(position);
                UserCookies.getInstants().updateCurrentQuestion(practiceId, position);
                UserCookies.getInstants().updateReadCount(questions.get(position).getId().toString());

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tvCommit.setOnClickListener(v -> commitPractice());
        tvView.setOnClickListener(v -> redirect());
    }

    /**
     * 查看监听
     */
    private void redirect() {
        List<QuestionResult> results = UserCookies.getInstants().getResultFromCookies(questions);
        Intent intent=new Intent(this,ResultActivity.class);
        intent.putExtra(EXTRA_PRACTICE_ID,practiceId);
        intent.putParcelableArrayListExtra(EXTRA_RESULT, (ArrayList<? extends Parcelable>) results);
        startActivityForResult(intent, REQUEST_CODE_RESULT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (data.getBooleanExtra(ResultActivity.QUESTION_C,false)){
                FavoriteFactory factory=FavoriteFactory.getInstance();
                List<Question> qs=new ArrayList<>();
                for (Question question:questions){
                    if (factory.isQuestionStarred(question.getId().toString())){
                        qs.add(question);
                    }

                }
                questions.clear();
                questions.addAll(qs);
                initDots();
                adapter.notifyDataSetChanged();
            }
            pager.setCurrentItem(data.getIntExtra(ResultActivity.RESULT_POSITION,0),true);
        }

    }

    /**
     * 提交监听
     */
    String info;

    private void commitPractice() {
        List<QuestionResult> results = UserCookies.getInstants().getResultFromCookies(questions);
        List<String> macs = AppUtils.getMacAddress();
        String[] items = new String[macs.size()];
        macs.toArray(items);
        info = items[0];
        new AlertDialog.Builder(this)
                .setTitle("选择mac地址")
                .setSingleChoiceItems(items, 0, (dialog, which) -> info = items[0])
                .setNegativeButton("取消", null)
                .setPositiveButton("提交", (dialog, which) -> {
                    PracticeResult result = new PracticeResult(results, apiId, "李印," + info);
                    postResult(result);
                })
                .show();
    }

    private void postResult(PracticeResult result) {
        isCommitted=true;
        ViewUtils.showProgress(this, "正在提交...");
        AppUtils.getExecutor().execute(() -> {
            try {
                int code = PracticeService.postResult(result);
                handler.sendMessage(handler.obtainMessage(REQUEST_CODE_RESULT, code));
            } catch (JSONException | IOException e) {
                handler.sendMessage(handler.obtainMessage(FAILED, e.getMessage()));
            }
        });
    }

    private static class ResultHandler extends AbstractStaticHandler<QuestionActivity> {

        ResultHandler(QuestionActivity context) {
            super(context);
        }

        @Override
        public void handleMessage(Message msg, QuestionActivity questionActivity) {

            ViewUtils.disminssProgress();
            if (msg.what == REQUEST_CODE_RESULT) {
                int code = (int) msg.obj;
                if (code >= SUCCEED_CODES_MIX && code <= SUCCEED_CODES_MAX) {
                    Toast.makeText(questionActivity, "提交成功", Toast.LENGTH_SHORT).show();
                    UserCookies.getInstants().commitPractice(questionActivity.practiceId);
                    questionActivity.redirect();
                } else {
                    Toast.makeText(questionActivity, "提交失败，请重试", Toast.LENGTH_SHORT).show();
                }
            } else if (msg.what == FAILED) {
                Toast.makeText(questionActivity, "提交失败，请重试", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initDots() {
        int count = questions.size();
        dots = new View[count];
        LinearLayout container = findViewById(R.id.activity_question_dots);
        container.removeAllViews();
        //圆点的大小
        int px = ViewUtils.px2dp(16, this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(px, px);
        //圆点之间的间距
        px = ViewUtils.dp2px(5, this);
        params.setMargins(px, px, px, px);
        for (int i = 0; i < count; i++) {
            TextView tvDot = new TextView(this);
            tvDot.setLayoutParams(params);
            tvDot.setBackgroundResource(R.drawable.bg_shap);
            tvDot.setTag(i);
            tvDot.setOnClickListener(v -> pager.setCurrentItem((Integer) v.getTag()));
            container.addView(tvDot);
            dots[i] = tvDot;
        }
    }

    private void refreshDots(int pos) {
        for (int i = 0; i < dots.length; i++) {
            int drawable = i == pos ? R.drawable.bg_card_style : R.drawable.bg_shap;
            dots[i].setBackgroundResource(drawable);
        }
    }

    private void initViews() {
        tvView = findViewById(R.id.activity_question_tv_view);
        tvCommit = findViewById(R.id.activity_question_tv_commit);
        tvHint = findViewById(R.id.activity_question_tv_hint);
        pager = findViewById(R.id.activity_question_pager);

        if (isCommitted) {
            tvCommit.setVisibility(View.GONE);
            tvView.setVisibility(View.VISIBLE);
            tvHint.setVisibility(View.VISIBLE);
        } else {
            tvCommit.setVisibility(View.VISIBLE);
            tvView.setVisibility(View.GONE);
            tvHint.setVisibility(View.GONE);
        }
        adapter = new FragmentStatePagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {

                Question question = questions.get(position);

                return QuestionFragment.newInstance(question.getId().toString(), position, isCommitted);
            }

            @Override
            public int getCount() {
                return questions.size();
            }
        };
        pager.setAdapter(adapter);
    }

    private void retrieveData() {
        practiceId = getIntent().getStringExtra(PracticesActivity.EXTRA_PRACTICE_ID);
        apiId = getIntent().getIntExtra(PracticesActivity.EXTRA_API_ID, -1);
        questions = QuestionFactory.getInstance().getByPractice(practiceId);
        isCommitted=UserCookies.getInstants().isPracticeCommitted(practiceId);
        if (apiId < 0 || questions == null || questions.size() == 0) {
            Toast.makeText(this, "no questions", Toast.LENGTH_SHORT).show();
            finish();
        }

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        AppUtils.removeActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.setRunningActivity(getLocalClassName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        AppUtils.setRunningActivity(getLocalClassName());
    }
}
