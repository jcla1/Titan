package org.maxgamer.rs.logonv4;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import org.maxgamer.io.ByteReader;
import org.maxgamer.io.CircularBuffer;
import org.maxgamer.rs.cache.RSInputStream;
import org.maxgamer.rs.network.io.stream.RSInputBuffer;

/**
 * Represents a packet received from the client
 * @author netherfoam
 */
public class LSIncomingPacket extends RSInputBuffer implements ByteReader {
	
	public static LSIncomingPacket parse(RSInputBuffer b) throws BufferUnderflowException, IndexOutOfBoundsException {
		int opcode = b.readByte() & 0xFF;
		int length = b.readShort();
		
		LSIncomingPacket p = new LSIncomingPacket(opcode, b, length);
		return p;
	}
	
	public static LSIncomingPacket parse(RSInputStream in) throws IOException, BufferUnderflowException {
		int opcode = in.read();
		if (opcode == -1) throw new BufferUnderflowException();
		
		int l1 = in.read();
		if (l1 == -1) throw new BufferUnderflowException();
		int l2 = in.read();
		if (l2 == -1) throw new BufferUnderflowException();
		
		int length = (l1 << 8) | l2;
		
		ByteBuffer bb = ByteBuffer.allocate(length);
		while (bb.hasRemaining()) {
			int v = in.read();
			if (v == -1) throw new BufferUnderflowException();
			bb.put((byte) v);
		}
		bb.flip();
		return new LSIncomingPacket(opcode, bb, length);
	}
	
	//Value 0-255
	private int opcode;
	
	private LSIncomingPacket(int opcode, RSInputBuffer b, int length) throws BufferUnderflowException, IndexOutOfBoundsException {
		super(b, length);
		this.opcode = opcode;
	}
	
	private LSIncomingPacket(int opcode, CircularBuffer b, int length) throws IOException {
		super(b, length);
		this.opcode = opcode;
	}
	
	public LSIncomingPacket(int opcode, ByteBuffer bb, int length) {
		super(bb, length);
		this.opcode = opcode;
	}
	
	public int getOpcode() {
		return opcode;
	}
	
	public static LSIncomingPacket parse(CircularBuffer b) throws IOException {
		int opcode = b.readByte() & 0xFF;
		int length = b.readShort();
		
		LSIncomingPacket p = new LSIncomingPacket(opcode, b, length);
		return p;
	}
}