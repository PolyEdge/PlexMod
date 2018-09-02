package pw.ipex.plex.commandqueue;

public class PlexCommandQueueDelaySet {
    public long commandDelay = 1600L; // 1.6 seconds
    public long lobbySwitchDelay = 4000L; // 4 seconds
    public long joinServerDelay = 4000L; // 4 seconds
    public long chatOpenDelay = 1000L; // 1.0 second

    public long backoffDelay = 3000L; // 3.0 seconds
}
