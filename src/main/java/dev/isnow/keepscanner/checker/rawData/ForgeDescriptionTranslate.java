package dev.isnow.keepscanner.checker.rawData;

import com.google.gson.annotations.SerializedName;

public class ForgeDescriptionTranslate
{
    @SerializedName("translate")
    private String translate;
    
    public String getTranslate() {
        return this.translate;
    }
}
