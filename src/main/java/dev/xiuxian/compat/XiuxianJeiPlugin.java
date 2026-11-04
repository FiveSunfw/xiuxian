package dev.xiuxian.compat;
import dev.xiuxian.*;
import mezz.jei.api.*;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.*;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.*;
import java.util.*;
@JeiPlugin
public class XiuxianJeiPlugin implements IModPlugin {
    public record Entry(List<ItemStack> inputs,ItemStack output){}
    static final RecipeType<Entry> BREW=new RecipeType<>(new ResourceLocation(Xiuxian.ID,"herbal_brewing"),Entry.class), PILL=new RecipeType<>(new ResourceLocation(Xiuxian.ID,"alchemy"),Entry.class), FORGE=new RecipeType<>(new ResourceLocation(Xiuxian.ID,"forging"),Entry.class), KITCHEN=new RecipeType<>(new ResourceLocation(Xiuxian.ID,"spirit_cooking"),Entry.class);
    @Override public ResourceLocation getPluginUid(){return new ResourceLocation(Xiuxian.ID,"jei");}
    @Override public void registerCategories(IRecipeCategoryRegistration r){var gui=r.getJeiHelpers().getGuiHelper();r.addRecipeCategories(new Category(gui,BREW,Xiuxian.FURNACE_ITEM.get()),new Category(gui,PILL,Alchemy.FURNACE_ITEM.get()),new Category(gui,FORGE,ForgingContent.BASIC_FORGING_TABLE_ITEM.get()),new Category(gui,KITCHEN,SpiritualFarming.KITCHEN_ITEM.get()));}
    @Override public void registerRecipes(IRecipeRegistration r){
        List<Entry> brews=new ArrayList<>();
        for(var kind:HerbKind.values())brews.add(new Entry(List.of(new ItemStack(Xiuxian.HERB_ITEMS.get(kind).get()),water()),new ItemStack(Xiuxian.ELIXIRS.get(kind).get())));
        brews.add(new Entry(List.of(new ItemStack(Xiuxian.HERB_ITEMS.get(HerbKind.WOOD).get()),new ItemStack(Items.WHEAT),water()),new ItemStack(Alchemy.item("healing_liquid"))));
        brews.add(new Entry(List.of(new ItemStack(Xiuxian.HERB_ITEMS.get(HerbKind.FROST).get()),new ItemStack(Xiuxian.HERB_ITEMS.get(HerbKind.MARSH).get()),water()),new ItemStack(Alchemy.item("cleansing_liquid"))));
        brews.add(new Entry(List.of(new ItemStack(Xiuxian.HERB_ITEM.get()),new ItemStack(Xiuxian.HERB_ITEMS.get(HerbKind.WOOD).get()),water()),new ItemStack(Alchemy.item("warming_liquid"))));r.addRecipes(BREW,brews);
        List<Entry> pills=new ArrayList<>();for(var f:Alchemy.formulas())for(var kind:HerbKind.values()){if(!f.id().equals("qi_gathering")&&kind!=HerbKind.QI)continue;pills.add(new Entry(List.of(new ItemStack(Alchemy.item(f.id()+"_formula")),new ItemStack(f.id().equals("qi_gathering")?Xiuxian.HERB_ITEMS.get(kind).get():f.main(),f.mainCount()),new ItemStack(f.auxiliary(),f.auxiliaryCount()),new ItemStack(CultivationResources.STONE.get(),f.stones()),new ItemStack(Items.COAL)),new ItemStack(f.result())));}r.addRecipes(PILL,pills);
        List<Entry> forging=new ArrayList<>();
        for(var blankEntry:ForgingContent.BLANKS.entrySet()){
            ItemStack blank=new ItemStack(blankEntry.getValue().get());var slot=ForgingRules.slot(blank);if(slot==null)continue;
            List<String> compatible=ForgingRules.compatibleMaterials(slot);
            for(int first=0;first<compatible.size();first++){
                String firstId=compatible.get(first);ItemStack firstInfusion=ForgedItemData.infuse(blank,firstId);
                forging.add(new Entry(List.of(blank.copy(),new ItemStack(CultivationResources.STONE.get()),new ItemStack(Beasts.material(firstId))),firstInfusion));
                for(int second=first+1;second<compatible.size();second++){
                    String secondId=compatible.get(second);ItemStack result=ForgedItemData.infuse(firstInfusion,secondId);
                    forging.add(new Entry(List.of(firstInfusion.copy(),new ItemStack(CultivationResources.STONE.get()),new ItemStack(Beasts.material(secondId))),result));
                }
            }
        }
        r.addRecipes(FORGE,forging);
        List<Entry> meals=new ArrayList<>();
        for(var recipe:SpiritKitchenRecipes.all())for(var main:recipe.mains())for(var fuel:List.of(Items.COAL,CultivationResources.STONE.get())){
            List<ItemStack> ingredients=new ArrayList<>();ingredients.add(new ItemStack(main));ingredients.add(new ItemStack(SpiritualFarming.PLANTS.get(recipe.plant()).get(),recipe.plantCount()));
            if(recipe.container()!=null)ingredients.add(new ItemStack(recipe.container()));ingredients.add(new ItemStack(fuel));
            meals.add(new Entry(ingredients,recipe.output()));
        }
        r.addRecipes(KITCHEN,meals);
    }
    static ItemStack water(){return PotionUtils.setPotion(new ItemStack(Items.POTION),Potions.WATER);}
    @Override public void registerRecipeCatalysts(IRecipeCatalystRegistration r){r.addRecipeCatalyst(new ItemStack(Xiuxian.FURNACE_ITEM.get()),BREW);r.addRecipeCatalyst(new ItemStack(Alchemy.FURNACE_ITEM.get()),PILL);r.addRecipeCatalyst(new ItemStack(ForgingContent.BASIC_FORGING_TABLE_ITEM.get()),FORGE);r.addRecipeCatalyst(new ItemStack(SpiritualFarming.KITCHEN_ITEM.get()),KITCHEN);}
    static class Category implements IRecipeCategory<Entry>{
        final RecipeType<Entry> type; final Item machine; final IDrawable bg,icon;
        Category(IGuiHelper gui,RecipeType<Entry> type,Item machine){this.type=type;this.machine=machine;bg=gui.createBlankDrawable(170,65);icon=gui.createDrawableIngredient(VanillaTypes.ITEM_STACK,new ItemStack(machine));}
        @Override public RecipeType<Entry> getRecipeType(){return type;}
        @Override public Component getTitle(){return machine.getDescription();}
        @Override public IDrawable getBackground(){return bg;}
        @Override public IDrawable getIcon(){return icon;}
        @Override public void setRecipe(IRecipeLayoutBuilder b,Entry e,IFocusGroup f){for(int i=0;i<e.inputs().size();i++)b.addSlot(RecipeIngredientRole.INPUT,4+i*25,12).addItemStack(e.inputs().get(i));b.addSlot(RecipeIngredientRole.OUTPUT,148,12).addItemStack(e.output());}
        @Override public void draw(Entry e,mezz.jei.api.gui.ingredient.IRecipeSlotsView slots,net.minecraft.client.gui.GuiGraphics g,double x,double y){Component hint=type==PILL?Component.translatable("jei.xiuxian.alchemy"):type==FORGE?Component.translatable("jei.xiuxian.forging"):type==KITCHEN?Component.translatable("jei.xiuxian.kitchen"):Component.literal("→");g.drawString(net.minecraft.client.Minecraft.getInstance().font,hint,4,43,0xff604a35,false);}
    }
}
