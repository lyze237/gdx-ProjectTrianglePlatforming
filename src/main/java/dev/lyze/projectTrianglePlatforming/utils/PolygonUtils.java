package dev.lyze.projectTrianglePlatforming.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ShortArray;
import dev.lyze.projectTrianglePlatforming.triangulators.ITriangulator;
import lombok.experimental.UtilityClass;
import lombok.var;

@UtilityClass
public class PolygonUtils {
    public float[] transformVertices(float[] vertices, float scale) {
        return transformVertices(vertices, scale, 0, 0);
    }

    public float[] transformVertices(float[] vertices, float scale, float x, float y) {
        var newVertices = new float[vertices.length];

        for (var i = 0; i < vertices.length; i += 2) {
            newVertices[i] = vertices[i] * scale + x;
            newVertices[i + 1] = vertices[i + 1] * scale + y;
        }

        return newVertices;
    }

    public Array<float[]> triangulate(float[] vertices, ITriangulator triangulator) {
        var triangulatedVertices = triangulator.triangulate(vertices);

        return convertToTriangles(triangulatedVertices);
    }

    public float[] applyIndexes(float[] vertices, ShortArray indexes) {
        var newVertices = new float[indexes.size * 2];

        for (var i = 0; i < indexes.size; i++) {
            var index = indexes.get(i);
            newVertices[i * 2] = vertices[index * 2];
            newVertices[i * 2 + 1] = vertices[index * 2 + 1];
        }

        return newVertices;
    }

    private Array<float[]> convertToTriangles(float[] points) {
        var triangles = new Array<float[]>();

        for (int i = 0; i < points.length; i += 6) {
            var triangle = new float[6];
            System.arraycopy(points, i, triangle, 0, triangle.length);
            triangles.add(triangle);
        }

        return triangles;
    }

    // chat gpt
    public boolean isConcave(float[] polygon) {
        var n = polygon.length / 2;

        if (n < 3) {
            // A polygon with less than 3 vertices is neither concave nor convex
            return false;
        }

        var isClockwise = false;
        var isCounterClockwise = false;

        for (var currentIndex = 0; currentIndex < n; currentIndex++) {
            var prevIndex = (currentIndex + n - 1) % n;
            var nextIndex = (currentIndex + 1) % n;

            var crossProduct = crossProduct(
                    polygon[prevIndex * 2], polygon[prevIndex * 2 + 1],
                    polygon[currentIndex * 2], polygon[currentIndex * 2 + 1],
                    polygon[nextIndex * 2], polygon[nextIndex * 2 + 1]);

            if (crossProduct > 0) {
                isCounterClockwise = true;
            } else if (crossProduct < 0) {
                isClockwise = true;
            }

            if (isClockwise && isCounterClockwise) {
                // The polygon has both clockwise and counterclockwise turns, so it is concave
                return true;
            }
        }

        // If the polygon is either entirely clockwise or entirely counterclockwise, it is convex
        return false;
    }

    // chat gpt
    private float crossProduct(float ax, float ay, float bx, float by, float cx, float cy) {
        // Calculate the cross product of vectors AB and BC
        return (bx - ax) * (cy - by) - (by - ay) * (cx - bx);
    }

    // chat gpt
    public static float[] simplifyPolygon(float[] polygon) {
        var simplifiedPolygon = new FloatArray();

        // Add first point to the simplified polygon
        simplifiedPolygon.add(polygon[0]);
        simplifiedPolygon.add(polygon[1]);

        int n = polygon.length;

        for (var i = 2; i < n; i += 2) {
            var currentX = polygon[i];
            var currentY = polygon[i + 1];
            var prevX = simplifiedPolygon.get(simplifiedPolygon.size - 2);
            var prevY = simplifiedPolygon.get(simplifiedPolygon.size - 1);

            // Check collinearity with the next point or the first point
            var collinear = false;
            if (i < n - 2) {
                var nextX = polygon[i + 2];
                var nextY = polygon[i + 3];
                collinear = areCollinear(prevX, prevY, currentX, currentY, nextX, nextY);
            } else {
                collinear = areCollinear(prevX, prevY, currentX, currentY, polygon[0], polygon[1]);
            }

            // Add the point to the simplified polygon if not collinear
            if (!collinear) {
                simplifiedPolygon.add(currentX);
                simplifiedPolygon.add(currentY);
            }
        }

        return simplifiedPolygon.shrink();
    }

    // chat gpt
    public static boolean areCollinear(float x1, float y1, float x2, float y2, float x3, float y3) {
        // Cross product of vectors (p1, p2) and (p1, p3)
        var crossProduct = (x2 - x1) * (y3 - y1) - (x3 - x1) * (y2 - y1);

        // Points are collinear if the cross product is zero
        return crossProduct == 0;
    }
}
