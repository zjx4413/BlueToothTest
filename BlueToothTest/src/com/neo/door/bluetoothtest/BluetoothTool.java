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
 * ʱ�� 2015/4/17 ˵�������ݸ�ʽ�� String ����Hex��ʽ�������б�ʾ�ġ�
 * 
 * @author �ܾ�ѧ
 * 
 */
public class BluetoothTool {

	private static final String TAG = "Bluetooth";
	// �ְ����
	private int snc = 0x01;
	// �������
	private static int snr = 0x01;
	// ��ʼ�ַ�
	private String SOI = Integer.toHexString(0x2E).toUpperCase();
	// �����ַ�
	private String EOI = numToHexString(0X0D);

	// ���ܵ��Ž�����
	private String doorMessage = null;

	// �����õ��˺š��û�������
	private String username = "123456";
	private String password = "123456";

	// �ܰ���
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
	 * �õ�У���ֽڣ��ֽ��ַ���
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
		Log.i(TAG, "У���ֽ�erase:" + numToHexString(temp));

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
	 * ���������ݼ���
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
	 * ��ȡ�����Ž����ŵ������������� ����װ
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
		Log.i(TAG, "�����Ž��ŵ����ݣ�" + buffer.toString());
		return strToByte(buffer.toString());

	}

	/**
	 * ��ȡINF0�ֶε�����
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
	 * ��ȡ�����������ݣ�����װ
	 * 
	 * @return
	 */

	public byte[] getOpenDoorMessage(Boolean isRepeatSend) {

		String[] INFO = getSendOpenDoorINFO();
		if (INFO == null) {
			return null;
		}
		Log.i(TAG, "�ڰ˲�����֮ǰ�ķְ����ݽ�����ʼ�ַ��������ַ������...........");
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
					"��" + (i + 1) + "���������ʼ�ַ��������ַ�����" + ":"
							+ buffer2.toString());
		}
		Log.i(TAG, "�����ʼ�ַ��������ַ�����");
		Log.i(TAG, "���յ������Ž����ŵ����ݣ�" + buffer.toString());
		return strToByte(buffer.toString());

	}

	/**
	 * �ְ����� ��ȡ���Ϳ��������е�INFO�ֶε����ݣ��ְ���
	 * 
	 * @return null ��ʾ��ȡ��ǰ��ȡ���Ž�����ʧ��
	 * 
	 */
	private String[] getSendOpenDoorINFO() {

		// ��ȡ�˺���Ϣ���Ž���Ϣ�ļ���
		String info = getOpenDoorINFO();
		// ��ȡ���������е������ֶ�ʧ��
		if (info == null) {
			Log.i(TAG, "open door info Ϊ��");
			return null;
		}
		// �����Ƕ�INFO���ݽ��а�װ
		int rond = (info.length() / 2 + 1) / 14;
		int yu = (info.length() / 2 + 1) % 14;
		Log.i(TAG, "���岽:���з�װ:" + "�ֳɣ�" + rond + "����" + ",����ʣ�£�" + yu
				+ "������(�ֽ�)");

		TNC = yu == 0 ? rond : rond + 1;
		String[] infoStrings = new String[TNC];
		int i = 0;
		clearSNC();
		Log.i(TAG, "������:����������:");
		Log.i(TAG, "���߲�:�ְ���ʼ...............:");
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
			Log.i(TAG, "��" + (i + 1) + "������װ������" + infoStrings[i]);
		}
		if (yu != 0) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(info.substring(26 + (rond - 1) * 28));
			for (int j = 0; j < 14 - yu; j++) {
				buffer.append(Integer.toHexString(0xFF).toUpperCase());
			}
			infoStrings[i] = buffer.toString();
			Log.i(TAG, "��" + i + "������װ������" + infoStrings[i]);
		}
		Log.i(TAG, "�ְ���װ���������ֳɣ�" + infoStrings.length + "����" + "..............");
		return infoStrings;

	}

	/**
	 * ������������INFO�ֶ��е������ֶ�(����INFO�������ֶ�)
	 * 
	 * @return
	 */
	private String getOpenDoorINFO() {
		StringBuffer buffer = new StringBuffer();
		// �˺���Ϣ
		buffer.append(charToHexString(username));
		Log.i(TAG, "��һ��������˺���Ϣ��" + buffer.toString());
		// md5���ܵ�����
		buffer.append(pwdMd5(password));

		// �������û�н��ܵ��Ž�����,�򷵻�null
		if (doorMessage == null) {
			Log.i(TAG, "doorMessage Ϊ��");
			return null;
		}
		buffer.append(doorMessage);
		Log.i(TAG, "������:����Ž�����Ϣ:" + doorMessage);
		// �����ֶν��м���
		StringBuffer encrBuffer = new StringBuffer();
		encrBuffer.append(encrypt(buffer.toString()));

		Log.i(TAG, "���Ĳ�:��ǰ����������Ϣ���м���:" + encrBuffer.toString());
		return encrBuffer.toString();

	}

	/**
	 * �������������ݼ���
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
		Log.i(TAG, "�ڶ���:��������м���:" + byteToStr(pwdMd5));
		return byteToStr(pwdMd5);

	}

	/**
	 * ��װ�Ž����͹���������(��ʱû�п��ǵ������ظ���
	 */
	public String HandlerDoorMessage() {

		Log.i(TAG, "��ʼ��Ͻ��յİ�..........");
		int num = doorMessagesList.size();
		if (num != doorMessagesList.get(0).getTnc()) {
			Log.i(TAG, "�а���ʧ..........");
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
			Log.i(TAG, "������ϵ�"+i+"����");
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
	 * �����Ž������͹�������Ϣ
	 * 
	 * @param data
	 *            ���յ������ֽ�
	 * @param length
	 *            ���ݳ���
	 * @return ture ��ʾȫ�����Ѿ�������� false ��ʾ���еȴ��İ���Ҫ����
	 */
	public Boolean eraseDoorMessage(byte[] data, int length) {

		Log.i(TAG, "����һ����������Ϊ��" + length);
		ByteArrayInputStream in = new ByteArrayInputStream(data, 0, length);
		DoorMessage doorMessage = new DoorMessage();
		try {
			String result = readSNC(in, doorMessage);
			Log.i(TAG, "��ʼ�����Ž����͹���������..........");
			if (!result.equals("false") && doorMessage.getMessage() != null) {

				String message = doorMessage.getMessage();
				if (getErase(message.substring(0, message.length() - 2))
						.equals(message.substring(message.length() - 2))) {
					doorMessagesList.add(doorMessage);
					Log.i(TAG, "�ð�������ȷ........");
					String tnc = doorMessage.getMessage().substring(2, 4);
					String snc = doorMessage.getMessage().substring(4, 6);
					if (tnc.equals(snc)) {
						Log.i(TAG, "ȫ�����������........�ܹ�������"+doorMessagesList.size()+"��");
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

		Log.i(TAG, "����һ����������Ϊ��" + length);
		ByteArrayInputStream in = new ByteArrayInputStream(data, 0, length);
		DoorMessage doorMessage = new DoorMessage();
		try {
			String result = readSNC(in, doorMessage);
			Log.i(TAG, "��ʼ�����Ž����͹���������..........");
			if (!result.equals("false") && doorMessage.getMessage() != null) {

				String message = doorMessage.getMessage();
				if (getErase(message.substring(0, message.length() - 2))
						.equals(message.substring(message.length() - 2))) {
					
					Log.i(TAG, "�ð�������ȷ........");
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
	 * �Խ��յ����ݽ��н���
	 * 
	 * @param data
	 * @return
	 */

	public DoorMessage eraseMessage(byte[] data) {
		Log.i(TAG, "��ʼ�����Ž����͹���������..........");

		ByteArrayInputStream in = new ByteArrayInputStream(data, 0, data.length);
		try {
			String FirstSNC = readSNC(in);
			Log.i(TAG, "������һ����������:" + FirstSNC);
			byte[] snr = strToByte(FirstSNC.substring(0, 2));
			if (snr[0] == 0x02) {
				Log.i(TAG, ".......���յ��������Ž������ķ�����Ϣ..........");
				return eraseOpenDoorMessage(in, FirstSNC);

			} else {
				Log.i(TAG, ".......���յ��������Ž��������Ž�������Ϣ..........");
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
				Log.i(TAG, "��һ������" + DoorMessage.SMALLMESSAGE);
				doorMessage = null;
				return message;
			}
			if (FirstSNC.substring(6, 8).equals("01")) {
				message.setState(DoorMessage.NODOORNUM);
				doorMessage = null;
				Log.i(TAG, "�Ž�������" + DoorMessage.NODOORNUM);
				return message;
			}
			if (FirstSNC.substring(6, 8).equals("7E")) {
				message.setState(DoorMessage.FAILINSTRUCTION);
				doorMessage = null;
				Log.i(TAG, "�Ž�������" + DoorMessage.FAILINSTRUCTION);
				return message;
			}
			String subString = FirstSNC.substring(0, FirstSNC.length() - 2);
			if (!getErase(subString).equals(
					FirstSNC.substring(FirstSNC.length() - 2))) {
				message.setState(DoorMessage.SNKFAIL);
				Log.i(TAG, "�ð������ݣ�" + DoorMessage.SNKFAIL);
				doorMessage = null;
				return message;
			}
			Log.i(TAG, "�ð�������ȷ........");
			byte[] snr = strToByte(FirstSNC.substring(0, 2));

			buffer.append(FirstSNC.substring(6, FirstSNC.length() - 2));

			message.setSnr((int) snr[0]);

			String temp = null;
			while ((temp = readSNC(in)) != null) {
				Log.i(TAG, "..........���Ž�����һ��.........");
				if (temp.length() != 36) {
					message.setState(DoorMessage.SMALLMESSAGE);
					Log.i(TAG, "�ð������ݣ�" + DoorMessage.SMALLMESSAGE);
					doorMessage = null;
					return message;
				}
				String tempSubString = temp.substring(0, temp.length() - 2);
				if (!getErase(tempSubString).equals(
						temp.substring(temp.length() - 2))) {
					message.setState(DoorMessage.SNKFAIL);
					Log.i(TAG, "�ð�������" + DoorMessage.SNKFAIL);
					doorMessage = null;
					return message;
				}
				buffer.append(temp.substring(6, temp.length() - 2));
			}
			message.setMessage(buffer.toString());
			doorMessage = buffer.toString().substring(2);
			Log.i(TAG, "�õ����Ž�����:" + doorMessage);
			return message;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 
	 * @param ���Ľ�����
	 * @return ���ս��������false Ϊ���������һ�������İ�
	 * @throws IOException
	 */

	private String readSNC(InputStream in, DoorMessage doorMessage)
			throws IOException {

		int ch = 0;
		StringBuilder b = new StringBuilder();
		int valid = in.available();
		ch = in.read();
		// �����Ѿ��������
		if (ch == -1) {
			return null;
		}
		// ��ʾ���������һ�������İ�
		if (ch != 0x2E) {
			Log.i(TAG, "�ð�����ʼ�ֽڶ�ʧ");
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
				"�ð����ڵ���ţ�" + doorMessage.getSnr() + ",�ð��İ���ţ�"
						+ doorMessage.getSnc() + ",��Ӧ�İ�����"
						+ doorMessage.getTnc());

		if (valid == 20) {
			doorMessage.setMessage(b.toString());
		}

		Log.i(TAG, "������������:" + b.toString());
		return b.toString();
	}

	private String readSNC(InputStream in) throws IOException {

		int ch = 0;
		StringBuilder b = new StringBuilder();
		ch = in.read();
		// �����Ѿ��������
		if (ch == -1) {
			return null;
		}
		// ��ʾ���������һ�������İ�
		if (ch != 0x2E) {
			return "false";
		}
		for (ch = in.read(); ch != -1 && ch != 0x0D; ch = in.read())

			b.append(numToHexString(ch));

		if (ch == -1)
			return null;

		Log.i(TAG, "������������:" + b.toString());
		return b.toString();
	}

	public DoorMessage eraseOpenDoorMessage(InputStream in, String FirstSNC) {

		DoorMessage message = new DoorMessage();

		StringBuffer buffer = new StringBuffer();

		if (FirstSNC.length() != 36) {
			message.setState(DoorMessage.SMALLMESSAGE);
			Log.i(TAG, "�ð������ݣ�" + DoorMessage.SMALLMESSAGE);
			doorMessage = null;
			return message;
		}
		if (FirstSNC.substring(6, 8).equals("01")) {
			message.setState(DoorMessage.NORIGHT);
			Log.i(TAG, "�ӻ�������" + DoorMessage.NORIGHT);
			return message;
		}
		if (FirstSNC.substring(6, 8).equals("7E")) {
			message.setState(DoorMessage.FAILINSTRUCTION);
			Log.i(TAG, "�ӻ�������" + DoorMessage.FAILINSTRUCTION);
			return message;
		}
		String subString = FirstSNC.substring(0, FirstSNC.length() - 2);
		if (getErase(subString).equals(
				FirstSNC.substring(FirstSNC.length() - 2))) {
			message.setState(DoorMessage.SNKFAIL);
			Log.i(TAG, "�ӻ�������" + DoorMessage.SNKFAIL);
			return message;
		}

		buffer.append(FirstSNC.substring(6, FirstSNC.length() - 2));
		byte[] snr = strToByte(FirstSNC.substring(0, 2));
		message.setSnr((int) snr[0]);
		message.setState(DoorMessage.DOOROPEN);
		Log.i(TAG, "�ӻ�������" + DoorMessage.DOOROPEN);
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
