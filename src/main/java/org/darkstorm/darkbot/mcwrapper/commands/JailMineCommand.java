package org.darkstorm.darkbot.mcwrapper.commands;

import org.darkstorm.darkbot.mcwrapper.MinecraftBotWrapper;
import org.darkstorm.darkbot.minecraftbot.ai.JailMineTask;

public class JailMineCommand extends AbstractCommand {
	public JailMineCommand(MinecraftBotWrapper bot) {
		super(bot, "jailmine", "Mine in jail mine", "", ".*");
	}

	@Override
	public void execute(String[] args) {
		JailMineTask jailMine = bot.getTaskManager().getTaskFor(JailMineTask.class);
		if (!jailMine.isActive()) {
			if (jailMine.start((String[]) null)) {
				bot.say("/msg " + controller.getOwners()[0] + " Started mining!");
			} else {
				bot.say("/msg " + controller.getOwners()[0] + " Can't start mining!");
			}
		} else {
			if (args.length > 0) {
				if (args[0].equalsIgnoreCase("drop")) {
					jailMine.forceEmptyInventory();
					return;
				}
			}
			bot.say("/msg " + controller.getOwners()[0] + " Aborting mining");
			jailMine.stop();
		}
	}
}
