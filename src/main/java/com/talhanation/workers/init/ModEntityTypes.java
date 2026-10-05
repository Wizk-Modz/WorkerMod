package com.talhanation.workers.init;

import com.talhanation.workers.WorkersMain;
import com.talhanation.workers.entities.FarmerEntity;
import com.talhanation.workers.entities.LumberjackEntity;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntityTypes {
	public static final EntityType<FarmerEntity> FARMER = register("farmer",
			EntityType.Builder.of(FarmerEntity::new, MobCategory.CREATURE)
					.sized(0.6F, 1.95F)
					.setTrackingRange(32)
					.setShouldReceiveVelocityUpdates(true));

	public static final EntityType<LumberjackEntity> LUMBERJACK = register("lumberjack",
			EntityType.Builder.of(LumberjackEntity::new, MobCategory.CREATURE)
					.sized(0.6F, 1.95F)
					.setTrackingRange(32)
					.setShouldReceiveVelocityUpdates(true));

	// Đăng ký một entity type vào registry
	private static <T extends Entity> EntityType<T> register(String name, EntityType.Builder<T> builder) {
		ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, WorkersMain.id(name));
		return Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(key));
	}

	// Đăng ký entity type và thuộc tính cho toàn bộ worker
	public static void register() {
		FabricDefaultAttributeRegistry.register(FARMER, FarmerEntity.setAttributes());
		FabricDefaultAttributeRegistry.register(LUMBERJACK, LumberjackEntity.setAttributes());
	}
}
