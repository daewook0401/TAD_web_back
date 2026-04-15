package com.tad.www.api.analysis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisPlayerPayload {

    private String name;
    private Integer kills;
    private Integer deaths;
    private Integer assists;
    private Integer cs;
    private Integer gold;
}
