package com.customenchants.block;

import com.customenchants.enchantment.ModEnchantments;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import com.customenchants.mixin.AnvilLevelCostAccessor;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

public class ArcaneAnvilScreenHandler extends AnvilScreenHandler {

    public ArcaneAnvilScreenHandler(int syncId, PlayerInventory playerInventory) {
        super(syncId, playerInventory, ScreenHandlerContext.EMPTY);
    }

    public ArcaneAnvilScreenHandler(int syncId, PlayerInventory playerInventory,
                                     ScreenHandlerContext context) {
        super(syncId, playerInventory, context);
    }

    @Override
    public void updateResult() {
        ItemStack left  = this.getSlot(0).getStack();
        ItemStack right = this.getSlot(1).getStack();

        if (!left.isEmpty() && !right.isEmpty() && this.player != null) {
            Registry<Enchantment> registry = this.player.getRegistryManager()
                .getOrThrow(RegistryKeys.ENCHANTMENT);
            var vanillaEffOpt = registry.getOptional(Enchantments.EFFICIENCY);
            var customEffOpt  = registry.getOptional(ModEnchantments.EFFICIENCY);

            if (vanillaEffOpt.isPresent() && customEffOpt.isPresent()) {
                RegistryEntry<Enchantment> vanillaEff = vanillaEffOpt.get();
                RegistryEntry<Enchantment> customEff  = customEffOpt.get();

                int leftVanilla  = getEnchLevel(left,  vanillaEff);
                int rightVanilla = getEnchLevel(right, vanillaEff);
                int leftCustom   = getEnchLevel(left,  customEff);
                int rightCustom  = getEnchLevel(right, customEff);

                int newLevel = 0;
                if (leftVanilla == 5 && rightVanilla == 5 && leftCustom == 0 && rightCustom == 0) {
                    newLevel = 6;
                } else if (leftCustom == 6 && rightCustom == 6) {
                    newLevel = 7;
                }

                if (newLevel > 0) {
                    ItemStack output = left.copy();
                    output.setCount(1);
                    ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(
                        output.getOrDefault(DataComponentTypes.ENCHANTMENTS,
                            ItemEnchantmentsComponent.DEFAULT));
                    builder.set(vanillaEff, 0);
                    builder.set(customEff, newLevel);
                    output.set(DataComponentTypes.ENCHANTMENTS, builder.build());
                    this.getSlot(2).setStack(output);
                    ((AnvilLevelCostAccessor) this).getLevelCost().set(3);
                    this.sendContentUpdates();
                    return;
                }
            }
        }

        super.updateResult();
    }

    private static int getEnchLevel(ItemStack stack, RegistryEntry<Enchantment> enchant) {
        return stack.getOrDefault(DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT)
            .getLevel(enchant);
    }
}
