package dev.isnow.keepscanner.checker;


public class PingOptions
{
    private String hostname;
    private int port;
    private int timeout;
    
    public PingOptions setHostname(final String hostname) {
        this.hostname = hostname;
        return this;
    }
    
    public PingOptions setPort(final int port) {
        this.port = port;
        return this;
    }
    
    public PingOptions setTimeout(final int timeout) {
        this.timeout = timeout;
        return this;
    }
    
    String getHostname() {
        return this.hostname;
    }
    
    int getPort() {
        return this.port;
    }
    
    public int getTimeout() {
        return this.timeout;
    }
}
