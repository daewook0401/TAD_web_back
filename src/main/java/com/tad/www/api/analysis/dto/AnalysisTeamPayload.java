package com.tad.www.api.analysis.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnalysisTeamPayload {

    private List<AnalysisPlayerPayload> players;
}
