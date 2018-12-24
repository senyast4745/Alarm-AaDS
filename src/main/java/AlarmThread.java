public class AlarmThread extends Thread {

    private AlarmNode node; // cuurent node in thread


    AlarmThread(AlarmNode node) {
        this.node = node;
    }

    /**
     * run thread method
     */
    public void run() {
        try {
            while (true) {
                long currTime = System.currentTimeMillis();
                try {
                    Thread.sleep(node.time - currTime);
                } catch (IllegalArgumentException e) {
                    System.out.println(node.message + "____ " + (node.time - currTime));
                    MainAlarm.alarmThread.interrupt();
                    break;
                }
                System.out.println(node.message + " " + node.timeUser);
                node.schedule = true;
                MainAlarm.length--;
                boolean flag = true;
                while (node.next != null) {
                    node = node.next;
                    if (!node.cancel) {
                        MainAlarm.headAlarm = node;
                        flag = false;
                        break;
                    }
                }
                if (flag)
                    MainAlarm.alarmThread.interrupt();
            }
        } catch (InterruptedException ignore) {
        }
    }

}
