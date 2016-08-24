package com.mordenkainen.equivalentenergistics.integration.ae2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Equivalence;
import com.google.common.base.Equivalence.Wrapper;
import com.mordenkainen.equivalentenergistics.EquivalentEnergistics;
import com.mordenkainen.equivalentenergistics.integration.Integration;
import com.mordenkainen.equivalentenergistics.items.ItemEMCCrystal;
import com.mordenkainen.equivalentenergistics.items.ItemPattern;
import com.mordenkainen.equivalentenergistics.registries.ItemEnum;
import com.mordenkainen.equivalentenergistics.util.CompItemStack;

import appeng.api.AEApi;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.data.IAEItemStack;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public final class EMCCraftingPattern implements ICraftingPatternDetails {
	
	private static Equivalence<ItemStack> eq = new CompItemStack();
	private static Map<Equivalence.Wrapper<ItemStack>, EMCCraftingPattern> patterns = new HashMap<Equivalence.Wrapper<ItemStack>, EMCCraftingPattern>();
	
	private IAEItemStack[] ingredients;
	private IAEItemStack[] result = new IAEItemStack[1];
	public float outputEMC;
	public float inputEMC;
	public boolean valid = true;
	
	// ICraftingPatternDetails Overrides
	// ------------------------
	private EMCCraftingPattern(final ItemStack craftingResult) {
		buildPattern(craftingResult);
	}
	
	@Override
	public ItemStack getPattern() {
		return ItemPattern.getItemForPattern(result[0].getItemStack());
	}

	@Override
	public boolean isValidItemForSlot(final int slotIndex, final ItemStack itemStack, final World world) {
		return false;
	}

	@Override
	public boolean isCraftable() {
		return false;
	}

	@Override
	public IAEItemStack[] getInputs() {
		return ingredients.clone();
	}

	@Override
	public IAEItemStack[] getCondensedInputs() {
		return getInputs();
	}

	@Override
	public ItemStack getOutput(final InventoryCrafting craftingInv, final World world) {
		return null;
	}
	
	@Override
	public IAEItemStack[] getOutputs() {
		return result.clone();
	}
	
	@Override
	public IAEItemStack[] getCondensedOutputs() {
		return getOutputs();
	}

	@Override
	public boolean canSubstitute() {
		return false;
	}

	@Override
	public int getPriority() {
		return 0;
	}

	@Override
	public void setPriority(final int priority) {}
	// ------------------------

	private void buildPattern(final ItemStack craftingResult)	{
		if (craftingResult.getItem() == ItemEnum.EMCCRYSTAL.getItem()) {
			createCrystalPattern(craftingResult.getItemDamage());
		} else {
			createItemPattern(craftingResult);
		}
	}

	private void createCrystalPattern(final int tier) {
		valid = true;
		outputEMC = inputEMC = ItemEMCCrystal.CRYSTAL_VALUES[tier + 1];
		result[0] = AEApi.instance().storage().createItemStack(ItemEnum.EMCCRYSTAL.getStack(64, tier));
		ingredients = new IAEItemStack[] {AEApi.instance().storage().createItemStack(ItemEnum.EMCCRYSTAL.getDamagedStack(tier + 1))};
	}
	
	private void createItemPattern(final ItemStack craftingResult) {
		result[0] = AEApi.instance().storage().createItemStack(craftingResult).setStackSize(1);
		float remainingEMC = outputEMC = Integration.emcHandler.getSingleEnergyValue(craftingResult);
		inputEMC = 0;
		valid = false;
		final ArrayList<IAEItemStack> crystals = new ArrayList<IAEItemStack>();
		for(int x = 4; x >= 0 && remainingEMC > 0; x--) {
			final float crystalEMC = ItemEMCCrystal.CRYSTAL_VALUES[x];
			int numCrystals = (int) (remainingEMC / crystalEMC);
			while (numCrystals > 0) {
				crystals.add(AEApi.instance().storage().createItemStack(ItemEnum.EMCCRYSTAL.getDamagedStack(x)).setStackSize(numCrystals));
				final float totalEMC = crystalEMC * numCrystals;
				remainingEMC -= totalEMC;
				inputEMC += totalEMC;
				numCrystals = (int) (remainingEMC / crystalEMC);
			}
		}
		
		if (remainingEMC > 0) {
			if (crystals.get(crystals.size() - 1).getItemDamage() == 0) {
				crystals.get(crystals.size() - 1).setStackSize(crystals.get(crystals.size() - 1).getStackSize() + 1);
			} else {
				crystals.add(AEApi.instance().storage().createItemStack(ItemEnum.EMCCRYSTAL.getDamagedStack(0)));
			}
			inputEMC++;
		}
		
		ingredients = (IAEItemStack[])crystals.toArray(new IAEItemStack[crystals.size()]);
		
		if (crystals.size() <= 9) {
			valid = true;
		}
	}
	
	public static void relearnPatterns() {
		final Iterator<Wrapper<ItemStack>> iter =  patterns.keySet().iterator();
		
		while (iter.hasNext()) {
			final Wrapper<ItemStack> wrappedStack = iter.next();
			if(Integration.emcHandler.hasEMC(wrappedStack.get())) {
				final EMCCraftingPattern pattern = (EMCCraftingPattern) patterns.get(wrappedStack);
				pattern.buildPattern(wrappedStack.get());
				if (!pattern.valid) {
					EquivalentEnergistics.logger.warn("Invalid EMC pattern detected. Item: " + StatCollector.translateToLocal(pattern.result[0].getItem().getUnlocalizedName(pattern.result[0].getItemStack()) + ".name") + " EMC: " + String.format("%f", pattern.outputEMC));
					iter.remove();
				}
			} else {
				iter.remove();
			}
		}
	}
	
	public static EMCCraftingPattern get(final ItemStack result) {
		if (patterns.containsKey(eq.wrap(result))) {
			return (EMCCraftingPattern)patterns.get(eq.wrap(result));
		}
		
		final EMCCraftingPattern newPattern = new EMCCraftingPattern(result);
		if (newPattern.valid) {
			patterns.put(eq.wrap(result), newPattern);
			return newPattern;
		} else {
			EquivalentEnergistics.logger.warn("Invalid EMC pattern detected. Item: " + StatCollector.translateToLocal(newPattern.result[0].getItem().getUnlocalizedName(newPattern.result[0].getItemStack()) + ".name") + " EMC: " + String.format("%f", newPattern.outputEMC));
			return null;
		}
	}
	
}