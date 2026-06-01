package com.nav.service.impl;

import com.nav.dto.CurrentLocationDTO;
import com.nav.service.ScenicSpotService;
import com.nav.vo.ScenicSpotLocationVO;
import com.nav.vo.ScenicSpotVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class ScenicSpotServiceImpl implements ScenicSpotService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Method SET_LATITUDE_METHOD = resolveCoordinateSetter("setLatitude");
    private static final Method SET_LONGITUDE_METHOD = resolveCoordinateSetter("setLongitude");

    private static Method resolveCoordinateSetter(String methodName) {
        try {
            return ScenicSpotVO.class.getMethod(methodName, Double.class);
        } catch (NoSuchMethodException ex) {
            return null;
        }
    }

    @Override
    public ScenicSpotVO getCurrentScenic(CurrentLocationDTO currentLocationDTO) {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id, name, description, image_url, audio_url, latitude, longitude, radius, " +
                            "ST_Distance_Sphere(location, ST_GeomFromText(CONCAT('POINT(', ?, ' ', ?, ')'), 4326)) AS distance " +
                            "FROM scenic_spots " +
                            "WHERE is_deleted = 0 " +
                            "AND ST_Distance_Sphere(location, ST_GeomFromText(CONCAT('POINT(', ?, ' ', ?, ')'), 4326)) <= GREATEST(radius, 150) " +
                            "ORDER BY distance ASC LIMIT 1",
                    currentLocationDTO.getLatitude(), currentLocationDTO.getLongitude(),
                    currentLocationDTO.getLatitude(), currentLocationDTO.getLongitude()
            );
            if (rows.isEmpty()) {
                return null;
            }
            ScenicSpotVO vo = toVO(rows.get(0));
            vo.setDistance(String.valueOf(Math.round(asDouble(rows.get(0).get("distance"), 0.0))));
            return vo;
        } catch (Exception e) {
            log.error("Current scenic query failed: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public ScenicSpotLocationVO getScenicLocation(String scenicId) {
        Long id = asLong(scenicId);
        if (id == null) {
            return null;
        }

        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id, name, latitude, longitude FROM scenic_spots WHERE id = ? AND is_deleted = 0 LIMIT 1",
                    id
            );
            if (rows.isEmpty()) {
                return null;
            }
            Map<String, Object> row = rows.get(0);
            ScenicSpotLocationVO vo = new ScenicSpotLocationVO();
            vo.setScenicId(String.valueOf(row.get("id")));
            vo.setName(asString(row.get("name")));
            vo.setLatitude(asDoubleObj(row.get("latitude")));
            vo.setLongitude(asDoubleObj(row.get("longitude")));
            return vo;
        } catch (Exception e) {
            log.error("Get scenic location failed for scenicId={}: {}", scenicId, e.getMessage());
            return null;
        }
    }

    @Override
    public List<ScenicSpotVO> listAllScenicSpots() {
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "SELECT id, name, description, image_url, audio_url, latitude, longitude, radius, updated_at " +
                            "FROM scenic_spots WHERE is_deleted = 0 ORDER BY id DESC"
            );
            return toVOList(rows);
        } catch (Exception e) {
            log.error("List scenic spots failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<ScenicSpotVO> searchScenicSpots(String keyword) {
        String raw = keyword == null ? "" : keyword.trim();
        Set<String> variants = keywordVariants(raw);
        Set<Long> seen = new LinkedHashSet<>();
        List<ScenicSpotVO> merged = new ArrayList<>();

        for (String variant : variants) {
            try {
                List<Map<String, Object>> rows;
                if (variant.isBlank()) {
                    rows = jdbcTemplate.queryForList(
                            "SELECT id, name, description, image_url, audio_url, latitude, longitude, radius, updated_at " +
                                    "FROM scenic_spots WHERE is_deleted = 0 ORDER BY id DESC"
                    );
                } else {
                    rows = jdbcTemplate.queryForList(
                            "SELECT id, name, description, image_url, audio_url, latitude, longitude, radius, updated_at " +
                                    "FROM scenic_spots WHERE is_deleted = 0 AND (name LIKE CONCAT('%', ?, '%') OR description LIKE CONCAT('%', ?, '%')) " +
                                    "ORDER BY CASE " +
                                    "WHEN name = ? THEN 0 " +
                                    "WHEN name LIKE CONCAT(?, '%') THEN 1 " +
                                    "WHEN name LIKE CONCAT('%', ?, '%') THEN 2 " +
                                    "ELSE 3 END, id DESC",
                            variant, variant, variant, variant, variant
                    );
                }

                for (ScenicSpotVO vo : toVOList(rows)) {
                    Long id = asLong(vo.getScenicId());
                    if (id != null && seen.add(id)) {
                        merged.add(vo);
                    }
                }
            } catch (Exception e) {
                log.warn("Search variant failed: {}, reason: {}", variant, e.getMessage());
            }
        }

        return merged;
    }

    private List<ScenicSpotVO> toVOList(List<Map<String, Object>> rows) {
        List<ScenicSpotVO> list = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            list.add(toVO(row));
        }
        return list;
    }

    private ScenicSpotVO toVO(Map<String, Object> row) {
        ScenicSpotVO vo = new ScenicSpotVO();
        String id = String.valueOf(row.get("id"));
        vo.setScenicId(id);
        vo.setName(asString(row.get("name")));
        vo.setImage(asString(row.get("image_url")));
        vo.setIntro(asString(row.get("description")));
        vo.setInductionRange(asInt(row.get("radius"), 60));
        setOptionalCoordinate(vo, SET_LATITUDE_METHOD, asDoubleObj(row.get("latitude")));
        setOptionalCoordinate(vo, SET_LONGITUDE_METHOD, asDoubleObj(row.get("longitude")));
        vo.setDistance("0");

        String audioUrl = asString(row.get("audio_url"));
        String intro = asString(row.get("description"));
        boolean hasAudio = (audioUrl != null && !audioUrl.isBlank()) || (intro != null && !intro.isBlank());
        vo.setHasAudio(hasAudio);
        vo.setAudioId(hasAudio ? id : null);
        return vo;
    }

    private Set<String> keywordVariants(String keyword) {
        Set<String> variants = new LinkedHashSet<>();
        if (keyword == null || keyword.isBlank()) {
            variants.add("");
            return variants;
        }

        String compact = keyword.replaceAll("\\s+", "");
        String normalized = compact
                .replace("号教学楼", "教")
                .replace("教学楼", "教")
                .replace("号楼", "教");

        variants.add(keyword.trim());
        variants.add(compact);
        variants.add(normalized);

        if (normalized.matches("^\\d+$")) {
            variants.add(normalized + "教");
            variants.add(normalized + "号楼");
            variants.add(normalized + "教学楼");
        }

        return variants;
    }

    private void setOptionalCoordinate(ScenicSpotVO vo, Method setter, Double value) {
        if (setter == null || value == null) {
            return;
        }
        try {
            setter.invoke(vo, value);
        } catch (Exception ignored) {
            // Backward compatibility: old ScenicSpotVO in local Maven cache may not contain coordinate setters.
        }
    }

    private String asString(Object value) {
        return value == null ? "" : value.toString();
    }

    private int asInt(Object value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        return defaultValue;
    }

    private double asDouble(Object value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        return defaultValue;
    }

    private Double asDoubleObj(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal decimal) {
            return decimal.doubleValue();
        }
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Long asLong(String text) {
        try {
            return Long.parseLong(text);
        } catch (Exception e) {
            return null;
        }
    }
}
