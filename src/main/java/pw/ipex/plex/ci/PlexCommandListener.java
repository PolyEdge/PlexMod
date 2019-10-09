package pw.ipex.plex.ci;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.ClientCommandHandler;
import pw.ipex.plex.Plex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlexCommandListener {
    private boolean enabled = true;
    private boolean global = true;
    private PlexCommandHandler handler;

    public List<PlexCommandListenerClientCommandListener> listeners;

    public PlexCommandListener(String ...commandNames) {
        this.listeners = new ArrayList<>();
        for (String name : commandNames) {
            PlexCommandListenerClientCommandListener listener = new PlexCommandListenerClientCommandListener(name, this);
            this.listeners.add(listener);
            ClientCommandHandler.instance.registerCommand(listener);
        }
    }

    public boolean isActive() {
        return (this.enabled && (this.global || Plex.serverState.onMineplex));
    }

    public PlexCommandListener setHandler(PlexCommandHandler handler) {
        this.handler = handler;
        return this;
    }

    public PlexCommandListener setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public PlexCommandListener setGlobal(boolean global) {
        this.global = global;
        return this;
    }

    public static String getCommandNamespace(String[] args) {
        if (args.length == 0) {
            return "";
        }
        return args[0];
    }

    public static String[] getCommandArgs(String[] args) {
        if (args.length < 2) {
            return new String[] {};
        }
        List<String> finalArgs = Arrays.asList(args).subList(1, (args.length));
        return finalArgs.toArray(new String[0]);
    }

    public void processCommand(PlexCommandListenerClientCommandListener item, ICommandSender sender, String[] args) throws CommandException  {
        if (!this.isActive() || this.handler == null) {
            item.sendAsPlayerCommand(args);
            return;
        }
        this.handler.processCommand(sender, getCommandNamespace(args), getCommandArgs(args));
    }

    public List<String> processTabCompletion(PlexCommandListenerClientCommandListener item, ICommandSender sender, String[] args, BlockPos pos) {
        if (!this.isActive() || this.handler == null) {
            return Collections.emptyList();
        }
        return this.handler.tabCompletion(sender, getCommandNamespace(args), getCommandArgs(args), pos);
    }

}
