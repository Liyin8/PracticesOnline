package net.lzzy.practicesonline.fragments;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import net.lzzy.practicesonline.R;
import net.lzzy.practicesonline.models.Practice;
import net.lzzy.practicesonline.models.PracticeFactory;
import net.lzzy.practicesonline.models.Question;
import net.lzzy.practicesonline.models.UserCookies;
import net.lzzy.practicesonline.network.DetectWebService;
import net.lzzy.practicesonline.network.PracticeService;
import net.lzzy.practicesonline.network.QuestionService;
import net.lzzy.practicesonline.utils.AbstractStaticHandler;
import net.lzzy.practicesonline.utils.AppUtils;
import net.lzzy.practicesonline.utils.DateTimeUtils;
import net.lzzy.practicesonline.utils.ViewUtils;
import net.lzzy.sqllib.GenericAdapter;
import net.lzzy.sqllib.ViewHolder;

import org.json.JSONException;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author lzzy_gxy
 * @date 2019/4/16
 * Description:
 */
public class PracticesFragment extends BaseFragment {

    public static final int WHAT_EXCEPTION = 1;
    public static final int WHAT_QUESTION_DOWN = 2;
    public static final int WHAT_QUESTION_EXCEPTION = 3;
    private SwipeRefreshLayout swipe;
    private ListView listView;
    private TextView tvNone;
    private TextView tvHint;
    private TextView tvTime;
    private List<Practice> practices;
    private GenericAdapter<Practice> adapter;
    private static final int WHAT_PRACTICE_DOWN = 0;
    private PracticeFactory factory = PracticeFactory.getInstance();
    private ThreadPoolExecutor executor = AppUtils.getExecutor();
    private DownloadHandler handler = new DownloadHandler(this);
    private boolean isDelete;
    private double MIX_DISTANCE = 50;
    private OnQuestionSelectedListener listener;
    //启动线程池一定要有 handler

    private static class DownloadHandler extends AbstractStaticHandler<PracticesFragment> {

        protected DownloadHandler(PracticesFragment context) {
            super(context);
        }

        @Override
        public void handleMessage(Message msg, PracticesFragment fragment) {
            switch (msg.what) {
                case WHAT_PRACTICE_DOWN:
                    fragment.tvTime.setText(DateTimeUtils.DATE_TIME_FORMAT.format(new Date()));
                    UserCookies.getInstants().updateLastRefreshTime();
                    try {
                        List<Practice> practices = PracticeService.getPractices(msg.obj.toString());
                        for (Practice practice : practices) {
                            fragment.adapter.add(practice);
                        }
                        Toast.makeText(fragment.getContext(), "同步完成", Toast.LENGTH_SHORT).show();
                        fragment.finishRefresh();
                    } catch (IllegalAccessException | JSONException | java.lang.InstantiationException e) {
                        e.printStackTrace();
                        fragment.handlerPracticeException(e.getMessage());
                    }

                    break;
                case WHAT_EXCEPTION:
                    fragment.handlerPracticeException(msg.obj.toString());
                    break;
                case WHAT_QUESTION_DOWN:
                    UUID practiceId = fragment.factory.getPracticeId(msg.arg1);
                    fragment.saveQuestions(msg.obj.toString(), practiceId);
                    break;
                case WHAT_QUESTION_EXCEPTION:
                    ViewUtils.disminssProgress();
                    Toast.makeText(fragment.getContext(), "下载失败请重试!" + msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }

    private void saveQuestions(String json, UUID practiceId) {
        try {
            List<Question> questions = QuestionService.getQuestions(json, practiceId);
            factory.saveQuestions(questions, practiceId);
            for (Practice practice : practices) {
                if (practice.getId().equals(practiceId)) {
                    practice.setDownloaded(true);
                }
            }
            adapter.notifyDataSetChanged();
        } catch (Exception e) {
            Toast.makeText(getContext()
                    , "下载失败请重试！" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void handlerPracticeException(String message) {
        finishRefresh();
        Snackbar.make(listView, "同步失败\n" + message, Snackbar.LENGTH_LONG)
                .setAction("重试", v -> {
                    swipe.setRefreshing(true);
                    refreshListener.onRefresh();
                }).show();
    }

    private void finishRefresh() {
        swipe.setRefreshing(false);
        tvTime.setVisibility(View.GONE);
        tvHint.setVisibility(View.GONE);
        NotificationManager manager=(NotificationManager) Objects.requireNonNull(getContext())
                .getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager!=null){
            manager.cancel(DetectWebService.NOTIFICATION_DETECT_ID);
        }
    }


    @Override
    protected void populate() {
        initViews();
        loadPractices();
        initSwipe();
    }

    private void downloadPracticesAsync() {
        new PracticeDownloader(this).execute();
    }

    /**
     * Params执行异步任务参数 Progress进度条数据单位 返回结果
     */
    static class PracticeDownloader extends AsyncTask<Void, Void, String> {
        WeakReference<PracticesFragment> fragment;

        PracticeDownloader(PracticesFragment fragment) {
            this.fragment = new WeakReference<>(fragment);
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            PracticesFragment fragment = this.fragment.get();
            fragment.tvTime.setVisibility(View.VISIBLE);
            fragment.tvHint.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(Void... voids) {
            try {
                return PracticeService.getPracticesFromServer();
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            PracticesFragment fragment = this.fragment.get();
            fragment.tvTime.setText(DateTimeUtils.DATE_TIME_FORMAT.format(new Date()));
            UserCookies.getInstants().updateLastRefreshTime();
            try {
                List<Practice> practices = PracticeService.getPractices(s);
                for (Practice practice : practices) {
                    fragment.adapter.add(practice);
                }
                Toast.makeText(fragment.getContext(), "同步完成", Toast.LENGTH_SHORT).show();
                fragment.finishRefresh();
            } catch (IllegalAccessException | JSONException | java.lang.InstantiationException e) {
                e.printStackTrace();
                fragment.handlerPracticeException(e.getMessage());
            }
        }
    }

    static class QuestionDownloader extends AsyncTask<Void, Void, String> {
        WeakReference<PracticesFragment> fragment;
        Practice practice;

        QuestionDownloader(PracticesFragment fragment, Practice practice) {
            this.fragment = new WeakReference<>(fragment);
            this.practice = practice;
        }

        /**
         * 执行异步任务之前
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            ViewUtils.showProgress(fragment.get().getContext(), "开始下载..");

        }

        /**
         * 后台的异步任务
         *
         * @param voids
         * @return
         */
        @Override
        protected String doInBackground(Void... voids) {

            try {
                return QuestionService.getQuestionsOfPracticeFromServer(practice.getApiId());
            } catch (IOException e) {
                return e.getMessage();
            }
        }

        /**
         * 执行异步任务之后
         *
         * @param s
         */
        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            fragment.get().saveQuestions(s, practice.getId());
            ViewUtils.disminssProgress();
        }
    }

    private SwipeRefreshLayout.OnRefreshListener refreshListener = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            dialogPractice();
        }
    };

    private void dialogPractice() {
        tvTime.setVisibility(View.VISIBLE);
        tvHint.setVisibility(View.VISIBLE);
        executor.execute(() -> {
            try {
                String json = PracticeService.getPracticesFromServer();
                handler.sendMessage(handler.obtainMessage(WHAT_PRACTICE_DOWN, json));
            } catch (IOException e) {
                e.printStackTrace();
                handler.sendMessage(handler.obtainMessage(WHAT_EXCEPTION, e.getMessage()));
            }

        });
    }

    public void startRefresh(){
        swipe.setRefreshing(true);
        refreshListener.onRefresh();
    }

    private void initSwipe() {
        swipe.setOnRefreshListener(refreshListener);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean isTop = view.getChildCount() == 0 || view.getChildAt(0).getTop() >= 0;
                swipe.setEnabled(isTop);

            }
        });
    }

    private void loadPractices() {
        practices = factory.get();
        listView.findViewById(R.id.fragment_practices_lv);
        tvNone.findViewById(R.id.fragment_practices_tv_none);
        tvHint.findViewById(R.id.fragment_practices_tv_hint);
        tvTime.findViewById(R.id.fragment_practices_tv_time);
        /**
         * 排序方法
         */
        Collections.sort(practices, ((o1, o2) -> o2.getDownloadDate().compareTo(o1.getDownloadDate())));

        adapterDelete();

        listView.setAdapter(adapter);
    }

    private void adapterDelete() {
        adapter = new GenericAdapter<Practice>(getActivity(), R.layout.practices_item, practices) {
            @Override
            public void populate(ViewHolder holder, Practice practice) {
                holder.setTextView(R.id.practice_item_tv_name, practice.getName());
                TextView tvPoint = holder.getView(R.id.practice_item_tv_point);
                if (practice.isDownloaded()) {
                    tvPoint.setVisibility(View.VISIBLE);
                    tvPoint.setOnClickListener(v -> new AlertDialog.Builder(getContext())
                            .setMessage(practice.getOutlines())
                            .show());
                } else {
                    tvPoint.setVisibility(View.GONE);
                }
                Button btnDel = holder.getView(R.id.practice_item_btn_del);
                btnDel.setOnClickListener(v -> new AlertDialog.Builder(getActivity())
                        .setTitle("删除确认")
                        .setMessage("要删除题目？")
                        .setNegativeButton("取消", null)
                        .setPositiveButton("确认", (dialog, which) -> {
                            isDelete = false;
                            adapter.remove(practice);

                        }).show());
                int visible = isDelete ? View.VISIBLE : View.GONE;
                btnDel.setVisibility(visible);

                holder.getConvertView().setOnTouchListener(new View.OnTouchListener() {

                    private float touchX1;
                    private float touchX2;

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        slideToDelete(event, practice, btnDel);
                        return true;
                    }

                    private void slideToDelete(MotionEvent event, Practice practice, Button btn) {
                        switch (event.getAction()) {
                            case MotionEvent.ACTION_DOWN:
                                touchX1 = event.getX();
                                break;
                            case MotionEvent.ACTION_UP:
                                touchX2 = event.getX();
                                if (touchX1 - touchX2 > MIX_DISTANCE) {
                                    if (!isDelete) {
                                        btn.setVisibility(View.VISIBLE);
                                        isDelete = true;
                                    }
                                } else {
                                    if (btn.isShown()) {
                                        btn.setVisibility(View.GONE);
                                        isDelete = false;
                                    } else {
                                        clickPractice(practice);
                                    }
                                }
                                break;
                            default:
                                break;
                        }
                    }
                });
            }

            @Override
            public boolean persistInsert(Practice practice) {
                return factory.add(practice);
            }

            @Override
            public boolean persistDelete(Practice practice) {
                return factory.deletePracticeAndRelated(practice);
            }
        };
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (OnQuestionSelectedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + "必须实现OnQuestionSelectedListener");
        }
    }

    private void clickPractice(Practice practice) {
        if (practice.isDownloaded() && listener != null) {
            listener.onQuestionSelected(practice.getId().toString(), practice.getApiId());
        } else {
            new AlertDialog.Builder(getContext())
                    .setMessage("是否下载该章节")
//                    .setPositiveButton("下载", (dialog, which) -> downloadQuestion(practice.getApiId()))
                    .setPositiveButton("下载", (dialog, which) -> downloadQuestionAsyn(practice))
                    .setNeutralButton("取消", null)
                    .show();
        }
    }

    private void downloadQuestion(int apiId) {
        ViewUtils.showProgress(getContext(), "开始下载...");
        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    String json = QuestionService.getQuestionsOfPracticeFromServer(apiId);
                    Message msg = handler.obtainMessage(WHAT_QUESTION_DOWN, json);
                    msg.arg1 = apiId;
                    handler.sendMessage(msg);
                } catch (IOException e) {
                    handler.sendMessage(handler.obtainMessage(WHAT_QUESTION_EXCEPTION));
                }
            }
        });
    }

    /**
     * 异步
     *
     * @param practice
     */
    private void downloadQuestionAsyn(Practice practice) {
        new QuestionDownloader(this, practice).execute();
    }

    private void initViews() {
        listView = find(R.id.fragment_practices_lv);
        tvNone = find(R.id.fragment_practices_tv_none);
        listView.setEmptyView(tvNone);
        swipe = find(R.id.fragment_practices_swipe);
        tvHint = find(R.id.fragment_practices_tv_hint);
        tvTime = find(R.id.fragment_practices_tv_time);
        tvTime.setText(UserCookies.getInstants().getLastRefreshTime());
        tvHint.setVisibility(View.GONE);
        tvTime.setVisibility(View.GONE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected int getLayoutRes() {
        return R.layout.fragment_practices;
    }

    @Override
    public void search(String kw) {
        practices.clear();
        if (kw.isEmpty()) {
            practices.addAll(factory.get());
        } else {
            practices.addAll(factory.searchPractices(kw));
        }
        adapter.notifyDataSetChanged();
    }

    public interface OnQuestionSelectedListener {
        /**
         * practices跳转
         *
         * @param practiceId
         * @param apiId
         */
        void onQuestionSelected(String practiceId, int apiId);

    }

}
