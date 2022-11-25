package cn.edu.njnu.geoproblemsolving.business.tool.landisII;

import cn.edu.njnu.geoproblemsolving.business.tool.landisII.biomass.LifeParameter;
import cn.edu.njnu.geoproblemsolving.business.tool.landisII.biomass.RequiredInput;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName LandisBiomassSuccession
 * @Description Landis-succession
 * @Author zhngzhng
 * @Date 2022/2/28
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class LandisBiomassSuccession {
    RequiredInput requiredInput;
    LifeParameter lifeParameter;
}
