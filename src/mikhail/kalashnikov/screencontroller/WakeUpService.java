package mikhail.kalashnikov.screencontroller;

import mikhail.kalashnikov.screencontroller.WakeUpSensorManager.WakeUpListener;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;

public class WakeUpService extends Service implements WakeUpListener{
	private final String TAG = getClass().getSimpleName();
	public final static String SENSOR_NAME = "SENSOR_NAME";
	private WakeUpSensorManager mWakeUpSensorManager=null;
	private PowerManager mPowerManager;
	private PowerManager.WakeLock mWakeLock;
	private ScreenOnOffBroadcastReceiver mScreenOnOffBroadcastReceiver = null;
	private Context mContext;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(DebugGuard.DEBUG) Log.d(TAG, "onStartCommand");
		mContext = getBaseContext();
		String sensorName = null;
		if(intent != null && intent.getExtras() != null && intent.getExtras().size()>0){
			sensorName = intent.getExtras().getString(SENSOR_NAME);
		}
		if(sensorName == null){
			SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			sensorName = pref.getString(SettingsActivity.PREF_SENSOR_MODE, "ACCELEROMETER_ONLY");
		}
		mWakeUpSensorManager = new WakeUpSensorManager(this, sensorName);
		mWakeUpSensorManager.setWakeUpListener(this);

		mPowerManager = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire();
		
		mScreenOnOffBroadcastReceiver = new ScreenOnOffBroadcastReceiver();
		IntentFilter screenOnOffFilter = new IntentFilter();
		screenOnOffFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenOnOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenOnOffBroadcastReceiver, screenOnOffFilter);
		
		if(!mPowerManager.isScreenOn()){
			mWakeUpSensorManager.start();
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
			mWakeUpSensorManager.stop(true);
			mWakeUpSensorManager = null;
			mWakeLock.release();
		}
		if(mScreenOnOffBroadcastReceiver != null){
			unregisterReceiver(mScreenOnOffBroadcastReceiver);
			mScreenOnOffBroadcastReceiver =null;
		}
		super.onDestroy();
	}
	
	@Override
	public void onWakeUp() {
		if(DebugGuard.DEBUG) Log.d(TAG, "onWakeUp");
		if(!mPowerManager.isScreenOn()){
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
				mWakeUpSensorManager.stop(false);
			}else if(intent.getAction() == Intent.ACTION_SCREEN_OFF){
				//mWakeUpSensorManager.stop(false);
				mWakeUpSensorManager.start();
			}
		}
		
	}

}
