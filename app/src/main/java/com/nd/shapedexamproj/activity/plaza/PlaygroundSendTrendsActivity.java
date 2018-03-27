package com.nd.shapedexamproj.activity.plaza;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.my.PersonalInfo;
import com.nd.shapedexamproj.net.ServerApi;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.StringUtils;
import com.nd.shapedexamproj.util.UIHelper;
import com.nd.shapedexamproj.view.InputBottomBar;
import com.nd.shapedexamproj.view.myphoto.ImageGridActivity;
import com.nd.shapedexamproj.view.myphoto.PhotoActivity;
import com.nd.shapedexamproj.view.plaza.ShowGridViewImageView;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttpException;
import com.tming.common.net.TmingResponse;
import com.tming.common.util.*;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 发动态
 * 
 * @author Xujs
 * 
 */
public class PlaygroundSendTrendsActivity extends BaseActivity {

	private static final String TAG = "PlaygroundSendTrendsActivity";
	public static final int CHOOSEABLE_PIC_COUNT = 5;// 可选5张图片
	public static final String CHOOSEABLE_PIC_COUNT_TEXT = "5";// 可选5张图片字符串
	private static final int INPUT_MAX_LENGTH = 300;//最多300个字
    private static final int UPLOAD_IMAGE_MAX_SIZE = 1024;// 上传图片最大为1M
	private static final int INPUT_MIN_LENGHT = 1;
	
	private int screenHeight;
	private Context mContext;
	private Button commonHeadRightBtn;// 公用导航左边、右边按钮
	private ImageView commonheader_left_iv;
	private TextView commonheader_title_tv;
	private EditText sendTrendsET;// 写动态
	private String contentVal;// 用户输入信息
	// private LinearLayout sendPictureListLL;// 添加图片控件
	private ScrollView sendContextSC;
	private ShowGridViewImageView sendPictureListGV;// 图片
	private PicGridAdapter picAdapter;
	private LinearLayout sendFaceIconLL;
	private InputBottomBar mInputBottomBar;// 表情
	private LinearLayout tweetPubTitleLL;
	private RelativeLayout tweetPubViewpagerRL;
	
	private AlertDialog dialog;
	// private LinearLayout mSendPubMessageLl;
	private View loadingView;
	private RelativeLayout mSendFormRl;
	private final int PHOTO_IMAGE_BACK_VAL = 201;// 启动系统照相机拍照成功后返回的操作值
	private RelativeLayout mSendTrendsRL;
	private ImageView tweetPubFootbarFace;// 表情图标
	private FragmentManager fragmentManager;
	private InputMethodManager imm;// 软键盘管理类
//	private GridView mGridView;
	private ViewPager tweetPubViewpager;
	private RelativeLayout tweetPubViewpagerRl;

    //用来控制不能重复点击提交
	private PostContentTask mPostTask;

    private File mCaptureFile = null;
    private ArrayList<String> selectedImageFile = new ArrayList<String>();

	@Override
	public int initResource() {
		return R.layout.playground_send_common_stat;
	}

	@Override
	public void initComponent() {
		mContext = this;
		// 软键盘管理类
		imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
		fragmentManager = getSupportFragmentManager();
		tweetPubFootbarFace = (ImageView) findViewById(R.id.tweet_pub_footbar_face);
		tweetPubViewpager = (ViewPager) findViewById(R.id.tweet_pub_viewpager);
		tweetPubViewpagerRl = (RelativeLayout) findViewById(R.id.tweet_pub_viewpager_rl);
		// commonHeadLeftBtn = (Button) findViewById(R.id.common_head_left_btn);
		commonHeadRightBtn = (Button) findViewById(R.id.common_head_right_btn);
		commonheader_left_iv = (ImageView) findViewById(R.id.commonheader_left_iv);
		commonheader_title_tv = (TextView) findViewById(R.id.commonheader_title_tv);
		
		tweetPubTitleLL = (LinearLayout) findViewById(R.id.tweet_pub_title_ll);
		tweetPubViewpagerRL = (RelativeLayout) findViewById(R.id.tweet_pub_viewpager_rl);
		
		mSendTrendsRL = (RelativeLayout) findViewById(R.id.send_trends_rl);
		sendTrendsET = (EditText) findViewById(R.id.send_context_et);
		if (getIntent() != null) {
			int sendType = getIntent().getIntExtra("send_type", 0);
			if (sendType == 1) {
//				TimelineRequest.classid = getIntent().getStringExtra("classid");
//				TimelineRequest.teachingpointid = getIntent().getStringExtra(
//						"teachingpointid");
			} else {
				contentVal = getIntent().getStringExtra("content_val");
				sendTrendsET.setText(contentVal);
			}

		} else {
			sendTrendsET.setText("");
		}
		sendContextSC = (ScrollView) findViewById(R.id.send_context_sc);
		
		sendPictureListGV = (ShowGridViewImageView) findViewById(R.id.send_picture_list_gv);
		sendFaceIconLL = (LinearLayout) findViewById(R.id.send_icon_ll);
		sendPictureListGV.setSelector(new ColorDrawable(Color.TRANSPARENT));

		loadingView = findViewById(R.id.loading_layout);
		loadingView.setVisibility(View.GONE);
		mSendFormRl = (RelativeLayout) findViewById(R.id.send_context_ll);
		showIMM();
	}

	@Override
	public void initData() {
	    
	    screenHeight = getWindowManager().getDefaultDisplay().getHeight();
		commonheader_title_tv.setText(getResources().getString(
				R.string.playground_dynamic_but));
		commonHeadRightBtn.setText(getResources().getString(
				R.string.playground_send_but));

		sendTrendsET.setHint(getResources().getString(
				R.string.playground_dynamic_et));
		
		sendPictureListGV.setVisibility(View.VISIBLE);
		mInputBottomBar = new InputBottomBar(mContext, fragmentManager, sendFaceIconLL,sendTrendsET,INPUT_MAX_LENGTH);
		
		picAdapter = new PicGridAdapter(this);
		sendPictureListGV.setAdapter(picAdapter);
		sendPictureListGV.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				if (arg2 == selectedImageFile.size()) {
					// new PopupWindows(PlaygroundSendTrendsActivity.this,
					// noScrollgridview);
					showAddPicDialog();
				} else {
					Intent intent = new Intent(PlaygroundSendTrendsActivity.this, PhotoActivity.class);// 删除界面
                    intent.putStringArrayListExtra(PhotoActivity.EXTRA_DATA, selectedImageFile);
					intent.putExtra("ID", arg2);
					startActivityForResult(intent, PhotoActivity.REQUEST_CODE);
				}
				
				if (tweetPubViewpagerRl.getVisibility() == View.VISIBLE) {
                    // 隐藏表情
                    hideFace();
                } else {
                    imm.hideSoftInputFromWindow(sendTrendsET.getWindowToken(), 0);
                }
			}
		});
		
	}

    @Override
	public void addListener() {
		commonheader_left_iv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				
				if (!sendTrendsET.getText().toString().trim().equals("") || picAdapter.getCount() > 1) {//输入过文字或图片，进行提示
                    showFinishDialog();
                } else {
                    finishActivity();
                }
				
			}
		});
		// 点击发送按钮
		commonHeadRightBtn.setOnClickListener(new OnClickListener() {

			public void onClick(final View v) {
			    if (PhoneUtil.checkNetworkEnable() == PhoneUtil.NETSTATE_DISABLE) {
			        Toast.makeText(mContext, getResources().getString(R.string.please_open_network), Toast.LENGTH_SHORT).show();
			        return;
			    }
			    
			    String contentVal = sendTrendsET.getText().toString().trim();
			    if (picAdapter.getCount() <= 1 ) {
                    if (contentVal.length() < INPUT_MIN_LENGHT) {
                        UIHelper.ToastMessage(v.getContext(), getString(R.string.playground_null));
                        return;
                    }
			    } else if (StringUtils.isEmpty(contentVal)) {
                    contentVal = getResources().getString(R.string.share_photoes);
                }

			    if (mPostTask != null) {
                    Log.e("提交", "返回");
                    UIHelper.ToastMessage(v.getContext(), getString(R.string.playground_sending));
                    return ;
                }

                if (getCurrentFocus() != null && getCurrentFocus().getWindowToken() != null) {
                    // 隐藏表情选择框
                    IBinder token = PlaygroundSendTrendsActivity.this.getCurrentFocus().getWindowToken();
                    mInputBottomBar.getInputMethodManager().hideSoftInputFromWindow(token, 0);
                }

                loadingView.setVisibility(View.VISIBLE);
                mSendFormRl.setVisibility(View.VISIBLE);
                mPostTask = new PostContentTask(contentVal, selectedImageFile);
                mPostTask.execute();
			}
		});
        
		// 编辑器点击事件
		sendTrendsET.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// 显示软键盘
				showIMM();
			}
		});
		
	}
	
	
	private void showIMM() {
		tweetPubFootbarFace.setTag(1);
		showOrHideIMM();
	}
	
	private void showOrHideIMM() {
		if (tweetPubFootbarFace.getTag() == null) {
			// 隐藏软键盘
			imm.hideSoftInputFromWindow(sendTrendsET.getWindowToken(), 0);
			// 显示表情
			showFace();
		} else {
			// 显示软键盘
			imm.showSoftInput(sendTrendsET, InputMethodManager.SHOW_FORCED);
			// 隐藏表情
			hideFace();
		}
	}
	
	private void showFace() {
		tweetPubFootbarFace.setImageResource(R.drawable.widget_bar_keyboard);
		tweetPubFootbarFace.setTag(1);
		tweetPubViewpagerRl.setVisibility(View.VISIBLE);
	}
	
	private void hideFace() {
		tweetPubFootbarFace.setImageResource(R.drawable.widget_bar_face);
		tweetPubFootbarFace.setTag(null);
		tweetPubViewpagerRl.setVisibility(View.GONE);
	}

    private class PostContentTask extends AsyncTask<Void, Void, Boolean> {

        private StringBuilder mContent;
        private List<String> mImageFilePaths;
        private StringBuilder errorMsg = new StringBuilder();
        private String info = null;

        public PostContentTask(String content, List<String> imageFilePaths) {
            mContent = new StringBuilder(content);
            mImageFilePaths = imageFilePaths;
        }

        @Override
        protected void onPreExecute() {
            mSendFormRl.setVisibility(View.VISIBLE);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // 重新获取USER_ID，防止已经被回收
            String userID = App.getUserId();
            // 更新Constants中的CLASS_ID和TEACHING_POINT_ID
            try {
                TmingResponse response = TmingHttp.tmingHttpRequest(ServerApi.getUserInfoUrl(userID), null);
                PersonalInfo personalInfo = PersonalInfo.personalInfoJSONPasing(response.asString());
                App.sClassId = String.valueOf(personalInfo.getClassid());
                App.sTeachingPointId = personalInfo.getTeachingpointid();
            } catch (Throwable e) {
                e.printStackTrace();
                errorMsg.append("获取用户信息失败,请重试！");
                return false;
            }

            // 发送图片
			StringBuffer imageIdsStr = new StringBuffer();
            if (mImageFilePaths.size() > 0) {
                int count = mImageFilePaths.size();
                for (int i = 0; i < count; i++) {
                    String filePath = mImageFilePaths.get(i);
                    try {
                        File file = new File(filePath);
                        file = BitmapUtil.compressImageFileAndFixOritation(file, true, UPLOAD_IMAGE_MAX_SIZE, 1024);
						int[] bitmapSize = BitmapUtil.getBitmapSizeWithFile(file);
						HashMap<String, Object> uploadImageParams = new HashMap<String, Object>();
						uploadImageParams.put("imgfile", file);
						uploadImageParams.put("width", bitmapSize[0]);
						uploadImageParams.put("height", bitmapSize[1]);
						TmingResponse response = TmingHttp.syncSendFile(Constants.IMAGE_UPLOAD, uploadImageParams);
                        JSONObject resJson = response.asJSONObject();
                        if (resJson.getInt("flag") == 1) {
                            long imageId = resJson.getLong("imageId");
							imageIdsStr.append(imageId).append(",");
                        } else {
                            throw new Exception("upload image fail!!:" + file.getPath());
                        }
                    } catch (Throwable e) {
                        Log.d(TAG, "upload image fail:" + filePath, e);
                        errorMsg.append("上传图片失败，请重试！");
                        return false;
                    }
                }
            }

            // 发布帖子
            try {
                TmingResponse response = TmingHttp.tmingHttpRequest(ServerApi.getPublicTimelineUrl(mContent.toString(), imageIdsStr.toString()), null);
                JSONObject resJson = response.asJSONObject();
                if (resJson.getInt("flag") != 1) {
                    return false;
                }
                info = resJson.getString("info");
                FileUtil.deleteCreatedTempFiles();
            } catch (TmingHttpException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSuccess) {
            if (info != null) {
                UIHelper.ToastMessage(PlaygroundSendTrendsActivity.this, info);
            } else if (errorMsg.length() > 0) {
                UIHelper.ToastMessage(PlaygroundSendTrendsActivity.this, errorMsg.toString());
            }
            if (isSuccess) {
                // 发送更新广播
                Intent mIntent = new Intent(Constants.SEND_COMMENT_SUCCESS_REFRESH_LIST_ACTION);
                // 发送广播
                sendBroadcast(mIntent);
                finishActivity();
            } else {
                loadingView.setVisibility(View.GONE);
                mSendFormRl.setVisibility(View.VISIBLE);
            }
            mPostTask = null;
        }

        @Override
        protected void onCancelled() {
            mPostTask = null;
        }
    }

	private void showAddPicDialog() {
		// 列表长按事件
		Builder builder = new Builder(mContext);
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.playground_list_dialog, null);
		// 初始化Dialog中的控件
	    LinearLayout chooseToAlbumDialogLL = (LinearLayout) view
				.findViewById(R.id.playground_list_comment_ll);// 从相册中选择
	    LinearLayout photoDialogLL = (LinearLayout) view
				.findViewById(R.id.playground_person_info_ll);// 拍照
	    TextView chooseToAlbumDialogTV = (TextView) view
				.findViewById(R.id.playground_list_comment_tv);
	    TextView photoDialogTV = (TextView) view
				.findViewById(R.id.playground_person_info_tv);
		chooseToAlbumDialogTV.setText(getResources().getString(
				R.string.album_choose));
		photoDialogTV.setText(getResources().getString(R.string.photos_choose));

		builder.setView(view);// 设置自定义布局view
		dialog = builder.show();
		chooseToAlbumDialogLL.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 从手机相册中选择
				Intent intent = new Intent(PlaygroundSendTrendsActivity.this, ImageGridActivity.class);
                intent.putExtra(ImageGridActivity.DATA_EXTRA_IMAGE_LIST, selectedImageFile);
				startActivityForResult(intent, ImageGridActivity.CHOOSE_IMAGE_REQUEST_CODE);
				dialog.dismiss();
			}
		});

		photoDialogLL.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 点击拍照
                mCaptureFile = ActionUtil.Media.captureImage(PlaygroundSendTrendsActivity.this, 1, PHOTO_IMAGE_BACK_VAL);
                dialog.dismiss();
			}
		});

	}

    private void showFinishDialog() {
	    Builder builder = new Builder(mContext);
	    View view = LayoutInflater.from(mContext).inflate(
                R.layout.playground_list_dialog, null);
        // 初始化Dialog中的控件
        LinearLayout finishDialogLL = (LinearLayout) view
                .findViewById(R.id.playground_list_comment_ll);// 退出
        LinearLayout cancelDialogLL = (LinearLayout) view
                .findViewById(R.id.playground_person_info_ll);// 取消
        TextView quitDialogTV = (TextView) view
                .findViewById(R.id.playground_list_comment_tv);
        TextView continueDialogTV = (TextView) view
                .findViewById(R.id.playground_person_info_tv);
        quitDialogTV.setText(getResources().getString(
                R.string.playground_quit));
        continueDialogTV.setText(getResources().getString(R.string.playground_continue_edit));

        builder.setView(view);// 设置自定义布局view
        dialog = builder.show();
        finishDialogLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finishActivity();
            }
        });

        cancelDialogLL.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
	}
	
	private void finishActivity() {
        // 隐藏软键盘
        imm.hideSoftInputFromWindow(sendTrendsET.getWindowToken(), 0);
        
        PlaygroundSendTrendsActivity.this.finish();
	}

    MediaScannerConnection connection = null;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == PHOTO_IMAGE_BACK_VAL && resultCode == Activity.RESULT_OK) {
            if (mCaptureFile == null) {
                return;
            }
            // 通知系统更新图库
            connection = new MediaScannerConnection(this, new MediaScannerConnection.MediaScannerConnectionClient() {

                @Override
                public void onScanCompleted(String path, Uri uri) {
                    connection.disconnect();
                }

                @Override
                public void onMediaScannerConnected() {
                    if (null != mCaptureFile) {
                        connection.scanFile(mCaptureFile.toString(), "image/jpeg");
                    }
                }
            });
            connection.connect();
            selectedImageFile.add(mCaptureFile.getPath());
            picAdapter.notifyDataSetChanged();
        } else if (requestCode == ImageGridActivity.CHOOSE_IMAGE_REQUEST_CODE && resultCode == ImageGridActivity.CHOOSE_IMAGE_RESULT_CODE) {
            selectedImageFile = data.getStringArrayListExtra(ImageGridActivity.DATA_EXTRA_IMAGE_LIST);
            picAdapter.notifyDataSetChanged();
        } else if (requestCode == PhotoActivity.REQUEST_CODE && resultCode == PhotoActivity.RESULT_CODE) {
            selectedImageFile = data.getStringArrayListExtra(PhotoActivity.EXTRA_DATA);
            picAdapter.notifyDataSetChanged();
        }
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (tweetPubViewpagerRl.getVisibility() == View.VISIBLE) {
			// 隐藏表情
			hideFace();
		}
        picAdapter.notifyDataSetChanged();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (tweetPubViewpagerRl.getVisibility() == View.VISIBLE) {
				// 隐藏表情
				hideFace();
			} else {
			    if (!sendTrendsET.getText().toString().trim().equals("") || picAdapter.getCount() > 1) {//输入过文字或图片，进行提示
	                showFinishDialog();
	            } else {
	                return super.onKeyDown(keyCode, event);
	            }
				
			}
		}
		return true;
	}
	
	@Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            
            int bottomBarHeight = tweetPubViewpagerRL.getHeight() + tweetPubTitleLL.getHeight();
            int bottom = screenHeight - bottomBarHeight;
            // 获得当前得到焦点的View，一般情况下就是EditText（特殊情况就是轨迹求或者实体案件会移动焦点）
            View v = getCurrentFocus();

            if (isShouldHideInput(v, ev, bottom)) {
                if (tweetPubViewpagerRl.getVisibility() == View.VISIBLE) {
                    // 隐藏表情
                    hideFace();
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

	 /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时没必要隐藏
     * 
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideInput(View v, MotionEvent event, int bottom) {
        if (v != null && (v instanceof EditText)) {
            int[] l = { 0, 0 };
            v.getLocationInWindow(l);
            int left = l[0], top = l[1],  right = left + v.getWidth();
            /*int bottom = top + v.getHeight();*/
            if (event.getX() > left && event.getX() < right && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return true;
            } else {
                return false;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditView上，和用户用轨迹球选择其他的焦点
        return false;
    }


	public class PicGridAdapter extends BaseAdapter {
		private int selectedPosition = -1;// 选中的位置
		private boolean shape;
        int showThumbnailSize = -1;

        public boolean isShape() {
			return shape;
		}

		public void setShape(boolean shape) {
			this.shape = shape;
		}

		public PicGridAdapter(Context context) {
            showThumbnailSize = Helper.dip2px(PlaygroundSendTrendsActivity.this, 72);
		}

		public int getCount() {
			return (selectedImageFile.size() + 1);
		}

		public Object getItem(int arg0) {

			return null;
		}

		public long getItemId(int arg0) {

			return 0;
		}

		public void setSelectedPosition(int position) {
			selectedPosition = position;
		}

		public int getSelectedPosition() {
			return selectedPosition;
		}

		/**
		 * ListView Item设置
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			final int coord = position;
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = View.inflate(PlaygroundSendTrendsActivity.this, R.layout.timeline_gridview_img2, null);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView.findViewById(R.id.item_grida_image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

            if (position == selectedImageFile.size()) {
                holder.image.setImageResource(R.drawable.icon_addpic_unfocused);
                if (position == CHOOSEABLE_PIC_COUNT) {
                    holder.image.setVisibility(View.GONE);
                }
            } else {
                try {
                    File file = new File(selectedImageFile.get(position));
                    holder.image.setImageBitmap(BitmapUtil.loadImage(file, showThumbnailSize, showThumbnailSize, true));
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                }
            }
			return convertView;
		}

		public class ViewHolder {
			public ImageView image;
		}
	}

	public String getString(String s) {
		String path = null;
		if (s == null)
			return "";
		for (int i = s.length() - 1; i > 0; i++) {
			s.charAt(i);
		}
		return path;
	}


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList("selectedImageList", selectedImageFile);
        if (mCaptureFile != null) {
            outState.putString("captureFile", mCaptureFile.getPath());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        App.initCommonApp(getApplicationContext());
        selectedImageFile = savedInstanceState.getStringArrayList("selectedImageList");
        String captureFilePath = savedInstanceState.getString("captureFile");
        if (captureFilePath != null) {
            mCaptureFile = new File(captureFilePath);
        }
    }

}
