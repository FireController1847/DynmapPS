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
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.List;

public class DynmapPS extends JavaPlugin {

    // Constants
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(1));

    // Plugins
    private DynmapAPI dynmapApi;
    private PreciousStones preciousStones;
    private SimpleClans simpleClans;

    // Variables
    private MarkerSet markerSet; // The set of markers to use on Dynmap
    private Thread updateThread; // The thread which loops for rebuilding

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
        this.updateLoop();

        // Log Enabled
        this.getLogger().info("Enabled " + this.getName() + "!");
    }

    // On Disable
    @Override
    public void onDisable() {
        if (updateThread != null) {
            updateThread.interrupt();
        }

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

    // The update loop
    private void updateLoop() {
        // If the thread already exists, but the method is called from a
        // different thread (therefore making a new one), interrupt the old thread.
        if (updateThread != null && Thread.currentThread() != updateThread && updateThread.isAlive()) {
            updateThread.interrupt();
        }

        // Create the thread and start it
        updateThread = new Thread(() -> {
            // Do work
            this.updateMarkerSet();

            // Wait patiently
            int time = this.getConfig().getInt("update_time");
            if (time < 2) {
                time = 2;
            }

            // Sleep for "time" and return if interrupted
            try {
                Thread.sleep(time * 1000);
            } catch (InterruptedException e) {
                return;
            }

            // Call next loop
            updateLoop();
        });
        updateThread.start();
    }

    // Update the marker set
    private void updateMarkerSet() {
        ForceFieldManager manager = preciousStones.getForceFieldManager();

        // Create marker cache
        ArrayList<DPSMarker> markersCache = new ArrayList<>();

        // Loop through all worlds
        for (World world : this.getServer().getWorlds()) {

            // Loop through all fields
            List<Field> fields = manager.getFields("*", world);
            for (Field field : fields) {
                // Skip certain fields
                if (field.isDisabled() || field.getHidingModule().isHidden()) {
                    continue;
                }

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
         *
         * NOTE: If you ever need to debug something, the JTS TestBuilder is the way to go. Log out the polygons using .toText() and put them into the builder.
         */
        // TODO: This logic needs to be simplified somehow, someway. Either that, or we figure out a solution for the > 1s time it takes.
        this.getLogger().info("Starting " + this.getName() + " rebuild...");
        long startTime = System.nanoTime();
        int prevcount = markersCache.size();

        // Begin Combination Loop
        try {
            for (int i = 0; i < markersCache.size(); i++) {
                DPSMarker outer = markersCache.get(i);

                // Begin Inner Loop
                boolean intersected = false;
                for (int j = 0; j < markersCache.size(); j++) {
                    DPSMarker inner = markersCache.get(j);

                    // Prevent checking the same thing
                    if (outer.equals(inner)) {
                        continue;
                    }

                    // If the labels or the world don't match, don't check
                    if (!outer.getLabel().equals(inner.getLabel()) || !outer.getWorld().equals(inner.getWorld())) {
                        continue;
                    }

                    // Check to ensure the polygons do intersect
                    if (!outer.getGeometry().intersects(inner.getGeometry())) {
                        continue;
                    }

                    // Union the polygons
                    Geometry union = UnaryUnionOp.union(GEOMETRY_FACTORY.createGeometryCollection(new Geometry[] { outer.getGeometry(), inner.getGeometry() }));

                    // Ingore non-polygons
                    if (!(union instanceof Polygon)) {
                        continue;
                    }

                    // If the geometry has holes, continue
                    if (((Polygon) union).getNumInteriorRing() > 0) {
                        continue;
                    }

                    // Convert coordinates
                    Coordinate[] coordinates = union.getCoordinates();
                    double[] x = new double[coordinates.length];
                    double[] z = new double[coordinates.length];
                    for (int k = 0; k < coordinates.length; k++) {
                        x[k] = coordinates[k].x;
                        z[k] = coordinates[k].y;
                    }

                    // Update Outer
                    outer.setX(x);
                    outer.setZ(z);
                    outer.setGeometry(union);

                    // Remove the unioned polygon
                    markersCache.remove(inner);

                    // We did intersect so "return true"
                    intersected = true;

                    // Now that outer is updated, run this loop with j - 1 because we removed an inner polygon
                    --j;
                }

                // Update the cache
                markersCache.set(i, outer);

                // Re-loop if we intersected to try and find any more intersections
                if (intersected) {
                    --i;
                }
            }
        } catch (Exception e) {
            e.printStackTrace(); // TODO: debug option
        }

        // Empty old marker set
        for (AreaMarker marker : markerSet.getAreaMarkers()) {
            marker.deleteMarker();
        }

        // Create the markers
        for (DPSMarker dpsMarker : markersCache) {
            this.createAreaMarker(dpsMarker.getId(), dpsMarker.getLabel(), dpsMarker.getWorld(), dpsMarker.getX(), dpsMarker.getZ());
        }

        // Determine how long it took
        long endTime = System.nanoTime();
        this.getLogger().info("Rebuild complete. Took " + (endTime - startTime) / 1000000 + "ms. Combined " + prevcount + " fields down into " + markerSet.getAreaMarkers().size() + " markers.");

        // Collect garbage because we generated a lot
        System.gc();
    }

    // Utility method to create a marker
    private AreaMarker createAreaMarker(String id, String label, String world, double[] x, double[] z) {
        return markerSet.createAreaMarker(id, label, false, world, x, z, false);
    }

}
