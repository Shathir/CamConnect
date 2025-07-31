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
#include "yolo.h"
#include "midas.h"

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

/* Structure to contain all our information, so we can pass it to callbacks */
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
    gboolean od;
    gboolean ds;
    gboolean far_roi;
    jint width;
    jint height;
    jlong currentTimeMillis;
} CustomData;


static Yolo* g_yolo = 0;
static Midas* g_midas = 0;

static ncnn::Mutex lock;

static char const* TAG = "GStreamerPlayer";
static JavaVM  *java_vm = nullptr;

static pthread_t gst_app_thread;
static pthread_key_t current_jni_env;
static jfieldID custom_data_field_id;
static jmethodID set_message_method_id;
static jmethodID od_callback_id;
static jmethodID on_gstreamer_initialized_method_id;

/* Register this thread with the VM */
static JNIEnv *attach_current_thread (void) {
    JNIEnv *env;
    JavaVMAttachArgs args;

    GST_DEBUG ("Attaching thread %p", g_thread_self ());
    args.version = JNI_VERSION_1_6;
    args.name = NULL;
    args.group = NULL;

    if (java_vm->AttachCurrentThread (&env, &args) < 0) {
        GST_ERROR ("Failed to attach current thread");
        return NULL;
    }

    return env;
}

/* Unregister this thread from the VM */
static void detach_current_thread (void *env) {
    GST_DEBUG ("Detaching thread %p", g_thread_self ());
    java_vm->DetachCurrentThread ();
}

/* Retrieve the JNI environment for this thread */
static JNIEnv *get_jni_env (void) {
    JNIEnv *env;

    if ((env = (JNIEnv *)pthread_getspecific (current_jni_env)) == NULL) {
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

static void od_callback(const std::vector<Object> &objects, const std::vector<float> &dep_thres, CustomData *data) {
    JNIEnv *env = get_jni_env ();
    GST_DEBUG ("od_callback");

    jintArray labelArray = (env)->NewIntArray(objects.size());
    jfloatArray probArray = (env)->NewFloatArray(objects.size());
    jintArray pointxArray = (env)->NewIntArray(objects.size());
    jintArray pointyArray = (env)->NewIntArray(objects.size());
    jintArray pointwArray = (env)->NewIntArray(objects.size());
    jintArray pointhArray = (env)->NewIntArray(objects.size());
    jfloatArray depThresArray = (env)->NewFloatArray(dep_thres.size());
    for (int i = 0; i < objects.size(); i++) {
        jint label = objects[i].label;  // Assuming objects is a vector of struct Object
        jfloat prob = objects[i].prob;
        jint point_x = objects[i].rect.x*1920;//points[i].x;
        jint point_y = objects[i].rect.y*1080;//points[i].y;
        jint point_w = objects[i].rect.width*1920;//points[i].y;
        jint point_h = objects[i].rect.height*1080;//points[i].y;
        jfloat depThres = 0.;
        if(data->ds && dep_thres.size()>0){
            depThres=dep_thres[i];
        }

        // Set the 'label' and 'prob' values in the corresponding arrays
        (env)->SetIntArrayRegion(labelArray, i, 1, &label);
        (env)->SetIntArrayRegion(pointxArray, i, 1, &point_x);
        (env)->SetIntArrayRegion(pointyArray, i, 1, &point_y);
        (env)->SetIntArrayRegion(pointwArray, i, 1, &point_w);
        (env)->SetIntArrayRegion(pointhArray, i, 1, &point_h);
        (env)->SetFloatArrayRegion(probArray, i, 1, &prob);
        if(data->ds && dep_thres.size()>0){
            (env)->SetFloatArrayRegion(depThresArray, i, 1, &depThres);
        }
    }
    env->CallVoidMethod (data->app, od_callback_id, labelArray, probArray,
                         pointxArray, pointyArray, pointwArray, pointhArray, depThresArray);
    if (env->ExceptionCheck ()) {
        GST_ERROR ("Failed to call Java method");
        env->ExceptionClear ();
    }
    env->DeleteLocalRef (labelArray);
    env->DeleteLocalRef (probArray);
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

static GstFlowReturn new_sample (GstElement *sink, CustomData *data) {
    GstSample *sample;

    /* Retrieve the buffer */
    g_signal_emit_by_name (sink, "pull-sample", &sample);
    if (sample) {
        GST_DEBUG ("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@got sample");
        GST_DEBUG ("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@got %d od", data->od);
        GstBuffer *buffer = gst_sample_get_buffer(sample);
        GstCaps* caps = NULL;
        const GstStructure* info = NULL;
        caps = gst_sample_get_caps (sample);
        info = gst_caps_get_structure (caps, 0);
        int sample_width = 0;
        int sample_height = 0;

        gst_structure_get_int (info, "width", &sample_width);
        gst_structure_get_int (info, "height", &sample_height);
        GstMapInfo gstBufferMap;
        gboolean bret = gst_buffer_map(buffer, &gstBufferMap, GST_MAP_READ);
        if (!bret)
        {
            gst_sample_unref(sample);
            return GST_FLOW_ERROR;
        }
        cv::Mat bgr(sample_height, sample_width, CV_8UC3);
        memcpy(bgr.data, gstBufferMap.data, gstBufferMap.size);
        gst_buffer_unmap(buffer, &gstBufferMap);
        gst_sample_unref(sample);
        // nanodet
        {
            ncnn::MutexLockGuard g(lock);

            if (data->od && g_yolo) {
                GST_DEBUG ("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@got %d x %d bgr", sample_width, sample_height);
                std::vector<float> dep_thres;
                int midas_ret=1;
//                dep_thres.reserve(objects.size());
                if(data->ds && g_midas) {
                    midas_ret=g_midas->invokeProcessAsync(bgr);
                }
                std::vector<Object> objects;
                g_yolo->detect(bgr, objects);//yolo od threshold 0.6
                std::vector<cv::Point2f> points2F;
                points2F.reserve(objects.size());
                for(Object &obj:objects) {
                    points2F.emplace_back((obj.rect.x+obj.rect.width/2), (obj.rect.y+obj.rect.height/2));
                    obj.rect.x=obj.rect.x/bgr.cols;
                    obj.rect.y=obj.rect.y/bgr.rows;
                    obj.rect.width=obj.rect.width/bgr.cols;
                    obj.rect.height=obj.rect.height/bgr.rows;
                }

                GST_DEBUG ("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@got %d objects", objects.size());
                if(data->ds && g_midas) {
                    if(midas_ret==0) {
                        g_midas->updatePoints(points2F);
                        g_midas->postProcess(bgr.cols, bgr.rows, dep_thres);
                    }
                }

                GST_DEBUG ("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@got %d objects", objects.size());
                od_callback(objects, dep_thres, data);
            }
        }
        return GST_FLOW_OK;
    }

    return GST_FLOW_ERROR;
}
/* Main method for the native code. This is executed on its own thread. */
static void *app_function (void *userdata) {
    JavaVMAttachArgs args;
    GstBus *bus;
    CustomData *data = (CustomData *)userdata;
    GSource *bus_source;
    GError *error = NULL;

    GST_DEBUG ("Creating pipeline in CustomData at %p", data);

    /* Create our own GLib Main Context and make it the default one */
    data->context = g_main_context_new ();
    g_main_context_push_thread_default(data->context);

    /* Build pipeline */
    char rtsp_pipeline[1000];
    if(data->od) {

        sprintf(rtsp_pipeline, "rtspsrc location=%s latency=100 drop-on-latency=true ! "
                               "rtph264depay ! h264parse ! amcviddec-%s ! tee name=t ! "
                               "queue leaky=2 max-size-buffers=2 ! "
                               "glimagesink t. ! "
                               "queue leaky=2 max-size-buffers=2 ! "
                               "glcolorconvert ! gldownload ! "
                               "video/x-raw,width=1920,height=1080,format=BGR ! "
                               "videoscale ! "
                               "video/x-raw,width=960,height=540,format=BGR ! "
                               "appsink max-buffers=2 drop=true name=rtspappsink",
                RTSP_URL, data->avc_decoder);
        /*sprintf(rtsp_pipeline, "rtspsrc location=%s latency=100 drop-on-latency=true ! "
                               "rtph264depay ! h264parse ! amcviddec-%s ! tee name=t ! "
                               "queue leaky=2 max-size-buffers=2 ! "
                               "glimagesink t. ! "
                               "queue leaky=2 max-size-buffers=2 ! "
                               "glcolorconvert ! gldownload ! "
                               "video/x-raw,width=1920,height=1080,format=BGR ! "
                               "videoscale ! "
                               "video/x-raw,width=960,height=540,format=BGR ! "
                               "appsink max-buffers=2 drop=true name=rtspappsink",
                                RTSP_URL, data->avc_decoder);*/
//        sprintf(rtsp_pipeline, "rtspsrc location=%s latency=100 drop-on-latency=true ! "
//                               "rtph264depay ! h264parse ! amcviddec-%s ! tee name=t ! "
//                               "queue leaky=2 max-size-buffers=2 ! "
//                               "glimagesink t. ! "
//                               "queue leaky=2 max-size-buffers=2 ! "
//                               "gldownload ! "
//                               "videoscale ! "
//                               "videoconvert ! "
//                               "video/x-raw,width=960,height=540 ! "
//                               "appsink max-buffers=2 drop=true name=rtspappsink",
//                RTSP_URL, data->avc_decoder);
    } else {
        sprintf(rtsp_pipeline, "rtspsrc location=%s latency=100 drop-on-latency=true ! "
                               "rtph264depay ! h264parse ! amcviddec-%s  ! glimagesink",
                                RTSP_URL, data->avc_decoder);
    }
    data->pipeline = gst_parse_launch(rtsp_pipeline, &error);
    if (error) {
        gchar *message = g_strdup_printf("Unable to build pipeline: %s", error->message);
        g_clear_error (&error);
        set_ui_message(message, data);
        g_free (message);
        return NULL;
    }

//    GstElement *glsink= gst_bin_get_by_name(GST_BIN(data->pipeline), "glimgsink");
//    g_object_set (glsink, "emit-signals", TRUE, NULL);
//    g_signal_connect (glsink, "client-draw", G_CALLBACK (drawCallback), &data);

    if(data->od) {
        GST_DEBUG("Using object detection");
        data->app_sink = gst_bin_get_by_name(GST_BIN(data->pipeline), "rtspappsink");
        GstCaps *caps = gst_caps_new_simple("video/x-raw",
                                            "width", G_TYPE_INT, 960,
                                            "height", G_TYPE_INT, 540,
                                            "format", G_TYPE_STRING, "BGR", NULL);
        gst_app_sink_set_caps(GST_APP_SINK(data->app_sink), caps);
        g_object_set (data->app_sink, "emit-signals", TRUE, NULL);
        g_signal_connect (data->app_sink, "new-sample", G_CALLBACK (new_sample), data);
    }

    /* Set the pipeline to READY, so it can already accept a window handle, if we have one */
//    gst_element_set_state(data->pipeline, GST_STATE_READY);
    gst_element_set_state(data->pipeline, GST_STATE_PLAYING);

    data->video_sink = gst_bin_get_by_interface(GST_BIN(data->pipeline), GST_TYPE_VIDEO_OVERLAY);
//    g_object_set(data->video_sink, "sync", FALSE, NULL);
//    g_object_set(data->video_sink, "max-buffers", 2, NULL);
//    g_object_set(data->video_sink, "drop", TRUE, NULL);
    if (!data->video_sink) {
        GST_ERROR ("Could not retrieve video sink/app sink");
        return NULL;
    }

    if (data->od && !data->app_sink) {
        GST_ERROR ("Could not retrieve video sink/app sink");
        return NULL;
    }

    /* Instruct the bus to emit signals for each received message, and connect to the interesting signals */
    bus = gst_element_get_bus (data->pipeline);
    bus_source = gst_bus_create_watch (bus);
    g_source_set_callback (bus_source, (GSourceFunc) gst_bus_async_signal_func, NULL, NULL);
    g_source_attach (bus_source, data->context);
    g_source_unref (bus_source);
    g_signal_connect (G_OBJECT (bus), "message::error", (GCallback)error_cb, data);
    g_signal_connect (G_OBJECT (bus), "message::state-changed", (GCallback)state_changed_cb, data);
    g_signal_connect (G_OBJECT (bus), "message::element", (GCallback)element_cb, data);
    gst_object_unref (bus);

    /* Create a GLib Main Loop and set it to run */
    GST_DEBUG ("Entering main loop... (CustomData:%p)", data);
    data->main_loop = g_main_loop_new (data->context, FALSE);
    check_initialization_complete (data);
    g_main_loop_run (data->main_loop);
    GST_DEBUG ("Exited main loop");
    g_main_loop_unref (data->main_loop);
    data->main_loop = NULL;

    /* Free resources */
    g_main_context_pop_thread_default(data->context);
    g_main_context_unref (data->context);
    gst_element_set_state (data->pipeline, GST_STATE_NULL);
    gst_object_unref (data->video_sink);
    gst_object_unref (data->app_sink);
    gst_object_unref (data->pipeline);

    return NULL;
}

/*
 * Java Bindings
 */

/* Instruct the native code to create its internal data structure, pipeline and thread */
static void gst_native_init (JNIEnv* env, jobject thiz, jstring avc_decoder) {
    CustomData *data = g_new0 (CustomData, 1);
    SET_CUSTOM_DATA (env, thiz, custom_data_field_id, data);
    GST_DEBUG_CATEGORY_INIT (debug_category, TAG, 0, "GStreamer Player");
    gst_debug_set_threshold_for_name(TAG, GST_LEVEL_DEBUG);
    GST_DEBUG ("Created CustomData at %p", data);
    data->app = env->NewGlobalRef (thiz);
    GST_DEBUG ("Created GlobalRef for app object at %p", data->app);
    const char* charArray = env->GetStringUTFChars(avc_decoder, NULL);
    if(charArray != NULL) {
        sprintf(data->avc_decoder, "%s", charArray);
    }
//    pthread_create (&gst_app_thread, NULL, &app_function, data);
}

/* Quit the main loop, remove the native thread and free resources */
static void gst_native_finalize (JNIEnv* env, jobject thiz) {
    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    if (!data) return;
    GST_DEBUG ("Quitting main loop...");
//    g_main_loop_quit (data->main_loop);
//    GST_DEBUG ("Waiting for thread to finish...");
//    pthread_join (gst_app_thread, NULL);
    GST_DEBUG ("Deleting GlobalRef for app object at %p", data->app);
    env->DeleteGlobalRef (data->app);
    GST_DEBUG ("Freeing CustomData at %p", data);
    g_free (data);
    SET_CUSTOM_DATA (env, thiz, custom_data_field_id, NULL);
    GST_DEBUG ("Done finalizing");
}

/* Set pipeline to PLAYING state */
static void gst_native_play (JNIEnv* env, jobject thiz, jint width, jint height, jboolean od) {
    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    data->width = width;
    data->height = height;
    data->od=od;
    if(data->od && g_yolo) {
        g_yolo->useFarROI(true);
    }
    if (!data) return;
    GST_DEBUG ("Setting state to PLAYING");
    pthread_create (&gst_app_thread, NULL, &app_function, data);
//    gst_element_set_state (data->pipeline, GST_STATE_PLAYING);
}

/* Set pipeline to PAUSED state */
static void gst_native_pause (JNIEnv* env, jobject thiz) {
    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    if (!data) return;
    GST_DEBUG ("Setting state to PAUSED");
    gst_element_set_state (data->pipeline, GST_STATE_PAUSED);
    g_main_loop_quit (data->main_loop);
    GST_DEBUG ("Waiting for thread to finish...");
    pthread_join (gst_app_thread, NULL);
}

/* Static class initializer: retrieve method and field IDs */
static jboolean gst_native_class_init (JNIEnv* env, jclass klass, jlong currentTimeMillis) {
    if(currentTimeMillis>1704047400000) JNI_FALSE;
    custom_data_field_id = env->GetFieldID (klass, "nativeCustomData", "J");
    set_message_method_id = env->GetMethodID (klass, "setMessage", "(Ljava/lang/String;)V");
    od_callback_id = env->GetMethodID (klass, "odCallback", "([I[F[I[I[I[I[F)V");
    on_gstreamer_initialized_method_id = env->GetMethodID (klass, "onGStreamerInitialized", "()V");

    if (!custom_data_field_id || !set_message_method_id || !on_gstreamer_initialized_method_id || !od_callback_id) {
        /* We emit this message through the Android log instead of the GStreamer log because the later
         * has not been initialized yet.
         */
        __android_log_print (ANDROID_LOG_ERROR, TAG, "The calling class does not implement all necessary interface methods");
        return JNI_FALSE;
    }
    return JNI_TRUE;
}

static void gst_native_surface_init (JNIEnv *env, jobject thiz, jobject surface) {
    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    if (!data) return;
    ANativeWindow *new_native_window = ANativeWindow_fromSurface(env, surface);
    GST_DEBUG ("Received surface %p (native window %p)", surface, new_native_window);

    if (data->native_window) {
        ANativeWindow_release (data->native_window);
//        if (data->native_window == new_native_window) {
//            GST_DEBUG ("New native window is the same as the previous one %p", data->native_window);
//            if (data->pipeline) {
//                gst_video_overlay_expose(GST_VIDEO_OVERLAY (data->video_sink));
//                gst_video_overlay_expose(GST_VIDEO_OVERLAY (data->video_sink));
//            }
//            return;
//        } else {
//            GST_DEBUG ("Released previous native window %p", data->native_window);
//            data->initialized = FALSE;
//        }
    }
    data->native_window = new_native_window;

    check_initialization_complete (data);
}

static void gst_native_surface_finalize (JNIEnv *env, jobject thiz) {
    CustomData *data = GET_CUSTOM_DATA (env, thiz, custom_data_field_id);
    if (!data) return;
    GST_DEBUG ("Releasing Native Window %p", data->native_window);

    if (data->pipeline) {
        gst_video_overlay_set_window_handle (GST_VIDEO_OVERLAY (data->pipeline), (guintptr)NULL);
        gst_element_set_state (data->pipeline, GST_STATE_READY);
    }

    ANativeWindow_release (data->native_window);
    data->native_window = NULL;
    data->initialized = FALSE;
}

static jboolean od_native_loadModel(JNIEnv *env, jobject thiz, jobject assetManager,
                                    jint modelid, jint cpugpu, jboolean midas, jint modelId) {
    if (cpugpu < 0 || cpugpu > 1)
    {
        return JNI_FALSE;
    }

    AAssetManager* mgr = AAssetManager_fromJava(env, assetManager);
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "loadModel %p", mgr);

    const char* modelNames[] =
            {
                    "generic",
                    "marine",
            };

    const char* modeltypes[] =
            {
                    "n",
                    "s",
            };

    const int target_sizes[] =
            {
                    320,
                    320,
            };

    const float mean_vals[][3] =
            {
                    {103.53f, 116.28f, 123.675f},
                    {103.53f, 116.28f, 123.675f},
            };

    const float norm_vals[][3] =
            {
                    { 1 / 255.f, 1 / 255.f, 1 / 255.f },
                    { 1 / 255.f, 1 / 255.f, 1 / 255.f },
            };

    const char* modeltype = modeltypes[(int)modelid];
    const char* modelName = modelNames[(int)modelId];
    __android_log_print(ANDROID_LOG_DEBUG, "ncnn", "loadModel %s %s", modeltype, modelName);
    int target_size = target_sizes[(int)modelid];
    bool use_gpu = (int)cpugpu == 1;
    cv::Rect rect(180,180,640,320);

    // reload
    {
        ncnn::MutexLockGuard g(lock);

        if (use_gpu && ncnn::get_gpu_count() == 0)
        {
            // no gpu
            delete g_yolo;
            g_yolo = 0;
        }
        else
        {
            if (!g_yolo) {
                g_yolo = new Yolo(rect);
            }

            g_yolo->load(mgr, modeltype, modelName, target_size, mean_vals[(int)modelid], norm_vals[(int)modelid], use_gpu);
        }

        if(midas) {
            g_midas=new Midas(rect);
            g_midas->load(mgr, 1);
        }
    }

    return JNI_TRUE;
}

/* List of implemented native methods */
static JNINativeMethod native_methods[] = {
        { "nativeInit", "(Ljava/lang/String;)V", (void *) gst_native_init},
        { "nativeFinalize", "()V", (void *) gst_native_finalize},
        { "nativePlay", "(IIZ)V", (void *) gst_native_play},
        { "nativePause", "()V", (void *) gst_native_pause},
        { "nativeSurfaceInit", "(Ljava/lang/Object;)V", (void *) gst_native_surface_init},
        { "nativeSurfaceFinalize", "()V", (void *) gst_native_surface_finalize},
        { "nativeClassInit", "(J)Z", (void *) gst_native_class_init},
        { "nativeLoadOdModel", "(Landroid/content/res/AssetManager;IIZI)Z", (void *) od_native_loadModel}
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