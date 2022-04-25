//Yousef EL-Qawasmi
//190615960

import java .util.*;

public class Dispatcher{
    
    // Track the time
    private Integer timer;

    // Track the ready processes using their IDs. The running process is on top of the queue.
    private Queue<Integer> readyQueue = new LinkedList<>();

    // Map resource number to a process numbers (ex: R3 -> P1). Multiple process can request a single resource.
    private HashMap<Integer,HashSet<Integer>> blocked = new HashMap<>();

    // History of all the processes. This will include all the updates done on each process.
    private HashMap<Integer,Process> processes = new HashMap<>();

    class Process {
    Process(int id) {
        this.id = id;
        runningTime = 0;
        blockedTime = 0;
        readyTime = 0;
    }

    int id;
    int runningTime;
    int blockedTime;
    int readyTime;
    }
    
    public void input() throws Exception{
        Scanner scanS = new Scanner(System.in);
        String input = scanS.nextLine();
        while(!(input.isEmpty())){
            processInput(input);
            input = scanS.nextLine();
        }
        processInput(input);
    }

    // Dispatcher should initially have idle process 0, independent of process queue
    void Dispatcher() throws Exception {
        processes.put(0, new Process(0));
        timer = 0;
        input();
    }
    

    public void processInput(String input) throws Exception {
        // New line, end of program
        if (input.isEmpty()) {
            printProcessStatistics();
        }

        // Otherwise, process the event
        else {
            String[] _split = input.split(" ");

            int newTime = Integer.parseInt(_split[0]);
            char event = _split[1].charAt(0);

            updateProcessTimes(newTime);

            if (event == 'C') {
                int processId = Integer.parseInt(_split[2]);
                create(processId);
            }

            else if (event == 'E') {
                int processId = Integer.parseInt(_split[2]);
                exit(processId);
            }

            else if (event == 'R') {
                int processId = Integer.parseInt(_split[3]);
                int resourceId = Integer.parseInt(_split[2]);
                requestResource(processId, resourceId);
            }

            else if (event == 'I') {
                int processId = Integer.parseInt(_split[3]);
                int resourceId = Integer.parseInt(_split[2]);
                interruptResource(resourceId, processId);
            }

            else if (event == 'T') {
                timerInterrupt();
            }
        }
    }

    // Update process stats
    private void updateProcessTimes(int newTime) {
        int timePassed = newTime - timer;
        timer = newTime;

        // Update running times
        if (readyQueue.isEmpty()) {
            processes.get(0).runningTime += timePassed;
        }

        else {
            int runningProcessId = readyQueue.peek();
            processes.get(runningProcessId).runningTime += timePassed;
        }

        // Update blocked times
        for (HashSet<Integer> blockedProcesses : blocked.values()) {
            for (Integer processId : blockedProcesses) {
                processes.get(processId).blockedTime += timePassed;
            }
        }

        // Update ready times
        if (!readyQueue.isEmpty()) {
            int runningProcessId = readyQueue.peek();

            for (Integer processId : readyQueue) {
                if(processId != runningProcessId) {
                    processes.get(processId).readyTime += timePassed;
                }
            }
        }
    }

    // Create event should create a new process and add it to the ready queue.
    private void create(int id) {
        processes.put(id, new Process(id));
        readyQueue.add(id);
    }

    // Exit event should remove the process from the ready queue, assuming it is running
    private void exit(int id) throws Exception {
        readyQueue.poll();
    }

    // Request event should allocate the resource to the process, assuming it is running. The process will be blocked
    private void requestResource(int processId, int resourceId) throws Exception {
        readyQueue.poll();

        if(!blocked.containsKey(resourceId)) {
            blocked.put(resourceId, new HashSet<>());
        }

        blocked.get(resourceId).add(processId);
    }

    // Resource nterrupt event should unblock the process and put in the ready queue
    private void interruptResource(int resourceId, int processId) {
        blocked.get(resourceId).remove(processId);
        readyQueue.add(processId);
    }

    // Timer interrupt event should stop the running process and re-queue it into ready state
    private void timerInterrupt() {
        int process = readyQueue.poll();
        readyQueue.add(process);
    }

    // Print out stats
    private void printProcessStatistics() {
        for (Process process : processes.values()) {
            if(process.id == 0){
                System.out.println(process.id + " " + process.runningTime );
            }
            else{
                System.out.println(process.id + " " + process.runningTime + " " + process.readyTime + " " + process.blockedTime);
            }
        }
    }



    

// Class to test my dispatcher.
    public static void main(String[] args) throws Exception {
        Dispatcher test = new Dispatcher();
        test.Dispatcher();
    }
}