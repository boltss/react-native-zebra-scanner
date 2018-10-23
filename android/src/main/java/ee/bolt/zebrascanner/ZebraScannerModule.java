package ee.bolt.zebrascanner;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import ee.bolt.zebrascanner.module.BarcodeModule;
import ee.bolt.zebrascanner.module.BarcodeScannedCallback;


public class ZebraScannerModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

	private final BarcodeModule barcodeModule;
	private static final String BARCODE_SCANNED_EVENT = "barcodeScanned";


    public ZebraScannerModule(ReactApplicationContext reactContext) {
        super(reactContext);
	    reactContext.addLifecycleEventListener(this);

	    barcodeModule = new BarcodeModule(getReactApplicationContext());

	    barcodeModule.subscribeToBarcodeScanned(new BarcodeScannedCallback() {
		    @Override
		    public void barcodeScanned(String barcode) {
			    getReactApplicationContext()
					    .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
					    .emit(BARCODE_SCANNED_EVENT, barcode);
		    }
	    });
    }

    @Override
    public String getName() {
    	return "ZebraScanner";
    }


	@Override
	public void onHostResume() {
		barcodeModule.onResume();
	}

	@Override
	public void onHostPause() {
		barcodeModule.onPause();
	}

	@Override
	public void onHostDestroy() {
		barcodeModule.onDestroy();
	}


	@ReactMethod
	public void isAvailable(Promise promise) {
		promise.resolve(barcodeModule.isScannerPresent());
	}

	@ReactMethod
	public void enable() {
    	barcodeModule.enable();
	}

	@ReactMethod
	public void disable() {
		barcodeModule.disable();
	}
}