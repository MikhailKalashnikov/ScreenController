package mikhail.kalashnikov.screencontroller;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.util.Log;

public class WakeUpSensorManager implements SensorEventListener {
	private final String TAG = getClass().getSimpleName();
	enum SENSOR_MODE{
		ACCELEROMETER_ONLY,
		PROXIMITY_ONLY,
		ACCELEROMETER_AND_PROXIMITY
	}
	private static final int FORCE_THRESHOLD = 1000;
	private static final int TIME_THRESHOLD = 100;
	private static final int SHAKE_TIMEOUT = 500;
	private static final int SHAKE_DURATION = 1000;
	private static final int SHAKE_COUNT = 3;
	private static final boolean START_ACCELEROMTER_WHEN_PROXIMITY_FAR = true;
	
	private Context mContext;
	private ScreenContollListener mScreenContollListener;
	private SensorManager mSensorManager;
	
	private int mShakeCount = 0;
	private long mLastShake;
	private long mLastForce;
	private float mLastX=-1.0f, mLastY=-1.0f, mLastZ=-1.0f;
	private long mLastTime;
	
	private float mMaxDistance;
	private float mLastDistance = 0;
	private SENSOR_MODE mSensorMode = SENSOR_MODE.ACCELEROMETER_ONLY;
	private Sensor mAccelerometerSensor;
	private Sensor mProximitySensor;
	private boolean mIsPhoneActive=false;
	private boolean mAutoLock=true;
	
	public interface ScreenContollListener{
		public void onWakeUp();
		public void onLock();
	}
	
	public WakeUpSensorManager(Context context, String sensorMode, boolean isAutoLock){
		mContext = context;
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		if (mSensorManager == null) {
			throw new UnsupportedOperationException("Sensors not supported");
		}
		mAutoLock = isAutoLock;
		mSensorMode = sensorMode.equals("ACCELEROMETER_ONLY")? SENSOR_MODE.ACCELEROMETER_ONLY
						:sensorMode.equals("PROXIMITY_ONLY")? SENSOR_MODE.PROXIMITY_ONLY:
							SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY;
		//resume();
		
		if(mSensorMode == SENSOR_MODE.ACCELEROMETER_ONLY || mSensorMode == SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY){
			mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			if(mAccelerometerSensor==null){
				throw new UnsupportedOperationException("Accelerometer not supported");
			}
		}
		
		if(mSensorMode == SENSOR_MODE.PROXIMITY_ONLY || mSensorMode == SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY){
			mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			if(mProximitySensor==null){
				throw new UnsupportedOperationException("TYPE_PROXIMITY not supported");
			}
			mMaxDistance = mProximitySensor.getMaximumRange();
			mLastDistance = mMaxDistance;
		}
	}
	
	public void startAccelerometer() {
		if(DebugGuard.DEBUG) Log.d(TAG, "startAccelerometer");
		mIsPhoneActive = false;
		if(mSensorMode == SENSOR_MODE.ACCELEROMETER_ONLY || 
				(mSensorMode == SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY
						&& (!START_ACCELEROMTER_WHEN_PROXIMITY_FAR || mLastDistance == mMaxDistance))){
			boolean isOK = mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
			if(DebugGuard.DEBUG) Log.d(TAG, "registerListener Accelerometer " + isOK);
		}
	}

	
	public void startProximity() {
		if(DebugGuard.DEBUG) Log.d(TAG, "startProximity");
		if(mSensorMode == SENSOR_MODE.PROXIMITY_ONLY || mSensorMode == SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY){
			mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_GAME);
			if(DebugGuard.DEBUG) Log.d(TAG, "registerListener Proximity");
		}
	}

	public void stop(){
		mIsPhoneActive = true;
		if(mSensorManager != null){
			mSensorManager.unregisterListener(this);
		}
	}

	public void setScreenContollListener(ScreenContollListener listener){
		mScreenContollListener = listener;
	}
	
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
//		Log.d(TAG, "onSensorChanged " + sensorEvent.sensor.getName());
		int sensorType = sensorEvent.sensor.getType();
		if((mSensorMode == SENSOR_MODE.ACCELEROMETER_ONLY && sensorType != Sensor.TYPE_ACCELEROMETER)
			|| (mSensorMode == SENSOR_MODE.PROXIMITY_ONLY && sensorType != Sensor.TYPE_PROXIMITY)
			|| (mSensorMode == SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY && 
					(!(sensorType == Sensor.TYPE_ACCELEROMETER || sensorType == Sensor.TYPE_PROXIMITY)))){
			return;
		}
		
		if(sensorType == Sensor.TYPE_PROXIMITY && 
				(mSensorMode == SENSOR_MODE.PROXIMITY_ONLY || mSensorMode == SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY)){
			float distance = sensorEvent.values[0];
			boolean proximityWakeUp = mLastDistance != distance && distance == mMaxDistance;
			boolean proximityLock = mAutoLock && mLastDistance != distance && distance != mMaxDistance; 
			if(DebugGuard.DEBUG) Log.d(TAG, "onSensorChanged PROXIMITY part distance=" + distance + ", proximityWakeUp=" + proximityWakeUp
					+ ", " + mSensorMode + ", " + (mScreenContollListener != null)
					+ ", isPhoneActive= " + mIsPhoneActive
					+ ", proximityLock= " + proximityLock);
			mLastDistance = distance;
			if(!mIsPhoneActive && proximityWakeUp && mSensorMode == SENSOR_MODE.PROXIMITY_ONLY && mScreenContollListener != null) { 
    			mScreenContollListener.onWakeUp(); 
			
			}else if(START_ACCELEROMTER_WHEN_PROXIMITY_FAR && !mIsPhoneActive && proximityWakeUp && mSensorMode == SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY){
				boolean isOK = mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
				if(DebugGuard.DEBUG) Log.d(TAG, "registerListener Accelerometer " + isOK);
				
			}else if(proximityLock &&(mSensorMode == SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY || mSensorMode == SENSOR_MODE.PROXIMITY_ONLY)){
				ProximityLockTask plt = new ProximityLockTask();
				plt.execute();
			}
				
		}

		if(sensorType == Sensor.TYPE_ACCELEROMETER &&
			(mSensorMode == SENSOR_MODE.ACCELEROMETER_ONLY || 
				(mLastDistance == mMaxDistance && mSensorMode == SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY))){
			//Log.d(TAG, "onSensorChanged ACCELEROMETER part");
			long now = System.currentTimeMillis();
			
			if ((now - mLastForce) > SHAKE_TIMEOUT) {
				mShakeCount = 0;
			}
			if ((now - mLastTime) > TIME_THRESHOLD) {
				long diff = now - mLastTime;
				float speed = Math.abs(sensorEvent.values[0] + sensorEvent.values[1] + sensorEvent.values[2] - mLastX - mLastY - mLastZ) / diff * 10000;
			    if (speed > FORCE_THRESHOLD) {
			    	if ((++mShakeCount >= SHAKE_COUNT) && (now - mLastShake > SHAKE_DURATION)) {
			    		mLastShake = now;
			    		mShakeCount = 0;
			    		if (mScreenContollListener != null) { 
			    			mScreenContollListener.onWakeUp(); 
			    		}
			        }
			    	mLastForce = now;
			    }
			    mLastTime = now;
			    mLastX = sensorEvent.values[0];
			    mLastY = sensorEvent.values[1];
			    mLastZ = sensorEvent.values[2];
			}
		}		
	}

	class ProximityLockTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			if(mLastDistance != mMaxDistance && mScreenContollListener != null){
				mScreenContollListener.onLock();
			}
		}
	}
}
