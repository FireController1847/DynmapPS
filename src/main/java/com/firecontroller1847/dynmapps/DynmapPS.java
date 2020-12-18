package com.firecontroller1847.dynmapps;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.ForceFieldManager;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

public class DynmapPS extends JavaPlugin {

    // Plugins
    private DynmapAPI dynmapApi;
    private PreciousStones preciousStones;
    private SimpleClans simpleClans;

    // Variables
    private MarkerSet markerSet; // The set of markers to use on Dynmap
    private ArrayList<AreaMarker> markers = new ArrayList<>(); // A cache of AreaMarkers

    // On Enable
    @Override
    public void onEnable() {
        // Setup Configuration
        this.saveDefaultConfig();

        // Get Dynmap
        dynmapApi = (DynmapAPI) this.getServer().getPluginManager().getPlugin("dynmap");
        if (dynmapApi == null) {
            this.getLogger().severe("Missing Dynmap!");
            return;
        }

        // Get PreciousStones
        preciousStones = (PreciousStones) this.getServer().getPluginManager().getPlugin("PreciousStones");
        if (preciousStones == null) {
            this.getLogger().severe("Missing PreciousStones!");
            return;
        }

        // Get SimpleClans (Optional)
        simpleClans = (SimpleClans) this.getServer().getPluginManager().getPlugin("SimpleClans");

        // Prepare Marker Set
        this.prepareMarkerSet();

        // TODO: Actually update the markers...
        this.updateMarkerSet();

        // Log Enabled
        this.getLogger().info("Enabled " + this.getName() + "!");
    }

    // On Disable
    @Override
    public void onDisable() {
        // Log Disabled
        this.getLogger().info("Disabled " + this.getName() + "!");
    }

    // Initialize the PreciousStones marker set
    private void prepareMarkerSet() {
        MarkerAPI markerApi = dynmapApi.getMarkerAPI();
        markerSet = markerApi.getMarkerSet("preciousstones.fields");
        if (markerSet == null) {
            markerSet = markerApi.createMarkerSet("preciousstones.fields", "Protection Fields", null, false);
        }
        if (markerSet == null) {
            this.getLogger().severe("Error creating the protection field marker set!");
            return;
        }
    }

    // Update the marker set
    private void updateMarkerSet() {
        ForceFieldManager manager = preciousStones.getForceFieldManager();

        // Empty old marker set
        for (AreaMarker marker : markerSet.getAreaMarkers()) {
            marker.deleteMarker();
        }
        markers.clear();

        // Create marker cache
        ArrayList<DPSMarker> markersCache = new ArrayList<>();

        // Loop through all worlds
        for (World world : this.getServer().getWorlds()) {

            // Loop through all fields
            List<Field> fields = manager.getFields("*", world);
            for (Field field : fields) {
                // Prepare field information
                String id = String.valueOf(field.getId());
                String name = field.getOwner();

                // Add SimpleClans support
                if (simpleClans != null) {
                    // Check if there is a clan allowed on the field
                    String clanTag = null;
                    for (String allowed : field.getAllAllowed()) {
                        if (allowed.startsWith("c:")) {
                            clanTag = allowed.substring(2);
                            break;
                        }
                    }

                    // Attempt to fetch the clan
                    if (clanTag != null) {
                        Clan clan = simpleClans.getClanManager().getClan(clanTag);
                        if (clan != null) {
                            // Set the name to the clan name
                            name = "Clan " + clan.getName();
                        }
                    }
                }

                // Create the field outline
                double[] x = new double[4];
                double[] z = new double[4];
                x[0] = field.getMaxx();
                z[0] = field.getMaxz();
                x[1] = field.getMaxx();
                z[1] = field.getMinz() + 0.0;
                x[2] = field.getMinx() + 0.0;
                z[2] = field.getMinz() + 0.0;
                x[3] = field.getMinx() + 0.0;
                z[3] = field.getMaxz();

                // Create the marker and add to cache
                markersCache.add(new DPSMarker(id, name, world.getName(), x, z));
            }

        }

        /*
         * Primary Combination Loop
         *
         * For every marker, loop through every other marker and determine whether or not it can be combined.
         * If it can be combined, replace the current marker with the combination. Continue doing so until there are no more markers to check
         * Once all possible combinations are combined, move on to the next marker which may have possible combinations
         *
         * Due to the removal of markers, we have to re-iterate over multiple times every time one is removed to ensure all
         * markers are accounted for. If we determined that we have combined with another marker, we also need to re-iterate
         * through the outer loop to ensure any other possible combinations are accounted for.
         *
         * This method is probably not even remotely close to the most efficient way to do this, but this is what I was
         * able to come up with and I'm quite proud of it. If you can improve it without breaking the logic, go for it.
         */
        this.getLogger().info("Starting " + this.getName() + " rebuild...");
        long startTime = System.nanoTime();
        int prevcount = markersCache.size();
        for (int i = 0; i < markersCache.size(); i++) {
            DPSMarker outer = markersCache.get(i);

            boolean insersection = false;
            for (int j = 0; j < markersCache.size(); j++) {
                DPSMarker inner = markersCache.get(j);

                // If they are the same, don't check
                if (outer.equals(inner)) {
                    continue;
                }

                // If the labels and the world don't match, don't check
                if (!outer.getLabel().equals(inner.getLabel()) || !outer.getWorld().equals(inner.getWorld())) {
                    continue;
                }

                // Create Polygons
                Polygon outerPolygon = outer.getPolygon();
                Polygon innerPolygon = inner.getPolygon();

                // If the markers do not intersect, do not combine them
                if (!outerPolygon.intersects(innerPolygon)) {
                    continue;
                }

                // Union the Polygons
                Geometry unionPolygon = outerPolygon.union(innerPolygon);
                Coordinate[] unionCoordinates = unionPolygon.getCoordinates();

                // Convert Coordinates to Marker Format
                double[] x = new double[unionCoordinates.length];
                double[] z = new double[unionCoordinates.length];
                for (int k = 0; k < unionCoordinates.length; k++) {
                    x[k] = unionCoordinates[k].x;
                    z[k] = unionCoordinates[k].y;
                }

                // Create New Marker
                DPSMarker unionMarker = new DPSMarker(outer.getId() + "_" + inner.getId(), outer.getLabel(), outer.getWorld(), x, z);

                // Remove inner marker so it does not get counted as an outer marker
                markersCache.remove(inner);

                // Set us back a number so we re-loop with the new size
                j--;

                // Set Outer Marker to Combination
                outer = unionMarker;

                // Set intersection to true
                insersection = true;
            }

            // If we intersected, run this one again
            if (insersection) {
                markersCache.set(i, outer);
                i--;
            }
        }

        // Create the markers
        for (DPSMarker dpsMarker : markersCache) {
            markers.add(this.createAreaMarker(dpsMarker.getId(), dpsMarker.getLabel(), dpsMarker.getWorld(), dpsMarker.getX(), dpsMarker.getZ()));
        }

        // Determine how long it took
        long endTime = System.nanoTime();
        this.getLogger().info("Rebuild complete. Took " + (endTime - startTime) / 1000000 + "ms. Combined " + prevcount + " fields down into " + markers.size() + " markers.");

        // Collect garbage because we generated a lot
        System.gc();
    }

    // Utility method to create a marker
    private AreaMarker createAreaMarker(String id, String label, String world, double[] x, double[] z) {
        return markerSet.createAreaMarker(id, label, false, world, x, z, false);
    }

}
