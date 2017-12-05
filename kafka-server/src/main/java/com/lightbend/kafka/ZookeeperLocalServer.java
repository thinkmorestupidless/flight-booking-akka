package com.lightbend.kafka;

import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.server.ZooKeeperServerMain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Properties;

import static com.opengamma.strata.collect.Unchecked.*;

public class ZookeeperLocalServer {

    private static Logger log = LoggerFactory.getLogger(ZookeeperLocalServer.class);

    ZooKeeperServerMain zooKeeperServer;

    public ZookeeperLocalServer(Properties zkProperties, int port, File dataDir) throws Exception {
        log.info("starting zookeeper on port {} with data in {}", port, dataDir);

        clean(dataDir);

        TestingServer server = new TestingServer(port, dataDir, false);

        new Thread() {
            public void run() {
                try {
                    server.start();
                } catch (Exception e) {
                    log.error("ZooKeeper Failed", e);
                }
            }
        }.start();
    }

    private void clean(File dir) {
        try {
            Path rootPath = Paths.get(dir.getAbsolutePath());

            Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS).sorted(Comparator.reverseOrder()).forEach(path -> wrap(() -> Files.delete(path)));
        } catch (IOException e) {
            log.error("problem cleaning dir", e);
        }
    }
}
