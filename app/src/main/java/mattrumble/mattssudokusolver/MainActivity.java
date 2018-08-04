package mattrumble.mattssudokusolver;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Scanner;

import android.util.Log;

import mattrumble.mattssudokusolver.Logic.Board;
import mattrumble.mattssudokusolver.Logic.FillResult;
import mattrumble.mattssudokusolver.Logic.Solver;

public class MainActivity extends AppCompatActivity {

    private int unselectedCellColor = Color.rgb(165, 237, 182);
    private int selectedCellColor = Color.rgb(43, 143, 43);

    private TextView[][] cells = new TextView[9][9];
    private boolean cellsAreSelectable = true;
    private TextView previousCell = null;
    private TextView selectedCell = null;
    private Button[] numberButtons = new Button[9];
    private Button buttonClear = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Add listener to the clear button
        buttonClear = (Button) findViewById(R.id.buttonClear);
        buttonClear.setOnClickListener(new ClearButtonListener(this, buttonClear));
        buttonClear.setEnabled(false);

        // Add listeners to the number buttons
        for (int i = 1; i <= 9; i++) {
            int resID = getResources().getIdentifier("button" + i, "id", getPackageName());
            Button numberButton = (Button) findViewById(resID);
            numberButton.setOnClickListener(new NumberButtonListener(this, i));
            numberButtons[i - 1] = numberButton;
        }

        // Add the TextView cells to an array
        for (int i = 0; i < 81; i++) {
            int resID = getResources().getIdentifier("textView" + (i + 1), "id", getPackageName());
            TextView tv = (TextView) findViewById(resID);
            cells[i % 9][i / 9] = tv;
            tv.setOnClickListener(new TextViewListener(this));
        }

        //presetBoard();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void setSelectedCell(TextView cell) {
        if (!cellsAreSelectable) {
            return;
        }
        previousCell = selectedCell;
        selectedCell = cell;
        if (previousCell != null) {
            previousCell.setBackgroundColor(unselectedCellColor);
        }
        selectedCell.setBackgroundColor(selectedCellColor);
        if (cellIsEmpty(selectedCell)) {
            buttonClear.setEnabled(false);
        } else {
            buttonClear.setEnabled(true);
        }
    }

    public void deselectSelectedCell() {
        if (selectedCell != null) {
            selectedCell.setBackgroundColor(unselectedCellColor);
            selectedCell = null;
        }
    }

    public boolean aCellIsSelected() {
        if (selectedCell == null) {
            return false;
        }
        return true;
    }

    public void setCellNumber(TextView cell, int number) {
        if (cell == null) {
            Toast.makeText(getApplicationContext(), "Error: No cell selected", Toast.LENGTH_SHORT).show();
            return;
        }
        // If number == 0, then make the cell empty.
        if (number == 0) {
            cell.setText("");
        } else {
            cell.setText(String.valueOf(number));
            buttonClear.setEnabled(true);
        }
    }

    public TextView getSelectedCell() {
        return selectedCell;
    }

    public void clearCell(TextView cell) {
        setCellNumber(cell, 0);
    }

    public void clearAllCells() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                clearCell(cells[i][j]);
            }
        }
    }

    public boolean cellIsEmpty(TextView cell) {
        if (cell.getText() == "") {
            return true;
        }
        return false;
    }

    public void clearAllButtonClick(MenuItem menuItem) {
        clearAllCells();
        setAllNumberButtonsEnabled(true);
        cellsAreSelectable = true;
        buttonClear.setEnabled(false);
    }

    public void solveButtonClick(MenuItem menuItem) {
        setAllNumberButtonsEnabled(false);
        buttonClear.setEnabled(false);
        cellsAreSelectable = false;
        deselectSelectedCell();

        Board startingBoard = convertCurrentCellsToBoard();
        FillResult solveResult = new Solver(startingBoard).solve();
        setCellsToMatchBoard(solveResult.getBoard());
        if (solveResult.isValidBoard()) {
            Toast.makeText(getApplicationContext(), "Solving complete.", Toast.LENGTH_SHORT).show();
            buttonClear.setEnabled(false);
        } else {
            Toast.makeText(getApplicationContext(), "Invalid board given.", Toast.LENGTH_SHORT).show();
        }

    }

    public void setAllNumberButtonsEnabled(boolean state) {
        for (int i = 0; i < 9; i++) {
            numberButtons[i].setEnabled(state);
        }
    }

    public void setCellsToMatchBoard(Board board) {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                setCellNumber(cells[i][j], board.getFilledValue(i, j));
            }
        }
    }

    private Board convertCurrentCellsToBoard() {
        Board board = new Board();
        for (int y = 0; y < 9; y++) {
            for (int x = 0; x < 9; x++) {
                String textValue = (String) cells[x][y].getText();
                if (!textValue.equals("")) {
                    board.setFilledValue(x, y, Integer.parseInt(textValue));
                }
            }
        }
        return board;
    }

    public String convertBoardToString(Board board) {
        String str = "";
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                str += board.getFilledValue(j, i);
            }
            str += "\n";
        }
        return str;
    }

    public Board convertStringToBoard(String str) {
        Scanner reader = new Scanner(str);
        Board board = new Board();
        for (int i = 0; i < 9; i++) {
            String line = reader.nextLine();
            for (int j = 0; j < 9; j++) {
                int value = Character.getNumericValue(line.charAt(j));
                if (value != 0) {
                    board.setFilledValue(j, i, value);
                }
            }
        }
        return board;
    }

    private void presetBoard() {
        String test1 = ""; // Easy one
        test1 += "903542000" + "\n";
        test1 += "040000089" + "\n";
        test1 += "706900000" + "\n";
        test1 += "508167000" + "\n";
        test1 += "070403090" + "\n";
        test1 += "000289706" + "\n";
        test1 += "000004905" + "\n";
        test1 += "420000070" + "\n";
        test1 += "000796402" + "\n";

        String test2 = ""; // Hard one
        test2 += "120400300" + "\n";
        test2 += "300010050" + "\n";
        test2 += "006000100" + "\n";
        test2 += "700090000" + "\n";
        test2 += "040603000" + "\n";
        test2 += "003002000" + "\n";
        test2 += "500080700" + "\n";
        test2 += "007000005" + "\n";
        test2 += "000000098" + "\n";

        setCellsToMatchBoard(convertStringToBoard(test1));
        buttonClear.setEnabled(false);
    }
}
