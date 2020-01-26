package cc.dyspore.plex.mods.messagingscreen.callback;

import cc.dyspore.plex.Plex;

import cc.dyspore.plex.core.util.PlexUtilRender;
import cc.dyspore.plex.core.util.PlexUtilChat;
import cc.dyspore.plex.mods.messagingscreen.render.PlexMessagingMessageHoverState;

public class PlexMessagingMessageEventParty extends PlexMessagingMessageEventHandler {
    public void onClick(PlexMessagingMessageHoverState hoverState, int button) {
        if (!(hoverState.message.parentAdapter.regexEntryName.equals("party_invite") || hoverState.message.parentAdapter.regexEntryName.equals("party_invite_local"))) {
            return;
        }
        if (button != 0) {
            return;
        }
        if (hoverState.message.getBreakdownItemByIndex(hoverState.globalStringOffset).equals("!ACCEPT_BUTTON") && hoverState.message.hasTag("invitation_sender_ign")) {
            Plex.minecraft.thePlayer.sendChatMessage("/party accept " + hoverState.message.getTag("invitation_sender_ign"));
        }
        else if (hoverState.message.getBreakdownItemByIndex(hoverState.globalStringOffset).equals("!DENY_BUTTON") && hoverState.message.hasTag("invitation_sender_ign")) {
            Plex.minecraft.thePlayer.sendChatMessage("/party deny " + hoverState.message.getTag("invitation_sender_ign"));
        }
    }

    public void onHover(PlexMessagingMessageHoverState hoverState) {
        if (!(hoverState.message.parentAdapter.regexEntryName.equals("party_invite") || hoverState.message.parentAdapter.regexEntryName.equals("party_invite_local"))) {
            return;
        }
        if (hoverState.message.getBreakdownItemByIndex(hoverState.globalStringOffset).equals("!ACCEPT_BUTTON") && hoverState.message.hasTag("invitation_sender_ign")) {
            PlexUtilRender.drawTooltip(PlexUtilChat.chatFromAmpersand("&aClick to accept the invite."), hoverState.mouseX, hoverState.mouseY);
        }
        else if (hoverState.message.getBreakdownItemByIndex(hoverState.globalStringOffset).equals("!DENY_BUTTON") && hoverState.message.hasTag("invitation_sender_ign")) {
            PlexUtilRender.drawTooltip(PlexUtilChat.chatFromAmpersand("&cClick to deny the invite."), hoverState.mouseX, hoverState.mouseY);
        }
    }
}
