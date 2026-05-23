package com.nav.controller;

import com.nav.dto.CurrentLocationDTO;
import com.nav.result.Result;
import com.nav.service.ScenicSpotService;
import com.nav.vo.ScenicSpotVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/scenic")
@Slf4j
public class ScenicSpotController {

    @Autowired
    private ScenicSpotService scenicSpotService;

    /**
     * 获取当前位置景点信息
     * @param currentLocationDTO 经纬度入参
     * @return 统一返回结果 Result<ScenicSpotVO>
     */
    @PostMapping("/current")
    public Result<ScenicSpotVO> getCurrentScenic(@RequestBody CurrentLocationDTO currentLocationDTO) {
        log.info("接收到位置上报请求: {}", currentLocationDTO);

        // 健壮性校验
        if (currentLocationDTO.getLatitude() == null || currentLocationDTO.getLongitude() == null) {
            return Result.error(400, "经纬度参数不能为空");
        }

        ScenicSpotVO scenicSpotVO = scenicSpotService.getCurrentScenic(currentLocationDTO);
        return Result.success(scenicSpotVO);
    }
}