package pw.ipex.plex.mods.messagingscreen.callback;

import pw.ipex.plex.Plex;

import pw.ipex.plex.core.PlexCoreUtils;
import pw.ipex.plex.mods.messagingscreen.render.PlexMessagingMessageHoverState;

public class PlexMessagingMessageEventParty extends PlexMessagingMessageEventHandler {
    public void onClick(PlexMessagingMessageHoverState hoverState, int button) {
        if (button == 0) {
            if (hoverState.message.parentAdapter.regexEntryName.equals("party_invite") && hoverState.message.getBreakdownItemByIndex(hoverState.globalStringOffset) != null) {
                if (hoverState.message.getBreakdownItemByIndex(hoverState.globalStringOffset).equals("!ACCEPT_BUTTON") && hoverState.message.hasTag("invitation_sender_ign")) {
                    Plex.minecraft.thePlayer.sendChatMessage("/party accept " + hoverState.message.getTag("invitation_sender_ign"));
                }
                if (hoverState.message.getBreakdownItemByIndex(hoverState.globalStringOffset).equals("!DENY_BUTTON") && hoverState.message.hasTag("invitation_sender_ign")) {
                    Plex.minecraft.thePlayer.sendChatMessage("/party deny " + hoverState.message.getTag("invitation_sender_ign"));
                }
            }
        }
    }

    public void onHover(PlexMessagingMessageHoverState hoverState) {
        if (hoverState.message.parentAdapter.regexEntryName.equals("party_invite") && hoverState.message.getBreakdownItemByIndex(hoverState.globalStringOffset) != null) {
            if (hoverState.message.getBreakdownItemByIndex(hoverState.globalStringOffset).equals("!ACCEPT_BUTTON") && hoverState.message.hasTag("invitation_sender_ign")) {
                Plex.renderUtils.staticDrawTooltip(PlexCoreUtils.ampersandToFormatCharacter("&aClick to accept the invite."), hoverState.mouseX, hoverState.mouseY);
            }
            if (hoverState.message.getBreakdownItemByIndex(hoverState.globalStringOffset).equals("!DENY_BUTTON") && hoverState.message.hasTag("invitation_sender_ign")) {
                Plex.renderUtils.staticDrawTooltip(PlexCoreUtils.ampersandToFormatCharacter("&cClick to deny the invite."), hoverState.mouseX, hoverState.mouseY);
            }
        }
    }
}
