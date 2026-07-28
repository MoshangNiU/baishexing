package com.yunlan.dto;

import lombok.Data;

@Data
public class EvaluationDTO {
    private Long orderId;
    private Long serveItemId;
    private String content;
    private String pics;
    private Integer star;
    private Long evaluationId;
    private Integer likeFlag;
}
