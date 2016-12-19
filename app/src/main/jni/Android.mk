# Copyright (C) 2010 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=off
OPENCV_LIB_TYPE:=STATIC
include D:/Desenvolvimento/sdk/opencv-android-sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES  := DetectionBasedTracker.cpp

#LOCAL_C_INCLUDES += $(LOCAL_PATH)

LOCAL_LDLIBS +=  -llog -ldl
LOCAL_LDLIBS += -L$(SYSROOT)/usr/lib -llog
LOCAL_MODULE     := app

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)
#opencv
LOCAL_C_INCLUDES:= D:/Desenvolvimento/sdk/opencv-android-sdk/native/jni/include
OPENCVROOT:= D:/Desenvolvimento/sdk/opencv-android-sdk
OPENCV_CAMERA_MODULES:=on
OPENCV_INSTALL_MODULES:=of
OPENCV_LIB_TYPE:=SHARED
include D:/Desenvolvimento/sdk/opencv-android-sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES := facerec.cpp
LOCAL_LDLIBS += -llog  -ldl
LOCAL_MODULE := facerec
#LOCAL_SRC_FILES := facerec.so
#LOCAL_CFLAGS += -I<path>, talvez inserir

include $(BUILD_SHARED_LIBRARY)

