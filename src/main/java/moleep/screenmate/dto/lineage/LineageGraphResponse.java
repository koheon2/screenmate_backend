package moleep.screenmate.dto.lineage;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "가계도 그래프 응답")
public class LineageGraphResponse {

    private List<LineageNodeResponse> nodes;
    private List<LineageEdgeResponse> edges;
}

