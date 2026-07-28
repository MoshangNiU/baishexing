package com.yunlan.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class LikeDTO {
    @JsonAlias("state")
    private Integer likeFlag;
    private Integer likeTargetType;
    @JsonAlias("id")
    private Long evaluationId;
}
