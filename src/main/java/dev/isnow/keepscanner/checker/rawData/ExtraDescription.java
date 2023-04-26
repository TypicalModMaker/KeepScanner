package dev.isnow.keepscanner.checker.rawData;

import com.google.gson.annotations.SerializedName;

public class ExtraDescription
{
    @SerializedName("extra")
    private Extra[] extra;
    
    public String getText() {
        final StringBuilder s = new StringBuilder();
        for (final Extra e : this.extra) {
            s.append(e.getText());
        }
        return s.toString();
    }
}
