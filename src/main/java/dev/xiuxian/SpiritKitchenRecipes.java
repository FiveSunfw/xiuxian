package dev.xiuxian;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.List;

/** Shared by the server cooking logic and JEI. */
public final class SpiritKitchenRecipes {
    public record Recipe(SpiritFoodKind food, List<Item> mains, FoodPlantKind plant, int plantCount, Item container) {
        public boolean matches(ItemStack main, ItemStack ingredient, ItemStack vessel) {
            return mains.contains(main.getItem()) && main.getCount() >= 1 &&
                ingredient.is(SpiritualFarming.PLANTS.get(plant).get()) && ingredient.getCount() >= plantCount &&
                (container == null ? vessel.isEmpty() : vessel.is(container) && vessel.getCount() >= 1);
        }
        public ItemStack output() { return new ItemStack(SpiritualFarming.FOODS.get(food).get()); }
    }
    private SpiritKitchenRecipes() {}
    private static final class RegisteredRecipes {
        private static final List<Recipe> ALL = List.of(
            new Recipe(SpiritFoodKind.RICE, List.of(SpiritualFarming.PLANTS.get(FoodPlantKind.JADE_RICE).get()), FoodPlantKind.JADE_RICE, 1, Items.BOWL),
            new Recipe(SpiritFoodKind.BREAD, List.of(Items.BREAD), FoodPlantKind.GREEN_WHEAT, 2, null),
            new Recipe(SpiritFoodKind.STEAK, List.of(Items.COOKED_BEEF), FoodPlantKind.DRAGON_PEPPER, 1, null),
            new Recipe(SpiritFoodKind.STEW, List.of(Items.COOKED_MUTTON), FoodPlantKind.EARTH_GINGER, 1, Items.BOWL),
            new Recipe(SpiritFoodKind.POTATO, List.of(Items.BAKED_POTATO), FoodPlantKind.ARMOR_POTATO, 1, null),
            new Recipe(SpiritFoodKind.SOUP, List.of(Items.COOKED_COD, Items.COOKED_SALMON), FoodPlantKind.PURPLE_BAMBOO, 1, Items.BOWL),
            new Recipe(SpiritFoodKind.DRINK, List.of(Items.SWEET_BERRIES, Items.MELON_SLICE), FoodPlantKind.DEW_MELON, 1, Items.GLASS_BOTTLE),
            new Recipe(SpiritFoodKind.CAKE, List.of(Items.HONEY_BOTTLE, Items.COOKIE), FoodPlantKind.STAR_FLOWER, 1, null)
        );
    }
    public static List<Recipe> all() { return RegisteredRecipes.ALL; }
    public static Recipe find(ItemStack main, ItemStack plant, ItemStack vessel) {
        return all().stream().filter(recipe -> recipe.matches(main, plant, vessel)).findFirst().orElse(null);
    }
    public static boolean isMain(ItemStack stack) { return all().stream().anyMatch(recipe -> recipe.mains().contains(stack.getItem())); }
    public static boolean isPlant(ItemStack stack) { return SpiritualFarming.PLANTS.values().stream().anyMatch(item -> stack.is(item.get())); }
}
