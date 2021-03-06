package org.maxgamer.rs.command.commands;

import org.maxgamer.rs.command.PlayerCommand;
import org.maxgamer.rs.core.Core;
import org.maxgamer.rs.model.entity.mob.persona.Persona;
import org.maxgamer.rs.model.entity.mob.persona.player.Player;
import org.maxgamer.rs.model.entity.mob.persona.player.Rights;
import org.maxgamer.rs.model.map.Location;

/**
 * @author netherfoam
 */
public class Tphere implements PlayerCommand {
	
	@Override
	public void execute(Player p, String[] args) {
		if (args.length <= 0) {
			p.sendMessage("Arg0 must be the user to teleport.");
			return;
		}
		
		Location dest = p.getLocation();
		Persona victim = Core.getServer().getPersonas().getPersona(args[0], true);
		if (victim == null) {
			p.sendMessage("Target " + args[0] + " not online.");
			return;
		}
		
		victim.teleport(dest);
		p.sendMessage("Teleported " + victim.getName() + " to you.");
	}
	
	@Override
	public int getRankRequired() {
		return Rights.MOD;
	}
}