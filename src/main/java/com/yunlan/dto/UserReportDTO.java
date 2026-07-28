package com.yunlan.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class UserReportDTO {
    @JsonAlias("bizId")
    private Long evaluationId;
    @JsonAlias("reason")
    private String reportReason;
    private Long serveItemId;
    private Integer bizType;
    private Integer targetTypeId;
}
