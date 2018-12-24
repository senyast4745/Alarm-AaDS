import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainAlarm {

    private static AlarmNode headAll;
    static AlarmNode headAlarm;
    static int length = 0;
    static AlarmThread alarmThread;
    private static int allLength = 0;

    public static void main(String[] args) {
        System.out.println("Enter <add - <message> - <dd.mm.yyyy hh:mm Z>> in future " +
                "\nZ = +0300 for Moscow Timezone \nCommand: add - add alarm" +
                "\n<list> - lists of alarms" +
                "\n<cancel - <index>> - cancel alarm by index");
        MainAlarm.run();
    }

    /**
     * This is main method to this program
     */
    @SuppressWarnings("InfiniteLoopStatement")
    private static void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String s = null;
        while (true) {
            try {
                s = in.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            assert s != null;
            String[] spl = s.split("-");
            switch (spl[0].trim()) {
                case "add":
                    try {
                        check(spl);
                    } catch (IllegalArgumentException e) {
                        System.out.println(s + ": denied. Time in past");
                        continue;
                    } catch (ParseException e) {
                        System.out.println("Incorrect input: parse date exception");
                        e.getStackTrace();
                        continue;
                    } catch (IOException e) {
                        System.out.println(s + ": denied. This alarm is already exists");
                        continue;
                    }
                    break;

                case "list":
                    int i = 0;
                    AlarmNode node = headAll;
                    if (allLength == 0) {
                        break;
                    }
                    do {
                        if (node.cancel) {
                            System.out.println("[" + i + "] " + node.timeUser + " [CANCELED]");
                            i++;
                        } else {
                            if (!node.schedule) {
                                System.out.println("[" + i + "] " + node.timeUser + " [SCHEDULED]");
                                i++;
                            } else {
                                System.out.println("[" + i + "] " + node.timeUser + " [EXECUTED]");
                                i++;
                            }
                        }
                        node = node.next;
                    } while (node != null);
                    break;

                case "cancel":
                    try {
                        cancel(Integer.parseInt(spl[1].trim()));

                    } catch (IllegalArgumentException e) {
                        System.out.println("This alarm is already canceled or scheduled");
                    } catch (ArrayIndexOutOfBoundsException e) {
                        System.out.println("This alarm isn't exists");
                    }
                    System.out.println("canceled");
                    break;
                default: {
                    System.out.println("Unsupported method");
                }
            }
        }
    }

    /**
     * This method check if we can add an alarm to the list
     *
     * @param s - splits input string as massive of sub string
     * @throws ParseException - if parse failed
     * @throws IOException - if alarm is already exists
     */
    private static void check(String[] s) throws ParseException, IOException {
        String dateString = s[2].trim();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm Z");
        Date date = dateFormat.parse(dateString);
        long unixTime = date.getTime();
        if (unixTime < System.currentTimeMillis()) {
            throw new IllegalArgumentException();
        }
        AlarmNode alarm = new AlarmNode(unixTime, s[1].trim(), s[2].trim());
        if (length != 0) {
            AlarmNode node = headAlarm;
            while (node.next != null) {
                if (node.time == alarm.time && !node.cancel)
                    throw new IOException();
                node = node.next;
            }
        }
        addNode(alarm);
        System.out.println("added");
    }

    /**
     * This method add new alarm node to linked list of nods
     *
     * @param alarmNode - alarm Node to add
     */
    private static void addNode(AlarmNode alarmNode) {
        boolean done = false;
        if (allLength == 0) {
            headAll = alarmNode;
            headAlarm = headAll;
            alarmThread = new AlarmThread(alarmNode);
            alarmThread.start();
        } else {
            AlarmNode node = headAll;
            if (node.time > alarmNode.time) {
                alarmThread.interrupt();
                alarmNode.next = node;
                headAll = alarmNode;
                headAlarm = headAll;
                alarmThread = new AlarmThread(headAll);
                alarmThread.start();
            } else {
                while (node.next != null) {
                    if (node.next.time > alarmNode.time) {
                        alarmNode.next = node.next;
                        node.next = alarmNode;
                        done = true;
                        if (headAlarm.time > alarmNode.time || length == 0) {
                            alarmThread.interrupt();
                            headAlarm = alarmNode;
                            alarmThread = new AlarmThread(alarmNode);
                            alarmThread.start();
                        }
                        break;
                    }
                    node = node.next;
                }
                if (!done) {
                    node.next = alarmNode;
                    if (headAlarm.time > alarmNode.time || length == 0) {
                        alarmThread.interrupt();
                        headAlarm = alarmNode;
                        alarmThread = new AlarmThread(alarmNode);
                        alarmThread.start();
                    }
                }
            }
        }
        allLength++;
        length++;
    }

    /**
     * This method cancel alarmNode by index
     *
     * @param index - index of alarmNode to cancel
     */
    private static void cancel(int index) {
        if (index >= allLength) {
            throw new IndexOutOfBoundsException();
        }
        int tmpInd = 0;
        AlarmNode node = headAll;
        while (tmpInd < index) {
            node = node.next;
            tmpInd++;
        }
        if (node.cancel || node.schedule) {
            throw new IllegalArgumentException();
        }
        node.cancel = true;
        length--;
        if (length == 0) {
            alarmThread.interrupt();
            return;
        }
        if (node.time == headAlarm.time) {
            while (node.next != null) {
                node = node.next;
                if (!node.cancel) {
                    alarmThread.interrupt();
                    alarmThread = new AlarmThread(node);
                    alarmThread.start();
                    break;
                }
            }
        }

    }

}

