package com.niara3.transcat;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

public class ServerContext implements ConnectedContext.Callback {
	private static final int LISTEN_PORT = 49000;

	private Selector mSelector;
	private ServerSocketChannel mSocketChannel;
	private List<ConnectedContext> mConnects;

	public ServerContext(Selector selector) throws IOException {
		mSelector = selector;
		mConnects = new LinkedList<ConnectedContext>();
		Log.d("ServerSocketChannel", "open");
		mSocketChannel = ServerSocketChannel.open();
		mSocketChannel.configureBlocking(false);
		mSocketChannel.socket().bind(new InetSocketAddress(LISTEN_PORT));
		mSocketChannel.accept();
		mSocketChannel.register(selector, SelectionKey.OP_ACCEPT, this);
	}

	public void onAcceptable() throws IOException {
		if (mSocketChannel == null) {
			return;
		}
		Log.d("ServerSocketChannel", "accept");
		ConnectedContext connectedContext = new ConnectedContext(this, mSelector, mSocketChannel.accept());
		if (mConnects != null) {
			Log.d("ServerSocketChannel", "add " + connectedContext);
			mConnects.add(connectedContext);
		}
	}

	public void close() {
		if (mSocketChannel != null) {
			try {
				mSocketChannel.register(mSelector, 0, null);
			} catch (ClosedChannelException e) {
				e.printStackTrace();
			}
			try {
				Log.d("ServerSocketChannel", "close");
				mSocketChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (mConnects != null) {
			for (ConnectedContext ch : mConnects) {
				ch.close();
			}
			mConnects.clear();
		}
	}

	@Override
	public void connectedClose(ConnectedContext connectedContext) {
		if (connectedContext == null) {
			return;
		}
		connectedContext.close();
		if (mConnects != null) {
			Log.d("ServerSocketChannel", "remove " + connectedContext);
			mConnects.remove(connectedContext);
		}
	}
}
