package dev.isnow.keepscanner.checker.rawData;

import com.google.gson.annotations.SerializedName;

public class Version
{
    @SerializedName("name")
    private String name;
    @SerializedName("protocol")
    private int protocol;
    
    public void setName(final String a) {
        this.name = a;
    }
    
    public String getName() {
        return this.name;
    }
}
