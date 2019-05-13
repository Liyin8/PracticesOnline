package net.lzzy.practicesonline.fragments;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.models.FavoriteFactory;
import net.lzzy.practicesonline.models.Option;
import net.lzzy.practicesonline.models.Question;
import net.lzzy.practicesonline.models.QuestionFactory;
import net.lzzy.practicesonline.models.UserCookies;
import net.lzzy.practicesonline.models.view.QuestionType;

import java.util.List;

/**
 * @author lzzy_gxy
 * @date 2019/4/30
 * Description:
 */
public class QuestionFragment extends BaseFragment {
    static final String ARG_QUESTION_ID = "argQuestionId";
    static final String ARG_POS = "argPos";
    static final String ARG_IS_COMMITTED = "argIsCommitted";
    private Question question;
    private int pos;
    private boolean isCommitted;
    private boolean isMulti = false;
    private int starId;
    private ImageButton imf;
    private TextView tvContent;
    private RadioGroup radioGroup;
    private TextView textView;
    private SharedPreferences spRadio;


    public static QuestionFragment newInstance(String questionId, int pos, boolean isCommitted) {
        QuestionFragment fragment = new QuestionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_QUESTION_ID, questionId);
        args.putInt(ARG_POS, pos);
        args.putBoolean(ARG_IS_COMMITTED, isCommitted);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            pos = getArguments().getInt(ARG_POS);
            isCommitted = getArguments().getBoolean(ARG_IS_COMMITTED);
            question = QuestionFactory.getInstance().getById(getArguments().getString(ARG_QUESTION_ID));
        }

    }

    @Override
    protected void populate() {
        initView();
        questionType();
        produceOptions();
    }

    private void produceOptions() {
        List<Option> options = question.getOptions();
        for (Option option : options) {
            CompoundButton btn = isMulti ? new CheckBox(getContext()) : new RadioButton(getContext());
            String content = option.getLabel() + "." + option.getContent();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                btn.setButtonTintList(ColorStateList.valueOf(Color.GRAY));
            }
            btn.setText(content);
            btn.setEnabled(!isCommitted);
            radioGroup.addView(btn);

            btn.setOnCheckedChangeListener((buttonView, isChecked) ->
                    UserCookies.getInstants().changeOptionState(option, isChecked, isMulti));

            boolean shouldCheck = UserCookies.getInstants().isOptionSelected(option);
            if (isMulti) {
                btn.setChecked(shouldCheck);
            } else if (shouldCheck) {
                radioGroup.check(btn.getId());
            }

            if (isCommitted && option.isAnswer()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    btn.setTextColor(getResources().getColor(R.color.colorPrimary, null));
                } else {
                    btn.setTextColor(getResources().getColor(R.color.colorPrimary));
                }
            }
        }

    }

    private void questionType() {
        isMulti = question.getType() == QuestionType.MULTI_CHOICE;
        int label = pos + 1;
        String qType = label + "." + question.getType().toString();
        textView.setText(qType);
        tvContent.setText(question.getContent());
        starId = FavoriteFactory.getInstance().isQuestionStarred(question.getId().toString())
                ? android.R.drawable.star_on : android.R.drawable.star_off;
        imf.setImageResource(starId);
        imf.setOnClickListener(v -> collect());
    }

    private void collect() {
        FavoriteFactory factory = FavoriteFactory.getInstance();
        if (factory.isQuestionStarred(question.getId().toString())) {
            factory.cancelStarQuestion(question.getId());
            imf.setImageResource(android.R.drawable.star_off);
        } else {
            factory.starQuestion(question.getId());
            imf.setImageResource(android.R.drawable.star_on);
        }

    }

    private void initView() {
        textView = find(R.id.fragment_question_tv_type);
        tvContent = find(R.id.fragment_question_tv_content);
        imf = find(R.id.fragment_question_img_favorite);
        radioGroup = find(R.id.fragment_question_option_container);
        if (isCommitted) {
            radioGroup.setOnClickListener(v -> {
                new AlertDialog.Builder(getContext())
                        .setMessage(question.getAnalysis())
                        .show();
            });
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_question;
    }

    @Override
    public void search(String kw) {

    }
}
