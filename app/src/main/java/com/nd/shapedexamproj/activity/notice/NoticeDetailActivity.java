package com.nd.shapedexamproj.activity.notice;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.util.DateUtils;
import com.nd.shapedexamproj.util.Utils;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.util.Log;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author WuYuLong
 * @version V1.0
 * @Title: NoticeDetailActivity
 * @Description: 系统公告详情
 * @date 2014-5-7
 */
public class NoticeDetailActivity extends BaseActivity {
    private static final String TAG = "NoticeDetailActivity";
    private Context context;
    private RelativeLayout common_head_layout;
    private Button common_head_right_btn;
    private ImageView commonheader_left_iv, notice_detail_image;
    private TextView commonheader_title_tv;
    private View loadingView;//网络指示标

    private TmingCacheHttp cacheHttp;
    private HashMap<String, Object> map;
    /*private String topic_id;*/

    private View notice_detail_line_tv;
    private TextView notice_detail_title_tv, notice_detail_time_tv, no_data_tv;
    private TextView notice_detail_content_tv;
    private ImageCacheTool imageCacheTool;

    @Override
    public int initResource() {
        return R.layout.notice_detail_activity;
    }

    @Override
    public void initComponent() {
        context = this;
        //设置头部透明色
        common_head_layout = (RelativeLayout) findViewById(R.id.notice_detail_head);
        common_head_layout.setBackgroundColor(getResources().getColor(R.color.transparent));
        //头部按钮
        commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
        commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
        commonheader_title_tv.setText(R.string.notice_detail_activity_title);
        commonheader_title_tv.setText(commonheader_title_tv.getText());

//		common_head_left_btn = (Button) findViewById(R.id.common_head_left_btn);
//		common_head_left_btn.setText(R.string.notice_detail_activity_title);
//		common_head_left_btn.setText("  " + common_head_left_btn.getText());
        common_head_right_btn = (Button) findViewById(R.id.common_head_right_btn);
        common_head_right_btn.setVisibility(View.INVISIBLE);

        notice_detail_line_tv = findViewById(R.id.notice_detail_line_tv);
        no_data_tv = (TextView) findViewById(R.id.no_data_tv);
        notice_detail_title_tv = (TextView) findViewById(R.id.notice_detail_title_tv);
        notice_detail_time_tv = (TextView) findViewById(R.id.notice_detail_time_tv);
        notice_detail_content_tv = (TextView) findViewById(R.id.notice_detail_content_tv);
        notice_detail_content_tv.setMovementMethod(LinkMovementMethod.getInstance());
        notice_detail_image = (ImageView) findViewById(R.id.notice_detail_image);
        loadingView = findViewById(R.id.loading_layout);

        imageCacheTool = ImageCacheTool.getInstance(this);
    }

    @Override
    public void initData() {
        cacheHttp = TmingCacheHttp.getInstance();
        map = new HashMap<String, Object>();
		/*topic_id = getIntent().getStringExtra("topic_id");*/
		/*noticeReflash();*/
        String title = getIntent().getStringExtra("title");
        String content = getIntent().getStringExtra("content");
        String time = getIntent().getStringExtra("time");
        loadData(title, content, time);
    }

    /**
     * 首次加载网络数据 或 刷新
     private void noticeReflash(){
     requestData();
     }

     *//**
     * 获取网络数据
     *//*
	Runnable dataLoadRunnable = new Runnable() {
		@Override
		public void run() {
			requestData();
		}
	};*/

    /**
     * 请求网络数据
     */
//	private void requestData(){
		/*//开启网络指示标
		loadingView = findViewById(R.id.loading_layout);
		
		String url = Constants.ANNOUNCEMENT_INFO;
		map.put("announcementid", topic_id);
		
		TmingHttp.asyncRequest(url, map, new RequestCallback<String>(){

            @Override
            public String onReqestSuccess(String respones) throws Exception {
                return jsonParsing(respones);
            }

            @Override
            public void success(String respones) {
                loadData(respones);
            }

            @Override
            public void exception(Exception exception) {
                
            }

		});*/
//	}

    /**
     * 网络请求成功后，加载数据
     *
     * @param
     */
    private void loadData(String title, String content, String time) {


        no_data_tv.setVisibility(View.GONE);
        notice_detail_line_tv.setVisibility(View.VISIBLE);

        notice_detail_title_tv.setText(title);
        time = DateUtils.getDate(time, "yyyy-MM-dd HH:mm:ss");
        notice_detail_time_tv.setText(time);

        content = content.replaceAll("\\n+\\t\\s+", "\n\t\t ");//段落前空两格
        notice_detail_content_tv.setText(parseWebUrlLink(content));

        loadingView.setVisibility(View.GONE);
    }

    public Spanned parseWebUrlLink(CharSequence input) {
        Pattern pattern = Pattern.compile("((http|ftp|https):\\/\\/)?[A-Za-z0-9_\\-_]+(\\.[A-Za-z0-9_\\-_]+)+([A-Za-z0-9_\\-\\.,@?^=%&:/~\\+#]*[A-Za-z0-9_\\-\\@?^=%&/~\\+#])?");
        Matcher matcher = pattern.matcher(input);
        SpannableStringBuilder out = new SpannableStringBuilder(input);
        while (matcher.find()) {
            String urlString = matcher.group();
            if (!urlString.matches("(http|ftp|https):\\/\\/.*")) {// 如果没有Http开头
                urlString = "http://" + urlString;
            }
            MyURLSpan span = new MyURLSpan(urlString);
            out.setSpan(span, matcher.start(), matcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return out;
    }

    private class MyURLSpan extends ClickableSpan {

        private String mUrl;

        public MyURLSpan(String urlString) {
            Log.d("MyURLSpan", urlString);
            mUrl = urlString;
        }

        @Override
        public void onClick(View widget) {
            Log.d("MyURLSpan", mUrl);
            Utils.openBrowser(widget.getContext(), mUrl);
        }
    }

    /**
     * 回调方法
     *
     * @param data
     * @return
     */
    private String jsonParsing(String data) {
        return data;
    }

    @Override
    public void addListener() {
        commonheader_left_iv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // backMain();
                finish();
            }
        });
    }
}
