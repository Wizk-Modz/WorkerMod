package com.talhanation.workers.entities;

import com.google.common.collect.ImmutableSet;
import com.talhanation.workers.config.WorkersConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;

import javax.annotation.Nullable;
import java.util.Set;

public class FarmerEntity extends AbstractWorkerEntity {
	public static final Set<Block> TILLABLES = ImmutableSet.of(
			Blocks.DIRT,
			Blocks.ROOTED_DIRT,
			Blocks.COARSE_DIRT,
			Blocks.GRASS_BLOCK);

	public FarmerEntity(EntityType<? extends AbstractWorkerEntity> entityType, Level level) {
		super(entityType, level);
	}

	// Gọi thiết lập ban đầu khi farmer được spawn lần đầu
	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
			EntitySpawnReason reason, @Nullable SpawnGroupData spawnGroupData) {
		initSpawn();
		return super.finalizeSpawn(level, difficulty, reason, spawnGroupData);
	}

	// Khai báo thuộc tính của farmer
	public static AttributeSupplier.Builder setAttributes() {
		return createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.1D)
				.add(Attributes.ATTACK_DAMAGE, 0.5D)
				.add(Attributes.FOLLOW_RANGE, 32.0D)
				.add(Attributes.ATTACK_SPEED);
	}

	// Gán tên và thiết lập ban đầu khi farmer spawn
	public void initSpawn() {
		setCustomName(Component.literal("Farmer"));
		setPersistenceRequired();
	}

	// Farmer giữ lại cuốc và hạt giống của ruộng đang làm
	@Override
	public boolean wantsToKeep(ItemStack itemStack) {
		if (itemStack.getItem() instanceof net.minecraft.world.item.HoeItem) {
			return countMatchingItems(stack -> stack.getItem() instanceof net.minecraft.world.item.HoeItem) <= 1;
		}
		return super.wantsToKeep(itemStack);
	}

	// Farmer nhặt nông sản, hạt giống và các item trong config
	@Override
	public boolean wantsToPickUp(ItemStack itemStack) {
		Identifier id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
		if (id == null) return false;

		if (WorkersConfig.get().farmerPickup.contains(id.toString())) return true;
		if (itemStack.getItem() instanceof BlockItem blockItem
				&& blockItem.getBlock() instanceof CropBlock) return true;

		return super.wantsToPickUp(itemStack);
	}
}
