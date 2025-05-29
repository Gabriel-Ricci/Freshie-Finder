import java.util.*;

public class BrooklynTechNavigator {
    private Map<String, Node> nodes = new HashMap<>();
    
    public Map<String, Node> getNodes() {
        return nodes;
    }

    public BrooklynTechNavigator() {
        buildSchoolGraph();
    }

    // Build the school graph based on Brooklyn Tech's floor layout
    private void buildSchoolGraph() {
        // Start with the basement (Floor 0)
        addBasement();
        
        // Build rooms and edges for each floor from 1 to 8
        for (int floor = 1; floor <= 8; floor++) {
            addFloor(floor);
        }
    }

    // Add basement layout (No center hallway and a pool on the south side)
    private void addBasement() {
        addRoomArea(0, "N", 1, 9); // North side (Rooms 1 to 9)
        addRoomArea(0, "S", 1, 9); // South side (Rooms 1 to 9)
        addRoomArea(0, "W", 1, 24); // West side (Rooms 1 to 24)
        
        // Add the pool area as an unconnected node in the south
        addNode("B_Pool", "Pool Area, Basement");
        
        // Add staircases for basement
        addStaircases(0);
    }

    // Add rooms for a given floor (1 to 8)
    private void addFloor(int floor) {
        // North side (Rooms 1 to 9)
        addRoomArea(floor, "N", 1, 9);
        
        // South side (Rooms 1 to 9)
        addRoomArea(floor, "S", 1, 9);
        
        // East and West sides (24 rooms each)
        addRoomArea(floor, "E", 1, 24);
        addRoomArea(floor, "W", 1, 24);
        
        // Center area (Rooms 1 to 8)
        addRoomArea(floor, "C", 1, 8);
        
        // Special floor handling (gym, library, cafeteria, etc.)
        handleSpecialFloorCases(floor);

        // Add staircases for the floor
        addStaircases(floor);
    }

    // Helper method to add rooms for an area
    private void addRoomArea(int floor, String side, int startRoom, int endRoom) {
        for (int roomNum = startRoom; roomNum <= endRoom; roomNum++) {
            String roomId = side + roomNum + "F" + floor;
            addNode(roomId, side + " Side, Floor " + floor + ", Room " + roomNum);
        }
    }

    // Add staircases for each floor (4 corners + 2 middle ones on the East/West side)
    private void addStaircases(int floor) {
        String[] corners = {"NW", "NE", "SW", "SE"};
        for (String corner : corners) {
            addNode(corner + "StairF" + floor, "Staircase " + corner + ", Floor " + floor);
        }
        
        // East/West middle staircases (excluding floor 7)
        if (floor != 7) {
            addNode("E12StairF" + floor, "East Staircase 12, Floor " + floor);
            addNode("W12StairF" + floor, "West Staircase 12, Floor " + floor);
        }
    }

    // Special floor-specific handling for things like gym, cafeteria, library, etc.
    private void handleSpecialFloorCases(int floor) {
        switch (floor) {
            case 0: // Basement
                break;
            case 1: // Gym in South
                // Gym connected to south rooms
                addEdge("S1F1", "GymF1", 10);
                break;
            case 5: // Library in Center (only accessible from West side)
                // West side rooms should have access to the library in center
                addEdge("W12F5", "LibraryF5", 5);
                break;
            case 7: // Cafeteria (connected to 4 corners)
                addEdge("NWStairF7", "CafeteriaF7", 10);
                addEdge("NEStairF7", "CafeteriaF7", 10);
                addEdge("SWStairF7", "CafeteriaF7", 10);
                addEdge("SEStairF7", "CafeteriaF7", 10);
                break;
            case 8: // Gym in Center
                addEdge("GymF8", "CenterF8", 10);
                break;
            default:
                break;
        }
    }

    // Add a node to the graph (room or area)
    private void addNode(String id, String label) {
        nodes.put(id, new Node(id, label));
    }

    // Add an edge between two rooms (nodes)
    private void addEdge(String fromId, String toId, int time) {
        Node from = nodes.get(fromId);
        Node to = nodes.get(toId);
        if (from != null && to != null) {
            from.edges.add(new Edge(to, time));
            to.edges.add(new Edge(from, time)); // bidirectional
        }
    }

    public static void main(String[] args) {
        BrooklynTechNavigator nav = new BrooklynTechNavigator();
        Scanner sc = new Scanner(System.in);

        System.out.println("Welcome to Brooklyn Tech Navigator!");
        System.out.println("Enter your current room (e.g., 'W12F1'):");

        String startId = sc.nextLine().trim().toUpperCase();

        System.out.println("Enter your destination room (e.g., 'W14F2'):");

        String endId = sc.nextLine().trim().toUpperCase();

        if (!nav.getNodes().containsKey(startId) || !nav.getNodes().containsKey(endId)) {
            System.out.println("Invalid room(s). Please check your input.");
            return;
        }

        List<Node> path = nav.shortestPath(startId, endId);
        if (path == null) {
            System.out.println("No path found between those rooms.");
        } else {
            System.out.println("\nDIRECTIONS:");
            for (Node n : path) {
                System.out.println(" - " + n.label);
            }
            int totalTime = nav.calculateTime(path);
            System.out.println("Estimated travel time: " + totalTime + " seconds.");
        }
    }
}
