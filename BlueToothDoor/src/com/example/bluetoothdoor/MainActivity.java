package com.example.bluetoothdoor;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;
import android.view.Menu;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

	private static final String TAG = "Bluetooth";
	private BluetoothServerSocket serverSocket;
	private BluetoothSocket socket;
	private String uuid = "0000FEE0-0000-1000-8000-00805F9B34FB";
	byte[] data1 = new byte[] { 0x2E, 0x01, 0x02, 0x01, 0x00, 0x11, 0x12, 0x13,
			0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0X1B, 0X1C, 0X1D, 0x13,
			0x0D, };
	byte[] data2 = new byte[] { 0x2E, 0x01, 0x02, 0x02, 0x20, 0x21, 0x22, 0x23,
			0x24, 0x25, 0x26, (byte) 0XFF, (byte) 0xFF, (byte) 0XFF,
			(byte) 0xFF, (byte) 0XFF, (byte) 0xFF, (byte) 0XFF, (byte) 0xD9,
			0x0D, };

	byte[] openDoor = new byte[] { 0x2E, 0x02, 0X01, 0X01, 0x00, (byte) 0xFF,
			(byte) 0XFF, (byte) 0xFF, (byte) 0XFF, (byte) 0xFF, (byte) 0XFF,
			(byte) 0xFF, (byte) 0XFF, (byte) 0xFF, (byte) 0XFF, (byte) 0xFF,
			(byte) 0XFF, (byte) 0xFF, (byte) 0XFD, (byte) 0x0D
	};

	byte[] faildata1 = new byte[] { 0x2E, 0x01, 0x01, 0x01, 0x01, (byte) 0xFF,
			(byte) 0XFF, (byte) 0xFF, (byte) 0XFF, (byte) 0xFF, (byte) 0XFF,
			(byte) 0xFF, (byte) 0XFF, (byte) 0xFF, (byte) 0XFF, (byte) 0xFF,
			(byte) 0XFF, (byte) 0xFF, (byte) 0XFF, (byte) 0x0D };
	byte[] faildata2 = new byte[] { 0x2E, 0x01, 0x01, 0x01, 0x7E, (byte) 0xFF,
			(byte) 0XFF, (byte) 0xFF, (byte) 0XFF, (byte) 0xFF, (byte) 0XFF,
			(byte) 0xFF, (byte) 0XFF, (byte) 0xFF, (byte) 0XFF, (byte) 0xFF,
			(byte) 0XFF, (byte) 0xFF, (byte) 0X80, (byte) 0x0D };
	private EditText receive;

	private EditText send;
	private SocketThread thread;
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				if (isFirst) {
					receive.setText(msg.obj.toString());
					isFirst = false;
				} else {
					receive.setText(receive.getText() + "\n"
							+ msg.obj.toString());
				}

				break;
			case 1:
				if (isFirstSend) {
					send.setText(msg.obj.toString());
					isFirstSend = false;
				} else {
					send.setText(send.getText()+ "\n" + msg.obj.toString());
				}

				break;
			default:
				break;
			}

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		receive = (EditText) findViewById(R.id.receive);
		send = (EditText) findViewById(R.id.send);
		new Thread() {

			public void run() {
				try {
					serverSocket = BluetoothUtl.server(uuid);
					while (true) {
						socket = serverSocket.accept();
						thread = new SocketThread(socket);
						thread.start();
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}.start();

	}

	private BluetoothTool bluetoothTool = new BluetoothTool();

	protected void onResume() {
		super.onResume();

	}

	private Boolean isFirst = true;
	private Boolean isFirstSend = true;

	private class SocketThread extends Thread {

		BluetoothSocket socket;
		InputStream in;
		OutputStream out;

		public SocketThread(BluetoothSocket socket) {

			this.socket = socket;
			try {
				in = this.socket.getInputStream();
				out = this.socket.getOutputStream();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		public void run() {
			Log.i("bluetooth", "receive");
			bluetoothTool = new BluetoothTool();
			isFirst = true;
			isFirstSend = true;
			while (true) {

				byte[] data = new byte[20];
				int read = 0;
				try {
					read = in.read(data);

					mHandler.obtainMessage(0, bluetoothTool.byteToStr(data)).sendToTarget();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}

				if (bluetoothTool.eraseDoorMessage(data, read)) {
					List<DoorMessage> dList = bluetoothTool.getList();
					Log.i(TAG, dList.get(0).getMessage().substring(6, 8));
					if (dList.get(0).getMessage().substring(6, 8).equals("11")) {

//						mHandler.obtainMessage(0,
//								bluetoothTool.HandlerDoorMessage())
//								.sendToTarget();
						new Thread() {
							public void run() {
								try {

									Log.i(TAG, "WRITHR");
									out.write(data1);
									mHandler.obtainMessage(1,
											bluetoothTool.byteToStr(data1))
											.sendToTarget();
									out.flush();
									out.write(data2);
									mHandler.obtainMessage(1,
											bluetoothTool.byteToStr(data2))
											.sendToTarget();
									out.flush();
									out.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						}.start();

						break;
					}
					if (dList.get(0).getMessage().substring(6, 8).equals("12")) {
//						mHandler.obtainMessage(0,
//								bluetoothTool.HandlerDoorMessage())
//								.sendToTarget();

						new Thread() {
							public void run() {

								try {
									Log.i(TAG, "W");
									out.write(openDoor);
									out.flush();
									mHandler.obtainMessage(1,
											bluetoothTool.byteToStr(openDoor))
											.sendToTarget();
									out.close();
									Log.i(TAG, "wang cheng");
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

							}
						}.start();

						break;
					}
				}
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	protected void onDestroy() {
		super.onDestroy();
		thread.interrupt();
	}
}
