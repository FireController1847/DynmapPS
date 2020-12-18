package com.firecontroller1847.dynmapps;

import net.sacredlabyrinth.Phaed.PreciousStones.PreciousStones;
import net.sacredlabyrinth.Phaed.PreciousStones.field.Field;
import net.sacredlabyrinth.Phaed.PreciousStones.managers.ForceFieldManager;
import net.sacredlabyrinth.phaed.simpleclans.Clan;
import net.sacredlabyrinth.phaed.simpleclans.SimpleClans;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.dynmap.DynmapAPI;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class DynmapPSOLD extends JavaPlugin {

    private DynmapAPI dynmapApi;
    private MarkerAPI markerApi;
    private PreciousStones preciousStonesApi;
    private SimpleClans simpleClansApi;

    private MarkerSet markerSet;
//    private HashMap<String, AreaMarker> markers = new HashMap<>();
    private ArrayList<AreaMarker> markers = new ArrayList<>();

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
        markerApi = dynmapApi.getMarkerAPI();

        // Get PreciousStones
        preciousStonesApi = (PreciousStones) this.getServer().getPluginManager().getPlugin("PreciousStones");
        if (preciousStonesApi == null) {
            this.getLogger().severe("Missing PreciousStones!");
            return;
        }

        // Get SimpleClans (Optional)
        simpleClansApi = (SimpleClans) this.getServer().getPluginManager().getPlugin("SimpleClans");

        // Prepare the marker set
        this.prepareMarkerSet();

        // Update the marker set
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
        markerSet = markerApi.getMarkerSet("preciousstones.fields");
        if (markerSet == null) {
            markerSet = markerApi.createMarkerSet("preciousstones.fields", "Protection Fields", null, false);
        }
        if (markerSet == null) {
            this.getLogger().severe("Error creating the protection field marker set!");
            return;
        }
    }

    // Updates the marker set with all of the fields
    private void updateMarkerSet() {
        ForceFieldManager manager = preciousStonesApi.getForceFieldManager();
        for (World world : this.getServer().getWorlds()) {
            List<Field> fields = manager.getFields("*", world);
            for (Field field : fields) {
                // Prepare Field Info
                String id = String.valueOf(field.getId());
                Location location = field.getBlock().getLocation();
                String name = field.getOwner();
                if (simpleClansApi != null) {
                    boolean teamAllowed = false;
                    for (String allowed : field.getAllAllowed()) {
                        if (allowed.startsWith("c:")) {
                            teamAllowed = true;
                            break;
                        }
                    }
                    if (teamAllowed) {
                        UUID uuid = preciousStonesApi.getPlayerManager().getPlayerEntry(name).getOnlineUUID();
                        Clan clan = simpleClansApi.getClanManager().getClanByPlayerUniqueId(uuid);
                        if (clan != null) {
                            name = "Team " + clan.getName();
                        }
                    }
                }

                // Create Outline
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

                // Create Marker
                AreaMarker marker = markerSet.createAreaMarker(id, name, false, world.getName(), x, z, false);

                markers.add(marker);
            }
        }

        // Combine Conflicting Fields
        for (int i = 1; i < markers.size(); i ++) {
            AreaMarker previous = markers.toArray(new AreaMarker[0])[i - 1];
            AreaMarker current = markers.toArray(new AreaMarker[0])[i];

            // combine them
            ArrayList<Coordinate> coordinates1 = new ArrayList<>();
            for (int j = 0; j < previous.getCornerCount(); j++) {
                coordinates1.add(new Coordinate(previous.getCornerX(j), previous.getCornerZ(j)));
            }
            coordinates1.add(new Coordinate(previous.getCornerX(0), previous.getCornerZ(0)));
            System.out.println("COORDINATES 1: " + coordinates1);
            Polygon poly1 = new GeometryFactory().createPolygon(coordinates1.toArray(new Coordinate[0]));
            System.out.println(poly1.toText());

            ArrayList<Coordinate> coordinates2 = new ArrayList<>();
            for (int j = 0; j < current.getCornerCount(); j++) {
                coordinates2.add(new Coordinate(current.getCornerX(j), current.getCornerZ(j)));
            }
            coordinates2.add(new Coordinate(current.getCornerX(0), current.getCornerZ(0)));
            System.out.println("COORDINATES 2: " + coordinates2);
            Polygon poly2 = new GeometryFactory().createPolygon(coordinates2.toArray(new Coordinate[0]));
            System.out.println(poly2.toText());

            if (previous.getLabel().equalsIgnoreCase(current.getLabel()) && poly1.intersects(poly2)) {
                System.out.println("INTERSECTS!");
                Geometry union = poly1.union(poly2);
                Coordinate[] coordinates = union.getCoordinates();
                System.out.println("COORDINATES 3: " + Arrays.toString(coordinates));
                System.out.println(union.toText());

                double[] x = new double[coordinates.length];
                double[] z = new double[coordinates.length];
                for (int j = 0; j < coordinates.length; j++) {
                    x[j] = coordinates[j].x;
                    z[j] = coordinates[j].y;
                }

                AreaMarker marker = markerSet.createAreaMarker(previous.getMarkerID() + "_" + current.getMarkerID(), previous.getLabel(), false, previous.getWorld(), x, z, false);
                previous.deleteMarker();
                current.deleteMarker();
                markers.set(i, marker);
            }
        }
    }

}
