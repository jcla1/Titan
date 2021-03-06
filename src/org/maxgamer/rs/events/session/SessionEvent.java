package org.maxgamer.rs.events.session;

import org.maxgamer.rs.events.RSEvent;
import org.maxgamer.rs.network.Session;

/**
 * @author netherfoam
 */
public class SessionEvent extends RSEvent {
	private Session s;
	
	public SessionEvent(Session s) {
		this.s = s;
	}
	
	public Session getSession() {
		return s;
	}
	
	@Override
	public boolean isAsync() {
		return true;
	}
}