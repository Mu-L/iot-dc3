/*
 * Copyright 2016-present Pnoker All Rights Reserved
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *      https://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.pnoker.center.manager.service;

import io.github.pnoker.common.base.Service;
import io.github.pnoker.common.dto.GroupDto;
import io.github.pnoker.common.model.Group;

/**
 * Group Interface
 *
 * @author pnoker
 * @since 2022.1.0
 */
public interface GroupService extends Service<Group, GroupDto> {
    /**
     * 根据分组 NAME 查询
     *
     * @param name     分组名称
     * @param tenantId 租户ID
     * @return Group
     */
    Group selectByName(String name, String tenantId);

}
