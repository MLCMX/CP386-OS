//Yousef EL-Qawasmi
//190615960
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class Detector {
	
	public static boolean DEBUG_ENABLED = true;
	private int[][] resourceRequestMatrix;
	private int[][] resourceAllocationMatrix;
	private int[] unallocatedResourceVector;
	private int processCount;
	private int resourcesCount;
	private int[] resourcesCapacity;
	//private Set<Integer> previousCompletedProcessesList;
	//private Set<Integer> completedProcessesList;
	
        private ArrayList <Integer> previousCompletedProcessesList;//used arraylist to keep the completed processsed in order 
	private ArrayList <Integer> completedProcessesList;
        
	public Detector(int processCount, int[] resourceCapacity) {
		this.processCount = processCount;
		this.resourcesCount = resourceCapacity.length;
		this.resourcesCapacity = resourceCapacity;
		
		this.resourceRequestMatrix = new int[this.processCount][this.resourcesCount];
		this.zero(this.resourceRequestMatrix);
		
		this.resourceAllocationMatrix = new int[this.processCount][this.resourcesCount];
		this.zero(this.resourceAllocationMatrix);
		
		this.unallocatedResourceVector = new int[this.resourcesCount];
		for(int i = 0; i < this.resourcesCapacity.length; i++) {
			this.unallocatedResourceVector[i] = this.resourcesCapacity[i];
		}
		//this.completedProcessesList = new HashSet<Integer>();
		//this.previousCompletedProcessesList = new HashSet<Integer>();
                this.completedProcessesList = new ArrayList <Integer>();
                this.previousCompletedProcessesList = new ArrayList <Integer>();
	}
	
	private void zero(int[][] matrix) {
		for(int[] row: matrix) {
			Arrays.fill(row, 0);
		}
	}
	//print statement for testing 
	private void printRow(int[] row) {
		for(int i = 0; i < row.length; i++) {
			System.out.print(row[i] + " ");
		}
		System.out.println();
	}
	//print statement for testing 
	private void printMatrix(int[][] matrix) {
		for(int[] row: matrix) {
			this.printRow(row);
		}
	}
	//print for testing
	public void printCurrentStatus() {
		System.out.println("=======================");
		this.printMatrix(this.resourceRequestMatrix);
		this.printMatrix(this.resourceAllocationMatrix);
		this.printRow(this.unallocatedResourceVector);
		System.out.println("=======================");
	}
	
	public void request(int processId, int requestedResourceId) {
		// Decrement by 1 to match row/matrix indices at it is 0-based.
		processId--;
		requestedResourceId--;
		if(this.unallocatedResourceVector[requestedResourceId] > 0) {
			this.resourceAllocationMatrix[processId][requestedResourceId]++;
			this.unallocatedResourceVector[requestedResourceId]--;
		} else if(this.unallocatedResourceVector[requestedResourceId] == 0) {
			this.resourceRequestMatrix[processId][requestedResourceId]++;
		} else {
			throw new IllegalStateException("Not reachable!");
		}
	}
	
	public boolean checkAvailability(int processId, int requestedResourceId) {
		if(this.unallocatedResourceVector[requestedResourceId] > 0) {
			return true;
		} else if(this.unallocatedResourceVector[requestedResourceId] == 0) {
			return false;
		} else {
			throw new IllegalStateException("Not reachable!");
		}
	}
	
	public void releaseResources() {
		int processId = -1;
		for(int[] resourceRequestRow: this.resourceRequestMatrix) {
			processId++;
			if(this.blockedProcess(resourceRequestRow) || this.completedProcessesList.contains(processId)) {
				continue;
			}
			// process is not blocked, release allocated resources.
			//System.out.println("Process Completed: " + (processId + 1));
			this.completedProcessesList.add(processId);
			for(int i = 0; i < this.resourceAllocationMatrix[processId].length; i++) {
				if(this.resourceAllocationMatrix[processId][i] > 0) {
					this.unallocatedResourceVector[i] += this.resourceAllocationMatrix[processId][i];
					this.resourceAllocationMatrix[processId][i] = 0;
				}
			}
                        //System.out.println(this.completedProcessesList);
		}
	}
	
	private boolean blockedProcess(int[] resourceRequestVector) {
		for(int resource: resourceRequestVector) {
			if(resource == 1) {
				return true;
			}
		}
		return false;
	}
	
	private boolean completedProcesses() {
		return this.completedProcessesList.size() == this.processCount;
	}
	
	public void processPendingRequests() {
		int processId = -1;
		for(int[] pendingRequestRow: this.resourceRequestMatrix) {
			processId++;
			for(int i = 0; i < pendingRequestRow.length; i++) {
				for(int j = 0; j < pendingRequestRow[i]; j++) {
					if(this.checkAvailability(processId, i)) {
						this.request(processId + 1, i + 1);
						this.resourceRequestMatrix[processId][i]--;
					}
				}
			}
		}
	}
	//if theres no deadlock print statement
	public void printSimulationResult() {
		System.out.println("No deadlock, completion order");
		for(int processId: this.completedProcessesList) {
			System.out.print((processId + 1) + " ");
		}
		System.out.println();
	}
	//if theres a deadlock print statement
	public void printDeadlockResult() {
		System.out.println("Deadlock, processes involved are");
		for(int i = 0; i < this.processCount; i++) {
			if(!this.completedProcessesList.contains(i)) {
				System.out.print((i + 1) + " ");
			}
		}
		System.out.println();
	}
	
	public boolean checkDeadlock() {
		if(this.completedProcessesList.equals(this.previousCompletedProcessesList)) {
			return true;
		}
		this.previousCompletedProcessesList.clear();
		this.previousCompletedProcessesList.addAll(this.completedProcessesList);
		return false;
	}
	
	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		String inputLine = "";
		boolean headerProcessed = false;
		List<int[]> commands = new ArrayList<int[]>();
		int[] resourcesVector = null;
		Set<Integer> processes = new HashSet<Integer>();
		while(!(inputLine = scanner.nextLine()).isEmpty()) {
			if(!headerProcessed) {
				String[] resourcesVectorString = inputLine.trim().split(" ");
				resourcesVector = new int[resourcesVectorString.length];
				for(int i = 0; i < resourcesVectorString.length; i++) {
					resourcesVector[i] = Integer.parseInt(resourcesVectorString[i]);
				}
				headerProcessed = true;
			} else {
				String[] processRequestCommand = inputLine.trim().split(" ");
				int processId = Integer.parseInt(processRequestCommand[1]);
				processes.add(processId);
				commands.add(new int[] {processId, Integer.parseInt(processRequestCommand[4])});
			}
		}
		scanner.close();
		Detector deadlock = new Detector(processes.size(), resourcesVector);
		for(int[] row: commands) {
			deadlock.request(row[0], row[1]);
		}
                
		//deadlock.printCurrentStatus();
		while(true) {
			deadlock.releaseResources();
			//deadlock.printCurrentStatus();
			if(deadlock.completedProcesses()) {
				deadlock.printSimulationResult();
				break;
			}
			if(deadlock.checkDeadlock()) {
				deadlock.printDeadlockResult();
				break;
			}
			deadlock.processPendingRequests();
		}
	}
}
