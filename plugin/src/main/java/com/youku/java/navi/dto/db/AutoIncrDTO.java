package com.youku.java.navi.dto.db;

import com.youku.java.navi.server.serviceobj.AbstractNaviDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author sgran<sunguangran@youku.com>
 * @since 2015/5/12
 */
@Setter
@Getter
@Document(collection = "t_autoincr_id")
public class AutoIncrDTO extends AbstractNaviDto {

    private static final long serialVersionUID = -362648082764636851L;
    @Id
    private String _id;
    private long idv;

    public AutoIncrDTO() {
        super();
    }

    public AutoIncrDTO(String id, int idv) {
        this._id = id;
        this.idv = idv;
    }

    @Override
    public Long getId() {
        return null;
    }

    @Override
    public void setId(Long id) {

    }

}
