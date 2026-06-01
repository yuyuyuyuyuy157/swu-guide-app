package com.swu.guide;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.speech.tts.TextToSpeech;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.List;
import java.util.Locale;

@CapacitorPlugin(name = "NativeTts")
public class NativeTtsPlugin extends Plugin {
    private TextToSpeech tts;
    private boolean ready = false;
    private boolean initFailed = false;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private PluginCall pendingCall;
    private String pendingText;
    private float pendingRate = 1.0f;

    @Override
    public void load() {
        initTts();
    }

    private void initTts() {
        if (tts != null) {
            return;
        }
        tts = new TextToSpeech(getContext(), status -> {
            ready = status == TextToSpeech.SUCCESS;
            if (ready) {
                int languageResult = tts.setLanguage(Locale.CHINA);
                if (languageResult == TextToSpeech.LANG_MISSING_DATA || languageResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    tts.setLanguage(Locale.CHINESE);
                }
                speakPending();
            } else {
                initFailed = true;
                rejectPending("Native TTS init failed");
            }
        });
    }

    @PluginMethod
    public void speak(PluginCall call) {
        String text = call.getString("text", "");
        if (text == null || text.trim().isEmpty()) {
            call.reject("Text is empty");
            return;
        }
        if (!hasInstalledTtsEngine()) {
            call.reject("No TTS engine installed", "NO_TTS_ENGINE");
            return;
        }
        Double rateValue = call.getDouble("rate", 1.0);
        float rate = normalizeRate(rateValue == null ? 1.0f : rateValue.floatValue());

        if (tts == null) {
            initTts();
        }
        if (initFailed) {
            call.reject("Native TTS init failed", "TTS_INIT_FAILED");
            return;
        }
        if (!ready) {
            pendingCall = call;
            pendingText = text;
            pendingRate = rate;
            mainHandler.postDelayed(() -> {
                if (pendingCall == call && !ready) {
                    rejectPending("Native TTS is not ready", "TTS_NOT_READY");
                }
            }, 5000);
            return;
        }

        speakNow(call, text, rate);
    }

    private float normalizeRate(float rate) {
        return Math.max(0.5f, Math.min(2.0f, rate));
    }

    private void speakNow(PluginCall call, String text, float rate) {
        tts.setSpeechRate(rate);
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "swu-guide-tts");

        JSObject result = new JSObject();
        result.put("speaking", true);
        call.resolve(result);
    }

    private void speakPending() {
        if (pendingCall == null || pendingText == null) {
            return;
        }
        PluginCall call = pendingCall;
        String text = pendingText;
        float rate = pendingRate;
        pendingCall = null;
        pendingText = null;
        speakNow(call, text, rate);
    }

    private void rejectPending(String message) {
        rejectPending(message, null);
    }

    private void rejectPending(String message, String code) {
        if (pendingCall != null) {
            if (code == null) {
                pendingCall.reject(message);
            } else {
                pendingCall.reject(message, code);
            }
            pendingCall = null;
            pendingText = null;
        }
    }

    @PluginMethod
    public void getStatus(PluginCall call) {
        JSObject result = new JSObject();
        result.put("ready", ready);
        result.put("hasEngine", hasInstalledTtsEngine());
        result.put("defaultEngine", tts != null ? tts.getDefaultEngine() : "");
        result.put("engineCount", getTtsEngineCount());
        call.resolve(result);
    }

    @PluginMethod
    public void openSettings(PluginCall call) {
        if (openActivity("android.settings.TEXT_READING_SETTINGS")
                || openActivity(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                || openActivity(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                || openActivity(Settings.ACTION_SETTINGS)) {
            call.resolve();
            return;
        }
        call.reject("Cannot open TTS settings", "OPEN_SETTINGS_FAILED");
    }

    private boolean hasInstalledTtsEngine() {
        return getTtsEngineCount() > 0;
    }

    private int getTtsEngineCount() {
        PackageManager packageManager = getContext().getPackageManager();
        Intent intent = new Intent(TextToSpeech.Engine.INTENT_ACTION_TTS_SERVICE);
        List<ResolveInfo> engines = packageManager.queryIntentServices(intent, 0);
        return engines == null ? 0 : engines.size();
    }

    private boolean openActivity(String action) {
        Intent intent = new Intent(action);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (intent.resolveActivity(getContext().getPackageManager()) == null) {
            return false;
        }
        getContext().startActivity(intent);
        return true;
    }

    @PluginMethod
    public void stop(PluginCall call) {
        pendingCall = null;
        pendingText = null;
        if (tts != null) {
            tts.stop();
        }
        call.resolve();
    }

    @Override
    protected void handleOnDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
            tts = null;
        }
        pendingCall = null;
        pendingText = null;
        super.handleOnDestroy();
    }
}
