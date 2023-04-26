package dev.isnow.keepscanner.checker.data;

import dev.isnow.keepscanner.checker.rawData.Players;
import dev.isnow.keepscanner.checker.rawData.Version;
public class FinalResponse extends MCResponse
{
    private final String description;
    
    public FinalResponse(final Players players, final Version version, final String favicon, final String description) {
        this.description = description;
        this.favicon = favicon;
        this.players = players;
        this.version = version;
    }
    
    public Players getPlayers() {
        return this.players;
    }
    
    public Version getVersion() {
        return this.version;
    }
    
    public String getDescription() {
        return this.description;
    }
}
