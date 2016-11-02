package com.peerstars.android.pstutilities;

/**
 * Created by bmiller on 8/26/2015.
 */
public interface ICallbacks {
    void callbackProgress(int value);

    void callbackComplete();

    void callbackCompleteWithError(String error);

    void callbackResult(String result);

    void callbackResultObject(Object result);
}
