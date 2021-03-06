package com.firecontroller1847.dynmapps;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Arrays;

public class DPSMarker {

    // Variables
    private String id;
    private String label;
    private String world;
    private double[] x;
    private double[] z;
    private Geometry geometry;
    private DPSLayer layer;

    // Dynamic Variables

    // Constructor
    public DPSMarker(String id, String label, String world, double[] x, double[] z, DPSLayer layer) {
        this.id = id;
        this.label = label;
        this.world = world;
        this.x = x;
        this.z = z;
        this.geometry = toPolygon();
        this.layer = layer;
    }

    // Getters
    public String getId() {
        return id;
    }
    public String getLabel() {
        return label;
    }
    public String getWorld() {
        return world;
    }
    public double[] getX() {
        return x;
    }
    public double[] getZ() {
        return z;
    }
    public Geometry getGeometry() {
        return geometry;
    }
    public DPSLayer getLayer() {
        return layer;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public void setWorld(String world) {
        this.world = world;
    }
    public void setX(double[] x) {
        this.x = x;
    }
    public void setZ(double[] z) {
        this.z = z;
    }
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
    public void setLayer(DPSLayer layer) {
        this.layer = layer;
    }

    // Utility method to convert from a DPSMarker to a Polygon
    public Polygon toPolygon() {
        return DynmapPS.GEOMETRY_FACTORY.createPolygon(toCoordinates(this));
    }

    // Utility method to convert from an DPSMarker to Coordinates
    public Coordinate[] toCoordinates(DPSMarker marker) {
        ArrayList<Coordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < marker.getX().length; i++) {
            coordinates.add(new Coordinate(marker.getX()[i], marker.getZ()[i]));
        }
        coordinates.add(new Coordinate(marker.getX()[0], marker.getZ()[0])); // Add the beginning marker to make a closed loop
        return coordinates.toArray(new Coordinate[0]);
    }

    // Converts from a coordinate array to a double array
    public static double[][] toDoubleArray(Coordinate[] coordinates) {
        double[] x = new double[coordinates.length];
        double[] z = new double[coordinates.length];
        for (int k = 0; k < coordinates.length; k++) {
            x[k] = coordinates[k].x;
            z[k] = coordinates[k].y;
        }
        return new double[][] { x, z };
    }

    // Stringify
    @Override
    public String toString() {
        return "DPSMarker{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", world='" + world + '\'' +
                ", x=" + Arrays.toString(x) +
                ", z=" + Arrays.toString(z) +
                ", geometry=" + geometry +
                ", layer=" + layer +
                '}';
    }

}
