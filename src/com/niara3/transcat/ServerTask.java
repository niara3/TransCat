package com.niara3.transcat;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;

import android.os.AsyncTask;
import android.util.Log;

public class ServerTask extends AsyncTask<String/*Params*/, Integer/*Progress*/, Long/*Result*/> {
	private ServerContext mServerContext;

	public ServerTask() {
	}

	@Override
	protected Long doInBackground(String/*Params*/... arg0) {
		Selector selector = null;
		try {
			Log.d("Selector", "open");
			selector = Selector.open();
			mServerContext = new ServerContext(selector);
			while (!isCancelled()) {
				int keyCount = selector.select();
				if (keyCount <= 0) {
					Log.w("Selector", "keyCount = " + keyCount);
					continue;
				}
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while (!isCancelled() && it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();
					if (key.isAcceptable()) {
						((ServerContext)key.attachment()).onAcceptable();
						continue;
					}
					if (key.isReadable()) {
						((ConnectedContext)key.attachment()).onReadable();
						continue;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (mServerContext != null) {
				mServerContext.close();
			}
			if (selector != null) {
				try {
					Log.d("Selector", "close");
					selector.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute (Long/*Result*/ result) {
		super.onPostExecute(result);
	}
}
