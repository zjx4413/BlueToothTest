package com.neo.door.bluetoothtest;


import java.security.PublicKey;

public class DoorMessage {
	
	public  final static String NODOORNUM = "不能返回门禁机号";
	public  final static String FAILINSTRUCTION = "从机接收到不完整的指令";

	public final static String OK = "接收正常";
	
	public final static String SMALLMESSAGE = "包中的数据丢失";
	
	public final static String SNKFAIL = "校验字节错误";
	
	public final static String STARTMESSAGE = "开始的数据";
	
	public final static String STOPMESSAGE = "接收的数据";
	
	public final static String DOOROPEN = "门禁机开门";
	public final static String NORIGHT = "没有开门权限";
	
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
