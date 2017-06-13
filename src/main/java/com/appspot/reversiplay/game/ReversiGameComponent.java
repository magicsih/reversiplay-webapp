package com.appspot.reversiplay.game;

import java.nio.ByteBuffer;
import java.util.List;

public class ReversiGameComponent {

	
	private static final int BOARD_SIZE = 8;
	private static final int BITS_PER_BYTE = 8;
	private static final int BIN_SIZE_OF_BOARD = BOARD_SIZE * BOARD_SIZE * 2;
	private static final int BYTE_SIZE_OF_BOARD = BIN_SIZE_OF_BOARD / BITS_PER_BYTE;
	
	private static final byte[] NEW_BOARD;
	static {
		ByteBuffer buffer = ByteBuffer.allocate(BYTE_SIZE_OF_BOARD);
		
		buffer
			.putShort(Short.parseShort("0000000000000000",2))
			.putShort(Short.parseShort("0000000000000000",2))
			.putShort(Short.parseShort("0000001100000000",2))
			.putShort(Short.parseShort("0000111001000000",2))
			.putShort(Short.parseShort("0000000110110000",2))
			.putShort(Short.parseShort("0000000011000000",2))
			.putShort(Short.parseShort("0000000000000000",2))
			.putShort(Short.parseShort("0000000000000000",2));
		
		NEW_BOARD = buffer.array();
	}
	public static byte[] newGame() {
		return NEW_BOARD;
	}
	
	public static byte[] put(List<String> boardState, short position) {
		return null;
	}
	
	
}
