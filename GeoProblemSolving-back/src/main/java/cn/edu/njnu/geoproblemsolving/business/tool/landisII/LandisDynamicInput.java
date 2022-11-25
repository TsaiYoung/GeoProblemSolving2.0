package cn.edu.njnu.geoproblemsolving.business.tool.landisII;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName LandisDynamicInput
 * @Description Dynamic -inputs
 * @Author zhngzhng
 * @Date 2022/3/2
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LandisDynamicInput {
    private String year;
    private String ecoregion;
    private String species;
    private String probEst;
    private String maxANPP;
    private String maxB;
}
