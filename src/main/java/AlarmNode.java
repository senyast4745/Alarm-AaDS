
class AlarmNode {

    long time; //time in UNIX format
    String timeUser; //time as string
    String message; //message of alarm
    AlarmNode next = null;  //pointer to next node
    boolean cancel = false; //bool flag. Mark as canceled
    boolean schedule = false; //bool flag. Mark as scheduled

    /**
    * constructor of data class - Alarm-node
    *
    * @param time - time, when thread must wake up
    * @param message - a message that will display the alarm when he wakes up
    * @param timeUser - time in string format that is displayed
    * */
    AlarmNode(long time, String message, String timeUser) {
        this.timeUser = timeUser;
        this.time = time;
        this.message = message;
    }
}
