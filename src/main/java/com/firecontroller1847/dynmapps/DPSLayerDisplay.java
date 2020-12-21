package com.firecontroller1847.dynmapps;

import java.util.Map;

public class DPSLayerDisplay {

    // Variables
    private boolean detectClans;
    private String name;
    private String clanName;

    // Constructor
    public DPSLayerDisplay(boolean detectClans, String name, String clanName) {
        this.detectClans = detectClans;
        this.name = name;
        this.clanName = clanName;
    }
    private DPSLayerDisplay() { }

    // Getters
    public boolean isDetectClans() {
        return detectClans;
    }
    public String getName() {
        return name;
    }
    public String getClanName() {
        return clanName;
    }

    // Setters
    public void setDetectClans(boolean detectClans) {
        this.detectClans = detectClans;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setClanName(String clanName) {
        this.clanName = clanName;
    }

    // Attempts to parse a layer style from a map
    public static DPSLayerDisplay parseLayerStyle(Map<?,?> map) throws Exception {
        DPSLayerDisplay display = new DPSLayerDisplay();
        try {
            display.detectClans = (Boolean) map.get("detect_clans");
            display.name = (String) map.get("name");
            display.clanName = (String) map.get("clan_name");
        } catch (Exception e) {
            throw new Exception("Invalid configuration!", e);
        }
        return display;
    }

    // Stringify
    @Override
    public String toString() {
        return "DPSLayerDisplay{" +
                "detectClans=" + detectClans +
                ", name='" + name + '\'' +
                ", clanName='" + clanName + '\'' +
                '}';
    }

}
