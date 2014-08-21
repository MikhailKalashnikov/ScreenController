package mikhail.kalashnikov.screencontroler;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.WindowManager;

public class TurnOnScreenActivity extends Activity {
	private PowerManager.WakeLock mWakeLock;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("TurnOnScreenActivity","onCreate");
		mWakeLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(
				PowerManager.SCREEN_DIM_WAKE_LOCK
				| PowerManager.ACQUIRE_CAUSES_WAKEUP,
				"TurnOnScreenActivity");
		mWakeLock.acquire();
		
		WindowManager.LayoutParams params = new WindowManager.LayoutParams(); 
		params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON|
		           WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON|
		           //WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD|
		           WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
		params.screenBrightness = 0.9f;
		this.getWindow().setAttributes(params);
		FinishTask ft = new FinishTask();
		ft.execute();
	}
	
	class FinishTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... params) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			mWakeLock.release();
			finish();
		}
	}
}
