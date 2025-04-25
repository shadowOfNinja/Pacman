import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Random;
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

        Block(Image image,int x, int y, int width, int height) {
            this.image = image;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.startX = x;
            this.startY = y;
        }

        void updateDirection(char direction) {
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
            switch (this.direction) {
                case 'U':
                    this.velocityX = 0;
                    this.velocityY = -tileSize/4;
                    break;
                case 'D':
                    this.velocityX = 0;
                    this.velocityY = tileSize/4;
                    break;
                case 'L':
                    this.velocityX = -tileSize/4;
                    this.velocityY = 0;
                    break;
                case 'R':
                    this.velocityX = tileSize/4;
                    this.velocityY = 0;
                    break;
            }
        }

        void reset() {
            this.x = startX;
            this.y = startY;
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
    HashSet<Block> ghosts;
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
        for (Block ghost : ghosts) {
            char newDirection = directions[random.nextInt(directions.length)];
            ghost.updateDirection(newDirection);
        }
        gameLoop = new Timer(50, this); //20fps (1000/50)
        gameLoop.start();
    }

    public void loadMap() {
        walls = new HashSet<Block>();
        foods = new HashSet<Block>();
        ghosts = new HashSet<Block>();
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
                    ghosts.add(new Block(blueGhostImage, x, y, tileSize, tileSize));
                } else if (tileMapChar == 'o') {
                    ghosts.add(new Block(orangeGhostImage, x, y, tileSize, tileSize));
                } else if (tileMapChar == 'p') {
                    ghosts.add(new Block(pinkGhostImage, x, y, tileSize, tileSize));
                } else if (tileMapChar == 'r') {
                    ghosts.add(new Block(redGhostImage, x, y, tileSize, tileSize));
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

        for (Block ghost : ghosts) {
            g.drawImage(ghost.image, ghost.x, ghost.y, ghost.width, ghost.height, null);
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
        for (Block ghost : ghosts) {
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

    public boolean collision(Block a, Block b) {
        return a.x < b.x + b.width && a.x + a.width > b.x && a.y < b.y + b.height && a.y + a.height > b.y;
    }

    public void resetPositions() {
        for (Block ghost : ghosts) {
            ghost.reset();
            char newDirection = directions[random.nextInt(directions.length)];
            ghost.updateDirection(newDirection);
        }
        pacman.reset();
        pacman.velocityX = 0;
        pacman.velocityY = 0;
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