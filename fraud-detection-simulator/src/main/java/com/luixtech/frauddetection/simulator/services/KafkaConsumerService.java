/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.luixtech.frauddetection.simulator.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luixtech.frauddetection.simulator.config.ApplicationProperties;
import com.luixtech.frauddetection.simulator.domain.Rule;
import com.luixtech.frauddetection.simulator.model.RulePayload;
import com.luixtech.frauddetection.simulator.repository.RuleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private static final ObjectMapper          OBJECT_MAPPER = new ObjectMapper();
    private final        SimpMessagingTemplate simpTemplate;
    private final        RuleRepository        ruleRepository;
    private final        ApplicationProperties applicationProperties;

    @KafkaListener(topics = "${application.kafka.topic.alerts}", groupId = "alerts")
    public void templateAlerts(@Payload String message) {
        log.warn("Detected alert {}", message);
        simpTemplate.convertAndSend(applicationProperties.getWebSocket().getTopic().getAlerts(), message);
    }

    @KafkaListener(topics = "${application.kafka.topic.latency}", groupId = "latency")
    public void templateLatency(@Payload String message) {
        log.warn("Found latency {}", message);
        simpTemplate.convertAndSend(applicationProperties.getWebSocket().getTopic().getLatency(), message);
    }

    @KafkaListener(topics = "${application.kafka.topic.current-rules}", groupId = "current-rules")
    public void templateCurrentFlinkRules(@Payload String message) throws IOException {
        log.info("{}", message);
        RulePayload payload = OBJECT_MAPPER.readValue(message, RulePayload.class);
        Integer payloadId = payload.getRuleId();
        Optional<Rule> existingRule = ruleRepository.findById(payloadId);
        if (!existingRule.isPresent()) {
            ruleRepository.save(new Rule(payloadId, OBJECT_MAPPER.writeValueAsString(payload)));
        }
    }
}
