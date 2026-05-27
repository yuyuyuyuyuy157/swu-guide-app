package com.nav.utils;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.nav.properties.BaiduMapProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class BaiduMapUtil {

    @Autowired
    private BaiduMapProperties baiduMapProperties;

    // 百度地图逆地理编码官方标准标准的请求网关地址
    private static final String REVERSE_GEOCODING_URL = "https://api.map.baidu.com/reverse_geocoding/v3/";

    /**
     * 🌍 单一核心功能：根据经纬度反查真实地理位置描述（逆地理编码）
     * @param lat 纬度
     * @param lng 经度
     * @return 格式化后的详细地址文字（如：西南大学第一运动场附近）
     */
    public String getAddressByCoordinate(double lat, double lng) {
        log.info("📡 正在向百度地图开放平台发起逆地理编码请求，坐标: [{}, {}]", lng, lat);

        // 1. 组装百度地图官方规定的标准 Query 参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("ak", baiduMapProperties.getAk());
        paramMap.put("output", "json");
        // 百度地图要求路径格式为：纬度,经度 (lat,lng)
        paramMap.put("location", lat + "," + lng);
        paramMap.put("extensions_poi", "1"); // 召回周边的POI（兴趣点），让定位描述更精准

        try {
            // 2. 🎯 利用 Hutool 的 HttpUtil 极其优雅地发送 GET 请求
            String jsonResult = HttpUtil.get(REVERSE_GEOCODING_URL, paramMap);
            log.info("📩 百度地图原始响应报文: {}", jsonResult);

            // 3. 🎯 利用 Fastjson2 高速解析复杂的树状 JSON 报文
            JSONObject jsonObject = JSON.parseObject(jsonResult);

            // 百度地图标准状态码：0 代表成功
            Integer status = jsonObject.getInteger("status");
            if (status != null && status == 0) {
                JSONObject result = jsonObject.getJSONObject("result");

                // 我们可以直接拿：formatted_address（标准格式化结构地址）
                String formattedAddress = result.getString("formatted_address");

                // 也可以更进一步，拿更具人情味的 sematic_description（当前位置的语义化描述，如“西南大学内”）
                String semanticDescription = result.getString("sematic_description");

                String finalLocationName = (semanticDescription != null && !semanticDescription.isBlank())
                        ? formattedAddress + " (" + semanticDescription + ")"
                        : formattedAddress;

                log.info("🎯 逆地理编码转换成功。最终解析位置: {}", finalLocationName);
                return finalLocationName;
            } else {
                log.error("❌ 百度地图开放平台接口返回错误，错误状态码(status): {}", status);
                return "未知美丽的未知领域";
            }
        } catch (Exception e) {
            log.error("💥 远程调用百度地图接口发生网络物理故障:", e);
            return "定位信号微弱";
        }
    }
}