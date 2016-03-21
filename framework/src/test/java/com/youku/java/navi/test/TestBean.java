package com.youku.java.navi.test;

import com.youku.java.navi.server.serviceobj.AbstractNaviDto;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

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
    private ObjectId id;

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

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public String getOId() {
        return String.valueOf(getUid());
    }

    @Override
    public void setOId(Long id) {

    }

    public String getUsernm() {
        return usernm;
    }

    public void setUsernm(String usernm) {
        this.usernm = usernm;
    }


}
