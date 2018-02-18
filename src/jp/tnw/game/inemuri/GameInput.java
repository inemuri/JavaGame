package jp.tnw.game.inemuri;

import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JFrame;

// Input management
public class GameInput implements MouseListener {

	static int MX, MY;
	private static boolean pressing;
	private static boolean mc;

	public void update(JFrame w) {
		MX = MouseInfo.getPointerInfo().getLocation().x - w.getLocationOnScreen().x - w.getInsets().left;
		MY = MouseInfo.getPointerInfo().getLocation().y - w.getLocationOnScreen().y - w.getInsets().top;
		if (GameManager.phase() == 1) {
			mc = false;
		}
	}

	public static boolean clicked() {
		boolean r = mc;
		mc = false;
		return r;
	}

	public static boolean pressing() {
		return pressing;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (GameManager.phase() != 2 || GameManager.active()) {
			mc = true;
		} else {
			mc = false;
		}
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		pressing = true;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		pressing = false;
	}

}
