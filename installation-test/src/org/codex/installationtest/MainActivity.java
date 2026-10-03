package org.codex.installationtest;
import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
public final class MainActivity extends Activity {
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        TextView message = new TextView(this);
        message.setText("Installation successful\n\nThis test app has no permissions and performs no other actions. You can remove it after testing.");
        message.setTextSize(28);
        message.setPadding(40, 40, 40, 40);
        setContentView(message);
    }
}
