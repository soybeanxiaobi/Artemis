package mobile.ga.hik.com.apigateway;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wangkuilin on 2017/9/14.
 */

public class ApiGate {

    private static ApiGate instance = null;
    private final String ARTEMIS_PATH = "/artemis";

    public static ApiGate getInstance() {
        if (null == instance) {
            instance = new ApiGate();
        }

        return instance;
    }


    /**
     * 根据父组织编号查询组织树
     *
     * @return
     */
    public String findControlUnitByUnitCode(String strUnitCode) {
        if (null == strUnitCode || "".equals(strUnitCode)) {
            return "";
        }

        String url = ARTEMIS_PATH + "/api/common/v1/remoteControlUnitRestService/findControlUnitByUnitCode";

        Map<String, String> querys = new HashMap<String, String>();
        querys.put("unitCode", strUnitCode);

        return HttpClient.doGet(url, querys);
    }


}
