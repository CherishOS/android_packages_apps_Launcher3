/*
 * Copyright (C) 2019-2020 The PixelDust Project
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
 */
package com.android.launcher3.cherish;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.biometrics.BiometricManager.Authenticators;
import android.hardware.biometrics.BiometricPrompt;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import com.android.launcher3.R;

public class CherishUtils {

    /**
     * Shows authentication screen to confirm credentials (pin, pattern or password) for the current
     * user of the device.
     *
     * @param context The {@code Context} used to get {@code KeyguardManager} service
     * @param title the {@code String} which will be shown as the pompt title
     * @param successRunnable The {@code Runnable} which will be executed if the user does not setup
     *                        device security or if lock screen is unlocked
     */
    public static void showLockScreen(Context context, String title, Runnable successRunnable) {
        if (hasSecureKeyguard(context)) {
            final BiometricPrompt.AuthenticationCallback authenticationCallback =
                    new BiometricPrompt.AuthenticationCallback() {
                        @Override
                        public void onAuthenticationSucceeded(
                                    BiometricPrompt.AuthenticationResult result) {
                            successRunnable.run();
                        }

                        @Override
                        public void onAuthenticationError(int errorCode, CharSequence errString) {
                            //Do nothing
                        }
            };

            final BiometricPrompt bp = new BiometricPrompt.Builder(context)
                    .setTitle(title)
                    .setAllowedAuthenticators(Authenticators.BIOMETRIC_STRONG |
                                              Authenticators.DEVICE_CREDENTIAL)
                    .build();

            final Handler handler = new Handler(Looper.getMainLooper());
            bp.authenticate(new CancellationSignal(),
                    runnable -> handler.post(runnable),
                    authenticationCallback);
        } else {
            // Notify the user a secure keyguard is required for protected apps,
            // but allow to set hidden apps
            Toast.makeText(context, R.string.trust_apps_no_lock_error, Toast.LENGTH_LONG)
                .show();
            successRunnable.run();
        }
    }

    public static boolean hasSecureKeyguard(Context context) {
        final KeyguardManager keyguardManager = context.getSystemService(KeyguardManager.class);
        return keyguardManager != null && keyguardManager.isKeyguardSecure();
    }

    public static boolean isPackageEnabled(Context context, String pkgName) {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(pkgName, 0);
            return ai.enabled;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isPackageInstalled(Context context, String pkg, boolean ignoreState) {
        if (pkg != null) {
            try {
                PackageInfo pi = context.getPackageManager().getPackageInfo(pkg, 0);
                if (!pi.applicationInfo.enabled && !ignoreState) {
                    return false;
                }
            } catch (NameNotFoundException e) {
                return false;
            }
        }

        return true;
    }
}
