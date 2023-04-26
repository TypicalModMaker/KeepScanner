package dev.isnow.keepscanner.server.impl;

import lombok.Data;

import java.net.InetAddress;
import java.util.List;

@Data
public class IP {
    private final InetAddress ip;
    private final List<Integer> alreadyFoundPorts;
}
