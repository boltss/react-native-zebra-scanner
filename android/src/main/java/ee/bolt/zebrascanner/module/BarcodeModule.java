package ee.bolt.zebrascanner.module;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.symbol.emdk.EMDKManager;
import com.symbol.emdk.EMDKResults;
import com.symbol.emdk.barcode.BarcodeManager;
import com.symbol.emdk.barcode.ScanDataCollection;
import com.symbol.emdk.barcode.Scanner;
import com.symbol.emdk.barcode.ScannerException;
import com.symbol.emdk.barcode.ScannerResults;
import com.symbol.emdk.barcode.StatusData;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class BarcodeModule implements EMDKManager.EMDKListener, Scanner.DataListener, Scanner.StatusListener {

	private static final String TAG = BarcodeModule.class.getSimpleName();

	private EMDKManager emdkManager = null;
	private BarcodeManager barcodeManager = null;
	private Scanner scanner = null;

	private static BarcodeScannedCallback barcodeScannedCallback = null;

	private boolean shouldBeEnabled = false;


	public boolean isScannerPresent() {
		return emdkManager != null;
	}

	public BarcodeModule(Context context) {
		try {
			EMDKResults results = EMDKManager.getEMDKManager(context, this);
			if (results.statusCode != EMDKResults.STATUS_CODE.SUCCESS) {
				log("Status: " + "EMDKManager object request failed!");
			}
		} catch (Exception exception) {
			log("Scanner is not present");
		}
	}

	public void onDestroy() {
		deInitializeScanner();

		barcodeManager = null;
		if (emdkManager != null) {
			emdkManager.release();
			emdkManager = null;
		}
	}

	public void onPause() {
		deInitializeScanner();

		barcodeManager = null;
		if (emdkManager != null) {
			emdkManager.release(EMDKManager.FEATURE_TYPE.BARCODE);
		}
	}

	public void onResume() {
		if (isScannerPresent()) {
			initializeScanner();
		}
	}

	public void enable() {
		shouldBeEnabled = true;
		if (isScannerPresent()) {
			deInitializeScanner();
			initializeScanner();
			try {
				scanner.read();
			} catch (ScannerException e) {
				String statusString = "Enable: " + e.getMessage();
				new AsyncStatusUpdate().execute(statusString);
			}
		}
	}

	public void disable() {
		shouldBeEnabled = false;
		if (isScannerPresent()) {
			deInitializeScanner();
		}
	}

	public void subscribeToBarcodeScanned(BarcodeScannedCallback callback) {
		barcodeScannedCallback = callback;
	}



	@Override
	public void onOpened(EMDKManager emdkManager) {
		this.emdkManager = emdkManager;
		initializeScanner();
	}

	@Override
	public void onClosed() {
		if (emdkManager != null) {
			// Release all the resources
			barcodeManager = null;
			emdkManager.release();
			emdkManager = null;
		}
		log("Status: " + "EMDK closed unexpectedly! Please close and restart the application.");
	}

	@Override
	public void onData(ScanDataCollection scanDataCollection) {
		if ((scanDataCollection != null) && (scanDataCollection.getResult() == ScannerResults.SUCCESS)) {
			ArrayList<ScanDataCollection.ScanData> scanData = scanDataCollection.getScanData();
			for(ScanDataCollection.ScanData data : scanData) {
				String dataString =  data.getData();

				new AsyncDataUpdate().execute(dataString);
			}
		}
	}

	@Override
	public void onStatus(StatusData statusData) {
		String statusString;
		StatusData.ScannerStates state = statusData.getState();
		switch(state) {
			case IDLE:
				statusString = statusData.getFriendlyName()+" is enabled and idle...";
				new AsyncStatusUpdate().execute(statusString);
				if (shouldBeEnabled) {
					try {
						// An attempt to use the scanner continuously and rapidly (with a delay < 100 ms between scans)
						// may cause the scanner to pause momentarily before resuming the scanning.
						// Hence add some delay (>= 100ms) before submitting the next read.
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

						scanner.read();
					} catch (ScannerException e) {
						statusString = "Idle: " + e.getMessage();
						new AsyncStatusUpdate().execute(statusString);
					}
				}
				break;
			case WAITING:
				statusString = "Scanner is waiting for trigger press...";
				new AsyncStatusUpdate().execute(statusString);
				break;
			case SCANNING:
				statusString = "Scanning...";
				new AsyncStatusUpdate().execute(statusString);
				break;
			case DISABLED:
				statusString = statusData.getFriendlyName()+" is disabled.";
				new AsyncStatusUpdate().execute(statusString);
				break;
			case ERROR:
				statusString = "An error has occurred.";
				new AsyncStatusUpdate().execute(statusString);
				break;
			default:
				break;
		}
	}

	private void initializeScanner() {
		if (scanner == null) {
			barcodeManager = (BarcodeManager) emdkManager.getInstance(EMDKManager.FEATURE_TYPE.BARCODE);
			try {
				scanner = barcodeManager.getDevice(BarcodeManager.DeviceIdentifier.DEFAULT);
			} catch (Exception e) {
				log("Scanner creation: " + e.getMessage());
			}

			if (scanner != null) {
				scanner.addDataListener(this);
				scanner.addStatusListener(this);

				try {
					scanner.enable();
				} catch (ScannerException e) {
					log("Status: " + e.getMessage());
				}
			}else{
				log("Status: " + "Failed to initialize the scanner device.");
			}
		}
	}

	private void deInitializeScanner() {
		if (scanner != null) {
			try {
				scanner.cancelRead();
				scanner.disable();
			} catch (Exception e) {
				log("Status: " + e.getMessage());
			}

			try {
				scanner.removeDataListener(this);
				scanner.removeStatusListener(this);
			} catch (Exception e) {
				log("Status: " + e.getMessage());
			}

			try{
				scanner.release();
			} catch (Exception e) {
				log("Status: " + e.getMessage());
			}

			scanner = null;
		}
	}

	private static class AsyncDataUpdate extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			return params[0];
		}

		protected void onPostExecute(String barcode) {
			if (barcode != null) {
				log("Scanned: " + barcode);
				barcodeScannedCallback.barcodeScanned(barcode);
			}
		}
	}

	private static class AsyncStatusUpdate extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... params) {
			return params[0];
		}

		@Override
		protected void onPostExecute(String result) {
			log("Status: " + result);
		}
	}

	private static void log(String message) {
		Log.d(TAG, message);
	}

}
