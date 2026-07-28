package com.yunlan.dto;

import lombok.Data;
import java.util.List;

@Data
public class EvaluationVO {
    private Long id;
    private Integer totalScore;
    private String content;
    private List<String> pictureArray;
    private Boolean isLiked;
    private String createTime;
    private EvaluatorInfo evaluatorInfo;
    private Statistics statistics;

    @Data
    public static class EvaluatorInfo {
        private String avatar;
        private String nickName;
        private Integer isAnonymous;
    }

    @Data
    public static class Statistics {
        private Integer likeNumber;
    }
}
