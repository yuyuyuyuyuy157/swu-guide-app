package com.nav.controller;

import com.nav.dto.CurrentLocationDTO;
import com.nav.result.Result;
import com.nav.service.ScenicSpotService;
import com.nav.vo.ScenicSpotVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
@RestController
@RequestMapping("/api/v1/scenic")
@Slf4j
@Tag(name = "景点模块接口")
public class ScenicSpotController {

    @Autowired
    private ScenicSpotService scenicSpotService;

    // 获取当前位置景点
    @PostMapping("/current")
    @Operation(summary = "获取当前位置景点信息")
    public Result<ScenicSpotVO> getCurrentScenic(@RequestBody CurrentLocationDTO currentLocationDTO) {
        log.info("接收到位置上报请求: {}", currentLocationDTO);

        // 健壮性校验
        if (currentLocationDTO.getLatitude() == null || currentLocationDTO.getLongitude() == null) {
            return Result.error(400, "经纬度参数不能为空");
        }

        ScenicSpotVO scenicSpotVO = scenicSpotService.getCurrentScenic(currentLocationDTO);
        return Result.success(scenicSpotVO);
    }

    @GetMapping("/list")
    @Operation(summary = "获取全部景点列表信息")
    public Result<List<ScenicSpotVO>> listAllScenicSpots() {
        log.info("用户端初始化，触发全量景点列表查询");
        List<ScenicSpotVO> list = scenicSpotService.listAllScenicSpots();
        return Result.success(list);
    }
}