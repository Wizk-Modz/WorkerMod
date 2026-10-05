package com.talhanation.workers.init;

import com.talhanation.workers.WorkersMain;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;

public class ModItems {
	public static final Item FARMER_SPAWN_EGG = registerSpawnEgg("farmer_spawn_egg", ModEntityTypes.FARMER, 16755200, 16777045);
	public static final Item LUMBERJACK_SPAWN_EGG = registerSpawnEgg("lumberjack_spawn_egg", ModEntityTypes.LUMBERJACK, 16755200, 16777045);

	// Đăng ký một spawn egg cho loại worker tương ứng
	private static Item registerSpawnEgg(String name, EntityType<? extends Mob> type, int primaryColor, int secondaryColor) {
		ResourceKey<Item> key = ResourceKey.create(Registries.ITEM, WorkersMain.id(name));
		return Registry.register(BuiltInRegistries.ITEM, key,
				new SpawnEggItem(type, primaryColor, secondaryColor, new Item.Properties().setId(key)));
	}

	public static void register() {
	}
}
