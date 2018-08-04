package mattrumble.mattssudokusolver;

import android.view.View;
import android.widget.TextView;

public class TextViewListener implements View.OnClickListener {

    private MainActivity mainActivity;

    public TextViewListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onClick(View view) {
        mainActivity.setSelectedCell((TextView) view);
    }

}
