import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.List;
import javax.swing.*;

public class PacMan extends JPanel implements ActionListener, KeyListener {
    class Block {
        int x;
        int y;
        int width;
        int height;
        Image image;

        int startX;
        int startY;
        char direction = 'U';
        int velocityX = 0;
        int velocityY = 0;

        // New attributes for future position
        int futureX;
        int futureY;

        Block(Image image,int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;

            this.futureX = x;
            this.futureY = y;
        }

        // Method to calculate future position
        void calculateFuturePosition(int steps, int boardWidth, int boardHeight) {
            // Start from the current position
            int futureX = this.x;
            int futureY = this.y;

            // Store the last valid position
            int lastValidX = futureX;
            int lastValidY = futureY;

            System.out.println("Current Position: " + futureX + ", " + futureY);

            // Simulate movement step-by-step based on Pac-Man's dimensions
            for (int i = 0; i < steps; i++) {
                // Calculate the next position based on the current direction
                switch (this.direction) {
                    case 'U': // Moving up
                        futureY -= this.height;
                        break;
                    case 'D': // Moving down
                        futureY += this.height;
                        break;
                    case 'L': // Moving left
                        futureX -= this.width;
                        break;
                    case 'R': // Moving right
                        futureX += this.width;
                        break;
                }

                // Check for collisions with walls
                boolean collisionDetected = false;
                for (Block wall : walls) {
                    if (futureX < wall.x + wall.width && futureX + this.width > wall.x &&
                        futureY < wall.y + wall.height && futureY + this.height > wall.y) {
                        collisionDetected = true;
                        System.out.println("Collision detected with wall at: " + wall.x + ", " + wall.y);
                        break;
                    }
                }

                // If a collision is detected, stop at the last valid position
                if (collisionDetected) {
                    futureX = lastValidX;
                    futureY = lastValidY;
                    break;
                }

                // Update the last valid position
                lastValidX = futureX;
                lastValidY = futureY;

                // Wrap around the board if Pac-Man goes off-screen
                if (futureX < 0) {
                    futureX = boardWidth - this.width;
                } else if (futureX >= boardWidth) {
                    futureX = 0;
                }
                if (futureY < 0) {
                    futureY = boardHeight - this.height;
                } else if (futureY >= boardHeight) {
                    futureY = 0;
                }
            }

            // Update the future position
            this.futureX = futureX;
            this.futureY = futureY;

            System.out.println("Future Position: " + this.futureX + ", " + this.futureY);
        }

        void updateDirection(char direction) {
            if (this.direction == direction) {
                return; // No change in direction
            }
            
            char prevDirection = this.direction;
            this.direction = direction;
            updateVelocity();

            //make sure the pacman is not stuck in a wall
            this.x += this.velocityX;
            this.y += this.velocityY;
            for (Block wall : walls) {
                if (collision(this, wall)) {
                    this.x -= this.velocityX;
                    this.y -= this.velocityY;
                    this.direction = prevDirection;
                    updateVelocity();
                    break;
                }
            }

            switch (pacman.direction) {
                case 'U':
                    pacman.image = pacmanUpImage;
                    break;
                case 'D':
                    pacman.image = pacmanDownImage;
                    break;
                case 'L':
                    pacman.image = pacmanLeftImage;
                    break;
                case 'R':
                    pacman.image = pacmanRightImage;
                    break;
            }
        }

        void updateVelocity() {
            int speed = tileSize / 4; // Speed of pacman and ghosts             
            
            // Match speed for ghosts
            if (this instanceof Ghost) {
                speed = Math.max(Math.abs(pacman.velocityX), Math.abs(pacman.velocityY)); // Match Pacman's speed
            }
            
            switch (this.direction) {
                case 'U':
                    this.velocityX = 0;
                    this.velocityY = -speed;
                    break;
                case 'D':
                    this.velocityX = 0;
                    this.velocityY = speed;
                    break;
                case 'L':
                    this.velocityX = -speed;
                    this.velocityY = 0;
                    break;
                case 'R':
                    this.velocityX = speed;
                    this.velocityY = 0;
                    break;
            }
        }

        void reset() {
            this.x = startX;
            this.y = startY;
        }
    }

    class Ghost extends Block {
        String behaviour; 
        long lastPathUpdateTime;
        List<Node> path;
        int targetX;
        int targetY;

        Ghost (Image image, int x, int y, int width, int height, String behaviour) {
            super(image, x, y, width, height);
            this.behaviour = behaviour;
            this.lastPathUpdateTime = 0;
            this.targetX = -1;
            this.targetY = -1;
        }
    }

    class Node {
        int x, y;
        int gCost, hCost, fCost;
        Node parent;
    
        Node(int x, int y) {
            this.x = x;
            this.y = y;
        }
    
        void calculateCosts(Node target, int gCostFromStart) {
            this.gCost = gCostFromStart;
            this.hCost = Math.abs(target.x - this.x) + Math.abs(target.y - this.y); // Manhattan distance
            this.fCost = this.gCost + this.hCost;
        }
    }
    
    private int rowCount = 21;
    private int columnCount = 19;
    private int tileSize = 32;
    private int boardWidth = columnCount * tileSize;
    private int boardHeight = rowCount * tileSize;

    private Image wallImage;
    private Image blueGhostImage;
    private Image orangeGhostImage;
    private Image pinkGhostImage;
    private Image redGhostImage;

    private Image pacmanUpImage;
    private Image pacmanDownImage;
    private Image pacmanLeftImage;
    private Image pacmanRightImage;

    //X = wall, O = skip, P = pac man, ' ' = food
    //Ghosts: b = blue, o = orange, p = pink, r = red
    private String[] tileMap = {
        "XXXXXXXXXXXXXXXXXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X                 X",
        "X XX X XXXXX X XX X",
        "X    X       X    X",
        "XXXX XXXX XXXX XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXrXX X XXXX",
        "O       bpo       O",
        "XXXX X XXXXX X XXXX",
        "OOOX X       X XOOO",
        "XXXX X XXXXX X XXXX",
        "X        X        X",
        "X XX XXX X XXX XX X",
        "X  X     P     X  X",
        "XX X X XXXXX X X XX",
        "X    X   X   X    X",
        "X XXXXXX X XXXXXX X",
        "X                 X",
        "XXXXXXXXXXXXXXXXXXX" 
    };

    HashSet<Block> walls;
    HashSet<Block> foods;
    HashSet<Ghost> ghosts;
    Block pacman;

    Timer gameLoop;
    char[] directions = {'U', 'D', 'L', 'R'};
    Random random = new Random();
    int score = 0;
    int highScore = 0;
    int lives = 3;
    boolean gameOver = false;
    char desiredDirection = ' ';

    PacMan() {
        setPreferredSize(new Dimension(boardWidth, boardHeight));
        setBackground(Color.BLACK);
        addKeyListener(this);
        setFocusable(true);

        // Load images
        wallImage = new ImageIcon(getClass().getResource("./images/wall.png")).getImage();
        blueGhostImage = new ImageIcon(getClass().getResource("./images/blueGhost.png")).getImage();
        orangeGhostImage = new ImageIcon(getClass().getResource("./images/orangeGhost.png")).getImage();
        pinkGhostImage = new ImageIcon(getClass().getResource("./images/pinkGhost.png")).getImage();
        redGhostImage = new ImageIcon(getClass().getResource("./images/redGhost.png")).getImage();

        pacmanUpImage = new ImageIcon(getClass().getResource("./images/pacmanUp.png")).getImage();
        pacmanDownImage = new ImageIcon(getClass().getResource("./images/pacmanDown.png")).getImage();
        pacmanLeftImage = new ImageIcon(getClass().getResource("./images/pacmanLeft.png")).getImage();
        pacmanRightImage = new ImageIcon(getClass().getResource("./images/pacmanRight.png")).getImage();

        // Load map
        loadMap();
        for (Ghost ghost : ghosts) {
            char newDirection = directions[random.nextInt(directions.length)];
            ghost.updateDirection(newDirection);
        }
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Ghost>();
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                String rowString = tileMap[row];
                char tileMapChar = rowString.charAt(col);
                int x = col * tileSize;
                int y = row * tileSize;

                if (tileMapChar == 'X') {
                    walls.add(new Block(wallImage, x, y, tileSize, tileSize));
                } else if (tileMapChar == ' ') {
                    foods.add(new Block(null, x + 14, y + 14, 4, 4));
                } else if (tileMapChar == 'P') {
                    pacman = new Block(pacmanRightImage, x, y, tileSize, tileSize);
                } else if (tileMapChar == 'b') {
                    ghosts.add(new Ghost(blueGhostImage, x, y, tileSize, tileSize, "scatter"));
                } else if (tileMapChar == 'o') {
                    ghosts.add(new Ghost(orangeGhostImage, x, y, tileSize, tileSize, "scatter"));
                } else if (tileMapChar == 'p') {
                    ghosts.add(new Ghost(pinkGhostImage, x, y, tileSize, tileSize, "ambusher"));
                } else if (tileMapChar == 'r') {
                    ghosts.add(new Ghost(redGhostImage, x, y, tileSize, tileSize, "chaser"));
                }
            }
        }
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    public void draw(Graphics g) {
        g.drawImage(pacman.image, pacman.x, pacman.y, pacman.width, pacman.height, null);

        // if pacman's future position is not the same as the current position, draw it
        if (pacman.futureX != pacman.x || pacman.futureY != pacman.y) {
            g.setColor(Color.PINK);
            g.fillRect(pacman.futureX, pacman.futureY, pacman.width, pacman.height);
        }

        for (Ghost ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);

            // Draw the chaser's path
            if (ghost.behaviour.equals("chaser")) {
                if (ghost.path != null) {
                    g.setColor(Color.RED);
                    for (int i = 0; i < ghost.path.size() - 1; i++) {
                        Node current = ghost.path.get(i);
                        Node next = ghost.path.get(i + 1);
                        int x1 = current.x * tileSize + tileSize / 2;
                        int y1 = current.y * tileSize + tileSize / 2;
                        int x2 = next.x * tileSize + tileSize / 2;
                        int y2 = next.y * tileSize + tileSize / 2;
                        g.drawLine(x1, y1, x2, y2);
                    }
                } else {
                    System.out.println("No path found from red ghost to pacman.");
                }
            }
            // Draw the amusher's path
            if (ghost.behaviour.equals("ambusher")) {
                if (ghost.path != null) {
                    g.setColor(Color.PINK);
                    for (int i = 0; i < ghost.path.size() - 1; i++) {
                        Node current = ghost.path.get(i);
                        Node next = ghost.path.get(i + 1);
                        int x1 = current.x * tileSize + tileSize / 2;
                        int y1 = current.y * tileSize + tileSize / 2;
                        int x2 = next.x * tileSize + tileSize / 2;
                        int y2 = next.y * tileSize + tileSize / 2;
                        g.drawLine(x1, y1, x2, y2);
                    }
                } else {
                    System.out.println("No path found from pink ghost to pacman.");
                }
            }
        }

        for (Block wall : walls) {
            g.drawImage(wall.image, wall.x, wall.y, wall.width, wall.height, null);
        }

        for (Block food : foods) {
            g.setColor(Color.WHITE);
            g.fillRect(food.x, food.y, food.width, food.height);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        if (gameOver) {
            g.drawString("Game Over! Your score: " + String.valueOf(score), tileSize/2, tileSize/2);
        } else {
            g.drawString("x" + String.valueOf(lives) + " Score: " + String.valueOf(score) + " High Score: " + String.valueOf(highScore), tileSize/2, tileSize/2);
        }
    }

    public void move() {
        // Try to apply the desired direction from the buffer
        if (desiredDirection != ' ') {
            pacman.updateDirection(desiredDirection);
            if (pacman.direction == desiredDirection) {
                desiredDirection = ' '; // Clear the buffer if the direction was applied
            }
        }
        
        pacman.x += pacman.velocityX;
        pacman.y += pacman.velocityY;

        // Update Pac-Man's future position
        pacman.calculateFuturePosition(3, boardWidth, boardHeight);

        // Check for collision with walls
        for (Block wall : walls) {
            if (collision(pacman, wall)) {
                pacman.x -= pacman.velocityX;
                pacman.y -= pacman.velocityY;
                break;
            }
        }

        // Check if pacman goes off screen
        if (pacman.x < 0 && pacman.velocityX < 0) {
            // Teleport pacman to the right side of the screen
            pacman.x = boardWidth - pacman.width;
        } else if (pacman.x > boardWidth - pacman.width && pacman.velocityX > 0) {
            // Teleport pacman to the left side of the screen
            pacman.x = 0;
        }

        // update ghost directions
        for (Ghost ghost : ghosts) {
            // Update the ghost's velocity to match Pac-Man's speed
            ghost.updateVelocity();
            
            if (collision(pacman, ghost)) {
                lives--;
                
                // Brielfy stop the game loop to show the collision
                gameLoop.stop();
                try {
                    Thread.sleep(3000); // Pause for 3 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                gameLoop.start();

                resetPositions();
                if (lives == 0) {
                    gameOver = true;
                }
            }

            // Update ghost direction based on its behaviour
            switch (ghost.behaviour) {
                case "chaser":
                    moveTowardPacman(ghost);
                    break;
                case "ambusher":
                    moveTowardPacmanFuturePosition(ghost);
                    break;
                /*case "random":
                    moveRandomly(ghost);
                    break;
                case "scatter":
                    moveTowardCorner(ghost);
                    break;*/
                default:
                    break;
            }

            movetoNextStep(ghost);

            ghost.x += ghost.velocityX;
            ghost.y += ghost.velocityY;

            // Check for collision with walls
            for (Block wall : walls) {
                //NOTE: quick fix for ghosts not getting stuck on the 9th row
                if (ghost.y == tileSize * 9 && ghost.direction != 'U' && ghost.direction != 'D') {
                    ghost.updateDirection('U');
                }
                // NOTE: This includes a quick fix for the two ghosts going off screen
                if (collision(ghost, wall)) {
                        ghost.x -= ghost.velocityX;
                        ghost.y -= ghost.velocityY;
                        char newDirection = directions[random.nextInt(directions.length)];
                        ghost.updateDirection(newDirection);
                        break;
                }

                // Check if ghost goes off screen
                if (ghost.x < 0 && ghost.velocityX < 0) {
                    // Teleport ghost to the right side of the screen
                    ghost.x = boardWidth - ghost.width;
                } else if (ghost.x > boardWidth - ghost.width && ghost.velocityX > 0) {
                    // Teleport ghost to the left side of the screen
                    ghost.x = 0;
                } 
            }
        }

        // Check for collision with food
        for (Block food : foods) {
            if (collision(pacman, food)) {
                foods.remove(food);
                score += 10;
                break;
            }
        }

        // Check if all food is eaten
        if (foods.isEmpty()) {
            loadMap();
            resetPositions();
            lives += 1; // Extra life for completing the level
            score += 100; // Bonus points for completing the level

            //pause the game loop to show the level completion
            gameLoop.stop();
            try {
                Thread.sleep(5000); // Pause for 3 seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            gameLoop.start();
        }

        // Update high score
        if (score > highScore) {
            highScore = score;
        }
    }

    private void moveTowardPacman(PacMan.Ghost ghost) {
        System.out.println("Chaser calculating path to pacman...");
        // Calculate Pac-Man's current position on the grid
        int pacmanGridX = pacman.x / tileSize;
        int pacmanGridY = pacman.y / tileSize;
    
        // Check if the ghost's target has changed
        if (ghost.targetX != pacmanGridX || ghost.targetY != pacmanGridY) {
            // Update the path
            System.out.println("Calculating path from " + ghost.x + ", " + ghost.y + " to " + pacman.futureX + ", " + pacman.futureY);
            ghost.path = findPath(ghost.x, ghost.y, pacman.x, pacman.y);
    
            // Update the target position
            ghost.targetX = pacmanGridX;
            ghost.targetY = pacmanGridY;
        }
    
        // DUPLICATE OF movetoNextStep() - could be refactored, but seems to work better here
        // If a path exists, move toward the next step
        if (ghost.path != null && !ghost.path.isEmpty()) {
            Node nextStep = ghost.path.get(0);
            int targetX = nextStep.x * tileSize;
            int targetY = nextStep.y * tileSize;
    
            if (targetX > ghost.x) {
                ghost.updateDirection('R');
            } else if (targetX < ghost.x) {
                ghost.updateDirection('L');
            } else if (targetY > ghost.y) {
                ghost.updateDirection('D');
            } else if (targetY < ghost.y) {
                ghost.updateDirection('U');
            }
    
            // Remove the step once the ghost reaches it
            if (ghost.x == targetX && ghost.y == targetY) {
                ghost.path.remove(0);
            }
        }
        System.out.println("Chaser calculation: done!");
    }

    private void moveTowardPacmanFuturePosition(PacMan.Ghost ghost) {
        System.out.println("Ambusher calculating path to pacman...");
        // Convert Pac-Man's future position to grid coordinates
        int pacmanFutureGridX = pacman.futureX / tileSize;
        int pacmanFutureGridY = pacman.futureY / tileSize;
    
        // Check if the ghost's target has changed
        if (ghost.targetX != pacmanFutureGridX || ghost.targetY != pacmanFutureGridY) {
            // Update the path
            System.out.println("Calculating path from " + ghost.x + ", " + ghost.y + " to " + pacman.futureX + ", " + pacman.futureY);
            ghost.path = findPath(ghost.x, ghost.y, pacman.futureX, pacman.futureY);
    
            // Update the target position
            ghost.targetX = pacmanFutureGridX;
            ghost.targetY = pacmanFutureGridY;
        }
    
        // DUPLICATE OF movetoNextStep() - could be refactored, but seems to work better here
        // If a path exists, move toward the next step
        if (ghost.path != null && !ghost.path.isEmpty()) {
            Node nextStep = ghost.path.get(0);
            int targetX = nextStep.x * tileSize;
            int targetY = nextStep.y * tileSize;
    
            if (targetX > ghost.x) {
                ghost.updateDirection('R');
            } else if (targetX < ghost.x) {
                ghost.updateDirection('L');
            } else if (targetY > ghost.y) {
                ghost.updateDirection('D');
            } else if (targetY < ghost.y) {
                ghost.updateDirection('U');
            }
    
            // Remove the step once the ghost reaches it
            if (ghost.x == targetX && ghost.y == targetY) {
                ghost.path.remove(0);
            }
        }
        System.out.println("Ambusher calculation: done!");
    }

    private void movetoNextStep(PacMan.Ghost ghost) {
        // Calculate the next step in the path
        if (ghost.path != null && !ghost.path.isEmpty()) {
            Node nextStep = ghost.path.get(0); // Get the next step in the path
            int targetX = nextStep.x * tileSize;
            int targetY = nextStep.y * tileSize;

            if (targetX > ghost.x) {
                ghost.updateDirection('R');
            } else if (targetX < ghost.x) {
                ghost.updateDirection('L');
            } else if (targetY > ghost.y) {
                ghost.updateDirection('D');
            } else if (targetY < ghost.y) {
                ghost.updateDirection('U');
            }

            // Remove the step once the ghost reaches it
            if (ghost.x == targetX && ghost.y == targetY) {
                ghost.path.remove(0);
            }
        }
    }

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    public void resetPositions() {
        for (Ghost ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(directions.length)];
            ghost.updateDirection(newDirection);
        }
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
    }

    // A* pathfinding algorithm to find the path from the ghost to target coordinates
    public List<Node> findPath(int startX, int startY, int targetX, int targetY) {
        boolean[][] walkable = new boolean[rowCount][columnCount];
        
        for (int row = 0; row < rowCount; row++) {
            for (int col = 0; col < columnCount; col++) {
                walkable[row][col] = true; // Default all tiles to walkable
            }
        }
        for (Block wall : walls) {
            walkable[wall.y / tileSize][wall.x / tileSize] = false; // Mark walls as non-walkable
        }

        if (startX < 0 || startY < 0 || startX >= boardWidth || startY >= boardHeight ||
        targetX < 0 || targetY < 0 || targetX >= boardWidth || targetY >= boardHeight) {
        System.out.println("Invalid start or target position.");
        return null;
        }

        if (!walkable[startY / tileSize][startX / tileSize] || !walkable[targetY / tileSize][targetX / tileSize]) {
            System.out.println("Start or target position is not walkable.");
            return null;
        }

        Node startNode = new Node(startX / tileSize, startY / tileSize);
        Node targetNode = new Node(targetX / tileSize, targetY / tileSize);

        List<Node> openList = new ArrayList<>();
        List<Node> closedList = new ArrayList<>();
        openList.add(startNode);

        int maxIterations = rowCount * columnCount;
        int iterations = 0;

        while (!openList.isEmpty()) {
            iterations++;
            if (iterations > maxIterations) {
                System.out.println("Pathfinding exceeded maximum iterations.");
                return null; // Pathfinding failed
            }

            Node currentNode = openList.get(0);
            for (Node node : openList) {
                if (node.fCost < currentNode.fCost || (node.fCost == currentNode.fCost && node.hCost < currentNode.hCost)) {
                    currentNode = node;
                }
            }

            openList.remove(currentNode);
            closedList.add(currentNode);

            if (currentNode.x == targetNode.x && currentNode.y == targetNode.y) {
                return reconstructPath(currentNode);
            }

            for (int[] direction : new int[][]{{0, -1}, {0, 1}, {-1, 0}, {1, 0}}) {
                int neighborX = currentNode.x + direction[0];
                int neighborY = currentNode.y + direction[1];

                if (neighborX < 0 || neighborY < 0 || neighborX >= columnCount || neighborY >= rowCount || !walkable[neighborY][neighborX]) {
                    continue;
                }

                Node neighbor = new Node(neighborX, neighborY);
                if (closedList.contains(neighbor)) {
                    continue;
                }

                int gCostFromStart = currentNode.gCost + 1;
                if (!openList.contains(neighbor)) {
                    neighbor.calculateCosts(targetNode, gCostFromStart);
                    neighbor.parent = currentNode;
                    openList.add(neighbor);
                } else if (gCostFromStart < neighbor.gCost) {
                    neighbor.calculateCosts(targetNode, gCostFromStart);
                    neighbor.parent = currentNode;
                }
            }
        }

        return null; // No path found
    }

    private List<Node> reconstructPath(Node currentNode) {
        List<Node> path = new ArrayList<>();
        while (currentNode != null) {
            path.add(0, currentNode);
            currentNode = currentNode.parent;
        }
        return path;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        move();
        repaint();
        if (gameOver) {
            gameLoop.stop();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (gameOver) {
            loadMap();
            resetPositions();
            lives = 3;
            score = 0;
            gameOver = false;
            gameLoop.start();
        }
        
        // Get key presses and set the desired direction in the buffer
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
                desiredDirection = 'U';
                break;
            case KeyEvent.VK_DOWN:
                desiredDirection = 'D';
                break;
            case KeyEvent.VK_LEFT:
                desiredDirection = 'L';
                break;
            case KeyEvent.VK_RIGHT:
                desiredDirection = 'R';
                break;
            case KeyEvent.VK_SPACE: // Space bar to pause the game
                // Pause the game
                if (gameLoop.isRunning()) {
                    gameLoop.stop();
                } else {
                    gameLoop.start();
                }
        }

    }
}

// TO DO
// 1. Add logic so pacman and ghost teleport when they go into the blank space - DONE
// 2. Add movement logic specific to each ghost
// 3. Add a new level/(s) when all the food is eaten
// 4. Add a high score feature - DONE
// 5. Allow player to pause the game - DONE
// 6. Add power pellets that allow pacman to eat ghosts for a limited time
// 7. Fix movements to be more responsive to key presses - DONE
//      - Could be because it's always looking at all possible spaces for collision every key release?