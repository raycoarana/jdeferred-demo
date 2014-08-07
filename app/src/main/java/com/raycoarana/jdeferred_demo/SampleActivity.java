/*
 * jdeferred-demo
 * Copyright (C) 2014.  Rayco Araña (http://raycoarana.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.raycoarana.jdeferred_demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.jdeferred.AlwaysCallback;
import org.jdeferred.DoneCallback;
import org.jdeferred.FailCallback;
import org.jdeferred.ProgressCallback;
import org.jdeferred.Promise;
import org.jdeferred.android.AndroidAlwaysCallback;
import org.jdeferred.android.AndroidDeferredManager;
import org.jdeferred.android.AndroidExecutionScope;
import org.jdeferred.impl.DeferredObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SampleActivity extends Activity {

    private static final int NUMBER_OF_CPUS = Runtime.getRuntime().availableProcessors();
    private static final String TAG = "JDEFERRED_DEMO";

    private AndroidDeferredManager mDeferredManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample);

        mDeferredManager = new AndroidDeferredManager(Executors.newFixedThreadPool(NUMBER_OF_CPUS));
        this.doWorkInBackground().then(new DoneCallback<String>() {
                                           @Override
                                           public void onDone(String result) {
                                               Log.i(TAG, "then() on thread " + Thread.currentThread().getId());
                                           }
                                       }).progress(new ProgressCallback<Integer>() {
                                           @Override
                                           public void onProgress(Integer progress) {
                                               Log.i(TAG, "progress() on thread " + Thread.currentThread().getId());
                                           }
                                       }).done(new DoneCallback<String>() {
                                           @Override
                                           public void onDone(String result) {
                                               Log.i(TAG, "done() on thread " + Thread.currentThread().getId());
                                           }
                                       }).fail(new FailCallback<Throwable>() {
                                           @Override
                                           public void onFail(Throwable result) {
                                               Log.i(TAG, "fail() on thread " + Thread.currentThread().getId());
                                           }
                                       }).always(new AndroidAlwaysCallback<String, Throwable>() {
                                           @Override
                                           public void onAlways(Promise.State state, String resolved, Throwable rejected) {
                                               Log.i(TAG, "always() on thread " + Thread.currentThread().getId());
                                           }

                                           @Override
                                           public AndroidExecutionScope getExecutionScope() {
                                               return AndroidExecutionScope.BACKGROUND;
                                           }
                                       });
    }

    private Promise<String, Throwable, Integer> doWorkInBackground() {
        final DeferredObject<String, Throwable, Integer> deferredObject = new DeferredObject<String, Throwable, Integer>();

        Runnable work = new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i <= 100; i += 20) {
                        Thread.sleep(1000);
                        Log.i(TAG, "Done " + i + "% of work on thread " + Thread.currentThread().getId());
                        deferredObject.notify(i);
                    }

                    deferredObject.resolve("Finish!");
                } catch (Throwable ex) {
                    deferredObject.reject(ex);
                }
            }
        };
        mDeferredManager.when(work);

        return mDeferredManager.when(deferredObject);
    }

}
