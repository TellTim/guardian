// IResponse.aidl
package cn.telltim.voice.app.binder;

interface IResponse {
    void onResponse(int code, inout Bundle data);
}