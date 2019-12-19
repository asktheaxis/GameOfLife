import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.control.Slider;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCombination;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
/* Alex Chrystal
 * CSC 360-003
 * Homework 5
 * 9/25/2018
 * This program simulates Conway's Game of Life and adds an additional game option called High Life. The game allows a user to select cells from grid in which they "come alive" and plays an 
 * animation based on a set of rules. These rules cause the cells to either stay alive, or die. Alive cells are green, and dead cells are black. The animation continues endlessly and if the 
 * animation causes the alive cells to "move" then, the edges of the grid are wrapped to the other side like in pac-man and other games. High Life does the same thing, only with a different 
 * set of rules that cause the cells to either stay alive or die. The game also has save/load features that allows the user to save the state of the cells + the type of game they're playing.
 * 
 * Extra Work: Accelerators, Suggested Games, Revert Button that displays the original state (useful if your messing around and don't remember the original state but liked the animation)
 * 
 */

public class Life extends Application {
	private static int DIM = 32;
	private Cell[][] cell = new Cell[DIM][DIM];
	private boolean[][] nextState = new boolean[DIM][DIM];
	private boolean[][] originalState = new boolean[DIM][DIM];
	private boolean[][] randomGame = new boolean[DIM][DIM];
	private Timeline animation = new Timeline(new KeyFrame(Duration.millis(300), e -> step()));
	private RadioButton rbLife = new RadioButton("Life");
	private RadioButton rbHLife = new RadioButton("HighLife");
	private Button revert = new Button("Revert");
	private Button step = new Button("Step");
	private Button playStop = new Button("Play");
	private Button clear = new Button("Clear");
	private Slider slRate = new Slider();
	private Label rate = new Label("Rate: ");
	
	
	@Override
	public void start(Stage primaryStage) {
		GridPane pane = new GridPane();
		pane.setStyle("-fx-background-color: black");
		for (int i = 0; i < DIM; i++)
			for (int j = 0; j < DIM; j++)
				pane.add(cell[i][j] = new Cell(), j, i);
		
		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(pane);
		borderPane.setBottom(getHBox());
		
		MenuBar menuBar = new MenuBar();
		Menu menuFile = new Menu("File");
		Menu menuGames = new Menu("Games");
		menuBar.getMenus().addAll(menuFile, menuGames);
		MenuItem menuItemClear = new MenuItem("Clear");
		MenuItem menuItemSave = new MenuItem("Save As...");
		MenuItem menuItemLoad = new MenuItem("Load Game");
		MenuItem menuItemExit = new MenuItem("Exit");
		
		MenuItem flock = new MenuItem("Flock of Seagulls");
		MenuItem sweet = new MenuItem("Sweet Treat");
		MenuItem xMark = new MenuItem("It Did This Time, Dr. Jones");
		MenuItem pong = new MenuItem("Pong");
		menuGames.getItems().addAll(flock, sweet, xMark, pong);
		menuFile.getItems().addAll(menuItemClear, new SeparatorMenuItem(), menuItemSave, menuItemLoad, 
				new SeparatorMenuItem(), menuItemExit);
		borderPane.setTop(menuBar);
		
		menuItemClear.setAccelerator(KeyCombination.keyCombination("Ctrl+C"));
		menuItemSave.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
		menuItemLoad.setAccelerator(KeyCombination.keyCombination("Ctrl+L"));
		menuItemExit.setAccelerator(KeyCombination.keyCombination("Ctrl+X"));
		
		menuItemClear.setOnAction(e -> clearBoard());
		menuItemSave.setOnAction(e -> saveAs(primaryStage));
		menuItemLoad.setOnAction(e -> load(primaryStage));
		menuItemExit.setOnAction(e -> System.exit(0));
		
		flock.setOnAction(e -> loadGame(primaryStage, "flockOfSeagulls"));
		sweet.setOnAction(e -> loadGame(primaryStage, "sweetTreat"));
		xMark.setOnAction(e -> loadGame(primaryStage, "xSpot"));
		pong.setOnAction(e -> loadGame(primaryStage, "pong"));
		
		animation.setCycleCount(Timeline.INDEFINITE);
		slRate.setMax(20);
		slRate.setMin(1);
		revert.setOnAction(e -> revertToOriginal());
		step.setOnAction(e -> step());
		playStop.setOnAction(e -> {
			if (playStop.getText() == "Play") {
				playAnimation();
			}
			else
				stopAnimation();
		});
		animation.rateProperty().bind(slRate.valueProperty());
		clear.setOnAction(e -> clearBoard());
		rbLife.setSelected(true);
		
		Scene scene = new Scene(borderPane, 700, 750);
		primaryStage.setTitle("Game of Life");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	private HBox getHBox() {
		HBox hBox = new HBox(20);
		hBox.setPadding(new Insets(15, 15, 15, 15));
		ToggleGroup group = new ToggleGroup();
		rbLife.setToggleGroup(group);
		rbHLife.setToggleGroup(group);
		hBox.getChildren().addAll(revert, step, playStop, rate, slRate, clear, rbLife, rbHLife);
		return hBox;
	}
	
	public void playAnimation() {
		originalState = rememberState();
		animation.play();
		playStop.setText("Stop");
		step.setDisable(true);	
	}
	
	/*public boolean[][] randomState() {
		boolean[][] randomGame = new boolean[DIM][DIM];
		for (int i=0; i < DIM; i++) {
			for (int j = 0; j < DIM; j++) {
				randomGame[i][j] = cell[i][j].isAlive();
			}
		}
		return randomGame;
	}*/
	
	public boolean[][] rememberState() {
		boolean[][] cellStates = new boolean[DIM][DIM];
		for (int i=0; i < DIM; i++) {
			for (int j = 0; j < DIM; j++) {
				cellStates[i][j] = cell[i][j].isAlive();
			}
		}
		return cellStates;
	}
	
	public void revertToOriginal() {
		stopAnimation();
		clearBoard();
		for (int i = 0; i < DIM; i++)
		      for (int j = 0; j < DIM; j++)
		    	cell[i][j].updateState(originalState[i][j], i, j);
		
	}
	
	public void saveAs(Stage primaryStage) {
		stopAnimation();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("."));
		fileChooser.setTitle("Enter file name");
		fileChooser.getExtensionFilters().add(new ExtensionFilter("Game-Of-Life-files", "*.gol"));
		File selectedFile = fileChooser.showSaveDialog(primaryStage);
		if (selectedFile != null) {
			try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(selectedFile));){
				if (rbLife.isSelected()) {
					output.writeChar('y');
				}
				else {
					output.writeChar('n');
				}
				boolean[][] cellStates = new boolean[DIM][DIM];
				for (int i=0; i < DIM; i++) {
					for (int j = 0; j < DIM; j++) {
						cellStates[i][j] = cell[i][j].isAlive();
					}
				}
				output.writeObject(cellStates);
			}
			catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	
	public void load(Stage primaryStage) {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setInitialDirectory(new File("."));
		fileChooser.setTitle("Enter file name");
		fileChooser.getExtensionFilters().add(
			         new ExtensionFilter("Game-Of-Life-files", "*.gol"));
		File selectedFile = fileChooser.showOpenDialog(primaryStage);
		if (selectedFile != null)
		try {
		  try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(selectedFile)); ) {
		    if (input.readChar() == 'y') {
		    	rbLife.setSelected(true);
		    }
		    else {
		    	rbHLife.setSelected(true);
		    }
		    boolean[][] cellStates = (boolean[][])(input.readObject());
		    for (int i = 0; i < DIM; i++)
		      for (int j = 0; j < DIM; j++)
		    	cell[i][j].updateState(cellStates[i][j], i, j);
		  }
		}
		catch (Exception ex) {
		  ex.printStackTrace();
		}
	}
	
	public void loadGame(Stage primaryStage, String fileName) {
		File selectedFile = new File("src/" + fileName + ".gol");
		if (selectedFile != null)
		try {
		  try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(selectedFile)); ) {
		    if (input.readChar() == 'y') {
		    	rbLife.setSelected(true);
		    }
		    else {
		    	rbHLife.setSelected(true);
		    }
		    boolean[][] cellStates = (boolean[][])(input.readObject());
		    for (int i = 0; i < DIM; i++)
		      for (int j = 0; j < DIM; j++)
		    	cell[i][j].updateState(cellStates[i][j], i, j);
		  }
		}
		catch (Exception ex) {
		  ex.printStackTrace();
		}
	}
	
	public void step() {
		for (int i=0; i < DIM; i++) {
			for (int j = 0; j < DIM; j++) {
				computeNextState(i, j);
			}
		}
		
		for (int i=0; i < DIM; i++) {
			for (int j = 0; j < DIM; j++) {
				cell[i][j].updateState(nextState[i][j], i, j);
				
			}
		}
	}
	
	public boolean[][] computeNextState(int row, int col) {
		int liveCount = 0;
		for (int i = -1; i <= 1; i++)
			for (int j = -1; j <= 1; j++)
				if (cell[(row + i + DIM) % DIM][(col + j + DIM) % DIM].isAlive())
					liveCount++;
				if (cell[row][col].isAlive())
					liveCount--;
				if (rbLife.isSelected()) {
					if (liveCount == 3 || cell[row][col].isAlive() && liveCount == 2) {
						nextState[row][col] = true;
					}
					else
						nextState[row][col] = false;
				}
				else {
					if (liveCount == 3 || cell[row][col].isAlive() && liveCount == 6) {
						nextState[row][col] = true;
					}
					else
						nextState[row][col] = false;
				}
		return nextState;
	}
	
	private void clearBoard() {
		for (int i = 0; i < DIM; i++) {
			for (int j = 0; j < DIM; j++) {
				cell[i][j].setToken(' ');
				cell[i][j].setStyle("-fx-border-color: white; -fx-border-width: .17; -fx-background-color: black");
			}
		}
	}
	
	public void stopAnimation() {
		animation.stop();
		playStop.setText("Play");
		step.setDisable(false);
	}
	
	public class Cell extends Pane {
		private char token = ' ';
		
		public Cell() {
			setStyle("-fx-border-color: white; -fx-border-width: .17; -fx-background-color: black");
			this.setPrefSize(100, 100);
			this.setOnMouseClicked(e -> handleMouseClick());
		}
		
		public boolean isAlive() {
			if (token == 'a') {
				return true;
			}
			else
				return false;
		}
		
		public char getToken() {
			return token;
		}
		
		public void setToken(char c) {
			token = c;
		}
		
		private void handleMouseClick() {
			if (token == ' ') {
				setStyle("-fx-background-color: green");
				setToken('a');
			}
			else {
				setStyle("-fx-background-color: black");
				setToken(' ');
			}
			
		}
		
		public void updateState(boolean state, int row, int col) {
			if (state) {
				setToken('a');
				setStyle("-fx-background-color: green");
			}
			else {
				setToken(' ');
				cell[row][col].setStyle("-fx-border-color: white; -fx-border-width: .17; -fx-background-color: black");
			}
		}
	}
	
	public static void main(String[] args) {
	    launch(args);
	  }
}
