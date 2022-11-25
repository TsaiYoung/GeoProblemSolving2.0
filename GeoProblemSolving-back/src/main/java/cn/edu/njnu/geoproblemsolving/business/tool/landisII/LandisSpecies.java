package cn.edu.njnu.geoproblemsolving.business.tool.landisII;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @ClassName Species
 * @Description Landiss-species
 * @Author zhngzhng
 * @Date 2022/2/28
 **/
@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LandisSpecies {
    private String name = "none";
    private String longevity = "none";
    private String maturity = "none";
    private String s_tolerance = "none";
    private String f_tolerance = "none";
    private String e_seeding = "none";
    private String m_seeding = "none";
    private String reproduction = "none";
    private String min_resprouting = "none";
    private String max_resprouting = "none";
    private String post_fire = "none";
}
