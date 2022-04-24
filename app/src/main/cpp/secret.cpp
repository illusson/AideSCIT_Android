#include <jni.h>

extern "C"
JNIEXPORT jstring JNICALL
Java_io_github_sgpublic_aidescit_core_manager_Security_getAppKey(JNIEnv *env, jobject thiz) {
    // TODO: implement getAppKey()
    return (*env).NewStringUTF(((string)"").c_str());
}

extern "C"
JNIEXPORT jstring JNICALL
Java_io_github_sgpublic_aidescit_core_manager_Security_getAppSecret(JNIEnv *env, jobject thiz) {
    // TODO: implement getAppSecret()
    return (*env).NewStringUTF(((string)"").c_str());
}
