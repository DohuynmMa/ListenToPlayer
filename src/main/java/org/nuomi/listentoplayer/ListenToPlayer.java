package org.nuomi.listentoplayer;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

public final class ListenToPlayer extends JavaPlugin implements Listener {
    private ArrayList<String> PlayerMessages = new ArrayList<>();
    private ArrayList<String> PlayerCommands = new ArrayList<>();
    private List<String> CheckWords;
    private boolean isTheWord;
    private String host;
    private String port;
    private String username;
    private String password;
    private String recipient;
    private int taskId;
    @Override
    public void onEnable() {
        getServer().getConsoleSender().sendMessage("ListenToPlayer插件已启动！");
        getLogger().info("LTP配置文件已加载！");
        saveDefaultConfig();
        this.getCommand("ltp").setTabCompleter(new CommandTab());
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("LTP插件初始化完成！");
        Calendar now = Calendar.getInstance();
        int currentHour = now.get(Calendar.HOUR_OF_DAY);
        int targetHour;
        if(getConfig().getInt("targetHour") > 0) {
                targetHour = getConfig().getInt("targetHour");
            }
        else {
                targetHour = 22;
                getConfig().set("targetHour", 22);
                saveConfig();
            }
        long delay;
        if (currentHour < targetHour) {
                delay = (targetHour - currentHour) * 60 * 60 * 20L;
            }
        else {
                delay = (24 - (currentHour - targetHour)) * 60 * 60 * 20L;
            }
        taskId = Bukkit.getScheduler().runTaskTimer(this, () -> {
                if(getConfig().getBoolean("time-send-email")) {
                    sendEmail(MixingMessagesAndCommands(PlayerCommands,PlayerMessages,"PlayerCommands-Auto","PlayerMessages-Auto"));
                    PlayerMessages.clear();
                    PlayerCommands.clear();
                    getLogger().info("LTP >> 每日玩家消息定时邮件发送成功！");
                }
            }, delay, 24 * 60 * 60 * 20L).getTaskId();

    }

    public void sendEmail(ArrayList<String> s1) {
        this.host = getConfig().getString("host");
        this.port = getConfig().getString("port");
        this.username = getConfig().getString("username");
        this.password = getConfig().getString("password");
        this.recipient = getConfig().getString("to");
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message emailMessage = new MimeMessage(session);
            emailMessage.setFrom(new InternetAddress(username));
            emailMessage.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipient));
            emailMessage.setSubject("玩家消息");
            StringBuilder messageContent = new StringBuilder();
            for (String message : s1) {
                messageContent.append(message).append("\n");
            }
            emailMessage.setText(messageContent.toString());
            Transport.send(emailMessage);
        } catch (MessagingException e) {
            getLogger().severe("发送邮件出现错误： " + e.getMessage());
        }
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        isTheWord = false;
        CheckWords = getConfig().getStringList("checking-words");
        if(CheckWords.size() == 0) isTheWord = true;
        else {
            for (int i = 0; i < CheckWords.size(); i++) {
                if (message.contains(CheckWords.get(i))) {
                    isTheWord = true;
                    break;
                } else isTheWord = false;
            }
        }
        if(isTheWord){
            Player player = event.getPlayer();
            String playerName = player.getName();
            LocalDateTime timestamp = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedTime = timestamp.format(formatter);
            PlayerMessages.add(" [ " + formattedTime + " ] " + playerName + " 说： " + message);
        }
    }

    @EventHandler
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        Player player = event.getPlayer();
        String playerName = player.getName();
        LocalDateTime timestamp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = timestamp.format(formatter);
        PlayerCommands.add(" [ " + formattedTime + " ] " + playerName + " 发送了指令： " + command);
    }

    private ArrayList<String> MixingMessagesAndCommands(ArrayList<String> a , ArrayList<String> b , String aTop , String bTop) {
        ArrayList<String> mixed = new ArrayList<String>();
        mixed.add(aTop);
        for (int i = 0; i < a.size(); i++) {
            mixed.add(PlayerCommands.get(i));
        }
        mixed.add(bTop);
        for (int i = 0; i < b.size(); i++) {
            mixed.add(PlayerMessages.get(i));
        }
        return mixed;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
        Player sender = (Player) commandSender;
        String m1 = args.length >= 1 ? args[0] : "help";
        if (s.equalsIgnoreCase("ltp") && sender.hasPermission("listentomessage.op")){
            if (m1.equalsIgnoreCase("help")) return sendHelpMessage(sender);
            if (m1.equalsIgnoreCase("mail")) {
                String m2 = args.length >= 2 ? args[1] : "all";
                if(m2.equalsIgnoreCase("all")){
                    sendEmail(MixingMessagesAndCommands(PlayerCommands,PlayerMessages,"PlayerCommands","PlayerMessages"));
                    PlayerCommands.clear();
                    PlayerMessages.clear();
                    sender.sendMessage("§e§lLTP >> §b邮件已发送！mode: " + m2);
                    return true;
                }
                else if(m2.equalsIgnoreCase("message")){
                    sendEmail(PlayerMessages);
                    PlayerMessages.clear();
                    sender.sendMessage("§e§lLTP >> §b邮件已发送！mode: " + m2);
                    return true;
                }
                else if(m2.equalsIgnoreCase("command")){
                    sendEmail(PlayerCommands);
                    PlayerCommands.clear();
                    sender.sendMessage("§e§lLTP >> §b邮件已发送！mode: " + m2);
                    return true;
                }
                else {
                    sender.sendMessage("§e§lLTP >> §b未知数据类型！ " + m2);
                    return false;
                }
            }
            if (m1.equalsIgnoreCase("clear")) {
                String m2 = args.length >= 2 ? args[1] : "error";
                if(m2.equalsIgnoreCase("all")){
                    PlayerCommands.clear();
                    PlayerMessages.clear();
                    sender.sendMessage("§e§lLTP >> §b数据已清理！mode: " + m2);
                    return true;
                }
                else if(m2.equalsIgnoreCase("command")){
                    PlayerCommands.clear();
                    sender.sendMessage("§e§lLTP >> §b数据已清理！mode: " + m2);
                    return true;
                }
                else if(m2.equalsIgnoreCase("message")){
                    PlayerMessages.clear();
                    sender.sendMessage("§e§lLTP >> §b数据已清理！mode: " + m2);
                    return true;
                }
                else {
                    sender.sendMessage("§e§lLTP >> §b未知数据类型！mode: " + m2);
                    return false;
                }
            }
            if (m1.equalsIgnoreCase("list")) {
                String m2 = args.length >= 2 ? args[1] : "error";
                if(m2.equalsIgnoreCase("message")) {
                    StringBuilder listmessage = new StringBuilder();
                    for (String message : PlayerMessages) {
                        listmessage.append(message).append("\n");
                    }
                    sender.sendMessage("§e§l        ——LTP玩家消息列表——");
                    sender.sendMessage("§e" + listmessage.toString());
                    return true;
                }
                else if(m2.equalsIgnoreCase("command")) {
                    StringBuilder listmessage = new StringBuilder();
                    for (String message : PlayerCommands) {
                        listmessage.append(message).append("\n");
                    }
                    sender.sendMessage("§e§l        ——LTP玩家指令列表——");
                    sender.sendMessage("§e" + listmessage.toString());
                    return true;
                }
                else {
                    sender.sendMessage("§e§lLTP >> §b未知数据类型！mode: " + m2);
                    return false;
                }
            }
            if (m1.equalsIgnoreCase("word")) {
                String m2 = args.length >= 2 ? args[1] : "<cancel>";
                if(m2.equalsIgnoreCase("<cancel>"))
                {
                    sender.sendMessage("§e§lLTP >> §b取消操作");
                    return false;
                }
                if(m2.equalsIgnoreCase("add")){
                    String m3 = args.length >= 3 ? args[2] : "<cancel>";
                    if(m3.equalsIgnoreCase("<cancel>"))
                    {
                        sender.sendMessage("§e§lLTP >> §b取消操作");
                        return false;
                    }
                    else {
                        CheckWords = getConfig().getStringList("checking-words");
                        CheckWords.add(m3);
                        getConfig().set("checking-words", CheckWords);
                        saveConfig();
                        sender.sendMessage("§e§lLTP >> §b已添加检测词到配置文件- §e" + m3);
                        return true;
                    }
                }
                if(m2.equalsIgnoreCase("clear")){
                    CheckWords.clear();
                    getConfig().set("checking-words", CheckWords);
                    saveConfig();
                    sender.sendMessage("§e§lLTP >> §b已清空所有检测词！");
                    return true;
                }
                if(m2.equalsIgnoreCase("list")){
                    CheckWords = getConfig().getStringList("checking-words");
                    StringBuilder listwords = new StringBuilder();
                    for (String message : CheckWords) {
                        listwords.append(message).append("\n");
                    }
                    sender.sendMessage("§e§l        ——LTP检测词列表——");
                    sender.sendMessage("§e" + listwords.toString());
                    return true;
                }
            }
            if (m1.equalsIgnoreCase("time")) {
                String m2 = args.length >= 2 ? args[1] : "<cancel>";
                if(m2.equalsIgnoreCase("<cancel>")) return sendHelpMessage(sender);
                if(m2.equalsIgnoreCase("set")) {
                    int m3 = args.length >= 3 ? Integer.parseInt(args[2]) : 22;
                    getConfig().set("targetHour", m3);
                    saveConfig();
                    sender.sendMessage("§e§lLTP >> §b已设置定时发送邮件时间- §e" + m3 + " §b将在下次重启服务器或下次定时邮件生效！");
                    return true;
                }
                if(m2.equalsIgnoreCase("true")){
                    getConfig().set("time-send-email", true);
                    saveConfig();
                    sender.sendMessage("§e§lLTP >> §b已设置定时发送邮件：TRUE");
                    return true;
                }
                if(m2.equalsIgnoreCase("false")){
                    getConfig().set("time-send-email", false);
                    saveConfig();
                    sender.sendMessage("§e§lLTP >> §b已设置定时发送邮件：FALSE");
                    return true;
                }
            }
            if (m1.equalsIgnoreCase("reload")) {
                reloadConfig();
                sender.sendMessage("§e§lLTP >> §b插件重载完成！");
                return true;
            }
        }
        return false;
    }
    private boolean sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§e§l        ——ListenToPlayer信息监听插件 帮助——");
        sender.sendMessage("§b  邮箱配置请前往配置文件编辑~");
        sender.sendMessage("§e /ltp reload §b重载配置文件");
        sender.sendMessage("§e /ltp list                §b列出收集到的信息");
        sender.sendMessage("§e          - message       §b玩家发送的消息");
        sender.sendMessage("§e          - command       §b玩家发送的指令");
        sender.sendMessage("§e /ltp mail                §b通过邮件将保存的玩家信息发送给OP，并清空玩家信息列表重新收集");
        sender.sendMessage("§e /ltp clear               §b清理收集到的信息");
        sender.sendMessage("§e          - all           §b玩家发送的指令+信息");
        sender.sendMessage("§e          - message       §b玩家发送的消息");
        sender.sendMessage("§e          - command       §b玩家发送的指令");
        sender.sendMessage("§e /ltp word                §b操作检测词，当检测词不为空，则只监听含检测词消息，反之全部监听。");
        sender.sendMessage("§e          - add <String>  §b添加检测词");
        sender.sendMessage("§e          - clear         §b清空检测词");
        sender.sendMessage("§e          - list          §b查看当前的检测词");
        sender.sendMessage("§b  若要快速编辑指定检测词-推荐前往配置文件");
        sender.sendMessage("§e /ltp time                §b操作检测词，当检测词不为空，则只监听含检测词消息，反之全部监听。");
        sender.sendMessage("§e          - set <int>     §b设置每日定时发送邮件时间");
        sender.sendMessage("§e          - true          §b开启每日定时发送邮件");
        sender.sendMessage("§e          - false         §b关闭每日定时发送邮件");
        sender.sendMessage("§b  玩家指令暂不支持识别word！");
        return false;
    }
    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTask(taskId);
    }
}

