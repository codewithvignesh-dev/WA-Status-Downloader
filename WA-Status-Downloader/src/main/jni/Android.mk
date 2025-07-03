LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)
LOCAL_MODULE    := status_scanner
LOCAL_SRC_FILES := status_scanner.cpp
LOCAL_LDLIBS    := -llog
LOCAL_CPPFLAGS  := -std=c++11
include $(BUILD_SHARED_LIBRARY)

