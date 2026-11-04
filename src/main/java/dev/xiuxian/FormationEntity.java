package dev.xiuxian;

import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.*;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.*;

public final class FormationEntity extends BlockEntity implements Container,MenuProvider {
    private final NonNullList<ItemStack> items=NonNullList.withSize(2,ItemStack.EMPTY);
    private UUID owner;
    private boolean enabled,structureDirty=true;
    private int scale;
    private long checkedAt=Long.MIN_VALUE,paidUntil=Long.MIN_VALUE;
    private double energy;
    private List<BlockPos> flags=List.of();
    private final Set<BlockPos> plants=new HashSet<>();
    private int scanCursor;
    private long nextScan;
    public FormationEntity(BlockPos pos,BlockState state) { super(Formations.ENTITY.get(),pos,state); }
    @Override public void onLoad() { super.onLoad();if(level instanceof ServerLevel server){FormationIndex.get(server).add(this);invalidate();} }
    @Override public void setRemoved() { if(level instanceof ServerLevel server)FormationIndex.get(server).remove(worldPosition);super.setRemoved(); }
    @Override public void onChunkUnloaded() { if(level instanceof ServerLevel server)FormationIndex.get(server).remove(worldPosition);super.onChunkUnloaded(); }
    public void assignOwner(UUID id) { owner=id;setChanged(); }
    UUID ownerId() { return owner; }
    public boolean canManage(Player player) { return player.hasPermissions(2)||player.getUUID().equals(owner); }
    public FormationKind kind() { return FormationKind.from(items.get(0)); }
    public double energy() { return energy; }
    public boolean enabled() { return enabled; }
    public int scale() { ensureStructure();return scale; }
    public void setEnabled(boolean value) { enabled=value;invalidate();setChanged(); }
    public void invalidate() { structureDirty=true; }
    void flagChanged() {
        invalidate();
        if(level instanceof ServerLevel server && enabled) {
            var player=server.players().stream().filter(p->p.getUUID().equals(owner)).findFirst().orElse(null);
            if(player!=null)player.displayClientMessage(Component.translatable("formation.xiuxian.changed"),true);
        }
    }
    private void ensureStructure() {
        if(!(level instanceof ServerLevel server)||isRemoved())return;
        FormationIndex.get(server).add(this);
        long now=level.getGameTime();if(!structureDirty&&now-checkedAt<40)return;
        int previous=scale;scale=0;flags=List.of();checkedAt=now;structureDirty=false;
        for(int distance:new int[]{8,4}) {
            List<BlockPos> found=new ArrayList<>();
            for(Direction direction:Direction.Plane.HORIZONTAL) {
                BlockPos base=worldPosition.relative(direction,distance);BlockPos flag=null;
                for(int dy:new int[]{0,-1,1}) { var candidate=base.offset(0,dy,0);if(FormationIndex.get(server).loaded(candidate)&&level.getBlockState(candidate).is(Formations.FLAG.get())){flag=candidate;break;} }
                if(flag==null)break;found.add(flag);
            }
            if(found.size()==4){scale=distance==8?2:1;flags=List.copyOf(found);break;}
        }
        if(scale!=previous){plants.clear();scanCursor=0;nextScan=0;}
    }
    public int radius() { return scale==2?16:8; }
    public int height() { return scale==2?6:4; }
    public boolean contains(BlockPos pos) { return scale>0&&Math.abs((long)pos.getX()-worldPosition.getX())<=radius()&&Math.abs((long)pos.getZ()-worldPosition.getZ())<=radius()&&Math.abs((long)pos.getY()-worldPosition.getY())<=height(); }
    public AABB bounds() { return new AABB(worldPosition.offset(-radius(),-height(),-radius()),worldPosition.offset(radius()+1,height()+1,radius()+1)); }
    public double gatheringMultiplier() { return scale==2?1.50:1.35; }
    public double upkeep() { var kind=kind();return kind==null?0:kind.upkeep; }
    ServerPlayer assaultOwner() {
        if(!(level instanceof ServerLevel server)||owner==null)return null;
        return server.players().stream().filter(p->owner.equals(p.getUUID())&&p.isAlive()&&!p.isRemoved()&&p.level()==level).findFirst().orElse(null);
    }
    public enum Status { ACTIVE, OFF, NO_TOKEN, STRUCTURE, REDSTONE, OWNER, ENERGY }
    private Status baseStatus() {
        ensureStructure();
        if(!enabled)return Status.OFF;if(kind()==null)return Status.NO_TOKEN;if(scale==0)return Status.STRUCTURE;
        if(!(level instanceof ServerLevel server)||isRemoved()||!FormationIndex.get(server).loaded(worldPosition))return Status.STRUCTURE;
        // A flag chunk can unload between the periodic structure checks.
        for(var flag:flags)if(!FormationIndex.get(server).loaded(flag)){structureDirty=true;return Status.STRUCTURE;}
        if(level.hasNeighborSignal(worldPosition))return Status.REDSTONE;
        if(kind()==FormationKind.ASSAULT&&assaultOwner()==null)return Status.OWNER;
        return Status.ACTIVE;
    }
    public Status status() {
        Status status=baseStatus();if(status!=Status.ACTIVE)return status;
        if(level.getGameTime()<paidUntil)return Status.ACTIVE;
        double cost=upkeep();return energy+1e-9>=cost||Formations.fuel(items.get(1))?Status.ACTIVE:Status.ENERGY;
    }
    public boolean active() {
        if(baseStatus()!=Status.ACTIVE)return false;
        long now=level.getGameTime();double cost=upkeep();
        // Once per running second; no offline catch-up charges.
        if(now>=paidUntil) { if(!spend(cost))return false;paidUntil=now+20; }
        return true;
    }
    boolean spend(double cost) {
        if(!Double.isFinite(cost)||cost<0)return false;
        if(energy+1e-9<cost) {
            if(!Formations.fuel(items.get(1)))return false;
            energy+=items.get(1).is(CultivationResources.STONE_2.get())?12000:1200;items.get(1).shrink(1);
        }
        if(energy+1e-9<cost)return false;
        energy=Math.max(0,energy-cost);if(cost>0)setChanged();return true;
    }
    boolean hostile(LivingEntity entity) {
        if(!(entity instanceof Enemy)||!entity.isAlive()||entity instanceof Player||entity instanceof TamableAnimal)return false;
        var player=assaultOwner();return player==null||!player.isAlliedTo(entity);
    }
    void guardPulse() {
        if(!(level instanceof ServerLevel server)||!active())return;
        for(var entity:server.getEntitiesOfClass(LivingEntity.class,bounds(),e->contains(e.blockPosition()))) {
            if(entity instanceof Player){entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,40,0));entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION,40,0));}
            else if(hostile(entity)) {
                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,2));entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,40,1));
                Vec3 outward=entity.position().subtract(worldPosition.getCenter()).multiply(1,0,1).normalize();if(outward.lengthSqr()==0)outward=new Vec3(1,0,0);entity.push(outward.x*.12,0,outward.z*.12);
            }
        }
    }
    int attackPulse() {
        if(!(level instanceof ServerLevel server)||!active())return 0;
        var player=assaultOwner();if(player==null)return 0;
        var targets=server.getEntitiesOfClass(LivingEntity.class,bounds(),e->contains(e.blockPosition())&&hostile(e));
        targets.sort(Comparator.<LivingEntity>comparingDouble(e->e.distanceToSqr(worldPosition.getCenter())).thenComparingInt(LivingEntity::getId));int attacked=0;
        var source=new DamageSource(server.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(Formations.DAMAGE),null,player);
        for(var target:targets) {
            if(attacked>=5||!spend(10))break;
            attacked++;if(target.hurt(source,10))target.setSecondsOnFire(3);
            server.sendParticles(ParticleTypes.CRIT,target.getX(),target.getY()+1,target.getZ(),10,.3,.5,.3,.02);
            server.sendParticles(ParticleTypes.FLAME,target.getX(),target.getY()+.5,target.getZ(),3,.2,.3,.2,0);
        }
        return attacked;
    }
    void assaultSlow() {
        if(!(level instanceof ServerLevel server)||!active())return;
        for(var target:server.getEntitiesOfClass(LivingEntity.class,bounds(),e->contains(e.blockPosition())&&hostile(e)))target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,40,1));
    }
    void particles() {
        if(!(level instanceof ServerLevel server)||!active())return;
        var particle=kind()==FormationKind.GATHERING?ParticleTypes.ENCHANT:kind()==FormationKind.GUARDIAN?ParticleTypes.END_ROD:ParticleTypes.SOUL_FIRE_FLAME;
        for(var flag:flags) {
            Vec3 start=worldPosition.getCenter(),end=flag.getCenter().add(0,.6,0);
            for(int i=0;i<=8;i++){Vec3 point=start.lerp(end,i/8.0);server.sendParticles(particle,point.x,point.y,point.z,1,0,0,0,0);}
        }
    }
    static boolean growing(BlockState state) { return state.getBlock() instanceof QiHerb&&state.getValue(QiHerb.AGE)<2||state.getBlock() instanceof FoodPlantBlock&&state.getValue(FoodPlantBlock.AGE)<3; }
    void plantChanged(BlockPos pos,boolean removing) { ensureStructure();if(!contains(pos))return;if(removing)plants.remove(pos);else if(level!=null&&growing(level.getBlockState(pos)))plants.add(pos.immutable()); }
    boolean scanPending() { return level!=null&&level.getGameTime()>=nextScan; }
    int scanPlants(int budget) {
        if(!(level instanceof ServerLevel server)||kind()!=FormationKind.GATHERING||!active())return 0;
        long now=level.getGameTime();if(now<nextScan)return 0;
        int width=2*radius()+1,depth=2*height()+1,total=width*width*depth,count=0;
        while(count<budget&&scanCursor<total) {
            int index=scanCursor++,x=index%width,z=(index/width)%width,y=index/(width*width);
            BlockPos pos=worldPosition.offset(x-radius(),y-height(),z-radius());
            if(FormationIndex.get(server).loaded(pos)) { if(growing(level.getBlockState(pos)))plants.add(pos);else plants.remove(pos); }
            count++;
        }
        if(scanCursor>=total){scanCursor=0;nextScan=now+200;}
        return count;
    }
    int growPlants(Set<BlockPos> used) {
        if(!(level instanceof ServerLevel server)||!active())return 0;
        var candidates=new ArrayList<>(plants);Collections.shuffle(candidates,new Random(server.random.nextLong()));int count=0;
        for(var pos:candidates) {
            if(!contains(pos)||!FormationIndex.get(server).loaded(pos))continue;
            var state=server.getBlockState(pos);if(!growing(state)){plants.remove(pos);continue;}
            if(count>=3)break;if(!used.add(pos))continue;
            state.randomTick(server,pos,server.random);count++;
        }
        return count;
    }
    void showActive() {
        if(level instanceof ServerLevel&&!isRemoved()) { boolean value=active();var state=level.getBlockState(worldPosition);if(state.is(Formations.PLATE.get())&&state.getValue(FormationBlock.ACTIVE)!=value)level.setBlock(worldPosition,state.setValue(FormationBlock.ACTIVE,value),3); }
    }
    @Override protected void saveAdditional(CompoundTag tag) { super.saveAdditional(tag);ContainerHelper.saveAllItems(tag,items);tag.putDouble("Energy",energy);tag.putBoolean("Enabled",enabled);if(owner!=null)tag.putUUID("Owner",owner); }
    @Override public void load(CompoundTag tag) { super.load(tag);ContainerHelper.loadAllItems(tag,items);double saved=tag.getDouble("Energy");energy=Double.isFinite(saved)?Math.max(0,Math.min(13200,saved)):0;enabled=tag.getBoolean("Enabled");owner=tag.hasUUID("Owner")?tag.getUUID("Owner"):null;paidUntil=Long.MIN_VALUE;invalidate(); }
    @Override public Component getDisplayName() { return Component.translatable("block.xiuxian.formation_plate"); }
    @Override public AbstractContainerMenu createMenu(int id,Inventory inv,Player player) { return new FormationMenu(id,inv,this,new SimpleContainerData(5)); }
    @Override public int getContainerSize() { return 2; }
    @Override public boolean isEmpty() { return items.stream().allMatch(ItemStack::isEmpty); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }
    @Override public ItemStack removeItem(int slot,int amount) { var result=ContainerHelper.removeItem(items,slot,amount);if(!result.isEmpty()){invalidate();setChanged();}return result; }
    @Override public ItemStack removeItemNoUpdate(int slot) { var result=ContainerHelper.takeItem(items,slot);invalidate();return result; }
    @Override public void setItem(int slot,ItemStack stack) { items.set(slot,stack);stack.setCount(Math.min(stack.getMaxStackSize(),getMaxStackSize()));invalidate();setChanged(); }
    @Override public void clearContent() { items.clear();invalidate();setChanged(); }
    @Override public boolean stillValid(Player player) { return level!=null&&level.getBlockEntity(worldPosition)==this&&canManage(player)&&player.distanceToSqr(worldPosition.getCenter())<=64; }
    static boolean accepts(int slot,ItemStack stack) { return slot==0?FormationKind.from(stack)!=null:slot==1&&Formations.fuel(stack); }
    @Override public boolean canPlaceItem(int slot,ItemStack stack) { return accepts(slot,stack); }
}
