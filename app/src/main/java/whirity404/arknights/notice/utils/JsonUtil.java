package whirity404.arknights.notice.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import whirity404.arknights.notice.models.BulletinItem;
import whirity404.arknights.notice.models.BulletinDetail;

public class JsonUtil {
    public static List<BulletinItem> parseBulletinList(String jsonStr) throws Exception {
        List<BulletinItem> list = new ArrayList<>();
        JSONObject json = new JSONObject(jsonStr);
        JSONObject data = json.getJSONObject("data");
        JSONArray items = data.getJSONArray("list");

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String cid = item.getString("cid");
            String title = item.getString("title");
            String displayTime = item.getString("displayTime");
            list.add(new BulletinItem(cid, title, displayTime));
        }

        return list;
    }

    public static BulletinDetail parseBulletinDetail(String jsonStr) throws Exception {
        JSONObject json = new JSONObject(jsonStr);
        JSONObject data = json.getJSONObject("data");

        BulletinDetail detail = new BulletinDetail();
        detail.setTitle(data.optString("title", null));
        detail.setHeader(data.optString("header", null));
        detail.setContent(data.optString("content", null));
        detail.setJumpLink(data.optString("jumpLink", null));
        detail.setBannerImageUrl(data.optString("bannerImageUrl", null));
        detail.setDisplayTime(data.getString("displayTime"));

        return detail;
    }
}
