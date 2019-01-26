#ifndef FRAME_GLRENDERER_CONTROL_IFC_H
#define FRAME_GLRENDERER_CONTROL_IFC_H

class IFrameGLRendererControl {

public:

    virtual ~IFrameGLRendererControl() {};

    virtual int Start() = 0;

    virtual int Stop() = 0;

    virtual void SetAngleX(float angle) = 0;
    virtual void SetAngleY(float angle) = 0;
    virtual float GetAngleX() = 0;
    virtual float GetAngleY() = 0;
    virtual void SetPeak(float peak) = 0;
    virtual float GetPeak() = 0;

    virtual void OnSurfaceCreated() = 0;

    virtual void OnSurfaceChanged(int screen_width, int screen_height) = 0;

    virtual void OnDrawFrame() = 0;
};

#endif // FRAME_GLRENDERER_CONTROL_IFC_H
