package net.lzzy.practicesonline.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.PracticesActivity;
import net.lzzy.practicesonline.activities.QuestionActivity;
import net.lzzy.practicesonline.models.Question;
import net.lzzy.practicesonline.models.QuestionFactory;
import net.lzzy.practicesonline.models.UserCookies;
import net.lzzy.practicesonline.models.view.QuestionResult;
import net.lzzy.practicesonline.utils.ViewUtils;
import net.lzzy.practicesonline.views.BarChartView;
import net.lzzy.practicesonline.views.LineChartView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzzy_gxy
 * @date 2019/5/16
 * Description:
 */
public class AnalysisFragment extends BaseFragment {
    private static final String GRID_RESULTS = "charResults";
    private List<QuestionResult> results;
    private ChartFragment.OnResultSwitchListener listener;
    private static final String[] HORIZONTAL_AXIS = {"正确", "少选", "多选", "错选"};

    @Override
    protected void populate() {
        find(R.id.fragment_analysis_tv_go).setOnClickListener(v -> {
                    if (listener != null) {
                        listener.gotoGrid();
                    }
                }
        );
        int extra = 0, miss = 0, wrong = 0, right = 0;
        for (QuestionResult result : results) {
            switch (result.getType()) {
                case RIGHT_OPTIONS:
                    right++;
                    break;
                case EXTRA_OPTIONS:
                    extra++;
                    break;
                case MISS_OPTIONS:
                    miss++;
                    break;
                case WRONG_OPTIONS:
                    wrong++;
                    break;
                default:
                    break;
            }
        }
        float[] data = new float[]{right, miss, extra, wrong};
        float max = right;
        for (float f : data) {
            if (f > max) {
                max = f;
            }
        }
        BarChartView barChart = find(R.id.fragment_analysis_bar);
        barChart.setHorizontalAxis(HORIZONTAL_AXIS);
        barChart.setDataList(data, (int) (max * 2));

    }


    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_analysis;

    }

    public static AnalysisFragment newInstance(List<QuestionResult> result) {
        AnalysisFragment fragment = new AnalysisFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(GRID_RESULTS, (ArrayList<? extends Parcelable>) result);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            results = getArguments().getParcelableArrayList(GRID_RESULTS);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (ChartFragment.OnResultSwitchListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "必须实现onResultSwitchListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public void search(String kw) {

    }
}
