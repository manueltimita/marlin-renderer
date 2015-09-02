/*
 * Copyright (c) 2014, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package test;

import sun.java2d.pipe.BlendComposite;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.marlin.pisces.MarlinProperties;
import org.marlin.pisces.PiscesRenderingEngine;

/**
 * Simple Line rendering test using GeneralPath to enable Pisces / marlin / ductus renderers
 */
public class LineTests {

    private final static String FILE_NAME = "LinesTest-gamma-norm-subpix_lg_";
    private final static boolean useColor = true;
    private final static Color COL_1 = (useColor) ? Color.blue : Color.white;
    private final static Color COL_2 = (useColor) ? Color.yellow : Color.black;
//    private final static Color COL_3 = (useColor) ? Color.green : Color.white;
//    private final static Color COL_1 = Color.white;
//    private final static Color COL_2 = Color.black;
    private final static Color COL_3 = (useColor) ? Color.green : Color.white;
    //new Color(192, 255, 192)

    private final static boolean useCustomComposite = true;
    private final static boolean drawThinLine = true;

    public static void main(String[] args) {
        final int size = 600;
        final int width = size + 100;
        final int height = size;

        System.out.println("LineTests: size = " + width + " x " + height);

        final boolean useLinearRGB = false;

        final BufferedImage image;

        if (useLinearRGB) {
            final ColorModel cm = new DirectColorModel(
                    ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB),
                    32,
                    0x00ff0000, // Red
                    0x0000ff00, // Green
                    0x000000ff, // Blue
                    0xff000000, // Alpha
                    false,
                    DataBuffer.TYPE_INT);

            final WritableRaster raster = cm.createCompatibleWritableRaster(width, height);

            image = new BufferedImage(cm, raster, false, null);
        } else {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }

        final Graphics2D g2d = (Graphics2D) image.getGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        g2d.setClip(0, 0, width, height);

        g2d.setBackground(COL_1);
        g2d.clearRect(0, 0, width, height);

        final long start = System.nanoTime();

        paint(g2d, width, height);

        final long time = System.nanoTime() - start;

        System.out.println("paint: duration= " + (1e-6 * time) + " ms.");

        try {
            final File file = new File(FILE_NAME + MarlinProperties.getSubPixel_Log2_X()
                    + "x" + MarlinProperties.getSubPixel_Log2_Y() + BlendComposite.getBlendingMode() + ".png");

            System.out.println("Writing file: " + file.getAbsolutePath());;
            ImageIO.write(image, "PNG", file);
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            g2d.dispose();
        }
    }

    private static void paint(final Graphics2D g2d, final double width, final double height) {

        final double size = Math.min(width, height);

        Composite c = (useCustomComposite)
                ? BlendComposite.getInstance(BlendComposite.BlendingMode.SRC_OVER)
                : AlphaComposite.SrcOver;

        g2d.setComposite(c);

        g2d.setColor(Color.RED);
        final double radius = 0.25 * Math.min(width, height);
        g2d.fillOval((int) (0.5 * width - radius), (int) (0.5 * height - radius),
                (int) (2.0 * radius), (int) (2.0 * radius));

        double thinStroke = 1.5;
        double lineStroke = 2.5;

        final Path2D.Float path = new Path2D.Float();

        for (double angle = 1d / 5d; angle <= 90d; angle += 1d) {
            double angRad = Math.toRadians(angle);

            double cos = Math.cos(angRad);
            double sin = Math.sin(angRad);

            // same algo as agg:
            g2d.setColor(COL_2);
            drawLine(path, 5d * cos, 5d * sin, size * cos, size * sin, lineStroke);
            g2d.fill(path);

            if (drawThinLine) {
                g2d.setColor(COL_3);
                drawLine(path, 5d * cos, 5d * sin, size * cos, size * sin, thinStroke);
                g2d.fill(path);
            }
        }

        final double rectW = Math.abs(width - height);
        if (rectW > 0.0) {
            final int w = (int) (rectW / 2.);
            final double step = 0.01;
            final double yStep = step * height;
            double alpha = 0.0;

            // BlendingMode.SRC_OVER

            for (double y = 0; y < height; y += yStep, alpha += step) {
                g2d.setColor(new Color(COL_2.getRed(), COL_2.getGreen(), COL_2.getBlue(), (int) (255 * alpha)));
                g2d.fillRect((int) height, (int) y, w, (int) yStep);
            }

            c = AlphaComposite.SrcOver;
            g2d.setComposite(c);
            alpha = 0.0;

            for (double y = 0; y < height; y += yStep, alpha += step) {
                g2d.setColor(new Color(COL_2.getRed(), COL_2.getGreen(), COL_2.getBlue(), (int) (255 * alpha)));
                g2d.fillRect((int) height + w, (int) y, w, (int) yStep);
            }
        }
    }

    private static void drawLine(final Path2D.Float path,
            double x1, double y1,
            double x2, double y2,
            double width) {

        double dx = x2 - x1;
        double dy = y2 - y1;
        double d = Math.sqrt(dx * dx + dy * dy);

        dx = width * (y2 - y1) / d;
        dy = width * (x2 - x1) / d;

        path.reset();

        path.moveTo(x1 - dx, y1 + dy);
        path.lineTo(x2 - dx, y2 + dy);
        path.lineTo(x2 + dx, y2 - dy);
        path.lineTo(x1 + dx, y1 - dy);
    }
}