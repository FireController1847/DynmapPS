package com.firecontroller1847.dynmapps;

import java.util.Map;

public class DPSLayerStyle {

    // Variables
    private String fillColor;
    private double fillOpacity;
    private String strokeColor;
    private double strokeOpacity;
    private int strokeWeight;

    // Constructor
    public DPSLayerStyle(String fillColor, double fillOpacity, String strokeColor, double strokeOpacity, int strokeWeight) {
        this.fillColor = fillColor;
        this.fillOpacity = fillOpacity;
        this.strokeColor = strokeColor;
        this.strokeOpacity = strokeOpacity;
        this.strokeWeight = strokeWeight;
    }
    private DPSLayerStyle() { }

    // Getters
    public String getFillColor() {
        return fillColor;
    }
    public double getFillOpacity() {
        return fillOpacity;
    }
    public String getStrokeColor() {
        return strokeColor;
    }
    public double getStrokeOpacity() {
        return strokeOpacity;
    }
    public int getStrokeWeight() {
        return strokeWeight;
    }

    // Setters
    public void setFillColor(String fillColor) {
        this.fillColor = fillColor;
    }
    public void setFillOpacity(double fillOpacity) {
        this.fillOpacity = fillOpacity;
    }
    public void setStrokeColor(String strokeColor) {
        this.strokeColor = strokeColor;
    }
    public void setStrokeOpacity(double strokeOpacity) {
        this.strokeOpacity = strokeOpacity;
    }
    public void setStrokeWeight(int strokeWeight) {
        this.strokeWeight = strokeWeight;
    }

    // Attempts to parse a layer style from a map
    public static DPSLayerStyle parseLayerStyle(Map<?,?> map) throws Exception {
        DPSLayerStyle style = new DPSLayerStyle();
        try {
            style.fillColor = (String) map.get("fill_color");
            style.fillOpacity = Double.parseDouble(map.get("fill_opacity").toString());
            style.strokeColor = (String) map.get("stroke_color");
            style.strokeOpacity = Double.parseDouble(map.get("stroke_opacity").toString());
            style.strokeWeight = Integer.parseInt(map.get("stroke_weight").toString());
        } catch (Exception e) {
            throw new Exception("Invalid configuration!", e);
        }
        return style;
    }

    // Stringify
    @Override
    public String toString() {
        return "DPSLayerStyle{" +
                "fillColor='" + fillColor + '\'' +
                ", fillOpacity=" + fillOpacity +
                ", strokeColor='" + strokeColor + '\'' +
                ", strokeOpacity=" + strokeOpacity +
                ", strokeWeight=" + strokeWeight +
                '}';
    }

}
