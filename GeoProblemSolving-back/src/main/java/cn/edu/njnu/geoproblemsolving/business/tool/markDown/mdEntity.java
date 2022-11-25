package cn.edu.njnu.geoproblemsolving.business.tool.markDown;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class mdEntity {

    private String txt;
    private String html;
}
