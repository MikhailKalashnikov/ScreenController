package mikhail.kalashnikov.screencontroller;

import android.app.admin.DeviceAdminInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class SettingsActivity extends PreferenceActivity
	implements OnSharedPreferenceChangeListener{
	public final static String PREF_SENSOR_MODE = "pref_sensor_mode";
	public final static String PREF_AUTO_LOCK = "pref_auto_lock";
	public final static String PREF_WAKE_UP = "pref_wake_up";
	private SharedPreferences mPref;
	private String mSensorMode;
	private boolean mPrefAutoLock;
	private boolean mPrefWakeUpEnabled;
	private ComponentName mDeviceAdmin;
	private DevicePolicyManager mDevicePolicyManager;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		ListPreference prefSensorModes = (ListPreference) findPreference(PREF_SENSOR_MODE);
		prefSensorModes.setSummary(prefSensorModes.getEntry());
		
		mPref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		mPref.registerOnSharedPreferenceChangeListener(this);
		mSensorMode = mPref.getString(SettingsActivity.PREF_SENSOR_MODE, WakeUpSensorManager.SENSOR_MODE_PROXIMITY_ONLY);
		mPrefAutoLock = mPref.getBoolean(SettingsActivity.PREF_AUTO_LOCK, false);
		mPrefWakeUpEnabled = mPref.getBoolean(SettingsActivity.PREF_WAKE_UP, false);
		mDeviceAdmin = new ComponentName(this, DeviceAdminLockReceiver.class);
		mDevicePolicyManager = (DevicePolicyManager)getSystemService(Context.DEVICE_POLICY_SERVICE);
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals(PREF_SENSOR_MODE)){
			@SuppressWarnings("deprecation")
			ListPreference prefSensorModes = (ListPreference) findPreference(PREF_SENSOR_MODE);
			prefSensorModes.setSummary(prefSensorModes.getEntry());
		}
		boolean mPrefAutoLockOld = mPrefAutoLock;
		boolean mPrefWakeUpEnabledOld = mPrefWakeUpEnabled;
		if(key.equals(SettingsActivity.PREF_SENSOR_MODE)){
			mSensorMode = mPref.getString(SettingsActivity.PREF_SENSOR_MODE, WakeUpSensorManager.SENSOR_MODE_PROXIMITY_ONLY);
		}else if(key.equals(SettingsActivity.PREF_AUTO_LOCK)){
			mPrefAutoLock = mPref.getBoolean(SettingsActivity.PREF_AUTO_LOCK, false);
		}else if(key.equals(SettingsActivity.PREF_WAKE_UP)){
			mPrefWakeUpEnabled = mPref.getBoolean(SettingsActivity.PREF_WAKE_UP, false);
		}
		
		if(mPrefAutoLock || !(mPrefWakeUpEnabled)){
			mSensorMode = WakeUpSensorManager.SENSOR_MODE_PROXIMITY_ONLY;
		}
		stopWakeUpService();
		if(mPrefAutoLock || mPrefWakeUpEnabled){
			startWakeUpService();
		}
		
		if((mPrefWakeUpEnabled || mPrefAutoLock) // if was disabled. and now enabled then enable bootreceiver
				&& (!mPrefWakeUpEnabledOld && !mPrefAutoLockOld)){ 
			ComponentName receiver = new ComponentName(this, BootReceiver.class);
			PackageManager pm = getPackageManager();
			pm.setComponentEnabledSetting(receiver,        
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED,        
					PackageManager.DONT_KILL_APP);
			
		}else if((!mPrefWakeUpEnabled && !mPrefAutoLock) // if was enabled, and now disabled. then disable bootreceiver
				&& (mPrefWakeUpEnabledOld || mPrefAutoLockOld)){ 
			ComponentName receiver = new ComponentName(this, BootReceiver.class);
			PackageManager pm = getPackageManager();
			pm.setComponentEnabledSetting(receiver,        
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED,        
					PackageManager.DONT_KILL_APP);
		}
		
		
		if(mPrefAutoLock && !mPrefAutoLockOld && !mDevicePolicyManager.isAdminActive(mDeviceAdmin)){ // if enable auto-lock check that DeviceAdmin is enabled 
			Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
			intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdmin);
	        intent.putExtra("force-lock", DeviceAdminInfo.USES_POLICY_FORCE_LOCK);
	        startActivity(intent);
		}
	}
	
	public void startWakeUpService(){
		Intent i = new Intent(this, WakeUpService.class);
		i.putExtra(WakeUpService.SENSOR_NAME, mSensorMode);
		i.putExtra(WakeUpService.IS_AUTO_LOCK, mPrefAutoLock);
		i.putExtra(WakeUpService.IS_WAKE_UP_ENABLED, mPrefWakeUpEnabled);
		startService(i);
	}
	
	public boolean stopWakeUpService(){
		Intent i = new Intent(this, WakeUpService.class);
		return stopService(i);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	    if(mPref !=null){
			Editor editor = mPref.edit();
			editor.putString(SettingsActivity.PREF_SENSOR_MODE, mSensorMode);
			editor.putBoolean(SettingsActivity.PREF_AUTO_LOCK, mPrefAutoLock);
			editor.putBoolean(SettingsActivity.PREF_WAKE_UP, mPrefWakeUpEnabled);
			editor.apply();
		}
	}

}
