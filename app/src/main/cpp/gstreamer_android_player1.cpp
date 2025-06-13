#include <jni.h>
#include <gst/gst.h>
#include <gio/gio.h>
#include <android/log.h>
#include <android/native_window.h>
#include <android/native_window_jni.h>
#include <gst/video/video.h>
#include <gst/video/videooverlay.h>
#include <string>
#include <android/asset_manager_jni.h>
#include <opencv2/core/core.hpp>
#include <gst/app/gstappsink.h>

#define RTSP_URL "rtsp://onvif:test@192.168.2.1/live1.sdp"

GST_DEBUG_CATEGORY_STATIC (debug_category);
#define GST_CAT_DEFAULT debug_category

/*
 * These macros provide a way to store the native pointer to CustomData, which might be 32 or 64 bits, into
 * a jlong, which is always 64 bits, without warnings.
 */
#if GLIB_SIZEOF_VOID_P == 8
# define GET_CUSTOM_DATA(env, thiz, fieldID) (CustomData *)(env)->GetLongField (thiz, fieldID)
# define SET_CUSTOM_DATA(env, thiz, fieldID, data) (env)->SetLongField (thiz, fieldID, (jlong)data)
#else
# define GET_CUSTOM_DATA(env, thiz, fieldID) (CustomData *)(jint) (env)->GetLongField (thiz, fieldID)
# define SET_CUSTOM_DATA(env, thiz, fieldID, data) env->SetLongField (thiz, fieldID, (jlong)(jint)(data))
#endif

typedef struct _CustomData {
    jobject app;           /* Application instance, used to call its methods. A global reference is kept. */
    GstElement *pipeline;  /* The running pipeline */
    GMainContext *context; /* GLib context used to run the main loop */
    GMainLoop *main_loop;  /* GLib main loop */
    gboolean initialized;  /* To avoid informing the UI multiple times about the initialization */
    GstElement *video_sink; /* The video sink element which receives VideoOverlay commands */
    GstElement *app_sink; /* The video sink element which receives VideoOverlay commands */
    ANativeWindow *native_window; /* The Android native window where video will be rendered */
    char avc_decoder[100];
    char storage_path[250];
    jlong currentTimeMillis;
} CustomData;

static char const* TAG = "GStreamerPlayer";
static JavaVM  *java_vm = nullptr;

static pthread_t gst_app_thread;
static pthread_key_t current_jni_env;
static jfieldID custom_data_field_id;
static jmethodID set_message_method_id;
static jmethodID od_callback_id;
static jmethodID on_gstreamer_initialized_method_id;

/* Register this thread with the VM */
static JNIEnv *attach_current_thread () {
    JNIEnv *env;
    JavaVMAttachArgs args;

    GST_DEBUG ("Attaching thread %p", g_thread_self ());
    args.version = JNI_VERSION_1_6;
    args.name = nullptr;
    args.group = nullptr;

    if (java_vm->AttachCurrentThread (&env, &args) < 0) {
        GST_ERROR ("Failed to attach current thread");
        return nullptr;
    }

    return env;
}

/* Unregister this thread from the VM */
static void detach_current_thread (void *env) {
    GST_DEBUG ("Detaching thread %p", g_thread_self ());
    java_vm->DetachCurrentThread ();
}

/* Retrieve the JNI environment for this thread */
static JNIEnv *get_jni_env () {
    JNIEnv *env;

    if ((env = (JNIEnv *)pthread_getspecific (current_jni_env)) == nullptr) {
        env = attach_current_thread ();
        pthread_setspecific (current_jni_env, env);
    }

    return env;
}

/* Change the content of the UI's TextView */
static void set_ui_message (const gchar *message, CustomData *data) {
    JNIEnv *env = get_jni_env ();
    GST_DEBUG ("Setting message to: %s", message);
    jstring jmessage = env->NewStringUTF(message);
    env->CallVoidMethod (data->app, set_message_method_id, jmessage);
    if (env->ExceptionCheck ()) {
        GST_ERROR ("Failed to call Java method");
        env->ExceptionClear ();
    }
    env->DeleteLocalRef (jmessage);
}

/* Retrieve errors from the bus and show them on the UI */
static void error_cb (GstBus *bus, GstMessage *msg, CustomData *data) {
    GError *err;
    gchar *debug_info;
    gchar *message_string;

    gst_message_parse_error (msg, &err, &debug_info);
    message_string = g_strdup_printf ("Error received from element %s: %s", GST_OBJECT_NAME (msg->src), err->message);
    g_clear_error (&err);
    g_free (debug_info);
    set_ui_message (message_string, data);
    g_free (message_string);
    gst_element_set_state (data->pipeline, GST_STATE_NULL);
}

/* Retrieve errors from the bus and show them on the UI */
static void element_cb (GstBus *bus, GstMessage *msg, CustomData *data) {
    GError *err;
    gchar *debug_info;
    gchar *message_string;

    const GstStructure *structure = gst_message_get_structure(msg);
//    GstElement *dec=gst_bin_get_by_interface(GST_BIN(data->pipeline), GST_TYPE_VIDEO_DECODER);

    if (gst_structure_has_name(structure, "element")) {
        const gchar *element_name = gst_structure_get_string(structure, "name");
        message_string = g_strdup_printf ("Decoder used by decodebin: %s", element_name);
        set_ui_message (message_string, data);
        g_free (message_string);
    }
}

/* Notify UI about pipeline state changes */
static void state_changed_cb (GstBus *bus, GstMessage *msg, CustomData *data) {
    GstState old_state, new_state, pending_state;
    gst_message_parse_state_changed (msg, &old_state, &new_state, &pending_state);
    /* Only pay attention to messages coming from the pipeline, not its children */
    if (GST_MESSAGE_SRC (msg) == GST_OBJECT (data->pipeline)) {
        gchar *message = g_strdup_printf("State changed to %s", gst_element_state_get_name(new_state));
        set_ui_message(message, data);
        g_free (message);
    }
}

/* Check if all conditions are met to report GStreamer as initialized.
 * These conditions will change depending on the application */
static void check_initialization_complete (CustomData *data) {
    JNIEnv *env = get_jni_env ();
    if (!data->initialized && data->native_window && data->main_loop) {
        GST_DEBUG ("Initialization complete, notifying application. main_loop:%p", data->main_loop);

        /* The main loop is running and we received a native window, inform the sink about it */
        gst_video_overlay_set_window_handle (GST_VIDEO_OVERLAY (data->video_sink), (guintptr)data->native_window);

        env->CallVoidMethod (data->app, on_gstreamer_initialized_method_id);
        if (env->ExceptionCheck ()) {
            GST_ERROR ("Failed to call Java method");
            env->ExceptionClear ();
        }
        data->initialized = TRUE;
    }
}

static void *app_function (void *userdata) {
    JavaVMAttachArgs args;
    GstBus *bus;
    auto *data = (CustomData *) userdata;
    GSource *bus_source;
    GError *error = nullptr;

    GST_DEBUG ("Creating pipeline in CustomData at %p", data);

    /* Create our own GLib Main Context and make it the default one */
    data->context = g_main_context_new();
    g_main_context_push_thread_default(data->context);

    /* Build pipeline */
    char rtsp_pipeline[1000];
    sprintf(rtsp_pipeline, "rtspsrc location=%s latency=200 drop-on-latency=true ! "
                           "rtph264depay ! h264parse ! amcviddec-%s ! glimagesink",
            RTSP_URL, data->avc_decoder);

    data->pipeline = gst_parse_launch(rtsp_pipeline, &error);
    if (error) {
        gchar *message = g_strdup_printf("Unable to build pipeline: %s", error->message);
        g_clear_error (&error);
        set_ui_message(message, data);
        g_free (message);
        return nullptr;
    }

    gst_element_set_state(data->pipeline, GST_STATE_PLAYING);
    data->video_sink = gst_bin_get_by_interface(GST_BIN(data->pipeline), GST_TYPE_VIDEO_OVERLAY);

    if (!data->video_sink) {
        GST_ERROR ("Could not retrieve video sink/app sink");
        return nullptr;
    }

    bus = gst_element_get_bus (data->pipeline);
    bus_source = gst_bus_create_watch (bus);
    g_source_set_callback (bus_source, (GSourceFunc) gst_bus_async_signal_func, nullptr, nullptr);
    g_source_attach (bus_source, data->context);
    g_source_unref (bus_source);
    g_signal_connect (G_OBJECT (bus), "message::error", (GCallback)error_cb, data);
    g_signal_connect (G_OBJECT (bus), "message::state-changed", (GCallback)state_changed_cb, data);
    g_signal_connect (G_OBJECT (bus), "message::element", (GCallback)element_cb, data);

    GST_DEBUG ("Entering main loop... (CustomData:%p)", data);
    data->main_loop = g_main_loop_new (data->context, FALSE);
    check_initialization_complete (data);
    g_main_loop_run (data->main_loop);
    GST_DEBUG ("Exited main loop");
    g_main_loop_unref (data->main_loop);
    data->main_loop = nullptr;

    /* Free resources */
    g_main_context_pop_thread_default(data->context);
    g_main_context_unref (data->context);
    gst_element_set_state (data->pipeline, GST_STATE_NULL);
    gst_object_unref (data->video_sink);
    gst_object_unref (data->app_sink);
    gst_object_unref (data->pipeline);

    return nullptr;
}


static jboolean gst_native_class_init (JNIEnv* env, jclass klass, jlong currentTimeMillis) {
    if(currentTimeMillis>1704047400000) JNI_FALSE;
    custom_data_field_id = env->GetFieldID (klass, "nativeCustomData", "J");
    set_message_method_id = env->GetMethodID (klass, "setMessage", "(Ljava/lang/String;)V");
//    od_callback_id = env->GetMethodID (klass, "odCallback", "([I[F[I[I[I[I[F)V");
//    on_gstreamer_initialized_method_id = env->GetMethodID (klass, "onGStreamerInitialized", "()V");

    if (!custom_data_field_id || !set_message_method_id || !on_gstreamer_initialized_method_id || !od_callback_id) {
        /* We emit this message through the Android log instead of the GStreamer log because the later
         * has not been initialized yet.
         */
        __android_log_print (ANDROID_LOG_ERROR, TAG, "The calling class does not implement all necessary interface methods");
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

/* Instruct the native code to create its internal data structure, pipeline and thread */
static void gst_native_init (JNIEnv* env, jobject thiz, jstring avc_decoder) {
    CustomData *data = g_new0 (CustomData, 1);
    SET_CUSTOM_DATA (env, thiz, custom_data_field_id, data);
    GST_DEBUG_CATEGORY_INIT (debug_category, TAG, 0, "GStreamer Player");
    gst_debug_set_threshold_for_name(TAG, GST_LEVEL_DEBUG);
    GST_DEBUG ("Created CustomData at %p", data);
    data->app = env->NewGlobalRef (thiz);
    GST_DEBUG ("Created GlobalRef for app object at %p", data->app);
    const char* charArray = env->GetStringUTFChars(avc_decoder, nullptr);
    if(charArray != nullptr) {
        sprintf(data->avc_decoder, "%s", charArray);
    }
    env->ReleaseStringUTFChars(avc_decoder, charArray);
//    pthread_create (&gst_app_thread, nullptr, &app_function, data);
}

static void gst_native_surface_init (JNIEnv *env, jobject thiz, jobject surface) {
    auto *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    if (!data) return;
    ANativeWindow *new_native_window = ANativeWindow_fromSurface(env, surface);
    GST_DEBUG ("Received surface %p (native window %p)", surface, new_native_window);

    if (data->native_window) {
        ANativeWindow_release (data->native_window);
    }
    data->native_window = new_native_window;

    check_initialization_complete (data);
}

static void gst_native_play(JNIEnv* env, jobject thiz){
    auto *data = GET_CUSTOM_DATA(env, thiz, custom_data_field_id);
    if(!data) return;

    pthread_create(&gst_app_thread, nullptr, &app_function, data);
}

static void gst_native_pause (JNIEnv* env, jobject thiz) {
    auto *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    if (!data) return;
    GST_DEBUG ("Setting state to PAUSED");
    gst_element_set_state (data->pipeline, GST_STATE_NULL);
    g_main_loop_quit (data->main_loop);
    GST_DEBUG ("Waiting for thread to finish...");
    pthread_join (gst_app_thread, nullptr);
}


static void gst_native_surface_finalize (JNIEnv *env, jobject thiz) {
    auto *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    if (!data) return;
    GST_DEBUG ("Releasing Native Window %p", data->native_window);

    if (data->pipeline) {
        gst_video_overlay_set_window_handle (GST_VIDEO_OVERLAY (data->pipeline), (guintptr)nullptr);
        gst_element_set_state (data->pipeline, GST_STATE_READY);
    }

    ANativeWindow_release (data->native_window);
    data->native_window = nullptr;
    data->initialized = FALSE;
}

static JNINativeMethod native_methods[] = {
        { "nativeClassInit", "(J)Z", (void *) gst_native_class_init},
        {"nativeInit", "(Ljava/lang/String;)V", (void *) gst_native_init},
        {"nativePlay", "()V", (void *)gst_native_play},
        { "nativePause", "()V", (void *) gst_native_pause},
        { "nativeSurfaceInit", "(Ljava/lang/Object;)V", (void *) gst_native_surface_init},
        { "nativeSurfaceFinalize", "()V", (void *) gst_native_surface_finalize},
};

jint JNI_OnLoad(JavaVM* vm, void* reserved) {
    java_vm = vm;

    JNIEnv* env;
    if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Could not retrieve JNIEnv");
        return -1;
    }

    jclass klass = env->FindClass("com/outdu/camconnect/MainActivity");
    if (!klass) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Could not retrieve class com.example.gstreamer.GStreamerPlayer");
        return -1;
    }

    if (env->RegisterNatives(klass, native_methods, G_N_ELEMENTS(native_methods))) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Could not register native methods for org.freedesktop.gstreamer.GStreamer");
        return -1;
    }
    pthread_key_create (&current_jni_env, detach_current_thread);

    return JNI_VERSION_1_6;
}