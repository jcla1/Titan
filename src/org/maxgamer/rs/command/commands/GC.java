package org.maxgamer.rs.command.commands;

import org.maxgamer.rs.command.CmdName;
import org.maxgamer.rs.command.CommandSender;
import org.maxgamer.rs.command.GenericCommand;
import org.maxgamer.rs.core.Core;
import org.maxgamer.rs.model.entity.mob.persona.player.Rights;

/**
 * @author netherfoam
 */
@CmdName(names = { "GarbageCollection" })
public class GC implements GenericCommand {
	
	@Override
	public void execute(final CommandSender player, final String[] args) throws Exception {
		Core.getServer().getMap().trim();
		System.gc();
	}
	
	@Override
	public int getRankRequired() {
		return Rights.ADMIN;
	}
	
}