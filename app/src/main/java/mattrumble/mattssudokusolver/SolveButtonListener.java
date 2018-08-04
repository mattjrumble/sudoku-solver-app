package mattrumble.mattssudokusolver;

import android.view.View;
import android.widget.Toast;

public class SolveButtonListener implements View.OnClickListener {

    private MainActivity mainActivity;

    public SolveButtonListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    public void onClick(View view) {
        Toast.makeText(mainActivity.getApplicationContext(), String.valueOf(mainActivity.findViewById(R.id.button1).getWidth()), Toast.LENGTH_SHORT).show();
    }

}
