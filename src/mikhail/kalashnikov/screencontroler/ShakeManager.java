package mikhail.kalashnikov.screencontroler;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class ShakeManager implements SensorEventListener {
	private final String TAG = getClass().getSimpleName();
	enum SENSOR_MODE{
		ACCELEROMETER_ONLY,
		PROXIMITY_ONLY,
		ACCELEROMETER_AND_PROXIMITY,
	}
	private static final int FORCE_THRESHOLD = 1000;
	private static final int TIME_THRESHOLD = 100;
	private static final int SHAKE_TIMEOUT = 500;
	private static final int SHAKE_DURATION = 1000;
	private static final int SHAKE_COUNT = 3;
	
	private Context mContext;
	private WakeUpListener mWakeUpListener;
	private SensorManager mSensorManager;
	
	private int mShakeCount = 0;
	private long mLastShake;
	private long mLastForce;
	private float mLastX=-1.0f, mLastY=-1.0f, mLastZ=-1.0f;
	private long mLastTime;
	
	private float mMaxDistance;
	private float mLastDistance = 0;
	private SENSOR_MODE mSensorMode = SENSOR_MODE.ACCELEROMETER_ONLY;
	
	public interface WakeUpListener{
		public void onWakeUp();
	}
	
	public ShakeManager(Context context){
		mContext = context;
		mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
		if (mSensorManager == null) {
			throw new UnsupportedOperationException("Sensors not supported");
		}
		//resume();
	}
	
	public void start() {
		Log.d(TAG, "onStart");
		if(mSensorMode == SENSOR_MODE.ACCELEROMETER_ONLY || mSensorMode == SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY){
			Sensor accelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			if(accelerometerSensor==null){
				throw new UnsupportedOperationException("Accelerometer not supported");
			}
			mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
			Log.d(TAG, "registerListener Accelerometer");
		}
		
		if(mSensorMode == SENSOR_MODE.PROXIMITY_ONLY || mSensorMode == SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY){
			Sensor proximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
			if(proximitySensor==null){
				throw new UnsupportedOperationException("TYPE_PROXIMITY not supported");
			}
			mMaxDistance = proximitySensor.getMaximumRange();
			mSensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_GAME);
		}
		
	}
	
	public void stop(){
		if(mSensorManager != null){
			mSensorManager.unregisterListener(this);
//			mSensorManager = null;
		}
	}

	public void setWakeUpListener(WakeUpListener listener){
		mWakeUpListener = listener;
	}
	
	
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {	}

	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		Log.d(TAG, "onSensorChanged " + sensorEvent.sensor.getName());
		if((mSensorMode == SENSOR_MODE.ACCELEROMETER_ONLY && sensorEvent.sensor.getType() != Sensor.TYPE_ACCELEROMETER)
			|| (mSensorMode == SENSOR_MODE.PROXIMITY_ONLY && sensorEvent.sensor.getType() != Sensor.TYPE_PROXIMITY)
			|| (mSensorMode == SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY && 
					(sensorEvent.sensor.getType() != Sensor.TYPE_ACCELEROMETER || sensorEvent.sensor.getType() != Sensor.TYPE_PROXIMITY))){
			return;
		}
		
		boolean proximityWakeUp = true;
		if(mSensorMode == SENSOR_MODE.PROXIMITY_ONLY || mSensorMode == SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY){
			Log.d(TAG, "onSensorChanged PROXIMITY part");
			float distance = sensorEvent.values[0];
			proximityWakeUp = mLastDistance != distance && distance == mMaxDistance; 
			mLastDistance = distance;
			if(proximityWakeUp && mSensorMode == SENSOR_MODE.PROXIMITY_ONLY && mWakeUpListener != null) { 
    			mWakeUpListener.onWakeUp(); 
			}
		}
		
		if(mSensorMode == SENSOR_MODE.ACCELEROMETER_ONLY || 
				(proximityWakeUp && mSensorMode == SENSOR_MODE.ACCELEROMETER_AND_PROXIMITY)){
			Log.d(TAG, "onSensorChanged ACCELEROMETER part");
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
			    		if (mWakeUpListener != null) { 
			    			mWakeUpListener.onWakeUp(); 
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

}
