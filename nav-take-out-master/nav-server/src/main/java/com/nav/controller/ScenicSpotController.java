package com.nav.controller;

import com.nav.dto.CurrentLocationDTO;
import com.nav.result.Result;
import com.nav.service.ScenicSpotService;
import com.nav.vo.ScenicSpotLocationVO;
import com.nav.vo.ScenicSpotVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/scenic")
@Tag(name = "Scenic APIs")
public class ScenicSpotController {

    @Autowired
    private ScenicSpotService scenicSpotService;

    @PostMapping("/current")
    @Operation(summary = "Get current scenic spot")
    public Result<ScenicSpotVO> getCurrentScenic(@RequestBody CurrentLocationDTO currentLocationDTO) {
        log.info("Current location request: {}", currentLocationDTO);

        if (currentLocationDTO.getLatitude() == null || currentLocationDTO.getLongitude() == null) {
            return Result.error(400, "Latitude and longitude are required");
        }

        ScenicSpotVO scenicSpotVO = scenicSpotService.getCurrentScenic(currentLocationDTO);
        return Result.success(scenicSpotVO);
    }

    @GetMapping("/list")
    @Operation(summary = "List scenic spots")
    public Result<List<ScenicSpotVO>> listAllScenicSpots() {
        log.info("List all scenic spots");
        List<ScenicSpotVO> list = scenicSpotService.listAllScenicSpots();
        return Result.success(list);
    }

    @GetMapping("/search")
    @Operation(summary = "Search scenic spots")
    public Result<List<ScenicSpotVO>> search(@RequestParam(required = false) String keyword) {
        log.info("Search scenic spots, keyword: {}", keyword);
        List<ScenicSpotVO> list = scenicSpotService.searchScenicSpots(keyword);
        return Result.success(list);
    }

    @GetMapping("/location/{scenicId}")
    @Operation(summary = "Get scenic spot location by scenicId")
    public Result<ScenicSpotLocationVO> getScenicLocation(@PathVariable String scenicId) {
        log.info("Get scenic location, scenicId: {}", scenicId);
        ScenicSpotLocationVO vo = scenicSpotService.getScenicLocation(scenicId);
        if (vo == null) {
            return Result.error(404, "Scenic spot not found");
        }
        return Result.success(vo);
    }
}
