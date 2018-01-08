package com.example.karaianas.renderer_v2;

/**
 * Created by karaianas on 11/1/2017.
 */

import android.opengl.Matrix;
import android.util.Log;

public class Face
{
    private float fn[] = new float[3];
    private int v[] = new int[3];

    Face(int vertex1, int vertex2, int vertex3)
    {
        v[0] = vertex1;v[1] = vertex2;v[2] = vertex3;
        fn[0] = 0;fn[1] = 0;fn[2] = 0;
    }

    public float [] compute_normal(float [] p1, float [] p2, float [] p3)
    {
        float a1, a2, a3;
        float b1, b2, b3;

        //og.d("STATE", "pos: " + p1[0] + " " + p2[0] + " " + p3[0]);

        a1 = p2[0] - p1[0]; a2 = p2[1] - p1[1]; a3 = p2[2] - p1[2];
        b1 = p3[0] - p1[0]; b2 = p3[1] - p1[1]; b3 = p3[2] - p1[2];

        float x, y, z;
        x = a2 * b3 - b2 * a3;
        y = b1 * a3 - a1 * b3;
        z = a1 * b2 - b1 * a2;

        float len = Matrix.length(x, y, z);
        x /= len; y /= len; z /= len;

        fn[0] = x; fn[1] = y; fn[2] = z;

        return fn;
    }

    public void print_fn()
    {
        Log.d("STATE", "Face normal: " + fn[0] + " " + fn[1] + " " + fn[2]);
    }
}
