package cc.dyspore.plex.commands.client;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.ClientCommandHandler;
import cc.dyspore.plex.Plex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class PlexCommandListener {
    private boolean enabled = true;
    private Availability activation = Availability.MINEPLEX;
    private DisabledAction disabledAction = DisabledAction.PROMPT;
    private PlexCommandHandler handler;

    public List<PlexClientCommandListener> listeners;

    public PlexCommandListener(String ...commandNames) {
        this.listeners = new ArrayList<>();
        for (String name : commandNames) {
            PlexClientCommandListener listener = new PlexClientCommandListener(name, this);
            this.listeners.add(listener);
            ClientCommandHandler.instance.registerCommand(listener);
        }
    }

    public boolean isActive() {
        if (!this.enabled) {
            return false;
        }
        switch (this.activation) {
            case GLOBAL:
                return true;
            case MULTIPLAYER:
                return Plex.gameState.isMultiplayer;
            case MINEPLEX:
                return Plex.gameState.isMineplex;
            default:
                return false;
        }
    }

    public PlexCommandListener setHandler(PlexCommandHandler handler) {
        this.handler = handler;
        return this;
    }

    public PlexCommandListener setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public PlexCommandListener setAvailability(Availability activation) {
        this.activation = activation;
        return this;
    }

    public PlexCommandListener setDisabledAction(DisabledAction disabledAction) {
        this.disabledAction = disabledAction;
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

    public void processCommand(PlexClientCommandListener item, ICommandSender sender, String[] args) throws CommandException  {
        if (!this.isActive() || this.handler == null) {
            switch (this.disabledAction) {
                case PROMPT:
                case PASSTHROUGH:
                    item.sendToServer(args);
            }
            return;
        }
        this.handler.processCommand(sender, getCommandNamespace(args), getCommandArgs(args));
    }

    public List<String> processTabCompletion(PlexClientCommandListener item, ICommandSender sender, String[] args, BlockPos pos) {
        if (!this.isActive() || this.handler == null) {
            switch (this.disabledAction) {
                case PROMPT:
                    return Collections.emptyList();
                case PASSTHROUGH:
                    return null;
            }
        }
        return this.handler.tabCompletion(sender, getCommandNamespace(args), getCommandArgs(args), pos);
    }

    public enum Availability {
        GLOBAL,
        MULTIPLAYER,
        MINEPLEX
    }

    public enum DisabledAction {
        PASSTHROUGH,
        PROMPT
    }
}
