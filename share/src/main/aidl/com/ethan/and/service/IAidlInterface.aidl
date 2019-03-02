// IAidlInterface.aidl
package com.ethan.and.service;

// Declare any non-default types here with import statements

interface IAidlInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    void startService();
    void stopService();
    boolean isServerAlive();
    String getIp();
    int getPort();
}
