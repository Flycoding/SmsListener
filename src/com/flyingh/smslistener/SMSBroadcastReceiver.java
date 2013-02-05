package com.flyingh.smslistener;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.StrictMode;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSBroadcastReceiver extends BroadcastReceiver {

	private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
	private static final String TAG = "SMSBroadcastReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (!SMS_RECEIVED.equals(intent.getAction())) {
			return;
		}
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().detectAll().build());
		}
		Object[] objs = (Object[]) intent.getExtras().get("pdus");
		for (Object obj : objs) {
			SmsMessage msg = SmsMessage.createFromPdu((byte[]) obj);
			String content = msg.getMessageBody();
			String address = msg.getOriginatingAddress();
			long ts = msg.getTimestampMillis();
			if (sendMsg(content, address, ts)) {
				Log.i(TAG, "send success");
			} else {
				Log.i(TAG, "send failure");
			}
		}
	}

	private boolean sendMsg(String content, String address, long ts) {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL("http://10.1.79.29:8080/News/SmsServlet").openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			String params = "content=" + content + "&address=" + address + "&ts=" + getDate(ts);
			byte[] buf = params.getBytes();
			conn.setRequestProperty("Content-Length", String.valueOf(buf.length));
			conn.getOutputStream().write(buf);
			return conn.getResponseCode() == 200;
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage());
		}
		return false;
	}

	private String getDate(long ts) {
		return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US).format(new Date(ts));
	}
}
