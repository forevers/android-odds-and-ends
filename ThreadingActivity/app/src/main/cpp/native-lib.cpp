#include <android/log.h>
#include <libgen.h>
#include <jni.h>
#include <memory>
#include <unistd.h>
#include <string>
#include <thread>
#include <unistd.h>

static JavaVM *savedVm;

void setVM(JavaVM *vm) {
    savedVm = vm;
}

#define	NUM_ARRAY_ELEMENTS_(p) ((int) sizeof(p) / sizeof(p[0]))

class NativeThread;
std::shared_ptr<NativeThread> task1_ (nullptr);
std::shared_ptr<NativeThread> task2_ (nullptr);

#define LOG(TAG, FMT, ...) __android_log_print(ANDROID_LOG_DEFAULT, TAG, "[%d*%s:%d:%s]:" FMT,	\
							gettid(), basename(__FILE__), __LINE__, __FUNCTION__, ## __VA_ARGS__)

#define LOGV(TAG, FMT, ...) __android_log_print(ANDROID_LOG_VERBOSE, TAG, "[%d*%s:%d:%s]:" FMT,	\
							gettid(), basename(__FILE__), __LINE__, __FUNCTION__, ## __VA_ARGS__)

#define LOGD(TAG, FMT, ...) __android_log_print(ANDROID_LOG_DEBUG, TAG, "[%d*%s:%d:%s]:" FMT,	\
							gettid(), basename(__FILE__), __LINE__, __FUNCTION__, ## __VA_ARGS__)

#define LOGI(TAG, FMT, ...) __android_log_print(ANDROID_LOG_INFO, TAG, "[%d*%s:%d:%s]:" FMT,	\
							gettid(), basename(__FILE__), __LINE__, __FUNCTION__, ## __VA_ARGS__)

#define LOGW(TAG, FMT, ...) __android_log_print(ANDROID_LOG_WARN, TAG, "[%d*%s:%d:%s]:" FMT,	\
							gettid(), basename(__FILE__), __LINE__, __FUNCTION__, ## __VA_ARGS__)

#define LOGE(TAG, FMT, ...) __android_log_print(ANDROID_LOG_ERROR, TAG, "[%d*%s:%d:%s]:" FMT,	\
							gettid(), basename(__FILE__), __LINE__, __FUNCTION__, ## __VA_ARGS__)

#define LOGF(TAG, FMT, ...) __android_log_print(ANDROID_LOG_FATAL, TAG, "[%d*%s:%d:%s]:" FMT,	\
							gettid(), basename(__FILE__), __LINE__, __FUNCTION__, ## __VA_ARGS__)


class NativeThread
{
    int id_;
    useconds_t usec_;
    std::mutex run_mtx_;
    bool run_state_;

    std::mutex release_mtx_;
    bool release_ = false;
    std::condition_variable release_cv_;

    std::shared_ptr<std::thread> thread_{nullptr};

//    void operator()() // for standard functor
    void run()
    {
        while (get_run_state()) {
            usleep(usec_);
            LOGI("thread", "work() id = %d", id_);
        }

        // wait for release_cv signal
        LOGI("thread", "pre-release id = %02x", id_);
        std::unique_lock<std::mutex> lck(release_mtx_);
        while (!release_) release_cv_.wait(lck);
        LOGI("thread", "post-release id = %02x", id_);
    }

public:

    NativeThread(int id, useconds_t usec) : id_(id), usec_(usec)
    {
        LOGI("thread", "TaskSource constructor id = %02x", id_);
        run_state_ = false;
    }

    ~NativeThread()
    {
        LOGI("thread", "TaskSource destructor id = %02x", id_);
        thread_ = nullptr;
    }

    void start()
    {
        std::unique_lock<std::mutex> lck(run_mtx_);
        run_state_ = true;
        thread_ = std::make_shared<std::thread>(&NativeThread::run, this);
    }

    void stop()
    {
        std::unique_lock<std::mutex> lck(run_mtx_);
        run_state_ = false;
    }

    void release()
    {
        {
            // signal thread to continue to exit at sync point
            std::unique_lock<std::mutex> lck(release_mtx_);
            release_ = true;
            release_cv_.notify_one();
        }

        // block client until thread exist
        if (thread_->joinable()) {
            thread_->join();
            LOGI("thread", "joined to id = %02x", id_);
        } else {
            LOGI("thread", "not joinable to id = %02x", id_);
        }
    }

    bool get_run_state()
    {
        std::unique_lock<std::mutex> lck(run_mtx_);
        return run_state_;
    }
};


extern "C" JNIEXPORT void JNICALL
thread_construction(JNIEnv *pEnv, jobject pObj)
{
    LOGI("thread", "thread_construction()");

    // 1 sec sleeps for 60 seconds
    task1_ = std::make_shared<NativeThread>(1, 1000000, 10);
    // 1 sec sleeps for 10 seconds
    task2_ = std::make_shared<NativeThread>(2, 1000000, 10);

    task1_->start();
    task2_->start();
}

extern "C" JNIEXPORT void JNICALL
thread_deconstruction(JNIEnv *pEnv, jobject pObj)
{
    LOGI("thread", "thread_deconstruction()");

    task1_->stop();
    task2_->stop();

    task1_->release();
    task2_->release();

    task1_ = nullptr;
    task2_ = nullptr;
}


jint registerNativeMethods(JNIEnv* env, const char *class_name, JNINativeMethod *methods, int num_methods) {
    int result = 0;

    jclass clazz = env->FindClass(class_name);
    if (clazz) {
        result = env->RegisterNatives(clazz, methods, num_methods);
        if (result < 0) {
            LOGE("native", "registerNativeMethods failed(class=%s)", class_name);
        }
    } else {
        LOGE("native", "registerNativeMethods: class'%s' not found", class_name);
    }
    return result;
}


static JNINativeMethod methods[] = {
        { "naThreadConstruction", "()V", (void*) thread_construction },
        { "naThreadDeconstruction", "()V", (void*) thread_deconstruction },
};


int register_main_activity(JNIEnv *env) {

    if (registerNativeMethods(env,
                              "com/ess/threadingactvity/MainActivity",
                              methods, NUM_ARRAY_ELEMENTS_(methods)) < 0) {
    }

    return 0;
};


jint JNI_OnLoad(JavaVM *vm, void *reserved) {

    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }
    // register native methods
    int result_module = register_main_activity(env);

    setVM(vm);

    return JNI_VERSION_1_6;
}
