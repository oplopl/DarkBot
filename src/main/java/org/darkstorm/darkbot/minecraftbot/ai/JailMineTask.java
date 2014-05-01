package org.darkstorm.darkbot.minecraftbot.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import org.darkstorm.darkbot.minecraftbot.MinecraftBot;
import org.darkstorm.darkbot.minecraftbot.event.EventHandler;
import org.darkstorm.darkbot.minecraftbot.event.EventListener;
import org.darkstorm.darkbot.minecraftbot.event.protocol.server.RespawnEvent;
import org.darkstorm.darkbot.minecraftbot.event.protocol.server.TeleportEvent;
import org.darkstorm.darkbot.minecraftbot.world.WorldLocation;
import org.darkstorm.darkbot.minecraftbot.world.block.Block;
import org.darkstorm.darkbot.minecraftbot.world.block.BlockLocation;
import org.darkstorm.darkbot.minecraftbot.world.block.BlockType;
import org.darkstorm.darkbot.minecraftbot.world.block.SignTileEntity;
import org.darkstorm.darkbot.minecraftbot.world.item.ChestInventory;
import org.darkstorm.darkbot.minecraftbot.world.item.ItemStack;

public class JailMineTask implements Task, EventListener {
	private boolean running = false;

	private MinecraftBot bot;
	private BlockLocation nextTarget = null;

	private int waitFor = 0;

	private int emptyInventory = 0;
	private int emptyInventoryCurrentId = -1;

	private boolean teleported = false;

	private int[] target = new int[] {173};

	public JailMineTask(MinecraftBot bot) {
		this.bot = bot;
		bot.getEventBus().register(this);
	}

	@Override
	public boolean isPreconditionMet() {
		return running;
	}

	@Override
	public boolean start(String... options) {
		if (running) return false;
		if (findNextTarget()) {
			running = true;
		} else if (options != null) {
			bot.say("/warp b");
			teleported = true;
			waitFor = 100;
			running = true;
		}
		return running;
	}

	@Override
	public void stop() {
		if (teleported) return;
		running = false;
		nextTarget = null;
		System.out.println("[JailMineTask] Task stopped.");
		try {throw new Exception();} catch (Exception e) {e.printStackTrace();}
	}

	@Override
	public void run() {
		if (bot.getTaskManager().getTaskFor(EatTask.class).isActive()) return;

		if (!running) return;
		if (waitFor > 0) {
			waitFor--;
			return;
		} else {
			teleported = false;
		}

		if (emptyInventory > 0) {
//			System.out.println("TICK! emptyInv=" + emptyInventory + " currId=" + emptyInventoryCurrentId + " teleported=" + teleported);
//			System.out.println("content of inventory: [size=" + bot.getPlayer().getInventory().getSize() + "]");
//			for (int i = 0; i < bot.getPlayer().getInventory().getSize(); i++) {
//				System.out.print("[" + i + ">" + bot.getPlayer().getInventory().getItemAt(i) + "], ");
//			}
//			System.out.println();
//			System.out.println("window = " + bot.getPlayer().getWindow());
//			if (bot.getPlayer().getWindow() != null) {
//				System.out.println("content of window: [size=" + bot.getPlayer().getWindow().getSize() + "]");
//				for (int i = 0; i < bot.getPlayer().getWindow().getSize() + 30; i++) {
//					System.out.print("[" + i + ">" + bot.getPlayer().getWindow().getItemAt(i) + "], ");
//				}
//				System.out.println();
//			}
			switch (emptyInventory) {
			case 1: // Tp
				bot.say("/plotme home oplopl");
				teleported = true;
				waitFor = 200;
				emptyInventory = 2;
				return;
			case 2: // Drop
				// Look for chests around you
				HashMap<Integer, BlockLocation> chests = new HashMap<Integer, BlockLocation>();
				for (int x = -20; x <= 20; x++) for (int y = -5; y <= 5; y++) for (int z = -20; z <= 20; z++) {
					Block b = getBlockWithOffset(x, y, z);
					if (b == null) continue;
					if (b.getId() == 63) {
						String[] text = ((SignTileEntity) bot.getWorld().getTileEntityAt(b.getLocation())).getText();
						int itemId = -1;
						if (text[0].equals("pickrefill")) {
							itemId = -2;
						} else {
							try {
								itemId = Integer.parseInt(text[0]);
							} catch (NumberFormatException e) {}
							if (itemId != -1) {
								chests.put(itemId, b.getLocation());
							}
						}
					}
				}
				if (chests.size() == 0) {
					emptyInventory = 4;
					return;
				}
				// Look for items in your inventory to put into chests that were found
				boolean hasPick = false;
				for (int i = 0; i < 37; i++) {
					if (i == 36) {
						if (!hasPick) {
							emptyInventory = 5;
							return;
						}
						emptyInventory = 4;
						return;
					}
					ItemStack item = bot.getPlayer().getInventory().getItemAt(i);
					if (item == null || item.getId() == 0) continue;
					if (chests.keySet().contains(item.getId())) {
						if (bot.hasActivity()) return;
						BlockLocation chestLoc = chests.get(item.getId());
						if (bot.getPlayer().getDistanceToSquared(chestLoc) > 2) {
							bot.getPlayer().walkTo(chestLoc);
						} else {
							bot.getPlayer().placeBlock(chestLoc, 1);
							emptyInventoryCurrentId = item.getId();
							waitFor = 100;
							emptyInventory = 3;
							return;
						}
					} else if (item.getId() == 278) {
						hasPick = true;
					}
				}
				return;
			case 3: // Dump items into the chest
				if (bot.getPlayer().getWindow() instanceof ChestInventory) {
					ChestInventory chest = (ChestInventory) bot.getPlayer().getWindow();
					System.out.println("loop!");
					for (int i = 0; i < 37; i++) {
						if (i == 36) {
							bot.getPlayer().closeWindow();
							waitFor = 50;
							emptyInventory = 2;
							System.out.println("done with " + emptyInventoryCurrentId + "!");
							return;
						}
						ItemStack item = chest.getItemAt(chest.getSize() + i);
						System.out.println("i=" + (chest.getSize() + i) + " item=" + item);
						if (item != null && item.getId() == emptyInventoryCurrentId) {
							System.out.println("i like this item! invSize=" + bot.getPlayer().getInventory().getSize() + " invSlot=" + i + " invItem=" + item + " chestInvSlot=" + (chest.getSize() + i - 1) + " chestInvItem=" + bot.getPlayer().getWindow().getItemAt(chest.getSize() + i - 1));
							bot.getPlayer().getWindow().selectItemAtWithShift(chest.getSize() + i);
							waitFor = new Random().nextInt(20) + 40;
							return;
						}
					}
				} else if (waitFor == 0) {
					emptyInventory = 4;
					return;
				}
				return;
			case 4:
				bot.say("/msg oplopl I dropped mined stuff in base");
				bot.getPlayer().closeWindow();
				bot.say("/warp b");
				teleported = true;
				emptyInventory = 0;
				waitFor = 100;
				return;
			case 5:
				// Look for chests around you
				chests = new HashMap<Integer, BlockLocation>();
				for (int x = -20; x <= 20; x++) for (int y = -5; y <= 3; y++) for (int z = -20; z <= 20; z++) {
					Block b = getBlockWithOffset(x, y, z);
					if (b == null) continue;
					if (b.getId() == 63) {
						String[] text = ((SignTileEntity) bot.getWorld().getTileEntityAt(b.getLocation())).getText();
						int itemId = -1;
						if (text[0].equals("pickrefill")) {
							itemId = -2;
						} else {
							try {
								itemId = Integer.parseInt(text[0]);
							} catch (NumberFormatException e) {}
						}
						if (itemId != -1) {
							chests.put(itemId, b.getLocation());
						}
					}
				}
				if (chests.size() == 0) {
					emptyInventory = 4;
					return;
				}
				BlockLocation chestLoc = chests.get(-2);
				if (chestLoc == null) {
					bot.say("/kit c");
					emptyInventory = 4;
					waitFor = 10;
					return;
				} else {
					if (bot.getPlayer().getWindow() instanceof ChestInventory) {
						ChestInventory chest = (ChestInventory) bot.getPlayer().getWindow();
						boolean foundPick = false;
						for (int i = 0; i < chest.getSize(); i++) {
							ItemStack item = chest.getItemAt(i);
							if (item != null && item.getId() == 278) {
								chest.selectItemAtWithShift(i);
								foundPick = true;
								break;
							}
						}
						emptyInventory = 4;
						bot.getPlayer().closeWindow();
						waitFor = 5;
						if (!foundPick) {
							bot.say("/kit c");
						}
						return;
					} else {
						if (bot.hasActivity()) return;
						if (bot.getPlayer().getDistanceToSquared(chestLoc) > 2) {
							bot.getPlayer().walkTo(chestLoc);
							return;
						} else {
							bot.getPlayer().placeBlock(chestLoc, 1);
							return;
						}
					}
				}
			}
			return;
		}

		boolean emptySlots = false;
		boolean foundPick = false;
		for (int i = 0; i < 36; i++) {
			ItemStack item = bot.getPlayer().getInventory().getItemAt(i);
			if (item == null) {
				emptySlots = true;
			} else if (item.getId() == 278) {
				foundPick = true;
			}
		}
		if (!emptySlots || !foundPick) {
			emptyInventory = 1;
		}

		if (checkSurrounding()) { // Mine
			if (!bot.getPlayer().breakBlock(nextTarget)) {
				System.out.println("[JailMineTask] Can't break block! " + nextTarget.toString());
				stop();
			}
			nextTarget = null;
		} else { // Walk/Dig to the target
			System.out.println("next target: " + nextTarget);
			if (nextTarget == null || !isTarget(bot.getWorld().getBlockAt(nextTarget))) {
				if (!findNextTarget()) {
					System.out.println("[JailMineTask] Can't find any target blocks!");
					stop();
					return;
				}
			}
			if (bot.getActivity() != null) return;
			BlockLocation pp = new BlockLocation(bot.getPlayer().getLocation());
			int x = (pp.getX() > nextTarget.getX() ? -1 : pp.getX() < nextTarget.getX() ? 1 : 0);
			int z = (pp.getZ() > nextTarget.getZ() ? -1 : pp.getZ() < nextTarget.getZ() ? 1 : 0);
			if (x != 0 && z != 0) x = 0;
			int y = 20;
			if (!isEmpty(getBlockWithOffset(x, 1, z))) {
				y = 1;
			} else if (!isEmpty(getBlockWithOffset(x, 0, z))) {
				y = 0;
			} else if (pp.getY() > nextTarget.getY() && !isEmpty(getBlockWithOffset(x, -1, z)) && pp.getX() == nextTarget.getX() && pp.getZ() == nextTarget.getZ()) {
				y = -1;
			} else if (pp.getY() + 1 < nextTarget.getY() && !isEmpty(getBlockWithOffset(0, 2, 0)) && pp.getX() == nextTarget.getX() && pp.getZ() == nextTarget.getZ()) {
				y = 2;
				x = z = 0;
			}
			System.out.println("x: " + x + " y: " + y + " z: " + z);
			if (y == 20 && x == 0 && z == 0) {
				findNextTarget();
				return;
			}
			if (y != 20 && !isEmpty(bot.getWorld().getBlockAt(pp.offset(x, y, z)))) {
				System.out.println("mining it");
				bot.getPlayer().breakBlock(pp.offset(x, y, z));
				return;
			} else {
				bot.getPlayer().walkTo(pp.offset(x, (y != 20 ? y : 0), z));
				return;
			}
		}
	}

	private boolean findNextTarget() {
		ArrayList<Block> potentialTargets = new ArrayList<Block>();
		for (int x = -15; x <= 15; x++) for (int y = -10; y <= 4; y++) for (int z = 15; z >= -15; z--) {
			Block b = bot.getWorld().getBlockAt((int) bot.getPlayer().getLocation().getX() + x, (int) bot.getPlayer().getLocation().getY() + y, (int) bot.getPlayer().getLocation().getZ() + z);
			for (int id : target)
				if (b != null && b.getId() == id)
					potentialTargets.add(b);
		}

		Block closest = null;
		int distToPlayer = Integer.MAX_VALUE;
		for (Block b : potentialTargets) {
			if (closest == null) {
				closest = b;
				continue;
			}
			int distSquared = b.getLocation().getDistanceToSquared(new BlockLocation(bot.getPlayer().getLocation()));
			if (b.getLocation().getY() < bot.getPlayer().getLocation().getY()) distSquared += 20;
			if (distToPlayer > distSquared) {
				closest = b;
				distToPlayer = distSquared;
			}
		}
		if (closest != null) {
			nextTarget = closest.getLocation();
			return true;
		} else {
			return false;
		}
	}

	private boolean checkSurrounding() {
		if (!isEmpty(getBlockWithOffset(0, 1, 0)) || !isEmpty(getBlockWithOffset(0, 0, 0))) {
			bot.say("/warp b");
			teleported = true;
			waitFor = 200;
			nextTarget = null;
		} else if (isTarget(0, 2, 0)) {
			nextTarget = getBlockWithOffset(0, 2, 0).getLocation();
		} else if (isTarget(getBlockWithOffset(0, 3, 0)) && isEmpty(getBlockWithOffset(0, 2, 0))) {
			nextTarget = getBlockWithOffset(0, 3, 0).getLocation();
		} else if (isTarget(getBlockWithOffset(0, 4, 0)) && isEmpty(getBlockWithOffset(0, 2, 0)) && isEmpty(getBlockWithOffset(0, 3, 0))) {
			nextTarget = getBlockWithOffset(0, 4, 0).getLocation();
		} else if (isTarget(-1, 0, 0)) {
			nextTarget = getBlockWithOffset(-1, 0, 0).getLocation();
		} else if (isTarget(-1, 1, 0)) {
			nextTarget = getBlockWithOffset(-1, 1, 0).getLocation();
		} else if (isTarget(1, 0, 0)) {
			nextTarget = getBlockWithOffset(1, 0, 0).getLocation();
		} else if (isTarget(1, 1, 0)) {
			nextTarget = getBlockWithOffset(1, 1, 0).getLocation();
		} else if (isTarget(0, 0, -1)) {
			nextTarget = getBlockWithOffset(0, 0, -1).getLocation();
		} else if (isTarget(0, 1, -1)) {
			nextTarget = getBlockWithOffset(0, 1, -1).getLocation();
		} else if (isTarget(0, 0, 1)) {
			nextTarget = getBlockWithOffset(0, 0, 1).getLocation();
		} else if (isTarget(0, 1, 1)) {
			nextTarget = getBlockWithOffset(0, 1, 1).getLocation();
		} else if (isTarget(0, -1, 0)) {
			nextTarget = getBlockWithOffset(0, -1, 0).getLocation();
		} else {
			return false;
		}
		return true;
	}

	private Block getBlockWithOffset(int xOffset, int yOffset, int zOffset) {
		return bot.getWorld().getBlockAt((int) bot.getPlayer().getLocation().getX() + xOffset, (int) bot.getPlayer().getLocation().getY() + yOffset, (int) bot.getPlayer().getLocation().getZ() + zOffset);
	}

	private boolean isTarget(int xOffset, int yOffset, int zOffset) {
		return isTarget(getBlockWithOffset(xOffset, yOffset, zOffset));
	}

	private boolean isTarget(Block b) {
		for (int id : target)
			if (b.getId() == id)
				return true;
		return false;
	}

	private boolean isEmpty(Block b) {
		BlockType bt = BlockType.getById(b.getId());
		return !bt.isSolid();
	}

	@EventHandler
	public void playerTeleport(TeleportEvent event) {
		bot.getPlayer().setLocation(new WorldLocation(event.getX(), event.getY(), event.getZ()));
		waitFor = new Random().nextInt(100) + 10;
	}

	@EventHandler
	public void playerRespawn(RespawnEvent event) {
		waitFor = new Random().nextInt(100) + 10;
	}

	public void forceEmptyInventory() {
		emptyInventory = 1;
	}

	@Override
	public boolean isActive() {
		return running;
	}

	@Override
	public TaskPriority getPriority() {
		return TaskPriority.NORMAL;
	}

	@Override
	public boolean isExclusive() {
		return false;
	}

	@Override
	public boolean ignoresExclusive() {
		return false;
	}

	@Override
	public String getName() {
		return "JailMine";
	}

	@Override
	public String getOptionDescription() {
		return "";
	}

}
