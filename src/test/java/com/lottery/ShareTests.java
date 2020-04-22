package com.lottery;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ShareTests {

//    @Ignore
    @Test
    public void speendTest() throws IOException, InterruptedException {// 1643082 //11467725 //4913317
        String url = "http://hq.sinajs.cn/list=sz000908";
        url = "https://hq.kaipanla.com/w1/api/index.php";
        int index = 5000;
        int size = 6000;
        String stockId = "002581";
//        while (true) {
        HttpResponse<String> httpResponse = HttpClient
                .newHttpClient().send(
                        HttpRequest.newBuilder(URI.create(url))
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .POST(BodyPublishers.ofString("DeviceID=dd81c83ba6afa08ecabb858113498d8b58c102bb&Index="
                                        + index + "&PhoneOSNew=3193&StockID=" + stockId
                                        + "&Type=2&UserID=778861&a=GetStockFenBi2&apiv=w21&c=StockL2Data&st=" + size,
                                        StandardCharsets.UTF_8))
                                .build(),
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject jsonObject = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
        System.out.println(httpResponse.body());
        JsonArray data = jsonObject.getAsJsonArray("fb");
        index = jsonObject.get("Count").getAsInt();
        System.out.println(index);
        List<Trade> list = new ArrayList<>(data.size() << 1);
        jsonObject.getAsJsonArray("fb").forEach(t -> {
            JsonArray asJsonArray = t.getAsJsonArray();
            list.add(new Trade(asJsonArray.get(0).getAsString(), asJsonArray.get(1).getAsFloat(),
                    asJsonArray.get(3).getAsInt(), asJsonArray.get(5).getAsInt() == 1, asJsonArray.get(7).getAsInt()));
        });
        List<Trade> collect = list.parallelStream().filter(t -> t.total > 1000000).collect(Collectors.toList());
        System.out.println("超过100万每单总笔数：" + collect.size());
        Map<Boolean, List<Trade>> map = collect.parallelStream().collect(Collectors.groupingBy(t -> t.buy));
        System.out.println("超过100万每单买入总笔数：" + map.get(true).size());
        System.out.println("超过100万每单卖出总笔数：" + map.get(false).size());
        System.out.println("超过100万每单买入总金额："
                + map.get(true).parallelStream().map(t -> t.total).collect(Collectors.summarizingInt(t -> t)));
        System.out.println("超过100万每单卖出总金额："
                + map.get(false).parallelStream().map(t -> t.total).collect(Collectors.summarizingInt(t -> t)));
//        TimeUnit.SECONDS.sleep(1);
//        }

//        int i = 50;
////		System.out.println(e - s);
//        int total = 0;
//        while (i-- > 0) {
//            long e = System.currentTimeMillis();
//            HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url)).build(),
//                    HttpResponse.BodyHandlers.ofString());
//            total += System.currentTimeMillis() - e;
//        }
//        System.out.println(total);
    }

//  @Ignore
    @Test
    public void findGoodNew() throws IOException, InterruptedException {
        long s = 345600;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String path = ShareTests.class.getResource("shareDesByOk.txt").getPath();
//      path = path.substring(1);
        List<String> collect = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8).stream()
                .map(t -> t.split("\t")[2]).collect(Collectors.toList());
        List<String[]> list = new ArrayList<>();
        int i = 0;
        for (String stockId : collect) {
            try {
                String url = "https://m.0033.com/listv4/hs/" + stockId + "_1.json";
                HttpResponse<String> httpResponse = HttpClient.newHttpClient()
                        .send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString());
                JsonArray jsonArray = JsonParser.parseString(httpResponse.body()).getAsJsonObject().get("data")
                        .getAsJsonObject().get("pageItems").getAsJsonArray();
                for (JsonElement element : jsonArray) {
                    JsonObject jsonObject = element.getAsJsonObject();
                    long asLong = jsonObject.get("ctime").getAsLong();
                    Date l = new Date((asLong + s) * 1000);
                    if (l.after(new Date())) {
                        if ("利好".equals(jsonObject.get("label_property").getAsString())) {
                            System.out.println();
                            String[] tmp = new String[] { stockId, simpleDateFormat.format(new Date(asLong * 1000)),
                                    jsonObject.get("source").getAsString(), jsonObject.get("title").getAsString(),
                                    jsonObject.get("url").getAsString() };
                            list.add(tmp);
                        }
                    } else {
                        break;
                    }
                }
                i++;
            } catch (Exception e) {
                System.out.println(i + "----------");
            }

        }
        list.forEach(t -> System.out.println(Arrays.toString(t)));
        List<String> list2 = list.stream().map(t -> t[0]).collect(Collectors.toList());
        System.out.println(list2);
    }

    @Test
    public void monitorViews() throws IOException, InterruptedException {
        int index = 0;
        int size = 3;
        String listUrl = "https://article.kaipanla.com/w1/api/index.php?Index=" + index
                + "&NewsID=0&PhoneOSNew=2&Token=b1c0216d069ff3c40e17ef97ee38dbf3&Type=0&UserID=778861&a=GetList&apiv=w21&c=ThemeNews&st="
                + size;
        String contentUrl = "https://article.kaipanla.com/w1/api/index.php?apiv=w21&PhoneOSNew=2";
        LinkedList<Integer> records = new LinkedList<>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat format = new DecimalFormat("0.00");
        while (true) {
            HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create(listUrl)).GET().build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonArray jsonArray = JsonParser.parseString(httpResponse.body()).getAsJsonObject().get("List")
                    .getAsJsonArray();
            int s = records.size();
            List<Integer> tmp = new ArrayList<>();
            jsonArray.forEach(t -> {
                int id = t.getAsJsonObject().get("CID").getAsInt();
                if (!records.contains(id)) {
                    tmp.add(id);
                }
            });
            Collections.reverse(tmp);
            records.addAll(tmp);
            if (s != 0 && records.size() > s) {
                httpResponse = HttpClient.newHttpClient().send(
                        HttpRequest.newBuilder(URI.create(contentUrl))
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .POST(BodyPublishers.ofString(
                                        "c=ThemeNews&a=GetInfo&NewsID=" + records.getLast()
                                                + "&UserID=778861&Token=b1c0216d069ff3c40e17ef97ee38dbf3",
                                        StandardCharsets.UTF_8))
                                .build(),
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
                JsonObject jsonObject = JsonParser.parseString(httpResponse.body()).getAsJsonObject().get("Info")
                        .getAsJsonObject();
                String title = jsonObject.get("Title").getAsString();
                String ZsName = jsonObject.get("ZSName").getAsString();
                String date = simpleDateFormat.format(new Date(jsonObject.get("TimeStamp").getAsLong() * 1000));
                String content = jsonObject.get("Content").getAsString();
                JsonArray stocks = jsonObject.get("Stocks").getAsJsonArray();
                List<String[]> codes = new ArrayList<>();
                stocks.forEach(t -> codes.add(new String[] { t.getAsJsonObject().get("Name").getAsString(),
                        t.getAsJsonObject().get("Code").getAsString(),
                        format.format(t.getAsJsonObject().get("Rate").getAsDouble()) }));
                System.out.println("title: " + title);
                System.out.println("ZsName: " + ZsName);
                System.out.println("date: " + date);
                System.out.println("content: " + content);
                System.out.println("--------------------------------------------------");
                codes.forEach(t -> {
                    System.out.println(t[0] + "\t" + t[1] + "\t" + t[2]);
                });
            }
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    @Test
    public void shareTest() throws IOException, InterruptedException {
        String url = "http://hq.sinajs.cn/list=sz002075";
        HttpResponse<String> httpResponse = HttpClient.newHttpClient()
                .send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString());
        System.out.println(httpResponse.body().split(",").length);
    }

//    @Ignore
    @Test
    public void myShareTest() throws IOException, InterruptedException {
        String url = "http://hq.sinajs.cn/list=sh600678,sh600068"
                + ",sh600989,sz300051,sz002277,sh603366,sz000716,sz300002,sh600853,sz002310,sz002154,sz002505,sz002305,sh600159";
        url = "http://hq.sinajs.cn/list=sz002075,sz002603,sz002156,sz002030";
        DecimalFormat format = new DecimalFormat("0.00");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        while (true) {
            HttpResponse<String> httpResponse = HttpClient.newHttpClient()
                    .send(HttpRequest.newBuilder(URI.create(url)).build(), HttpResponse.BodyHandlers.ofString());
            String str = httpResponse.body();
            String[] result = str.split("\n");
            System.out.println(simpleDateFormat.format(new Date()));
//			System.out.println(Stream.of(result[0].split(",")).collect(Collectors.joining("\t")));
            for (int i = 0; i < result.length; i++) {
                String[] tmp = result[i].split(",");
//                System.out.println(Arrays.toString(tmp));
                // // 买
//                System.out.println(tmp[20] + "--" + tmp[21]);
                // // 卖
//                System.out.println(tmp[10] + "--" + tmp[11]);
                System.out.print(tmp[0].split("\"")[1] + "\t" + tmp[0].split("=\"")[0].split("_")[2] + "\t" + tmp[3]
                        + "\t" + format.format((Float.valueOf(tmp[3]) / Float.valueOf(tmp[2]) - 1) * 100) + "%\t"
                        + (int) Math.floor(Float.valueOf(tmp[10]) / 100) + "\t" + tmp[11]);
                System.out.print("\t\t");
                if ((i & 1) == 1) {
                    System.out.println();
                }
            }
            System.out.println();
            TimeUnit.MILLISECONDS.sleep(3992);
        }
    }

    @Ignore
    @Test
    public void createTest() throws IOException {
        Files.writeString(Paths.get(ShareTests.class.getResource("").getPath(), "shareFail.txt"), "hwhw",
                StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    @Ignore
    @Test
    public void updateShareByOkTest() throws IOException {
        List<String> readAllLines = Files.readAllLines(
                Paths.get(ShareTests.class.getResource("shareFail.txt").getPath()), StandardCharsets.UTF_8);
        String result = Files
                .readAllLines(Paths.get(ShareTests.class.getResource("shareOk.txt").getPath()), StandardCharsets.UTF_8)
                .parallelStream().filter(t -> !readAllLines.contains(t)).collect(Collectors.joining("\r\n"));
        Files.writeString(Paths.get(ShareTests.class.getResource("").getPath(), "shareDesByOk.txt"), result,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    @Ignore
    @Test
    public void sortTest() throws IOException {
        List<JsonObject> dest = Files
                .readAllLines(Paths.get(ShareTests.class.getResource("shareSuccess.txt").getPath()),
                        StandardCharsets.UTF_8)
                .stream().map(t -> JsonParser.parseString(t).getAsJsonObject())
                .filter(t -> !t.getAsJsonObject("result").get("scoresAll").isJsonNull()
                        && t.getAsJsonObject("result").get("lastPrice").getAsFloat() < 16)
                .sorted((t1, t2) -> {
                    JsonObject jsonObject = t1.getAsJsonObject("result");
                    JsonElement jsonElement = jsonObject.get("scoresAll");
                    float t1Score = jsonElement.getAsFloat();
                    jsonObject = t2.getAsJsonObject();
                    jsonObject = jsonObject.getAsJsonObject("result");
                    jsonElement = jsonObject.get("scoresAll");
                    float t2Score = jsonElement.getAsFloat();
                    return t1Score == t2Score ? 0 : t1Score > t2Score ? -1 : 1;
                }).collect(Collectors.toList());
        dest.stream().limit(10).map(t -> {
            JsonObject jsonObject = t.getAsJsonObject("result");
            jsonObject.remove("sentiment");
            jsonObject.remove("stockIndex");
            jsonObject.remove("shIndex");
            jsonObject.remove("baseAnalysis");
            jsonObject.remove("blacklist");
            jsonObject.remove("category");
            jsonObject.remove("klineData");
            return t;
        }).collect(Collectors.groupingBy(t -> t.getAsJsonObject("result").get("scoresAll").getAsFloat()))
                .forEach((k, v) -> {
                    System.out.println("score: " + k + " -------> " + v);
                });
        Files.writeString(Paths.get(ShareTests.class.getResource("").getPath(), "result.txt"),
                dest.stream().map(t -> t.toString()).collect(Collectors.joining("\r\n")), StandardCharsets.UTF_8,
                StandardOpenOption.CREATE);
    }

    @Ignore
    @Test
    public void updateShareBySuccessTest() throws IOException {
        Map<String, String> orig = new HashMap<>();
        Files.readAllLines(Paths.get(ShareTests.class.getResource("shareOk.txt").getPath()), StandardCharsets.UTF_8)
                .stream().collect(Collectors.groupingBy(t -> {
                    String[] tmp = t.split("\\t");
                    return tmp[0] + "_" + tmp[2];
                })).forEach((k, v) -> orig.put(k, v.get(0).split("\\t")[1]));
        String result = Files.readAllLines(Paths.get(ShareTests.class.getResource("shareSuccess.txt").getPath()),
                StandardCharsets.UTF_8).stream().map(t -> {
                    JsonObject jsonObject = JsonParser.parseString(t).getAsJsonObject();
                    jsonObject = jsonObject.getAsJsonObject("result");
                    String name = jsonObject.get("stockName").getAsString();
                    String code = jsonObject.get("stockCode").getAsString();
                    return name + "\t" + orig.get(name + "_" + code) + "\t" + code;
                }).collect(Collectors.joining("\r\n"));
        Files.writeString(Paths.get(ShareTests.class.getResource("").getPath(), "shareDesBySuccess.txt"), result,
                StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    @Test
    public void getStartingPlate() throws IOException, InterruptedException {
        int size = 10;
        int index = 0;
        String url = "https://hq.kaipanla.com/w1/api/index.php";
        HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(BodyPublishers.ofString(
                                "Index=" + index + "&PhoneOSNew=2&a=Radar&apiv=w21&c=HomeDingPan&st=" + size,
                                StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonArray jsonArray = JsonParser.parseString(httpResponse.body()).getAsJsonObject().get("list")
                .getAsJsonArray();
        // {"time":1585810617,"status":"封涨大减","stock_name":"国脉科技","plate_type":1,"status_color":1,"zf":"","content":"涨停封单大幅减少1055万元，剩余封单5510万元","stockid":"002093"}
        // 时间, 状态，名称，板块类型，是涨还是跌，未知，内容，id
        jsonArray.forEach(System.out::println);
    }

    @Test
    public void getTopPlate() throws IOException, InterruptedException {
        String url = "https://hq.kaipanla.com/w1/api/index.php";
        int size = 3;
        int index = 0;
        while (true) {
            HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(BodyPublishers.ofString("Index=" + index
                            + "&Order=1&PhoneOSNew=2&Type=1&ZSType=7&a=RealRankingInfo&apiv=w21&c=ZhiShuRanking&st="
                            + size, StandardCharsets.UTF_8))
                    .build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonArray jsonArray = JsonParser.parseString(httpResponse.body()).getAsJsonObject().get("list")
                    .getAsJsonArray();
            jsonArray.forEach(System.out::println);
            System.out.println("---------------------");
            Thread.sleep(3000);
        }

    }

    @Test
    public void getAllPlate() throws IOException, InterruptedException {
        String url = "https://hq.kaipanla.com/w1/api/index.php";
        int size = 4000;
        int index = 0;
        HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(BodyPublishers.ofString("Index=" + index
                                + "&Order=1&PhoneOSNew=2&Type=1&ZSType=7&a=RealRankingInfo&apiv=w21&c=ZhiShuRanking&st="
                                + size, StandardCharsets.UTF_8))
                        .build(),
                HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonArray jsonArray = JsonParser.parseString(httpResponse.body()).getAsJsonObject().get("list")
                .getAsJsonArray();
        // ["881107","\u91c7\u6398\u670d\u52a1",1180,6.8,0.11,2171380619,214237543,408463506,-194225963,2.35,133540709106]
        // id, 名称, 强度, 涨幅, 涨速, 成交，主力净额，主力买，主力卖，量比，流通值
        LinkedList<String> ids = new LinkedList<>();
        jsonArray.forEach(t -> ids.add(t.getAsJsonArray().get(0).getAsString()));
        List<String[]> records = new ArrayList<>();
        for (String stockId : ids) {
            String id = stockId;
            httpResponse = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(BodyPublishers.ofString("Index=" + index + "&Order=1&PhoneOSNew=2&PlateID=" + id
                            + "&Token=b1c0216d069ff3c40e17ef97ee38dbf3&Type=6&UserID=778861&a=ZhiShuStockList_W8&apiv=w21&c=ZhiShuRanking&old=1&st="
                            + size, StandardCharsets.UTF_8))
                    .build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            jsonArray = JsonParser.parseString(httpResponse.body()).getAsJsonObject().get("list").getAsJsonArray();
            // ["300576","\u5bb9\u5927\u611f\u5149","\u6e38\u8d44",0,"\u5149\u523b\u80f6\u3001\u5927\u57fa\u91d1\u4e8c\u671f",40.48,10,612149442,27.23,0,2389977656,92028225,-27485909,64542316,
            // 15.03,4.49,10.54,3.85,1.15,2.7,9.26,1.0561,0,"3\u59292\u677f","\u9f99\u4e00",27.23,"",0,33586256,142434944,"",""]
            // id, 名称, 是否游资, 未知，板块，价格, 涨幅，成交额，实际换手，涨速，实际流通，主力买，主力卖，主力净额，
            // 未知，未知，未知，未知，未知，净流占比，区间涨幅，量比，未知，板数，龙几，实际换手，未知，未知，收盘封单，最大封单
            jsonArray.forEach(t -> {
                JsonArray j = t.getAsJsonArray();
                String[] tmp = new String[] { j.get(0).getAsString(), j.get(1).getAsString(), j.get(2).getAsString(),
                        j.get(3).getAsString(), j.get(4).getAsString(), j.get(5).getAsString(), j.get(6).getAsString(),
                        j.get(7).getAsString(), j.get(8).getAsString(), j.get(9).getAsString(), j.get(10).getAsString(),
                        j.get(11).getAsString(), j.get(12).getAsString(), j.get(13).getAsString() };
                records.add(tmp);
            });
        }

        List<String[]> collect = records.stream().filter(t -> {
            if (Double.valueOf(t[5]) < 90 && Double.valueOf(t[6]) > 3 && Double.valueOf(t[6]) < 6
                    && Double.valueOf(t[8]) > 4 && Double.valueOf(t[13]) > 0) {
                return true;
            }
            return false;
        }).collect(Collectors.toList());
        Set<String> stockIds = new HashSet<>();
        for (String[] stockId : collect) {
            String id = stockId[0];
            httpResponse = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create(url)).header("Content-Type", "application/x-www-form-urlencoded")
                            .POST(BodyPublishers.ofString(
                                    "PhoneOSNew=2&StockID=" + id + "&a=GetZhangTingGene&apiv=w21&c=StockL2Data",
                                    StandardCharsets.UTF_8))
                            .build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            jsonArray = JsonParser.parseString(httpResponse.body()).getAsJsonObject().get("List").getAsJsonArray();
            // [18,8,81.8182,80,20,23.0769]
            // 涨停次数，溢价5%次数，次日红盘率，首板封板率，首板破板率，连板率
            if (jsonArray.get(0).getAsInt() >= 5) {
                stockIds.add(id);
                System.out.println(Arrays.toString(stockId));
            }
        }
        System.out.println(stockIds);

    }

    @Test
    public void getSelfShare() throws IOException, InterruptedException {
        String url = "https://hq.kaipanla.com/w1/api/index.php";
        int size = 4000;
        int index = 0;
        String date = "2020-04-15";
        HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(BodyPublishers.ofString("Date=" + date
                        + "&Filter=0&Order=1&PhoneOSNew=2&Ratio=6&Type=6&a=RealRankingInfo_W8&apiv=w21&c=NewStockRanking&index="
                        + index + "&st=" + size, StandardCharsets.UTF_8))
                .build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonArray jsonArray = JsonParser.parseString(httpResponse.body()).getAsJsonObject().get("list")
                .getAsJsonArray();
        // ["002076","雪莱特","",0,"病毒防治、环保",2.7,10.2,83533938,5.33,0,1611541032,42995098,0,42995098,51.47,0,51.47,2.67,0,2.67,11.11,1.12803,0,"首板","",5.33,"",0,0,0,"","",""]
        // id, 名称, 是否游资, 未知，板块，价格, 涨幅，成交额，实际换手，涨速，实际流通，主力买，主力卖，主力净额，
        // 未知，未知，未知，未知，未知，净流占比，区间涨幅，量比，未知，板数，龙几，实际换手，未知，未知，收盘封单，最大封单
        List<String> stocks = new ArrayList<String>(200);
        System.out.println(jsonArray.size());
        for (int i = 0; i < 200; i++) {
            System.out.println(jsonArray.get(i).getAsJsonArray().get(6));
            stocks.add(jsonArray.get(i).getAsJsonArray().get(0).getAsString());
        }
        System.out.println(stocks);
    }

//    class Share {
//        String id;
//        String name;
//        boolean main;
//        String plate;
//        double price;
//        double sup;
//        double tprice;
//        double exchange;
//    }

    @Test
    public void biddingPrice() throws IOException, InterruptedException, URISyntaxException {
        // PhoneOSNew=2&StockID=300576&Token=b1c0216d069ff3c40e17ef97ee38dbf3&UserID=778861&a=GetStockBid&apiv=w21&c=StockL2Data
//        List<String> collect = Stream.of(
//                "603095, 002980, 300325, 002426, 000766, 300158, 300368, 002437, 002349, 600829, 300147, 002826, 603963, 600107, 600396, 000955, 002928, 300534, 603330, 300385, 002581, 002041, 002198, 002824, 002979, 300202, 000919, 603489, 603387, 300026, 300463, 002249, 300471, 002644, 600225, 300049, 002847, 300827, 300658, 002030, 603109, 600511, 603032, 300551, 002603, 300465, 002866, 600268, 300039, 300334, 000767, 002400, 600527, 300639, 002750, 300332, 300194, 002882, 300722, 002456, 603920, 601179, 002317, 603059, 300676, 300485, 300628, 603158, 600267, 600186, 603313, 002585, 002850, 600222, 603709, 600836, 600732, 600371, 688012, 603258, 600200, 300314, 300755, 002693, 002709, 300133, 300813, 600513, 002563, 002859, 002938, 600855, 002560, 002873, 002653, 603979, 600933, 688051, 002185, 000710, 002166, 002758, 002668, 300598, 000822, 688111, 002639, 603501, 600218, 002970, 002967, 002785, 000982, 002065, 300417, 603676, 600321, 688298, 002086, 600283, 603127, 601689, 600228, 603601, 300259, 603345, 300782, 600767, 600526, 000596, 600488, 002333, 002749, 600789, 600080, 002501, 000806, 603990, 002480, 002864, 000760, 002445, 000650, 002050, 002330, 002027, 603129, 002160, 002566, 688068, 603309, 300066, 300386, 600594, 002411, 002925, 600351, 002398, 300416, 002878, 002973, 000957, 300349, 300298, 002022, 002459, 300644, 600557, 300255, 002556, 300371, 300204, 002129, 600613, 300600, 603583, 300393, 603987, 002176, 600696, 300492, 603579, 603986, 002165, 300086, 002002, 000718, 002886, 603686, 000859, 000518, 002594, 002583, 300636, 603926, 002487, 600409, 002072, 600645, 603712"
//                        .split(",\\s"))
//                .collect(Collectors.toList());
        List<String> readAllLines = Files
                .readAllLines(Paths.get(ShareTests.class.getResource("shareStockId.txt").toURI()));
        List<JsonArray> lists = new LinkedList<JsonArray>();
        for (String id : readAllLines) {
            if (id.trim().equals("")) {
                continue;
            }
            String url = "https://hq.kaipanla.com/w1/api/index.php";
            HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(BodyPublishers.ofString("PhoneOSNew=2&StockID=" + id.trim()
                            + "&Token=1c90c577bbf1c9abd83f4ff1295f37b8&UserID=778861&a=GetStockBid&apiv=w21&c=StockL2Data",
                            StandardCharsets.UTF_8))
                    .build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonObject jsonObject = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
            JsonElement jsonElement = jsonObject.get("preclose_px");
            if (null == jsonElement) {
                continue;
            }
            String preclose = jsonObject.get("preclose_px").getAsString();
            JsonArray asJsonArray = jsonObject.get("bid").getAsJsonArray();
            if (asJsonArray.size() == 0) {
                continue;
            }
            JsonArray jsonArray = asJsonArray.get(asJsonArray.size() - 1).getAsJsonArray();
            jsonArray.add(id);
            jsonArray.add(preclose);
            lists.add(jsonArray);
        }
        Collections.sort(lists, new Comparator<JsonArray>() {
            @Override
            public int compare(JsonArray o1, JsonArray o2) {
                if (o1.get(3).getAsInt() == o2.get(3).getAsInt()) {
                    return 0;
                }
                return o1.get(3).getAsInt() > o2.get(3).getAsInt() ? -1 : 1;
            }
        });
        System.out.println(lists.size());
        List<JsonArray> collect2 = lists.stream().filter(t -> {
            float f = (t.get(3).getAsFloat() - t.get(5).getAsFloat()) / t.get(5).getAsFloat();
            t.add(f);
            return Math.abs(f) < 3;
        }).collect(Collectors.toList());
        collect2.stream().limit(100).forEach(System.out::println);
    }

    @Test
    public void tiger() throws IOException, InterruptedException, ParseException {
        // DeviceID=dd81c83ba6afa08ecabb858113498d8b58c102bb&Index=0&PhoneOSNew=2&Time=2020-04-02&Token=b1c0216d069ff3c40e17ef97ee38dbf3&Type=1&UserID=778861&a=GetStockList&apiv=w21&c=LongHuBang&st=300
        String url = "https://lhb.kaipanla.com/w1/api/index.php";
        int index = 0;
        int size = 2;
        String date = "2015-09-24";
        HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(BodyPublishers.ofString("DeviceID=dd81c83ba6afa08ecabb858113498d8b58c102bb&Index=" + index
                        + "&PhoneOSNew=2&Time=" + date
                        + "&Token=b1c0216d069ff3c40e17ef97ee38dbf3&Type=1&UserID=778861&a=GetStockList&apiv=w21&c=LongHuBang&st="
                        + size, StandardCharsets.UTF_8))
                .build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (StringUtils.isNoneBlank(httpResponse.body())) {
            System.out.println(httpResponse.body());
        }
//        JsonObject jsonObject = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
//        System.out.println(jsonObject);
    }
    
    @Test
    public void tigerDetail() throws IOException, InterruptedException, ParseException {
        // DeviceID=dd81c83ba6afa08ecabb858113498d8b58c102bb&Index=0&PhoneOSNew=2&Time=2020-04-02&Token=b1c0216d069ff3c40e17ef97ee38dbf3&Type=1&UserID=778861&a=GetStockList&apiv=w21&c=LongHuBang&st=300
        String url = "https://lhb.kaipanla.com/w1/api/index.php?apiv=w21&PhoneOSNew=2";
        int index = 0;
        int size = 2;
        String date = "2015-09-24";
        HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(BodyPublishers.ofString("c=Stock&a=GetNewOneStockInfo&UserID=778861&Token=1c90c577bbf1c9abd83f4ff1295f37b8&Type=0&Time="+date+"&StockID=002030", StandardCharsets.UTF_8))
                .build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (StringUtils.isNoneBlank(httpResponse.body())) {
            System.out.println(httpResponse.body());
        }
//        JsonObject jsonObject = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
//        System.out.println(jsonObject);
    }

    @Test
    public void tradeDetail() throws IOException, InterruptedException {
        // DeviceID=dd81c83ba6afa08ecabb858113498d8b58c102bb&Index=4200&PhoneOSNew=3193&StockID=002426&Type=2&UserID=778861&a=GetStockFenBi2&apiv=w21&c=StockL2Data&st=10
        String url = "https://hq.kaipanla.com/w1/api/index.php";
        String id = "002426";
        int index = 3905;
        int size = 10;
        HttpResponse<String> httpResponse = HttpClient
                .newHttpClient().send(
                        HttpRequest.newBuilder(URI.create(url))
                                .header("Content-Type", "application/x-www-form-urlencoded")
                                .POST(BodyPublishers.ofString("DeviceID=dd81c83ba6afa08ecabb858113498d8b58c102bb&Index="
                                        + index + "&PhoneOSNew=3193&StockID=" + id
                                        + "&Type=2&UserID=778861&a=GetStockFenBi2&apiv=w21&c=StockL2Data&st=" + size,
                                        StandardCharsets.UTF_8))
                                .build(),
                        HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        JsonObject jsonObject = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
        System.out.println(jsonObject);
    }

    @Ignore
    @Test
    public void allShareTest() throws IOException, InterruptedException {
//		String url = "http://api.money.126.net/data/feed/0600389,money.api?callback=test";
//		url = "http://hq.sinajs.cn/list=sh600389";
        List<String> readAllLines = Files.readAllLines(
                Paths.get(ShareTests.class.getResource("shareDesBySuccess.txt").getPath()), StandardCharsets.UTF_8);
        StringBuffer sb = new StringBuffer();
        StringBuffer sb1 = new StringBuffer();
        CountDownLatch countDownLatch = new CountDownLatch(readAllLines.size());
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() << 1);
        for (String str : readAllLines) {
            if ("".equals(str.trim())) {
                countDownLatch.countDown();
                continue;
            }
            executorService.execute(() -> {
                String[] tmp = str.split("\\t");
                String market = tmp[1];
                String code = tmp[2];
                String url = "https://gateway.jinyi999.cn/rjhy-stock-diagnosis/api/1/app/1DE3B03F8A42974EA0ABCD009FAC6351EC/summary/stock/diagnosis?dataSourceType=A&market="
                        + market + "&stockCode=" + code + "&period=TWENTY";
                HttpResponse<String> httpResponse;
                try {
                    httpResponse = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url)).build(),
                            HttpResponse.BodyHandlers.ofString());
                    String result = httpResponse.body();
                    if (!result.contains(":2000")) {
                        sb.append(result + "\r\n");
                    } else {
                        sb1.append(str + "\r\n");
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        Files.writeString(Paths.get(ShareTests.class.getResource("").getPath(), "shareSuccess.txt"), sb.toString(),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE);
        Files.writeString(Paths.get(ShareTests.class.getResource("").getPath(), "shareFail.txt"), sb1.toString(),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }

    @Ignore
    @Test
    public void parseShareTest() throws URISyntaxException, IOException {
        List<String> readAllLines = Files.readAllLines(Paths.get(ShareTests.class.getResource("/share.txt").getPath()),
                StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        String prefix = readAllLines.get(0).split("\\)")[0].split("\\(")[1].split("-")[0];
        String content = readAllLines.get(1);
        String[] first = content.split("\\)");
        for (String str : first) {
            String[] tmp = str.split("\\(");
            sb.append(tmp[0]).append("\t").append(prefix).append("\t").append(tmp[1]).append("\r\n");
        }
        prefix = readAllLines.get(2).split("\\)")[0].split("\\(")[1].split("-")[0];
        content = readAllLines.get(3);
        first = content.split("\\)");
        for (String str : first) {
            String[] tmp = str.split("\\(");
            sb.append(tmp[0]).append("\t").append(prefix).append("\t").append(tmp[1]).append("\r\n");
        }
        Files.writeString(Paths.get(ShareTests.class.getResource("").getPath(), "shareOk.txt"), sb.toString(),
                StandardCharsets.UTF_8, StandardOpenOption.CREATE);
    }
}

class Trade {
    String time;
    float price;
    int num;
    boolean buy;
    int total;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public boolean isBuy() {
        return buy;
    }

    public void setBuy(boolean buy) {
        this.buy = buy;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public Trade(String time, float price, int num, boolean buy, int total) {
        super();
        this.time = time;
        this.price = price;
        this.num = num;
        this.buy = buy;
        this.total = total;
    }

    @Override
    public String toString() {
        return "Trade [time=" + time + ", price=" + price + ", num=" + num + ", buy=" + buy + ", total=" + total + "]";
    }

}
