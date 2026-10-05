package com.talhanation.workers.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.talhanation.workers.WorkersMain;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class WorkersConfig {
	private static WorkersConfig instance;

	public int farmerCost = 10;
	public int lumberjackCost = 12;
	public int minerCost = 16;
	public int builderCost = 20;
	public int merchantCost = 30;
	public int courierCost = 20;
	public int cookCost = 25;
	public int animalPenMaxAnimals = 32;
	public boolean shouldOnlyPlacingBuildingsBePossible = false;
	public boolean shouldWorkAreaOnlyBeInFactionClaim = false;
	public boolean workersReplaceAllay = true;
	public BuildMode buildMode = BuildMode.FREE;

	public List<String> farmerPickup = List.of(
			"minecraft:wheat",
			"minecraft:wheat_seeds",
			"minecraft:beetroot_seeds",
			"minecraft:carrot",
			"minecraft:potato",
			"minecraft:poisonous_potato",
			"minecraft:pumpkin",
			"minecraft:pumpkin_seeds",
			"minecraft:melon",
			"minecraft:melon_seeds",
			"minecraft:melon_slice",
			"supplementaries:flax",
			"farmersdelight:straw",
			"herbalbrews:green_tea_leaf"
	);

	public List<String> minerPickup = List.of(
			"minecraft:torch",
			"minecraft:lantern",
			"minecraft:redstone",
			"minecraft:cobblestone",
			"minecraft:deepslate",
			"minecraft:cobbled_deepslate",
			"minecraft:sandstone",
			"minecraft:sand",
			"minecraft:gravel",
			"minecraft:flint",
			"minecraft:coal",
			"minecraft:calcite",
			"minecraft:clay_ball",
			"minecraft:dripstone_block",
			"minecraft:pointed_dripstone",
			"minecraft:netherrack",
			"minecraft:emerald",
			"minecraft:lapis_lazuli",
			"minecraft:diamond",
			"psg:raw_potassium",
			"psg:sulphur"
	);

	public List<String> minerIgnore = List.of(
			"minecraft:air",
			"minecraft:cave_air",
			"minecraft:torch",
			"minecraft:wall_torch",
			"minecraft:lantern",
			"minecraft:lever",
			"minecraft:redstone_torch",
			"minecraft:redstone_wall_torch",
			"minecraft:redstone_wire",
			"minecraft:redstone_lamp",
			"minecraft:rail",
			"minecraft:water",
			"minecraft:soul_lantern",
			"minecraft:soul_torch",
			"minecraft:soul_wall_torch",
			"minecraft:netherrack"
	);

	public List<String> animalFarmerPickup = List.of(
			"minecraft:feather",
			"minecraft:leather",
			"minecraft:milk_bucket",
			"minecraft:chicken",
			"minecraft:mutton",
			"minecraft:egg",
			"minecraft:beef",
			"minecraft:rabbit",
			"naturalist:duck",
			"naturalist:venison",
			"naturalist:fur",
			"naturalist:bushmeat",
			"naturalist:antler",
			"naturalist:duck_egg",
			"farmersdelight:ham"
	);

	public List<String> fishermanPickup = List.of(
			"minecraft:tropical_fish",
			"minecraft:pufferfish",
			"naturalist:catfish",
			"naturalist:bass"
	);

	public List<String> lumberjackPickup = List.of(
			"minecraft:stick",
			"minecraft:bee_nest",
			"dynamictrees:oak_seed",
			"dynamictrees:birch_seed",
			"dynamictrees:apple_oak_seed",
			"dynamictrees:spruce_seed",
			"dynamictrees:cocoa_seed"
	);

	// Trả về config đã load, load lần đầu nếu chưa có
	public static WorkersConfig get() {
		if (instance == null) load();
		return instance;
	}

	// Đọc config từ đĩa, tạo file default nếu chưa tồn tại
	public static void load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve("workers.json");
		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		if (Files.exists(path)) {
			try {
				instance = gson.fromJson(Files.readString(path), WorkersConfig.class);
			} catch (IOException e) {
				WorkersMain.LOGGER.error("Không đọc được workers.json, dùng giá trị mặc định", e);
				instance = new WorkersConfig();
			}
		} else {
			instance = new WorkersConfig();
			try {
				Files.writeString(path, gson.toJson(instance));
			} catch (IOException e) {
				WorkersMain.LOGGER.error("Không ghi được workers.json", e);
			}
		}
		if (instance == null) instance = new WorkersConfig();
	}
}
