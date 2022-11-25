package cn.edu.njnu.geoproblemsolving.Entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "ShareTokenController")
@Data
public class ShareTokenEntity {
    @Id
    private String tokenId;
    private String token;
}
