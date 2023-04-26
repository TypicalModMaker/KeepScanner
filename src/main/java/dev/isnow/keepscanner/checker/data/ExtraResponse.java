package dev.isnow.keepscanner.checker.data;

import com.google.gson.annotations.SerializedName;
import dev.isnow.keepscanner.checker.rawData.ExtraDescription;

public class ExtraResponse extends MCResponse
{
    @SerializedName("description")
    private ExtraDescription description;
    
    public FinalResponse toFinalResponse() {
        return new FinalResponse(this.players, this.version, this.favicon, this.description.getText());
    }
}
