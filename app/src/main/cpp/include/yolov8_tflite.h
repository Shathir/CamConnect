//#pragma once
//#include <vector>
//#include <string>
//#include <memory>
//#include <opencv2/opencv.hpp>
//#include <pthread.h>
//#include <atomic>
//#include "tensorflow/lite/interpreter.h"
//#include "tensorflow/lite/kernels/register.h"
//#include "tensorflow/lite/model.h"
//#include <android/asset_manager_jni.h>
//
//struct Object {
//    cv::Rect2f rect;
//    int label;
//    float prob;
//};
//
//class YoloV8TFLite {
//public:
//
//
//    YoloV8TFLite(const std::string& model_path, const std::string& device = "cpu", float conf_thresh = 0.25f, float nms_thresh = 0.45f);
//    ~YoloV8TFLite();
//
//    // Synchronous API
//    int load(AAssetManager* mgr);
//    void detect(const cv::Mat& rgb, std::vector<Object>& detections);
//
//    // Asynchronous API (only inference is async, preprocess/postprocess are sync)
//    int submitRequest(const cv::Mat& rgb);  // Preprocess + submit for async inference
//    int fetchResults(std::vector<Object>& detections);  // Wait for inference + postprocess
//    bool isInferenceReady();  // Check if inference is complete (non-blocking)
//
//private:
//    void preProcessImage(const cv::Mat& rgb);
//    void postProcess(std::vector<Object>& detections);
//    void decodeDetections(const float* output, std::vector<Object>& detections);
//    std::vector<Object> applyNMS(const std::vector<Object>& detections);
//
//    // Async worker thread
//    static void* inferenceWorker(void* arg);
//    void inferenceLoop();
//
//    // TFLite
//    std::vector<std::string> class_names;
//    std::string _model_path;
//    std::string _device;
//    std::unique_ptr<tflite::FlatBufferModel> _model;
//    std::unique_ptr<tflite::ops::builtin::BuiltinOpResolver> _resolver;
//    std::unique_ptr<tflite::Interpreter> _interpreter;
//    TfLiteTensor* _input_tensor;
//    TfLiteTensor* _output_tensor;
//    void* _model_buf;
//    TfLiteDelegate* _gpu_delegate;
//
//    // Params
//    float _conf_thresh;
//    float _nms_thresh;
//
//    // For padding/scale info
//    int _last_pad_x, _last_pad_y;
//    float _last_scale;
//    int _last_img_w, _last_img_h;
//
//    // Async state
//    enum State {
//        IDLE = 0,          // Ready for new request
//        PREPROCESSING = 1, // Preprocessing in progress (not used, kept for compatibility)
//        INFERENCE = 2,     // Inference running in worker thread
//        READY = 3         // Inference complete, ready to fetch
//    };
//    std::atomic<State> _state;
//    pthread_t _worker_thread;
//    pthread_mutex_t _mutex;
//    pthread_cond_t _cond;
//    bool _quit;
//    bool _worker_started;
//
//    // Helper to get input size from tensor
//    int getInputSize() const;
//};