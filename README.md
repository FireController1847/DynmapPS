# DynmapPS [![Build Status](https://img.shields.io/github/actions/workflow/status/FireController1847/DynmapPS/maven.yml)](https://github.com/FireController1847/DynmapPS/actions) [![Latest Release](https://img.shields.io/github/v/release/FireController1847/DynmapPS)](https://github.com/FireController1847/DynmapPS/releases) [![Download](https://img.shields.io/github/downloads/FireController1847/DynmapPS/total?color=%234C1)](https://github.com/FireController1847/DynmapPS/releases) [![Issues](https://img.shields.io/github/issues/FireController1847/DynmapPS)](https://github.com/FireController1847/DynmapPS/issues)

Show your PreciousStones on your Dynmap for your Minecraft server, with support for SimpleClans.

[See this plugin on Hangar](https://hangar.papermc.io/FireController1847/DynmapPS/)  
[See this plugin on SpigotMC](https://www.spigotmc.org/resources/dynmapps.86957/)

## Commands
- /dynmapps or /dps - The primary command. Lists other commands
- /dps reload - Reloads the configuration
- /dps forceupdate - Forces a map update

## Permissions
### Star Permissions
- dynmapps.*
- dynmapps.command.*

### Regular Permissions
- dynmapps.command
- dynmapps.command.reload
- dynmapps.command.forceupdate

## Default Configuration
```yml
####################
#     Language     #
####################
# What language you wish to use.
# The name of the file in the lang/ folder to use for configuration.
# Default is en which results in lang/en.yml
rebuild_time: 60

#################
#     Debug     #
#################
# Whether or not to log debug information
debug: false

##################
#     Worlds     #
##################
# The names of the worlds that should not be included
skipped_worlds:
  - "world_creative"

##################
#     Layers     #
##################
# The layers to be used for your PreciousStones
#
#  layer:
#   id: The id of this layer that will be used in dynmap (changes take effect after restart)
#   label: The name of the layer that will show on dynmap (changes take effect after restart)
#   fields: A list of fields to include in this layer. Relates to the "title" option on PreciousStones
#   combine: Whether or not to combine overlapping fields into one marker (prevents a map mess, but can improve build time)
#   priority: The order in which fields should show up on the layers menu (not actual in-map layering)
#   display: Options for the naming of the markers
#     detect_clans: When a clan is detected, use clan_name instead of name
#     name: The name for every marker. See language file for supported variables
#     clan_name: The name for every clan. See language file for supported variables
#   style: Settings for the style of a layer
#     fill_color: The color that markers should be filled in with
#     fill_opacity: The opacity of the fill (1.0 to 0.0)
#     stroke_color: The color of the outline of each marker
#     stroke_opacity: The opacity of the outline (1.0 to 0.0)
#     stroke_weight: How thick the stroke of the marker is in pixels
layers:
  - id: protection_fields
    label: "Protection Fields"
    fields:
      - "protection"
      - "repellent"
    combine: true
    priority: 1
    display:
      detect_clans: true
      name: "%owner%"
      clan_name: "Clan %clan%"
    style:
      fill_color: "#FFFFFF"
      fill_opacity: 0.2
      stroke_color: "#000000"
      stroke_opacity: 1.0
      stroke_weight: 1
  - id: force_fields
    label: "Force Fields"
    fields:
      - "force"
    priority: 2
    combine: false
    display:
      detect_clans: false
      name: "%owner%"
      clan_name: "Clan %clan%"
    style:
      fill_color: "#CC3333"
      fill_opacity: 0.2
      stroke_color: "#CC3333"
      stroke_opacity: 1.0
      stroke_weight: 2
```
