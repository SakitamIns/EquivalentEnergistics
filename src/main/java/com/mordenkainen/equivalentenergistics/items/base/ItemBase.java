package com.mordenkainen.equivalentenergistics.items.base;

import com.mordenkainen.equivalentenergistics.EquivalentEnergetics;
import com.mordenkainen.equivalentenergistics.core.Reference;

import net.minecraft.item.Item;

public abstract class ItemBase extends Item {

    protected String name;

    public ItemBase(final String name) {
        super();
        this.name = name;
        setTranslationKey(Reference.MOD_ID + ":" + name);
        setCreativeTab(EquivalentEnergetics.tabEE);
        setRegistryName(name);
    }

    public void registerItemModel() {
        EquivalentEnergetics.proxy.registerItemRenderer(this, 0, name);
    }

}
