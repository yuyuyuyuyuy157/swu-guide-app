package com.nav.controller.admin;

import com.nav.dto.ScenicSpotDTO;
import com.nav.dto.ScenicSpotPageQueryDTO;
import com.nav.result.PageResult;
import com.nav.result.Result;
import com.nav.service.AdminScenicSpotService;
import com.nav.vo.ScenicSpotVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/scenic")
@Slf4j
public class AdminScenicSpotController {

    @Autowired
    private AdminScenicSpotService adminScenicSpotService;

    @Value("${nav.upload.base-dir:D:/nav-uploads}")
    private String uploadBaseDir;

    /**
     * 5.2 获取景点列表（管理端分页搜索）
     */
    @GetMapping("/list")
    public Result<PageResult> pageQuery(ScenicSpotPageQueryDTO pageQueryDTO) {
        log.info("管理员查询景点列表: {}", pageQueryDTO);
        PageResult pageResult = adminScenicSpotService.pageQuery(pageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 新增景点
     */
    @PostMapping
    public Result<String> save(@RequestBody ScenicSpotDTO scenicSpotDTO) {
        log.info("管理员新增景点: {}", scenicSpotDTO);
        adminScenicSpotService.saveWithFields(scenicSpotDTO);
        return Result.success("新增成功");
    }

    @PostMapping("/upload-image")
    public Result<Map<String, String>> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "图片文件不能为空");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return Result.error(400, "仅支持图片文件");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(originalFilename);
        String safeExt = ext == null || ext.isBlank() ? "jpg" : ext.toLowerCase();
        String datePath = LocalDate.now().toString().replace("-", "/");
        String filename = UUID.randomUUID() + "." + safeExt;
        Path dir = Path.of(uploadBaseDir, "scenic", datePath);
        Files.createDirectories(dir);
        Path target = dir.resolve(filename);
        file.transferTo(target);

        String url = "/download/scenic/" + datePath + "/" + filename;
        Map<String, String> data = new HashMap<>();
        data.put("url", url);
        return Result.success(data);
    }

    /**
     * 5.3 获取景点详情（用于回显编辑）
     */
    @GetMapping("/detail/{id}")
    public Result<ScenicSpotDTO> getById(@PathVariable Long id) {
        log.info("管理员获取景点详情，ID: {}", id);
        ScenicSpotDTO dto = adminScenicSpotService.getById(id);
        return Result.success(dto);
    }

    /**
     * 修改景点
     */
    @PutMapping
    public Result<String> update(@RequestBody ScenicSpotDTO scenicSpotDTO) {
        log.info("管理员修改景点: {}", scenicSpotDTO);
        adminScenicSpotService.updateWithFields(scenicSpotDTO);
        return Result.success("修改成功");
    }

    /**
     * 删除景点（单个/批量）
     */
    @DeleteMapping
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("管理员批量删除景点，IDs: {}", ids);
        adminScenicSpotService.deleteBatch(ids);
        return Result.success("删除成功");
    }
}
