package org.nuomi.listentoplayer;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class CommandTab implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (command.getName().equalsIgnoreCase("ltp")) {
            if (args.length == 1) {
                suggestions.add("reload");
                suggestions.add("mail");
                suggestions.add("clear");
                suggestions.add("list");
                suggestions.add("word");
                suggestions.add("help");
                suggestions.add("time");
            }
            if (args.length == 2) {
                if(args[0].equalsIgnoreCase( "word")) {
                    suggestions.add("add");
                    suggestions.add("clear");
                    suggestions.add("list");
                }
                if(args[0].equalsIgnoreCase( "time")) {
                    suggestions.add("true");
                    suggestions.add("false");
                    suggestions.add("set");
                }
                if(args[0].equalsIgnoreCase( "mail")||args[0].equalsIgnoreCase( "clear")||args[0].equalsIgnoreCase( "list")) {
                    if(!args[0].equalsIgnoreCase( "list")) suggestions.add("all");
                    suggestions.add("message");
                    suggestions.add("command");
                }
            }
            if (args.length == 3) {
                if(args[0].equalsIgnoreCase( "word")) {
                    suggestions.add("<String>");
                }
                if(args[0].equalsIgnoreCase( "time")) {
                    suggestions.add("<int>");
                }
            }
            else return suggestions;
        }
        return suggestions;
    }
}

