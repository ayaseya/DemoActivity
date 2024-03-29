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

import android.content.Context;
import android.content.Intent;

/**
 * Helper class providing methods and constants common to other classes in the
 * app.
 */

/**
 *  ユーティリティクラスは、staticな共通の処理のメソッドを集めたクラスです。
 *　同じ処理が2箇所以上で出てきた場合は、ユーティリティクラスにまとめることができる可能性があります。
 *　ユーティリティクラスを使うと共通処理を1箇所にまとめることができますので、
 *  再利用性の向上ばかりでなく、あとから修正が発生してもユーティリティクラスの修正だけですみ、
 *  保守性が向上するというメリットがあります。
 * */

public final class CommonUtilities {

    /**
     * Base URL of the Demo Server (such as http://my_host:8080/gcm-demo)
     * GAEのユニークなURL、末尾の/は削除しておく必要がある
     */
    static final String SERVER_URL = "http://1-dot-ayaseya-dev-jp.appspot.com";

    /**
     * Google API project id registered to use GCM.
     * Google Developers Console
     * https://console.developers.google.com/
     * プロジェクト番号＝SENDER_ID
     */
    static final String SENDER_ID = "977505068557";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM";

    /**
     * Intent used to display a message in the screen.
     */
    static final String DISPLAY_MESSAGE_ACTION =
            "com.google.android.gcm.demo.app.DISPLAY_MESSAGE";

    /**
     * Intent's extra that contains the message to be displayed.
     */
    static final String EXTRA_MESSAGE = "message";

    /**
     * Notifies UI to display a message.
     * <p>
     * This method is defined in the common helper because it's used both by
     * the UI and the background service.
     *
     * @param context application's context.
     * @param message message to be displayed.
     */
    static void displayMessage(Context context, String message) {
    	// Broadcastに添付するIntent、引数は一意（ユニーク）な文字列を指定
    	// 一般的にはパッケージ名＋任意の文字列
        Intent intent = new Intent(DISPLAY_MESSAGE_ACTION);
        intent.putExtra(EXTRA_MESSAGE, message);
        context.sendBroadcast(intent);
    }
}
