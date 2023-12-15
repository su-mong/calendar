import java.sql.Timestamp;

public class EventModel {
    int eventId;
    int eventInfoId;
    String title;
    String description;
    Timestamp start;
    Timestamp end;
    int hostId;
    int userId;
    int reminderInterval;
    int reminderTimeFrame;

    public EventModel() {
        eventId = -1;
        eventInfoId = -1;
        title = "";
        description = "";
        start = new Timestamp(0);
        end = new Timestamp(0);
        hostId = -1;
        userId = -1;
        reminderInterval = 0;
        reminderTimeFrame = 60;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public void setEventInfoId(int eventInfoId) {
        this.eventInfoId = eventInfoId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStart(Timestamp start) {
        this.start = start;
    }

    public void setEnd(Timestamp end) {
        this.end = end;
    }

    public void setHostId(int hostId) {
        this.hostId = hostId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setReminderInterval(int reminderInterval) {
        this.reminderInterval = reminderInterval;
    }

    public void setReminderTimeFrame(int reminderTimeFrame) {
        this.reminderTimeFrame = reminderTimeFrame;
    }

    public int getEventId() {
        return eventId;
    }

    public int getEventInfoId() {
        return eventInfoId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Timestamp getStart() {
        return start;
    }

    public Timestamp getEnd() {
        return end;
    }

    public int getHostId() {
        return hostId;
    }

    public int getUserId() {
        return userId;
    }

    public int getReminderInterval() {
        return reminderInterval;
    }

    public int getReminderTimeFrame() {
        return reminderTimeFrame;
    }

    @Override
    public String toString() {
        return this.title;
    }
}