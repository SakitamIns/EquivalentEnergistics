package com.mordenkainen.equivalentenergistics.blocks.base.block;

import com.mordenkainen.equivalentenergistics.EquivalentEnergetics;
import com.mordenkainen.equivalentenergistics.core.Reference;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;

public abstract class BlockBase extends Block {

    protected String name;

    public BlockBase(final Material material, final String name) {
        super(material);
        this.name = name;
        setTranslationKey(Reference.MOD_ID + ":" + name);
        setRegistryName(Reference.MOD_ID + ":" + name);
        setCreativeTab(EquivalentEnergetics.tabEE);
    }

    public void registerItemModel(final Item itemBlock) {
        EquivalentEnergetics.proxy.registerItemRenderer(itemBlock, 0, name);
    }

    public Item createItemBlock() {
        return new ItemBlock(this).setRegistryName(getRegistryName());
    }

}
