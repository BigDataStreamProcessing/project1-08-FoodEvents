package com.example.bigdata;

import com.espertech.esper.common.client.EPCompiled;
import com.espertech.esper.common.client.EventBean;
import com.espertech.esper.common.client.configuration.Configuration;
import com.espertech.esper.compiler.client.CompilerArguments;
import com.espertech.esper.compiler.client.EPCompileException;
import com.espertech.esper.compiler.client.EPCompiler;
import com.espertech.esper.compiler.client.EPCompilerProvider;
import com.espertech.esper.runtime.client.*;
import net.datafaker.Faker;
import net.datafaker.fileformats.Format;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class EsperClient {
    public static void main(String[] args) throws InterruptedException {
        int noOfRecordsPerSec;
        int howLongInSec;
        if (args.length < 2) {
            noOfRecordsPerSec = 2;
            howLongInSec = 5;
        } else {
            noOfRecordsPerSec = Integer.parseInt(args[0]);
            howLongInSec = Integer.parseInt(args[1]);
        }

        Configuration config = new Configuration();
        EPCompiled epCompiled = getEPCompiled(config);

        // Connect to the EPRuntime server and deploy the statement
        EPRuntime runtime = EPRuntimeProvider.getRuntime("http://localhost:port", config);
        EPDeployment deployment;
        try {
            deployment = runtime.getDeploymentService().deploy(epCompiled);
        } catch (EPDeployException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }

        EPStatement resultStatement = runtime.getDeploymentService().getStatement(deployment.getDeploymentId(), "answer");

        resultStatement.addListener((newData, oldData, stmt, runTime) -> {
            for (EventBean eventBean : newData) {
                System.out.printf("R: %s%n", eventBean.getUnderlying());

            }
        });

        Faker faker = new Faker();
        String record;

        List<String> dishes = new ArrayList<>();
        while (dishes.size() < 5) {
            String newDish = faker.food().dish();
            if (!dishes.contains(newDish)) {
                dishes.add(newDish);
            }
        }

        List<String> shops = new ArrayList<>();
        shops.add("Lidl");
        shops.add("Carrefour");
        shops.add("Auchan");
        shops.add("Mila");
        shops.add("Spar");
        shops.add("Netto");

        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() < startTime + (1000L * howLongInSec)) {
            for (int i = 0; i < noOfRecordsPerSec; i++) {
                Timestamp eTimestamp = faker.date().past(30, TimeUnit.SECONDS);
                eTimestamp.setNanos(0);
                Timestamp iTimestamp = Timestamp.valueOf(LocalDateTime.now().withNano(0));
                record = Format.toJson()
                        .set("ean", () -> faker.code().ean8())
                        .set("dish", () -> getRandomElement(dishes))
                        .set("quantity", () -> String.valueOf(faker.number().numberBetween(-5, 10)))
                        .set("bought_accepted", eTimestamp::toString)
                        .set("shop", () -> getRandomElement(shops))
                        .set("its", iTimestamp::toString)
                        .set("ets", eTimestamp::toString)
                        .build().generate();
                runtime.getEventService().sendEventJson(record, "FoodEvent");
            }
            waitToEpoch();
        }
    }

    private static EPCompiled getEPCompiled(Configuration config) {
        CompilerArguments compilerArgs = new CompilerArguments(config);

        // Compile the EPL statement
        EPCompiler compiler = EPCompilerProvider.getCompiler();
        EPCompiled epCompiled;
        try {
            epCompiled = compiler.compile("""
                    @public @buseventtype create json schema FoodEvent(ean string, dish string, quantity int,
                        bought_accepted string, shop string, its string, ets string);
                    @name('answer') SELECT ean, dish, quantity, shop, ets, its
                    FROM FoodEvent#ext_timed(java.sql.Timestamp.valueOf(its).getTime(), 3 sec);
                    """, compilerArgs);

        } catch (EPCompileException ex) {
            // handle exception here
            throw new RuntimeException(ex);
        }
        return epCompiled;
    }

    static void waitToEpoch() throws InterruptedException {
        long millis = System.currentTimeMillis();
        Instant instant = Instant.ofEpochMilli(millis);
        Instant instantTrunc = instant.truncatedTo(ChronoUnit.SECONDS);
        long millis2 = instantTrunc.toEpochMilli();
        TimeUnit.MILLISECONDS.sleep(millis2 + 1000 - millis);
    }

    public static String getRandomElement(List<String> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

}

