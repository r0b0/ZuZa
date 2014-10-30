package org.eu.sk.zero.zuza;

import java.text.DecimalFormat;
import java.util.Calendar;

import android.R.color;
import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String TAG = "ZuZa";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		Resources res = getResources();
		//final int ID=0;
		//final int DATE=1;
		final int BODY=2;
		final String[] projection = new String[] { "_id", "date", "body" };
		final String selection = "address='ZUNO' and body like '%ste zaplatili%' and date>=?";
		final String[] selectionArgs = new String[] { Long.toString(getStartOfMonth()) }; 
		final String sortOrder = "_id asc";
		
		Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), projection, 
				selection, selectionArgs, sortOrder);
		
		int count=cursor.getCount();
		//Log.d(TAG, "cursor.count: " + count);
		
		float totalAmount=0;
		String lastMsg="";
		while(cursor.moveToNext()){
			totalAmount += readAmountFromMessage(cursor.getString(BODY));
			lastMsg=cursor.getString(BODY);
		};
		
		TextView t=(TextView)findViewById(R.id.introText);
		t.setText(String.format(res.getString(R.string.nr_payments_made), 
				res.getQuantityString(R.plurals.nr_payments, count, count), 
				timestampToString(getStartOfMonth())));
		
		t=(TextView)findViewById(R.id.totalAmountText);
		DecimalFormat format=new DecimalFormat("#,##0.00");
		t.setText(format.format(totalAmount) + "\u20AC");
		
		if(totalAmount>100.0) 
			t.setBackgroundColor(res.getColor(color.holo_green_light));
		else
			t.setBackgroundColor(res.getColor(color.holo_red_light));
		
		if(count>0) {
			t=(TextView)findViewById(R.id.lastMsgTextCaption);
			t.setText(res.getText(R.string.last_message));
			t.setVisibility(View.VISIBLE);
			t=(TextView)findViewById(R.id.lastMsgText);
			t.setText(lastMsg);
			t.setVisibility(View.VISIBLE);
		} else {
			t=(TextView)findViewById(R.id.lastMsgTextCaption);
			t.setVisibility(View.INVISIBLE);
			t=(TextView)findViewById(R.id.lastMsgText);
			t.setVisibility(View.INVISIBLE);
		}
	}
	
	private String timestampToString(long time) {
	    Calendar cal = Calendar.getInstance();
	    cal.setTimeInMillis(time);
	    String date = DateFormat.format("yyyy-MM-dd", cal).toString();
	    return date;
	}
	
	private long getStartOfMonth() {
		Calendar cal = Calendar.getInstance();
		cal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), 1);
		return cal.getTimeInMillis();
	}
	
	private float readAmountFromMessage(String message) {
		try {
			String line1 = message.split("\n")[1];
			String amount = line1.replace("EUR", "").replace(".", "").replace(",", ".").replace(" ", "");
			
			return Float.parseFloat(amount);
		} catch(Exception e) {
			Log.w(TAG, "Failed to parse card message:");
			Log.w(TAG, message);
			return 0;
		}
	}
}
