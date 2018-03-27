package com.nd.shapedexamproj.activity.inner;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.*;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.*;
import android.widget.AdapterView.OnItemClickListener;

import com.nd.shapedexamproj.R;
import com.nd.shapedexamproj.activity.BaseActivity;
import com.nd.shapedexamproj.model.TeachPoints;
import com.nd.shapedexamproj.util.Constants;
import com.nd.shapedexamproj.util.UIHelper;
import com.tming.common.net.TmingHttp;
import com.tming.common.util.Helper;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * 在线报名 步骤1<br/>
 * gps定位，显示附近报名点。
 *
 * @version 1.0.0
 * @author Abay Zhuang <br/>
 *         Create at 2014-5-23<br/>
 *         修改者，修改日期，修改内容。
 */
public class Registration extends BaseActivity {
	private final static String TAG = Registration.class.getSimpleName();
	private TeachPoints mTeachPoints;
	private TextView mTitleTv;
	private Button mNextBtn;
	private ImageView mBackImgV;
	private TextView mLocationTv;
	private RelativeLayout mTeachPointMoreRl;
	private TextView mTeachPointMoreTv;
	private LinearLayout mTeachPointLl;
	private ListView mTeachPointLstV;
	private RegistrationAdapter mAdapter;

	@Override
	public int initResource() {
		return R.layout.inner_registration;
	}

	@Override
	public void initComponent() {
		mLocationTv = (TextView) findViewById(R.id.inner_registration_location_tv);
		mTeachPointMoreTv = (TextView) findViewById(R.id.registration_teachpoint_more_tv);
		mTeachPointMoreRl = (RelativeLayout) findViewById(R.id.registration_teachpoint_more_rl);
		mTeachPointLl = (LinearLayout) findViewById(R.id.registration_teachpoint_ll);
		mTeachPointLstV = (ListView) findViewById(R.id.registration_teachpoint_lstv);
		mTitleTv = (TextView) findViewById(R.id.commonheader_title_tv);
		mTitleTv.setText(R.string.online_registration_header);
		mNextBtn = (Button) findViewById(R.id.registration_next_btn);
		mBackImgV = (ImageView) findViewById(R.id.commonheader_left_iv);
	}

	private void openGPSSettings() {
		LocationManager alm = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);
		if (alm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			//Toast.makeText(this, "GPS模块正常", Toast.LENGTH_SHORT).show();
			doWork();
			return;
		}

		Toast.makeText(this, "请开启GPS！", Toast.LENGTH_SHORT).show();
		Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
		startActivityForResult(intent, 0); // 此为设置完成后返回到获取界面

	}

	private void doWork() {
		String msg = "";

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Criteria criteria = new Criteria();
		// 获得最好的定位效果
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		// 使用省电模式
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		// 获得当前的位置提供者
		String provider = locationManager.getBestProvider(criteria, true);
		// 获得当前的位置
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		Location location = locationManager.getLastKnownLocation(provider);
		if (location == null){
			Log.e(TAG, "location in null ");
			Toast.makeText(Registration.this, "无法获取地理信息", Toast.LENGTH_SHORT);
			return;
		}

		Geocoder gc = new Geocoder(this);
		List<Address> addresses = null;
		try {
			addresses = gc.getFromLocation(location.getLatitude(),
					location.getLongitude(), 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (addresses.size() > 0) {
			msg += "AddressLine：" + addresses.get(0).getAddressLine(0) + "\n";
			msg += "CountryName：" + addresses.get(0).getCountryName() + "\n";
			msg += "Locality：" + addresses.get(0).getLocality() + "\n";
			msg += "FeatureName：" + addresses.get(0).getFeatureName();
		}
		Log.e(TAG, "ss " + msg);
	}

	@Override
	public void initData() {
		mAdapter = new RegistrationAdapter(Registration.this, mTeachPoints);
		mTeachPointLstV.setAdapter(mAdapter);
		openGPSSettings();
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("area", "福州");

		TmingHttp.asyncRequest(Constants.TEACHING_POINT, map,
				new TmingHttp.RequestCallback<TeachPoints>() {

					@Override
					public TeachPoints onReqestSuccess(String respones)
							throws Exception {
						TeachPoints.Result result = TeachPoints
								.getObject(respones);
						if (result.flag == 1)
							return result.res.data;
						return null;
					}

					@Override
					public void success(TeachPoints respones) {
						if (respones != null) {
							Log.e(TAG, "success " + respones.getTotal());
							mTeachPoints = respones;
							mAdapter.setTeachPoints(respones);
						}

						updateView();
					}

					@Override
					public void exception(Exception exception) {
						Helper.ToastUtil(Registration.this, "网络错误");
						updateView();

					}
				});

	}

	public void updateView(){
		if (mTeachPoints != null){
			mTeachPointMoreTv.setText(String.format(getText(R.string.online_registration_first_teach_point_more).toString(), mTeachPoints.getTotal()));
		} else { 
			mTeachPointMoreTv.setText("没有相关内容");
		}
		
	}
	
	
	@Override
	public void addListener() {
		mBackImgV.setOnClickListener(UIHelper.finish(this));
		mNextBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				UIHelper.showOnlineRegisterSecond(Registration.this);
			}
		});
		mTeachPointMoreRl.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
					mTeachPointLl.setVisibility(View.GONE);
					mTeachPointLstV.setVisibility(View.VISIBLE);
			}
		});
		
		mTeachPointLstV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				
				mAdapter.setSelected(arg2);
				
			}
		});
	}

}
