package datameshmanager.hive;

import datameshmanager.sdk.DataMeshManagerAssetsSynchronizer;
import datameshmanager.sdk.DataMeshManagerClient;
import datameshmanager.sdk.DataMeshManagerStateRepositoryRemote;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "datameshmanager")
@ConfigurationPropertiesScan("datameshmanager")
@EnableScheduling
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public DataMeshManagerClient dataMeshManagerClient(
            @Value("${datameshmanager.client.host}") String host,
            @Value("${datameshmanager.client.apikey}") String apiKey) {
        return new DataMeshManagerClient(host, apiKey);
    }

    @Bean(destroyMethod = "stop")
    @ConditionalOnProperty(value = "datameshmanager.client.hive.assets.enabled", havingValue = "true")
    public DataMeshManagerAssetsSynchronizer dataMeshManagerAssetsSynchronizer(
            HiveProperties hiveProperties,
            DataMeshManagerClient client,
            TaskExecutor taskExecutor) {
        try {
            var connectorId = hiveProperties.assets().connectorid();
            var stateRepository = new DataMeshManagerStateRepositoryRemote(connectorId, client);
            var assetsSupplier = new HiveAssetsSupplier(hiveProperties, stateRepository);
            var dataMeshManagerAssetsSynchronizer = new DataMeshManagerAssetsSynchronizer(connectorId, client, assetsSupplier);
            if (hiveProperties.assets().pollinterval() != null) {
                dataMeshManagerAssetsSynchronizer.setDelay(hiveProperties.assets().pollinterval());
            }

            taskExecutor.execute(dataMeshManagerAssetsSynchronizer::start);
            return dataMeshManagerAssetsSynchronizer;
        } catch (Exception e) {
            return new DataMeshManagerAssetsSynchronizer("test-connector", client, null);
        }
    }

    @Bean
    public SimpleAsyncTaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
}