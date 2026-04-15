package com.tad.www.api.analysis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisDraftUpdateRequest {

    private String winner;
    private AnalysisDraftTeamUpdateRequest team1;
    private AnalysisDraftTeamUpdateRequest team2;
}
