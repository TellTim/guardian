// IVoiceAssistantService.aidl
package cn.telltim.voice.app.aidlbinder;

// Declare any non-default types here with import statements
import cn.telltim.voice.app.aidlbinder.IResponse;

interface IVoiceAssistantService {
    void voiceAssistantApi(int code, inout Bundle args, IResponse response);
}