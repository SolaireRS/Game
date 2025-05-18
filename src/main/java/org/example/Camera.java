package org.example;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Math;

public class Camera {
    public Vector3f position;
    public float pitch; // up/down angle in degrees
    public float yaw;   // left/right angle in degrees

    public Camera(Vector3f startPos) {
        this.position = startPos;
        this.pitch = 0.0f;
        this.yaw = -90.0f; // looking towards negative Z by default
    }

    public Matrix4f getViewMatrix() {
        Vector3f front = getFront();
        Vector3f target = new Vector3f(position).add(front);
        return new Matrix4f().lookAt(position, target, new Vector3f(0, 1, 0));
    }

    public Vector3f getFront() {
        Vector3f front = new Vector3f();
        front.x = (float) Math.cos(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.y = (float) Math.sin(Math.toRadians(pitch));
        front.z = (float) Math.sin(Math.toRadians(yaw)) * (float) Math.cos(Math.toRadians(pitch));
        front.normalize();
        return front;
    }

    public Vector3f getRight() {
        // Right vector is cross product of front and world up (0,1,0)
        return getFront().cross(new Vector3f(0, 1, 0)).normalize();
    }

    public Vector3f getUp() {
        // Up vector is cross of right and front
        return getRight().cross(getFront()).normalize();
    }
}
