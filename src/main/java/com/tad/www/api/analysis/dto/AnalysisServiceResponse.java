package com.tad.www.api.analysis.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisServiceResponse {

    private String winner;
    private AnalysisTeamPayload team1;
    private AnalysisTeamPayload team2;
}
