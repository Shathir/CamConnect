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

// 1. install
//      pip3 install -U ultralytics pnnx ncnn
// 2. export yolo11 torchscript
//      yolo export model=yolo11n.pt format=torchscript
// 3. convert torchscript with static shape
//      pnnx yolo11n.torchscript
// 4. modify yolo11n_pnnx.py for dynamic shape inference
//      A. modify reshape to support dynamic image sizes
//      B. permute tensor before concat and adjust concat axis
//      C. drop post-process part
//      before:
//          v_235 = v_204.view(1, 144, 6400)
//          v_236 = v_219.view(1, 144, 1600)
//          v_237 = v_234.view(1, 144, 400)
//          v_238 = torch.cat((v_235, v_236, v_237), dim=2)
//          ...
//      after:
//          v_235 = v_204.view(1, 144, -1).transpose(1, 2)
//          v_236 = v_219.view(1, 144, -1).transpose(1, 2)
//          v_237 = v_234.view(1, 144, -1).transpose(1, 2)
//          v_238 = torch.cat((v_235, v_236, v_237), dim=1)
//          return v_238
//      D. modify area attention for dynamic shape inference
//      before:
//          v_95 = self.model_10_m_0_attn_qkv_conv(v_94)
//          v_96 = v_95.view(1, 2, 128, 400)
//          v_97, v_98, v_99 = torch.split(tensor=v_96, dim=2, split_size_or_sections=(32,32,64))
//          v_100 = torch.transpose(input=v_97, dim0=-2, dim1=-1)
//          v_101 = torch.matmul(input=v_100, other=v_98)
//          v_102 = (v_101 * 0.176777)
//          v_103 = F.softmax(input=v_102, dim=-1)
//          v_104 = torch.transpose(input=v_103, dim0=-2, dim1=-1)
//          v_105 = torch.matmul(input=v_99, other=v_104)
//          v_106 = v_105.view(1, 128, 20, 20)
//          v_107 = v_99.reshape(1, 128, 20, 20)
//          v_108 = self.model_10_m_0_attn_pe_conv(v_107)
//          v_109 = (v_106 + v_108)
//          v_110 = self.model_10_m_0_attn_proj_conv(v_109)
//      after:
//          v_95 = self.model_10_m_0_attn_qkv_conv(v_94)
//          v_96 = v_95.view(1, 2, 128, -1)
//          v_97, v_98, v_99 = torch.split(tensor=v_96, dim=2, split_size_or_sections=(32,32,64))
//          v_100 = torch.transpose(input=v_97, dim0=-2, dim1=-1)
//          v_101 = torch.matmul(input=v_100, other=v_98)
//          v_102 = (v_101 * 0.176777)
//          v_103 = F.softmax(input=v_102, dim=-1)
//          v_104 = torch.transpose(input=v_103, dim0=-2, dim1=-1)
//          v_105 = torch.matmul(input=v_99, other=v_104)
//          v_106 = v_105.view(1, 128, v_95.size(2), v_95.size(3))
//          v_107 = v_99.reshape(1, 128, v_95.size(2), v_95.size(3))
//          v_108 = self.model_10_m_0_attn_pe_conv(v_107)
//          v_109 = (v_106 + v_108)
//          v_110 = self.model_10_m_0_attn_proj_conv(v_109)
// 5. re-export yolo11 torchscript
//      python3 -c 'import yolo11n_pnnx; yolo11n_pnnx.export_torchscript()'
// 6. convert new torchscript with dynamic shape
//      pnnx yolo11n_pnnx.py.pt inputshape=[1,3,640,640] inputshape2=[1,3,320,320]
// 7. now you get ncnn model files
//      mv yolo11n_pnnx.py.ncnn.param yolo11n.ncnn.param
//      mv yolo11n_pnnx.py.ncnn.bin yolo11n.ncnn.bin

// the out blob would be a 2-dim tensor with w=144 h=8400
//
//        | bbox-reg 16 x 4       | per-class scores(80) |
//        +-----+-----+-----+-----+----------------------+
//        | dx0 | dy0 | dx1 | dy1 |0.1 0.0 0.0 0.5 ......|
//   all /|     |     |     |     |           .          |
//  boxes |  .. |  .. |  .. |  .. |0.0 0.9 0.0 0.0 ......|
//  (8400)|     |     |     |     |           .          |
//       \|     |     |     |     |           .          |
//        +-----+-----+-----+-----+----------------------+
//

#include "yolo11.h"

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

static inline float intersection_area(const Object& a, const Object& b)
{
    cv::Rect_<float> inter = a.rect & b.rect;
    return inter.area();
}

static void qsort_descent_inplace(std::vector<Object>& objects, int left, int right)
{
    int i = left;
    int j = right;
    float p = objects[(left + right) / 2].prob;

    while (i <= j)
    {
        while (objects[i].prob > p)
            i++;

        while (objects[j].prob < p)
            j--;

        if (i <= j)
        {
            // swap
            std::swap(objects[i], objects[j]);

            i++;
            j--;
        }
    }

    // #pragma omp parallel sections
    {
        // #pragma omp section
        {
            if (left < j) qsort_descent_inplace(objects, left, j);
        }
        // #pragma omp section
        {
            if (i < right) qsort_descent_inplace(objects, i, right);
        }
    }
}

static void qsort_descent_inplace(std::vector<Object>& objects)
{
    if (objects.empty())
        return;

    qsort_descent_inplace(objects, 0, objects.size() - 1);
}

static void nms_sorted_bboxes(const std::vector<Object>& objects, std::vector<int>& picked, float nms_threshold, bool agnostic = false)
{
    picked.clear();

    const int n = objects.size();

    std::vector<float> areas(n);
    for (int i = 0; i < n; i++)
    {
        areas[i] = objects[i].rect.area();
    }

    for (int i = 0; i < n; i++)
    {
        const Object& a = objects[i];

        int keep = 1;
        for (int j = 0; j < (int)picked.size(); j++)
        {
            const Object& b = objects[picked[j]];

            if (!agnostic && a.label != b.label)
                continue;

            // intersection over union
            float inter_area = intersection_area(a, b);
            float union_area = areas[i] + areas[picked[j]] - inter_area;
            // float IoU = inter_area / union_area
            if (inter_area / union_area > nms_threshold)
                keep = 0;
        }

        if (keep)
            picked.push_back(i);
    }
}

static inline float sigmoid(float x)
{
    return 1.0f / (1.0f + expf(-x));
}

static void generate_proposals(const ncnn::Mat& pred, int stride, const ncnn::Mat& in_pad, float prob_threshold, std::vector<Object>& objects)
{
    const int w = in_pad.w;
    const int h = in_pad.h;

    const int num_grid_x = w / stride;
    const int num_grid_y = h / stride;

    const int reg_max_1 = 16;
    const int num_class = pred.w - reg_max_1 * 4; // number of classes. 80 for COCO

    for (int y = 0; y < num_grid_y; y++)
    {
        for (int x = 0; x < num_grid_x; x++)
        {
            const ncnn::Mat pred_grid = pred.row_range(y * num_grid_x + x, 1);

            // find label with max score
            int label = -1;
            float score = -FLT_MAX;
            {
                const ncnn::Mat pred_score = pred_grid.range(reg_max_1 * 4, num_class);

                for (int k = 0; k < num_class; k++)
                {
                    float s = pred_score[k];
                    if (s > score)
                    {
                        label = k;
                        score = s;
                    }
                }

                score = sigmoid(score);
            }

            if (score >= prob_threshold)
            {
                ncnn::Mat pred_bbox = pred_grid.range(0, reg_max_1 * 4).reshape(reg_max_1, 4);

                {
                    ncnn::Layer* softmax = ncnn::create_layer("Softmax");

                    ncnn::ParamDict pd;
                    pd.set(0, 1); // axis
                    pd.set(1, 1);
                    softmax->load_param(pd);

                    ncnn::Option opt;
                    opt.num_threads = 1;
                    opt.use_packing_layout = false;

                    softmax->create_pipeline(opt);

                    softmax->forward_inplace(pred_bbox, opt);

                    softmax->destroy_pipeline(opt);

                    delete softmax;
                }

                float pred_ltrb[4];
                for (int k = 0; k < 4; k++)
                {
                    float dis = 0.f;
                    const float* dis_after_sm = pred_bbox.row(k);
                    for (int l = 0; l < reg_max_1; l++)
                    {
                        dis += l * dis_after_sm[l];
                    }

                    pred_ltrb[k] = dis * stride;
                }

                float pb_cx = (x + 0.5f) * stride;
                float pb_cy = (y + 0.5f) * stride;

                float x0 = pb_cx - pred_ltrb[0];
                float y0 = pb_cy - pred_ltrb[1];
                float x1 = pb_cx + pred_ltrb[2];
                float y1 = pb_cy + pred_ltrb[3];

                Object obj;
                obj.rect.x = x0;
                obj.rect.y = y0;
                obj.rect.width = x1 - x0;
                obj.rect.height = y1 - y0;
                obj.label = label;
                obj.prob = score;

                objects.push_back(obj);
            }
        }
    }
}

//static void generate_proposals(const ncnn::Mat& pred, const std::vector<int>& strides, const ncnn::Mat& in_pad, float prob_threshold, std::vector<Object>& objects)
//{
//    const int w = in_pad.w;
//    const int h = in_pad.h;
//
//    int pred_row_offset = 0;
//    for (size_t i = 0; i < strides.size(); i++)
//    {
//        const int stride = strides[i];
//
//        const int num_grid_x = w / stride;
//        const int num_grid_y = h / stride;
//        const int num_grid = num_grid_x * num_grid_y;
//
//        generate_proposals(pred.row_range(pred_row_offset, num_grid), stride, in_pad, prob_threshold, objects);
//        pred_row_offset += num_grid;
//    }
//}

static void generate_proposals(const ncnn::Mat& pred, const std::vector<int>& strides, const ncnn::Mat& in_pad, float prob_threshold, std::vector<Object>& objects)
{
    const int w = in_pad.w;
    const int h = in_pad.h;

    // Parallel processing: each stride in a separate thread
    std::vector<std::thread> threads;
    std::vector<std::vector<Object>> thread_objects(strides.size());
    std::mutex merge_mutex;

    int pred_row_offset = 0;
    for (size_t i = 0; i < strides.size(); i++)
    {
        const int stride = strides[i];

        const int num_grid_x = w / stride;
        const int num_grid_y = h / stride;
        const int num_grid = num_grid_x * num_grid_y;

        // Launch thread for this stride
        threads.emplace_back([&, i, stride, pred_row_offset, num_grid]() {
            generate_proposals(pred.row_range(pred_row_offset, num_grid), stride, in_pad, prob_threshold, thread_objects[i]);
        });

        pred_row_offset += num_grid;
    }

    // Wait for all threads to complete
    for (auto& thread : threads)
    {
        thread.join();
    }

    // Merge results from all threads
    for (const auto& thread_objs : thread_objects)
    {
        objects.insert(objects.end(), thread_objs.begin(), thread_objs.end());
    }
}

int YOLO11_det::detect(const cv::Mat& rgb, std::vector<Object>& objects)
{
    const int target_size = det_target_size;//640;
    const float prob_threshold = 0.25f;
    const float nms_threshold = 0.45f;

    int img_w = rgb.cols;
    int img_h = rgb.rows;

    // ultralytics/cfg/models/v8/yolo11.yaml
    std::vector<int> strides(3);
    strides[0] = 8;
    strides[1] = 16;
    strides[2] = 32;
    const int max_stride = 32;

    ncnn::Mat in_pad;
    int wpad = 0;
    int hpad = 0;
    float scale = 1.f;

    // Check if image is already target_size x target_size
    if (img_w == target_size && img_h == target_size)
    {
        // No resize or padding needed, directly convert to ncnn::Mat
        ncnn::Mat in = ncnn::Mat::from_pixels(rgb.data, ncnn::Mat::PIXEL_RGB, img_w, img_h);
        
        const float norm_vals[3] = {1 / 255.f, 1 / 255.f, 1 / 255.f};
        in.substract_mean_normalize(0, norm_vals);
        
        in_pad = in;
    }
    else
    {
        // letterbox pad to multiple of max_stride
        int w = img_w;
        int h = img_h;
        if (w > h)
        {
            scale = (float)target_size / w;
            w = target_size;
            h = h * scale;
        }
        else
        {
            scale = (float)target_size / h;
            h = target_size;
            w = w * scale;
        }

        ncnn::Mat in = ncnn::Mat::from_pixels_resize(rgb.data, ncnn::Mat::PIXEL_RGB, img_w, img_h, w, h);

        // letterbox pad to target_size rectangle
        wpad = (w + max_stride - 1) / max_stride * max_stride - w;
        hpad = (h + max_stride - 1) / max_stride * max_stride - h;
        ncnn::copy_make_border(in, in_pad, hpad / 2, hpad - hpad / 2, wpad / 2, wpad - wpad / 2, ncnn::BORDER_CONSTANT, 114.f);

        const float norm_vals[3] = {1 / 255.f, 1 / 255.f, 1 / 255.f};
        in_pad.substract_mean_normalize(0, norm_vals);
    }

    // Round-robin between two model instances
    int current_selector = instance_selector.fetch_add(1, std::memory_order_relaxed);
    bool use_i1 = (current_selector % 2) == 0;
    
    ncnn::Extractor ex = use_i1 ? yolo11_i1.create_extractor() 
                                : yolo11_i2.create_extractor();

    ex.input("in0", in_pad);

    ncnn::Mat out;
    ex.extract("out0", out);

    std::vector<Object> proposals;
    generate_proposals(out, strides, in_pad, prob_threshold, proposals);

    // sort all proposals by score from highest to lowest
    qsort_descent_inplace(proposals);

    // apply nms with nms_threshold
    std::vector<int> picked;
    nms_sorted_bboxes(proposals, picked, nms_threshold);

    int count = picked.size();

    objects.resize(count);
    for (int i = 0; i < count; i++)
    {
        objects[i] = proposals[picked[i]];

        // adjust offset to original unpadded
        float x0 = (objects[i].rect.x - (wpad / 2)) / scale;
        float y0 = (objects[i].rect.y - (hpad / 2)) / scale;
        float x1 = (objects[i].rect.x + objects[i].rect.width - (wpad / 2)) / scale;
        float y1 = (objects[i].rect.y + objects[i].rect.height - (hpad / 2)) / scale;

        // clip
        x0 = std::max(std::min(x0, (float)(img_w - 1)), 0.f);
        y0 = std::max(std::min(y0, (float)(img_h - 1)), 0.f);
        x1 = std::max(std::min(x1, (float)(img_w - 1)), 0.f);
        y1 = std::max(std::min(y1, (float)(img_h - 1)), 0.f);

        objects[i].rect.x = x0;
        objects[i].rect.y = y0;
        objects[i].rect.width = x1 - x0;
        objects[i].rect.height = y1 - y0;
    }

    // sort objects by area
    struct
    {
        bool operator()(const Object& a, const Object& b) const
        {
            return a.rect.area() > b.rect.area();
        }
    } objects_area_greater;
    std::sort(objects.begin(), objects.end(), objects_area_greater);

    return 0;
}

int YOLO11_det::draw(cv::Mat& rgb, const std::vector<Object>& objects)
{
    static const char* class_names[] = {
        "person", "bicycle", "car", "motorcycle", "airplane", "bus", "train", "truck", "boat", "traffic light",
        "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse", "sheep", "cow",
        "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee",
        "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard",
        "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple",
        "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "donut", "cake", "chair", "couch",
        "potted plant", "bed", "dining table", "toilet", "tv", "laptop", "mouse", "remote", "keyboard", "cell phone",
        "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear",
        "hair drier", "toothbrush"
    };

    static cv::Scalar colors[] = {
        cv::Scalar( 67,  54, 244),
        cv::Scalar( 30,  99, 233),
        cv::Scalar( 39, 176, 156),
        cv::Scalar( 58, 183, 103),
        cv::Scalar( 81, 181,  63),
        cv::Scalar(150, 243,  33),
        cv::Scalar(169, 244,   3),
        cv::Scalar(188, 212,   0),
        cv::Scalar(150, 136,   0),
        cv::Scalar(175,  80,  76),
        cv::Scalar(195,  74, 139),
        cv::Scalar(220,  57, 205),
        cv::Scalar(235,  59, 255),
        cv::Scalar(193,   7, 255),
        cv::Scalar(152,   0, 255),
        cv::Scalar( 87,  34, 255),
        cv::Scalar( 85,  72, 121),
        cv::Scalar(158, 158, 158),
        cv::Scalar(125, 139,  96)
    };

    for (size_t i = 0; i < objects.size(); i++)
    {
        const Object& obj = objects[i];

        const cv::Scalar& color = colors[i % 19];

        // fprintf(stderr, "%d = %.5f at %.2f %.2f %.2f x %.2f\n", obj.label, obj.prob,
                // obj.rect.x, obj.rect.y, obj.rect.width, obj.rect.height);

        cv::rectangle(rgb, obj.rect, color);

        char text[256];
        sprintf(text, "%s %.1f%%", class_names[obj.label], obj.prob * 100);

        int baseLine = 0;
        cv::Size label_size = cv::getTextSize(text, cv::FONT_HERSHEY_SIMPLEX, 0.5, 1, &baseLine);

        int x = obj.rect.x;
        int y = obj.rect.y - label_size.height - baseLine;
        if (y < 0)
            y = 0;
        if (x + label_size.width > rgb.cols)
            x = rgb.cols - label_size.width;

        cv::rectangle(rgb, cv::Rect(cv::Point(x, y), cv::Size(label_size.width, label_size.height + baseLine)),
                      cv::Scalar(255, 255, 255), -1);

        cv::putText(rgb, text, cv::Point(x, y + label_size.height),
                    cv::FONT_HERSHEY_SIMPLEX, 0.5, cv::Scalar(0, 0, 0));
    }

    return 0;
}

// Async inference implementation

void YOLO11_det::preprocess(const cv::Mat& rgb, AsyncInferenceContext& ctx)
{
    std::lock_guard<std::mutex> lock(ctx.mtx);
    
    const int target_size = det_target_size;
    
    ctx.img_w = rgb.cols;
    ctx.img_h = rgb.rows;
    
    // ultralytics/cfg/models/v8/yolo11.yaml
    const int max_stride = 32;
    
    // Check if image is already target_size x target_size
    if (ctx.img_w == target_size && ctx.img_h == target_size)
    {
        // No resize or padding needed, directly convert to ncnn::Mat
        ncnn::Mat in = ncnn::Mat::from_pixels(rgb.data, ncnn::Mat::PIXEL_RGB, ctx.img_w, ctx.img_h);
        const float norm_vals[3] = {1 / 255.f, 1 / 255.f, 1 / 255.f};
        in.substract_mean_normalize(0, norm_vals);
        
        ctx.in_pad = in;
        ctx.scale = 1.0f;
        ctx.wpad = 0;
        ctx.hpad = 0;
    }
    else
    {
        // letterbox pad to multiple of max_stride
        int w = ctx.img_w;
        int h = ctx.img_h;
        if (w > h)
        {
            ctx.scale = (float)target_size / w;
            w = target_size;
            h = h * ctx.scale;
        }
        else
        {
            ctx.scale = (float)target_size / h;
            h = target_size;
            w = w * ctx.scale;
        }

        ncnn::Mat in = ncnn::Mat::from_pixels_resize(rgb.data, ncnn::Mat::PIXEL_RGB, ctx.img_w, ctx.img_h, w, h);

        // letterbox pad to target_size rectangle
        ctx.wpad = (w + max_stride - 1) / max_stride * max_stride - w;
        ctx.hpad = (h + max_stride - 1) / max_stride * max_stride - h;
        ncnn::copy_make_border(in, ctx.in_pad, ctx.hpad / 2, ctx.hpad - ctx.hpad / 2, 
                              ctx.wpad / 2, ctx.wpad - ctx.wpad / 2, ncnn::BORDER_CONSTANT, 114.f);

        const float norm_vals[3] = {1 / 255.f, 1 / 255.f, 1 / 255.f};
        ctx.in_pad.substract_mean_normalize(0, norm_vals);
    }
    
    ctx.ready = true;
}

std::shared_ptr<AsyncInferenceContext> YOLO11_det::detect_async(const cv::Mat& rgb)
{
    // Step 1: Check if any instance is available
    // Try instance 1 first
    bool i1_was_free = false;
    bool expected = false;
    if (i1_busy.compare_exchange_strong(expected, true, std::memory_order_acquire)) {
        i1_was_free = true;
    }
    
    // If i1 busy, try instance 2
    bool i2_was_free = false;
    if (!i1_was_free) {
        expected = false;
        if (i2_busy.compare_exchange_strong(expected, true, std::memory_order_acquire)) {
            i2_was_free = true;
        }
    }
    
    // If both instances are busy, return nullptr
    if (!i1_was_free && !i2_was_free) {
        return nullptr;  // Both instances busy, caller can skip this frame
    }
    
    bool use_i1 = i1_was_free;
    
    auto ctx = std::make_shared<AsyncInferenceContext>();
    
    // Step 2: Preprocess the image (synchronous, fast)
    preprocess(rgb, *ctx);
    
    // Step 3: Launch inference in a separate thread (truly async!)
    // Use std::thread instead of std::async for better Android compatibility
    std::thread inference_thread([this, ctx, use_i1]() {
        int ret = -1;
        
        // Run inference
        if (use_i1) {
            ncnn::Extractor ex = yolo11_i1.create_extractor();
            ex.input("in0", ctx->in_pad);
            ncnn::Mat out;
            ret = ex.extract("out0", out);
            
            // Lock and write results
            ctx->mtx.lock();
            ctx->out = out;
            ctx->inference_done = true;
            ctx->mtx.unlock();
            
            // Mark instance available
            i1_busy.store(false, std::memory_order_release);
        } else {
            ncnn::Extractor ex = yolo11_i2.create_extractor();
            ex.input("in0", ctx->in_pad);
            ncnn::Mat out;
            ret = ex.extract("out0", out);
            
            // Lock and write results
            ctx->mtx.lock();
            ctx->out = out;
            ctx->inference_done = true;
            ctx->mtx.unlock();
            
            // Mark instance available
            i2_busy.store(false, std::memory_order_release);
        }
    });
    
    // Detach thread so it runs independently
    inference_thread.detach();
    
    // Returns immediately without waiting for inference to complete!
    return ctx;
}

int YOLO11_det::postprocess(AsyncInferenceContext& ctx, std::vector<Object>& objects)
{
    std::lock_guard<std::mutex> lock(ctx.mtx);
    
    if (!ctx.inference_done)
    {
        return -1; // Inference not complete
    }
    
    const float prob_threshold = 0.25f;
    const float nms_threshold = 0.45f;
    
    // ultralytics/cfg/models/v8/yolo11.yaml
    std::vector<int> strides(3);
    strides[0] = 8;
    strides[1] = 16;
    strides[2] = 32;
    
    std::vector<Object> proposals;
    generate_proposals(ctx.out, strides, ctx.in_pad, prob_threshold, proposals);
    
    // sort all proposals by score from highest to lowest
    qsort_descent_inplace(proposals);
    
    // apply nms with nms_threshold
    std::vector<int> picked;
    nms_sorted_bboxes(proposals, picked, nms_threshold);
    
    int count = picked.size();
    
    objects.resize(count);
    for (int i = 0; i < count; i++)
    {
        objects[i] = proposals[picked[i]];
        
        // adjust offset to original unpadded
        float x0 = (objects[i].rect.x - (ctx.wpad / 2)) / ctx.scale;
        float y0 = (objects[i].rect.y - (ctx.hpad / 2)) / ctx.scale;
        float x1 = (objects[i].rect.x + objects[i].rect.width - (ctx.wpad / 2)) / ctx.scale;
        float y1 = (objects[i].rect.y + objects[i].rect.height - (ctx.hpad / 2)) / ctx.scale;
        
        // clip
        x0 = std::max(std::min(x0, (float)(ctx.img_w - 1)), 0.f);
        y0 = std::max(std::min(y0, (float)(ctx.img_h - 1)), 0.f);
        x1 = std::max(std::min(x1, (float)(ctx.img_w - 1)), 0.f);
        y1 = std::max(std::min(y1, (float)(ctx.img_h - 1)), 0.f);
        
        objects[i].rect.x = x0;
        objects[i].rect.y = y0;
        objects[i].rect.width = x1 - x0;
        objects[i].rect.height = y1 - y0;
    }
    
    // sort objects by area
    struct
    {
        bool operator()(const Object& a, const Object& b) const
        {
            return a.rect.area() > b.rect.area();
        }
    } objects_area_greater;
    std::sort(objects.begin(), objects.end(), objects_area_greater);
    
    return 0;
}

int YOLO11_det::fetch_results(std::shared_ptr<AsyncInferenceContext> ctx, std::vector<Object>& objects)
{
    if (!ctx)
    {
        return -1; // Invalid context
    }
    
    // Wait for inference to complete (busy wait with sleep)
    bool done = false;
    while (!done)
    {
        ctx->mtx.lock();
        done = ctx->inference_done;
        ctx->mtx.unlock();
        
        if (!done)
        {
            // Sleep briefly to avoid busy spinning
            std::this_thread::sleep_for(std::chrono::milliseconds(1));
        }
    }
    
    // Perform postprocessing and return results
    return postprocess(*ctx, objects);
}
