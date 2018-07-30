package com.gdx.mythic;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import java.lang.Math;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Mythic extends ApplicationAdapter {
	private static final int MAX_ROWS = 100;
	private static final int MAX_COLS = 100;

	private enum State {
		WAITING, MOVING;
	}
	private State state;

	//game resources
	private Animation<TextureRegion> playerAnimation;
	private Animation<TextureRegion> npcAnimation;
	private TextureAtlas atlas;
	private Player player;
	private NPC npc;
	private Set<Widget> widgets;
	private Map<String, Cell> template;
	private Cell[][] cells = new Cell[MAX_COLS][MAX_ROWS];
	private String[][] testBoard;
	
	//libgdx resources (camera, etc.)
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private FitViewport viewport;
	
	private Vector3 touchPos;

	// resources related to player movement
	// used to calculate the difference between touch(x,y) and player(x,y)
	private float xDiff;
	private float yDiff;
	// used to keep track of the player's original position when dragging if it
	// needs to snap back (i.e. dragged outside the play area)	
	private float xOrigin;
	private float yOrigin;
	// used to track the available squares for the player to move to
	Set<Vector2> movementRange;
	
	private float stateTime;

	@Override
	public void create() {
		TextureRegion player0 = new TextureRegion(new Texture(Gdx.files.internal("Player0.png")));
		TextureRegion player1 = new TextureRegion(new Texture(Gdx.files.internal("Player1.png")));
		TextureRegion player2 = new TextureRegion(new Texture(Gdx.files.internal("Player2.png")));
		TextureRegion npc0 = new TextureRegion(new Texture(Gdx.files.internal("Platina0.png")));
		TextureRegion npc1 = new TextureRegion(new Texture(Gdx.files.internal("Platina1.png")));
		TextureRegion[] playerFrames = new TextureRegion[] {player0, player1, player0, player2};
		TextureRegion[] npcFrames = new TextureRegion[] {npc0, npc1};
		playerAnimation = new Animation<TextureRegion>(0.5f, playerFrames);
		npcAnimation = new Animation<TextureRegion>(1f, npcFrames);
		atlas = new TextureAtlas(Gdx.files.internal("Biomes/Lava/Tileset.atlas"));
		template = createTemplate(atlas);

		/*testBoard = new String[][] {{"wall1","floor1","floor4","floor4","pit11","pit13","floor1","floor7","pit13","pit13"},
									{"wall1","floor1","floor4","floor4","pit11","pit13","floor0","floor6","pit13","pit13"},
									{"wall1","floor1","floor4","floor4","pit10","pit22","pit13","pit13","pit13","pit13"},
									{"wall1","floor1","floor4","floor4","floor4","pit10","pit12","pit12","pit12","pit12"},
									{"wall1","floor1","floor4","floor4","floor4","floor4","floor4","floor4","floor4","floor4"},
									{"wall1","floor1","floor4","floor4","floor4","floor4","floor4","floor4","floor4","floor4"},
									{"wall1","floor1","floor4","floor4","floor4","floor4","floor4","floor4","floor4","floor4"},
									{"wall1","floor0","floor3","floor3","floor3","floor3","floor3","floor3","floor3","floor3"},
									{"wall1","wall9","wall9","wall9","wall9","wall9","wall9","wall9","wall9","wall9"},
									{"wall0", "wall3","wall3","wall3","wall3","wall3","wall3","wall3","wall3","wall3"}};*/
		
		testBoard = new String[MAX_COLS][MAX_ROWS];
		for(int col = 0; col < MAX_COLS; col++) {
			for(int row = 0; row < MAX_ROWS; row++) {
				testBoard[col][row] = "floor4";
			}
		}
		int numPools = 0;
		while(numPools < 50) {
			if(addPool(testBoard)) {
				numPools++;
			}
		}
		int numWalls = 0;
		while(numWalls < 10) {
			if(addWall(testBoard)) {
				numWalls++;
			}
		}
		
		player = new Player(playerAnimation, 0, 0, "player");
		npc = new NPC(npcAnimation, 1, 1, "npc");
		widgets = new HashSet<Widget>();
		for(int row = 0; row < MAX_ROWS; row++) {
			for(int col = 0; col < MAX_COLS; col++) {
				cells[col][row] = template.get(testBoard[row][col]).clone();
			}
		}
		cells[0][0].setActor(player);
		cells[1][1].setActor(npc);
		camera = new OrthographicCamera();
		viewport = new FitViewport(20, 20, camera);
		batch = new SpriteBatch();
		state = State.WAITING;
		touchPos = new Vector3(); // could this be a local variable instead?
		xDiff = 0;
		yDiff = 0;
		xOrigin = 0;
		yOrigin = 0;
		movementRange = new HashSet<Vector2>();
		stateTime = 0;
	}

	@Override
	public void render() {
		Gdx.gl.glClearColor(1, 0, 0, 1);
     	Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

     	camera.update();
     	update(Gdx.graphics.getDeltaTime());
		//camera.position.set(player.getX(), player.getY(), 0);
		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		// this loop will be deprecated as it draws ALL the cells in the grid; for now it is fine
		// it uses [row][col] to access the board because of the way arrays are stored vs the way I visualize a matrix
		// i.e. in {{0, 0, 0,},
		//          {1, 1, 1}} the "rows" are actually columns drawn from bottom (left) to top (right)
		if(state == State.MOVING) {
			Vector2 coords = new Vector2();
			for(int row = 0; row < MAX_ROWS; row++) {
				for(int col = 0; col < MAX_COLS; col++) {
					if(movementRange.contains(coords.set(col, row))) {
						batch.setColor(Color.SKY);
						batch.draw(cells[col][row].getTexture(stateTime), (float) col, (float) row, 1, 1);
					}
					else {
						batch.setColor(Color.WHITE);
						batch.draw(cells[col][row].getTexture(stateTime), (float) col, (float) row, 1, 1);
					}
				}
			}
			batch.setColor(Color.WHITE);
		}
		else for(int row = 0; row < MAX_ROWS; row++) {
			for(int col = 0; col < MAX_COLS; col++) {
				batch.draw(cells[col][row].getTexture(stateTime), (float) col, (float) row, 1, 1);
				//batch.draw(template.get(testBoard[row][col]).getTexture(stateTime), (float) col, (float) row, 1, 1);
			}
		}
		batch.draw(player.getTexture(stateTime), player.getX(), player.getY(), 1, 1);
		batch.draw(npc.getTexture(stateTime), npc.getX(), npc.getY(), 1, 1);
		for(Widget w : widgets) {
			w.draw(batch);
		}
		batch.end();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
		camera.position.set(player.getX(), player.getY(), 0);
	}

	public void update(float dt) {
		if(!widgets.isEmpty()) {
			Set<Widget> temp = new HashSet<Widget>();
			Iterator<Widget> itr = widgets.iterator();
			while(itr.hasNext()) {
				Widget w = itr.next();
				w.update(dt);
				if(w.isFinished()) {
					if(w.hasNext()) {
						temp.add(w.nextWidget());
					}
					itr.remove();
				}
			}
			widgets.addAll(temp);
		}
		stateTime += dt;
		if(state == State.WAITING && widgets.isEmpty()) {
			if(Gdx.input.isTouched()) {
				touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				camera.unproject(touchPos, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());

				// if the touch position is within the player's current location (a 1x1 square)
				if(touchPos.x >= player.getX() && touchPos.x < player.getX() + 1
				&& touchPos.y >= player.getY() && touchPos.y < player.getY() + 1) {
					state = State.MOVING;
					xOrigin = player.getX();
					yOrigin = player.getY();
					xDiff = touchPos.x - player.getX();
					yDiff = touchPos.y - player.getY();
					
					// set up the movement grid
					int range = player.getRange();
					movementRange.clear();
					for(int col =  (int) xOrigin - range; col <= (int) xOrigin + range; col++) {
						int yRange = Math.abs(Math.abs(col - (int) xOrigin) - range);
						for(int row = (int) yOrigin - yRange; row <= (int) yOrigin + yRange; row++) {
							if(col >= 0 && row >= 0 && col < MAX_COLS && row < MAX_ROWS)
								if(cells[col][row].isPassable())
							movementRange.add(new Vector2((float) col, (float) row));
							
						}
					}
				}
			}
		}
		else if(state == State.MOVING) {
			if(Gdx.input.isTouched()) {
				touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
				camera.unproject(touchPos, viewport.getScreenX(), viewport.getScreenY(), viewport.getScreenWidth(), viewport.getScreenHeight());
				player.setX(touchPos.x - xDiff);
				player.setY(touchPos.y - yDiff);
				batch.begin();
				batch.end();
			}
			else {
				// set X and Y coordinates to the nearest whole number
				Vector2 coords = new Vector2(Math.round(player.getX()), Math.round(player.getY()));

				// if x/y coords are out of bounds (movement range OR entire grid range) then
				// send player back to origin. later, we will smoothly snap them back over several frames of animation 
				// but for now, we'll just set the values to their previous state (xOrigin, yOrigin)
				if(!movementRange.contains(coords)) {
					player.setX(xOrigin);
					player.setY(yOrigin);
				}
				else { 
					player.setX(coords.x);
					player.setY(coords.y);
					cells[(int) xOrigin][(int) yOrigin].removeActor();
					cells[(int) player.getX()][(int) player.getY()].setActor(player);
				}
				camera.position.set(player.getX(), player.getY(), 0);
				state = State.WAITING;
			}
		}
	}
	
	public Map<String, Cell> createTemplate(TextureAtlas atlas) {
		// ATLAS MUST BE FORMATTED CORRECTLY. This includes the following regions:
		// Floor (7x3), Wall (6x3), Pit1A (3x2), Pit1B (4x2), Pit2A (3x2), Pit2B (4x2)
		
		// local variables that are used to split the AtlasRegions
		// even though they're only used once each, that may change, so I'm 
		// putting them here rather than use "magic numbers"
		int floorWidth = 7;
		int floorHeight = 3;
		int wallWidth = 6;
		int wallHeight = 3;
		int pit1Width = 3;
		int pit1Height = 2;
		int pit2Width = 4;
		int pit2Height = 2;
		
		TextureRegion[][] floor = atlas.findRegion("Floor").split(16, 16);
		TextureRegion[][] wall = atlas.findRegion("Wall").split(16, 16);
		TextureRegion[][] pit1a = atlas.findRegion("Pit1A").split(16, 16);
		TextureRegion[][] pit1b = atlas.findRegion("Pit1B").split(16, 16);
		TextureRegion[][] pit2a = atlas.findRegion("Pit2A").split(16, 16);
		TextureRegion[][] pit2b = atlas.findRegion("Pit2B").split(16, 16);
		
		Map<String, Cell> rVal = new HashMap<>();
		ArrayList<Animation<TextureRegion>> pit1Animations = new ArrayList<>();
		ArrayList<Animation<TextureRegion>> pit2Animations = new ArrayList<>();
		
		for(int row = 0; row < pit1Width; row++) {
			for(int col = 0; col < pit1Height; col++) {
				pit1Animations.add(new Animation<TextureRegion>(1.0f, new TextureRegion[] {pit1a[col][row], pit1b[col][row]}));
			}
		}
		
		for(int row = 0; row < pit2Width; row++) {
			for(int col = 0; col < pit2Height; col++) {
				pit2Animations.add(new Animation<TextureRegion>(1.0f, new TextureRegion[] {pit2a[col][row], pit2b[col][row]}));
			}
		}
		
		for(int row = 0; row < floorWidth; row++) {
			for(int col = 0; col < floorHeight; col++) {
				rVal.put("floor" + (row*floorHeight + col), new StaticCell(null, null, true, floor[col][row]));
			}
		}
		
		for(int row = 0; row < wallWidth; row++) {
			for(int col = 0; col < wallHeight; col++) {
				rVal.put("wall" + (row*wallHeight + col), new StaticCell(null, null, false, wall[col][row]));
			}
		}
		
		for(int row = 0; row < pit1Width; row++) {
			for(int col = 0; col < pit1Height; col++) {
				rVal.put("pit1" + (row*pit1Height + col), new AnimatedCell(null, null, false, pit1Animations.get(row*pit1Height+col)));
			}
		}
		
		for(int row = 0; row < pit2Width; row++) {
			for(int col = 0; col < pit2Height; col++) {
				rVal.put("pit2" + (row*pit2Height + col), new AnimatedCell(null, null, false, pit2Animations.get(row*pit1Height+col)));
			}
		}
		
		return rVal;
	}
	
	public boolean addPool(String[][] board) {
		// pick two ints between 5 ~ 10 for width and height
		int width = ThreadLocalRandom.current().nextInt(5, 11);
		int height = ThreadLocalRandom.current().nextInt(5, 11);
		// pick two ints for the beginning coordinates
		int x = ThreadLocalRandom.current().nextInt(0, MAX_COLS - width - 1);
		int y = ThreadLocalRandom.current().nextInt(0, MAX_ROWS - height - 1);
		
		boolean successful = true;
		//check if the area is clear
		for(int col = x; col < x + width; col++) {
			for(int row = y; row < y + height; row++) {
				if(!(board[row][col] == "floor4")) {
					successful = false;
				}
			}
		}
		if(successful) {
			board[y+height-1][x] = "pit10";
			board[y+height-1][x+width-1] = "pit14";
			for(int row = y; row < y+height-1; row++) {
				board[row][x] = "pit11";
				board[row][x+width-1] = "pit15";
			}
			for(int col = x+1; col < x+width-1; col++) {
				board[y+height-1][col] = "pit12";
			}
			for(int col = x+1; col < x+width-1; col++) {
				for(int row = y; row < y+height-1; row++) {
					board[row][col] = "pit13";
				}
			}
		}
		
		return successful;
	}
	
	public boolean addWall(String[][] board) {
		boolean successful = true;
		
		// pick one int for length, one boolean for vertical/horizontal
		int length = ThreadLocalRandom.current().nextInt(4, 15);
		boolean horizontal = ThreadLocalRandom.current().nextBoolean();
	
		
		// pick two ints for beginning coordinates
		int x, y;
		if(horizontal) {
			x = ThreadLocalRandom.current().nextInt(0, MAX_COLS - length - 1);
			y = ThreadLocalRandom.current().nextInt(0, MAX_ROWS - 1);
		}
		else {
			x = ThreadLocalRandom.current().nextInt(0, MAX_COLS - 1);
			y = ThreadLocalRandom.current().nextInt(0, MAX_ROWS - length - 1);
		}
		
		if(horizontal) {
			for(int col = x; col < x + length; col++) {
				if(!(board[y][col].contains("wall") || board[y][col].contains("floor"))) {
					successful = false;
				}
			}
		}
		else {
			for(int row = y; row < y + length; row++) {
				if(!(board[row][x].contains("wall") || board[row][x].contains("floor"))) {
					successful = false;
				}
			}
		}

		if(successful) {
			if(horizontal) {
				for(int col = x; col < x + length; col++) {
					if(board[y][col].contains("wall")) {
						board[y][col] = "wall13";
					}
					else {
						board[y][col] = "wall3";
					}
				}
			}
			else {
				for(int row = y; row < y + length; row++) {
					if(board[row][x].contains("wall")) {
						board[row][x] = "wall13";
					}
					else {
						board[row][x] = "wall1";
					}
				}
			}
		}
	
	
		return successful;
	}
}

// playing with git part 4