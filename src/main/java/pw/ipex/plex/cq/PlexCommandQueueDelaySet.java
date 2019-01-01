package pw.ipex.plex.cq;

public class PlexCommandQueueDelaySet implements Cloneable {
    public long commandDelay = 1600L;
    public long lobbySwitchDelay = 4000L;
    public long joinServerDelay = 4000L;
    public long chatOpenDelay = 1000L;

    public long backoffDelay = 3000L;

    public PlexCommandQueueDelaySet clone() {
        try {
            return (PlexCommandQueueDelaySet) super.clone();
        }
        catch (CloneNotSupportedException e) {
            return new PlexCommandQueueDelaySet();
        }
    }
}
