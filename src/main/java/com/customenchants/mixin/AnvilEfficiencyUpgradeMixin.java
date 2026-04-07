package com.customenchants.mixin;

import com.customenchants.enchantment.ModEnchantments;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilScreenHandler.class)
public abstract class AnvilEfficiencyUpgradeMixin {

    @Inject(method = "updateResult", at = @At("RETURN"))
    private void upgradeEfficiency(CallbackInfo ci) {
        AnvilScreenHandler self = (AnvilScreenHandler) (Object) this;

        // Find the player by scanning all slots for a PlayerInventory —
        // ForgingScreenHandler adds player inv slots starting at index 3.
        // This avoids @Shadow which requires a refmap.
        PlayerEntity player = null;
        for (Slot slot : self.slots) {
            if (slot.inventory instanceof PlayerInventory pi) {
                player = pi.player;
                break;
            }
        }
        if (player == null) return;

        ItemStack left  = self.getSlot(0).getStack();
        ItemStack right = self.getSlot(1).getStack();
        if (left.isEmpty() || right.isEmpty()) return;

        Registry<Enchantment> registry = player.getRegistryManager()
            .getOrThrow(RegistryKeys.ENCHANTMENT);

        var vanillaEffOpt = registry.getOptional(Enchantments.EFFICIENCY);
        var customEffOpt  = registry.getOptional(ModEnchantments.EFFICIENCY);
        if (vanillaEffOpt.isEmpty() || customEffOpt.isEmpty()) return;

        RegistryEntry<Enchantment> vanillaEff = vanillaEffOpt.get();
        RegistryEntry<Enchantment> customEff  = customEffOpt.get();

        int leftLevel  = getEffectiveEffLevel(left,  vanillaEff, customEff);
        int rightLevel = getEffectiveEffLevel(right, vanillaEff, customEff);

        int newLevel = 0;
        if (leftLevel == 5 && rightLevel == 5) {
            newLevel = 6;
        } else if (leftLevel == 6 && rightLevel == 6) {
            newLevel = 7;
        }
        // If no upgrade applies but either side has Eff VI+, block vanilla
        // from producing a downgraded output (e.g. Eff VI + Eff V → Eff V).
        if (newLevel == 0) {
            if (leftLevel >= 6 || rightLevel >= 6) {
                self.getSlot(2).setStack(ItemStack.EMPTY);
                self.sendContentUpdates();
            }
            return;
        }

        boolean outputIsBook = left.isOf(Items.ENCHANTED_BOOK) && right.isOf(Items.ENCHANTED_BOOK);
        ItemStack output;
        if (outputIsBook) {
            output = new ItemStack(Items.ENCHANTED_BOOK);
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(
                ItemEnchantmentsComponent.DEFAULT);
            builder.set(customEff, newLevel);
            output.set(DataComponentTypes.STORED_ENCHANTMENTS, builder.build());
        } else {
            output = left.copy();
            output.setCount(1);
            ItemEnchantmentsComponent existing = output.getOrDefault(
                DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
            ItemEnchantmentsComponent.Builder builder = new ItemEnchantmentsComponent.Builder(existing);
            builder.set(vanillaEff, 0);
            builder.set(customEff, newLevel);
            output.set(DataComponentTypes.ENCHANTMENTS, builder.build());
        }

        self.getSlot(2).setStack(output);
        ((AnvilLevelCostAccessor) self).getLevelCost().set(3);
        self.sendContentUpdates();
    }

    private static int getEffectiveEffLevel(ItemStack stack,
                                             RegistryEntry<Enchantment> vanillaEff,
                                             RegistryEntry<Enchantment> customEff) {
        ItemEnchantmentsComponent enc = stack.getOrDefault(
            DataComponentTypes.ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        int level = Math.max(enc.getLevel(vanillaEff), enc.getLevel(customEff));
        if (level > 0) return level;

        ItemEnchantmentsComponent stored = stack.getOrDefault(
            DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantmentsComponent.DEFAULT);
        return Math.max(stored.getLevel(vanillaEff), stored.getLevel(customEff));
    }
}
