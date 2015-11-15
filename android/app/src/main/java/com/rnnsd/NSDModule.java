package com.rnnsd;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by qiaoliang89 on 13/11/15.
 */

public class NSDModule extends ReactContextBaseJavaModule {

    ReactApplicationContext mContext;
    NsdManager mNsdManager;
    NsdManager.ResolveListener mResolveListener;
    NsdManager.DiscoveryListener mDiscoveryListener;

    public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String SERVICE_FOUND = "SERVICE_FOUND";
    public static final String SERVICE_RESOLVED = "SERVICE_RESOLVED";
    public static final String SPHERE_SERIVE_NAME = "SPHERE_SERIVE_NAME";
    public String mServiceName = "";

    HashMap<String, NsdServiceInfo> mServicesFound;
    public static final String TAG = "BigSpoon eMenu";

    @Override
    public String getName() {
        return "NSDModule";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(SERVICE_FOUND, "SERVICE_FOUND");
        constants.put(SERVICE_RESOLVED, "SERVICE_RESOLVED");
        constants.put(SPHERE_SERIVE_NAME, "Sphere POS Store Box");
        return constants;
    }

    @ReactMethod
    public void discover() { discoverServices(); }

    @ReactMethod
    public void stop() {
        stopDiscovery();
    }

    @ReactMethod
    public void resolve(String serviceName) {
        this.mServiceName = serviceName;
        Log.d(TAG, "resolving: " + serviceName );
        Log.d(TAG, mServicesFound.get(serviceName).getServiceName());
        mNsdManager.resolveService(mServicesFound.get(serviceName), mResolveListener);
    }

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    public NSDModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;
        mNsdManager = (NsdManager) reactContext.getSystemService(Context.NSD_SERVICE);
        mServicesFound = new HashMap<String, NsdServiceInfo>();
        initializeResolveListener();
        initializeDiscoveryListener();
    }


    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                mServicesFound.put(service.getServiceName(), service);
                WritableMap params = Arguments.createMap();
                params.putString("data", service.getServiceName());
                sendEvent(mContext, SERVICE_FOUND, params);
                Log.d(TAG, "Service discovery success, name :" + service.getServiceName());
                Log.d(TAG, "Service type" + service.getServiceType());
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                mServicesFound.remove(service.getServiceName());
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                WritableMap params = Arguments.createMap();
                params.putString("data", serviceInfo.getHost().getHostAddress());
                Log.e(TAG, "Resolved IP:" + serviceInfo.getHost().getHostAddress());
                sendEvent(mContext, SERVICE_RESOLVED, params);

            }
        };
    }

    public void discoverServices() {
        mNsdManager.discoverServices(
                SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        mNsdManager.stopServiceDiscovery(mDiscoveryListener);
    }
}