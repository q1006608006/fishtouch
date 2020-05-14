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

    public static void main(String[] args) throws IOException {
        init();

        LocalTime markTime = LocalTime.parse(markTimeStr);
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(2);

        long nextStartTime;
        LocalDateTime todayMarkTime = LocalDate.now().atTime(markTime);
        if (todayMarkTime.isAfter(LocalDateTime.now())) {
            nextStartTime = getSecond(todayMarkTime) - getSecond(LocalDateTime.now());
        } else {
            nextStartTime = getSecond(todayMarkTime.plusDays(1)) - getSecond(LocalDateTime.now());
        }


        System.out.println("下次启动时间: " + nextStartTime);
        executor.scheduleAtFixedRate(new TaskRunner(), nextStartTime, 60 * 60 * 24, TimeUnit.SECONDS);

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
            Queue<SchTask> taskQueue = getRandomTask(baseTaskList);
            long finalTime = System.currentTimeMillis() + randomRange * 1000;
            System.out.println("task数量: " + taskQueue.size());

            while (taskQueue.size() > 0) {
                SchTask task = taskQueue.poll();
                int free = ((Long) (finalTime - System.currentTimeMillis())).intValue() / (taskQueue.size() + 1);
                long sleep = new Random().nextInt(free);
                try {
                    System.out.println("休眠 " + sleep / 1000);
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                execTask(task);
            }
        }
    }

    private static void execTask(SchTask task) {
        for (int i = 0; i < 3; i++) {
            try {
                send(attachHost, task.getAttachBody());
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            long sleepTime = (new Random().nextInt(15) + 10) * 1000;
            System.out.println("sleep " + sleepTime / 1000);
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 3; i++) {
            try {
                send(healthCodeHost, task.getHealthBody());
                break;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void send(String host, String body) throws IOException {
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

        if (conn.getResponseCode() < 300) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

            StringBuilder builder = new StringBuilder();
            String temp;
            while ((temp = reader.readLine()) != null) {
                builder.append(temp).append("\n");
            }
            System.out.println(builder.toString());

            reader.close();
        }

        os.close();
        conn.disconnect();
    }

    private static long getSecond(LocalDateTime time) {
        return time.toInstant(ZoneOffset.of("+8")).getEpochSecond();
    }
}
