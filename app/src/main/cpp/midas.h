//
// Created by outdu-gram on 26/09/23.
//

#ifndef MOTOCAMAPP8_MIDAS_H
#define MOTOCAMAPP8_MIDAS_H
#include <cstdio>
#include <iostream>
#include <opencv2/opencv.hpp>
#include "tensorflow/lite/interpreter.h"
#include "tensorflow/lite/kernels/register.h"
#include "tensorflow/lite/kernels/internal/tensor_ctypes.h"
#include "tensorflow/lite/model.h"
//#include "tensorflow/lite/optional_debug_tools.h"

#include <android/asset_manager.h>

class Midas {
public:
    Midas(const cv::Rect &roi);
    ~Midas();

    int load(AAssetManager* mgr, bool use_gpu = false);

    int invokeProcess();
    int invokeProcessAsync(const cv::Mat &rgb);
//    int invokeProcessAsync(const cv::Mat &rgb, std::vector<float> &dep_thres);
    int updatePoints(const std::vector<cv::Point2f> &points);
    int postProcess(int w, int h, std::vector<float> &dep_thres);
    int useFarROI(bool far_roi);

    //for async
    pthread_t _thread;
    pthread_mutex_t _mutex;
    pthread_cond_t _cond;
    bool _quit;
private:

    char *_model_buf;
    std::unique_ptr<tflite::FlatBufferModel> _model;
    std::unique_ptr<tflite::ops::builtin::BuiltinOpResolver> _resolver;
    std::unique_ptr<tflite::Interpreter> _interpreter;
    TfLiteDelegate* _delegate;

    float _mean_vals[3];
    float _norm_vals[3];

    TfLiteTensor* _input_tensor;
    TfLiteTensor* _output_tensor;

    void preProcessImage(const cv::Mat &);

    //async
    bool _input_ready;
    std::vector<cv::Point2f> _pre_points;

    //far
    cv::Rect _roi;
    bool _far_roi;

};
#endif //MOTOCAMAPP8_MIDAS_H
