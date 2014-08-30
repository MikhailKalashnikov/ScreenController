package mikhail.kalashnikov.screencontroller;

import java.util.List;


import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class CheckAvailbleSensorsActivity extends Activity implements SensorEventListener {
	private final String TAG = getClass().getSimpleName();
	private SensorManager mSensorManager;
	private Sensor mLightSensor;
	private Sensor mAccelerometerSensor;
	private Sensor mProximitySensor;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	//	setContentView(R.layout.activity_main);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		List<Sensor> deviceSensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
		for(Sensor sensor: deviceSensors){
			Log.d(TAG, sensor.getName()  + ", power=" + sensor.getPower()  + ", midDelay=" + sensor.getMinDelay() + " , type = " + sensor.getType());
		}
		
		checkSensor(Sensor.TYPE_ACCELEROMETER,"TYPE_ACCELEROMETER");
//		checkSensor(Sensor.TYPE_AMBIENT_TEMPERATURE,"TYPE_AMBIENT_TEMPERATURE");
		checkSensor(Sensor.TYPE_GRAVITY,"TYPE_GRAVITY");
		checkSensor(Sensor.TYPE_GYROSCOPE,"TYPE_GYROSCOPE");
		checkSensor(Sensor.TYPE_LIGHT,"TYPE_LIGHT");
		checkSensor(Sensor.TYPE_LINEAR_ACCELERATION,"TYPE_LINEAR_ACCELERATION");
		checkSensor(Sensor.TYPE_MAGNETIC_FIELD,"TYPE_MAGNETIC_FIELD");
		checkSensor(Sensor.TYPE_PRESSURE,"TYPE_PRESSURE");
		checkSensor(Sensor.TYPE_PROXIMITY,"TYPE_PROXIMITY");
//		checkSensor(Sensor.TYPE_RELATIVE_HUMIDITY,"TYPE_RELATIVE_HUMIDITY");
		checkSensor(Sensor.TYPE_ROTATION_VECTOR,"TYPE_ROTATION_VECTOR");
		
		
		//mLightSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		//mAccelerometerSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mProximitySensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
		Log.d(TAG, "mProximitySensor.getMaximumRange() = " + mProximitySensor.getMaximumRange());
	}

	private boolean checkSensor(int type, String name){
		if (mSensorManager.getDefaultSensor(type) != null){
			Log.d(TAG, name + " TRUE!");
			return true;
		}
		Log.d(TAG, name + " FALSE!");
		return false;
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
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.d(TAG, "onAccuracyChanged " + sensor.getName());
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Log.d(TAG, "onSensorChanged " + event.sensor.getName() + " " + event.values.length);
		if(event.sensor.getType() == Sensor.TYPE_PROXIMITY){
			float distance = event.values[0];
			Log.d(TAG, "PROXIMITY distance = " + distance);
		}
		
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		//mSensorManager.registerListener(this, mLightSensor, SensorManager.SENSOR_DELAY_NORMAL);
		//mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(this, mProximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
}
