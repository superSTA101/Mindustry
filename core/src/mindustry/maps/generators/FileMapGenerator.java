package mindustry.maps.generators;

import arc.math.*;
import arc.math.geom.*;
import mindustry.content.*;
import mindustry.game.*;
import mindustry.io.*;
import mindustry.maps.*;
import mindustry.type.*;
import mindustry.world.*;
import mindustry.world.blocks.storage.*;

import static mindustry.Vars.*;

public class FileMapGenerator implements WorldGenerator{
    public final Map map;
    public final SectorPreset preset;

    public FileMapGenerator(String mapName, SectorPreset preset){
        this.map = maps != null ? maps.loadInternalMap(mapName) : null;
        this.preset = preset;
    }

    public FileMapGenerator(Map map, SectorPreset preset){
        this.map = map;
        this.preset = preset;
    }

    /** If you use this constructor, make sure to override generate()! */
    public FileMapGenerator(SectorPreset preset){
        this(emptyMap, preset);
    }

    @Override
    public void generate(Tiles tiles){
        if(map == null) throw new RuntimeException("Generator has null map, cannot be used.");

        world.setGenerating(false);
        SaveIO.load(map.file, world.filterContext(map));
        world.setGenerating(true);

        tiles = world.tiles;

        //TODO why is this hardcoded into the map generator
        Item[] items = {Items.blastCompound, Items.pyratite, Items.copper, Items.thorium, Items.copper, Items.lead};

        for(Tile tile : tiles){
            if(tile.block() instanceof StorageBlock && !(tile.block() instanceof CoreBlock) && state.hasSector()){
                for(Item content : items){
                    if(Mathf.chance(0.2)){
                        tile.build.items.add(content, Math.min(Mathf.random(500), tile.block().itemCapacity));
                    }
                }
            }
        }

        boolean anyCores = false;

        for(Tile tile : tiles){

            if(tile.overlay() == Blocks.spawn){
                int rad = 10;
                Geometry.circle(tile.x, tile.y, tiles.width, tiles.height, rad, (wx, wy) -> {
                    if(tile.overlay().itemDrop != null){
                        tile.clearOverlay();
                    }
                });
            }

            if(tile.isCenter() && tile.block() instanceof CoreBlock && tile.team() == state.rules.defaultTeam && !anyCores){
                Schematics.placeLaunchLoadout(tile.x, tile.y);
                anyCores = true;

                if(preset.addStartingItems){
                    tile.build.items.add(state.rules.loadout);
                }
            }
        }

        if(!anyCores){
            throw new IllegalArgumentException("All maps must have a core.");
        }

        state.map = map;
    }
}
