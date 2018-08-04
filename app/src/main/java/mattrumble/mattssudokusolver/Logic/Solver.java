package mattrumble.mattssudokusolver.Logic;

import android.util.Log;

import java.util.ArrayList;
        import java.util.Iterator;
        import java.util.Collections;
        import java.util.List;
        import java.util.Set;
        import java.util.HashSet;

public class Solver {

    private Board startingBoard;

    public Solver(Board startingBoard) {
        this.startingBoard = startingBoard;
    }

    public FillResult solve() {
        Board copyBoard = startingBoard.copy();

        // Try SimpleFill until unable.
        SimpleFillResult simpleFillResult = repeatSimpleFillUntilUnable(copyBoard, null);
        copyBoard = simpleFillResult.getBoard();
        // If we have completed the board.
        if (copyBoard.isComplete()) {
            return new FillResult(copyBoard, simpleFillResult.isValidBoard());
        } else {
            // Try lookAheadOneFill until unable.
            LookAheadFillResult lookAheadOneFillResult = repeatLookAheadOneFillUntilUnable(copyBoard);
            copyBoard = lookAheadOneFillResult.getBoard();
            if (copyBoard.isComplete()) {
                return new FillResult(copyBoard, lookAheadOneFillResult.isValidBoard());
            } else {
                // Try lookAheadTwoFill until unable.
                LookAheadFillResult lookAheadTwoFillResult = repeatLookAheadTwoFillUntilUnable(copyBoard);
                copyBoard = lookAheadTwoFillResult.getBoard();
                // Whether it's completed or not, return the board.
                return new FillResult(copyBoard, lookAheadTwoFillResult.isValidBoard());
            }
        }
    }

    public SimpleFillResult simpleFill(Board board, Set<Cell> newFilledCells) {
        // Updates the possible values for all unfilled cells using the newFilledCells.
        // Fills in any cells with only one possible value.
        // Looks at all the unfilled cells in each row/column/box, and fills in any that have a unique possible value.
        // Does some clever stuff with looking at each box too.
        // Fills in any cells with only one possible value again.
        // Returns the newlyFilledCells.

        // If no newFilledCells are given, assume every filled cell is a newFilledCell.
        //if (newFilledCells == null) { (This bit causes an error if uncommented. I'm not sure why.)
            newFilledCells = new HashSet<>();
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    Cell cell = board.getCell(i, j);
                    if (cell.getFilledValue() != 0) {
                        newFilledCells.add(cell);
                    }
                }
            }
        //}
        // Update all the possible values on the board.
        updatePossibleValuesOnBoard(board, newFilledCells);

        Set<Cell> newlyFilledCells = new HashSet<>();
        fillInCellsWithOnlyOnePossibleValue(board, newlyFilledCells);

        // Looks at all the unfilled cells in each row/column/box, and fills in any that have a unique possible value.
        for (int i = 0; i < 9; i++) {

            Set<Cell> unfilledCellsInRow = unfilledCellsInRow(board, i);
            searchAndFillForUniquePossibleValues(board, newlyFilledCells, unfilledCellsInRow);

            Set<Cell> unfilledCellsInColumn = unfilledCellsInColumn(board, i);
            searchAndFillForUniquePossibleValues(board, newlyFilledCells, unfilledCellsInColumn);

            int boxX = i % 3;
            int boxY = i / 3;
            Set<Cell> unfilledCellsInBox = unfilledCellsInBox(board, boxX, boxY);
            searchAndFillForUniquePossibleValues(board, newlyFilledCells, unfilledCellsInBox);
        }

        // Look at each box. If, for example, all the box's 2's are in one column, then no other cells in that
        // column can have a 2.
        for (int i = 0;
             i < 9; i++) {
            int boxX = i % 3;
            int boxY = i / 3;
            Set<Cell> unfilledCellsInBox = unfilledCellsInBox(board, boxX, boxY);
            Set<Cell> allCellsInBox = cellsInBox(board, boxX, boxY);
            Set<Integer> valuesRemainingInBox = valuesRemaining(allCellsInBox);
            for (int value : valuesRemainingInBox) {
                Set<Cell> cellsContainingValue = new HashSet<>();
                for (Cell cell : unfilledCellsInBox) {
                    if (cell.getPossibleValues().contains(value)) {
                        cellsContainingValue.add(cell);
                    }
                }
                if (cellsAreInTheSameRow(cellsContainingValue)) {
                    for (Cell cell : unfilledCells(cellsInTheSameRow(board, cellsContainingValue))) {
                        cell.removePossibleValue(value);
                    }
                }
                if (cellsAreInTheSameColumn(cellsContainingValue)) {
                    // As above.
                    for (Cell cell : unfilledCells(cellsInTheSameColumn(board, cellsContainingValue))) {
                        cell.removePossibleValue(value);
                    }
                }
            }
        }

        fillInCellsWithOnlyOnePossibleValue(board, newlyFilledCells);

        // Look at each row. If, for example, all the row's 9's are in one box, then no other cells in that
        // box can have a 9.
        for (int rowNumber = 0;
             rowNumber < 9; rowNumber++) {
            Set<Cell> unfilledCellsInRow = unfilledCellsInRow(board, rowNumber);
            Set<Cell> allCellsInRow = cellsInRow(board, rowNumber);
            Set<Integer> valuesRemainingInRow = valuesRemaining(allCellsInRow);
            for (int value : valuesRemainingInRow) {
                Set<Cell> cellsContainingValue = new HashSet<>();
                for (Cell cell : unfilledCellsInRow) {
                    if (cell.getPossibleValues().contains(value)) {
                        cellsContainingValue.add(cell);
                    }
                }
                if (cellsAreInTheSameBox(cellsContainingValue)) {
                    for (Cell cell : unfilledCells(cellsInTheSameBox(board, cellsContainingValue))) {
                        cell.removePossibleValue(value);
                    }
                }
            }
        }

        fillInCellsWithOnlyOnePossibleValue(board, newlyFilledCells);

        // Do the same as above, but for columns.
        for (int columnNumber = 0;
             columnNumber < 9; columnNumber++) {
            Set<Cell> unfilledCellsInColumn = unfilledCellsInColumn(board, columnNumber);
            Set<Cell> allCellsInColumn = cellsInColumn(board, columnNumber);
            Set<Integer> valuesRemainingInColumn = valuesRemaining(allCellsInColumn);
            for (int value : valuesRemainingInColumn) {
                Set<Cell> cellsContainingValue = new HashSet<>();
                for (Cell cell : unfilledCellsInColumn) {
                    if (cell.getPossibleValues().contains(value)) {
                        cellsContainingValue.add(cell);
                    }
                }
                if (cellsAreInTheSameBox(cellsContainingValue)) {
                    for (Cell cell : unfilledCells(cellsInTheSameBox(board, cellsContainingValue))) {
                        cell.removePossibleValue(value);
                    }
                }
            }
        }

        fillInCellsWithOnlyOnePossibleValue(board, newlyFilledCells);

        // Check whether the board is valid (this is only required when SimpleFill is called from LookAheadOneFill),
        // or if an invalid Sudoku puzzle was inputted to begin with.
        // Checking validity means checking each unfilled cell to see if it has zero possible values.
        boolean isValidBoard = true;
        for (Cell cell
                : unfilledCellsInBoard(board)) {
            if (cell.getPossibleValues().isEmpty()) {
                isValidBoard = false;
                break;
            }
        }
        // TEST: Extend this to check every row, column and box for repeated numbers.
        for (int i = 0;
             i < 9; i++) {

            List<Integer> valuesList = new ArrayList<>();
            for (Cell cell : filledCellsInRow(board, i)) {
                valuesList.add(cell.getFilledValue());
            }
            Set<Integer> valuesSet = new HashSet<>(valuesList);
            if (valuesSet.size() < valuesList.size()) {
                isValidBoard = false;
                break;
            }

            valuesList = new ArrayList<>();
            for (Cell cell : filledCellsInColumn(board, i)) {
                valuesList.add(cell.getFilledValue());
            }
            valuesSet = new HashSet<>(valuesList);
            if (valuesSet.size() < valuesList.size()) {
                isValidBoard = false;
                break;
            }

            int boxX = i % 3;
            int boxY = i / 3;
            valuesList = new ArrayList<>();
            for (Cell cell : filledCellsInBox(board, boxX, boxY)) {
                valuesList.add(cell.getFilledValue());
            }
            valuesSet = new HashSet<>(valuesList);
            if (valuesSet.size() < valuesList.size()) {
                isValidBoard = false;
                break;
            }
        }
        if (!isValidBoard) {
            newlyFilledCells.clear();
        }

        return new SimpleFillResult(board, isValidBoard, newlyFilledCells);

    }

    public SimpleFillResult repeatSimpleFillUntilUnable(Board board, Set<Cell> newFilledCells) {
        SimpleFillResult simpleFillResult = simpleFill(board, newFilledCells);
        while (!simpleFillResult.getNewFilledCells().isEmpty()) {
            simpleFillResult = simpleFill(board, simpleFillResult.getNewFilledCells());
        }
        return simpleFillResult;
    }

    public LookAheadFillResult lookAheadOneFill(Board board) {
        // Goes through each unfilled cell, and tries each possible value.
        // Sees if this leads to either a completed board or an invalid board.
        // If it leads to a completed board, this is great, we've solved the Sudoku.
        // Otherwise, this removes possible values for unfilled cells.
        // Or, if out of all the valid trial boards, an unfilled cell has the same value, then that is its true value.
        // Hopefully, this reduces a cell to one possible value, which we then fill in.

        List<Cell> unfilledCellsInBoard = new ArrayList<Cell>(unfilledCellsInBoard(board));
        Collections.sort(unfilledCellsInBoard);
        for (Cell unfilledCell : unfilledCellsInBoard) {
            Iterator iterator = unfilledCell.getPossibleValues().iterator();
            List<Board> trialBoards = new ArrayList<>();
            while (iterator.hasNext()) {
                int possibleValue = (int) iterator.next();
                Board trialBoard = board.copy();
                trialBoard.setFilledValue(unfilledCell.getX(), unfilledCell.getY(), possibleValue);
                SimpleFillResult trialBoardSimpleFillResult = repeatSimpleFillUntilUnable(trialBoard, Collections.singleton(unfilledCell));
                trialBoard = trialBoardSimpleFillResult.getBoard();
                if (!trialBoardSimpleFillResult.isValidBoard()) {
                    // Remove possible value.
                    iterator.remove();
                } else if (trialBoard.isComplete()) {
                    // We've found the completed board!
                    return new LookAheadFillResult(trialBoard, true, null);
                } else {
                    trialBoards.add(trialBoard);
                }
            }
            // Check if the cell now has only 1 possible value.
            int cellPossibleValues = unfilledCell.getPossibleValues().size();
            if (cellPossibleValues == 1) {
                fillCell(board, unfilledCell, unfilledCell.getPossibleValues().get(0), null);
                return new LookAheadFillResult(board, true, unfilledCell);
            } else if (cellPossibleValues == 0 && unfilledCell.getFilledValue() == 0) {
                return new LookAheadFillResult(board, false, null);
            }

            // Check if any out of all the valid trial boards, any unfilled cells have the same value.
            // If they do, fill it in with that value.
            Cell possibleNewCell = checkTrialBoardsForSameUnfilledCellAndFill(board, trialBoards);
            if (possibleNewCell != null) {
                // If the above worked, return the result.
                return new LookAheadFillResult(board, true, possibleNewCell);
            }

        }
        // If we get here, no new cell values have been found.
        return new LookAheadFillResult(board, true, null);

    }

    public LookAheadFillResult repeatLookAheadOneFillUntilUnable(Board board) {
        LookAheadFillResult lookAheadOneFillResult = lookAheadOneFill(board);
        while (lookAheadOneFillResult.getNewFilledCell() != null) {
            lookAheadOneFillResult = lookAheadOneFill(board);
        }
        return lookAheadOneFillResult;
    }

    public LookAheadFillResult lookAheadTwoFill(Board board) {

        List<Cell> unfilledCellsInBoard = new ArrayList<Cell>(unfilledCellsInBoard(board));
        Collections.sort(unfilledCellsInBoard);
        for (Cell unfilledCell : unfilledCellsInBoard) {
            Iterator iterator = unfilledCell.getPossibleValues().iterator();
            while (iterator.hasNext()) {
                int possibleValue = (int) iterator.next();
                Board trialBoard = board.copy();
                trialBoard.setFilledValue(unfilledCell.getX(), unfilledCell.getY(), possibleValue);
                LookAheadFillResult trialBoardLookAheadOneFillResult = repeatLookAheadOneFillUntilUnable(trialBoard);
                trialBoard = trialBoardLookAheadOneFillResult.getBoard();
                if (!trialBoardLookAheadOneFillResult.isValidBoard()) {
                    // Remove possible value.
                    iterator.remove();
                } else if (trialBoard.isComplete()) {
                    // We've found the completed board!
                    return new LookAheadFillResult(trialBoard, true, null);
                }
            }
            // Check if the cell now has only 1 possible value.
            int cellPossibleValues = unfilledCell.getPossibleValues().size();
            if (cellPossibleValues == 1) {
                fillCell(board, unfilledCell, unfilledCell.getPossibleValues().get(0), null);
                return new LookAheadFillResult(board, true, unfilledCell);
            } else if (cellPossibleValues == 0 && unfilledCell.getFilledValue() == 0) {
                return new LookAheadFillResult(board, false, null);
            }
        }
        // If we get here, no new cell values have been found.
        return new LookAheadFillResult(board, true, null);
    }

    public LookAheadFillResult repeatLookAheadTwoFillUntilUnable(Board board) {
        LookAheadFillResult lookAheadTwoFillResult = lookAheadTwoFill(board);

        while (lookAheadTwoFillResult.getNewFilledCell() != null) {
            lookAheadTwoFillResult = lookAheadTwoFill(board);
        }
        return lookAheadTwoFillResult;
    }

    public void fillCell(Board board, Cell cell, int value, Set<Cell> newCells) {
        cell.setFilledValue(value);
        if (newCells != null) {
            newCells.add(cell);
        }
        updatePossibleValuesOnBoard(board, Collections.singleton(cell));
    }

    public void fillInCellsWithOnlyOnePossibleValue(Board board, Set<Cell> newlyFilledCells) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                Cell cell = board.getCell(i, j);
                if (cell.getFilledValue() == 0) {
                    if (cell.getPossibleValues().size() == 1) {
                        fillCell(board, cell, cell.getPossibleValues().get(0), newlyFilledCells);
                    }
                }
            }
        }
    }

    public void updatePossibleValuesOnBoard(Board board, Set<Cell> newlyFilledCells) {
        for (Cell cell : newlyFilledCells) {
            int cellValue = cell.getFilledValue();
            for (Cell affectedCell : directlyAffectedCells(board, cell)) {
                if (affectedCell.getPossibleValues().contains(cellValue)) {
                    affectedCell.removePossibleValue(cellValue);
                }
            }
        }
    }

    private Set<Cell> directlyAffectedCells(Board board, Cell cell) {
        // Returns all the other cells in the same row, column, or 3x3 box.

        Set<Cell> directlyAffectedCells = new HashSet<>();
        for (Cell cellInTheSameRow : cellsInTheSameRow(board, cell)) {
            directlyAffectedCells.add(cellInTheSameRow);
        }
        for (Cell cellInTheSameColumn : cellsInTheSameColumn(board, cell)) {
            directlyAffectedCells.add(cellInTheSameColumn);
        }
        for (Cell cellInTheSameBox : cellsInTheSameBox(board, cell)) {
            directlyAffectedCells.add(cellInTheSameBox);
        }

        return directlyAffectedCells;
    }

    private Set<Cell> cellsInTheSameRow(Board board, Cell cell) {
        Set<Cell> cellsInTheSameRow = cellsInRow(board, cell.getY());
        cellsInTheSameRow.remove(cell);
        return cellsInTheSameRow;
    }

    private Set<Cell> cellsInTheSameRow(Board board, Set<Cell> cells) {
        // Method assumes that the given set 'cells' are all in the same row already.
        if (cells.isEmpty()) {
            return null;
        }
        Set<Cell> cellsInTheSameRow = cellsInRow(board, cells.iterator().next().getY());
        for (Cell cell : cells) {
            cellsInTheSameRow.remove(cell);
        }
        return cellsInTheSameRow;
    }

    private Set<Cell> cellsInTheSameColumn(Board board, Cell cell) {
        Set<Cell> cellsInTheSameColumn = cellsInColumn(board, cell.getX());
        cellsInTheSameColumn.remove(cell);
        return cellsInTheSameColumn;
    }

    private Set<Cell> cellsInTheSameColumn(Board board, Set<Cell> cells) {
        // Method assumes that the given set 'cells' are all in the same column already.
        if (cells.isEmpty()) {
            return null;
        }
        Set<Cell> cellsInTheSameColumn = cellsInColumn(board, cells.iterator().next().getX());
        for (Cell cell : cells) {
            cellsInTheSameColumn.remove(cell);
        }
        return cellsInTheSameColumn;
    }

    private Set<Cell> cellsInTheSameBox(Board board, Cell cell) {
        // Returns all the other cells in the same 3x3 box as the given cell.
        int boxX = cell.getX() / 3;
        int boxY = cell.getY() / 3;
        Set<Cell> cellsInTheSameBox = cellsInBox(board, boxX, boxY);
        cellsInTheSameBox.remove(cell);
        return cellsInTheSameBox;
    }

    private Set<Cell> cellsInTheSameBox(Board board, Set<Cell> cells) {
        // Method assumes that the given set 'cells' are all in the same box already.
        if (cells.isEmpty()) {
            return null;
        }
        Cell sampleCell = cells.iterator().next();
        Set<Cell> cellsInTheSameBox = cellsInBox(board, sampleCell.getX() / 3, sampleCell.getY() / 3);
        for (Cell cell : cells) {
            cellsInTheSameBox.remove(cell);
        }
        return cellsInTheSameBox;
    }

    private Set<Cell> cellsInRow(Board board, int rowNumber) {
        Set<Cell> cellsInRow = new HashSet<>();
        for (int i = 0; i < 9; i++) {
            cellsInRow.add(board.getCell(i, rowNumber));
        }
        return cellsInRow;
    }

    private Set<Cell> cellsInColumn(Board board, int columnNumber) {
        Set<Cell> cellsInColumn = new HashSet<>();
        for (int i = 0; i < 9; i++) {
            cellsInColumn.add(board.getCell(columnNumber, i));
        }
        return cellsInColumn;
    }

    private Set<Cell> cellsInBox(Board board, int boxX, int boxY) {
        // x = 0, 1, 2 and y = 0, 1, 2
        Set<Cell> cellsInBox = new HashSet<>();
        cellsInBox.add(board.getCell(3 * boxX, 3 * boxY));
        cellsInBox.add(board.getCell(3 * boxX, 3 * boxY + 1));
        cellsInBox.add(board.getCell(3 * boxX, 3 * boxY + 2));
        cellsInBox.add(board.getCell(3 * boxX + 1, 3 * boxY));
        cellsInBox.add(board.getCell(3 * boxX + 1, 3 * boxY + 1));
        cellsInBox.add(board.getCell(3 * boxX + 1, 3 * boxY + 2));
        cellsInBox.add(board.getCell(3 * boxX + 2, 3 * boxY));
        cellsInBox.add(board.getCell(3 * boxX + 2, 3 * boxY + 1));
        cellsInBox.add(board.getCell(3 * boxX + 2, 3 * boxY + 2));
        return cellsInBox;
    }

    private Set<Cell> filledCells(Set<Cell> cells) {
        Set<Cell> filledCells = new HashSet<>();
        for (Cell cell : cells) {
            if (cell.getFilledValue() != 0) {
                filledCells.add(cell);
            }
        }
        return filledCells;
    }

    private Set<Cell> filledCellsInRow(Board board, int rowNumber) {
        return filledCells(cellsInRow(board, rowNumber));
    }

    private Set<Cell> filledCellsInColumn(Board board, int columnNumber) {
        return filledCells(cellsInColumn(board, columnNumber));
    }

    private Set<Cell> filledCellsInBox(Board board, int boxX, int boxY) {
        return filledCells(cellsInBox(board, boxX, boxY));
    }

    private Set<Cell> unfilledCells(Set<Cell> cells) {
        Set<Cell> unfilledCells = new HashSet<>();
        for (Cell cell : cells) {
            if (cell.getFilledValue() == 0) {
                unfilledCells.add(cell);
            }
        }
        return unfilledCells;
    }

    private Set<Cell> unfilledCellsInRow(Board board, int rowNumber) {
        return unfilledCells(cellsInRow(board, rowNumber));
    }

    private Set<Cell> unfilledCellsInColumn(Board board, int columnNumber) {
        return unfilledCells(cellsInColumn(board, columnNumber));
    }

    private Set<Cell> unfilledCellsInBox(Board board, int boxX, int boxY) {
        return unfilledCells(cellsInBox(board, boxX, boxY));
    }

    private Set<Cell> unfilledCellsInBoard(Board board) {
        Set<Cell> unfilledCells = new HashSet<>();
        for (int i = 0; i < 9; i++) {
            for (Cell cell : unfilledCellsInRow(board, i)) {
                unfilledCells.add(cell);
            }
        }
        return unfilledCells;
    }

    private boolean cellsAreInTheSameRow(Set<Cell> cells) {
        // Returns false if cells is empty.
        if (cells.isEmpty()) {
            return false;
        }
        boolean firstCheck = true;
        int firstRowValue = -1;
        for (Cell cell : cells) {
            if (firstCheck) {
                firstRowValue = cell.getY();
                firstCheck = false;
            } else {
                if (cell.getY() != firstRowValue) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean cellsAreInTheSameColumn(Set<Cell> cells) {
        // Returns false if cells is empty.
        if (cells.isEmpty()) {
            return false;
        }
        boolean firstCheck = true;
        int firstColumnValue = -1;
        for (Cell cell : cells) {
            if (firstCheck) {
                firstColumnValue = cell.getX();
                firstCheck = false;
            } else {
                if (cell.getX() != firstColumnValue) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean cellsAreInTheSameBox(Set<Cell> cells) {
        // Returns false if cells is empty.
        if (cells.isEmpty()) {
            return false;
        }
        boolean firstCheck = true;
        int firstBoxX = -1;
        int firstBoxY = -1;
        for (Cell cell : cells) {
            if (firstCheck) {
                firstBoxX = cell.getX() / 3;
                firstBoxY = cell.getY() / 3;
                firstCheck = false;
            } else {
                if ((cell.getX() / 3 != firstBoxX) || (cell.getY() / 3 != firstBoxY)) {
                    return false;
                }
            }
        }
        return true;
    }

    private Set<Integer> valuesRemaining(Set<Cell> allCells) {
        // Given every cell (filled and unfilled), returns all the unfilled values.
        Set<Integer> valuesRemaining = new HashSet<>();
        for (int i = 1; i <= 9; i++) {
            valuesRemaining.add(i);
        }
        for (Cell cell : allCells) {
            int cellValue = cell.getFilledValue();
            if (cellValue != 0) {
                valuesRemaining.remove(cellValue);
            }
        }
        return valuesRemaining;
    }

    private void searchAndFillForUniquePossibleValues(Board board, Set<Cell> newlyFilledCells, Set<Cell> cellsToSearch) {
        Set<Integer> valuesRemaining = new HashSet<>();
        for (Cell cell : cellsToSearch) {
            for (int value : cell.getPossibleValues()) {
                valuesRemaining.add(value);
            }
        }

        Iterator iterator = valuesRemaining.iterator();
        while (iterator.hasNext()) {
            int value = (int) iterator.next();
            boolean valueFoundOnce = false;
            boolean valueFoundMoreThanOnce = false;
            Cell uniqueCell = null;
            for (Cell cell : cellsToSearch) {
                if (cell.getPossibleValues().contains(value)) {
                    if (!valueFoundOnce) {
                        valueFoundOnce = true;
                        uniqueCell = cell;
                    } else {
                        valueFoundMoreThanOnce = true;
                        uniqueCell = null;
                        break;
                    }
                }
            }
            if (valueFoundOnce && !(valueFoundMoreThanOnce) && (uniqueCell != null)) {
                fillCell(board, uniqueCell, value, newlyFilledCells);
                cellsToSearch.remove(uniqueCell);
                iterator.remove();
            }
        }
    }

    private Cell checkTrialBoardsForSameUnfilledCellAndFill(Board board, List<Board> trialBoards) {
        // Checks if all of the trial boards contain any identical cells, which are not already in the original board.
        // If one is found, fill in that cell's value and return the cell.
        for (Cell unfilledCell : unfilledCellsInBoard(board)) {
            int cellX = unfilledCell.getX();
            int cellY = unfilledCell.getY();
            boolean sameValue = true;
            boolean hadFirstValue = false;
            int firstValue = -1;
            for (Board trialBoard : trialBoards) {
                if (trialBoard.getFilledValue(cellX, cellY) == 0) {
                    sameValue = false;
                    break;
                }
                if (!hadFirstValue) {
                    firstValue = trialBoard.getFilledValue(cellX, cellY);
                    hadFirstValue = true;
                } else {
                    if (trialBoard.getFilledValue(cellX, cellY) != firstValue) {
                        sameValue = false;
                        break;
                    }
                }
            }
            if (sameValue == true) {
                fillCell(board, unfilledCell, firstValue, null);
                return unfilledCell;
            }
        }
        return null;
    }

    private String convertBoardToString(Board board) {
        String str = "";
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                String value = String.valueOf(board.getFilledValue(j, i));
                if (value == "0") {
                    str += "-";
                } else {
                    str += board.getFilledValue(j, i);
                }
            }
            str += "\n";
        }
        return str;
    }
}
