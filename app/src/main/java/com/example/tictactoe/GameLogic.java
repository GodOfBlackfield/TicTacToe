package com.example.tictactoe;

import android.util.Log;

public class GameLogic {
    private String[][] board;
    private String compPiece;

    public GameLogic() {
        board = new String[3][3];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = "";
            }
        }
    }

    public boolean checkXWin() {
        boolean a = (board[0][0] == "X" && board[0][1] == "X" && board[0][2] == "X");
        boolean b = (board[1][0] == "X" && board[1][1] == "X" && board[1][2] == "X");
        boolean c = (board[2][0] == "X" && board[2][1] == "X" && board[2][2] == "X");
        boolean d = (board[0][0] == "X" && board[1][0] == "X" && board[2][0] == "X");
        boolean e = (board[0][1] == "X" && board[1][1] == "X" && board[2][1] == "X");
        boolean f = (board[0][2] == "X" && board[1][2] == "X" && board[2][2] == "X");
        boolean g = (board[0][0] == "X" && board[1][1] == "X" && board[2][2] == "X");
        boolean h = (board[0][2] == "X" && board[1][1] == "X" && board[2][0] == "X");
        if (a || b || c || d || e || f || g || h) {
            return true;
        }
        return false;
    }

    public boolean checkOWin() {
        boolean a = (board[0][0] == "O" && board[0][1] == "O" && board[0][2] == "O");
        boolean b = (board[1][0] == "O" && board[1][1] == "O" && board[1][2] == "O");
        boolean c = (board[2][0] == "O" && board[2][1] == "O" && board[2][2] == "O");
        boolean d = (board[0][0] == "O" && board[1][0] == "O" && board[2][0] == "O");
        boolean e = (board[0][1] == "O" && board[1][1] == "O" && board[2][1] == "O");
        boolean f = (board[0][2] == "O" && board[1][2] == "O" && board[2][2] == "O");
        boolean g = (board[0][0] == "O" && board[1][1] == "O" && board[2][2] == "O");
        boolean h = (board[0][2] == "O" && board[1][1] == "O" && board[2][0] == "O");
        if (a || b || c || d || e || f || g || h) {
            return true;
        }
        return false;
    }

    private int evaluateBoard() {
        if ((compPiece == "X" && checkXWin()) || (compPiece == "O" && checkOWin())) {
            return 10;
        } else if ((compPiece == "O" && checkXWin()) || (compPiece == "X" && checkOWin())) {
            return -10;
        }
        return 0;
    }

    private boolean isMovesLeft() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == "") {
                    return true;
                }
            }
        }
        return false;
    }

    private int miniMax(int depth, boolean isComp) {
        int score = evaluateBoard();
        if (score == 10) {
            return score - depth;
        }
        if (score == -10) {
            return score + depth;
        }
        if (!isMovesLeft()) {
            return 0;
        }
        if (isComp) {
            int best = Integer.MIN_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == "") {
                        board[i][j] = compPiece;
                        best = Math.max(best, miniMax(depth + 1, false));
                        board[i][j] = "";
                    }
                }
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[i][j] == "") {
                        board[i][j] = compPiece == "X" ? "O" : "X";
                        best = Math.min(best, miniMax(depth + 1, true));
                        board[i][j] = "";
                    }
                }
            }
            return best;
        }
    }

    private int[] findBestMove() {
        int bestScore = Integer.MIN_VALUE;
        int[] bestMove = {-1, -1};
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == "") {
                    board[i][j] = compPiece;
                    int moveVal = miniMax(0, false);
                    board[i][j] = "";
                    if (moveVal > bestScore) {
                        bestScore = moveVal;
                        bestMove[0] = i;
                        bestMove[1] = j;
                    }
                }
            }
        }
        return bestMove;
    }

    public void setCompPiece(String s) {
        compPiece = s;
    }

    public void setBoard(int x,String piece) {
        switch(x) {
            case 0:
                board[0][0] = piece;
                break;
            case 1:
                board[0][1] = piece;
                break;
            case 2:
                board[0][2] = piece;
                break;
            case 3:
                board[1][0] = piece;
                break;
            case 4:
                board[1][1] = piece;
                break;
            case 5:
                board[1][2] = piece;
                break;
            case 6:
                board[2][0] = piece;
                break;
            case 7:
                board[2][1] = piece;
                break;
            case 8:
                board[2][2] = piece;
                break;
        }
    }

    public int playGame() {
        int[] bestMove = findBestMove();
        board[bestMove[0]][bestMove[1]] = compPiece;
        int res = -1;
        if (bestMove[0] == 0 && bestMove[1] == 0) {
            res = 0;
        } else if (bestMove[0] == 0 && bestMove[1] == 1) {
            res = 1;
        } else if (bestMove[0] == 0 && bestMove[1] == 2) {
            res = 2;
        } else if (bestMove[0] == 1 && bestMove[1] == 0) {
            res = 3;
        } else if (bestMove[0] == 1 && bestMove[1] == 1) {
            res = 4;
        } else if (bestMove[0] == 1 && bestMove[1] == 2) {
            res = 5;
        } else if (bestMove[0] == 2 && bestMove[1] == 0) {
            res = 6;
        } else if (bestMove[0] == 2 && bestMove[1] == 1) {
            res = 7;
        } else if (bestMove[0] == 2 && bestMove[1] == 2) {
            res = 8;
        }
        return res;
    }

    public String getBoardPiece(int pos) {
        String s = "";
        switch (pos) {
            case 0:
                s = board[0][0];
                break;
            case 1:
                s = board[0][1];
                break;
            case 2:
                s = board[0][2];
                break;
            case 3:
                s = board[1][0];
                break;
            case 4:
                s = board[1][1];
                break;
            case 5:
                s = board[1][2];
                break;
            case 6:
                s = board[2][0];
                break;
            case 7:
                s = board[2][1];
                break;
            case 8:
                s = board[2][2];
                break;
        }
        return s;
    }
}