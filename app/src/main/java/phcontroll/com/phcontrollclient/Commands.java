package phcontroll.com.phcontrollclient;

import org.jetbrains.annotations.Contract;

/**
 * Created by root on 21.07.17.
 */
public enum Commands {
    VOL_UP("UP"),
    VOL_DOWN("DOWN");

    private String _text;

    Commands(String text) {
        _text = text;
    }

    @Contract(pure = true)
    public byte[] getBytes() {
        return _text.getBytes();
    }
}
