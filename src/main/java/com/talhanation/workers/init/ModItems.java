package com.talhanation.workers.init;

import com.talhanation.workers.WorkersMain;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;

public class ModItems {
	public static final Item FARMER_SPAWN_EGG = registerSpawnEgg("farmer_spawn_egg", ModEntityTypes.FARMER);
	public static final Item LUMBERJACK_SPAWN_EGG = registerSpawnEgg("lumberjack_spawn_egg", ModEntityTypes.LUMBERJACK);

	// Đăng ký một spawn egg gắn với entity type tương ứng
	private static Item registerSpawnEgg(String name, EntityType<?> type) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, WorkersMain.id(name));
		Item.Properties properties = new Item.Properties()
				.setId(key)
				.component(DataComponents.ENTITY_DATA, TypedEntityData.of(type, new CompoundTag()));
		return Registry.register(BuiltInRegistries.ITEM, key, new SpawnEggItem(properties));
	}

	public static void register() {
	}
}
