package com.niara3.transcat;

import java.io.IOException;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

import android.util.Log;

public class ConnectedContext {
	private static final int READ_TIMEOUT = 2000;

	private Callback mCallback;
	private Selector mSelector;
	private SocketChannel mSocketChannel;
	private ByteBuffer mReadBuffer = ByteBuffer.allocate(1024);

	public ConnectedContext(Callback callback, Selector selector, SocketChannel socketChannel) {
		mCallback = callback;
		mSelector = selector;
		Log.d("SocketChannel", "connected");
		mSocketChannel = socketChannel;
		try {
			socketChannel.configureBlocking(false);
			Socket sock = socketChannel.socket();
			sock.setSoTimeout(READ_TIMEOUT);
			Log.d("SocketChannel", "register R");
			socketChannel.register(selector, SelectionKey.OP_READ, this);
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			socketChannel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void onReadable() {
		Log.d("SocketChannel", "read");
		try {
			ByteBuffer readBuffer = getByteBuffer();
			int readed = mSocketChannel.read(readBuffer);
			if (readed < 0) {
				Log.d("SocketChannel", "unregister");
				mSocketChannel.register(mSelector, 0, null);
				posfClose();
				return;
			}
			if (readBuffer.position() > 0) {
				Log.d("SocketChannel", Arrays.toString(
						Arrays.copyOf(readBuffer.array(), readBuffer.position())));
			}
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		posfClose();
	}

	private ByteBuffer getByteBuffer() {
		mReadBuffer.clear();
		return mReadBuffer;
	}

	private void posfClose() {
		if (mCallback != null) {
			mCallback.connectedClose(this);
		}
	}

	public void close() {
		if (mSocketChannel != null) {
			if (!mSocketChannel.socket().isClosed()) {
				try {
					mSocketChannel.register(mSelector, 0, null);
				} catch (ClosedChannelException e) {
					e.printStackTrace();
				}
				try {
					Log.d("SocketChannel", "close");
					mSocketChannel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public interface Callback {
		public void connectedClose(ConnectedContext connectedContext);
	}
}
