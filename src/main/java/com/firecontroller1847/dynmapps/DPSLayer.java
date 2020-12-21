package com.firecontroller1847.dynmapps;

import org.dynmap.markers.MarkerSet;

import java.util.List;
import java.util.Map;

public class DPSLayer {

    // Variables
    private String id;
    private String label;
    private List<String> fields;
    private boolean combine;
    private int priority;
    private DPSLayerDisplay display;
    private DPSLayerStyle style;
    private MarkerSet markerSet; // The marker set for this DPS layer

    // Constructor
    public DPSLayer(String id, String label, List<String> fields, boolean combine, int priority, DPSLayerDisplay display, DPSLayerStyle style) {
        this.id = id;
        this.label = label;
        this.fields = fields;
        this.combine = combine;
        this.priority = priority;
        this.display = display;
        this.style = style;
    }
    private DPSLayer() { }

    // Getters
    public String getId() {
        return id;
    }
    public String getLabel() {
        return label;
    }
    public List<String> getFields() {
        return fields;
    }
    public boolean isCombine() {
        return combine;
    }
    public int getPriority() {
        return priority;
    }
    public DPSLayerDisplay getDisplay() {
        return display;
    }
    public DPSLayerStyle getStyle() {
        return style;
    }
    public MarkerSet getMarkerSet() {
        return markerSet;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }
    public void setLabel(String label) {
        this.label = label;
    }
    public void setFields(List<String> fields) {
        this.fields = fields;
    }
    public void setCombine(boolean combine) {
        this.combine = combine;
    }
    public void setPriority(int priority) {
        this.priority = priority;
    }
    public void setDisplay(DPSLayerDisplay display) {
        this.display = display;
    }
    public void setStyle(DPSLayerStyle style) {
        this.style = style;
    }
    public void setMarkerSet(MarkerSet markerSet) {
        this.markerSet = markerSet;
    }

    // Attempts to parse a layer from a map
    @SuppressWarnings("unchecked")
    public static DPSLayer parseLayer(Map<?, ?> map) throws Exception {
        DPSLayer layer = new DPSLayer();
        try {
            layer.id = (String) map.get("id");
            layer.label = (String) map.get("label");
            layer.fields = (List<String>) map.get("fields");
            layer.combine = (Boolean) map.get("combine");
            layer.priority = Integer.parseInt(map.get("priority").toString());
            layer.display = DPSLayerDisplay.parseLayerStyle((Map<?, ?>) map.get("display"));
            layer.style = DPSLayerStyle.parseLayerStyle((Map<?, ?>) map.get("style"));
        } catch (Exception e) {
            throw new Exception("Invalid configuration!", e);
        }
        return layer;
    }

    // Stringify
    @Override
    public String toString() {
        return "DPSLayer{" +
                "id='" + id + '\'' +
                ", label='" + label + '\'' +
                ", fields=" + fields +
                ", combine=" + combine +
                ", priority=" + priority +
                ", display=" + display +
                ", style=" + style +
                ", markerSet=" + markerSet +
                '}';
    }

}
