package com.github.axet.callrecorder.services;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;

/**
 * Accessibility service, which does nothing aside from being valid service,
 * so users can enable it.
 *
 * Since Android 10+ applications can't record sound in the background,
 * with few exceptions including apps with enabled accessibility service.
 *
 * With this service enabled, app is able to record sound in background
 * on devices with Android 10+ (sdk 29+)
 */
public class VoiceRecognitionService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Nothing to do
    }

    @Override
    public void onInterrupt() {
        // Nothing to do
    }

    @Override
    public void onServiceConnected() {
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        // Set the type of events that this service wants to listen to. Others
        // won't be passed to this service.
        info.eventTypes = 0;

        // If you only want this service to work with specific applications, set their
        // package names here. Otherwise, when the service is activated, it will listen
        // to events from all applications.
        info.packageNames = new String[]{};

        // Set the type of feedback your service will provide.
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_AUDIBLE;

        // Default services are invoked only if no package-specific ones are present
        // for the type of AccessibilityEvent generated. This service *is*
        // application-specific, so the flag isn't necessary. If this was a
        // general-purpose service, it would be worth considering setting the
        // DEFAULT flag.

        // info.flags = AccessibilityServiceInfo.DEFAULT;

        info.notificationTimeout = 100;

        this.setServiceInfo(info);

    }

}
