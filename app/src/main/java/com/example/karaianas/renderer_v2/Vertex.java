package com.example.karaianas.renderer_v2;

/**
 * Created by karaianas on 11/1/2017.
 */

import android.util.Log;

/**
 * Created by karai on 9/19/2017.
 */

public class Vertex {
    private float p[] = new float[3];
    private float n[] = new float[3];

    public Vertex(float x, float y, float z)
    {
        p[0] = x;
        p[1] = y;
        p[2] = z;

        n[0] = 0.0f;
        n[1] = 0.0f;
        n[2] = 0.0f;
    }

    public void add_normal(float [] fn)
    {
        n[0] += fn[0];
        n[1] += fn[1];
        n[2] += fn[2];
    }

    public void print_pos()
    {
        Log.d("STATE", "Position: " + p[0] + " " + p[1] + " " + p[2]);
    }

    public float [] get_position()
    {
        return p;
    }

    public float [] get_normal()
    {
        return n;
    }

}
