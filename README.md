# cordova-plugin-cognex-mx1000-scanner
Cordova Beam Scanner for Cognex MX1000

# Install

```javascript

cordova plugin add cordova-plugin-cognex-mx1000-scanner

```

# Usages

### 1.Set scan callback

```javascript
// If the device is connected, whenever the barcodes are read by scanner, these callback will be called.
cordova.plugins.cognex.mx1000scanner.set(successCallback, errorCallback);

var successCallback = function(result) {
  console.log(result);
}

var errorCallback = function(error) {
  console.log(error);
}

```

### 2.Connect scanner

```javascript
// Connect to USB device
cordova.plugins.cognex.mx1000scanner.connect(successCallback, errorCallback);

var successCallback = function(result) {
  // now it is ready to scan
}

var errorCallback = function(error) {
  // scanner is not connected properly (read callback will not be called)
}

```

### 3.Close connection

```javascript
// Close the connection
cordova.plugins.cognex.mx1000scanner.close(successCallback, errorCallback);

var successCallback = function(result) {
  // device is closed
}

var errorCallback = function(error) {
  // some error was occured
}

```

### 4.Check connection status

```javascript
cordova.plugins.cognex.mx1000scanner.isConnected(resultCallback);

var resultCallback = function(result) {
// result will be true if device is connected
// otherwise result will be false
}

```

# History
0.0.2
- Update script

0.0.1
- Release

# License

MIT
