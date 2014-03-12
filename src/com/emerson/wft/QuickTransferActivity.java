package com.emerson.wft;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.emerson.wft.constant.Constant;
import com.emerson.wft.hotspot.WifiAdmin;
import com.emerson.wft.hotspot.WifiApAdmin;

public class QuickTransferActivity extends Activity implements DisplayMesage, ITransferResult {
	/** Called when the activity is first created. */
	TextView			content;
	Button				mBtn3, mBtn4;
	WifiAdmin			mWifiAdmin;
	WifiApAdmin			wifiAp;
	Context				context;
	final static String	TAG	= "robin";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		content = (TextView) this.findViewById(R.id.content);
		mBtn3 = (Button) findViewById(R.id.button3);
		mBtn4 = (Button) findViewById(R.id.button4);
		mBtn3.setText("点击连接Wifi");
		mBtn4.setText("点击创建Wifi热点");
		context = this;
		mBtn3.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				mWifiAdmin = new WifiAdmin(context) {

					@Override
					public void myUnregisterReceiver(BroadcastReceiver receiver) {
						// TODO Auto-generated method stub
						unregisterReceiver(receiver);
					}

					@Override
					public Intent myRegisterReceiver(BroadcastReceiver receiver, IntentFilter filter) {
						// TODO Auto-generated method stub
						registerReceiver(receiver, filter);
						return null;
					}

					@Override
					public void onNotifyWifiConnected() {
						// TODO Auto-generated method stub
						Log.v(TAG, "have connected success!");
						Log.v(TAG, "###############################");
					}

					@Override
					public void onNotifyWifiConnectFailed() {
						// TODO Auto-generated method stub
						Log.v(TAG, "have connected failed!");
						Log.v(TAG, "###############################");
					}

				};
				mWifiAdmin.openWifi();
				mWifiAdmin.addNetwork(mWifiAdmin.createWifiInfo(Constant.HOST_SPOT_SSID, Constant.HOST_SPOT_PASS_WORD,
						WifiAdmin.TYPE_WPA));

			}
		});

		mBtn4.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				wifiAp = new WifiApAdmin(context);
				wifiAp.startWifiAp(Constant.HOST_SPOT_SSID, Constant.HOST_SPOT_PASS_WORD);
			}
		});
	}

	public void onClick(View view) {
		if (view.getId() == R.id.button1) {
			WifiManager wifiManage = (WifiManager) getSystemService(Context.WIFI_SERVICE);
			DhcpInfo info = wifiManage.getDhcpInfo();
			WifiInfo wifiinfo = wifiManage.getConnectionInfo();
			String ip = intToIp(wifiinfo.getIpAddress());
			String serverAddress = intToIp(info.serverAddress);
			new Sender(serverAddress, this, this).start();
			Log.w("robin", "ip:" + ip + "serverAddress:" + serverAddress + info);
		}
		else if (view.getId() == R.id.button2) {
			Receiver service = new Receiver(this, this);
			service.start();

		}
	}

	Handler			handler		= new Handler();
	StringBuffer	strBuffer	= new StringBuffer();

	public void displayMesage(final String msg) {
		Runnable r = new Runnable() {

			public void run() {
				// TODO Auto-generated method stub
				if (strBuffer.length() > 1024) {
					strBuffer.delete(0, 100);
				}
				strBuffer.append(msg + "\n");
				content.setText(strBuffer.toString());
				content.invalidate();
			}
		};
		handler.post(r);
	}

	// 将获取的int转为真正的ip地址,参考的网上的，修改了下

	private String intToIp(int i) {
		return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
	}

	@Override
	public void appendMesage(String msg) {
		displayMesage(msg);

	}

	@Override
	public void onResult(int result, long size) {
		if (wifiAp != null) {
			wifiAp.closeWifiAp(context);
		}
		// TODO Auto-generated method stub
		closeWifi();
		Runnable r = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				displayMesage("try to open wifi");
				openWifi();
				displayMesage("open wifi end");
			}

		};
		handler.postDelayed(r, 30 * 1000);

	}

	WifiManager	mWifiManager;

	// 打开WIFI
	public void openWifi() {
		if (mWifiAdmin != null) {
			mWifiAdmin.openWifi();
			return;
		}
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		}
		if (!mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(true);
		}
	}

	// 关闭WIFI
	public void closeWifi() {
		if (mWifiAdmin != null) {
			mWifiAdmin.closeWifi();
			return;
		}
		if (mWifiManager == null) {
			mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		}
		if (mWifiManager.isWifiEnabled()) {
			mWifiManager.setWifiEnabled(false);
		}
	}
}

interface DisplayMesage {
	void displayMesage(String msg);

	void appendMesage(String msg);
}

interface ITransferResult {
	void onResult(int result, long size);
}

class Receiver extends Thread {
	private static BufferedReader	in;
	DisplayMesage					console;
	ITransferResult					transferResult;

	Receiver(DisplayMesage console, ITransferResult transferResult) {
		super();
		this.console = console;
		this.transferResult = transferResult;

	}

	public void run() {
		try {
			ServerSocket socketService = new ServerSocket(3358);
			Log.i("robin", "waiting a connection from the client" + socketService);
			Socket sock = socketService.accept();
			String hostAddress = sock.getLocalAddress().getHostAddress();
			String inetAddress = sock.getInetAddress().getHostAddress();

			Log.w("robin", "local:" + hostAddress + "| inetAddress" + inetAddress + "|" + sock.getRemoteSocketAddress());
			Log.w("robin", "local name:" + sock.getLocalAddress().getHostName() + "| inetAddress"
					+ sock.getInetAddress().getHostName() + "|" + InetAddress.getLocalHost().getHostAddress());
			in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			String line;
			boolean flag = true;
			long count = 0;
			long time = System.currentTimeMillis();
			do {
				line = in.readLine();
				if (flag) {
					console.displayMesage("Recevie:" + line);
					flag = false;
				}
				count = count + line.length();
				if (count % 1024 == 0) {
					console.displayMesage("Recevied:" + ((count << 1) >> 10) + "kB data");
				}
			} while (!Constant.END.equals(line));
			Log.w("robin", "you input is :" + line);
			long t = System.currentTimeMillis() - time;
			if (t == 0)
				t = 1;
			count = count << 1;
			long rate = ((count / t) * 1000) / 1024;
			count = count >> 10;
			console.displayMesage("Recevied:" + count + "kB data" + " in " + t + " ms" + " at rate:" + rate
					+ " kB/second");
			Log.i("robin", "exit the app");
			sock.close();
			socketService.close();
			transferResult.onResult(1, count);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

class Sender extends Thread {
	DisplayMesage	console;
	String			serverIp;
	ITransferResult	transferResult;

	Sender(String serverAddress, DisplayMesage console, ITransferResult transferResult) {
		super();
		serverIp = serverAddress;
		this.console = console;
		this.transferResult = transferResult;
	}

	public void run() {
		Socket sock = null;
		PrintWriter out;
		try {

			// 声明sock，其中参数为服务端的IP地址与自定义端口
			sock = new Socket(serverIp, 3358);
			Log.w("robin", "I am try to writer" + sock);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		char data[] = new char[1024 * 10];
		for (int i = 0; i < data.length; i++) {
			data[i] = (char) i;
		}
		try {
			if (sock != null) {
				// 声明输出流out，向服务端输出“Output Message！！”
				final String msg = "Hello,this is robin!!";
				Log.w("robin", "try to writer");
				out = new PrintWriter(sock.getOutputStream(), true);
				StringBuffer strBuffer = new StringBuffer();
				strBuffer.append(msg);
				String str = msg;
				for (int i = 0; i < 1024; i++) {
					if (i != 0) {
						str = msg + System.currentTimeMillis() + "|";
						out.write(data);
					}

					out.println(str);
					Log.w("robin", str);
					if (i == 0) {
						console.displayMesage("send message....");
					}
					else if (i % 100 == 0) {
						console.displayMesage("send message " + i + " success!");
					}
					if (strBuffer.length() > 1024) {
						strBuffer.delete(0, strBuffer.length());
					}
				}
				out.println(Constant.END);
				out.flush();
			}
			transferResult.onResult(1, 1);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			if (sock != null) {
				sock.close();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
