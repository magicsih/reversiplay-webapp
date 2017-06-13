package com.appspot.reversiplay.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ReversiGameComponent {

	private static final Logger LOG = Logger.getLogger(ReversiGameComponent.class.getSimpleName());

	private static final int BOARD_SIZE = 8;
	private static final int BITS_PER_BYTE = 8;
	private static final int BIN_SIZE_OF_BOARD = BOARD_SIZE * BOARD_SIZE * 2;
	private static final int BYTE_SIZE_OF_BOARD = BIN_SIZE_OF_BOARD / BITS_PER_BYTE;

	private static final byte[] NEW_BOARD;
	static {
		ByteBuffer buffer = ByteBuffer.allocate(BYTE_SIZE_OF_BOARD + 2);
		buffer.order(ByteOrder.LITTLE_ENDIAN);
		buffer
		.putShort(Short.parseShort("0000000000000000",2))
		.putShort(Short.parseShort("0000000000000000",2))
		.putShort(Short.parseShort("0000001100000000",2))
		.putShort(Short.parseShort("0000111001000000",2))
		.putShort(Short.parseShort("0000000110110000",2))
		.putShort(Short.parseShort("0000000011000000",2))
		.putShort(Short.parseShort("0000000000000000",2))
		.putShort(Short.parseShort("0000000000000000",2))
		.putShort((short)Piece.BLACK.ordinal());
		
		NEW_BOARD = buffer.array();
	}
	public static byte[] newGame() {
		return NEW_BOARD;
	}

	public static byte[] put(Piece putter, List<String> binaryBoardState, int position) {
		
		LOG.info("Piece:" + putter + " position:" + position);
		Piece[][] board = new Piece[BOARD_SIZE][BOARD_SIZE];

		for(int i=0; i < BOARD_SIZE; ++i) {
			String row = binaryBoardState.get(i);
			for(int j=0; j < row.length(); j=j+2) {
				String twobit = row.substring(j, j+2);
				if("01".equals(twobit)) {
					board[i][j/2] = Piece.BLACK;
				} else if("10".equals(twobit)) {
					board[i][j/2] = Piece.WHITE;
				} else{
					board[i][j/2] = Piece.NONE;
				}
			}
		}

		int row = position / BOARD_SIZE;
		int col = position % BOARD_SIZE;

		board[row][col] = putter;
		for (int width = -1; width < 2; ++width) {
			for (int height = -1; height < 2; ++height) {
				if (!checkZeroDirection(width, height) && checkDirection(board, putter, row, col, width, height)) {
					playDirection(board, putter, row, col, width, height);
				}
			}
		}

		Piece nextTurn = getOpponentPiece(putter);
		
		List<Integer> validMoves = getValidMoves(board, nextTurn);
		
		if(validMoves.isEmpty()) {
			// See if next next turn (means me) also doesn't have valid moves.
			List<Integer> nextValidMoves = getValidMoves(board, putter);
			if(nextValidMoves.isEmpty()) {
				nextTurn = Piece.NONE;
			} else{
				nextTurn = putter;
			}
		} 

		return makeByteArrayFromBoardState(nextTurn, board, validMoves);
	}

	private static List<Integer> getValidMoves(Piece[][] board, Piece putter) {
		List<Integer> validMoves = new ArrayList<>();

		boolean nextMove;

		for (int x = 0; x < BOARD_SIZE; ++x) {
			for (int y = 0; y < BOARD_SIZE; ++y) {

				nextMove = (board[x][y] != Piece.NONE);
				for (int width = -1; !nextMove && width < 2; ++width) {
					for (int height = -1; !nextMove && height < 2; ++height) {
						if (!checkZeroDirection(width, height) && checkDirection(board, putter, x, y, width, height)) {
							validMoves.add(x * BOARD_SIZE + y);
							nextMove = true;
						}
					}
				}
			}
		}
		return validMoves;
	}

	private static void playDirection(Piece[][] board, Piece putter, int row, int col, int width, int height) {
		int startX = row + width;
		int startY = col + height;

		while (board[startX][startY] == getOpponentPiece(putter)) {
			board[startX][startY] = putter;
			startX = startX + width;
			startY = startY + height;
		}
	}

	private static Piece getOpponentPiece(Piece putter) {
		if (putter == Piece.BLACK)
			return Piece.WHITE;
		if (putter == Piece.WHITE)
			return Piece.BLACK;
		return Piece.NONE;
	}

	private static boolean checkDirection(Piece[][] board, Piece putter, int row, int col, int width, int height) {
		boolean foundOpponent = false;
		int positionX = row + width;
		int positionY = col + height;

		// 상하좌우로 이동한 내 위치(start)가 보드 사이즈 보다 크지 않은지 체크하고, 그 위에 상대방 플레이어가 있다면
		while (checkBounds(positionX, positionY) && board[positionX][positionY] == getOpponentPiece(putter)) {
			positionX = positionX + width;
			positionY = positionY + height;
			foundOpponent = true;
		}

		return (foundOpponent && checkBounds(positionX, positionY) && board[positionX][positionY] == putter);
	}

	/**
	 * Increase 된 위치가 BOARD_SIZE를 초과하지 않는지 경계 체크
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private static boolean checkBounds(int x, int y) {
		return (-1 < x && x < BOARD_SIZE && -1 < y && y < BOARD_SIZE);
	}

	/**
	 * 현재 루프 내 위치가 자기 자신인지 여부 체크, 자기 자신이면 skip을 하기 위해 씀
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	private static boolean checkZeroDirection(int width, int height) {
		if (width == 0 && height == 0) {
			return true;
		} else {
			return false;
		}
	}

	@SuppressWarnings("unused")
	private static Piece[][] initializeBoard() {
		Piece[][] board = new Piece[BOARD_SIZE][BOARD_SIZE];
		for (int i = 0; i < BOARD_SIZE; ++i) {
			for (int j = 0; j < BOARD_SIZE; ++j) {
				board[i][j] = Piece.NONE;
				if ((BOARD_SIZE / 2) - 2 < i && i < (BOARD_SIZE / 2) + 1 && (BOARD_SIZE / 2) - 2 < j && j < (BOARD_SIZE / 2) + 1) {
					if (i == j) {
						board[i][j] = Piece.WHITE;
					} else {
						board[i][j] = Piece.BLACK;
					}
				}
			}
		}
		return board;
	}

	private static byte[] makeByteArrayFromBoardState(Piece turn ,Piece[][] boardState, List<Integer> validMoves){
		ByteBuffer boardStateBuffer = ByteBuffer.allocate(BYTE_SIZE_OF_BOARD + 2);
		boardStateBuffer.order(ByteOrder.LITTLE_ENDIAN);

		for(int r=0;r < BOARD_SIZE;++r){
			StringBuilder sb = new StringBuilder();
			for(int c=0; c<BOARD_SIZE;++c) {
				if(Piece.BLACK.equals(boardState[r][c])){
					sb.append("01");
				} else if(Piece.WHITE.equals(boardState[r][c])){
					sb.append("10");
				} else {
					int position = r*BOARD_SIZE + c;
					if(validMoves.contains(position)) {
						sb.append("11");
					} else{
						sb.append("00");
					}
				}
			}
			String string = sb.toString();

			short v = Short.parseShort(string,2);
			System.out.println(string + "-" + v);
			
			boardStateBuffer.putShort(v);
		}
		
		boardStateBuffer.putShort((short)turn.ordinal());
		
		return boardStateBuffer.array();
	}

	private static void debugBoard(Piece[][] board) {
		StringBuilder builder = new StringBuilder();
		builder.append("\nboard");
		builder.append("\n  ");
		for (int i = 1; i <= BOARD_SIZE; ++i) {
			builder.append(i + " ");
		}
		builder.append("\n");
		int c = 1;
		for (Piece[] ps : board) {
			builder.append(c++ + " ");
			for (Piece p : ps) {
				builder.append(p.toString().substring(0, 1) + " ");
			}
			builder.append("\n");
		}
		System.out.println(builder.toString());
	}
}
