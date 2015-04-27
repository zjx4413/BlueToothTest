package com.neo.door.bluetoothtest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import android.text.style.BulletSpan;
import android.util.Log;

/**
 * 时间 2015/4/17 说明：数据格式中 String 是与Hex格式方法进行表示的。
 * 
 * @author 周景学
 * 
 */
public class BluetoothTool {

	private static final String TAG = "Bluetooth";
	// 分包序号
	private int snc = 0x01;
	// 数据序号
	private static int snr = 0x01;
	// 起始字符
	private String SOI = Integer.toHexString(0x2E).toUpperCase();
	// 结束字符
	private String EOI = numToHexString(0X0D);

	// 接受到门禁机号
	private String doorMessage = null;

	// 测试用的账号、用户名密码
	private String username = "123456";
	private String password = "123456";

	// 总包数
	private int TNC = 1;

	private int receiveTnc = 1;

	private List<DoorMessage> doorMessagesList = new ArrayList<DoorMessage>();

	public BluetoothTool() {

	}

	public BluetoothTool(int snr) {
		this.snr = snr;
	}

	public BluetoothTool(int snr, String doorMessage) {
		this.snr = snr;
		this.doorMessage = doorMessage;
	}

	public void clearSNR(){
		this.snr=0x01;
	}
	public void setSNR(int snr) {
		this.snr = snr;
	}

	public String getSNR() {
		return numToHexString(snr++);
	}

	public String getSNC() {
		return numToHexString(snc++);
	}

	public String getDoorMessage(){
		return doorMessage;
	}
	
	public void clearDoorList(){
		this.doorMessagesList.clear();
	}
	/**
	 * 得到校验字节，字节字符串
	 * 
	 * @param data
	 * @return
	 */
	public String getErase(String data) {
		byte[] buf = new byte[data.length() / 2];
		int temp = 0x00;
		for (int i = 0; i < data.length(); i += 2) {
			int n = Integer.parseInt(data.substring(i, i + 2), 16);
			temp = n ^ temp;
		}
		Log.i(TAG, "校验字节erase:" + numToHexString(temp));

		return numToHexString(temp);

	}

	private byte[] strToByte(String data) {

		byte[] buf = new byte[data.length() / 2];
		for (int i = 0; i < data.length(); i += 2) {
			int n = Integer.parseInt(data.substring(i, i + 2), 16);
			buf[i / 2] = (byte) (n & 0xff);
		}
		return buf;
	}

	/**
	 * 请求开锁数据加密
	 * 
	 * @return
	 */
	private String encrypt(String data) {
		return data;
	}

	/**
	 * 
	 * @param buffer
	 * @return
	 */

	public static String byteToStr(byte[] buffer) {
		String data = "";
		for (int s = 0; s < buffer.length; s++) {
			String hex = Integer.toHexString(buffer[s] & 0xFF);
			if (hex.length() == 1) {
				hex = '0' + hex;
			}
			data += hex.toUpperCase();
		}
		return data;
	}

	/**
	 * 获取请求门禁机号的蓝牙发送数据 即包装
	 * 
	 * @return
	 */
	public byte[] getSendDoorMessage(Boolean isRepeatSend) {

		StringBuffer buffer = new StringBuffer();
		buffer.append(SOI);
		if (isRepeatSend) {
			buffer.append(numToHexString(snr-1));
		} else {
			buffer.append(getSNR());
		}
		buffer.append(numToHexString(0x01));
		buffer.append(getSNC());
		buffer.append(getSendDoorMessageINFO());
		buffer.append(getErase(buffer.toString().substring(2)));
		buffer.append(numToHexString(0x0D));
		Log.i(TAG, "请求门禁号的数据：" + buffer.toString());
		return strToByte(buffer.toString());

	}

	/**
	 * 获取INF0字段的数据
	 * 
	 * @return
	 */
	private String getSendDoorMessageINFO() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(Integer.toHexString(0x11));
		for (int i = 0; i < 13; i++) {
			buffer.append(Integer.toHexString(0xFF).toUpperCase());
		}
		return buffer.toString();

	}

	/**
	 * 获取请求开锁的数据，即包装
	 * 
	 * @return
	 */

	public byte[] getOpenDoorMessage(Boolean isRepeatSend) {

		String[] INFO = getSendOpenDoorINFO();
		if (INFO == null) {
			return null;
		}
		Log.i(TAG, "第八步：对之前的分包数据进行起始字符、结束字符的添加...........");
		StringBuffer buffer = new StringBuffer();
		
		int snr;
		if (isRepeatSend) {
			snr = this.snr-1;
		} else {
			snr = (this.snr)++;
		}
		for (int i = 0; i < TNC; i++) {
			StringBuffer buffer2 = new StringBuffer();
			buffer2.append(SOI);
			buffer2.append(numToHexString(snr));
			buffer2.append(numToHexString(TNC));
			buffer2.append(getSNC());
			buffer2.append(INFO[i]);
			buffer2.append(getErase(buffer2.substring(2)));
			buffer2.append(EOI);
			buffer.append(buffer2.toString());

			Log.i(TAG,
					"第" + (i + 1) + "个包添加起始字符、结束字符结束" + ":"
							+ buffer2.toString());
		}
		Log.i(TAG, "添加起始字符、结束字符结束");
		Log.i(TAG, "最终的请求门禁机号的数据：" + buffer.toString());
		return strToByte(buffer.toString());

	}

	/**
	 * 分包数据 获取发送开门数据中的INFO字段的数据（分包）
	 * 
	 * @return null 表示获取先前获取的门禁机号失败
	 * 
	 */
	private String[] getSendOpenDoorINFO() {

		// 获取账号信息和门禁信息的集合
		String info = getOpenDoorINFO();
		// 获取请求数据中的数据字段失败
		if (info == null) {
			Log.i(TAG, "open door info 为空");
			return null;
		}
		// 以下是对INFO数据进行包装
		int rond = (info.length() / 2 + 1) / 14;
		int yu = (info.length() / 2 + 1) % 14;
		Log.i(TAG, "第五步:进行封装:" + "分成：" + rond + "个包" + ",还有剩下：" + yu
				+ "个数据(字节)");

		TNC = yu == 0 ? rond : rond + 1;
		String[] infoStrings = new String[TNC];
		int i = 0;
		clearSNC();
		Log.i(TAG, "第六步:清除包的序号:");
		Log.i(TAG, "第七步:分包开始...............:");
		boolean isFirst = true;
		for (; i < rond; i++) {
			StringBuffer buffer = new StringBuffer();
			if (isFirst) {
				buffer.append(Integer.toHexString(0x12));

				buffer.append(info.substring(0, 26));
				infoStrings[i] = buffer.toString();

				isFirst = false;

			} else {
				buffer.append(info.substring(26 + 28 * (i - 1), 26 + 28 * i));
				infoStrings[i] = buffer.toString();

			}
			Log.i(TAG, "第" + (i + 1) + "个包封装结束：" + infoStrings[i]);
		}
		if (yu != 0) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(info.substring(26 + (rond - 1) * 28));
			for (int j = 0; j < 14 - yu; j++) {
				buffer.append(Integer.toHexString(0xFF).toUpperCase());
			}
			infoStrings[i] = buffer.toString();
			Log.i(TAG, "第" + i + "个包封装结束：" + infoStrings[i]);
		}
		Log.i(TAG, "分包封装结束，共分成：" + infoStrings.length + "个包" + "..............");
		return infoStrings;

	}

	/**
	 * 请求开锁命令中INFO字段中的数据字段(整个INFO的数据字段)
	 * 
	 * @return
	 */
	private String getOpenDoorINFO() {
		StringBuffer buffer = new StringBuffer();
		// 账号信息
		buffer.append(charToHexString(username));
		Log.i(TAG, "第一步：添加账号信息：" + buffer.toString());
		// md5加密的数据
		buffer.append(pwdMd5(password));

		// 如果事先没有接受到门禁机号,则返回null
		if (doorMessage == null) {
			Log.i(TAG, "doorMessage 为空");
			return null;
		}
		buffer.append(doorMessage);
		Log.i(TAG, "第三步:添加门禁机信息:" + doorMessage);
		// 数据字段进行加密
		StringBuffer encrBuffer = new StringBuffer();
		encrBuffer.append(encrypt(buffer.toString()));

		Log.i(TAG, "第四步:对前面三步的信息进行加密:" + encrBuffer.toString());
		return encrBuffer.toString();

	}

	/**
	 * 请求开锁密码数据加密
	 * 
	 * @return
	 */
	private String pwdMd5(String password) {
		MessageDigest md5 = null;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch (Exception e) {
			Log.i(TAG, e.toString());
			e.printStackTrace();
			return "";
		}
		byte[] pwdMd5 = md5.digest(password.getBytes());
		Log.i(TAG, "第二步:对密码进行加密:" + byteToStr(pwdMd5));
		return byteToStr(pwdMd5);

	}

	/**
	 * 重装门禁发送过来的数据(暂时没有考虑到包的重复）
	 */
	public String HandlerDoorMessage() {

		Log.i(TAG, "开始组合接收的包..........");
		int num = doorMessagesList.size();
		if (num != doorMessagesList.get(0).getTnc()) {
			Log.i(TAG, "有包丢失..........");
			return "packeg lost";
		}
		StringBuffer buffer = new StringBuffer();
		if(num == 1){
			DoorMessage doorMessage = doorMessagesList.get(0);
			String string = doorMessage.getMessage();
			StringBuffer bufferTemp = new StringBuffer();
			if(string.substring(6, 8).equals("01")){
				bufferTemp.append("no door"+string);
			}
			if(string.substring(6, 8).equals("7E")){
				bufferTemp.append("fail message"+string);
			}else{
				return string;
			}
			return bufferTemp.toString();
		}
		for (int i = 1; i <= num; i++) {
			Log.i(TAG, "现在组合第"+i+"个包");
			for (DoorMessage doorMessage : doorMessagesList) {
				if (doorMessage.getSnc() == i) {
					if (i == 1) {
						buffer.append(doorMessage.getMessage().substring(8, 34));
					}
					if(i==num){
						String string = doorMessage.getMessage().substring(6,34);
						int index = string.indexOf("FF");
					   if(index == -1){
						   buffer.append(string);
					   }else{
						   buffer.append(string.substring(0,index));
					   }
					    
					}else{
						buffer.append(doorMessage.getMessage().substring(6, 34));
					}
				}
			}
		}
		return doorMessage = buffer.toString();
	}

	/**
	 * 解析门禁机发送过来的信息
	 * 
	 * @param data
	 *            接收到蓝牙字节
	 * @param length
	 *            数据长度
	 * @return ture 表示全部包已经接收完成 false 表示还有等待的包需要接收
	 */
	public Boolean eraseDoorMessage(byte[] data, int length) {

		Log.i(TAG, "接收一个包，长度为：" + length);
		ByteArrayInputStream in = new ByteArrayInputStream(data, 0, length);
		DoorMessage doorMessage = new DoorMessage();
		try {
			String result = readSNC(in, doorMessage);
			Log.i(TAG, "开始解析门禁发送过来的数据..........");
			if (!result.equals("false") && doorMessage.getMessage() != null) {

				String message = doorMessage.getMessage();
				if (getErase(message.substring(0, message.length() - 2))
						.equals(message.substring(message.length() - 2))) {
					doorMessagesList.add(doorMessage);
					Log.i(TAG, "该包数据正确........");
					String tnc = doorMessage.getMessage().substring(2, 4);
					String snc = doorMessage.getMessage().substring(4, 6);
					if (tnc.equals(snc)) {
						Log.i(TAG, "全部包接收完成........总共接收了"+doorMessagesList.size()+"包");
						return true;
					} else {
						return false;
					}
				}
			}
			return false;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 
	 * @param data
	 * @param length
	 */
	public Boolean eraseOpenDoor(byte[] data, int length) {

		Log.i(TAG, "接收一个包，长度为：" + length);
		ByteArrayInputStream in = new ByteArrayInputStream(data, 0, length);
		DoorMessage doorMessage = new DoorMessage();
		try {
			String result = readSNC(in, doorMessage);
			Log.i(TAG, "开始解析门禁发送过来的数据..........");
			if (!result.equals("false") && doorMessage.getMessage() != null) {

				String message = doorMessage.getMessage();
				if (getErase(message.substring(0, message.length() - 2))
						.equals(message.substring(message.length() - 2))) {
					
					Log.i(TAG, "该包数据正确........");
					return true;
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 对接收的数据进行解析
	 * 
	 * @param data
	 * @return
	 */

	public DoorMessage eraseMessage(byte[] data) {
		Log.i(TAG, "开始解析门禁发送过来的数据..........");

		ByteArrayInputStream in = new ByteArrayInputStream(data, 0, data.length);
		try {
			String FirstSNC = readSNC(in);
			Log.i(TAG, "读到第一个包的数据:" + FirstSNC);
			byte[] snr = strToByte(FirstSNC.substring(0, 2));
			if (snr[0] == 0x02) {
				Log.i(TAG, ".......接收到数据是门禁开锁的反馈信息..........");
				return eraseOpenDoorMessage(in, FirstSNC);

			} else {
				Log.i(TAG, ".......接收到数据是门禁反馈的门禁机号信息..........");
				return eraseDoorMessage(in, FirstSNC);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public DoorMessage eraseDoorMessage(InputStream in, String FirstSNC) {

		DoorMessage message = new DoorMessage();

		StringBuffer buffer = new StringBuffer();
		try {

			if (FirstSNC.length() != 36) {
				message.setState(DoorMessage.SMALLMESSAGE);
				Log.i(TAG, "第一个包：" + DoorMessage.SMALLMESSAGE);
				doorMessage = null;
				return message;
			}
			if (FirstSNC.substring(6, 8).equals("01")) {
				message.setState(DoorMessage.NODOORNUM);
				doorMessage = null;
				Log.i(TAG, "门禁反馈：" + DoorMessage.NODOORNUM);
				return message;
			}
			if (FirstSNC.substring(6, 8).equals("7E")) {
				message.setState(DoorMessage.FAILINSTRUCTION);
				doorMessage = null;
				Log.i(TAG, "门禁反馈：" + DoorMessage.FAILINSTRUCTION);
				return message;
			}
			String subString = FirstSNC.substring(0, FirstSNC.length() - 2);
			if (!getErase(subString).equals(
					FirstSNC.substring(FirstSNC.length() - 2))) {
				message.setState(DoorMessage.SNKFAIL);
				Log.i(TAG, "该包的数据：" + DoorMessage.SNKFAIL);
				doorMessage = null;
				return message;
			}
			Log.i(TAG, "该包数据正确........");
			byte[] snr = strToByte(FirstSNC.substring(0, 2));

			buffer.append(FirstSNC.substring(6, FirstSNC.length() - 2));

			message.setSnr((int) snr[0]);

			String temp = null;
			while ((temp = readSNC(in)) != null) {
				Log.i(TAG, "..........接着解析下一包.........");
				if (temp.length() != 36) {
					message.setState(DoorMessage.SMALLMESSAGE);
					Log.i(TAG, "该包的数据：" + DoorMessage.SMALLMESSAGE);
					doorMessage = null;
					return message;
				}
				String tempSubString = temp.substring(0, temp.length() - 2);
				if (!getErase(tempSubString).equals(
						temp.substring(temp.length() - 2))) {
					message.setState(DoorMessage.SNKFAIL);
					Log.i(TAG, "该包的数据" + DoorMessage.SNKFAIL);
					doorMessage = null;
					return message;
				}
				buffer.append(temp.substring(6, temp.length() - 2));
			}
			message.setMessage(buffer.toString());
			doorMessage = buffer.toString().substring(2);
			Log.i(TAG, "得到的门禁机号:" + doorMessage);
			return message;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param 包的接收流
	 * @return 接收解析情况，false 为这个包不是一个完整的包
	 * @throws IOException
	 */

	private String readSNC(InputStream in, DoorMessage doorMessage)
			throws IOException {

		int ch = 0;
		StringBuilder b = new StringBuilder();
		int valid = in.available();
		ch = in.read();
		// 代表已经接收完成
		if (ch == -1) {
			return null;
		}
		// 表示这个包不是一个完整的包
		if (ch != 0x2E) {
			Log.i(TAG, "该包的起始字节丢失");
			return "false";
		}
		for (ch = in.read(); ch != -1 && ch != 0x0D; ch = in.read())

			b.append(numToHexString(ch));

		if (ch == -1)
			return null;
		doorMessage.setSnr(strToByte(b.substring(0, 2))[0]);
		doorMessage.setSnc(strToByte(b.substring(4, 6))[0]);
		doorMessage.setTnc(strToByte(b.substring(2, 4))[0]);

		Log.i(TAG,
				"该包所在的序号：" + doorMessage.getSnr() + ",该包的包序号："
						+ doorMessage.getSnc() + ",响应的包数："
						+ doorMessage.getTnc());

		if (valid == 20) {
			doorMessage.setMessage(b.toString());
		}

		Log.i(TAG, "读到包的数据:" + b.toString());
		return b.toString();
	}

	private String readSNC(InputStream in) throws IOException {

		int ch = 0;
		StringBuilder b = new StringBuilder();
		ch = in.read();
		// 代表已经接收完成
		if (ch == -1) {
			return null;
		}
		// 表示这个包不是一个完整的包
		if (ch != 0x2E) {
			return "false";
		}
		for (ch = in.read(); ch != -1 && ch != 0x0D; ch = in.read())

			b.append(numToHexString(ch));

		if (ch == -1)
			return null;

		Log.i(TAG, "读到包的数据:" + b.toString());
		return b.toString();
	}

	public DoorMessage eraseOpenDoorMessage(InputStream in, String FirstSNC) {

		DoorMessage message = new DoorMessage();

		StringBuffer buffer = new StringBuffer();

		if (FirstSNC.length() != 36) {
			message.setState(DoorMessage.SMALLMESSAGE);
			Log.i(TAG, "该包的数据：" + DoorMessage.SMALLMESSAGE);
			doorMessage = null;
			return message;
		}
		if (FirstSNC.substring(6, 8).equals("01")) {
			message.setState(DoorMessage.NORIGHT);
			Log.i(TAG, "从机反馈：" + DoorMessage.NORIGHT);
			return message;
		}
		if (FirstSNC.substring(6, 8).equals("7E")) {
			message.setState(DoorMessage.FAILINSTRUCTION);
			Log.i(TAG, "从机反馈：" + DoorMessage.FAILINSTRUCTION);
			return message;
		}
		String subString = FirstSNC.substring(0, FirstSNC.length() - 2);
		if (getErase(subString).equals(
				FirstSNC.substring(FirstSNC.length() - 2))) {
			message.setState(DoorMessage.SNKFAIL);
			Log.i(TAG, "从机反馈：" + DoorMessage.SNKFAIL);
			return message;
		}

		buffer.append(FirstSNC.substring(6, FirstSNC.length() - 2));
		byte[] snr = strToByte(FirstSNC.substring(0, 2));
		message.setSnr((int) snr[0]);
		message.setState(DoorMessage.DOOROPEN);
		Log.i(TAG, "从机反馈：" + DoorMessage.DOOROPEN);
		return message;

	}

	public void clearSNC() {
		this.snc = 1;
	}

	public String charToHexString(String data) {

		char[] temp = data.toCharArray();
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < temp.length; i++) {
			String a = numToHexString(temp[i]);
			buffer.append(a);
		}

		return buffer.toString();
	}

	public String numToHexString(int i) {
		String hex = Integer.toHexString(i & 0xFF);
		if (hex.length() == 1) {
			hex = '0' + hex;
		}
		return hex.toUpperCase();
	}
}
