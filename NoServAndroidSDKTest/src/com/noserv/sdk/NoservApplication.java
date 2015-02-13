package com.noserv.sdk;

import android.app.Application;

import com.noserv.NoServ;

public class NoservApplication extends Application {

	@Override
	public void onCreate() {

		super.onCreate();
		
		NoServ.initialize(this, NoservCfg.APPLICATION_ID, NoservCfg.RESTAPI_KEY);
		NoServ.setMasterKey(NoservCfg.MASTER_KEY);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}
