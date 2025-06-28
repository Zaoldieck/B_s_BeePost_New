# B_s_BeePost_New

## Overview

This plugin adds bee-related features to Minecraft servers.

## Changes in this fork (Minecraft 1.20.1 compatibility and SkyBlock improvements)

This fork includes updates to make the plugin fully compatible with Minecraft 1.20.1 and improve its behavior on SkyBlock servers.

### Detailed changes:

- Updated Java and Maven configurations for Java 17 compatibility, as required by Minecraft 1.20.1.
- Replaced deprecated Bukkit particle constants (`WHITE_SMOKE` → `SMOKE_NORMAL`, `DUST` → `REDSTONE`) to fix compilation errors.
- Adjusted bee spawn logic to reduce spawn radius and increase vertical offset, making it more suitable for small SkyBlock islands.
- Modified distance checks for bees to prevent premature disappearance on limited-space maps.
- Improved scheduling and task management for bee behaviors.
- General code cleanup and optimizations to maintain original functionality.

## How to build

To build the plugin jar, run:

```bash
mvn clean package
