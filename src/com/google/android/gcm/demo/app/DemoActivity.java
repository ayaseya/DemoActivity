/*
 * Copyright 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.gcm.demo.app;

import static com.google.android.gcm.demo.app.CommonUtilities.*;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gcm.GCMRegistrar;

/**
 * Main UI for the demo app.
 */
public class DemoActivity extends Activity {

	TextView mDisplay;
	AsyncTask<Void, Void, Void> mRegisterTask;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// nullチェック用の内部クラス、下記に定義していいます。
		checkNotNull(SERVER_URL, "SERVER_URL");
		checkNotNull(SENDER_ID, "SENDER_ID");

		// Make sure the device has the proper dependencies.
		// checkDevice() メソッドは GCM をサポートしていることを検証し、
		// そうでない場合 ( 例えば、デバイスが Google APIs を含んでいないエミュレータの場合など ) は、
		// 例外をスローします。
		GCMRegistrar.checkDevice(this);
		
		// Make sure the manifest was properly set - comment out this line
		// while developing the app, then uncomment it when it's ready.
		// checkManifest() メソッドは、アプリケーションのマニフェストが
		// すべての要件を満たしているものを含んでいることを検証します。
		GCMRegistrar.checkManifest(this);
		
		setContentView(R.layout.main);
		mDisplay = (TextView) findViewById(R.id.display);
		
		// registerReceiverの登録を行う
		// 第一引数 BroadcastReceiver,第二引数 IntentFilter
		// mHandleMessageReceiverは内部クラスとして最下部で定義しています。
		registerReceiver(mHandleMessageReceiver, new IntentFilter(DISPLAY_MESSAGE_ACTION));
		
		// チェックが完了した時点で、デバイスは GCMRegsistrar.register() を呼び出し、
		// GCM にサインアップしたときに取得した SENDER_ID を渡して登録します。
		// しかし、GCMRegistrar のシングルトンが登録インテントの到着で登録 ID を記録していることから、
		// GCMRegistrar.getRegistrationId() を最初に呼び出し、
		// デバイスがすでに登録されているかどうかをチェックすることができます。
		final String regId = GCMRegistrar.getRegistrationId(this);// デバイスが登録済みか否かのチェックのためGCMから登録IDを取得する
		
		if (regId.equals("")) {
			// Automatically registers application on startup.
			
			// 登録IDが存在しなかった場合、GCMへSENDER_IDを送信します。
			GCMRegistrar.register(this, SENDER_ID);
		} else {
			// Device is already registered on GCM, check server.
			// 登録IDが存在した場合
			if (GCMRegistrar.isRegisteredOnServer(this)) {
				// Skips registration.
				// 登録IDが存在した場合、その旨を表示します。
				mDisplay.append(getString(R.string.already_registered) + "\n");
			} else {
				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.
				// GCMの登録IDが存在したが、サーバーへの登録がされていないので通知します。
				final Context context = this;
				// 非同期処理で別スレッドに登録処理を任せます。（GUIスレッドではHTTP通信はできないため）
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {
						ServerUtilities.register(context, regId);
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
					}

				};
				mRegisterTask.execute(null, null, null);
			}
		}
	}
	// オプションメニュー
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.options_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		/*
		 * Typically, an application registers automatically, so options below
		 * are disabled. Uncomment them if you want to manually register or
		 * unregister the device (you will also need to uncomment the equivalent
		 * options on options_menu.xml).
		 */
		/*
		 * case R.id.options_register: GCMRegistrar.register(this, SENDER_ID);
		 * return true; case R.id.options_unregister:
		 * GCMRegistrar.unregister(this); return true;
		 */
		case R.id.options_clear:
			mDisplay.setText(null);
			return true;
		case R.id.options_exit:
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);// 処理中、処理待ちの非同期処理をキャンセルする
		}
		unregisterReceiver(mHandleMessageReceiver);// レシーバーの解除
		GCMRegistrar.onDestroy(this);// 
		super.onDestroy();
	}
	
	// nullチェック用の内部クラス
	// nullだった場合は第二引数をエラーとしたNullPointerExceptionが投げられます。
	private void checkNotNull(Object reference, String name) {
		if (reference == null) {
			throw new NullPointerException(getString(R.string.error_config,
					name));
		}
	}
	
	// BroadcastReceiver、onReceive（）に受信時の挙動を記載しています。
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Intentに添付された文字列情報を取り出しTextViewに追記します。
			String newMessage = intent.getExtras().getString(EXTRA_MESSAGE);
			mDisplay.append(newMessage + "\n");
		}
	};

}