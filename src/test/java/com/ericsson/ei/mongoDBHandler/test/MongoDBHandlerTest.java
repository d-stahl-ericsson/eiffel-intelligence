package com.ericsson.ei.mongoDBHandler.test;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.ei.handlers.test.ObjectHandlerTest;
import com.ericsson.ei.mongodbhandler.MongoDBHandler;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodProcess;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.AfterClass;

public class MongoDBHandlerTest {

    static Logger log = (Logger) LoggerFactory.getLogger(MongoDBHandlerTest.class);

    static MongodExecutable mongodExecutable = null;
    static MongoDBHandler mongoDBHandler = null;

    static String host = "localhost";
    static int port = 27017;

    static String dataBaseName = "EventStorageDBbbb";
    static String collectionName = "SampleEvents";
    static String input = "{\"id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase1data\"},{\"event_id\" : \"testcaseid2\", \"test_data\" : \"testcase2data\"}]}";
    String updateInput = "{\"id\":\"eventId\",\"type\":\"eventType11\",\"test_cases\" : [{\"event_id\" : \"testcaseid1\", \"test_data\" : \"testcase2data\"},{\"event_id\" : \"testcaseid3\", \"test_data\" : \"testcase3data\"}]}";
    static String condition = "{\"test_cases.event_id\" : \"testcaseid1\"}";

    public static void setUpEmbeddedMongo() throws Exception {
        System.setProperty("mongodb.port", ""+port);

        MongodStarter starter = MongodStarter.getDefaultInstance();

        String bindIp = "localhost";

        IMongodConfig mongodConfig = new MongodConfigBuilder()
            .version(Version.Main.PRODUCTION)
            .net(new Net(bindIp, port, Network.localhostIsIPv6()))
            .build();


        try {
            mongodExecutable = starter.prepare(mongodConfig);
            MongodProcess mongod = mongodExecutable.start();
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
    }

    @BeforeClass
    public static void init() throws Exception
    {
        setUpEmbeddedMongo();
        mongoDBHandler = new MongoDBHandler();
        mongoDBHandler.createConnection(host,port);
        assertTrue(mongoDBHandler.insertDocument(dataBaseName, collectionName, input));
    }

    @Test
    public void testGetDocuments(){
        ArrayList<String> documents = mongoDBHandler.getAllDocuments(dataBaseName, collectionName);
        assertTrue(documents.size() > 0);
    }

    @Test
    public void testGetDocumentsOnCondition(){
        ArrayList<String> documents = mongoDBHandler.find(dataBaseName, collectionName, condition);
        assertTrue(documents.size() > 0);
    }

//    @Test
//    TODO fix this test case
//    public void testGetDocumentOnCondition(){
//        ArrayList<String> documents = mongoDBHandler.getDocumentsOnCondition(dataBaseName, collectionName, condition);
//        String document = documents.get(0);
//        assertEquals(document, input);
//    }

    @Test
    public void testUpdateDocument(){
        assertTrue(mongoDBHandler.updateDocument(dataBaseName, collectionName, input, updateInput));
    }

    @AfterClass
    public static void dropCollection()
    {
        assertTrue(mongoDBHandler.dropDocument(dataBaseName, collectionName, condition));
        if (mongodExecutable != null)
            mongodExecutable.stop();
    }
}
