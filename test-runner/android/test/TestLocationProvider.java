/*
 * Copyright (C) 2007 The Android Open Source Project
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

package android.test;


import android.location.Criteria;
import android.location.ILocationManager;
import android.location.Location;
import android.location.LocationProviderImpl;
import android.os.Bundle;
import android.os.SystemClock;

/**
 * @hide - This is part of a framework that is under development and should not be used for
 * active development.
 */
public class TestLocationProvider extends LocationProviderImpl {

    public static final String PROVIDER_NAME = "test";
    public static final double LAT = 0;
    public static final double LON = 1;
    public static final double ALTITUDE = 10000;
    public static final float SPEED = 10;
    public static final float BEARING = 1;
    public static final int STATUS = AVAILABLE;
    private static final long LOCATION_INTERVAL = 1000;

    private Location mLocation;
    private boolean mEnabled;
    private TestLocationProviderThread mThread;

    private class TestLocationProviderThread extends Thread {

        private boolean mDone = false;

        public TestLocationProviderThread() {
            super("TestLocationProviderThread");
        }

        public void run() {            
            // thread exits after disable() is called
            synchronized (this) {
                while (!mDone) {
                    try {
                        wait(LOCATION_INTERVAL);
                    } catch (InterruptedException e) {
                    }
                    
                    if (!mDone) {
                        TestLocationProvider.this.updateLocation();
                    }
                }
            }
        }
        
        synchronized void setDone() {
            mDone = true;
            notify();
        }
    }

    public TestLocationProvider(ILocationManager locationManager) {
        super(PROVIDER_NAME, locationManager);
        mLocation = new Location(PROVIDER_NAME);
    }

    //LocationProvider methods

    @Override
    public int getAccuracy() {
        return Criteria.ACCURACY_COARSE;
    }

    @Override
    public int getPowerRequirement() {
        return Criteria.NO_REQUIREMENT;
    }

    @Override
    public boolean hasMonetaryCost() {
        return false;
    }

    @Override
    public boolean requiresCell() {
        return false;
    }

    @Override
    public boolean requiresNetwork() {
        return false;
    }

    @Override
    public boolean requiresSatellite() {
        return false;
    }

    @Override
    public boolean supportsAltitude() {
        return true;
    }

    @Override
    public boolean supportsBearing() {
        return true;
    }

    @Override
    public boolean supportsSpeed() {
        return true;
    }

    //LocationProviderImpl methods
    @Override
    public synchronized void disable() {
        mEnabled = false;
        if (mThread != null) {
            mThread.setDone();
            try {
                mThread.join();
            } catch (InterruptedException e) {
            }
            mThread = null;
        }
    }

    @Override
    public synchronized void enable() {
       mEnabled = true;
        mThread = new TestLocationProviderThread();
        mThread.start();
    }

    @Override
    public boolean isEnabled() {
        return mEnabled;
    }

    @Override
    public int getStatus(Bundle extras) {
        return STATUS;
    }

    private void updateLocation() {
        long time = SystemClock.uptimeMillis();
        long multiplier = (time/5000)%500000;
        mLocation.setLatitude(LAT*multiplier);
        mLocation.setLongitude(LON*multiplier);
        mLocation.setAltitude(ALTITUDE);
        mLocation.setSpeed(SPEED);
        mLocation.setBearing(BEARING*multiplier);

        Bundle extras = new Bundle();
        extras.putInt("extraTest", 24);
        mLocation.setExtras(extras);
        mLocation.setTime(time);
        reportLocationChanged(mLocation);
    }

}
