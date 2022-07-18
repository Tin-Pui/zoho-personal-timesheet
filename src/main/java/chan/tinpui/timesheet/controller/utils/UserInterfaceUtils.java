package chan.tinpui.timesheet.controller.utils;

import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class UserInterfaceUtils {

    private static final Text HELPER = new Text();
    static {
        HELPER.setWrappingWidth(0.0D);
        HELPER.setLineSpacing(0.0D);
    }

    public static double computeTextWidth(Font font, String text) {
        HELPER.setText(text);
        HELPER.setFont(font);
        HELPER.setWrappingWidth((int) Math.ceil(Math.min(HELPER.prefWidth(-1.0D), 0.0d)));
        return Math.ceil(HELPER.getLayoutBounds().getWidth());
    }
}
