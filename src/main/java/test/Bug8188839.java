package test;

import sun.awt.image.BufImgSurfaceData;
import sun.java2d.SunGraphics2D;
import sun.java2d.SurfaceData;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage; 

/**
 * Shows the behavior when a transform with a scale too small is attempted to be drawn with stroke
 * Behavior: JVM crashes with exit code -1073741571 (0xC00000FD)
 */ 
public class Bug8188839 {

    // Initial setup, largely unimportant - needed to set up graphics objects
    private static Color foregroundColor = new Color(153, 153, 0);
    private static Color backgroundColor = new Color(0, 0, 0);
    private static Font graphicsFont = Font.getFont("Dialog");
    private static BufferedImage surfaceImg = new BufferedImage(1232, 680, 1);

    public static void main(String[] args) {
        // Attempt drawing with all the same parameters, except scale in transform is slightly larger
        System.out.println("Started drawing success case...");
        Graphics2D successCase = setupSuccessCase();
        draw(successCase);
        System.out.println("Completed drawing success case..." + System.lineSeparator());

        // Attempt drawing with all the same parameters, except scale in transform is slightly smaller
        System.out.println("Started drawing failure case...");
        Graphics2D failureCase = setupFailureCase();
        draw(failureCase);
        // This line is never reached
        System.out.println("Completed drawing failure case..." + System.lineSeparator());
    }

    private static Graphics2D setup() {
        SurfaceData sfdata = BufImgSurfaceData.createData(surfaceImg);
        Graphics2D g2D = new SunGraphics2D(sfdata, foregroundColor, backgroundColor, graphicsFont);
        setStrokeOnGraphics(g2D);
        return g2D;
    }

    private static Graphics2D setupSuccessCase() {
        Graphics2D g2D = setup();
        setTransformOnGraphics_Success(g2D);
        return g2D;
    }

    private static Graphics2D setupFailureCase() {
        Graphics2D g2D = setup();
        setTransformOnGraphics_Fail(g2D);
        return g2D;
    }

    private static void draw(Graphics2D g2D) {
        int x = -10725;
        int y = -10725;
        int width = 21450;
        int height = 21450;
        g2D.drawOval(x, y, width, height);
    }

    private static void setStrokeOnGraphics(Graphics2D g2D) {
        float lineWidth = 301.5389f;
        int endCap = BasicStroke.CAP_BUTT;
        int lineJoin = BasicStroke.JOIN_MITER;
        float miterLimit = 10.0f;
        float[] dashArray = {418.80402f, 209.40201f, 83.7608f, 209.40201f};
        float dashPhase = 0.0f;
        Stroke stk = new BasicStroke(lineWidth, endCap, lineJoin, miterLimit, dashArray, dashPhase);
        g2D.setStroke(stk);
    }

    private static void setTransformOnGraphics_Success(Graphics2D g2D) {
        float scaleX = 2.452038323174252E-4f; // this is different from failure case: 2.452038323174252E-5f
        float shearY = 0.0f;
        float shearX = -0.0f;
        float scaleY = -2.452038323174252E-4f; // this is different from failure case: -2.452038323174252E-5f
        float translateX = 752.8698159461076f;
        float translateY = 343.31690612839196f;
        AffineTransform affTrans = new AffineTransform(scaleX, shearY, shearX, scaleY, translateX, translateY);
        g2D.setTransform(affTrans);
    }

    private static void setTransformOnGraphics_Fail(Graphics2D g2D) {
        float scaleX = 2.452038323174252E-5f; // this is different from success case: 2.452038323174252E-4f
        float shearY = -0.0f;
        float shearX = 0.0f;
        float scaleY = -2.452038323174252E-5f; // this is different from success case: -2.452038323174252E-4f
        float translateX = 752.8698159461076f;
        float translateY = 343.31690612839196f;
        AffineTransform affTrans = new AffineTransform(scaleX, shearY, shearX, scaleY, translateX, translateY);
        g2D.setTransform(affTrans);
    }
} 