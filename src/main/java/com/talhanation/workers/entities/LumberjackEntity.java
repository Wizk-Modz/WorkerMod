package com.talhanation.workers.entities;

import com.talhanation.workers.config.WorkersConfig;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.SaplingBlock;

import org.jetbrains.annotations.Nullable;

public class LumberjackEntity extends AbstractWorkerEntity {

	public LumberjackEntity(EntityType<? extends AbstractWorkerEntity> entityType, Level level) {
		super(entityType, level);
	}

	// Gọi thiết lập ban đầu khi lumberjack được spawn lần đầu
	@Nullable
	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
			EntitySpawnReason reason, @Nullable SpawnGroupData spawnGroupData) {
		initSpawn();
		return super.finalizeSpawn(level, difficulty, reason, spawnGroupData);
	}

	// Khai báo thuộc tính của lumberjack
	public static AttributeSupplier.Builder setAttributes() {
		return createMobAttributes()
				.add(Attributes.MAX_HEALTH, 20.0D)
				.add(Attributes.MOVEMENT_SPEED, 0.3D)
				.add(Attributes.KNOCKBACK_RESISTANCE, 0.1D)
				.add(Attributes.ATTACK_DAMAGE, 0.5D)
				.add(Attributes.FOLLOW_RANGE, 32.0D)
				.add(Attributes.ATTACK_SPEED);
	}

	// Gán tên và thiết lập ban đầu khi lumberjack spawn
	public void initSpawn() {
		setCustomName(Component.literal("Lumberjack"));
		setPersistenceRequired();
	}

	// Lumberjack giữ lại rìu đang dùng
	@Override
	public boolean wantsToKeep(ItemStack itemStack) {
		if (itemStack.getItem() instanceof AxeItem) {
			return countMatchingItems(stack -> stack.getItem() instanceof AxeItem) <= 1;
		}
		return super.wantsToKeep(itemStack);
	}

	// Lumberjack nhặt gỗ, lá, cây giống và các item trong config
	@Override
	public boolean wantsToPickUp(ItemStack itemStack) {
		Identifier id = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
		if (id == null) return false;

		if (WorkersConfig.get().lumberjackPickup.contains(id.toString())) return true;

		if (itemStack.getItem() instanceof BlockItem blockItem) {
			var state = blockItem.getBlock().defaultBlockState();
			if (blockItem.getBlock() instanceof SaplingBlock) return true;
			if (state.is(BlockTags.LOGS)) return true;
			if (state.is(BlockTags.LEAVES)) return true;
		}

		return super.wantsToPickUp(itemStack);
	}
}
