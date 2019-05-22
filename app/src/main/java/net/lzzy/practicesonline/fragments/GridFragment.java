package net.lzzy.practicesonline.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.models.view.QuestionResult;
import net.lzzy.sqllib.GenericAdapter;
import net.lzzy.sqllib.ViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author lzzy_gxy
 * @date 2019/5/13
 * Description:
 */
public class GridFragment extends BaseFragment {
    private static final String GRID_RESULTS = "gridResults";
    private List<QuestionResult> results;
    private TextView textView;
    private OnResultListener listener;
    private GridView gridView;
    //region

    public static GridFragment newInstance(List<QuestionResult> results) {
        GridFragment fragment = new GridFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(GRID_RESULTS, (ArrayList<? extends Parcelable>) results);
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
    protected void populate() {
        produceOptions();
    }

    private void produceOptions() {
        gridView = find(R.id.fragment_grid_gv);
        textView = find(R.id.fragment_grid_tv_go);
        GenericAdapter<QuestionResult> adapter = new GenericAdapter<QuestionResult>(getContext(), R.layout.result_item, results) {
            @Override
            public void populate(ViewHolder holder, QuestionResult result) {
                holder.setTextView(R.id.fragment_grid_btn_item, getPosition(result) + 1 + "");
                TextView tv = holder.getView(R.id.fragment_grid_btn_item);
                tv.setOnClickListener(v -> listener.onResultTopic(getPosition(result)));

                if (result.isRight()) {
                    tv.setBackgroundResource(R.drawable.btn_radios_right);
                } else {
                    tv.setBackgroundResource(R.drawable.btn_radios_errors);
                }
            }

            @Override
            public boolean persistInsert(QuestionResult result) {
                return false;
            }

            @Override
            public boolean persistDelete(QuestionResult result) {
                return false;
            }
        };
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (listener != null) {
                    listener.onResultTopic(position);
                }
            }
        });
        find(R.id.fragment_grid_tv_go).setOnClickListener(v ->
                {
                    if (listener != null) {
                        listener.onClickImgButton();
                    }
                }
        );
    }


//endregion

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_grid;
    }

    @Override
    public void search(String kw) {

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnResultListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "必须实现OnResultListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    public interface OnResultListener {
        /**
         * 跳转题目视图传position数据
         *
         * @param pos
         */
        void onResultTopic(int pos);

        /**
         * 图表切换
         */
        void onClickImgButton();
    }
}
