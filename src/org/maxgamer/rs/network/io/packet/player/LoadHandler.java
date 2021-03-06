package org.maxgamer.rs.network.io.packet.player;

import org.maxgamer.rs.model.entity.mob.persona.player.Player;
import org.maxgamer.rs.network.io.packet.RSIncomingPacket;
import org.maxgamer.rs.network.io.packet.PacketProcessor;

/**
 * Packet is sent whenever the player finishes loading a map that we sent to
 * them
 * @author netherfoam
 */
public class LoadHandler implements PacketProcessor<Player> {
	@Override
	public void process(Player c, RSIncomingPacket p) throws Exception {
		//Opcode 75, 0 bytes
		if (c.isLoaded() == false) {
			c.setLoaded(true);
		}
	}
}