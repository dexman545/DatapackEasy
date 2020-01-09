package datapackeasy;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.loader.api.FabricLoader;

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
		Path mainDir = FabricLoader.getInstance().getGameDirectory().toPath();

		Path datapackSource = mainDir.resolve("Global Datapacks");
		if (!datapackSource.toFile().exists()) {
			datapackSource.toFile().mkdir();
		}

		ServerStartCallback.EVENT.register(t -> {
			//Variable to check if datapacks changed
			boolean changed = false;

			//get datapacks from master folder
			List<Path> globalDatapacks = null;
			try {
				globalDatapacks = Files.walk(datapackSource, 1)
						.collect(Collectors.toList());
				globalDatapacks.remove(0);
			} catch (IOException e) {
				System.out.println("Error finding global datapacks!!");
				e.printStackTrace();
			}

			//Save directory
			Path saveDir = mainDir.resolve("saves");

			//get world folders
			List<Path> worldfolders = null;
			try {
				worldfolders = Files.walk(saveDir, 1)
						.filter(Files::isDirectory)
						.collect(Collectors.toList());
				worldfolders.remove(saveDir);
			} catch (IOException e) {
				System.out.println("Error finding world folders!");
				e.printStackTrace();
			}

			//get datapack folders
			if (worldfolders != null) {
				for (Path worldFolder : worldfolders) {
					Path worldDatapackDir = worldFolder.resolve("datapacks");
					//for (Path wldDp : worldDatapackDir) {
						List<Path> datapacks = null;
						try {
							datapacks = Files.walk(worldDatapackDir, 1)
									.collect(Collectors.toList());
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

					}
				//}
			}

			//Reload the datapacks if needed
			if (changed) {
				System.out.println("Reloaded datapacks");
				t.reload();
			}

		});

	}
}
