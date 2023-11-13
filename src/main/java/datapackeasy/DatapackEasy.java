package datapackeasy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.WorldSavePath;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;


public class DatapackEasy implements ModInitializer {
	@Override
	public void onInitialize() {

		//Get main directory
		Path mainDir = FabricLoader.getInstance().getGameDir();

		Path datapackSource = mainDir.resolve("Global Datapacks");
		if (!datapackSource.toFile().exists()) {
			datapackSource.toFile().mkdir();
		}

		ServerLifecycleEvents.SERVER_STARTED.register(server -> {
			//Variable to check if datapacks changed
			boolean changed = false;

			//get datapacks from master folder
			List<Path> globalDatapacks = null;
			try (var walk = Files.walk(datapackSource, 1)) {
				globalDatapacks = walk.collect(Collectors.toList());
				globalDatapacks.remove(0);
			} catch (IOException e) {
				System.out.println("Error finding global datapacks!!");
				e.printStackTrace();
			}

			Path worldDatapackDir = server.getSavePath(WorldSavePath.DATAPACKS);

			List<Path> datapacks = null;
			try (var datapackStream = Files.walk(worldDatapackDir, 1)) {
				datapacks = datapackStream.collect(Collectors.toList());
				datapacks.remove(worldDatapackDir);
			} catch (IOException e) {
				e.printStackTrace();
			}

			//copy datapacks over
			if (globalDatapacks != null) {
				for (Path datapack : globalDatapacks) {
					//keep track of datapack in the world folder or not
					boolean alreadyExists = false;
					if (datapacks != null) {
						for (Path localDatapack : datapacks) {
							alreadyExists = datapack.getFileName().equals(localDatapack.getFileName());
						}
					}
					if (!alreadyExists) {
						try {
							Files.copy(datapack, new FileOutputStream(Paths.get(worldDatapackDir.toString(), datapack.toFile().getName()).toString()));
							changed = true;
							System.out.println("Added datapacks to world");
						} catch (IOException e) {
							System.out.println("Error copying datapacks to world folder!");
							e.printStackTrace();
						}
					}
				}
			}

			//Reload the datapacks if needed
			if (changed) {
				server.getCommandManager().executeWithPrefix(server.getCommandSource(), "/reload");
				System.out.println("Reloaded datapacks");
			}
		});

	}
}
