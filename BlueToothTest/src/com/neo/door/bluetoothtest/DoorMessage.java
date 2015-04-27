package com.neo.door.bluetoothtest;


import java.security.PublicKey;

public class DoorMessage {
	
	public  final static String NODOORNUM = "���ܷ����Ž�����";
	public  final static String FAILINSTRUCTION = "�ӻ����յ���������ָ��";

	public final static String OK = "��������";
	
	public final static String SMALLMESSAGE = "���е����ݶ�ʧ";
	
	public final static String SNKFAIL = "У���ֽڴ���";
	
	public final static String STARTMESSAGE = "��ʼ������";
	
	public final static String STOPMESSAGE = "���յ�����";
	
	public final static String DOOROPEN = "�Ž�������";
	public final static String NORIGHT = "û�п���Ȩ��";
	
	private String state;
	
	private String message;

	private int snr;
	
	private int  snc;
	
	private int tnc;
	
	
	
	public int getTnc() {
		return tnc;
	}

	public void setTnc(int tnc) {
		this.tnc = tnc;
	}

	public int getSnc() {
		return snc;
	}

	public void setSnc(int snc) {
		this.snc = snc;
	}

	public int getSnr() {
		return snr;
	}

	public void setSnr(int snr) {
		this.snr = snr;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	
}
