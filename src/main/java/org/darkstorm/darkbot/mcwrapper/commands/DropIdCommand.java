package org.darkstorm.darkbot.mcwrapper.commands;

import org.darkstorm.darkbot.mcwrapper.MinecraftBotWrapper;
import org.darkstorm.darkbot.minecraftbot.world.entity.MainPlayerEntity;
import org.darkstorm.darkbot.minecraftbot.world.item.*;

public class DropIdCommand extends AbstractCommand {

	public DropIdCommand(MinecraftBotWrapper bot) {
		super(bot, "dropid", "Drop all items of a specific ID", "<id>",
				"[0-9]+");
	}

	@Override
	public void execute(String[] args) {
		MainPlayerEntity player = bot.getPlayer();
		PlayerInventory inventory = player.getInventory();
		int id = Integer.parseInt(args[0]);
		for(int slot = 0; slot < 40; slot++) {
			ItemStack item = inventory.getItemAt(slot);
			System.out.println(item != null ? item.getId() : "null");
			if(item != null && item.getId() == id) {
				inventory.selectItemAt(slot, true);
				inventory.dropSelectedItem();
			}
		}
		inventory.close();
		controller.say("/msg " + controller.getOwners()[0] + " Dropped all items of ID " + id + "!");
	}
}
