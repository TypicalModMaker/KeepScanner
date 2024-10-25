package dev.isnow.keepscanner.discord;

import dev.isnow.keepscanner.KeepScannerImpl;
import dev.isnow.keepscanner.server.impl.IP;
import dev.isnow.keepscanner.server.impl.Server;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.TimeFormat;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.awt.*;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

public class DiscordBot implements EventListener {

    public final JDA bot;
    public String statusMessageID;
    public final String statusChannelID;
    public final String hitChannelID;
    public final String guildID;
    public Guild mainServer;

    public DiscordBot(String token, String statusMessageID, String statusChannelID, String hitChannelID, String guildID) {
        this.statusMessageID = statusMessageID;
        this.statusChannelID = statusChannelID;
        this.hitChannelID = hitChannelID;
        this.guildID = guildID;
        JDABuilder builder = JDABuilder.createDefault(token);

        builder.disableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE);
        builder.setBulkDeleteSplittingEnabled(false);
        builder.setActivity(Activity.watching(KeepScannerImpl.getInstance().getServerManager().getServers().size() + " Servers"));
        builder.addEventListeners(this);
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);
        bot = builder.build();
    }

    @Override
    public void onEvent(GenericEvent genericEvent) {
        if(genericEvent instanceof ReadyEvent) {
            mainServer = bot.getGuildById(guildID);
            editStatus();
            Thread t = new Thread(() -> {
                while(true) {
                    editStatus();
                    try {
                        Thread.sleep(500000);
                    } catch (InterruptedException ignored) {}
                }
            });
            t.start();
        } else if(genericEvent instanceof MessageReceivedEvent) {
            MessageReceivedEvent event = (MessageReceivedEvent) genericEvent;
            // TODO: Improve this shitty code LOLxd
            // Will never happen
            if(event.getMessage().getContentRaw().startsWith("!removeserver")) {
                String[] split = event.getMessage().getContentRaw().split(" ");
                String name = split[1];

                Server currentServer = KeepScannerImpl.getInstance().getServerManager().getServer(name);
                if(currentServer == null) {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("FAILED!");
                    eb.setColor(Color.red);
                    eb.setDescription("Keep Scanner");
                    eb.addField("STATUS", "Failed!", false);
                    eb.addField("STATUS", "This server does not exist!", false);
                    event.getChannel().sendMessageEmbeds(eb.build()).complete();
                } else {
                    KeepScannerImpl.getInstance().getServerManager().removeServer(currentServer);
                    KeepScannerImpl.getInstance().getServerManager().save();
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("SUCCESS!");
                    eb.setColor(Color.green);
                    eb.setDescription("Keep Scanner");
                    eb.addField("STATUS", "Success!", false);
                    eb.addField("STATUS", "Successfully removed the server!", false);
                    event.getChannel().sendMessageEmbeds(eb.build()).complete();
                }
                return;
            }
            if(event.getMessage().getContentRaw().startsWith("!addservers")) {
                String[] split = event.getMessage().getContentRaw().split(" ");

                String[] ips = split[1].split(",");
                if(split[0].contains(":")) {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("FAILED!");
                    eb.setColor(Color.red);
                    eb.setDescription("Keep Scanner");
                    eb.addField("STATUS", "Failed!", false);
                    eb.addField("STATUS", "Please put ips without port numbers!", false);
                    eb.addField("STATUS", "Keep Scanner will automatically scan all ports!", false);
                    event.getChannel().sendMessageEmbeds(eb.build()).complete();
                    return;
                }
                String name = split[2].toLowerCase(Locale.ROOT);
                if(name.contains(".")) {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("FAILED!");
                    eb.setColor(Color.red);
                    eb.setDescription("Keep Scanner");
                    eb.addField("STATUS", "Failed!", false);
                    eb.addField("STATUS", "Invalid Server Name! Dont put . to the name!", false);
                    event.getChannel().sendMessageEmbeds(eb.build()).complete();
                    return;
                }
                Server existingServer = KeepScannerImpl.getInstance().getServerManager().getServer(name);
                if(existingServer != null) {
                    try {
                        existingServer.addIps(ips);
                        KeepScannerImpl.getInstance().getServerManager().save();
                    } catch (UnknownHostException e) {
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle("FAILED!");
                        eb.setColor(Color.red);
                        eb.setDescription("Keep Scanner");
                        eb.addField("STATUS", "Failed!", false);
                        eb.addField("STATUS", "Invalid IP('s)!", false);
                        event.getChannel().sendMessageEmbeds(eb.build()).complete();
                        return;
                    }
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("SUCCESS!");
                    eb.setColor(Color.green);
                    eb.setDescription("Keep Scanner");
                    eb.addField("STATUS", "Success!", false);
                    eb.addField("STATUS", "Successfully added a new IP to " + split[2] + "!", false);
                    event.getChannel().sendMessageEmbeds(eb.build()).complete();
                } else {
                    KeepScannerImpl.getInstance().getServerManager().addServer(new Server(name, new ArrayList<>()));
                    try {
                        KeepScannerImpl.getInstance().getServerManager().getServer(name).addIps(ips);
                        KeepScannerImpl.getInstance().getServerManager().save();
                    } catch (UnknownHostException e) {
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle("FAILED!");
                        eb.setColor(Color.red);
                        eb.setDescription("Keep Scanner");
                        eb.addField("STATUS", "Failed!", false);
                        eb.addField("STATUS", "Invalid IP('s)!", false);
                        event.getChannel().sendMessageEmbeds(eb.build()).complete();
                        return;
                    }
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("SUCCESS!");
                    eb.setColor(Color.green);
                    eb.setDescription("Keep Scanner");
                    eb.addField("STATUS", "Success!", false);
                    eb.addField("STATUS", "Successfully added a new server!", false);
                    event.getChannel().sendMessageEmbeds(eb.build()).complete();
                }
                return;
            }
            if(event.getMessage().getContentRaw().startsWith("!removeip")) {
                String[] split = event.getMessage().getContentRaw().split(" ");

                String ip = split[1];

                Server s = KeepScannerImpl.getInstance().getServerManager().getServerByIp(ip);
                if(s == null) {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("FAILED!");
                    eb.setColor(Color.red);
                    eb.setDescription("Keep Scanner");
                    eb.addField("STATUS", "Failed!", false);
                    eb.addField("STATUS", "Server containing this IP couldn't be found!", false);
                    event.getChannel().sendMessageEmbeds(eb.build()).complete();
                    return;
                }
                s.removeIP(ip);
                KeepScannerImpl.getInstance().getServerManager().save();
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("SUCCESS!");
                eb.setColor(Color.green);
                eb.setDescription("Keep Scanner");
                eb.addField("STATUS", "Success!", false);
                eb.addField("STATUS", "Successfully removed the IP from " + s.getName() + "!", false);
                event.getChannel().sendMessageEmbeds(eb.build()).complete();
            }
            if(event.getMessage().getContentRaw().startsWith("!addserver")) {
                String[] split = event.getMessage().getContentRaw().split(" ");

                String ip = split[1];
                if(ip.contains(":")) {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("FAILED!");
                    eb.setColor(Color.red);
                    eb.setDescription("Keep Scanner");
                    eb.addField("STATUS", "Failed!", false);
                    eb.addField("STATUS", "Please put the ip without the port number!", false);
                    eb.addField("STATUS", "Keep Scanner will automatically scan all ports!", false);
                    event.getChannel().sendMessageEmbeds(eb.build()).complete();
                    return;
                }
                String name = split[2].toLowerCase(Locale.ROOT);
                if(name.contains(".")) {
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("FAILED!");
                    eb.setColor(Color.red);
                    eb.setDescription("Keep Scanner");
                    eb.addField("STATUS", "Failed!", false);
                    eb.addField("STATUS", "Invalid Server Name! Dont put . to the name!", false);
                    event.getChannel().sendMessageEmbeds(eb.build()).complete();
                    return;
                }
                Server existingServer = KeepScannerImpl.getInstance().getServerManager().getServer(name);
                if(existingServer != null) {
                    try {
                        existingServer.getBackendIps().add(new IP(InetAddress.getByName(ip), new ArrayList<>()));
                        KeepScannerImpl.getInstance().getServerManager().save();
                    } catch (UnknownHostException e) {
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle("FAILED!");
                        eb.setColor(Color.red);
                        eb.setDescription("Keep Scanner");
                        eb.addField("STATUS", "Failed!", false);
                        eb.addField("STATUS", "Invalid IP!", false);
                        event.getChannel().sendMessageEmbeds(eb.build()).complete();
                        return;
                    }
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("SUCCESS!");
                    eb.setColor(Color.green);
                    eb.setDescription("Keep Scanner");
                    eb.addField("STATUS", "Success!", false);
                    eb.addField("STATUS", "Successfully added a new IP to " + split[2] + "!", false);
                    event.getChannel().sendMessageEmbeds(eb.build()).complete();
                } else {
                    KeepScannerImpl.getInstance().getServerManager().addServer(new Server(name, new ArrayList<>()));
                    try {
                        KeepScannerImpl.getInstance().getServerManager().getServer(name).getBackendIps().add(new IP(InetAddress.getByName(ip), new ArrayList<>()));
                        KeepScannerImpl.getInstance().getServerManager().save();
                    } catch (UnknownHostException e) {
                        EmbedBuilder eb = new EmbedBuilder();
                        eb.setTitle("FAILED!");
                        eb.setColor(Color.red);
                        eb.setDescription("Keep Scanner");
                        eb.addField("STATUS", "Failed!", false);
                        eb.addField("STATUS", "Invalid IP('s)!", false);
                        event.getChannel().sendMessageEmbeds(eb.build()).complete();
                        return;
                    }
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setTitle("SUCCESS!");
                    eb.setColor(Color.green);
                    eb.setDescription("Keep Scanner");
                    eb.addField("STATUS", "Success!", false);
                    eb.addField("STATUS", "Successfully added a new server!", false);
                    event.getChannel().sendMessageEmbeds(eb.build()).complete();
                }
                return;
            }
            if(event.getMessage().getContentRaw().startsWith("!listservers")) {
                int MAX_CHARACTERS_PER_EMBED = 5000;
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("LIST");
                eb.setColor(Color.BLACK);
                eb.setDescription("Keep Scanner");
                eb.addField("Info", "List of servers:", false);

                int charCount = 0;
                for (Server s : KeepScannerImpl.getInstance().getServerManager().getServers()) {
                    String serverName = s.getName();
                    String ips = s.getBackendIps().stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(",", "{", "}"));
                    String serverInfo = "Name: " + serverName + "\nIPS: " + ips + "\n\n";
                    if (charCount + serverInfo.length() > MAX_CHARACTERS_PER_EMBED) {
                        event.getChannel().sendMessageEmbeds(eb.build()).complete();
                        eb = new EmbedBuilder();
                        eb.setTitle("LIST");
                        eb.setColor(Color.BLACK);
                        eb.setDescription("Keep Scanner");
                        eb.addField("Info", "List of servers:", false);
                        charCount = 0;
                    }
                    eb.addField("Name", serverName, false);
                    eb.addField("IPS:", ips, true);
                    charCount += serverInfo.length();
                }

                if (eb.getFields().size() > 1) {
                    event.getChannel().sendMessageEmbeds(eb.build()).complete();
                }

            }
            if(event.getMessage().getAttachments().stream().filter(attachment -> attachment.getFileExtension() != null && attachment.getFileExtension().equals(".txt")).findFirst().orElse(null) != null) {
                Message.Attachment attachmentobj = event.getMessage().getAttachments().stream().filter(attachment -> attachment.getFileExtension() != null && attachment.getFileExtension().equals(".txt")).findFirst().orElse(null);
                attachmentobj.getProxy().downloadToFile(new File("temp.txt")).thenAccept(file -> {

                });
            }
        }
    }

    public void editStatus() {
        TextChannel channel = mainServer.getChannelById(TextChannel.class, this.statusChannelID);
        try {
            Message message = channel.retrieveMessageById(statusMessageID).complete();
            message.editMessage("ONLINE, Last Edit: " + TimeFormat.RELATIVE.now()).complete();
        } catch (ErrorResponseException e) {
            this.statusMessageID = channel.sendMessage("ONLINE, Last Edit: " + TimeFormat.RELATIVE.now()).complete().getId();
            channel.sendMessage("The new statusMessageID is now: " + statusMessageID);
        }
        setActivity();
    }

    public void setActivity() {
        bot.getPresence().setActivity(Activity.watching(KeepScannerImpl.getInstance().getServerManager().getServers().size() + " Servers"));
    }
    public void sendHit(String quboHit, String serverName) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle("HIT");
        eb.setColor(Color.YELLOW);
        eb.setDescription("Keep Scanner");
        eb.addField("New Server!", ":)", false);
        eb.addField("Info", quboHit, false);
        eb.addField("Server Name", serverName, false);
        TextChannel channel = mainServer.getChannelById(TextChannel.class, hitChannelID);
        channel.sendMessageEmbeds(eb.build()).complete();
        channel.sendMessage("@here").complete();
    }
}
