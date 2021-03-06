package org.maxgamer.rs.model.action;

import org.maxgamer.rs.lib.Erratic;
import org.maxgamer.rs.model.entity.mob.Mob;
import org.maxgamer.rs.model.entity.mob.facing.Facing;
import org.maxgamer.rs.model.map.Location;
import org.maxgamer.rs.model.map.path.Direction;
import org.maxgamer.rs.model.map.path.Directions;
import org.maxgamer.rs.model.map.path.Path;
import org.maxgamer.rs.model.map.path.PathFinder;
import org.maxgamer.rs.model.map.path.ProjectilePathFinder;

/**
 * A class to handle one mob following another.
 * @author netherfoam
 */
public class CombatFollow extends Follow {
	private Location lastPos;
	private Location nextPos;
	
	/**
	 * The preferred follow distance, 1 to max.
	 */
	private int prefDistance = 1;
	private PathFinder pathFinder;
	private PathFinder rangeFinder = new ProjectilePathFinder();
	private WalkAction walk;
	private Path path;
	private Path rangePath;
	
	/**
	 * Constructs a new Follow object.
	 * @param owner the mob who is following another
	 * @throws NullPointerException if the owner is null
	 */
	public CombatFollow(Mob owner, Mob target, int prefDistance, int breakDistance, PathFinder pather) {
		super(owner, target, breakDistance);
		if (prefDistance > breakDistance) throw new IllegalArgumentException("Preferred distance must be <= breakDistance");
		if (prefDistance <= 0 || breakDistance <= 0) throw new IllegalArgumentException("PrefDistance and BreakDistance must be > 0");
		if (pather == null) throw new NullPointerException("Pather may not be null");
		
		this.prefDistance = prefDistance;
		this.pathFinder = pather;
	}
	
	public boolean isReachable() {
		if (path != null && path.hasFailed()) { //Maybe this.
			//if(path == null || path.hasFailed()){
			//No walking path
			if (rangePath == null || rangePath.hasFailed()) {
				//We cannot range them
				return false;
			}
		}
		return true;
	}
	
	/**
	 * The preferred number of tiles between this mob and the target. Once this
	 * follow reaches this many times remaining, it will begin yielding to other
	 * Actions, though it will not stop. If the target moves out of this
	 * distance, they will be followed again until this distance is acceptable.
	 * @return the preferred distance between us and the target.
	 */
	public int getPreferredDistance() {
		return prefDistance;
	}
	
	@Override
	protected boolean run() {
		if (isFollowing() == false) {
			getOwner().getCombat().setTarget(null);
			return true; //We're done following and combatting
		}
		
		if (nextPos == null) {
			//We haven't started this following yet
			getOwner().setFacing(Facing.face(getTarget()));
		}
		
		if (getOwner().getLocation().equals(getTarget().getLocation())) {
			//Whoops, we're ontop of our target!
			Direction[] dirs = Directions.ALL;
			
			int r = Erratic.nextInt(0, dirs.length - 1);
			
			//Finds a random walk direction to move in.
			for (int i = r; i < dirs.length; i++) {
				Direction d = dirs[i];
				if (d.conflict(getOwner().getLocation()) == 0) {
					path = new Path();
					path.addFirst(d);
					walk = new WalkAction(mob, path);
					walk.run();
					return false; //Not done yet.
				}
			}
			//Finds a random walk direction to run in.
			for (int i = 0; i < r; i++) {
				Direction d = dirs[i];
				if (d.conflict(getOwner().getLocation()) == 0) {
					path = new Path();
					path.addFirst(d);
					walk = new WalkAction(mob, path);
					walk.run();
					return false; //Not done yet.
				}
			}
			return false; //We could not move off of their square.
		}
		
		rangePath = null;
		if (prefDistance * prefDistance < getOwner().getLocation().distanceSq(getTarget().getLocation()) || (prefDistance > 1 && (rangePath = rangeFinder.findPath(getOwner().getCenter(), getTarget().getCenter(), getTarget().getCenter(), 1, 1)).hasFailed())) {
			//We are further than the desired distance from the target! We should attempt to move closer.
			if (lastPos == null || lastPos.equals(getTarget().getLocation()) == false) {
				//Our previous destination is now invalid.
				lastPos = nextPos;
				
				if (lastPos == null) { //So nextPos is null, this is our first move.
					lastPos = getTarget().getLocation();
				}
				
				path = pathFinder.findPath(getOwner().getLocation(), lastPos, lastPos, getOwner().getSizeX(), getOwner().getSizeY());
				if (path.hasFailed() == false && path.isEmpty() == false) {
					path.removeLast();
				}
				walk = new WalkAction(mob, path);
			}
			
			//Call returns true if we've reached the destination.
			if (walk.run() && path.hasFailed() == false && ((rangePath != null && rangePath.hasFailed() == false) || rangePath == null)) {
				yield();
			}
			
			nextPos = getTarget().getLocation();
			return false;
		}
		else {
			//TODO: This is a bit peculiar. We really need to check that we can still reach the
			//target, and for that, the path can't be null.
			if (path == null || path.hasFailed() == false) {
				yield();
			}
		}
		
		return false;
	}
}