package io.anuke.mindustry.world.blocks.distribution;

import io.anuke.arc.Core;
import io.anuke.arc.graphics.g2d.Draw;
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.math.geom.Geometry;
import io.anuke.arc.math.geom.Vector2;
import io.anuke.arc.util.Time;
import io.anuke.mindustry.entities.type.TileEntity;
import io.anuke.mindustry.entities.type.Unit;
import io.anuke.mindustry.graphics.Layer;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.world.Block;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.meta.BlockGroup;
import io.anuke.mindustry.world.meta.BlockStat;
import io.anuke.mindustry.world.meta.StatUnit;

import static io.anuke.mindustry.Vars.tilesize;

public class Conveyor extends Block{
    private TextureRegion[][] regions = new TextureRegion[7][4];
    protected float speed = 0f;

    protected Conveyor(String name){
        super(name);
        rotate = true;
        update = true;
        layer = Layer.overlay;
        group = BlockGroup.transportation;
        hasItems = true;
        itemCapacity = 4;
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(BlockStat.itemsMoved, speed * 60, StatUnit.itemsSecond);
    }

    @Override
    public void load(){
        super.load();

        for(int i = 0; i < regions.length; i++){
            for(int j = 0; j < 4; j++){
                regions[i][j] = Core.atlas.find(name + "-" + i + "-" + j);
            }
        }
    }

    @Override
    public void draw(Tile tile){
        ConveyorEntity entity = tile.entity();
        byte rotation = tile.getRotation();

        int frame = entity.clogHeat <= 0.5f ? (int) (((Time.time() * speed * 8f * entity.timeScale)) % 4) : 0;
        Draw.rect(regions[Mathf.clamp(entity.blendbits, 0, regions.length - 1)][Mathf.clamp(frame, 0, regions[0].length - 1)], tile.drawx(), tile.drawy(),
            tilesize * entity.blendsclx, tilesize * entity.blendscly,  rotation*90);
    }

    @Override
    public void onProximityUpdate(Tile tile){
        super.onProximityUpdate(tile);

        ConveyorEntity entity = tile.entity();
        entity.blendbits = 0;
        entity.blendsclx = entity.blendscly = 1;
        entity.blendshadowrot = -1;

        if(blends(tile, 2) && blends(tile, 1) && blends(tile, 3)){
            entity.blendbits = 3;
        }else if(blends(tile, 1) && blends(tile, 3)){
            entity.blendbits = 4;
        }else if(blends(tile, 1) && blends(tile, 2)){
            entity.blendbits = 2;
        }else if(blends(tile, 3) && blends(tile, 2)){
            entity.blendbits = 2;
            entity.blendscly = -1;
        }else if(blends(tile, 1)){
            entity.blendbits = 1;
            entity.blendscly = -1;
            entity.blendshadowrot = 0;
        }else if(blends(tile, 3)){
            entity.blendbits = 1;
            entity.blendshadowrot = 1;
        }
    }

    private boolean blends(Tile tile, int direction){
        Tile other = tile.getNearby(Mathf.mod(tile.getRotation() - direction, 4));
        if(other != null) other = other.target();

        return other != null && other.block().outputsItems()
        && ((tile.getNearby(tile.getRotation()) == other) || (!other.block().rotate || other.getNearby(other.getRotation()) == tile));
    }

    @Override
    public TextureRegion[] generateIcons(){
        return new TextureRegion[]{Core.atlas.find(name + "-0-0")};
    }

    @Override
    public void drawLayer(Tile tile){

    }

    @Override
    public void unitOn(Tile tile, Unit unit){
        ConveyorEntity entity = tile.entity();

        entity.noSleep();

        float speed = this.speed * tilesize / 2.3f;
        float centerSpeed = 0.1f;
        float centerDstScl = 3f;
        float tx = Geometry.d4[tile.getRotation()].x, ty = Geometry.d4[tile.getRotation()].y;

        float centerx = 0f, centery = 0f;

        if(Math.abs(tx) > Math.abs(ty)){
            centery = Mathf.clamp((tile.worldy() - unit.y) / centerDstScl, -centerSpeed, centerSpeed);
            if(Math.abs(tile.worldy() - unit.y) < 1f) centery = 0f;
        }else{
            centerx = Mathf.clamp((tile.worldx() - unit.x) / centerDstScl, -centerSpeed, centerSpeed);
            if(Math.abs(tile.worldx() - unit.x) < 1f) centerx = 0f;
        }

        unit.velocity().add((tx * speed + centerx) * entity.delta(), (ty * speed + centery) * entity.delta());
    }

    @Override
    public void update(Tile tile){

    }

    @Override
    public boolean isAccessible(){
        return true;
    }

    @Override
    public int removeStack(Tile tile, Item item, int amount){
        return 0;
    }

    @Override
    public void getStackOffset(Item item, Tile tile, Vector2 trns){
        trns.trns(tile.getRotation() * 90 + 180f, tilesize / 2f);
    }

    @Override
    public int acceptStack(Item item, int amount, Tile tile, Unit source){
        return 0;
    }

    @Override
    public void handleStack(Item item, int amount, Tile tile, Unit source){

    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        return false;
    }

    @Override
    public void handleItem(Item item, Tile tile, Tile source){

    }

    @Override
    public TileEntity newEntity(){
        return new ConveyorEntity();
    }

    public class ConveyorEntity extends TileEntity{
        int blendshadowrot = -1;
        int blendbits;
        int blendsclx, blendscly;

        float clogHeat = 0f;
    }

}