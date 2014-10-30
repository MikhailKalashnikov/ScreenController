package mikhail.kalashnikov.screencontroller;

import mikhail.kalashnikov.screencontroller.WakeUpSensorManager.ScreenContollListener;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class WakeUpService extends Service implements ScreenContollListener{
	private final String TAG = getClass().getSimpleName();
	public final static String SENSOR_NAME = "SENSOR_NAME";
	public final static String IS_AUTO_LOCK = "IS_AUTO_LOCK";
	public final static String IS_WAKE_UP_ENABLED = "IS_WAKE_UP_ENABLED";
	private WakeUpSensorManager mWakeUpSensorManager=null;
	private PowerManager mPowerManager;
	private PowerManager.WakeLock mWakeLock=null;
	private ScreenOnOffBroadcastReceiver mScreenOnOffBroadcastReceiver = null;
	private Context mContext;
	private String mSensorName = null;
	private boolean mPrefAutoLock;
	private boolean mPrefWakeUpEnabled;
	private DevicePolicyManager mDevicePolicyManager;
	private ComponentName mDeviceAdmin;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(DebugGuard.DEBUG) Log.d(TAG, "onStartCommand");
		mContext = getBaseContext();
		mDevicePolicyManager = (DevicePolicyManager) getApplicationContext().getSystemService(DEVICE_POLICY_SERVICE);
		mDeviceAdmin = new ComponentName(this, DeviceAdminLockReceiver.class);
		
		if(intent != null && intent.getExtras() != null && intent.getExtras().size()>0){
			mSensorName = intent.getExtras().getString(SENSOR_NAME);
			mPrefAutoLock = intent.getExtras().getBoolean(IS_AUTO_LOCK);
			mPrefWakeUpEnabled = intent.getExtras().getBoolean(IS_WAKE_UP_ENABLED);
		}
		if(mSensorName == null){
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			mSensorName = pref.getString(SettingsActivity.PREF_SENSOR_MODE, WakeUpSensorManager.SENSOR_MODE_PROXIMITY_ONLY);
			mPrefAutoLock = pref.getBoolean(SettingsActivity.PREF_AUTO_LOCK, false);
			mPrefWakeUpEnabled = pref.getBoolean(SettingsActivity.PREF_WAKE_UP, false);
		}
		if(mPrefWakeUpEnabled && mPrefAutoLock){
			stopSelf();
		}
		
		mWakeUpSensorManager = new WakeUpSensorManager(this, mSensorName, mPrefAutoLock);
		mWakeUpSensorManager.setScreenContollListener(this);
		mWakeUpSensorManager.startProximity();

		mPowerManager = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
		if(mPrefWakeUpEnabled){
			mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
			mWakeLock.acquire();
		}
		
		mScreenOnOffBroadcastReceiver = new ScreenOnOffBroadcastReceiver();
		IntentFilter screenOnOffFilter = new IntentFilter();
		screenOnOffFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenOnOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenOnOffBroadcastReceiver, screenOnOffFilter);
		
		if(!mPowerManager.isScreenOn()){
			mWakeUpSensorManager.startAccelerometer();
		}
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		if(DebugGuard.DEBUG) Log.d(TAG, "onDestroy");
		if(mWakeUpSensorManager != null){
			mWakeUpSensorManager.stop();
			mWakeUpSensorManager = null;
		}
		if(mScreenOnOffBroadcastReceiver != null){
			unregisterReceiver(mScreenOnOffBroadcastReceiver);
			mScreenOnOffBroadcastReceiver =null;
		}
		if(mWakeLock!=null){
			mWakeLock.release();
		}
		super.onDestroy();
	}
	
	@Override
	public void onWakeUp() {
		if(DebugGuard.DEBUG) Log.d(TAG, "onWakeUp");
		if(mPrefWakeUpEnabled && !mPowerManager.isScreenOn()){
			if(DebugGuard.DEBUG) Log.d(TAG, "Wake up!");
			Intent intent = new Intent(mContext, TurnOnScreenActivity.class);   
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
			getApplication().startActivity(intent);
		}
	}
	
	
	class ScreenOnOffBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(DebugGuard.DEBUG) Log.d(TAG, "ScreenOnOffBroadcastReceiver.onReceive " + intent);
			if(intent.getAction() == Intent.ACTION_SCREEN_ON && mPowerManager.isScreenOn()){
				// We need stop all sensors (not only Accelerometer) and then register proximity again, because otherwise Accelerometer would not work
				mWakeUpSensorManager.stop();
				mWakeUpSensorManager.startProximity();
			}else if(intent.getAction() == Intent.ACTION_SCREEN_OFF){
				
				if(!mPrefWakeUpEnabled){
					mWakeUpSensorManager.stop();
				}else{
					//mWakeUpSensorManager.stop(false);
					mWakeUpSensorManager.startAccelerometer();
				}
			}
		}
		
	}


	@Override
	public void onLock() {
		if(DebugGuard.DEBUG) Log.d(TAG, "onLock");

		if(mDevicePolicyManager.isAdminActive(mDeviceAdmin)){
			mDevicePolicyManager.lockNow();
			mDevicePolicyManager.setMaximumTimeToLock(mDeviceAdmin, 0);
		}
	}

}
