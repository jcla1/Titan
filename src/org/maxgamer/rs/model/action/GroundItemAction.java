package org.maxgamer.rs.model.action;

import java.io.File;

import org.maxgamer.rs.core.Core;
import org.maxgamer.rs.lib.log.Log;
import org.maxgamer.rs.model.entity.mob.persona.Persona;
import org.maxgamer.rs.model.item.ground.GroundItemStack;
import org.maxgamer.rs.module.ScriptUtil;
import org.maxgamer.rs.network.Client;
import org.maxgamer.rs.structure.timings.StopWatch;

import bsh.EvalError;
import bsh.Interpreter;
import bsh.NameSpace;
import bsh.Primitive;

/**
 * @author netherfoam
 */
public class GroundItemAction extends Action {
	private GroundItemStack item;
	private Interpreter environment;
	private File file;
	
	public GroundItemAction(Persona mob, String option, GroundItemStack item) {
		super(mob);
		this.item = item;
		
		StopWatch w = Core.getTimings().start(getClass().getSimpleName());
		File[] files = new File[] { new File("scripts" + File.separator + "ground_action" + File.separator + item.getItem().getName(), option + ".java"), new File("scripts" + File.separator + "ground_action", option + ".java") };
		
		for (File f : files) {
			if (f.exists()) {
				this.environment = ScriptUtil.getScript(f);
				try {
					this.environment.set("self", this);
				}
				catch (EvalError e) {
					e.printStackTrace();
				}
				this.file = f;
				break;
			}
		}
		w.stop();
	}
	
	@Override
	public String toString() {
		return super.toString() + " item=[" + item.toString() + "]";
	}
	
	@Override
	public Persona getOwner() {
		return (Persona) super.getOwner();
	}
	
	@Override
	public boolean run() {
		if (environment == null) {
			if (getOwner() instanceof Client) {
				((Client) getOwner()).sendMessage("GroundItem option not implemented.");
			}
			return true;
		}
		NameSpace ns = environment.getNameSpace();
		Object o;
		try {
			o = ns.invokeMethod("run", new Object[] { getOwner(), item }, environment);
		}
		catch (EvalError e) {
			e.printStackTrace();
			return true; //Error, stop.
		}
		
		try {
			boolean b = (boolean) Primitive.unwrap(o);
			return b;
		}
		catch (RuntimeException e) {
			//ClassCastException or NullPointerException
			Log.warning("Method run(Persona, ItemStack) in ScriptAction in script " + file.getPath() + " should return true (done) or false (continue). Got " + o);
			return true;
		}
	}
	
	@Override
	protected void onCancel() {
		
	}
	
	@Override
	protected boolean isCancellable() {
		return true;
	}
}