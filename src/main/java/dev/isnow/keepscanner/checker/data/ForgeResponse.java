package dev.isnow.keepscanner.checker.data;

import com.google.gson.annotations.SerializedName;
import dev.isnow.keepscanner.checker.rawData.Description;
import dev.isnow.keepscanner.checker.rawData.ForgeModInfo;
import dev.isnow.keepscanner.checker.rawData.Players;
import dev.isnow.keepscanner.checker.rawData.Version;

public class ForgeResponse
{
    @SerializedName("description")
    private Description description;
    @SerializedName("players")
    private Players players;
    @SerializedName("version")
    private Version version;
    @SerializedName("modinfo")
    private ForgeModInfo modinfo;
    
    public FinalResponse toFinalResponse() {
        this.version.setName(this.version.getName() + " FML with " + this.modinfo.getNMods() + " mods");
        return new FinalResponse(this.players, this.version, "", this.description.getText());
    }
}
