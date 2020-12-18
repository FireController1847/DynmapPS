package com.firecontroller1847.dynmapps;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;

public class DPSMarker {

    // Variables
    private String id;
    private String label;
    private String world;
    private double[] x;
    private double[] z;
    private Polygon polygon;

    // Dynamic Variables

    // Constructor
    public DPSMarker(String id, String label, String world, double[] x, double[] z) {
        this.id = id;
        this.label = label;
        this.world = world;
        this.x = x;
        this.z = z;
        this.polygon = toPolygon();
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
    public Polygon getPolygon() {
        return polygon;
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
    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    // Utility method to convert from a DPSMarker to a Polygon
    public Polygon toPolygon() {
        return new GeometryFactory().createPolygon(toCoordinates(this));
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

}
