package org.yourorghere;

public class Transform {

    float x, y, z;

    Transform(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void cross(Transform w) {
        float newx, newy, newz;
        newx = y * w.z - z * w.y;
        newy = z * w.x - x * w.z;
        newz = x * w.y - y * w.x;
        x = newx;
        y = newy;
        z = newz;
    }

    public void cross(float wx, float wy, float wz) {
        float newx, newy, newz;
        newx = y * wz - z * wy;
        newy = z * wx - x * wz;
        newz = x * wy - y * wx;
        x = newx;
        y = newy;
        z = newz;
    }

    public void normalize() {
        float L = (float) Math.sqrt(x * x + y * y + z * z);
        x = x / L;
        y = y / L;
        z = z / L;
    }
}
