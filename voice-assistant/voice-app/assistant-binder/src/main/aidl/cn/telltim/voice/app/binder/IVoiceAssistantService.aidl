// IVoiceAssistantService.aidl
package cn.telltim.voice.app.binder;

// Declare any non-default types here with import statements
import cn.telltim.voice.app.binder.IResponse;

interface IVoiceAssistantService {
    void voiceAssistantApi(int code, inout Bundle args, IResponse response);
}