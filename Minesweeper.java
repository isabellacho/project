import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

class Game extends World {
  public static final int CELL_WIDTH = 20;
  public static final int CELL_HEIGHT = 20;
  int gridHeight;
  int gridWidth;
  int columns;
  int rows;
  int mines;
  boolean minePicked;
  ArrayList<ArrayList<Cell>> cells = new ArrayList<ArrayList<Cell>>();

  Game(int columns, int rows, int mines) {
    this(columns, rows, mines, new Random());
  }

  Game(int columns, int rows, int mines, Random rand) {
    this.columns = columns;
    this.rows = rows;
    this.mines = mines;
    this.minePicked = false;

    this.gridHeight = this.columns * CELL_WIDTH;
    this.gridWidth = this.rows * CELL_HEIGHT;

    // initialize cells
    for (int i = 0; i < rows; i++) {
      cells.add(new ArrayList<Cell>());
      for (int j = 0; j < columns; j++) {
        Cell cell = new Cell(i, j);
        cells.get(i).add(cell);
      }
    }

    // initializes neighbors of cells
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        cells.get(i).get(j).initializeNeighbors(cells);
      }
    }

    // places mines at random x, y positions
    for (int i = 0; i < mines; i++) {
      int randX = rand.nextInt(columns);
      int randY = rand.nextInt(rows);
      // if cell at random x and y does not have a mine
      Cell cell = cells.get(randY).get(randX);
      if (!cell.hasMine) {
        // add a mine
        cell.hasMine = true;
      }
      else {
        i = i - 1;
      }

    }
  }

  // creates grid
  public WorldScene makeScene() {
    WorldScene scene = new WorldScene(gridHeight, gridWidth);

    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < columns; j++) {
        cells.get(i).get(j).draw(scene);
      }
    }
    return scene;
  }

  // reveals cells when left clicked, flags cells when right clicked
  // sets click bomb to true when bomb clicked
  public void onMouseClicked(Posn mc, String button) {
    Cell selected = this.cells.get((int) (mc.x / CELL_HEIGHT)).get((int) (mc.y / CELL_WIDTH));
    if (button.equals("LeftButton")) {
      selected.revealCell();
      if (selected.hasMine) {
        this.minePicked = true;
      }
    }
    else if (button.equals("RightButton")) {
      selected.flag();
    }

  }

// returns 0 if all cells without mines are revealed, else returns 1
  int gameEnd() {
    boolean temp = true;
    for (ArrayList<Cell> totalList : this.cells) {
      for (Cell cell : totalList) {
        if (!cell.revealed && !cell.hasMine) {
          temp = false;
        }
      }
    }
    if (temp) {
      return 0;
    }
    else {
      return 1;
    }
  }

  // world scene that displays losing message
  public WorldScene finalLosingScene() {
    WorldScene endScene = new WorldScene(gridWidth, gridHeight);
    endScene.placeImageXY(new TextImage("You hit a mine! Loser!", 30, Color.RED),
        this.gridWidth / 2, this.gridHeight / 2);
    return endScene;
  }

  // world scene that displays winning message
  public WorldScene finalWinningScene() {
    WorldScene endScene = new WorldScene(gridWidth, gridHeight);
    endScene.placeImageXY(new TextImage("You Win! :)", 30, Color.GREEN), this.gridWidth / 2,
        this.gridHeight / 2);
    return endScene;
  }

  // determines when the user wins or loses
  public WorldEnd worldEnds() {
    if (minePicked) {
      return new WorldEnd(true, this.finalLosingScene());
    }
    else if (this.gameEnd() == 0) {
      return new WorldEnd(true, this.finalWinningScene());
    }
    else {
      return new WorldEnd(false, this.makeScene());
    }
  }

}

class Cell {
  ArrayList<Cell> neighbors = new ArrayList<Cell>();
  int i;
  int j;
  boolean hasMine; // primed
  boolean flagged;
  boolean revealed;

  Cell(int i, int j) {
    this.i = i;
    this.j = j;
    this.hasMine = false;
    this.flagged = false;
    this.revealed = false;

  }

  // draws scene, places mine or number on cells where appropriate
  public void draw(WorldScene scene) {
    // if cell is flagged, box is shown in pink
    if (flagged) {
      scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.PINK),
          i * Game.CELL_WIDTH + (Game.CELL_WIDTH / 2),
          j * Game.CELL_HEIGHT + (Game.CELL_HEIGHT / 2));
      scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
          i * Game.CELL_WIDTH + (Game.CELL_WIDTH / 2),
          j * Game.CELL_HEIGHT + (Game.CELL_HEIGHT / 2));
    }
    // if cell is revealed draw cells in grid
    else if (revealed) {
      scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.BLUE),
          i * Game.CELL_WIDTH + (Game.CELL_WIDTH / 2),
          j * Game.CELL_HEIGHT + (Game.CELL_HEIGHT / 2));
      scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
          i * Game.CELL_WIDTH + (Game.CELL_WIDTH / 2),
          j * Game.CELL_HEIGHT + (Game.CELL_HEIGHT / 2));
      // draw mine if cell has mine
      if (this.hasMine) {
        scene.placeImageXY(new CircleImage(6, OutlineMode.SOLID, Color.RED),
            i * Game.CELL_WIDTH + (Game.CELL_HEIGHT / 2),
            j * Game.CELL_HEIGHT + (Game.CELL_HEIGHT / 2));
      }
      // draw numbers if no mine
      else {
        int numNeighbors = this.neighboringMines();
        if (numNeighbors != 0) {
          scene.placeImageXY(new TextImage(Integer.toString(numNeighbors), Color.WHITE),
              i * Game.CELL_WIDTH + (Game.CELL_HEIGHT / 2),
              j * Game.CELL_HEIGHT + (Game.CELL_HEIGHT / 2));
        }
      }
    }
    // if not revealed draw gray cells
    else if (!revealed) {
      scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.GRAY),
          i * Game.CELL_WIDTH + (Game.CELL_WIDTH / 2),
          j * Game.CELL_HEIGHT + (Game.CELL_HEIGHT / 2));
      scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
          i * Game.CELL_WIDTH + (Game.CELL_WIDTH / 2),
          j * Game.CELL_HEIGHT + (Game.CELL_HEIGHT / 2));
    }

  }

  // creates list of adjacent neighbors, checks for border cases
  void initializeNeighbors(ArrayList<ArrayList<Cell>> cells) {
    // when x is not zero
    if (i != 0) {
      // add left neighbor
      this.neighbors.add(cells.get(i - 1).get(j));
      if (j != 0) {
        this.neighbors.add(cells.get(i - 1).get(j - 1));
      }
    }
    // when y is not zero
    if (j != 0) {
      // add top neighbor
      this.neighbors.add(cells.get(i).get(j - 1));
      if (i != cells.size() - 1) {
        this.neighbors.add(cells.get(i + 1).get(j - 1));
      }
    }
    // when x is not r
    if (i != cells.size() - 1) {
      // add right neighbor
      this.neighbors.add(cells.get(i + 1).get(j));
      if (j != cells.get(0).size() - 1) {
        this.neighbors.add(cells.get(i + 1).get(j + 1));
      }
    }
    // when y is not bottom
    if (j != cells.get(0).size() - 1) {
      // add bottom neighbor
      this.neighbors.add(cells.get(i).get(j + 1));
      if (i != 0) {
        this.neighbors.add(cells.get(i - 1).get(j + 1));
      }
    }
  }

  // get number of neighbors with a mine
  int neighboringMines() {
    int counter = 0;
    for (int i = 0; i < neighbors.size(); i++) {
      if (neighbors.get(i).hasMine) {
        counter = counter + 1;
      }
    }
    return counter;
  }

//flags the cell when right clicked
  void flag() {
    this.flagged = !this.flagged;
  }

//reveals selected cell
  void revealCell() {
    this.revealed = true;
    if (this.numNeighborBombs() == 0) {
      this.revealNeighbors();
    }
  }

  // reveals neighbors of cell, flood fill
  void revealNeighbors() {
    for (Cell t : this.neighbors) {
      if (!t.revealed) {
        t.revealCell();
      }
    }
  }

  // number of neighboring bombs
  int numNeighborBombs() {
    int bombs = 0;
    for (Cell t : this.neighbors) {
      if (t.hasMine) {
        bombs = bombs + 1;
      }
    }
    return bombs;
  }
}

class ExamplesWorld {
  ArrayList<ArrayList<Cell>> cells = new ArrayList<ArrayList<Cell>>();
  Cell c = new Cell(0, 0);
  Cell c01 = new Cell(0, 1);
  Cell c10 = new Cell(1, 0);
  Cell c11 = new Cell(1, 1);
  Cell test = new Cell(2, 1);
  ArrayList<Cell> neighbors = new ArrayList<Cell>();
  ArrayList<Cell> cNeighbors = new ArrayList<Cell>(Arrays.asList(c10, c11, test));
  WorldScene scene = new WorldScene(500, 500);
  Game game1;

  // initializes game
  void initGame() {
    this.game1 = new Game(20, 30, 30);
  }

  // initialize scene
  void initScene() {
    scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.SOLID, Color.PINK),
        0 * Game.CELL_WIDTH + (Game.CELL_WIDTH / 2), 0 * Game.CELL_HEIGHT + (Game.CELL_HEIGHT / 2));
    scene.placeImageXY(new RectangleImage(20, 20, OutlineMode.OUTLINE, Color.BLACK),
        0 * Game.CELL_WIDTH + (Game.CELL_WIDTH / 2), 0 * Game.CELL_HEIGHT + (Game.CELL_HEIGHT / 2));
  }

  void initEndScene() {
    WorldScene endScene = new WorldScene(200, 200);
    endScene.placeImageXY(new TextImage("You hit a mine! Loser!", 30, Color.RED), 100, 100);
  }

  // initialize cells
  void initCells() {
    for (int i = 0; i < 20; i++) {
      cells.add(new ArrayList<Cell>());
      for (int j = 0; j < 20; j++) {
        Cell cell = new Cell(i, j);
        cells.get(i).add(cell);
      }
    }
  }

  void initCells2() {
    c01.flagged = true;
    c01.hasMine = true;
    c01.revealed = true;
  }

  void testGame(Tester t) {
    Game game = new Game(20, 30, 30);
    game.bigBang(game.rows * Game.CELL_WIDTH, game.columns * Game.CELL_HEIGHT, .01);
  }

  void testInitializeNeighbors(Tester t) {
    initCells();
    t.checkExpect(c.neighbors, this.neighbors);
    c.initializeNeighbors(cells);
    t.checkExpect(c.neighbors, this.cNeighbors);
  }

  void testNeighboringMines(Tester t) {
    Game g1 = new Game(10, 10, 15, new Random(30));
    t.checkExpect(g1.cells.get(2).get(5).neighboringMines(), 1);
    t.checkExpect(g1.cells.get(3).get(1).neighboringMines(), 2);
  }

  void testDraw(Tester t) {
    WorldScene emptyScene = new WorldScene(500, 500);
    initCells2();
    c.flag(); // return true;
    c.draw(emptyScene);
    initScene();
    t.checkExpect(emptyScene, scene);
  }

//  void testMakeScene(Tester t) {
//    initGame();
//    Game g1 = new Game(10, 10, 15, new Random(30));
//    WorldScene scene = new WorldScene(200, 200);
//    g1.makeScene();
//    initScene();
//    t.checkExpect(g1.makeScene(), scene);
//
//  }

  void testNumNeighborBombs(Tester t) {
    initCells2();
    c01.numNeighborBombs();
    t.checkExpect(c.numNeighborBombs(), 0);
    t.checkExpect(c01.hasMine, true);
  }

  void testRevealNeighbors(Tester t) {
    initCells2();
    c01.revealNeighbors();
    t.checkExpect(c01.revealed, true);

  }

  void testRevealCell(Tester t) {
    initCells2();
    c01.revealCell();
    t.checkExpect(c01.revealed, true);

  }

  void testFlag(Tester t) {
    initCells2();
    c01.flag();
    t.checkExpect(c01.flagged, false);
  }

  void testWorldEnds(Tester t) {

  }

//  void testFinalLosingScene(Tester t) {
//    WorldScene endScene = new WorldScene(30, 30);
//    endScene.placeImageXY(new TextImage("You hit a mine! Loser!", 30, Color.RED), 30 / 2,
//        30 / 2);
//    initGame();
//    this.game1.finalLosingScene();
//    t.checkExpect(this.game1, endScene);
//    
//    
//
//  }
//
//  void testFinalWinningScene(Tester t) {
//    WorldScene testScene = new WorldScene(30, 30);
//    testScene.placeImageXY(new TextImage("You Win! :)", 30, Color.GREEN), 30 / 2,
//        30 / 2);
//    initGame();
//    this.game1.finalWinningScene();
//    t.checkExpect(this.game1, testScene);
//  }

  void testGameEnd(Tester t) {
    initGame();
    t.checkExpect(this.game1.gameEnd(), 1);

  }
//
//  void testOnMouseClicked(Tester t) {
//   initGame();
//   game1.onMouseClicked();
//
//  }
}
