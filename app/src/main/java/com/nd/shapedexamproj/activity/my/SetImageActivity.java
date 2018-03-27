package com.nd.shapedexamproj.activity.my;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;

import com.nd.shapedexamproj.App;
import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.ImageResult;
import com.nd.shapedexamproj.util.Constants;
import com.tming.common.cache.image.ImageCacheTool;
import com.tming.common.cache.net.TmingCacheHttp;
import com.tming.common.net.TmingHttp;
import com.tming.common.net.TmingHttp.RequestCallback;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 * 
 * 编辑封面&头像
 * 
 * @author Linlg
 * 
 *         Create on 2014-4-2
 */
public class SetImageActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = SetImageActivity.class.getSimpleName();
	private RelativeLayout beforeLayout, afterLayout, photoLayout,
			cameraLayout;
	private Button cancelBtn, settingBtn;
	private ImageView pictureIv;
	private TmingCacheHttp cacheHttp;
	private Button rightBtn;
	private File file;
	private File tempFile = new File(Environment.getExternalStorageDirectory(),
			getPhotoFileName());
	private View submitView;
	private Bitmap photo;
	private boolean photoOpened = false;//added by Caiyx on 2014-09-02

	/*
	 * @Override protected void onCreate(Bundle savedInstanceState) {
	 * super.onCreate(savedInstanceState); cacheHttp =
	 * TmingCacheHttp.getInstance(this); setContentView(R.layout.my_change_img);
	 * initView(); }
	 */

	/**
	 * 初始化视图
	 */
	private void initView() {
		((TextView) findViewById(R.id.mychangeimg_head_layout).findViewById(
				R.id.commonheader_title_tv)).setText(getIntent()
				.getStringExtra("title"));
		((ImageView) findViewById(R.id.mychangeimg_head_layout).findViewById(
				R.id.commonheader_left_iv)).setOnClickListener(this);
		rightBtn = (Button) findViewById(R.id.commonheader_right_btn);
		submitView = findViewById(R.id.my_change_img_sumbit);
		rightBtn.setVisibility(View.GONE);
		pictureIv = (ImageView) findViewById(R.id.mychangeimg_image_iv);
		beforeLayout = (RelativeLayout) findViewById(R.id.mychangeimg_before_rl);
		afterLayout = (RelativeLayout) findViewById(R.id.mychangeimg_after_rl);
		photoLayout = (RelativeLayout) findViewById(R.id.mychangeimg_photo_rl);
		photoLayout.setOnClickListener(this);
		cameraLayout = (RelativeLayout) findViewById(R.id.mychangeimg_camera_rl);
		cameraLayout.setOnClickListener(this);
		cancelBtn = (Button) findViewById(R.id.mychangeimg_cancel_btn);
		cancelBtn.setOnClickListener(this);
		settingBtn = (Button) findViewById(R.id.mychangeimg_setting_btn);
		settingBtn.setOnClickListener(this);
		findViewById(R.id.mychangeimg_head_layout).findViewById(
				R.id.commonheader_left_iv);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.commonheader_left_iv:
			finish();
			break;
		case R.id.mychangeimg_photo_rl:
			if (!photoOpened) {//modified by Caiyx on 2014-09-02
				Intent it = new Intent();
				it.setType("image/*");
				it.setAction(Intent.ACTION_GET_CONTENT);
				startActivityForResult(it, 100);
				photoOpened = true;
			}

			break;
		case R.id.mychangeimg_camera_rl:
			file = new File(Environment.getExternalStorageDirectory(),
					getPhotoFileName());
			Intent cameraintent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
			cameraintent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
			startActivityForResult(cameraintent, 200);

			break;
		case R.id.mychangeimg_cancel_btn:
			afterLayout.setVisibility(View.GONE);
			beforeLayout.setVisibility(View.VISIBLE);
			break;

		case R.id.mychangeimg_setting_btn:
		    
			submitView.setVisibility(View.VISIBLE);
			if (file == null)
				return;
			
			uploadPhoto();

			break;
		}
	}

	public void uploadPhoto() {
		try {
		    if (App.createDownloadPath() == null) {
		        return;
            }
			HashMap<String, Object> params = new HashMap<String, Object>();
			params.put("userid", App.getUserId());
			params.put("userpic",
					encodeBase64File(new File(App.createDownloadPath()
							+ "myPhoto.jpg")));

			TmingHttp.asyncRequest(Constants.HOST_DEBUG + "user/editPic.html",
					params, new RequestCallback<ImageResult>() {

						@Override
						public ImageResult onReqestSuccess(String respones)
								throws Exception {
							return ImageResult.parseJson(respones);
						}

						@Override
						public void success(ImageResult respones) {

							if (respones != null && respones.flag == 1) {
								submitView.setVisibility(View.GONE);

								Log.e(TAG, "photo : " + respones.res.data.info.get(0).userpic);
                                String imageUrl = respones.res.data.info.get(0).userpic;
                                ImageCacheTool.getInstance().removeCache(imageUrl);

                                Toast.makeText(SetImageActivity.this, "设置成功", Toast.LENGTH_LONG).show();
                                Intent it = new Intent("com.tming.kd.my.resetimg");
                                it.putExtra("newImageUrl", imageUrl);
                                sendBroadcast(it);
                                SetImageActivity.this.finish();
							} else {
								Toast.makeText(SetImageActivity.this, "设置失败",
										Toast.LENGTH_LONG).show();
							}
						}

						@Override
						public void exception(Exception exception) {
                            exception.printStackTrace();;
                            Toast.makeText(SetImageActivity.this, "设置失败", Toast.LENGTH_LONG).show();
						}
					});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode != RESULT_OK) {
			afterLayout.setVisibility(View.GONE);
			beforeLayout.setVisibility(View.VISIBLE);
			photoOpened = false;//added by Caiyx on 2014-09-02
			return;
		}
		if (requestCode == 100) {
			beforeLayout.setVisibility(View.GONE);
			afterLayout.setVisibility(View.VISIBLE);
			Uri uri = data.getData();
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = managedQuery(uri, proj, null, null, null);
			if(cursor == null){
				afterLayout.setVisibility(View.GONE);
				beforeLayout.setVisibility(View.VISIBLE);
				return;
			}
			int index = cursor.getColumnIndexOrThrow(proj[0]);
			cursor.moveToFirst();
			String imgPath = cursor.getString(index);
			Log.e(TAG, "onActivityResult imgPath:=" + imgPath);
			if (imgPath == null) {
			    return;
			}
			ContentResolver cr = SetImageActivity.this.getContentResolver();
			// try {
			file = new File(imgPath);
			// Bitmap bitmap =
			// BitmapFactory.decodeStream(cr.openInputStream(uri));
			// pictureIv.setImageBitmap(bitmap);
			startPhotoZoom(Uri.fromFile(file));
			// } catch (FileNotFoundException e) {
			// e.printStackTrace();
			// }
			
			photoOpened = false;//added by Caiyx on 2014-09-02
		} else if (requestCode == 200) {
			if (file != null && file.exists()) {
				startPhotoZoom(Uri.fromFile(file));	
			}
		} else if (requestCode == 300) {
			if (data != null) {
				sentPicToNext(data);
				beforeLayout.setVisibility(View.GONE);
				afterLayout.setVisibility(View.VISIBLE);
			}
		}

	}

	private void startPhotoZoom(Uri uri) {
		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setDataAndType(uri, "image/*");
		// crop为true是设置在开启的intent中设置显示的view可以剪裁
		intent.putExtra("crop", "true");

		// aspectX aspectY 是宽高的比例
		intent.putExtra("aspectX", 1);
		intent.putExtra("aspectY", 1);

		// outputX,outputY 是剪裁图片的宽高
		intent.putExtra("outputX", 150);
		intent.putExtra("outputY", 150);
		intent.putExtra("return-data", true);
		intent.putExtra("noFaceDetection", true);
		startActivityForResult(intent, 300);
	}

	// 将进行剪裁后的图片传递到下一个界面上
	private void sentPicToNext(Intent picdata) {
		Bundle bundle = picdata.getExtras();
		if (bundle != null) {
			photo = bundle.getParcelable("data");
			if (photo == null) {
				return;
			} else {
			    
			    FileOutputStream out = null;
	            try {
	                if (App.createDownloadPath() == null) {
	                    return;
	                }
	                out = new FileOutputStream(new File(App.createDownloadPath() + "myPhoto.jpg"));
	                photo.compress(Bitmap.CompressFormat.JPEG, 100, out);
	            } catch (Exception e) {
	                e.getStackTrace();
	            } finally {
	                if (out != null) {
	                    try {
	                        out.flush();
	                        out.close();
	                    } catch (Exception e) {
	                        e.printStackTrace();
	                    }
	                } 
	            }
			    
				pictureIv.setImageBitmap(photo);
			}

			
		}
	}

	/**
	 * 系统日期时间作为照片名称
	 */
	private String getPhotoFileName() {
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"'IMG'_yyyyMMdd_HHmmss");
		return dateFormat.format(date) + ".jpg";
	}

	/**
	 * 图片上传
	 */
	public boolean uploadFile(File file, String RequestURL, String name) {
		int time_out = 120 * 1000;
		String result = null;
		String BOUNDARY = UUID.randomUUID().toString(); // 边界标识 随机生成
		String PREFIX = "--", LINE_END = "\r\n";
		String CONTENT_TYPE = "multipart/form-data"; // 内容类型

		try {
			URL url = new URL(RequestURL);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(time_out);
			conn.setConnectTimeout(time_out);
			conn.setDoInput(true); // 允许输入流
			conn.setDoOutput(true); // 允许输出流
			conn.setUseCaches(false); // 不允许使用缓存
			conn.setRequestMethod("POST"); // 请求方式
			conn.setRequestProperty("Charset", "UTF-8"); // 设置编码
			conn.setRequestProperty("connection", "keep-alive");
			conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="
					+ BOUNDARY);

			if (file != null) {
				DataOutputStream dos = new DataOutputStream(
						conn.getOutputStream());
				StringBuffer sb = new StringBuffer();
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINE_END);
				sb.append("Content-Disposition: form-data; name=\"ringname\""
						+ LINE_END);
				sb.append(LINE_END);
				sb.append(name + LINE_END);

				sb.append(PREFIX + BOUNDARY + LINE_END);

				sb.append("Content-Disposition: form-data; name=\"" + name
						+ "\"; filename=\"" + file.getParent() + "\""
						+ LINE_END);
				sb.append("Content-Type: application/octet-stream; charset=UTF-8"
						+ LINE_END);
				sb.append(LINE_END);
				dos.write(sb.toString().getBytes());
				InputStream is = new FileInputStream(file);
				byte[] bytes = new byte[1024];
				int len = 0;
				while ((len = is.read(bytes)) != -1) {
					dos.write(bytes, 0, len);
				}
				is.close();
				dos.write(LINE_END.getBytes());
				byte[] end_data = (PREFIX + BOUNDARY + PREFIX + LINE_END)
						.getBytes();
				dos.write(end_data);
				dos.flush();
				/**
				 * 获取响应码 200=成功 当响应成功，获取响应的流
				 */
				int res = conn.getResponseCode();
				InputStream input = conn.getInputStream();
				StringBuffer sb1 = new StringBuffer();
				int ss;
				while ((ss = input.read()) != -1) {
					sb1.append((char) ss);
				}
				result = sb1.toString();
				System.out.println("upload:" + result);
				int flag = 0;
				try {
					flag = new JSONObject(result).getInt("flag");
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (flag != 0) {
					return true;
				}
				return false;
			}
			return false;
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int initResource() {
		return R.layout.my_change_img;
	}

	@Override
	public void initComponent() {
		cacheHttp = TmingCacheHttp.getInstance(this);
		initView();
	}

	@Override
	public void initData() {

	}

	@Override
	public void addListener() {

	}

	public static String encodeBase64File(File file) throws Exception {
		FileInputStream inputFile = new FileInputStream(file);
		byte[] buffer = new byte[(int) file.length()];
		inputFile.read(buffer);
		inputFile.close();
		return Base64.encodeToString(buffer, Base64.DEFAULT);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (photo != null) {
			photo.recycle();
			Log.e(TAG, "photo:" + photo);
			photo = null;
		}
	}
}
