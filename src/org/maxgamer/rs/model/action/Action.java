package org.maxgamer.rs.model.action;

import java.util.LinkedList;

import org.maxgamer.rs.model.entity.mob.Mob;

/**
 * An abstract class which represents an action that a player may perform, such
 * as combat or chopping an oak tree.
 * @author netherfoam
 */
public abstract class Action {
	/** The mob who is performing the action */
	protected final Mob mob;
	protected LinkedList<Action> paired = new LinkedList<Action>();
	
	/**
	 * Constructs a new Action, but does not apply it, for the given mob.
	 * @param mob the mob to construct the action for
	 * @throws NullPointerException if the given mob is null
	 */
	public Action(Mob mob) {
		if (mob == null) throw new NullPointerException("The owner of an Action may not be null");
		this.mob = mob;
	}
	
	/**
	 * Returns the owner of this action, not null
	 * @return the owner of this action.
	 */
	public Mob getOwner() {
		return mob;
	}
	
	/**
	 * Called when a tick passes and this action is the first action in the
	 * queue. If the action has finished, this method should return true. If the
	 * action is not fully complete, it should return false. If the action is
	 * cancellable, then despite returning false, it may not have its run()
	 * method invoked again. When an action is cancelled, whether it started or
	 * not, it's cancel() method will always be invoked.
	 * @return true if finished, false if continue to call run() every tick.
	 */
	protected abstract boolean run();
	
	/**
	 * Cancels this action. This is called when it is interrupted or cancelled
	 * before it could be started. If run() returns true, this method will not
	 * be called, otherwise it will be called. This allows cleanup.
	 */
	protected abstract void onCancel();
	
	/**
	 * Returns true if this action is cancellable (Eg, movement, following, and
	 * combat are cancellable. Eating and being stunned are not). If this method
	 * returns false, it can still be cancelled if the ActionQueue is requested
	 * to cancel it specifically. When the ActionQueue has the clear() method
	 * invoked, however, only cancellable Actions will be removed.
	 * @return true if this action can be cancelled, false if it should not.
	 */
	protected abstract boolean isCancellable();
	
	/**
	 * Yields this action's turn to the next action, thus invoking the run()
	 * method on the next action. The next action may yield and so on until one
	 * doesn't yield, or the end of the ActionQueue is reached. Any exceptions
	 * thrown are caught by this method. This is a shortcut to
	 * getOwner().getActions().yield(this). This is useful in situations such as
	 * combat, where a Follow is desired until the target is reached, in which
	 * case a Follow Action would call yield(), allowing a Combat action to be
	 * executed immediately after.
	 */
	public void yield() {
		getOwner().getActions().yield(this);
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
	
	/**
	 * Pairs this action with another. If one action is cancelled, the other
	 * action will also become cancelled. If one action completes however, the
	 * other action will not be cancelled.
	 * @param a the action to pair it with
	 */
	public void pair(Action a) {
		if (paired.contains(a)) {
			throw new IllegalArgumentException("That action is already paired with this one. Given: " + a);
		}
		
		paired.add(a);
		if (a.paired.contains(this) == false) a.paired.add(this);
	}
}