package cn.blockforge.generated.generatedmod;

import cn.blockforge.generated.generatedmod.plants.SpiritPlantRegistry;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

/** Selected formulas may be rendered before the player has supplied their ingredients. */
@GameTestHolder("wuxingxiantu_entry")
@PrefixGameTestTemplate(false)
public final class HerbPreparationGameTests {
    private static ItemStack herb(int count, boolean prepared) {
        ItemStack raw = new ItemStack(SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get(), count);
        if (!prepared) return raw;
        return HerbPreparation.prepare(raw).copyWithCount(count);
    }

    private static AlchemyRecipe recipe(int... counts) {
        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (int ignored : counts) ingredients.add(Ingredient.of(SpiritPlantRegistry.SPIRIT_SPROUT_GRASS_ITEM.get()));
        return new AlchemyRecipe(ResourceLocation.fromNamespaceAndPath("wuxingxiantu", "qa_prepared_herbs"), ingredients,
                java.util.Arrays.stream(counts).boxed().toList(), new ItemStack(Items.APPLE), 0, 20, 1, false);
    }

    private static void assertBonusReadOnly(GameTestHelper h, AlchemyRecipe recipe, CheckedContainer input,
                                          int expected, String situation) {
        ItemStack[] before = new ItemStack[input.getContainerSize()];
        for (int slot = 0; slot < before.length; slot++) before[slot] = input.getItem(slot).copy();
        int changes = input.changes;
        // Rendering queries the same state repeatedly; none may reserve or consume the inputs.
        for (int frame = 0; frame < 4; frame++) {
            h.assertTrue(HerbPreparation.bonus(recipe, input) == expected, situation + ": incorrect preview bonus");
        }
        for (int slot = 0; slot < before.length; slot++) {
            h.assertTrue(ItemStack.matches(before[slot], input.getItem(slot)), situation + ": preview changed slot " + slot);
        }
        h.assertTrue(input.changes == changes, situation + ": preview marked inventory changed");
    }

    private static void assertReservationRejected(GameTestHelper h, AlchemyRecipe recipe, CheckedContainer input) {
        h.assertTrue(recipe.tryReservation(input).isEmpty(), "Preview allocation accepted insufficient ingredients");
        boolean rejected = false;
        try {
            recipe.reservation(input);
        } catch (IllegalStateException expected) {
            rejected = true;
        }
        h.assertTrue(rejected, "Strict brewing reservation accepted insufficient ingredients");
    }

    @GameTest(template = "empty")
    public static void unavailableSelectedFormulaHasZeroBonusWithoutChangingInputs(GameTestHelper h) {
        AlchemyRecipe recipe = recipe(3);
        CheckedContainer input = new CheckedContainer(8);
        assertBonusReadOnly(h, recipe, input, 0, "Selected formula with empty materials");
        h.assertTrue(!recipe.matches(input, h.getLevel()), "Empty ingredients match formula");
        assertReservationRejected(h, recipe, input);

        input.setItem(0, herb(2, true));
        input.getItem(0).getOrCreateTag().putString("RegressionMarker", "preserve-me");
        assertBonusReadOnly(h, recipe, input, 0, "Selected formula with too few prepared herbs");
        h.assertTrue(!recipe.matches(input, h.getLevel()), "Short stack matches formula");
        assertReservationRejected(h, recipe, input);

        ItemStack wrongHerb = new ItemStack(SpiritPlantRegistry.DEW_DROP_FLOWER_ITEM.get(), 3);
        input.setItem(0, HerbPreparation.prepare(wrongHerb).copyWithCount(3));
        assertBonusReadOnly(h, recipe, input, 0, "Selected formula with wrong prepared herb");
        assertReservationRejected(h, recipe, input);
        assertBonusReadOnly(h, null, input, 0, "No selected formula");
        h.assertTrue(HerbPreparation.bonus(recipe, null) == 0, "No input container has a bonus");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void completePreparedFormulaRetainsMaximumBonus(GameTestHelper h) {
        AlchemyRecipe recipe = recipe(3);
        CheckedContainer input = new CheckedContainer(8);
        input.setItem(0, herb(3, false));
        assertBonusReadOnly(h, recipe, input, 0, "Complete raw batch");
        input.setItem(0, herb(3, true));
        h.assertTrue(recipe.matches(input, h.getLevel()), "Prepared herbs no longer match their ingredient");
        assertBonusReadOnly(h, recipe, input, 6, "Complete prepared batch");
        int[] reservation = recipe.reservation(input);
        h.assertTrue(reservation.length == AlchemyFurnaceBlockEntity.MATERIAL_SLOT_COUNT
                && reservation[0] == 3, "Valid formula has wrong strict reservation");
        h.assertTrue(java.util.Arrays.equals(reservation, recipe.tryReservation(input).orElseThrow()),
                "Preview allocation differs from strict brewing reservation");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void bonusUsesActualReservedProportionAndIgnoresSparePreparedHerbs(GameTestHelper h) {
        AlchemyRecipe recipe = recipe(4);
        CheckedContainer input = new CheckedContainer(8);
        input.setItem(0, herb(3, false));
        input.setItem(1, herb(32, true));
        input.setItem(7, herb(64, true));
        assertBonusReadOnly(h, recipe, input, 1, "One prepared herb out of four consumed herbs");
        int[] reservation = recipe.reservation(input);
        h.assertTrue(reservation[0] == 3 && reservation[1] == 1 && reservation[7] == 0,
                "Reservation counts spare prepared herbs");

        input.setItem(0, herb(4, false));
        assertBonusReadOnly(h, recipe, input, 0, "Raw formula with only spare prepared herbs");
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void smallContainersAndNonMaterialSlotsRespectReservationBounds(GameTestHelper h) {
        AlchemyRecipe recipe = recipe(3);
        CheckedContainer empty = new CheckedContainer(0);
        assertBonusReadOnly(h, recipe, empty, 0, "Zero-slot input");
        h.assertTrue(!recipe.matches(empty, h.getLevel()), "Zero-slot input matches formula");
        assertReservationRejected(h, recipe, empty);

        CheckedContainer small = new CheckedContainer(1);
        small.setItem(0, herb(3, true));
        assertBonusReadOnly(h, recipe, small, 6, "Complete one-slot input");
        h.assertTrue(recipe.matches(small, h.getLevel()) && recipe.reservation(small)[0] == 3,
                "One-slot input cannot be reserved");
        small.setItem(0, herb(2, true));
        assertBonusReadOnly(h, recipe, small, 0, "Incomplete one-slot input");
        assertReservationRejected(h, recipe, small);

        CheckedContainer furnace = new CheckedContainer(9);
        furnace.setItem(8, herb(3, true));
        assertBonusReadOnly(h, recipe, furnace, 0, "Herbs outside the eight material slots");
        h.assertTrue(!recipe.matches(furnace, h.getLevel()), "Ninth slot supplied recipe ingredients");
        assertReservationRejected(h, recipe, furnace);
        h.succeed();
    }

    @GameTest(template = "empty")
    public static void repeatedIngredientEntriesCannotReserveTheSameHerbsTwice(GameTestHelper h) {
        AlchemyRecipe recipe = recipe(2, 1);
        CheckedContainer input = new CheckedContainer(2);
        input.setItem(0, herb(2, true));
        h.assertTrue(!recipe.matches(input, h.getLevel()), "Repeated ingredient entries reused a short stack");
        assertBonusReadOnly(h, recipe, input, 0, "Repeated ingredients with only two of three herbs");
        assertReservationRejected(h, recipe, input);

        input.setItem(0, herb(1, true));
        input.setItem(1, herb(2, false));
        h.assertTrue(recipe.matches(input, h.getLevel()), "Split repeated ingredients fail to match");
        int[] reservation = recipe.reservation(input);
        h.assertTrue(reservation[0] == 1 && reservation[1] == 2, "Repeated ingredient allocation reused material");
        h.assertTrue(java.util.Arrays.equals(reservation, recipe.tryReservation(input).orElseThrow()),
                "Repeated ingredient preview differs from strict allocation");
        assertBonusReadOnly(h, recipe, input, 2, "One prepared herb across repeated ingredient entries");
        h.succeed();
    }

    /** SimpleContainer normally tolerates an out-of-range read, hiding reservation boundary bugs. */
    private static final class CheckedContainer extends SimpleContainer {
        private int changes;

        private CheckedContainer(int size) { super(size); }

        @Override
        public ItemStack getItem(int slot) {
            if (slot < 0 || slot >= getContainerSize()) throw new IndexOutOfBoundsException("Unexpected slot " + slot);
            return super.getItem(slot);
        }

        @Override
        public void setChanged() {
            changes++;
            super.setChanged();
        }
    }
}
