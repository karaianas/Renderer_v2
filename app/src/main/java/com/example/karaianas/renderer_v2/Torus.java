package com.example.karaianas.renderer_v2;

/**
 * Created by karaianas on 11/1/2017.
 */

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import android.opengl.GLES20;
import android.opengl.Matrix;

import java.io.InputStream;
import android.content.Context;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class Torus extends Object{

    private List<String> verticesList;
    private List<String> facesList;
    private List<String> normalsList;

    private FloatBuffer verticesBuffer;
    private ShortBuffer facesBuffer;
    private FloatBuffer normalsBuffer;

    private List<Vertex> vertexSet;
    private List<Face> faceSet;

    private int program;

    private float[] M = new float[16];
    private float[] MV = new float[16];
    private float[] MVP = new float[16];

    int positionAttribute;
    int normalAttribute;
    int MVPmtx;
    int MVmtx;

    int lPos;

    int positionIdx;
    int normalIdx;

    // Somehow the getAssets() throws exception
    public Torus(Context context) {

        verticesList = new ArrayList<>();
        facesList = new ArrayList<>();
        normalsList = new ArrayList<>();

        vertexSet = new ArrayList<>();
        faceSet = new ArrayList<>();

        InputStream is = context.getResources().openRawResource(R.raw.icoshpere);

        Scanner scanner = new Scanner(is);

        // Open the OBJ file with a Scanner
        // Somehow this throws an IO exception
        //Scanner scanner = new Scanner(context.getAssets().open("torus.obj"));

        // Loop through all its lines
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            if (line.startsWith("v ")) {
                // Add vertex line to list of vertices
                verticesList.add(line);
            } else if (line.startsWith("f ")) {
                // Add face line to faces list
                // ***Optimize the pattern matching later. Currently it's matching all 6 numbers
                Pattern p = Pattern.compile("(\\d+)//(\\d+) (\\d+)//(\\d+) (\\d+)//(\\d+)");
                Matcher m = p.matcher(line);
                m.find();
                String v1 = m.group(1);
                String v2 = m.group(3);
                String v3 = m.group(5);

                facesList.add(v1 + " " + v2 + " " + v3);
            }
            /*
            else if(line.startsWith("vn "))
            {
                normalsList.add(line);
            }
            */
        }

        // Close the scanner
        scanner.close();

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
        ByteBuffer buffer3 = ByteBuffer.allocateDirect(verticesList.size() * 3 * 4);
        buffer3.order(ByteOrder.nativeOrder());
        normalsBuffer = buffer3.asFloatBuffer();

        for (String vertex : verticesList) {
            String coords[] = vertex.split(" ");

            float x = Float.parseFloat(coords[1]);
            float y = Float.parseFloat(coords[2]);
            float z = Float.parseFloat(coords[3]);

            verticesBuffer.put(x);
            verticesBuffer.put(y);
            verticesBuffer.put(z);

            Vertex v = new Vertex(x, y, z);
            vertexSet.add(v);
        }
        verticesBuffer.position(0);

        for (String face : facesList) {
            String vertexIndices[] = face.split(" ");

            short vertex1 = Short.parseShort(vertexIndices[0]);
            short vertex2 = Short.parseShort(vertexIndices[1]);
            short vertex3 = Short.parseShort(vertexIndices[2]);

            facesBuffer.put((short) (vertex1 - 1));
            facesBuffer.put((short) (vertex2 - 1));
            facesBuffer.put((short) (vertex3 - 1));

            // Compute and add the normals right here
            float[] p1 = vertexSet.get(vertex1 - 1).get_position();
            float[] p2 = vertexSet.get(vertex2 - 1).get_position();
            float[] p3 = vertexSet.get(vertex3 - 1).get_position();

            Face f = new Face(vertex1 - 1, vertex2 - 1, vertex3 - 1);
            faceSet.add(f);

            float[] fn = f.compute_normal(p1, p2, p3);

            vertexSet.get(vertex1 - 1).add_normal(fn);
            vertexSet.get(vertex2 - 1).add_normal(fn);
            vertexSet.get(vertex3 - 1).add_normal(fn);
        }
        facesBuffer.position(0);

        for (Vertex v : vertexSet) {
            float[] vn = v.get_normal();
            normalsBuffer.put(vn[0]);
            normalsBuffer.put(vn[1]);
            normalsBuffer.put(vn[2]);
        }
        normalsBuffer.position(0);

        // Convert vertex_shader.txt to a string
        Scanner vScanner = new Scanner(context.getResources().openRawResource(R.raw.vertex_shader), "UTF-8");
        String vertexShaderCode = vScanner.useDelimiter("\\A").next();
        vScanner.close();

        Scanner fScanner = new Scanner(context.getResources().openRawResource(R.raw.fragment_shader), "UTF-8");
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

        Log.i("TORUS", ":" + program);

        // ----------------------------------
        //GLES20.glBindAttribLocation(program, 0, "a_position");
        //GLES20.glBindAttribLocation(program, 1, "a_normal");
        // ----------------------------------

        // Link the main program
        GLES20.glLinkProgram(program);

        // Create buffers for vertices and normals
        final int buffers[] = new int[2];
        GLES20.glGenBuffers(2, buffers, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[0]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, verticesBuffer.capacity() * 4, verticesBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, buffers[1]);
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, normalsBuffer.capacity() * 4, normalsBuffer, GLES20.GL_STATIC_DRAW);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        positionIdx = buffers[0];
        normalIdx = buffers[1];
    }
    public int getProgram()
    {
        return program;
    }

    public void setM(float[] modelMatrix) {
        M = modelMatrix;
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
        normalAttribute = GLES20.glGetAttribLocation(program, "a_normal");
        MVPmtx = GLES20.glGetUniformLocation(program, "u_MVP");
        MVmtx = GLES20.glGetUniformLocation(program, "u_MV");
        lPos = GLES20.glGetUniformLocation(program, "u_light_position");

        // Bind vertices
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, positionIdx);
        GLES20.glEnableVertexAttribArray(positionAttribute);
        GLES20.glVertexAttribPointer(positionAttribute, 3, GLES20.GL_FLOAT, false, 0, 0);

        // Bind normals
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, normalIdx);
        GLES20.glEnableVertexAttribArray(normalAttribute);
        GLES20.glVertexAttribPointer(normalAttribute, 3, GLES20.GL_FLOAT, false, 0, 0);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Send information
        //Matrix.multiplyMM(MV, 0, V, 0, M, 0);
        //Matrix.multiplyMM(MVP, 0, P, 0, MV, 0);
        MV = V;
        MVP = P;
        GLES20.glUniformMatrix4fv(MVPmtx, 1, false, MVP, 0);
        GLES20.glUniformMatrix4fv(MVmtx, 1, false, MV, 0);
        GLES20.glUniform3f(lPos, lPosition[0], lPosition[1], lPosition[2]);

        // Draw
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, facesList.size() * 3, GLES20.GL_UNSIGNED_SHORT, facesBuffer);
        //GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, facesList.size()  / 3);

        //GLES20.glDisableVertexAttribArray(position);

    }
}
