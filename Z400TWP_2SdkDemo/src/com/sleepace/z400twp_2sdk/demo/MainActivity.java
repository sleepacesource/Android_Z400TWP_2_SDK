package com.sleepace.z400twp_2sdk.demo;

import java.util.ArrayList;
import java.util.List;

import com.sleepace.sdk.domain.BleDevice;
import com.sleepace.sdk.interfs.IConnectionStateCallback;
import com.sleepace.sdk.interfs.IDeviceManager;
import com.sleepace.sdk.manager.CONNECTION_STATE;
import com.sleepace.sdk.util.SdkLog;
import com.sleepace.z400twp_2sdk.demo.fragment.DeviceFragment;
import com.sleepace.z400twp_2sdk.demo.fragment.LoginFragment;
import com.sleepace.z400twp_2sdk.demo.fragment.ReportFragment;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class MainActivity extends BaseActivity {
	
	private RadioGroup rgTab;
	private RadioButton rbDevice;
	private FragmentManager fragmentMgr;
	private Fragment loginFragment, deviceFragment, reportFragment;
	private BleDevice device;
	private ProgressDialog upgradeDialog;
	
	//缓存数据
	public static String deviceName, power, version;
	public static byte collectStatus = -100;
	public static final int userId = 33131;
//	public static final String deviceId = "y24mlthq3fxga";
	public static String deviceId = "";
	
	private final int requestCode = 101;//权限请求码
    private boolean hasPermissionDismiss = false;//有权限没有通过
    private List<String> unauthoPersssions = new ArrayList<String>();
    private String[] permissions = new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		findView();
		initListener();
		initUI();
		checkPermissions();
	}
	
	private void checkPermissions() {
		if(Build.VERSION.SDK_INT >= 23) {
			unauthoPersssions.clear();
			//逐个判断你要的权限是否已经通过
			for (int i = 0; i < permissions.length; i++) {
				if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
					unauthoPersssions.add(permissions[i]);//添加还未授予的权限
				}
			}
			//申请权限
			if (unauthoPersssions.size() > 0) {//有权限没有通过，需要申请
				ActivityCompat.requestPermissions(this, new String[]{unauthoPersssions.get(0)}, requestCode);
			}
		}
    }
	
	@Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        hasPermissionDismiss = false;
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (this.requestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    hasPermissionDismiss = true;
                    break;
                }
            }

            //如果有权限没有被允许
            if (hasPermissionDismiss) {
            	
            }else{
                checkPermissions();
            }
        }
    }
	
	@Override
	protected void findView() {
		// TODO Auto-generated method stub
		super.findView();
		rgTab = (RadioGroup) findViewById(R.id.rg_tab);
		rbDevice = (RadioButton) findViewById(R.id.rb_device);
	}


	@Override
	protected void initListener() {
		// TODO Auto-generated method stub
		super.initListener();
		rgTab.setOnCheckedChangeListener(checkedChangeListener);
	}


	@Override
	protected void initUI() {
		// TODO Auto-generated method stub
		super.initUI();
		device = (BleDevice) getIntent().getSerializableExtra("device");
		fragmentMgr = getFragmentManager();
		loginFragment = new LoginFragment();
		deviceFragment = new DeviceFragment();
		reportFragment = new ReportFragment();
		rbDevice.setChecked(true);
		ivBack.setImageResource(R.drawable.tab_btn_scenes_home);
		
		upgradeDialog = new ProgressDialog(this);
		upgradeDialog.setMessage(getString(R.string.fireware_updateing, ""));
		upgradeDialog.setCancelable(false);
		upgradeDialog.setCanceledOnTouchOutside(false);
	}
	
	
	public void setTitle(int res){
		tvTitle.setText(res);
	}
	
	public BleDevice getDevice() {
		return device;
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if(v == ivBack){
			exit();
		}
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			exit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	public void exit(){
		clearCache();
//		Intent intent = new Intent(this, SplashActivity.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		startActivity(intent);
		finish();
	}
	
	
	private void clearCache(){
		collectStatus = -100;
		deviceName = null;
		power = null;
		version = null;
	}
	
	private IConnectionStateCallback stateCallback = new IConnectionStateCallback() {
		@Override
		public void onStateChanged(IDeviceManager manager, CONNECTION_STATE state) {
			// TODO Auto-generated method stub
			SdkLog.log(TAG+" onStateChanged state:" + state);
			if(state == CONNECTION_STATE.DISCONNECT){
				collectStatus = -1;
			}
		}
	};
	
	
	private OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			// TODO Auto-generated method stub
			FragmentTransaction trans = fragmentMgr.beginTransaction();
			if(checkedId == R.id.rb_device){
				trans.replace(R.id.content, loginFragment);
			}else if(checkedId == R.id.rb_control){
				trans.replace(R.id.content, deviceFragment);
			}else if(checkedId == R.id.rb_data){
				trans.replace(R.id.content, reportFragment);
			}
			trans.commit();
		}
	};
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == RESULT_OK){
			
		}
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	public void showUpgradeDialog(){
		upgradeDialog.show();
	}
	
	public void hideUpgradeDialog(){
		upgradeDialog.dismiss();
	}
	
}








































