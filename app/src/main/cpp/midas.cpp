//
// Created by outdu-gram on 26/09/23.
//

#include "midas.h"
#include "tensorflow/lite/delegates/gpu/delegate.h"

#define INPUT_NAME  "Const"
#define INPUT_DIMS  { 1, 256, 256, 3 }
#define INPUT_W_H   256
#define IS_NCHW     false
#define IS_RGB      true
#define OUTPUT_NAME "midas_net_custom/sequential/re_lu_9/Relu"
#define TENSORTYPE  kTfLiteFloat32

static void *midas_app_function (void *userdata) {
    Midas *midas= static_cast<Midas *>(userdata);
    while (true) {
        pthread_mutex_lock(&midas->_mutex);
        if(midas->_quit) {
            pthread_mutex_unlock(&midas->_mutex);
            break;
        }
        midas->invokeProcess();
        pthread_cond_wait(&midas->_cond, &midas->_mutex);
        pthread_mutex_unlock(&midas->_mutex);
    }
}

Midas::Midas()
{
    _quit=false;
    pthread_mutex_init(&_mutex, nullptr);
    pthread_cond_init(&_cond, nullptr);
}

Midas::~Midas()
{
    pthread_mutex_destroy(&_mutex);
    pthread_cond_destroy(&_cond);
    pthread_join (_thread, NULL);
    _model.reset();
    delete model_buf;
    _resolver.reset();
    _interpreter.reset();
    TfLiteGpuDelegateV2Delete(_delegate);
}

int Midas::load(AAssetManager* mgr, bool use_gpu)
{
    // Open the asset.
//    AAsset* asset = AAssetManager_open(mgr, "mds_model_opt.tflite", AASSET_MODE_BUFFER);
    AAsset* asset = AAssetManager_open(mgr, "lite-model_midas_v2_1_small_1_lite_1.tflite", AASSET_MODE_BUFFER);

    // Read the file into memory.
    const void* buffer = AAsset_getBuffer(asset);
    off_t length = AAsset_getLength(asset);
    // Create a new FlatBufferModel from the memory buffer.
    model_buf = new char[length];
    memcpy(model_buf, buffer, length);
    _model =
            tflite::FlatBufferModel::BuildFromBuffer(
                    reinterpret_cast<const char*>(model_buf), length);
    AAsset_close(asset);

//    AAsset* asset1 = AAssetManager_open(mgr, "dog.jpg", AASSET_MODE_BUFFER);
//    const void* buffer1 = AAsset_getBuffer(asset1);


//    tflite::ops::builtin::BuiltinOpResolver *resolver=new tflite::ops::builtin::BuiltinOpResolver;
    _resolver.reset(new tflite::ops::builtin::BuiltinOpResolver());
    tflite::InterpreterBuilder builder(*_model, *_resolver);
    builder(&_interpreter);

//    auto resolver = std::unique_ptr<tflite::ops::builtin::BuiltinOpResolver>(
//            new tflite::ops::builtin::BuiltinOpResolver());
//    auto builder = std::unique_ptr<tflite::InterpreterBuilder>(
//            new tflite::InterpreterBuilder(*_model, *resolver));
//    builder->operator()(&_interpreter);

    assert(_interpreter != nullptr);

    _interpreter->SetNumThreads(1);
//    _interpreter->SetAllowFp16PrecisionForFp32(true);

    if(use_gpu) {
        auto options = TfLiteGpuDelegateOptionsV2Default();
        options.inference_preference = TFLITE_GPU_INFERENCE_PREFERENCE_SUSTAINED_SPEED;
        options.inference_priority1 = TFLITE_GPU_INFERENCE_PRIORITY_MIN_LATENCY;
        _delegate = TfLiteGpuDelegateV2Create(&options);
        _interpreter->ModifyGraphWithDelegate(_delegate);
    }


//    _interpreter->AllocateTensors();
    if (_interpreter->AllocateTensors() != kTfLiteOk) {
        return 1;
    }

    std::vector<int> inputs=_interpreter->inputs();
    std::vector<int> outputs=_interpreter->outputs();
    // Get input and output tensors.
    _input_tensor = _interpreter->tensor(_interpreter->inputs()[0]);
    _output_tensor = _interpreter->tensor(_interpreter->outputs()[0]);

    assert(_input_tensor->type == TENSORTYPE);

//    for(int i=0;i<_input_tensor->dims->size;i++) {
//        int t=_input_tensor->dims->data[i];
//    }
//
//    for(int i=0;i<_output_tensor->dims->size;i++) {
//        int t=_output_tensor->dims->data[i];
//    }



    _mean_vals[0] = 0.485f;
    _mean_vals[1] = 0.456f;
    _mean_vals[2] = 0.406f;
    _norm_vals[0] = 0.229f;
    _norm_vals[1] = 0.224f;
    _norm_vals[2] = 0.225f;

    //for faster normalization
    for (int32_t i = 0; i < 3; i++) {
        _mean_vals[i] *= 255.0f;
        _norm_vals[i] *= 255.0f;
        _norm_vals[i] = 1.0f / _norm_vals[i];
    }

    pthread_create (&_thread, NULL, &midas_app_function, this);
    return 0;
}

//int Midas::postProcess(const std::vector<cv::Point2f> &points, std::vector<float> &dep_thres)
//{
//
////    int width = rgb.cols;
////    int height = rgb.rows;
////    int target_size = INPUT_W_H;
//
////    // pad to multiple of 32
////    int w = width;
////    int h = height;
////    float scale = 1.f;
////    if (w > h)
////    {
////        scale = (float)target_size / w;
////        w = target_size;
////        h = h * scale;
////    }
////    else
////    {
////        scale = (float)target_size / h;
////        h = target_size;
////        w = w * scale;
////    }
//
////    cv::Mat in_pad(target_size, target_size, rgb.type());
////    in.copyTo(in_pad(cv::Rect(0, 0, in.cols, in.rows)));
//
////    if (in_pad.cols != INPUT_W_H || in_pad.rows != INPUT_W_H) {
////        return 1;
////    }
//
////    preProcessImage(3, in_pad);
////    preProcessImage(3, in);
//
//    // Inference
//
//    auto *outputdata=tflite::GetTensorData<float>(_output_tensor);
//    cv::Mat mat_out(INPUT_W_H, INPUT_W_H, CV_32FC1, outputdata);  /* value has no specific range */
//    double depth_min, depth_max;
//    cv::minMaxLoc(mat_out, &depth_min, &depth_max);
////    DEBUG
//#if 0
//    cv::Mat pred;
//    mat_out.convertTo(pred, CV_8UC1, 255. / (depth_max - depth_min), (-255. * depth_min) / (depth_max - depth_min));
//    float x=points[0].x*INPUT_W_H;
//    float y=points[0].y*INPUT_W_H;
//    cv::rectangle(pred, cv::Point((int)x-2,(int)y-2),cv::Point((int)x+2,(int)y+2),cv::Scalar(0,0,0));
//    std::string externalStorageDir = "/storage/emulated/0/Android/data/com.outdu.sr.motocamapp4/"; // Modify this to match the actual path
//    std::string completeFilePath = externalStorageDir + "test_dep_rgb.jpg";
//    bool result = imwrite(completeFilePath, pred);
//    completeFilePath = externalStorageDir + "test_rgb.jpg";
//    result = imwrite(completeFilePath, in);
//#endif
//
//    for(cv::Point2f point2F : points){
//        float x=point2F.x*INPUT_W_H;
//        float y=point2F.y*INPUT_W_H;
//        float dat=outputdata[(int)(INPUT_W_H*y+x)];
//        float dat1=dat/depth_max;
//        dep_thres.push_back(dat1);
//    }
//
////    cv::Mat mat_out(256, 256, CV_32FC1);  /* value has no specific range */
//    // Copy output data from output tensor.
////    memcpy(mat_out.data, _output_tensor->data.f,
////           _output_tensor->bytes);
////    memcpy(dst, outputdata,
////           _output_tensor->bytes);
////    memcpy(mat_out.data, outputdata,
////           _output_tensor->bytes);
//
////    double depth_min, depth_max;
////    cv::minMaxLoc(mat_out, &depth_min, &depth_max);
//    return 0;
//}

int Midas::postProcess(const std::vector<cv::Point2f> &points, std::vector<float> &dep_thres)
{
    auto *outputdata=tflite::GetTensorData<float>(_output_tensor);
    cv::Mat mat_out(INPUT_W_H, INPUT_W_H, CV_32FC1, outputdata);
    double depth_min, depth_max;
    cv::minMaxLoc(mat_out, &depth_min, &depth_max);

    for(cv::Point2f point2F : points){
        float x=point2F.x*INPUT_W_H;
        float y=point2F.y*INPUT_W_H;
        float dat=outputdata[(int)(INPUT_W_H*y+x)];
        float dat1=dat/depth_max;
        dep_thres.push_back(dat1);
    }
    return 0;
}

int Midas::invokeProcess() {
    if (_interpreter->Invoke() != kTfLiteOk){
        return 1;
    }
    return 0;
}

int Midas::invokeProcessAsync(const cv::Mat &rgb, const std::vector<cv::Point2f> &points, std::vector<float> &dep_thres) {
    int result=pthread_mutex_trylock(&_mutex);
    if(result==0) {
        postProcess(_pre_points, dep_thres);
        preProcessImage(rgb, points);
        pthread_cond_signal(&_cond);
        pthread_mutex_unlock(&_mutex);
    }
    return 0;
}

void Midas::preProcessImage(const cv::Mat &rgb, const std::vector<cv::Point2f> &points)
{
    _pre_points=points;
    cv::Mat in;
    cv::resize(rgb, in, cv::Size(INPUT_W_H,INPUT_W_H));

    const int32_t img_width = in.cols;
    const int32_t img_height = in.rows;
    const int32_t img_channel = in.channels();

    uint8_t* src = (uint8_t*)(in.data);
    float* dst = tflite::GetTensorData<float>(_input_tensor);

#pragma omp parallel for num_threads(3)
        for (int32_t i = 0; i < img_width * img_height; i++) {
//            BGR to RGB
            dst[i * img_channel + 0] = (src[i * img_channel + 2] - _mean_vals[0]) * _norm_vals[0];//R
            dst[i * img_channel + 1] = (src[i * img_channel + 1] - _mean_vals[1]) * _norm_vals[1];//G
            dst[i * img_channel + 2] = (src[i * img_channel + 0] - _mean_vals[2]) * _norm_vals[2];//B
        }

}
