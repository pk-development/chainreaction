package de.maikmerten.chainreaction;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Field {

	private final int width, height;
	private final ArrayList<FieldListener> listeners = new ArrayList<FieldListener>();
	private final List<List<Cell>> rows;

	public Field(int width, int height) {
		this.width = width;
		this.height = height;
		rows = new ArrayList<List<Cell>>(height);
		for (int y = 0; y < height; y++) {
			List<Cell> row = new ArrayList<Cell>(width);
			for (int x = 0; x < width; x++) {
				row.add(x, new Cell());
			}
			rows.add(row);
		}
	}

	public Field getCopy() {
		Field copy = new Field(getWidth(), getHeight());
		for (int x = 0; x < getWidth(); ++x) {
			for (int y = 0; y < getHeight(); ++y) {
				Cell orginalCell = getCellAtPosition(x, y);
				Cell newCell = new Cell(orginalCell.getNumberOfAtoms(), orginalCell.getOwningPlayer());
				copy.setCellAtPosition(newCell, x, y);
			}
		}
		return copy;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	private Cell getCellAtPosition(int x, int y) {
		List<Cell> row = rows.get(y);
		return row.get(x);
	}

	private void setCellAtPosition(Cell cell, int x, int y) {
		List<Cell> row = rows.get(y);
		row.set(x, cell);
	}

	byte getNumerOfAtomsAtPosition(int x, int y) {
		return getCellAtPosition(x, y).getNumberOfAtoms();
	}

	public Player getOwnerOfCellAtPosition(int x, int y) {
		return getCellAtPosition(x, y).getOwningPlayer();
	}

	public int getTotalNumberOfAtomsForPlayer(Player player) {
		int count = 0;

		for (int x = 0; x < getWidth(); ++x) {
			for (int y = 0; y < getHeight(); ++y) {
				if (getOwnerOfCellAtPosition(x, y) == player) {
					count += getNumerOfAtomsAtPosition(x, y);
				}
			}
		}

		return count;
	}
	
	public int getPlayerFields(Player player) {
		int count = 0;
		for (int x = 0; x < getWidth(); ++x) {
			for (int y = 0; y < getHeight(); ++y) {
				if (getOwnerOfCellAtPosition(x, y) == player) {
					++count;
				}
			}
		}

		return count;
	}
	

	public void putAtom(Player player, int x, int y) {
		putAtomInternal(player, x, y);
		if(getNumerOfAtomsAtPosition(x, y) == 1) {
			fireOnOwnerChange(player, x, y);
		}
		fireOnAtomAdded(player, x, y);
	}

	/**
	 * Increases the atom count of the cell identified by the x and y coordinates by one
	 * and alters the owner to the given {@link Player}.
	 *
	 * @param player
	 * 	The new owner of the specified cell.
	 * @param x
	 * 	X coordinate of the cell
	 * @param y
	 * 	Y coordinate of the cell
	 */
	private void putAtomInternal(Player player, int x, int y) {
		Cell cell = getCellAtPosition(x, y);
		cell.increaseNumberOfAtoms();
		cell.setOwningPlayer(player);
	}
	
	private void clearCellAtPosition(int x, int y) {
		getCellAtPosition(x, y).clear();
		fireOnCellCleared(x, y);
		fireOnOwnerChange(Player.NONE, x, y);
	}

	byte getCapacityOfCellAtPosition(int x, int y) {
		byte capacity = 3;

		boolean firstColumn = (x == 0);
		boolean lastColumn = (x == (width - 1));

		if (firstColumn || lastColumn) {
			--capacity;
		}

		boolean firstRow = (y == 0);
		boolean lastRow = (y == (height - 1));

		if (firstRow || lastRow) {
			--capacity;
		}

		return capacity;
	}

	public boolean isCritical(int x, int y) {
		return getNumerOfAtomsAtPosition(x, y) == getCapacityOfCellAtPosition(x, y);
	}

	private void spreadAtoms(int x, int y) {
		Player player = getOwnerOfCellAtPosition(x, y);
		final List<Move> moves = new LinkedList<Move>();
		// move left
		if (x > 0) {
			moveAtom(player, x, y, x - 1, y, moves);
		}
		// move right
		if (x < getWidth() - 1) {
			moveAtom(player, x, y, x + 1, y, moves);
		}
		// move up
		if (y > 0) {
			moveAtom(player, x, y, x, y - 1, moves);
		}
		// move down
		if (y < getHeight() - 1) {
			moveAtom(player, x, y, x, y + 1, moves);
		}
		fireOnAtomsMoved(moves);
		// clear cell
		clearCellAtPosition(x, y);
		
	}

	private void moveAtom(Player player, int x1, int y1, int x2, int y2, List<Move> moves) {
		if(getOwnerOfCellAtPosition(x2, y2) != player) {
			fireOnOwnerChange(player, x2, y2);
		}

		putAtomInternal(player, x2, y2);
		if (getCellAtPosition(x2, y2).getNumberOfAtoms() <= 4) {
			// move
			moves.add(new Move(x1, y1, x2, y2));
		}
	}

	public void react() {
		boolean stable = false;
		byte iter = 16;
		while (!stable && iter-- > 0) {
			stable = true;
			for (int x = 0; x < getWidth(); ++x) {
				for (int y = 0; y < getHeight(); ++y) {
					byte count = getNumerOfAtomsAtPosition(x, y);
					if (count > getCapacityOfCellAtPosition(x, y)) {
						stable = false;
						spreadAtoms(x, y);
					}
				}
			}
		}
	}
	
	public void addFieldListener(final FieldListener l) {
		this.listeners.add(l);
	}
	
	private void fireOnAtomAdded(Player player, int x, int y) {
		for(final FieldListener l : listeners) {
			l.onAtomAdded(player, x, y);
		}
	}
	
	private void fireOnAtomsMoved(List<Move> moves) {
		for(final FieldListener l : listeners) {
			l.onAtomsMoved(moves);
		}
	}
	
	private void fireOnCellCleared(int x, int y) {
		for(final FieldListener l : listeners) {
			l.onCellCleared(x, y);
		}
	}
	
	private void fireOnOwnerChange(Player player, int x, int y) {
		for(final FieldListener l : listeners) {
			l.onOwnerChanged(player, x, y);
		}
	}
}
