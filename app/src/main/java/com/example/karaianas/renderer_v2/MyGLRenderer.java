package com.example.karaianas.renderer_v2;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import com.google.vr.sdk.base.Eye;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;
import com.google.vr.sdk.base.HeadTransform;
import com.google.vr.sdk.base.Viewport;

import javax.microedition.khronos.egl.EGLConfig;

/**
 * Created by karaianas on 10/27/2017.
 */

public class MyGLRenderer extends GvrActivity implements GvrView.StereoRenderer{
    private Context mContext;
    private Torus mTorus;
    private Grid mGrid;
    private Poly mPoly;
    private Skybox mSkybox;

    protected float[] modelCube;
    protected float[] modelPosition;

    private static final float Z_NEAR = 0.1f;
    private static final float Z_FAR = 100.0f;

    private static final float CAMERA_Z = 5.f;//0.01f
    private static final float TIME_DELTA = 0.3f;

    private static final float YAW_LIMIT = 0.12f;
    private static final float PITCH_LIMIT = 0.12f;

    private static final float[] LIGHT_POS_IN_WORLD_SPACE = new float[] {0.0f, 2.0f, 0.0f, 1.0f};
    private final float[] lightPosInEyeSpace = new float[4];

    private float[] camera;
    private float[] view;
    private float[] headView;
    private float[] modelViewProjection;
    private float[] modelView;
    private float[] modelFloor;

    private float[] headRotation;

    public MyGLRenderer(Context context)
    {
        mContext = context;
    }

    @Override
    public void onSurfaceCreated(EGLConfig eglConfig) {
        //GLES20.glClearColor(0.1f, 0.5f, 0.5f, 0.5f);
        //GLES20.glClearColor(0.1f, 0.5f, 0.5f, 0.5f);
        //GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        initialize_matrices();

        mGrid = new Grid(mContext);
        mTorus = new Torus(mContext);
        mPoly = new Poly(mContext);
        mSkybox = new Skybox(mContext);

    }

    public void initialize_matrices()
    {
        modelCube = new float[16];
        camera = new float[16];
        view = new float[16];
        modelViewProjection = new float[16];
        modelView = new float[16];
        modelFloor = new float[16];

        // Model first appears directly in front of user.
        //modelPosition = new float[] {0.0f, 0.0f, -MAX_MODEL_DISTANCE / 2.0f};

        // Model is at the origin
        modelPosition = new float[] {0.0f, 0.0f, 0.0f};
        headRotation = new float[4];
        headView = new float[16];

        Matrix.setIdentityM(modelCube, 0);
        Matrix.translateM(modelCube, 0, modelPosition[0], modelPosition[1], modelPosition[2]);
    }

    @Override
    public void onNewFrame(HeadTransform headTransform) {
        setCubeRotation();

        // Build the camera matrix and apply it to the ModelView.
        Matrix.setLookAtM(camera, 0, 0.0f, 0.0f, CAMERA_Z, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);

        headTransform.getHeadView(headView, 0);
        headTransform.getQuaternion(headRotation, 0);
    }

    protected void setCubeRotation()
    {
        //Matrix.rotateM(modelCube, 0, TIME_DELTA, 1.0f, 0.5f, 1.0f);
        Matrix.rotateM(modelCube, 0, TIME_DELTA, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public void onDrawEye(Eye eye) {
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Apply the eye transformation to the camera.
        Matrix.multiplyMM(view, 0, eye.getEyeView(), 0, camera, 0);

        // Set the position of the light
        Matrix.multiplyMV(lightPosInEyeSpace, 0, view, 0, LIGHT_POS_IN_WORLD_SPACE, 0);

        // Build the ModelView and ModelViewProjection matrices
        // for calculating cube position and light.
        float[] perspective = eye.getPerspective(Z_NEAR, Z_FAR);
        Matrix.multiplyMM(modelView, 0, view, 0, modelCube, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        // -----TEMP
        // Need to fix Torus.draw() later
        float [] temp1 = {0.0f, 5.0f, 0.0f, 1.0f};
        // -----TEMP
        mTorus.draw(modelView, modelViewProjection, temp1);

        //mPoly.draw(modelView, modelViewProjection, temp1);
        //mSkybox.draw();

        // Set modelView for the floor, so we draw floor in the correct location
        // -----TEMP
        float floorDepth = 20.0f;
        Matrix.setIdentityM(modelFloor, 0);
        Matrix.translateM(modelFloor, 0, 0, -floorDepth, 0); // Floor appears below user.
        // -----TEMP
        Matrix.multiplyMM(modelView, 0, view, 0, modelFloor, 0);
        Matrix.multiplyMM(modelViewProjection, 0, perspective, 0, modelView, 0);
        //mGrid.draw(lightPosInEyeSpace, modelView, modelViewProjection);
    }

    @Override
    public void onFinishFrame(Viewport viewport) {

    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onRendererShutdown() {

    }
}
