package com.appspot.reversiplay.game;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ReversiGame {

    private final int boardSize;

    private Piece[][] board;
    private Piece currentPiece;

    private List<Integer> validMoves;
    private int blackStones;
    private int whiteStones;

    private boolean isGameOver;

    public ReversiGame(int boardSize) {
        super();
        this.boardSize = boardSize;
        this.board = new Piece[boardSize][boardSize];
        this.validMoves = new ArrayList<>();
        this.newGame(Piece.BLACK);
    }

    private void newGame(Piece startPiece) {
        this.initializeBoard();
        this.currentPiece = startPiece;
        this.calculateAndStoreValidMoves();
        this.isGameOver = false;
    }

    /**
     * 보드의 정 중앙에 흑과 백이 크로스로 배치되도록 보드를 초기화 한다.
     */
    private void initializeBoard() {
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
                this.board[i][j] = Piece.NONE;
                if ((this.boardSize / 2) - 2 < i && i < (this.boardSize / 2) + 1 && (this.boardSize / 2) - 2 < j && j < (this.boardSize / 2) + 1) {
                    if (i == j) {
                        this.board[i][j] = Piece.WHITE;
                    } else {
                        this.board[i][j] = Piece.BLACK;
                    }
                }
            }
        }
    }

    /**
     * 현재 판 정보를 기반으로 다음 턴에 움직일 수 있는 유효한 움직임에 대해 계산하고 List에 상태를 저장 해 둔다.
     */
    private void calculateAndStoreValidMoves() {
        validMoves.clear();
        boolean nextMove;
        blackStones = 0;
        whiteStones = 0;

        for (int x = 0; x < this.boardSize; ++x) {
            for (int y = 0; y < this.boardSize; ++y) {
                if (this.board[x][y] == Piece.BLACK) {
                    blackStones++;
                } else if (this.board[x][y] == Piece.WHITE) {
                    whiteStones++;
                }

                nextMove = (this.board[x][y] != Piece.NONE);
                for (int width = -1; !nextMove && width < 2; ++width) {
                    for (int height = -1; !nextMove && height < 2; ++height) {
                        if (!checkZeroDirection(width, height) && checkDirection(x, y, width, height)) {
                            validMoves.add(x * boardSize + y);
                            nextMove = true;
                        }
                    }
                }
            }
        }
    }

    private boolean checkDirection(int x, int y, int width, int height) {
        boolean foundOpponent = false;
        int positionX = x + width;
        int positionY = y + height;

        // 상하좌우로 이동한 내 위치(start)가 보드 사이즈 보다 크지 않은지 체크하고, 그 위에 상대방 플레이어가 있다면
        while (checkBounds(positionX, positionY) && this.board[positionX][positionY] == this.getCurrentOpponentPiece()) {
            positionX = positionX + width;
            positionY = positionY + height;
            foundOpponent = true;
        }

        return (foundOpponent && checkBounds(positionX, positionY) && this.board[positionX][positionY] == this.currentPiece);
    }

    /**
     * Increase 된 위치가 BOARD_SIZE를 초과하지 않는지 경계 체크
     * 
     * @param x
     * @param y
     * @return
     */
    private boolean checkBounds(int x, int y) {
        return (-1 < x && x < this.boardSize && -1 < y && y < this.boardSize);
    }

    /**
     * 현재 피스의 반대 피스를 반환한다.
     * 
     * @return
     */
    private Piece getCurrentOpponentPiece() {
        if (this.currentPiece == Piece.BLACK)
            return Piece.WHITE;
        if (this.currentPiece == Piece.WHITE)
            return Piece.BLACK;
        return Piece.NONE;
    }

    /**
     * 현재 루프 내 위치가 자기 자신인지 여부 체크, 자기 자신이면 skip을 하기 위해 씀
     * 
     * @param width
     * @param height
     * @return
     */
    private boolean checkZeroDirection(int width, int height) {
        if (width == 0 && height == 0) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 해당 좌표로 조각을 옮길 수 있는지 확인한다.
     * 
     * @param x
     * @param y
     * @return
     */
    public boolean playPiece(int x, int y) {
        if (this.currentPiece == Piece.WHITE || this.currentPiece == Piece.BLACK) {
            // TODO contains 확인..
            if (validMoves.contains(x * boardSize + y)) {
                this.board[x][y] = this.currentPiece;
                for (int width = -1; width < 2; ++width) {
                    for (int height = -1; height < 2; ++height) {
                        if (!checkZeroDirection(width, height) && checkDirection(x, y, width, height)) {
                            playDirection(x, y, width, height);
                        }
                    }
                }
                return swapTurn();
            }
        }

        throw new RuntimeException("Invalid move!");
    }

    private void playDirection(int x, int y, int width, int height) {
        int startX = x + width;
        int startY = y + height;

        while (this.board[startX][startY] == getCurrentOpponentPiece()) {
            this.board[startX][startY] = this.currentPiece;
            startX = startX + width;
            startY = startY + height;
        }
    }

    /**
     * 턴을 스왑하는데 실제로 스왑됐는지 여부를 반환값으로 돌려준다. 우선 턴을 스왑하고 다음 ValidMove를 계산한 다음에
     * ValidMove가 없는 경우에는 다시 턴을 스왑하고 ValidMove가 있는지 계산한다. 만약 Valid Move가 없는 경우에는
     * 게임이 끝난 것으로 간주한다.
     * 
     * @return
     */
    private boolean swapTurn() {
        boolean hasChanged = false;

        this.currentPiece = this.getCurrentOpponentPiece();
        this.validMoves.clear();
        calculateAndStoreValidMoves();
        if (this.validMoves.isEmpty()) {
            this.currentPiece = this.getCurrentOpponentPiece();
            this.validMoves.clear();
            calculateAndStoreValidMoves();

            if (this.validMoves.isEmpty()) {
                isGameOver = true;
            } else {
                // markValidMove();
            }
        } else {
            // markValidMove();
            hasChanged = true;
        }

        return hasChanged;
    }

    public boolean isGameOver() {
        return this.isGameOver;
    }

    public List<String> getBoard() {
        List<String> pieces = new LinkedList<>();
        for (int i = 0; i < boardSize; ++i) {
            for (int j = 0; j < boardSize; ++j) {
            	if(this.validMoves.contains(i*boardSize + j)) {
            		pieces.add("V");
            	} else{
            		pieces.add(this.board[i][j].toString().substring(0, 1));
            	}
            }
        }
        return pieces;
    }

    public List<Integer> getValidMoves() {
        return this.validMoves;
    }

    public int getBlackStones() {
        return blackStones;
    }

    public int getWhiteStones() {
        return whiteStones;
    }

    public Piece getCurrentPiece() {
        return currentPiece;
    }

    public void printBoard() {
        System.out.print("  ");
        for (int i = 1; i <= boardSize; ++i) {
            System.out.print(i + " ");
        }
        System.out.println("");

        int c = 1;
        for (Piece[] ps : board) {
            System.out.print(c++ + " ");
            for (Piece p : ps) {
                System.out.print(p.toString().substring(0, 1) + " ");
            }
            System.out.println("");
        }
        System.out.println("");
    }

    public void printValidMoves() {
        System.out.print("Valid moves:");
        for (Integer p : validMoves) {
            int x = p / boardSize;
            int y = p - (x * boardSize);
            System.out.print("[" + (x + 1) + "," + (y + 1) + "]");
        }
        System.out.println("");
    }

    public void printScore() {
        System.out.println(String.format("Black:{%d} White:{%d}", this.blackStones, this.whiteStones));
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\nboardSize=");
        builder.append(boardSize);
        builder.append("\nboard");
        builder.append("\n  ");
        for (int i = 1; i <= boardSize; ++i) {
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
        builder.append("currentPiece=");
        builder.append(currentPiece);
        builder.append("\nvalidMoves=");
        builder.append(validMoves);
        builder.append("\nblackStones=");
        builder.append(blackStones);
        builder.append("\nwhiteStones=");
        builder.append(whiteStones);
        builder.append("\nisGameOver=");
        builder.append(isGameOver);
        return builder.toString();
    }
}
