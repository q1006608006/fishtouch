package top.ivan.ac;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author Ivan
 * @description
 * @date 2020/5/12
 */
public class AutoHealthCode {

    private static String healthCodeHost;
    private static String attachHost;

    private static String markTimeStr;
    private static int randomRange;

    private static List<SchTask> baseTaskList;

    private final static Map<String, LocalDateTime> lastSuccessMap = new HashMap<>();

    public static void init() throws IOException {
        Properties prop = new Properties();
        InputStream resIn;
        File file = new File("config.properties");
        if (file.exists()) {
            resIn = new FileInputStream(file);
        } else {
            resIn = AutoHealthCode.class.getClassLoader().getResourceAsStream("config.properties");
        }
        prop.load(new InputStreamReader(resIn, StandardCharsets.UTF_8));
        resIn.close();

        healthCodeHost = prop.getProperty("healthCodeHost");
        attachHost = prop.getProperty("attachHost");

        markTimeStr = prop.getProperty("markTime");
        randomRange = Integer.parseInt(prop.getProperty("randomRange"));

        int index = 0;
        List<String> hList = new ArrayList<>();
        List<String> aList = new ArrayList<>();

        for (; ; index++) {
            String healthBody = prop.getProperty("healthBody[" + index + "]");
            String attachBody = prop.getProperty("attachBody[" + index + "]");

            if (healthBody == null || attachBody == null) {
                break;
            }
            if (healthBody.trim().length() > 0) {
                hList.add(healthBody);
            }
            if (attachBody.trim().length() > 0) {
                aList.add(attachBody);
            }
        }
        baseTaskList = new ArrayList<>();
        for (int i = 0; i < aList.size(); i++) {
            baseTaskList.add(new SchTask(aList.get(i), hList.get(i)));
        }
    }

    public static Queue<SchTask> getRandomTask(List<SchTask> src) {
        int all = src.size();
        Integer[] group = new Integer[all];

        Random random = new Random();
        for (int i = 0; i < all; i++) {
            int pos = random.nextInt(100) % (all - i);
            for (int mod = 0; mod < all; mod++) {
                if (group[pos + mod] == null) {
                    group[pos + mod] = i;
                    break;
                }
            }
        }

        List<SchTask> randomTask = new ArrayList<>();
        for (Integer pos : group) {
            randomTask.add(src.get(pos));
        }
        return new LinkedBlockingQueue<>(randomTask);
    }

    public static class SchTask {
        private String attachBody;
        private String healthBody;

        public SchTask(String attachBody, String healthBody) {
            this.attachBody = attachBody;
            this.healthBody = healthBody;
        }

        public String getAttachBody() {
            return attachBody;
        }

        public void setAttachBody(String attachBody) {
            this.attachBody = attachBody;
        }

        public String getHealthBody() {
            return healthBody;
        }

        public void setHealthBody(String healthBody) {
            this.healthBody = healthBody;
        }
    }

    public static class TaskRunner implements Runnable {

        @Override
        public void run() {
            try {
                Queue<SchTask> taskQueue = getRandomTask(baseTaskList);
                long finalTime = System.currentTimeMillis() + randomRange * 1000;
                int free = ((Long) (finalTime - System.currentTimeMillis())).intValue() / (taskQueue.size() + 1);
                if (free < 0) {
                    free = 1;
                }
                System.out.println("task数量: " + taskQueue.size());

                while (taskQueue.size() > 0) {
                    SchTask task = taskQueue.poll();
                    long sleep = new Random().nextInt(free);
                    try {
                        System.out.println("休眠 " + sleep / 1000);
                        Thread.sleep(sleep);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    execTask(task);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void execTask(SchTask task) {
        for (int i = 0; i < 3; i++) {
            if (testSuccessToday(task.getAttachBody())) {
                System.out.println("success today: " + task.getAttachBody());
                break;
            }
            try {
                send(attachHost, forAttach(task.getAttachBody()));
                long sleepTime = (new Random().nextInt(15) + 10) * 1000;
                System.out.println("sleep " + sleepTime / 1000);
                Thread.sleep(sleepTime);
                send(healthCodeHost, task.getHealthBody());

                lastSuccessMap.put(task.getAttachBody(), LocalDateTime.now());
                break;
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static String forAttach(String attach) {
        if (LocalDate.now().getDayOfWeek().getValue() > 5) {
            return attach.replaceAll("\"returnWorkStatus\":\"1\"", "\"returnWorkStatus\":\"0\"");
        }
        return attach;
    }

    private static boolean testSuccessToday(String attachBody) {
        LocalDateTime time = lastSuccessMap.get(attachBody);
        return time != null && time.toLocalDate().isEqual(LocalDate.now());
    }

    public static void send(String host, String body) throws IOException {
        URL url = new URL(host);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("POST");
        conn.setConnectTimeout(60000);
        conn.setReadTimeout(60000);
        conn.setDoOutput(true);
        conn.setDoInput(true);

        conn.setRequestProperty("Content-Type", "application/json");

        OutputStream os = conn.getOutputStream();
        os.write(body.getBytes());

        BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        StringBuilder builder = new StringBuilder();
        String temp;
        while ((temp = reader.readLine()) != null) {
            builder.append(temp).append("\n");
        }
        System.out.println(builder.toString());

        os.close();
        conn.disconnect();
    }

    private static long getSecond(LocalDateTime time) {
        return time.toInstant(ZoneOffset.of("+8")).getEpochSecond();
    }

    public static void main(String[] args) throws IOException {
        init();

        String[] times = markTimeStr.split(",");

        for (int i = 0; i < times.length; i++) {

            String markTimeStr = times[i];
            LocalTime markTime = LocalTime.parse(markTimeStr);
            ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

            long nextStartTime;
            LocalDateTime todayMarkTime = LocalDate.now().atTime(markTime);
            if (todayMarkTime.isAfter(LocalDateTime.now())) {
                nextStartTime = getSecond(todayMarkTime) - getSecond(LocalDateTime.now());
            } else {
                nextStartTime = getSecond(todayMarkTime.plusDays(1)) - getSecond(LocalDateTime.now());
            }

            System.out.println("任务" + i + "启动时间: " + nextStartTime);
            executor.scheduleAtFixedRate(new TaskRunner(), nextStartTime, 60 * 60 * 24, TimeUnit.SECONDS);
        }

    }
}
