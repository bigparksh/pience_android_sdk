package com.noserv.sdk;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class PushReceive extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);

		Intent intent = this.getIntent();
		
		if(intent != null) {
			
			Log.e("Title:", "title : "+intent.getStringExtra("title"));
			Log.e("Alert:", "alert : "+intent.getStringExtra("alert"));
			Log.e("Action:", "action : "+intent.getStringExtra("action"));
		}

	}

}
