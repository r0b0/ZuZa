package org.eu.sk.zero.zuza;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

public class SMSReceiver extends BroadcastReceiver{
	private static final String TAG = "ZuZa_Receiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d(TAG, "onReceive called");
		
		Bundle bundle=intent.getExtras();
		final Object[] pdusObj = (Object[]) bundle.get("pdus");
		for (int i = 0; i < pdusObj.length; i++) {
			SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj[i]);
			
			if(currentMessage.getOriginatingAddress().equals("ZUNO") &&
					currentMessage.getMessageBody().contains("ste zaplatili")) {
				
				Log.d(TAG, "zuno payment message, starting the main activity");
				Intent zuzaIntent=new Intent(context, MainActivity.class);
				zuzaIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(zuzaIntent);
			} else {
				Log.d(TAG, "getOriginatingAddress: " + currentMessage.getOriginatingAddress());
				Log.d(TAG, "getMessageBody: " + currentMessage.getMessageBody());
			}
		}
	}
}
