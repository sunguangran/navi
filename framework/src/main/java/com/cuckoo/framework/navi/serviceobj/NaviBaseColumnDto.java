package com.cuckoo.framework.navi.serviceobj;

import java.lang.reflect.Field;

/**
 * 符合传统业务习惯的列式Dto类
 */
public abstract class NaviBaseColumnDto extends AbstractNaviDto implements INaviColumnDto {

    private static final long serialVersionUID = 9052289710527464858L;

    /**
     * 用于转换成扩展型列式Dto
     *
     * @return ExtDto对象
     */
    public NaviColumnExtDto toColumnExtDto() {
        Field[] fields = this.getClass().getDeclaredFields();
        NaviColumnExtDto dto = new NaviColumnExtDto();
        for (Field field : fields) {
            DtoInfo anatation = field.getAnnotation(DtoInfo.class);
            if (anatation != null) {
                String cf = anatation.cf();
                String column = anatation.column();
                if (cf.equals("")) {
                    continue;
                } else {
                    String fieldNm = field.getName();
                    if (column.equals("")) {
                        column = fieldNm;
                    }
                    try {
                        Object value = this.getValue(fieldNm);
                        if (value != null) {
                            dto.add(cf, column, null, value);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        dto.setRowkey(this.getRowkey());
        return dto;
    }
}
