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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ShareTests {

//    @Ignore
    @Test
    public void speendTest() throws IOException, InterruptedException {
        String url = "http://hq.sinajs.cn/list=sh600678";
        url = "https://hq.kaipanla.com/w1/api/index.php";
        int index = 4339;
        int size = 100000;
        while (true) {
            HttpResponse<String> httpResponse = HttpClient.newHttpClient().send(HttpRequest.newBuilder(URI.create(url))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(BodyPublishers.ofString("DeviceID=dd81c83ba6afa08ecabb858113498d8b58c102bb&Index=" + index
                            + "&PhoneOSNew=3193&StockID=600267&Type=2&UserID=778861&a=GetStockFenBi2&apiv=w21&c=StockL2Data&st="
                            + size, StandardCharsets.UTF_8))
                    .build(), HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonObject jsonObject = JsonParser.parseString(httpResponse.body()).getAsJsonObject();
            JsonArray data = jsonObject.getAsJsonArray("fb");
            index = jsonObject.get("Count").getAsInt();
            System.out.println(index);
            List<Trade> list = new ArrayList<>(data.size() << 1);
            jsonObject.getAsJsonArray("fb").forEach(t -> {
                JsonArray asJsonArray = t.getAsJsonArray();
                list.add(new Trade(asJsonArray.get(0).getAsString(), asJsonArray.get(1).getAsFloat(),
                        asJsonArray.get(3).getAsInt(), asJsonArray.get(5).getAsInt() == 1,
                        asJsonArray.get(7).getAsInt()));
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
            TimeUnit.SECONDS.sleep(1);
        }

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

//    @Ignore
    @Test
    public void myShareTest() throws IOException, InterruptedException {
        String url = "http://hq.sinajs.cn/list=sh600678,sh600068"
                + ",sh600989,sz300051,sz002277,sh603366,sz000716,sz300002,sh600853,sz002310,sz002154,sz002505,sz002305,sh600159";
        url = "http://hq.sinajs.cn/list=sz002506,sh601398,sz000725,sh600089,sz000420,sz002405,sz000100,sh000001,sh601857,sh600028";
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
    public void getStaringPlate() throws IOException, InterruptedException {
        int size = 2000;
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
        System.out.println(jsonArray.get(0));
    }

    @Test
    public void getAllPlate() throws IOException, InterruptedException {
        String url = "https://hq.kaipanla.com/w1/api/index.php";
        int size = 2000;
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
        String id = "300576";
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
        System.out.println(jsonArray);
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

}
