//#include "yolov8_tflite.h"
//#include <opencv2/imgproc.hpp>
//#include <cassert>
//#include <cstring>
//#include <algorithm>
//#include <fstream>
//#include <iostream>
//#include <chrono>
//#include <android/log.h>
//#ifdef __ANDROID__
//#include "tensorflow/lite/delegates/gpu/delegate.h"
//#endif
//#define TENSORTYPE  kTfLiteFloat32
//
//YoloV8TFLite::YoloV8TFLite(const std::string& model_path, const std::string& device, float conf_thresh,
//                           float nms_thresh)
//    : _model_path(model_path), _device(device), _model_buf(nullptr),
//      _input_tensor(nullptr), _output_tensor(nullptr), _gpu_delegate(nullptr),
//      _conf_thresh(conf_thresh), _nms_thresh(nms_thresh),
//      _state(IDLE), _quit(false), _worker_started(false)
//{
//    // in model_path replace extension with .labels and set as labelsFilePath
//    //spdlog::info("model_path: {}, device: {}, conf_thresh: {}, nms_thresh: {}", model_path, device, conf_thresh, nms_thresh);
//    size_t startPos = model_path.find_last_of('.');
//    std::string labelsFilePath = model_path.substr(0, startPos) + ".labels";
//    // read labels from labelsFilePath and set as class_names
//    std::ifstream inputFile(labelsFilePath);
//    std::string line;
//    //spdlog::info("classes:");
//    while (std::getline(inputFile, line))
//    {
//        //spdlog::info(line);
//        class_names.push_back(line);
//    }
//
//    // Initialize pthread primitives
//    pthread_mutex_init(&_mutex, nullptr);
//    pthread_cond_init(&_cond, nullptr);
//}
//
//YoloV8TFLite::~YoloV8TFLite()
//{
//    // Stop worker thread if running
//    if (_worker_started) {
//        pthread_mutex_lock(&_mutex);
//        _quit = true;
//        pthread_cond_signal(&_cond);
//        pthread_mutex_unlock(&_mutex);
//        pthread_join(_worker_thread, nullptr);
//    }
//
//    if (_model_buf) delete[] reinterpret_cast<char*>(_model_buf);
//    _model.reset();
//    _resolver.reset();
//    _interpreter.reset();
//
//    pthread_mutex_destroy(&_mutex);
//    pthread_cond_destroy(&_cond);
//}
//
//int YoloV8TFLite::load(AAssetManager* mgr)
//{
////    FILE* fp = fopen(_model_path.c_str(), "rb");
////    if (!fp) return 1;
////    fseek(fp, 0, SEEK_END);
////    size_t length = ftell(fp);
////    fseek(fp, 0, SEEK_SET);
////    _model_buf = new char[length];
////    fread(_model_buf, 1, length, fp);
////    fclose(fp);
////
////    _model = tflite::FlatBufferModel::BuildFromBuffer(reinterpret_cast<const char*>(_model_buf), length);
//
//
//    AAsset* asset = AAssetManager_open(mgr, _model_path.c_str(), AASSET_MODE_BUFFER);
//    const void* buffer = AAsset_getBuffer(asset);
//    off_t length = AAsset_getLength(asset);
//    _model_buf = new char[length];
//    memcpy(_model_buf, buffer, length);
//    _model =
//            tflite::FlatBufferModel::BuildFromBuffer(
//                    reinterpret_cast<const char*>(_model_buf), length);
//    AAsset_close(asset);
//
//    _resolver.reset(new tflite::ops::builtin::BuiltinOpResolver());
//
//    tflite::InterpreterBuilder builder(*_model, *_resolver);
//    builder(&_interpreter);
//    if (!_interpreter) return 1;
//
//    // Configure based on device
//    if (_device == "gpu") {
//#ifdef __ANDROID__
////        auto options = TfLiteGpuDelegateOptionsV2Default();
////        options.inference_preference = TFLITE_GPU_INFERENCE_PREFERENCE_SUSTAINED_SPEED;
////        options.inference_priority1 = TFLITE_GPU_INFERENCE_PRIORITY_MIN_LATENCY;
////        _gpu_delegate = TfLiteGpuDelegateV2Create(&options);
////        req.interpreter->ModifyGraphWithDelegate(_gpu_delegate);
//
//        // Create GPU delegate for Android
////        TfLiteGpuDelegateOptionsV2 options = TfLiteGpuDelegateOptionsV2Default();
////        options.inference_preference = TFLITE_GPU_INFERENCE_PREFERENCE_SUSTAINED_SPEED;
////        options.inference_priority1 = TFLITE_GPU_INFERENCE_PRIORITY_MIN_LATENCY;
////        options.inference_preference = TFLITE_GPU_INFERENCE_PREFERENCE_FAST_SINGLE_ANSWER;
////        options.inference_priority1 = TFLITE_GPU_INFERENCE_PRIORITY_MIN_LATENCY;
////        options.inference_priority2 = TFLITE_GPU_INFERENCE_PRIORITY_AUTO;
////        options.inference_priority3 = TFLITE_GPU_INFERENCE_PRIORITY_AUTO;
//        TfLiteGpuDelegateOptionsV2 gpu_opts = TfLiteGpuDelegateOptionsV2Default();
//        gpu_opts.is_precision_loss_allowed = 1;  // FP16 precision
//        gpu_opts.inference_preference = TFLITE_GPU_INFERENCE_PREFERENCE_FAST_SINGLE_ANSWER;
//        gpu_opts.inference_priority1 = TFLITE_GPU_INFERENCE_PRIORITY_MIN_LATENCY;
//        gpu_opts.inference_priority2 = TFLITE_GPU_INFERENCE_PRIORITY_AUTO;
//        gpu_opts.inference_priority3 = TFLITE_GPU_INFERENCE_PRIORITY_AUTO;
////        gpu_opts.experimental_flags |= TFLITE_GPU_EXPERIMENTAL_FLAGS_ENABLE_LOGGING;
//
//        _gpu_delegate = TfLiteGpuDelegateV2Create(&gpu_opts);
////        _gpu_delegate = TfLiteGpuDelegateV2Create(nullptr);
//        if (_gpu_delegate) {
//            if (_interpreter->ModifyGraphWithDelegate(_gpu_delegate) != kTfLiteOk) {
////                spdlog::error("Failed to apply GPU delegate, falling back to CPU");
//                __android_log_print (ANDROID_LOG_ERROR, "YOLO",
//                                     "Failed to apply GPU delegate, falling back to CPU");
//                TfLiteGpuDelegateV2Delete(_gpu_delegate);
//                _gpu_delegate = nullptr;
//                _interpreter->SetNumThreads(4);
//            } else {
//                __android_log_print (ANDROID_LOG_ERROR, "YOLO", "GPU delegate enabled successfully");
//            }
//        } else {
//            //spdlog::error("Failed to create GPU delegate, falling back to CPU");
//            _interpreter->SetNumThreads(4);
//        }
//#else
//        //spdlog::warn("GPU delegate requested but not supported on this platform, using CPU");
//        _interpreter->SetNumThreads(4);
//#endif
//    } else {
//        // CPU mode
//        _interpreter->SetNumThreads(4);
//        //spdlog::info("Using CPU with 4 threads");
//    }
//
//    if (_interpreter->AllocateTensors() != kTfLiteOk) return 1;
//
//    _input_tensor = _interpreter->tensor(_interpreter->inputs()[0]);
//    _output_tensor = _interpreter->tensor(_interpreter->outputs()[0]);
//
//    // Initialize input tensor with zeros
////    float* dst = _input_tensor->data.f;
////    int img_channel = 3;
////    int input_size = getInputSize();
////    std::fill(dst, dst + input_size * input_size * img_channel, 0.0f);
//
//    // Start worker thread for async inference
//    _state = IDLE;
//    if (pthread_create(&_worker_thread, nullptr, &YoloV8TFLite::inferenceWorker, this) == 0) {
//        _worker_started = true;
//        //spdlog::info("Inference worker thread started");
//    } else {
//        //spdlog::error("Failed to start inference worker thread");
//        return 1;
//    }
//
//    return 0;
//}
//
//// Helper to get input size from tensor (assume square, NHWC)
//int YoloV8TFLite::getInputSize() const
//{
//    if (_input_tensor && _input_tensor->dims->size >= 3)
//    {
//        int h = _input_tensor->dims->data[1];
//        int w = _input_tensor->dims->data[2];
//        return std::min(h, w); // assume square
//    }
//    return 640; // fallback
//}
//
//void YoloV8TFLite::detect(const cv::Mat& rgb, std::vector<Object>& detections)
//{
//    detections.clear();
//
//    // Preprocess
//    preProcessImage(rgb);
//
//    // Inference
//    if (_interpreter->Invoke() != kTfLiteOk)
//    {
//        //spdlog::error("Failed to invoke TFLite interpreter");
//        return;
//    }
//
//    // Postprocess
//    postProcess(detections);
//}
//
//void YoloV8TFLite::preProcessImage(const cv::Mat& rgb)
//{
//    int w = rgb.cols, h = rgb.rows;
//    int input_size = getInputSize();
//
////    float scale = std::min(input_size / (float)w, input_size / (float)h);
////    int new_w = static_cast<int>(w * scale);
////    int new_h = static_cast<int>(h * scale);
////    int pad_x = (input_size - new_w) / 2;
////    int pad_y = (input_size - new_h) / 2;
////
////    cv::Mat resized;
////    cv::resize(rgb, resized, cv::Size(new_w, new_h));
//
//    // Clear input tensor with zeros (black padding)
////    float* dst = _input_tensor->data.f;
//    float* dst = _interpreter->typed_input_tensor<float>(0);
//    if (!dst) {
//        __android_log_print(ANDROID_LOG_ERROR, "YOLO", "Invalid input tensor pointer");
//        return;
//    }
//    int img_channel = 3;
////    std::fill(dst, dst + input_size * input_size * img_channel, 0.0f);
//
//    // Copy resized image to input tensor with padding
//    for (int y = 0; y < 640; ++y)
//    {
//        for (int x = 0; x < 640; ++x)
//        {
//            int dst_x = x ;
//            int dst_y = y ;
//            int dst_idx = (dst_y * input_size + dst_x) * img_channel;
//            const cv::Vec3b& pixel = rgb.at<cv::Vec3b>(y, x);
//            dst[dst_idx + 0] = pixel[0] / 255.0f; // R
//            dst[dst_idx + 1] = pixel[1] / 255.0f; // G
//            dst[dst_idx + 2] = pixel[2] / 255.0f; // B
//        }
//    }
//
//    _last_pad_x = 0;
//    _last_pad_y = 0;
//    _last_scale = 1;
//    _last_img_w = w;
//    _last_img_h = h;
//}
//
//void YoloV8TFLite::postProcess(std::vector<Object>& detections)
//{
//    const float* output = _output_tensor->data.f;
//    std::vector<Object> raw_detections;
//    decodeDetections(output, raw_detections);
//
//    // Apply NMS
//    auto nms_dets = applyNMS(raw_detections);
//
//    // Map coordinates back to original image space and normalize
//    detections.clear();
//    for (const auto& det : nms_dets)
//    {
//        Object mapped_det = det;
//        mapped_det.rect.x = (mapped_det.rect.x - _last_pad_x) / _last_scale;
//        mapped_det.rect.y = (mapped_det.rect.y - _last_pad_y) / _last_scale;
//        mapped_det.rect.width = mapped_det.rect.width / _last_scale;
//        mapped_det.rect.height = mapped_det.rect.height / _last_scale;
////        mapped_det.rect.x /= _last_img_w;
////        mapped_det.rect.y /= _last_img_h;
////        mapped_det.rect.width /= _last_img_w;
////        mapped_det.rect.height /= _last_img_h;
//        detections.push_back(mapped_det);
//    }
//}
//
//void YoloV8TFLite::decodeDetections(const float* output, std::vector<Object>& detections)
//{
//    int num_dims = _output_tensor->dims->size;
//    int num_boxes = 0;
//    int num_classes = 0;
//    int input_size = getInputSize();
//
//    if (num_dims == 3 && _output_tensor->dims->data[1] == 84)
//    {
//        num_boxes = _output_tensor->dims->data[2]; // 8400
//        num_classes = _output_tensor->dims->data[1] - 4; // 80
//        detections.clear();
//        for (int i = 0; i < num_boxes; ++i)
//        {
//            float x = output[0 * num_boxes + i];
//            float y = output[1 * num_boxes + i];
//            float w = output[2 * num_boxes + i];
//            float h = output[3 * num_boxes + i];
//            float max_score = -1.0f;
//            int class_id = -1;
//            for (int c = 0; c < num_classes; ++c)
//            {
//                float score = output[(4 + c) * num_boxes + i];
//                if (score > max_score)
//                {
//                    max_score = score;
//                    class_id = c;
//                }
//            }
//            if (max_score < _conf_thresh) continue;
//            float left = (x - w / 2.0f) * input_size;
//            float top = (y - h / 2.0f) * input_size;
//            float width = w * input_size;
//            float height = h * input_size;
//            Object det;
//            det.rect = cv::Rect2f(left, top, width, height);
//            det.prob = max_score;
//            det.label = class_id;
//            detections.push_back(det);
//        }
//        return;
//    }
//    detections.clear();
//}
//
//std::vector<Object> YoloV8TFLite::applyNMS(const std::vector<Object>& detections)
//{
//    std::vector<cv::Rect> boxes;
//    std::vector<float> scores;
//    for (const auto& det : detections)
//    {
//        boxes.push_back(det.rect);
//        scores.push_back(det.prob);
//    }
//    std::vector<int> indices;
//    cv::dnn::NMSBoxes(boxes, scores, _conf_thresh, _nms_thresh, indices);
//
//    std::vector<Object> result;
//    for (int idx : indices)
//    {
//        result.push_back(detections[idx]);
//    }
//    return result;
//}
//
//// ==================== Async API ====================
//
//// Worker thread function
//void* YoloV8TFLite::inferenceWorker(void* arg)
//{
//    YoloV8TFLite* yolo = static_cast<YoloV8TFLite*>(arg);
//    yolo->inferenceLoop();
//    return nullptr;
//}
//
//// Worker thread loop
//void YoloV8TFLite::inferenceLoop()
//{
//    //spdlog::debug("Inference worker loop started");
//
//    while (true) {
//        pthread_mutex_lock(&_mutex);
//
//        // Wait for work or quit signal
//        while (_state != INFERENCE && !_quit) {
//            pthread_cond_wait(&_cond, &_mutex);
//        }
//
//        if (_quit) {
//            pthread_mutex_unlock(&_mutex);
//            break;
//        }
//
//        // We have work to do - state is INFERENCE
//        pthread_mutex_unlock(&_mutex);
//
//        // Run inference (this is the only async part)
//        //spdlog::debug("Worker: Running inference");
//        auto t0 = std::chrono::high_resolution_clock::now();
//        TfLiteStatus status = _interpreter->Invoke();
//        auto t1 = std::chrono::high_resolution_clock::now();
//        double ms = std::chrono::duration_cast<std::chrono::microseconds>(t1 - t0).count() / 1000.0;
//
//        if (status != kTfLiteOk) {
//            //spdlog::error("Worker: Inference failed");
//        } else {
//            //spdlog::debug("Worker: Inference completed in {:.2f}ms", ms);
//        }
//
//        // Update state to READY
//        pthread_mutex_lock(&_mutex);
//        _state = READY;
//        pthread_cond_signal(&_cond);  // Signal that inference is done
//        pthread_mutex_unlock(&_mutex);
//    }
//
//    //spdlog::debug("Inference worker loop exited");
//}
//
//// Submit request for async inference
//int YoloV8TFLite::submitRequest(const cv::Mat& rgb)
//{
//    pthread_mutex_lock(&_mutex);
//
//    // Check if we can accept a new request
//    if (_state != IDLE) {
//        pthread_mutex_unlock(&_mutex);
//        //spdlog::warn("submitRequest: System busy (state={}), cannot submit new request", (int)_state.load());
//        return 1;  // Busy
//    }
//
//    pthread_mutex_unlock(&_mutex);
//
//    // Preprocess (synchronous, in caller thread)
//    //spdlog::debug("submitRequest: Preprocessing image");
//    preProcessImage(rgb);
//
//    // Signal worker thread to run inference
//    pthread_mutex_lock(&_mutex);
//    _state = INFERENCE;
//    pthread_cond_signal(&_cond);
//    pthread_mutex_unlock(&_mutex);
//
//    //spdlog::debug("submitRequest: Request submitted for async inference");
//    return 0;
//}
//
//// Check if inference is ready (non-blocking)
//bool YoloV8TFLite::isInferenceReady()
//{
//    return _state == READY;
//}
//
//// Fetch results (blocks if inference not ready, then postprocesses)
//int YoloV8TFLite::fetchResults(std::vector<Object>& detections)
//{
//    pthread_mutex_lock(&_mutex);
//
//    // Check if there's no pending request (first time call without submitRequest)
//    if (_state == IDLE) {
//        pthread_mutex_unlock(&_mutex);
//        //spdlog::debug("fetchResults: No pending request, returning empty");
//        detections.clear();
//        return 1;  // No request to fetch
//    }
//
//    // Wait for inference to complete
//    while (_state != READY && !_quit) {
//        //spdlog::debug("fetchResults: Waiting for inference to complete (state={})", (int)_state.load());
//        pthread_cond_wait(&_cond, &_mutex);
//    }
//
//    if (_quit) {
//        pthread_mutex_unlock(&_mutex);
//        return 1;
//    }
//
//    // Inference is ready, proceed to postprocess
//    pthread_mutex_unlock(&_mutex);
//
//    // Postprocess (synchronous, in caller thread)
//    //spdlog::debug("fetchResults: Postprocessing results");
//    postProcess(detections);
//
//    // Reset state to IDLE
//    pthread_mutex_lock(&_mutex);
//    _state = IDLE;
//    pthread_mutex_unlock(&_mutex);
//
//    //spdlog::debug("fetchResults: Fetched {} detections", detections.size());
//    return 0;
//}
