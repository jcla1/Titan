package org.maxgamer.rs.logonv4.logon;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;

import org.maxgamer.rs.lib.log.Log;
import org.maxgamer.rs.logonv4.LSIncomingPacket;
import org.maxgamer.rs.logonv4.LSOutgoingPacket;
import org.maxgamer.rs.logonv4.Opcode;
import org.maxgamer.rs.logonv4.OpcodeDecoder;
import org.maxgamer.rs.logonv4.Profile;
import org.maxgamer.rs.network.AuthResult;
import org.maxgamer.rs.network.io.stream.RSInputBuffer;

/**
 * @author netherfoam
 */
public class GameDecoder extends OpcodeDecoder<LSIncomingPacket> implements Handler {
	private WorldHost host;
	
	public GameDecoder(WorldHost host) {
		this.host = host;
	}
	
	@Opcode(opcode = 1)
	public void decodeAuth(LSIncomingPacket in) {
		int sessionId = in.readInt();
		String name = in.readPJStr1();
		String pass = in.readPJStr1();
		String ip = in.readPJStr1();
		
		Log.debug("Auth request for " + name + "!");
		
		LSOutgoingPacket out = new LSOutgoingPacket(3);
		AuthResult result = AuthResult.SUCCESS;
		out.writeInt(sessionId);
		
		Profile profile = null;
		byte[] payload = null;
		do {
			//TODO: Check the player is currently offline
			if (LogonServer.getLogon().isOnline(name)) {
				result = AuthResult.ALREADY_ONLINE;
				break;
			}
			
			try {
				profile = LogonServer.getLogon().getProfiles().get(name);
				
				if (profile.isPass(pass) == false) {
					//Auth success
					result = AuthResult.INVALID_PASSWORD;
					break;
				}
			}
			catch (SQLException e) {
				result = AuthResult.SYSTEM_UNAVAILABLE;
				e.printStackTrace();
				break;
			}
			
			try {
				File file;
				FileInputStream fin;
				if ((file = new File("players", name.toLowerCase() + ".dat")).exists()) {
					fin = new FileInputStream(file);
				}
				else if ((file = new File("config", "default_player.yml")).exists()) {
					fin = new FileInputStream(file);
				}
				else {
					fin = null;
				}
				
				if (fin == null) {
					payload = new byte[0]; //Empty profile
				}
				else {
					payload = new byte[fin.available()];
					fin.read(payload);
					fin.close();
				}
			}
			catch (IOException e) {
				result = AuthResult.ERROR_LOADING_PROFILE;
				e.printStackTrace();
				break;
			}
		} while (false);
		
		out.writeByte(result.getCode());
		if (result != AuthResult.SUCCESS) {
			Log.debug("Auth declined: " + result);
			host.write(out);
			return;
		}
		
		out.writePJStr1(profile.getLastIP());
		out.writeLong(profile.getLastSeen());
		out.writeInt(payload.length);
		out.write(payload);
		
		profile.setLastIP(ip);
		profile.setLastSeen(System.currentTimeMillis());
		
		try {
			profile.update();
		}
		catch (SQLException e) {
			//Not a big issue, and probably won't occur.
			e.printStackTrace();
		}
		Log.debug("Auth Result: " + result);
		this.host.add(profile);
		
		host.write(out);
	}
	
	@Opcode(opcode = 2)
	public void decodeLeave(LSIncomingPacket in) {
		String name = in.readPJStr1();
		byte[] payload = new byte[in.readInt()];
		in.read(payload);
		Log.debug("leaving " + name);
		Profile profile = host.getPlayer(name);
		
		if (profile == null) {
			throw new RuntimeException("Player " + name + " leave requested, but player is not online");
		}
		
		File file = new File("players", name.toLowerCase() + ".bin");
		file.getParentFile().mkdirs();
		
		try {
			file.createNewFile();
			FileOutputStream out = new FileOutputStream(file);
			out.write(payload);
			out.close();
		}
		catch (IOException e) {
			Log.warning("Failed to write player profile for " + name);
			e.printStackTrace();
		}
		
		host.remove(profile);
	}
	
	@Opcode(opcode = 3)
	public void decodeSave(LSIncomingPacket in) {
		while (in.isEmpty() == false) {
			String name = in.readPJStr1();
			byte[] data = new byte[in.readInt()];
			in.read(data);
			
			try {
				FileOutputStream out = new FileOutputStream(new File("players", name.toLowerCase() + ".bin"));
				out.write(data);
				out.close();
			}
			catch (IOException e) {
				System.out.println("Failed to save profile for " + name);
				e.printStackTrace();
			}
		}
	}
	
	@Opcode(opcode = 4)
	public void decodePing(LSIncomingPacket in) {
		//nothing
	}
	
	@Override
	public void handle(RSInputBuffer in) {
		while (in.isEmpty() == false) {
			LSIncomingPacket packet = LSIncomingPacket.parse(in);
			this.decode(packet.getOpcode(), packet);
		}
	}
}