package com.talhanation.workers;

import com.talhanation.workers.config.WorkersConfig;
import com.talhanation.workers.init.ModEntityTypes;
import com.talhanation.workers.init.ModItems;
import net.fabricmc.api.ModInitializer;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkersMain implements ModInitializer {
	public static final String MOD_ID = "workers";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// Khởi tạo mod: đăng ký config, entity, item và network
	@Override
	public void onInitialize() {
		WorkersConfig.load();
		ModEntityTypes.register();
		ModItems.register();
		LOGGER.info("Villager Workers loading");
	}

	// Tạo Identifier trong namespace của mod
	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath(MOD_ID, path);
	}
}
