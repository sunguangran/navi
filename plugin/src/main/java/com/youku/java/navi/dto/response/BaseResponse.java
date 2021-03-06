/*
 * Copyright (C) 2014-2020 Youku Group Holding Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package com.youku.java.navi.dto.response;

import com.youku.java.navi.common.NaviError;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaseResponse {

    private int code = NaviError.ACTION_FAILED;
    private String msg;

    public static BaseResponse instance() {
        return new BaseResponse();
    }

    public BaseResponse code(int code) {
        this.code = code;
        return this;
    }

    public int code() {
        return this.code;
    }

    public BaseResponse msg(String msg) {
        this.msg = msg;
        return this;
    }

    public String msg() {
        return this.msg;
    }

    public boolean success() {
        return this.code == NaviError.ACTION_SUCCED;
    }
}
