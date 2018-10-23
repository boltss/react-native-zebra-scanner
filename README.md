
# react-native-zebra-scanner

## Getting started

`$ npm install react-native-zebra-scanner --save`

### Mostly automatic installation

`$ react-native link react-native-zebra-scanner`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainApplication.java`
  - Add `new ZebraScannerPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-zebra-scanner'
  	project(':react-native-zebra-scanner').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-zebra-scanner/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      implementation project(':react-native-zebra-scanner')
  	```


## Usage
```javascript
import ZebraScanner from 'react-native-zebra-scanner';

// Check if hardware scanner is available
await ZebraScanner.isAvailable(); // true or false

// Add listener
const scanListener = (scannedCode) => {
	// scannedCode is string. '01245234562345' etc.
}
ZebraScanner.addScanListener(scanListener)
// Remove listener
ZebraScanner.removeScanListener(scanListener)
```
  