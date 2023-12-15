public class ReminderModel {
    int id;
    int eventId;
    String remindAt;
    String eventTitle;
    String startDateTime;
    String endDateTime;
    String description;
    String hostName;

    public ReminderModel() {
        id = -1;
        eventId = -1;
        remindAt = null;
        eventTitle = null;
        startDateTime = null;
        endDateTime = null;
        description = null;
        hostName = null;
    }
}
