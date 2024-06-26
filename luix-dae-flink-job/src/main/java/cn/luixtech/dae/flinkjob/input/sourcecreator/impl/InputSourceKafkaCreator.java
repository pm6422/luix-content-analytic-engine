package cn.luixtech.dae.flinkjob.input.sourcecreator.impl;

import cn.luixtech.dae.flinkjob.core.Arguments;
import cn.luixtech.dae.flinkjob.input.sourcecreator.SourceCreator;
import com.luixtech.utilities.serviceloader.annotation.SpiName;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.connector.kafka.source.KafkaSource;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import static cn.luixtech.dae.flinkjob.utils.KafkaUtils.createKafkaSource;

@SpiName("input-" + Arguments.CHANNEL_KAFKA)
public class InputSourceKafkaCreator implements SourceCreator {
    @Override
    public DataStreamSource<String> create(StreamExecutionEnvironment env, Arguments arguments) {
        KafkaSource<String> kafkaSource = createKafkaSource(arguments, arguments.inputTopic, arguments.inputTopicGroup);

        // NOTE: Idiomatically, watermarks should be assigned here, but this done later
        // because of the mix of the new Source (Kafka) and SourceFunction-based interfaces.
        // TODO: refactor when FLIP-238 is added
        return env.fromSource(kafkaSource, WatermarkStrategy.noWatermarks(), arguments.messageChannel);
    }
}
