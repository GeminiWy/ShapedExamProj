package com.nd.shapedexamproj.view.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.course.CourseDetailActivity;
import com.nd.shapedexamproj.adapter.NoteAdapter;
import com.nd.shapedexamproj.model.note.NoteInfo;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.StringUtils;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import com.tming.common.net.TmingResponse;
import com.tming.common.util.Helper;
import com.tming.common.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * 
 * 语音笔记
 * 
 * @author Linlg
 * 
 *         Create on 2014-4-23
 */
public class MenuNoteView extends RelativeLayout {

    private static final String TAG = "MenuNoteView";
    private View view;
    private Context context;
    private Button speakBtn;
    private RelativeLayout speakLayout, playLayout, cancelLayout;
    private TextView noNoteTv, totalTimeTv;
    private MediaRecorder mediaRecorder;
    private MediaPlayer player;
    private ListView lv;
    private NoteAdapter adapter;
    private String fileName = "", courseId = "", videoId = "";
    // private long currentPosition = 0;
    private Handler handler;
    private Timer timer;
    private RecorderTimerTask task;
    private VoicePlayTask voicePlayTask;
    private UploadVoiceTask uploadVoiceTask;
    private int second = 0;
    private TmingCacheHttp cacheHttp;
    private int page = 1;
    private List<NoteInfo> noteInfos = new ArrayList<NoteInfo>();
    private long currentTime;
    private Activity mActivity;
    private boolean isCancel = false;

    public MenuNoteView(Context context, String courseId, String videoId, Activity activity) {
        super(context);
        this.mActivity = activity;
        this.context = context;
        this.courseId = courseId;
        this.videoId = videoId;// TODO
        cacheHttp = TmingCacheHttp.getInstance(context);
        handler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                second++;
            }
        };
        initView();
        requestNoteListData();
        addListener();
    }

    private void initView() {
        view = LayoutInflater.from(context).inflate(R.layout.videoview_menu_note, this);
        speakBtn = (Button) view.findViewById(R.id.videoview_menu_note_btn);
        speakLayout = (RelativeLayout) view.findViewById(R.id.videoview_menu_note_speaking_rl);
        playLayout = (RelativeLayout) view.findViewById(R.id.videoview_menu_note_playing_layout);
        totalTimeTv = (TextView) view.findViewById(R.id.videoview_menu_note_totaltime_tv);
        cancelLayout = (RelativeLayout) view.findViewById(R.id.videoview_menu_note_playing_rl);
        lv = (ListView) view.findViewById(R.id.videoview_menu_note_lv);
        adapter = new NoteAdapter(context);
        lv.setAdapter(adapter);
        noNoteTv = (TextView) view.findViewById(R.id.videoview_menu_note_nonote_tv);
        // tv.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // player = new MediaPlayer();
        // try {
        // fileName = Environment.getExternalStorageDirectory()
        // .getAbsolutePath()
        // + File.separator
        // + System.currentTimeMillis() + ".3gp";
        // player.setDataSource(fileName);
        // player.prepare();
        // player.start();
        // } catch (IllegalArgumentException e) {
        // e.printStackTrace();
        // } catch (SecurityException e) {
        // e.printStackTrace();
        // } catch (IllegalStateException e) {
        // e.printStackTrace();
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // }
        // });
        player = new MediaPlayer();
        timer = new Timer(true);
    }

    private void requestNoteListData() {
        TmingHttp.asyncRequest(ServerApi.getNoteList(videoId, page, 10), new HashMap<String, Object>(),
                new RequestCallback<List<NoteInfo>>() {

                    @Override
                    public List<NoteInfo> onReqestSuccess(String respones) throws Exception {
                        return NoteInfo.JSONParsing(respones);
                    }

                    @Override
                    public void success(List<NoteInfo> respones) {
                        adapter.addItemCollection(respones);
                        adapter.notifyDataSetChanged();
                        for (int i = 0; i < adapter.getCount(); i++) {
                            NoteInfo info = (NoteInfo) adapter.getItem(i);
                            if (!noteInfos.contains(info)) {
                                noteInfos.add(info);
                            }
                        }
                        if (adapter.getCount() == 0) {
                            noNoteTv.setVisibility(View.VISIBLE);
                        } else {
                            noNoteTv.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void exception(Exception exception) {}
                });
    }

    // private void requestNoteListData() {
    // cacheHttp.asyncRequestWithCache(
    // ServerApi.getNoteList(videoId, page, 10),
    // new HashMap<String, Object>(), requestNoteList);
    // // TmingHttp.asyncRequest(ServerApi.getNoteList(videoId, page, 10), new
    // HashMap<String, Object>(), new );
    // }
    //
    // private RequestWithCacheCallBack<List<NoteInfo>> requestNoteList = new
    // RequestWithCacheCallBack<List<NoteInfo>>() {
    //
    // @Override
    // public void success(List<NoteInfo> cacheRespones,
    // List<NoteInfo> newRespones) {
    // adapter.replaceItem(cacheRespones, newRespones);
    // for (int i = 0; i < adapter.getCount(); i++) {
    // NoteInfo info = (NoteInfo) adapter.getItem(i);
    // if (!noteInfos.contains(info)) {
    // noteInfos.add(info);
    // }
    // }
    // }
    //
    // @Override
    // public List<NoteInfo> onReqestSuccess(String respones) throws Exception {
    // Log.i(TAG, "respones:" + respones);
    // return NoteInfo.JSONParsing(respones);
    // }
    //
    // @Override
    // public void onPreRequestSuccess(List<NoteInfo> data) {
    // adapter.addItemCollection(data);
    // adapter.notifyDataSetChanged();
    //
    // for (int i = 0; i < adapter.getCount(); i++) {
    // NoteInfo info = (NoteInfo) adapter.getItem(i);
    // if (!noteInfos.contains(info)) {
    // noteInfos.add(info);
    // }
    // }
    // }
    //
    // @Override
    // public List<NoteInfo> onPreRequestCache(String cache) throws Exception {
    // Log.i(TAG, "cache:" + cache);
    // return NoteInfo.JSONParsing(cache);
    // }
    //
    // @Override
    // public void exception(Exception exception) {
    //
    // }
    // };
    private void submitRecorder(String courseId, String videoId) {
        Log.i(TAG, "submitRecorder");
    }

    private class RecorderTimerTask extends TimerTask {

        @Override
        public void run() {
            handler.sendEmptyMessage(1);
        }
    }

    public void addListener() {
        player.setOnCompletionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer arg0) {
                arg0.reset();
                Intent it = new Intent();
                it.setAction("com.tming.videoview.play.resume");
                context.sendBroadcast(it);
                playLayout.setVisibility(View.GONE);
            }
        });
        
        cancelLayout.setOnClickListener(new OnClickListener() {
            
            @Override
            public void onClick(View v) {
                if (player.isPlaying()) {
                    player.stop();
                    player.reset();
                }
                Intent it = new Intent();
                it.setAction("com.tming.videoview.play.resume");
                context.sendBroadcast(it);
                playLayout.setVisibility(View.GONE);
            }
        });
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                NoteInfo noteInfo = noteInfos.get(arg2);
                playLayout.setVisibility(View.VISIBLE);
                totalTimeTv.setText("" + StringUtils.generateTime(noteInfo.totalTime));
                backgroundPlayVoice(noteInfo.noteUrl);
            }
        });
        speakBtn.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        isCancel = true;
                        Intent it = new Intent();
                        it.setAction("com.tming.videoview.play.pause");
                        context.sendBroadcast(it);
                        Log.i(TAG, "MotionEvent.ACTION_DOWN");
                        currentTime = System.currentTimeMillis();
                        fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator
                                + ((CourseDetailActivity) mActivity).getVideoCurrent() + ".3gp";
                        Log.i(TAG, "Recorder fileName:" + fileName);
                        speakBtn.setText("松开结束");
                        speakLayout.setVisibility(View.VISIBLE);
                        second = 0;
                        mediaRecorder = new MediaRecorder();
                        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        mediaRecorder.setOutputFile(fileName);
                        // mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory().getAbsolutePath());
                        try {
                            mediaRecorder.prepare();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        mediaRecorder.start();
                        task = new RecorderTimerTask();
                        timer.schedule(task, 1000, 1000);
                        break;
                    case MotionEvent.ACTION_MOVE:
                        Log.i(TAG, "MotionEvent.ACTION_MOVE");
                        if (event.getY() < 0) {
                            isCancel = false;
                            if (mediaRecorder != null) {
                                mediaRecorder.stop();
                                mediaRecorder.release();
                            }
                            task.cancel();
                            System.out.println("timer.cancel():" + second);
                            mediaRecorder = null;
                            speakLayout.setVisibility(View.GONE);
                            speakBtn.setText("按住说话记录笔记");
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        Intent itResume = new Intent();
                        itResume.setAction("com.tming.videoview.play.resume");
                        context.sendBroadcast(itResume);
                        if (System.currentTimeMillis() - currentTime <= 1000) {
                            Helper.ToastUtil(context, "录音时间太短");
                            speakBtn.setText("按住说话记录笔记");
                            speakLayout.setVisibility(View.GONE);
                            if (mediaRecorder != null) {
                                mediaRecorder.stop();
                                mediaRecorder.release();
                            }
                            task.cancel();
                            mediaRecorder = null;
                            break;
                        }
                        if (isCancel) {
                            Log.i(TAG, "MotionEvent.ACTION_UP");
                            speakBtn.setText("按住说话记录笔记");
                            speakLayout.setVisibility(View.GONE);
                            if (mediaRecorder != null) {
                                mediaRecorder.stop();
                                mediaRecorder.release();
                            }
                            task.cancel();
                            mediaRecorder = null;
                            backgroundUpload();
                        }
                        break;
                }
                return false;
            }
        });
    }

    private class UploadVoiceTask extends AsyncTask<Void, Void, Void> {

        private boolean flag = false;

        @Override
        protected Void doInBackground(Void... params) {
            System.out.println("++++++" + ((CourseDetailActivity) mActivity).getVideoCurrent());
            synchronized (this) {
//                flag = Utils.uploadFile(new File(fileName),
//                        ServerApi.getUploadNoteUrl(courseId, videoId, ((CourseDetailActivity) mActivity)
//                                .getVideoCurrent(), System.currentTimeMillis() - currentTime), "file");
                TmingResponse res = TmingHttp.syncSendFile(ServerApi.getUploadNoteUrl(courseId, videoId, ((CourseDetailActivity) mActivity)
                        .getVideoCurrent(), System.currentTimeMillis() - currentTime), "file", new File(fileName));
                if (res != null && res.getStatusCode() == 200) {
                    flag = true;
                } else {
                    flag = false;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (flag) {
                Helper.ToastUtil(context, "上传成功");
                noteInfos.clear();
                adapter.clear();
                requestNoteListData();
            } else {
                Helper.ToastUtil(context, "上传失败");
            }
        }
    }

    private void backgroundUpload() {
        uploadVoiceTask = new UploadVoiceTask();
        uploadVoiceTask.execute();
    }

    private class VoicePlayTask extends AsyncTask<Void, Void, Void> {

        private String url;

        @Override
        protected Void doInBackground(Void... params) {
            synchronized (this) {
                playVoice(url);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

        public void setUrl(String url) {
            this.url = url;
        }
    }

    private void backgroundPlayVoice(String url) {
        voicePlayTask = new VoicePlayTask();
        voicePlayTask.setUrl(url);
        voicePlayTask.execute();
    }

    private void playVoice(String url) {
        Log.i(TAG, "url:" + url);
        if (!StringUtils.isEmpty(url)) {
            Intent intent = new Intent();
            intent.setAction("com.tming.videoview.play.pause");
            context.sendBroadcast(intent);
            Log.i(TAG, "sendBroadcast finish");
            try {
                if (player.isPlaying()) {
                    Log.i(TAG, "player stop");
                    player.stop();
                    player.reset();
                }
                Log.i(TAG, "player start");
                player.setDataSource(url);
                player.prepare();
                player.start();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private int mCurrentPosition;
    public void pause() {
        if (player != null && player.isPlaying()) {
            player.pause();
            mCurrentPosition = player.getCurrentPosition();
        }
    }
    
    public void resume() {
        if (player != null) {
            player.seekTo(mCurrentPosition);
            player.start();
        }
    }
    
    public void onDestroy() {
        if (player != null) {
            Log.i(TAG, "onDestroy");
            player.stop();
            player.release();
        }
        if (null != timer) {
            timer.cancel();
        }
    }
}
