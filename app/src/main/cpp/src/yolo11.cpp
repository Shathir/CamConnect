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

#include "yolo11.h"

YOLO11::~YOLO11()
{
    // If there are detached inference threads still running, wait for them to finish before
    // destroying the model/net objects. Otherwise those threads may dereference freed memory.
    std::unique_lock<std::mutex> lk(active_threads_mtx);
    active_threads_cv.wait(lk, [this]() { return active_inference_threads.load() == 0; });
}

void YOLO11::on_inference_thread_started()
{
    active_inference_threads.fetch_add(1, std::memory_order_relaxed);
}

void YOLO11::on_inference_thread_finished()
{
    // If this was the last running inference thread, wake any destructor waiting.
    if (active_inference_threads.fetch_sub(1, std::memory_order_acq_rel) == 1)
    {
        std::lock_guard<std::mutex> lk(active_threads_mtx);
        active_threads_cv.notify_all();
    }
}

//int YOLO11::load(const char* parampath, const char* modelpath, bool use_gpu)
//{
//    // Clear both instances
//    yolo11_i1.clear();
//    yolo11_i2.clear();
//
//    // Initialize instance 1
//    yolo11_i1.opt = ncnn::Option();
//#if NCNN_VULKAN
//    yolo11_i1.opt.use_vulkan_compute = true;
//#endif
//    yolo11_i1.load_param(parampath);
//    yolo11_i1.load_model(modelpath);
//
//    // Initialize instance 2
//    yolo11_i2.opt = ncnn::Option();
//#if NCNN_VULKAN
//    yolo11_i2.opt.use_vulkan_compute = false;
//#endif
//    yolo11_i2.load_param(parampath);
//    yolo11_i2.load_model(modelpath);
//
//    // Initialize selector and busy flags
//    instance_selector.store(0);
//    i1_busy.store(false);
//    i2_busy.store(false);
//
//    return 0;
//}

 int YOLO11::load(AAssetManager* mgr, const char* parampath, const char* modelpath, bool use_gpu)
 {
     // Clear both instances
     yolo11_i1.clear();
     yolo11_i2.clear();

     // Initialize instance 1
     yolo11_i1.opt = ncnn::Option();
#if NCNN_VULKAN
     yolo11_i1.opt.use_vulkan_compute = use_gpu;
#endif
     yolo11_i1.opt.num_threads = 2;  // Multi-threaded for better performance
     yolo11_i1.load_param(mgr,parampath);
     yolo11_i1.load_model(mgr, modelpath);

     // Initialize instance 2 with SAME settings (both must use GPU or both CPU)
     yolo11_i2.opt = ncnn::Option();
#if NCNN_VULKAN
     yolo11_i2.opt.use_vulkan_compute = use_gpu;  // FIXED: Respects use_gpu parameter
#endif
     yolo11_i2.opt.num_threads = 2;  // Multi-threaded for better performance
     // NOTE: detect_async() can select instance 2 when instance 1 is busy. If instance 2 isn't
     // loaded, NCNN may crash during inference. Keep both instances loaded with the same model.
     yolo11_i2.load_param(mgr, parampath);
     yolo11_i2.load_model(mgr, modelpath);

     // Initialize selector and busy flags
     instance_selector.store(0);
     i1_busy.store(false);
     i2_busy.store(false);

     return 0;
 }

void YOLO11::set_det_target_size(int target_size)
{
    det_target_size = target_size;
}
