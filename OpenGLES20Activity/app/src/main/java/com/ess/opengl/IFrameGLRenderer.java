package com.ess.opengl;

/**
 * Interface providing frame surface rendering component control access
 */
public interface IFrameGLRenderer {

    /**
     * Start rendering
     */
    public void start();

    /**
     * Stop rendering
     */
    public void stop();

    public void setAngleX(float angle);
    public void setAngleY(float angle);
    public float getAngleX();
    public float getAngleY();
    public void setPeak(float peak);
    public float getPeak();
}
