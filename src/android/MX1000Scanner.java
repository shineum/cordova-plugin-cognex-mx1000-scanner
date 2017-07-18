package com.shineum.cognex;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.EnumSet;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.BatteryManager;
import android.util.Log;

import com.cognex.dataman.sdk.discovery.UsbDiscoverer;
import com.cognex.dataman.sdk.ResultType;

import com.cognex.dataman.sdk.DataManSystem;
import com.cognex.dataman.sdk.DataManSystem.OnConnectedListener;
import com.cognex.dataman.sdk.DataManSystem.OnConnectionErrorListener;
import com.cognex.dataman.sdk.DataManSystem.OnDisconnectedListener;
import com.cognex.dataman.sdk.DataManSystem.OnHeartbeatResponseMissedListener;
import com.cognex.dataman.sdk.DataManSystem.OnImageArrivedListener;
import com.cognex.dataman.sdk.DataManSystem.OnImageGraphicsArrivedListener;
import com.cognex.dataman.sdk.DataManSystem.OnReadStringArrivedListener;
import com.cognex.dataman.sdk.DataManSystem.OnResponseReceivedListener;

public class MX1000Scanner extends CordovaPlugin implements OnConnectedListener, OnDisconnectedListener, OnConnectionErrorListener, OnReadStringArrivedListener {

  private static final String ACTION_USB_PERMISSION = "ACTION_COGNEX_USB_PERMISSION";
  private static CallbackContext callbackContext = null;
  private static CallbackContext connectCallback = null;
  private static CallbackContext closeCallback = null;

  private static boolean isConnecting = false;
  private static boolean isClosing = false;
  private static DataManSystem dms = null;
  private static Activity activity = null;

  static {
  }

  private void callbackSuccessSafely(CallbackContext cbContext, String msg) {
    if (cbContext != null) {
      cbContext.success(msg);
    }
  }

  private void callbackErrorSafely(CallbackContext cbContext, String msg) {
    if (cbContext != null) {
      cbContext.error(msg);
    }
  }

	@Override
	public void onConnected(DataManSystem dataManSystem) {
    isConnecting = false;
    isClosing = false;

    callbackSuccessSafely(connectCallback, "Connected");
	}
	
	@Override
	public void onDisconnected(DataManSystem dataManSystem) {
    if (isClosing) {
      callbackSuccessSafely(closeCallback, "Closed");
      isClosing = false;
    }

    isConnecting = false;
	}

	@Override
	public void onConnectionError(DataManSystem dataManSystem, Exception exception) {
    if (isConnecting) {
      callbackErrorSafely(connectCallback, "Connection Error");
      isConnecting = false;
    }
    isClosing = false;
	}

  @Override
	public void onReadStringArrived(DataManSystem dataManSystem, int resultId, String readString) {
    callbackSuccessSafely(callbackContext, readString);
	}

	private void prepareDataManSystem(DataManSystem dms) {
    if (dms != null) {
      this.dms = dms;

      dms.setResultTypes(EnumSet.of(ResultType.READ_STRING));

      dms.setOnConnectedListener(this);
      dms.setOnDisconnectedListener(this);
      dms.setOnConnectionErrorListener(this);
      dms.setOnReadStringArrivedListener(this);

      this.isConnecting = true;
      dms.connect();
    }
	}

  private BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      synchronized (this) {
        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
          if (activity != null) {
            UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            UsbAccessory acc = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);

            DataManSystem dms = null;

            if (device != null) {
              dms = DataManSystem.createDataManSystemOverUsb(activity, device);
            } else if (acc != null) {
              dms = DataManSystem.createDataManSystemOverUsbAccessory(activity, acc);
            }

            prepareDataManSystem(dms);
          } else {
            callbackErrorSafely(connectCallback, "Activity is not ready");
          }
        } else {
          callbackErrorSafely(connectCallback, "Scanner Permission Error");
        }
      }
    }
  };

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext cbContext) throws JSONException {
      // Context context = this.cordova.getActivity().getApplicationContext();
      if (action.equals("isConnected")) {

        if (dms != null && dms.isConnected()) cbContext.success("1");
        else cbContext.success("0");
        return true;

      } else if (action.equals("connect")) {

        if (dms != null && dms.isConnected()) {
          cbContext.error("Already Connected");
        } else {
          boolean isDeviceFound = false;

          this.connectCallback = cbContext;
          this.activity = this.cordova.getActivity();

          UsbManager manager = (UsbManager) this.activity.getSystemService(Context.USB_SERVICE);
          UsbAccessory[] usbAccessoryArr = UsbDiscoverer.getDataManAccessories(this.activity);

          if (usbAccessoryArr != null && usbAccessoryArr.length > 0) {
            for(UsbAccessory usbAccessory: usbAccessoryArr) {
              if ("Cognex".equals(usbAccessory.getManufacturer())) {
                this.activity.registerReceiver(usbPermissionReceiver, new IntentFilter(ACTION_USB_PERMISSION));
                manager.requestPermission(usbAccessory, PendingIntent.getBroadcast(this.activity, 0, new Intent(ACTION_USB_PERMISSION), 0));
                isDeviceFound = true;
              }
            }
          } 

          if (!isDeviceFound) {
            cbContext.error("Scanner was not found");
          }
        }
        return true;

      } else if (action.equals("close")) {

        if (dms != null && dms.isConnected()) {
          this.closeCallback = cbContext;
          this.isClosing = true;
          this.dms.disconnect();
        } else {
          cbContext.error("Not Connected");
        }
        return true;

      } else if (action.equals("set")) {

        this.callbackContext = cbContext;
        return true;

      }
      return false;
  }
}
