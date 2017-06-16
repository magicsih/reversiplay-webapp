package com.appspot.reversiplay.game;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.google.appengine.api.utils.SystemProperty;

public class ReversiGameComponent {

	private static final Logger LOG = Logger.getLogger(ReversiGameComponent.class.getSimpleName());

	private static final int ADDITIONAL_BOARD_STATE = 2; // current boardstate+turn(2byte)
	private static final int BOARD_SIZE = 8;
	private static final int BITS_PER_BYTE = 8;
	private static final int BIN_SIZE_OF_BOARD = BOARD_SIZE * BOARD_SIZE * 2;
	private static final int BYTE_SIZE_OF_BOARD = BIN_SIZE_OF_BOARD / BITS_PER_BYTE;

	private static final byte[] NEW_BOARD;
	static {
		ByteBuffer buffer = ByteBuffer.allocate(BYTE_SIZE_OF_BOARD + ADDITIONAL_BOARD_STATE);
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
		
		LOG.fine("Piece:" + putter + " position:" + position);
		
		Piece[][] board = makeBoardFromBinaryStrings(binaryBoardState);

		playMove(putter, position, board);

		Piece nextTurn = getOpponentPiece(putter);
		
		List<Integer> validMoves = getValidMoves(board, nextTurn);
		
		if(validMoves.isEmpty()) {
			// See if next next turn (means me) also doesn't have valid moves.
			validMoves = getValidMoves(board, putter);
			if(validMoves.isEmpty()) {
				nextTurn = Piece.NONE;
			} else{
				nextTurn = putter;
			}
		}
		
		return makeByteArrayFromBoardState(nextTurn, board, validMoves);
	}
	
	public static byte[] getBestMove(Piece turn, List<String> binaryBoardState, short depth) {
		Piece[][] board = makeBoardFromBinaryStrings(binaryBoardState);
		getBoardStateAsString(board);
		
		List<Integer> validMoves = getValidMoves(board, turn);
		
		
		int bestValidMove = getBestMove(board, validMoves, turn, turn, depth);
		
		if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Development) {
			LOG.info(getBoardStateAsString(board));
			LOG.info("Depth:" + depth);
			LOG.info("ValidMoves:" + validMoves);
			LOG.info("BestValidMove:" + bestValidMove);
		}
		
		return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort((short)bestValidMove).array();
	}

	private static void playMove(Piece putter, int position, Piece[][] board) {
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
	}

	private static Piece[][] makeBoardFromBinaryStrings(List<String> binaryBoardState) {
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
		return board;
	}

	class Evaluation {
		public int position;
		public int score;
	}
	
	/**
	 * Get the best move from valid moves.
	 * Using MiniMax Algorithm
	 * @param board
	 * @param validMoves
	 * @param putter
	 * @param depth
	 * @return
	 */
	public static int getBestMove(final Piece[][] board, List<Integer> validMoves, final Piece maximizer, final Piece putter, final int depth) {
		boolean isGameOver = false;
		List<Integer> copiedValidMoves = new ArrayList<>(validMoves);
		Piece[][] copiedBoard = new Piece[BOARD_SIZE][BOARD_SIZE];
		
		for(int i=0; i < BOARD_SIZE; ++i) {
			for(int j=0; j < BOARD_SIZE; ++j) {
				copiedBoard[i][j] = board[i][j];
			}
		}
		if(copiedValidMoves.isEmpty()) {
			// See if next next turn (means me) also doesn't have valid moves.
			copiedValidMoves = getValidMoves(board, putter);
			if(copiedValidMoves.isEmpty()) {
				isGameOver = true;
			}
		}
		
		final boolean isMaximizingPiece = maximizer.equals(putter);
		int currentScore = 0;
		int bestScore = (!isMaximizingPiece) ? Integer.MAX_VALUE : Integer.MIN_VALUE;
		
		int bestMove = -1;
		
		if (depth == 0 || isGameOver) {            
	        return evaluate(board, putter); 
	    } else {
	    	
	    	for(int m : copiedValidMoves) {
	    		playMove(putter, m, copiedBoard);
	    		
	    		Piece nextTurn = getOpponentPiece(putter);
	    		
	    		List<Integer> vm = getValidMoves(copiedBoard, nextTurn);
	    		
	    		if(vm.isEmpty()) {
	    			// See if next next turn (means me) also doesn't have valid moves.
	    			vm = getValidMoves(board, putter);
	    			if(!vm.isEmpty()) {
	    				nextTurn = putter;
	    			} 
	    		}
	    		currentScore = getBestMove(copiedBoard, vm, maximizer, nextTurn, depth - 1);
	    		
	    		if(isMaximizingPiece) {
	    			if (currentScore > bestScore) {
	                    bestScore = currentScore;
	                    bestMove = m;
	                 }
	    		} else {
	    			if (currentScore < bestScore) {
	                    bestScore = currentScore;
	                    bestMove = m;
	                 }
	    		}
	    	}
	    }
		return bestMove;
	}
	
	private static final int EVAL_CORNER =  BOARD_SIZE * 2 + BOARD_SIZE;
	private static final int EVAL_PRE_CORNER = -BOARD_SIZE * 2;
	private static final int EVAL_DISADVANTAGE = -BOARD_SIZE;
	private static final int EVAL_OUTER = BOARD_SIZE * 2;
	
	private static int evaluate(Piece[][] board, Piece putter) {
		int score = 0;
		//FILL EVALUATION LOGIC
		
		Piece[] corners = {board[0][0], board[0][7],board[7][0], board[7][7]};
		for(Piece p : corners) {
			if(p.equals(putter)) {
				score += EVAL_CORNER;
			} else if (p.equals(getOpponentPiece(putter))){ 
				score -= EVAL_CORNER;
			} else {
				// ignore
			}
		}
		
		Piece[] preCorners = {board[1][1], board[1][6],board[6][1], board[6][6]};
		for(Piece p : preCorners) {
			if(p.equals(putter)) {
				score += EVAL_PRE_CORNER;
			} else if (p.equals(getOpponentPiece(putter))){ 
				score -= EVAL_PRE_CORNER;
			} else {
				// ignore
			}
		}
		
		Piece[] cornerPath = { 
				board[1][2], board[1][3], board[1][4], board[1][5]
                ,board[2][1], board[3][1], board[4][1], board[5][1]
                ,board[6][2], board[6][3], board[6][4], board[6][5]
                ,board[2][6], board[3][6], board[4][6], board[5][6]};
		
		for(Piece p : cornerPath) {
			if(p.equals(putter)) {
				score += EVAL_DISADVANTAGE;
			} else if (p.equals(getOpponentPiece(putter))){ 
				score -= EVAL_DISADVANTAGE;
			} else {
				// ignore
			}
		}
		
		Piece[] outerStones = { 
                board[0][1], board[0][2], board[0][3], board[0][4], board[0][5], board[0][6]
              , board[1][0], board[2][0], board[3][0], board[4][0], board[5][0], board[6][0]
              , board[7][1], board[7][2], board[7][3], board[7][4], board[7][5], board[7][6]
              , board[1][7], board[2][7], board[3][7], board[4][7], board[5][7], board[6][7]
            };                                                            
		
		for(Piece p : outerStones) {
			if(p.equals(putter)) {
				score += EVAL_OUTER;
			} else if (p.equals(getOpponentPiece(putter))){ 
				score -= EVAL_OUTER;
			} else {
				// ignore
			}
		}
		
		return score;
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

	private static byte[] makeByteArrayFromBoardState(Piece turn ,Piece[][] boardState, List<Integer> validMoves){
		ByteBuffer boardStateBuffer = ByteBuffer.allocate(BYTE_SIZE_OF_BOARD + ADDITIONAL_BOARD_STATE);
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
			int value = Integer.parseInt(string,2);
//			System.out.println(string + "-" + value);

			ByteBuffer putInt = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putInt(value);
			byte[] array = putInt.array();
			boardStateBuffer.put(array[0]);
			boardStateBuffer.put(array[1]);
		}
		
//		boardStateBuffer.putShort((short)bestValidMove);
		//Append here if you append here ByteBuffer size also need to expand. here and newGame function
		boardStateBuffer.putShort((short)turn.ordinal()); // 누가 차례인지를 Last로 하자..
		
		return boardStateBuffer.array();
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

	private static String getBoardStateAsString(Piece[][] board) {
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
		return builder.toString();
	}

	
}
