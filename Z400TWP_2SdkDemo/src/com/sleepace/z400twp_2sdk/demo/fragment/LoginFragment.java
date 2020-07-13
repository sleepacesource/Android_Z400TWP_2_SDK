package com.sleepace.z400twp_2sdk.demo.fragment;

import java.util.HashMap;

import com.sleepace.sdk.interfs.IResultCallback;
import com.sleepace.sdk.manager.CallbackData;
import com.sleepace.sdk.manager.DeviceType;
import com.sleepace.sdk.util.SdkLog;
import com.sleepace.sdk.wifidevice.WiFiDeviceSdkHelper;
import com.sleepace.sdk.wifidevice.bean.IdentificationBean;
import com.sleepace.sdk.z400twp_2.Z400TWP2Helper;
import com.sleepace.z400twp_2sdk.demo.MainActivity;
import com.sleepace.z400twp_2sdk.demo.R;
import com.sleepace.z400twp_2sdk.demo.util.ActivityUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginFragment extends BaseFragment {
	private Button btnConnect, btnUpgrade, btnBindDevice, btnUnbindDevice;
	private EditText etServerAddress, etToken, etChannelId, etDeviceId, etVersion;
	private WiFiDeviceSdkHelper wifiDeviceSdkHelper;
	private Z400TWP2Helper z400twpHelper;
	private ProgressDialog progressDialog;
	private ProgressDialog upgradeDialog;
	private SharedPreferences mSetting;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View root = inflater.inflate(R.layout.fragment_login, null);
		// LogUtil.log(TAG+" onCreateView-----------");
		wifiDeviceSdkHelper = WiFiDeviceSdkHelper.getInstance(mActivity.getApplicationContext());
		z400twpHelper = Z400TWP2Helper.getInstance(mActivity.getApplicationContext());
		mSetting = mActivity.getSharedPreferences("config", Context.MODE_PRIVATE);
		findView(root);
		initListener();
		initUI();
		return root;
	}

	protected void findView(View root) {
		// TODO Auto-generated method stub
		super.findView(root);
		btnConnect = (Button) root.findViewById(R.id.btn_connect);
		btnUpgrade = (Button) root.findViewById(R.id.btn_upgrade_device);
		btnBindDevice = (Button) root.findViewById(R.id.btn_bind_device);
		btnUnbindDevice = (Button) root.findViewById(R.id.btn_unbind_device);
		etServerAddress = (EditText) root.findViewById(R.id.et_server_address);
		etToken = (EditText) root.findViewById(R.id.et_token);
		etChannelId = (EditText) root.findViewById(R.id.et_channel_id);
		etDeviceId = (EditText) root.findViewById(R.id.et_device_id);
		etVersion = (EditText) root.findViewById(R.id.et_device_version);
	}

	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		btnUpgrade.setOnClickListener(this);
		btnConnect.setOnClickListener(this);
		btnBindDevice.setOnClickListener(this);
		btnUnbindDevice.setOnClickListener(this);
	}

	protected void initUI() {
		// TODO Auto-generated method stub
		mActivity.setTitle(R.string.login);
		progressDialog = new ProgressDialog(mActivity);
		progressDialog.setIcon(android.R.drawable.ic_dialog_info);
		progressDialog.setMessage(getString(R.string.connecting_server));
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);

		upgradeDialog = new ProgressDialog(mActivity);
		upgradeDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);// 设置进度条的形式为圆形转动的进度条
		upgradeDialog.setMessage(getString(R.string.fireware_updateing, ""));
		upgradeDialog.setCancelable(false);
		upgradeDialog.setCanceledOnTouchOutside(false);
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		String serverHost = mSetting.getString("serverHost", "http://120.24.68.136:8091");
		etServerAddress.setText(serverHost);
		String token = mSetting.getString("token", "lisufang");
		etToken.setText(token);
		String channelId = mSetting.getString("channelId", "13700");
		etChannelId.setText(channelId);
		String deviceId = mSetting.getString("deviceId", MainActivity.deviceId);
		etDeviceId.setText(deviceId);
		String version = mSetting.getString("version", "2.53");
		etVersion.setText(version);
	}

	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Editor editor = mSetting.edit();
		String serverHost = etServerAddress.getText().toString().trim();
		if (!TextUtils.isEmpty(serverHost)) {
			editor.putString("serverHost", serverHost);
		}
		String token = etToken.getText().toString().trim();
		if (!TextUtils.isEmpty(token)) {
			editor.putString("token", token);
		}
		String channelId = etChannelId.getText().toString().trim();
		if (!TextUtils.isEmpty(channelId)) {
			editor.putString("channelId", channelId);
		}
		String deviceId = etDeviceId.getText().toString().trim();
		if (!TextUtils.isEmpty(deviceId)) {
			editor.putString("deviceId", deviceId);
		}
		String version = etVersion.getText().toString().trim();
		if (!TextUtils.isEmpty(version)) {
			editor.putString("version", version);
		}
		editor.commit();
	}

	@Override
	public void onDestroyView() {
		// TODO Auto-generated method stub
		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		super.onClick(v);
		String host = etServerAddress.getText().toString().trim();
		WiFiDeviceSdkHelper.initServerHost(host);

		if (v == btnConnect) {
			progressDialog.show();
			HashMap<String, Object> args = new HashMap<String, Object>();
			args.put("token", etToken.getText().toString().trim());
			args.put("channelId", etChannelId.getText().toString().trim());
			wifiDeviceSdkHelper.authorize(args, new IResultCallback() {
				@Override
				public void onResultCallback(final CallbackData cd) {
					// TODO Auto-generated method stub
					SdkLog.log(TAG + " authorize cd:" + cd);
					if (cd.isSuccess()) {
						final String deviceId = etDeviceId.getText().toString().trim();
						IdentificationBean bean = (IdentificationBean) cd.getResult();
						z400twpHelper.login(bean.getIp(), bean.getPort(), bean.getSid(), deviceId, new IResultCallback() {
							@Override
							public void onResultCallback(final CallbackData cd) {
								// TODO Auto-generated method stub
								// SdkLog.log(TAG+" login cd:" + cd);
								if (ActivityUtil.isActivityAlive(mActivity)) {
									mActivity.runOnUiThread(new Runnable() {
										@Override
										public void run() {
											// TODO Auto-generated method stub
											progressDialog.dismiss();
											if (cd.isSuccess()) {
												Toast.makeText(mActivity, R.string.connect_server_success, Toast.LENGTH_SHORT).show();
											} else {
												Toast.makeText(mActivity, R.string.failure, Toast.LENGTH_SHORT).show();
											}
										}
									});
								}
							}
						});
					} else {
						if (ActivityUtil.isActivityAlive(mActivity)) {
							mActivity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									progressDialog.dismiss();
									Toast.makeText(mActivity, R.string.failure, Toast.LENGTH_SHORT).show();
								}
							});
						}
					}
				}
			});
		} else if (v == btnUpgrade) {
			String deviceId = etDeviceId.getText().toString().trim();
			String strVersion = etVersion.getText().toString().trim();
			if (TextUtils.isEmpty(strVersion)) {
				return;
			}

			float deviceVersion = Float.valueOf(strVersion);
			upgradeDialog.show();
			z400twpHelper.upgradeDevice(deviceVersion, DeviceType.DEVICE_TYPE_Z400TWP_2.getType(), deviceId, new IResultCallback<Integer>() {
				@Override
				public void onResultCallback(final CallbackData<Integer> cd) {
					// TODO Auto-generated method stub
					if (ActivityUtil.isActivityAlive(mActivity)) {
						mActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								if (cd.isSuccess()) {
									Integer progress = cd.getResult();
									upgradeDialog.setProgress(progress);
									if (progress == 100) {
										upgradeDialog.dismiss();
										Toast.makeText(mActivity, R.string.update_completed, Toast.LENGTH_SHORT).show();
									}
								} else {
									upgradeDialog.dismiss();
									Toast.makeText(mActivity, R.string.failure, Toast.LENGTH_SHORT).show();
								}
							}
						});
					}
				}
			});
		} else if (v == btnBindDevice) {
			progressDialog.show();
			String deviceId = etDeviceId.getText().toString().trim();
			HashMap<String, Object> args = new HashMap<String, Object>();
			args.put("deviceId", deviceId);
			args.put("leftRight", 0);
			wifiDeviceSdkHelper.bindDevice(args, new IResultCallback() {
				@Override
				public void onResultCallback(final CallbackData cd) {
					// TODO Auto-generated method stub
					SdkLog.log(TAG + " bindDevice cd:" + cd);
					if (ActivityUtil.isActivityAlive(mActivity)) {
						mActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								progressDialog.dismiss();
								if (cd.isSuccess()) {
									Toast.makeText(mActivity, R.string.bind_device_success, Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(mActivity, R.string.failure, Toast.LENGTH_SHORT).show();
								}
							}
						});
					}
				}
			});
		} else if (v == btnUnbindDevice) {
			progressDialog.show();
			String deviceId = etDeviceId.getText().toString().trim();
			HashMap<String, Object> args = new HashMap<String, Object>();
			args.put("deviceId", deviceId);
			args.put("leftRight", 0);
			wifiDeviceSdkHelper.unbindDevice(args, new IResultCallback() {
				@Override
				public void onResultCallback(final CallbackData cd) {
					// TODO Auto-generated method stub
					SdkLog.log(TAG + " unbindDevice cd:" + cd);
					if (ActivityUtil.isActivityAlive(mActivity)) {
						mActivity.runOnUiThread(new Runnable() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								progressDialog.dismiss();
								if (cd.isSuccess()) {
									Toast.makeText(mActivity, R.string.unbind_device_success, Toast.LENGTH_SHORT).show();
								} else {
									Toast.makeText(mActivity, R.string.failure, Toast.LENGTH_SHORT).show();
								}
							}
						});
					}
				}
			});
		}
	}

}
