import java.util.*;

public class BrooklynTechNavigation {
    
    // Constants for building layout
    private static final int BASEMENT = 0;
    private static final int MAX_FLOOR = 8;
    private static final double WALKING_SPEED_FEET_PER_MINUTE = 350; // ~4 mph (faster student pace)
    private static final double FEET_PER_ROOM = 30; // Estimated distance between adjacent rooms
    private static final double STAIR_TIME_PER_FLOOR = 0.5; // Minutes to go up/down one floor
    
    // Room coordinates (x, y) for pathfinding
    private static class Coordinate {
        int x, y;
        Coordinate(int x, int y) { this.x = x; this.y = y; }
        
        double distanceTo(Coordinate other) {
            return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Coordinate that = (Coordinate) obj;
            return x == that.x && y == that.y;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y);
        }
    }
    
    // Building layout data
    private static Map<String, Coordinate> roomCoordinates = new HashMap<>();
    private static Map<Integer, Set<String>> floorRooms = new HashMap<>();
    private static Map<String, String> specialAreas = new HashMap<>();
    
    static {
        initializeBuildingLayout();
    }
    
    private static void initializeBuildingLayout() {
        // Initialize room coordinates and floor mappings
        for (int floor = BASEMENT; floor <= MAX_FLOOR; floor++) {
            floorRooms.put(floor, new HashSet<>());
            
            if (floor == BASEMENT) {
                // Basement: Only North and West sides
                addNorthRooms(floor);
                addWestRooms(floor);
                addSpecialArea(floor + "Pool", "Pool", 15, 5);
            } else if (floor == 1) {
                // Floor 1: Normal except gym in south, auditorium entrance in center
                addNorthRooms(floor);
                addEastRooms(floor);
                addWestRooms(floor);
                addSpecialArea(floor + "Gym", "Main Gym", 15, 5);
                addSpecialArea(floor + "Auditorium", "Auditorium", 12, 12);
            } else if (floor == 2 || floor == 4 || floor == 6) {
                // Completely normal floors
                addAllStandardRooms(floor);
            } else if (floor == 3) {
                // Floor 3: Normal except south side is locker room
                addNorthRooms(floor);
                addEastRooms(floor);
                addWestRooms(floor);
                addCenterRooms(floor);
                addSpecialArea(floor + "LockerRoom", "Locker Room", 15, 5);
            } else if (floor == 5) {
                // Floor 5: Normal except center is library (accessible from west)
                addNorthRooms(floor);
                addSouthRooms(floor);
                addEastRooms(floor);
                addWestRooms(floor);
                addSpecialArea(floor + "Library", "Library", 12, 12);
            } else if (floor == 7) {
                // Floor 7: Entirely cafeteria (accessible from corners)
                addSpecialArea(floor + "Cafeteria", "Cafeteria", 15, 15);
            } else if (floor == 8) {
                // Floor 8: Normal except gym in center
                addNorthRooms(floor);
                addSouthRooms(floor);
                addEastRooms(floor);
                addWestRooms(floor);
                addSpecialArea(floor + "Gym", "Upper Gym", 12, 12);
            }
        }
    }
    
    private static void addNorthRooms(int floor) {
        for (int room = 1; room <= 9; room++) {
            String roomName = floor + "N" + room;
            roomCoordinates.put(roomName, new Coordinate(room * 2, 20));
            floorRooms.get(floor).add(roomName);
        }
    }
    
    private static void addSouthRooms(int floor) {
        for (int room = 1; room <= 9; room++) {
            String roomName = floor + "S" + room;
            // S1 is in SE corner, S9 in SW corner
            roomCoordinates.put(roomName, new Coordinate(20 - (room - 1) * 2, 5));
            floorRooms.get(floor).add(roomName);
        }
    }
    
    private static void addEastRooms(int floor) {
        for (int room = 1; room <= 24; room++) {
            String roomName = floor + "E" + room;
            // E24 starts in NE corner, E1 in SE corner
            roomCoordinates.put(roomName, new Coordinate(20, 20 - room + 1));
            floorRooms.get(floor).add(roomName);
        }
    }
    
    private static void addWestRooms(int floor) {
        for (int room = 1; room <= 24; room++) {
            String roomName = floor + "W" + room;
            // W1 in NW corner, W24 in SW corner
            roomCoordinates.put(roomName, new Coordinate(2, 20 - room + 1));
            floorRooms.get(floor).add(roomName);
        }
    }
    
    private static void addCenterRooms(int floor) {
        for (int room = 1; room <= 8; room++) {
            String roomName = floor + "C" + room;
            roomCoordinates.put(roomName, new Coordinate(8 + room, 12));
            floorRooms.get(floor).add(roomName);
        }
    }
    
    private static void addAllStandardRooms(int floor) {
        addNorthRooms(floor);
        addSouthRooms(floor);
        addEastRooms(floor);
        addWestRooms(floor);
        addCenterRooms(floor);
    }
    
    private static void addSpecialArea(String key, String displayName, int x, int y) {
        roomCoordinates.put(key, new Coordinate(x, y));
        specialAreas.put(displayName.toLowerCase(), key);
        int floor = Integer.parseInt(key.substring(0, 1));
        floorRooms.get(floor).add(key);
    }
    
    // Staircase locations (corner and middle staircases)
    private static final Coordinate[] CORNER_STAIRS = {
        new Coordinate(2, 20),   // NW corner
        new Coordinate(20, 20),  // NE corner
        new Coordinate(20, 5),   // SE corner
        new Coordinate(2, 5)     // SW corner
    };
    
    private static final Coordinate[] MIDDLE_STAIRS = {
        new Coordinate(2, 12),   // West middle (near W12)
        new Coordinate(20, 12)   // East middle (near E12)
    };
    
    public static class NavigationResult {
        private String directions;
        private double estimatedTime;
        
        public NavigationResult(String directions, double estimatedTime) {
            this.directions = directions;
            this.estimatedTime = estimatedTime;
        }
        
        public String getDirections() { return directions; }
        public double getEstimatedTime() { return estimatedTime; }
    }
    
    public static NavigationResult getDirections(String fromRoom, String toRoom) {
        // Normalize input and handle special areas
        String normalizedFrom = normalizeRoomInput(fromRoom);
        String normalizedTo = normalizeRoomInput(toRoom);
        
        if (normalizedFrom == null) {
            return new NavigationResult("Error: Starting room '" + fromRoom + "' not found.", 0);
        }
        if (normalizedTo == null) {
            return new NavigationResult("Error: Destination room '" + toRoom + "' not found.", 0);
        }
        
        if (normalizedFrom.equals(normalizedTo)) {
            return new NavigationResult("You are already at your destination!", 0);
        }
        
        return calculateRoute(normalizedFrom, normalizedTo);
    }
    
    private static String normalizeRoomInput(String input) {
        input = input.trim().toLowerCase();
        
        // Check if it's a special area
        if (specialAreas.containsKey(input)) {
            return specialAreas.get(input);
        }
        
        // Check if it's already a properly formatted room
        for (String room : roomCoordinates.keySet()) {
            if (room.toLowerCase().equals(input.toUpperCase())) {
                return room.toUpperCase();
            }
        }
        
        // Try to parse as a room number (e.g., "4N5" or "4n5")
        input = input.toUpperCase();
        if (roomCoordinates.containsKey(input)) {
            return input;
        }
        
        return null;
    }
    
    private static NavigationResult calculateRoute(String from, String to) {
        Coordinate fromCoord = roomCoordinates.get(from);
        Coordinate toCoord = roomCoordinates.get(to);
        
        int fromFloor = getFloorFromRoom(from);
        int toFloor = getFloorFromRoom(to);
        
        StringBuilder directions = new StringBuilder();
        double totalTime = 0;
        
        directions.append("Navigation from ").append(getRoomDisplayName(from))
                 .append(" to ").append(getRoomDisplayName(to)).append(":\n\n");
        
        if (fromFloor == toFloor) {
            // Same floor navigation
            double distance = fromCoord.distanceTo(toCoord) * FEET_PER_ROOM;
            totalTime = distance / WALKING_SPEED_FEET_PER_MINUTE;
            
            directions.append("1. Walk directly to ").append(getRoomDisplayName(to));
            directions.append(getDirectionalGuidance(fromCoord, toCoord));
        } else {
            // Multi-floor navigation
            Coordinate bestStair = findBestStaircase(fromCoord, toCoord, fromFloor, toFloor);
            
            // Walk to staircase
            double distanceToStair = fromCoord.distanceTo(bestStair) * FEET_PER_ROOM;
            totalTime += distanceToStair / WALKING_SPEED_FEET_PER_MINUTE;
            
            directions.append("1. Walk to the staircase");
            directions.append(getDirectionalGuidance(fromCoord, bestStair));
            directions.append("\n");
            
            // Use staircase
            int floorDifference = Math.abs(toFloor - fromFloor);
            totalTime += floorDifference * STAIR_TIME_PER_FLOOR;
            
            directions.append("2. Take the stairs ");
            directions.append(toFloor > fromFloor ? "UP" : "DOWN");
            directions.append(" ").append(floorDifference).append(" floor");
            if (floorDifference > 1) directions.append("s");
            directions.append(" to floor ").append(toFloor).append("\n");
            
            // Walk to destination
            double distanceFromStair = bestStair.distanceTo(toCoord) * FEET_PER_ROOM;
            totalTime += distanceFromStair / WALKING_SPEED_FEET_PER_MINUTE;
            
            directions.append("3. Walk to ").append(getRoomDisplayName(to));
            directions.append(getDirectionalGuidance(bestStair, toCoord));
            
            // Add special access instructions
            String specialInstructions = getSpecialAccessInstructions(to);
            if (!specialInstructions.isEmpty()) {
                directions.append("\n4. ").append(specialInstructions);
            }
        }
        
        directions.append("\n\nEstimated walking time: ");
        directions.append(String.format("%.1f", totalTime)).append(" minutes");
        
        return new NavigationResult(directions.toString(), totalTime);
    }
    
    private static Coordinate findBestStaircase(Coordinate from, Coordinate to, int fromFloor, int toFloor) {
        List<Coordinate> availableStairs = new ArrayList<>();
        
        // Corner staircases are always available
        availableStairs.addAll(Arrays.asList(CORNER_STAIRS));
        
        // Middle staircases are available if not going to/from floor 7
        if (fromFloor != 7 && toFloor != 7) {
            availableStairs.addAll(Arrays.asList(MIDDLE_STAIRS));
        }
        
        // Find the staircase that minimizes total walking distance
        Coordinate bestStair = availableStairs.get(0);
        double minTotalDistance = from.distanceTo(bestStair) + bestStair.distanceTo(to);
        
        for (Coordinate stair : availableStairs) {
            double totalDistance = from.distanceTo(stair) + stair.distanceTo(to);
            if (totalDistance < minTotalDistance) {
                minTotalDistance = totalDistance;
                bestStair = stair;
            }
        }
        
        return bestStair;
    }
    
    private static String getDirectionalGuidance(Coordinate from, Coordinate to) {
        if (from.equals(to)) return "";
        
        StringBuilder guidance = new StringBuilder(" (");
        
        if (to.y > from.y) guidance.append("head NORTH");
        else if (to.y < from.y) guidance.append("head SOUTH");
        
        if (to.x > from.x) {
            if (guidance.length() > 2) guidance.append(", then ");
            guidance.append("head EAST");
        } else if (to.x < from.x) {
            if (guidance.length() > 2) guidance.append(", then ");
            guidance.append("head WEST");
        }
        
        guidance.append(")");
        return guidance.toString();
    }
    
    private static String getSpecialAccessInstructions(String room) {
        if (room.contains("Library")) {
            return "Enter the Library from the center of the west hallway.";
        } else if (room.contains("Cafeteria")) {
            return "Enter the Cafeteria from one of the four corner entrances.";
        }
        return "";
    }
    
    private static int getFloorFromRoom(String room) {
        if (room.matches("\\d.*")) {
            return Integer.parseInt(room.substring(0, 1));
        }
        return 0; // Default to basement if parsing fails
    }
    
    private static String getRoomDisplayName(String room) {
        // Check if it's a special area
        for (Map.Entry<String, String> entry : specialAreas.entrySet()) {
            if (entry.getValue().equals(room)) {
                return entry.getKey().substring(0, 1).toUpperCase() + 
                       entry.getKey().substring(1) + " (Floor " + getFloorFromRoom(room) + ")";
            }
        }
        return "Room " + room;
    }
    
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Brooklyn Technical High School Navigation System ===");
        System.out.println("Enter room numbers (e.g., 4N5, 2E12) or special areas (e.g., Library, Gym, Cafeteria)");
        System.out.println("Type 'quit' to exit\n");
        
        while (true) {
            System.out.print("Enter your current location: ");
            String from = scanner.nextLine().trim();
            
            if (from.equalsIgnoreCase("quit")) {
                System.out.println("Thanks for using Brooklyn Tech Navigation!");
                break;
            }
            
            System.out.print("Enter your destination: ");
            String to = scanner.nextLine().trim();
            
            if (to.equalsIgnoreCase("quit")) {
                System.out.println("Thanks for using Brooklyn Tech Navigation!");
                break;
            }
            
            NavigationResult result = getDirections(from, to);
            System.out.println("\n" + result.getDirections() + "\n");
            System.out.println("=" + "=".repeat(50) + "\n");
        }
        
        scanner.close();
    }
}