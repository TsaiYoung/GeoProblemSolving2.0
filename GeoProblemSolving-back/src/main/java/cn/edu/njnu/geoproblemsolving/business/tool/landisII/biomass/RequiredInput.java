package cn.edu.njnu.geoproblemsolving.business.tool.landisII.biomass;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @ClassName RequiredInput
 * @Description Todo
 * @Author zhngzhng
 * @Date 2022/2/28
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RequiredInput {
    private String timeStep;
    private String seedingAlgorithm = "WardSeedDispersal";
    private String initialCommunities;
    private String initialCommunitiesMap;
    private String climateConfigFile;
    //  yes/on
    private String calibrateMode;
    private String spinUpMortalityFraction;
}
