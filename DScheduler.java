import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

public class DScheduler {

	/**
	 * A comparable class to represent a requested track number to be read/written from/to disk.
	 */
	public static class RequestedTrackNumber implements Comparable<RequestedTrackNumber>{

		private int trackNumber;

		private int distance;

		private boolean processed;

		public RequestedTrackNumber(int trackNumber) {
			this.trackNumber = trackNumber;
			this.distance = 0;
			this.processed = false;
		}

		public void setDistance(int newDistance) {
			this.distance = newDistance;
		}

		public int distance() {
			return this.distance;
		}

		public void setProcessed() {
			this.processed = true;
		}

		public boolean processed() {
			return this.processed;
		}

		public int trackNumber() {
			return this.trackNumber;
		}

		@Override
		public int compareTo(RequestedTrackNumber o) {
			return Integer.compare(this.trackNumber, o.trackNumber());
		}

	}

	/**
	 * A utility class contains a set of disk scheduling algorithms.
	 */
	public static class Algorithms {

		/**
		 * Implements the First Come First Serve (FCFS) disk scheduling algorithm.
		 *
		 * @param headDirection The current {@link HeadReadDirections}.
		 * @param headLocation The current disk head location.
		 * @param requestedTrackNumbers An array of {@link RequestedTrackNumber}s.
		 */
		public static void FCFS(HeadReadDirections headDirection, int headLocation, RequestedTrackNumber[] requestedTrackNumbers) {
			int totalHeadMovements = 0;
			int currentHeadLocation = headLocation;

		    for (int i = 0; i < requestedTrackNumbers.length; i++) {

		    	// Compute seek distance
		        totalHeadMovements += Math.abs(requestedTrackNumbers[i].trackNumber() - currentHeadLocation);

		        // Move the head to the currently processed track number.
		        currentHeadLocation = requestedTrackNumbers[i].trackNumber();
		    }

		    System.out.print("FCFS: ");
		    for(RequestedTrackNumber requestTrackNumber: requestedTrackNumbers) {
		    	System.out.print(requestTrackNumber.trackNumber() + " ");
		    }
		    System.out.println("Total Head Movement " + totalHeadMovements);
		}

		/**
		 * Implements the Shortest Seek Time First (SSTF) disk scheduling algorithm.
		 *
		 * @param headDirection The current {@link HeadReadDirections}.
		 * @param headLocation The current disk head location.
		 * @param requestedTrackNumbers An array of {@link RequestedTrackNumber}s.
		 */
		public static void SSTF(HeadReadDirections headDirection, int headLocation, RequestedTrackNumber[] requestedTrackNumbers) {
			int totalHeadMovements = 0;
			int currentHeadLocation = headLocation;
	        int[] processedTrackNumbers = new int[requestedTrackNumbers.length];

	        for (int i = 0; i < requestedTrackNumbers.length; i++) {

	        	// Compute seek distance for each non-processed RequestedTrackNumber
	        	for (int j = 0; j < requestedTrackNumbers.length; j++) {
	            	if(!requestedTrackNumbers[j].processed()) {
	            		requestedTrackNumbers[j].setDistance(Math.abs(requestedTrackNumbers[j].trackNumber() - currentHeadLocation));
	            	}
	            }

	        	// Find the minimal seek distance from the currentHeadLocation.
		    	RequestedTrackNumber minimumDistanceTrackNumberRequest = null;
		        int minimum = Integer.MAX_VALUE;
		        for (int k = 0; k < requestedTrackNumbers.length; k++) {
		        	if(!requestedTrackNumbers[k].processed() && minimum > requestedTrackNumbers[k].distance()) {
		        		minimum = requestedTrackNumbers[k].distance();
		        		minimumDistanceTrackNumberRequest = requestedTrackNumbers[k];
		        	}
		        }
		        minimumDistanceTrackNumberRequest.setProcessed();


	            // Accumulate the total head movements.
	            totalHeadMovements += minimumDistanceTrackNumberRequest.distance();

	            // Move the head to the currently processed track number.
	            currentHeadLocation = minimumDistanceTrackNumberRequest.trackNumber();

	            // Store the processing sequence.
	            processedTrackNumbers[i] = currentHeadLocation;
	        }

		    System.out.print("SSTF: ");
		    for(int trackNumber: processedTrackNumbers) {
		    	System.out.print(trackNumber + " ");
		    }
		    System.out.println("Total Head Movement " + totalHeadMovements);
		}

		/**
		 * Implements the LOOK disk scheduling algorithm.
		 *
		 * @param headDirection The current {@link HeadReadDirections}.
		 * @param headLocation The current disk head location.
		 * @param requestedTrackNumbers An array of {@link RequestedTrackNumber}s.
		 */
		public static void LOOK(HeadReadDirections headDirection, int headLocation, RequestedTrackNumber[] requestedTrackNumbers) {
			int totalHeadMovements = 0;
			int currentHeadLocation = headLocation;

			// Track RequestedTrackNumbers in the direction of head read direction.
	        List<RequestedTrackNumber> requestFollowingDirection = new ArrayList<RequestedTrackNumber>();

	        // Track RequestedTrackNumber in the counter-direction of head read direction.
	        List<RequestedTrackNumber> requestFollowingOppositeDirection = new ArrayList<RequestedTrackNumber>();
	        if(HeadReadDirections.DOWN.equals(headDirection)) {
	        	for (int j = 0; j < requestedTrackNumbers.length; j++) {
	        		if(requestedTrackNumbers[j].trackNumber() <= currentHeadLocation) {
	        			requestFollowingDirection.add(requestedTrackNumbers[j]);
	        		} else {
	        			requestFollowingOppositeDirection.add(requestedTrackNumbers[j]);
	        		}
	        	}
	        	Collections.sort(requestFollowingDirection);
	        	Collections.reverse(requestFollowingDirection);
	        	Collections.sort(requestFollowingOppositeDirection);
	        } else if(HeadReadDirections.UP.equals(headDirection)) {
	        	for (int j = 0; j < requestedTrackNumbers.length; j++) {
	        		if(requestedTrackNumbers[j].trackNumber() >= currentHeadLocation) {
	        			requestFollowingDirection.add(requestedTrackNumbers[j]);
	        		} else {
	        			requestFollowingOppositeDirection.add(requestedTrackNumbers[j]);
	        		}
	        	}
	        	Collections.sort(requestFollowingDirection);
	        	Collections.sort(requestFollowingOppositeDirection);
	        	Collections.reverse(requestFollowingOppositeDirection);
	        }

	        for(RequestedTrackNumber requestTrackNumber: requestFollowingDirection) {
	        	totalHeadMovements += Math.abs(requestTrackNumber.trackNumber() - currentHeadLocation);
	        	currentHeadLocation = requestTrackNumber.trackNumber();
	        }

	        for(RequestedTrackNumber requestTrackNumber: requestFollowingOppositeDirection) {
	        	totalHeadMovements += Math.abs(requestTrackNumber.trackNumber() - currentHeadLocation);
	        	currentHeadLocation = requestTrackNumber.trackNumber();
	        }

		    System.out.print("LOOK: ");

	        for(RequestedTrackNumber requestTrackNumber: requestFollowingDirection) {
	        	System.out.print(requestTrackNumber.trackNumber() + " ");
	        }

	        for(RequestedTrackNumber requestTrackNumber: requestFollowingOppositeDirection) {
	        	System.out.print(requestTrackNumber.trackNumber() + " ");
	        }

		    System.out.println("Total Head Movement " + totalHeadMovements);
		}

		/**
		 * Implements the C-LOOK disk scheduling algorithm.
		 *
		 * @param headDirection The current {@link HeadReadDirections}.
		 * @param headLocation The current disk head location.
		 * @param requestedTrackNumbers An array of {@link RequestedTrackNumber}s.
		 */
		public static void CLOOK(HeadReadDirections headDirection, int headLocation, RequestedTrackNumber[] requestedTrackNumbers) {
			int totalHeadMovements = 0;
			int currentHeadLocation = headLocation;

			// Track RequestedTrackNumbers in the direction of head read direction.
	        List<RequestedTrackNumber> requestFollowingDirection = new ArrayList<RequestedTrackNumber>();

	        // Track RequestedTrackNumber in the counter-direction of head read direction.
	        List<RequestedTrackNumber> requestFollowingOppositeDirection = new ArrayList<RequestedTrackNumber>();
	        if(HeadReadDirections.DOWN.equals(headDirection)) {
	        	for (int j = 0; j < requestedTrackNumbers.length; j++) {
	        		if(requestedTrackNumbers[j].trackNumber() <= currentHeadLocation) {
	        			requestFollowingDirection.add(requestedTrackNumbers[j]);
	        		} else {
	        			requestFollowingOppositeDirection.add(requestedTrackNumbers[j]);
	        		}
	        	}
	        	Collections.sort(requestFollowingDirection);
	        	Collections.sort(requestFollowingOppositeDirection);
	        } else if(HeadReadDirections.UP.equals(headDirection)) {
	        	for (int j = 0; j < requestedTrackNumbers.length; j++) {
	        		if(requestedTrackNumbers[j].trackNumber() >= currentHeadLocation) {
	        			requestFollowingDirection.add(requestedTrackNumbers[j]);
	        		} else {
	        			requestFollowingOppositeDirection.add(requestedTrackNumbers[j]);
	        		}
	        	}
	        	Collections.sort(requestFollowingDirection);
	        	Collections.sort(requestFollowingOppositeDirection);
	        }

	        for(RequestedTrackNumber requestTrackNumber: requestFollowingDirection) {
	        	totalHeadMovements += Math.abs(requestTrackNumber.trackNumber() - currentHeadLocation);
	        	currentHeadLocation = requestTrackNumber.trackNumber();
	        }

	        for(RequestedTrackNumber requestTrackNumber: requestFollowingOppositeDirection) {
	        	totalHeadMovements += Math.abs(requestTrackNumber.trackNumber() - currentHeadLocation);
	        	currentHeadLocation = requestTrackNumber.trackNumber();
	        }

		    System.out.print("C-LOOK: ");

	        for(RequestedTrackNumber requestTrackNumber: requestFollowingDirection) {
	        	System.out.print(requestTrackNumber.trackNumber() + " ");
	        }

	        for(RequestedTrackNumber requestTrackNumber: requestFollowingOppositeDirection) {
	        	System.out.print(requestTrackNumber.trackNumber() + " ");
	        }

		    System.out.println("Total Head Movement " + totalHeadMovements);
		}

	}

	/**
	 * An enumeration for current disk head read direction.
	 */
	private enum HeadReadDirections {
		DOWN("down"),
		UP("up");

		private String string;

		private HeadReadDirections(String string) {
			this.string = string;
		}

		public static HeadReadDirections fromString(String string) {
			for(HeadReadDirections direction: HeadReadDirections.values()) {
				if(direction.string.equals(string)) {
					return direction;
				}
			}
			throw new IllegalArgumentException("Invalid enumeration requested: " + string);
		}

	};

	public static void main(String[] args) {
		Scanner scanner = new Scanner(System.in);
		String inputLine = "";
		while(!(inputLine = scanner.nextLine()).isEmpty()) {
			String[] parts = inputLine.trim().split(" ");
			int requestsCount = Integer.parseInt(parts[0].trim());
			int headLocation = Integer.parseInt(parts[1].trim());
			HeadReadDirections headReadDirection = HeadReadDirections.fromString(parts[2].trim());
			RequestedTrackNumber[] requestedTrackNumbers = new RequestedTrackNumber[requestsCount];
			for(int i = 0; i < requestsCount; i++) {
				requestedTrackNumbers[i] = new RequestedTrackNumber(Integer.parseInt(parts[3 + i].trim()));
			}
			Algorithms.FCFS(headReadDirection, headLocation, requestedTrackNumbers);
			Algorithms.SSTF(headReadDirection, headLocation, requestedTrackNumbers);
			Algorithms.LOOK(headReadDirection, headLocation, requestedTrackNumbers);
			Algorithms.CLOOK(headReadDirection, headLocation, requestedTrackNumbers);
		}
		scanner.close();
	}

}
