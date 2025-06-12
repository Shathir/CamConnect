cmake_minimum_required(VERSION 3.10.2)

if (${ANDROID_ABI} STREQUAL "armeabi-v7a")
    set(GSTREAMER_ARCH armv7)
elseif (${ANDROID_ABI} STREQUAL "arm64-v8a")
    set(GSTREAMER_ARCH arm64)
elseif (${ANDROID_ABI} STREQUAL "x86")
    set(GSTREAMER_ARCH x86)
elseif (${ANDROID_ABI} STREQUAL "x86_64")
    set(GSTREAMER_ARCH x86_64)
endif()

set(GSTREAMER_ROOT ${GSTREAMER_ROOT_ANDROID}/${GSTREAMER_ARCH})
message("GSTREAMER_ROOT: ${GSTREAMER_ROOT}")
set(ENV{PKG_CONFIG_PATH} "${GSTREAMER_ROOT}/lib/pkgconfig:${GSTREAMER_ROOT}/lib/gstreamer-1.0/pkgconfig")

find_package(PkgConfig REQUIRED)

pkg_check_modules(GST REQUIRED
        gstcoreelements
        gstcoretracers
        gstadder
        gstapp
        gstaudioconvert
        gstaudiomixer
        gstaudiorate
        gstaudioresample
        gstaudiotestsrc
        gstcompositor
        gstgio
        gstoverlaycomposition
        gstpango
        gstrawparse
        gsttypefindfunctions
        gstvideoconvertscale
        gstvideorate
        gstvideotestsrc
        gstvolume
        gstautodetect
        gstvideofilter
        gstswitchbin

        gstsubparse
        gstogg
        gsttheora
        gstvorbis
        gstopus
        gstivorbisdec
        gstadaptivedemux2
        gstalaw
        gstapetag
        gstaudioparsers
        gstauparse
        gstavi
        gstdv
        gstflac
        gstflv
        gstflxdec
        gsticydemux
        gstid3demux
        gstisomp4
        gstjpeg
        gstlame
        gstmatroska
        gstmpg123
        gstmulaw
        gstmultipart
        gstpng
        gstspeex
        gsttaglib
        gstvpx
        gstwavenc
        gstwavpack
        gstwavparse
        gstxingmux
        gsty4menc
        gstadpcmdec
        gstadpcmenc
        gstassrender
        gstbz2
        gstcodecalpha
        gstcodectimestamper
        gstdash
        gstdvbsubenc
        gstdvbsuboverlay
        gstdvdspu
        gsthls
        gstid3tag
        gstkate
        gstmidi
        gstmxf
        gstopenh264
        gstopusparse
        gstpcapparse
        gstpnm
        gstrfbsrc
        gstsiren
        gstsmoothstreaming
        gstsubenc
        gstvideoparsersbad
        gsty4mdec
        gstjpegformat
        gstgdp
        gstrsvg
        gstopenjpeg
        gstspandsp
        gstsbc
        gstzbar
        gstandroidmedia

        gstencoding

        gsttcp
        gstrtsp
        gstrtp
        gstrtpmanager
        gstsoup
        gstudp
        gstdtls
        gstnetsim
        gstrist
        gstrtmp2
        gstrtpmanagerbad
        gstrtponvif
        gstsctp
        gstsdpelem
        gstsrtp
        gstsrt
        gstwebrtc
        gstnice
        gstrtspclientsink

        gstplayback

        gstopengl
        gstipcpipeline
        gstopensles

        #        gstreamer-1.0
        #        gstreamer-app-1.0
        #        gstreamer-tag-1.0
        #        gstreamer-pbutils-1.0
        #        gstreamer-base-1.0
        #        gstreamer-video-1.0
        #        gstreamer-audio-1.0
        #        gstreamer-rtsp-server-1.0
        #        gstreamer-rtsp-1.0
        #        gstreamer-sdp-1.0
        #        gstreamer-rtp-1.0
        #        gstreamer-net-1.0
        #        gstreamer-riff-1.0
        #        gstreamer-gl-1.0
        #        gstreamer-controller-1.0
        #        graphene-gobject-1.0
        #        gstreamer-webrtc-1.0
        #        gstreamer-sctp-1.0
        #        gstreamer-webrtc-nice-1.0

        bzip2
        gmodule-no-export-2.0
        gstreamer-pbutils-1.0
        json-glib-1.0
        librtmp
        sbc
        cairo-fc
        gnustl
        gstreamer-photography-1.0
        kate
        libsoup-2.4
        soundtouch
        cairo-ft
        gobject-2.0
        gstreamer-play-1.0
        libass
        libsrtp2
        spandsp
        cairo-gobject
        graphene-1.0
        gstreamer-player-1.0
        libavcodec
        libssl
        speex
        cairo
        graphene-gobject-1.0
        gstreamer-plugins-bad-1.0
        libavfilter
        libswresample
        sqlite3
        cairo-pdf
        gst-editing-services-1.0
        gstreamer-plugins-base-1.0
        libavformat
        libtiff-4
        srt
        cairo-png
        gstreamer-1.0
        gstreamer-riff-1.0
        libavutil
        libxml-2.0
        taglib
        cairo-ps
        gstreamer-allocators-1.0
        gstreamer-rtp-1.0
        libcroco-0.6
        ltc
        theoradec
        cairo-svg
        gstreamer-app-1.0
        gstreamer-rtsp-1.0
        libcrypto
        nice
        theoraenc
        dav1d
        gstreamer-audio-1.0
        gstreamer-rtsp-server-1.0
        libdca
        oggkate
        theora
        expat
        gstreamer-bad-audio-1.0
        gstreamer-sctp-1.0
        libdts
        ogg
        vo-aacenc
        flac
        gstreamer-base-1.0
        gstreamer-sdp-1.0
        libdv
        opencore-amrnb
        vorbisenc
        fontconfig
        gstreamer-check-1.0
        gstreamer-tag-1.0
        libffi
        opencore-amrwb
        vorbisfile
        freetype2
        gstreamer-codecparsers-1.0
        gstreamer-transcoder-1.0
        libjpeg
        openh264
        vorbisidec
        fribidi
        gstreamer-controller-1.0
        gstreamer-validate-1.0
        libmpg123
        openssl
        vorbis
        gdk-pixbuf-2.0
        gstreamer-fft-1.0
        gstreamer-video-1.0
        libopenjp2
        opus
        vpx
        gio-2.0
        gstreamer-gl-1.0
        gstreamer-vulkan-1.0
        libpcre2-8
        orc-0.4
        wavpack
        gio-unix-2.0
        gstreamer-gl-prototypes-1.0
        gstreamer-webrtc-1.0
        libpng16
        pangocairo
        webrtc-audio-processing
        glib-2.0
        gstreamer-insertbin-1.0
        gstreamer-webrtc-nice-1.0
        libpng
        pangoft2
        x264
        gmodule-2.0
        gstreamer-mpegts-1.0
        gthread-2.0
        libpsl
        pango
        zbar
        gmodule-export-2.0
        gstreamer-net-1.0
        harfbuzz
        librsvg-2.0
        pixman-1
        zlib
)
pkg_check_modules(GLIB REQUIRED glib-2.0 gobject-2.0 gmodule-2.0 zlib libpcre2-8 libffi)

find_library(log-lib log)

include_directories(
        ${GLIB_INCLUDE_DIRS}
        ${GST_INCLUDE_DIRS}
        ${CMAKE_SOURCE_DIR}
        ${CMAKE_SOURCE_DIR}/flatbuffers/include
)

link_directories(
        ${GLIB_INCLUDE_DIRS}
        ${GST_LIBRARY_DIRS}
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}
)

set(OpenCV_DIR ${CMAKE_SOURCE_DIR}/opencv-mobile-4.6.0-android/sdk/native/jni)
find_package(OpenCV REQUIRED core imgproc highgui)
#set(ncnn_DIR ${CMAKE_SOURCE_DIR}/ncnn-20230517-android-vulkan/${ANDROID_ABI}/lib/cmake/ncnn)
#find_package(ncnn REQUIRED)
#
#add_library(tflite SHARED IMPORTED)
#set_target_properties(tflite PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_CURRENT_LIST_DIR}/../jniLibs/${ANDROID_ABI}/libtensorflowlite.so)
#
#add_library(tflitegpu SHARED IMPORTED)
#set_target_properties(tflitegpu PROPERTIES IMPORTED_LOCATION
#        ${CMAKE_CURRENT_LIST_DIR}/../jniLibs/${ANDROID_ABI}/libtensorflowlite_gpu_delegate.so)

add_library(gstreamer_android_player SHARED gstreamer_android_player.cpp)

target_link_libraries(gstreamer_android_player gstreamer_android
        ${GLIB_LIBRARIES}
        ${GST_LIBRARIES}
        ${log-lib}
        orc-0.4
        -lOpenSLES -liconv -landroid
        c++_shared
        -lm -ldl -llog -lbz2 -lz -ljpeg -ldl -pthread -lGLESv2 -lEGL -lpng16 -lexpat
        -lgstisoff-1.0 -lgsturidownloader-1.0 -lgstadaptivedemux-1.0 -lintl -lmp3lame
         ${OpenCV_LIBS} camera2ndk mediandk
)