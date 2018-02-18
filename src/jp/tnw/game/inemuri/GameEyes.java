package jp.tnw.game.inemuri;

// Eyes block data
public class GameEyes {

	final private int SIZE = 60; // Pixel size
	final private int SPD = 4; // Fall speed
	private int index = -1; // Color
	private int dying = 0; // Die animation counter
	private int blockX = -1; // X index in tiles
	private int blockY = -1; // Y index in tiles
	private double x = -SIZE; // Screen position X
	private double y = -SIZE; // Screen position Y
	private double targetx = -SIZE; // Movement target position X
	private double targety = -SIZE; // Movement target position Y
	private boolean flashing = false; // Selected

	GameEyes(int bx, int by) {
		blockX = bx;
		blockY = by;
	}

	void update() {
		if (dying == 0) {
			targetx = blockX * SIZE;
			targety = blockY * SIZE;
			x = Math.abs(x - targetx) < 1 ? targetx : x + (targetx - x) / SPD;
			y = Math.abs(y - targety) < 1 ? targety : y + (targety - y) / SPD;
		}
	}

	void updateDeadAnime() {
		dying = dying > 0 ? dying - 1 : 0;
	}

	// Request new eye
	void birth(int xx, int yy, int tx, int ty, int ii) {
		x = xx;
		y = yy;
		targetx = tx;
		targety = ty;
		blockX = tx / 60;
		blockY = ty / 60;
		index = ii;
	}

	void kill() {
		dying = 20;
		reset();
	}

	void reset() {
		index = -1;
		x = -SIZE;
		y = -SIZE;
		targetx = -SIZE;
		targety = -SIZE;
		flashing = false;
	}

	int getIndex() {
		return index;
	}

	void setIndex(int i) {
		index = i;
	}

	boolean isFlashing() {
		return flashing;
	}

	boolean isMoving() {
		return x != targetx || y != targety;
	}

	boolean isAlive() {
		return index >= 0;
	}

	void setFlash(boolean b) {
		flashing = b;
	}

	double getX() {
		return x;
	}

	double getY() {
		return y;
	}

	double getTargetX() {
		return targetx;
	}

	double getTargetY() {
		return targety;
	}

	void setBX(int bx) {
		blockX = bx;
	}

	void setBY(int by) {
		blockY = by;
	}

	int getBX() {
		return blockX;
	}

	int getBY() {
		return blockY;
	}

	int getDyingCnt() {
		return dying;
	}
}