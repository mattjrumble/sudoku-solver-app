package mattrumble.mattssudokusolver;

import android.view.View;
import android.widget.Toast;

public class NumberButtonListener implements View.OnClickListener{

    private MainActivity mainActivity;
    private int number;

    public NumberButtonListener(MainActivity mainActivity, int number) {
        this.mainActivity = mainActivity;
        this.number = number;
    }

    public void onClick(View view) {
        if (mainActivity.aCellIsSelected()) {
            mainActivity.setCellNumber(mainActivity.getSelectedCell(), number);
        } else {
            Toast.makeText(mainActivity.getApplicationContext(), "No cell selected", Toast.LENGTH_SHORT).show();
        }
    }

}
