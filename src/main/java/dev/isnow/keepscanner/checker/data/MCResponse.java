package dev.isnow.keepscanner.checker.data;

import com.google.gson.annotations.SerializedName;
import dev.isnow.keepscanner.checker.rawData.Players;
import dev.isnow.keepscanner.checker.rawData.Version;

class MCResponse
{
    @SerializedName("players")
    Players players;
    @SerializedName("version")
    Version version;
    @SerializedName("favicon")
    String favicon;
}
