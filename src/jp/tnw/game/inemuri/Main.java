package jp.tnw.game.inemuri;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferStrategy;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Main {

	final static int winW = 1260; // Window width
	final static int winH = 720; // Window height
	BufferStrategy bs; // Graphic buffer
	Insets insets; // Window frame info
	JFrame window = new JFrame("GazerS"); // Game window
	Timer timer = new Timer(); // For main loop timer task

	// Core parts
	GameManager gm = new GameManager();
	GameGraphics gg = new GameGraphics();
	GameInput input = new GameInput();

	Main() {
		// Setup window and graphics
		window.setIgnoreRepaint(true);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		window.setVisible(true);
		window.setBackground(new Color(16, 16, 32));
		insets = window.getInsets();
		int sizeW = winW + insets.left + insets.right;
		int sizeH = winH + insets.top + insets.bottom;
		window.setSize(sizeW, sizeH);
		window.setLocationRelativeTo(null);
		window.createBufferStrategy(2);
		bs = window.getBufferStrategy();
		
		// Setup input
		window.addMouseListener(input);
		
		// Setup eye block data
		gg.setTiles(gm.getTilesData());

		// Run timer task nearly 60fps
		timer.schedule(new MainLoop(), 17, 17);
	}

	class MainLoop extends TimerTask {
		public void run() {

			// Main update
			input.update(window);
			gm.update();

			Graphics g = bs.getDrawGraphics();
			Graphics2D graphicLayer = (Graphics2D) g;

			if (bs.contentsLost() == false) {
				// Fit window's frame
				graphicLayer.translate(insets.left, insets.top);
				graphicLayer.clearRect(0, 0, winW, winH);

				// Main graphic
				gg.update(graphicLayer);

				// Draw from buffer
				bs.show();
				graphicLayer.dispose();
			}
		}
	}

	// Entry of program
	public static void main(String[] args) {
		// Anti buffer bug
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				Main Game = new Main();
			}
		});
	}
}
