/*
 * Copyright 2019 Veronica Anokhina.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package ru.org.sevn.simplebrowser;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Map;

public class Util {

    public static boolean hasPermission(final Map<String, Boolean> permissionsMap, final String name) {
        final Boolean result = permissionsMap.get(name);
        if (result == null) {
            return false;
        }
        return result.booleanValue();
    }

    public static void askPermissionResult(@NonNull String[] permissions,
                                           @NonNull int[] grantResults, final Map<String, Boolean> permissionsMap) {
        for(int i = 0; i < permissions.length && i < grantResults.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                permissionsMap.put(permissions[i], Boolean.TRUE);
            } else {
                permissionsMap.put(permissions[i], Boolean.FALSE);
            }
        }
    }

    public static void askPermission(final Activity activity, final Map<String, Boolean> permissions, final String ... permission) {
        final ArrayList<String> permissionList = new ArrayList<>();
        for (final String p : permission) {
            final int grant = ContextCompat.checkSelfPermission(activity, p);
            if (grant != PackageManager.PERMISSION_GRANTED) {
                permissions.put(p, Boolean.FALSE);
                permissionList.add(p);
            } else {
                permissions.put(p, Boolean.TRUE);
            }
        }
        if (permissionList.size() > 0) {
            ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[permissionList.size()]), 1);
        }
    }

    public static String phoneNumber(final String str) {
        if (str != null) {
            //TODO
            return str.replace(" ", "").replace("(", "").replace(")", "").replace("-", "");
        } else {
            return str;
        }
    }

    public static void anyToast(final Context context, final String msg) {
        anyToast(context, msg, Toast.LENGTH_SHORT);
    }

    public static void anyToast(final Context context, final String msg, final int length) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(
                new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context, msg, length).show();
                    }
                }
        );
    }
}
