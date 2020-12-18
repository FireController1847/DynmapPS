package com.firecontroller1847.dynmapps;

import org.locationtech.jts.algorithm.Area;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.impl.CoordinateArraySequence;

import javax.sound.sampled.Line;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Test {

    public static void main(String[] args) {
//        ArrayList<Integer> xpoints = new ArrayList<>();
//        ArrayList<Integer> zpoints = new ArrayList<>();
//        double[] coords = new double[6];
//        int count = 0;
//
//        Polygon poly1 = new Polygon(new int[] { 0, 0, 2, 2 }, new int[] { 0, 2, 2, 0 }, 4);
//        Area area1 = new Area(poly1);
//        System.out.println("Area 1");
//        for (PathIterator iterator = area1.getBounds2D().getPathIterator(null); !iterator.isDone(); iterator.next()) {
//            int type = iterator.currentSegment(coords);
//            if (type == PathIterator.SEG_LINETO) {
//                System.out.println("Point " + count + ": (" + coords[0] + ", " + coords[1] + ")");
//            }
//            count++;
//        }
//
//        Polygon poly2 = new Polygon(new int[] { 1, 1, 3, 3 }, new int[] { 0, 2, 2, 0 }, 4);
//        Area area2 = new Area(poly2);
//        count = 0;
//        System.out.println("\nArea 2");
//        for (PathIterator iterator = area2.getBounds2D().getPathIterator(null); !iterator.isDone(); iterator.next()) {
//            int type = iterator.currentSegment(coords);
//            if (type == PathIterator.SEG_LINETO) {
//                System.out.println("Point " + count + ": (" + coords[0] + ", " + coords[1] + ")");
//            }
//            count++;
//        }
//
//        Area combined = (Area) area1.clone();
//        combined.add(area2);
//        count = 0;
//        System.out.println("\nCombined Area");
//        for (PathIterator iterator = combined.getBounds2D().getPathIterator(null); !iterator.isDone(); iterator.next()) {
//            int type = iterator.currentSegment(coords);
//            if (type == PathIterator.SEG_LINETO) {
//                System.out.println("Point " + count + ": (" + coords[0] + ", " + coords[1] + ")");
//            }
//            count++;
//        }
//
//        Polygon poly3 = new Polygon(new int[] { 2, 2, 4, 4 }, new int[] { 0, 4, 4, 0 }, 4);
//        Area area3 = new Area(poly3);
//        count = 0;
//        System.out.println("\nArea 3");
//        for (PathIterator iterator = area3.getBounds2D().getPathIterator(null); !iterator.isDone(); iterator.next()) {
//            int type = iterator.currentSegment(coords);
//            if (type == PathIterator.SEG_LINETO) {
//                System.out.println("Point " + count + ": (" + coords[0] + ", " + coords[1] + ")");
//            }
//            count++;
//        }
//
//        Area combined2 = (Area) combined.clone();
//        combined2.add(area3);
//        count = 0;
//        System.out.println("\nCombined Area");
//        for (PathIterator iterator = combined2.getBounds2D().getPathIterator(null); !iterator.isDone(); iterator.next()) {
//            int type = iterator.currentSegment(coords);
//            if (type == PathIterator.SEG_LINETO) {
//                System.out.println("Point " + count + ": (" + coords[0] + ", " + coords[1] + ")");
//                xpoints.add((int) coords[0]);
//                zpoints.add((int) coords[1]);
//            }
//            count++;
//        }
//
//        JFrame frame = new JFrame();
//        JPanel canvas = new JPanel() {
//            @Override
//            public void paintComponent(Graphics g) {
//                ((Graphics2D) g).scale(20, 20);
//                this.setBackground(Color.WHITE);
//                g.translate(5, 5);
//
//                // Draw Poly1
//                g.setColor(Color.RED);
//                g.drawPolygon(poly1);
//
//                // Draw Poly2
//                g.setColor(Color.BLUE);
//                g.drawPolygon(poly2);
//
//                // Draw Poly3
//                g.setColor(Color.PINK);
//                g.drawPolygon(poly3);
//
//                // Draw Final Polygon
//                g.setColor(Color.BLACK);
//                g.drawPolygon(xpoints.stream().mapToInt(i -> i).toArray(), zpoints.stream().mapToInt(i -> i).toArray(), xpoints.size());
//            }
//        };

        Polygon poly1 = new GeometryFactory().createPolygon(new Coordinate[] {
            new Coordinate(0, 0),
            new Coordinate(0, 2),
            new Coordinate(2, 2),
            new Coordinate(2, 0),
            new Coordinate(0, 0)
        });
        Polygon poly2 = new GeometryFactory().createPolygon(new Coordinate[] {
                new Coordinate(1, 0),
                new Coordinate(1, 2),
                new Coordinate(3, 2),
                new Coordinate(3, 0),
                new Coordinate(1, 0)
        });
        Polygon poly3 = new GeometryFactory().createPolygon(new Coordinate[] {
                new Coordinate(2, 0),
                new Coordinate(2, 4),
                new Coordinate(4, 4),
                new Coordinate(4, 0),
                new Coordinate(2, 0)
        });

        System.out.println(poly1.toText());
        System.out.println(poly2.toText());
        System.out.println(poly3.toText());


        Geometry combined1 = poly1.union(poly2);
        System.out.println(combined1.toText());

        Geometry combined2 = combined1.union(poly3);
        System.out.println(combined2.toText());

        JFrame frame = new JFrame();
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                ((Graphics2D) g).scale(20, 20);
                this.setBackground(Color.WHITE);
                g.translate(5, 5);

                g.setColor(Color.RED);
                drawGeometry((Graphics2D) g, poly1);

                g.setColor(Color.BLUE);
                drawGeometry((Graphics2D) g, poly2);

                g.setColor(Color.PINK);
                drawGeometry((Graphics2D) g, poly3);

                g.setColor(Color.BLACK);
                drawGeometry((Graphics2D) g, combined2);
            }
        };
        frame.add(panel);
        frame.setSize(500, 500);
        frame.setVisible(true);

//        canvas.setForeground(Color.BLUE);
//        canvas.setBackground(Color.RED);
//        canvas.getGraphics().drawString("Hello",40,40);
//        canvas.getGraphics().fillRect(130, 30,100, 80);
//        canvas.getGraphics().drawPolyline(xpoints.stream().mapToInt(i -> i).toArray(), zpoints.stream().mapToInt(i -> i).toArray(), xpoints.size());
    }

    public static void drawGeometry(Graphics2D g, Geometry geo) {
        Coordinate[] coords = geo.getCoordinates();
        System.out.println("COORDS: " + Arrays.toString(coords));
        for (int i = 1; i < coords.length; i++) {
            g.drawLine((int) coords[i - 1].x, (int) coords[i - 1].y, (int) coords[i].x, (int) coords[i].y);
        }
    }

}
