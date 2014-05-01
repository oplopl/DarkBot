package org.darkstorm.darkbot.mcwrapper.commands;

import org.darkstorm.darkbot.mcwrapper.MinecraftBotWrapper;
import org.darkstorm.darkbot.minecraftbot.ai.HostileTask;

public class AttackAllCommand extends AbstractCommand {

	public AttackAllCommand(MinecraftBotWrapper bot) {
		super(bot, "attackall", "Attack all nearby players and monsters");
	}

	@Override
	public void execute(String[] args) {
		HostileTask task = bot.getTaskManager().getTaskFor(HostileTask.class);
		if(task.isActive()) {
			task.stop();
			controller.say("/msg " + controller.getOwners()[0] + " No longer in hostile mode.");
		} else {
			task.start();
			controller.say("/msg " + controller.getOwners()[0] + " Now in hostile mode!");
		}
	}
}
