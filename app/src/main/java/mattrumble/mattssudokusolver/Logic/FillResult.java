package mattrumble.mattssudokusolver.Logic;

public class FillResult {

    private Board board;
    private Boolean isValidBoard;

    public FillResult(Board board, Boolean isValidBoard) {
        this.board = board;
        this.isValidBoard = isValidBoard;
    }

    public Board getBoard() {
        return board;
    }

    public Boolean isValidBoard() {
        return isValidBoard;
    }
}