package com.rnnsd;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;
import android.widget.Toast;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.net.UnknownHostException;
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
    NsdServiceInfo mServiceFound;
    public static final String SERVICE_TYPE = "_http._tcp.";
    public static final String SERVICE_FOUND = "serviceDidFound";
    public static final String SERVICE_RESOLVED = "serviceDidResolved";
    public String mServiceName = "";

    NsdServiceInfo mService;
    public static final String TAG = "BigSpoon eMenu";

    @Override
    public String getName() {
        return "NSDModule";
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(SERVICE_RESOLVED, SERVICE_RESOLVED);
        constants.put(SERVICE_FOUND, SERVICE_FOUND);
        return constants;
    }

    @ReactMethod
    public void discover() {
        discoverServices();
    }

    @ReactMethod
    public void stop() {
        stopDiscovery();
    }

    @ReactMethod
    public void resolve(String serviceName) {
        this.mServiceName = serviceName;
        mNsdManager.resolveService(mServiceFound, mResolveListener);
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
        initializeResolveListener();
        initializeDiscoveryListener();
    }


    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {

            @Override
            public void onDiscoveryStarted(String regType) {
                Toast.makeText(mContext, "discover started", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Service discovery started");
            }

            @Override
            public void onServiceFound(NsdServiceInfo service) {
                NSDModule.this.mServiceFound = service;

                Toast.makeText(mContext, "service found", Toast.LENGTH_SHORT).show();
                WritableMap params = Arguments.createMap();
                params.putString("data", service.getServiceName());
                sendEvent(mContext, SERVICE_FOUND, params);

                Log.d(TAG, "Service discovery success" + service);
                if (!service.getServiceType().equals(SERVICE_TYPE)) {
                    Log.d(TAG, "Unknown Service Type: " + service.getServiceType());
                } else if (service.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same machine: " + mServiceName);
                } else if (service.getServiceName().contains(mServiceName)){
                    mNsdManager.resolveService(service, mResolveListener);
                }
            }

            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Toast.makeText(mContext, "service lost" + service, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "service lost" + service);
                if (mService == service) {
                    mService = null;
                }
            }

            @Override
            public void onDiscoveryStopped(String serviceType) {
                Toast.makeText(mContext, "Discovery stopped: " + serviceType, Toast.LENGTH_SHORT).show();
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }

            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Toast.makeText(mContext, "Discovery failed: Error code:" + errorCode, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }

            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Toast.makeText(mContext, "Discovery failed: Error code:" + errorCode, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
                mNsdManager.stopServiceDiscovery(this);
            }
        };
    }

    public void initializeResolveListener() {
        mResolveListener = new NsdManager.ResolveListener() {

            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Toast.makeText(mContext, "Resolve failed" + errorCode, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Resolve failed" + errorCode);
            }

            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                try {
                    WritableMap params = Arguments.createMap();
                    params.putString("data", serviceInfo.getHost().getLocalHost().getHostAddress());
                    sendEvent(mContext, SERVICE_RESOLVED, params);
                } catch (UnknownHostException e) {
                    WritableMap params = Arguments.createMap();
                    params.putString("data", e.getMessage());
                    sendEvent(mContext, SERVICE_RESOLVED, params);
                }

                Toast.makeText(mContext, "Resolve Succeeded. " + serviceInfo, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);

                if (serviceInfo.getServiceName().equals(mServiceName)) {
                    Log.d(TAG, "Same IP.");
                    return;
                }
                mService = serviceInfo;
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

    public NsdServiceInfo getChosenServiceInfo() {
        return mService;
    }
}