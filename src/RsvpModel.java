public class RsvpModel {
    int id;
    int eventId;
    String remindAt;
    String eventTitle;
    String startDateTime;
    String endDateTime;
    String description;
    String hostName;

    public RsvpModel() {
        this.id = -1;
        this.eventId = -1;
        this.remindAt = null;
        this.eventTitle = null;
        this.startDateTime = null;
        this.endDateTime = null;
        this.description = null;
        this.hostName = null;
    }
}
