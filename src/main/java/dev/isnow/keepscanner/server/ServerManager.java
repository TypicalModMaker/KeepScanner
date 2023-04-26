package dev.isnow.keepscanner.server;

import dev.isnow.keepscanner.KeepScannerImpl;
import dev.isnow.keepscanner.server.impl.IP;
import dev.isnow.keepscanner.server.impl.Server;
import lombok.Getter;
import org.bspfsystems.yamlconfiguration.configuration.ConfigurationSection;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

@Getter
public class ServerManager {
    private final ArrayList<Server> servers = new ArrayList<>();

    @SuppressWarnings("ALL")
    public ServerManager(YamlConfiguration config) throws UnknownHostException {

        ArrayList<Server> servers = new ArrayList<>();
        if(config.getConfigurationSection("servers") == null) {
            config.createSection("servers");
        }
        for(String s : config.getConfigurationSection("servers").getKeys(false)) {
            ConfigurationSection server = config.getConfigurationSection("servers." + s);
            List<IP> ipList = new ArrayList<>();
            for(String s1 : server.getConfigurationSection("backendIps").getKeys(false)) {
                InetAddress address = InetAddress.getByName(s1.replaceAll("\\-", "."));
                List<Integer> alreadyFoundPorts = server.getConfigurationSection("backendIps." + s1).getIntList("alreadyFoundPorts");
                ipList.add(new IP(address, alreadyFoundPorts));
            }
            servers.add(new Server(s, ipList));
        }

        addServers(servers);
    }


    public void addServer(Server s) {
        this.servers.add(s);
    }

    public void addServers(ArrayList<Server> servers) {
        this.servers.addAll(servers);
    }

    public Server getServer(String name) {
        return this.servers.stream().filter(server -> server.getName().equals(name)).findFirst().orElse(null);
    }

    public Server getServerByIp(String ip) {
        return this.servers.stream().filter(server -> server.getBackendIps().stream().anyMatch(ip1 -> ip1.getIp().getHostAddress().replaceAll("\\\\", "").equals(ip))).findFirst().orElse(null);
    }

    public void removeServer(Server server) {
        this.servers.remove(server);
        KeepScannerImpl.getInstance().getConfig().getConfigurationSection("servers").set(server.getName(), null);
    }

    @SuppressWarnings("ALL")
    public void save() {
        YamlConfiguration config = KeepScannerImpl.getInstance().getConfig();
        for(Server s : servers) {
            if(config.getConfigurationSection("servers").getConfigurationSection(s.getName()) == null) {
                config.getConfigurationSection("servers").createSection(s.getName());
            }
            ConfigurationSection section = config.getConfigurationSection("servers." + s.getName());
            if(section.getConfigurationSection("backendIps") == null) {
                section.createSection("backendIps");
            }

            for(IP ip: s.getBackendIps()) {;
                if(section.getConfigurationSection("backendIps").getConfigurationSection(ip.getIp().getHostAddress().replaceAll("\\.", "-")) == null) {
                    section.getConfigurationSection("backendIps").createSection(ip.getIp().getHostAddress().replaceAll("\\.", "-"));
                }
                section.getConfigurationSection("backendIps").getConfigurationSection(ip.getIp().getHostAddress().replaceAll("\\.", "-")).set("alreadyFoundPorts", ip.getAlreadyFoundPorts());
            }
        }

        try {
            config.save(new File("servers.yml"));
        } catch (IOException e) {
            System.out.println("Failed to save the DB!");
        }
    }
}
