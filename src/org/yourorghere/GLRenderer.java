package org.yourorghere;

import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

public class GLRenderer implements GLEventListener,
        MouseListener,
        MouseMotionListener {

    int frameCounter = 0;

    float textPoints[] = {
        0.0f, 0.0f,
        1.0f, 0.0f,
        0.0f, 1.0f,
        1.0f, 1.1f,};

    int[] texName = new int[1];

    int MAX_STEPS = 80;

    float btab[][] = new float[4][MAX_STEPS];
    float d_btab[][] = new float[4][MAX_STEPS];

    float bez[][] = new float[MAX_STEPS][3];
    float d_bez[][] = new float[MAX_STEPS][3];

    Transform g_vStartPoint;
    Transform g_vControlPoint1;
    Transform g_vControlPoint2;
    Transform g_vEndPoint;

    float ssx = -0.25f, ssy = -1.75f, ssz = 0;
    float c1sx = 0, c1sy = -1, c1sz = 0;
    float c2sx = -0.5f, c2sy = -0.25f, c2sz = 0;
    float esx = 0, esy = 0.25f, esz = 0;

    float cylRad = 0.0225f;
    float incFac = 1.0225f;

    float rotx = 0.0f;
    float rotz = 0.0f;
    float lastx = 0.0f;
    float lastz = 0.0f;
    float delta_rotx = 1.0f;
    float delta_rotz = 1.0f;
    float movey = 0;
    float movez = 0;
    boolean isMoving = false;

    GLAutoDrawable myDrawable = null;

    public void init(GLAutoDrawable drawable) {

        GL gl = drawable.getGL();
        System.err.println("INIT GL IS: " + gl.getClass().getName());

        gl.setSwapInterval(1);
        myDrawable = drawable;

        float ambient[] = {0.4f, 0.4f, 0.4f, 1.0f};
        float diffuse[] = {1.0f, 1.0f, 1.0f, 1.0f};
        float position[] = {1.0f, 1.0f, 1.0f, 0.0f};

        gl.glLightfv(GL.GL_LIGHT0, GL.GL_AMBIENT, ambient, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_DIFFUSE, diffuse, 0);
        gl.glLightfv(GL.GL_LIGHT0, GL.GL_POSITION, position, 0);

        gl.glEnable(GL.GL_LIGHT0);
        gl.glEnable(GL.GL_LIGHTING);

        gl.glShadeModel(GL.GL_SMOOTH);

        gl.glEnable(GL.GL_NORMALIZE);

        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LESS);

        gl.glClearColor(0.9f, 0.9f, 0.9f, 1);

        int tix = 0;
        while (tix < MAX_STEPS) {
            float t = (1.0f / (1.0f * MAX_STEPS)) * (1.0f * tix);
            btab[0][tix] = (1 - t) * (1 - t) * (1 - t);
            btab[1][tix] = 3.0f * t * (1 - t) * (1 - t);
            btab[2][tix] = 3.0f * t * t * (1 - t);
            btab[3][tix] = t * t * t;
            tix++;
        }
        tix = 0;
        while (tix < MAX_STEPS) {
            float t = (1.0f / (1.0f * MAX_STEPS)) * (1.0f * tix);
            d_btab[0][tix] = -3.0f * t * t + 6.0f * t - 3.0f;
            d_btab[1][tix] = 9.0f * t * t - 12.0f * t + 3.0f;
            d_btab[2][tix] = -9.0f * t * t + 6.0f * t;
            d_btab[3][tix] = 3.0f * t * t;
            tix++;
        }
    }

    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL gl = drawable.getGL();
        GLU glu = new GLU();

        if (height <= 0) {
            height = 1;
        }
        final float h = (float) width / (float) height;
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0f, h, 1.0, 150.0);
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);

        gl.glLoadIdentity();

        showScene(gl, true);

        gl.glFlush();
    }

    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseMoved(MouseEvent e) {
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
        isMoving = false;
        myDrawable.display();
    }

    public void mousePressed(MouseEvent e) {
        lastx = e.getX();
        lastz = e.getY();
        isMoving = true;
    }

    public void mouseDragged(MouseEvent e) {
        if (!isMoving) {
            return;
        }

        int x = e.getX();
        int y = e.getY();

        Dimension dim = e.getComponent().getSize();
        int width = dim.width;
        int height = dim.height;

        rotz += (90.0f * ((float) (x - lastx) / (float) (width)));
        rotx += (90.0f * ((float) (y - lastz) / (float) (height)));

        lastx = x;
        lastz = y;

        myDrawable.display();
    }

    public void showScene(GL gl, boolean bRender) {
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glClear(GL.GL_ACCUM_BUFFER_BIT);
        gl.glLoadIdentity();

        GLU glu = new GLU();
        glu.gluLookAt(0.0f, 4.0f, 5.0f,
                0.0f, 0.0f, 0.0f,
                0.0f, 1.0f, 0.0f);

        gl.glRotatef(rotx, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(rotz, 0.0f, 0.0f, 1.0f);
        gl.glTranslatef(0, movey, movez);

        gl.glPushMatrix();

        gl.glEnable(GL.GL_TEXTURE_2D);
        drawArms(gl);
        gl.glDisable(GL.GL_TEXTURE_2D);

        gl.glPopMatrix();
        gl.glFlush();
    }

    private void setupArm(int index) {
        g_vControlPoint2 = new Transform(-1, 0.125f, 0);
        g_vEndPoint = new Transform(0, 0, 0);

        if (index == 0) {
            g_vStartPoint = new Transform(-2, 0, 0);
            g_vControlPoint1 = new Transform(-1.5f, -0.5f, 0);
        } else if (index == 1) {
            g_vStartPoint = new Transform(-2, 0, 0);
            g_vControlPoint1 = new Transform(-1.5f, 0.5f, 0);
        } else if (index == 2) {
            g_vStartPoint = new Transform(-1.75f, 0, 0);
            g_vControlPoint1 = new Transform(-1, 0, 0);
        } else if (index == 3) {
            g_vStartPoint = new Transform(-2, 0, 0);
            g_vControlPoint1 = new Transform(-1.5f, -0.25f, 0);
        } else if (index == 4) {
            g_vStartPoint = new Transform(-2, 0, 0);
            g_vControlPoint1 = new Transform(-1.5f, -0.75f, 0);
        } else if (index == 5) {
            g_vStartPoint = new Transform(-2, 0, 0);
            g_vControlPoint1 = new Transform(-1.5f, -0.15f, 0);
        } else if (index == 6) {
            g_vStartPoint = new Transform(-2.25f, 0, 0);
            g_vControlPoint1 = new Transform(-1.5f, 0f, 0);
        } else if (index == 7) {
            g_vStartPoint = new Transform(-1.5f, 0, 0);
            g_vControlPoint1 = new Transform(-1.5f, -1, 0);
        }

        makeBezier(g_vStartPoint, g_vControlPoint1, g_vControlPoint2, g_vEndPoint);
        make_d_Bezier(g_vStartPoint, g_vControlPoint1, g_vControlPoint2, g_vEndPoint);
    }

    public void drawArms(GL gl) {
        textures(gl, "images/skin_gradient.png");
        setTexParameter(gl);
        gl.glPushMatrix();

        for (int i = 0; i < 8; i++) {
            
            setupArm(i);
            
            gl.glRotatef(45 * i, 0, 1, 0);

            int ix = 0;
            cylRad = 0.0225f;

            while (ix < MAX_STEPS) {
                Transform T = new Transform(d_bez[ix][0], d_bez[ix][1], d_bez[ix][2]);
                T.normalize();

                Transform B = new Transform(d_bez[ix][0], d_bez[ix][1], d_bez[ix][2]);
                B.cross(0.0f, 0.0f, 1.0f);
                B.normalize();

                Transform N = new Transform(B.x, B.y, B.z);
                N.cross(T);

                Transform C = new Transform(bez[ix][0], bez[ix][1], bez[ix][2]);

                float M[] = {
                    N.x, N.y, N.z, 0,
                    B.x, B.y, B.z, 0,
                    T.x, T.y, T.z, 0,
                    C.x, C.y, C.z, 1
                };

                gl.glPushMatrix();

                gl.glMultMatrixf(M, 0);

                GLU glu = new GLU();
                GLUquadric glpQ = glu.gluNewQuadric();
                if (ix == 0) {
                    glu.gluQuadricTexture(glpQ, true);
                    glu.gluSphere(glpQ, cylRad, 20, 20);
                }
                glu.gluQuadricTexture(glpQ, true);
                glu.gluCylinder(glpQ, cylRad, cylRad * incFac, (2 / MAX_STEPS) + 0.075, 15, 1);
                glu.gluDeleteQuadric(glpQ);
                gl.glPopMatrix();

                ix++;

                cylRad *= incFac;
            }
        }
        gl.glPopMatrix();
    }

    private void drawBezier(GL gl) {
        gl.glBegin(GL.GL_LINE_STRIP);
        int ix = 0;
        while (ix < MAX_STEPS) {
            gl.glVertex3f(bez[ix][0], bez[ix][1], bez[ix][2]);
            ix++;
        }
        gl.glEnd();
    }

    private void makeBezier(Transform p0, Transform p1, Transform p2, Transform p3) {
        int ix = 0;
        while (ix < MAX_STEPS) {
            bez[ix][0] = p0.x * btab[0][ix] + p1.x * btab[1][ix] + p2.x * btab[2][ix] + p3.x * btab[3][ix];
            bez[ix][1] = p0.y * btab[0][ix] + p1.y * btab[1][ix] + p2.y * btab[2][ix] + p3.y * btab[3][ix];
            bez[ix][2] = p0.z * btab[0][ix] + p1.z * btab[1][ix] + p2.z * btab[2][ix] + p3.z * btab[3][ix];
            ix++;
        }
    }

    private void make_d_Bezier(Transform p0, Transform p1, Transform p2, Transform p3) {
        int ix = 0;
        while (ix < MAX_STEPS) {
            d_bez[ix][0] = p0.x * d_btab[0][ix] + p1.x * d_btab[1][ix] + p2.x * d_btab[2][ix] + p3.x * d_btab[3][ix];
            d_bez[ix][1] = p0.y * d_btab[0][ix] + p1.y * d_btab[1][ix] + p2.y * d_btab[2][ix] + p3.y * d_btab[3][ix];
            d_bez[ix][2] = p0.z * d_btab[0][ix] + p1.z * d_btab[1][ix] + p2.z * d_btab[2][ix] + p3.z * d_btab[3][ix];
            ix++;
        }
    }

    public void textures(GL gl, String file) {
        try {
            TextureReader.Texture tx = TextureReader.readTexture(file);
            gl.glGenTextures(1, texName, 0);
            gl.glBindTexture(GL.GL_TEXTURE_2D, texName[0]);
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, 3,
                    tx.getWidth(),
                    tx.getHeight(),
                    0, GL.GL_RGB, GL.GL_UNSIGNED_BYTE,
                    tx.getPixels());

        } catch (Exception ex) {
            System.out.println("texture error");
        }
    }

    public void setTexParameter(GL gl) {
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_S, GL.GL_CLAMP);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_WRAP_T, GL.GL_CLAMP);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
        gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_REPLACE);
    }
}
