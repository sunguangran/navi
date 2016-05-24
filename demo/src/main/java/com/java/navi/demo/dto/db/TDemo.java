package com.java.navi.demo.dto.db;

import com.youku.java.navi.common.Resp;
import com.youku.java.navi.server.serviceobj.AbstractNaviDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * demo dto,表主键为Long类型
 *
 * @author sgran<sunguangran@youku.com>
 * @since 16/5/23
 */
@Getter
@Setter
@Document(collection = "t_demo")
@CompoundIndexes({
    @CompoundIndex(name = "ccid_1", def = "{'ccid':1}")}
)
public class TDemo extends AbstractNaviDto {

    @Id
    @Resp
    private Long id;

    @Resp("name")
    private String na;

    @Resp("customer_id")
    private String ccid;

}
