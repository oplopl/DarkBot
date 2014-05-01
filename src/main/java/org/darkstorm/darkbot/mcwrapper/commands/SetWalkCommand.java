package org.darkstorm.darkbot.mcwrapper.commands;

import org.darkstorm.darkbot.mcwrapper.MinecraftBotWrapper;
import org.darkstorm.darkbot.minecraftbot.ai.WalkActivity;

public class SetWalkCommand extends AbstractCommand {

	public SetWalkCommand(MinecraftBotWrapper bot) {
		super(bot, "walksettings", "Change walk settings",
				"<speed> [jump-factor [fall-factor [liquid-factor]]]",
				"[-]?[0-9]+(\\.[0-9]+)?" + "( [-]?[0-9]+(\\.[0-9]+)?"
						+ "( [-]?[0-9]+(\\.[0-9]+)?"
						+ "( [-]?[0-9]+(\\.[0-9]+)?)?)?)?");
	}

	@Override
	public void execute(String[] args) {
		controller.say("/msg " + controller.getOwners()[0] + " Set walk settings!");
		WalkActivity.setDefaultSpeed(Double.parseDouble(args[0]));
		if(args.length == 1)
			return;
		WalkActivity.setDefaultJumpFactor(Double.parseDouble(args[1]));
		if(args.length == 2)
			return;
		WalkActivity.setDefaultFallFactor(Double.parseDouble(args[2]));
		if(args.length == 3)
			return;
		WalkActivity.setDefaultLiquidFactor(Double.parseDouble(args[3]));
	}
}
