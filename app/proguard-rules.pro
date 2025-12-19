# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# OkHttp Platform Detection - Ignore missing optional dependencies
# These are optional TLS providers that OkHttp checks for at runtime
-dontwarn okhttp3.internal.platform.**
-dontwarn org.bouncycastle.jsse.**
-dontwarn org.conscrypt.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.impl.StaticLoggerBinder

# Keep OkHttp platform classes
-keep class okhttp3.internal.platform.** { *; }
-keep class okhttp3.** { *; }

# Keep OkHttp platform detection methods
-keepclassmembers class okhttp3.internal.platform.Platform {
    *;
}

# Keep OkHttp optional platform implementations
-keep class okhttp3.internal.platform.BouncyCastlePlatform { *; }
-keep class okhttp3.internal.platform.ConscryptPlatform { *; }
-keep class okhttp3.internal.platform.OpenJSSEPlatform { *; }

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ============================================
# JNI Native Code Rules - Keep MainActivity fields and methods accessible from native code
# ============================================

# Keep MainActivity class and all its members that are accessed via JNI
-keep class com.outdu.camconnect.MainActivity {
    # Keep the field that native code accesses
    long nativeCustomData;

    # Keep all native methods (external functions)
    native <methods>;

    # Keep methods called from native code via JNI
    public void setMessage(java.lang.String);
    public void odCallback(int[], float[], int[], int[], int[], int[], float[]);
    public void onGStreamerInitialized();
    public void onStreamError(int);

    # Keep the companion object's native method
    public static native boolean nativeClassInit(long);
}

# Keep JNI-related attributes
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# Keep native method names (important for JNI)
-keepclasseswithmembernames class * {
    native <methods>;
}

# ============================================
# GStreamer Rules - Keep all GStreamer classes and native methods
# ============================================

# Keep all GStreamer classes (they are accessed from native code)
-keep class org.freedesktop.gstreamer.** { *; }
-keepclassmembers class org.freedesktop.gstreamer.** {
    *;
}

# Specifically keep GStreamer Android media callback classes
-keep class org.freedesktop.gstreamer.androidmedia.GstAhsCallback { *; }
-keep class org.freedesktop.gstreamer.androidmedia.GstAhcCallback { *; }
-keep class org.freedesktop.gstreamer.androidmedia.GstAmcOnFrameAvailableListener { *; }

# Keep all native methods in GStreamer classes
-keepclasseswithmembernames class org.freedesktop.gstreamer.** {
    native <methods>;
}

# ============================================
# Android Camera API Rules - Keep deprecated Camera API fields
# ============================================

# Keep Android Camera.Parameters class and its static fields
# (GStreamer native code accesses deprecated Camera API constants)
-keep class android.hardware.Camera$Parameters {
    public static final java.lang.String *;
}

# Keep Camera class itself (needed for GstAhcCallback)
-keep class android.hardware.Camera { *; }
-keep class android.hardware.Camera$* { *; }

# Keep Sensor classes (needed for GstAhsCallback)
-keep class android.hardware.Sensor { *; }
-keep class android.hardware.SensorEvent { *; }
-keep interface android.hardware.SensorEventListener { *; }

# Keep SurfaceTexture (needed for GstAmcOnFrameAvailableListener)
-keep class android.graphics.SurfaceTexture { *; }
-keep interface android.graphics.SurfaceTexture$OnFrameAvailableListener { *; }