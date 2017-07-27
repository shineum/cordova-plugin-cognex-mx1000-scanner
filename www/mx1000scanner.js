    var exec = cordova.require("cordova/exec");

    /**
     * Constructor.
     *
     * @returns {Mx1000Scanner}
     */
    function Mx1000Scanner() {
    }

    /**
     * connect
     * Connect to scanner.
     */
    Mx1000Scanner.prototype.connect = function (successCallback, errorCallback, config) {
      exec(
          function(result) {
              try {
                  successCallback(result);
              } catch (err) {}              
          }
          , function(error) {
                try {
                  errorCallback(error);
                } catch (err) {}
          }            
          , 'MX1000Scanner', "connect", null);
    };


    /**
     * close
     * Close scanner connection.
     */
    Mx1000Scanner.prototype.close = function (successCallback, errorCallback, config) {
      exec(
          function(result) {
              try {
                  successCallback(result);
              } catch (err) {}              
          }
          , function(error) {
                try {
                  errorCallback(error);
                } catch (err) {}
          }
          , 'MX1000Scanner', "close", null);
    };

    /**
     * isConnected
     * Check scanner is connected.
     */
    Mx1000Scanner.prototype.isConnected = function (successCallback, errorCallback, config) {
      exec(
          function(result) {
              try {
                  successCallback(result == "1" ? true : false);
              } catch (err) {}
          }
          , null, 'MX1000Scanner', "isConnected", null);
    };

    /**
     * set
     * Set callbacks
     */
    Mx1000Scanner.prototype.set = function (successCallback, errorCallback) {
          exec(
            function(result) {
                try {
                  successCallback(result);
                } catch (err) {}
                mx1000Scanner.set(successCallback, errorCallback);
            }
            , function(error) {
                try {
                  errorCallback(error);
                } catch (err) {}
                mx1000Scanner.set(successCallback, errorCallback);
            }, 'MX1000Scanner', 'set', null);
    };

    var mx1000Scanner = new Mx1000Scanner();
    module.exports = mx1000Scanner;

