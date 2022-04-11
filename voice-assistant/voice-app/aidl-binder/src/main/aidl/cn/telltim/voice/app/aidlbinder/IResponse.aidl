// IResponse.aidl
package cn.telltim.voice.app.aidlbinder;

interface IResponse {
    void onResponse(int code, inout Bundle data);
}