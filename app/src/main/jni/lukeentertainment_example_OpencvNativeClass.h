/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include<stdio.h>
#include<opencv2/opencv.hpp>
#include<opencv2/core/core.hpp>
#include<opencv2/highgui/highgui.hpp>
#include<opencv2/imgproc/imgproc.hpp>
#include<opencv2/ml/ml.hpp>
#include<stdlib.h>
#include <cv.h>
#include<ml.h>
#include<android/log.h>
#include<string.h>

#include<iostream>
#include<fstream>
#include<vector>



/* Header for class lukeentertainment_example_OpencvNativeClass */

//using namespace std;
using namespace cv;


#ifndef _Included_lukeentertainment_example_OpencvNativeClass
#define _Included_lukeentertainment_example_OpencvNativeClass
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     lukeentertainment_example_OpencvNativeClass
 * Method:    convertGray
 * Signature: (JLjava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_train
  (JNIEnv *, jclass, jlong, jstring,jint,jint);

JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_trainMalayalam
  (JNIEnv *, jclass, jlong, jstring,jint,jint);
/*
 * Class:     lukeentertainment_example_OpencvNativeClass
 * Method:    testInput
 * Signature: ()I
 */
JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_testInput
  (JNIEnv *, jclass,jlong,jstring);
JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_testInputMalayalam
  (JNIEnv *, jclass,jlong,jstring);
JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_encryptTrain
  (JNIEnv *, jclass,jlong,jstring);

JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_decryptTest
  (JNIEnv *, jclass,jlong,jstring);

JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_trainIndi
  (JNIEnv *, jclass,jlong,jstring);

JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_processImage
    (JNIEnv *, jclass,jlong);
JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_rotateImage
    (JNIEnv *, jclass,jlong,jint);

JNIEXPORT jint JNICALL Java_lukeentertainment_example_OpencvNativeClass_detectWords
  (JNIEnv *, jclass,jlong);






#ifdef __cplusplus
}
#endif
#endif
