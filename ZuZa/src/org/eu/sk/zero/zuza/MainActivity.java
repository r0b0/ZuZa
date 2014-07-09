package org.eu.sk.zero.zuza;

import java.util.Calendar;
import android.R.color;
import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends Activity {
	private static final String TAG = "ZuZa";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		
		final int ID=0;
		final int DATE=1;
		final int BODY=2;
		final String[] projection = new String[] { "_id", "date", "body" };
		final String selection = "address='ZUNO' and body like '%ste zaplatili%' and date>=?";
		final String[] selectionArgs = new String[] { Long.toString(getStartOfMonth()) }; 
		final String sortOrder = "_id asc";
		
		Cursor cursor = getContentResolver().query(Uri.parse("content://sms/inbox"), projection, 
				selection, selectionArgs, sortOrder);
		
		Log.d(TAG, "cursor.count: " + cursor.getCount());
		
		float totalAmount=0;
		String lastMsg="";
		while(cursor.moveToNext()){
			totalAmount += readAmountFromMessage(cursor.getString(BODY));
			lastMsg=cursor.getString(BODY);
		};
		
		TextView t=(TextView)findViewById(R.id.introText);
		t.setText(cursor.getCount() + " card messages from zuno since " + timestampToString(getStartOfMonth()) + " totalling:");
		
		t=(TextView)findViewById(R.id.totalAmountText);
		t.setText(totalAmount + "EUR");
		if(totalAmount>100.0) 
			t.setBackgroundColor(getResources().getColor(color.holo_green_light));
		else
			t.setBackgroundColor(getResources().getColor(color.holo_red_light));
		
		if(cursor.getCount()>0) {
			t=(TextView)findViewById(R.id.lastMsgTextCaption);
			t.setText("Last message:");
			t.setVisibility(1);
			t=(TextView)findViewById(R.id.lastMsgText);
			t.setText(lastMsg);
			t.setVisibility(1);
		} else {
			t=(TextView)findViewById(R.id.lastMsgTextCaption);
			t.setVisibility(0);
			t=(TextView)findViewById(R.id.lastMsgText);
			t.setVisibility(0);
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
			String amount = line1.replace(" EUR.", "").replace(",", ".").replace(" ", "");
			
			return Float.parseFloat(amount);
		} catch(Exception e) {
			Log.w(TAG, "Failed to parse card message:");
			Log.w(TAG, message);
			return 0;
		}
	}
}
