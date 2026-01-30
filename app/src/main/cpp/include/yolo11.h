// Tencent is pleased to support the open source community by making ncnn available.
//
// Copyright (C) 2025 THL A29 Limited, a Tencent company. All rights reserved.
//
// Licensed under the BSD 3-Clause License (the "License"); you may not use this file except
// in compliance with the License. You may obtain a copy of the License at
//
// https://opensource.org/licenses/BSD-3-Clause
//
// Unless required by applicable law or agreed to in writing, software distributed
// under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
// CONDITIONS OF ANY KIND, either express or implied. See the License for the
// specific language governing permissions and limitations under the License.

#ifndef YOLO11_H
#define YOLO11_H

#include <opencv2/core/core.hpp>
#include <mutex>
#include <memory>
#include <thread>
#include <atomic>
#include <chrono>
#include <condition_variable>

#include <net.h>

struct KeyPoint
{
    cv::Point2f p;
    float prob;
};

struct Object
{
    cv::Rect_<float> rect;
    cv::RotatedRect rrect;
    int label;
    float prob;
    int gindex;
    cv::Mat mask;
    std::vector<KeyPoint> keypoints;
};

// Async inference context
struct AsyncInferenceContext
{
    cv::Mat preprocessed_image;  // Preprocessed input image
    ncnn::Mat in_pad;            // NCNN input mat
    int img_w;                   // Original image width
    int img_h;                   // Original image height
    float scale;                 // Scale factor
    int wpad;                    // Width padding
    int hpad;                    // Height padding
    bool ready;                  // Whether preprocessing is done
    bool inference_done;         // Whether inference is complete
    ncnn::Mat out;               // Output from network
    std::mutex mtx;              // Mutex for thread safety

    AsyncInferenceContext()
        : img_w(0), img_h(0), scale(1.0f), wpad(0), hpad(0),
          ready(false), inference_done(false) {}
};

class YOLO11
{
public:
    virtual ~YOLO11();

//    int load(const char* parampath, const char* modelpath, bool use_gpu = false);
     int load(AAssetManager* mgr, const char* parampath, const char* modelpath, bool use_gpu = false);

    void set_det_target_size(int target_size);

    virtual int detect(const cv::Mat& rgb, std::vector<Object>& objects) = 0;
    virtual int draw(cv::Mat& rgb, const std::vector<Object>& objects) = 0;

    virtual std::shared_ptr<AsyncInferenceContext> detect_async(const cv::Mat& rgb) = 0;

    // Step 2: Fetch results from async inference (blocking until ready)
    virtual int fetch_results(std::shared_ptr<AsyncInferenceContext> ctx, std::vector<Object>& objects) = 0;

protected:
    ncnn::Net yolo11_i1;  // Model instance 1
    ncnn::Net yolo11_i2;  // Model instance 2
    int det_target_size;
    std::atomic<int> instance_selector;  // Round-robin selector (0 or 1)
    std::atomic<bool> i1_busy;  // True if instance 1 is processing
    std::atomic<bool> i2_busy;  // True if instance 2 is processing

    // Track detached inference threads so the model object cannot be destroyed while they run.
    // This prevents use-after-free crashes when reloading the model.
    std::atomic<int> active_inference_threads{0};
    std::mutex active_threads_mtx;
    std::condition_variable active_threads_cv;

    void on_inference_thread_started();
    void on_inference_thread_finished();
};

class YOLO11_det : public YOLO11
{
public:
    virtual int detect(const cv::Mat& rgb, std::vector<Object>& objects);
    virtual int draw(cv::Mat& rgb, const std::vector<Object>& objects);

    // Async inference API
    // Step 1: Submit image for async inference, returns context handle
    virtual std::shared_ptr<AsyncInferenceContext> detect_async(const cv::Mat& rgb);

    // Step 2: Fetch results from async inference (blocking until ready)
    virtual int fetch_results(std::shared_ptr<AsyncInferenceContext> ctx, std::vector<Object>& objects);

private:
    // Helper function for preprocessing
    void preprocess(const cv::Mat& rgb, AsyncInferenceContext& ctx);

    // Helper function for postprocessing
    int postprocess(AsyncInferenceContext& ctx, std::vector<Object>& objects);
};

#endif // YOLO11_H
