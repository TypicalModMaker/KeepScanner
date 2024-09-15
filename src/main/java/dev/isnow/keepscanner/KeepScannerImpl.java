package dev.isnow.keepscanner;

import dev.isnow.keepscanner.discord.DiscordBot;
import dev.isnow.keepscanner.process.ProcessOutput;
import dev.isnow.keepscanner.server.ServerManager;
import dev.isnow.keepscanner.util.FileUtil;
import lombok.Getter;
import org.bspfsystems.yamlconfiguration.file.YamlConfiguration;

import java.net.UnknownHostException;

@Getter
public class KeepScannerImpl {
    @Getter
    private static KeepScannerImpl instance;

    private ServerManager serverManager;
    private YamlConfiguration config;
    private final DiscordBot bot;

    public KeepScannerImpl(String[] args) {
        instance = this;
        System.out.println("   ▄█   ▄█▄    ▄████████    ▄████████    ▄███████▄    ▄████████  ▄████████    ▄████████ ███▄▄▄▄   ███▄▄▄▄      ▄████████    ▄████████ \n" +
                "  ███ ▄███▀   ███    ███   ███    ███   ███    ███   ███    ███ ███    ███   ███    ███ ███▀▀▀██▄ ███▀▀▀██▄   ███    ███   ███    ███ \n" +
                "  ███▐██▀     ███    █▀    ███    █▀    ███    ███   ███    █▀  ███    █▀    ███    ███ ███   ███ ███   ███   ███    █▀    ███    ███ \n" +
                " ▄█████▀     ▄███▄▄▄      ▄███▄▄▄       ███    ███   ███        ███          ███    ███ ███   ███ ███   ███  ▄███▄▄▄      ▄███▄▄▄▄██▀ \n" +
                "▀▀█████▄    ▀▀███▀▀▀     ▀▀███▀▀▀     ▀█████████▀  ▀███████████ ███        ▀███████████ ███   ███ ███   ███ ▀▀███▀▀▀     ▀▀███▀▀▀▀▀   \n" +
                "  ███▐██▄     ███    █▄    ███    █▄    ███                 ███ ███    █▄    ███    ███ ███   ███ ███   ███   ███    █▄  ▀███████████ \n" +
                "  ███ ▀███▄   ███    ███   ███    ███   ███           ▄█    ███ ███    ███   ███    ███ ███   ███ ███   ███   ███    ███   ███    ███ \n" +
                "  ███   ▀█▀   ██████████   ██████████  ▄████▀       ▄████████▀  ████████▀    ███    █▀   ▀█   █▀   ▀█   █▀    ██████████   ███    ███ \n" +
                "  ▀                                                                                                                        ███    ███ ");
        if(args.length < 4) {
            System.out.println("Invalid arguments! Start with java -jar KeepScanner.jar botToken statusMessageID statusChannelID hitChannelID guildID");
        }
        System.out.println("Starting...");

        long current = System.currentTimeMillis();
        System.out.println("Loading DB...");
        try {
            config = FileUtil.createDB();
        } catch (Exception e) {
            System.out.println("Failed to create the file! Exception: " + e.getMessage());
            System.exit(0);
        }

        try {
            serverManager = new ServerManager(config);
        } catch (UnknownHostException e) {
            System.out.println("Failed to load a host! Exception: " + e.getMessage());
            System.exit(0);
        }

        System.out.println("Initializing Discord Bot...");
        bot = new DiscordBot(args[0], args[1], args[2], args[3], args[4]);

        System.out.println("Initializing Scanner...");

        Thread t = new Thread(ProcessOutput::new);
        t.start();

        System.out.println("SUCCESS! Done in " + (System.currentTimeMillis() - current) / 1000 + " Seconds!");
    }
}
