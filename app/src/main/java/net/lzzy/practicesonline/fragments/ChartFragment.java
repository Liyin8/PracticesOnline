package net.lzzy.practicesonline.fragments;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.utils.MPPointF;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.activities.PracticesActivity;
import net.lzzy.practicesonline.activities.QuestionActivity;
import net.lzzy.practicesonline.models.UserCookies;
import net.lzzy.practicesonline.models.view.QuestionResult;
import net.lzzy.practicesonline.models.view.WrongType;
import net.lzzy.practicesonline.utils.ViewUtils;
import net.lzzy.practicesonline.views.BarChartView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author lzzy_gxy
 * @date 2019/5/13
 * Description:
 */
public class ChartFragment extends BaseFragment {

    private static final String GRID_RESULTS = "charResults";
    private static final String COLOR_GREEN = "#629755";
    private static final String COLOR_RED = "#D81B60";
    private static final String COLOR_PRIMARY = "#008577";
    private static final String COLOR_BROWN = "#00574B";
    private static final int MIX_DSTANCE = 50;
    private List<QuestionResult> results;
    private OnResultSwitchListener listener;
    private PieChart pieChart;
    private LineChart lineChar;
    private BarChart barChar;
    private Chart[] charts;
    private String[] title = {"错误比例单位%", "题目阅读量统计", "错误类型统计"};
    private int rightCount = 0;
    private View[] dots;
    private float touchX1;
    private int chartIndex = 0;

    @Override
    protected void populate() {
        find(R.id.fragment_char_tv_go).setOnClickListener(v -> {
                    if (listener != null) {
                        listener.gotoGrid();
                    }
                }
        );

        initCharts();
        configPieChart();
        displayPieChart();

        configBarLineChart(barChar);
        displayBarChart();

        configBarLineChart(lineChar);
        displayLineChart();
        pieChart.setVisibility(View.VISIBLE);

        View dot1 = find(R.id.fragment_char_dot1);
        View dot2 = find(R.id.fragment_char_dot2);
        View dot3 = find(R.id.fragment_char_dot3);
        dots = new View[]{dot1, dot2, dot3};
        find(R.id.fragment_char_container).setOnTouchListener(new ViewUtils.AbstractTouchListener() {
            @Override
            public boolean handleTouch(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    touchX1 = event.getX();
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    float touchX2 = event.getX();
                    if (Math.abs(touchX2 - touchX1) > MIX_DSTANCE) {
                        if (chartIndex < charts.length - 1) {
                            chartIndex++;
                        } else {
                            chartIndex = 0;
                        }
                    } else {
                        if (chartIndex > 0) {
                            chartIndex--;
                        } else {
                            chartIndex = charts.length - 1;
                        }
                    }
                    switchChart();
                }
                return true;
            }
        });
    }

    private void displayLineChart() {
        List<Entry> entries = new ArrayList<>();
        for (int i = 0; i < results.size(); i++) {
            entries.add(new Entry(i + 1, UserCookies.getInstants()
                    .getReadCount(results.get(i).getQuestionId().toString())));
        }
        LineDataSet dataSet = new LineDataSet(entries, "查看访问数量");
        LineData lineData = new LineData(dataSet);

        lineChar.setData(lineData);
        lineChar.invalidate();

        ValueFormatter xFormat = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return " " + (int) value;
            }
        };
        lineChar.getXAxis().setValueFormatter(xFormat);


    }

    private void displayBarChart() {
        ValueFormatter xFormat = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return WrongType.getInstance((int) value).toString();
            }
        };
        barChar.getXAxis().setValueFormatter(xFormat);
        int ok = 0, miss = 0, extra = 0, wrong = 0;
        for (QuestionResult result : results) {
            switch (result.getType()) {
                case WRONG_OPTIONS:
                    wrong++;
                    break;
                case EXTRA_OPTIONS:
                    extra++;
                    break;
                case MISS_OPTIONS:
                    miss++;
                    break;
                case RIGHT_OPTIONS:
                    ok++;
                    break;
                default:
                    break;
            }
        }
        List<BarEntry> entries=new ArrayList<>();
        entries.add(new BarEntry(0,ok));
        entries.add(new BarEntry(1,miss));
        entries.add(new BarEntry(2,extra));
        entries.add(new BarEntry(3,wrong));
        BarDataSet dataSet=new BarDataSet(entries,"查看类型");
        dataSet.setColors(Color.parseColor(COLOR_RED),Color.parseColor(COLOR_GREEN)
                ,Color.parseColor(COLOR_BROWN),Color.parseColor(COLOR_PRIMARY));
        ArrayList<IBarDataSet> dataSets=new ArrayList<>();
        dataSets.add(dataSet);
        BarData data=new BarData(dataSets);
        data.setBarWidth(0.8f);
        barChar.setData(data);
        barChar.invalidate();
    }

    private void configBarLineChart(BarLineChartBase charts) {

        XAxis xAxis = charts.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(8f);
        xAxis.setGranularity(1f);
        YAxis yAxis = charts.getAxisLeft();
        yAxis.setLabelCount(8, true);
        yAxis.setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART);
        yAxis.setTextSize(8f);
        yAxis.setGranularity(1f);
        yAxis.setAxisMinimum(0);
        charts.getLegend().setEnabled(false);
        charts.getAxisRight().setEnabled(false);
        charts.setPinchZoom(false);

    }

    private void switchChart() {
        for (int i = 0; i < charts.length; i++) {
            if (chartIndex == i) {
                charts[i].setVisibility(View.VISIBLE);
                dots[i].setBackgroundResource(R.drawable.bg_card_style);
            } else {
                charts[i].setVisibility(View.GONE);
                dots[i].setBackgroundResource(R.drawable.bg_shap);
            }
        }
    }

    private void displayPieChart() {
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(rightCount, "正确"));
        entries.add(new PieEntry(results.size() - rightCount, "错误"));
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);
        List<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor(COLOR_GREEN));
        colors.add(Color.parseColor(COLOR_RED));
        dataSet.setColors(colors);
        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new PercentFormatter());
        pieData.setValueTextSize(11f);
        pieData.setValueTextColor(Color.WHITE);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }

    private void configPieChart() {
        pieChart.setUsePercentValues(true);
        //中心圆形要不要
        pieChart.setDrawHoleEnabled(false);
        pieChart.getLegend().setOrientation(Legend.LegendOrientation.HORIZONTAL);
        pieChart.getLegend().setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        pieChart.getLegend().setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
    }


    private void initCharts() {
        pieChart = find(R.id.fragment_char_pie);
        lineChar = find(R.id.fragment_char_line);
        barChar = find(R.id.fragment_char_bar);
        charts = new Chart[]{pieChart, lineChar, barChar};
        int i = 0;
        for (Chart chart : charts) {
            chart.setTouchEnabled(false);
            chart.setVisibility(View.GONE);
            Description des = new Description();
            des.setText(title[i++]);
            chart.setDescription(des);
            chart.setNoDataText("数据获取中...");
            chart.setExtraOffsets(5, 10, 5, 25);
        }
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_char;

    }

    public static ChartFragment newInstance(List<QuestionResult> result) {
        ChartFragment fragment = new ChartFragment();
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
        for (QuestionResult result : results) {
            if (result.isRight()) {
                rightCount++;
            }
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnResultSwitchListener) context;
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

    public interface OnResultSwitchListener {
        /**
         * 图表转换
         */
        void gotoGrid();
    }

}
