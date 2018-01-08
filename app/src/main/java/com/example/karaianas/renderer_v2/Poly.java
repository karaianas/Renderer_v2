package com.example.karaianas.renderer_v2;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.util.Log;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by karaianas on 11/2/2017.
 */

public class Poly {
    private List<String> verticesList;
    private List<String> facesList;
    private List<String> normalsList;
    private List<String> colorsList;

    private FloatBuffer verticesBuffer;
    private ShortBuffer facesBuffer;
    private FloatBuffer normalsBuffer;
    private FloatBuffer colorsBuffer;

    private List<Vertex> vertexSet;
    private List<Face> faceSet;

    int program;
    int positionIdx;
    int normalIdx;
    int colorIdx;

    Context mContext;

    private float[] M = new float[16];
    private float[] MV = new float[16];
    private float[] MVP = new float[16];

    int positionAttribute;
    int normalAttribute;
    int colorAttribute;
    int MVPmtx;
    int MVmtx;

    int lPos;

    // Light coefficients
    float [] L00 = {0.79f, 0.44f, 0.54f};
    float [] L1_1 = {0.39f, 0.35f, 0.60f};
    float [] L10 = {-0.34f, -0.18f, -0.27f};
    float [] L11 = {-0.29f, -0.06f, 0.01f};
    float [] L2_2 = {-0.11f, -0.05f, -0.12f};
    float [] L2_1 = {-0.26f, -0.22f, -0.47f};
    float [] L20 = {-0.16f, -0.09f, -0.15f};
    float [] L21 = {0.56f, 0.21f, 0.14f};
    float [] L22 = {0.21f, -0.05f, -0.30f};

    int countv = 0;
    int countc = 0;

    public Poly(Context context) {
        mContext = context;

        verticesList = new ArrayList<>();
        facesList = new ArrayList<>();
        normalsList = new ArrayList<>();
        colorsList = new ArrayList<>();

        vertexSet = new ArrayList<>();
        faceSet = new ArrayList<>();

        InputStream is = mContext.getResources().openRawResource(R.raw.simple_sphere);

        Scanner scanner = new Scanner(is);

        // Open the PLY file with a Scanner

        // Loop through all its lines
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("ply"))
                scanner.nextLine();
            else if (line.startsWith("format"))
                scanner.nextLine();
            else if (line.startsWith("comment"))
                scanner.nextLine();
            else if (line.startsWith("element"))
                scanner.nextLine();
            else if (line.startsWith("property"))
            {
                //Log.d("WTF", line);
                scanner.nextLine();
            }
            else if (line.startsWith("end_header")) {
                //scanner.nextLine();
            }
            /*
            else if(line.charAt(0) == '3')
            {
                Log.d("WTF", line);
                //scanner.nextLine();
                Pattern p = Pattern.compile("(\\d+) (\\d+) (\\d+)");
                Matcher m = p.matcher(line);
                m.find();
                String v1 = m.group(1);
                String v2 = m.group(2);
                String v3 = m.group(3);

                Log.d("FACE:", v1 + " " + v2 + " " + v3);
                facesList.add(v1 + " " + v2 + " " + v3);
            }
            */
            else
            {
                countv++;
                //Log.d("WTF", line);
                Pattern p = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+)" +
                        " ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+)");
//                Pattern p = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+)" +
//                        " ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+)");
                Matcher m = p.matcher(line);
                m.find();

                String v1 = m.group(1);
                String v2 = m.group(2);
                String v3 = m.group(3);

                v1 = String.valueOf(Float.valueOf(v1) * 2.0f);
                v2 = String.valueOf(Float.valueOf(v2) * 2.0f);
                v3 = String.valueOf(Float.valueOf(v3) * 2.0f);
                verticesList.add(v1 + " " + v2 + " " + v3);

                //Log.i("TEST", v1 + " " + v2 + " " + v3);

                String n1 = m.group(4);
                String n2 = m.group(5);
                String n3 = m.group(6);

                normalsList.add(n1 + " " + n2 + " " + n3);
                //Log.d("TEST", n1 + " " + n2 + " " + n3);
            }
        }
        scanner.close();

        // Read in colors
        InputStream is_c = mContext.getResources().openRawResource(R.raw.simple_sphere_coeff);
        Scanner scanner_c = new Scanner(is_c);

        // Loop through all its lines
        while (scanner_c.hasNextLine())
        {
            countc++;
            String line = scanner_c.nextLine();
            Pattern p = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+)" +
                    " ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+)" +
                    " ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+)");
//                Pattern p = Pattern.compile("([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+)" +
//                        " ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+) ([-+]?[0-9]*\\.?[0-9]+)");
            Matcher m = p.matcher(line);
            m.find();

            String v1 = m.group(1);
            String v2 = m.group(2);
            String v3 = m.group(3);
            String v4 = m.group(4);
            String v5 = m.group(5);
            String v6 = m.group(6);
            String v7 = m.group(7);
            String v8 = m.group(8);
            String v9 = m.group(9);

            //Log.i("TEST", v1 + " " + v9 + " " + v3);
            float val_r =  (Float.parseFloat(v1) *L00[0] +Float.parseFloat(v2) * L1_1[0] + Float.parseFloat(v3) *L10[0] +
                    Float.parseFloat(v4) *L11[0] + Float.parseFloat(v5) *L2_2[0] + Float.parseFloat(v6) *L2_1[0] +
                    Float.parseFloat(v7) *L20[0] + Float.parseFloat(v8) *L21[0] + Float.parseFloat(v9) *L22[0]);

            float val_g =  (Float.parseFloat(v1) *L00[1] +Float.parseFloat(v2) * L1_1[1] + Float.parseFloat(v3) *L10[1] +
                    Float.parseFloat(v4) *L11[1] + Float.parseFloat(v5) *L2_2[1] + Float.parseFloat(v6) *L2_1[1] +
                    Float.parseFloat(v7) *L20[1] + Float.parseFloat(v8) *L21[1] + Float.parseFloat(v9) *L22[1]);

            float val_b =  (Float.parseFloat(v1) *L00[2] +Float.parseFloat(v2) * L1_1[2] + Float.parseFloat(v3) *L10[2] +
                    Float.parseFloat(v4) *L11[2] + Float.parseFloat(v5) *L2_2[2] + Float.parseFloat(v6) *L2_1[2] +
                    Float.parseFloat(v7) *L20[2] + Float.parseFloat(v8) *L21[2] + Float.parseFloat(v9) *L22[2]);


            colorsList.add(Float.toString(val_r) + " " + Float.toString(val_g) + " " + Float.toString(val_b));
        }
        scanner_c.close();

        // Read in indices
        InputStream is_o = mContext.getResources().openRawResource(R.raw.simple_sphere_order);
        Scanner scanner_o = new Scanner(is_o);

        // Loop through all its lines
        while (scanner_o.hasNextLine())
        {
            //countc++;
            String line = scanner_o.nextLine();
            Pattern p = Pattern.compile("(\\d+) (\\d+) (\\d+) (\\d+)");
            Matcher m = p.matcher(line);
            m.find();
            String v1 = m.group(2);
            String v2 = m.group(3);
            String v3 = m.group(4);

            //Log.d("FACE:", v1 + " " + v2 + " " + v3);
            facesList.add(v1 + " " + v2 + " " + v3);
        }
        scanner_o.close();

        // Create buffer for vertices
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(verticesList.size() * 3 * 4);
        buffer1.order(ByteOrder.nativeOrder());
        verticesBuffer = buffer1.asFloatBuffer();

        // Create buffer for faces
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(facesList.size() * 3 * 2);
        buffer2.order(ByteOrder.nativeOrder());
        facesBuffer = buffer2.asShortBuffer();

        // Create buffer for (vertex) normals
        // Need to make it the same size as the verticesBuffer
//        ByteBuffer buffer3 = ByteBuffer.allocateDirect(verticesList.size() * 3 * 4);
//        buffer3.order(ByteOrder.nativeOrder());
//        normalsBuffer = buffer3.asFloatBuffer();

        // Create buffer for (vertex) colors
        ByteBuffer buffer4 = ByteBuffer.allocateDirect(colorsList.size() * 3 * 4);
        buffer4.order(ByteOrder.nativeOrder());
        colorsBuffer = buffer4.asFloatBuffer();

        for (String vertex : verticesList) {
            String coords[] = vertex.split(" ");

            float x = Float.parseFloat(coords[0]);
            float y = Float.parseFloat(coords[1]);
            float z = Float.parseFloat(coords[2]);

            //Log.d("WTH", ":" + x + " " + y + " " + z);

            verticesBuffer.put(x);
            verticesBuffer.put(y);
            verticesBuffer.put(z);

            Vertex v = new Vertex(x, y, z);
            vertexSet.add(v);
        }
        verticesBuffer.position(0);

        for (String color : colorsList) {
            String coords[] = color.split(" ");

            float x = Float.parseFloat(coords[0]);
            float y = Float.parseFloat(coords[1]);
            float z = Float.parseFloat(coords[2]);

            //Log.d("WTH", ":" + x + " " + y + " " + z);

            colorsBuffer.put(x);
            colorsBuffer.put(y);
            colorsBuffer.put(z);

        }
        colorsBuffer.position(0);

        for (String face : facesList) {
            String vertexIndices[] = face.split(" ");

            short vertex1 = Short.parseShort(vertexIndices[0]);
            short vertex2 = Short.parseShort(vertexIndices[1]);
            short vertex3 = Short.parseShort(vertexIndices[2]);

            facesBuffer.put((short) vertex1);
            facesBuffer.put((short) vertex2);
            facesBuffer.put((short) vertex3);

            // Compute and add the normals right here
//            float[] p1 = vertexSet.get(vertex1 - 1).get_position();
//            float[] p2 = vertexSet.get(vertex2 - 1).get_position();
//            float[] p3 = vertexSet.get(vertex3 - 1).get_position();
//
//            Face f = new Face(vertex1 - 1, vertex2 - 1, vertex3 - 1);
//            faceSet.add(f);
//
//            float[] fn = f.compute_normal(p1, p2, p3);
//
//            vertexSet.get(vertex1 - 1).add_normal(fn);
//            vertexSet.get(vertex2 - 1).add_normal(fn);
//            vertexSet.get(vertex3 - 1).add_normal(fn);
        }
        facesBuffer.position(0);


//        for (Vertex v : vertexSet) {
//            float[] vn = v.get_normal();
//            normalsBuffer.put(vn[0]);
//            normalsBuffer.put(vn[1]);
//            normalsBuffer.put(vn[2]);
//        }
//        normalsBuffer.position(0);

        //Log.d("WTF", countv + ", " + countc);
        setupShader();
    }

    void setupShader()
    {
        // Convert vertex_shader.txt to a string
        Scanner vScanner = new Scanner(mContext.getResources().openRawResource(R.raw.vshader_poly), "UTF-8");
        String vertexShaderCode = vScanner.useDelimiter("\\A").next();
        vScanner.close();

        Scanner fScanner = new Scanner(mContext.getResources().openRawResource(R.raw.fshader_poly), "UTF-8");
        String fragmentShaderCode = fScanner.useDelimiter("\\A").next();
        fScanner.close();

        // Create shader objects
        int vertexShader = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader, vertexShaderCode);

        int fragmentShader = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader, fragmentShaderCode);

        // Pass shader objects to the compiler
        GLES20.glCompileShader(vertexShader);
        GLES20.glCompileShader(fragmentShader);

        // Create new program
        program = GLES20.glCreateProgram();
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);

        // Link the main program
        GLES20.glLinkProgram(program);

        // Create buffers for vertices and normals and colors
        final int buffers[] = new int[2];
        GLES20.glGenBuffers(2, buffers, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, verticesBuffer.capacity() * 4, verticesBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, colorsBuffer.capacity() * 4, colorsBuffer, GLES20.GL_STATIC_DRAW);

//        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[2]);
//        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, normalsBuffer.capacity() * 4, normalsBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        positionIdx = buffers[0];
        colorIdx = buffers[1];
        //normalIdx = buffers[2];
    }

    public void draw(float[] V, float[] P, float[] lPosition) {
        /*
        verticesBuffer.limit(0);
        verticesBuffer = null;
        normalsBuffer.limit(0);
        normalsBuffer = null;
        */

        GLES20.glUseProgram(program);

        positionAttribute = GLES20.glGetAttribLocation(program, "a_position");
        colorAttribute = GLES20.glGetAttribLocation(program, "a_color");
        MVPmtx = GLES20.glGetUniformLocation(program, "u_MVP");
        MVmtx = GLES20.glGetUniformLocation(program, "u_MV");
        lPos = GLES20.glGetUniformLocation(program, "u_light_position");

        // Bind vertices
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionIdx);
        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glVertexAttribPointer(positionAttribute, 3, GLES20.GL_FLOAT, false, 0, 0);
        //GLES20.glDisableVertexAttribArray(positionAttribute);

        // Bind normals
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, colorIdx);
        GLES20.glEnableVertexAttribArray(colorAttribute);
        GLES20.glVertexAttribPointer(colorAttribute, 3, GLES20.GL_FLOAT, false, 0, 0);
        //GLES20.glDisableVertexAttribArray(colorAttribute);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        //Log.i("BUFFER", ":" + positionAttribute + " " + colorAttribute);
        // Send information
        //Matrix.multiplyMM(MV, 0, V, 0, M, 0);
        //Matrix.multiplyMM(MVP, 0, P, 0, MV, 0);
        float [] M = new float[16];
//        Matrix.rotateM(M, 0, 90, 0, 0, 1);
//        Matrix.multiplyMM(MV, 0, V, 0, M, 0);
//        Matrix.multiplyMM(MVP, 0, P, 0, MV, 0);
        MV = V;
        MVP = P;
        GLES20.glUniformMatrix4fv(MVPmtx, 1, false, MVP, 0);
        GLES20.glUniformMatrix4fv(MVmtx, 1, false, MV, 0);
        GLES20.glUniform3f(lPos, lPosition[0], lPosition[1], lPosition[2]);

        // Draw
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        //
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, facesList.size() * 3, GLES20.GL_UNSIGNED_SHORT, facesBuffer);
        Log.d("STRANGE", verticesList.size() + " " + colorsList.size());
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 200);
//
        //GLES20.glDisableVertexAttribArray(position);

    }
}
