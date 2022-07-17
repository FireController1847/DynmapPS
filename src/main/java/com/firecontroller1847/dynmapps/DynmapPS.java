package com.firecontroller1847.dynmapps;

import com.firecontroller1847.dynmapps.command.CommandDynmapPS;
import com.firecontroller1847.dynmapps.tabcompleter.TabCompleterDynmapPS;
import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.ForceFieldManager;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.operation.union.UnaryUnionOp;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

// TODO: Add reload command
public class DynmapPS extends FirePlugin {

    // Constants
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

    // Dependent Plugins
    private DynmapAPI dynmapApi;
    private PreciousStones preciousStones;
    private SimpleClans simpleClans;

    // Layers
    private ArrayList<DPSLayer> layers = new ArrayList<>();

    // After Configuration
    @Override
    public boolean onAfterConfiguration() {
        // Load dependent plugins
        dynmapApi = (DynmapAPI) this.loadPlugin("dynmap");
        preciousStones = (PreciousStones) this.loadPlugin("PreciousStones");
        simpleClans = (SimpleClans) this.loadPlugin("SimpleClans");

        // Verify required plugins
        if (dynmapApi == null || preciousStones == null) {
            this.getLogger().severe("Dynmap or PreciousStones dependency is missing!");
            return false;
        }

        // Parse configuration
        if (!this.onConfigReload()) {
            return false;
        }

        // Create the marker sets
        this.createMarkerSets();

        // Register update loop
        int updateLoopDelay = this.getConfig().getInt("rebuild_time");
        if (updateLoopDelay < 5) {
            updateLoopDelay = 5;
        }
        this.addLoop("update", updateLoopDelay * 1000, this::update);

        // Register commands
        PluginCommand cmdDynmapps = Objects.requireNonNull(this.getCommand("dynmapps"));
        cmdDynmapps.setExecutor(new CommandDynmapPS());
        cmdDynmapps.setTabCompleter(new TabCompleterDynmapPS());

        // We have loaded successfully
        return true;
    }

    @Override
    public boolean onConfigReload() {
        // Ensure layers is empty
        layers.clear();

        // Parse layers in the configuration
        List<Map<?, ?>> layersRaw = this.getConfig().getMapList("layers");
        for (Map<?, ?> layerRaw : layersRaw) {
            try {
                DPSLayer layer = DPSLayer.parseLayer(layerRaw);
                layers.add(layer);
            } catch (Exception e) {
                e.printStackTrace();
                this.getLogger().severe("Error parsing configuration. Are your layers set up correctly?");
                return false;
            }
        }

        // Update the marker sets
        this.createMarkerSets();

        // All's well that ends well
        return true;
    }

    // Constructs all of the marker sets
    public void createMarkerSets() {
        MarkerAPI markerApi = dynmapApi.getMarkerAPI();

        // Loop through all layers
        for (DPSLayer layer : layers) {

            // Search for existing marker, and make one if it doesn't exist
            MarkerSet markerSet = markerApi.getMarkerSet(layer.getId());
            if (markerSet == null) {
                markerSet = markerApi.createMarkerSet(layer.getId(), layer.getLabel(), null, false);
            }

            // If the marker set still doesn't exist, there had to have been an error
            if (markerSet == null) {
                this.getLogger().severe("Error creating the protection field marker set!");
                disable();
                return;
            }

            // Set layer priority
            markerSet.setLayerPriority(layer.getPriority());

            // Tie the marker to the layer
            layer.setMarkerSet(markerSet);
        }
    }

    // Updates the markers and marker sets
    // This all happens on a different thread than the main server thread to prevent lag
    public void update() {
        ForceFieldManager manager = preciousStones.getForceFieldManager();
        if (this.getConfig().getBoolean("debug")) {
            this.getLogger().info("Starting " + this.getName() + " rebuild...");
        }
        long startTime = System.nanoTime();

        // Create marker cache
        ArrayList<DPSMarker> markersCache = new ArrayList<>();

        // Loop through all worlds
        for (World world : this.getServer().getWorlds()) {

            List<String> skippedWorlds = this.getConfig().getStringList("skipped_worlds");
            boolean skip = false;
            for (String skippedWorld : skippedWorlds) {
                if (world.getName().equals(skippedWorld)) {
                    skip = true;
                    break;
                }
            }
            if (skip) {
                continue;
            }

            // Loop through all fields
            List<Field> fields = manager.getFields("*", world);
            for (Field field : fields) {
                // Skip certain fields
                if (field.isDisabled() || field.getHidingModule().isHidden()) {
                    continue;
                }

                // Get layer otherwise skip this field
                DPSLayer ourLayer = null;
                for (DPSLayer layer : layers) {
                    if (layer.getFields().contains(field.getSettings().getTitle())) {
                        ourLayer = layer;
                        break;
                    }
                }
                if (ourLayer == null) {
                    continue;
                }

                // SimpleClans support
                String finalName = ourLayer.getDisplay().getName()
                        .replace("%id%", String.valueOf(field.getId()))
                        .replace("%name%", field.getName())
                        .replace("%owner%", field.getOwner())
                        .replace("%title%", field.getSettings().getTitle());
                if (ourLayer.getDisplay().isDetectClans() && simpleClans != null) {
                    // Check if there is a clan allowed on the field
                    String clanTag = null;
                    for (String allowed : field.getAllAllowed()) {
                        if (allowed.startsWith("c:")) {
                            clanTag = allowed.substring(2);
                            break;
                        }
                    }

                    // Attempt to fetch the clan
                    Clan clan = null;
                    if (clanTag != null) {
                        clan = simpleClans.getClanManager().getClan(clanTag);
                    }

                    // Set name
                    if (clan != null) {
                        finalName = ourLayer.getDisplay().getClanName()
                                .replace("%id%", String.valueOf(field.getId()))
                                .replace("%name%", field.getName())
                                .replace("%owner%", field.getOwner())
                                .replace("%title%", field.getSettings().getTitle())
                                .replace("%clan%", clan.getName())
                                .replace("%clan_owner%", clan.getLeaders().get(0).getCleanName())
                                .replace("%clan_description%", clan.getDescription() != null ? clan.getDescription() : "")
                                .replace("%clan_tag%", clan.getTag())
                                .replace("%clan_member_count%", String.valueOf(clan.getMembers().size()));
                    }
                }

                // Create the field outline
                // We add and subtract .5 to ensure that fields go to the edge of blocks
                double[] x = new double[4];
                double[] z = new double[4];
                x[0] = field.getMaxx() + 0.5;
                z[0] = field.getMaxz() + 0.5;
                x[1] = field.getMaxx() + 0.5;
                z[1] = field.getMinz() - 0.5;
                x[2] = field.getMinx() - 0.5;
                z[2] = field.getMinz() - 0.5;
                x[3] = field.getMinx() - 0.5;
                z[3] = field.getMaxz() + 0.5;

                // Create the marker and add to cache
                markersCache.add(new DPSMarker(String.valueOf(field.getId()), finalName, world.getName(), x, z, ourLayer));
            }

        }

        int prevcount = markersCache.size();

        // Skip even checking for combinations if all layers have it disabled
        int count = 0;
        for (DPSLayer layer : layers) {
            if (!layer.isCombine()) {
                count++;
            }
        }
        if (count != layers.size()) {
            combine(markersCache);
        }

        // Empty all of the old marker sets
        for (DPSLayer layer : layers) {
            for (AreaMarker marker : layer.getMarkerSet().getAreaMarkers()) {
                marker.deleteMarker();
            }
        }

        // Create the markers
        for (DPSMarker dpsMarker : markersCache) {
            DPSLayer layer = dpsMarker.getLayer();
            MarkerSet set = layer.getMarkerSet();
            AreaMarker marker = set.createAreaMarker(dpsMarker.getId(), dpsMarker.getLabel(), false, dpsMarker.getWorld(), dpsMarker.getX(), dpsMarker.getZ(), false);
            if (marker != null) {
                marker.setFillStyle(layer.getStyle().getFillOpacity(), Integer.parseInt(layer.getStyle().getFillColor().replace("#", ""), 16));
                marker.setLineStyle(layer.getStyle().getStrokeWeight(), layer.getStyle().getStrokeOpacity(), Integer.parseInt(layer.getStyle().getStrokeColor().replace("#", ""), 16));
            }
        }

        // Determine how long it took
        long endTime = System.nanoTime();
        if (this.getConfig().getBoolean("debug")) {
            this.getLogger().info("Rebuild complete. Took " + (endTime - startTime) / 1000000 + "ms. Combined " + prevcount + " fields down into " + markersCache.size() + " markers.");
        }

        // Collect garbage because we generated a lot
        System.gc();
    }

    // Combines all of the markers in the given array
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
    private void combine(ArrayList<DPSMarker> markers) {
        try {
            for (int i = 0; i < markers.size(); i++) {
                DPSMarker outer = markers.get(i);

                // Begin Inner Loop
                boolean intersected = false;
                for (int j = 0; j < markers.size(); j++) {
                    DPSMarker inner = markers.get(j);

                    // Prevent checking the same thing
                    if (outer.equals(inner)) {
                        continue;
                    }

                    // If the labels or the world don't match, do not combine
                    if (!outer.getLabel().equals(inner.getLabel()) || !outer.getWorld().equals(inner.getWorld())) {
                        continue;
                    }

                    // If the layers do not match, do not combine
                    // No need to check IDs when the objects should be the same
                    if (!outer.getLayer().equals(inner.getLayer())) {
                        continue;
                    }

                    // If the layer is set to not combine, do not combine
                    // NOTE: We don't need to check inner because we just checked to ensure the layers are the same
                    if (!outer.getLayer().isCombine()) {
                        continue;
                    }

                    // If the polygons do not intersect, do not combine
                    if (!outer.getGeometry().intersects(inner.getGeometry())) {
                        continue;
                    }

                    // Union the polygons
                    Geometry result = UnaryUnionOp.union(GEOMETRY_FACTORY.createGeometryCollection(new Geometry[] { outer.getGeometry(), inner.getGeometry() }));

                    // Ingore non-polygons
                    if (!(result instanceof Polygon)) {
                        continue;
                    }

                    // If the geometry has holes, take the intersection instead of the union
                    // Doesn't really combine more-so fill in the hole so it doesn't overlap
                    if (((Polygon) result).getNumInteriorRing() > 0) {
                        result = inner.getGeometry().difference(outer.getGeometry());
                        double[][] doubleArray = DPSMarker.toDoubleArray(result.getCoordinates());
                        double[] x = doubleArray[0];
                        double[] z = doubleArray[1];
                        inner.setGeometry(result);
                        inner.setX(x);
                        inner.setZ(z);
                        continue;
                    }

                    // Convert coordinates
                    double[][] doubleArray = DPSMarker.toDoubleArray(result.getCoordinates());
                    double[] x = doubleArray[0];
                    double[] z = doubleArray[1];

                    // Update Outer
                    outer.setX(x);
                    outer.setZ(z);
                    outer.setGeometry(result);

                    // Remove the unioned polygon
                    markers.remove(inner);

                    // We did intersect so "return true"
                    intersected = true;

                    // Now that outer is updated, run this loop with j - 1 because we removed an inner polygon
                    --j;
                }

                // Update the cache
                markers.set(i, outer);

                // Re-loop if we intersected to try and find any more intersections
                if (intersected) {
                    --i;
                }
            }
        } catch (Exception e) {
            if (this.getConfig().getBoolean("debug")) {
                e.printStackTrace();
            }
            this.getLogger().warning("Internal error when attempting to combine a marker.");
        }
    }

}
