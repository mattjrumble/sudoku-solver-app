package mattrumble.mattssudokusolver.Logic;

import java.util.ArrayList;
import java.util.List;

public class Cell implements Comparable<Cell> {

    private int x;
    private int y;
    private List<Integer> possibleValues;
    private int filledValue;

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
        this.filledValue = 0;
        this.possibleValues = new ArrayList<>();
        for (int i = 1; i <= 9; i++) {
            possibleValues.add(i);
        }
    }

    public void setFilledValue(int value) {
        this.filledValue = value;
        this.possibleValues.clear();
        this.possibleValues.add(value);
    }

    public void removePossibleValue(int possibleValue) {
        try {
            possibleValues.remove(Integer.valueOf(possibleValue));
        } catch (Exception e) {
            System.out.println("Error 1: Illegal possible value.");
        }
    }

    public List<Integer> getPossibleValues() {
        return possibleValues;
    }

    public int getFilledValue() {
        return filledValue;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Cell copy() {
        Cell copyCell = new Cell(x, y);
        copyCell.possibleValues.clear();
        for (int value : getPossibleValues()) {
            copyCell.possibleValues.add(value);
        }
        copyCell.filledValue = getFilledValue();
        return copyCell;
    }

    @Override
    public String toString() {
        return "(" + x + "," + y + "): + {" + filledValue + "}, (" + possibleValues.toString() + ")";
    }

    @Override
    public int compareTo(Cell otherCell) {
        if (otherCell == null) {
            return 10;
        }
        return (getPossibleValues().size() - otherCell.getPossibleValues().size());
    }

}
