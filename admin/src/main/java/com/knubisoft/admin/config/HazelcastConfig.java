package com.knubisoft.admin.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.InMemoryFormat;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MergePolicyConfig;
import com.hazelcast.config.TcpIpConfig;
import com.hazelcast.spi.merge.PutIfAbsentMergePolicy;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

@Configuration
public class HazelcastConfig {

    public Config hazelcast() {
        MapConfig eventStoreMap = new MapConfig("spring-boot-admin-event-store")
                .setInMemoryFormat(InMemoryFormat.OBJECT)//хранить данные в памяти в формате Java-объектов, что делает доступ к данным более быстрым и эффективным
                .setBackupCount(1)//создает копию данных в другом узле кластера, чтобы обеспечить отказоустойчивость и сохранность данных
                .setEvictionConfig(new EvictionConfig().setEvictionPolicy(EvictionPolicy.NONE))
                // отключает политику вытеснения для удаления старых и неиспользуемых данных из памяти
                .setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));
        //запись, которая пытается выполнить операцию "put", будет сохранена, если она еще не существует в карте

        MapConfig setNotificationsMap = new MapConfig("spring-boot-admin-application-store")
                .setInMemoryFormat(InMemoryFormat.OBJECT)
                .setBackupCount(1)
                .setEvictionConfig(new EvictionConfig().setEvictionPolicy(EvictionPolicy.LRU))
                .setMergePolicyConfig(new MergePolicyConfig(PutIfAbsentMergePolicy.class.getName(), 100));

        Config config = new Config();
        config.addMapConfig(eventStoreMap);
        config.addMapConfig(setNotificationsMap);
        config.setProperty("hazelcast.jmx", "true");

        config.getNetworkConfig()
                .getJoin()
                .getMulticastConfig()
                .setEnabled(false);
        TcpIpConfig tcpIpConfig = config.getNetworkConfig()
                .getJoin()
                .getTcpIpConfig();
        tcpIpConfig.setEnabled(true);
        tcpIpConfig.setMembers(Collections.singletonList("127.0.0.1"));
        return config;
    }
}
