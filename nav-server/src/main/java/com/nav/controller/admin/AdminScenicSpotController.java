package com.nav.controller.admin;

import com.nav.dto.ScenicSpotDTO;
import com.nav.dto.ScenicSpotPageQueryDTO;
import com.nav.result.PageResult;
import com.nav.result.Result;
import com.nav.service.AdminScenicSpotService;
import com.nav.vo.ScenicSpotVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
@RestController
@RequestMapping("/api/v1/admin/scenic")
@Slf4j
@Tag(name = "管理端-景点与路线规划接口")
public class AdminScenicSpotController {

    @Autowired
    private AdminScenicSpotService adminScenicSpotService;

    @PostMapping
    @Operation(summary = "管理端新增景点")
    public Result<String> save(@RequestBody ScenicSpotDTO scenicSpotDTO) {
        log.info("管理员新增景点: {}", scenicSpotDTO);
        adminScenicSpotService.saveWithFields(scenicSpotDTO);
        return Result.success("新增成功");
    }


    @GetMapping("/detail/{id}")
    @Operation(summary = "管理端获取景点详情（用于回显编辑）")
    public Result<ScenicSpotDTO> getById(@PathVariable Long id) {
        log.info("管理员获取景点详情，ID: {}", id);
        ScenicSpotDTO dto = adminScenicSpotService.getById(id);
        return Result.success(dto);
    }

    @PutMapping
    @Operation(summary = "管理端修改景点")
    public Result<String> update(@RequestBody ScenicSpotDTO scenicSpotDTO) {
        log.info("管理员修改景点: {}", scenicSpotDTO);
        adminScenicSpotService.updateWithFields(scenicSpotDTO);
        return Result.success("修改成功");
    }

    @DeleteMapping
    @Operation(summary = "管理端删除景点（单个/批量）")
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("管理员批量删除景点，IDs: {}", ids);
        adminScenicSpotService.deleteBatch(ids);
        return Result.success("删除成功");
    }

    @GetMapping("/list")
    @Operation(summary = "管理端景点分页搜索列表")
    public Result<PageResult> pageQuery(ScenicSpotPageQueryDTO pageQueryDTO) {
        log.info("管理员工作台触发条件检索，参数: {}", pageQueryDTO);

        // 1. 黄金边界健壮性兜底防御
        if (pageQueryDTO.getPage() == null || pageQueryDTO.getPage() < 1) {
            pageQueryDTO.setPage(1);
        }
        if (pageQueryDTO.getPageSize() == null || pageQueryDTO.getPageSize() > 50) {
            pageQueryDTO.setPageSize(10); // 严格对齐管理端文档：每页条数最大 50
        }

        // 2. 推进至管理端专用 Service 链路
        PageResult pageResult = adminScenicSpotService.pageQuery(pageQueryDTO);
        return Result.success(pageResult);
    }
}