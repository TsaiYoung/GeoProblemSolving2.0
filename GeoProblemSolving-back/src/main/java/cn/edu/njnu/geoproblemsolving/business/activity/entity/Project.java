package cn.edu.njnu.geoproblemsolving.business.activity.entity;

import cn.edu.njnu.geoproblemsolving.business.activity.enums.ProjectCategory;
import cn.edu.njnu.geoproblemsolving.business.activity.enums.ProjectPrivacy;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "Projects")
@AllArgsConstructor
public class Project extends Activity{

    /**
     * Privacy control
     */
    private ProjectPrivacy privacy;

    /**
     * tag
     * picture: descriptive picture
     */
    private String tag;
    private String picture;

    /**
     * Project category
     */
    private ProjectCategory category;

    public Project(){
        this.setLevel(0);
    }
}
