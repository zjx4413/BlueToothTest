package com.neo.door.bluetoothtest;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.IllegalFormatCodePointException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ThreadFactory;

import javax.xml.transform.Templates;

import android.net.NetworkInfo.State;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.R.integer;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * 
 * @author 周景学 时间：2015/4/17 20:31 增加了对接收到的包的重装 ，和对接收到的包进行接收反馈 每次的问/答，会建立一个建立
 */
public class MainActivity extends Activity {

	private static final String TAG = "Bluetooth";

	private EditText send_editText;

	private EditText state;
	private EditText result;

	private Button send_open_door_btn;
	private Button send_door_num_btn;
	private Button refreshBtn;

	private String STATE;
	private final String BLUETOOTH1 = "蓝牙已经开启,并且已经完成配对";
	private final String BLUETOOTH2 = "蓝牙已经开启,但没有配对";
	private final String BLUETOOTH3 = "蓝牙没有开启";

	private InputStream in;
	private OutputStream out;

	private BluetoothSocket bluetoothSocket = null;
	BluetoothDevice device;

	private BluetoothTool bluetoothTool = null;
	Set<BluetoothDevice> devices;
	private String uuid = "0000FEE0-0000-1000-8000-00805F9B34FB";

	private int sendDoorNum = 0;
	private int sendOpenDoorNum = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		setContentView(R.layout.activity_main);

		send_editText = (EditText) findViewById(R.id.send_data);

		state = (EditText) findViewById(R.id.state);
		result = (EditText) findViewById(R.id.receive_result);
		send_door_num_btn = (Button) findViewById(R.id.send_get_door_num);
		send_open_door_btn = (Button) findViewById(R.id.send_open_door);
		refreshBtn = (Button) findViewById(R.id.refresh);
		setOnClick();

	}

	private void setOnClick() {
		send_door_num_btn.setOnClickListener(new send_door_num_onClick());
		refreshBtn.setOnClickListener(new refresh_onClick());
		send_open_door_btn.setOnClickListener(new send_open_door_onClick());
	}

	private ProgressDialog progressDialog;

	private Handler derviceHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				progressDialog.dismiss();
				state.setText("蓝牙已经打开！,并且完成配对\n" + msg.obj.toString());

				break;
			case 1:
				progressDialog.dismiss();
				state.setText("蓝牙没有打开！");
				break;
			case 2:
				progressDialog.dismiss();
				state.setText("蓝牙已经打开！,但没有配对");
			default:
				break;
			}

		}

	};

	private class refresh_onClick implements android.view.View.OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			refresh();
			send_editText.setText(null);
			result.setText(null);
			device = null;
			if(bluetoothTool !=null){
				bluetoothTool.clearSNR();
			}
			
			sendDoorNum = 0;
			sendOpenDoorNum =0;
		
			try {
				if(out!=null){
					out.close();
				    in.close();
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	protected void onResume() {
		super.onResume();

		progressDialog = new ProgressDialog(MainActivity.this);
		refresh();

	}

	private void refresh() {
		progressDialog.setMessage("正在获取蓝牙信息");
		progressDialog.show();
		new Thread() {
			public void run() {
				if (BluetoothUtl.getBluetoothEnable()
						&& BluetoothUtl.isBouldDevice()) {

					STATE = BLUETOOTH1;
					devices = BluetoothUtl.getBouldDevice();
					Iterator<BluetoothDevice> iterator = devices.iterator();
					String[] deviceName = new String[devices.size()];
					StringBuffer buffer = new StringBuffer();
					int i = 0;
					while (iterator.hasNext()) {
						deviceName[i++] = iterator.next().getName();
						buffer.append("配对:" + deviceName[i - 1] + "\n");
					}

					Message message = derviceHandler.obtainMessage();
					message.obj = buffer.toString();
					message.what = 0;
					message.sendToTarget();

				} else {
					if (!BluetoothUtl.getBluetoothEnable()) {
						Message message = derviceHandler.obtainMessage();

						message.what = 1;
						message.sendToTarget();

						STATE = BLUETOOTH3;
					} else {
						Message message = derviceHandler.obtainMessage();

						message.what = 2;
						message.sendToTarget();

						STATE = BLUETOOTH2;
					}
				}
			}
		}.start();
	}

	private class send_open_door_onClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (!STATE.equals(BLUETOOTH1)) {
				Toast toast = Toast.makeText(MainActivity.this, STATE,
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
				MediaUtil.playBeepSoundAndVibrate(MainActivity.this, R.raw.beep);
			} else {
				if (device == null) {
					mHandler.obtainMessage(6).sendToTarget();
					MediaUtil.playBeepSoundAndVibrate(MainActivity.this,
							R.raw.beep);

				} else {
					if(bluetoothTool.getDoorMessage()==null){
						mHandler.obtainMessage(6).sendToTarget();
						MediaUtil.playBeepSoundAndVibrate(MainActivity.this,
								R.raw.beep);
						return;
					}
					new Thread() {
						public void run() {
							try {
								sendOpenDoorNum++;
								bluetoothSocket = BluetoothUtl.connect(device,
										uuid);
								in = bluetoothSocket.getInputStream();
								out = bluetoothSocket.getOutputStream();
								new Thread(scheduleOpenDoorThread).start();
							} catch (IOException e) {
								// TODO Auto-generated catch
								// block
								MediaUtil.playBeepSoundAndVibrate(
										MainActivity.this, R.raw.beep);
								mHandler.obtainMessage(5).sendToTarget();
								e.printStackTrace();
							}

						}
					}.start();
				}
			}

		}

	}

	private class send_door_num_onClick implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			if (!STATE.equals(BLUETOOTH1)) {
				MediaUtil
						.playBeepSoundAndVibrate(MainActivity.this, R.raw.beep);
				Toast toast = Toast.makeText(MainActivity.this, STATE,
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.CENTER, 0, 0);
				toast.show();
			} else {

				if (device == null) {
					String[] name = new String[devices.size()];
					Iterator<BluetoothDevice> iterator = devices.iterator();
					final List<BluetoothDevice> dList = new ArrayList<BluetoothDevice>();
					int i = 0;
					while (iterator.hasNext()) {
						BluetoothDevice temp = iterator.next();
						dList.add(temp);
						name[i++] = temp.getName();
					}

					AlertDialog.Builder builder = new Builder(MainActivity.this);
					builder.setIcon(R.drawable.ic_launcher);
					builder.setTitle("请选择要进行通信的蓝牙设备");
					builder.setItems(
							name,
							new android.content.DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub
									device = dList.get(which);

									new Thread() {
										public void run() {
											try {

												sendDoorNum++;
												bluetoothSocket = BluetoothUtl
														.connect(device, uuid);
												in = bluetoothSocket
														.getInputStream();
												out = bluetoothSocket
														.getOutputStream();
												new Thread(
														scheduleDoorMessageThread)
														.start();
											} catch (IOException e) {
												// TODO Auto-generated catch
												// block
												MediaUtil
														.playBeepSoundAndVibrate(
																MainActivity.this,
																R.raw.beep);
												mHandler.obtainMessage(5)
														.sendToTarget();
												e.printStackTrace();
											}

										}
									}.start();

								}

							});

					builder.create().show();

				} else {

					new Thread() {
						public void run() {
							try {
								sendDoorNum++;
								bluetoothSocket = BluetoothUtl.connect(device,
										uuid);
								in = bluetoothSocket.getInputStream();
								out = bluetoothSocket.getOutputStream();
								new Thread(scheduleDoorMessageThread).start();
							} catch (IOException e) {
								// TODO Auto-generated catch
								// block
								MediaUtil.playBeepSoundAndVibrate(
										MainActivity.this, R.raw.beep);
								mHandler.obtainMessage(5).sendToTarget();
								e.printStackTrace();
							}

						}
					}.start();

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

	Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				send_editText.setText(msg.obj.toString());

				break;
			case 1:
				int num = msg.obj.toString().length() / 40;
				String data = msg.obj.toString();
				StringBuffer buffer = new StringBuffer();
				for (int i = 0; i < num; i++) {
					buffer.append(data.substring(i * 40, (i + 1) * 40));
					buffer.append("\n");
				}
				send_editText.setText(buffer.toString());

				break;
			case 3:
				String string = msg.obj.toString();
				if (string.startsWith("no door")) {
					state.setText(device.getName() + "\n" + "不能返回门禁机号");
					result.setText(string.substring(7));
					break;
				}
				if (string.startsWith("fail message")) {
					state.setText(device.getName() + "\n" + "信息接收不完备");
					result.setText(string.substring(12));
					break;
				}
				state.setText(device.getName() + "\n" + "发送请求门禁机号：第"
						+ sendDoorNum + "次问/答成功！\n"+"门禁返回门禁机号！");
				result.setText("门禁机号:" + msg.obj.toString());
				break;
			case 4:
				String string1 = msg.obj.toString();
				if (string1.startsWith("no door")) {
					state.setText(device.getName() + "\n" + "门禁机无相关权限");
					result.setText(string1.substring(7));
				}
				if (string1.startsWith("fail message")) {
					state.setText(device.getName() + "\n" + "信息接收不完备");
					result.setText(string1.substring(12));
				}
				state.setText(device.getName() + "\n" + "发送请求开门：第"
						+ sendOpenDoorNum + "次问/答成功！\n"+"门禁开锁成功！");
				result.setText(msg.obj.toString());
				break;
			case 5:
				showLongToast("蓝牙连接异常！");
				result.setText("蓝牙连接异常！");
				break;
			case 6:
				showLongToast("必须先获取门禁机号");
				state.setText("必须先获取门禁机号");
				break;
			default:
			}
		}
	};

	/**
	 * 下面是发送器请求门禁机号的进程
	 */
	volatile Boolean isOutTime = true;
	private static final int REQUESTOUT = 100;
	private static final int TIMEOUT = 500;
	private static final int SENDNUM = 5;
	private Boolean isRepeatSend = false;

	/**
	 * 调控请求门禁机号的问/答机制
	 */
	private Runnable scheduleDoorMessageThread = new Runnable() {

		public void run() {
			int i = 0;
			isOutTime = true;
			isRepeatSend = false;
			while (!Thread.currentThread().isInterrupted()) {

				if (isOutTime) {
					if (i == 0) {

						bluetoothTool = new BluetoothTool();

						Log.i(TAG, "创建一个Bluetooth工具类对象");

						Log.i(TAG, "开始发送请求门禁机号数据");
						new Thread(sendDoorMessageThread).start();
						new Thread(receivDoorMessage).start();
					} else if (i < 5) {
						isRepeatSend = true;
						Log.i(TAG, "门禁机没有响应，重新发送数据");
						new Thread(sendDoorMessageThread).start();
					} else {
						Log.i(TAG, "网络发送了问题");
						try {
							out.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}
					i++;
				} else {
					break;
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Thread.currentThread().interrupt();
				}
			}
			Log.i(TAG, "scheduleDoorMessageThread is quit");
		}

	};

	private Runnable sendDoorMessageThread = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			bluetoothTool.clearSNC();
			byte[] data = bluetoothTool.getSendDoorMessage(isRepeatSend);
			mHandler.obtainMessage(0, BluetoothTool.byteToStr(data))
					.sendToTarget();

			// 将数据发送过去
			try {
				out.write(data);
				out.flush();
				Log.i(TAG, "请求门禁机号的数据发送成功..........");

			} catch (IOException e) {
				// TODO Auto-generated catch block

				e.printStackTrace();
			}
			Log.i(TAG, "sendDoorMessageThread is quit");
		}
	};

	private Runnable receivDoorMessage = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Boolean isFirst = true;

			while (!Thread.currentThread().isInterrupted()) {
				byte[] data = new byte[20];
				try {

					int read = in.read(data);
					if (isFirst) {
						isOutTime = false;
						isFirst = false;
					}
					if (read == -1) {
						in.close();
						Log.i(TAG, "read == 1");
						Message message = mHandler.obtainMessage();
						message.what = 3;
						message.obj = bluetoothTool.HandlerDoorMessage();
						mHandler.sendMessage(message);
						break;
					}
					if (bluetoothTool.eraseDoorMessage(data, read)) {
						in.close();
						Message message = mHandler.obtainMessage();
						message.what = 3;
						message.obj = bluetoothTool.HandlerDoorMessage();
						mHandler.sendMessage(message);
						// bluetoothSocket.close();
						break;
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					try {
						in.close();
						break;
					} catch (IOException e1) {
						// TODO Auto-generated catch block

						e1.printStackTrace();
						break;
					}

				}
			}

			Log.i(TAG, "receivDoorMessageThread is quit");
		}
	};

	Boolean isOpenOutTime = true;
	/**
	 * 发送请求开门的数据
	 */
	Boolean isOpenRepeatSend = false;
	private Runnable scheduleOpenDoorThread = new Runnable() {

		public void run() {
			int i = 0;
			isOpenOutTime = true;
			isOpenRepeatSend = false;
			while (!Thread.currentThread().isInterrupted()) {

				if ((bluetoothTool == null)
						|| bluetoothTool.getDoorMessage() == null) {

					mHandler.obtainMessage(6).sendToTarget();
					return;
				}
				if (isOpenOutTime) {
					if (i == 0) {
						Log.i(TAG, "开始发送开锁数据");
						new Thread(sendOpenDooorThread).start();
						new Thread(receivOpenDoor).start();
					} else if (i < 5) {
						isOpenRepeatSend = false;
						Log.i(TAG, "门禁机没有响应，重新发送数据");
						new Thread(sendOpenDooorThread).start();
					} else {
						Log.i(TAG, "网络发送了问题");
						try {
							out.close();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						return;
					}
					i++;
				} else {
					try {
						out.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					return;
				}
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					Thread.currentThread().interrupt();
					e.printStackTrace();
				}
			}
			Log.i(TAG, "scheduleOpenDoorThread is quit");
		}

	};

	private Runnable sendOpenDooorThread = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub

			byte[] data = bluetoothTool.getOpenDoorMessage(isOpenRepeatSend);

			mHandler.obtainMessage(1, BluetoothTool.byteToStr(data))
					.sendToTarget();

			int num = data.length / 20;
			String string = BluetoothTool.byteToStr(data);
			for (int i = 0; i < num; i++) {
				// 将数据发送过去
				try {
					Log.i(TAG, "发送第"+(i+1)+"个包");
					Log.i(TAG, string.substring(i*40,(i+1)*40));
					out.write(data, i * 20, 20);
					out.flush();
					Log.i(TAG, "请求门禁开锁的数据发送成功..........");

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
//			try {
//				//out.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			Log.i(TAG, "sendOpenDooorThread is quit");
		}
	};

	private Boolean isOpenFirst = true;
	private Runnable receivOpenDoor = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			isOpenFirst = true;
			while (!Thread.currentThread().isInterrupted()) {
				byte[] data = new byte[20];
				try {

					int read = in.read(data);
					if (isOpenFirst) {
						bluetoothTool.clearDoorList();
						isOpenOutTime = false;
						isOpenFirst = false;
					}
					if (read == -1) {
						in.close();
						return;
					}

					
					if (bluetoothTool.eraseDoorMessage(data, read)) {
						in.close();
						Message message = mHandler.obtainMessage();
						message.what = 4;
						message.obj = bluetoothTool.HandlerDoorMessage();
						mHandler.sendMessage(message);
						bluetoothSocket.close();
						break;
					}

				} catch (IOException e) {
					// TODO Auto-generated catch block
					try {
						in.close();
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					return;
				}
			}
			Log.i(TAG, "receivOpenDoor is quit");
		}

	};

	/** 长时间显示Toast提示(来自String) **/
	protected void showLongToast(String text) {
		Toast toast = Toast
				.makeText(MainActivity.this, text, Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER, 0, 0);
		toast.show();
	}

	protected void onDestroy() {
		super.onDestroy();
		if(bluetoothTool != null){
			bluetoothTool.clearSNR();
		}
		
		// scheduleDoorMessageThread.interrupt();
		// scheduleOpenDoorThread.interrupt();
	}
}
