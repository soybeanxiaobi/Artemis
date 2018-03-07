package mobile.ga.hik.com.apigateway;


import com.hikvision.artemis.sdk.ArtemisHttpUtil;
import com.hikvision.artemis.sdk.config.ArtemisConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangkuilin on 2017/9/14.
 */

public class HttpClient {
    private static String ARTEMIS_PATH = "/artemis";

    static {
        init();
    }

    public static void init() {
        ArtemisConfig.host = "";
        ArtemisConfig.appKey = "";
        ArtemisConfig.appSecret = "";
    }

    public static String doGet(final String strUrl, Map<String, String> querys) {
        Map<String, String> path = new HashMap<String, String>(2) {
            {
                put("https://", strUrl);
            }
        };

        String str = ArtemisHttpUtil.doGetArtemis(path, querys, null, null);
        return str;
    }

    public static String doPost(Map<String, String> path, Map<String, String> paramMap) {
        return ArtemisHttpUtil.doPostFormArtemis(path, paramMap,null,null,null);
    }
}
