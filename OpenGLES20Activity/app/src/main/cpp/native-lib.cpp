#include <android/log.h>
#include <condition_variable>
#include <jni.h>
#include <unistd.h>

#include "frame_glrenderer.h"
#include "util.h"


#define  LOG_TAG    "ess.opengl.native_lib"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO, LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR, LOG_TAG,__VA_ARGS__)

extern "C" {

// FrameGLRenderer
JNIEXPORT long JNICALL Java_com_ess_opengl_FrameGLRenderer_nativeCreate(JNIEnv * env, jobject obj, long frame_access_handle, int height, int width);
JNIEXPORT void JNICALL Java_com_ess_opengl_FrameGLRenderer_nativeStart(JNIEnv * env, jobject obj, long renderer_handle);
JNIEXPORT void JNICALL Java_com_ess_opengl_FrameGLRenderer_nativeStop(JNIEnv * env, jobject obj, long renderer_handle);
JNIEXPORT void JNICALL Java_com_ess_opengl_FrameGLRenderer_nativeSetAngleX(JNIEnv * env, jobject obj, long renderer_handle, float angle);
JNIEXPORT void JNICALL Java_com_ess_opengl_FrameGLRenderer_nativeSetAngleY(JNIEnv * env, jobject obj, long renderer_handle, float angle);
JNIEXPORT float JNICALL Java_com_ess_opengl_FrameGLRenderer_nativeGetAngleX(JNIEnv * env, jobject obj, long renderer_handle);
JNIEXPORT float JNICALL Java_com_ess_opengl_FrameGLRenderer_nativeGetAngleY(JNIEnv * env, jobject obj, long renderer_handle);
JNIEXPORT void JNICALL Java_com_ess_opengl_FrameGLRenderer_nativeSetPeak(JNIEnv * env, jobject obj, long renderer_handle, float peak);
JNIEXPORT float JNICALL Java_com_ess_opengl_FrameGLRenderer_nativeGetPeak(JNIEnv * env, jobject obj, long renderer_handle);
JNIEXPORT void JNICALL Java_com_ess_opengl_FrameGLRenderer_nativeOnSurfaceCreated(JNIEnv * env, jobject obj, long renderer_handle);
JNIEXPORT void JNICALL Java_com_ess_opengl_FrameGLRenderer_nativeOnSurfaceChanged(JNIEnv * env, jobject obj, long renderer_handle, int width, int height);
JNIEXPORT void JNICALL Java_com_ess_opengl_FrameGLRenderer_nativeOnDrawFrame(JNIEnv * env, jobject obj, long renderer_handle);

};


/*********************** FrameGLRenderer ***********************/
JNIEXPORT long JNICALL
Java_com_ess_opengl_FrameGLRenderer_nativeCreate(JNIEnv * env, jobject obj, long frame_access_handle, int height, int width)
{
    ENTER_(MAIN_TAG);

    FrameGLRenderer* frame_renderer = nullptr;

    frame_renderer = new FrameGLRenderer(width, height);

    RETURN_(MAIN_TAG, reinterpret_cast<jlong>(frame_renderer), jlong);
}


JNIEXPORT void JNICALL
Java_com_ess_opengl_FrameGLRenderer_nativeStart(JNIEnv * env, jobject obj, long renderer_handle)
{
    ENTER_(MAIN_TAG);

    IFrameGLRendererControl* renderer_control = reinterpret_cast<IFrameGLRendererControl *>(renderer_handle);

    renderer_control->Start();
}


JNIEXPORT void JNICALL
Java_com_ess_opengl_FrameGLRenderer_nativeStop(JNIEnv * env, jobject obj, long renderer_handle)
{
    ENTER_(MAIN_TAG);

    IFrameGLRendererControl* renderer_control = reinterpret_cast<IFrameGLRendererControl *>(renderer_handle);

    renderer_control->Stop();
}


JNIEXPORT void JNICALL
Java_com_ess_opengl_FrameGLRenderer_nativeSetAngleX(JNIEnv * env, jobject obj, long renderer_handle, float angle)
{
    ENTER_(MAIN_TAG);

    IFrameGLRendererControl* renderer_control = reinterpret_cast<IFrameGLRendererControl *>(renderer_handle);

    renderer_control->SetAngleX(angle);
}


JNIEXPORT void JNICALL
Java_com_ess_opengl_FrameGLRenderer_nativeSetAngleY(JNIEnv * env, jobject obj, long renderer_handle, float angle)
{
    ENTER_(MAIN_TAG);

    IFrameGLRendererControl* renderer_control = reinterpret_cast<IFrameGLRendererControl *>(renderer_handle);

    renderer_control->SetAngleY(angle);
}


JNIEXPORT float JNICALL
Java_com_ess_opengl_FrameGLRenderer_nativeGetAngleX(JNIEnv * env, jobject obj, long renderer_handle)
{
    ENTER_(MAIN_TAG);

    IFrameGLRendererControl* renderer_control = reinterpret_cast<IFrameGLRendererControl *>(renderer_handle);

    return renderer_control->GetAngleX();
}


JNIEXPORT float JNICALL
Java_com_ess_opengl_FrameGLRenderer_nativeGetAngleY(JNIEnv * env, jobject obj, long renderer_handle)
{
    ENTER_(MAIN_TAG);

    IFrameGLRendererControl* renderer_control = reinterpret_cast<IFrameGLRendererControl *>(renderer_handle);

    return renderer_control->GetAngleY();
}


JNIEXPORT void JNICALL
Java_com_ess_opengl_FrameGLRenderer_nativeSetPeak(JNIEnv * env, jobject obj, long renderer_handle, float peak)
{
    ENTER_(MAIN_TAG);

    IFrameGLRendererControl* renderer_control = reinterpret_cast<IFrameGLRendererControl *>(renderer_handle);

    renderer_control->SetPeak(peak);
}


JNIEXPORT float JNICALL
Java_com_ess_opengl_FrameGLRenderer_nativeGetPeak(JNIEnv * env, jobject obj, long renderer_handle)
{
    ENTER_(MAIN_TAG);

    IFrameGLRendererControl* renderer_control = reinterpret_cast<IFrameGLRendererControl *>(renderer_handle);

    return renderer_control->GetPeak();
}


JNIEXPORT void JNICALL
Java_com_ess_opengl_FrameGLRenderer_nativeOnSurfaceCreated(JNIEnv * env, jobject obj, long renderer_handle)
{
    ENTER_(MAIN_TAG);

    IFrameGLRendererControl* renderer_control = reinterpret_cast<IFrameGLRendererControl *>(renderer_handle);

    renderer_control->OnSurfaceCreated();
}


JNIEXPORT void JNICALL
Java_com_ess_opengl_FrameGLRenderer_nativeOnSurfaceChanged(JNIEnv * env, jobject obj, long renderer_handle, int screen_width, int screen_height)
{
    ENTER_(MAIN_TAG);

    IFrameGLRendererControl* renderer_control = reinterpret_cast<IFrameGLRendererControl *>(renderer_handle);

    renderer_control->OnSurfaceChanged(screen_width, screen_height);
}


JNIEXPORT void JNICALL
Java_com_ess_opengl_FrameGLRenderer_nativeOnDrawFrame(JNIEnv * env, jobject obj, long renderer_handle)
{
    IFrameGLRendererControl* renderer_control = reinterpret_cast<IFrameGLRendererControl *>(renderer_handle);

    renderer_control->OnDrawFrame();
}
