package com.niara3.transcat;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class TransCatServerService extends Service {
	private ServerTask mTask;

	/* no bind */
	@Override
	public IBinder onBind(Intent intent) { return null; }

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i("ServerService", "onCreate");
		mTask = new ServerTask();
		mTask.execute("");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("Server", "Received start id " + startId + ": " + intent);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.i("ServerService", "onDestroy");
		mTask.cancel(true);
		// Tell the user we stopped.
		//Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show();
	}

}
