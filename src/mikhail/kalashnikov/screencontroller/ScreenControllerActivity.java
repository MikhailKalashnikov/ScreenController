package mikhail.kalashnikov.screencontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

public class ScreenControllerActivity extends Activity implements OnSharedPreferenceChangeListener, OnClickListener {
	protected static final String TAG = "ScreenControllerActivity";
	private ComponentName mDeviceAdmin;
	private DevicePolicyManager mDevicePolicyManager;
	private SharedPreferences mPref;
	private String mSensorMode;
	private boolean mPrefAutoLock;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDeviceAdmin = new ComponentName(this, DeviceAdminLockReceiver.class);
		mDevicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
		mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mPref.registerOnSharedPreferenceChangeListener(this);
		mSensorMode = mPref.getString(SettingsActivity.PREF_SENSOR_MODE, getResources().getString(R.string.sensor_mode_accelerometer_only));
		mPrefAutoLock = mPref.getBoolean(SettingsActivity.PREF_AUTO_LOCK, false);
		(findViewById(R.id.lock)).setOnClickListener(this);
		(findViewById(R.id.restart)).setOnClickListener(this);
		(findViewById(R.id.startService)).setOnClickListener(this);
		(findViewById(R.id.stopService)).setOnClickListener(this);
	}
	
	public void startWakeUpService(){
		Intent i = new Intent(this, WakeUpService.class);
		i.putExtra(WakeUpService.SENSOR_NAME, mSensorMode);
		i.putExtra(WakeUpService.IS_AUTO_LOCK, mPrefAutoLock);
		startService(i);
	}
	
	public boolean stopWakeUpService(){
		Intent i = new Intent(this, WakeUpService.class);
		return stopService(i);
	}
	
	public void lockScreen(){
		
		if(!mDevicePolicyManager.isAdminActive(mDeviceAdmin)){
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
            intent.putExtra("force-lock", DeviceAdminInfo.USES_POLICY_FORCE_LOCK);
            startActivity(intent);
		}else{
			mDevicePolicyManager.lockNow();
			mDevicePolicyManager.setMaximumTimeToLock(mDeviceAdmin, 0);
		}
		
	}

	public void restartPhone(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.msg_restart_confirmation)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "Restart");
					PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
					pm.reboot("test");
					
				}
			});
		builder.create().show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			startActivity(new Intent(this, SettingsActivity.class));
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {

		if(key.equals(SettingsActivity.PREF_SENSOR_MODE)){
			mSensorMode = mPref.getString(SettingsActivity.PREF_SENSOR_MODE, getResources().getString(R.string.sensor_mode_accelerometer_only));
			boolean serviceWasActive = stopWakeUpService();
			if(serviceWasActive){
				startWakeUpService();
			}
		}else if(key.equals(SettingsActivity.PREF_AUTO_LOCK)){
			mPrefAutoLock = mPref.getBoolean(SettingsActivity.PREF_AUTO_LOCK, false);
			boolean serviceWasActive = stopWakeUpService();
			if(serviceWasActive){
				startWakeUpService();
			}
		}
	}

	@Override
	protected void onPause() {
		if(mPref !=null){
			Editor editor = mPref.edit();
			editor.putString(SettingsActivity.PREF_SENSOR_MODE, mSensorMode);
			editor.putBoolean(SettingsActivity.PREF_AUTO_LOCK, mPrefAutoLock);
			editor.apply();
		}
		super.onPause();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.lock:
			lockScreen();
			break;
		
		case R.id.restart:
			restartPhone();
			break;
		
		case R.id.startService:
			startWakeUpService();
			break;
		
		case R.id.stopService:
			stopWakeUpService();
			break;
		}
		
	}
}
