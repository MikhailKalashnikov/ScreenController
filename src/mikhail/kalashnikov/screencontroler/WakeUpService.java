package mikhail.kalashnikov.screencontroler;

import mikhail.kalashnikov.screencontroler.ShakeManager.WakeUpListener;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

public class WakeUpService extends Service implements WakeUpListener{
	private final String TAG = getClass().getSimpleName();
	private ShakeManager mShakeManager=null;
	private PowerManager mPowerManager;
	private PowerManager.WakeLock mWakeLock;
	private ScreenOnOffBroadcastReceiver mScreenOnOffBroadcastReceiver = null;
	private Context mContext;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(DebugGuard.DEBUG) Log.d(TAG, "onStartCommand");
		mContext = getBaseContext();
		mShakeManager = new ShakeManager(this);
		mShakeManager.setWakeUpListener(this);
		
		mPowerManager = (PowerManager)getApplicationContext().getSystemService(Context.POWER_SERVICE);
		mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		mWakeLock.acquire();
		
		mScreenOnOffBroadcastReceiver = new ScreenOnOffBroadcastReceiver();
		IntentFilter screenOnOffFilter = new IntentFilter();
		screenOnOffFilter.addAction(Intent.ACTION_SCREEN_ON);
		screenOnOffFilter.addAction(Intent.ACTION_SCREEN_OFF);
		registerReceiver(mScreenOnOffBroadcastReceiver, screenOnOffFilter);
		return START_NOT_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		if(DebugGuard.DEBUG) Log.d(TAG, "onDestroy");
		if(mShakeManager != null){
			mShakeManager.stop();
			mWakeLock.release();
		}
		if(mScreenOnOffBroadcastReceiver != null){
			unregisterReceiver(mScreenOnOffBroadcastReceiver);
		}
		super.onDestroy();
	}
	
	@Override
	public void onWakeUp() {
		if(DebugGuard.DEBUG) Log.d(TAG, "Shooken!");
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
				mShakeManager.stop();
			}else if(intent.getAction() == Intent.ACTION_SCREEN_OFF){
				mShakeManager.stop();
				mShakeManager.start();
			}
		}
		
	}

}
