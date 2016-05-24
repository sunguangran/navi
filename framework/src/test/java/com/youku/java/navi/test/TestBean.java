package com.youku.java.navi.test;

import com.youku.java.navi.server.serviceobj.AbstractNaviDto;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Setter
@Getter
@Document(collection = "tb_test")
@CompoundIndexes({
    @CompoundIndex(name = "uid_index", def = "{'uid':1}", unique = true)
})
public class TestBean extends AbstractNaviDto {

    /**
     *
     */
    private static final long serialVersionUID = 9045140442436627687L;

    @Id
    private Long id;

    private String uid;

    private String usernm;

    public TestBean() {
        super();
    }

    public TestBean(String uid, String usernm) {
        this.uid = uid;
        this.usernm = usernm;
    }

    public TestBean(String uid) {
        this.uid = uid;
    }

}
