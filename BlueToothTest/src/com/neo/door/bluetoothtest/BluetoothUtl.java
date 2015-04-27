package com.neo.door.bluetoothtest;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;


public class BluetoothUtl {
	
	private BluetoothDevice device;
	private static BluetoothAdapter _bluetooth = BluetoothAdapter.getDefaultAdapter(); // 获取本地蓝牙适配器，即蓝牙设备
	
	private static BluetoothSocket _socket = null; // 蓝牙通信socket
	public static Boolean getBluetoothEnable(){
		
		return _bluetooth.isEnabled();
	}
	
   public static Boolean isBouldDevice(){
		
	   Set<BluetoothDevice> devices = _bluetooth.getBondedDevices();  
		return (devices == null ||devices.size()==0)?false:true;
	}    
   
   public static Set<BluetoothDevice>  getBouldDevice(){
	   return _bluetooth.getBondedDevices();  
   }
   
   public static  BluetoothSocket connect(BluetoothDevice device,String SPP_UUID) throws IOException {    
	   

	    UUID uuid = UUID.fromString(SPP_UUID);    
	    BluetoothSocket socket = device.createRfcommSocketToServiceRecord(uuid);    
	    socket.connect();    
	    return socket;
	}   
	
}
