package mattrumble.mattssudokusolver;

import android.view.View;
import android.widget.Button;

public class ClearButtonListener implements View.OnClickListener {

    private MainActivity mainActivity;
    private Button button;

    public ClearButtonListener(MainActivity mainActivity, Button button) {
        this.mainActivity = mainActivity;
        this.button = button;
    }

    public void onClick(View view) {
        mainActivity.clearCell(mainActivity.getSelectedCell());
        button.setEnabled(false);
    }

}
