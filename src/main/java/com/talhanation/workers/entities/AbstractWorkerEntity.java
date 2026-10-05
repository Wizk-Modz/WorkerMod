package com.talhanation.workers.entities;

import com.talhanation.workers.world.NeededItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class AbstractWorkerEntity extends PathfinderMob {
	public static final int INVENTORY_SIZE = 15;
	public static final int INVENTORY_START = 6;
	public static final int FOLLOW_STATE_WORK = 6;

	public static final EntityDataAccessor<String> OWNER_UUID =
			SynchedEntityData.defineId(AbstractWorkerEntity.class, EntityDataSerializers.STRING);
	public static final EntityDataAccessor<String> OWNER_NAME =
			SynchedEntityData.defineId(AbstractWorkerEntity.class, EntityDataSerializers.STRING);
	public static final EntityDataAccessor<Integer> FOLLOW_STATE =
			SynchedEntityData.defineId(AbstractWorkerEntity.class, EntityDataSerializers.INT);
	public static final EntityDataAccessor<Float> MORALE =
			SynchedEntityData.defineId(AbstractWorkerEntity.class, EntityDataSerializers.FLOAT);

	public final SimpleContainer inventory = new SimpleContainer(INVENTORY_SIZE);

	@Nullable public UUID homeAreaUUID = null;
	@Nullable public UUID lastStorage = null;
	public boolean isFleeing = false;
	@Nullable public BlockPos fleeTarget = null;
	public List<NeededItem> neededItems = new ArrayList<>();
	public int farmedItems;
	public boolean forcedDeposit;

	private boolean moraleNightChecked = false;

	public AbstractWorkerEntity(EntityType<? extends AbstractWorkerEntity> entityType, Level level) {
		super(entityType, level);
		this.setCanPickUpLoot(true);
	}

	// Khai báo các trường dữ liệu đồng bộ client-server
	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(OWNER_UUID, "");
		builder.define(OWNER_NAME, "");
		builder.define(FOLLOW_STATE, 0);
		builder.define(MORALE, 100.0f);
	}

	// Đọc dữ liệu owner và trạng thái làm việc từ NBT
	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		super.readAdditionalSaveData(input);
		input.getString("ownerUUID").ifPresent(s -> setOwnerUUID(UUID.fromString(s)));
		input.getString("ownerName").ifPresent(this::setOwnerName);
		homeAreaUUID = input.getString("homeAreaUUID").map(UUID::fromString).orElse(null);
		lastStorage = input.getString("lastStorage").map(UUID::fromString).orElse(null);
		farmedItems = input.getIntOr("farmedItems", 0);
	}

	// Ghi dữ liệu owner và trạng thái làm việc xuống NBT
	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		super.addAdditionalSaveData(output);
		UUID owner = getOwnerUUID();
		if (owner != null) output.putString("ownerUUID", owner.toString());
		if (!getOwnerName().isEmpty()) output.putString("ownerName", getOwnerName());
		if (homeAreaUUID != null) output.putString("homeAreaUUID", homeAreaUUID.toString());
		if (lastStorage != null) output.putString("lastStorage", lastStorage.toString());
		output.putInt("farmedItems", farmedItems);
	}

	public boolean isOwned() {
		return getOwnerUUID() != null;
	}

	@Nullable
	public UUID getOwnerUUID() {
		String value = this.entityData.get(OWNER_UUID);
		if (value.isEmpty()) return null;
		try {
			return UUID.fromString(value);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	public String getOwnerName() {
		return this.entityData.get(OWNER_NAME);
	}

	public void setOwnerUUID(@Nullable UUID uuid) {
		this.entityData.set(OWNER_UUID, uuid == null ? "" : uuid.toString());
	}

	public void setOwnerName(String name) {
		this.entityData.set(OWNER_NAME, name);
	}

	// Trả về người chơi sở hữu worker, null nếu chủ không online
	@Nullable
	public Player getOwner() {
		UUID uuid = getOwnerUUID();
		if (uuid == null) return null;
		return level().getPlayerInAnyDimension(uuid);
	}

	public void setOwner(Player player) {
		setOwnerUUID(player.getUUID());
		setOwnerName(player.getName().getString());
	}

	public int getFollowState() {
		return this.entityData.get(FOLLOW_STATE);
	}

	public void setFollowState(int state) {
		this.entityData.set(FOLLOW_STATE, state);
	}

	public float getMorale() {
		return this.entityData.get(MORALE);
	}

	public void setMoral(float value) {
		this.entityData.set(MORALE, value);
	}

	public boolean isWorking() {
		return getFollowState() == FOLLOW_STATE_WORK;
	}

	public SimpleContainer getInventory() {
		return this.inventory;
	}

	// Worker cần ngủ khi trời tối (dùng đồng hồ overworld thay isDay cũ)
	public boolean needsToSleep() {
		long time = level().getOverworldClockTime() % 24000L;
		return time >= 13000L && time < 23000L;
	}

	// Worker chỉ làm việc khi có chủ và không ở trạng thái bận khác
	public boolean shouldWork() {
		return isOwned() && (getFollowState() == 0 || getFollowState() == FOLLOW_STATE_WORK);
	}

	// Đếm số lượng item khớp điều kiện trong kho
	public int countMatchingItems(Predicate<ItemStack> predicate) {
		int count = 0;
		for (int i = INVENTORY_START; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && predicate.test(stack)) count += stack.getCount();
		}
		return count;
	}

	// Đếm số slot chứa item khớp điều kiện
	public int countMatchingStacks(Predicate<ItemStack> predicate) {
		int count = 0;
		for (int i = INVENTORY_START; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && predicate.test(stack)) count++;
		}
		return count;
	}

	// Trả về item đầu tiên khớp điều kiện, null nếu không có
	@Nullable
	public ItemStack getMatchingItem(Predicate<ItemStack> predicate) {
		for (int i = INVENTORY_START; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (!stack.isEmpty() && predicate.test(stack)) return stack;
		}
		return null;
	}

	// Kiểm tra kho còn chỗ chứa item
	public boolean canAddItem(ItemStack itemToAdd) {
		for (int i = INVENTORY_START; i < inventory.getContainerSize(); i++) {
			ItemStack slot = inventory.getItem(i);
			if (slot.isEmpty()) return true;
			if (ItemStack.isSameItemSameComponents(slot, itemToAdd) && slot.getCount() < slot.getMaxStackSize()) {
				return true;
			}
		}
		return false;
	}

	// Thêm item vào kho, trả về phần còn dư
	public ItemStack addItem(ItemStack itemStackToAdd) {
		if (itemStackToAdd.isEmpty()) return ItemStack.EMPTY;

		ItemStack itemstack = itemStackToAdd.copy();
		moveItemToOccupiedSlotsWithSameType(itemstack);
		if (itemstack.isEmpty()) return ItemStack.EMPTY;

		moveItemToEmptySlots(itemstack);
		return itemstack.isEmpty() ? ItemStack.EMPTY : itemstack;
	}

	// Gộp item vào các slot đã có cùng loại
	private void moveItemToOccupiedSlotsWithSameType(ItemStack itemStackToMove) {
		for (int i = INVENTORY_START; i < inventory.getContainerSize(); i++) {
			ItemStack itemstack = inventory.getItem(i);
			if (ItemStack.isSameItemSameComponents(itemstack, itemStackToMove)) {
				moveItemsBetweenStacks(itemStackToMove, itemstack);
				if (itemStackToMove.isEmpty()) return;
			}
		}
	}

	// Đặt item vào slot trống đầu tiên
	private void moveItemToEmptySlots(ItemStack itemStack) {
		for (int i = INVENTORY_START; i < inventory.getContainerSize(); i++) {
			ItemStack itemstack = inventory.getItem(i);
			if (itemstack.isEmpty()) {
				inventory.setItem(i, itemStack.copyAndClear());
				return;
			}
		}
	}

	// Chuyển item giữa hai stack tới giới hạn max stack
	private void moveItemsBetweenStacks(ItemStack from, ItemStack to) {
		int max = Math.min(64, to.getMaxStackSize());
		int moved = Math.min(from.getCount(), max - to.getCount());
		if (moved > 0) {
			to.grow(moved);
			from.shrink(moved);
			inventory.setChanged();
		}
	}

	// Kiểm tra kho còn slot trống
	public boolean hasFreeInvSlot() {
		for (int i = INVENTORY_START; i < inventory.getContainerSize(); i++) {
			if (inventory.getItem(i).isEmpty()) return true;
		}
		return false;
	}

	// Chuyển item khớp điều kiện từ kho lên tay chính
	public void switchMainHandItem(Predicate<ItemStack> predicate) {
		if (!isAlive() || predicate == null) return;

		ItemStack mainHand = getMainHandItem();
		if (predicate.test(mainHand)) return;

		for (int i = INVENTORY_START; i < inventory.getContainerSize(); i++) {
			ItemStack stack = inventory.getItem(i);
			if (predicate.test(stack)) {
				inventory.setItem(i, mainHand);
				setItemInHand(InteractionHand.MAIN_HAND, stack);
				return;
			}
		}
	}

	// Trừ độ bền item đang cầm trên tay chính
	public void damageMainHandItem() {
		ItemStack stack = getMainHandItem();
		if (!stack.isEmpty() && stack.isDamageableItem()) {
			stack.hurtAndBreak(1, this, net.minecraft.world.entity.EquipmentSlot.MAINHAND);
		}
	}

	// Worker giữ lại thức ăn có giá trị dinh dưỡng cao
	public boolean wantsToKeep(ItemStack itemStack) {
		net.minecraft.world.food.FoodProperties food =
				itemStack.get(net.minecraft.core.component.DataComponents.FOOD);
		return food != null && food.nutrition() > 4;
	}

	// Worker nhặt item nếu muốn giữ hoặc item nằm trong danh sách cần
	@Override
	public boolean wantsToPickUp(ItemStack itemStack) {
		if (wantsToKeep(itemStack)) return true;
		for (NeededItem needed : neededItems) {
			if (needed.matches(itemStack)) return true;
		}
		return super.wantsToPickUp(itemStack);
	}

	// Nhặt item từ mặt đất vào kho, cập nhật số item đã farm
	@Override
	protected void pickUpItem(net.minecraft.server.level.ServerLevel serverLevel, ItemEntity itemEntity) {
		ItemStack itemstack = itemEntity.getItem();
		if (!wantsToPickUp(itemstack)) return;
		if (!canAddItem(itemstack)) return;

		onItemPickup(itemEntity);
		take(itemEntity, itemstack.getCount());
		ItemStack remainder = addItem(itemstack);

		farmedItems += itemstack.getCount() - remainder.getCount();
		NeededItem.applyToNeededItems(itemstack, neededItems);

		if (remainder.isEmpty()) {
			itemEntity.discard();
		} else {
			itemstack.setCount(remainder.getCount());
		}
	}

	// Thêm yêu cầu item, gộp với yêu cầu cùng loại cùng nguồn nếu có
	public void addNeededItem(NeededItem neededItem) {
		for (NeededItem existing : neededItems) {
			if (existing.isSameRequest(neededItem)) {
				existing.count = Math.max(existing.count, neededItem.count);
				return;
			}
		}
		neededItems.add(neededItem);
	}

	public boolean needsToGetItems() {
		return neededItems.stream().anyMatch(neededItem -> neededItem.required);
	}

	public boolean needsToDeposit() {
		return forcedDeposit || farmedItems > 128;
	}

	// Worker cần tới chest khi thiếu item, cần trả đồ hoặc cần lấy đồ
	public boolean needsToGetToChest() {
		return needsToDeposit() || needsToGetItems();
	}

	private long lastNotifyDay = Long.MIN_VALUE;
	private boolean notifyGateOpen = true;

	// Mở lại gate thông báo cho chủ
	public void resetNotifyGate() {
		this.notifyGateOpen = true;
	}

	// Giới hạn thông báo cho chủ tối đa một lần mỗi ngày
	public boolean canNotifyOwner() {
		long day = level().getOverworldClockTime() / 24000L;
		if (day != lastNotifyDay) return true;
		return notifyGateOpen;
	}

	// Gửi thông báo cho chủ, có chống spam theo ngày
	public void notifyOwner(Component message) {
		if (!canNotifyOwner()) return;
		Player owner = getOwner();
		if (owner != null) {
			owner.sendSystemMessage(message);
			this.lastNotifyDay = level().getOverworldClockTime() / 24000L;
			this.notifyGateOpen = false;
		}
	}

	// Gửi thông báo cho chủ không qua chống spam, dùng cho việc quan trọng
	public void notifyOwnerAlways(Component message) {
		Player owner = getOwner();
		if (owner != null) {
			owner.sendSystemMessage(message);
		}
	}

	// Giảm hoặc hồi morale theo ngày tùy worker có nhà hay không
	private void tickMorale() {
		if (level().isClientSide()) return;

		if (needsToSleep() && !moraleNightChecked) {
			moraleNightChecked = true;
			float morale = getMorale();

			if (homeAreaUUID == null) {
				morale = Math.max(0, morale - 10);
			} else if (morale < 60) {
				morale = Math.min(100, morale + 5);
			}
			setMoral(morale);
		}

		if (!needsToSleep()) {
			moraleNightChecked = false;
		}
	}

	// Cho worker bắt đầu bỏ chạy tới vị trí chỉ định
	public void startFleeing(BlockPos target) {
		this.isFleeing = true;
		this.fleeTarget = target;
		this.setFollowState(FOLLOW_STATE_WORK);
	}

	public void stopFleeing() {
		this.isFleeing = false;
		this.fleeTarget = null;
	}

	// Cho worker nghỉ việc và trả về trạng thái rảnh
	public void disband() {
		setOwnerUUID(null);
		setOwnerName("");
		setFollowState(0);
	}

	// Gán chủ cho worker
	public void hire(Player player) {
		setOwner(player);
		playSound(SoundEvents.VILLAGER_YES, 1.0F, 1.0F);
	}

	// Tick AI chung: nhặt đồ xung quanh và cập nhật morale định kỳ
	@Override
	public void aiStep() {
		super.aiStep();
		if (!(level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) return;

		if (canPickUpLoot() && isAlive() && !dead) {
			List<ItemEntity> nearbyItems = level().getEntities(
					net.minecraft.world.level.entity.EntityTypeTest.forClass(ItemEntity.class),
					getBoundingBox().inflate(5.5D), e -> true);
			for (ItemEntity itemEntity : nearbyItems) {
				if (!itemEntity.isRemoved() && !itemEntity.getItem().isEmpty()
						&& !itemEntity.hasPickUpDelay() && wantsToPickUp(itemEntity.getItem())) {
					pickUpItem(serverLevel, itemEntity);
				}
			}
		}

		if (tickCount % 20 == 0) {
			tickMorale();
		}
	}

	// Xử lý chuột phải: nhận làm chủ nếu chưa có chủ, mở lại gate thông báo nếu là chủ
	@Override
	protected InteractionResult mobInteract(Player player, InteractionHand hand) {
		if (!level().isClientSide()) {
			if (!isOwned()) {
				hire(player);
			} else if (player.getUUID().equals(getOwnerUUID())) {
				resetNotifyGate();
			}
		}
		return InteractionResult.SUCCESS;
	}

	// Giải phóng work area và nhà khi worker chết
	@Override
	public void die(net.minecraft.world.damagesource.DamageSource damageSource) {
		super.die(damageSource);
		releaseHomeArea();
	}

	// Giải phóng nhà đang ở để worker khác có thể nhận
	public void releaseHomeArea() {
		homeAreaUUID = null;
	}
}
