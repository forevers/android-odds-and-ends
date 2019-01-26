#include <util.h>
#include <cstdlib>
#include <cstring>

#include "frame_glrenderer.h"

#include <glm/glm.hpp>
#include <glm/ext.hpp>

#define ESS_FRAME_RENDERER_OPENGL_TAG "FRAME_RENDERER_OPENGL"


FrameGLRenderer::FrameGLRenderer(int width, int height) :
        request_width_(width),
        request_height_(height),
        render_format_(WINDOW_FORMAT_RGBA_8888),
        is_running_(false),
        vertices_(nullptr),
        vertices_ushort_(nullptr),
        indices_(nullptr),
        angleX_(0),
        angleY_(0),
        cameraZOffset_(0.0f)
{
    ENTER_(ESS_FRAME_RENDERER_OPENGL_TAG);

    LOGI_(ESS_FRAME_RENDERER_OPENGL_TAG, "width : %d, height : %d", width, height);

    pthread_cond_init(&surface_sync_, nullptr);
    pthread_mutex_init(&surface_mutex_, nullptr);

    EXIT_(ESS_FRAME_RENDERER_OPENGL_TAG);
}


FrameGLRenderer::~FrameGLRenderer() {

    ENTER_(ESS_FRAME_RENDERER_OPENGL_TAG);

    pthread_cond_destroy(&surface_sync_);
    pthread_mutex_destroy(&surface_mutex_);

    EXIT_(ESS_FRAME_RENDERER_OPENGL_TAG);
}


inline const bool FrameGLRenderer::IsRunning() const {
    return is_running_;
}


void FrameGLRenderer::ClearSurface() {
    ENTER_(ESS_FRAME_RENDERER_OPENGL_TAG);

    // Redraw background color
    glClear(GL_COLOR_BUFFER_BIT);

    EXIT_(ESS_FRAME_RENDERER_OPENGL_TAG);
}


int FrameGLRenderer::Start() {

    ENTER_(ESS_FRAME_RENDERER_OPENGL_TAG);

    int result = EXIT_FAILURE;

    if (!IsRunning()) {

        is_running_ = true;

        result = pthread_create(&thread_, nullptr, ThreadImpl, (void *) this);

        if (result != EXIT_SUCCESS) {
            LOGE_(ESS_FRAME_RENDERER_OPENGL_TAG, "pthread_create() failure : %d", result);
            is_running_ = false;
        }
    }

    RETURN_(ESS_FRAME_RENDERER_OPENGL_TAG, result, int);
}


int FrameGLRenderer::Stop() {
    ENTER_(ESS_FRAME_RENDERER_OPENGL_TAG);

    bool b = IsRunning();
    if (b) {
        is_running_ = false;

        pthread_mutex_lock(&surface_mutex_);
        pthread_cond_signal(&surface_sync_);
        pthread_mutex_unlock(&surface_mutex_);

        if (pthread_join(thread_, nullptr) != EXIT_SUCCESS) {
            LOGW_(ESS_FRAME_RENDERER_OPENGL_TAG, "FrameRendererOpengl pthread_join() failure");
        }

    } else {
        LOGI_(ESS_FRAME_RENDERER_OPENGL_TAG, "StopRender() call when not running");
    }

    ClearSurface();

    RETURN_(ESS_FRAME_RENDERER_OPENGL_TAG, FRAME_SUCCESS, int);
}


void FrameGLRenderer::SetAngleX(float angle) {
    angleX_ = angle;
}


void FrameGLRenderer::SetAngleY(float angle) {
    angleY_ = angle;
}


float FrameGLRenderer::GetAngleX() {
    return angleX_;
}


float FrameGLRenderer::GetAngleY() {
    return angleY_;
}


void FrameGLRenderer::SetPeak(float peak) {
    peak_ = peak;
}


float FrameGLRenderer::GetPeak() {
    return peak_;
}


void FrameGLRenderer::OnSurfaceCreated() {
    ENTER_(ESS_FRAME_RENDERER_OPENGL_TAG);

    // Set the background frame color
    glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

    // initialize a geometry
    cameraZOffset_ = -2*request_height_;
    vertices_ushort_ = GetVerticesUshort();

    indices_ = GetIndices();

    program_ = CreateProgram(vertexShader_, fragmentShader_);
    if (!program_) {
        LOGE_(FRAME_GLRENDERER_TAG, "Could not create program.");
        return;
    }
    vPositionHandle_ = glGetAttribLocation(program_, "vPosition");
    CheckGlError("glGetAttribLocation");
    LOGI_(ESS_FRAME_RENDERER_OPENGL_TAG, "glGetAttribLocation(\"vPosition\") = %d\n", vPositionHandle_);

    EXIT_(ESS_FRAME_RENDERER_OPENGL_TAG);
}


int FrameGLRenderer::GetVerticesCount() {
    return request_width_ * request_height_ * 3;
}


GLushort* FrameGLRenderer::GetVerticesUshort() {

    if (vertices_ushort_ != nullptr) return vertices_ushort_;

    GLushort* vertices = new GLushort[GetVerticesCount()];
    int i = 0;

    for (GLushort row = 0; row < request_height_; row++) {
        for (GLushort col = 0; col < request_width_; col++) {
            vertices[i++] = col;
            vertices[i++] = row;
            vertices[i++] = 0;
        }
    }

    return vertices;
}


int FrameGLRenderer::GetIndicesCount() {
    return (request_width_*request_height_) + (request_width_-1)*(request_height_-2);
}


short* FrameGLRenderer::GetIndices() {

    if (indices_ != nullptr) return indices_;

    int numIndicies = GetIndicesCount();
    short* indices = new short[numIndicies];
    int i = 0;

    for ( short row=0; row<(request_width_-1); row++ ) {
        if ( (row&1)==0 ) { // even rows
            for (short col=0; col<request_width_; col++ ) {
                indices[i++] = (short)(col + row * request_width_);
                indices[i++] = (short)(col + (row+1) * request_width_);
            }
        } else { // odd rows
            for ( int col=request_width_-1; col>0; col-- ) {
                indices[i++] = (short)(col + (row+1) * request_width_);
                indices[i++] = (short)(col - 1 + + row * request_width_);
            }
        }
    }
    if ( ((request_height_ & 1) != 0) && (request_height_ > 2) ) {
        indices[i++] = (short)((request_height_-1) * request_width_);
    }

    return indices;
}


GLuint FrameGLRenderer::LoadShader(GLenum shaderType, const char* pSource) {

    GLuint shader = glCreateShader(shaderType);
    if (shader) {

        glShaderSource(shader, 1, &pSource, NULL);
        glCompileShader(shader);
        GLint compiled = 0;
        glGetShaderiv(shader, GL_COMPILE_STATUS, &compiled);
        if (!compiled) {

            GLint infoLen = 0;
            glGetShaderiv(shader, GL_INFO_LOG_LENGTH, &infoLen);
            if (infoLen) {

                char* buf = (char*) malloc(infoLen);
                if (buf) {

                    glGetShaderInfoLog(shader, infoLen, NULL, buf);
                    LOGE_(FRAME_GLRENDERER_TAG, "Could not compile shader %d:\n%s\n", shaderType, buf);
                    free(buf);
                }
                glDeleteShader(shader);
                shader = 0;
            }
        }
    }
    return shader;
}


GLuint FrameGLRenderer::CreateProgram(const char* pVertexSource, const char* pFragmentSource) {
    GLuint vertexShader = LoadShader(GL_VERTEX_SHADER, pVertexSource);
    if (!vertexShader) {
        return 0;
    }

    GLuint pixelShader = LoadShader(GL_FRAGMENT_SHADER, pFragmentSource);
    if (!pixelShader) {
        return 0;
    }

    GLuint program = glCreateProgram();
    if (program) {
        glAttachShader(program, vertexShader);
        CheckGlError("glAttachShader");
        glAttachShader(program, pixelShader);
        CheckGlError("glAttachShader");
        glLinkProgram(program);
        GLint linkStatus = GL_FALSE;
        glGetProgramiv(program, GL_LINK_STATUS, &linkStatus);
        if (linkStatus != GL_TRUE) {
            GLint bufLength = 0;
            glGetProgramiv(program, GL_INFO_LOG_LENGTH, &bufLength);
            if (bufLength) {
                char* buf = (char*) malloc(bufLength);
                if (buf) {
                    glGetProgramInfoLog(program, bufLength, NULL, buf);
                    LOGE_(FRAME_GLRENDERER_TAG, "Could not link program:\n%s\n", buf);
                    free(buf);
                }
            }
            glDeleteProgram(program);
            program = 0;
        }
    }
    return program;
}


void FrameGLRenderer::CheckGlError(const char* op) {
    for (GLint error = glGetError(); error; error = glGetError()) {
        LOGI_(ESS_FRAME_RENDERER_OPENGL_TAG, "after %s() glError (0x%x)\n", op, error);
    }
}


void FrameGLRenderer::OnSurfaceChanged(int screen_width, int screen_height) {

    glViewport(0, 0, screen_width, screen_height);
    CheckGlError("glViewport");

    float ratio = (float) screen_width / screen_height;

    // this projection matrix is applied to object coordinates in the onDrawFrame() method
    projectionMatrix_ = glm::perspective(
            glm::radians(45.0f), // The vertical Field of View, in radians: the amount of "zoom". Think "camera lens". Usually between 90° (extra wide) and 30° (quite zoomed in)
            ratio,               // Aspect Ratio. Depends on the size of your window.
            3.0f,                // Near clipping plane. Keep as big as possible, or you'll get precision issues.
            -2*cameraZOffset_    // Far clipping plane. Keep as little as possible.
    );
}


void FrameGLRenderer::OnDrawFrame() {

    // Redraw background color
    glClear(GL_COLOR_BUFFER_BIT);

    // Set the camera position (View matrix)
    glm::mat4 viewMatrix;

    // place camera at origin
    viewMatrix = glm::lookAt(glm::vec3(0.0f, 0.0f, cameraZOffset_), // camera position
                                       glm::vec3(0.0f, 0.0f, 0.0f),  // target position
                                       glm::vec3(0.0f, 1.0f, 0.0f)); // up vector

    // Calculate the projection and view transformation
    glm::mat4 MVPmatrix = projectionMatrix_ * viewMatrix;

    glm::mat4 mRotationMatrixY;
    glm::mat4 scratch;

    // translate to origin
    glm::mat4 translateMatrix = glm::translate(glm::mat4(1.0f), glm::vec3((-request_width_*1280/720)/2, -request_height_/2, 0.0f));
    // rotation
    glm::mat4 rotation_matrix_x = glm::rotate(glm::mat4(1.0f), glm::radians(angleX_), glm::vec3(1.0f, 0.0f, 0.0f));

    glm::mat4 rotation_matrix_y/*temp*/ = glm::rotate(glm::mat4(1.0f), glm::radians(angleY_), glm::vec3(0.0f, 1.0f, 0.0f));

    // Combine the rotation matrix with the projection and camera view
    // Note that the mMVPMatrix factor *must be first* in order
    // for the matrix multiplication product to be correct.
    scratch = projectionMatrix_ * viewMatrix * rotation_matrix_y * rotation_matrix_x * translateMatrix;

    // Add program to OpenGL environment
    glUseProgram(program_);
    CheckGlError("glUseProgram");

    // get handle to vertex shader's vPosition member
    vPositionHandle_ = glGetAttribLocation(program_, "vPosition");
    CheckGlError("glGetAttribLocation");

    // Enable a handle to the shapes vertices
    glEnableVertexAttribArray(vPositionHandle_);
    CheckGlError("glEnableVertexAttribArray");

    // Prepare the shape coordinate data
    glVertexAttribPointer(vPositionHandle_, coordsPerVertex, GL_UNSIGNED_SHORT, GL_FALSE,
                          coordsPerVertex * 2, vertices_ushort_);

    CheckGlError("glVertexAttribPointer");

    // get handle to shape's transformation matrix
    GLint mvp_matrix_handle = glGetUniformLocation(program_, "uMVPMatrix");
    CheckGlError("glGetUniformLocation");

    // Pass the projection and view transformation to the shader
    glUniformMatrix4fv(mvp_matrix_handle, 1, false, glm::value_ptr(scratch));
    CheckGlError("glUniformMatrix4fv");

    // Draw the strip
    // TODO save num_indicies at construciton time
    int num_indicies = GetIndicesCount();
    glDrawArrays(GL_POINTS,0, request_height_*request_width_);

    // Disable vertex array
    glDisableVertexAttribArray(vPositionHandle_);
}


void* FrameGLRenderer::ThreadImpl(void *vptr_args) {
    ENTER_(ESS_FRAME_RENDERER_OPENGL_TAG);

    int result;
    FrameGLRenderer* frame_render = reinterpret_cast<FrameGLRenderer *>(vptr_args);
    if (frame_render) {
        frame_render->Run();
    }

    PRE_EXIT_(ESS_FRAME_RENDERER_OPENGL_TAG);
    pthread_exit(nullptr);
}


float FrameGLRenderer::getPeak() {
    return peak_;
}


void FrameGLRenderer::setPeak(float peak) {

    if (vertices_ushort_ != nullptr) {

        if (peak <= 0.0) peak = 0.0f;
        else if (peak >= 1.0) peak = 1.0f;

        if (peak != peak_) {
            peak_ = peak;
            GLuint peak_ushort = peak * 256;
            for (int row = request_height_ / 4; row < 3 * request_height_ / 4; row++) {
                for (int col = request_width_ / 4; col < 3 * request_width_ / 4; col++) {
                    vertices_ushort_[2 + 3 * (row * request_width_ + col)] = peak_ushort;
                }
            }
        }
    }
}


void FrameGLRenderer::Run(void) {

    ENTER_(ESS_FRAME_RENDERER_OPENGL_TAG);

    for ( ; IsRunning() ; ) {

        bool peak_up = true;

        while(IsRunning()) {

            usleep(1000);

            float peak = getPeak();

            if (peak_up) {
                peak += 0.001;
                if (peak >= 1.0f) {
                    peak = 1.0;
                    peak_up = false;
                }
            } else {
                peak -= 0.01;
                if (peak <= 0.0f) {
                    peak = 0.0f;
                    peak_up = true;
                }
            }

            setPeak(peak);
        }

    }

    ClearSurface();

    LOGI_(ESS_FRAME_RENDERER_OPENGL_TAG, "thread exit");

    EXIT_(ESS_FRAME_RENDERER_OPENGL_TAG);
}
