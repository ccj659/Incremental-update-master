LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := bspatch
LOCAL_SRC_FILES := bspatch.c

include $(BUILD_SHARED_LIBRARY)
