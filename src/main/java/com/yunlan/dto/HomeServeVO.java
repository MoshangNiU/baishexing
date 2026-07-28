package com.yunlan.dto;

import lombok.Data;
import java.util.List;

@Data
public class HomeServeVO {
    private Long serveTypeId;
    private String serveTypeIcon;
    private String serveTypeName;
    private List<ServeResDTO> serveResDTOList;
}
