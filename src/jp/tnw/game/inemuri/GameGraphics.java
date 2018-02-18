package jp.tnw.game.inemuri;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.function.Predicate;

// Graphics management
public class GameGraphics {

	private int mtr = 0; // Main timer
	private double atr = 0; // Angel timer
	public static boolean Reset = false; // For gameover return to title
	private static boolean Ready = false; // Tell manager animation is end
	final private String TITLE_NAME = "GazerS";

	final private int MAX = 10000; // Max number of effect particle
	private int gsscene = 0; // Animation phase
	private double gscamerax = 0; // All particle's position shift x
	private double gscameray = 0; // All particle's position shift y
	private int[] titleCharY = new int[TITLE_NAME.length()]; // Title text
																// motion

	// Particle pool
	private int[] pFlag = new int[MAX];
	private int[] pLife = new int[MAX];
	private int[] pMaxlife = new int[MAX];
	private float[] pOpacity = new float[MAX];
	private double[] pX = new double[MAX];
	private double[] pY = new double[MAX];
	private double[] pSpdX = new double[MAX];
	private double[] pSpdY = new double[MAX];
	private double[] pAccX = new double[MAX];
	private double[] pAccY = new double[MAX];

	private ArrayList<GameEyes> tiles; // Eye objects list
	private Color bgColor = new Color(16, 16, 32); // Background color
	private Font f = new Font("Default", Font.PLAIN, 14);
	private Font titletext = new Font("Default", Font.BOLD, 100);
	private Font smalltext = new Font("Default", Font.BOLD, 20);
	private Random r = new Random();

	GameGraphics() {
		Arrays.fill(titleCharY, 2000);
	}

	void update(Graphics2D g) {
		if (Reset) {
			resetTitle();
		}
		// Particle and timer update
		updateParticle();
		g.setColor(bgColor);
		g.fillRect(0, 0, Main.winW, Main.winH);

		switch (GameManager.phase()) {
		case 0:
			drawOpening(g);
			break;
		case 1:
			if (Ready) {
				for (int i = 0; i < MAX; i++) {
					kill(i);
				}
				Ready = false;
			}
		case 2:
			drawMainGame(g);
			break;
		case 3:
			drawStageClear(g);
			break;
		case 4:
			drawMainGame(g);
			drawGameOver(g);
			break;
		}

		// Debug info
		drawDebug(g);
	}

	// Particle action
	private void updateParticle() {
		for (int i = 0; i < MAX; i++) {
			if (pMaxlife[i] != -1 && pLife[i] > pMaxlife[i]) {
				kill(i);
				continue;
			}
			if (pFlag[i] > 0) {
				pSpdX[i] += pAccX[i];
				pSpdY[i] += pAccY[i];
				pX[i] += pSpdX[i] - gscamerax;
				pY[i] += pSpdY[i] - gscameray;
				pLife[i]++;
			}
		}
		// Timers
		mtr += 1;
		atr += 0.2;
	}

	// Title scene
	private void drawOpening(Graphics2D g) {
		switch (gsscene) {
		case 0:
			for (int i = 0; i < MAX; i++) {
				pOpacity[i] = 1f;
			}
			// Numbers of star
			int count = 1500;
			while (count > 0) {
				count--;
				int i = findIdle();
				pFlag[i] = 1;
				pX[i] = r.nextInt(1260);
				pY[i] = r.nextInt(2000);
				pOpacity[i] = r.nextFloat();
				pMaxlife[i] = -1;
			}
			gsscene++;
			break;
		case 1:
			if (mtr > 600) {
				// Title text animation
				titleAnimation(g, false);
				g.setFont(f);
			}
			if (mtr > 300 && mtr < 700) {
				gscameray += 0.01;
			} else if (mtr > 700 && gscameray > 0) {
				gscameray -= 0.02;
			} else if (mtr > 800 && gscameray <= 0) {
				gsscene++;
			}
			break;
		case 2:
			Ready = true;
			titleAnimation(g, true);
			break;
		}

		titleStar(g);

	}

	// Title background
	private void titleStar(Graphics2D g) {
		for (int i = 0; i < MAX; i++) {
			if (pFlag[i] > 0) {
				switch (pFlag[i]) {
				case 1:
					float val = mtr - 60 < 0 ? 0f
							: (mtr - 60) / 240f > 1f ? pOpacity[i] : (mtr - 60) / 240f * pOpacity[i];
					if ((Math.toRadians(i)) % Math.PI < Math.PI / 2) {
						val *= (Math.sin(atr / 6 + Math.toRadians(i)) + 1) / 2;
					}
					g.setColor(Color.WHITE);
					g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, val)));
					if ((Math.toRadians(i)) % Math.PI < Math.PI / 2) {
						g.fillRect((int) pX[i], (int) pY[i], 3, 3);
					} else {
						g.fillRect((int) pX[i], (int) pY[i], 2, 2);
					}
					g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f)));
					break;
				case 2:
					break;
				}
			}
		}
	}

	// Title text
	private void titleAnimation(Graphics2D g, boolean over) {
		char[] titlechars = TITLE_NAME.toCharArray();
		g.setFont(titletext);
		FontMetrics fm = g.getFontMetrics();
		g.setColor(Color.WHITE);
		for (int i = 0; i < titlechars.length; i++) {
			int titlewidth = fm.stringWidth(TITLE_NAME);
			int charpos = fm.stringWidth(TITLE_NAME.substring(0, i));
			int basex = Main.winW / 2 - titlewidth / 2 + charpos;
			int basey = (int) (Math.sin((double) (mtr + i * 20) / 30) * 15);
			float val = 0.3f + (float) Math.sin((float) mtr / 20) * 0.3f;
			if (over) {
				titleCharY[i] = 300;
				g.setFont(smalltext);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, val));
				g.drawString("Click to Start", 580, 440);
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				g.setFont(titletext);
			} else {
				titleCharY[i] += (double) (300 - titleCharY[i])
						/ (5 + Math.abs(((titlechars.length - 1) * -10) / 2 + i * 10));
			}
			g.drawString("" + titlechars[i], basex, titleCharY[i] + basey);
		}
	}

	// Main game scene
	private void drawMainGame(Graphics2D g) {

		// Eye start
		tiles.stream().filter(t -> t.isAlive()).forEach(t -> {
			// Motion params
			int x = (int) t.getX();
			int y = (int) t.getY();
			int cx = x + 30;
			int cy = y + 30;
			int sy = y + (int) (Math.sin((atr / 5) + t.getIndex()) * 4);
			int size = (int) (Math.sin(atr / 5 + t.getIndex()) * 4 - 2);
			int marked = (int) (Math.sin(atr) * 3 - 1);
			double angle = Math.atan2(GameInput.MY - cy, GameInput.MX - cx);
			int px = (int) (Math.cos(angle) * 10);
			int py = (int) (Math.sin(angle) * 10);
			float co = (float) (Math.sin(atr));

			// Setup eyes color
			Color c1 = null;
			Color c2 = null;
			Color c3 = null;
			switch (t.getIndex()) {
			case 0:
				c1 = new Color(244, 192, 0);
				c2 = new Color(160, 128, 0);
				c3 = new Color(64, 64, 0);
				break;
			case 1:
				c1 = new Color(244, 0, 64);
				c2 = new Color(160, 0, 32);
				c3 = new Color(64, 0, 16);
				break;
			case 2:
				c1 = new Color(64, 160, 244);
				c2 = new Color(32, 128, 160);
				c3 = new Color(16, 32, 64);
				break;
			case 3:
				c1 = new Color(128, 244, 64);
				c2 = new Color(64, 160, 32);
				c3 = new Color(32, 64, 16);
				break;
			case 4:
				c1 = new Color(192, 0, 244);
				c2 = new Color(128, 0, 160);
				c3 = new Color(64, 0, 64);
				break;
			case 5:
				c1 = new Color(0, 192, 244);
				c2 = new Color(0, 128, 160);
				c3 = new Color(0, 64, 96);
				break;
			}

			// Eyes acting
			if (GameManager.phase() == 4) {
				// Gameover red eyes
				c1 = new Color(64, 0, 0);
				c2 = new Color(64, 0, 0);
				c3 = new Color(255, 0, 0);
			}
			if (t.isFlashing()) {
				g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f + (co + 1) / 4)));
				g.setColor(c1);
				g.fillOval(x + 6 + marked, y + 6 + marked, 48 - marked * 2, 48 - marked * 2);
				g.setColor(c2);
				g.fillArc(x + 10, y + 10, 40, 40, 0 + mtr, 180);
				g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)));
				g.setColor(c3);
				if (GameManager.phase() != 4) {
					g.fillArc(x + 15 + marked / 2, y + 15 + marked / 2, 30 - marked, 30 - marked, 0, 360);
					g.setColor(new Color(255, 255, 255));
					g.fillOval(x + 10 + marked, y + 10 + marked, 16, 16);
					g.fillOval(x + 22 + marked, y + 22 + marked, 6, 6);
				} else {
					g.fillArc(x + 15 + marked / 2 + r.nextInt(4) - 2, y + 15 + marked / 2 + r.nextInt(4) - 2,
							30 - marked, 30 - marked, 0, 360);
				}
			} else {
				g.setColor(new Color(32, 32, 32));
				g.fillOval(x + 1, sy + 1, 58, 58);
				g.setColor(new Color(16, 16, 16));
				g.fillOval(x + 2, sy + 2, 56, 56);
				g.setColor(new Color(8, 8, 8));
				g.fillOval(x + 3, sy + 3, 54, 54);
				g.setColor(c1);
				g.fillOval(x + 4, sy + 4, 52, 52);
				g.setColor(c2);
				g.fillArc(x + 10, sy + 10, 40, 40, 0 + mtr, 180);
				g.setColor(c3);
				g.fillArc(x + 20 + size + px, sy + 15 + py, 20 - size * 2, 30, 0, 360);
				if (GameManager.phase() != 4) {
					g.setColor(new Color(255, 255, 255));
					g.fillOval(x + 10, sy + 10, 16, 16);
					g.fillOval(x + 22, sy + 22, 6, 6);
				} else {
					g.fillArc(x + 20 + size + px + r.nextInt(4) - 2, sy + 15 + py + r.nextInt(4) - 2, 20 - size * 2, 30,
							0, 360);
				}
			}
		});
		// Eye end

		// Mouse
		drawMouseEffects(g);
	}

	// Stage clear scene
	private void drawStageClear(Graphics2D g) {
		Color[] cs = { Color.yellow, Color.red, Color.cyan, Color.green, Color.magenta };
		String cleartext = (GameManager.colors() + "Colors Clear!!!");
		g.setFont(titletext);
		FontMetrics fm = g.getFontMetrics();
		for (int i = 0; i < cleartext.length(); i++) {
			int titlewidth = fm.stringWidth(cleartext);
			int charpos = fm.stringWidth(cleartext.substring(0, i));
			int basex = 1260 / 2 - titlewidth / 2 + charpos;
			int basey = (int) (Math.sin((double) (mtr + i * 20) / 30) * 15);
			// Set Multi colors
			Color c = Color.white;
			if (i <= GameManager.colors() - 1 && i <= 4) {
				c = cs[i];
			}
			g.setColor(c);
			g.drawString("" + cleartext.toCharArray()[i], basex, 400 + basey);
		}
		g.setFont(f);
		Ready = true;
	}

	// Gameover scene
	private void drawGameOver(Graphics2D g) {
		if (bgColor.getRed() < 100) {
			bgColor = new Color(bgColor.getRed() + 1, bgColor.getGreen(), bgColor.getBlue());
		} else {
			Ready = true;
		}
		String gameovertext = "YOU DIED";
		g.setFont(titletext);
		g.setColor(Color.red);
		FontMetrics fm = g.getFontMetrics();
		for (int i = 0; i < gameovertext.length(); i++) {
			int titlewidth = fm.stringWidth(gameovertext);
			int charpos = fm.stringWidth(gameovertext.substring(0, i));
			int basex = 1260 / 2 - titlewidth / 2 + charpos + r.nextInt(3) - 6;
			int basey = 400 + r.nextInt(3) - 6;
			g.drawString("" + gameovertext.toCharArray()[i], basex, basey);
		}
		g.setFont(f);
	}

	// Mouse click particle
	private void drawMouseEffects(Graphics2D g) {
		if (mtr % 2 == 0 && GameInput.pressing()) {
			int n = findIdle();
			pX[n] = GameInput.MX;
			pY[n] = GameInput.MY;
			pSpdX[n] = r.nextDouble() * 5 - 2;
			pSpdY[n] = r.nextDouble() * 5 - 2;
			pFlag[n] = 2;
			pMaxlife[n] = 50;
		}
		for (int i = 0; i < MAX; i++) {
			if (pFlag[i] == 2) {
				pOpacity[i] = pLife[i] < 50 ? 1.0f - pLife[i] / 50f : 0f;
				g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, pOpacity[i])));
				g.setColor(Color.BLACK);
				g.fillRect((int) pX[i] - 1, (int) pY[i] - 1, 5, 5);
				g.setColor(Color.WHITE);
				g.fillRect((int) pX[i], (int) pY[i], 3, 3);
				g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)));
			}
		}
	}

	// Debug info
	private void drawDebug(Graphics2D g) {

		int mx = GameInput.MX / 60;
		int my = GameInput.MY / 60;
		g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f)));
		g.setColor(Color.black);
		g.fillRect(1025, 5, 220, 240);
		g.setComposite((AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f)));
		g.setFont(f);
		drawStrokeString(g, mx + "," + my, 1030, 30);
		drawStrokeString(g, "PHASE : " + GameManager.phase(), 1080, 30);
		tiles.forEach(t -> {
			drawStrokeString(g, t.getIndex() >= 0 ? isAlone(t) ? "A" : t.getIndex() + "" : " " + " ",
					1030 + t.getBX() * 10, 50 + t.getBY() * 16);
		});
		//g.setFont(f);
		//int alive = (int) Arrays.stream(pFlag).filter(i -> i > 0).count();
		//drawStrokeString(g, "P_Alive : " + alive, 10, 20);
		//drawStrokeString(g, "MainTimer : " + mtr, 10, 35);
		//drawStrokeString(g, "Animation : " + gsscene, 10, 50);
		//drawStrokeString(g, "GameScene : " + GameManager.phase(), 10, 65);
	}

	// For debug
	private boolean isAlone(GameEyes t) {
		Predicate<GameEyes> isNeighbour = (tt -> Math.abs(tt.getBX() - t.getBX())
				+ Math.abs(tt.getBY() - t.getBY()) == 1);
		Predicate<GameEyes> fitColor = (tt -> tt.getIndex() == t.getIndex() && tt.isAlive());
		return tiles.stream().noneMatch(isNeighbour.and(fitColor));
	}

	// Stroke text
	private void drawStrokeString(Graphics2D g, String string, int i, int j) {
		g.setColor(Color.BLACK);
		g.drawString(string, i - 1, j - 1);
		g.drawString(string, i - 1, j + 1);
		g.drawString(string, i + 1, j - 1);
		g.drawString(string, i + 1, j + 1);
		g.setColor(Color.WHITE);
		g.drawString(string, i, j);
	}

	// Animation is over or not
	static boolean ready() {
		return Ready;
	}

	// Recive eye data from manager
	void setTiles(ArrayList<GameEyes> t) {
		tiles = t;
	}

	private void resetTitle() {
		mtr = 0;
		atr = 0;
		Ready = false;
		gsscene = 0;
		gscamerax = 0;
		gscameray = 0;
		Arrays.fill(titleCharY, 2000);
		Reset = false;
		bgColor = new Color(16, 16, 32);
		return;
	}

	// Following is about practile
	// Find an idle practile
	private int findIdle() {
		for (int i = 0; i < MAX; i++) {
			if (pFlag[i] == 0) {
				return i;
			}
		}
		return 0;
	}

	// Kill a practile
	private void kill(int i) {
		pX[i] = 0;
		pY[i] = 0;
		pSpdX[i] = 0;
		pSpdY[i] = 0;
		pAccX[i] = 0;
		pAccY[i] = 0;
		pFlag[i] = 0;
		pOpacity[i] = 1f;
		pLife[i] = 0;
		pMaxlife[i] = 0;
	}

	// Practile is out of window
	private boolean outBorder(int i, int addlimit) {
		return pX[i] > 1260 + addlimit || pX[i] < 0 - addlimit || pY[i] > 720 + addlimit || pY[i] < 0 - addlimit;
	}

}
