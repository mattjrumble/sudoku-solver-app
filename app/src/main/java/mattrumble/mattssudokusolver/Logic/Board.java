package mattrumble.mattssudokusolver.Logic;

import java.util.List;

public class Board {

    private Cell[][] cells;

    public Board() {
        cells = new Cell[9][9];
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }
    }

    public void removePossibleValue(int x, int y, int possibleValue) {
        cells[x][y].removePossibleValue(possibleValue);
    }

    public void setFilledValue(int x, int y, int filledValue) {
        cells[x][y].setFilledValue(filledValue);
    }

    public int getFilledValue(int x, int y) {
        return cells[x][y].getFilledValue();
    }

    public List<Integer> getPossibleValues(int x, int y) {
        return cells[x][y].getPossibleValues();
    }

    public Cell getCell(int x, int y) {
        return cells[x][y];
    }

    public Cell[][] getCells() {
        return cells;
    }

    public boolean isComplete() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (cells[i][j].getFilledValue() == 0) {
                    return false;
                }
            }
        }
        return true;
    }

    public Board copy() {
        Board copyBoard = new Board();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                copyBoard.getCells()[i][j] = this.getCell(i, j).copy();
            }
        }
        return copyBoard;
    }

}
