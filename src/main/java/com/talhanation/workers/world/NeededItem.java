package com.talhanation.workers.world;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class NeededItem {
	public final Predicate<ItemStack> matcher;
	public int count;
	public final boolean required;

	/**
	 * Định danh nguồn (thường là UUID của work area) đã đẩy yêu cầu này.
	 * Hai NeededItem cùng matchKey nhưng khác sourceKey được giữ riêng,
	 * để hai ruộng khác nhau cùng cần bone meal không gộp count vào nhau.
	 */
	public final Object sourceKey;

	/** Khóa cache "matcher này chấp nhận item gì", dùng cho isSameRequest. */
	private Object cachedMatchKey;
	private boolean matchKeyResolved;

	public NeededItem(Predicate<ItemStack> matcher, int count, boolean required) {
		this(matcher, count, required, null, null);
	}

	public NeededItem(Predicate<ItemStack> matcher, int count, boolean required, Object sourceKey) {
		this(matcher, count, required, null, sourceKey);
	}

	public NeededItem(Predicate<ItemStack> matcher, int count, boolean required, Object matchKey, Object sourceKey) {
		this.matcher = matcher;
		this.count = count;
		this.required = required;
		this.sourceKey = sourceKey;
		if (matchKey != null) {
			this.cachedMatchKey = matchKey;
			this.matchKeyResolved = true;
		}
	}

	// Trả về khóa định danh loại item mà matcher chấp nhận, cache lại sau lần đầu
	public Object getMatchKey() {
		if (!matchKeyResolved) {
			cachedMatchKey = tryExtractSingleItemFromMatcher();
			matchKeyResolved = true;
		}
		return cachedMatchKey;
	}

	// Hai yêu cầu là "cùng loại" nếu khớp cùng item và cùng nguồn
	public boolean isSameRequest(NeededItem other) {
		Object myKey = this.getMatchKey();
		Object otherKey = other.getMatchKey();
		if (myKey == null || otherKey == null) return false;
		if (!Objects.equals(myKey, otherKey)) return false;
		return Objects.equals(this.sourceKey, other.sourceKey);
	}

	// Kiểm tra item có thỏa matcher không
	public boolean matches(ItemStack stack) {
		if (stack == null || stack.isEmpty()) return false;
		return matcher.test(stack);
	}

	// Tìm item đầu tiên trong registry mà matcher chấp nhận
	private Item tryExtractSingleItemFromMatcher() {
		for (Item item : BuiltInRegistries.ITEM) {
			if (matcher.test(new ItemStack(item))) {
				return item;
			}
		}
		return null;
	}

	// Mô tả yêu cầu dạng "số lượng x tên item"
	@Override
	public String toString() {
		Item displayItem = tryExtractSingleItemFromMatcher();
		String name = displayItem != null ? new ItemStack(displayItem).getHoverName().getString() : "Unknown Item";
		return count + "x " + name;
	}

	// Trừ số lượng item vừa nhận khỏi danh sách yêu cầu
	public static void applyToNeededItems(ItemStack stack, List<NeededItem> neededItems) {
		if (stack.isEmpty()) return;

		for (int i = neededItems.size() - 1; i >= 0; i--) {
			NeededItem needed = neededItems.get(i);

			if (needed.matches(stack)) {
				int toUse = Math.min(stack.getCount(), needed.count);
				stack.shrink(toUse);

				if (toUse == needed.count) {
					neededItems.remove(i);
				} else {
					needed.count -= toUse;
				}
				return;
			}
		}
	}
}
