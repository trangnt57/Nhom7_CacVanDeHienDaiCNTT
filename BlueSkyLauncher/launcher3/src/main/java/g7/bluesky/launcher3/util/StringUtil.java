package g7.bluesky.launcher3.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by tuanpt on 11/26/2015.
 */
public class StringUtil {

    public static String convertVNString(String str) {
        Map<String, String> characterMap = new HashMap<>();
        characterMap.put("a", "á|à|ả|ã|ạ|ă|ắ|ặ|ằ|ẳ|ẵ|â|ấ|ầ|ẩ|ẫ|ậ");
        characterMap.put("d", "đ");
        characterMap.put("e", "é|è|ẻ|ẽ|ẹ|ê|ế|ề|ể|ễ|ệ");
        characterMap.put("i", "í|ì|ỉ|ĩ|ị");
        characterMap.put("o", "ó|ò|ỏ|õ|ọ|ô|ố|ồ|ổ|ỗ|ộ|ơ|ớ|ờ|ở|ỡ|ợ");
        characterMap.put("u", "ú|ù|ủ|ũ|ụ|ư|ứ|ừ|ử|ữ|ự");
        characterMap.put("y", "ý|ỳ|ỷ|ỹ|ỵ");

        for (String mapKey: characterMap.keySet()) {
            // Regex to replace
            String regexToReplace = characterMap.get(mapKey);
            str = str.replaceAll(regexToReplace, mapKey);
            // Repeat with uppercase value
            regexToReplace = regexToReplace.toUpperCase();
            str = str.replaceAll(regexToReplace, mapKey.toUpperCase());
        }

        return str;
    }
}
