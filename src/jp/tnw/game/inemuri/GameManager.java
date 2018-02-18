package jp.tnw.game.inemuri;

import java.util.ArrayList;
import java.util.Random;
import java.util.function.Predicate;

// Game logic management
public class GameManager {

	final private int SIZE = 60; // Size of every block (pixel)
	final private int X_MAX = 21; // Max row
	final private int Y_MAX = 12; // Max column
	private static int COLORS = 2; // Numbers of color or block type
	private static int PHASE = 0; // Control game scenes
	private int startCntX, startCntY; // For entery animation
	private static boolean ACTIVE; // Free for input
	private ArrayList<GameEyes> tiles = new ArrayList<GameEyes>();
	private Random r = new Random();

	GameManager() {
		for (int yy = 0; yy < Y_MAX; yy++) {
			for (int xx = 0; xx < X_MAX; xx++) {
				GameEyes t = new GameEyes(xx, yy);
				tiles.add(t);
			}
		}
		startCntX = 0;
		startCntY = Y_MAX - 1;
	}

	void update() {
		switch (PHASE) {
		case 0:
			if (GameInput.clicked() && GameGraphics.ready()) {
				PHASE++;
			}
			break;
		case 1:
			startAnimationProcess();
			break;
		case 2:
			mainProcess();
			break;
		case 3:
			// Stage clear click
			if (GameInput.clicked() && GameGraphics.ready()) {
				ACTIVE = false;
				PHASE = 1;
				COLORS++;
			}
			break;
		case 4:
			// Gameover click
			if (GameInput.clicked() && GameGraphics.ready()) {
				ACTIVE = false;
				tiles.forEach(t -> t.reset());
				GameGraphics.Reset = true;
				PHASE = 0;
				COLORS = 2;
			}
			break;
		}
	}

	// Entery animation
	private void startAnimationProcess() {
		tiles.stream().filter(t -> !t.isAlive()).findFirst().ifPresent(t -> {
			int x = startCntX * SIZE;
			int y = startCntY * SIZE;
			t.birth(r.nextInt((X_MAX - 1) * SIZE), y - SIZE, x, y, r.nextInt(COLORS));
			startCntX++;
			if (startCntX == X_MAX) {
				startCntX = 0;
				startCntY--;
				if (startCntY < 0) {
					startCntY = Y_MAX - 1;
					// Game Start
					PHASE = 2;
				}
			}
		});
		tiles.forEach(t -> t.update());
	}
	
	private void mainProcess() {
		// Reset status and update tiles data for search
		int[][] tempdata = new int[Y_MAX][X_MAX];
		boolean dying = tiles.stream().peek(t -> {
			tempdata[t.getBY()][t.getBX()] = t.getIndex();
			t.setFlash(false);
		}).anyMatch(t -> t.getDyingCnt() > 0);
		if (dying) {
			// TODO : 爆炸效果
			tiles.forEach(t -> t.updateDeadAnime());
			return;
		} else {
			tiles.forEach(t -> t.update());
		}
		// When inputable
		ACTIVE = tiles.stream().noneMatch(t -> t.isMoving());
		if (ACTIVE && leftAligned()) {
			// Check game result
			if (tiles.stream().noneMatch(t -> t.isAlive())) {
				// Stage clear
				PHASE = 3;
				return;
			} else if (tiles.stream().filter(t -> t.isAlive()).allMatch(t -> isAlone(t))) {
				// Gameover
				PHASE = 4;
				return;
			}
			// Mouseover
			tiles.stream().filter(t -> t.isAlive() && GameInput.MX / 60 == t.getBX() && GameInput.MY / 60 == t.getBY())
					.findAny().ifPresent(t -> findSame(tempdata, t.getBX(), t.getBY(), t.getIndex()));
			int marked = (int) tiles.stream().filter(t -> tempdata[t.getBY()][t.getBX()] == 999)
					.peek(t -> t.setFlash(true)).count();
			// Mouseclick
			if (GameInput.clicked() && marked > 1) {
				killTiles();
			}
		}

	}

	// Kill selected eyes
	private void killTiles() {
		tiles.stream().filter(t -> t.isFlashing()).forEach(t -> {
			t.kill();
			tiles.stream().filter(tt -> tt.getBX() == t.getBX() && tt.getBY() < t.getBY())
					.forEach(tt -> tt.setBY(tt.getBY() + 1));
			t.setBY(0);
		});
		ACTIVE = false;
	}

	// Remove blanks
	private boolean leftAligned() {
		tiles.stream().filter(t -> !t.isAlive() && t.getBY() == Y_MAX - 1).forEach(t -> {
			int xx = t.getBX();
			if (tiles.stream().anyMatch(chk -> chk.getBX() >= xx && chk.isAlive())) {
				tiles.stream().filter(tt -> tt.getBX() == xx).forEach(samerow -> {
					samerow.setBX(X_MAX);
				});
				tiles.stream().filter(tt -> tt.getBX() > xx).forEach(allrow -> {
					allrow.setBX(allrow.getBX() - 1);
				});
				ACTIVE = false;
			}
		});
		return ACTIVE;
	}

	// Check an eye can match around or not
	private boolean isAlone(GameEyes t) {
		Predicate<GameEyes> isNeighbour = (tt -> Math.abs(tt.getBX() - t.getBX())
				+ Math.abs(tt.getBY() - t.getBY()) == 1);
		Predicate<GameEyes> fitColor = (tt -> tt.getIndex() == t.getIndex() && tt.isAlive());
		return tiles.stream().noneMatch(isNeighbour.and(fitColor));
	}

	// Recursion search
	private void findSame(int[][] data, int sx, int sy, int c) {
		if (sx < 0 || sy < 0 || sx > data[0].length - 1 || sy > data.length - 1) {
			return;
		} else {
			data[sy][sx] = 999;
			if (sx - 1 >= 0 && data[sy][sx - 1] == c) {
				findSame(data, sx - 1, sy, c);
			}
			if (sx + 1 < data[0].length && data[sy][sx + 1] == c) {
				findSame(data, sx + 1, sy, c);
			}
			if (sy - 1 >= 0 && data[sy - 1][sx] == c) {
				findSame(data, sx, sy - 1, c);
			}
			if (sy + 1 < data.length && data[sy + 1][sx] == c) {
				findSame(data, sx, sy + 1, c);
			}
		}
	}

	ArrayList<GameEyes> getTilesData() {
		return tiles;
	}

	static int phase() {
		return PHASE;
	}

	static boolean active() {
		return ACTIVE;
	}

	static int colors() {
		return COLORS;
	}
}