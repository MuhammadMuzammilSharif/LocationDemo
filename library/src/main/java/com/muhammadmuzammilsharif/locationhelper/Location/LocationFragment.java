/*
 * Copyright (C) 2018 Muhammad Muzammil Sharif
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

package com.muhammadmuzammilsharif.locationhelper.Location;
/*
 * Created by M_Muzammil Sharif on 25-Aug-17.
 */

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;

import com.muhammadmuzammilsharif.locationhelper.Utils.PermissionHandlerHelper;

public abstract class LocationFragment extends Fragment implements OnLocationChangeListener {
    private FusedLocationApiService fusedLocationApiService;
    private GPSTracker mGpsTracker;
    private final String[] LOCATION_PERMISSION = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    protected abstract void showNeededLocationPermissionDialog();

    protected abstract void showLocationServiceEnableDialog();

    protected void openLocationServiceSetting() {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivityForResult(intent, 5319);
    }

    protected void openAppPermissionSettingToEnableLocation(final String APPLICATION_ID) {
        Intent myAppSettings = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + APPLICATION_ID));
        myAppSettings.addCategory(Intent.CATEGORY_DEFAULT);
        startActivityForResult(myAppSettings,5319);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 5319) {
            PermissionHandlerHelper.onRequestPermissionsResultHelper(getContext(), LOCATION_PERMISSION, new PermissionHandlerHelper.PermissionResultResponse() {
                @Override
                public void permissionDenied() {
                    showNeededLocationPermissionDialog();
                }

                @Override
                @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
                public void permissionGranted() {
                    setupGPS();
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 5319) {
            PermissionHandlerHelper.onRequestPermissionsResultHelper(getContext(), LOCATION_PERMISSION, new PermissionHandlerHelper.PermissionResultResponse() {
                @Override
                public void permissionDenied() {
                    showNeededLocationPermissionDialog();
                }

                @Override
                @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
                public void permissionGranted() {
                    setupGPS();
                }
            });
        }
    }

    private void getLatLong() {
        if (fusedLocationApiService == null) {
            fusedLocationApiService = new FusedLocationApiService(getContext(), LocationFragment.this);
            fusedLocationApiService.startListeningToUpdates();
        } else {
            fusedLocationApiService.startListeningToUpdates();
        }
    }

    private void initialiseGpsTracker() {
        mGpsTracker = new GPSTracker(getContext());
        if (!mGpsTracker.canGetLocation()) {
            showLocationServiceEnableDialog();
        } else {
            getLatLong();
        }
    }

    private void setupGPS() {
        if (mGpsTracker != null) {
            if (!mGpsTracker.canGetLocation()) {
                showLocationServiceEnableDialog();
            } else {
                initialiseGpsTracker();
                return;
            }
            getLatLong();
        } else {
            initialiseGpsTracker();
        }
    }

    protected final void startLocationService() {
        PermissionHandlerHelper.checkPermissionHelper(getContext(), LOCATION_PERMISSION, new PermissionHandlerHelper.CheckPermissionResponse() {
            @Override
            @RequiresPermission(allOf = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION})
            public void permissionGranted() {
                setupGPS();
            }

            @Override
            public void showNeededPermissionDialog() {
                showNeededLocationPermissionDialog();
            }

            @Override
            public void requestPermission() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    ActivityCompat.requestPermissions(getActivity(), LOCATION_PERMISSION, 5319);
                }
            }
        });
    }

    protected final void stopLocationService() {
        if (mGpsTracker != null) {
            mGpsTracker.stopUsingGPS();
        }
        mGpsTracker = null;
        if (fusedLocationApiService != null) {
            fusedLocationApiService.stopLocationUpdates();
        }
    }
}
