//Yousef EL-Qawasmi
//190615960
import java.util.*;

/**
 * A class to represents Process scheduler.
 */
public class Dispatcher {
	
	/**
	 * The ready queue for our scheduler.
	 */
	private Queue<Process> readyQueue;
	
	/**
	 * A mapping from a process-id to the {@link Process} to efficient querying.
	 */
	private Map<Integer, Process> processIdToProcessMap;
	
	/**
	 * A mapping between a {@link Resource} and the corresponding list of {@link Process}s requesting the resource.
	 */
	private EnumMap<Resource, List<Process>> resourceToProcessMap;
	
	/**
	 * The master {@link Process} with id 0.
	 */
	private Process systemIdleProcess;
	
	/**
	 * A pointer to the currently running process on our scheduler.
	 */
    private Process currentlyRunningProcess;
    
    /**
     * Construsts a new instance of {@link Dispatcher}.
     */
    public Dispatcher() {
    	this.readyQueue = new LinkedList<Process>();
    	this.processIdToProcessMap = new HashMap<Integer, Process>();
    	this.resourceToProcessMap = new EnumMap<Resource, List<Process>>(Resource.class);
    	
    	// We have three resources in our system.
    	this.resourceToProcessMap.put(Resource.RESOURCE_1, new ArrayList<Process>());
    	this.resourceToProcessMap.put(Resource.RESOURCE_2, new ArrayList<Process>());
    	this.resourceToProcessMap.put(Resource.RESOURCE_3, new ArrayList<Process>());
    	
    	// The master process is the one starts running at time 0.
    	this.systemIdleProcess = new Process(0, ProcessState.RUNNING, 0);
    	this.processIdToProcessMap.put(this.systemIdleProcess.id(), this.systemIdleProcess);
    	this.currentlyRunningProcess = this.systemIdleProcess;
	}

    /**
     * The scheduling operations.
     */
    void dispatcher() {
        Scanner scanner = new Scanner(System.in);
        String inputLine = null;
        while (!((inputLine = scanner.nextLine().trim()).equals(""))) {
            String[] parts = inputLine.split(" ");
            
        	if(inputLine.isEmpty() || parts.length < 2) {
        		break;
        	}
            
            // The first field is about the time in milliseconds.
            int currentEventTime = Integer.parseInt(parts[0]);
            
            // The second field is about the event.
            char event = parts[1].charAt(0);
            
            int processId = 0;
            Resource resource = null;
            if(event == 'R' || event == 'I') {
            	int resourceId = Integer.parseInt(parts[2]);
            	switch(resourceId) {
            	case 1:
            		resource = Resource.RESOURCE_1;
            		break;
            	case 2:
            		resource = Resource.RESOURCE_2;
            		break;
            	case 3:
            		resource = Resource.RESOURCE_3;
            		break;
            	default:
            		break;
            	}
            	processId = Integer.parseInt(parts[3]);
            } else if(event != 'T') {
            	processId = Integer.parseInt(parts[2]);
            }
            
            switch(event) {
            case 'C': { // Create a new process.
            	Process newProcess = new Process(processId, ProcessState.READY, currentEventTime);
            	this.processIdToProcessMap.put(newProcess.id(), newProcess);
            	//  if process 0 is running and new process is created, or as the result of an event one of the
            	// blocked processes becomes ready (unblocked), this process will get CPU immediately.
        		if(this.currentlyRunningProcess.equals(this.systemIdleProcess)) {
        			this.systemIdleProcess.setState(ProcessState.READY, currentEventTime);
        			this.currentlyRunningProcess = newProcess;
        			newProcess.setState(ProcessState.RUNNING, currentEventTime);
        		} else {
        			this.readyQueue.add(newProcess);
        		}
            	break;
            }
            case 'E': { // Exit the process.
            	Process toExitProcess = this.processIdToProcessMap.get(processId);
            	toExitProcess.setState(ProcessState.EXITED, currentEventTime);
            	this.readyQueue.remove(toExitProcess);
            	if(this.currentlyRunningProcess.equals(toExitProcess)) {
            		this.currentlyRunningProcess = null;
            	}
            	this.runNextProcess(currentEventTime);
            	break;
            }
            case 'R': { // Request a resource
        		Process requestingResourceProcess = this.processIdToProcessMap.get(processId);
        		requestingResourceProcess.setState(ProcessState.BLOCKED, currentEventTime);
        		this.readyQueue.remove(requestingResourceProcess);
        		this.currentlyRunningProcess = null;
        		switch(resource) {
        		case RESOURCE_1:
        			this.resourceToProcessMap.get(Resource.RESOURCE_1).add(requestingResourceProcess);
        			break;
        		case RESOURCE_2:
        			this.resourceToProcessMap.get(Resource.RESOURCE_2).add(requestingResourceProcess);
        			break;
        		case RESOURCE_3:
        			this.resourceToProcessMap.get(Resource.RESOURCE_3).add(requestingResourceProcess);
        			break;
        		default:
        			break;
        		}
        		this.runNextProcess(currentEventTime);
            	break;
            }
            case 'I': {
            	Process resourceAccomplishedProcess = this.processIdToProcessMap.get(processId);
            	resourceAccomplishedProcess.setState(ProcessState.READY, currentEventTime);
            	
        		switch(resource) {
        		case RESOURCE_1:
        			this.resourceToProcessMap.get(Resource.RESOURCE_1).remove(resourceAccomplishedProcess);
        			break;
        		case RESOURCE_2:
        			this.resourceToProcessMap.get(Resource.RESOURCE_2).remove(resourceAccomplishedProcess);
        			break;
        		case RESOURCE_3:
        			this.resourceToProcessMap.get(Resource.RESOURCE_3).remove(resourceAccomplishedProcess);
        			break;
        		default:
        			break;
        		}
        		
            	//  if process 0 is running and new process is created, or as the result of an event one of the
            	// blocked processes becomes ready (unblocked), this process will get CPU immediately.
        		if(this.currentlyRunningProcess.equals(this.systemIdleProcess)) {
        			this.systemIdleProcess.setState(ProcessState.READY, currentEventTime);
        			this.currentlyRunningProcess = resourceAccomplishedProcess;
        			resourceAccomplishedProcess.setState(ProcessState.RUNNING, currentEventTime);
        		} else {
        			this.readyQueue.add(resourceAccomplishedProcess);
        		}

            	break;
            }
            case 'T': {
            	this.preemptProcess(currentEventTime);
                break;
            }
            default:
                break;            
            }
        }
        scanner.close();
        
        // Print the results.
        for(Process process: processIdToProcessMap.values()) {
        	if(process.id() == 0) {
        		System.out.format("%d %d\n", process.id(), process.runningTime());
        	} else {
        		System.out.format("%d %d %d %d\n", process.id(), process.runningTime(), process.readyTime(), process.blockTime());
        	}
        }
    }
    
    private void preemptProcess(int currentEventTime) {
		if(this.currentlyRunningProcess.equals(this.systemIdleProcess)) {
			if(this.readyQueue.isEmpty()) {
    			this.currentlyRunningProcess = this.systemIdleProcess;
    			this.systemIdleProcess.setState(ProcessState.RUNNING, currentEventTime);
			} else {
				this.systemIdleProcess.setState(ProcessState.READY, currentEventTime);
				
    			Process toRunProcess = this.readyQueue.remove();
    			this.currentlyRunningProcess = toRunProcess;
    			toRunProcess.setState(ProcessState.RUNNING, currentEventTime);
			}
		} else {
			if(this.readyQueue.isEmpty()) {
				if(ProcessState.EXITED.equals(this.currentlyRunningProcess.state()) || ProcessState.BLOCKED.equals(this.currentlyRunningProcess.state())) {
        			this.currentlyRunningProcess = this.systemIdleProcess;
        			this.systemIdleProcess.setState(ProcessState.RUNNING, currentEventTime); 
				} else {
					this.currentlyRunningProcess.setState(ProcessState.RUNNING, currentEventTime);
				}
			} else {
				this.currentlyRunningProcess.setState(ProcessState.READY, currentEventTime);
    			Process toRunProcess = this.readyQueue.remove();
    			this.readyQueue.add(this.currentlyRunningProcess);
    			this.currentlyRunningProcess = toRunProcess;
    			toRunProcess.setState(ProcessState.RUNNING, currentEventTime);
			}
		}
    }
    
    private void runNextProcess(int currentEventTime) {
    	if(this.currentlyRunningProcess == null) {
    		if(this.readyQueue.isEmpty()) {
    			this.currentlyRunningProcess = this.systemIdleProcess;
    			this.systemIdleProcess.setState(ProcessState.RUNNING, currentEventTime);
    		} else {
    			Process toRunProcess = this.readyQueue.remove();
    			this.currentlyRunningProcess = toRunProcess;
    			toRunProcess.setState(ProcessState.RUNNING, currentEventTime);
    		}
    	} else {
    		if(this.currentlyRunningProcess.equals(this.systemIdleProcess)) {
    			if(this.readyQueue.isEmpty()) {
        			this.currentlyRunningProcess = this.systemIdleProcess;
        			this.systemIdleProcess.setState(ProcessState.RUNNING, currentEventTime);
    			} else {
    				this.systemIdleProcess.setState(ProcessState.READY, currentEventTime);
    				
        			Process toRunProcess = this.readyQueue.remove();
        			this.currentlyRunningProcess = toRunProcess;
        			toRunProcess.setState(ProcessState.RUNNING, currentEventTime);
    			}
    		} else {
    			if(this.readyQueue.isEmpty()) {
    				if(ProcessState.EXITED.equals(this.currentlyRunningProcess.state()) || ProcessState.BLOCKED.equals(this.currentlyRunningProcess.state())) {
            			this.currentlyRunningProcess = this.systemIdleProcess;
            			this.systemIdleProcess.setState(ProcessState.RUNNING, currentEventTime); 
    				} else {
    					this.currentlyRunningProcess.setState(ProcessState.RUNNING, currentEventTime);
    				}
    			} else {
    				this.currentlyRunningProcess.setState(ProcessState.READY, currentEventTime);
        			Process toRunProcess = this.readyQueue.remove();
        			this.readyQueue.add(this.currentlyRunningProcess);
        			this.currentlyRunningProcess = toRunProcess;
        			toRunProcess.setState(ProcessState.RUNNING, currentEventTime);
    			}
    		}
    	}
    }
    
    /**
     * The dispatcher driver.
     */
    public static void main(String[] args) {
        Dispatcher test = new Dispatcher();
        test.dispatcher();
    }
    
    /**
     * An enumeration class to represent the available resources to the system.
     */
    public enum Resource {
    	RESOURCE_1,
    	RESOURCE_2,
    	RESOURCE_3;
    }
    
    /** 
     * An enumeration class to represent process states.
     */
    public enum ProcessState {
    	READY,
    	RUNNING,
    	EXITED,
    	BLOCKED;
    }
    
    /**
     * Class to represent a Process.
     */
    public static class Process {
    	
        private int id;
        private ProcessState state;
        private int runningTime;
        private int readyTime;
        private int blockTime;
        
        private int lastTimeEvent;
        
        public Process(int id, ProcessState state, int lastTimeEvent) {
        	this.id = id;
        	this.state = state;
        	this.runningTime = 0;
        	this.readyTime = 0;
        	this.blockTime = 0;
        	this.lastTimeEvent = lastTimeEvent;
    	}
        
        public void setState(ProcessState newState, int timeEvent) {
        	int increaseAmount = timeEvent - this.lastTimeEvent;
        	switch(this.state) {
        	case READY:
        		this.readyTime += increaseAmount;
        		break;
        	case RUNNING:
        		this.runningTime += increaseAmount;
        		break;
        	case BLOCKED:
        		this.blockTime += increaseAmount;
        		break;
    		default:
    			break;
        	}
        	this.state = newState;
        	this.lastTimeEvent = timeEvent;
        }
        
        public int id() {
        	return this.id;
        }
        
        public ProcessState state() {
        	return this.state;
        }
        
        public int runningTime() {
        	return this.runningTime;
        }
        
        public int readyTime() {
        	return this.readyTime;
        }
        
        public int blockTime() {
        	return this.blockTime;
        }

    	@Override
    	public int hashCode() {
    		final int prime = 31;
    		int result = 1;
    		result = prime * result + id;
    		return result;
    	}

    	@Override
    	public boolean equals(Object obj) {
    		if (this == obj)
    			return true;
    		if (obj == null)
    			return false;
    		if (getClass() != obj.getClass())
    			return false;
    		Process other = (Process) obj;
    		if (id != other.id)
    			return false;
    		return true;
    	}

    	@Override
    	public String toString() {
    		return "Process [" + id + "]";
    	}
        
    }
    
}