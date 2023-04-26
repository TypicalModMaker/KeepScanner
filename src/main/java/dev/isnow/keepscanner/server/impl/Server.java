package dev.isnow.keepscanner.server.impl;

import dev.isnow.keepscanner.KeepScannerImpl;
import lombok.Data;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Data
public class Server {
    private final String name;
    private final List<IP> backendIps;

    public void addIps(String[] ips) throws UnknownHostException {
        for(String ip : ips) {
            InetAddress inetAddress = InetAddress.getByName(ip);
            backendIps.add(new IP(inetAddress, new ArrayList<>()));
        }
    }

    public void removeIP(String ip) {
        IP ipObj = backendIps.stream().filter(ip1 -> ip1.getIp().getHostAddress().replaceAll("\\\\", "").equals(ip)).findFirst().orElse(null);
        if(ipObj != null) {
            backendIps.remove(ipObj);
            KeepScannerImpl.getInstance().getConfig().getConfigurationSection("servers." + name + ".backendIps").set(ipObj.getIp().getHostAddress().replaceAll("\\\\", "").replaceAll("\\.", "-"), null);
        }
    }
}
