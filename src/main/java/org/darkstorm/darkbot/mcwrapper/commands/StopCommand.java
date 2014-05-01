package org.darkstorm.darkbot.mcwrapper.commands;

import org.darkstorm.darkbot.mcwrapper.MinecraftBotWrapper;

public class StopCommand extends AbstractCommand {

	public StopCommand(MinecraftBotWrapper bot) {
		super(bot, "stop", "Stop all tasks and activities");
	}

	@Override
	public void execute(String[] args) {
		bot.getTaskManager().stopAll();
		bot.setActivity(null);
		controller.say("/msg " + controller.getOwners()[0] + " Stopped all tasks.");
	}
}
