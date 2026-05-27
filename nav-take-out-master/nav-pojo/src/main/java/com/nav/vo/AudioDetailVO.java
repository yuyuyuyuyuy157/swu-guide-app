package com.nav.vo;

import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

@Data
@Builder
public class AudioDetailVO implements Serializable {
    private String audioUrl;
    private Integer duration;
    private String title;
    private Integer lastProgress;
}
