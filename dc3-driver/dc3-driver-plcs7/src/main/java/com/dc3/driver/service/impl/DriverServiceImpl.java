/*
 * Copyright 2019 Pnoker. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dc3.driver.service.impl;

import com.dc3.common.exception.ServiceException;
import com.dc3.common.model.Device;
import com.dc3.common.model.Point;
import com.dc3.common.sdk.bean.AttributeInfo;
import com.dc3.common.sdk.bean.DriverContext;
import com.dc3.common.sdk.service.DriverService;
import com.dc3.common.sdk.service.rabbit.PointValueService;
import com.dc3.driver.bean.Plcs7PointVariable;
import com.github.s7connector.api.S7Connector;
import com.github.s7connector.api.S7Serializer;
import com.github.s7connector.api.factory.S7ConnectorFactory;
import com.github.s7connector.api.factory.S7SerializerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dc3.common.sdk.util.DriverUtils.attribute;

/**
 * @author pnoker
 */
@Slf4j
@Service
public class DriverServiceImpl implements DriverService {
    @Resource
    private PointValueService pointValueService;
    @Resource
    private DriverContext driverContext;

    /**
     * Plc Connector Map
     */
    private volatile Map<Long, S7Connector> s7ConnectorMap;

    @Override
    public void initial() {
        s7ConnectorMap = new ConcurrentHashMap<>(16);
    }

    @Override
    public String read(Map<String, AttributeInfo> driverInfo, Map<String, AttributeInfo> pointInfo, Device device, Point point) {
        S7Serializer serializer = getS7Serializer(device.getId(), driverInfo);
        Plcs7PointVariable plcs7PointVariable = getPointVariable(pointInfo);
        return String.valueOf(serializer.dispense(plcs7PointVariable));
    }

    @Override
    public Boolean write(Map<String, AttributeInfo> driverInfo, Map<String, AttributeInfo> pointInfo, Device device, AttributeInfo value) {
        return false;
    }

    @Override
    public void schedule() {

    }

    /**
     * 获取 plcs7 serializer
     * 先从缓存中取，没有就新建
     *
     * @param deviceId
     * @param driverInfo
     * @return
     */
    private S7Serializer getS7Serializer(Long deviceId, Map<String, AttributeInfo> driverInfo) {
        S7Connector s7Connector = s7ConnectorMap.get(deviceId);
        if (null == s7Connector) {
            String host = attribute(driverInfo, "host");
            Integer port = attribute(driverInfo, "port");
            log.debug("connectInfo: host:{},port:{}", host, port);
            try {
                s7Connector = S7ConnectorFactory.buildTCPConnector().withHost(host).withPort(port).build();
            } catch (Exception e) {
                throw new ServiceException("new s7connector fail" + e.getMessage());
            }
        }
        if (null != s7Connector) {
            s7ConnectorMap.put(deviceId, s7Connector);
            return S7SerializerFactory.buildSerializer(s7Connector);
        }
        throw new ServiceException("new s7connector fail");
    }

    /**
     * 获取位号变量信息
     *
     * @param pointInfo
     * @return
     */
    private Plcs7PointVariable getPointVariable(Map<String, AttributeInfo> pointInfo) {
        int dbNum = attribute(pointInfo, "dbNum");
        int byteOffset = attribute(pointInfo, "byteOffset");
        int bitOffset = attribute(pointInfo, "bitOffset");
        int blockSize = attribute(pointInfo, "blockSize");
        String type = attribute(pointInfo, "type");
        log.debug("pointVariable: dbNum:{},byteOffset:{},bitOffset:{},blockSize:{},type:{}", dbNum, byteOffset, bitOffset, blockSize, type);
        return new Plcs7PointVariable(dbNum, byteOffset, bitOffset, blockSize, type);
    }

}