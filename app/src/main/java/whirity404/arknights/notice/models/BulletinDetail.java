package whirity404.arknights.notice.models;

public class BulletinDetail {
    private String title;
    private String header;
    private String content;
    private String jumpLink;
    private String bannerImageUrl;
    private String displayTime;

    // Getters
    public String getTitle() { return title; }
    public String getHeader() { return header; }
    public String getContent() { return content; }
    public String getJumpLink() { return jumpLink; }
    public String getBannerImageUrl() { return bannerImageUrl; }
    public String getDisplayTime() { return displayTime; }

    // Setters
    public void setTitle(String title) { this.title = title; }
    public void setHeader(String header) { this.header = header; }
    public void setContent(String content) { this.content = content; }
    public void setJumpLink(String jumpLink) { this.jumpLink = jumpLink; }
    public void setBannerImageUrl(String bannerImageUrl) { this.bannerImageUrl = bannerImageUrl; }
    public void setDisplayTime(String displayTime) { this.displayTime = displayTime; }
}
