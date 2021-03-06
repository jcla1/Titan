package org.maxgamer.rs.network.io.packet.player;

import java.util.Arrays;

import org.maxgamer.rs.core.Core;
import org.maxgamer.rs.events.mob.persona.PersonaInteractMobEvent;
import org.maxgamer.rs.model.action.NPCAction;
import org.maxgamer.rs.model.entity.mob.npc.NPC;
import org.maxgamer.rs.model.entity.mob.npc.NPCDefinition;
import org.maxgamer.rs.model.entity.mob.persona.player.Player;
import org.maxgamer.rs.network.io.packet.PacketProcessor;
import org.maxgamer.rs.network.io.packet.RSIncomingPacket;

/**
 * @author netherfoam
 */
public class NPCOptionsHandler implements PacketProcessor<Player> {
	//Not sure what order these should be in.
	public static final int FIRST_OPTION = 28;
	public static final int SECOND_OPTION = 18;
	public static final int THIRD_OPTION = 33;
	public static final int FOURTH_OPTION = 31;
	public static final int FIFTH_OPTION = 72;
	public static final int EXAMINE = 5;
	
	@Override
	public void process(Player player, RSIncomingPacket packet) throws Exception {
		NPC target = null;
		int option = -1;
		int index;
		
		@SuppressWarnings("unused")
		boolean run = false; //If the user held CTRL while clicking, this will be true.
		switch (packet.getOpcode()) {
			case FIRST_OPTION:
				//Usually attack
				run = packet.readByteS() == 1;
				index = packet.readLEShort() - 1;
				
				option = 1;
				break;
			case SECOND_OPTION:
				index = packet.readShort() - 1;
				run = packet.readByte() == 1;
				option = 2;
				break;
			case THIRD_OPTION:
				index = packet.readShort() - 1;
				run = packet.readByteA() == 1;
				option = 3;
				break;
			case FOURTH_OPTION:
				index = packet.readShort() - 1;
				run = packet.readByteS() == 1;
				option = 4;
				break;
			case FIFTH_OPTION:
				index = packet.readShort() - 1;
				run = packet.readByte() == 1;
				option = 5;
				break;
			case EXAMINE:
				int npcId = packet.readShort();
				try {
					NPCDefinition d = NPC.getDefinition(npcId);
					player.sendMessage(d.getExamine());
					
					//TODO DEBUG: Send the player the options they can click on the NPC
					String[] options = new String[] { d.getInteraction(0), d.getInteraction(1), d.getInteraction(2), d.getInteraction(3), d.getInteraction(4) };
					player.sendMessage("NPCID " + npcId + " Options: " + Arrays.toString(options) + ", Clicked " + option);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				
				return; //Return, not break
			default:
				return;
		}
		target = Core.getServer().getNPCs().get(index);
		if (target == null) {
			player.getCheats().log(10, "Player attempted to interact with a NULL NPC");
			return;
		}
		
		if (player.getProtocol().isVisible(target) == false) {
			player.getCheats().log(30, "Player attempted to interact with an NPC that wasn't on screen. Target: " + target + ", given index: " + index + " + 1");
			for (NPC n : player.getLocation().getNearby(NPC.class, 4)) {
				player.sendMessage(n.toString());
			}
			return;
		}
		
		PersonaInteractMobEvent e = new PersonaInteractMobEvent(player, target, option);
		e.call();
		if (e.isCancelled()) return;
		
		//Get the NPC option they clicked
		String type = target.getDefinition().getInteraction(option - 1);
		
		//Core.getServer().getScriptEngine().run("npc_option/" + type, "run", player, target, option);
		NPCAction script = new NPCAction(player, type, target);
		player.getActions().clear();
		player.getActions().queue(script);
	}
}