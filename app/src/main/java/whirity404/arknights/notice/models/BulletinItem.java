package whirity404.arknights.notice.models;

public class BulletinItem {
    private String cid;
    private String title;
    private String displayTime;

    public BulletinItem(String cid, String title, String displayTime) {
        this.cid = cid;
        this.title = title;
        this.displayTime = displayTime;
    }

    // Getters
    public String getCid() { return cid; }
    public String getTitle() { return title; }
    public String getDisplayTime() { return displayTime; }
}
