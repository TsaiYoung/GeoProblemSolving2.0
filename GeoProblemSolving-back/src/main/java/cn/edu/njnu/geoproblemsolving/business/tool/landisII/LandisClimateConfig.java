package cn.edu.njnu.geoproblemsolving.business.tool.landisII;

import lombok.Data;
import org.springframework.stereotype.Component;

/**
 * @ClassName LandisClimateConfig
 * @Description Todo
 * @Author zhngzhng
 * @Date 2022/2/28
 **/
@Component
@Data
public class LandisClimateConfig {
    private String climateTimeSeries = "none";
    private String climateFile = "none";
    private String climateFileFormat = "none";
    private String spinUpClimateTimeSeries = "none";
    private String spinUpClimateFile = "none";
    private String spinUpClimateFileFormat = "none";

}
