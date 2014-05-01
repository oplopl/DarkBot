package org.darkstorm.darkbot.mcwrapper.commands;

import org.darkstorm.darkbot.mcwrapper.MinecraftBotWrapper;
import org.darkstorm.darkbot.minecraftbot.ai.*;
import org.darkstorm.darkbot.minecraftbot.world.entity.MainPlayerEntity;

public class StatusCommand extends AbstractCommand {

	public StatusCommand(MinecraftBotWrapper bot) {
		super(bot, "status", "Display bot state information");
	}

	@Override
	public void execute(String[] args) {
		MainPlayerEntity player = bot.getPlayer();
		if(player == null)
			return;

		controller.say("/msg " + controller.getOwners()[0] + " Health: [" + player.getHealth() + "/20] Hunger: [" + player.getHunger() + "/20] Level " + player.getExperienceLevel() + " (" + player.getExperienceTotal() + " total exp.)");
		try {
			StringBuilder activeTasks = new StringBuilder();
			TaskManager manager = bot.getTaskManager();
			for(Task task : manager.getRegisteredTasks()) {
				if(task.isActive()) {
					if(activeTasks.length() > 0)
						activeTasks.append(", ");
					activeTasks.append(task.getClass().getSimpleName());
				}
			}
			bot.say("/msg " + controller.getOwners()[0] + " Active tasks: [" + activeTasks + "]");
		} catch(Exception exception) {
			exception.printStackTrace();
		}
	}
}
