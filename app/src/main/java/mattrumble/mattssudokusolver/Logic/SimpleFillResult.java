package mattrumble.mattssudokusolver.Logic;

import java.util.Set;

public class SimpleFillResult extends FillResult {

    private Set<Cell> newFilledCells;

    public SimpleFillResult(Board board, Boolean isValidBoard, Set<Cell> newFilledCells) {
        super(board, isValidBoard);
        this.newFilledCells = newFilledCells;
    }

    public Set<Cell> getNewFilledCells() {
        return newFilledCells;
    }

}
