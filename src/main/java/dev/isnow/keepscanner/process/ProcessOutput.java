package dev.isnow.keepscanner.process;

import dev.isnow.keepscanner.KeepScannerImpl;
import dev.isnow.keepscanner.checker.ServerChecker;
import dev.isnow.keepscanner.server.impl.IP;
import dev.isnow.keepscanner.server.impl.Server;
import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

public class ProcessOutput
        implements Runnable {

    @Getter
    public Process process;


    public ProcessOutput() {
        startMasscan();
    }

    @Override
    public void run() {
        final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while (process != null && process.isAlive()) {
            try {
                String line = bufferedReader.readLine();
                if (line == null) continue;
                if(line.startsWith("Discovered")) {
                    String ip = line.split("on ")[1].split(" ")[0];
                    int port = Integer.parseInt(line.split("port ")[1].split("/")[0]);
                    if(port == 8080 || port == 8443 || port == 2022 || port == 80 || port == 443 || port == 27017 || port == 6379 || port == 3306 || port == 22 | port == 21) { // ignore common ports
                        continue;
                    }
                    Server scannedServer = KeepScannerImpl.getInstance().getServerManager().getServerByIp(ip);
                    if(scannedServer == null) {
                        System.out.println("WTF? " + ip + ":" + port);
                        continue;
                    }
                    IP backendIP = scannedServer.getBackendIps().stream().filter(ip1 -> ip1.getIp().getHostAddress().replaceAll("\\\\", "").equals(ip)).findFirst().orElse(null);
                    if(backendIP == null) {
                        System.out.println("WTF2? " + ip + ":" + port);
                        continue;
                    }
                    if(backendIP.getAlreadyFoundPorts().contains(port)) {
                        continue;
                    }
                    Thread t = new Thread(() -> {
                        try {
                            String check = ServerChecker.check(ip, port);
                            if(check.equals("FAILED")) {
                                return;
                            }
                            System.out.println(check);
                            backendIP.getAlreadyFoundPorts().add(port);
                            KeepScannerImpl.getInstance().getServerManager().save();
                            KeepScannerImpl.getInstance().getBot().sendHit(check, scannedServer.getName());
                        } catch (NullPointerException ignored) {}
                    });
                    t.start();
                }
                if(line.contains("waiting")) {
                    this.process.destroy();
                    startMasscan();
                }
            } catch (Exception ignored) {}
        }
        this.process.destroy();
        startMasscan();
    }

    private void startMasscan() {
        System.out.println("NEW SCAN!");
        ArrayList<String> ips = new ArrayList<>();

        for(Server server : KeepScannerImpl.getInstance().getServerManager().getServers()) {
            for(IP ip : server.getBackendIps()) {
                ips.add(ip.getIp().getHostAddress().replaceAll("\\\\", ""));
            }
        }
        String ipStr = String.join(",", ips);
        if(ipStr.equals("")) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            startMasscan();
        }
        try {
            final ProcessBuilder ps = new ProcessBuilder("bash", "-c", "masscan -nmap --retries=1 --open-only --range=" + ipStr + " --max-rate 50000 --ports 1-65535");
            ps.redirectErrorStream(true);
            process = ps.start();
            run();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
