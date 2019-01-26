#ifndef FRAME_GLRENDER_H
#define FRAME_GLRENDER_H

#include <android/native_window.h>
#include <glm/glm.hpp>
#include <GLES2/gl2.h>
#include <jni.h>
#include <pthread.h>
#include <stddef.h>

#include "frame_include.h"
#include "frame_glrenderer_control_ifc.h"

class FrameGLRenderer : public IFrameGLRendererControl {

public:

    FrameGLRenderer(int width, int height);

    ~FrameGLRenderer();

    // IFrameRendererControl methods
    virtual int Start();
    virtual int Stop();
    virtual void SetAngleX(float angle);
    virtual void SetAngleY(float angle);
    virtual float GetAngleX();
    virtual float GetAngleY();
    virtual void SetPeak(float peak);
    virtual float GetPeak();

    virtual void OnSurfaceCreated();
    virtual void OnSurfaceChanged(int screen_width, int screen_height);
    virtual void OnDrawFrame();

    inline const bool IsRunning() const;

    float getPeak();
    void setPeak(float peak);

private:

    const int coordsPerVertex = 3;

    float peak_ = 0;

    // rotations angle
    float angleX_;
    float angleY_;

    const char* vertexShader_ =
            "uniform mat4 uMVPMatrix;\n"
                    "attribute vec4 vPosition;\n"
                    "void main() {\n"
                    "  gl_Position = uMVPMatrix * vPosition;\n"
                    "  gl_PointSize = 0.5;\n"
                    "}\n";

    const char* fragmentShader_ =
            "precision mediump float;\n"
                    "void main() {\n"
                    "  gl_FragColor = vec4(1.0, 0.0, 1.0, 1.0);\n"
                    "}\n";

    GLuint program_;

    GLfloat* vertices_;
    GLushort* vertices_ushort_;

    short* indices_;
    GLuint vPositionHandle_;

    glm::mat4 projectionMatrix_;
    float cameraZOffset_;

    GLushort* GetVerticesUshort();

    int GetVerticesCount();
    short* GetIndices();
    int GetIndicesCount();
    GLuint CreateProgram(const char* pVertexSource, const char* pFragmentSource);
    void CheckGlError(const char* op);
    GLuint LoadShader(GLenum shaderType, const char* pSource);

    volatile bool is_running_;

    // render window size
    int request_width_, request_height_;

    pthread_t thread_;

    void ClearSurface();

    int render_format_;

    pthread_cond_t surface_sync_;
    pthread_mutex_t surface_mutex_;

    static void* ThreadImpl(void *vptr_args);

    void Run(void);
};

#endif // FRAME_GLRENDER_H
